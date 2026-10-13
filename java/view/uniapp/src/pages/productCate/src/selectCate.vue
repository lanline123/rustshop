<template>
    <view class="cate-box" :class="{ main: type === 'main' }">
        <scroll-view :scroll-x="true" class="cate-scroll" :scroll-with-animation="true" :scroll-left="scrollLeft" @scroll="handleScroll">
            <template v-for="item in menuList" :key="item.categoryId">
                <view v-if="item.categoryName" class="cate-item" @click="handleClick(item.categoryId)">
                    <view class="item-text" :class="getActiveClass(item.categoryId)">
                        {{ item.categoryName }}
                    </view>
                </view>
            </template>
        </scroll-view>
        <view class="cate-btn" @click="handleShowPopup">
            <text class="iconfont-h5 icon-arrowdown cate-btn-arrow" />
        </view>
        <tig-popup v-model:show="show" position="top" :z-index="999" :show-close="false" background-color="#fff">
            <view class="cate-popup">
                <view class="cate-popup-title">{{ catePopupTitle }}</view>
                <view class="cate-popup-list-box">
                    <view class="cate-popup-list">
                        <template v-for="item in menuList" :key="item.categoryId">
                            <view class="list-item popup-item" @click="handleClick(item.categoryId)">
                                <view class="item-text popup-text line1" :class="getActiveClass(item.categoryId)">
                                    {{ item.categoryName }}
                                </view>
                            </view>
                        </template>
                    </view>
                </view>
                <view class="cate-popup-close" @click="show = false">{{ $t("点击收起") }}<text class="iconfont-h5 icon-xiajiantou1-copy" /></view>
            </view>
        </tig-popup>
    </view>
</template>

<script setup lang="ts">
/**
 * 分类选择弹窗组件
 * - 弹窗展示分类树
 * - 支持多级分类选择
 */
import { computed, ref, watch, type PropType } from "vue";
import { getCategoryList } from "@/api/productCate/productCate";
import type { filterSeleted } from "@/types/productCate/productCate";
import type { CateCenterV2Data } from "@/components/modules/cate-center/cate-center-v2/types";
import { useI18n } from "vue-i18n";
const { t } = useI18n();

const props = defineProps({
    currentCateId: {
        type: Number
    },
    type: {
        type: String,
        default: ""
    },
    /** 装修配置（用于获取 subTabStyle 等配置） */
    module: {
        type: Object as PropType<CateCenterV2Data>,
        default: () => ({})
    }
});
const emit = defineEmits(["change"]);
const subCurrentCateId = defineModel("subCurrentCateId", {
    default: 0
});
const scrollLeft = ref(0);
const menuList = defineModel<filterSeleted[]>("menuList", {
    default: []
});
const getMenuList = async (id: number) => {
    try {
        const result = await getCategoryList({ id });
        menuList.value = [{ categoryId: 0, categoryName: t("全部商品") }, ...result];
        emit("change");
        scrollLeft.value = 0;
    } catch (error) {
        console.error(error);
    }
};

const handleClick = (id: number) => {
    if (id === subCurrentCateId.value) return;
    subCurrentCateId.value = id;
    emit("change");
};

const handleScroll = (e: any) => {
    scrollLeft.value = e.detail.scrollLeft;
};

/**
 * 二级分类标签样式：1=实心高亮，2=描边高亮
 */
const subTabStyle = computed(() => props.module?.subTabStyle ?? 1);

/**
 * 拼出激活态 class，按 subTabStyle 区分 solid / outline
 */
const getActiveClass = (categoryId: number) => {
    const isActive = categoryId === subCurrentCateId.value;
    const isMain = props.type === "main";

    if (!isActive) {
        return isMain ? "main" : "";
    }

    if (isMain) {
        return subTabStyle.value === 1 ? "active main solid" : "active main outline";
    }

    return subTabStyle.value === 1 ? "active solid" : "active outline";
};

const handleShowPopup = () => {
    if (menuList.value.length) {
        show.value = true;
    }
};

