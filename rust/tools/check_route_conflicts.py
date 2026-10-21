#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""检测切片路由与既有内联路由之间的重复注册（axum merge 会 panic）。

axum 0.8 的 `Router::merge` 遇到同一 (method, path) 重复注册会**在运行启动时
panic**（不是编译错误），所以并行合并切片前必须先跑本脚本。

用法::

    cd rust && python tools/check_route_conflicts.py            # 报告
    cd rust && python tools/check_route_conflicts.py --strict   # 有冲突则非零退出
"""
from __future__ import annotations

import argparse
import os
import re
import sys
from collections import defaultdict

try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)
CRATES = os.path.join(RUST, "crates")

ROUTE_RE = re.compile(r'\.route\(\s*"([^"]+)"')
METHOD_RE = re.compile(r"\b(get|post|put|delete|patch|head|options)\s*\(\s*([A-Za-z_]\w*)\s*\)")

SIDES = [
    ("admin", "/adminapi", "api/admin/src"),
    ("public", "/api", "api/public/src"),
]

# 共享文件里显式注册的内联路由（lib.rs 的 .route，不参与并行的域文件）
INLINE_FILES = {
    "admin": ["api/admin/src/lib.rs"],
    "public": ["api/public/src/lib.rs"],
}


def read(path):
    with open(path, "rb") as fh:
        return fh.read().decode("utf-8", "replace")


def strip_comments(text):
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    text = re.sub(r"(?m)//[^\n]*", "", text)
    return text


def balance_args(text, start):
    """从 `start`（应为 '(' 位置）起返回配平的参数区文本（含括号内内容）。"""
    depth = 0
    in_str = False
    i = start
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
                return text[start + 1 : i]
        i += 1
    return text[start + 1 :]


def collect(file_path, prefix):
    """从单个文件收集 (method, path) -> handler。"""
    text = strip_comments(read(file_path))
    out = []
    for m in ROUTE_RE.finditer(text):
        path = m.group(1)
        open_paren = text.index("(", m.start())
        tail = balance_args(text, open_paren)
        for hm in METHOD_RE.finditer(tail):
            out.append((hm.group(1).upper(), prefix + path, hm.group(2)))
    return out


def effective_prefix(dirpath, src_root, side_prefix):
    """`src/root/**` 下的路由挂在应用根路径，不加 `/adminapi`|`/api` 前缀。

    原 Java 少数控制器的 @RequestMapping 不含模块前缀（IMConfigController
    `im/config/config`、FilesController `/img/**`、ShortUrlController `/s`），
    Rust 侧对应文件放在 `src/root/` 下，见 `crates/api/*/src/root/mod.rs`。
    """
    sub = os.path.relpath(dirpath, src_root).replace("\\", "/")
    return "" if sub == "root" or sub.startswith("root/") else side_prefix


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--strict", action="store_true")
    args = ap.parse_args(argv)

    only_slices = "--only-slices" in (argv or [])

    owned = defaultdict(list)   # (side, verb, path) -> [(owner, handler)]
    for side, prefix, root in SIDES:
        base = os.path.join(CRATES, root)
        src_root = os.path.join(base, "src")
        for dirpath, _dirs, files in os.walk(base):
            for fn in files:
                if not fn.endswith(".rs"):
                    continue
                full = os.path.join(dirpath, fn)
                rel = os.path.relpath(full, CRATES).replace("\\", "/")
                owner = "SHARED" if rel in INLINE_FILES.get(side, []) else rel
                # 冲突只在**同一个应用内**成立：admin(8080) 与 api(8181)
                # 端口不同，同路径各自注册不会让 axum panic。
                for verb, path, handler in collect(full, effective_prefix(dirpath, src_root, prefix)):
                    owned[(side, verb, path)].append((owner, handler))

    conflicts = {k: v for k, v in owned.items() if len(v) > 1}
    bare = {k: v for k, v in owned.items() if len(set(o for o, _ in v)) > 1}

    print("=" * 72)
    print("axum 路由重复注册检测（重复 -> Router::merge 启动时 panic）")
    print("=" * 72)
    print("扫描到唯一路由 %d 条，其中被多处注册的 %d 条" % (len(owned), len(conflicts)))

    if not conflicts:
        print("\n✅ 无重复注册")
        return 0

    print("\n冲突清单（按路径）：")
    for key in sorted(conflicts, key=lambda x: x[2]):
        side, verb, path = key
        print("  [%s] %-6s %-52s" % (side, verb, path))
        for o, h in conflicts[key]:
            print("           <- %s  (%s)" % (o, h))

    print("\n跨文件冲突（真正会导致 panic 的）: %d 条" % len(bare))
    for key in sorted(bare, key=lambda x: x[2]):
        side, verb, path = key
        print("  [%s] %-6s %s" % (side, verb, path))

    if args.strict and bare:
        print("\n存在跨文件重复路由，合并前必须去重", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
