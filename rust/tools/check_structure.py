#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""TigShop Rust v2 结构门禁（ARCHITECTURE.md §2/§3/§7）。

校验工作区是否已从「编号切片（sliceNNN）」结构迁移到「限界上下文（ctx）」结构，
并守住依赖方向与组合根状态规模。检查项：

1. **切片命名**：``crates/*/src`` 下文件名或 ``mod sliceNNN;`` 声明（v2 禁止）。
2. **模块树布局**：四层只允许
   - ``core/src/models/<ctx>/<feature>.rs``
   - ``infrastructure/src/db/repo/<ctx>/<feature>.rs``
   - ``services/src/<ctx>/<feature>.rs``
   - ``api/{admin,public}/src/{routes,root}/<ctx>/<feature>.rs``
3. **上下文合法性**：``<ctx>`` 必须是 14 个限界上下文之一
   （优先读 ``docs/domain-map.json`` 的 ``contexts``，缺失时用 ARCHITECTURE.md §3 内置表）。
4. **空壳路由模块**：``routes()`` 无任何 ``.route(`` 调用，或
   ``implemented_routes()`` 返回空 ``Vec`` 字面量。
5. **依赖方向**：解析各 crate 源码中的 ``use tigshop_<crate>`` 导入，禁止反向/越级边
   （如 infrastructure -> services/api、core -> 任何 crate、services -> api/*）。
6. **上帝状态**：``AdminState``/``PublicState`` 字段数 > 20，或 ``Repositories``
   字段声明行 > 200 —— 默认仅警告，``--strict`` 时计为违规。

用法::

    cd rust
    python tools/check_structure.py            # 报告（仅提示，不阻塞）
    python tools/check_structure.py --strict   # 有违规 -> exit 1（CI 门禁）
    python tools/check_structure.py --json     # JSON 输出（stdout 只有 JSON）

退出码：0 = 通过；1 = 存在违规（仅 ``--strict``）；2 = 工具错误（路径/读取失败）。
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)
DEFAULT_CRATES = os.path.join(RUST, "crates")
DEFAULT_DOCS = os.path.join(RUST, "docs")

# ARCHITECTURE.md §3 的 14 个限界上下文（domain-map.json 缺失时的兜底）。
ARCH_CTX = [
    "identity",
    "merchant",
    "wholesale",
    "catalog",
    "trade",
    "payment",
    "marketing",
    "member",
    "logistics",
    "content",
    "system",
    "im",
    "analytics",
    "openapi",
]
CTX_DESC = {
    "identity": "管理员/角色/权限/登录/操作日志",
    "merchant": "店铺/商户/入驻/保证金/店铺资金",
    "wholesale": "批发",
    "catalog": "商品/分类/品牌/属性/库存/评价/搜索/卡券",
    "trade": "购物车/订单/售后/退款/发票/结算",
    "payment": "支付/余额/充值/提现/账期/对账/分账",
    "marketing": "优惠券/秒杀/拼团/积分/抽奖/分销",
    "member": "用户/会员/标签/站内信/收藏/签到",
    "logistics": "运费模板/配送/物流公司/打印",
    "content": "文章/装修/素材/导航/SEO/公众号/直播",
    "system": "设置/配置/地区/版本/模板/文件/缓存",
    "im": "客服/会话/消息/接入规则",
    "analytics": "面板/统计/访问日志",
    "openapi": "开放接口/WorkBuddy/MCP",
}

# 允许的 crate 间依赖（ARCHITECTURE.md §2 单向依赖的传递闭包）。
# 键为 crates 相对目录；值为允许 `use` 的 crate 目录集合。
ALLOWED_DEPS = {
    "core": set(),
    "common": {"core"},
    "security": {"core", "common"},
    "infrastructure": {"core", "common"},
    "services": {"core", "common", "infrastructure", "security"},
    "api/admin": {"core", "common", "infrastructure", "security", "services", "api/openapi"},
    "api/public": {"core", "common", "infrastructure", "security", "services", "api/openapi"},
    "api/openapi": set(),
    "pc-web": {"core", "common", "infrastructure", "services"},
    "bin": {
        "core",
        "common",
        "infrastructure",
        "security",
        "services",
        "api/admin",
        "api/public",
        "api/openapi",
        "pc-web",
    },
}

MAX_STATE_FIELDS = 20       # AdminState / PublicState
MAX_REPO_FIELD_LINES = 200  # Repositories
MAX_SAMPLES = 8             # 控制台每类最多展示

SLICE_TOKEN_RE = re.compile(r"\bslice\d{3}\b")
# 注意：行首缩进用 ``[ \t]*`` 而非 ``\s*`` —— 后者会跨行吞换行符，导致行号错位。
MOD_DECL_RE = re.compile(
    r"(?m)^[ \t]*(?:pub(?:\([^)]*\))?[ \t]+)?mod[ \t]+(slice\d{3})[ \t]*;"
)
MOD_DECL_LINE_RE = re.compile(
    r"^[ \t]*(?:pub(?:\([^)]*\))?[ \t]+)?mod[ \t]+slice\d{3}[ \t]*;"
)
USE_TIGSHOP_RE = re.compile(r"\btigshop_([A-Za-z0-9_]+)\s*::")


# --------------------------------------------------------------------------- #
# 基础 IO / 文本工具
# --------------------------------------------------------------------------- #
def read_text(path):
    """读取源码（UTF-8 优先，兼容历史 GBK 文件），非法字节替换。"""
    with open(path, "rb") as fh:
        raw = fh.read()
    for enc in ("utf-8-sig", "utf-8", "gbk", "gb18030", "latin-1"):
        try:
            return raw.decode(enc)
        except (UnicodeDecodeError, LookupError):
            continue
    return raw.decode("latin-1", errors="replace")


def strip_comments(text):
    """去掉行/块注释，**保留换行**（以便报告准确行号）。

    与 ``check_api_coverage.py`` 的 ``strip_comments`` 语义一致，唯一区别是
    块注释按字符替换为空格/换行，保证行号不漂移。
    """
    out = []
    i = 0
    n = len(text)
    while i < n:
        ch = text[i]
        if ch == '"':
            j = i + 1
            while j < n:
                if text[j] == "\\":
                    j += 2
                    continue
                if text[j] == '"':
                    j += 1
                    break
                j += 1
            out.append(text[i:j])
            i = j
            continue
        if ch == "/" and i + 1 < n:
            if text[i + 1] == "/":
                j = text.find("\n", i)
                if j < 0:
                    out.append(" " * (n - i))
                    i = n
                else:
                    out.append(" " * (j - i))
                    i = j
                continue
            if text[i + 1] == "*":
                j = text.find("*/", i + 2)
                end = n if j < 0 else j + 2
                chunk = text[i:end]
                out.append("".join("\n" if c == "\n" else " " for c in chunk))
                i = end
                continue
        out.append(ch)
        i += 1
    return "".join(out)


