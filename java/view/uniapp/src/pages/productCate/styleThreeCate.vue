<template>
    <view :style="decorateVars">
        <tig-navbar v-if="!isOverseasSkin" :title="$t('产品分类')" :show-left="false" />
        <search v-if="!isOverseasSkin" :needMenu="true" :placeholder="searchPlaceholder" @menu="handleMenu" @search="handleSearch" />
        <view class="scroll-box">
            <view class="content-box">
                <view class="side">
                    <menuBox v-model:current-cate-id="currentCateId" @change="reset" />
                </view>
                <view class="list-box">
                    <selectCate
                        v-model:menu-list="menuList"
                        v-model:sub-current-cate-id="subCurrentCateId"
                        type="main"
                        :current-cate-id="currentCateId"
                        :module="moduleConfig"
                        @change="handleChange"
                    />
                    <view class="list-content">
                        <template v-if="!isLoading && total > 0">
                            <scroll-view :scroll-y="true" class="list-scroll" @scrolltolower="reachBottom">
                                <view class="product-list" :class="productListClasses">
                                    <view v-for="item in list" :key="item.productId" class="product-item">
                                        <view class="item-left" @click="handleToDetail(item.productId!)">
                                            <tig-lazy-image :src="productImage(item)" />
                                            <template v-if="shouldShowSoldOut(item) || item.productStatus == 0">
                                                <view class="product-status-box">
                                                    <view class="outsale">{{ $t(shouldShowSoldOut(item) ? "已售罄" : "已下架") }}</view>
                                                </view>
                                            </template>
                                            <image
                                                v-if="item.productId != null && styleMap[item.productId]?.borderImage"
                                                class="product-style-border"
                                                :src="item.productId != null ? styleMap[item.productId]?.borderImage : ''"
                                                mode="scaleToFill"
                                            />
                                        </view>
                                        <view class="item-right">
                                            <view
                                                v-if="showProductName"
                                                class="item-right-title"
                                                :class="nameRowClass"
                                                @click="handleToDetail(item.productId!)"
                                            >
                                                <text>{{ item.productName }}</text>
                                            </view>
                                            <view class="item-right-meta">
                                                <view class="item-right-price">
                                                    <template v-if="isWholesaleCard(item)">
                                                        <format-price
                                                            :decimals-style="{
                                                                fontSize: '24rpx',
                                                                fontWeight: 'bold'
                                                            }"
                                                            :currency-style="{
                                                                fontSize: '23rpx',
                                                                fontWeight: 'bold'
                                                            }"
                                                            :font-style="{ fontSize: '32rpx' }"
                                                            :price-data="item.firstWholesalePrice"
                                                        />
                                                    </template>
                                                    <template v-else>
                                                        <format-price
                                                            :decimals-style="{
                                                                fontSize: '24rpx',
                                                                fontWeight: 'bold'
                                                            }"
                                                            :currency-style="{
                                                                fontSize: '23rpx',
                                                                fontWeight: 'bold'
                                                            }"
                                                            :font-style="{ fontSize: '32rpx' }"
                                                            :price-data="item.deliveryMode === 2 ? item.cyclePrice : item.productPrice"
                                                        />
                                                    </template>
                                                </view>
                                                <view v-if="showBuyBtn" class="buy_icon">
                                                    <view class="buy_btn" :class="buyBtnClasses" @click="handleBuy(item)">
                                                        <template v-if="item.isCheck">
                                                            <up-loading-icon mode="circle" />
                                                        </template>
                                                        <template v-else-if="buyBtnStyleVal === 5 || buyBtnStyleVal === 6">
                                                            <text class="buy_btn_text">{{ $t("购买") }}</text>
                                                        </template>
                                                        <template v-else-if="buyBtnStyleVal === 7 || buyBtnStyleVal === 8">
                                                            <text class="buy_btn_text">{{ $t("马上抢") }}</text>
                                                        </template>
                                                        <template v-else-if="buyBtnStyleVal === 1">
                                                            <text class="iconfont-h5 buy_btn_icon icon-gouwuche" />
                                                        </template>
                                                        <template v-else-if="buyBtnStyleVal === 2">
                                                            <text class="iconfont-h5 buy_btn_icon icon-gouwuche1" />
                                                        </template>
                                                        <template v-else-if="buyBtnStyleVal === 3">
                                                            <text class="iconfont-h5 buy_btn_icon icon-jia" />
                                                        </template>
                                                        <template v-else-if="buyBtnStyleVal === 4">
                                                            <text class="iconfont-h5 buy_btn_icon icon-jia1" />
                                                        </template>
                                                        <template v-else>
                                                            <text class="iconfont-h5 icon-gouwuche3 buy_btn_icon" />
                                                        </template>
                                                    </view>
                                                </view>
                                            </view>
                                        </view>
                                    </view>
                                </view>
                            </scroll-view>
                        </template>
                        <template v-if="!isLoading && total === 0">
                            <view class="empty-box">
                                <up-empty :icon="staticResource('salesman/no_order.png')" :text="$t('暂无数据')" />
                            </view>
                        </template>
                    </view>
                </view>
            </view>
        </view>
        <specification
            ref="specificationRef"
            v-model="productId"
            v-model:is-cycle-buy="isCycleBuy"
            @load-end="loadEnd"
            @close="handleClose"
            @send-value="handleSpecValue"
        />
        <WholesaleListSpec ref="wholesaleSpecRef" v-model="wholesaleProductId" @load-end="wholesaleLoadEnd" @close="handleWholesaleClose" />
    </view>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { productImage } from "@/utils/format";

