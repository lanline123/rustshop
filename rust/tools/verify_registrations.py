#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""校验切片注册的一致性（与 docs/slices.json 的 modules 字段逐片比对）。

为什么需要它
------------
`register_slices.py` 只负责"把行插进去"，但不判断"插得对不对、有没有漏插"。
真实踩过的坑（都是本脚本能自动抓到的）：

* public-only 切片被插进了 `AdminState`（锚点 `slice001: slice001.clone(),`
  在 AdminState / PublicState 里同时存在，工具取了第一个匹配）；
* 因幂等检查是全文件级，`slice021` 的 PublicState 字段被判"已存在"而**漏插**；
* 完全没有 `crates/core/src/models/<name>.rs` 的切片，却被加了 `pub mod` 声明
  → `E0583 file not found for module`。

这些错误都不会让 `cargo check` 失败（漏插只是路由不生效，插错块才会报错），
所以必须用静态一致性检查兜住。

用法::

    cd rust && python tools/verify_registrations.py            # 报告
    cd rust && python tools/verify_registrations.py --strict    # 有不一致则非零退出
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys

try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)
CRATES = os.path.join(RUST, "crates")

F_CORE_MODELS = "core/src/models/mod.rs"
F_SERVICES = "services/src/lib.rs"
F_REPO_MOD = "infrastructure/src/db/repo/mod.rs"
F_DB_MOD = "infrastructure/src/db/mod.rs"
F_ADMIN_MOD = "api/admin/src/routes/mod.rs"
F_PUBLIC_MOD = "api/public/src/routes/mod.rs"
F_ADMIN_LIB = "api/admin/src/lib.rs"
F_PUBLIC_LIB = "api/public/src/lib.rs"
F_MAIN = "bin/src/main.rs"

# 根级路由通道（不带 /adminapi、/api 前缀的切片，见各侧 src/root/mod.rs 注释）。
# 这类切片的路由文件在 src/root/ 下，由 lib.rs 的 router() 直接 merge 到应用根，
# 因此**不能**要求它们出现在 routes/mod.rs 里（否则 axum 会重复注册 panic）。
F_ADMIN_ROOT = "api/admin/src/root/mod.rs"
F_PUBLIC_ROOT = "api/public/src/root/mod.rs"


def read(rel):
    with open(os.path.join(CRATES, rel), "rb") as fh:
        return fh.read().decode("utf-8", "replace")