def _skip_string(text, i):
    """返回从双引号起点的字符串字面量结束后的下标。"""
    n = len(text)
    j = i + 1
    while j < n:
        if text[j] == "\\":
            j += 2
            continue
        if text[j] == '"':
            return j + 1
        j += 1
    return n


def find_block(text, brace_idx):
    """从 ``{`` 下标起返回配平的块内容（不含外层花括号）。"""
    depth = 0
    i = brace_idx
    n = len(text)
    while i < n:
        ch = text[i]
        if ch == '"':
            i = _skip_string(text, i)
            continue
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                return text[brace_idx + 1 : i]
        i += 1
    return text[brace_idx + 1 :]


def find_fn_body(text, fn_name):
    """定位 ``pub fn <name>(...)`` 的函数体（配平花括号）；找不到返回 None。"""
    m = re.search(r"\bpub\s+fn\s+" + re.escape(fn_name) + r"\s*\(", text)
    if not m:
        return None
    paren = text.find("(", m.start())
    depth = 0
    i = paren
    n = len(text)
    while i < n:
        ch = text[i]
        if ch == '"':
            i = _skip_string(text, i)
            continue
        if ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
            if depth == 0:
                break
        i += 1
    brace = text.find("{", i)
    if brace < 0:
        return None
    return find_block(text, brace)


