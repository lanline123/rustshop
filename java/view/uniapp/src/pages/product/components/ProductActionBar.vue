<template>
    <view
        :style="actionBarSafeBottomStyle"
        class="bottom-bar-box"
        :class="{
            'bottom-bar-box--overseas': isOverseasBar,
            'bottom-bar-box--overseas-simple': isOverseasSimpleBar,
            'bottom-bar-box--overseas-classic': isOverseasClassicBar,
            'bottom-bar-box--vortmall-simple': isVortmallSimpleBar,
            'bottom-bar-box--vortmall-classic': isVortmallClassicBar,
            'bottom-bar-box--o2o-simple': isO2oSimpleBar
        }"
    >
        <view class="bottom-bar flex align-center">
            <view v-if="!isOverseasBar" class="icon-box">
                <view v-if="decShowHome" class="label" @click="emit('toHome')">
                    <view>
                        <image mode="widthFix" :src="staticResource('product/home.png')" class="img" />
                    </view>
                    <view class="label-text">
                        <text>{{ $t("首页") }}</text>
                    </view>
                </view>

                <view v-if="showService && decShowService" class="label">
                    <service :phone="shopInfo.kefuPhone" :product-id="productId" :shop-id="shopInfo.shopId">
                        <view>
                            <image mode="widthFix" :src="staticResource('product/service.png')" class="img" />
                        </view>
                        <view class="label-text">
                            <text>{{ $t("客服") }}</text>
                        </view>
                    </service>
                </view>

                <view v-if="!isCycleBuy && !isPresale && decShowCart" class="label cart-position" @click.stop="emit('toCart')">
                    <view>
                        <uni-badge class="uni-badge-left-margin badgecolor" :text="cartCount" absolute="rightTop" size="small">
                            <image mode="widthFix" :src="staticResource('product/cart.png')" class="img" />
                        </uni-badge>
                    </view>
                    <view class="label-text">
                        <text>{{ $t("购物车") }}</text>
                    </view>
                </view>
            </view>
            <view class="btn-box">
                <template v-if="product.productType === 4 && product.isBuy === 1">
                    <tig-button :disabled="true" class="isBuyBtn" :style="fullBtnStyle">{{ $t("商品已购买") }} </tig-button>
                </template>
                <template v-else-if="(product.productStatus === 0 || productStock === 0) && !isSubCardReservationProduct">
                    <view class="other-products-btn" :style="fullBtnStyle" @click="emit('toProductList')">{{ $t("查看其他商品") }}</view>
                </template>
                <template v-else>
                    <template v-if="type === 'exchange'">
                        <view class="other-products-btn" :style="fullBtnStyle" @click="emit('showSpecification')">{{ $t("立即兑换") }}</view>
                    </template>
                    <template v-else>
                        <template v-if="isPresale && productPresale">
                            <view class="other-products-btn" :style="fullBtnStyle" @click="emit('showSpecification')">
                                <template v-if="productPresale.saleMode === 2">
                                    <format-price
                                        :font-style="{ fontWeight: 'bold' }"
                                        :decimals-style="{
                                            fontSize: '24rpx',
                                            fontWeight: 'bold'
                                        }"
                                        :currency-style="{
                                            fontSize: '24rpx',
                                            fontWeight: 'bold'
                                        }"
                                        :price-data="productPresale.depositAmount"
                                    />
                                    {{ $t("立即预定") }}
                                </template>
                                <template v-else>
                                    {{ $t("立即预购") }}
                                </template>
                            </view>
                        </template>
                        <template v-else>
                            <template v-if="product.productType === 1 || isGroupon">
                                <template v-if="isGroupon">
                                    <view class="btn cart" :style="cartBtnStyle" @click="emit('showSpecification')">{{ $t("单独购买") }} </view>
                                    <view v-if="grouponRestricted" class="btn buy btn-restricted" :style="buyBtnStyle">
                                        {{ $t("我要开团") }}
                                    </view>
                                    <view v-else class="btn buy" :style="buyBtnStyle" @click="emit('showGrouponPopup')">
                                        {{ $t("我要开团") }}
                                    </view>
                                </template>
                                <template v-else>
                                    <template v-if="isCycleBuy || isSubCard">
                                        <view class="other-products-btn" :style="fullBtnStyle" @click="emit('showSpecification')">{{ $t("立即购买") }}</view>
                                    </template>
                                    <template v-else-if="memberRestricted">
                                        <view class="btn cart btn-restricted" :style="cartBtnStyle">{{ $t("加入购物车") }} </view>
                                        <view class="btn buy btn-restricted" :style="buyBtnStyle">{{ $t("立即购买") }} </view>
                                    </template>
                                    <template v-else>
                                        <view class="btn cart" :style="cartBtnStyle" @click="emit('showSpecification')">{{ $t("加入购物车") }}</view>
                                        <view class="btn buy" :style="buyBtnStyle" @click="emit('showSpecification', true)">{{ $t("立即购买") }} </view>
                                    </template>
                                </template>
                            </template>
                            <template v-else>
                                <view class="other-products-btn" :style="fullBtnStyle" @click="emit('showSpecification')">{{
                                    isReservation && !isSubCardReservationProduct ? $t("立即预约") : $t("立即购买")
                                }}</view>
                            </template>
                        </template>
                    </template>
                </template>
            </view>
            <template v-if="memberRestricted">
                <view class="product-status member-restrict-status">
                    <text class="iconfont-h5 icon-tishi" />
                    {{ memberRestrictTip || $t("该商品仅限指定会员购买") }}
                </view>
            </template>
            <template v-else-if="isGroupon && grouponRestricted">
                <view class="product-status member-restrict-status">
                    <text class="iconfont-h5 icon-tishi" />
                    {{ $t("该商品仅限指定会员购买") }}
                </view>
            </template>
            <template v-if="detailLoaded && (product.productStatus === 0 || productStock === 0) && !isSubCardReservationProduct">
                <view class="product-status">
                    {{ $t(product.productStatus === 0 ? "商品已下架" : "商品已售罄") }}
                </view>
            </template>
        </view>
    </view>
