<template>
    <!-- 国内版：右侧筛选抽屉（分类 / 品牌 / 属性 / 价格） -->
    <tig-popup v-model:show="showDrawer" position="right" :z-index="900" :show-close="false">
        <view class="search_condition" @touchmove.stop>
            <scroll-view scroll-y="true" class="search_condition-scroll">
                <view class="search_condition-box">
                    <view v-if="(categoryList?.length ?? 0) > 0 || (shopCategoryList?.length ?? 0) > 0" class="tab_box">
                        <view class="title-box flex justify-between">
                            <view class="txt">{{ $t("分类") }}</view>
                            <view class="more" @click="categoryShow = !categoryShow">
                                <text class="more-text">{{ categoryShow ? $t("收起") : $t("展开") }}</text>
                                <text v-if="categoryShow" class="iconfont-h5 icon-up" style="font-size: 16px; color: #bfbfbf" />
                                <text v-else class="iconfont-h5 icon-arrowdown" style="font-size: 16px; color: #bfbfbf" />
                            </view>
                        </view>
                        <template v-if="shopId">
                            <view class="tabs flex-wrap">
                                <view
                                    v-for="(item, index) in displayShopCategoryList"
                                    :key="index"
                                    class="item"
                                    :class="{ active: localShopCategoryId == item.categoryId }"
                                    @click="localShopCategoryId = item.categoryId"
                                >
                                    <text v-if="localShopCategoryId == item.categoryId" class="iconfont-h5 icon-gouxuan" style="font-size: 12px" />
                                    {{ item.categoryName }}
                                </view>
                            </view>
                        </template>
                        <template v-else>
                            <view class="tabs flex-wrap">
                                <view
                                    v-for="(item, index) in displayCategoryList"
                                    :key="index"
                                    class="item"
                                    :class="{ active: localCat == item.categoryId }"
                                    @click="localCat = item.categoryId"
                                >
                                    <text v-if="localCat == item.categoryId" class="iconfont-h5 icon-gouxuan" style="font-size: 12px" />
                                    {{ item.categoryName }}
                                </view>
                            </view>
                        </template>
                    </view>

                    <template v-if="(brandList?.length ?? 0) > 0">
                        <view class="tab_box">
                            <view class="title-box flex justify-between">
                                <view class="txt">{{ $t("品牌") }}</view>
                                <view class="more" @click="brandShow = !brandShow">
                                    <text class="more-text">{{ brandShow ? $t("收起") : $t("展开") }}</text>
                                    <text v-if="brandShow" class="iconfont-h5 icon-up" style="font-size: 16px; color: #bfbfbf" />
                                    <text v-else class="iconfont-h5 icon-arrowdown" style="font-size: 16px; color: #bfbfbf" />
                                </view>
                            </view>
                            <view class="tabs flex-wrap">
                                <view
                                    v-for="(item, index) in displayBrandList"
                                    :key="index"
                                    class="item"
                                    :class="{ active: localBrandIds.includes(item.brandId) }"
                                    @click="onChangeBrand(item)"
                                >
                                    <text v-if="localBrandIds.includes(item.brandId)" class="iconfont-h5 icon-gouxuan" style="font-size: 12px" />
                                    {{ item.brandName }}
                                </view>
                            </view>
                        </view>
                    </template>

                    <template v-if="attrsList.length > 0">
                        <view v-for="item in attrsList" :key="item.name" class="tab_box">
                            <view class="title-box flex justify-between">
                                <view class="txt">{{ item.name }}</view>
                                <view v-if="item.values.length > 9" class="more" @click="toggleAttrShow(item.name)">
                                    <text class="more-text">{{ isAttrExpanded(item.name) ? $t("收起") : $t("展开") }}</text>
                                    <text v-if="isAttrExpanded(item.name)" class="iconfont-h5 icon-up" style="font-size: 16px; color: #bfbfbf" />
                                    <text v-else class="iconfont-h5 icon-arrowdown" style="font-size: 16px; color: #bfbfbf" />
                                </view>
                            </view>
                            <view class="tabs flex-wrap">
                                <view
                                    v-for="(value, cIndex) in isAttrExpanded(item.name) ? item.values : item.values.slice(0, 9)"
                                    :key="`${item.name}-${cIndex}`"
                                    class="item"
                                    :class="{ active: isAttrSelected(item.name, getAttrOptionText(value)) }"
                                    @click="onAttrClick(item.name, getAttrOptionText(value))"
                                >
                                    <text v-if="isAttrSelected(item.name, getAttrOptionText(value))" class="iconfont-h5 icon-gouxuan" style="font-size: 12px" />
                                    <text>{{ getAttrOptionText(value) }}</text>
                                </view>
                            </view>
                        </view>
                    </template>

                    <view class="tab_box">
                        <view class="title-box flex justify-between">
                            <view class="txt">{{ $t("价格") }}</view>
                        </view>
                        <view class="tabs flex justify-between align-center">
                            <input v-model="localMin" class="uni-input" type="number" :placeholder="$t('最低价')" />
                            <view class="xian" />
                            <input v-model="localMax" class="uni-input" type="number" :placeholder="$t('最高价')" />
                        </view>
                    </view>
                </view>
            </scroll-view>
            <view class="btn-box" :class="{ 'safe-btn': safeBottom > 0 }">
                <view class="flex btn-flex">
                    <view class="btn" @click="onReset">{{ $t("重置") }}</view>
                    <view class="btn submit" @click="onSubmit">{{ $t("确定") }}</view>
                </view>
            </view>
        </view>
    </tig-popup>