def line_of(text, pos):
    return text.count("\n", 0, pos) + 1


# --------------------------------------------------------------------------- #
# crate / 文件发现
# --------------------------------------------------------------------------- #
def discover_crates(crates_dir):
    """返回 ``[(crate_rel, src_dir, package_name)]``。"""
    crates = []
    if not os.path.isdir(crates_dir):
        return crates
    for name in sorted(os.listdir(crates_dir)):
        full = os.path.join(crates_dir, name)
        if not os.path.isdir(full):
            continue
        if name == "api":
            for sub in sorted(os.listdir(full)):
                sub_full = os.path.join(full, sub)
                src = os.path.join(sub_full, "src")
                if os.path.isdir(src):
                    crates.append(("api/" + sub, src, _package_name(sub_full)))
            continue
        src = os.path.join(full, "src")
        if os.path.isdir(src):
            crates.append((name, src, _package_name(full)))
    return crates


def _package_name(crate_dir):
    cargo = os.path.join(crate_dir, "Cargo.toml")
    if not os.path.isfile(cargo):
        return None
    m = re.search(r'(?m)^\s*name\s*=\s*"([^"]+)"', read_text(cargo))
    return m.group(1) if m else None


def iter_src_files(crates):
    """返回 ``[(crate_rel, src_dir, full_path, rel_to_src)]``。"""
    out = []
    for crate, src_dir, _pkg in crates:
        for dirpath, dirnames, filenames in os.walk(src_dir):
            dirnames[:] = sorted(d for d in dirnames if d not in ("target", "target-verify"))
            for fn in sorted(filenames):
                if not fn.endswith(".rs"):
                    continue
                full = os.path.join(dirpath, fn)
                rel = os.path.relpath(full, src_dir).replace("\\", "/")
                out.append((crate, src_dir, full, rel))
    return out


def load_contexts(docs_dir):
    """返回 ``(ctx 列表, 来源说明, 是否与内置表一致)``。

    优先 ``docs/domain-map.json`` 的 ``contexts``；文件缺失/损坏时回退内置表
    （domain-map 由并行任务生成，门禁不能因它暂缺而报工具错误）。
    """
    path = os.path.join(docs_dir, "domain-map.json")
    if os.path.isfile(path):
        try:
            data = json.loads(read_text(path))
            ctxs = data.get("contexts")
            if isinstance(ctxs, list) and ctxs and all(isinstance(c, str) for c in ctxs):
                return list(ctxs), "docs/domain-map.json", set(ctxs) == set(ARCH_CTX)
            return list(ARCH_CTX), "内置（domain-map.json 无有效 contexts）", True
        except (ValueError, OSError) as exc:
            return list(ARCH_CTX), "内置（domain-map.json 解析失败: %s）" % exc, True
    return list(ARCH_CTX), "内置（docs/domain-map.json 尚不存在）", True


