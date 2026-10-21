#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""为尚未开工的切片批量生成**可编译骨架** + 注册记录。

为什么需要它
------------
`register_slices.py` 只往共享文件里插注册行，它要求
`crates/**/sliceNNN.rs` **已经存在**，否则 `pub mod sliceNNN;` 直接
`E0583 file not found for module`。而 `docs/slices.json` 里 180+ 个切片
还没有任何产出文件，逐片手写 5 个骨架文件在主会话里不可持续。

本工具按项目既有样板（`slice187` / `slice021` 的形状）生成：

* `crates/core/src/models/sliceNNN.rs`
* `crates/infrastructure/src/db/repo/sliceNNN.rs`
* `crates/services/src/sliceNNN.rs`
* `crates/api/admin/src/routes/sliceNNN.rs`（切片含 adminapi 时）
* `crates/api/public/src/routes/sliceNNN.rs`（切片含 api 时）
* `docs/registrations/sliceNNN.json`

**幂等**：已存在的文件一律不覆盖（域实现者的产出永远安全）。
生成后仍需主会话执行 `python tools/register_slices.py --file ...` 落注册行。

用法::

    cd rust
    python tools/scaffold_slices.py slice-042 slice-100
    python tools/scaffold_slices.py --from-coverage-gap --limit 12   # 缺口最小的若干片
