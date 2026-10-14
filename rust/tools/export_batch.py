#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""把 batches.json 的第 N 批导出成 workflow 可直接消费的 APIV2 参数（纯 JSON stdout）。"""
import io
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)

sys.stdout.reconfigure(encoding="utf-8", errors="replace")

batch_no = int(sys.argv[1])  # 1-based
data = json.load(open(os.path.join(RUST, "docs", "batches.json"), encoding="utf-8"))
batches = data["batches"]
if batch_no < 1 or batch_no > len(batches):
    print("batch out of range 1..%d" % len(batches), file=sys.stderr)
    sys.exit(1)

out = []
for s in batches[batch_no - 1]:
    out.append({
        "slice_id": s["slice_id"],
        "key": s["key"],
        "tag": s["tag"],
        "api_count": s["api_count"],
        "modules": s["modules"],
        "controllers": s["controllers"],
        "owned_files": s["owned_files"],
    })
json.dump({"batch_no": batch_no, "batch": out}, sys.stdout, ensure_ascii=False)
