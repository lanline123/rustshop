#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
TigShop Schema 解析器
=====================

从 `java/database/pure.sql`（基础结构，322 表）+ 全部 `*update.sql` 增量补丁
计算出版本 v5.8.57 的**最终表结构**，并生成 Rust 实体骨架。

用法:
    python schema_parser.py --dump-tables          # 列出所有表及列数
    python schema_parser.py --table product        # 打印某表的最终列定义
    python schema_parser.py --gen-entities         # 生成全部 Rust 实体
    python schema_parser.py --gen-entities --only product,user,order

支持的 DDL 变更（增量脚本中出现的）:
    CREATE TABLE [IF NOT EXISTS]
    ALTER TABLE ... ADD [COLUMN] / MODIFY / DROP [COLUMN]
    ALTER TABLE ... CHANGE (重命名列)
    DROP TABLE [IF EXISTS]
    RENAME TABLE a TO b
"""

from __future__ import annotations

import argparse
import json
import os
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path

# ---------------------------------------------------------------- 路径配置

# 本文件位于 <project>/rust/tools/schema_parser.py
RUST_ROOT = Path(__file__).resolve().parent.parent          # <project>/rust
PROJECT_ROOT = RUST_ROOT.parent                             # <project>
JAVA_DB = PROJECT_ROOT / "java" / "database"
PURE_SQL = JAVA_DB / "pure.sql"
RUST_ENTITIES = RUST_ROOT / "crates" / "core" / "src" / "entities"

# ---------------------------------------------------------------- 数据类型映射


@dataclass
class Column:
    """一个表列。"""

    name: str
    sql_type: str
    nullable: bool = True
    default: str | None = None
    comment: str = ""
    is_auto_increment: bool = False
    is_primary: bool = False

    @property
    def rust_type(self) -> str:
        """SQL 类型 -> Rust 类型（全部用 Option<T> 包裹，与既有代码风格一致）。"""
        t = self.sql_type.lower()
        # 去掉显示宽度/精度，再剥掉 unsigned/zerofill 等修饰符
        base = re.sub(r"\(.*?\)", "", t).strip()
        base = re.sub(r"\b(unsigned|zerofill|signed)\b", "", base).strip()
        base = re.sub(r"\s+", " ", base).strip()

        if base in ("tinyint", "smallint", "mediumint", "int", "integer", "year"):
            return "i32"
        if base in ("bigint",):
            return "i64"
        if base in ("decimal", "numeric", "double", "float", "real", "dec", "fixed"):
            return "Decimal"
        if base in ("char", "varchar", "text", "tinytext", "mediumtext", "longtext",
                    "enum", "set", "json", "time", "date", "datetime", "timestamp",
                    "binary", "varbinary", "blob", "tinyblob", "mediumblob", "longblob"):
            return "String"
        if base in ("bit", "bool", "boolean"):
            return "i32"
        # 未知类型回退为 String，保证能编译
        return "String"

    @property
    def rust_decl(self) -> str:
        """字段类型声明：NOT NULL 列用裸类型，可空列用 Option<T>。

        NOT NULL 且无 DEFAULT 的列没有合法默认值，用裸类型；
        NOT NULL 且有 DEFAULT 的列同样用裸类型（插入时省略即可由库填默认值）。
        仅真正可空的列才用 Option<T>，这样业务代码无需满屏 unwrap_or_default。
        """
        base = self.rust_type
        if self.nullable:
            return f"Option<{base}>"
        return base

    @property
    def rust_needs_decimal(self) -> bool:
        return self.rust_type == "Decimal"

    def rust_field_name(self) -> str:
        """列名 -> Rust 字段名（snake_case；关键字加 r#）。"""
        name = self.name
        # Rust 关键字需转义
        if name in RUST_KEYWORDS:
            return f"r#{name}"
        return name


RUST_KEYWORDS = {
    "type", "match", "move", "ref", "box", "fn", "let", "loop", "impl", "trait",
    "struct", "enum", "mod", "use", "pub", "crate", "self", "super", "where",
    "async", "await", "dyn", "static", "const", "unsafe", "extern", "in", "as",
    "break", "continue", "else", "for", "if", "return", "while", "yield", "abstract",
    "become", "do", "final", "macro", "override", "priv", "try", "typeof", "unsized",
    "virtual",
}


@dataclass
class Table:
    """一张表。"""

    name: str
    columns: dict[str, Column] = field(default_factory=dict)
    primary_key: list[str] = field(default_factory=list)
    comment: str = ""
    dropped: bool = False

    def add_column(self, col: Column) -> None:
        self.columns[col.name] = col

    def drop_column(self, name: str) -> None:
        self.columns.pop(name, None)


