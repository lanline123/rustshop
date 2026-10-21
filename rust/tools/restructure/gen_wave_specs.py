#!/usr/bin/env python3
"""Generate a per-wave feature spec file (exact contract endpoints + Java sources).

Usage:
    cd rust
    python tools/restructure/gen_wave_specs.py \
        --features trade/order_log,payment/pay,catalog/exchange \
        --out docs/_waveN-specs.json

For each `ctx/feature`, prints the still-unimplemented contract endpoints
(method, path, summary, auth, permissions, controller, handler, Java source:line)
so implementers never guess paths. Coverage/attribution logic is reused from
tools/check_api_coverage.py (the single authoritative route extractor).
"""
from __future__ import annotations

import argparse
import importlib.util
import json
import os
import sys
from collections import defaultdict

sys.stdout.reconfigure(encoding="utf-8", errors="replace")

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))


def load_cov():
    path = os.path.join(ROOT, "tools/check_api_coverage.py")
    spec = importlib.util.spec_from_file_location("cov", path)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--features", required=True,
                    help="comma separated ctx/feature pairs")
    ap.add_argument("--out", default="docs/_wave-specs.json")
    ap.add_argument("--include-implemented", action="store_true")
    args = ap.parse_args()

    cov = load_cov()
    contract = cov.load_contract(os.path.join(ROOT, "docs/api-contract.json"))
    routes = cov.collect_rust_routes(os.path.join(ROOT, "crates"))
    res = cov.analyse(contract, routes)
    missing = {(e["method"].upper(), e["path"]) for e in res["missing"]}

    dm = json.load(open(os.path.join(ROOT, "docs/domain-map.json"), encoding="utf-8"))
    ctrl2feat = {}
    for c in dm["controllers"]:
        ctrl2feat.setdefault(c["controller"], (c["ctx"], c["feature"]))

    wanted = set()
    for pair in args.features.split(","):
        pair = pair.strip()
        if not pair:
            continue
        if "/" not in pair:
            raise SystemExit("bad --features entry (expect ctx/feature): %s" % pair)
        wanted.add(tuple(pair.split("/", 1)))

    groups = defaultdict(list)
    for e in contract["endpoints"]:
        key = (e["method"].upper(), e["path"])
        if key not in missing and not args.include_implemented:
            continue
        cf = ctrl2feat.get(e["controller"])
        if cf in wanted:
            groups[cf].append(e)

    out = {}
    for ctx, feature in sorted(wanted):
        eps = groups.get((ctx, feature), [])
        out["%s/%s" % (ctx, feature)] = {
            "ctx": ctx,
            "feature": feature,
            "count": len(eps),
            "endpoints": [
                {
                    "method": e["method"],
                    "path": e["path"],
                    "summary": e.get("summary", ""),
                    "auth": e.get("auth"),
                    "permissions": e.get("permissions"),
                    "controller": e["controller"],
                    "handler": e["handler"],
                    "source": e["source"],
                }
                for e in eps
            ],
        }
        print("%-36s %3d" % ("%s/%s" % (ctx, feature), len(eps)))

    dst = os.path.join(ROOT, args.out)
    json.dump(out, open(dst, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print("wrote", os.path.relpath(dst, ROOT),
          "| implemented now: %d / %d" % (res["implemented"], res["total"]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
