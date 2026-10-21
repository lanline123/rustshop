#!/usr/bin/env python3
"""Regenerate route aggregators from the feature files on disk.

Route modules are the single source of truth: for every
`api/<crate>/src/routes/<ctx>/<feature>.rs` the script detects which entry
functions it exposes and regenerates:

  admin:  routes/<ctx>/mod.rs (routes + public_routes + implemented_routes)
          routes/mod.rs     (merge_all + merge_public + implemented_routes)
  public: routes/<ctx>/mod.rs (routes + public_routes + anonymous_routes +
                               authorized_routes + implemented_routes)
          routes/mod.rs     (merge_public + merge_authorized + implemented_routes)

It also deletes route files that contribute no route at all AND fixes the
`bin`-independent `api/admin/src/lib.rs` nest of anonymous admin routes.

Entry-function conventions (see docs/ARCHITECTURE.md §4):
  routes(&State)            -> self-auth module merged into protected router
  public_routes(&State)     -> admin anonymous (URL_WHITELIST) routes
  public_routes()           -> public anonymous, stateless
  anonymous_routes(&State)  -> public anonymous, needs state
  authorized_routes()       -> public requires-auth (outer require_auth)
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
STATE = {"admin": "AdminState", "public": "PublicState"}


def read(p):
    with open(p, encoding="utf-8", errors="replace") as f:
        return f.read()


def write(p, t):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(t)


def strip_comments(text):
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


def contributes(path):
    code = strip_comments(read(path))
    if ".route(" in code:
        return True
    m = re.search(r"pub fn implemented_routes\(\)[^{]*\{(.*?)\n\}", code, re.S)
    return bool(m and "RouteDoc" in m.group(1))


def route_fns(path):
    code = strip_comments(read(path))
    return set(re.findall(r"pub fn (\w+)\(", code))


def scan(crate):
    base = os.path.join(ROOT, ROUTES_DIR[crate])
    info = {}
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
                continue
            info.setdefault(ctx, {})[fn[:-3]] = route_fns(p)
        if not info.get(ctx):
            try:
                os.rmdir(d)
            except OSError:
                pass
    return info


def ctx_mod_admin(ctx, fns):
    feats = sorted(fns)
    protected = [f for f in feats if "routes" in fns[f]]
    public = [f for f in feats if "public_routes" in fns[f]]
    L = ["//! 管理端 %s 路由。" % ctx, ""]
    L += ["pub mod %s;" % f for f in feats]
    L += ["", "use axum::Router;", "", "use super::super::AdminState;", "",
          "/// 需登录的域路由（外层统一套 require_auth）。",
          "pub fn routes(state: &AdminState) -> Router<AdminState> {",
          "    let _ = state;", "    Router::new()"]
    L += ["        .merge(%s::routes(state))" % f for f in protected]
    L += ["}", "", "/// 免登录的域路由（对应 Java `URL_WHITELIST`，外层不套 require_auth）。",
          "pub fn public_routes(state: &AdminState) -> Router<AdminState> {",
          "    let _ = state;", "    Router::new()"]
    L += ["        .merge(%s::public_routes(state))" % f for f in public]
    L += ["}", "", "/// 本域已实现路由（覆盖率统计用）。", "#[must_use]",
          "pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {",
          "    let mut out = Vec::new();"]
    L += ["    out.extend(%s::implemented_routes());" % f for f in feats]
    L += ["    out", "}"]
    return "\n".join(L) + "\n"


def ctx_mod_public(ctx, fns):
    feats = sorted(fns)
    selfauth = [f for f in feats if "routes" in fns[f]]
    stateless = [f for f in feats if "public_routes" in fns[f]]
    anon = [f for f in feats if "anonymous_routes" in fns[f]]
    auth = [f for f in feats if "authorized_routes" in fns[f]]
    L = ["//! 商城端 %s 路由。" % ctx, ""]
    L += ["pub mod %s;" % f for f in feats]
    L += ["", "use axum::Router;", "", "use super::super::PublicState;", "",
          "/// 自带鉴权中间件的域路由（并入 merge_public，外层不再套 require_auth）。",
          "pub fn routes(state: &PublicState) -> Router<PublicState> {",
          "    let _ = state;", "    Router::new()"]
    L += ["        .merge(%s::routes(state))" % f for f in selfauth]
    L += ["}", "", "/// 匿名可访问路由（无状态）。",
          "pub fn public_routes() -> Router<PublicState> {", "    Router::new()"]
    L += ["        .merge(%s::public_routes())" % f for f in stateless]
    L += ["}", "", "/// 匿名可访问路由（需要 state，外层不套 require_auth）。",
          "pub fn anonymous_routes(state: &PublicState) -> Router<PublicState> {",
          "    let _ = state;", "    Router::new()"]
    L += ["        .merge(%s::anonymous_routes(state))" % f for f in anon]
    L += ["}", "", "/// 需登录路由（外层统一套 require_auth）。",
          "pub fn authorized_routes() -> Router<PublicState> {", "    Router::new()"]
    L += ["        .merge(%s::authorized_routes())" % f for f in auth]
    L += ["}", "", "/// 本域已实现路由（覆盖率统计用）。", "#[must_use]",
          "pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {",
          "    let mut out = Vec::new();"]
    L += ["    out.extend(%s::implemented_routes());" % f for f in feats]
    L += ["    out", "}"]
    return "\n".join(L) + "\n"


def top_mod_admin(ctxs):
    L = ["//! 管理端路由，按限界上下文分组。", ""]
    L += ["pub mod %s;" % c for c in ctxs]
    L += ["", "use axum::Router;", "", "use super::AdminState;", "",
          "/// 汇总所有需登录路由。",
          "pub fn merge_all(state: &AdminState) -> Router<AdminState> {",
          "    let _ = state;", "    Router::new()"]
    L += ["        .merge(%s::routes(state))" % c for c in ctxs]
    L += ["}", "", "/// 汇总所有免登录路由（`router()` 单独 nest 到 `/adminapi`）。",
          "pub fn merge_public(state: &AdminState) -> Router<AdminState> {",
          "    let _ = state;", "    Router::new()"]
    L += ["        .merge(%s::public_routes(state))" % c for c in ctxs]
    L += ["}", "", "/// 汇总所有已实现路由，供覆盖率统计使用。", "#[must_use]",
          "pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {",
          "    let mut out = Vec::new();"]
    L += ["    out.extend(%s::implemented_routes());" % c for c in ctxs]
    L += ["    out", "}"]
    return "\n".join(L) + "\n"


def top_mod_public(ctxs):
    L = ["//! 商城端路由，按限界上下文分组。", ""]
    L += ["pub mod %s;" % c for c in ctxs]
    L += ["", "use axum::Router;", "", "use super::PublicState;", "",
          "/// 公开路由（域模块自带鉴权或匿名，外层不得再套 require_auth）。",
          "pub fn merge_public(state: &PublicState) -> Router<PublicState> {",
          "    let _ = state;", "    Router::new()"]
    for c in ctxs:
        L += ["        .merge(%s::routes(state))" % c,
              "        .merge(%s::public_routes())" % c,
              "        .merge(%s::anonymous_routes(state))" % c]
    L += ["}", "", "/// 需登录路由（外层已套 require_auth）。",
          "pub fn merge_authorized(_state: &PublicState) -> Router<PublicState> {",
          "    Router::new()"]
    L += ["        .merge(%s::authorized_routes())" % c for c in ctxs]
    L += ["}", "", "/// 汇总所有域已实现路由，供覆盖率统计使用。", "#[must_use]",
          "pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {",
          "    let mut out = Vec::new();"]
    L += ["    out.extend(%s::implemented_routes());" % c for c in ctxs]
    L += ["    out", "}"]
    return "\n".join(L) + "\n"


def ensure_admin_nest():
    """`api/admin/src/lib.rs` 必须把免登录域路由单独 nest 到 /adminapi。"""
    p = os.path.join(CR, "api/admin/src/lib.rs")
    t = read(p)
    if "routes::merge_public(&state)" in t:
        return False
    marker = '.nest("/adminapi", protected)'
    if marker not in t:
        print("WARN: admin lib.rs nest marker not found")
        return False
    t = t.replace(marker, marker + '\n        .nest("/adminapi", routes::merge_public(&state))', 1)
    write(p, t)
    return True


def run():
    totals = {}
    for crate in ("admin", "public"):
        info = scan(crate)
        ctxs = [c for c in CONTEXTS if c in info]
        for ctx in ctxs:
            mod = ctx_mod_admin(ctx, info[ctx]) if crate == "admin" else ctx_mod_public(ctx, info[ctx])
            write(os.path.join(ROOT, ROUTES_DIR[crate], ctx, "mod.rs"), mod)
        if crate == "admin":
            write(os.path.join(ROOT, ROUTES_DIR[crate], "mod.rs"), top_mod_admin(ctxs))
        else:
            write(os.path.join(ROOT, ROUTES_DIR[crate], "mod.rs"), top_mod_public(ctxs))
        totals[crate] = len(ctxs)
    nested = ensure_admin_nest()
    print("route aggregators regenerated: admin ctx=%d public ctx=%d admin_public_nest=%s"
          % (totals["admin"], totals["public"], nested))


if __name__ == "__main__":
    run()