# ---------------------------------------------------------------- SQL 解析


def _unescape_sql_string(s: str) -> str:
    return s.replace("''", "'").replace("\\'", "'")


def _parse_column_def(line: str) -> Column | None:
    """解析一行列定义，如:
    `product_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
    """
    line = line.strip().rstrip(",").strip()
    if not line.startswith("`"):
        return None
    m = re.match(r"`([^`]+)`\s+([a-zA-Z]+(?:\s*\([^)]*\))?(?:\s+unsigned)?)(.*)$", line, re.I | re.S)
    if not m:
        return None
    name, sql_type, rest = m.group(1), m.group(2), m.group(3)
    rest_u = rest.upper()
    nullable = "NOT NULL" not in rest_u
    auto_inc = "AUTO_INCREMENT" in rest_u
    default = None
    dm = re.search(r"DEFAULT\s+('(?:[^']|'')*'|[-\w.]+)", rest, re.I)
    if dm:
        default = dm.group(1).strip("'")
    comment = ""
    cm = re.search(r"COMMENT\s+'((?:[^']|'')*)'", rest, re.I)
    if cm:
        comment = _unescape_sql_string(cm.group(1))
    return Column(
        name=name,
        sql_type=re.sub(r"\s+", " ", sql_type).strip(),
        nullable=nullable,
        default=default,
        comment=comment,
        is_auto_increment=auto_inc,
    )


def parse_create_table_like(stmt: str, tables: dict[str, Table]) -> bool:
    """处理 `CREATE TABLE a LIKE b`：复制 b 的结构到 a。返回是否命中。"""
    m = re.match(
        r"\s*CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?`?([^`\s(]+)`?\s+LIKE\s+`?([^`\s;]+)`?\s*;?\s*$",
        stmt, re.I,
    )
    if not m:
        return False
    new_name, src_name = m.group(1), m.group(2)
    src = tables.get(src_name)
    if src is None:
        return True  # 命中语法但源表未知，忽略
    clone = Table(
        name=new_name,
        columns={k: Column(**{**v.__dict__}) for k, v in src.columns.items()},
        primary_key=list(src.primary_key),
        comment=src.comment,
    )
    tables[new_name] = clone
    return True


def parse_create_table(stmt: str, tables: dict[str, Table]) -> None:
    """解析 CREATE TABLE 语句。"""
    m = re.match(
        r"\s*CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?`([^`]+)`\s*\((.*)\)\s*ENGINE",
        stmt, re.I | re.S,
    )
    if not m:
        # 无 ENGINE 子句的简化建表
        m = re.match(
            r"\s*CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?`([^`]+)`\s*\((.*)\)\s*;?\s*$",
            stmt, re.I | re.S,
        )
    if not m:
        return
    name, body = m.group(1), m.group(2)
    tbl = tables.get(name)
    if tbl is None or tbl.dropped:
        tbl = Table(name=name)
        tables[name] = tbl
    tbl.dropped = False

    # 按行解析（列定义一行一条，但 JSON/注释里可能含换行）
    depth = 0
    buf = ""
    for ch in body:
        if ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
        if ch == "," and depth == 0:
            _consume_table_item(buf, tbl)
            buf = ""
        else:
            buf += ch
    if buf.strip():
        _consume_table_item(buf, tbl)

    # 表注释
    tm = re.search(r"COMMENT\s*=\s*'((?:[^']|'')*)'", stmt, re.I)
    if tm:
        tbl.comment = _unescape_sql_string(tm.group(1))


def _consume_table_item(item: str, tbl: Table) -> None:
    """处理 CREATE TABLE 括号内的一项：列定义 / PRIMARY KEY / KEY / INDEX。"""
    s = item.strip()
    if not s:
        return
    up = s.upper()
    if up.startswith("PRIMARY KEY"):
        cols = re.findall(r"`([^`]+)`", s)
        tbl.primary_key = cols
        for c in cols:
            if c in tbl.columns:
                tbl.columns[c].is_primary = True
        return
    if up.startswith(("KEY ", "UNIQUE KEY", "INDEX ", "UNIQUE INDEX", "FULLTEXT", "CONSTRAINT", "FOREIGN KEY", "CHECK")):
        return
    col = _parse_column_def(s)
    if col:
        if col.is_primary:
            tbl.primary_key = [col.name]
        tbl.add_column(col)