# --------------------------------------------------------------------------- #
# 布局规则
# --------------------------------------------------------------------------- #
def layout_check(crate, rel):
    """检查文件相对路径是否符合 v2 布局。

    返回 ``None``（不在受管子树，放行）、``("ok", ctx_or_None)``（合法文件，
    若可提取第二层目录名则带回 ctx）、``("violation", 说明)``（布局越界）。
    """
    parts = rel.split("/")
    if ".." in parts:
        return None

    if crate == "core":
        # 只约束 models/ 子树；entities/ 为扁平表实体（ARCHITECTURE §2），
        # error.rs / types.rs / lib.rs 等放行。
        if rel != "models" and not rel.startswith("models/"):
            return None
        if rel in ("models", "models/mod.rs"):
            return ("ok", None)
        if len(parts) == 3 and (parts[2] == "mod.rs" or parts[2].endswith(".rs")):
            return ("ok", parts[1])
        return (
            "violation",
            "core/src/models 只允许 models/mod.rs、models/<ctx>/mod.rs、"
            "models/<ctx>/<feature>.rs",
        )

    if crate == "infrastructure":
        if not rel.startswith("db/repo/"):
            return None
        if rel == "db/repo/mod.rs":
            return ("ok", None)
        if len(parts) == 4 and (parts[3] == "mod.rs" or parts[3].endswith(".rs")):
            return ("ok", parts[2])
        return (
            "violation",
            "infrastructure/src/db/repo 只允许 db/repo/mod.rs、db/repo/<ctx>/mod.rs、"
            "db/repo/<ctx>/<feature>.rs",
        )

    if crate == "services":
        if rel == "lib.rs":
            return ("ok", None)
        if len(parts) == 2 and (parts[1] == "mod.rs" or parts[1].endswith(".rs")):
            return ("ok", parts[0])
        return ("violation", "services 层只允许 src/lib.rs 与 src/<ctx>/<feature>.rs")

    if crate in ("api/admin", "api/public"):
        side = crate.split("/")[1]
        if rel == "lib.rs":
            return ("ok", None)
        root = parts[0]
        # `support/` 用于非路由辅助模块（权限中间件/权限表/Excel 响应等），
        # 不注册路由，因此不参与「空壳路由模块」检查。
        if root == "support":
            if len(parts) == 2 and (parts[1] == "mod.rs" or parts[1].endswith(".rs")):
                return ("ok", None)
            return (
                "violation",
                "api/%s/src/support 只允许 support/mod.rs 与 support/<feature>.rs" % side,
            )
        if root not in ("routes", "root"):
            return (
                "violation",
                "api/%s 只允许 src/lib.rs、src/routes/<ctx>/<feature>.rs、"
                "src/root/<ctx>/<feature>.rs、src/support/<feature>.rs" % side,
            )
        if len(parts) == 2 and parts[1] == "mod.rs":
            return ("ok", None)
        if len(parts) == 3 and (parts[2] == "mod.rs" or parts[2].endswith(".rs")):
            return ("ok", parts[1])
        return (
            "violation",
            "api/%s/src/%s 只允许 %s/<ctx>/<feature>.rs" % (side, root, root),
        )

    return None


def is_slice_path(rel):
    """路径中是否含 ``sliceNNN`` 片段（文件或目录名）。"""
    return bool(SLICE_TOKEN_RE.search(rel))


def is_route_feature_file(crate, rel):
    """是否为 api 侧「特性路由文件」（排除聚合 mod.rs）。"""
    if crate not in ("api/admin", "api/public"):
        return False
    parts = rel.split("/")
    if len(parts) < 2 or parts[0] not in ("routes", "root"):
        return False
    return rel.endswith(".rs") and parts[-1] != "mod.rs"


def scaffold_reasons(text):
    """检测空壳路由模块，返回原因列表（空列表 = 非空壳）。

    只看「整个文件是否注册了任何路由」，因为 public 侧合法地存在
    ``routes()`` / ``public_routes()`` / ``authorized_routes()`` 三套约定；
    旧规则逐函数判定会把「只有 public_routes 的聚合文件」误报为空壳。
    同理，``implemented_routes()`` 返回空 Vec 但文件里确实有 ``.route(...)``
    的模块（契约外路由，例如 public seckill/timeDiscount/balance_pay）不是空壳。
    """
    reasons = []
    has_any_route = bool(re.search(r"\.route\s*\(", text))
    route_bodies = [
        (fn, find_fn_body(text, fn))
        for fn in ("routes", "public_routes", "authorized_routes")
    ]
    route_bodies = [(fn, body) for fn, body in route_bodies if body is not None]
    if route_bodies and not has_any_route:
        reasons.append("所有路由函数内均无 .route( 调用")

    impl_body = find_fn_body(text, "implemented_routes")
    if impl_body is not None and not has_any_route:
        has_push = re.search(r"\.(?:extend|push)\s*\(", impl_body)
        empty_lit = re.search(
            r"\b(?:Vec\s*::\s*new|Vec\s*::\s*<[^<>;{}]*>\s*::\s*new|vec!\s*\[\s*\])",
            impl_body,
        )
        if empty_lit and not has_push:
            reasons.append("implemented_routes() 返回空 Vec 字面量")
    return reasons