"""
from __future__ import annotations

import argparse
import io
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)
CRATES = os.path.join(RUST, "crates")
DOCS = os.path.join(RUST, "docs")

ADMIN_PREFIX = "/adminapi"
PUBLIC_PREFIX = "/api"


def write_new(path, text):
    """仅在文件不存在时写入，返回是否新建。"""
    if os.path.isfile(path):
        return False
    if path.endswith(".rs"):
        # 生成物自检：markdown 表格若漏掉 `//! ` 前缀会变成非法 Rust token。
        for ln in text.split("\n"):
            if ln.lstrip().startswith("|"):
                raise ValueError("生成的 %s 含未加 `//! ` 前缀的表格行: %r" % (path, ln[:60]))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with io.open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(text)
    return True


def load_slices():
    with io.open(os.path.join(DOCS, "slices.json"), encoding="utf-8") as fh:
        return json.load(fh)


def gap_counts():
    """返回 ``{(method, path)}`` 仍缺的接口集合（来自覆盖率产物）。"""
    p = os.path.join(DOCS, "api-coverage.json")
    if not os.path.isfile(p):
        return None
    with io.open(p, encoding="utf-8") as fh:
        cov = json.load(fh)
    return {(m["method"], m["path"]) for m in cov.get("missing", [])}


def api_table(apis, prefix):
    rows = []
    for a in apis:
        stripped = a["path"]
        if prefix and stripped.startswith(prefix):
            stripped = stripped[len(prefix):] or "/"
        rows.append(
            "//! | `%s %s` | `%s %s` | `%s.%s` | %s |"
            % (
                a["method"],
                stripped,
                a["method"],
                a["path"],
                a.get("controller", ""),
                a.get("handler", ""),
                a.get("auth", ""),
            )
        )
    # 每行都必须带 `//! ` 前缀，否则表格会掉出文档注释变成 Rust 代码（E0768/E0583）。
    return "\n".join(rows)


def models_doc(name, tag, sid):
    return (
        "//! %s 域 DTO / VO（%s）。\n"
        "//!\n"
        "//! 约定：`#[serde(rename_all = \"camelCase\")]`；金额 `rust_decimal::Decimal`；\n"
        "//! 时间毫秒 `i64`；可选数值用 `Option<...>`，与 Java VO 的 `null` 语义一致。\n"
        %
        (tag, sid)
    )


def repo_doc(name, tag, sid):
    cap = name[0].upper() + name[1:]
    return (
        "//! %s 域仓储（%s）骨架。\n"
        "//!\n"
        "//! 表名/列名必须来自权威 schema `java/database/pure.sql`\n"
        "//! （`cd rust && python tools/schema_parser.py --table <表名>`）；\n"
        "//! SQL 一律运行时 `sqlx::query_as`，**不用** `query!` 宏；每条 SQL 单行字面量。\n"
        "\n"
        "use sqlx::MySqlPool;\n"
        "\n"
        "/// 仓储（持有连接池）。\n"
        "#[derive(Clone)]\n"
        "pub struct %sRepository {\n"
        "    pool: MySqlPool,\n"
        "}\n"
        "\n"
        "impl %sRepository {\n"
        "    #[must_use]\n"
        "    pub fn new(pool: MySqlPool) -> Self {\n"
        "        Self { pool }\n"
        "    }\n"
        "\n"
        "    /// 暴露连接池（供服务层做事务编排）。\n"
        "    #[must_use]\n"
        "    pub fn pool_clone(&self) -> MySqlPool {\n"
        "        self.pool.clone()\n"
        "    }\n"
        "}\n"
        %
        (tag, sid, cap, cap)
    )


def services_doc(name, tag, sid):
    cap = name[0].upper() + name[1:]
    return (
        "//! %s 域服务（%s）骨架。\n"
        "//!\n"
        "//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。\n"
        "//! 详见 `rust/docs/prompts/%s.md`。\n"
        "\n"
        "use std::sync::Arc;\n"
        "\n"
        "use tigshop_infrastructure::db::Repositories;\n"
        "\n"
        "/// %s 域服务。\n"
        "pub struct %sService {\n"
        "    repos: Arc<Repositories>,\n"
        "}\n"
        "\n"
        "impl %sService {\n"
        "    #[must_use]\n"
        "    pub fn new(repos: Arc<Repositories>) -> Self {\n"
        "        Self { repos }\n"
        "    }\n"
        "\n"
        "    /// 本片仓储。\n"
        "    #[must_use]\n"
        "    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::%s::%sRepository {\n"
        "        &self.repos.%s\n"
        "    }\n"
        "}\n"
        %
        (tag, sid, sid, tag, cap, cap, name, cap, name)
    )


def routes_doc(tag, sid, side, java_refs, apis):
    if side == "admin":
        state, prefix, mount = "AdminState", ADMIN_PREFIX, "`/adminapi`"
    else:
        state, prefix, mount = "PublicState", PUBLIC_PREFIX, "`/api`"
    return (
        "//! %s —— %s路由（%s）骨架。\n"
        "//!\n"
        "//! 原 Java：%s\n"
        "//!\n"
        "//! ## 原 Java 路径对照表（本模块挂载于 %s 之下）\n"
        "//!\n"
        "//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |\n"
        "//! |-----------|------------------|----------------|-----------|\n"
        "%s\n"
        "//!\n"
        "//! 状态：骨架已注册并可编译，handler 待域实现者填充。\n"
        "//! 路由串写**去掉 %s 前缀**后的形式，`RouteDoc.path` 写完整契约路径。\n"
        "\n"
        "use axum::Router;\n"
        "\n"
        "use super::super::%s;\n"
        "\n"
        "/// 域路由（挂载于 %s 下）。\n"
        "pub fn routes(state: &%s) -> Router<%s> {\n"
        "    let _ = state;\n"
        "    Router::new()\n"
        "}\n"
        "\n"
        "/// 已实现路由清单（覆盖率统计用）。\n"
        "#[must_use]\n"
        "pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {\n"
        "    Vec::new()\n"
        "}\n"
        %
        (
            tag,
            "管理端" if side == "admin" else "商城前台",
            sid,
            java_refs,
            mount,
            api_table(apis, prefix),
            mount,
            state,
            mount,
            state,
            state,
        )
    )


def java_ref_lines(sl):
    ctrls = sl.get("controllers") or []
    srcs = sl.get("java_sources") or []
    out = []
    for c in ctrls:
        same = [s for s in srcs if c in s]
        loc = same[0] if same else (srcs[0] if srcs else "")
        out.append("`%s`（`%s`）" % (c, loc.replace("\\", "/") if loc else "见 docs/slices.json"))
    return "；".join(out) if out else "见 docs/slices.json"


def scaffold(sl, created):
    sid = sl["slice_id"]
    name = sid.replace("-", "")
    if not re.fullmatch(r"slice\d{3}", name):
        print("跳过非标准切片号：%s" % sid)
        return
    tag = sl.get("tag", "")
    mods = sl.get("modules") or []
    has_admin = "adminapi" in mods
    has_public = "api" in mods
    apis = sl.get("apis") or []

    files = {
        os.path.join(CRATES, "core/src/models/%s.rs" % name): models_doc(name, tag, sid),
        os.path.join(CRATES, "infrastructure/src/db/repo/%s.rs" % name): repo_doc(name, tag, sid),
        os.path.join(CRATES, "services/src/%s.rs" % name): services_doc(name, tag, sid),
    }
    if has_admin:
        files[os.path.join(CRATES, "api/admin/src/routes/%s.rs" % name)] = routes_doc(
            tag, sid, "admin", java_ref_lines(sl), apis
        )
    if has_public:
        files[os.path.join(CRATES, "api/public/src/routes/%s.rs" % name)] = routes_doc(
            tag, sid, "public", java_ref_lines(sl), apis
        )
    reg = os.path.join(DOCS, "registrations/%s.json" % name)
    files[reg] = json.dumps(
        OrderedDictish(
            slice_id=name,
            tag=tag,
            has_admin=has_admin,
            has_public=has_public,
            core_models_mod="pub mod %s;" % name,
        ),
        ensure_ascii=False,
        indent=2,
    ) + "\n"

    for path, text in files.items():
        rel = os.path.relpath(path, RUST).replace("\\", "/")
        if write_new(path, text):
            created.append(rel)
        else:
            print("  已存在，跳过：%s" % rel)


def OrderedDictish(**kw):
    """json.dumps 的 dict 在 py3.7+ 保序，此函数只为可读性留个名字。"""
    return kw


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("slices", nargs="*", help="slice-042 形式的切片号")
    ap.add_argument("--from-coverage-gap", action="store_true", help="按剩余接口数升序自动选取")
    ap.add_argument("--limit", type=int, default=8)
    ap.add_argument("--min-gap", type=int, default=1)
    args = ap.parse_args(argv)

    slices = load_slices()
    by_id = {s["slice_id"]: s for s in slices}

    targets = []
    for sid in args.slices:
        if sid not in by_id:
            print("未知切片号：%s" % sid, file=sys.stderr)
            return 2
        targets.append(by_id[sid])

    if args.from_coverage_gap:
        gaps = gap_counts()
        if gaps is None:
            print("缺少 docs/api-coverage.json，请先跑 check_api_coverage.py --json", file=sys.stderr)
            return 2
        ranked = []
        for s in slices:
            miss = [a for a in s.get("apis", []) if (a["method"], a["path"]) in gaps]
            if args.min_gap <= len(miss):
                ranked.append((len(miss), s))
        ranked.sort(key=lambda t: (t[0], t[1]["slice_id"]))
        targets.extend(s for _n, s in ranked[: args.limit])

    if not targets:
        print("没有目标切片", file=sys.stderr)
        return 2

    created = []
    for sl in targets:
        print("=== %s %s" % (sl["slice_id"], sl.get("tag", "")))
        scaffold(sl, created)

    print("\n新建 %d 个文件：" % len(created))
    for c in created:
        print("  + " + c)
    print("\n下一步（主会话执行）：")
    for sl in targets:
        print("  python tools/register_slices.py --file docs/registrations/%s.json"
              % sl["slice_id"].replace("-", ""))
    return 0


if __name__ == "__main__":
    sys.exit(main())
