#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""按「限界上下文（ctx）/ 特性（feature）」统计 Rust 接口实现覆盖率（v2 口径）。

数据来源：

* **契约**：``docs/api-contract.json``（Java 侧唯一权威接口清单）
* **已实现**：复用 ``check_api_coverage.py`` 的静态路由提取器
  （``collect_rust_routes`` / ``analyse``）——**不复制、不改写**提取语义，
  保证与 ``check_api_coverage.py`` 的口径永远一致（本仓库历史上两套口径
  互相漂移造成过误判）。
* **归属**：``docs/domain-map.json``：
  - 优先用 ``controllers[]``（controller+module -> ctx/feature）；
  - 其次用 ``slices[]`` + ``docs/slices.json`` 的 apis（method/path -> ctx/feature，
    支持 ``split[]`` 按 controller 拆子特性）；
  - domain-map 缺失时退化为 ``feature = snake_case(controller)``、``ctx = _unknown_``，
    并明确告警（不静默编造归属）。

输出：控制台紧凑表格 + ``docs/coverage-by-domain.json``（供 ``plan_domains.py`` 使用）。

用法::

    cd rust
    python tools/coverage_by_domain.py                 # 报告 + 写 docs/coverage-by-domain.json
    python tools/coverage_by_domain.py --json          # stdout 只输出 JSON
    python tools/coverage_by_domain.py --top 30        # 显示 pending 最多的 30 个特性
    python tools/coverage_by_domain.py --strict        # 仍有未实现接口 -> exit 1

退出码：0 = 成功；1 = ``--strict`` 且有未实现接口；2 = 工具错误（契约缺失等）。
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)
DOCS = os.path.join(RUST, "docs")
CRATES = os.path.join(RUST, "crates")

# ARCHITECTURE.md §3 的 14 个限界上下文（用于排序 / 缺省 ctx 列表）。
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
UNKNOWN_CTX = "_unknown_"


# --------------------------------------------------------------------------- #
# 复用 check_api_coverage：本仓库唯一的路由提取器
# --------------------------------------------------------------------------- #
sys.path.insert(0, HERE)
import check_api_coverage as cac  # noqa: E402  （依赖 HERE 先入 sys.path）


def read_json(path):
    with open(path, encoding="utf-8-sig") as fh:
        return json.load(fh)


def snake(name):
    """ProductStockController -> product_stock（domain-map 缺失时的兜底 feature）。"""
    name = re.sub(r"Controller$", "", name or "")
    name = re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", name)
    name = re.sub(r"[^A-Za-z0-9_]", "_", name)
    return name.strip("_").lower() or "unknown"


def rel_to_rust(path):
    """相对 rust/ 的展示路径；跨盘符（测试用临时目录）时退化为绝对路径。"""
    try:
        return os.path.relpath(path, RUST).replace("\\", "/")
    except ValueError:
        return os.path.abspath(path).replace("\\", "/")


def order_key(ctx):
    """ctx 排序键：14 域按 ARCHITECTURE 顺序，未知域排最后。"""
    if ctx in ARCH_CTX:
        return (0, ARCH_CTX.index(ctx))
    return (1, 0)


# --------------------------------------------------------------------------- #
# domain-map 索引
# --------------------------------------------------------------------------- #
def load_domain_map(docs_dir):
    """返回 ``(domain_map | None, 相对路径, 状态说明)``。"""
    path = os.path.join(docs_dir, "domain-map.json")
    rel = rel_to_rust(path)
    if not os.path.isfile(path):
        return None, rel, "缺失"
    try:
        return read_json(path), rel, "已加载"
    except (OSError, ValueError) as exc:
        return None, rel, "解析失败：%s" % exc


def build_controller_index(domain_map):
    """``(controller, module) -> (ctx, feature)``，同时登记不带 module 的兜底键。"""
    idx = {}
    if not domain_map:
        return idx
    for c in domain_map.get("controllers") or []:
        ctrl = c.get("controller")
        ctx = c.get("ctx")
        feature = c.get("feature")
        if not (ctrl and ctx and feature):
            continue
        idx[(ctrl, c.get("module"))] = (ctx, feature)
        idx.setdefault((ctrl, None), (ctx, feature))
    return idx


