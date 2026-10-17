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

    # ⚠️ 不要再用 docs/pending-slices.json 判断「未完成」：那是早期手工快照，
    # 没有生成器、会过期（实测它仍把已实现并已注册的 slice137/194 等列为待办，
    # 导致编排重复派发已完成切片）。改为**以源码实际内容为准**：
    # 服务层文件是每个切片必然产出的核心文件，它存在即视为已开工。
    crates = os.path.join(RUST, "crates")

    def is_skeleton(text):
        """识别 scaffold_slices.py 生成的**空骨架**。

        骨架特征：文件头写着「骨架」，且**没有 `async fn`**（无任何业务逻辑），
        `routes()` 返回空 `Router::new()`、`implemented_routes()` 返回空 Vec。
        这类切片**接口完全不可访问**，必须当作待办重新实现
        （见 .workbuddy/memory/2026-10-15.md：曾因把骨架当「已实现」
        而虚报进度 62 片 / 257 个接口）。
        """
        return "骨架" in text and "async fn" not in text

    def needs_work(key):
        p = os.path.join(crates, "services", "src", "%s.rs" % key)
        if not os.path.isfile(p):
            return True
        try:
            with open(p, encoding="utf-8", errors="replace") as fh:
                return is_skeleton(fh.read())
        except OSError:
            return True

    pend_keys = {
        s["slice_id"].replace("-", "")
        for s in slices
        if needs_work(s["slice_id"].replace("-", ""))
    }

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