def struct_fields(stripped_text, struct_name):
    """返回 ``(字段数, 字段名列表, 定义行号, 结构体名)``；未找到返回 None。"""
    m = re.search(
        r"\bpub\s+struct\s+" + re.escape(struct_name) + r"[ \t]*\{", stripped_text
    )
    if not m:
        return None
    brace = stripped_text.find("{", m.start())
    body = find_block(stripped_text, brace)
    fields = re.findall(
        r"(?m)^[ \t]*(?:pub[ \t]+)?([a-z_][A-Za-z0-9_]*)[ \t]*:", body
    )
    return len(fields), fields, line_of(stripped_text, m.start()), struct_name


def measure_god_states(files):
    """测量 AdminState / PublicState / Repositories 规模（超阈值者标记 violation）。"""
    targets = [
        ("api/admin", "AdminState", MAX_STATE_FIELDS),
        ("api/public", "PublicState", MAX_STATE_FIELDS),
        ("infrastructure", "Repositories", MAX_REPO_FIELD_LINES),
    ]
    out = []
    for crate, struct_name, threshold in targets:
        for f_crate, _src_dir, _full, rel in files:
            if f_crate != crate:
                continue
            text = strip_comments(read_text(os.path.join(_src_dir, rel)))
            info = struct_fields(text, struct_name)
            if not info:
                continue
            count, _fields, line, name = info
            out.append(
                {
                    "crate": crate,
                    "file": "crates/%s/src/%s" % (crate, rel),
                    "line": line,
                    "struct": name,
                    "fields": count,
                    "threshold": threshold,
                    "exceeded": count > threshold,
                    "detail": "%s 有 %d 个字段（阈值 %d）" % (name, count, threshold),
                }
            )
    return out