def parse_alter_table(stmt: str, tables: dict[str, Table]) -> None:
    """解析 ALTER TABLE 语句。"""
    m = re.match(r"\s*ALTER\s+TABLE\s+`?([^`\s]+)`?\s+(.*)$", stmt, re.I | re.S)
    if not m:
        return
    tname, actions = m.group(1), m.group(2)
    tbl = tables.get(tname)
    if tbl is None:
        tbl = Table(name=tname)
        tables[tname] = tbl

    for act in _split_actions(actions):
        a = act.strip()
        if not a:
            continue
        up = a.upper()
        if re.match(r"ADD\s+(COLUMN\s+)?[`\w]", a, re.I) and not re.match(r"ADD\s+(UNIQUE\s+)?(KEY|INDEX|CONSTRAINT|PRIMARY|FOREIGN|FULLTEXT)", a, re.I):
            body = re.sub(r"^ADD\s+(COLUMN\s+)?", "", a, flags=re.I)
            col = _parse_column_def(body)
            if col:
                tbl.add_column(col)
        elif re.match(r"MODIFY\s+(COLUMN\s+)?", a, re.I):
            body = re.sub(r"^MODIFY\s+(COLUMN\s+)?", "", a, flags=re.I)
            col = _parse_column_def(body)
            if col:
                tbl.add_column(col)  # 覆盖已有定义
        elif re.match(r"CHANGE\s+(COLUMN\s+)?", a, re.I):
            body = re.sub(r"^CHANGE\s+(COLUMN\s+)?", "", a, flags=re.I)
            om = re.match(r"`([^`]+)`\s+(.*)$", body, re.S)
            if om:
                old, remainder = om.group(1), om.group(2)
                col = _parse_column_def(f"`{remainder}")
                if col:
                    tbl.drop_column(old)
                    tbl.add_column(col)
        elif re.match(r"DROP\s+(COLUMN\s+)?", a, re.I):
            body = re.sub(r"^DROP\s+(COLUMN\s+)?", "", a, flags=re.I)
            nm = re.match(r"`?([^`\s]+)`?", body)
            if nm:
                tbl.drop_column(nm.group(1))
        # 其余（ADD KEY / DROP KEY / ENGINE= / CONVERT TO 等）忽略


def _split_actions(actions: str) -> list[str]:
    """按顶层逗号切分 ALTER 的多个动作。"""
    out, depth, buf, in_str = [], 0, "", False
    i = 0
    while i < len(actions):
        ch = actions[i]
        if in_str:
            buf += ch
            if ch == "'" and (i + 1 >= len(actions) or actions[i + 1] != "'"):
                in_str = False
            i += 1
            continue
        if ch == "'":
            in_str = True
            buf += ch
        elif ch == "(":
            depth += 1
            buf += ch
        elif ch == ")":
            depth -= 1
            buf += ch
        elif ch == "," and depth == 0:
            out.append(buf)
            buf = ""
        else:
            buf += ch
        i += 1
    if buf.strip():
        out.append(buf)
    return out


def split_statements(sql: str) -> list[str]:
    """按分号切分 SQL，忽略注释与字符串内的分号。"""
    # 去块注释
    sql = re.sub(r"/\*.*?\*/", "", sql, flags=re.S)
    # 去行注释
    sql = re.sub(r"(?m)^\s*--.*$", "", sql)
    sql = re.sub(r"(?m)--\s.*$", "", sql)

    stmts, buf, in_str = [], "", False
    i = 0
    while i < len(sql):
        ch = sql[i]
        if in_str:
            buf += ch
            if ch == "\\":
                if i + 1 < len(sql):
                    buf += sql[i + 1]
                    i += 1
            elif ch == "'":
                if i + 1 < len(sql) and sql[i + 1] == "'":
                    buf += sql[i + 1]
                    i += 1
                else:
                    in_str = False
        elif ch == "'":
            in_str = True
            buf += ch
        elif ch == ";":
            if buf.strip():
                stmts.append(buf.strip())
            buf = ""
        else:
            buf += ch
        i += 1
    if buf.strip():
        stmts.append(buf.strip())
    return stmts


