# -*- coding: utf-8 -*-
"""前端契约校验与规模统计工具。

对应文档：`docs/FRONTEND-MIGRATION-TASKS.md`

子命令
------
  contract   比对前端硬编码的接口路径与 `docs/api-contract.json`
  scale      统计原版前端与重构前端的规模（按域）
  decorate   检查装修模块类型覆盖（原版 vs 重构）

用法
----
  cd rust
  python tools/check_frontend.py contract --app admin-web --strict
  python tools/check_frontend.py contract --app pc-web --strict
  python tools/check_frontend.py scale
  python tools/check_frontend.py decorate

退出码
------
  0 = 通过；1 = 有未命中/差异（仅 `--strict` 时）
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
from collections import Counter, defaultdict

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

# tools/ 的上一级是 rust/，再上一级是仓库根
RUST_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REPO_ROOT = os.path.dirname(RUST_ROOT)

CONTRACT = os.path.join(RUST_ROOT, "docs", "api-contract.json")
ORIG_ADMIN = os.path.join(REPO_ROOT, "java", "view", "admin")
ORIG_PC = os.path.join(REPO_ROOT, "java", "view", "Tigshop-Pc")

APPS = {
    # 2026-10-20 迁移：前端统一放到 rust/web/{admin,pc}，与 rust/crates 区分。
    # 键名保留 admin-web / pc-web 以兼容既有命令与文档。
    "admin-web": {
        "dir": os.path.join(RUST_ROOT, "web", "admin"),
        "prefix": "/adminapi",
    },
    "pc-web": {
        "dir": os.path.join(RUST_ROOT, "web", "pc"),
        "prefix": "/api",
    },
}

SKIP_DIRS = {"node_modules", "dist", ".nuxt", ".output", ".git", "target", "src"}
SRC_EXTS = (".vue", ".ts", ".js")


# --------------------------------------------------------------------------- #
# 通用 IO
# --------------------------------------------------------------------------- #
def read_text(path: str) -> str:
    with open(path, "rb") as fh:
        raw = fh.read()
    for enc in ("utf-8-sig", "utf-8", "gbk", "latin-1"):
        try:
            return raw.decode(enc)
        except (UnicodeDecodeError, LookupError):
            continue
    return raw.decode("utf-8", errors="replace")


def walk_files(base: str, exts=SRC_EXTS, skip=SKIP_DIRS):
    for dirpath, dirnames, files in os.walk(base):
        dirnames[:] = [d for d in dirnames if d not in skip]
        for f in files:
            if f.endswith(exts):
                yield os.path.join(dirpath, f)


def count_lines(paths) -> int:
    n = 0
    for p in paths:
        try:
            with open(p, "rb") as fh:
                n += sum(1 for _ in fh)
        except OSError:
            pass
    return n


def rel(path: str) -> str:
    try:
        return os.path.relpath(path, REPO_ROOT)
    except ValueError:
        return path


# --------------------------------------------------------------------------- #
# contract
# --------------------------------------------------------------------------- #
URL_PATTERNS = (
    re.compile(r"""\burl:\s*['"`]([^'"`]+)['"`]"""),
    re.compile(r"""\bapi(?:List|Get|Save|Delete|Switch|Detail|Batch)?:\s*['"`](/[^'"`]*)['"`]"""),
    re.compile(r"""\bswitchApi:\s*['"`](/[^'"`]*)['"`]"""),
)


def load_contract_paths():
    data = json.load(open(CONTRACT, encoding="utf-8"))
    by_path = defaultdict(set)
    for e in data["endpoints"]:
        by_path[e["path"]].add(e["method"])
    return by_path, data["endpoints"]


def collect_frontend_urls(app_dir: str):
    """收集 src/ 下所有显式接口路径，返回 {path: [引用位置]}。"""
    found = defaultdict(list)
    src = os.path.join(app_dir, "src")
    for p in walk_files(src, exts=SRC_EXTS, skip={"node_modules", "dist"}):
        text = read_text(p)
        for pat in URL_PATTERNS:
            for m in pat.finditer(text):
                url = m.group(1).strip()
                if not url.startswith("/"):
                    continue
                line = text[: m.start()].count("\n") + 1
                found[url].append(f"{rel(p)}:{line}")
    return found


def cmd_contract(args) -> int:
    by_path, _ = load_contract_paths()
    failed = 0
    for app in args.app:
        cfg = APPS[app]
        urls = collect_frontend_urls(cfg["dir"])
        ok, miss = [], []
        for url in sorted(urls):
            full = cfg["prefix"] + url
            (ok if full in by_path else miss).append((url, full, urls[url]))
        print("=" * 78)
        print(f"{app}: 调用 {len(urls)} 条，命中契约 {len(ok)}，未命中 {len(miss)}")
        print("=" * 78)
        for url, full, refs in ok:
            methods = ",".join(sorted(by_path[full]))
            print(f"  OK    {methods:<10} {full}")
        for url, full, refs in miss:
            print(f"  MISS  {'':<10} {full}")
            for r in refs[:3]:
                print(f"          at {r}")
        if miss:
            failed += len(miss)
        print()
    if failed and args.strict:
        print(f"[strict] {failed} 条接口路径在契约中不存在 —— 必须修正")
        return 1
    return 0


