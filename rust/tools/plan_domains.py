#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成 v2 分域实施批次计划（替代 slice 时代的 plan_batches.py / slice_prompt.py）。

把「尚未实现的接口」按 ``ctx/feature`` 归并成工作单元（一个 feature = 一次提交，
四层同名文件 + 域聚合器），按 P0→P2 排序后切批，输出可交给子代理/人类执行的
JSON 计划（含每特性：Java 源文件、目标四层文件、接口清单）。

数据来源：

* ``docs/domain-map.json``（权威 slice/controller -> ctx/feature 映射）
* ``docs/api-contract.json``（接口清单与 Java 源文件）
* 覆盖率：默认**实时计算**，复用 ``coverage_by_domain.build_report``（与
  ``coverage_by_domain.py`` 同一口径）；也可用 ``--coverage`` 读取已生成的
  ``docs/coverage-by-domain.json``。

优先级缺省规则（ARCHITECTURE §8）：

* P0 平台/基础：identity / system / catalog / logistics / openapi
* P1 主体/交易：merchant / wholesale / trade / payment / member
* P2 营销/内容/分析：marketing / content / analytics / im

若 ``domain-map.json`` 的 ``slices[]`` 显式给出 ``priority``（P0/P1/P2），以显式值为准。

用法::

    cd rust
    python tools/plan_domains.py                             # 打印摘要
    python tools/plan_domains.py --out docs/domain-plan.json # 写计划
    python tools/plan_domains.py --batch-size 8 --out x.json
    python tools/plan_domains.py --coverage docs/coverage-by-domain.json --out x.json