def parse_dynamic_ddl(sql: str, tables: dict[str, Table]) -> int:
    """解析动态 DDL：增量脚本用 SET @sql = IF(..., 'ALTER TABLE ...', 'SELECT ...');
    PREPARE stmt FROM @sql; 的幂等模式包裹真实 DDL。这些 DDL 藏在字符串字面量里，
    常规语句切分拿不到，需专门提取。

    返回解析到的 DDL 数量。
    """
    count = 0
    # 提取所有单引号字符串字面量（含 '' 转义）
    for lit in re.findall(r"'((?:[^']|'')*)'", sql):
        candidate = lit.replace("''", "'").strip()
        if not re.match(r"(?i)^(ALTER\s+TABLE|CREATE\s+TABLE|CREATE\s+(UNIQUE\s+)?INDEX|DROP\s+TABLE)", candidate):
            continue
        if re.match(r"(?i)^CREATE\s+TABLE", candidate):
            if not parse_create_table_like(candidate, tables):
                parse_create_table(candidate, tables)
            count += 1
        elif re.match(r"(?i)^ALTER\s+TABLE", candidate):
            parse_alter_table(candidate, tables)
            count += 1
    return count


def build_schema(verbose: bool = False) -> dict[str, Table]:
    """pure.sql + 全部 update.sql -> 最终 schema。"""
    tables: dict[str, Table] = {}

    # 1) 基础结构
    base = PURE_SQL.read_text(encoding="utf-8", errors="replace")
    for stmt in split_statements(base):
        if re.match(r"\s*CREATE\s+TABLE", stmt, re.I):
            parse_create_table(stmt, tables)
    if verbose:
        print(f"[schema] pure.sql 解析出 {len(tables)} 张表", file=sys.stderr)

    # 2) 增量补丁：按版本号排序
    patches = sorted(
        JAVA_DB.glob("*update.sql"),
        key=lambda p: _version_key(p.name),
    )
    applied = 0
    dyn_total = 0
    for patch in patches:
        sql = patch.read_text(encoding="utf-8", errors="replace")
        for stmt in split_statements(sql):
            head = stmt.lstrip()
            if re.match(r"CREATE\s+TABLE", head, re.I):
                # CREATE TABLE ... LIKE ... 优先
                if not parse_create_table_like(head, tables):
                    parse_create_table(stmt, tables)
            elif re.match(r"ALTER\s+TABLE", head, re.I):
                parse_alter_table(stmt, tables)
            elif re.match(r"DROP\s+TABLE", head, re.I):
                m = re.match(r"DROP\s+TABLE\s+(?:IF\s+EXISTS\s+)?`?([^`\s;]+)`?", head, re.I)
                if m and m.group(1) in tables:
                    tables[m.group(1)].dropped = True
            elif re.match(r"RENAME\s+TABLE", head, re.I):
                # 支持多组重命名: RENAME TABLE a TO b, c TO d;
                pairs = re.findall(
                    r"`?([^`\s,]+)`?\s+TO\s+`?([^`\s,;]+)`?", head, re.I
                )
                for old_name, new_name in pairs:
                    if old_name in tables:
                        moved = tables.pop(old_name)
                        moved.name = new_name
                        tables[new_name] = moved
        # 动态 DDL（PREPARE 包裹的字符串）
        dyn_total += parse_dynamic_ddl(sql, tables)
        applied += 1
    if verbose:
        print(f"[schema] 应用 {applied} 个增量补丁，提取 {dyn_total} 条动态 DDL", file=sys.stderr)

    return {k: v for k, v in tables.items() if not v.dropped and v.columns}


def _version_key(filename: str) -> tuple:
    """'5.8.56-5.8.57update.sql' -> (5,8,56,5,8,57) 便于排序。"""
    nums = re.findall(r"\d+", filename)
    return tuple(int(n) for n in nums)


# ---------------------------------------------------------------- Rust 生成

HEADER = """//! {title}
//!
//! **本文件由 `tools/schema_parser.py` 从 `java/database/pure.sql` +
//! 全部 `*update.sql` 增量补丁自动生成，请勿手工编辑。**
//!
//! 重新生成: `python tools/schema_parser.py --gen-entities`
//!
//! 对应数据库表: {tables}

{imports}"""


def gen_imports(tables: list[Table]) -> str:
    """按该域实际用到的类型生成 use 语句，避免未使用导入告警。"""
    needs_decimal = any(
        c.rust_type == "Decimal"
        for t in tables
        for c in t.columns.values()
    )
    lines = []
    if needs_decimal:
        lines.append("use rust_decimal::Decimal;")
    lines.append("use serde::{Deserialize, Serialize};")
    lines.append("use sqlx::FromRow;")
    return "\n".join(lines)

STRUCT_TPL = '''
/// {comment}
#[derive(Debug, Clone, Default, FromRow, Serialize, Deserialize)]
#[sqlx(rename_all = "snake_case")]
pub struct {struct_name} {{
{fields}}}

impl {struct_name} {{
    /// 数据库表名。
    pub const TABLE: &'static str = "{table}";
}}
'''


