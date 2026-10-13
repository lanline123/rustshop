import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { AdImageModule } from "./types";

const defaultPic = {
    picUrl: "https://oss.tigshop.com/img/gallery/202605/1779689214Nbzv6Iw3ldEVFucDjc.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
    picThumb: "https://oss.tigshop.com/img/gallery/202605/1779689214Nbzv6Iw3ldEVFucDjc.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200"
};
const defaultInnerPadding = 0;

/** 与旧 mobileMember/adImage/View.vue 的 defaultModule 逐字段一致（契约） */
export const adImageDefaults = {
    pic: { ...defaultPic },
    picLink: { link: "", name: "" },
    frame: {
        ...defaultFrame,
        backgroundColor: "",
        boxRadius: 12,
        innerPadding: defaultInnerPadding,
        boxPadding: 5,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
};

registerModule({
    type: "adImage",
    label: "广告图",
    icon: '<i class="iconfont-admin icon-image_nav"></i>',
    url: "mobileMember",
    platform: "mobile",
    order: 8,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(adImageDefaults)
});