# --------------------------------------------------------------------------- #
# 主检查
# --------------------------------------------------------------------------- #
def run_checks(crates_dir, docs_dir):
    crates = discover_crates(crates_dir)
    files = iter_src_files(crates)
    contexts, ctx_source, ctx_matches_arch = load_contexts(docs_dir)
    ctx_set = set(contexts)

    pkg_to_crate = {}
    for crate, _src, pkg in crates:
        if pkg:
            pkg_to_crate[pkg] = crate

    slice_files = []
    slice_mods = []
    slice_refs = []
    layout = []
    unknown_ctx = []
    scaffolds = []
    dependencies = []
    route_modules = 0

    for crate, _src_dir, full, rel in files:
        # 注释剥离后仍保留换行，行号可直接用。
        text = strip_comments(read_text(full))

        # 1) 切片文件 / 2) 切片 mod 声明
        if is_slice_path(rel):
            slice_files.append({"crate": crate, "file": "crates/%s/src/%s" % (crate, rel)})
        for m in MOD_DECL_RE.finditer(text):
            slice_mods.append(
                {
                    "crate": crate,
                    "file": "crates/%s/src/%s" % (crate, rel),
                    "line": line_of(text, m.start()),
                    "module": m.group(1),
                }
            )

        # 残留在非切片文件里的 sliceNNN 引用（use 路径 / 类型名 / 状态字段）。
        if not is_slice_path(rel) and SLICE_TOKEN_RE.search(text):
            for ln, line in enumerate(text.split("\n"), 1):
                if not SLICE_TOKEN_RE.search(line):
                    continue
                if MOD_DECL_LINE_RE.match(line):
                    continue  # 已计入 mod 声明
                slice_refs.append(
                    {
                        "file": "crates/%s/src/%s" % (crate, rel),
                        "line": ln,
                        "snippet": line.strip()[:120],
                    }
                )

        # 3) 布局 / 4) ctx
        if not is_slice_path(rel):
            res = layout_check(crate, rel)
            if res is not None:
                status, value = res
                if status == "violation":
                    layout.append(
                        {
                            "crate": crate,
                            "file": "crates/%s/src/%s" % (crate, rel),
                            "detail": value,
                        }
                    )
                elif value is not None and value not in ctx_set:
                    unknown_ctx.append(
                        {
                            "crate": crate,
                            "file": "crates/%s/src/%s" % (crate, rel),
                            "ctx": value,
                        }
                    )

        # 5) 空壳路由模块
        if is_route_feature_file(crate, rel):
            route_modules += 1
            reasons = scaffold_reasons(text)
            if reasons:
                scaffolds.append(
                    {
                        "crate": crate,
                        "file": "crates/%s/src/%s" % (crate, rel),
                        "reasons": reasons,
                    }
                )

        # 6) 依赖方向
        deps = {}
        for m in USE_TIGSHOP_RE.finditer(text):
            pkg = "tigshop-" + m.group(1).replace("_", "-")
            target = pkg_to_crate.get(pkg)
            if target is None:
                deps.setdefault("?" + pkg, []).append(line_of(text, m.start()))
                continue
            deps.setdefault(target, []).append(line_of(text, m.start()))
        for target, lines in deps.items():
            if target.startswith("?"):
                continue  # 非工作区 crate：不参与方向判定
            if target == crate:
                if rel == "main.rs":
                    continue  # binary entrypoint references sibling library via package name
                dependencies.append(
                    {
                        "crate": crate,
                        "dependency": target,
                        "kind": "self",
                        "detail": "crate 内部应使用 crate:: 而非自身包名",
                        "file": "crates/%s/src/%s" % (crate, rel),
                        "lines": lines,
                    }
                )
            elif target not in ALLOWED_DEPS.get(crate, set()):
                dependencies.append(
                    {
                        "crate": crate,
                        "dependency": target,
                        "kind": "direction",
                        "detail": "禁止依赖 %s（允许：%s）"
                        % (target, ", ".join(sorted(ALLOWED_DEPS.get(crate, set()))) or "无"),
                        "file": "crates/%s/src/%s" % (crate, rel),
                        "lines": lines,
                    }
                )

        # 7) 上帝状态（在循环外统一测量，见 measure_god_states）

    god_measured = measure_god_states(files)
    god_exceeded = [g for g in god_measured if g["exceeded"]]

    # 依赖违规按 (crate, dependency) 归并，便于阅读
    merged_deps = {}
    for d in dependencies:
        key = (d["crate"], d["dependency"], d["kind"])
        item = merged_deps.setdefault(
            key,
            {
                "crate": d["crate"],
                "dependency": d["dependency"],
                "kind": d["kind"],
                "detail": d["detail"],
                "occurrences": 0,
                "files": [],
            },
        )
        item["occurrences"] += len(d["lines"])
        if d["file"] not in item["files"]:
            item["files"].append(d["file"])
    dep_list = [
        v
        for _k, v in sorted(
            merged_deps.items(), key=lambda kv: (kv[0][0], kv[0][2], kv[0][1])
        )
    ]

    findings = {
        "slice_files": slice_files,
        "slice_mod_decls": slice_mods,
        "layout": layout,
        "unknown_ctx": unknown_ctx,
        "empty_scaffolds": scaffolds,
        "dependencies": dep_list,
    }
    counts = {
        "crates": len(crates),
        "files": len(files),
        "route_modules": route_modules,
        "slice_files": len(slice_files),
        "slice_mod_decls": len(slice_mods),
        "slice_refs": len(slice_refs),
        "layout": len(layout),
        "unknown_ctx": len(unknown_ctx),
        "empty_scaffolds": len(scaffolds),
        "dependencies": len(dep_list),
        "god_state": len(god_exceeded),
    }
    return {
        "contexts": contexts,
        "ctx_source": ctx_source,
        "ctx_matches_arch": ctx_matches_arch,
        "findings": findings,
        "counts": counts,
        "god_state": god_exceeded,
        "god_state_measured": god_measured,
        "slice_ref_samples": slice_refs[:50],
        "slice_ref_total": len(slice_refs),
    }


