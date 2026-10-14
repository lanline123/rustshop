#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""把切片子代理返回的 `needs_registration` 机械地合并进共享文件。

背景
----
并行重构时每个切片子代理**独占**自己的文件，但不允许改共享文件（避免并发
写冲突）。主会话负责把各切片声明的注册信息统一合并。手工合并 5 个共享文件
在 200+ 切片规模下不可行，故用本工具自动化。

本工具**只做插入，不做删除**，且完全幂等（重复执行不会产生重复行）。
唯一需要人工判断的是「与本片重复的引导期内联路由」，那部分在主会话处置，
不在此工具职责内。

用法
----
    # 单个切片（JSON 文件或 stdin）
    python tools/register_slices.py --file reg_slice001.json

    # 批量：合并目录下所有 reg_*.json
    python tools/register_slices.py --dir docs/registrations

    # 只预览将要做出的修改，不落盘
    python tools/register_slices.py --dir docs/registrations --dry-run

注册记录格式（子代理返回值中 needs_registration 字段的子集）::

    {
      "slice_id": "slice003",
      "tag": "商品库存管理",
      "has_admin": true,
      "has_public": false,
      "core_models_mod": "pub mod slice003;"
    }

约定
----
* 切片号即文件名号：`slice%03d` -> `slice003`，故不需要子代理提供具体代码行，
  全部由本工具按项目既有样板生成，杜绝手抄错误。
* admin / public 是否注册由 `has_admin` / `has_public` 决定，不再依赖子代理
  返回的字符串（子代理常返回空串或带注释的脏字符串）。
