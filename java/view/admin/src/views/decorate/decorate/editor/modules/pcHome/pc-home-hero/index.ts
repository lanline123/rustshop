/**
 * PC 营销通栏（pc-home-hero，新套可视化，对齐 Vort）
 * - 三列布局：左类目导航 + 中轮播/频道卡 + 右会员卡/店铺街
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { LinkItem, PicListItem } from "../_shared/types";
import type { PcHomeHeroCenterCard, PcHomeHeroModule } from "./types";

export const defaultPremiumMemberLink: LinkItem = {
    path: "base",
    label: "权益卡中心",
    name: "权益卡中心",
    link: "member/premiumMember/index"
};

export type { PcHomeHeroCenterCard, PcHomeHeroLeftConfig, PcHomeHeroModule, PcHomeHeroRightConfig, PcHomeHeroStyle } from "./types";

registerModule({
    type: "pc-home-hero",
    label: "营销通栏",
    icon: '<i class="iconfont-admin icon-category_nav"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 4,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcHomeHeroDefaults) as Record<string, unknown>
});

// ==================== 默认值定义（必须可见演示数据，与 Vort 逐字段一致） ====================

const svgPlaceholder = (size: number, bg = "#f2f3f5", radius = 8) => {
    const svg = `<svg xmlns='http://www.w3.org/2000/svg' width='${size}' height='${size}'><rect fill='${bg}' width='${size}' height='${size}' rx='${radius}'/><path d='M${Math.round(
        size * 0.25
    )} ${Math.round(size * 0.62)} L${Math.round(size * 0.45)} ${Math.round(size * 0.42)} L${Math.round(
        size * 0.62
    )} ${Math.round(size * 0.58)} L${Math.round(size * 0.75)} ${Math.round(
        size * 0.38
    )}' stroke='#c0c4cc' stroke-width='${Math.max(1, Math.round(size * 0.06))}' fill='none' stroke-linecap='round' stroke-linejoin='round'/></svg>`;
    return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
};

const defaultIcon = svgPlaceholder(64, "#f7f8fa", 12);
export const defaultAvatar = "https://oss.vortmall.com/upload/c4ca4238/2026/02/09/9be6e26cd04b449a8e843df6f790d601.png";

/** 左侧类目演示数据 */
const demoCategories: PicListItem[] = [
    {
        picId: "1377",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947YV2lncqLSOSg8OvpbK.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947YV2lncqLSOSg8OvpbK.png",
        picName: "dangrisuda",
        picTitle: "女装 / 男装 / 内衣",
        picDesc: "",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1384",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957948LcXRbvCuwlCHyHy7Y0.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957948LcXRbvCuwlCHyHy7Y0.png",
        picName: "xiuxianlingshi",
        picTitle: "鞋包 / 女靴 / 男鞋",
        picDesc: "",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1378",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947XV6cPNonZJurGcDfOP.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947XV6cPNonZJurGcDfOP.png",
        picName: "mimianliangyou",
        picTitle: "美妆 / 养护 / 美容",
        picDesc: "",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1385",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/17309579481I5gopEn7PkOneF0NU.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/17309579481I5gopEn7PkOneF0NU.png",
        picName: "ruyinjiushui",
        picTitle: "冰洗 / 空调 / 电视 / 厨卫大件",
        picDesc: "",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1382",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957948Ouz6wPq8cQkgSjDCkF.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957948Ouz6wPq8cQkgSjDCkF.png",
        picName: "riyongbaihuo",
        picTitle: "电脑 / 办公 / 文具用品",
        picDesc: "",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1380",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947iOLEngIwvzxZDjhtQ6.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947iOLEngIwvzxZDjhtQ6.png",
        picName: "muyingyongpin",
        picTitle: "食品 / 酒类 / 生鲜 / 特产",
        picDesc: "",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1379",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947lWqY7UySCwQ89qsq7f.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947lWqY7UySCwQ89qsq7f.png",
        picName: "gerenxihu",
        picTitle: "生活电器 / 厨房小电 / 个护健康",
        picDesc: "",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1383",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957948vFCfxjt0TIROPOocWI.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957948vFCfxjt0TIROPOocWI.png",
        picName: "zhipinjiaqing",
        picTitle: "医药 / 器械 / 保健品",
        picDesc: "",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1376",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947BswaPOfoOZnn8tDSX6.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947BswaPOfoOZnn8tDSX6.png",
        picName: "meironghufu",
        picTitle: "配饰 / 手表 / 玉石",
        picDesc: "",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1381",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947vIKQibO98m2q9oP8ct.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947vIKQibO98m2q9oP8ct.png",
        picName: "fangbiansushi",
        picTitle: "家装 / 厨卫 / 家具",
        picDesc: "",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1386",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947YV2lncqLSOSg8OvpbK.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947YV2lncqLSOSg8OvpbK.png",
        picName: "qichepeijian",
        picTitle: "汽车 / 摩托 / 配件",
        picDesc: "",
        picLink: { link: "", name: "" }
    }
];

