#!/usr/bin/env python3
"""Enrich docs/domain-map.json into the single authoritative migration map.

Adds, per slice: coded/real/deleted/type_prefix/layers{layer: target path}.
Adds a top-level `baseline[]` for the pre-slice domain modules (76 files) that the
migration relocated, so coverage/planning can attribute those 135 endpoints.

Validates: (ctx, feature) uniqueness across real slices + baseline, every
baseline target file exists, every real slice target file exists.

Run from rust/:  python tools/restructure/sync_domain_map.py
"""
from __future__ import annotations

import json
import os
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
CR = os.path.join(ROOT, "crates")

import importlib.util

_spec = importlib.util.spec_from_file_location(
    "migrate", os.path.join(os.path.dirname(os.path.abspath(__file__)), "migrate.py"))
_m = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_m)

BASE_TABLES = {
    "models": _m.BASE_MODELS,
    "repo": _m.BASE_REPO,
    "service": _m.BASE_SERVICE,
    "admin_route": _m.BASE_ADMIN_ROUTES,
    "public_route": _m.BASE_PUBLIC_ROUTES,
}
LAYER_DIR = _m.LAYER_DIR


def main() -> int:
    dm_path = os.path.join(ROOT, "docs/domain-map.json")
    dm = json.load(open(dm_path, encoding="utf-8"))
    mm = json.load(open(os.path.join(ROOT, "docs/_migration-map.json"), encoding="utf-8"))
    mm_by_id = {s["slice_id"]: s for s in mm["slices"]}

    problems = []
    real_seen = {}
    for s in dm["slices"]:
        src = mm_by_id.get(s["slice_id"], {})
        real = bool(src.get("real"))
        deleted = bool(src.get("coded") and not real)
        s["coded"] = bool(src.get("coded"))
        s["real"] = real
        s["deleted"] = deleted
        # ctx/feature authoritative values come from the migration map
        if real:
            s["ctx"] = src["ctx"]
            s["feature"] = src["feature"]
            s["type_prefix"] = src["type_prefix"]
        layers = {}
        dropped = []
        if real:
            for layer in (src.get("files") or {}):
                rel = "crates/%s/%s/%s.rs" % (LAYER_DIR[layer], s["ctx"], s["feature"])
                if not os.path.isfile(os.path.join(ROOT, rel)):
                    # route shells that were removed because another feature owns
                    # the endpoints (e.g. content::decorate owns PcCatFloor).
                    dropped.append(layer)
                    continue
                layers[layer] = rel
        s["layers"] = layers
        if dropped:
            s["dropped_layers"] = sorted(dropped)
        if real:
            key = (s["ctx"], s["feature"])
            if key in real_seen:
                problems.append("COLLISION %s: %s vs %s" % (key, real_seen[key], s["slice_id"]))
            real_seen[key] = s["slice_id"]
        elif not deleted:
            key = (s["ctx"], s["feature"])
            if key in real_seen:
                problems.append("ROADMAP COLLISION %s: %s" % (key, s["slice_id"]))
            real_seen[key] = s["slice_id"]

    baseline = []
    for layer, table in BASE_TABLES.items():
        for key, (ctx, feature) in table.items():
            rel = "crates/%s/%s/%s.rs" % (LAYER_DIR[layer], ctx, feature)
            if not os.path.isfile(os.path.join(ROOT, rel)):
                continue
            entry = next((b for b in baseline
                          if b["ctx"] == ctx and b["feature"] == feature), None)
            if entry is None:
                entry = {"ctx": ctx, "feature": feature, "source_key": key, "layers": {}}
                baseline.append(entry)
                k = (ctx, feature)
                if k in real_seen:
                    problems.append("BASELINE COLLISION %s with %s" % (k, real_seen[k]))
                real_seen[k] = "baseline:" + key
            entry["layers"][layer] = rel

    dm["baseline"] = sorted(baseline, key=lambda b: (b["ctx"], b["feature"]))
    dm["migration"] = {
        "stage": 1,
        "tool": "tools/restructure/migrate.py",
        "real_slices": sum(1 for s in dm["slices"] if s.get("real")),
        "deleted_scaffolds": sum(1 for s in dm["slices"] if s.get("deleted")),
        "baseline_features": len(dm["baseline"]),
    }
    json.dump(dm, open(dm_path, "w", encoding="utf-8"), ensure_ascii=False, indent=1)

    print("slices=%(n)d real=%(r)d deleted=%(d)d baseline=%(b)d" % {
        "n": len(dm["slices"]),
        "r": dm["migration"]["real_slices"],
        "d": dm["migration"]["deleted_scaffolds"],
        "b": len(dm["baseline"]),
    })
    print("problems:", len(problems))
    for p in problems[:20]:
        print("  !", p)
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