/**
 * 分类样式三组件
 * - 顶部分类Tab+商品列表
 * - 支持SKU弹窗快速加购
 */
import { computed, reactive, ref, type PropType } from "vue";
import search from "./src/search.vue";
import menuBox from "./src/menu.vue";
import selectCate from "./src/selectCate.vue";
import specification from "@/components/product/specification.vue";
import WholesaleListSpec from "@/components/wholesale/WholesaleListSpec.vue";
import { useProductCardBuy } from "@/hooks/useProductCardBuy";
import { getCateProduct } from "@/api/home/home";
import type { GetProductFilterResult } from "@/types/home/home";
import { useList } from "@/hooks";
import { useProductStyle } from "@/hooks/useProductStyle";
import { staticResource, redirect } from "@/utils";
import { shouldShowSoldOut } from "@/utils/productSoldOut";
import type { filterSeleted } from "@/types/productCate/productCate";
import type { CateCenterV2Data } from "@/components/modules/cate-center/cate-center-v2/types";
import { buildCateCenterV2Vars, cateCenterV2Defaults } from "@/components/modules/cate-center/cate-center-v2/config";
import { deepMerge } from "@/utils/deepMerge";
import { useConfigStore } from "@/store/config";

const { t } = useI18n();
const configStore = useConfigStore();

const currentCateId = ref(0);

const subCurrentCateId = ref(0);

const menuList = ref<filterSeleted[]>([]);

const props = defineProps({
    height: {
        type: [Number, String],
        default: 0
    },
    /** 装修配置 */
    module: {
        type: Object as PropType<CateCenterV2Data>,
        default: () => ({})
    }
});

const moduleConfig = computed<CateCenterV2Data>(() => {
    return deepMerge({ ...cateCenterV2Defaults }, props.module || {});
});
const decorateVars = computed(() => buildCateCenterV2Vars(moduleConfig.value));

const searchPlaceholder = computed(() => moduleConfig.value.searchPlaceholder || t("搜索商品"));
const showProductName = computed(() => (moduleConfig.value.showName ?? cateCenterV2Defaults.showName ?? 1) !== 0);
const buyBtnStyleVal = computed(() => moduleConfig.value.buyBtnStyle ?? cateCenterV2Defaults.buyBtnStyle ?? 1);
const showBuyBtn = computed(() => buyBtnStyleVal.value !== 0);

const productListClasses = computed(() => {
    const cls: string[] = [];
    if (moduleConfig.value.textAlign === 2) cls.push("product-list--align-2");
    if (moduleConfig.value.textWeight === 1) cls.push("product-list--weight-1");
    return cls;
});