</template>

<script lang="ts" setup>
/**
 * 商品详情底部操作栏
 * - 首页 / 客服 / 购物车快捷入口
 * - 按商品类型（普通 / 拼团 / 预售 / 兑换 / 预约 / 周期购 / 次卡）渲染对应按钮矩阵
 * - 下架 / 售罄状态提示
 */
import { computed, getCurrentInstance } from "vue";
import service from "@/components/service/index.vue";
import { staticResource } from "@/utils";
import { useConfigStore } from "@/store/config";
import type { ProductItem } from "@/types/product/product";
import type { PresaleInfo } from "@/types/presale/presale";
import type { ShopDetailItem } from "@/types/shop/shop";

const configStore = useConfigStore();
const instance = getCurrentInstance();

/** 底栏装修配置（风格一 透传；其余风格不传，默认保持原样） */
interface DecorateBarConfig {
    /** 购买栏样式：A1 为 1-简约、2-经典；A2/A3 为 1-圆润、2-直角 */
    buyBarStyle?: 1 | 2;
    /** 按钮圆角（直角样式下生效，单位 rpx） */
    buttonRadius?: number;
    /** 是否显示首页入口 */
    showHome?: boolean;
    /** 是否显示客服入口 */
    showService?: boolean;
    /** 是否显示购物车入口 */
    showCart?: boolean;
}

/**
 * 查询组件内部节点位置（用组件自身实例，兼容小程序自定义组件边界）
 * @param selector 节点选择器
 */
const getRect = (selector: string): Promise<any> => {
    return new Promise((resolve) => {
        uni.createSelectorQuery()
            .in(instance)
            .select(selector)
            .boundingClientRect((data) => {
                resolve(data || null);
            })
            .exec();
    });
};