def block_lines(text, open_anchor):
    """返回以 `open_anchor` 起始的块内行（不含起止行）。"""
    lines = text.split("\n")
    start = None
    for i, ln in enumerate(lines):
        if ln.strip() == open_anchor.strip():
            start = i
            break
    if start is None:
        return None
    indent = len(lines[start]) - len(lines[start].lstrip())
    for i in range(start + 1, len(lines)):
        s = lines[i]
        if not s.strip():
            continue
        cur = len(s) - len(s.lstrip())
        if cur <= indent and s.lstrip().startswith("}"):
            return lines[start + 1 : i]
    return None


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--strict", action="store_true")
    ap.add_argument("--slices", default=os.path.join(RUST, "docs", "slices.json"))
    args = ap.parse_args(argv)

    with open(args.slices, encoding="utf-8") as fh:
        slices = json.load(fh)

    cache = {}

    def get(rel):
        if rel not in cache:
            path = os.path.join(CRATES, rel)
            cache[rel] = read(rel) if os.path.isfile(path) else ""
        return cache[rel]

    core_models = get(F_CORE_MODELS)
    services = get(F_SERVICES)
    repo_mod = get(F_REPO_MOD)
    db_mod = get(F_DB_MOD)
    admin_mod = get(F_ADMIN_MOD)
    public_mod = get(F_PUBLIC_MOD)
    admin_lib = get(F_ADMIN_LIB)
    public_lib = get(F_PUBLIC_LIB)
    admin_root = get(F_ADMIN_ROOT)
    public_root = get(F_PUBLIC_ROOT)
    main_rs = get(F_MAIN)

    admin_state = block_lines(main_rs, "let admin_state = AdminState {") or []
    public_state = block_lines(main_rs, "let public_state = PublicState {") or []
    admin_struct = block_lines(admin_lib, "pub struct AdminState {") or []
    public_struct = block_lines(public_lib, "pub struct PublicState {") or []
    repos_struct = block_lines(db_mod, "pub struct Repositories {") or []
    repos_init = block_lines(db_mod, "Self {") or []

    problems = []
    checked = 0

    for s in slices:
        sid = s["slice_id"]
        name = sid.replace("-", "")           # slice015
        if not re.fullmatch(r"slice\d{3}", name):
            continue
        # 只校验已经有产出文件的切片
        has_route_admin = os.path.isfile(os.path.join(CRATES, "api/admin/src/routes/%s.rs" % name))
        has_route_public = os.path.isfile(os.path.join(CRATES, "api/public/src/routes/%s.rs" % name))
        has_service = os.path.isfile(os.path.join(CRATES, "services/src/%s.rs" % name))
        has_repo = os.path.isfile(os.path.join(CRATES, "infrastructure/src/db/repo/%s.rs" % name))
        if not (has_route_admin or has_route_public or has_service or has_repo
                or os.path.isfile(os.path.join(CRATES, "api/admin/src/root/%s.rs" % name))
                or os.path.isfile(os.path.join(CRATES, "api/public/src/root/%s.rs" % name))):
            continue
        checked += 1

        want_admin = "adminapi" in s["modules"]
        want_public = "api" in s["modules"]
        cap = name[0].upper() + name[1:]

        def need(cond, msg):
            if not cond:
                problems.append("%s: %s" % (name, msg))

        # —— 必定存在的三项 ——
        need("pub mod %s;" % name in services, "services/src/lib.rs 缺 `pub mod %s;`" % name)
        need("pub mod %s;" % name in repo_mod, "repo/mod.rs 缺 `pub mod %s;`" % name)
        # 注意：repos_struct / repos_init 是**行列表**，必须用 any(... in 行) 做子串判断，
        # 不能写成 `"..." in 列表`（那是精确元素相等，永远为假）。
        need(any(("pub %s: %sRepository," % (name, cap)) in l for l in repos_struct),
             "db/mod.rs 的 Repositories 结构缺 `pub %s`" % name)
        need(any(("%s: %sRepository::new(" % (name, cap)) in l for l in repos_init),
             "db/mod.rs 的 Repositories::new 缺 `%s` 初始化" % name)

        # core models：仅当文件存在时才应有 mod 声明
        model_exists = os.path.isfile(os.path.join(CRATES, "core/src/models/%s.rs" % name))
        declared = ("pub mod %s;" % name) in core_models
        if model_exists and not declared:
            problems.append("%s: core/src/models/%s.rs 存在但 models/mod.rs 未声明" % (name, name))
        if declared and not model_exists:
            problems.append("%s: models/mod.rs 声明了 `pub mod %s;` 但文件不存在（E0583）" % (name, name))

        # —— admin / public 两侧必须与 modules 严格一致 ——
        # 路由挂载位置：src/routes/（带模块前缀）或 src/root/（应用根路径）。
        admin_root_file = os.path.isfile(os.path.join(CRATES, "api/admin/src/root/%s.rs" % name))
        public_root_file = os.path.isfile(os.path.join(CRATES, "api/public/src/root/%s.rs" % name))
        admin_mod_text = admin_root if admin_root_file and not has_route_admin else admin_mod
        public_mod_text = public_root if public_root_file and not has_route_public else public_mod
        admin_where = "admin/root/mod.rs" if admin_mod_text is admin_root else "admin/routes/mod.rs"
        public_where = "public/root/mod.rs" if public_mod_text is public_root else "public/routes/mod.rs"

        in_admin_merge = (".merge(%s::routes(state))" % name) in admin_mod_text
        in_public_merge = (".merge(%s::routes(state))" % name) in public_mod_text
        in_admin_state = any(("%s:" % name) in l for l in admin_state)
        in_public_state = any(("%s:" % name) in l for l in public_state)
        in_admin_struct = any(("%s:" % name) in l for l in admin_struct)
        in_public_struct = any(("%s:" % name) in l for l in public_struct)
        in_admin_main = ("pub mod %s;" % name) in admin_mod_text
        in_public_main = ("pub mod %s;" % name) in public_mod_text

        if want_admin:
            need(in_admin_main, "has_admin=true 但 %s 缺 `pub mod %s;`" % (admin_where, name))
            need(in_admin_merge, "has_admin=true 但 %s 缺 `.merge(%s::routes(state))`" % (admin_where, name))
            need(in_admin_struct, "has_admin=true 但 AdminState 缺 `%s` 字段" % name)
            need(in_admin_state, "has_admin=true 但 main.rs 的 admin_state 缺 `%s`" % name)
            if admin_root_file:
                need("root::merge_protected" in admin_lib or "root::merge_public" in admin_lib,
                     "root/ 下存在 %s.rs 但 admin/lib.rs 的 router() 未挂根级通道" % name)
                need((".merge(%s::routes(state))" % name) not in admin_mod,
                     "%s 是根级切片，却又被挂进带前缀的 admin/routes/mod.rs（重复注册会 panic）" % name)
        else:
            need(not in_admin_merge, "has_admin=false 却在 %s 注册了路由" % admin_where)
            need(not in_admin_struct, "has_admin=false 却出现在 AdminState 里")
            need(not in_admin_state, "has_admin=false 却出现在 main.rs 的 admin_state 里")

        if want_public:
            need(in_public_main, "has_public=true 但 %s 缺 `pub mod %s;`" % (public_where, name))
            need(in_public_merge, "has_public=true 但 %s 缺 `.merge(%s::routes(state))`" % (public_where, name))
            need(in_public_struct, "has_public=true 但 PublicState 缺 `%s` 字段" % name)
            need(in_public_state, "has_public=true 但 main.rs 的 public_state 缺 `%s`" % name)
            if public_root_file:
                need("root::merge_protected" in public_lib or "root::merge_public" in public_lib,
                     "root/ 下存在 %s.rs 但 public/lib.rs 的 router() 未挂根级通道" % name)
                need((".merge(%s::routes(state))" % name) not in public_mod,
                     "%s 是根级切片，却又被挂进带前缀的 public/routes/mod.rs（重复注册会 panic）" % name)
        else:
            need(not in_public_merge, "has_public=false 却在 %s 注册了路由" % public_where)
            need(not in_public_struct, "has_public=false 却出现在 PublicState 里")
            need(not in_public_state, "has_public=false 却出现在 main.rs 的 public_state 里")

    print("=" * 72)
    print("切片注册一致性校验（切片清单 vs 各共享文件的实际内容，含 src/root/ 根级通道）")
    print("=" * 72)
    print("已校验切片：%d 片" % checked)

    if not problems:
        print("\n✅ 注册一致，无问题")
        return 0

    print("\n发现 %d 处不一致：" % len(problems))
    for p in problems:
        print("  ✗ " + p)

    if args.strict:
        print("\n注册不一致，请修正后再编译", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
