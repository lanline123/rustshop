#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Rust 路由 -> 原 Java API 契约覆盖率校验。

从 Rust 源码中**静态提取**已注册的 axum 路由，与原 Java 契约
（``docs/api-contract.json``）比对，输出：

* 已实现 / 未实现接口数量与百分比（按模块）
* 契约中存在但 Rust 未实现的接口清单（开发待办）
* Rust 已注册但契约中不存在的路由（自加端点，需人工确认）

路由前缀按 crate 约定推导：
  ``crates/api/admin/**``  -> ``/adminapi``
  ``crates/api/public/**`` -> ``/api``

用法::

    python tools/check_api_coverage.py                 # 报告
    python tools/check_api_coverage.py --strict        # 有未实现则非零退出
    python tools/check_api_coverage.py --missing-csv out.csv
    python tools/check_api_coverage.py --min-percent 5
"""

from __future__ import annotations

import argparse
import csv
import json
import os
import re
import sys
from collections import OrderedDict, defaultdict

# ``.route("`` 起始标记；参数区用括号配平手工扫描，避免正则被嵌套括号打败。
ROUTE_START_RE = re.compile(r"\.route\(\s*\"(?P<path>[^\"]*)\"")
# 提取方法链：get(h) / post(h) / put(h) / delete(h) / get(h).post(h2)
METHOD_CALL_RE = re.compile(r"\b(get|post|put|delete|patch|head|options)\s*\(\s*([A-Za-z_]\w*)\s*\)")

# crate 目录 -> 路径前缀（与 Java 控制器 base path 对齐）
CRATE_PREFIX = {
    "admin": "/adminapi",
    "public": "/api",
}

# 文档/健康检查等自加端点，不参与覆盖率分母
SELF_ENDPOINT_PREFIXES = ("/openapi/",)
SELF_ENDPOINT_EXACT = {
    "/health", "/login", "/login/refresh",
    "/adminapi/health", "/adminapi/login/refresh", "/adminapi/system/profile/password",
}


def read(path):
    with open(path, "rb") as fh:
        raw = fh.read()
    for enc in ("utf-8", "gbk", "gb18030", "latin-1"):
        try:
            return raw.decode(enc)
        except (UnicodeDecodeError, LookupError):
            continue
    return raw.decode("latin-1", errors="replace")


def strip_comments(text):
    """去掉行注释与块注释，保留字符串。"""
    out = []
    i = 0
    n = len(text)
    while i < n:
        ch = text[i]
        if ch == '"':
            out.append(ch)
            i += 1
            while i < n:
                if text[i] == "\\":
                    out.append(text[i : i + 2])
                    i += 2
                    continue
                out.append(text[i])
                if text[i] == '"':
                    i += 1
                    break
                i += 1
            continue
        if ch == "/" and i + 1 < n:
            if text[i + 1] == "/":
                j = text.find("\n", i)
                i = n if j < 0 else j
                continue
            if text[i + 1] == "*":
                j = text.find("*/", i + 2)
                i = n if j < 0 else j + 2
                continue
        out.append(ch)
        i += 1
    return "".join(out)


def normalize_axum(path):
    """Axum 0.8 uses `:param`; the Java contract uses `{param}`. Normalize both."""
    return re.sub(r":([A-Za-z_][A-Za-z0-9_]*)", r"{\1}", path)


def full_path(prefix, path):
    """Join crate prefix unless the path already carries the module prefix."""
    path = normalize_axum(path)
    if prefix and (path == prefix or path.startswith(prefix + "/")):
        return path
    return join_path(prefix, path)


def join_path(prefix, path):
    parts = [p.strip("/") for p in (prefix, path) if p and p.strip("/")]
    return "/" + "/".join(parts) if parts else "/"


def extract_route_args(text, open_paren):
    """从 ``.route(`` 的左括号开始，返回配平的参数区文本。"""
    depth = 0
    in_str = False
    i = open_paren
    while i < len(text):
        ch = text[i]
        if in_str:
            if ch == "\\":
                i += 2
                continue
            if ch == '"':
                in_str = False
        elif ch == '"':
            in_str = True
        elif ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
            if depth == 0:
                return text[open_paren + 1 : i]
        i += 1
    return text[open_paren + 1 :]


ROUTE_TABLE_RE = re.compile(r"const\s+ROUTE_TABLE\s*:\s*\[[^\]]*\]\s*=\s*\[(.*?)\n\];", re.S)
TABLE_ENTRY_RE = re.compile(r'\(\s*"(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)"\s*,\s*"([^"]+)"\s*,\s*"([^"]*)"')