interface Props {
    /** 商品详情 */
    product: ProductItem;
    /** 店铺信息（客服入口用） */
    shopInfo: ShopDetailItem;
    /** 当前库存 */
    productStock?: number;
    /** 商品模式：exchange 为积分兑换 */
    type?: string;
    /** 是否拼团（o2o 沿用 number 语义） */
    isGroupon?: number;
    /** 是否预售 */
    isPresale?: boolean;
    /** 预售信息 */
    productPresale?: PresaleInfo | null;
    /** 是否周期购 */
    isCycleBuy?: boolean;
    /** 是否次卡 */
    isSubCard?: boolean;
    /** 是否次卡预约商品 */
    isSubCardReservationProduct?: boolean;
    /** 是否预约商品（兼容 productType 5/6 与 flowType 9/10） */
    isReservation?: boolean;
    /** 详情是否已加载（控制售罄提示时机） */
    detailLoaded?: boolean;
    /** 购物车角标数量 */
    cartCount?: number | string;
    /** 是否展示客服入口 */
    showService?: boolean;
    /** 商品 ID（字符串透传） */
    productId?: string;
    /** 底栏装修配置（风格一 透传） */
    decorateConfig?: DecorateBarConfig;
    /** 布局风格：A1 / A2 / A3，A3 走直角描边风 */
    layoutStyle?: string;
    /** 当前用户不满足商品的会员购买门槛 */
    memberRestricted?: boolean;
    /** 会员门槛提示文案，为空时用默认文案 */
    memberRestrictTip?: string;
    /** 当前用户不满足拼团的会员参团门槛 */
    grouponRestricted?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    productStock: 0,
    type: "",
    isGroupon: 0,
    isPresale: false,
    productPresale: null,
    isCycleBuy: false,
    isSubCard: false,
    isSubCardReservationProduct: false,
    isReservation: false,
    detailLoaded: false,
    cartCount: 0,
    showService: false,
    productId: "",
    decorateConfig: () => ({}),
    layoutStyle: "",
    memberRestricted: false,
    memberRestrictTip: "",
    grouponRestricted: false
});

// A3 与 A2 使用相同语义：1-经典连体，2-简约分离。
const isOverseasBar = computed(() => props.layoutStyle === "A3");
const isOverseasClassicBar = computed(() => isOverseasBar.value && props.decorateConfig?.buyBarStyle === 1);
const isOverseasSimpleBar = computed(() => isOverseasBar.value && !isOverseasClassicBar.value);
const isVortmallBar = computed(() => props.layoutStyle === "A1");
const isO2oSimpleBar = computed(() => props.layoutStyle === "A2" && props.decorateConfig?.buyBarStyle === 2);
const isVortmallSimpleBar = computed(() => isVortmallBar.value && props.decorateConfig?.buyBarStyle !== 2);
const isVortmallClassicBar = computed(() => isVortmallBar.value && props.decorateConfig?.buyBarStyle === 2);

/**
 * 底部安全区：H5 保持原有 rpx 逻辑；微信端使用系统返回的真实 px。
 * A3 自身已有 35rpx 底部留白，仅补足超过该留白的安全区，避免重复叠加。
 */
const actionBarSafeBottomStyle = computed(() => {
    let paddingBottom = `${configStore.safeBottom}rpx`;
    // #ifdef MP-WEIXIN
    const windowWidth = configStore.windowInfo.windowWidth || uni.getSystemInfoSync().windowWidth || 375;
    const overseasBaseBottom = (35 * windowWidth) / 750;
    const safeBottom = isOverseasBar.value ? Math.max(configStore.safeBottom - overseasBaseBottom, 0) : configStore.safeBottom;
    paddingBottom = `${safeBottom}px`;
    // #endif
    return { paddingBottom };
});