const nameRowClass = computed(() => {
    const row = moduleConfig.value.goodsNameRow ?? cateCenterV2Defaults.goodsNameRow ?? 2;
    return row === 1 ? "item-right-title--row-1" : "";
});

const buyBtnClasses = computed(() => {
    if (buyBtnStyleVal.value >= 1 && buyBtnStyleVal.value <= 8) {
        return [`buy_btn--style${buyBtnStyleVal.value}`];
    }
    return [];
});

const params = reactive({
    categoryId: 0,
    page: 1,
    size: 10
});
const {
    data: list,
    getList,
    reachBottom,
    isLoading,
    total
} = useList<GetProductFilterResult>(getCateProduct, {
    params,
    path: {
        dataKey: "records"
    },
    needReachBottom: false
});

const { styleMap } = useProductStyle(list);

const handleChange = () => {
    list.value = [];
    params.page = 1;
    if (subCurrentCateId.value === 0) {
        params.categoryId = currentCateId.value;
    } else {
        params.categoryId = subCurrentCateId.value;
    }
    getList();
};

defineExpose({
    refreshProductList: handleChange
});

const reset = () => {
    subCurrentCateId.value = 0;
    list.value = [];
    menuList.value = [];
    params.page = 1;
};

const handleMenu = () => {
    redirect({
        url: "/pages/index/index"
    });
};
const handleSearch = () => {
    redirect({
        url: "/pages/search/index"
    });
};

const handleToDetail = (id: number) => {
    redirect({
        url: `/pages/product/index?id=${id}`
    });
};

const productId = ref(0);
const specificationRef = ref();
const currentItem = ref<any>({});
// 是否是周期购
const isCycleBuy = ref(false);

// 接收规格组件传递的商品信息，判断是否为周期购
const handleSpecValue = (val: any) => {
    if (val.cycleInfo && Object.keys(val.cycleInfo).length > 0) {
        isCycleBuy.value = true;
    } else {
        isCycleBuy.value = false;
    }
};

const handleBuy = (item: any) => {
    if (item.isCheck) return;
    item.isCheck = true;
    currentItem.value = item;
    // 批发卡片：走独立批发列表规格弹层（与零售 specification 互斥）
    if (isWholesaleCard(item)) {
        wholesaleProductId.value = cardProductId(item);
        return;
    }
    productId.value = item.productId;
};
const loadEnd = () => {
    specificationRef.value.handleShowPopup();
    currentItem.value.isCheck = false;
};
const handleClose = () => {
    productId.value = 0;
};

const wholesaleProductId = ref(0);
const wholesaleSpecRef = ref<InstanceType<typeof WholesaleListSpec>>();
const { isWholesaleCard, cardProductId } = useProductCardBuy();
const wholesaleLoadEnd = (payload: { wholesale: boolean }) => {
    if (!payload.wholesale) {
        // 非批发商品或加载失败：回退零售规格半窗
        productId.value = wholesaleProductId.value;
        wholesaleProductId.value = 0;
        return;
    }
    wholesaleSpecRef.value?.handleShowPopup();
    currentItem.value.isCheck = false;
};
const handleWholesaleClose = () => {
    wholesaleProductId.value = 0;
};

const height = computed(() => {
    return `calc(${props.height} - 90rpx)`;
});
</script>
<style>
/* 兼容小程序 */
page {
    background-color: #fff !important;
}
</style>
<style lang="scss" scoped>
.main-header-spacer {
    width: 100%;
    flex-shrink: 0;
}