"""

from __future__ import annotations

import argparse
import glob
import json
import os
import re
import sys

try:
    # Windows 控制台默认 GBK：tag 里出现生僻字（或 mojibake）会让 print 直接
    # 抛 UnicodeEncodeError 并中断合并（曾导致 slice029 之后全部未注册）。
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)
CRATES = os.path.join(RUST, "crates")

# 共享文件（本工具唯一被允许修改的文件集合）
F_ROUTES_ADMIN_MOD = "api/admin/src/routes/mod.rs"
F_ROUTES_PUBLIC_MOD = "api/public/src/routes/mod.rs"
F_ROOT_ADMIN_MOD = "api/admin/src/root/mod.rs"
F_ROOT_PUBLIC_MOD = "api/public/src/root/mod.rs"
F_ADMIN_LIB = "api/admin/src/lib.rs"
F_PUBLIC_LIB = "api/public/src/lib.rs"
F_CORE_MODELS_MOD = "core/src/models/mod.rs"
F_SERVICES_LIB = "services/src/lib.rs"
F_REPO_MOD = "infrastructure/src/db/repo/mod.rs"
F_DB_MOD = "infrastructure/src/db/mod.rs"
F_MAIN = "bin/src/main.rs"

# 各挂载通道的锚点（(pub mod 行, .merge 行, implemented_routes 汇总行)）。
# 锚点必须是目标文件里**真实存在的整行**（`insert_after_line` 按 strip 后全等匹配，
# 找不到即抛错，绝不静默插错位置）。
ADMIN_ROUTES_ANCHORS = (
    "pub mod slice002;",
    ".merge(slice005::routes(state))",
    "out.extend(slice005::implemented_routes());",
)
PUBLIC_ROUTES_ANCHORS = (
    "pub mod slice001;",
    ".merge(slice001::routes(state))",
    "out.extend(slice001::implemented_routes());",
)
ADMIN_ROOT_ANCHORS = (
    "pub mod slice020;",
    ".merge(slice020::routes(state))",
    "out.extend(slice020::implemented_routes());",
)
PUBLIC_ROOT_ANCHORS = (
    "pub mod slice097;",
    ".merge(slice097::routes(state))",
    "out.extend(slice097::implemented_routes());",
)


def routes_mod_file(app):
    return F_ROUTES_ADMIN_MOD if app == "admin" else F_ROUTES_PUBLIC_MOD


def app_root_mod_file(app):
    return F_ROOT_ADMIN_MOD if app == "admin" else F_ROOT_PUBLIC_MOD


def routes_anchors(app):
    return ADMIN_ROUTES_ANCHORS if app == "admin" else PUBLIC_ROUTES_ANCHORS


def app_root_anchors(app):
    return ADMIN_ROOT_ANCHORS if app == "admin" else PUBLIC_ROOT_ANCHORS


def read(path):
    with open(path, "rb") as fh:
        return fh.read().decode("utf-8")


def write(path, text):
    with open(path, "w", encoding="utf-8", newline="") as fh:
        fh.write(text)


def _brace_delta(line):
    """统计一行的花括号净增量（忽略字符串与行注释内的括号）。"""
    # 去掉行注释
    code = line.split("//")[0]
    delta = 0
    in_str = False
    i = 0
    while i < len(code):
        ch = code[i]
        if in_str:
            if ch == "\\":
                i += 2
                continue
            if ch == '"':
                in_str = False
        elif ch == '"':
            in_str = True
        elif ch == "{":
            delta += 1
        elif ch == "}":
            delta -= 1
        i += 1
    return delta


def insert_after_line(text, anchor, line, indent=""):
    """把 `line` 插到包含 `anchor` 的那一行之后。

    * `anchor` 按**整行 strip 后完全相等**匹配（不是子串包含！），避免
      `"Slice"` 这类过宽锚点把代码插到无关位置。
    * 找不到时**抛异常**（由调用方决定是否降级），绝不静默插错位置。
    * 幂等：该行（去空白后）已存在则不重复插入。
    """
    if line.strip() and line.strip() in {l.strip() for l in text.splitlines()}:
        return text, False
    lines = text.split("\n")
    for i, ln in enumerate(lines):
        if ln.strip() == anchor.strip():
            lines.insert(i + 1, indent + line)
            out = "\n".join(lines)
            if not out.endswith("\n"):
                out += "\n"
            return out, True
    raise ValueError("锚点行未找到（要求整行相等）：%r" % anchor)


def insert_after_first_anchor(text, anchors, line):
    """按顺序尝试多个锚点，把 `line` 插到**第一个命中的锚点行的紧邻位置**。

    为什么不用「插在 `anchor` 所在块末尾」：那样新行会落在块尾，下次再用同一
    锚点时会与已有行争位，导致反复插到不同位置（churn）。插在锚点行**紧邻处**
    则位置稳定，且幂等判断（整行已存在）能可靠命中。

    锚点按整行 strip 相等匹配。
    """
    if line.strip() and line.strip() in {l.strip() for l in text.splitlines()}:
        return text, False
    lines = text.split("\n")
    for anchor in anchors:
        for i, ln in enumerate(lines):
            if ln.strip() == anchor.strip():
                lines.insert(i + 1, line)
                out = "\n".join(lines)
                if not out.endswith("\n"):
                    out += "\n"
                return out, True
    raise ValueError("所有候选锚点都未找到：%r" % (anchors,))


def _block_range(lines, open_anchor):
    """返回块起始行号与结束行号（半开区间 [start, end)）。"""
    start = None
    for i, ln in enumerate(lines):
        if ln.strip() == open_anchor.strip():
            start = i
            break
    if start is None:
        raise ValueError("块起始行未找到（要求整行相等）：%r" % open_anchor)
    indent = len(lines[start]) - len(lines[start].lstrip())
    for i in range(start + 1, len(lines)):
        s = lines[i]
        if not s.strip():
            continue
        cur = len(s) - len(s.lstrip())
        if cur <= indent and s.lstrip().startswith("}"):
            return start, i
    raise ValueError("块结束行未找到：%r" % open_anchor)


def insert_in_block(text, open_anchor, line):
    """把 `line` 插到以 `open_anchor` 起始的块的**字段/语句区末尾**。

    * 用**缩进相等且以 `}` 开头**的行界定块结束（比花括号计数稳，不受
      字符串或行注释里的括号干扰）。
    * **幂等检查是块作用域的**：只在块内查找该行。这一点很关键——
      `AdminState` 与 `PublicState` 里同一句 `sliceNNN: sliceNNN.clone(),`
      文本完全相同，若用全文件级检查，第二个块会被误判为"已存在"而漏插。
    """
    lines = text.split("\n")
    start, end = _block_range(lines, open_anchor)

    target = line.strip()
    for i in range(start + 1, end):
        if lines[i].strip() == target:
            return text, False  # 本块内已有，无需插入

    pos = end
    while pos - 1 > start and not lines[pos - 1].strip():
        pos -= 1
    lines.insert(pos, line)
    out = "\n".join(lines)
    if not out.endswith("\n"):
        out += "\n"
    return out, True


class Registrar:
    def __init__(self, dry_run=False, verbose=False):
        self.dry_run = dry_run
        self.verbose = verbose
        self.changes = []
        self.diffs = []
        self._cache = {}

    def load(self, rel):
        if rel not in self._cache:
            self._cache[rel] = read(os.path.join(CRATES, rel))
        return self._cache[rel]

    def save(self, rel, text):
        self._cache[rel] = text
        if not self.dry_run:
            write(os.path.join(CRATES, rel), text)

    def touch(self, rel, fn):
        """对某共享文件施加一次变换，返回是否有改动。"""
        before = self.load(rel)
        after, changed = fn(before)
        if changed and after != before:
            self.save(rel, after)
            self.changes.append(rel)
            if self.verbose:
                import difflib
                diff = [
                    d for d in difflib.unified_diff(
                        before.split("\n"), after.split("\n"),
                        lineterm="", n=0, fromfile="before/" + rel, tofile="after/" + rel,
                    )
                    if d.startswith(("+", "-")) and not d.startswith(("+++", "---"))
                ]
                self.diffs.append((rel, diff[:8]))
        return changed

    # ————————————————— 各注册点 —————————————————

    def reg_core_models(self, name):
        """登记 `crates/core/src/models/<name>.rs`。

        **仅当该文件真实存在时才加 `pub mod` 声明** —— 有些切片不需要自己的
        DTO（直接复用 `BatchDTO` / `OperateDTO` 等共享模型），无条件声明会
        导致 `E0583 file not found for module`。
        """
        path = os.path.join(CRATES, "core/src/models/%s.rs" % name)
        if not os.path.isfile(path):
            return False
        return self.touch(
            F_CORE_MODELS_MOD,
            lambda t: insert_after_line(t, "pub mod shop;", "pub mod %s;" % name),
        )

    def reg_services(self, mod):
        return self.touch(
            F_SERVICES_LIB,
            lambda t: insert_after_line(t, "pub mod shop;", mod),
        )

    def reg_repo_mod(self, mod):
        return self.touch(
            F_REPO_MOD,
            lambda t: insert_after_line(t, "pub mod shop;", mod),
        )

    def reg_db_mod_repo(self, name, repo):
        """`pub use repo::sliceNNN::SliceNNNRepository;` + Repositories 字段/初始化。"""
        def fn(t):
            changed = False
            t, c = insert_after_line(
                t,
                "pub use repo::shop::ShopRepository;",
                "pub use repo::%s::%s;" % (name, repo),
            )
            changed |= c
            t, c = insert_in_block(
                t,
                "pub struct Repositories {",
                "    pub %s: %s," % (name, repo),
            )
            changed |= c
            # 初始化项必须插进 `Self { ... }` **结构体字面量内**。
            # 不能用 `impl Repositories {` 作锚点：那会把行插到 `fn new` 的
            # 收尾 `}` 之后（块内但在字面量之外），语法直接报错。
            t, c = insert_in_block(
                t,
                "Self {",
                "            %s: %s::new(pool.clone())," % (name, repo),
            )
            changed |= c
            return t, changed

        return self.touch(F_DB_MOD, fn)

    def reg_admin(self, name, svc):
        def fn(t):
            changed = False
            t, c = insert_after_line(
                t,
                "use tigshop_services::shop::ShopService;",
                "use tigshop_services::%s::%s;" % (name, svc),
            )
            changed |= c
            t, c = insert_in_block(
                t, "pub struct AdminState {", "    pub %s: Arc<%s>," % (name, svc)
            )
            changed |= c
            return t, changed

        return self.touch(F_ADMIN_LIB, fn)

    def reg_public(self, name, svc):
        def fn(t):
            changed = False
            t, c = insert_after_line(
                t,
                "use tigshop_services::shop::ShopService;",
                "use tigshop_services::%s::%s;" % (name, svc),
            )
            changed |= c
            t, c = insert_in_block(
                t, "pub struct PublicState {", "    pub %s: Arc<%s>," % (name, svc)
            )
            changed |= c
            return t, changed

        return self.touch(F_PUBLIC_LIB, fn)

    def reg_routes_admin_mod(self, name):
        return self._reg_routes(name, "admin")

    def reg_routes_public_mod(self, name):
        return self._reg_routes(name, "public")

    def _reg_routes(self, name, app):
        """把切片路由挂进**正确通道**。

        同一应用有两个挂载点（见 `api/*/src/root/mod.rs` 顶部说明）：

        * `src/routes/<name>.rs` —— 带模块前缀（`/adminapi`、`/api`），绝大多数切片；
        * `src/root/<name>.rs`   —— 挂在**应用根路径**，对应 Java `@RequestMapping`
          里不含模块前缀的控制器（`/im/**`、`/img/**`、`/video/**`、`/app/**`、`/s/{code}`）。

        判据以**磁盘上真实存在的文件**为准：只有 `src/root/` 版本、没有 `src/routes/`
        版本时挂进 root 通道。挂错会同时造成「编译期 E0583 找不到模块」和
        「服务路径变成 `/adminapi/im/...` 的前端 404」。

        root 通道默认挂进 `merge_protected`（需登录）。属于 Java `URL_WHITELIST`
        的免登录根级切片（如静态文件 `/img/**`）必须由主会话手工改挂
        `merge_public`，本工具不猜测。
        """
        if self.is_root_channel(name, app):
            return self._reg_routes_into(app_root_mod_file(app), app_root_anchors(app), name)
        return self._reg_routes_into(routes_mod_file(app), routes_anchors(app), name)

    @staticmethod
    def is_root_channel(name, app):
        root = os.path.join(CRATES, "api/%s/src/root/%s.rs" % (app, name))
        prefixed = os.path.join(CRATES, "api/%s/src/routes/%s.rs" % (app, name))
        return os.path.isfile(root) and not os.path.isfile(prefixed)

    def _reg_routes_into(self, target, anchors, name):
        mod_anchor, merge_anchor, coverage_anchor = anchors

        def fn(t):
            changed = False
            t, c = insert_after_line(t, mod_anchor, "pub mod %s;" % name)
            changed |= c
            t, c = insert_after_line(
                t, merge_anchor, ".merge(%s::routes(state))" % name
            )
            changed |= c
            t, c = insert_after_line(
                t,
                coverage_anchor,
                "out.extend(%s::implemented_routes());" % name,
            )
            changed |= c
            return t, changed

        return self.touch(target, fn)

    def reg_main(self, name, svc, has_admin, has_public):
        """注册 bin/src/main.rs 的四处：use、服务构造、admin 状态、public 状态。

        每处都用**多候选锚点**（按项目既有行匹配，找不到再回退），保证：
        1. 位置稳定（不随已注册切片数量漂移，避免反复插入）；
        2. 锚点缺失时立刻报错，绝不静默插到错误位置。
        """
        def fn(t):
            changed = False
            # 1) use 导入
            t, c = insert_after_first_anchor(
                t,
                ["use tigshop_services::slice001::Slice001Service;"],
                "use tigshop_services::%s::%s;" % (name, svc),
            )
            changed |= c
            # 2) 服务构造（与其它切片构造相邻）
            t, c = insert_after_first_anchor(
                t,
                ["let slice001 = Arc::new(Slice001Service::new(repos.clone()));"],
                "    let %s = Arc::new(%s::new(repos.clone()));" % (name, svc),
            )
            changed |= c
            # 3) AdminState 字段（**块锚点必须无歧义**：不能用
            #    `slice001: slice001.clone(),` 这类在 PublicState 里也存在的行，
            #    否则会插错块——这里用 admin_state 的块起始行）
            if has_admin:
                t, c = insert_in_block(
                    t,
                    "let admin_state = AdminState {",
                    "        %s: %s.clone()," % (name, name),
                )
                changed |= c
            # 4) PublicState 字段（同理用 public_state 块起始行）
            if has_public:
                t, c = insert_in_block(
                    t,
                    "let public_state = PublicState {",
                    "        %s: %s.clone()," % (name, name),
                )
                changed |= c
            return t, changed

        return self.touch(F_MAIN, fn)


def strip_comment(s):
    """去掉子代理返回值里常见的行尾注释（如 `... // 注意...`）。"""
    if not isinstance(s, str):
        return ""
    return s.split("//")[0].strip()


REPO_DECL_RE = re.compile(r"pub struct\s+(Slice\d+Repository)\b")


def detect_repo_name(name):
    """探测该切片仓储结构体的**真实类型名**。

    约定是 `Slice003Repository`，但子代理可能写成别的名字。以
    `crates/infrastructure/src/db/repo/<name>.rs` 中实际的
    `pub struct XxxRepository` 为准，探测不到才回退到约定名。
    """
    path = os.path.join(CRATES, "infrastructure/src/db/repo/%s.rs" % name)
    if not os.path.isfile(path):
        return None, "仓库文件不存在：%s" % path
    text = read(path)
    m = REPO_DECL_RE.search(text)
    if not m:
        return None, "未找到 `pub struct SliceNNNRepository`"
    return m.group(1), None


def detect_service_name(name):
    """探测该切片服务结构体的真实类型名（约定 `Slice003Service`）。"""
    path = os.path.join(CRATES, "services/src/%s.rs" % name)
    if not os.path.isfile(path):
        return None, "服务文件不存在：%s" % path
    text = read(path)
    m = re.search(r"pub struct\s+(Slice\d+Service)\b", text)
    if not m:
        return None, "未找到 `pub struct SliceNNNService`"
    return m.group(1), None


def parse_record(rec):
    sid = strip_comment(rec.get("slice_id", ""))
    m = re.match(r"^slice-?(\d{1,3})$", sid)
    if not m:
        raise ValueError("slice_id 非法：%r" % rec.get("slice_id"))
    n = int(m.group(1))
    name = "slice%03d" % n

    nr = rec.get("needs_registration") or {}
    has_admin = bool(rec.get("has_admin"))
    has_public = bool(rec.get("has_public"))
    # 兼容子代理原始返回值：非空字符串即视为需要注册
    if not has_admin and strip_comment(nr.get("admin_routes_mod", "")):
        has_admin = True
    if not has_public and strip_comment(nr.get("public_routes_mod", "")):
        has_public = True

    # 真实类型名：以源码为准（找不到则记错误，注册时跳过该片以免插错）
    repo_name, repo_err = detect_repo_name(name)
    svc_name, svc_err = detect_service_name(name)

    return {
        "name": name,
        "repo": repo_name,
        "svc": svc_name,
        "errors": [e for e in (repo_err, svc_err) if e],
        "has_admin": has_admin,
        "has_public": has_public,
        "tag": rec.get("tag", ""),
    }


def apply_one(reg, info, verbose=True):
    if info["errors"]:
        print("  跳过 %s：%s" % (info["name"], "; ".join(info["errors"])), file=sys.stderr)
        return []

    done = []
    name, repo, svc = info["name"], info["repo"], info["svc"]

    steps = [
        ("core/models/mod.rs", lambda: reg.reg_core_models(name)),
        ("services/lib.rs", lambda: reg.reg_services("pub mod %s;" % name)),
        ("repo/mod.rs", lambda: reg.reg_repo_mod("pub mod %s;" % name)),
        ("db/mod.rs", lambda: reg.reg_db_mod_repo(name, repo)),
    ]
    if info["has_admin"]:
        steps += [
            ("admin/lib.rs", lambda: reg.reg_admin(name, svc)),
            ("admin/routes/mod.rs", lambda: reg.reg_routes_admin_mod(name)),
        ]
    if info["has_public"]:
        steps += [
            ("public/lib.rs", lambda: reg.reg_public(name, svc)),
            ("public/routes/mod.rs", lambda: reg.reg_routes_public_mod(name)),
        ]
    steps.append(("bin/main.rs", lambda: reg.reg_main(name, svc, info["has_admin"], info["has_public"])))

    for label, fn in steps:
        try:
            result = fn()
        except Exception as exc:  # 单点失败不阻断整体，交由主会话处理
            print("  !! %-22s %s" % (label, exc), file=sys.stderr)
            continue
        # 防御：每个注册点都必须返回「是否有改动」的布尔值。若返回 None，
        # 说明该方法漏写了 `return self.touch(F_XXX, fn)`——那样它**什么都不做
        # 却不会报错**（曾经真的漏过 reg_public，导致 PublicState 字段长期缺失
        # 而 cargo check 依旧全绿）。必须显式抓出来。
        if result is None:
            print(
                "  !! %-22s 未返回变更标记（疑似漏写 `return self.touch(...)`），"
                "本次未生效" % label,
                file=sys.stderr,
            )
            continue
        done.append(label)
    if verbose:
        print("  %s  %-14s repo=%-20s svc=%-18s admin=%-5s public=%-5s -> %d 个注册点" % (
            name, info["tag"], repo, svc, info["has_admin"], info["has_public"], len(done)))
    return done


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--file", help="单个注册记录 JSON")
    ap.add_argument("--dir", help="批量：目录下所有 *.json 注册记录")
    ap.add_argument("--dry-run", action="store_true", help="只预览，不写盘")
    ap.add_argument("-v", "--verbose", action="store_true", help="打印改动明细")
    args = ap.parse_args(argv)

    records = []
    if args.file:
        records.append(json.loads(read(args.file)))
    if args.dir:
        for p in sorted(glob.glob(os.path.join(args.dir, "*.json"))):
            try:
                records.append(json.loads(read(p)))
            except Exception as exc:
                print("跳过 %s：%s" % (p, exc), file=sys.stderr)
    if not records:
        ap.error("必须提供 --file 或 --dir")

    reg = Registrar(dry_run=args.dry_run, verbose=args.verbose)
    infos = []
    for rec in records:
        try:
            infos.append(parse_record(rec))
        except Exception as exc:
            print("记录非法：%s (%s)" % (exc, rec.get("slice_id")), file=sys.stderr)

    print("将合并 %d 个切片%s：" % (len(infos), "（dry-run）" if args.dry_run else ""))
    for info in infos:
        apply_one(reg, info)

    if args.verbose:
        print("\n改动明细（前 8 行/文件）：")
        by_file = {}
        for rel, diff in reg.diffs:
            by_file.setdefault(rel, []).extend(diff)
        for rel in sorted(by_file):
            print("  —— %s ——" % rel)
            for d in by_file[rel][:8]:
                print("    " + d)

    print("\n被修改的共享文件：")
    for rel in sorted(set(reg.changes)):
        print("  " + rel)
    if args.dry_run:
        print("\n（dry-run：未写盘）")
    return 0


if __name__ == "__main__":
    sys.exit(main())
