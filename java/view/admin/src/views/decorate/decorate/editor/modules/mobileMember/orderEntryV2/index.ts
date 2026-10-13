import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { isOverseas } from "@/utils/version";
import type { ModulePicListType } from "@/types/decorate/decorate";

export type { OrderEntryV2Module } from "./types";

const defaultCardList: ModulePicListType[] = [
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688569TRT9GOOm6YGq6RHtPG.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688569TRT9GOOm6YGq6RHtPG.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "待付款"
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688569I3uI9owxG28WqM4kZq.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688569I3uI9owxG28WqM4kZq.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "进行中"
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688569bsPmvK2X8p96DhrC8f.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688569bsPmvK2X8p96DhrC8f.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "已完成"
    }
];
const defaultListItems: ModulePicListType[] = [
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688569mV8f2OAjjvfB6GURqx.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688569mV8f2OAjjvfB6GURqx.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "评价"
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688569FJBk502CI7W1gOfH0l.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688569FJBk502CI7W1gOfH0l.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "售后"
    }
];

const defaultInnerPadding = 16;
const defaultCardBgColor = "#f6f7fb";
const defaultBadgeBgColor = "#ff4d4f";
const defaultBadgeTextColor = "#ffffff";

/** 与旧 mobileMember/orderEntryV2/View.vue 的 defaultModule 逐字段一致（契约） */
export const orderEntryV2Defaults = {
    title: "订单",
    cardList: defaultCardList.map((item) => ({ ...item })),
    listItems: defaultListItems.map((item) => ({ ...item })),
    cardBackgroundColor: defaultCardBgColor,
    badgeBgColor: defaultBadgeBgColor,
    badgeTextColor: defaultBadgeTextColor,
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: defaultInnerPadding,
        boxPadding: 0,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
};

