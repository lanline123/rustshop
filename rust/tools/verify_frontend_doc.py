# -*- coding: utf-8 -*-
"""校验 docs/FRONTEND-MIGRATION-TASKS.md 里引用的数字是否与实测一致。

用法：cd rust && python tools/verify_frontend_doc.py
退出码：0 = 全部一致；1 = 有不一致项
"""
import json
import os
import sys

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

RUST_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REPO_ROOT = os.path.dirname(RUST_ROOT)
DOC = os.path.join(RUST_ROOT, "docs", "FRONTEND-MIGRATION-TASKS.md")

SKIP = {"node_modules", "dist", ".nuxt", ".output"}


def walk(base):
    for dp, dn, fs in os.walk(base):
        dn[:] = [d for d in dn if d not in SKIP]
        for f in fs:
            yield os.path.join(dp, f)


def cnt_vue(base):
    return sum(1 for p in walk(base) if p.endswith(".vue"))


def count_lines(paths):
    n = 0
    for p in paths:
        try:
            with open(p, "rb") as fh:
                n += sum(1 for _ in fh)
        except OSError:
            pass
    return n


def lines_vue(base):
    return count_lines([p for p in walk(base) if p.endswith(".vue")])


def main() -> int:
    if not os.path.isfile(DOC):
        print(f"❌ 文档不存在: {DOC}")
        return 1
    text = open(DOC, encoding="utf-8").read()

    adm_src = os.path.join(REPO_ROOT, "java", "view", "admin", "src")
    adm_views = os.path.join(adm_src, "views")
    pc_pages = os.path.join(REPO_ROOT, "java", "view", "Tigshop-Pc", "pages")

    checks = [
        ("原版 admin .vue", cnt_vue(adm_src), 1329),
        ("重构 admin-web .vue", cnt_vue(os.path.join(REPO_ROOT, "admin-web", "src")), 9),
        ("原版 PC .vue", cnt_vue(os.path.join(REPO_ROOT, "java", "view", "Tigshop-Pc")), 430),
        ("重构 pc-web .vue", cnt_vue(os.path.join(REPO_ROOT, "pc-web", "src")), 10),
        ("admin views .vue", cnt_vue(adm_views), 1166),
        ("admin views 行数", lines_vue(adm_views), 282236),
        ("PC pages .vue", cnt_vue(pc_pages), 200),
        ("PC pages 行数", lines_vue(pc_pages), 68008),
    ]

    contract = os.path.join(RUST_ROOT, "docs", "api-contract.json")
    coverage = os.path.join(RUST_ROOT, "docs", "api-coverage.json")
    if os.path.isfile(contract):
        d = json.load(open(contract, encoding="utf-8"))
        checks.append(("契约端点", len(d["endpoints"]), 1564))
    if os.path.isfile(coverage):
        c = json.load(open(coverage, encoding="utf-8"))
        checks.append(("覆盖率 implemented", c["implemented"], 1564))

    print("=" * 70)
    print("文档数字 vs 实测")
    print("=" * 70)
    bad = 0
    for name, actual, expected in checks:
        ok = actual == expected
        if not ok:
            bad += 1
        print(f"  [{'OK ' if ok else 'BAD'}] {name:<24} 实测={actual:<8} 文档={expected}")

    print()
    # 卫生检查
    stale = [p for p in ("check_frontend_contract.py", "_diag") if p in text]
    print(f"{'❌' if stale else '✅'} 文档无过时引用: {stale or '无'}")

    tool = os.path.join(RUST_ROOT, "tools", "check_frontend.py")
    print(f"{'✅' if os.path.isfile(tool) else '❌'} rust/tools/check_frontend.py 存在")

    print()
    print(f"数字不一致项: {bad}")
    return 1 if bad else 0


if __name__ == "__main__":
    sys.exit(main())
