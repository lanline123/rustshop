<template>
    <!-- 海外版：底部「所有筛选条件」弹层（价格滑块 / 品牌 / 属性 / 新品） -->
    <tig-popup v-model:show="showPopup" :z-index="900" :show-close="false">
        <view class="filter-box">
            <view class="popup-title-box">
                <view class="title">{{ $t("所有筛选条件") }}</view>
                <view class="close-icon" @click="showPopup = false">
                    <image :src="staticResource('order/close.png')" />
                </view>
            </view>

            <scroll-view class="filter-content" scroll-y>
                <template v-if="maxPrice && maxPrice > 0">
                    <Slider ref="sliderRef" :current-max="sliderCurrentMax" :max-price="maxPrice" @slider-many="onSliderMany" />
                </template>
                <view class="filter">
                    <view class="tab_box">
                        <view class="title-box flex justify-between align-center" @click="brandShow = !brandShow">
                            <view class="txt">{{ $t("品牌") }}</view>
                            <view class="more" :class="{ avtive: brandShow }">
                                <text class="iconfont-h5 icon-a-gengduo20" />
                            </view>
                        </view>
                        <view v-show="brandShow" class="tabs tabs--brand flex-wrap">
                            <view
                                v-for="(item, index) in brandList"
                                :key="item.brandId || index"
                                class="item"
                                :class="{ active: brandIds.includes(item.brandId) }"
                                @click="emit('brandChange', item)"
                            >
                                <text class="line1">{{ item.brandName }}</text>
                            </view>
                        </view>
                    </view>
                    <template v-if="attrsList.length > 0">
                        <view v-for="attr in attrsList" :key="attr.name" class="tab_box">
                            <view class="title-box flex justify-between align-center" @click="toggleAttrShow(attr.name)">
                                <view class="txt">{{ $t(attr.name) }}</view>
                                <view class="more" :class="{ avtive: isAttrExpanded(attr.name) }">
                                    <text class="iconfont-h5 icon-a-gengduo20" />
                                </view>
                            </view>
                            <view v-show="isAttrExpanded(attr.name)" class="tabs flex-wrap">
                                <view
                                    v-for="(value, cIndex) in attr.values"
                                    :key="`${attr.name}-${cIndex}`"
                                    class="item"
                                    :class="{ active: isAttrSelected(attr.name, getAttrOptionText(value)) }"
                                    @click="emit('attrClick', attr.name, getAttrOptionText(value))"
                                >
                                    <text class="line1">{{ $t(getAttrOptionText(value)) }}</text>
                                </view>
                            </view>
                        </view>
                    </template>
                    <view class="tab_box">
                        <view class="title-box flex justify-between align-center" @click="otherShow = !otherShow">
                            <view class="txt">{{ $t("其他") }}</view>
                            <view class="more" :class="{ avtive: otherShow }">
                                <text class="iconfont-h5 icon-a-gengduo20" />
                            </view>
                        </view>
                        <view v-show="otherShow" class="tabs flex-wrap">
                            <view class="item" :class="{ active: sort === 'time' }" @click="emit('toggleNew')">
                                <text class="line1">{{ $t("新品") }}</text>
                            </view>
                        </view>
                    </view>
                </view>
            </scroll-view>

            <view class="btn-box" :class="{ 'safe-btn': safeBottom > 0 }">
                <tig-button2 class="btn" style="height: 70rpx" :delay="300" @click="emit('reset')">
                    {{ $t("重置") }}
                </tig-button2>
                <tig-button2 class="btn" style="height: 70rpx" :loading="isLoading" :delay="300" theme @click="showPopup = false">
                    <template v-if="isLoading">
                        <text class="loading-circle" />
                    </template>
                    <template v-else>
                        <text>{{ $t("显示") }} {{ Number(total) || 0 }} {{ $t("个结果") }}</text>
                    </template>
                </tig-button2>
            </view>
        </view>
    </tig-popup>
</template>

<script lang="ts" setup>
/**
 * 搜索结果页 - 海外版「所有筛选条件」底部弹层
 * vortmall 尚未抽此弹层；此处按当前 o2o 即时筛选行为薄封装，展开态留在组件内。
 */
import { ref, computed, type PropType } from "vue";
import { staticResource } from "@/utils";
import { useSafeAreaInsets } from "@/hooks";
import type { Brand, attrsData, attrsListData } from "@/types/search/search";
import { getAttrOptionText, isAttrSelected as checkAttrSelected, useSectionExpand } from "../../composables";
import Slider from "../slider.vue";

const { safeBottom } = useSafeAreaInsets();

