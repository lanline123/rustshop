<template>
    <div class="module-ad-con module-member-order-entry-v2" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content">
            <div class="order-entry-v2-wrap" :style="wrapStyle">
                <div class="order-entry-v2-container" :style="containerStyle">
                    <div class="order-entry-v2-header">
                        <div class="order-entry-v2-title">{{ module.title || "订单" }}</div>
                        <div class="order-entry-v2-more">
                            <span>查看更多</span>
                            <span class="iconfont-h5 icon-a-gengduo20"></span>
                        </div>
                    </div>
                    <div class="order-entry-v2-cards">
                        <div v-for="(item, index) in cardList" :key="index" class="order-card" :style="cardStyle">
                            <div class="order-card-icon">
                                <img v-if="productImage(item)" :src="productImage(item)" :alt="item.picTitle" />
                                <span v-if="demoCardBadges[index]" class="order-card-badge" :style="badgeStyle">{{ demoCardBadges[index] }}</span>
                            </div>
                            <div class="order-card-label">{{ item.picTitle }}</div>
                        </div>
                    </div>
                    <div class="order-entry-v2-list">
                        <div v-for="(item, index) in listItems" :key="index" class="order-entry-v2-list-item">
                            <div class="order-entry-v2-list-left">
                                <img v-if="productImage(item)" class="order-entry-v2-list-icon-img" :src="productImage(item)" :alt="item.picTitle" />
                                <span class="order-entry-v2-list-label">{{ item.picTitle }}</span>
                            </div>
                            <span class="iconfont-h5 icon-a-gengduo20"></span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { productImage } from "@/utils/format";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { orderEntryV2Defaults } from "./index";
import type { OrderEntryV2Module } from "./types";

const props = defineProps<{ module: OrderEntryV2Module }>();
const module = props.module;

const defaultInnerPadding = 16;
const defaultCardBgColor = "#f6f7fb";
const defaultBadgeBgColor = "#ff4d4f";
const defaultBadgeTextColor = "#ffffff";
mergeDefaultModule(module, orderEntryV2Defaults);

const frame = computed(() => module.frame ?? {});
const frameFormat = computed(() => formatFrame(frame.value));
const cardList = computed(() => module.cardList ?? []);
const listItems = computed(() => module.listItems ?? []);

const demoCardBadges = ["183", "5", ""];

const wrapStyle = computed(() => ({
    backgroundColor: frame.value.backgroundColor || "#ffffff",
    borderRadius: `${frame.value.boxRadius ?? 0}px`
}));

const containerStyle = computed(() => ({
    padding: `${frame.value.innerPadding ?? defaultInnerPadding}px`
}));

const cardStyle = computed(() => ({
    backgroundColor: module.cardBackgroundColor || defaultCardBgColor
}));

const badgeStyle = computed(() => ({
    backgroundColor: module.badgeBgColor || defaultBadgeBgColor,
    color: module.badgeTextColor || defaultBadgeTextColor
}));
</script>
<style lang="less" scoped>
.order-entry-v2-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.order-entry-v2-container {
    box-sizing: border-box;
}
.order-entry-v2-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}
.order-entry-v2-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
}
.order-entry-v2-more {
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 13px;
    color: #999;
    .icon-a-gengduo20 {
        font-size: 10px;
    }
}
.order-entry-v2-cards {
    display: flex;
    gap: 10px;
    margin-bottom: 16px;
}
.order-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 25px 12px;
    border-radius: 8px;
}
.order-card-icon {
    position: relative;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 15px;
    img {
        width: 32px;
        height: 32px;
        object-fit: contain;
    }
}
.order-card-badge {
    position: absolute;
    top: -10px;
    right: -12px;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    font-size: 10px;
    font-weight: 500;
    color: #fff;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
}
.order-card-label {
    font-size: 13px;
    color: #1a1a1a;
    text-align: center;
}
.order-entry-v2-list {
    display: flex;
    flex-direction: column;
    .icon-a-gengduo20 {
        font-size: 10px;
    }
}
.order-entry-v2-list-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 0;
    border-top: 1px solid #f0f0f0;
    &:first-child {
        border-top: none;
    }
}
.order-entry-v2-list-left {
    display: flex;
    align-items: center;
    gap: 10px;
}
.order-entry-v2-list-icon-img {
    width: 20px;
    height: 20px;
    object-fit: contain;
}
.order-entry-v2-list-label {
    font-size: 14px;
    color: #1a1a1a;
}
.order-entry-v2-list-arrow {
    font-size: 10px;
    color: #888b94;
}
</style>