def extract_route_table(text):
    """Extract (method, mount_path, java_ref) from a literal `ROUTE_TABLE` const.

    Several features (e.g. system/config_basic with 102 endpoints) register their
    handlers in a loop over a specification table instead of writing one
    `.route("literal", ...)` per endpoint.  The table is the feature's contract,
    so coverage must count it too; entries are de-duplicated against literal
    `.route(...)` matches by (method, full_path).
    """
    out = []
    for block in ROUTE_TABLE_RE.findall(text):
        for m in TABLE_ENTRY_RE.finditer(block):
            out.append((m.group(1), m.group(2), m.group(3)))
    return out


def collect_rust_routes(crates_dir):
    """返回 ``[(method, path, java_ref, source)]``。"""
    results = []
    for crate, prefix in CRATE_PREFIX.items():
        root = os.path.join(crates_dir, "api", crate, "src")
        if not os.path.isdir(root):
            continue
        for dirpath, _dirnames, filenames in os.walk(root):
            # `src/root/**` 下的路由挂在**应用根路径**：原 Java 少数控制器的
            # @RequestMapping 不含模块前缀（如 IMConfigController
            # @RequestMapping("im/config/config")、FilesController 的 /img/**），
            # 真实 URL 是 `<应用根>/im/...`，因此这些文件不加 crate 前缀。
            sub = os.path.relpath(dirpath, root).replace("\\", "/")
            eff_prefix = "" if sub == "root" or sub.startswith("root/") else prefix
            for fn in filenames:
                if not fn.endswith(".rs"):
                    continue
                full = os.path.join(dirpath, fn)
                text = strip_comments(read(full))
                rel = os.path.relpath(full, crates_dir)

                for m in ROUTE_START_RE.finditer(text):
                    path = m.group("path")
                    # 取 ``.route(`` 自身的左括号（在路径字符串之前）。
                    open_paren = text.index("(", m.start())
                    tail = extract_route_args(text, open_paren)
                    line = text[: m.start()].count("\n") + 1
                    for hm in METHOD_CALL_RE.finditer(tail):
                        verb = hm.group(1).upper()
                        handler = hm.group(2)
                        results.append(
                            OrderedDict(
                                [
                                    ("method", verb),
                                    ("path", full_path(eff_prefix, path)),
                                    ("handler", handler),
                                    ("java_ref", ""),
                                    ("source", "%s:%d" % (rel, line)),
                                ]
                            )
                        )
                # 3) 规格表注册（ROUTE_TABLE + 循环注册的 feature，如 config_basic）
                table_line = text.find("const ROUTE_TABLE")
                tline = text[:table_line].count("\n") + 1 if table_line >= 0 else 0
                for verb, mount, java_ref in extract_route_table(text):
                    results.append(
                        OrderedDict(
                            [
                                ("method", verb.upper()),
                                ("path", full_path(eff_prefix, mount)),
                                ("handler", "(ROUTE_TABLE)"),
                                ("java_ref", java_ref),
                                ("source", "%s:%d" % (rel, tline)),
                            ]
                        )
                    )
    # 去重（同一路由可能在多处 merge）
    seen = {}
    for r in results:
        key = (r["method"], r["path"])
        if key not in seen:
            seen[key] = r
    return list(seen.values())


def is_self_endpoint(path):
    if path in SELF_ENDPOINT_EXACT:
        return True
    return any(path.startswith(p) for p in SELF_ENDPOINT_PREFIXES)


def load_contract(path):
    with open(path, encoding="utf-8") as fh:
        return json.load(fh)


def analyse(contract, rust_routes):
    have = {(r["method"], r["path"]) for r in rust_routes}
    have_norm = {(m, p.rstrip("/") or "/") for m, p in have}

    implemented = []
    missing = []
    for ep in contract["endpoints"]:
        key = (ep["method"].upper(), ep["path"])
        norm = (key[0], ep["path"].rstrip("/") or "/")
        if key in have or norm in have_norm:
            implemented.append(ep)
        else:
            missing.append(ep)

    contract_keys = {(e["method"].upper(), e["path"]) for e in contract["endpoints"]}
    contract_keys_norm = {(m, p.rstrip("/") or "/") for m, p in contract_keys}
    extra = [
        r
        for r in rust_routes
        if (r["method"], r["path"]) not in contract_keys
        and (r["method"], r["path"].rstrip("/") or "/") not in contract_keys_norm
    ]

    by_module = defaultdict(lambda: [0, 0])
    for ep in contract["endpoints"]:
        by_module[ep["module"]][0] += 1
    for ep in implemented:
        by_module[ep["module"]][1] += 1

    return {
        "total": len(contract["endpoints"]),
        "implemented": len(implemented),
        "missing": missing,
        "extra": extra,
        "by_module": dict(by_module),
    }


