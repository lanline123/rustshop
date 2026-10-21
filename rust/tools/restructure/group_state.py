#!/usr/bin/env python3
"""Stage 2: group flat `<ctx>_<feature>` wiring into per-context bundles.

Before: `AdminState { identity_auth, catalog_product, ... }` (74 fields),
        `services/<ctx>/mod.rs` has no `Services` struct.
After : `services/<ctx>/mod.rs` owns `Services { pub <feature>: Arc<XService> }`,
        `AdminState`/`PublicState` hold `Arc<ctx::Services>` (one field per ctx),
        `bin/main.rs` builds one `Services` bundle per context.

Run on the already-migrated tree:
    python tools/restructure/group_state.py
"""
from __future__ import annotations

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


def service_type(ctx, feature):
    p = os.path.join(CR, "services/src", ctx, feature + ".rs")
    if not os.path.isfile(p):
        return None
    m = re.search(r"pub struct (\w+Service)\b", read(p))
    return m.group(1) if m else None


def main() -> int:
    main_path = os.path.join(CR, "bin/src/main.rs")
    text = read(main_path)

    # 1) construction statements: let <ctx>_<feature> = Arc::new(<Type>::new(args));
    ctor_re = re.compile(r"let\s+(\w+)\s*=\s*Arc::new\((\w+)::new\((.*?)\)\);\n", re.S)
    ctors = []
    for m in ctor_re.finditer(text):
        var, ty, args = m.group(1), m.group(2), m.group(3)
        if not ty.endswith("Service"):
            continue
        ctx = None
        feature = None
        for c in CONTEXTS:
            if var.startswith(c + "_"):
                ctx, feature = c, var[len(c) + 1:]
                break
        if not ctx:
            continue
        if service_type(ctx, feature) is None:
            print("WARN: no service type for", var)
            continue
        ctors.append({"var": var, "ctx": ctx, "feature": feature})
    print("grouping %d services across %d contexts" % (
        len(ctors), len({c["ctx"] for c in ctors})))

    rename = {c["var"]: c["feature"] for c in ctors}

    def rename_vars(s: str) -> str:
        for old in sorted(rename, key=len, reverse=True):
            s = re.sub(r"\b%s\b" % old, rename[old], s)
        return s

    # 2) rewrite construction block
    def ctor_sub(m):
        var, ty, args = m.group(1), m.group(2), m.group(3)
        if var not in rename:
            return m.group(0)
        return "let %s = Arc::new(%s::new(%s));\n" % (rename[var], ty, rename_vars(args))

    text = ctor_re.sub(ctor_sub, text)

    # 3) Services structs in services/<ctx>/mod.rs
    by_ctx = {}
    for c in ctors:
        by_ctx.setdefault(c["ctx"], []).append(c["feature"])
    for ctx in CONTEXTS:
        feats = sorted(by_ctx.get(ctx, []))
        if not feats:
            continue
        mod_path = os.path.join(CR, "services/src", ctx, "mod.rs")
        t = read(mod_path)
        t = t.split("#[derive(Clone)]")[0].rstrip() + "\n"
        L = [t.rstrip(), "", "use std::sync::Arc;", "",
             "/// %s 领域服务集合（由组合根装配一次，api 层只持有 `Arc<Services>`）。" % ctx,
             "#[derive(Clone)]", "pub struct Services {"]
        for f in feats:
            st = service_type(ctx, f)
            L.append("    pub %s: Arc<%s::%s>," % (f, f, st))
        L += ["}", ""]
        write(mod_path, "\n".join(L))

    # 4) state struct definitions
    def patch_state(path, name):
        t = read(path)
        idx = t.find("pub struct %s {" % name)
        if idx < 0:
            raise SystemExit("struct %s missing" % name)
        # find ctx set from current flat field names
        brace = t.index("{", idx)
        _, end = block_bounds(t, brace)
        body = t[brace + 1:end - 1]
        flat_fields = re.findall(r"\b([a-z][a-z0-9_]*)\s*:", body)
        ctxs = []
        for f in flat_fields:
            for c in CONTEXTS:
                if f.startswith(c + "_") and c not in ctxs:
                    ctxs.append(c)
        ctxs = [c for c in CONTEXTS if c in ctxs]
        L = ["pub struct %s {" % name]
        for c in ctxs:
            L.append("    pub %s: std::sync::Arc<tigshop_services::%s::Services>," % (c, c))
        L += ["    pub jwt: JwtService,", "}"]
        write(path, t[:idx] + "\n".join(L) + t[end:])
        return ctxs

    admin_ctxs = patch_state(os.path.join(CR, "api/admin/src/lib.rs"), "AdminState")
    public_ctxs = patch_state(os.path.join(CR, "api/public/src/lib.rs"), "PublicState")

    # 5) state literals in main
    flat_ctx = {c["var"]: (c["ctx"], c["feature"]) for c in ctors}
    for state_name, var_name in (("AdminState", "admin_state"), ("PublicState", "public_state")):
        marker = "let %s = %s {" % (var_name, state_name)
        idx = text.find(marker)
        if idx < 0:
            raise SystemExit("literal %s missing" % state_name)
        brace = text.index("{", idx)
        _, end = block_bounds(text, brace)
        body = text[brace + 1:end - 1]
        # collect ctxs from fields that are features
        used = set()
        jwt_expr = "(*jwt).clone()"
        for raw in body.splitlines():
            s = raw.strip().rstrip(",")
            if not s or s.startswith("//"):
                continue
            if ":" in s:
                fname, expr = [x.strip() for x in s.split(":", 1)]
            else:
                fname = expr = s.strip()
            if fname == "jwt":
                jwt_expr = expr
                continue
            info = flat_ctx.get(fname)
            if info:
                used.add(info[0])
        ordered = [c for c in CONTEXTS if c in used]
        L = [marker]
        for c in ordered:
            L.append("        %s: %s.clone()," % (c, c))
        L.append("        jwt: %s," % jwt_expr)
        L.append("    };")
        tail = text[end:]
        if tail.startswith(";"):
            tail = tail[1:]
        text = text[:idx] + "\n".join(L) + tail

    # 6) insert per-ctx bundles right before the first state literal
    bundles = []
    for ctx in CONTEXTS:
        feats = sorted(by_ctx.get(ctx, []))
        if not feats:
            continue
        lines = ["    // ---- %s 域 ----" % ctx,
                 "    let %s = Arc::new(tigshop_services::%s::Services {" % (ctx, ctx)]
        for f in feats:
            lines.append("        %s: %s.clone()," % (f, f))
        lines += ["    });"]
        bundles.append("\n".join(lines))
    marker = text.find("    let admin_state = AdminState {")
    if marker < 0:
        marker = text.find("let admin_state = AdminState {")
    text = text[:marker] + "\n".join(bundles) + "\n\n" + text[marker:]

    write(main_path, text)

    # 7) rewrite state.<flat> -> state.<ctx>.<feature> in api crates
    access_pat = re.compile(r"\bstate\.([a-z][a-z0-9_]*)")
    for crate in ("admin", "public"):
        base = os.path.join(CR, "api", crate, "src")
        for dp, _d, fs in os.walk(base):
            for fn in fs:
                if not fn.endswith(".rs"):
                    continue
                p = os.path.join(dp, fn)
                t = read(p)

                def sub(m):
                    name = m.group(1)
                    info = flat_ctx.get(name)
                    if not info:
                        return m.group(0)
                    return "state.%s.%s" % info

                nt = access_pat.sub(sub, t)
                if nt != t:
                    write(p, nt)

    # 8) residual bare sliceNNN identifiers inside services (local helper fields)
    key_to_feature = {}
    mapping = json.load(open(os.path.join(ROOT, "docs/_migration-map.json"), encoding="utf-8"))
    for s in mapping["slices"]:
        if s.get("real"):
            key_to_feature[s["slice_id"].replace("-", "")] = s["feature"]
    for dp, _d, fs in os.walk(os.path.join(CR, "services/src")):
        for fn in fs:
            if not fn.endswith(".rs"):
                continue
            p = os.path.join(dp, fn)
            t = read(p)
            nt = t
            for k, f in key_to_feature.items():
                nt = re.sub(r"\b%s\b" % k, f, nt)
            if nt != t:
                write(p, nt)

    print("bundles:", {c: len(v) for c, v in by_ctx.items()})
    print("admin ctx fields:", len(admin_ctxs), "| public ctx fields:", len(public_ctxs))
    return 0


if __name__ == "__main__":
    sys.exit(main())
