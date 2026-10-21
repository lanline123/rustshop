<template>
    <!-- 搜索页单列商品列表（listStyle=2）：左图右信息，仅搜索页使用，不影响通用 masonry -->
    <view class="search-list" :style="cssVars">
        <view v-for="(item, index) in productList" :key="item.productId || index" class="search-list__item" @click="toPage(item)">
            <view class="photo">
                <tig-lazy-image :src="item.picUrl" mode="widthFix" />
                <view v-if="showOutsale(item)" class="product-status-box">
                    <view class="outsale">{{ $t("已售罄") }}</view>
                </view>
            </view>
            <view class="info">
                <view class="detail" :style="titleClampStyle">{{ item.productName }}</view>
                <view v-if="config.showSales !== false" class="tips">{{ $t("已售") }} {{ item.virtualSales || 0 }}</view>

                <view v-if="getActivityInfo(item).length || isPresale(item)" class="flex activity-box">
                    <template v-for="subItem in getActivityInfo(item)" :key="subItem.promotionId || subItem.type">
                        <view v-if="subItem.type === 7" class="special-activity-tag groupon-tag">{{ $t("拼团") }}</view>
                        <activityCard v-else :activity-info="subItem" :theme-color="config.themeColor" />
                    </template>
                    <view v-if="isPresale(item)" class="special-activity-tag presale-tag">{{ $t("预售") }}</view>
                </view>

                <view class="action">
                    <view class="pricenum">
                        <format-price
                            :font-style="{ fontSize: '32rpx', fontWeight: 'bold' }"
                            :currency-style="{ fontSize: '22rpx', fontWeight: 'bold' }"
                            :decimals-style="{ fontSize: '25rpx', fontWeight: 'bold' }"
                            :price-data="item.price"
                        />
                    </view>
                    <view v-if="config.showShopTag !== false" class="shop-name">{{ item.shop?.shopTitle || $t("官方自营店") }}</view>
                    <view v-if="config.showCartButton !== false" class="shop-btn">
                        <view class="btn" @click.stop="handleBuy(item)">
                            <up-loading-icon v-if="item.isCheck" size="22" mode="circle" />
                            <text v-else>{{ $t("添加购物车") }}</text>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>

    <specification
        ref="specificationRef"
        v-model="productId"
        v-model:is-cycle-buy="isCycleBuy"
        v-model:is-sub-card="isSubCard"
        @add-card-succeed="emitCallback"
        @load-end="loadEnd"
        @close="handleClose"
    />
</template>

<script lang="ts" setup>
/**
 * 搜索结果页 - 单列商品列表
 * 加购走规格弹窗流程（与 masonry 商品卡一致）。
 */
import { ref, computed, type PropType, type CSSProperties } from "vue";
import specification from "@/components/product/specification.vue";
import activityCard from "@/components/masonry/activityCard.vue";
import { redirect } from "@/utils";
import { shouldShowSoldOut } from "@/utils/productSoldOut";
import type { SearchResultModuleData } from "@/components/modules/search-result/types";
import { searchResultDefaults } from "@/components/modules/search-result/config";

const props = defineProps({
    productList: {
        type: Array as PropType<any[]>,
        default: () => []
    },
    promotionList: {
        type: Object as PropType<Record<string, any>>,
        default: () => ({})
    },
    config: {
        type: Object as PropType<SearchResultModuleData>,
        default: () => ({ ...searchResultDefaults })
    }
});

const emit = defineEmits<{
    (e: "callback"): void;
}>();

const emitCallback = () => emit("callback");

const cssVars = computed<CSSProperties>(() => ({
    "--list-item-bg": props.config.itemBackgroundColor || "#ffffff",
    "--list-title-color": props.config.productTitleColor || "#333333",
    "--list-price-color": props.config.priceColor || props.config.themeColor || "var(--general)",
    "--list-theme-color": props.config.themeColor || "var(--general)",
    "--list-image-radius": `${(props.config.productImageRadius ?? 4) * 2}rpx`
}));

/**
 * 标题截断放在内联样式上：-webkit-box / -webkit-box-orient 走 CSS 时
 * 可能被 PostCSS 当作废弃属性剥离，导致行数配置失效
 */