/** 中间轮播演示数据 */
const demoBanners: PicListItem[] = [
    {
        picId: "1",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp",
        picName: "演示 Banner 1",
        picTitle: "限时直降",
        picLink: { link: "", name: "" }
    },
    {
        picId: "2",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp",
        picName: "演示 Banner 2",
        picTitle: "新品上新",
        picLink: { link: "", name: "" }
    }
];

/** 演示商品数据 */
const demoProducts = [
    {
        pic: {
            picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872pw39D1oGqdAaZMpvyo.jpg",
            picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872pw39D1oGqdAaZMpvyo.jpg"
        },
        price: "¥88.8"
    },
    {
        pic: {
            picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872f6EcZnbAqdW5kIGzxL.jpg",
            picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872f6EcZnbAqdW5kIGzxL.jpg"
        },
        price: "¥56.8"
    },
    {
        pic: {
            picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872o3s8aqPGdICr1DOCdB.jpg",
            picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872o3s8aqPGdICr1DOCdB.jpg"
        },
        price: "¥48.8"
    },
    {
        pic: {
            picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872Mry8L9BWOCiawPhp72.jpg",
            picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872Mry8L9BWOCiawPhp72.jpg"
        },
        price: "¥48.8"
    },
    {
        pic: {
            picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872yPYr4zSj7AXrfw9oON.jpg",
            picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872yPYr4zSj7AXrfw9oON.jpg"
        },
        price: "¥32.0"
    },
    {
        pic: {
            picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872NlX49KeGeYSN4Tw0wa.jpg",
            picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872NlX49KeGeYSN4Tw0wa.jpg"
        },
        price: "¥32.0"
    }
] as const;

/** 中间卡位演示数据（4个频道，双图模式） */
const demoCards: PcHomeHeroCenterCard[] = [
    {
        title: "严选臻品",
        badgeText: "优中选优",
        link: { link: "", name: "" },
        products: [
            { pic: demoProducts[0].pic, price: demoProducts[0].price },
            { pic: demoProducts[1].pic, price: demoProducts[1].price }
        ]
    },
    {
        title: "新品上市",
        badgeText: "重磅上新",
        link: { link: "", name: "" },
        products: [
            { pic: demoProducts[2].pic, price: demoProducts[2].price, label: "上新底价", labelBgColorA: "#00C176", labelBgColorB: "#008856" },
            { pic: demoProducts[3].pic, price: demoProducts[3].price, label: "多人加购", labelBgColorA: "#00C176", labelBgColorB: "#008856" }
        ]
    },
    {
        title: "领券中心",
        badgeText: "券",
        link: { link: "", name: "" },
        products: [
            { pic: demoProducts[4].pic, price: demoProducts[4].price },
            { pic: demoProducts[5].pic, price: demoProducts[5].price }
        ]
    },
    {
        title: "组团拼单",
        badgeText: "拼享低价",
        link: { link: "", name: "" },
        products: [
            { pic: demoProducts[0].pic, price: demoProducts[0].price, label: "2人团", labelBgColorA: "#FF9000", labelBgColorB: "#FF5000" },
            { pic: demoProducts[1].pic, price: demoProducts[1].price, label: "5人团", labelBgColorA: "#FF9000", labelBgColorB: "#FF5000" }
        ]
    }
];

/** 右侧推荐位（2个） */
const demoPromoCards: PcHomeHeroCenterCard[] = [
    {
        title: "限时秒杀",
        badgeText: "钜惠开秒",
        link: { link: "", name: "" },
        products: [
            { pic: demoProducts[2].pic, price: demoProducts[2].price, label: "秒杀价", labelBgColorA: "#FF4E4E", labelBgColorB: "#E1251B" },
            { pic: demoProducts[3].pic, price: demoProducts[3].price, label: "秒杀价", labelBgColorA: "#FF4E4E", labelBgColorB: "#E1251B" }
        ]
    },
    {
        title: "商家严选",
        badgeText: "品质好物",
        link: { link: "", name: "" },
        products: [
            { pic: demoProducts[4].pic, price: demoProducts[4].price, label: "历史低价", labelBgColorA: "#FF4E4E", labelBgColorB: "#E1251B" },
            { pic: demoProducts[5].pic, price: demoProducts[5].price, label: "多人复购", labelBgColorA: "#FF4E4E", labelBgColorB: "#E1251B" }
        ]
    }
];