// ===== 装修字段：底栏入口显隐（未配置视为显示，保持门店风原样）=====
const decShowHome = computed(() => props.decorateConfig?.showHome !== false);
const decShowService = computed(() => props.decorateConfig?.showService !== false);
const decShowCart = computed(() => props.decorateConfig?.showCart !== false);

// ===== 装修字段：A1 简约风使用可配圆角；A2/A3 的直角风继续使用原有圆角覆盖 =====
const isSquareBar = computed(() => !isVortmallBar.value && props.decorateConfig?.buyBarStyle === 2);
const btnRadius = computed(() => `${props.decorateConfig?.buttonRadius ?? 8}rpx`);
const vortmallSimpleRadius = computed(() => `${Number(props.decorateConfig?.buttonRadius ?? 5) * 2}rpx`);
const fullBtnStyle = computed(() => {
    if (isVortmallSimpleBar.value) return `border-radius:${vortmallSimpleRadius.value}`;
    return isSquareBar.value ? `border-radius:${btnRadius.value}` : "";
});
// 简约风按钮相互分离；经典风沿用 A2 的连体胶囊半边圆角。
const cartBtnStyle = computed(() => {
    if (isVortmallSimpleBar.value) return `border-radius:${vortmallSimpleRadius.value}`;
    if (isO2oSimpleBar.value) return `border-radius:${btnRadius.value}`;
    if (isOverseasSimpleBar.value) return `border-radius:${btnRadius.value}`;
    if (isOverseasClassicBar.value) return "border-radius:100rpx 0 0 100rpx";
    return isSquareBar.value ? `border-radius:${btnRadius.value} 0 0 ${btnRadius.value}` : "";
});
const buyBtnStyle = computed(() => {
    if (isVortmallSimpleBar.value) return `border-radius:${vortmallSimpleRadius.value}`;
    if (isO2oSimpleBar.value) return `border-radius:${btnRadius.value}`;
    if (isOverseasSimpleBar.value) return `border-radius:${btnRadius.value}`;
    if (isOverseasClassicBar.value) return "border-radius:0 100rpx 100rpx 0";
    return isSquareBar.value ? `border-radius:0 ${btnRadius.value} ${btnRadius.value} 0` : "";
});

const emit = defineEmits<{
    /** 打开规格弹窗（加购 / 立即购买 / 单独购买 / 兑换 / 预约 / 预售）；isQuick=true 表示立即购买（批发态用于区分加车/直购） */
    (e: "showSpecification", isQuick?: boolean): void;
    /** 打开开团规格弹窗 */
    (e: "showGrouponPopup"): void;
    /** 跳转首页 */
    (e: "toHome"): void;
    /** 跳转商品列表 */
    (e: "toProductList"): void;
    /** 跳转购物车 */
    (e: "toCart"): void;
}>();

defineExpose({
    /** 购物车入口位置（加购动画飞入终点） */
    getCartPositionRect: () => getRect(".cart-position"),
    /** 加购按钮位置（加购动画飞出起点） */
    getCartBtnRect: () => getRect(".cart")
});
</script>

