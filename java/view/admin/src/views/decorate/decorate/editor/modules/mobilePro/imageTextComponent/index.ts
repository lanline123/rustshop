import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle, defaultContentStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { ImageTextComponentModule } from "./types";

const picUrl = {
    picUrl: "https://oss.tigshop.com/img/gallery/202411/1732607856bEOJCFY5XLpF59AK7v.png",
    picThumb: "https://oss.tigshop.com/img/gallery/202411/1732607856bEOJCFY5XLpF59AK7v.png?x-oss-process=image/resize,m_pad,h_200,h_200",
    picId: 1518,
    picName: "FkByzE0nQeaPjfsodjaNYht8a6S"
};

/** 与旧 mobilePro/imageTextComponent/View.vue 的 defaultModule 逐字段一致（契约） */
export const imageTextComponentDefaults = {
    picUrl: picUrl, //海波设置
    title: "关于我们", //标题文字
    description: "我们是一个全类目的商城平台，应有尽有。其中包括休闲零食、日用百货、医疗健康、美容护理等类目，让你不出门就能买到你想要的。", //图片介绍文字
    picBorder: 0, //图片边框
    picBorderColor: "#EEEEEE", //图片边框颜色
    picRadius: 0, //图片圆角
    titTextAlignment: "center", //标题对齐方式
    titMarginBottom: 10, //标题介绍间距
    titColor: "#333333", //标题颜色
    descTextAlignment: "center", //介绍对齐方式
    descColor: "#333333", //介绍颜色

    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

registerModule({
    type: "imageTextComponent",
    label: "图文介绍",
    icon: '<i class="iconfont-admin icon-image_text_component"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 1,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(imageTextComponentDefaults)
});