def build_slice_index(domain_map, slices):
    """``(METHOD, path) -> (ctx, feature)``；支持 domain-map ``split[]`` 子特性。"""
    idx = {}
    if not domain_map:
        return idx
    by_key = {}
    for dms in domain_map.get("slices") or []:
        sid = (dms.get("slice_id") or "").replace("-", "").replace("_", "").lower()
        if sid:
            by_key[sid] = dms
    for s in slices or []:
        key = (s.get("slice_id") or "").replace("-", "").replace("_", "").lower()
        dms = by_key.get(key)
        if not dms:
            continue
        ctx = dms.get("ctx")
        feature = dms.get("feature")
        if not (ctx and feature):
            continue
        split_map = {}
        for sp in dms.get("split") or []:
            for ctrl in sp.get("controllers") or []:
                split_map[ctrl] = sp.get("feature") or feature
        for api in s.get("apis") or []:
            k = (str(api.get("method") or "").upper(), api.get("path"))
            idx[k] = (ctx, split_map.get(api.get("controller"), feature))
    return idx


def attribute_endpoint(ep, ctrl_index, slice_index):
    """决定接口归属：controller 索引 -> slice 索引 -> 兜底。"""
    ctrl = ep.get("controller")
    module = ep.get("module")
    if (ctrl, module) in ctrl_index:
        return ctrl_index[(ctrl, module)]
    if (ctrl, None) in ctrl_index:
        return ctrl_index[(ctrl, None)]
    key = (str(ep.get("method") or "").upper(), ep.get("path"))
    if key in slice_index:
        return slice_index[key]
    return UNKNOWN_CTX, snake(ctrl)


# --------------------------------------------------------------------------- #
# 覆盖率计算（可被 plan_domains.py 复用）
# --------------------------------------------------------------------------- #
def build_report(domain_map, contract, crates_dir, slices=None):
    """返回按 ctx/feature 聚合的覆盖率报告 dict。

    这是 ``coverage_by_domain.py`` 与 ``plan_domains.py`` 共用的唯一计算入口，
    避免两个工具各自算一套数字。
    """
    rust_routes = cac.collect_rust_routes(crates_dir)
    analysed = cac.analyse(contract, rust_routes)
    # 注意：``analyse()['implemented']`` 是**计数**（int），未实现清单是 ``missing``。
    # 所以「已实现」= 契约接口中不在 missing 集合里的那些，避免误读口径。
    missing = {
        (str(e.get("method") or "").upper(), e.get("path"))
        for e in analysed["missing"]
    }

    ctrl_index = build_controller_index(domain_map)
    slice_index = build_slice_index(domain_map, slices or [])

    feats = {}
    unmapped = 0
    for ep in contract.get("endpoints") or []:
        ctx, feature = attribute_endpoint(ep, ctrl_index, slice_index)
        if ctx == UNKNOWN_CTX:
            unmapped += 1
        key = (ctx, feature)
        rec = feats.setdefault(
            key,
            {
                "ctx": ctx,
                "feature": feature,
                "total": 0,
                "implemented": 0,
                "pending": 0,
                "controllers": set(),
                "modules": set(),
                "pending_endpoints": [],
            },
        )
        ep_key = (str(ep.get("method") or "").upper(), ep.get("path"))
        rec["total"] += 1
        if ep.get("controller"):
            rec["controllers"].add(ep["controller"])
        if ep.get("module"):
            rec["modules"].add(ep["module"])
        if ep_key not in missing:
            rec["implemented"] += 1
        else:
            rec["pending"] += 1
            rec["pending_endpoints"].append(
                {
                    "method": ep.get("method"),
                    "path": ep.get("path"),
                    "controller": ep.get("controller"),
                    "module": ep.get("module"),
                    "source": ep.get("source"),
                }
            )

    by_feature = []
    for (_ctx, _feature), rec in feats.items():
        by_feature.append(
            {
                "ctx": rec["ctx"],
                "feature": rec["feature"],
                "total": rec["total"],
                "implemented": rec["implemented"],
                "pending": rec["pending"],
                "percent": round(100.0 * rec["implemented"] / max(1, rec["total"]), 1),
                "controllers": sorted(rec["controllers"]),
                "modules": sorted(rec["modules"]),
                "pending_endpoints": rec["pending_endpoints"],
            }
        )
    by_feature.sort(
        key=lambda f: (
            order_key(f["ctx"]),
            -f["pending"],
            f["feature"],
        )
    )

    by_ctx = {}
    for f in by_feature:
        rec = by_ctx.setdefault(
            f["ctx"],
            {"ctx": f["ctx"], "features": 0, "pending_features": 0, "total": 0, "implemented": 0},
        )
        rec["features"] += 1
        rec["total"] += f["total"]
        rec["implemented"] += f["implemented"]
        if f["pending"]:
            rec["pending_features"] += 1
    for rec in by_ctx.values():
        rec["pending"] = rec["total"] - rec["implemented"]
        rec["percent"] = round(100.0 * rec["implemented"] / max(1, rec["total"]), 1)

    total = len(contract.get("endpoints") or [])
    implemented_total = sum(f["implemented"] for f in by_feature)
    return {
        "contract_version": contract.get("version"),
        "total": total,
        "implemented": implemented_total,
        "pending": total - implemented_total,
        "percent": round(100.0 * implemented_total / max(1, total), 1),
        "rust_registered_routes": len(rust_routes),
        "unmapped_endpoints": unmapped,
        "by_ctx": by_ctx,
        "by_feature": by_feature,
    }