<style lang="scss" scoped>
.badgecolor {
    :deep(.uni-badge--error) {
        background-color: var(--general);
    }
}
.bottom-bar-box {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 110rpx;
    background-color: var(--product-content-bg, #fff);
    border-top: 1rpx solid #f5f5f5;
    box-sizing: content-box;
    z-index: 10;
    .bottom-bar {
        height: 110rpx;
        padding: 0 20rpx;
        position: relative;
        justify-content: center;

        .product-status {
            position: absolute;
            top: -60rpx;
            left: 0;
            width: 100%;
            height: 60rpx;
            background-color: #fff7cc;
            color: #f85;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24rpx;

            &.member-restrict-status {
                background-color: #fff0f0;
                color: #ff4d4f;

                .iconfont-h5 {
                    margin-right: 6rpx;
                    font-size: 26rpx;
                }
            }
        }

        .icon-box {
            display: flex;
            align-items: center;
        }

        .btn-box {
            display: flex;
            padding-left: 20rpx;
            flex: 1;
            .other-products-btn {
                flex: 1;
                font-weight: bold;
                color: var(--main-text);
                background-color: var(--main-bg);
                height: 70rpx;
                border-radius: 100rpx;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 28rpx;
                column-gap: 5rpx;
            }
        }
        .label {
            font-size: 20rpx;
            text-align: center;
            width: 80rpx;
            .img {
                width: 35rpx;
                height: 35rpx;
            }
            .label-text {
                padding-top: 5rpx;
                color: #2f2f2f;
                font-weight: 500;
                font-size: 22rpx;
            }
        }
        .btn {
            flex: 1;
            text-align: center;
            line-height: 70rpx;
            height: 70rpx;
            color: #fff;
            font-size: 24rpx;
        }
        .cart {
            background: var(--vice-bg);
            border-radius: 100rpx 0 0 100rpx;
            color: var(--vice-text);
        }
        .buy {
            background: var(--general);
            border-radius: 0 100rpx 100rpx 0;
            color: var(--main-text);
        }
        .btn-restricted {
            opacity: 0.5;
        }
    }
}
.isBuyBtn {
    flex: 1;
    font-weight: 700;
    //color: var(--main-text);
    //background-color: var(--main-bg);
    height: 70rpx;
    border-radius: 100rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    cursor: not-allowed;
}

/* A1 简约风：两个操作按钮分离，并跟随商城主题色。 */
.bottom-bar-box--vortmall-simple {
    .bottom-bar {
        .btn-box {
            column-gap: 16rpx;
        }

        .cart {
            box-sizing: border-box;
            color: var(--general);
            background: #fff;
            border: 3rpx solid var(--general);
        }

        .buy,
        .other-products-btn {
            color: var(--main-text);
            background: var(--general);
        }
    }
}

/* A2 简约风：两个操作按钮分离，圆角由装修配置控制。 */
.bottom-bar-box--o2o-simple {
    .bottom-bar .btn-box {
        column-gap: 16rpx;
    }
}

/* A1 经典风：保持连体胶囊，并跟随商城主题色。 */
.bottom-bar-box--vortmall-classic {
    .bottom-bar {
        .cart {
            color: var(--vice-text);
            background: var(--vice-bg);
            border: 0;
        }

        .buy,
        .other-products-btn {
            color: var(--main-text);
            background: var(--general);
        }
    }
}

/* A3 底栏保留跨境版的整体高度和按钮尺寸。 */
.bottom-bar-box--overseas {
    height: 120rpx;

    .bottom-bar {
        box-sizing: border-box;
        height: 120rpx;
        padding: 0 28rpx 35rpx;

        .btn-box {
            padding-left: 0;
        }

        .btn {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 63rpx;
            font-size: 24.5rpx;
            font-weight: 700;
            line-height: normal;
        }

        .other-products-btn,
        .isBuyBtn {
            height: 63rpx;
            font-size: 24.5rpx;
            font-weight: 700;
        }
    }
}

/* A3 简约风：分离式描边/实色按钮，圆角跟随装修配置。 */
.bottom-bar-box--overseas-simple {
    .bottom-bar {
        .btn-box {
            column-gap: 14rpx;
        }

        .cart {
            color: #1a1a1a;
            background: transparent;
            border: 2px solid #505259;
        }

        .buy {
            color: var(--main-text);
            background: var(--general);
        }
    }
}

/* A3 经典风：与 A2 经典风一致，两个按钮无间距连成胶囊。 */
.bottom-bar-box--overseas-classic {
    .bottom-bar {
        .cart {
            color: var(--vice-text);
            background: var(--vice-bg);
            border: 0;
        }

        .buy {
            color: var(--main-text);
            background: var(--general);
        }
    }
}
</style>
