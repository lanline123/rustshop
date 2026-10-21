#!/usr/bin/env python3
"""Post-migration cleanup of route files that contribute no routes.

Deletes route modules whose `routes()/public_routes()/authorized_routes()` contain
no `.route(...)` calls AND whose `implemented_routes()` is empty — these are
duplicates already served by a baseline feature (e.g. `content::decorate` owns
PcCatFloor/PcNavigation) or mirror modules that expose nothing on this side.

Then regenerates `routes/<ctx>/mod.rs` + `routes/mod.rs` for admin and public from
the files actually present on disk.
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

ROUTES_DIR = {"admin": "crates/api/admin/src/routes",
              "public": "crates/api/public/src/routes"}


def read(p):
    with open(p, encoding="utf-8", errors="replace") as f:
        return f.read()


def write(p, t):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(t)


def strip_comments(text: str) -> str:
    out = []
    i, n = 0, len(text)
    while i < n:
        ch = text[i]
        if ch == '"':
            out.append(ch)
            i += 1
            while i < n:
                if text[i] == "\\":
                    out.append(text[i:i + 2])
                    i += 2
                    continue
                out.append(text[i])
                if text[i] == '"':
                    i += 1
                    break
                i += 1
            continue
        if ch == "/" and i + 1 < n and text[i + 1] == "/":
            j = text.find("\n", i)
            i = n if j < 0 else j
            continue
        out.append(ch)
        i += 1
    return "".join(out)


def contributes(path: str) -> bool:
    code = strip_comments(read(path))
    if ".route(" in code:
        return True
    # non-empty implemented_routes (some RouteDoc::new entries)
    m = re.search(r"pub fn implemented_routes\(\)[^{]*\{(.*?)\n\}", code, re.S)
    return bool(m and "RouteDoc" in m.group(1))


def route_fns(path: str) -> set[str]:
    code = strip_comments(read(path))
    return set(re.findall(r"pub fn (\w+)\(", code))


def gen_crate(crate: str) -> int:
    base = os.path.join(ROOT, ROUTES_DIR[crate])
    state = "AdminState" if crate == "admin" else "PublicState"
    deleted = 0
    info: dict[str, dict[str, set[str]]] = {}
    for ctx in CONTEXTS:
        d = os.path.join(base, ctx)
        if not os.path.isdir(d):
            continue
        for fn in sorted(os.listdir(d)):
            if not fn.endswith(".rs") or fn == "mod.rs":
                continue
            p = os.path.join(d, fn)
            if not contributes(p):
                os.remove(p)
                deleted += 1
                continue
            info.setdefault(ctx, {})[fn[:-3]] = route_fns(p)
        if not info.get(ctx):
            try:
                os.rmdir(d)
            except OSError:
                pass

    ctxs = [c for c in CONTEXTS if c in info]
    for ctx in ctxs:
        feats = sorted(info[ctx])
        if crate == "admin":
            L = ["//! 管理端 %s 路由。" % ctx, ""]
            L += ["pub mod %s;" % f for f in feats]
            L += ["", "use axum::Router;", "", "use super::super::AdminState;", "",
                  "/// 汇总本域路由。",
                  "pub fn routes(state: &AdminState) -> Router<AdminState> {",
                  "    let _ = state;", "    Router::new()"]
            L += ["        .merge(%s::routes(state))" % f for f in feats]
            L += ["}", "", "/// 本域已实现路由（覆盖率统计用）。", "#[must_use]",
                  "pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {",
                  "    let mut out = Vec::new();"]
            L += ["    out.extend(%s::implemented_routes());" % f for f in feats]
            L += ["    out", "}"]
            write(os.path.join(base, ctx, "mod.rs"), "\n".join(L) + "\n")
        else:
            st = [f for f in feats if "routes" in info[ctx][f]]
            pb = [f for f in feats if "public_routes" in info[ctx][f]]
            au = [f for f in feats if "authorized_routes" in info[ctx][f]]
            L = ["//! 商城端 %s 路由。" % ctx, ""]
            L += ["pub mod %s;" % f for f in feats]
            L += ["", "use axum::Router;", "", "use super::super::PublicState;", "",
                  "/// 自带鉴权中间件的域路由（并入 merge_public）。",
                  "pub fn routes(state: &PublicState) -> Router<PublicState> {",
                  "    let _ = state;", "    Router::new()"]
            L += ["        .merge(%s::routes(state))" % f for f in st]
            L += ["}", "", "/// 匿名可访问路由（外层不套 require_auth）。",
                  "pub fn public_routes() -> Router<PublicState> {", "    Router::new()"]
            L += ["        .merge(%s::public_routes())" % f for f in pb]
            L += ["}", "", "/// 需登录路由（外层统一套 require_auth）。",
                  "pub fn authorized_routes() -> Router<PublicState> {", "    Router::new()"]
            L += ["        .merge(%s::authorized_routes())" % f for f in au]
            L += ["}", "", "/// 本域已实现路由（覆盖率统计用）。", "#[must_use]",
                  "pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {",
                  "    let mut out = Vec::new();"]
            L += ["    out.extend(%s::implemented_routes());" % f for f in feats]
            L += ["    out", "}"]
            write(os.path.join(base, ctx, "mod.rs"), "\n".join(L) + "\n")

    L = ["//! %s端路由，按限界上下文分组。" % ("管理" if crate == "admin" else "商城"), ""]
    L += ["pub mod %s;" % c for c in ctxs]
    if crate == "admin":
        L += ["", "use axum::Router;", "", "use super::AdminState;", "",
              "/// 汇总所有域路由。",
              "pub fn merge_all(state: &AdminState) -> Router<AdminState> {",
              "    let _ = state;", "    Router::new()"]
        L += ["        .merge(%s::routes(state))" % c for c in ctxs]
        L += ["}", "", "/// 汇总所有域已实现路由，供覆盖率统计使用。", "#[must_use]",
              "pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {",
              "    let mut out = Vec::new();"]
        L += ["    out.extend(%s::implemented_routes());" % c for c in ctxs]
        L += ["    out", "}"]
    else:
        L += ["", "use axum::Router;", "", "use super::PublicState;", "",
              "/// 公开路由（域模块自带鉴权，外层不得再套 require_auth）。",
              "pub fn merge_public(state: &PublicState) -> Router<PublicState> {",
              "    let _ = state;", "    Router::new()"]
        for c in ctxs:
            L += ["        .merge(%s::routes(state))" % c,
                  "        .merge(%s::public_routes())" % c]
        L += ["}", "", "/// 需登录路由（外层已套 require_auth）。",
              "pub fn merge_authorized(_state: &PublicState) -> Router<PublicState> {",
              "    Router::new()"]
        L += ["        .merge(%s::authorized_routes())" % c for c in ctxs]
        L += ["}", "", "/// 汇总所有域已实现路由，供覆盖率统计使用。", "#[must_use]",
              "pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {",
              "    let mut out = Vec::new();"]
        L += ["    out.extend(%s::implemented_routes());" % c for c in ctxs]
        L += ["    out", "}"]
    write(os.path.join(base, "mod.rs"), "\n".join(L) + "\n")
    return deleted


if __name__ == "__main__":
    total = 0
    for crate in ("admin", "public"):
        total += gen_crate(crate)
    print("deleted non-contributing route files:", total)