# --------------------------------------------------------------------------- #
# 输出
# --------------------------------------------------------------------------- #
def print_report(result, strict):
    c = result["counts"]
    f = result["findings"]

    print("=" * 76)
    print("TigShop Rust v2 结构门禁（ARCHITECTURE.md §2/§3/§7）")
    print("=" * 76)
    print("扫描            : %d 个 crate / %d 个 .rs 文件（其中路由特性模块 %d 个）"
          % (c["crates"], c["files"], c["route_modules"]))
    print("上下文          : %s（%d 个）" % (result["ctx_source"], len(result["contexts"])))
    if not result["ctx_matches_arch"]:
        print("⚠️  domain-map.json 的 contexts 与 ARCHITECTURE.md §3 的 14 域不一致！")
    print("-" * 76)
    print("[违规] 切片命名文件    : %d" % c["slice_files"])
    print("[违规] 切片 mod 声明   : %d" % c["slice_mod_decls"])
    print("[违规] 模块树布局越界  : %d" % c["layout"])
    print("[违规] 未知 ctx        : %d" % c["unknown_ctx"])
    print("[违规] 空壳路由模块    : %d" % c["empty_scaffolds"])
    print("[违规] 依赖方向        : %d" % c["dependencies"])
    print("[警告] 上帝状态超阈值  : %d" % c["god_state"])
    print("[警告] 切片残留引用    : %d 处（非切片文件内的 use/类型/状态字段）"
          % c["slice_refs"])

    def dump(items, fmt, limit=MAX_SAMPLES):
        shown = items[:limit]
        for it in shown:
            print("    - " + fmt(it))
        if len(items) > limit:
            print("    ... 其余 %d 条省略" % (len(items) - limit))

    if f["slice_files"]:
        print("\n切片命名文件（前 %d）：" % MAX_SAMPLES)
        dump(f["slice_files"], lambda x: x["file"])
    if f["slice_mod_decls"]:
        print("\n切片 mod 声明（前 %d）：" % MAX_SAMPLES)
        dump(f["slice_mod_decls"], lambda x: "%s:%d  mod %s" % (x["file"], x["line"], x["module"]))
    if f["layout"]:
        print("\n模块树布局越界（前 %d）：" % MAX_SAMPLES)
        dump(f["layout"], lambda x: "%s  —— %s" % (x["file"], x["detail"]))
    if f["unknown_ctx"]:
        print("\n未知上下文（前 %d）：" % MAX_SAMPLES)
        dump(f["unknown_ctx"], lambda x: "%s  —— ctx=%r" % (x["file"], x["ctx"]))
    if f["empty_scaffolds"]:
        print("\n空壳路由模块（前 %d）：" % MAX_SAMPLES)
        dump(f["empty_scaffolds"], lambda x: "%s  —— %s" % (x["file"], "；".join(x["reasons"])))
    if f["dependencies"]:
        print("\n依赖方向违规（前 %d）：" % MAX_SAMPLES)
        dump(
            f["dependencies"],
            lambda x: "%s -> %s（kind=%s, %d 处）: %s"
            % (x["crate"], x["dependency"], x["kind"], x["occurrences"], x["files"][0]),
        )
    if result["god_state_measured"]:
        print(
            "\n上帝状态规模（阈值：State>%d 字段 / Repositories>%d 字段）："
            % (MAX_STATE_FIELDS, MAX_REPO_FIELD_LINES)
        )
        for x in result["god_state_measured"]:
            print(
                "    - %s:%d  %-13s %3d 字段（阈值 %3d）%s"
                % (
                    x["file"],
                    x["line"],
                    x["struct"],
                    x["fields"],
                    x["threshold"],
                    "  <-- 超阈值" if x["exceeded"] else "",
                )
            )
    if result["slice_ref_samples"]:
        print("\n切片残留引用样本（前 %d）：" % MAX_SAMPLES)
        dump(
            result["slice_ref_samples"],
            lambda x: "%s:%d  %s" % (x["file"], x["line"], x["snippet"]),
        )

    blocking = sum(
        c[k]
        for k in (
            "slice_files",
            "slice_mod_decls",
            "layout",
            "unknown_ctx",
            "empty_scaffolds",
            "dependencies",
        )
    )
    print("\n" + "-" * 76)
    if blocking:
        print("判定：FAIL —— 共 %d 处结构违规。" % blocking)
        if not strict:
            print("说明：未指定 --strict，仅报告（exit 0）；CI 请用 --strict（exit 1）。")
        else:
            print("说明：--strict 模式下存在违规 -> exit 1。")
    else:
        print("判定：PASS —— 未发现结构违规。")
        if c["god_state"]:
            print("说明：仍有 %d 处上帝状态告警（--strict 时计为违规）。" % c["god_state"])
    return blocking


