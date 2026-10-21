export default {
    path: "/promotion",
    name: "promotion",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "营销" },
    children: [
        {
            path: "/promotion",
            name: "promotionManage",
            meta: { title: "营销概览" },
            component: () => import("@/views/promotion/manage/List.vue")
        },
        {
            path: "/promotionStyle",
            name: "promotionStyleManage",
            meta: { title: "商品样式" },
            component: () => import("@/views/promotion/productStyle/List.vue")
        },
        {
            path: "",
            name: "promotionOverview",
            meta: { title: "营销管理" },
            redirect: "/promotion/coupon/list",
            children: [
                {
                    path: "coupon/list",
                    name: "couponManage",
                    meta: { title: "优惠券" },
                    component: () => import("@/views/promotion/coupon/List.vue")
                },
                {
                    path: "seckill/list",
                    name: "seckillManage",
                    meta: { title: "限时秒杀" },
                    component: () => import("@/views/promotion/seckill/List.vue")
                },
                {
                    path: "full-reduction/list",
                    name: "fullReductionManage",
                    meta: { title: "满减活动" },
                    component: () => import("@/views/promotion/productActivity/fullReduction/List.vue")
                },
                {
                    path: "reward-gift/list",
                    name: "rewardGiftManage",
                    meta: { title: "满赠活动" },
                    component: () => import("@/views/promotion/productActivity/rewardGift/List.vue")
                },
                {
                    path: "limitdiscount-gift/list",
                    name: "limitdiscountManage",
                    meta: { title: "限时折扣" },
                    component: () => import("@/views/promotion/productActivity/limitdiscount/List.vue")
                },
                {
                    path: "groupon/list",
                    name: "grouponPromotionManage",
                    meta: { title: "拼团活动" },
                    component: () => import("@/views/promotion/groupon/List.vue")
                },
                {
                    path: "product-gift/list",
                    name: "productGiftManage",
                    meta: { title: "活动赠品" },
                    component: () => import("@/views/promotion/productGift/List.vue")
                },
                {
                    path: "points-exchange/list",
                    name: "pointsExchangeManage",
                    meta: { title: "积分商品" },
                    component: () => import("@/views/promotion/pointsExchange/List.vue")
                },
                {
                    path: "sign-in-setting/list",
                    name: "signInSettingManage",
                    meta: { title: "积分签到" },
                    component: () => import("@/views/promotion/signInSetting/List.vue")
                },
                {
                    path: "recharge-setting/list",
                    name: "rechargeSettingManage",
                    meta: { title: "余额充值" },
                    component: () => import("@/views/promotion/rechargeSetting/List.vue")
                },
                {
                    path: "live/list",
                    name: "liveManage",
                    meta: { title: "微信直播" },
                    component: () => import("@/views/promotion/live/List.vue")
                },
                {
                    path: "e-card/list",
                    name: "eCardManage",
                    meta: { title: "电子卡券" },
                    component: () => import("@/views/promotion/eCard/List.vue")
                },
                {
                    path: "buyerShow/list",
                    name: "buyerShowManage",
                    meta: { title: "买家秀" },
                    component: () => import("@/views/promotion/buyerShow/List.vue")
                },
                {
                    path: "giftCard/list",
                    name: "giftCardManage",
                    meta: { title: "礼品卡" },
                    component: () => import("@/views/promotion/giftCard/List.vue")
                },
                {
                    path: "lottery/index",
                    name: "lotteryManage",
                    meta: { title: "抽奖" },
                    component: () => import("@/views/promotion/lottery/List.vue")
                }
            ]
        }
    ]
};
