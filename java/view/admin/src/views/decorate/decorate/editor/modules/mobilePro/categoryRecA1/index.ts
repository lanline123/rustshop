import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultSubTitle, defaultModuleStyle, defaultContentStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { CategoryRecA1Module } from "./types";

const defaultPicList = [
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779325953rpmWhZWipxQ7nmuAQj.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779325953rpmWhZWipxQ7nmuAQj.png",
        picTitle: "领取优惠",
        picDesc: "优惠券",
        picLink: { link: "", title: "" }
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779325953xOBpJxh9kzrNjJp3rB.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779325953xOBpJxh9kzrNjJp3rB.png",
        picTitle: "爆款水果",
        picDesc: "立即探鲜",
        picLink: { link: "", title: "" }
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779325953xOBpJxh9kzrNjJp3rB.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779325953xOBpJxh9kzrNjJp3rB.png",
        picTitle: "在家火锅",
        picDesc: "温暖冬季",
        picLink: { link: "", title: "" }
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779325953xOBpJxh9kzrNjJp3rB.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779325953xOBpJxh9kzrNjJp3rB.png",
        picTitle: "精致生活",
        picDesc: "居家好货",
        picLink: { link: "", title: "" }
    }
];

/** 与旧 mobilePro/categoryRecA1/View.vue 的 defaultModule 逐字段一致（契约，含对共享常量的展开覆写） */
export const categoryRecA1Defaults = {
    picList: defaultPicList,
    picFillType: "contain",
    picRadius: 8,
    subTitle: {
        ...defaultSubTitle,
        titleFontSize: 14,
        titleFontBold: 700
    },
    subTitleColor: "#999999",
    moduleStyle: {
        ...defaultModuleStyle,
        boxPaddingTop: 0,
        boxPaddingBottom: 0,
        boxPadding: 6
    },
    contentStyle: defaultContentStyle
};

registerModule({
    type: "categoryRecA1",
    label: "分类推荐A1",
    icon: '<i class="iconfont-admin icon-category_rec"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 7,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(categoryRecA1Defaults)
});