def to_pascal(snake: str) -> str:
    """snake_case -> PascalCase。"""
    return "".join(p.capitalize() for p in re.split(r"[_\W]+", snake) if p)


def gen_struct(table: Table) -> str:
    """生成一个 struct 定义。"""
    fields = []
    for col in table.columns.values():
        comment = col.comment.replace("\n", " ").strip()
        # 可空性标注，便于阅读与排查
        null_note = "" if col.nullable else " [NOT NULL]"
        if comment:
            fields.append(f"    /// {comment}{null_note}")
        elif not col.nullable:
            fields.append("    /// [NOT NULL]")
        fname = col.rust_field_name()
        fields.append(f"    pub {fname}: {col.rust_decl},")
    body = "\n".join(fields)
    return STRUCT_TPL.format(
        comment=table.comment or table.name,
        struct_name=to_pascal(table.name),
        table=table.name,
        fields=body + "\n",
    )


# 业务域 -> 表名前缀映射（用于分文件）
DOMAIN_BY_TABLE: dict[str, str] = {
    # 权限/账号
    "admin_user": "authority", "admin_role": "authority", "admin_log": "authority",
    "admin_api_token": "authority", "authority": "authority", "suppliers": "authority",
    # 商品
    "product": "product", "product_sku": "product", "product_gallery": "product",
    "product_attribute": "product", "product_style": "product", "product_style_item": "product",
    "product_video": "product", "product_related": "product", "product_group": "product",
    "product_member_price": "product", "product_presale": "product", "product_article": "product",
    "product_stock_record": "product", "product_stock_record_detail": "product",
    "product_stock_adjust_record": "product", "product_stock_allocate_record": "product",
    "product_stock_allocate_record_detail": "product", "product_stock_allocate_operation_record": "product",
    "product_auto_status": "product", "product_inventory_log": "product",
    "attributes_tpl": "product", "category": "product", "brand": "product",
    "comment": "product", "services": "product", "e_card": "product", "e_card_group": "product",
    "price_inquiry": "product", "price_inquiry_reply_record": "product",
    "stock_warning_rule": "product", "stock_warning_setting": "product",
    # 订单
    "order": "order", "order_item": "order", "order_log": "order",
    "order_amount_detail": "order", "order_coupon_detail": "order", "order_invoice": "order",
    "order_presale": "order", "order_reservation": "order", "order_spit_log": "order",
    "aftersales": "order", "aftersales_item": "order", "aftersales_log": "order",
    "logistics_api_log": "order",
    # 购物车
    "cart": "cart", "cart_global_change_price": "cart",
    # 用户
    "user": "user", "user_address": "user", "user_authorize": "user", "user_company": "user",
    "user_coupon": "user", "user_follow": "user", "user_growth_points_log": "user",
    "user_member_tag": "user", "user_message": "user", "user_message_log": "user",
    "user_points_log": "user", "user_rank": "user", "user_rank_config": "user",
    "user_rank_log": "user", "virtual_user_info": "user", "collect_product": "user",
    "feedback": "user", "member_tag": "user", "member_tag_group": "user",
    "rank_growth_log": "user", "pickup_contact": "user", "shop_member": "user",
    # 营销
    "coupon": "promotion", "coupon_issue_batch": "promotion", "coupon_issue_log": "promotion",
    "seckill": "promotion", "seckill_item": "promotion",
    "groupon_promotion": "promotion", "groupon_promotion_detail": "promotion",
    "groupon_promotion_item": "promotion", "groupon_promotion_record": "promotion",
    "groupon_promotion_shop": "promotion",
    "lottery_activity": "promotion", "lottery_prize": "promotion", "lottery_record": "promotion",
    "lottery_user_stat": "promotion",
    "promotion": "promotion", "product_promotion": "promotion", "product_gift": "promotion",
    "time_discount": "promotion", "time_discount_item": "promotion",
    "gift_card": "promotion", "gift_card_groups": "promotion", "gift_card_order": "promotion",
    "gift_card_products": "promotion",
    "points_exchange": "promotion", "sign": "promotion", "sign_in_setting": "promotion",
    "recharge_setting": "promotion", "short_url": "promotion",
    "buyer_show": "promotion", "buyer_show_comment": "promotion", "buyer_show_favorite": "promotion",
    "buyer_show_gallery": "promotion", "buyer_show_group": "promotion", "buyer_show_like": "promotion",
    "buyer_show_video": "promotion", "comment_like": "promotion",
    # 财务
    "paylog": "finance", "paylog_refund": "finance", "refund_apply": "finance",
    "refund_apply_paylog_refund": "finance", "refund_log": "finance",
    "statement": "finance", "statement_download": "finance", "statement_output": "finance",
    "credit_order": "finance", "credit_settlement": "finance", "credit_user": "finance",
    "merchant_application": "finance", "merchant_application_media": "finance",
    "order_finance_aftersales": "finance", "profit_sharing_order": "finance",
    "profit_sharing_return_order": "finance", "record_rate": "finance",
    "category_service_fee_rule": "finance", "category_service_fee_rule_category": "finance",
    "category_service_fee_rule_target": "finance",
    "sub_merchant": "finance", "user_balance_log": "finance", "user_cash_log": "finance",
    "user_invoice": "finance", "user_recharge_order": "finance",
    "user_withdraw_apply": "finance", "withdraw_apply_account": "finance",
    # 系统设置
    "config": "setting", "area_code": "setting", "region": "setting",
    "friend_links": "setting", "gallery": "setting", "gallery_pic": "setting",
    "logistics_company": "setting", "logistics_company_config": "setting",
    "shipping_tpl": "setting", "shipping_tpl_info": "setting", "shipping_type": "setting",
    "mail_templates": "setting", "message_template": "setting", "message_type": "setting",
    "tax_rate": "setting", "app_version": "setting", "print_config": "setting",
    # 供应商
    "vendor": "vendor", "vendor_account": "vendor", "vendor_account_log": "vendor",
    "vendor_settlement_order": "vendor", "vendor_shop_bind": "vendor",
    "vendor_withdraw": "vendor", "admin_user_vendor": "vendor",
    # 店铺
    "shop": "shop", "shop_funds": "shop", "shop_withdraw": "shop",
    "shop_product_category": "shop", "collect_shop": "shop", "account": "shop",
    "order_config": "shop", "admin_user_shop": "shop",
    # 分销
    "salesman": "salesman", "salesman_config": "salesman", "salesman_content": "salesman",
    "salesman_customer": "salesman", "salesman_group": "salesman",
    "salesman_material": "salesman", "salesman_material_category": "salesman",
    "salesman_order": "salesman", "salesman_product": "salesman",
    # 装修
    "decorate": "decorate", "decorate_discrete": "decorate", "decorate_share": "decorate",
    "mobile_cat_nav": "decorate", "pc_cat_floor": "decorate", "pc_navigation": "decorate",
    # 内容
    "article": "content", "article_category": "content",
    "wechat_live": "content", "wechat_live_goods": "content", "wechat_live_order_rel": "content",
    "wechat_live_room_goods": "content", "wechat_live_sync_log": "content",
    "wechat_video_account": "content",
    # 保证金
    "shop_deposit": "deposit", "shop_deposit_adjust_log": "deposit", "shop_deposit_log": "deposit",
    "shop_deposit_offshelf_log": "deposit", "shop_deposit_payee_account": "deposit",
    "shop_deposit_recharge": "deposit", "shop_deposit_refund": "deposit",
    "shop_deposit_rule": "deposit", "shop_deposit_rule_category": "deposit",
    # 门店 O2O
    "delivery_order": "o2o", "order_pickup_item": "o2o", "order_pickup_shipment": "o2o",
    "product_reservation_stock": "o2o", "product_reservation_time": "o2o",
    "reservation_tpl": "o2o", "store_product": "o2o", "store_product_sku": "o2o",
    "store_tip": "o2o", "tip": "o2o", "user_pickup_info": "o2o",
    "shop_home_service_config": "o2o", "shop_local_delivery_config": "o2o",
    "shop_pickup_config": "o2o", "shop_pickup_tpl": "o2o", "area_store_manager_shop": "o2o",
    # 批发
    "wholesaler": "wholesale", "wholesaler_grade": "wholesale",
    "product_wholesale_config": "wholesale", "product_wholesale_price": "wholesale",
    "product_wholesale_sku_config": "wholesale",
    # 客服 IM
    "im_conversation": "im", "im_message": "im", "im_servant": "im",
    "shop_kefu_config": "im", "admin_msg": "im",
    # 打印
    "print": "print", "print_log": "print", "print_pos_device": "print",
    # 商户
    "merchant": "merchant", "merchant_account": "merchant",
    "merchant_apply": "merchant", "merchant_user": "merchant",
    # 多语言
    "currency": "lang", "locales": "lang", "translations": "lang",
    "translations_data": "lang", "image_i18n": "lang",
    # 日志
    "access_log": "log", "request_log": "log", "statistics_base": "log", "statistics_log": "log",
    # 视频
    "gallery_video": "video", "gallery_video_info": "video",
    # 面板
    "panel_shortcut_menu": "panel",
    # 采购（进货管理）
    "purchase_order": "purchase", "purchase_order_item": "purchase",
    "purchase_receipt": "purchase", "purchase_receipt_item": "purchase",
    "purchase_reconciliation": "purchase", "purchase_reconciliation_item": "purchase",
    "purchase_reconciliation_source": "purchase", "purchase_return": "purchase",
    "purchase_return_item": "purchase", "purchase_setting": "purchase",
    "purchase_supplier_category": "purchase", "purchase_operation_log": "purchase",
    # 付费会员卡
    "premium_member_card": "premium",
    "premium_member_card_right": "premium",
    "premium_member_card_tier": "premium",
    "premium_member_decorate": "premium",
    "premium_member_order": "premium",
    "premium_member_right": "premium",
    "premium_member_right_rule": "premium",
    # 服务人员（上门服务）
    "service_staff": "staff", "service_staff_wallet": "staff",
    "service_staff_wallet_log": "staff", "service_staff_withdraw": "staff",
    # 砍价 / 团购（独立于 groupon_promotion）
    "bargain": "promotion", "bargain_group": "promotion", "bargain_log": "promotion",
    "groupon": "promotion", "groupon_item": "promotion",
    # 微信（关键词回复/菜单）
    "wechat_keyword_rule": "wechat", "wechat_keyword_rule_keyword": "wechat",
    "wechat_keyword_rule_reply": "wechat", "wechat_menu": "wechat",
    # 日志类
    "mail_log": "log", "sms_log": "log", "unipush_log": "log",
    "distribution_log": "log",
    # 其他
    "ai_translate_language": "lang", "album": "content", "keywords": "setting",
    "crons": "setting", "sensitive_words": "setting", "example": "setting",
    "im_config": "im", "area_store_manager": "o2o",
    "pos_device": "o2o", "pos_pending_order": "o2o",
}


