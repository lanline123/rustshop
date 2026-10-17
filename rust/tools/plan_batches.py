#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""按依赖顺序给未完成切片排批次，输出 --json 供编排使用。

排序原则（先地基、后上层）：
1. 基础数据域（地区/区号/物流公司/运费模板/支付方式/字典/配置）—— 多域引用
2. 主体域（店铺/商户/供应商/会员/管理员/权限/客服）—— 业务主体
3. 资金域（保证金/账户/充值/提现/对账单/结算）—— 依赖主体域
4. 商品扩展（分组/属性模板/服务/样式/批发/库存）
5. 营销域（优惠券/拼团/抽奖/满减/赠品/分销）
6. 内容与集成（文章/消息/公众号/微信直播/打印机/视频相册）—— 叶子
"""
from __future__ import annotations

import argparse
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)

# 关键词 -> 波次（数字越小越先做）。按 tag / 控制器路径匹配。
WAVES = [
    (1, "基础数据", [
        "地区", "地区管理", "区号", "物流公司", "运费", "支付方式", "字典",
        "配置", "分类", "品牌", "物流", "模板",
    ]),
    (2, "主体域", [
        "店铺", "商户", "供应商", "会员", "管理员", "权限", "客服",
        "登录", "账号", "员工", "入驻", "认证",
    ]),
    (3, "资金域", [
        "保证金", "账户", "充值", "提现", "对账", "结算", "余额", "发票",
        "收款", "费率",
    ]),
    (4, "商品扩展", [
        "商品", "库存", "批发", "分组", "属性", "服务", "样式", "卡券",
        "规格", "赠品",
    ]),
    (5, "营销域", [
        "优惠券", "拼团", "抽奖", "满减", "秒杀", "分销", "活动", "代客",
        "购物车", "订单", "售后", "评价", "评论", "询价", "留言", "提现申请",
    ]),
    (6, "内容集成", [
        "公众号", "微信", "直播", "打印机", "视频", "相册", "文章", "消息",
        "站内信", "友情链接", "标签", "分类栏", "首页", "装修",
    ]),
]


def wave_of(tag, key):
    hay = (tag or "") + " " + (key or "")
    for num, _name, kws in WAVES:
        for kw in kws:
            if kw in hay:
                return num
    return 6


def main():
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    except Exception:
        pass

    ap = argparse.ArgumentParser()
    ap.add_argument("--json", action="store_true")
    ap.add_argument("--batch-size", type=int, default=6)
    args = ap.parse_args()

    slices = json.load(open(os.path.join(RUST, "docs", "slices.json"), encoding="utf-8"))
    crates = os.path.join(RUST, "crates")

    # ⚠️ 判断「未完成」**必须用权威的接口覆盖率口径**，不能用文件启发式。
    #
    # 历史教训（两次踩坑）：
    #   1) 最初读 docs/pending-slices.json（早期手工快照、无生成器）→ 过期，
    #      把已实现已注册的切片列为待办，重复派发。
    #   2) 改成「services/<slice>.rs 存在且含 async fn 即完成」→ 仍然会漏：
    #      实测 slice-002（订单管理 0/30）、slice-041（0/5）、slice-194（0/4）
    #      都有真实 service 文件，但 **routes 文件缺失或不完整 → 路由从未注册**，
    #      接口 100% 不可达，却被判为「已完成」；另外**空骨架**切片
    #      （scaffold_slices.py 生成，routes() 返回空 Router）也含不了任何 async fn，
    #      用「有没有 async fn」判定会随脚手架写法漂移。
    #
    # 唯一可靠的判据：**把切片的每个接口拿去和 `check_api_coverage.py`
    # 静态提取的真实 axum 路由比对，只要有一条未实现即为待办。**
    # 复用同一个提取器，避免两套口径再次漂移（这正是本仓库反复出现的根因）。
    sys.path.insert(0, HERE)
    import check_api_coverage as cac

    contract = cac.load_contract(os.path.join(RUST, "docs", "api-contract.json"))
    rust_routes = cac.collect_rust_routes(crates)
    _report = cac.analyse(contract, rust_routes)
    _missing = {(e["method"].upper(), e["path"]) for e in _report["missing"]}
    _all = {(e["method"].upper(), e["path"]) for e in contract["endpoints"]}
    implemented = _all - _missing

    pend_keys = set()
    for s in slices:
        key = s["slice_id"].replace("-", "")
        apis = s.get("apis") or []
        if not apis:
            continue
        if any((a["method"].upper(), a["path"]) not in implemented for a in apis):
            pend_keys.add(key)

    items = []
    for s in slices:
        key = s["slice_id"].replace("-", "")
        if key not in pend_keys:
            continue
        w = wave_of(s.get("tag"), " ".join(s.get("controllers") or []))
        items.append({
            "slice_id": s["slice_id"],
            "key": key,
            "wave": w,
            "tag": s.get("tag"),
            "tier": s.get("tier"),
            "api_count": s.get("api_count"),
            "modules": s.get("modules"),
            "controllers": s.get("controllers"),
            "java_sources": s.get("java_sources"),
            "owned_files": s.get("owned_files"),
            "apis": s.get("apis"),
        })

    # 波次内：接口少的先做（快速起量），稳定排序保证可复现
    items.sort(key=lambda x: (x["wave"], x["api_count"] or 0, x["slice_id"]))

    batches = []
    for i in range(0, len(items), args.batch_size):
        batches.append(items[i:i + args.batch_size])

    if args.json:
        json.dump({"total": len(items), "batches": batches}, sys.stdout,
                  ensure_ascii=False, indent=1)
        return 0

    print("未完成切片 %d 个，分 %d 批（每批 %d）：" % (len(items), len(batches), args.batch_size))
    for bi, b in enumerate(batches, 1):
        w = b[0]["wave"]
        print("  批 %2d (wave%d)  %s" % (bi, w, ", ".join(x["slice_id"] for x in b)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