# --------------------------------------------------------------------------- #
# scale
# --------------------------------------------------------------------------- #
def domain_table(base: str, skip=()) -> list:
    rows = []
    if not os.path.isdir(base):
        return rows
    for d in sorted(os.listdir(base)):
        full = os.path.join(base, d)
        if not os.path.isdir(full):
            continue
        files = list(walk_files(full, exts=(".vue",), skip=skip))
        if not files:
            continue
        rows.append((d, len(files), count_lines(files)))
    rows.sort(key=lambda r: -r[2])
    return rows


def print_table(title: str, rows: list):
    print("=" * 70)
    print(title)
    print("=" * 70)
    print(f"{'域':<20}{'.vue':>8}{'行数':>10}")
    tp = tl = 0
    for name, n, ln in rows:
        print(f"{name:<20}{n:>8}{ln:>10}")
        tp += n
        tl += ln
    print(f"{'合计':<20}{tp:>8}{tl:>10}")
    return tp, tl


def cmd_scale(args) -> int:
    a = domain_table(os.path.join(ORIG_ADMIN, "src", "views"))
    print_table("原版 admin src/views/（含域内 src/ 组件）", a)
    print()
    p = domain_table(os.path.join(ORIG_PC, "pages"))
    print_table("原版 PC pages/（含域内 src/、components/）", p)

    # 根级页面（pages/index.vue、pages/[...slug].vue 等）
    pages_dir = os.path.join(ORIG_PC, "pages")
    root_files = [
        os.path.join(pages_dir, f)
        for f in sorted(os.listdir(pages_dir))
        if f.endswith(".vue") and os.path.isfile(os.path.join(pages_dir, f))
    ]
    if root_files:
        root_lines = count_lines(root_files)
        print(f"\n原版 PC 根级页面: {len(root_files)} 个 / {root_lines} 行")
        for f in root_files:
            print(f"    {os.path.basename(f):<20}{count_lines([f]):>10}")
    print()

    # 重构版
    for app, cfg in APPS.items():
        src = os.path.join(cfg["dir"], "src")
        files = list(walk_files(src, exts=(".vue",), skip={"node_modules", "dist"}))
        print(f"重构 {app}: .vue={len(files)}  行数={count_lines(files)}")

    # PC 路由页（排除 components/、src/）
    routed = 0
    for dirpath, dirnames, files in os.walk(pages_dir):
        r = os.path.relpath(dirpath, pages_dir)
        parts = [] if r == "." else r.split(os.sep)
        if "components" in parts or "src" in parts:
            continue
        routed += sum(1 for f in files if f.endswith(".vue"))
    print(f"原版 PC 路由页（排除 components/、src/）: {routed}")
    return 0


# --------------------------------------------------------------------------- #
# decorate
# --------------------------------------------------------------------------- #
KEBAB_RE = re.compile(r'type:\s*"(pc-[a-z0-9-]+)"')
# 原版新旧两套首页混用 `==` 与 `===`，两者都要抓
CAMEL_RE = re.compile(r'\.type\s*={2,3}\s*[\'"](pc[A-Z][A-Za-z0-9]+)[\'"]')


def cmd_decorate(args) -> int:
    registry = os.path.join(ORIG_PC, "decorate", "registry", "pc.ts")
    index_vue = os.path.join(ORIG_PC, "pages", "index.vue")

    kebab = set()
    if os.path.isfile(registry):
        kebab = set(KEBAB_RE.findall(read_text(registry)))

    camel = set()
    if os.path.isfile(index_vue):
        camel = set(CAMEL_RE.findall(read_text(index_vue)))

    print("=" * 70)
    print("原版 PC 装修模块类型")
    print("=" * 70)
    print(f"kebab（registry/pc.ts）: {len(kebab)}")
    for t in sorted(kebab):
        print(f"    {t}")
    print(f"camelCase（pages/index.vue）: {len(camel)}")
    for t in sorted(camel):
        print(f"    {t}")

    # 重构版识别到的类型（同样兼容 == 与 ===）
    new_types = set()
    pc_web = os.path.join(REPO_ROOT, "pc-web", "src")
    for p in walk_files(pc_web, exts=(".vue", ".ts"), skip={"node_modules", "dist"}):
        t = read_text(p)
        new_types |= set(
            re.findall(r'module\.type\s*={2,3}\s*[\'"]([^\'"]+)[\'"]', t)
        )

    all_orig = kebab | camel
    print()
    print(f"重构 pc-web 识别到的类型: {len(new_types)}")
    for t in sorted(new_types):
        mark = "OK  " if t in all_orig else "自造"
        print(f"    [{mark}] {t}")

    missing = all_orig - new_types
    print()
    print(f"未覆盖的原版类型: {len(missing)} / {len(all_orig)}")
    return 0


# --------------------------------------------------------------------------- #
def main() -> int:
    ap = argparse.ArgumentParser(description="前端契约校验与规模统计")
    sub = ap.add_subparsers(dest="cmd", required=True)

    c = sub.add_parser("contract", help="比对前端接口路径与契约")
    c.add_argument("--app", action="append", choices=sorted(APPS), help="可多次指定，默认全部")
    c.add_argument("--strict", action="store_true", help="有未命中则 exit 1")
    c.set_defaults(func=cmd_contract)

    s = sub.add_parser("scale", help="规模统计（按域）")
    s.set_defaults(func=cmd_scale)

    d = sub.add_parser("decorate", help="装修模块类型覆盖")
    d.set_defaults(func=cmd_decorate)

    args = ap.parse_args()
    if getattr(args, "app", None) is None and args.cmd == "contract":
        args.app = sorted(APPS)
    return args.func(args)


if __name__ == "__main__":
    sys.exit(main())
