#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""TigShop Rust 重构进度报告。

对 docs/slices.json 中每个切片，检查其 owned_files 是否已落地、
是否已有注册记录，并汇总契约覆盖率。

用法::
    python tools/status_report.py            # 汇总
    python tools/status_report.py --pending  # 列出未完成切片
    python tools/status_report.py --json     # 机器可读
"""
from __future__ import annotations

import argparse
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)
CRATES = os.path.join(RUST, "crates")
SLICES = os.path.join(RUST, "docs", "slices.json")
REGS = os.path.join(RUST, "docs", "registrations")


def main():
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass

    ap = argparse.ArgumentParser()
    ap.add_argument("--pending", action="store_true")
    ap.add_argument("--tier", default=None)
    ap.add_argument("--json", action="store_true")
    args = ap.parse_args()

    slices = json.load(open(SLICES, encoding="utf-8"))
    regs = set()
    if os.path.isdir(REGS):
        for fn in os.listdir(REGS):
            if fn.endswith(".json"):
                regs.add(fn[:-5])

    rows = []
    for s in slices:
        sid = s["slice_id"]                       # slice-001
        key = sid.replace("-", "")                # slice001
        files = s["owned_files"]
        # 服务层文件是每片必然产出的核心产物
        svc = "crates/services/src/%s.rs" % key
        has_svc = os.path.isfile(os.path.join(RUST, svc))
        present = sum(
            1 for f in files if os.path.isfile(os.path.join(RUST, f))
        )
        rows.append(
            {
                "slice_id": sid,
                "key": key,
                "tier": s.get("tier"),
                "tag": s.get("tag"),
                "api_count": s.get("api_count"),
                "files_total": len(files),
                "files_present": present,
                "has_service": has_svc,
                "registered": key in regs,
                "done": has_svc,
            }
        )

    done = [r for r in rows if r["done"]]
    pending = [r for r in rows if not r["done"]]

    if args.json:
        json.dump(
            {"total": len(rows), "done": len(done), "pending": pending},
            sys.stdout,
            ensure_ascii=False,
            indent=1,
        )
        return 0

    print("=" * 70)
    print("TigShop Rust 重构进度")
    print("=" * 70)
    print("切片总数        : %d" % len(rows))
    print("已实现(有服务层): %d" % len(done))
    print("未实现          : %d" % len(pending))
    print("已注册          : %d" % len([r for r in rows if r["registered"]]))
    print()
    for tier in ("P0", "P1", "P2"):
        t = [r for r in rows if r["tier"] == tier]
        td = [r for r in t if r["done"]]
        apis = sum(r["api_count"] or 0 for r in t)
        apis_done = sum(r["api_count"] or 0 for r in td if r["done"])
        print(
            "  %-3s 切片 %3d/%-3d   接口 %4d/%-4d  %5.1f%%"
            % (
                tier,
                len(td),
                len(t),
                apis_done,
                apis,
                100.0 * apis_done / max(1, apis),
            )
        )

    if args.pending:
        sel = pending
        if args.tier:
            sel = [r for r in sel if r["tier"] == args.tier]
        print()
        print("未实现切片（%d）:" % len(sel))
        for r in sel:
            print(
                "  %-12s %-4s %-22s %3d apis  files=%d/%d  reg=%s"
                % (
                    r["slice_id"],
                    r["tier"],
                    (r["tag"] or "")[:22],
                    r["api_count"] or 0,
                    r["files_present"],
                    r["files_total"],
                    "Y" if r["registered"] else "n",
                )
            )
    return 0


if __name__ == "__main__":
    sys.exit(main())