</template>

<script lang="ts" setup>
/**
 * 搜索结果页 - 国内版右侧筛选抽屉
 * 对齐 vortmall drawer-domestic：打开时拷贝父级条件为本地草稿，确定后再回写；关闭不提交则丢弃草稿。
 */
import { ref, computed, watch, type PropType } from "vue";
import { useI18n } from "vue-i18n";
import { useSafeAreaInsets } from "@/hooks";
import type { Brand, filterSeleted, attrsData, attrsListData } from "@/types/search/search";
import { getAttrOptionText, useBrandSelection, useAttrsSelection, useSectionExpand } from "../../composables";

const props = defineProps({
    show: {
        type: Boolean,
        default: false
    },
    shopId: {
        type: [Number, String],
        default: ""
    },
    categoryList: {
        type: Array as PropType<filterSeleted[]>,
        default: () => []
    },
    shopCategoryList: {
        type: Array as PropType<filterSeleted[]>,
        default: () => []
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
    cat: {
        type: [Number, String],
        default: 0
    },
    shopCategoryId: {
        type: [Number, String],
        default: ""
    },
    min: {
        type: [Number, String],
        default: ""
    },
    max: {
        type: [Number, String],
        default: ""
    }
});

const emit = defineEmits<{
    (e: "update:show", value: boolean): void;
    (
        e: "submit",
        data: {
            brandIds: number[];
            attrs: attrsData[];
            cat: number | string;
            shopCategoryId: number | string;
            min: string | number;
            max: string | number;
        }
    ): void;
    (e: "reset"): void;
}>();

const { t } = useI18n();
const { safeBottom } = useSafeAreaInsets();

const showDrawer = computed({
    get: () => props.show,
    set: (val) => emit("update:show", val)
});

const categoryShow = ref(false);
const brandShow = ref(false);
const { isExpanded: isAttrExpanded, toggleExpand: toggleAttrShow, resetExpand: resetAttrExpand } = useSectionExpand(true);

const localBrandIds = ref<number[]>([]);
const localAttrs = ref<attrsData[]>([]);
const localCat = ref<number | string>(0);
const localShopCategoryId = ref<number | string>("");
const localMin = ref<string | number>("");
const localMax = ref<string | number>("");

const { onChangeBrand } = useBrandSelection(localBrandIds);
const { isAttrSelected, onAttrClick } = useAttrsSelection(localAttrs);

const initLocalState = () => {
    localBrandIds.value = [...props.brandIds];
    localAttrs.value = props.attrs.map((item) => ({ ...item, attrValue: [...item.attrValue] }));
    localCat.value = props.cat ?? 0;
    localShopCategoryId.value = props.shopCategoryId ?? "";
    localMin.value = props.min ?? "";
    localMax.value = props.max ?? "";
    categoryShow.value = false;
    brandShow.value = false;
    resetAttrExpand(
        props.attrsList.map((item) => item.name),
        true
    );
};

watch(
    () => props.show,
    (val) => {
        if (val) {
            initLocalState();
        }
    }
);

const displayCategoryList = computed(() => {
    const list = props.categoryList ?? [];
    return list.length > 9 && !categoryShow.value ? list.slice(0, 9) : list;
});

const displayShopCategoryList = computed(() => {
    const list = props.shopCategoryList ?? [];
    return list.length > 9 && !categoryShow.value ? list.slice(0, 9) : list;
});

const displayBrandList = computed(() => {
    const list = props.brandList ?? [];
    return list.length > 9 && !brandShow.value ? list.slice(0, 9) : list;
});

const onReset = () => {
    localBrandIds.value = [];
    localAttrs.value = [];
    localCat.value = 0;
    localShopCategoryId.value = "";
    localMin.value = "";
    localMax.value = "";
    emit("reset");
};

const onSubmit = () => {
    if (Number(localMin.value) > Number(localMax.value)) {
        uni.showToast({
            title: t("最低价不能大于最高价"),
            icon: "none"
        });
        return;
    }
    emit("submit", {
        brandIds: localBrandIds.value,
        attrs: localAttrs.value,
        cat: localCat.value,
        shopCategoryId: localShopCategoryId.value,
        min: localMin.value,
        max: localMax.value
    });
    showDrawer.value = false;
};
</script>

<style lang="scss" scoped>
:deep(.popup-container) {
    border-radius: 15rpx 0 0 0;
}

.search_condition {
    width: 88vw;
    height: 100vh;
    display: flex;
    flex-direction: column;

    .search_condition-scroll {
        flex: 1;
        overflow: hidden;
    }

    .search_condition-box {
        position: relative;
        padding-top: var(--safe-top);
        padding-bottom: 20rpx;
    }

    .tab_box {
        padding: 20rpx;
        .title-box {
            display: flex;
            align-items: center;
            color: #666;
            padding: 10rpx 0;
            margin-bottom: 10rpx;

            .txt {
                line-height: 32rpx;
            }

            .more {
                display: flex;
                align-items: center;
                height: 32rpx;

                .more-text {
                    font-size: 24rpx;
                    color: #bfbfbf;
                    margin-right: 4rpx;
                    line-height: 32rpx;
                }

                .iconfont-h5 {
                    position: relative;
                    display: block;
                    width: 32rpx;
                    height: 32rpx;
                    font-size: 0 !important;

                    &::before {
                        position: absolute;
                        top: 50%;
                        left: 50%;
                        width: 12rpx;
                        height: 12rpx;
                        box-sizing: border-box;
                        border-right: 2rpx solid currentColor;
                        border-bottom: 2rpx solid currentColor;
                        content: "";
                    }

                    &.icon-arrowdown::before {
                        transform: translate(-50%, -70%) rotate(45deg);
                    }

                    &.icon-up::before {
                        transform: translate(-50%, -30%) rotate(-135deg);
                    }
                }
            }
        }
        .tabs {
            .item {
                margin-top: 10px;
                width: 30%;
                text-align: center;
                line-height: 29px;
                background-color: #f0f2f5;
                border: 1px solid #f0f2f5;
                border-radius: 5px;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
                font-size: 20rpx;
                margin: 10rpx;
                text {
                    overflow: hidden;
                    white-space: nowrap;
                    text-overflow: ellipsis;
                }
            }
            .active {
                border: 1px solid var(--search-theme, var(--general));
                color: var(--search-theme, var(--general));
                :deep(.iconfont-h5) {
                    color: var(--search-theme, var(--general)) !important;
                }
            }
            .uni-input {
                width: 42%;
                background-color: #f0f2f5;
                padding: 10rpx;
                font-size: 22rpx;
                text-align: center;
                border-radius: 5px;
            }
            .xian {
                width: 16rpx;
                margin: 0 10rpx;
                border-bottom: 2rpx solid #848689;
            }
        }
    }

    .btn-box {
        flex-shrink: 0;
        width: 100%;
        padding-bottom: 20rpx;
        background-color: #fff;
        border-top: 1px solid #f0f2f5;

        &.safe-btn {
            padding-bottom: var(--safe-bottom);
        }

        .btn-flex {
            padding: 0 20rpx;
            column-gap: 20rpx;
            justify-content: center;
        }

        .btn {
            height: 70rpx;
            flex: 1;
            background-color: #f0f2f5;
            box-sizing: content-box;
            border-radius: 8rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 0 90rpx;
        }
        .submit {
            background-color: var(--search-theme, var(--general));
            color: #fff;
        }
    }
}
</style>