const props = defineProps({
    show: {
        type: Boolean,
        default: false
    },
    brandList: {
        type: Array as PropType<Brand[]>,
        default: () => []
    },
    brandIds: {
        type: Array as PropType<number[]>,
        default: () => []
    },
    attrsList: {
        type: Array as PropType<attrsListData[]>,
        default: () => []
    },
    attrs: {
        type: Array as PropType<attrsData[]>,
        default: () => []
    },
    sort: {
        type: String,
        default: ""
    },
    max: {
        type: [Number, String],
        default: ""
    },
    maxPrice: {
        type: Number,
        default: 0
    },
    total: {
        type: [Number, String],
        default: 0
    },
    isLoading: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits<{
    (e: "update:show", value: boolean): void;
    (e: "brandChange", item: Brand): void;
    (e: "attrClick", name: string, value: string): void;
    (e: "toggleNew"): void;
    (e: "reset"): void;
    (e: "sliderMany", payload: { minPrice: number; maxPrice: number }): void;
}>();

const sliderRef = ref<{ init?: () => void } | null>(null);
const brandShow = ref(true);
const otherShow = ref(true);
const { isExpanded: isAttrExpanded, toggleExpand: toggleAttrShow } = useSectionExpand(true);

const showPopup = computed({
    get: () => props.show,
    set: (val) => emit("update:show", val)
});

const sliderCurrentMax = computed((): number | undefined => {
    const m = props.max;
    if (m === undefined || m === null || m === "") {
        return undefined;
    }
    return typeof m === "string" ? Number(m) : Number(m);
});

const isAttrSelected = (name: string, value: string) => checkAttrSelected(props.attrs, name, value);

const onSliderMany = (e: { minPrice: number; maxPrice: number }) => {
    emit("sliderMany", e);
};

defineExpose({
    initSlider: () => sliderRef.value?.init?.()
});
</script>

<style lang="scss" scoped>
.filter-box {
    height: 70vh;
    border-top-left-radius: 28rpx;
    border-top-right-radius: 28rpx;
    overflow: hidden;
    background-color: #fff;
    position: relative;
    display: flex;
    flex-direction: column;

    .popup-title-box {
        flex-shrink: 0;
        display: flex;
        justify-content: center;
        padding: 35rpx 28rpx 28rpx;
        background-color: #fff;

        .title {
            flex: 1;
            display: flex;
            justify-content: center;
            font-size: 32rpx;
            line-height: 42rpx;
            font-weight: 700;
        }

        .close-icon {
            display: flex;
            align-items: center;

            image {
                width: 35rpx;
                height: 35rpx;
            }
        }
    }

    .filter-content {
        flex: 1;
        height: 0;
        min-height: 0;
    }

    .filter {
        padding: 0 20rpx 24rpx;

        .tab_box {
            margin-top: 30rpx;

            .title-box {
                color: #666;
                padding: 10rpx 0;
                margin-bottom: 30rpx;
            }

            .more {
                flex-shrink: 0;
                width: 32rpx;
                height: 32rpx;
                display: flex;
                align-items: center;
                justify-content: center;

                .icon-a-gengduo20 {
                    font-size: 18rpx;
                    line-height: 1;
                    display: block;
                    transform: rotate(-90deg);
                    transform-origin: center center;
                }

                &.avtive .icon-a-gengduo20 {
                    transform: rotate(90deg);
                }
            }

            .txt {
                flex: 1;
                min-width: 0;
                font-size: 16px;
                font-weight: 700;
                color: #1a1a1a;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                padding-right: 16rpx;
            }

            .tabs {
                gap: 12rpx;

                .item {
                    background: #f5f6fa;
                    padding: 14rpx 21rpx;
                    font-size: 25rpx;
                    border-radius: 7rpx;
                    color: #505259;
                    border: 1rpx solid #f5f6fa;
                    overflow: hidden;
                    max-width: 317rpx;
                    display: flex;
                    align-items: center;
                    outline: none;
                }

                .active {
                    outline: 1.5px solid #1a1a1a;
                    outline-offset: -1px;
                    color: #1a1a1a;
                    background-color: #f7f8fc;
                }
            }
        }
    }

    .btn-box {
        flex-shrink: 0;
        z-index: 100;
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 16rpx;
        padding: 20rpx 32rpx;
        border-top: 1px solid rgba(0, 0, 0, 0.08);
        background-color: #fff;

        &.safe-btn {
            padding-bottom: var(--safe-bottom);
        }

        .btn {
            flex: 1;
        }
    }
}

.loading-circle {
    width: 28rpx;
    height: 28rpx;
    border: 3rpx solid rgba(255, 255, 255, 0.35);
    border-top-color: #fff;
    border-radius: 50%;
    display: inline-block;
    animation: search-loading-spin 0.8s linear infinite;
}

@keyframes search-loading-spin {
    to {
        transform: rotate(360deg);
    }
}
</style>
