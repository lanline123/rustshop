#!/usr/bin/env python3
"""Generate a draft slice -> (ctx, feature) migration map from authoritatives.

Outputs rust/docs/_migration-map.json. Overrides for the known ambiguous slices
are kept in OVERRIDES below so they are reviewable in one place.
"""
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

PKG_CTX = {
    "account": "identity", "authority": "identity", "common": "system",
    "content": "content", "decorate": "content", "decorate/discrete": "content",
    "deposit": "merchant", "example": "system", "finance": "payment", "im": "im",
    "merchant": "merchant", "msg": "system", "openapi/workbuddy": "openapi",
    "order": "trade", "panel": "analytics", "print": "logistics",
    "product": "catalog", "promotion": "marketing", "proxyshopping": "trade",
    "salesman": "marketing", "search": "catalog", "setting": "system",
    "setting/wechat": "content", "shop": "merchant", "user": "member",
    "wholesale": "wholesale", "app": "system", "article": "content",
    "buyershow": "marketing", "cart": "trade", "home": "content", "sys": "system",
}

# Controllers whose business domain differs from their Java package.
CTRL_CTX = {
    "ApplePayController": "payment",
    "PayController": "payment",
    "AfterSalesController": "trade",
    "UserOrderController": "trade",
    "OrderInvoiceClientController": "trade",
    "WithDrawApplyClientController": "payment",
    "RechargeOrderController": "payment",
    "UserCouponController": "marketing",
    "LogisticsCompanyController": "logistics",
    "LogisticsCompanyConfigController": "logistics",
    "ShippingTplController": "logistics",
    "ShippingTypeController": "logistics",
    "GalleryController": "content",
    "GalleryPicController": "content",
    "GalleryVideoController": "content",
    "GalleryVideoInfoController": "content",
    "SeoController": "content",
    "FriendLinksController": "content",
    "TaxRateController": "payment",
    "ApplePayConfigController": "payment",
    "HuifuConfigController": "payment",
    "LakalaConfigController": "payment",
    "YeepayConfigController": "payment",
    "MessagePushController": "system",
}

# Slices where the controller-derived name is ambiguous or clashes with a
# pre-existing domain module. feature is the final module name.
OVERRIDES = {
    # ---- 多控制器切片（迁移时按控制器人工拆分）----
    "slice-001": ("catalog", "product_manage", "multi-controller: ProductBatch+Product+Search+SearchGuess"),
    "slice-137": ("openapi", "workbuddy", "multi-controller: MessagePush+WorkBuddy*"),
    "slice-194": ("analytics", "statistics", "StatisticsAccess+StatisticsUser"),
    # ---- 同一控制器拆成多片：admin/client 或不同子域 ----
    "slice-002": ("trade", "order_admin", "OrderController(adminapi)，基线下单流程在 trade::order"),
    "slice-007": ("member", "user_center", "UserController(tigshop-api C 端用户中心)"),
    "slice-009": ("trade", "aftersales_client", "AfterSalesController(tigshop-api 售后)"),
    "slice-012": ("member", "user_manage", "UserController(tigshop-adminapi 会员管理)"),
    "slice-014": ("merchant", "shop_manage", "ShopController(adminapi)，基线店铺查询在 merchant::shop"),
    "slice-019": ("system", "app_version_admin", "AppVersionController(adminapi)"),
    "slice-022": ("marketing", "buyer_show_client", "BuyerShowController(tigshop-api C 端)"),
    "slice-024": ("marketing", "buyer_show", "BuyerShowController(adminapi 主体)"),
    "slice-026": ("payment", "paylog_batch", "PaylogController 批处理/字段更新"),
    "slice-051": ("catalog", "search_admin", "SearchController(adminapi 聚合搜索)"),
    "slice-073": ("payment", "user_account", "AccountController(tigshop-api 佣金/账户)"),
    "slice-084": ("catalog", "product_style_client", "ProductStyleController(tigshop-api)"),
    "slice-085": ("marketing", "product_style_admin", "ProductStyleController(adminapi)"),
    "slice-091": ("system", "region_client", "RegionController(tigshop-api)"),
    "slice-092": ("system", "region", "RegionController(adminapi)"),
    "slice-111": ("merchant", "shop_account_log", "AccountController(adminapi 店铺资金变化)"),
    "slice-135": ("content", "article_batch", "ArticleController 剩余（batch/issueInfo）"),
    "slice-136": ("content", "article_category", "ArticleCategoryController 剩余（update/batch）"),
    "slice-143": ("system", "app_version_client", "AppVersionController(tigshop-api 升级检查)"),
    "slice-187": ("system", "config_basic", "ConfigController 基础/商品/购物设置"),
    "slice-188": ("system", "config_style", "ConfigController 装修/详情/搜索样式"),
    "slice-189": ("system", "config_cashier", "ConfigController 收银订单设置"),
    "slice-200": ("trade", "cart_summary", "CartController 剩余（getCount/getCouponDiscount）"),
    "slice-207": ("system", "config_client", "ConfigController(tigshop-api 前台配置)"),
    "slice-216": ("marketing", "time_discount_batch", "TimeDiscountController 剩余（batch）"),
    "slice-217": ("marketing", "seckill_batch", "SeckillController 剩余（batch/decorate 列表）"),
}


def snake(name: str) -> str:
    name = re.sub(r"Controller$", "", name)
    s = re.sub(r"(?<=[a-z0-9])([A-Z])", r"_\1", name)
    s = re.sub(r"(?<=[A-Z])([A-Z][a-z])", r"_\1", s)
    return s.lower().strip("_")


def pascal(s: str) -> str:
    return "".join(p.capitalize() for p in s.split("_"))