.scroll-box {
    height: v-bind("height");
    overflow: hidden;
}
.content-box {
    height: 100%;
    width: 100%;
    display: flex;

    .side {
        width: 90px;
        height: 100%;
        background-color: var(--cate-side-bg, #f7f7f7);
    }

    .list-box {
        width: calc(100% - 90px);
        height: 100%;
        .list-content {
            height: calc(100% - 100rpx);
            .list-scroll {
                height: 100%;
            }

            .empty-box {
                display: flex;
                align-items: center;
                justify-content: center;
                height: 100%;
                width: 100%;
            }
        }
    }
}

/* --cate-* 由分类页装修下发（见 buildCateCenterV2Vars），未配置时 fallback 即原有视觉 */
.product-list {
    padding: 25rpx;
    background-color: var(--cate-goods-bg, #fff);

    .product-item {
        display: flex;
        padding: 20rpx 0;
        .item-left {
            width: 171rpx;
            height: 171rpx;
            border-radius: 10rpx;
            overflow: hidden;
            position: relative;

            .product-status-box {
                width: 100%;
                height: 100%;
                position: absolute;
                left: 0;
                top: 0;
                z-index: 10;
                display: flex;
                align-items: center;
                justify-content: center;

                .outsale {
                    width: 80%;
                    height: 80%;
                    line-height: 180rpx;
                    background: rgba(0, 0, 0, 0.4);
                    font-size: 30rpx;
                    color: #ffffff;
                    letter-spacing: 0;
                    border-radius: 100%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
            }

            .product-style-border {
                position: absolute;
                left: 0;
                top: 0;
                width: 100%;
                height: 100%;
                z-index: 5;
                pointer-events: none;
            }
        }

        .item-right {
            width: calc(100% - 171rpx);
            padding-left: 10rpx;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            position: relative;
            .item-right-title {
                font-size: 24rpx;
                font-weight: bold;
                color: var(--cate-title, #2a3145);
                display: -webkit-box;
                -webkit-box-orient: vertical;
                -webkit-line-clamp: 2;
                line-clamp: 2;
                overflow: hidden;
                word-break: break-all;
            }
            .item-right-title--row-1 {
                -webkit-line-clamp: 1;
                line-clamp: 1;
            }
            .item-right-meta {
                display: flex;
                align-items: center;
                justify-content: space-between;
                width: 100%;
            }
            .item-right-price {
                color: var(--cate-price, var(--cate-theme, var(--general)));
                font-weight: 600;
            }
            .buy_icon {
                color: var(--cate-buy-btn, var(--cate-theme, var(--general)));
                display: flex;
                align-items: center;
                .buy_btn_icon {
                    font-size: 40rpx;
                }
            }
        }
    }
}

.cycleBuyTag {
    display: inline-block;
    vertical-align: baseline;
    color: var(--cate-buy-btn, var(--general));
    position: relative;
    padding-right: 20rpx;
    &::before {
        position: absolute;
        content: "";
        right: 9rpx;
        top: 0;
        width: 2rpx;
        height: 70%;
        transform: translateY(22%);
        background-color: var(--cate-buy-btn, var(--general));
    }
}

// ===== 装修配置：文本对齐 / 文字粗细 =====
.product-list--align-2 {
    .product-item .item-right {
        align-items: center;
    }
    .product-item .item-right .item-right-title {
        width: 100%;
        text-align: center;
    }
    .product-item .item-right .item-right-meta {
        justify-content: center;
        column-gap: 16rpx;
    }
}

.product-list--weight-1 {
    .product-item .item-right .item-right-title {
        font-weight: normal;
    }
}

// ===== 购买按钮装修样式（1~8）=====
.product-item .item-right .buy_icon .buy_btn {
    display: flex;
    align-items: center;
    justify-content: center;

    .buy_btn_text {
        font-size: 24rpx;
        white-space: nowrap;
    }

    &.buy_btn--style5,
    &.buy_btn--style7 {
        height: 44rpx;
        padding: 0 16rpx;
        box-sizing: border-box;
        border: 2rpx solid var(--cate-buy-btn, var(--cate-theme, var(--general)));
        border-radius: 4rpx;
        background-color: transparent;
        color: var(--cate-buy-btn, var(--cate-theme, var(--general)));

        .buy_btn_text {
            color: var(--cate-buy-btn, var(--cate-theme, var(--general)));
        }
    }

    &.buy_btn--style6,
    &.buy_btn--style8 {
        height: 44rpx;
        padding: 0 20rpx;
        border-radius: 44rpx;
        background-color: var(--cate-buy-btn, var(--cate-theme, var(--general)));
        color: #fff;

        .buy_btn_text {
            color: #fff;
        }
    }
}
</style>
