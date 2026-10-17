#!/usr/bin/env python3
"""Stage 1 restructure: numbered "slice" layout -> domain layout (flat wiring).

- drops empty-scaffold slices,
- moves files into  <layer>/<ctx>/<feature>.rs ,
- rewrites module paths / type names / state+repo field access,
- regenerates mod.rs aggregators, flat Repositories, AdminState/PublicState
  and the composition root variable/field names in bin/src/main.rs.

Structure of state/repository fields stays flat in this stage (named
`<ctx>_<feature>`); stage 2 groups them into per-context bundles.
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="replace")

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
CR = os.path.join(ROOT, "crates")

CONTEXTS = ["identity", "merchant", "wholesale", "catalog", "trade", "payment",
            "marketing", "member", "logistics", "content", "system", "im",
            "analytics", "openapi"]

BASE_MODELS = {
    "auth": ("identity", "auth"), "catalog": ("catalog", "category_all"),
    "content": ("content", "article"), "coupon": ("marketing", "coupon"),
    "decorate": ("content", "decorate"), "logistics": ("logistics", "shipping_list"),
    "member": ("member", "member_lists"), "order": ("trade", "order"),
    "paylog": ("payment", "paylog"), "product": ("catalog", "product"),
    "promotion": ("marketing", "seckill"), "refund_apply": ("payment", "refund_apply"),
    "salesman": ("marketing", "salesman"), "shop": ("merchant", "shop"),
    "time_discount": ("marketing", "time_discount"),
}
BASE_REPO = {
    "admin_user": ("identity", "auth"), "cart": ("trade", "cart"),
    "catalog": ("catalog", "category_all"), "content": ("content", "article"),
    "coupon": ("marketing", "coupon"), "decorate": ("content", "decorate"),
    "logistics": ("logistics", "shipping_list"), "member": ("member", "member_lists"),
    "order": ("trade", "order"), "paylog": ("payment", "paylog"),
    "product": ("catalog", "product"), "refund_apply": ("payment", "refund_apply"),
    "salesman": ("marketing", "salesman"), "seckill": ("marketing", "seckill"),
    "shop": ("merchant", "shop"), "time_discount": ("marketing", "time_discount"),
    "user": ("member", "user"),
}
BASE_SERVICE = {
    "auth": ("identity", "auth"), "balance_pay": ("payment", "balance_pay"),
    "cart": ("trade", "cart"), "catalog": ("catalog", "category_all"),
    "content": ("content", "article"), "coupon": ("marketing", "coupon"),
    "decorate": ("content", "decorate"), "logistics": ("logistics", "shipping_list"),
    "member": ("member", "member_lists"), "order": ("trade", "order"),
    "payment": ("payment", "gateway"), "paylog": ("payment", "paylog"),
    "product": ("catalog", "product"), "refund_apply": ("payment", "refund_apply"),
    "salesman": ("marketing", "salesman"), "seckill": ("marketing", "seckill"),
    "shop": ("merchant", "shop"), "time_discount": ("marketing", "time_discount"),
    "user": ("member", "user"),
}
BASE_ADMIN_ROUTES = {
    "catalog": ("catalog", "category_all"), "content": ("content", "article"),
    "coupon": ("marketing", "coupon"), "decorate": ("content", "decorate"),
    "logistics": ("logistics", "shipping_list"), "member": ("member", "member_lists"),
    "paylog": ("payment", "paylog"), "refund_apply": ("payment", "refund_apply"),
    "salesman": ("marketing", "salesman"), "seckill": ("marketing", "seckill"),
    "shop": ("merchant", "shop"), "time_discount": ("marketing", "time_discount"),
}
BASE_PUBLIC_ROUTES = {
    "balance_pay": ("payment", "balance_pay"), "catalog": ("catalog", "category_all"),
    "content": ("content", "article"), "coupon": ("marketing", "coupon"),
    "decorate": ("content", "decorate"), "logistics": ("logistics", "shipping_list"),
    "member": ("member", "member_lists"), "paylog": ("payment", "paylog"),
    "salesman": ("marketing", "salesman"), "seckill": ("marketing", "seckill"),
    "shop": ("merchant", "shop"), "time_discount": ("marketing", "time_discount"),
}
REPO_FIELD = {
    "admin_users": ("identity", "auth"), "products": ("catalog", "product"),
    "orders": ("trade", "order"), "carts": ("trade", "cart"),
    "users": ("member", "user"), "coupons": ("marketing", "coupon"),
    "articles": ("content", "article"), "catalog": ("catalog", "category_all"),
    "logistics": ("logistics", "shipping_list"), "member": ("member", "member_lists"),
    "seckills": ("marketing", "seckill"), "time_discounts": ("marketing", "time_discount"),
    "paylogs": ("payment", "paylog"), "refund_applies": ("payment", "refund_apply"),
    "shops": ("merchant", "shop"), "decorates": ("content", "decorate"),
    "salesmen": ("marketing", "salesman"),
}
STATE_FIELD = {
    "auth": ("identity", "auth"), "product": ("catalog", "product"),
    "order": ("trade", "order"), "coupon": ("marketing", "coupon"),
    "content": ("content", "article"), "catalog": ("catalog", "category_all"),
    "logistics": ("logistics", "shipping_list"), "member": ("member", "member_lists"),
    "seckill": ("marketing", "seckill"), "time_discount": ("marketing", "time_discount"),
    "paylog": ("payment", "paylog"), "refund_apply": ("payment", "refund_apply"),
    "balance_pay": ("payment", "balance_pay"), "cart": ("trade", "cart"),
    "user": ("member", "user"), "shop": ("merchant", "shop"),
    "decorate": ("content", "decorate"), "salesman": ("marketing", "salesman"),
}
LAYER_DIR = {
    "models": "core/src/models", "repo": "infrastructure/src/db/repo",
    "service": "services/src", "admin_route": "api/admin/src/routes",
    "public_route": "api/public/src/routes", "admin_root": "api/admin/src/root",
    "public_root": "api/public/src/root",
}


def camel(s: str) -> str:
    return "".join(p.capitalize() for p in s.split("_"))


def read(p: str) -> str:
    with open(p, encoding="utf-8", errors="replace") as f:
        return f.read()


def write(p: str, t: str) -> None:
    os.makedirs(os.path.dirname(p) or ".", exist_ok=True)
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(t)


def first_struct(text: str, suffix: str = "") -> str | None:
    """Return the first `pub struct` name; prefer one ending with `suffix`."""
    names = re.findall(r"pub struct (\w+)", text)
    if suffix:
        for n in names:
            if n.endswith(suffix):
                return n
    return names[0] if names else None


def block_bounds(text: str, open_idx: int) -> tuple[int, int]:
    depth = 0
    i = open_idx
    while i < len(text):
        ch = text[i]
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                return open_idx, i + 1
        i += 1
    raise ValueError("unbalanced braces")


class World:
    def __init__(self):
        self.entries: dict[str, dict] = {}     # key -> {ctx,feature,kind,pascal}
        self.by_ctx: dict[str, dict[str, dict[str, str]]] = {}
        self.moves: list[tuple[str, str]] = []
        self.deleted: list[str] = []
        self.deleted_keys: set[str] = set()
        self.flat_var: dict[str, str] = {}     # old var/field -> flat name
        self.flat_field: dict[str, str] = {}   # state/repo field -> flat name

    def flat(self, ctx, feature):
        return "%s_%s" % (ctx, feature)


def build_world() -> World:
    w = World()
    mapping = json.load(open(os.path.join(ROOT, "docs/_migration-map.json"), encoding="utf-8"))
    for s in mapping["slices"]:
        key = s["slice_id"].replace("-", "")
        files = s.get("files") or {}
        if s.get("coded") and not s.get("real"):
            w.deleted.extend(files.values())
            w.deleted_keys.add(key)
            continue
        if not s.get("real"):
            continue
        ctx, feature = s["ctx"], s["feature"]
        pascal = s["type_prefix"]
        w.entries[key] = {"ctx": ctx, "feature": feature, "kind": "slice", "pascal": pascal}
        w.flat_var[key] = w.flat(ctx, feature)
        for layer, rel in files.items():
            dst = "crates/%s/%s/%s.rs" % (LAYER_DIR[layer], ctx, feature)
            w.moves.append((rel, dst))
            w.by_ctx.setdefault(ctx, {}).setdefault(feature, {})[layer] = \
                "%s/%s/%s.rs" % (LAYER_DIR[layer], ctx, feature)
    for table, layer in ((BASE_MODELS, "models"), (BASE_REPO, "repo"),
                         (BASE_SERVICE, "service"),
                         (BASE_ADMIN_ROUTES, "admin_route"),
                         (BASE_PUBLIC_ROUTES, "public_route")):
        for key, (ctx, feature) in table.items():
            rel = "crates/%s/%s.rs" % (LAYER_DIR[layer], key)
            if not os.path.isfile(os.path.join(ROOT, rel)):
                continue
            dst = "crates/%s/%s/%s.rs" % (LAYER_DIR[layer], ctx, feature)
            w.moves.append((rel, dst))
            w.by_ctx.setdefault(ctx, {}).setdefault(feature, {})[layer] = \
                "%s/%s/%s.rs" % (LAYER_DIR[layer], ctx, feature)
            w.entries.setdefault(key, {"ctx": ctx, "feature": feature,
                                       "kind": "baseline", "pascal": camel(feature)})
    # flat names for all entries
    for key, e in w.entries.items():
        w.flat_var.setdefault(key, w.flat(e["ctx"], e["feature"]))
    w.flat_field.update(w.flat_var)
    for old, (ctx, feature) in REPO_FIELD.items():
        w.flat_field[old] = w.flat(ctx, feature)
    for old, (ctx, feature) in STATE_FIELD.items():
        w.flat_field[old] = w.flat(ctx, feature)
    return w


def check_cross_refs(w: World) -> list[str]:
    """Report references to dropped slices that are NOT self-contained.

    Files that the generator rewrites wholesale are exempt (their module lists
    are regenerated), as are `use tigshop_services::sliceNNN::...` imports in
    bin/main and the api lib.rs files (dropped by drop_deleted_imports).
    """
    problems = []
    if not w.deleted_keys:
        return problems
    regenerated = {
        "services/src/lib.rs",
        "core/src/models/mod.rs",
        "infrastructure/src/db/mod.rs",
        "infrastructure/src/db/repo/mod.rs",
        "api/admin/src/routes/mod.rs",
        "api/public/src/routes/mod.rs",
        "api/admin/src/root/mod.rs",
        "api/public/src/root/mod.rs",
    }
    for dp, _d, fs in os.walk(CR):
        parts = dp.split(os.sep)
        if "target" in parts or "target-verify" in parts:
            continue
        for fn in fs:
            if not fn.endswith(".rs"):
                continue
            path = os.path.join(dp, fn)
            rel = os.path.relpath(path, CR).replace("\\", "/")
            if rel in regenerated:
                continue
            if any(rel in d for d in w.deleted):
                continue
            text = read(path)
            for line in text.splitlines():
                stripped = line.strip()
                if rel in ("bin/src/main.rs", "api/admin/src/lib.rs",
                           "api/public/src/lib.rs"):
                    # struct fields / literal fields / service imports are
                    # regenerated or dropped by the patcher.
                    if re.match(r"(pub\s+)?slice\d+\s*:", stripped):
                        continue
                    if re.match(r"let\s+slice\d+\s*=", stripped):
                        continue
                    if stripped.startswith(("use ", "pub use ")) and \
                            any(("::%s" % k) in stripped for k in w.deleted_keys):
                        continue
                if stripped.startswith(("use ", "pub use ", "pub mod ", "mod ")):
                    if any(("::%s" % k) in stripped for k in w.deleted_keys):
                        problems.append("%s: %s" % (rel, stripped[:100]))
                    continue
                if stripped.startswith("//"):
                    continue
                for key in w.deleted_keys:
                    if re.search(r"\b%s\b" % key, line):
                        problems.append("%s:%s %s" % (rel, text[:text.find(line)].count("\n") + 1,
                                                      stripped[:100]))
                        break
    return problems


# ------------------------------------------------------------- rewriting -----
def drop_deleted_imports(w: World) -> None:
    targets = [os.path.join(CR, "bin/src/main.rs"),
               os.path.join(CR, "api/admin/src/lib.rs"),
               os.path.join(CR, "api/public/src/lib.rs")]
    for path in targets:
        if not os.path.isfile(path):
            continue
        lines = read(path).splitlines()
        keep = []
        for ln in lines:
            if any(("::%s" % k) in ln and ln.lstrip().startswith("use ") for k in w.deleted_keys):
                continue
            keep.append(ln)
        write(path, "\n".join(keep) + "\n")


def rewrite_all(w: World) -> None:
    field_pat = re.compile(r"\b(state|repos)\s*\.\s*([a-z][a-z0-9_]*)")

    def fix_field(m):
        prefix, name = m.group(1), m.group(2)
        flat = w.flat_field.get(name)
        return "%s.%s" % (prefix, flat) if flat else m.group(0)

    state_pat = re.compile(r"\bsuper::super::(AdminState|PublicState)\b")
    state_pat1 = re.compile(r"\bsuper::(AdminState|PublicState)\b")

    type_rules = []
    for key, e in w.entries.items():
        if e["kind"] != "slice":
            continue
        num = key[5:]
        type_rules += [
            (re.compile(r"\bSlice%sService\b" % num), "%sService" % e["pascal"]),
            (re.compile(r"\bSlice%sRepository\b" % num), "%sRepository" % e["pascal"]),
            (re.compile(r"\bSlice%sRepo\b" % num), "%sRepository" % e["pascal"]),
        ]

    # ---- single-pass path rewriting (prevents cascading substitutions) ----
    all_keys = list(w.entries)
    all_alt = "|".join(sorted((re.escape(k) for k in all_keys), key=len, reverse=True))
    slice_alt = "|".join(sorted((re.escape(k) for k in all_keys
                                 if w.entries[k]["kind"] == "slice"),
                                key=len, reverse=True))
    prefixed = [
        ("tigshop_core::models::", all_alt),
        ("crate::models::", all_alt),
        ("tigshop_infrastructure::db::repo::", all_alt),
        ("crate::db::repo::", all_alt),
        ("tigshop_services::", all_alt),
    ]
    prefix_pats = [(re.compile(re.escape(p) + "(" + alt + r")\b"), p) for p, alt in prefixed]
    bare_pat = re.compile(r"\b(" + slice_alt + r")::") if slice_alt else None
    super_pat = re.compile(r"\bsuper::(" + slice_alt + r")\b") if slice_alt else None

    def key_target(key: str) -> str:
        e = w.entries[key]
        return "%s::%s" % (e["ctx"], e["feature"])

    # `crate::<module>` (same crate) is only valid for the services crate, where
    # sibling feature modules live under `crate::<ctx>::<feature>`.  Applied as a
    # separate, last pass so `crate::models::...` / `crate::db::repo::...` are
    # already rewritten and cannot be matched again.
    crate_key_pat = re.compile(r"\bcrate::(" + all_alt + r")\b")
    services_dir = os.path.join(CR, "services")

    def bare_prefix(rel_norm: str) -> str:
        """Absolute module prefix for a bare `<sliceNNN>::` reference.

        Bare references come from `use ...::{self, ...}` bindings; in this
        codebase they always point at the **models** module (DTO helpers and
        constants), including inside repository files (e.g. `slice085::now_secs`).
        """
        if rel_norm.startswith("core/src/models/"):
            return "crate::models::"
        return "tigshop_core::models::"

    for dp, _d, fs in os.walk(CR):
        parts = dp.split(os.sep)
        if "target" in parts or "target-verify" in parts:
            continue
        for fn in fs:
            if not fn.endswith(".rs"):
                continue
            path = os.path.join(dp, fn)
            old = read(path)
            out = field_pat.sub(fix_field, old)
            out = state_pat.sub(r"crate::\1", out)
            out = state_pat1.sub(r"crate::\1", out)
            for pat, pfx in prefix_pats:
                out = pat.sub(lambda m, p=pfx: p + key_target(m.group(1)), out)
            if path.startswith(services_dir):
                out = crate_key_pat.sub(lambda m: "crate::" + key_target(m.group(1)), out)
            if bare_pat is not None:
                rel_rel = os.path.relpath(path, CR).replace("\\", "/")
                pfx = bare_prefix(rel_rel)
                # `super::sliceNNN` must be rewritten before the bare `::` rule,
                # otherwise it degrades into `super::<ctx>::<feature>`.
                out = super_pat.sub(lambda m, _p=pfx: _p + key_target(m.group(1)), out)
                out = bare_pat.sub(
                    lambda m, _p=pfx: _p + key_target(m.group(1)) + "::", out)
            for pat, repl in type_rules:
                out = pat.sub(repl, out)
            # repository types re-exported from db/mod.rs (e.g.
            # `tigshop_infrastructure::db::Slice003Repository`) now live in the
            # nested module; expand to the full path after the type rename.
            for key, e in w.entries.items():
                rel = w.by_ctx.get(e["ctx"], {}).get(e["feature"], {}).get("repo")
                if not rel:
                    continue
                st = first_struct(read(os.path.join(CR, rel)), "Repository")
                if not st:
                    continue
                pas = e["pascal"] + "Repository" if e["kind"] == "slice" else st
                out = re.sub(
                    r"tigshop_infrastructure::db::%s\b" % re.escape(pas),
                    "tigshop_infrastructure::db::repo::%s::%s::%s"
                    % (e["ctx"], e["feature"], st), out)
            # bare slice references are only rewritten inside comments/tests so
            # struct field definitions keep their old names for patch_state_struct.
            new_lines = []
            for line in out.splitlines():
                s = line.lstrip()
                if s.startswith("//"):
                    for key, e in w.entries.items():
                        if e["kind"] != "slice":
                            continue
                        line = re.sub(r"\b%s\b(?!\s*::)" % key, e["feature"], line)
                        line = line.replace("slice-%s" % key[5:],
                                            "%s::%s" % (e["ctx"], e["feature"]))
                new_lines.append(line)
            out = "\n".join(new_lines)
            if out != old:
                write(path, out)


# ------------------------------------------------------------ generation -----
def gen_models(w: World) -> None:
    base = os.path.join(CR, LAYER_DIR["models"])
    ctxs = []
    for ctx in CONTEXTS:
        feats = sorted(f for f, l in w.by_ctx.get(ctx, {}).items() if "models" in l)
        if not feats:
            continue
        ctxs.append(ctx)
        body = "//! %s 领域模型（DTO / Query / VO）。\n\n" % ctx
        body += "".join("pub mod %s;\n" % f for f in feats)
        write(os.path.join(base, ctx, "mod.rs"), body)
    old = read(os.path.join(base, "mod.rs"))
    lines = []
    for line in old.splitlines():
        if line.startswith("pub mod "):
            continue
        m = re.match(r"pub use (\w+)::(.*)$", line)
        if m and m.group(1) in w.entries:
            e = w.entries[m.group(1)]
            lines.append("pub use %s::%s::%s" % (e["ctx"], e["feature"], m.group(2)))
        else:
            lines.append(line)
    text = "\n".join(lines).rstrip() + "\n\n" + "".join("pub mod %s;\n" % c for c in ctxs)
    write(os.path.join(base, "mod.rs"), text)


def gen_repos(w: World) -> None:
    base = os.path.join(CR, LAYER_DIR["repo"])
    ctxs = []
    field_lines = []
    ctor_lines = []
    for ctx in CONTEXTS:
        feats = sorted(f for f, l in w.by_ctx.get(ctx, {}).items() if "repo" in l)
        if not feats:
            continue
        ctxs.append(ctx)
        body = ["//! %s 领域仓储。" % ctx, ""]
        body += ["pub mod %s;" % f for f in feats]
        write(os.path.join(base, ctx, "mod.rs"), "\n".join(body) + "\n")
        for f in feats:
            rel = w.by_ctx[ctx][f]["repo"]
            repo_text = read(os.path.join(CR, rel))
            st = first_struct(repo_text, "Repository")
            if not st:
                raise SystemExit("no Repository struct in %s" % rel)
            m_new = re.search(r"impl %s\s*\{(.*?)\n\}" % re.escape(st), repo_text, re.S)
            zero_arg = False
            if m_new:
                mn = re.search(r"pub fn new\s*\(\s*\)", m_new.group(1))
                zero_arg = bool(mn)
            flat = w.flat(ctx, f)
            field_lines.append("    pub %s: repo::%s::%s::%s," % (flat, ctx, f, st))
            ctor_lines.append("            %s: repo::%s::%s::%s::new(%s)," %
                              (flat, ctx, f, st, "" if zero_arg else "pool.clone()"))
    lines = ["//! 仓储层：替代原 Java MyBatis Plus Mapper。", "//!",
             "//! 按限界上下文分组（`repo/<ctx>/<feature>.rs`）。", ""]
    lines += ["pub mod %s;" % c for c in ctxs]
    write(os.path.join(base, "mod.rs"), "\n".join(lines) + "\n")

    dbmod = os.path.join(CR, "infrastructure/src/db/mod.rs")
    text = read(dbmod)
    text = "\n".join(ln for ln in text.splitlines() if not ln.startswith("pub use repo::"))
    idx = text.find("pub struct Repositories")
    if idx > 0:
        head_lines = text[:idx].rstrip().splitlines()
        while head_lines and (head_lines[-1].strip().startswith(("#[", "///"))
                              or not head_lines[-1].strip()):
            head_lines.pop()
        head = "\n".join(head_lines)
        brace = text.index("{", idx)
        _, end = block_bounds(text, brace)
        rest = text[end:]
        ii = rest.find("impl Repositories {")
        if ii >= 0:
            b2 = rest.index("{", ii)
            _, e2 = block_bounds(rest, b2)
            rest = rest[e2:]
        text = head + "\n" + rest
    block = ["/// 全仓储聚合根（按限界上下文分组）。", "#[derive(Clone)]",
             "pub struct Repositories {"] + field_lines + ["}", "",
             "impl Repositories {", "    #[must_use]",
             "    pub fn new(pool: DbPool) -> Self {", "        Self {"] + ctor_lines + \
            ["        }", "    }", "}", ""]
    write(dbmod, text.rstrip() + "\n\n" + "\n".join(block))


def gen_services(w: World) -> None:
    base = os.path.join(CR, LAYER_DIR["service"])
    ctxs = []
    for ctx in CONTEXTS:
        feats = sorted(f for f, l in w.by_ctx.get(ctx, {}).items() if "service" in l)
        if not feats:
            continue
        ctxs.append(ctx)
        body = ["//! %s 领域服务。" % ctx, ""] + ["pub mod %s;" % f for f in feats]
        write(os.path.join(base, ctx, "mod.rs"), "\n".join(body) + "\n")
    lines = ["//! 用例服务层（按限界上下文分组）。", ""]
    lines += ["pub mod %s;" % c for c in ctxs]
    write(os.path.join(base, "lib.rs"), "\n".join(lines) + "\n")


def route_fns(path: str) -> set[str]:
    return set(re.findall(r"pub fn (\w+)\(", read(path)))


def gen_admin_routes(w: World) -> None:
    base = os.path.join(CR, LAYER_DIR["admin_route"])
    info = {}
    for ctx, feats in w.by_ctx.items():
        for f, layers in feats.items():
            if "admin_route" in layers:
                info.setdefault(ctx, {})[f] = route_fns(os.path.join(CR, layers["admin_route"]))
    ctxs = [c for c in CONTEXTS if c in info]
    for ctx in ctxs:
        feats = sorted(info[ctx])
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
    L = ["//! 管理端路由（`/adminapi` 前缀），按限界上下文分组。", ""]
    L += ["pub mod %s;" % c for c in ctxs]
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
    write(os.path.join(base, "mod.rs"), "\n".join(L) + "\n")


def gen_public_routes(w: World) -> None:
    base = os.path.join(CR, LAYER_DIR["public_route"])
    info = {}
    for ctx, feats in w.by_ctx.items():
        for f, layers in feats.items():
            if "public_route" in layers:
                info.setdefault(ctx, {})[f] = route_fns(os.path.join(CR, layers["public_route"]))
    ctxs = [c for c in CONTEXTS if c in info]
    for ctx in ctxs:
        feats = sorted(info[ctx])
        state_feats = [f for f in feats if "routes" in info[ctx][f]]
        pub_feats = [f for f in feats if "public_routes" in info[ctx][f]]
        auth_feats = [f for f in feats if "authorized_routes" in info[ctx][f]]
        L = ["//! 商城端 %s 路由。" % ctx, ""]
        L += ["pub mod %s;" % f for f in feats]
        L += ["", "use axum::Router;", "", "use super::super::PublicState;", "",
              "/// 自带鉴权中间件的域路由（并入 merge_public）。",
              "pub fn routes(state: &PublicState) -> Router<PublicState> {",
              "    let _ = state;", "    Router::new()"]
        L += ["        .merge(%s::routes(state))" % f for f in state_feats]
        L += ["}", "", "/// 匿名可访问路由（外层不套 require_auth）。",
              "pub fn public_routes() -> Router<PublicState> {", "    Router::new()"]
        L += ["        .merge(%s::public_routes())" % f for f in pub_feats]
        L += ["}", "", "/// 需登录路由（外层统一套 require_auth）。",
              "pub fn authorized_routes() -> Router<PublicState> {", "    Router::new()"]
        L += ["        .merge(%s::authorized_routes())" % f for f in auth_feats]
        L += ["}", "", "/// 本域已实现路由（覆盖率统计用）。", "#[must_use]",
              "pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {",
              "    let mut out = Vec::new();"]
        L += ["    out.extend(%s::implemented_routes());" % f for f in feats]
        L += ["    out", "}"]
        write(os.path.join(base, ctx, "mod.rs"), "\n".join(L) + "\n")
    L = ["//! 商城端路由（`/api` 前缀），按限界上下文分组。", ""]
    L += ["pub mod %s;" % c for c in ctxs]
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


def gen_roots(w: World) -> None:
    def classify(path):
        text = read(path)
        prot, pub = set(), set()
        for fn, bucket in (("merge_protected", prot), ("merge_public", pub)):
            i = text.find("pub fn %s" % fn)
            if i < 0:
                continue
            b = text.index("{", i)
            _, e = block_bounds(text, b)
            for m in re.finditer(r"(\w+)::routes", text[b:e]):
                bucket.add(m.group(1))
        return prot, pub

    key_to = {k: (e["ctx"], e["feature"]) for k, e in w.entries.items()}
    for crate, layer_root, state in (
        ("admin", "admin_root", "AdminState"),
        ("public", "public_root", "PublicState"),
    ):
        base = os.path.join(CR, LAYER_DIR[layer_root])
        modpath = os.path.join(base, "mod.rs")
        prot, pub = classify(modpath)
        prot_f = {key_to[k] for k in prot if k in key_to}
        pub_f = {key_to[k] for k in pub if k in key_to}
        info = {}
        for ctx, feats in w.by_ctx.items():
            for f, layers in feats.items():
                if layer_root in layers:
                    info.setdefault(ctx, {})[f] = route_fns(os.path.join(CR, layers[layer_root]))
        ctxs = [c for c in CONTEXTS if c in info]
        for ctx in ctxs:
            feats = sorted(info[ctx])
            p = [f for f in feats if (ctx, f) in prot_f]
            q = [f for f in feats if (ctx, f) in pub_f]
            L = ["//! %s 根级路由（不带模块前缀）。" % ctx, ""]
            L += ["pub mod %s;" % f for f in feats]
            L += ["", "use axum::Router;", "", "use super::super::%s;" % state, "",
                  "/// 需登录的根级路由。",
                  "pub fn merge_protected(state: &%s) -> Router<%s> {" % (state, state),
                  "    let _ = state;", "    Router::new()"]
            L += ["        .merge(%s::routes(state))" % f for f in p]
            L += ["}", "", "/// 免登录的根级路由。",
                  "pub fn merge_public(state: &%s) -> Router<%s> {" % (state, state),
                  "    let _ = state;", "    Router::new()"]
            L += ["        .merge(%s::routes(state))" % f for f in q]
            L += ["}", "", "/// 汇总本域根级已实现路由。", "#[must_use]",
                  "pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {",
                  "    let mut out = Vec::new();"]
            L += ["    out.extend(%s::implemented_routes());" % f for f in feats]
            L += ["    out", "}"]
            write(os.path.join(base, ctx, "mod.rs"), "\n".join(L) + "\n")
        L = ["//! 根级路由（不带模块前缀），按限界上下文分组。", ""]
        L += ["pub mod %s;" % c for c in ctxs]
        L += ["", "use axum::Router;", "", "use super::%s;" % state, "",
              "/// 需登录的根级路由。",
              "pub fn merge_protected(state: &%s) -> Router<%s> {" % (state, state),
              "    let _ = state;", "    Router::new()"]
        L += ["        .merge(%s::merge_protected(state))" % c for c in ctxs]
        L += ["}", "", "/// 免登录的根级路由。",
              "pub fn merge_public(state: &%s) -> Router<%s> {" % (state, state),
              "    let _ = state;", "    Router::new()"]
        L += ["        .merge(%s::merge_public(state))" % c for c in ctxs]
        L += ["}", "", "/// 汇总所有根级已实现路由。", "#[must_use]",
              "pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {",
              "    let mut out = Vec::new();"]
        L += ["    out.extend(%s::implemented_routes());" % c for c in ctxs]
        L += ["    out", "}"]
        write(modpath, "\n".join(L) + "\n")


def patch_state_struct(path: str, name: str, w: World) -> None:
    text = read(path)
    idx = text.find("pub struct %s {" % name)
    if idx < 0:
        raise SystemExit("struct %s not found in %s" % (name, path))
    brace = text.index("{", idx)
    _, end = block_bounds(text, brace)
    body = text[brace + 1:end - 1]
    fields = re.findall(r"pub (\w+):\s*([^,\n]+),", body)
    groups: dict[str, list[tuple[str, str]]] = {}
    for fname, ftype in fields:
        if fname in w.deleted_keys:
            continue
        flat = w.flat_field.get(fname, fname)
        ctx = None
        for e in w.entries.values():
            if w.flat(e["ctx"], e["feature"]) == flat:
                ctx = e["ctx"]
                break
        groups.setdefault(ctx or "misc", []).append((flat, ftype.strip()))
    L = ["pub struct %s {" % name]
    for ctx in CONTEXTS + ["misc"]:
        if ctx not in groups:
            continue
        L.append("    // —— %s ——" % ctx)
        for fname, ftype in groups[ctx]:
            L.append("    pub %s: %s," % (fname, ftype))
    L.append("}")
    write(path, text[:idx] + "\n".join(L) + text[end:])


def patch_main(path: str, w: World) -> None:
    text = read(path)
    # 1) constructor statements
    ctor_re = re.compile(r"let\s+(\w+)\s*=\s*Arc::new\((\w+)::new\((.*?)\)\);\n", re.S)
    removed = []

    def ctor_sub(m):
        var, ty, args = m.group(1), m.group(2), m.group(3)
        if var in w.deleted_keys:
            removed.append(var)
            return ""
        newvar = w.flat_var.get(var, var)
        # rename type for slices
        e = w.entries.get(var)
        if e and e["kind"] == "slice":
            ty = ty.replace("Slice%s" % var[5:], e["pascal"])
        # rename old vars inside args (longest first)
        for old in sorted(w.flat_var, key=len, reverse=True):
            args = re.sub(r"\b%s\b" % old, w.flat_var[old], args)
        return "let %s = Arc::new(%s::new(%s));\n" % (newvar, ty, args)

    text = ctor_re.sub(ctor_sub, text)
    # 2) state literals
    for sname in ("AdminState", "PublicState"):
        marker = "let %s = %s {" % (("admin_state", "AdminState") if sname == "AdminState"
                                    else ("public_state", "PublicState"))
        idx = text.find(marker)
        if idx < 0:
            continue
        brace = text.index("{", idx)
        _, end = block_bounds(text, brace)
        body = text[brace + 1:end - 1]
        groups: dict[str, list[str]] = {}
        for raw in body.splitlines():
            s = raw.strip().rstrip(",").strip()
            if not s or s.startswith("//"):
                continue
            if ":" in s:
                fname, expr = s.split(":", 1)
            else:
                fname = expr = s            # shorthand field: `refund_apply,`
            fname = fname.strip()
            expr = expr.strip()
            if fname in w.deleted_keys:
                continue
            flat = w.flat_field.get(fname, fname)
            for old in sorted(w.flat_var, key=len, reverse=True):
                expr = re.sub(r"\b%s\b" % old, w.flat_var[old], expr)
            ctx = None
            for e in w.entries.values():
                if w.flat(e["ctx"], e["feature"]) == flat:
                    ctx = e["ctx"]
                    break
            groups.setdefault(ctx or "misc", []).append(
                "%s: %s," % (flat, expr))
        L = ["let %s = %s {" % (("admin_state", "AdminState") if sname == "AdminState"
                                else ("public_state", "PublicState"))]
        for ctx in CONTEXTS + ["misc"]:
            if ctx not in groups:
                continue
            L.append("        // —— %s ——" % ctx)
            L += ["        " + x for x in groups[ctx]]
        L.append("    };")
        tail = text[end:]
        if tail.startswith(";"):
            tail = tail[1:]
        text = text[:idx] + "\n".join(L) + tail
    write(path, text)


def execute(w: World) -> None:
    for rel in w.deleted:
        p = os.path.join(ROOT, rel)
        if os.path.isfile(p):
            os.remove(p)
    for src, dst in w.moves:
        s = os.path.join(ROOT, src)
        d = os.path.join(ROOT, dst)
        if not os.path.isfile(s):
            continue
        os.makedirs(os.path.dirname(d), exist_ok=True)
        os.replace(s, d)
    drop_deleted_imports(w)
    rewrite_all(w)
    gen_models(w)
    gen_repos(w)
    gen_services(w)
    gen_admin_routes(w)
    gen_public_routes(w)
    gen_roots(w)
    patch_state_struct(os.path.join(CR, "api/admin/src/lib.rs"), "AdminState", w)
    patch_state_struct(os.path.join(CR, "api/public/src/lib.rs"), "PublicState", w)
    patch_main(os.path.join(CR, "bin/src/main.rs"), w)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()
    w = build_world()
    print("delete files: %d" % len(w.deleted))
    print("move files  : %d" % len(w.moves))
    print("entries     : %d (slices=%d, baseline=%d)" % (
        len(w.entries),
        sum(1 for e in w.entries.values() if e["kind"] == "slice"),
        sum(1 for e in w.entries.values() if e["kind"] == "baseline")))
    problems = check_cross_refs(w)
    if problems:
        print("\nCROSS REFERENCES TO DROPPED SLICES:")
        for p in problems[:40]:
            print("  !", p)
        if len(problems) > 40:
            print("  ... %d more" % (len(problems) - 40))
        return 1
    if args.dry_run:
        for s, d in w.moves:
            print("  %s -> %s" % (s, d))
        return 0
    execute(w)
    print("done")
    return 0


if __name__ == "__main__":
    sys.exit(main())
