<template>
    <view class="header-domestic">
        <view class="header acea-row row-center-wrapper" :class="{ 'header--with-back': showBack }">
            <view v-if="showBack" class="product-nav-left product-nav-icon" @click="emit('back')">
                <text class="iconfont-h5 icon-fanhui" />
            </view>
            <view class="acea-row row-between-wrapper input input-wrap">
                <scroll-view
                    class="input-combined-scroll"
                    scroll-x
                    enable-flex
                    :scroll-left="combinedScrollLeft"
                    show-scrollbar="false"
                    @click.stop="emit('toSearch')"
                >
                    <view class="input-combined-inner">
                        <TagRowDomestic
                            :filter-params="filterParams"
                            :brand-name="brandName"
                            :coupon-name="couponName"
                            :tree-list="treeList"
                            :intro-list="introList"
                            embedded
                            in-row
                            @delete="emit('delete', $event)"
                            @delete-attr="emit('deleteAttr', $event)"
                            @delete-category="emit('deleteCategory', $event)"
                        />
                        <view v-if="!keyword && !hasVisibleTags" class="txt">{{ $t("请输入搜索内容") }}</view>
                        <view v-else-if="keyword" class="keyword-inline">
                            <text class="keyword-text">{{ keyword }}</text>
                        </view>
                    </view>
                </scroll-view>
                <view class="search-btn" @click="emit('toSearch')">{{ $t("搜索") }}</view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { ref, computed, watch, nextTick, type PropType } from "vue";
import type { attrsData, filterSeleted } from "@/types/search/search";
import TagRowDomestic from "./tag-row-domestic.vue";

interface HeaderFilterParams {
    intro?: string;
    attrs?: attrsData[];
    max?: string | number | null;
    min?: string | number | null;
}

const props = defineProps({
    keyword: {
        type: String,
        default: ""
    },
    filterParams: {
        type: Object as PropType<HeaderFilterParams>,
        required: true
    },
    brandName: {
        type: String,
        default: ""
    },
    couponName: {
        type: String,
        default: ""
    },
    treeList: {
        type: Array as PropType<filterSeleted[]>,
        default: () => []
    },
    introList: {
        type: Object as PropType<Record<string, string>>,
        default: () => ({})
    },
    /** 跨境头不带返回时，在搜索框左侧展示返回 */
    showBack: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits<{
    (e: "toSearch"): void;
    (e: "back"): void;
    (e: "delete", type: string): void;
    (e: "deleteAttr", index: number): void;
    (e: "deleteCategory", index: number): void;
}>();

const hasVisibleTags = computed(() => {
    const fp = props.filterParams;
    if (fp.intro && props.introList[fp.intro]) return true;
    if (props.couponName) return true;
    if (props.brandName) return true;
    if (fp.attrs?.length) return true;
    if (fp.max || fp.min) return true;
    if (props.treeList?.length) return true;
    return false;
});

const combinedScrollLeft = ref(0);

const scrollCombinedToEnd = () => {
    combinedScrollLeft.value = 0;
    nextTick(() => {
        combinedScrollLeft.value = 99999;
    });
};

watch(
    () => props.keyword,
    () => {
        scrollCombinedToEnd();
    }
);

watch(
    () => [
        props.filterParams.intro,
        props.filterParams.attrs,
        props.filterParams.min,
        props.filterParams.max,
        props.brandName,
        props.couponName,
        props.treeList
    ],
    () => {
        scrollCombinedToEnd();
    },
    { deep: true }
);
</script>

<style lang="scss" scoped>
.header-domestic {
    width: 100%;
    background-color: var(--search-item-bg, #fff);

    .header {
        width: 100%;
        min-height: 99rpx;
        background-color: var(--search-item-bg, #fff);
        border-bottom: 1rpx solid #f5f5f5;
        display: flex;
        align-items: center;
        padding: 0 var(--search-item-padding-lr, 25rpx);
        box-sizing: border-box;
    }

    .header--with-back {
        gap: 12rpx;
        padding: 0 var(--search-item-padding-lr, 25rpx) 0 var(--search-item-padding-lr, 12rpx);
    }

    .product-nav-icon {
        flex-shrink: 0;
        width: 50rpx;
        height: 50rpx;
        display: flex;
        align-items: center;
        justify-content: center;

        text {
            font-size: 28rpx;
            color: #1a1a1a;
        }
    }

    .input.input-wrap {
        display: flex;
        flex-direction: row;
        align-items: center;
        flex: 1;
        width: 0;
        height: 60rpx;
        background-color: #f5f5f5;
        border-radius: 32rpx;
        box-sizing: border-box;
        padding: 0 15rpx 0 10rpx;
    }

    .input-combined-scroll {
        flex: 1;
        width: 0;
        height: 56rpx;
    }

    .input-combined-inner {
        display: inline-flex;
        flex-direction: row;
        flex-wrap: nowrap;
        align-items: center;
        width: max-content;
        min-width: 100%;
        height: 56rpx;
        padding-right: 8rpx;
        box-sizing: border-box;
    }

    .txt {
        flex-shrink: 0;
        height: 56rpx;
        color: #999;
        white-space: nowrap;
        font-size: 26rpx;
        line-height: 56rpx;
    }

    .keyword-inline {
        flex-shrink: 0;
        display: inline-flex;
        align-items: center;
        height: 56rpx;
        padding-left: 4rpx;
    }

    .keyword-text {
        white-space: nowrap;
        color: #1a1a1a;
        font-size: 26rpx;
        line-height: 56rpx;
    }

    .search-btn {
        padding: 0 20rpx;
        min-height: 44rpx;
        line-height: 44rpx;
        font-size: 24rpx;
        color: #666;
        white-space: nowrap;
        align-self: center;
    }
}
</style>