def slice_key(sid: str) -> str:
    return sid.replace("-", "")


def files_for(key: str):
    cands = {
        "models": f"crates/core/src/models/{key}.rs",
        "repo": f"crates/infrastructure/src/db/repo/{key}.rs",
        "service": f"crates/services/src/{key}.rs",
        "admin_route": f"crates/api/admin/src/routes/{key}.rs",
        "public_route": f"crates/api/public/src/routes/{key}.rs",
        "admin_root": f"crates/api/admin/src/root/{key}.rs",
        "public_root": f"crates/api/public/src/root/{key}.rs",
    }
    return {k: v for k, v in cands.items() if os.path.isfile(os.path.join(ROOT, v))}


def main():
    slices = json.load(open(os.path.join(ROOT, "docs/slices.json"), encoding="utf-8"))
    contract = json.load(open(os.path.join(ROOT, "docs/api-contract.json"), encoding="utf-8"))
    # controller -> (module, package) from contract
    ctrl_meta = {}
    for ep in contract["endpoints"]:
        src = ep["source"].split(":")[0]
        parts = src.split("\\")
        mod = parts[0]
        if "controller" in parts:
            i = parts.index("controller")
            pkg = "/".join(parts[i + 1:-1]) or "(root)"
        else:
            pkg = "(noncontroller)"
        ctrl_meta.setdefault(ep["controller"], set()).add((mod, pkg))

    out = []
    problems = []
    for s in slices:
        sid = s["slice_id"]
        key = slice_key(sid)
        present = files_for(key)
        rec = {
            "slice_id": sid,
            "ctx": None,
            "feature": None,
            "type_prefix": None,
            "coded": bool(present),
            "files": present,
            "api_count": s["api_count"],
            "controllers": s["controllers"],
            "tag": s["tag"],
            "java_modules": s["modules"],
            "notes": "",
        }
        if not present:
            rec["notes"] = "not implemented"
            # still assign a target ctx/feature for the roadmap
            ctrl = s["controllers"][0] if s["controllers"] else None
            metas = sorted(ctrl_meta.get(ctrl, []) or [])
            pkg = metas[0][1] if metas else None
            rec["ctx"] = CTRL_CTX.get(ctrl) or PKG_CTX.get(pkg) or "system"
            rec["feature"] = snake(ctrl) if ctrl else f"batch_{key[-3:]}"
            rec["type_prefix"] = pascal(rec["feature"])
            out.append(rec)
            continue

        if sid in OVERRIDES:
            ctx, feat, note = OVERRIDES[sid]
            rec["ctx"], rec["feature"], rec["notes"] = ctx, feat, note
        else:
            # primary controller = the one with the most endpoints in this slice
            counts = {}
            for a in s["apis"]:
                counts[a["controller"]] = counts.get(a["controller"], 0) + 1
            ctrl = max(counts, key=counts.get) if counts else s["controllers"][0]
            metas = sorted(ctrl_meta.get(ctrl, []) or [])
            pkg = metas[0][1] if metas else None
            rec["ctx"] = CTRL_CTX.get(ctrl) or PKG_CTX.get(pkg)
            rec["feature"] = snake(ctrl)
            if not rec["ctx"]:
                problems.append(f"{sid}: unknown ctx for {ctrl} pkg={pkg}")
        rec["type_prefix"] = pascal(rec["feature"])
        # classify real implementation vs empty scaffold (deleted, not migrated)
        rec["deleted"] = False
        if present:
            svc = present.get("service")
            has_async = False
            is_skel = False
            if svc:
                txt = open(os.path.join(ROOT, svc), encoding="utf-8", errors="replace").read()
                has_async = "async fn" in txt
                is_skel = ("骨架" in txt) and not has_async
            has_route = any(
                ".route(" in open(os.path.join(ROOT, p), encoding="utf-8", errors="replace").read()
                for k, p in present.items() if "route" in k or "root" in k
            )
            rec["real"] = has_async or has_route
            rec["deleted"] = not rec["real"]
            if not svc and not has_route:
                rec["deleted"] = True
                rec["notes"] = (rec["notes"] + " " if rec["notes"] else "") + "empty scaffold / partial file, dropped"
            elif is_skel and not has_route:
                rec["notes"] = (rec["notes"] + " " if rec["notes"] else "") + "empty scaffold, dropped"
        else:
            rec["real"] = False
        out.append(rec)

    # uniqueness within ctx for migrated (real) slices
    seen = {}
    for r in out:
        if not r.get("real"):
            continue
        if r["ctx"] not in CONTEXTS:
            problems.append(f"{r['slice_id']}: bad ctx {r['ctx']}")
        k = (r["ctx"], r["feature"])
        if k in seen:
            problems.append(f"COLLISION {k}: {seen[k]} vs {r['slice_id']}")
        seen[k] = r["slice_id"]

    doc = {"version": "draft", "contexts": CONTEXTS, "slices": out}
    dst = os.path.join(ROOT, "docs/_migration-map.json")
    with open(dst, "w", encoding="utf-8") as f:
        json.dump(doc, f, ensure_ascii=False, indent=1)

    # report
    coded = [r for r in out if r["coded"]]
    real = [r for r in out if r.get("real")]
    deleted = [r for r in out if r["coded"] and not r.get("real")]
    print(f"slices={len(out)} coded={len(coded)} real={len(real)} scaffold/dropped={len(deleted)} output={dst}")
    print(f"problems={len(problems)}")
    for p in problems:
        print("  !", p)
    from collections import Counter
    print("\nctx distribution (real):", dict(Counter(r["ctx"] for r in real)))
    print("\nscaffold/dropped:", ",".join(r["slice_id"] for r in deleted))


if __name__ == "__main__":
    main()