def domain_of(table_name: str) -> str:
    """推断表所属业务域。"""
    if table_name in DOMAIN_BY_TABLE:
        return DOMAIN_BY_TABLE[table_name]
    # 前缀匹配
    for prefix, domain in sorted(DOMAIN_BY_TABLE.items(), key=lambda kv: -len(kv[0])):
        if table_name.startswith(prefix):
            return domain
    return "misc"


DOMAIN_TITLES = {
    "authority": "权限与账号实体",
    "product": "商品域实体",
    "order": "订单域实体",
    "cart": "购物车实体",
    "user": "会员域实体",
    "promotion": "营销域实体（优惠券/秒杀/拼团/积分/抽奖/礼品卡/买家秀）",
    "finance": "财务域实体（支付流水/退款/结算/提现/发票/分账）",
    "setting": "系统设置域实体（配置/地区/物流/模板）",
    "vendor": "供应商域实体",
    "shop": "店铺域实体",
    "salesman": "分销域实体",
    "decorate": "页面装修域实体",
    "content": "内容域实体（文章/微信直播/视频号）",
    "deposit": "保证金域实体",
    "o2o": "门店 O2O 域实体（自提/同城配送/预约）",
    "wholesale": "批发域实体",
    "im": "客服 IM 域实体",
    "print": "打印域实体",
    "merchant": "商户域实体",
    "lang": "多语言域实体",
    "log": "日志与统计实体",
    "video": "视频域实体",
    "panel": "面板实体",
    "purchase": "采购域实体（进货单/收货/对账/退货）",
    "premium": "付费会员卡域实体",
    "staff": "服务人员域实体（上门服务/钱包/提现）",
    "wechat": "微信公众号域实体（关键词回复/菜单）",
    "misc": "其他实体",
}


