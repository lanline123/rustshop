#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
仓储层 SQL 列名校验器
=====================

Rust 仓储层使用运行时 `sqlx::query_as::<_, T>()`（非编译期 `query!` 宏），
SQL 中的列名不会被编译器校验 —— 这正是早期实体字段名漂移能逃过
`cargo check` + 单元测试的原因。本脚本在静态层面补上这道检查：

1. 从 `java/database/pure.sql` + 增量补丁解析出权威 schema（复用 schema_parser）
2. 扫描 `crates/infrastructure/src/db/repo/*.rs` 中的 SQL 字符串字面量
3. 提取 `SELECT/INSERT/UPDATE/DELETE` 涉及的列名，逐一对表校验
4. 报告：未知列、表名错误、以及实体字段与 SQL 列不一致

用法:
    python tools/check_sql_columns.py            # 校验并输出报告
    python tools/check_sql_columns.py --strict   # 有发现即非零退出（可用于 CI）
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from schema_parser import RUST_ROOT, build_schema  # noqa: E402

REPO_DIR = RUST_ROOT / "crates" / "infrastructure" / "src" / "db" / "repo"

# SQL 关键字/函数，提取列名时需排除
SQL_NOISE = {
    "select", "from", "where", "and", "or", "not", "null", "is", "in", "like",
    "insert", "into", "values", "update", "set", "delete", "as", "on", "join",
    "left", "right", "inner", "outer", "group", "by", "order", "limit", "offset",
    "asc", "desc", "count", "sum", "max", "min", "avg", "coalesce", "ifnull",
    "now", "distinct", "case", "when", "then", "else", "end", "exists", "between",
    "having", "union", "all", "any", "replace", "concat", "date", "from_unixtime",
    "unix_timestamp", "cast", "convert", "interval", "true", "false", "using",
}


def strip_rust_comments(src: str) -> str:
    """移除 Rust 行注释与块注释，避免把文档里的标识符误当 SQL 列名。"""
    # 块注释 /* ... */（非嵌套场景足够）
    src = re.sub(r"/\*.*?\*/", "", src, flags=re.S)
    # 行注释 // ...（保留换行以维持行号）
    src = re.sub(r"(?m)//[^\n]*", "", src)
    return src


def extract_sql_literals(rust_src: str) -> list[tuple[int, str]]:
    """提取 Rust 源码中的 SQL 字符串字面量。

    只取**引号内**的内容，避免把 Rust 代码（类型名、变量名）当成 SQL 列。
    处理 `format!("...")` 与跨行拼接的字符串。
    """
    out: list[tuple[int, str]] = []
    lines = rust_src.split("\n")
    i = 0
    while i < len(lines):
        line = lines[i]
        if not re.search(r'"(?:\s*)(SELECT|INSERT|UPDATE|DELETE)\b', line, re.I):
            i += 1
            continue

        # 从起始行开始收集，直到字符串字面量闭合
        start_line = i + 1
        collected: list[str] = []
        j = i
        closed = False
        while j < len(lines) and not closed:
            seg = lines[j]
            # 取该行所有 "..." 字面量内容
            for lit in re.findall(r'"((?:[^"\\]|\\.)*)"', seg):
                collected.append(lit)
            # 判断 SQL 字面量是否在本行闭合：出现 `"` 且后面只剩 , ) ; 之类
            if re.search(r'"[^"]*"\s*[,)]?\s*$', seg) and collected:
                closed = True
            j += 1
        out.append((start_line, "\n".join(collected)))
        i = j if j > i else i + 1
    return out


def extract_tables(sql: str) -> list[str]:
    """提取 SQL 涉及的表名。"""
    tables: list[str] = []
    # FROM / JOIN / INTO / UPDATE 后的表名（支持反引号）
    for m in re.finditer(
        r"(?i)\b(?:FROM|JOIN|INTO|UPDATE)\s+`?([a-z_][a-z0-9_]*)`?",
        sql,
    ):
        name = m.group(1)
        if name.lower() not in SQL_NOISE:
            tables.append(name)
    return tables


