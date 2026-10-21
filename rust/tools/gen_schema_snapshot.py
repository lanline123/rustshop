#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
生成 schema 快照（供 Rust 侧测试比对）
=====================================

把权威 schema（pure.sql + 增量补丁解析结果）导出为
`crates/core/tests/schema_snapshot.txt`，格式：

    <table>.<column>|<rust_type>

Rust 侧的 `tests/schema_consistency.rs` 会读取该快照，反射式地
（通过显式声明的最小字段清单）校验实体定义未发生漂移。

用法:
    python tools/gen_schema_snapshot.py
"""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from schema_parser import RUST_ROOT, build_schema  # noqa: E402

OUT = RUST_ROOT / "crates" / "core" / "tests" / "schema_snapshot.txt"

# 快照仅包含核心业务表，避免文件过大；可按需扩展
CORE_TABLES = [
    "admin_user", "user", "product", "product_sku", "category", "brand",
    "order", "order_item", "cart", "coupon", "user_coupon",
    "article", "article_category", "member_tag", "member_tag_group",
    "user_rank", "user_points_log", "logistics_company", "shipping_tpl",
    "shipping_tpl_info", "shipping_type",
    # 营销域（秒杀 / 限时折扣）
    "seckill", "seckill_item", "time_discount", "time_discount_item",
    # 财务域（支付日志 / 退款申请）
    "paylog", "paylog_refund", "refund_apply", "refund_log",
]


def main() -> int:
    schema = build_schema(verbose=True)
    OUT.parent.mkdir(parents=True, exist_ok=True)

    lines = [
        "# TigShop schema 快照（由 tools/gen_schema_snapshot.py 生成，勿手改）",
        "# 格式: <表名>.<列名>|<Rust 类型声明>",
        "# 用途: crates/core/tests/schema_consistency.rs 校验实体未漂移",
        "#",
    ]
    count = 0
    for tname in CORE_TABLES:
        t = schema.get(tname)
        if t is None:
            print(f"警告: 快照表 {tname} 在 schema 中不存在", file=sys.stderr)
            continue
        lines.append(f"[{tname}]")
        for col in t.columns.values():
            lines.append(f"{col.name}|{col.rust_decl}")
            count += 1
        lines.append("")

    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"已写入 {OUT} （{len(CORE_TABLES)} 张表 / {count} 列）", file=sys.stderr)
    return 0


if __name__ == "__main__":
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass
    sys.exit(main())