const catePopupTitle = computed(() => {
    return menuList.value.find((item) => item.categoryId === subCurrentCateId.value)?.categoryName;
});

watch(
    () => props.currentCateId,
    async (newVal) => {
        if (newVal && newVal > 0) {
            await getMenuList(newVal);
        }
    }
);

const show = ref(false);
</script>

<style lang="scss" scoped>
.cate-box {
    width: 100%;
    height: 100rpx;
    position: relative;
    display: flex;
    align-items: center;

    .cate-btn {
        position: absolute;
        right: 0;
        top: 0;
        bottom: 0;
        width: 80rpx;
        background-color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1;

        .cate-btn-arrow {
            font-size: 24rpx;
            color: #999;
            line-height: 1;
            display: block;
        }
    }

    .cate-scroll {
        width: 100%;
        white-space: nowrap;
        padding: 20rpx 0;
        box-sizing: border-box;
    }

    .cate-item {
        display: inline-block;
        min-width: 150rpx;
        padding: 5rpx 0;
        margin-right: 15rpx;
        vertical-align: middle;

        &:first-child {
            margin-left: 20rpx;
        }

        &:last-child {
            margin-right: 100rpx;
        }
    }
    .popup-item {
        max-width: 150rpx;
    }
}

/* --cate-* 由分类页装修下发（见 buildCateCenterV2Vars），未配置时 fallback 即原有视觉 */
.item-text {
    padding: 10rpx 10rpx;
    background-color: var(--cate-subtab-bg, #f6f6f6);
    border-radius: 50rpx;
    font-size: 26rpx;
    color: var(--cate-subtab-text, #999);
    text-align: center;
    border: 1px solid transparent;
    box-sizing: border-box;

    &.popup-text {
        margin-right: 0;
        width: 100%;
        padding: 10rpx;
    }

    &.active.main {
        color: var(--cate-subtab-active-text, var(--main-text));
        background-color: var(--cate-subtab-active-bg, var(--main-bg));
        border-color: transparent;
    }

    // 装修 subTabStyle=1：实心高亮
    &.active.solid {
        background-color: var(--cate-subtab-active-bg, var(--cate-theme, var(--general)));
        color: var(--cate-subtab-active-text, #fff);
        border-color: transparent;
    }

    &.active.main.solid {
        color: var(--cate-subtab-active-text, var(--main-text));
        background-color: var(--cate-subtab-active-bg, var(--main-bg));
        border-color: transparent;
    }

    // 装修 subTabStyle=2：描边高亮（对齐 admin：白底 + 主题色描边/文字）
    // 描边态没有实心底，subTabActiveBgColor 复用为边框色，未单独配文字色时文字跟它走
    &.active.outline {
        background-color: #fff;
        color: var(--cate-subtab-active-text, var(--cate-subtab-active-bg, var(--cate-theme, var(--general))));
        border-color: var(--cate-subtab-active-bg, var(--cate-theme, var(--general)));
    }

    &.active.main.outline {
        background-color: #fff;
        color: var(--cate-subtab-active-text, var(--cate-subtab-active-bg, var(--cate-theme, var(--general))));
        border-color: var(--cate-subtab-active-bg, var(--cate-theme, var(--general)));
    }
}
.cate-popup {
    max-height: 50vh;
    padding: 0 30rpx;
    padding-top: var(--status-bar-height);
    position: relative;

    .cate-popup-title {
        font-size: 32rpx;
        height: 100rpx;
        display: flex;
        align-items: center;
    }

    .cate-popup-list-box {
        height: calc(100% - 100rpx);
        padding-bottom: 70rpx;
        overflow: hidden;
        overflow-y: auto;
        .cate-popup-list {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            grid-gap: 20rpx;
            .list-item {
                height: 60rpx;
            }
        }
    }

    .cate-popup-close {
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        font-size: 24rpx;
        color: #999;
        display: flex;
        align-items: center;
        padding: 20rpx;

        .icon-xiajiantou1-copy {
            font-size: 28rpx;
            transform: rotate(180deg);
        }
    }
}
</style>