def main(argv=None):
    # Windows 控制台默认 GBK，中文报告会乱码；强制 UTF-8 输出。
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass

    here = os.path.dirname(os.path.abspath(__file__))
    rust_root = os.path.dirname(here)
    crates = os.path.join(rust_root, "crates")
    default_contract = os.path.join(rust_root, "docs", "api-contract.json")

    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--contract", default=default_contract)
    ap.add_argument("--crates", default=crates)
    ap.add_argument("--strict", action="store_true", help="存在未实现接口时非零退出")
    ap.add_argument("--min-percent", type=float, default=None)
    ap.add_argument("--missing-csv", default=None)
    ap.add_argument("--json", action="store_true")
    args = ap.parse_args(argv)

    if not os.path.isfile(args.contract):
        print(
            "契约文件缺失：%s\n请先运行 python tools/java_api_parser.py --all"
            % args.contract,
            file=sys.stderr,
        )
        return 2

    contract = load_contract(args.contract)
    rust_routes = collect_rust_routes(args.crates)
    report = analyse(contract, rust_routes)

    real_extra = [r for r in report["extra"] if not is_self_endpoint(r["path"])]

    print("=" * 68)
    print("TigShop Rust API 覆盖率报告")
    print("=" * 68)
    print("契约版本          : %s" % contract.get("version", "?"))
    print("契约接口总数      : %d" % report["total"])
    print("Rust 已实现       : %d" % report["implemented"])
    pct = 100.0 * report["implemented"] / max(1, report["total"])
    print("覆盖率            : %.1f%%" % pct)
    print("未实现            : %d" % len(report["missing"]))
    print("Rust 注册路由总数 : %d" % len(rust_routes))
    print("自加端点(非契约)  : %d" % (len(report["extra"]) - len(real_extra)))
    print("疑似多余路由      : %d" % len(real_extra))
    print()
    print("按模块：")
    for mod in sorted(report["by_module"]):
        t, i = report["by_module"][mod]
        print("  %-10s %4d / %-4d  %5.1f%%" % (mod, i, t, 100.0 * i / max(1, t)))

    if real_extra:
        print()
        print("疑似多余路由（Rust 有、契约无，需人工确认）：")
        for r in sorted(real_extra, key=lambda x: x["path"])[:20]:
            print("  %-6s %-52s %s" % (r["method"], r["path"], r["source"]))

    if report["missing"]:
        print()
        print("未实现接口按模块分布：")
        by_mod = defaultdict(int)
        for ep in report["missing"]:
            by_mod[ep["module"]] += 1
        for mod in sorted(by_mod):
            print("  %-10s %d" % (mod, by_mod[mod]))
        print()
        print("未实现接口示例（前 15 条）：")
        for ep in report["missing"][:15]:
            print(
                "  %-6s %-50s %s"
                % (ep["method"], ep["path"], ep.get("summary", ""))
            )

    if args.missing_csv:
        cols = ["module", "method", "path", "summary", "tag", "auth", "permissions", "controller", "handler", "source"]
        with open(args.missing_csv, "w", encoding="utf-8-sig", newline="") as fh:
            w = csv.DictWriter(fh, fieldnames=cols, extrasaction="ignore")
            w.writeheader()
            for ep in report["missing"]:
                row = dict(ep)
                row["permissions"] = ",".join(ep.get("permissions") or [])
                w.writerow(row)
        print()
        print("已写出未实现清单：%s" % args.missing_csv)

    if args.json:
        out = os.path.join(rust_root, "docs", "api-coverage.json")
        with open(out, "w", encoding="utf-8") as fh:
            json.dump(
                {
                    "contract_version": contract.get("version"),
                    "total": report["total"],
                    "implemented": report["implemented"],
                    "percent": round(pct, 1),
                    "by_module": report["by_module"],
                    "missing": report["missing"],
                    "extra_routes": real_extra,
                },
                fh,
                ensure_ascii=False,
                indent=2,
            )
        print("已写出覆盖率报告：%s" % out)

    if args.min_percent is not None and pct < args.min_percent:
        print(
            "\n覆盖率 %.1f%% 低于要求 %.1f%%" % (pct, args.min_percent),
            file=sys.stderr,
        )
        return 1

    if args.strict and report["missing"]:
        print(
            "\n仍有 %d 个接口未实现" % len(report["missing"]), file=sys.stderr
        )
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
