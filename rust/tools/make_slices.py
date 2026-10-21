#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""把 docs/api-missing.csv 切成垂直切片（slices.json），供 workflow 并行实现。

切片原则：
- 按 tag 聚合（tag 天然是一个业务域），单片上限 MAX_PER_SLICE，超出再拆
- 同一 controller 的接口尽量落在同一片（共享 service/repo）
- 优先级：P0 交易闭环 tag 先出
- 纯输出 JSON，不写任何 Rust 代码
"""
import csv
import json
import os
import sys
from collections import OrderedDict

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)
MISSING = os.path.join(RUST, "docs", "api-missing.csv")
OUT = os.path.join(RUST, "docs", "slices.json")

# 一个 tag = 一个切片 = 一组独占文件。
# 不把大 tag 拆成多片：同 tag 的接口共享 service/repo，
# 拆开会让多个子代理并发写同一个 services/<domain>.rs，必然互相覆盖。
MAX_PER_SLICE = 50

# P0 交易闭环优先（对齐 Rust-重构分阶段迁移计划.md 阶段 2/3）
P0_TAGS = [
    "商品", "订单管理", "商品库存管理", "商品分类", "品牌", "库存预警",
    "购物车", "前台用户中心", "售后管理", "前台用户售后",
]
P1_TAGS = ["权限", "管理员列表", "会员管理", "店铺", "店铺信息"]


def load():
    with open(MISSING, encoding="utf-8-sig") as f:
        return [r for r in csv.DictReader(f) if (r.get("path") or "").strip()]


def split_group(rows, maxn):
    """把一个 tag 的接口切成 <=maxn 的块，按 controller 聚拢以保持内聚。"""
    if len(rows) <= maxn:
        return [rows]
    by_ctrl = OrderedDict()
    for r in rows:
        by_ctrl.setdefault(r.get("controller") or "misc", []).append(r)
    chunks, cur = [], []
    for ctrl in sorted(by_ctrl, key=lambda c: -len(by_ctrl[c])):
        items = by_ctrl[ctrl]
        if len(items) > maxn:  # 单个控制器本身就超限，硬切
            for i in range(0, len(items), maxn):
                chunks.append(items[i:i + maxn])
            continue
        if cur and len(cur) + len(items) > maxn:
            chunks.append(cur)
            cur = []
        cur.extend(items)
    if cur:
        chunks.append(cur)
    return chunks


def slug(tag):
    """tag -> 唯一的 ascii 文件名（中文 tag 用序号兜底，保证不撞名）。"""
    return "slice%03d"


def owned_files(n, tag, admin_apis, public_apis):
    """本片独占的文件。用 slice 序号命名，天然唯一，杜绝跨片冲突。"""
    files = []
    if admin_apis:
        files.append("crates/api/admin/src/routes/slice%03d.rs" % n)
    if public_apis:
        files.append("crates/api/public/src/routes/slice%03d.rs" % n)
    files.append("crates/services/src/slice%03d.rs" % n)
    files.append("crates/infrastructure/src/db/repo/slice%03d.rs" % n)
    return files


def main():
    rows = load()
    by_tag = OrderedDict()
    for r in rows:
        by_tag.setdefault((r.get("tag") or "").strip() or "未分类", []).append(r)

    def ordered_tags():
        tags = list(by_tag)
        def key(t):
            if t in P0_TAGS:
                return (0, P0_TAGS.index(t))
            if t in P1_TAGS:
                return (1, P1_TAGS.index(t))
            return (2, t)
        return sorted(tags, key=key)

    slices = []
    n = 0
    for tag in ordered_tags():
        tier = "P0" if tag in P0_TAGS else ("P1" if tag in P1_TAGS else "P2")
        for chunk in split_group(by_tag[tag], MAX_PER_SLICE):
            n += 1
            ctrls = sorted({(c.get("controller") or "") for c in chunk})
            srcs = sorted({(c.get("source") or "").split(":")[0] for c in chunk})
            # 独占文件：本片的域路由文件由本片独占创建/修改。
            # 关键：同一 tag 只产生一片 -> 文件名唯一 -> 无并发写冲突。
            admin_apis = [c for c in chunk if c.get("module") == "adminapi"]
            public_apis = [c for c in chunk if c.get("module") == "api"]
            slices.append({
                "slice_id": "slice-%03d" % n,
                "tier": tier,
                "tag": tag,
                "api_count": len(chunk),
                "modules": sorted({c.get("module") for c in chunk}),
                "controllers": [c for c in ctrls if c],
                "java_sources": [s for s in srcs if s],
                "owned_files": owned_files(n, tag, admin_apis, public_apis),
                "apis": [{
                    "method": c.get("method"),
                    "path": c.get("path"),
                    "summary": c.get("summary"),
                    "auth": c.get("auth"),
                    "permissions": c.get("permissions"),
                    "controller": c.get("controller"),
                    "handler": c.get("handler"),
                    "source": c.get("source"),
                } for c in chunk],
            })

    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(slices, f, ensure_ascii=False, indent=1)

    p0 = [s for s in slices if s["tier"] == "P0"]
    print("total slices: %d (P0 %d / P1 %d / P2 %d)" % (
        len(slices), len(p0),
        len([s for s in slices if s["tier"] == "P1"]),
        len([s for s in slices if s["tier"] == "P2"])))
    print("P0 api total: %d" % sum(s["api_count"] for s in p0))
    for s in p0:
        print("  %s  %-14s %3d apis  ctrl=%s" % (
            s["slice_id"], s["tag"], s["api_count"], ",".join(s["controllers"][:3])))
    print("-> " + OUT)


if __name__ == "__main__":
    sys.exit(main())
