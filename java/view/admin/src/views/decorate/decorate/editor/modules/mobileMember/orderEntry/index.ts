import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import type { ModulePicListType } from "@/types/decorate/decorate";

export type { OrderEntryModule } from "./types";

const style1EntryList: ModulePicListType[] = [
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688296LEk49r7zpXWPuZYHE1.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688296LEk49r7zpXWPuZYHE1.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "待付款"
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688296MMBp6rwui5XUCK23J1.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688296MMBp6rwui5XUCK23J1.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "待发货"
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688296yHIquz41WhOu77OOOM.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688296yHIquz41WhOu77OOOM.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "待收货"
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688297NOaA9Zx6mSws3oqgh6.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688297NOaA9Zx6mSws3oqgh6.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "待评价"
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688297PzS7ITTZI1NC5BXiG4.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688297PzS7ITTZI1NC5BXiG4.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "退换/售后"
    }
];

const defaultInnerPadding = 16;

/** 与旧 mobileMember/orderEntry Edit 默认值对齐（含角标色） */
export const orderEntryDefaults = {
    title: "我的订单",
    layoutStyle: 1,
    entryList: style1EntryList.map((item) => ({ ...item })),
    badgeBgColor: "#4d9be4",
    badgeTextColor: "#ffffff",
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: defaultInnerPadding,
        boxPadding: 10,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
};

registerModule({
    type: "orderEntry",
    label: "我的订单",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileMember",
    platform: "mobile",
    order: 4,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(orderEntryDefaults)
});