退出码：0 = 成功；2 = 工具错误（缺 domain-map / 契约不可读等）。
"""
from __future__ import annotations

import argparse
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)
DOCS = os.path.join(RUST, "docs")
CRATES = os.path.join(RUST, "crates")

sys.path.insert(0, HERE)
import coverage_by_domain as cbd  # noqa: E402  （覆盖率唯一计算入口）

ARCH_CTX = cbd.ARCH_CTX
PRIORITIES = ["P0", "P1", "P2"]

# ctx -> 缺省优先级（与 docs/domain-roadmap.md 的 P0/P1/P2 口径一致）。
CTX_PRIORITY = {
    "identity": "P0",
    "system": "P0",
    "catalog": "P0",
    "logistics": "P0",
    "openapi": "P0",
    "merchant": "P1",
    "wholesale": "P1",
    "trade": "P1",
    "payment": "P1",
    "member": "P1",
    "marketing": "P2",
    "content": "P2",
    "analytics": "P2",
    "im": "P2",
}

PRIORITY_RULE = {
    "P0": ["identity", "system", "catalog", "logistics", "openapi"],
    "P1": ["merchant", "wholesale", "trade", "payment", "member"],
    "P2": ["marketing", "content", "analytics", "im"],
}


def read_json(path):
    with open(path, encoding="utf-8-sig") as fh:
        return json.load(fh)


def rel(path):
    """相对 rust/ 的展示路径；跨盘符（测试用临时目录）时退化为绝对路径。"""
    try:
        return os.path.relpath(path, RUST).replace("\\", "/")
    except ValueError:
        return os.path.abspath(path).replace("\\", "/")


def priority_index(p):
    return PRIORITIES.index(p) if p in PRIORITIES else len(PRIORITIES)


def build_priority_overrides(domain_map):
    """从 domain-map slices[] 读取显式 priority，返回 ``{(ctx, feature): priority}``。"""
    out = {}
    if not domain_map:
        return out
    for s in domain_map.get("slices") or []:
        p = s.get("priority")
        if p not in PRIORITIES:
            continue
        ctx = s.get("ctx")
        if not (ctx and s.get("feature")):
            continue
        out[(ctx, s["feature"])] = p
        for sp in s.get("split") or []:
            if sp.get("feature"):
                out[(ctx, sp["feature"])] = p
    return out


def java_source_of(endpoint):
    """从契约端点的 ``source``（``path.java:39``）取 Java 文件路径。"""
    src = endpoint.get("source") or ""
    if not src:
        return ""
    # 只去掉末尾的 ":<行号>"，Windows 盘符形式（C:\...）不受影响。
    head, sep, tail = src.rpartition(":")
    if sep and tail.isdigit():
        return head
    return src


def target_files(ctx, feature, modules):
    """四层目标文件。api 层可能 1~2 个（同一 feature 可同时有 admin/client 路由）。"""
    api = []
    if "adminapi" in modules:
        api.append("crates/api/admin/src/routes/%s/%s.rs" % (ctx, feature))
    if "api" in modules:
        api.append("crates/api/public/src/routes/%s/%s.rs" % (ctx, feature))
    if not api:  # 模块信息缺失时两条都给，由实现者按接口清单裁剪
        api = [
            "crates/api/admin/src/routes/%s/%s.rs" % (ctx, feature),
            "crates/api/public/src/routes/%s/%s.rs" % (ctx, feature),
        ]
    return {
        "core": "crates/core/src/models/%s/%s.rs" % (ctx, feature),
        "infrastructure": "crates/infrastructure/src/db/repo/%s/%s.rs" % (ctx, feature),
        "services": "crates/services/src/%s/%s.rs" % (ctx, feature),
        "api": api,
    }


def build_plan(report, domain_map, batch_size, coverage_source):
    """把覆盖率报告转成分批计划 dict。"""
    overrides = build_priority_overrides(domain_map)
    items = []
    for f in report["by_feature"]:
        if not f["pending"]:
            continue
        ctx = f["ctx"]
        feature = f["feature"]
        priority = overrides.get((ctx, feature)) or CTX_PRIORITY.get(ctx, "P2")
        pending = f["pending_endpoints"]
        modules = sorted({e.get("module") for e in pending if e.get("module")})
        controllers = sorted({e.get("controller") for e in pending if e.get("controller")})
        java_sources = []
        for e in pending:
            src = java_source_of(e)
            if src and src not in java_sources:
                java_sources.append(src)
        items.append(
            {
                "priority": priority,
                "ctx": ctx,
                "feature": feature,
                "feature_key": "%s/%s" % (ctx, feature),
                "endpoint_count": f["pending"],
                "total_endpoints": f["total"],
                "implemented": f["implemented"],
                "controllers": controllers,
                "modules": modules,
                "java_sources": java_sources,
                "target_files": target_files(ctx, feature, set(modules)),
                "pending_endpoints": pending,
            }
        )

    items.sort(
        key=lambda x: (
            priority_index(x["priority"]),
            cbd.order_key(x["ctx"]),
            -x["endpoint_count"],
            x["feature"],
        )
    )

    # 分批：严格按 P0 -> P1 -> P2 分段，同一批不混优先级（便于并行派发）。
    batches = []
    for prio in PRIORITIES + [p for p in {x["priority"] for x in items} if p not in PRIORITIES]:
        group = [x for x in items if x["priority"] == prio]
        for i in range(0, len(group), max(1, batch_size)):
            chunk = group[i : i + max(1, batch_size)]
            batches.append(
                {"batch": len(batches) + 1, "priority": prio, "features": chunk}
            )

    per_priority = {}
    for x in items:
        rec = per_priority.setdefault(
            x["priority"], {"features": 0, "endpoints": 0}
        )
        rec["features"] += 1
        rec["endpoints"] += x["endpoint_count"]

    return {
        "generated_by": "rust/tools/plan_domains.py",
        "architecture": "rust/docs/ARCHITECTURE.md §8（工作单位 = 一个 feature）",
        "coverage_source": coverage_source,
        "priority_rule": {
            "default_by_ctx": PRIORITY_RULE,
            "override": "domain-map.json slices[].priority 优先",
        },
        "batch_size": batch_size,
        "total_endpoints": report["total"],
        "total_implemented_endpoints": report["implemented"],
        "total_pending_features": len(items),
        "total_pending_endpoints": report["pending"],
        "per_priority": per_priority,
        "batches": batches,
    }


def print_summary(plan, out_path):
    print("=" * 80)
    print("TigShop v2 分域实施批次计划（工作单位 = ctx/feature）")
    print("=" * 80)
    print(
        "覆盖率          : %d/%d（%d 待办接口）"
        % (
            plan["total_implemented_endpoints"],
            plan["total_endpoints"],
            plan["total_pending_endpoints"],
        )
    )
    print(
        "待办特性        : %d 个，分 %d 批（每批 %d）"
        % (plan["total_pending_features"], len(plan["batches"]), plan["batch_size"])
    )
    print("覆盖率数据来源  : %s" % plan["coverage_source"])
    for p in PRIORITIES:
        rec = plan["per_priority"].get(p)
        if rec:
            print("  %-3s %3d 个特性 / %4d 个接口" % (p, rec["features"], rec["endpoints"]))
    print("-" * 80)
    for b in plan["batches"]:
        names = ", ".join(
            "%s(%d)" % (f["feature_key"], f["endpoint_count"]) for f in b["features"]
        )
        print("  批 %3d [%-5s] %s" % (b["batch"], b["priority"], names))
    if out_path:
        print("\n已写出批次计划：%s" % out_path)


def main(argv=None):
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass

    ap = argparse.ArgumentParser(description="生成 v2 分域实施批次计划")
    ap.add_argument("--docs", default=DOCS)
    ap.add_argument("--crates", default=CRATES)
    ap.add_argument("--contract", default=None, help="默认 <docs>/api-contract.json")
    ap.add_argument("--domain-map", default=None, help="默认 <docs>/domain-map.json")
    ap.add_argument(
        "--coverage",
        default=None,
        help="读取已有 coverage-by-domain.json（缺省实时计算）",
    )
    ap.add_argument("--batch-size", type=int, default=6, help="每批特性数（默认 6）")
    ap.add_argument("--out", default=None, help="写出计划 JSON 的路径")
    ap.add_argument("--json", action="store_true", help="stdout 仅输出计划 JSON")
    args = ap.parse_args(argv)

    domain_map = None
    dm_rel = rel(args.domain_map or os.path.join(args.docs, "domain-map.json"))
    dm_path = args.domain_map or os.path.join(args.docs, "domain-map.json")
    if os.path.isfile(dm_path):
        try:
            domain_map = read_json(dm_path)
        except (OSError, ValueError) as exc:
            print("工具错误：domain-map.json 解析失败：%s" % exc, file=sys.stderr)
            return 2

    contract_path = args.contract or os.path.join(args.docs, "api-contract.json")

    if args.coverage:
        if not os.path.isfile(args.coverage):
            print("工具错误：覆盖率文件不存在：%s" % args.coverage, file=sys.stderr)
            return 2
        try:
            artifact = read_json(args.coverage)
            report = {
                "total": artifact["total"],
                "implemented": artifact["implemented"],
                "pending": artifact["pending"],
                "by_feature": artifact["by_feature"],
            }
        except (OSError, ValueError, KeyError) as exc:
            print("工具错误：覆盖率文件无效：%s" % exc, file=sys.stderr)
            return 2
        coverage_source = "file:%s" % rel(args.coverage)
    else:
        if domain_map is None:
            print(
                "工具错误：缺少权威归属文件 %s。\n"
                "请先由 domain-taxonomy 生成 docs/domain-map.json，"
                "或用 --coverage 指定已生成的 docs/coverage-by-domain.json。" % dm_rel,
                file=sys.stderr,
            )
            return 2
        if not os.path.isfile(contract_path):
            print("工具错误：契约文件缺失：%s" % contract_path, file=sys.stderr)
            return 2
        try:
            contract = read_json(contract_path)
        except (OSError, ValueError) as exc:
            print("工具错误：契约文件无法解析：%s" % exc, file=sys.stderr)
            return 2
        try:
            slices = cbd.load_slices(args.docs)
            report = cbd.build_report(domain_map, contract, args.crates, slices)
        except OSError as exc:
            print("工具错误：扫描 Rust 源码失败：%s" % exc, file=sys.stderr)
            return 2
        coverage_source = "实时计算（coverage_by_domain.build_report）"

    plan = build_plan(report, domain_map, args.batch_size, coverage_source)

    if not plan["total_pending_features"]:
        print("✅ 契约接口已全部实现，无需分批。")
        if args.out:
            with open(args.out, "w", encoding="utf-8", newline="\n") as fh:
                json.dump(plan, fh, ensure_ascii=False, indent=1)
                fh.write("\n")
            print("已写出批次计划：%s" % args.out)
        return 0

    if args.out:
        try:
            with open(args.out, "w", encoding="utf-8", newline="\n") as fh:
                json.dump(plan, fh, ensure_ascii=False, indent=1)
                fh.write("\n")
        except OSError as exc:
            print("工具错误：写出 %s 失败：%s" % (args.out, exc), file=sys.stderr)
            return 2

    if args.json:
        print(json.dumps(plan, ensure_ascii=False, indent=2))
    else:
        print_summary(plan, args.out)
    return 0


if __name__ == "__main__":
    sys.exit(main())