def gen_mod_rs(stats: dict[str, int], dry_run: bool = False) -> str:
    """生成 entities/mod.rs，声明全部域模块并 re-export 全部结构体。"""
    tables = build_schema(verbose=False)
    by_domain: dict[str, list[Table]] = {}
    for t in tables.values():
        by_domain.setdefault(domain_of(t.name), []).append(t)

    domains = sorted(by_domain)
    lines = [
        "//! 数据库实体层。",
        "//!",
        "//! **本文件由 `tools/schema_parser.py` 自动生成，请勿手工编辑。**",
        "//!",
        "//! 字段名与列名严格对齐 `java/database/pure.sql` + 全部增量补丁",
        "//! （即 v5.8.57 的最终库结构），保证与既有 MySQL 零迁移兼容。",
        "//!",
        f"//! 共 {len(tables)} 张表 / {len(domains)} 个业务域。",
        "//!",
        "//! 重新生成: `python tools/schema_parser.py --gen-entities`",
        "",
    ]
    for d in domains:
        lines.append(f"pub mod {d};")
    # 手工维护的业务方法模块（不在生成范围内，但需在此声明）
    lines.append("pub mod business;")
    lines.append("")

    # re-export：仅导出各域结构体，避免 glob 冲突
    for d in domains:
        for t in sorted(by_domain[d], key=lambda x: x.name):
            lines.append(f"pub use {d}::{to_pascal(t.name)};")
    lines.append("")

    content = "\n".join(lines)
    if not dry_run:
        (RUST_ENTITIES / "mod.rs").write_text(content, encoding="utf-8")
    return content


