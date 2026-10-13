<template>
    <div class="module-ad-con module-member-order-entry" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content">
            <div class="order-entry-wrap" :style="wrapStyle">
                <div class="order-entry-container" :style="containerStyle">
                    <!-- 样式一：带标题头（JPEG 图标 + 蓝角标，默认） -->
                    <div v-if="module.layoutStyle === 1" class="order-entry-header">
                        <div class="order-entry-title">{{ module.title || "我的订单" }}</div>
                        <div class="order-entry-more">
                            <span>全部订单</span>
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
import { computed } from "vue";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { orderEntryDefaults } from "./index";
import type { OrderEntryModule } from "./types";

const props = defineProps<{ module: OrderEntryModule }>();
const module = props.module;

const defaultInnerPadding = 16;
mergeDefaultModule(module, orderEntryDefaults);

const frame = computed(() => module.frame ?? {});
const frameFormat = computed(() => formatFrame(frame.value));
const entryList = computed(() => module.entryList ?? []);

const demoBadges = ["99+", "88", "32", "18", ""];

const getDefaultBadgeBgColor = (layoutStyle?: number) => (layoutStyle === 1 ? "#4d9be4" : "#ff4d4f");
const badgeStyle = computed(() => ({
    backgroundColor: module.badgeBgColor || getDefaultBadgeBgColor(module.layoutStyle),
    color: module.badgeTextColor || "#ffffff"
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
