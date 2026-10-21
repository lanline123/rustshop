#!/usr/bin/env python3
"""Group the flat `Repositories` aggregate into per-context bundles.

Before: `Repositories { catalog_product: repo::catalog::product::ProductRepository,
                        catalog_comment: ..., identity_auth: ... }` (90+ fields)
After : `Repositories { catalog: repo::catalog::Repositories,
                        identity: repo::identity::Repositories, ... }`
        and `repo/<ctx>/mod.rs` owns `Repositories { <feature>: <Type> }`.

Also rewrites `self.repos.<ctx>_<feature>` -> `self.repos.<ctx>.<feature>` everywhere.

Run from rust/:  python tools/restructure/group_repositories.py
"""
from __future__ import annotations

import os
import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
CR = os.path.join(ROOT, "crates")
CONTEXTS = ["identity", "merchant", "wholesale", "catalog", "trade", "payment",
            "marketing", "member", "logistics", "content", "system", "im",
            "analytics", "openapi"]
DBMOD = os.path.join(CR, "infrastructure/src/db/mod.rs")


def read(p):
    with open(p, encoding="utf-8", errors="replace") as f:
        return f.read()


def write(p, t):
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(t)


def block_bounds(text, open_idx):
    depth = 0
    for i in range(open_idx, len(text)):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                return open_idx, i + 1
    raise ValueError("unbalanced")


def repo_new_takes_pool(feature: str, ctx: str) -> bool:
    p = os.path.join(CR, "infrastructure/src/db/repo", ctx, feature + ".rs")
    if not os.path.isfile(p):
        return True
    t = read(p)
    m = re.search(r"impl\s+(\w+Repository)\s*\{", t)
    if not m:
        return True
    i = t.index("{", m.start())
    _, end = block_bounds(t, i)
    body = t[i:end]
    return not re.search(r"pub fn new\s*\(\s*\)", body)


def main() -> int:
    text = read(DBMOD)
    i = text.find("pub struct Repositories {")
    if i < 0:
        print("already grouped or struct missing")
        return 1
    brace = text.index("{", i)
    _, end = block_bounds(text, brace)
    body = text[brace + 1:end - 1]
    fields = re.findall(r"pub (\w+):\s*repo::(\w+)::(\w+)::(\w+),", body)
    if not fields:
        print("no flat fields found")
        return 1
    groups: dict[str, list[tuple[str, str, str]]] = {}
    for flat, ctx, feature, ty in fields:
        groups.setdefault(ctx, []).append((feature, ty, flat))
    print("flat fields:", len(fields), "contexts:", len(groups))

    # 1) per-context repo module with its own Repositories
    for ctx in CONTEXTS:
        items = groups.get(ctx)
        if not items:
            continue
        items.sort()
        p = os.path.join(CR, "infrastructure/src/db/repo", ctx, "mod.rs")
        t = read(p)
        mods = sorted(re.findall(r"pub mod (\w+);", t))
        L = ["//! %s 领域仓储。" % ctx, ""]
        L += ["pub mod %s;" % m for m in mods]
        L += ["", "use crate::db::DbPool;", "", "#[derive(Clone)]",
              "pub struct Repositories {"]
        for feature, ty, _flat in items:
            L.append("    pub %s: %s::%s," % (feature, feature, ty))
        L += ["}", "", "impl Repositories {", "    #[must_use]",
              "    pub fn new(pool: DbPool) -> Self {", "        Self {"]
        for feature, ty, _flat in items:
            args = "pool.clone()" if repo_new_takes_pool(feature, ctx) else ""
            L.append("            %s: %s::%s::new(%s)," % (feature, feature, ty, args))
        L += ["        }", "    }", "}"]
        write(p, "\n".join(L) + "\n")

    # 2) central aggregate
    head = text[:i].rstrip()
    tail = text[end:]
    j = tail.find("impl Repositories {")
    if j >= 0:
        b2 = tail.index("{", j)
        _, e2 = block_bounds(tail, b2)
        tail = tail[e2:]
    ctxs = [c for c in CONTEXTS if c in groups]
    L = ["/// 全仓储聚合根（按限界上下文分组）。", "#[derive(Clone)]",
         "pub struct Repositories {"]
    L += ["    pub %s: repo::%s::Repositories," % (c, c) for c in ctxs]
    L += ["}", "", "impl Repositories {", "    #[must_use]",
          "    pub fn new(pool: DbPool) -> Self {", "        Self {"]
    L += ["            %s: repo::%s::Repositories::new(pool.clone())," % (c, c) for c in ctxs]
    L += ["        }", "    }", "}", ""]
    write(DBMOD, head + "\n" + tail.rstrip() + "\n\n" + "\n".join(L))

    # 3) rewrite accessors everywhere (exclude target dirs)
    mapping = {flat: (ctx, feature) for ctx, items in groups.items()
               for feature, _ty, flat in items}
    pat = re.compile(r"\brepos\.(" + "|".join(sorted(map(re.escape, mapping), key=len, reverse=True)) + r")\b")
    changed = 0
    for dp, _d, fs in os.walk(CR):
        parts = dp.split(os.sep)
        if "target" in parts or "target-verify" in parts:
            continue
        for fn in fs:
            if not fn.endswith(".rs"):
                continue
            p = os.path.join(dp, fn)
            t = read(p)
            nt = pat.sub(lambda m: "repos.%s.%s" % mapping[m.group(1)], t)
            if nt != t:
                write(p, nt)
                changed += 1
    print("rewritten files:", changed)
    return 0


if __name__ == "__main__":
    sys.exit(main())