def load_slices(docs_dir):
    path = os.path.join(docs_dir, "slices.json")
    if not os.path.isfile(path):
        return []
    try:
        return read_json(path)
    except (OSError, ValueError):
        return []


# --------------------------------------------------------------------------- #
# 输出
# --------------------------------------------------------------------------- #
def build_artifact(report, domain_map, dm_rel, dm_status):
    return {
        "generated_by": "rust/tools/coverage_by_domain.py",
        "contract_version": report["contract_version"],
        "route_extractor": "rust/tools/check_api_coverage.py::collect_rust_routes",
        "domain_map": {
            "path": dm_rel,
            "present": domain_map is not None,
            "status": dm_status,
        },
        "total": report["total"],
        "implemented": report["implemented"],
        "pending": report["pending"],
        "percent": report["percent"],
        "unmapped_endpoints": report["unmapped_endpoints"],
        "ctx_order": ARCH_CTX,
        "by_ctx": report["by_ctx"],
        "by_feature": [
            {
                "ctx": f["ctx"],
                "feature": f["feature"],
                "total": f["total"],
                "implemented": f["implemented"],
                "pending": f["pending"],
                "percent": f["percent"],
                "controllers": f["controllers"],
                "modules": f["modules"],
                "pending_endpoints": f["pending_endpoints"],
            }
            for f in report["by_feature"]
        ],
    }


def print_report(report, domain_map, dm_rel, dm_status, top):
    print("=" * 80)
    print("TigShop 按业务域/特性接口覆盖率（v2 口径）")
    print("=" * 80)
    print("契约版本        : %s" % (report["contract_version"] or "?"))
    print("契约接口总数    : %d" % report["total"])
    print("Rust 已实现     : %d" % report["implemented"])
    print("覆盖率          : %.1f%%" % report["percent"])
    print("未实现          : %d" % report["pending"])
    print("domain-map      : %s（%s）" % (dm_rel, dm_status))
    if report["unmapped_endpoints"]:
        print(
            "⚠️  归属未知接口 : %d（domain-map 未覆盖，ctx 记为 %s）"
            % (report["unmapped_endpoints"], UNKNOWN_CTX)
        )

    print("\n按 ctx：")
    print("  %-11s %6s %5s %10s %8s" % ("ctx", "特性", "待办", "已实现/总数", "覆盖率"))
    print("  " + "-" * 47)
    for ctx in sorted(report["by_ctx"], key=order_key):
        r = report["by_ctx"][ctx]
        print(
            "  %-11s %6d %5d %10s %7.1f%%"
            % (
                ctx,
                r["features"],
                r["pending_features"],
                "%d/%d" % (r["implemented"], r["total"]),
                r["percent"],
            )
        )

    pending_feats = sorted(
        [f for f in report["by_feature"] if f["pending"]],
        key=lambda f: (-f["pending"], order_key(f["ctx"]), f["feature"]),
    )
    if pending_feats:
        print("\n待办特性 Top %d（按未实现接口数降序）：" % top)
        print("  %-38s %12s %7s  %s" % ("ctx/feature", "已实现/总数", "待办", "controllers"))
        print("  " + "-" * 78)
        for f in pending_feats[:top]:
            print(
                "  %-38s %12s %7d  %s"
                % (
                    "%s/%s" % (f["ctx"], f["feature"]),
                    "%d/%d" % (f["implemented"], f["total"]),
                    f["pending"],
                    ",".join(f["controllers"][:3]) or "-",
                )
            )
    else:
        print("\n✅ 契约接口已全部实现。")


