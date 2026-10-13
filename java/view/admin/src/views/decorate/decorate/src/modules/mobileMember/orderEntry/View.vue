<template>
    <div class="module-ad-con module-member-order-entry" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content">
            <div class="order-entry-wrap" :style="wrapStyle">
                <div class="order-entry-container" :style="containerStyle">
                    <!-- 样式一：带标题头（JPEG 图标 + 蓝角标，默认） -->
                    <div v-if="module.layoutStyle === 1" class="order-entry-header">
                        <div class="order-entry-title">{{ module.title || $t('我的订单') }}</div>
                        <div class="order-entry-more">
                            <span>{{ $t('全部订单') }}</span>
                            <span class="iconfont-h5 icon-a-gengduo20"></span>
                        </div>
                    </div>
                    <!-- 图标列表（两种样式共用） -->
                    <div class="order-entry-list" :class="{ 'layout-style1': module.layoutStyle === 1 }">
                        <template v-for="(item, index) in entryList" :key="index">
                            <div v-if="module.layoutStyle === 2 && index === entryList.length - 1" class="order-entry-divider"></div>
                            <div class="order-entry-item">
                                <div class="order-entry-icon-wrapper">
                                    <img v-if="item.picUrl" class="order-entry-icon-img" :src="item.picUrl" :alt="item.picTitle" />
                                    <div v-else class="order-entry-icon-empty"></div>
                                    <span class="order-entry-badge" :style="badgeStyle">{{ demoBadges[index] }}</span>
                                </div>
                                <div class="order-entry-label">{{ item.picTitle }}</div>
                            </div>
                        </template>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, Ref } from "vue";
import { ModuleType, ModuleFrameType, ModulePicListType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
type ModuleOrderEntryType = Omit<ModuleType, "frame"> & {
    title?: string;
    layoutStyle?: number;
    entryList?: ModulePicListType[];
    badgeBgColor?: string;
    badgeTextColor?: string;
    frame: ModuleFrameType;
};

const module = defineModel<ModuleOrderEntryType>("module") as Ref<ModuleOrderEntryType>;

const style1EntryList: ModulePicListType[] = [
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688296LEk49r7zpXWPuZYHE1.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688296LEk49r7zpXWPuZYHE1.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('待付款')
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688296MMBp6rwui5XUCK23J1.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688296MMBp6rwui5XUCK23J1.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('待发货')
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688296yHIquz41WhOu77OOOM.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688296yHIquz41WhOu77OOOM.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('待收货')
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688297NOaA9Zx6mSws3oqgh6.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688297NOaA9Zx6mSws3oqgh6.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('待评价')
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779688297PzS7ITTZI1NC5BXiG4.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779688297PzS7ITTZI1NC5BXiG4.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('退换/售后')
    }
];

const style2EntryList: ModulePicListType[] = [
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779948168bVpgRKHWQeyik4YB5i.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779948168bVpgRKHWQeyik4YB5i.png",
        picTitle: t('待付款')
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779948167OzuyBnYUTGf6sMQcS4.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779948167OzuyBnYUTGf6sMQcS4.png",
        picTitle: t('待发货')
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779948168Ool4xEFRoCZqQ5eTDq.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779948168Ool4xEFRoCZqQ5eTDq.png",
        picTitle: t('待收货')
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779948168iIVh0NpHDDSf41hFPN.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779948168iIVh0NpHDDSf41hFPN.png",
        picTitle: t('退换/售后')
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779948169KmcNUMPH14twh8qBdD.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779948169KmcNUMPH14twh8qBdD.png",
        picTitle: t('我的订单')
    }
];

const defaultInnerPadding = 16;
const defaultModule = {
    title: t('我的订单'),
    layoutStyle: 1,
    entryList: style1EntryList.map((item) => ({ ...item })),
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
mergeDefaultModule(module.value, defaultModule);

const frame = computed(() => module.value.frame ?? {});
const frameFormat = computed(() => formatFrame(frame.value));
const entryList = computed(() => module.value.entryList ?? []);

const demoBadges = ["99+", "88", "32", "18", ""];

const getDefaultBadgeBgColor = (layoutStyle?: number) => (layoutStyle === 1 ? "#4d9be4" : "#ff4d4f");
const badgeStyle = computed(() => ({
    backgroundColor: module.value.badgeBgColor || getDefaultBadgeBgColor(module.value.layoutStyle),
    color: module.value.badgeTextColor || "#ffffff"
}));

const wrapStyle = computed(() => ({
    backgroundColor: frame.value.backgroundColor || "#ffffff",
    borderRadius: `${frame.value.boxRadius ?? 0}px`
}));

const containerStyle = computed(() => ({
    padding: `${frame.value.innerPadding ?? defaultInnerPadding}px`
}));
</script>
<style lang="less" scoped>
.order-entry-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.order-entry-container {
    box-sizing: border-box;
}
.order-entry-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
.order-entry-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
}
.order-entry-more {
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 11px;
    color: #999;
    .icon-a-gengduo20 {
        font-size: 10px;
    }
}
.order-entry-list {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.order-entry-divider {
    width: 10px;
    height: 54px;
    background-image: url("https://oss.tigshop.com/official/img/pc/202605/1779929210zBMvyOonKlS2ANuWkq.png");
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    transform: scaleX(-1);
    flex-shrink: 0;
    align-self: center;
    margin: 0 7px 0 12px;
}
.order-entry-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
}
.order-entry-icon-wrapper {
    position: relative;
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
}
.order-entry-icon-img {
    width: 28px;
    height: 28px;
    object-fit: contain;
    margin-bottom: 2px;
}
.layout-style1 .order-entry-icon-img {
    width: 24px;
    height: 24px;
    margin-bottom: 4px;
}
.order-entry-icon-empty {
    width: 28px;
    height: 28px;
    background: #f5f5f5;
    border-radius: 8px;
}
.order-entry-badge {
    position: absolute;
    top: 0;
    right: 0;
    transform: translate(90%, -50%);
    min-width: 16px;
    height: 16px;
    padding: 0 3px;
    font-size: 10px;
    font-weight: 500;
    color: #fff;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    &:empty {
        display: none;
    }
}
.order-entry-label {
    font-size: 12px;
    line-height: 20px;
    color: #666;
    text-align: center;
    white-space: nowrap;
}
</style>
