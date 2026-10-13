import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { isOverseas } from "@/utils/version";
import type { ModulePicListType } from "@/types/decorate/decorate";

export type { AccountModule } from "./types";

const defaultAccountList: ModulePicListType[] = [
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779691120KR8ldT5BHs0ZKZ4Uxk.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779691120KR8ldT5BHs0ZKZ4Uxk.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "账号详情",
        picDesc: ""
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779691120QiTfocEWPiXAr7ILcG.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779691120QiTfocEWPiXAr7ILcG.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "地址",
        picDesc: ""
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779691120penAIAi6kRrFMWeeoe.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779691120penAIAi6kRrFMWeeoe.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "设置",
        picDesc: ""
    }
];

const defaultInnerPadding = 15;

/** 与旧 mobileMember/account/View.vue 的 defaultModule 逐字段一致（契约） */
export const accountDefaults = {
    moduleTitle: "账号",
    accountList: defaultAccountList.map((item) => ({ ...item })),
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 0,
        innerPadding: defaultInnerPadding,
        boxPadding: 0,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
};

registerModule({
    type: "account",
    label: "账号",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileMember",
    platform: "mobile",
    order: 10,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(accountDefaults)
});
