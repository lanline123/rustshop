import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle, defaultContentStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { ShopTipsModule } from "./types";

// 演示提示项是 defaultModule 字面量的一部分（旧 View.vue 的 defaultPicList 常量，契约冻结）
const defaultPicList = [
    {
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779263739npDO706Su0cRzKK3ar.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779263739npDO706Su0cRzKK3ar.png",
        picTitle: "最快30分钟送达",
        picLink: {}
    },
    {
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779263739N0N6CeZ40H7KJY9vRb.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779263739N0N6CeZ40H7KJY9vRb.png",
        picTitle: "满69元包邮",
        picLink: {}
    },
    {
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779263739qL8q3AHR1CcdoNv4sD.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779263739qL8q3AHR1CcdoNv4sD.pngg",
        picTitle: "品质保障",
        picLink: {}
    }
];

/** 与旧 mobilePro/shopTips/View.vue 的 defaultModule 逐字段一致（契约） */
export const shopTipsDefaults = {
    picList: defaultPicList,
    rowNum: 3,
    itemGap: 0,
    showDivider: 0,
    titleColor: "#00C455",
    itemBackgroundColor: "",
    moduleStyle: {
        ...defaultModuleStyle,
        boxPaddingTop: 5,
        boxPaddingBottom: 5,
        boxPadding: 10
    },
    contentStyle: defaultContentStyle
};

registerModule({
    type: "shopTips",
    label: "商品提示",
    icon: '<i class="iconfont-admin icon-notice"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 24,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(shopTipsDefaults)
});