// 合并为 modules 列表
const demoModules: PcHomeHeroCenterCard[] = [
    // 0: 限时秒杀
    {
        moduleTitle: "限时秒杀",
        moduleSubTitle: "钜惠开秒",
        moduleSubTitleBgColorA: "#FF4E4E",
        moduleSubTitleBgColorB: "#E1251B",
        moduleSubTitleBgType: "leftRight",
        showPrice: 1,
        link: { link: "", name: "" },
        products: demoPromoCards[0]!.products
    },
    // 1: 商家严选
    {
        moduleTitle: "商家严选",
        moduleSubTitle: "品质好物",
        moduleSubTitleBgColorA: "#FF4E4E",
        moduleSubTitleBgColorB: "#E1251B",
        moduleSubTitleBgType: "leftRight",
        showPrice: 1,
        link: { link: "", name: "" },
        products: demoPromoCards[1]!.products
    },
    // 2: 严选臻品
    {
        moduleTitle: "严选臻品",
        moduleSubTitle: "优中选优",
        moduleSubTitleBgColorA: "#6C8CFF",
        moduleSubTitleBgColorB: "#3A5BFF",
        moduleSubTitleBgType: "leftRight",
        showPrice: 1,
        link: { link: "", name: "" },
        products: demoCards[0]!.products
    },
    // 3: 新品上市
    {
        moduleTitle: "新品上市",
        moduleSubTitle: "重磅上新",
        moduleSubTitleBgColorA: "#00C176",
        moduleSubTitleBgColorB: "#008856",
        moduleSubTitleBgType: "leftRight",
        showPrice: 1,
        link: { link: "", name: "" },
        products: demoCards[1]!.products
    },
    // 4: 领券中心
    {
        moduleTitle: "领券中心",
        moduleSubTitle: "券",
        moduleSubTitleBgColorA: "#FF4E4E",
        moduleSubTitleBgColorB: "#E1251B",
        moduleSubTitleBgType: "leftRight",
        showPrice: 1,
        link: { link: "", name: "" },
        products: demoCards[2]!.products
    },
    // 5: 组团拼单
    {
        moduleTitle: "组团拼单",
        moduleSubTitle: "拼享低价",
        moduleSubTitleBgColorA: "#FF9000",
        moduleSubTitleBgColorB: "#FF5000",
        moduleSubTitleBgType: "leftRight",
        showPrice: 1,
        link: { link: "", name: "" },
        products: demoCards[3]!.products
    }
];

/** 右侧快捷入口（店铺街图标） */
const demoShortcuts: PicListItem[] = [
    {
        picId: "s1",
        picThumb: defaultIcon,
        picUrl: defaultIcon,
        picName: "shop_1",
        picTitle: "进入店铺",
        picLink: { link: "", name: "" }
    },
    {
        picId: "s2",
        picThumb: defaultIcon,
        picUrl: defaultIcon,
        picName: "shop_2",
        picTitle: "进入店铺",
        picLink: { link: "", name: "" }
    },
    {
        picId: "s3",
        picThumb: defaultIcon,
        picUrl: defaultIcon,
        picName: "shop_3",
        picTitle: "进入店铺",
        picLink: { link: "", name: "" }
    },
    {
        picId: "s4",
        picThumb: defaultIcon,
        picUrl: defaultIcon,
        picName: "shop_4",
        picTitle: "进入店铺",
        picLink: { link: "", name: "" }
    }
];

/** 模块默认值 */
export const pcHomeHeroDefaults: PcHomeHeroModule = {
    frame: {
        boxPadding: 0,
        boxPaddingTop: 0,
        boxPaddingBottom: 0,
        backgroundColor: ""
    },
    left: {
        title: "全部商品分类",
        showCount: 11,
        categories: demoCategories,
        backgroundColor: "#ffffff",
        textColor: "#333333"
    },
    center: {
        banners: demoBanners,
        modules: demoModules
    },
    right: {
        user: {
            avatar: { picUrl: defaultAvatar, picThumb: defaultAvatar, picName: "avatar" },
            nickname: "欢迎来商城",
            welcomeText: "登录后更多精彩"
        },
        userCardBackground: "#ffffff",
        userCardGradientColorA: "#FFEDE1",
        userCardGradientColorB: "#FCF9F8",
        userCardGradientType: "upDown",
        premiumMember: {
            show: false,
            title: "会员",
            description: "",
            link: { ...defaultPremiumMemberLink }
        },
        stats: [
            { label: "优惠券", value: "85" },
            { label: "余额", value: "257.58" },
            { label: "积分", value: "544" }
        ],
        shortcuts: demoShortcuts,
        // 店铺街配置
        shopsTitle: "店铺街",
        shopsSubTitle: "HOT",
        shopsSubTitleBg: "#FF4E4E",
        shopsMoreText: "查看全部",
        shopsMoreLink: { link: "", name: "" },
        shopsTextColor: "#a06a38"
    },
    style: {
        height: 336,
        gap: 16,
        leftWidth: 220,
        rightWidth: 260,
        panelBackgroundColor: "#ffffff",
        panelRadius: 8,
        panelShadow: 1,
        titleFontSize: 14,
        titleColor: "#333333",
        mutedColor: "#999999",
        priceColor: "#e1251b",
        bannerRadius: 12,
        cardRadius: 12
    }
};
