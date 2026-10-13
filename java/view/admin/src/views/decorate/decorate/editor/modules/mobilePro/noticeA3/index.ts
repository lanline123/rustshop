import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle, defaultContentStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { NoticeA3Module } from "./types";

// 演示公告条目是 defaultModule 字面量的一部分（旧 View.vue 的 defaultNoticeList 常量，契约冻结）
const defaultNoticeList = [
    { title: "感恩大回馈，优惠大酬宾，先到先得,更多优惠等你来", prefix: "新品", link: {} },
    { title: "感恩大回馈，优惠大酬宾，先到先得,更多优惠等你来", prefix: "新品", link: {} }
];

/** 与旧 mobilePro/noticeA3/View.vue 的 defaultModule 逐字段一致（契约） */
export const noticeA3Defaults = {
    noticeList: defaultNoticeList,
    showHeader: 1,
    headerImageWidth: 48,
    textColor: "#666666",
    headerImage: {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779266900kyNhWWRQpNHEigvIfz.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779266900kyNhWWRQpNHEigvIfz.png?x-oss-process=image/resize,m_pad,h_200,h_200"
    },
    borderColor: "",
    contentBgColor: "",
    showPrefix: 1,
    showBorder: 0,
    moduleStyle: {
        ...defaultModuleStyle,
        boxPaddingTop: 5,
        boxPaddingBottom: 5,
        boxPadding: 10
    },
    contentStyle: defaultContentStyle
};

registerModule({
    type: "noticeA3",
    label: "商城公告",
    icon: '<i class="iconfont-admin icon-notice"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 25,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(noticeA3Defaults)
});