const titleClampStyle = computed<CSSProperties>(() => {
    const line = String(props.config.productTitleLine ?? 2);
    return {
        display: "-webkit-box",
        WebkitBoxOrient: "vertical",
        WebkitLineClamp: line,
        lineClamp: line,
        overflow: "hidden",
        textOverflow: "ellipsis",
        wordBreak: "break-all"
    };
});

const showOutsale = (item: any) => shouldShowSoldOut(item);

const getActivityInfo = (item: any) => {
    const activityInfo = props.promotionList?.[item.productId]?.activityInfo || [];
    // 单列模式按产品规则不展示优惠券；满减、折扣、秒杀等其它活动标签保留。
    return activityInfo.filter((sub: any) => sub.type !== 2);
};

const isPresale = (item: any) => Number(item?.saleMode ?? item?.presaleInfo?.saleMode ?? 0) > 0;

const toPage = (item: any) => {
    const promotionItem = props.promotionList[item.productId];
    const skuId = promotionItem?.activityInfo?.[0]?.skuIds?.[0];
    let params = `id=${item.productId}`;
    if (skuId) params += `&skuId=${skuId}`;
    if (item.shop?.shopId) params += `&shopId=${item.shop.shopId}`;
    redirect({ url: "/pages/product/index?" + params });
};

const productId = ref(0);
const specificationRef = ref();
const currentItem = ref<any>({});

const isCycleBuy = computed(() => currentItem.value.deliveryMode === 2 && currentItem.value.deliveryType === 1);
const isSubCard = computed(() => currentItem.value.deliveryMode === 2 && currentItem.value.deliveryType === 2);

const handleBuy = (item: any) => {
    if (item.isCheck) return;
    item.isCheck = true;
    currentItem.value = item;
    productId.value = item.productId;
};

const loadEnd = () => {
    specificationRef.value.handleShowPopup();
    currentItem.value.isCheck = false;
};

const handleClose = () => {
    productId.value = 0;
};
</script>

<style lang="scss" scoped>
.search-list {
    width: 100%;

    &__item {
        display: flex;
        margin-bottom: 16rpx;
        background-color: var(--list-item-bg, #fff);
        border-radius: var(--list-image-radius, 8rpx);
    }
}

.photo {
    width: 230rpx;
    height: 230rpx;
    flex-shrink: 0;
    overflow: hidden;
    border-radius: var(--list-image-radius, 8rpx);
    position: relative;

    image {
        width: 100%;
    }

    .product-status-box {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        display: flex;
        justify-content: center;
        align-items: center;
        width: 150rpx;
        height: 150rpx;
        background: rgba(0, 0, 0, 0.4);
        color: #fff;
        border-radius: 50%;
    }
}

.info {
    flex: 1;
    width: 0;
    padding: 2.5px 10px 0;
    display: flex;
    flex-direction: column;
    gap: 10rpx;

    .detail {
        font-size: 28rpx;
        font-weight: 500;
        line-height: 40rpx;
        color: var(--list-title-color, #333);
    }

    .tips {
        color: #888b94;
        font-size: 22rpx;
    }

    .activity-box {
        align-items: center;
        flex-wrap: wrap;
        gap: 5rpx;

        :deep(.promotion) {
            margin-bottom: 0;
            margin-right: 0;
        }

        .special-activity-tag {
            padding: 4rpx 12rpx;
            border-radius: 4rpx;
            color: #fff;
            font-size: 24rpx;
            line-height: 1;
        }

        .groupon-tag {
            background-color: #ee7700;
        }

        .presale-tag {
            background-color: #e47200;
        }
    }

    .action {
        margin-top: auto;
        padding: 5rpx 0 10rpx;

        .pricenum {
            color: var(--list-price-color, var(--general));
        }

        .shop-name {
            height: 28rpx;
            font-size: 22rpx;
            line-height: 28rpx;
            margin-top: 11rpx;
            color: #269636;
            font-weight: bold;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .shop-btn {
            height: 63rpx;
            color: #fff;
            background-color: #1a1a1a;
            border-radius: 7rpx;
            padding: 0 14rpx;
            min-width: 222rpx;
            max-width: 260rpx;
            font-weight: 600;
            font-size: 25rpx;
            margin-top: 10rpx;

            .btn {
                width: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
            }
        }
    }
}
</style>
