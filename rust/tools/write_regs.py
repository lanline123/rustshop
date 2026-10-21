#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""为主会话写切片注册登记文件（docs/registrations/sliceNNN.json）。

为什么需要它：子代理只被允许改自己的独占文件，共享文件（AdminState /
routes/mod.rs / services/lib.rs / repo/mod.rs / main.rs）由主会话统一合并。
`register_slices.py` 只认 `has_admin` / `has_public` 两个布尔量，
这里按 docs/slices.json 的 modules 字段自动推导，避免手工抄错。

用法::

    python tools/write_regs.py slice185 slice220 ...        # 按 slices.json 推导
    python tools/write_regs.py --admin-only slice081        # 强制 admin
"""
import argparse
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)

sys.stdout.reconfigure(encoding="utf-8", errors="replace")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("keys", nargs="+")
    ap.add_argument("--admin-only", action="store_true")
    ap.add_argument("--public-only", action="store_true")
    args = ap.parse_args()

    slices = json.load(open(os.path.join(RUST, "docs", "slices.json"), encoding="utf-8"))
    by_key = {s["slice_id"].replace("-", ""): s for s in slices}

    outdir = os.path.join(RUST, "docs", "registrations")
    os.makedirs(outdir, exist_ok=True)

    for key in args.keys:
        key = key.replace("-", "")
        s = by_key.get(key)
        if s is None:
            print("未知切片: %s" % key, file=sys.stderr)
            return 1
        mods = s.get("modules") or []
        has_admin = "adminapi" in mods
        has_public = "api" in mods
        if args.admin_only:
            has_admin, has_public = True, False
        if args.public_only:
            has_admin, has_public = False, True

        rec = {
            "slice_id": key,
            "tag": s.get("tag"),
            "has_admin": has_admin,
            "has_public": has_public,
        }
        path = os.path.join(outdir, key + ".json")
        with open(path, "w", encoding="utf-8") as fh:
            json.dump(rec, fh, ensure_ascii=False)
        print("%-10s admin=%-5s public=%-5s  %s" % (key, has_admin, has_public, s.get("tag")))
    return 0


if __name__ == "__main__":
    sys.exit(main())