def build_json(result, strict):
    c = result["counts"]
    violations = dict(result["findings"])
    warnings = {
        "slice_refs": {
            "count": result["slice_ref_total"],
            "samples": result["slice_ref_samples"],
            "detail": "非切片文件中的 sliceNNN 残留引用（use/类型/状态字段）",
        }
    }
    blocking = sum(
        c[k]
        for k in (
            "slice_files",
            "slice_mod_decls",
            "layout",
            "unknown_ctx",
            "empty_scaffolds",
            "dependencies",
        )
    )
    if strict:
        violations["god_state"] = result["god_state"]
        blocking += c["god_state"]
    else:
        warnings["god_state"] = result["god_state_measured"]

    return {
        "tool": "check_structure",
        "architecture": "rust/docs/ARCHITECTURE.md v2.0",
        "strict": strict,
        "scanned": {"crates": c["crates"], "files": c["files"]},
        "contexts": result["contexts"],
        "contexts_source": result["ctx_source"],
        "contexts_match_architecture": result["ctx_matches_arch"],
        "counts": c,
        "god_state_measured": result["god_state_measured"],
        "violations": violations,
        "warnings": warnings,
        "blocking": blocking,
        "ok": blocking == 0,
        "exit_code": 1 if (strict and blocking) else 0,
    }


def main(argv=None):
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass

    ap = argparse.ArgumentParser(description="TigShop Rust v2 结构门禁")
    ap.add_argument("--crates", default=DEFAULT_CRATES, help="crates 目录（默认 rust/crates）")
    ap.add_argument("--docs", default=DEFAULT_DOCS, help="docs 目录（默认 rust/docs）")
    ap.add_argument("--strict", action="store_true", help="存在违规时 exit 1")
    ap.add_argument("--json", action="store_true", help="输出 JSON（stdout 仅 JSON）")
    args = ap.parse_args(argv)

    if not os.path.isdir(args.crates):
        print("工具错误：crates 目录不存在：%s" % args.crates, file=sys.stderr)
        return 2
    if not os.path.isdir(args.docs):
        print("工具错误：docs 目录不存在：%s" % args.docs, file=sys.stderr)
        return 2

    try:
        result = run_checks(args.crates, args.docs)
    except OSError as exc:
        print("工具错误：扫描失败：%s" % exc, file=sys.stderr)
        return 2

    if not result["counts"]["files"]:
        print("工具错误：未在 %s 下发现任何 .rs 源文件" % args.crates, file=sys.stderr)
        return 2

    if args.json:
        print(json.dumps(build_json(result, args.strict), ensure_ascii=False, indent=2))
    else:
        print_report(result, args.strict)

    if args.strict:
        blocking = sum(
            result["counts"][k]
            for k in (
                "slice_files",
                "slice_mod_decls",
                "layout",
                "unknown_ctx",
                "empty_scaffolds",
                "dependencies",
                "god_state",
            )
        )
        if blocking:
            return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