def main(argv=None):
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass

    ap = argparse.ArgumentParser(description="按 ctx/feature 统计接口覆盖率")
    ap.add_argument("--docs", default=DOCS)
    ap.add_argument("--crates", default=CRATES)
    ap.add_argument("--contract", default=None, help="默认 <docs>/api-contract.json")
    ap.add_argument("--domain-map", default=None, help="默认 <docs>/domain-map.json")
    ap.add_argument(
        "--out",
        default=None,
        help="输出 JSON 路径（默认 <docs>/coverage-by-domain.json）",
    )
    ap.add_argument("--top", type=int, default=20, help="待办特性展示条数（默认 20）")
    ap.add_argument("--no-write", action="store_true", help="不写 coverage-by-domain.json")
    ap.add_argument("--strict", action="store_true", help="仍有未实现接口时 exit 1")
    ap.add_argument("--json", action="store_true", help="stdout 仅输出 JSON")
    args = ap.parse_args(argv)

    contract_path = args.contract or os.path.join(args.docs, "api-contract.json")
    if not os.path.isfile(contract_path):
        print(
            "工具错误：契约文件缺失：%s\n请先运行 python tools/java_api_parser.py --all"
            % contract_path,
            file=sys.stderr,
        )
        return 2

    try:
        contract = read_json(contract_path)
    except (OSError, ValueError) as exc:
        print("工具错误：契约文件无法解析：%s" % exc, file=sys.stderr)
        return 2

    if args.domain_map:
        dm_path = args.domain_map
        if not os.path.isfile(dm_path):
            print("工具错误：指定的 domain-map 不存在：%s" % dm_path, file=sys.stderr)
            return 2
        dm_rel = rel_to_rust(dm_path)
        try:
            domain_map, dm_status = read_json(dm_path), "已加载"
        except (OSError, ValueError) as exc:
            domain_map, dm_status = None, "解析失败：%s" % exc
    else:
        domain_map, dm_rel, dm_status = load_domain_map(args.docs)

    out_path = args.out or os.path.join(args.docs, "coverage-by-domain.json")
    slices = load_slices(args.docs)

    try:
        report = build_report(domain_map, contract, args.crates, slices)
    except OSError as exc:
        print("工具错误：扫描 Rust 源码失败：%s" % exc, file=sys.stderr)
        return 2

    artifact = build_artifact(report, domain_map, dm_rel, dm_status)

    if not args.no_write:
        try:
            with open(out_path, "w", encoding="utf-8", newline="\n") as fh:
                json.dump(artifact, fh, ensure_ascii=False, indent=1)
                fh.write("\n")
        except OSError as exc:
            print("工具错误：写入 %s 失败：%s" % (out_path, exc), file=sys.stderr)
            return 2

    if args.json:
        print(json.dumps(artifact, ensure_ascii=False, indent=2))
    else:
        print_report(report, domain_map, dm_rel, dm_status, args.top)
        if not args.no_write:
            print("\n已写出覆盖率数据：%s" % out_path)

    if args.strict and report["pending"]:
        print("\n仍有 %d 个接口未实现" % report["pending"], file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