def gen_entities(only: set[str] | None = None, dry_run: bool = False) -> dict[str, int]:
    """按业务域生成实体文件。"""
    tables = build_schema(verbose=True)
    if only:
        tables = {k: v for k, v in tables.items() if k in only}

    by_domain: dict[str, list[Table]] = {}
    for t in tables.values():
        by_domain.setdefault(domain_of(t.name), []).append(t)

    RUST_ENTITIES.mkdir(parents=True, exist_ok=True)
    stats: dict[str, int] = {}

    # 汇总域列表（用于 mod.rs）
    domain_list: list[str] = []

    for domain in sorted(by_domain):
        tbls = sorted(by_domain[domain], key=lambda t: t.name)
        domain_list.append(domain)
        chunks = [HEADER.format(
            title=DOMAIN_TITLES.get(domain, domain),
            tables=", ".join(f"`{t.name}`" for t in tbls),
            imports=gen_imports(tbls),
        )]
        for t in tbls:
            chunks.append(gen_struct(t))
        content = "\n".join(chunks)
        out = RUST_ENTITIES / f"{domain}.rs"
        if not dry_run:
            out.write_text(content, encoding="utf-8")
        stats[domain] = len(tbls)
        print(f"  {domain}.rs : {len(tbls)} 个结构体", file=sys.stderr)

    # 同步生成 mod.rs
    if not only:
        gen_mod_rs(stats, dry_run=dry_run)

    return stats


# ---------------------------------------------------------------- CLI


def main() -> int:
    ap = argparse.ArgumentParser(description="TigShop schema 解析与 Rust 实体生成")
    ap.add_argument("--dump-tables", action="store_true", help="列出所有表及列数")
    ap.add_argument("--table", help="打印指定表的最终列定义")
    ap.add_argument("--gen-entities", action="store_true", help="生成 Rust 实体")
    ap.add_argument("--only", help="仅处理指定表（逗号分隔），配合 --gen-entities")
    ap.add_argument("--dry-run", action="store_true", help="只统计不写文件")
    ap.add_argument("--json", action="store_true", help="以 JSON 输出")
    args = ap.parse_args()

    if args.dump_tables:
        tables = build_schema(verbose=True)
        items = sorted(tables.items())
        if args.json:
            print(json.dumps({k: len(v.columns) for k, v in items}, ensure_ascii=False, indent=2))
        else:
            print(f"共 {len(items)} 张表")
            for name, t in items:
                print(f"  {name:42} {len(t.columns):3} 列  {t.comment[:30]}")
        return 0

    if args.table:
        tables = build_schema(verbose=False)
        t = tables.get(args.table)
        if not t:
            print(f"表 {args.table} 不存在", file=sys.stderr)
            return 1
        print(f"表 `{t.name}` — {t.comment}")
        print(f"主键: {', '.join(t.primary_key) or '(未识别)'}")
        print(f"列数: {len(t.columns)}")
        for c in t.columns.values():
            flag = "NULL" if c.nullable else "NOT NULL"
            print(f"  {c.name:38} {c.sql_type:22} [{flag:8}] -> {c.rust_decl}")
        return 0

    if args.gen_entities:
        only = set(args.only.split(",")) if args.only else None
        stats = gen_entities(only=only, dry_run=args.dry_run)
        total = sum(stats.values())
        print(f"\n共生成 {total} 个结构体，分布于 {len(stats)} 个域文件", file=sys.stderr)
        return 0

    ap.print_help()
    return 0


if __name__ == "__main__":
    # Windows 控制台默认 GBK，强制 UTF-8 避免中文注释乱码
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass
    sys.exit(main())