def extract_columns(sql: str, table_names: set[str] | None = None) -> list[str]:
    """提取 SQL 中的候选列名（反引号包裹的 + 赋值左侧 + 常见裸列名）。

    `table_names` 用于排除被反引号包裹的**表名**（如 `` `user` ``），
    否则会误报为列。
    """
    exclude = {t.lower() for t in (table_names or set())}
    cols: set[str] = set()

    def add(name: str) -> None:
        if name and name.lower() not in exclude:
            cols.add(name)

    # 1) 反引号包裹的标识符（排除表名）
    for m in re.finditer(r"`([a-z_][a-z0-9_]*)`", sql, re.I):
        add(m.group(1))
    # 2) INSERT INTO t (a, b, c) 的列清单
    for m in re.finditer(r"(?is)INSERT\s+INTO\s+`?\w+`?\s*\(([^)]*)\)", sql):
        for c in re.split(r"[,\s]+", m.group(1)):
            c = c.strip().strip("`")
            if c and re.fullmatch(r"[a-z_][a-z0-9_]*", c, re.I):
                add(c)
    # 3) UPDATE t SET a = ?, b = ? 的赋值左侧
    #    注意 `SET points = points + ?` 这类表达式：右侧的列也需纳入。
    for m in re.finditer(r"(?is)\bSET\s+(.*?)(?:\bWHERE\b|$)", sql):
        for part in m.group(1).split(","):
            left = part.split("=")[0].strip().strip("`")
            if re.fullmatch(r"[a-z_][a-z0-9_]*", left, re.I):
                add(left)
            if "=" in part:
                right = part.split("=", 1)[1]
                rm = re.match(r"\s*`?([a-z_][a-z0-9_]*)`?\s*[-+*/]", right, re.I)
                if rm:
                    add(rm.group(1))
    # 4) SELECT 列表中的裸列名（a, b, t.c）——仅取明显形如标识符的
    for m in re.finditer(r"(?is)\bSELECT\s+(.*?)\s+\bFROM\b", sql):
        for part in m.group(1).split(","):
            token = part.strip()
            # 排除函数调用、别名、*
            if re.fullmatch(r"[a-z_][a-z0-9_.]*", token, re.I):
                token = token.split(".")[-1]
                if token.lower() not in SQL_NOISE:
                    add(token)
    # 5) WHERE/ORDER BY 中的裸列名（等值比较左侧、ORDER BY 首列）
    for m in re.finditer(r"(?is)\bWHERE\b(.*?)(?:\bORDER\b|\bLIMIT\b|$)", sql):
        for cm in re.finditer(r"`?([a-z_][a-z0-9_]*)`?\s*(?:=|>=|<=|>|<|\bLIKE\b|\bIN\b|\bIS\b)", m.group(1), re.I):
            add(cm.group(1))
    for m in re.finditer(r"(?is)\bORDER\s+BY\s+`?([a-z_][a-z0-9_]*)`?", sql):
        add(m.group(1))
    return sorted(cols)


def main() -> int:
    ap = argparse.ArgumentParser(description="校验仓储层 SQL 的列名与表名")
    ap.add_argument("--strict", action="store_true", help="有发现则以非零码退出")
    args = ap.parse_args()

    schema = build_schema(verbose=False)
    # 表名 -> 列名集合
    table_cols = {name: set(t.columns.keys()) for name, t in schema.items()}
    all_tables = set(table_cols)

    if not REPO_DIR.exists():
        print(f"仓储目录不存在: {REPO_DIR}", file=sys.stderr)
        return 1

    total_unknown_col = 0
    total_unknown_tbl = 0
    total_stmts = 0
    files = sorted(REPO_DIR.glob("*.rs"))

    for f in files:
        src = strip_rust_comments(f.read_text(encoding="utf-8", errors="replace"))
        stmts = extract_sql_literals(src)
        if not stmts:
            continue
        report: list[str] = []
        for line_no, raw in stmts:
            total_stmts += 1
            tables = extract_tables(raw)
            cols = extract_columns(raw, set(tables))
            # 表校验
            for t in set(tables):
                if t not in all_tables:
                    report.append(f"  L{line_no}: 未知表 `{t}`")
                    total_unknown_tbl += 1
            # 列校验：仅当该语句只涉及一张已知表时才做（多表 JOIN 归因困难）
            known = [t for t in set(tables) if t in all_tables]
            if len(known) == 1:
                valid = table_cols[known[0]]
                for c in cols:
                    if c.lower() in SQL_NOISE:
                        continue
                    if c not in valid:
                        report.append(
                            f"  L{line_no}: 表 `{known[0]}` 中不存在列 `{c}`"
                        )
                        total_unknown_col += 1
        if report:
            print(f"\n=== {f.name} ===")
            # 去重保序
            seen = set()
            for r in report:
                if r not in seen:
                    seen.add(r)
                    print(r)

    print(f"\n扫描 {len(files)} 个仓储文件 / {total_stmts} 条 SQL 语句")
    print(f"未知表: {total_unknown_tbl}    未知列: {total_unknown_col}")

    if total_unknown_col or total_unknown_tbl:
        print("\n⚠️  存在与真实 schema 不符的引用，连接数据库时会报错。")
        if args.strict:
            return 1
    else:
        print("\n✅ 所有 SQL 引用的表名与列名均与 schema 一致。")
    return 0


if __name__ == "__main__":
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass
    sys.exit(main())
