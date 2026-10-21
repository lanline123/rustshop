import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { ComboSuggestionsModule } from "./types";

const comboGroups = [
    {
        titleImage: {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731485744JPyV2kPmhJjwi9b1OD.png",
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485744JPyV2kPmhJjwi9b1OD.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picId: 1429,
            picName: "FpMGh7lhbRC26udZs5OVGsD1MQkm"
        },
        groupTitle: "限时特惠",
        groupSubTitle: "心动福利",
        groupDesc: "限时！西冷牛排降20.1元",
        groupColor: "rgba(218, 79, 51, 1)",
        groupType: "image",
        picList: [
            {
                picId: 1428,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485744Z36HLJIkKupT78pAPB.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202411/1731485744Z36HLJIkKupT78pAPB.jpg",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: "禽蛋抢先购"
            },
            {
                picId: 1427,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485744G6SecTiZ2YnnqTOrzO.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202411/1731485744G6SecTiZ2YnnqTOrzO.jpg",
                picName: "Fk9ihEJjXDwwG174KZcjuaQeyGus",
                picTitle: "清洁好帮手"
            },
            {
                picId: 1426,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485744OYBNIbPBqdFUwc8YMO.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202411/1731485744OYBNIbPBqdFUwc8YMO.jpg",
                picName: "FlX7c5fV9sbgl9QZ0i0z1K1DK6DB",
                picTitle: "精选好食材"
            }
        ],
        pruductList: [],
        moreLink: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        }
    },
    {
        titleImage: {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/17314857564qYuFM87aXbimtojrK.png",
            picThumb: "https://oss.tigshop.com/img/gallery/202411/17314857564qYuFM87aXbimtojrK.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picId: 1434,
            picName: "FqaBzM4nqSLzx4ydDgi3SfbS5R3g"
        },
        groupTitle: "生鲜惠选",
        groupSubTitle: "榜单推荐",
        groupDesc: "猫山王榴莲冰皮月饼香甜爆浆",
        groupColor: "rgba(177, 123, 57, 1)",
        groupType: "product",
        picList: [],
        pruductList: [
            {
                productId: 0,
                productSn: "",
                productName: "刺身拼盘",
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485756wtyOOnqoeEn6W2XfNP.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "冰鲜剥皮鱼",
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485756sGPXRzI7o84S5HAPo5.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "北极甜虾",
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485756JsgKabEE4BO09iRqnc.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "秋刀鱼",
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485756ivBG9CydBhi21do1xk.jpg"
            }
        ],
        moreLink: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        }
    },
    {
        titleImage: {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731485768izrm5sZYVmQOne5yYf.png",
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485768izrm5sZYVmQOne5yYf.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picId: 1439,
            picName: "FrEc7JTpoNjpEJR8HlE4Klv6trKM"
        },
        groupTitle: "全球好物",
        groupSubTitle: "大牌严选",
        groupDesc: "七夕礼~欧舒丹明细沐浴礼盒",
        groupColor: "rgba(36, 78, 179, 1)",
        groupType: "product",
        picList: [],
        pruductList: [
            {
                productId: 0,
                productSn: "",
                productName: "牙膏",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485768ECo8ixzZvZVOLDsJf3.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "卫生巾",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485768PFd7qRdzskII6k0ZTU.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "卫生巾",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485768yn1Oq5lqvNyx7N8gbE.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "沐浴露",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485768bH293zqhEdmoNbO7VQ.jpg"
            }
        ],
        moreLink: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        }
    },
    {
        titleImage: {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731485780Z15LILaEOLfudumi3H.png",
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485780Z15LILaEOLfudumi3H.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picId: 1441,
            picName: "FlF_0eUIigJ19i2n6VOTIhZuMn0G"
        },
        groupTitle: "逛出新奇",
        groupSubTitle: "上新日历",
        groupDesc: "颗颗饱满！甜！巨峰葡萄~",
        groupColor: "rgba(99, 49, 119, 1)",
        groupType: "product",
        picList: [],
        pruductList: [
            {
                productId: 0,
                productSn: "",
                productName: "摆件",
                productDesc: "",
                productPrice: 55.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485780O9GnV2rCLcOodydJAb.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "桌面垃圾桶",
                productDesc: "",
                productPrice: 25.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/17314857806ABCknloual53A5XH0.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "垃圾袋",
                productDesc: "",
                productPrice: 15.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485780y9OjupFgDDLmI31GhJ.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "脸盆",
                productDesc: "",
                productPrice: 25.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485780Okip8X3FFefKxhz8OG.jpg"
            }
        ],
        moreLink: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        }
    }
];

/** 与旧 mobilePro/comboSuggestions/View.vue 的 defaultModule 逐字段一致（契约） */
export const comboSuggestionsDefaults = {
    comboGroups: comboGroups,
    regularPriceColor: "#e84a40", //常规价格颜色
    regularPriceBgColor: "", //常规价格背景颜色（留空时无背景）
    regularPriceRadius: 24, //常规价格圆角半径（胶囊）
    regularPriceSize: 14, //常规价格文字大小
    saleTagBackgroundColorA: "#f8d7a0", //活动标签背景颜色a
    saleTagBackgroundColorB: "#ffffff", //活动标签背景颜色b
    saleTagFontColor: "#83441c", //活动标签文字颜色
    salePriceBackgroundColor: "#e73a68", //活动价格背景颜色
    salePriceFontColor: "#ffffff", //活动价格文字颜色
    salePriceDecimal: 0, //活动价格小数位数 0:不显示  1:显示1位 2:显示2位
    imageRadius: 6, //商品图或图片圆角半径
    soldOutIcon: 1, //缺货图标 1:显示 0:不显示

    backgroundGradientType: "upDown", //背景方向
    gradientColorA: "#ffeadf", //背景渐变色A
    gradientColorB: "#ffffff", //背景渐变色B
    groupTitleColor: "#000000", //分组大标题颜色
    bannerTittleColor: "#ffffff", //banner标题颜色
    bannerDotColor: "#ffffff", //banner指示数颜色
    groupRadius: 10, //内容圆角
    groupShadow: 0, //内容投影 1:显示 0:不显示

    moduleStyle: defaultModuleStyle, //模块样式
    carouselDirection: "horizontal", //轮播方向  横向 horizontal  纵向 vertical
    autoPlay: 1, //自动轮播 1:开启 0:关闭
    interval: 5 //轮播间隔
};

registerModule({
    type: "comboSuggestions",
    label: "组合推荐",
    icon: '<i class="iconfont-admin icon-combo_suggestions"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 13,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(comboSuggestionsDefaults)
});
