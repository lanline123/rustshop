<template>
    <view @click="handleShowPopup">
        <slot />
    </view>
    <tig-popup v-model:show="showPopup" :z-index="9999" :show-close="false" @close="handlePopupClose">
        <scroll-view
            scroll-y="true"
            class="specification"
            :class="{
                'specification--a1': isA1Style || isA2Style,
                'specification--overseas': isA3Style
            }"
        >
            <view class="product-sku-box flex justify-between" :class="{ 'product-sku-box--without-quantity': !showQuantitySelector }">
                <view class="product-info">
                    <view class="product-info-img" @click="handlePreviewImg(productImage)">
                        <tig-image :src="productImage" />
                        <view v-if="showOutsaleMask" class="product-status-box">
                            <view class="outsale">{{ $t("已售罄") }}</view>
                        </view>
                    </view>
                    <view class="info">
                        <view class="price">
                            <template v-if="type === 'exchange'">
                                <view class="exchange-box">
                                    <format-price
                                        :font-style="{ fontSize: isDecoratedHeaderStyle ? '36rpx' : '32rpx' }"
                                        :currency-style="{
                                            fontSize: '22rpx'
                                        }"
                                        :decimals-style="{
                                            fontSize: '25rpx'
                                        }"
                                        :price-data="productPrice"
                                    />
                                    <text class="exchange-icon">+</text>
                                    <image class="exchange-img" :src="staticResource('exchange/exchange.png')" />
                                    <text class="exchange-text">{{ exchangeDetail.exchangeIntegral }}</text>
                                </view>
                            </template>
                            <template v-else>
                                <format-price
                                    :font-style="{ fontSize: isDecoratedHeaderStyle ? '36rpx' : '32rpx' }"
                                    :currency-style="{
                                        fontSize: '22rpx'
                                    }"
                                    :decimals-style="{ fontSize: isDecoratedHeaderStyle ? '26rpx' : '24rpx' }"
                                    :price-data="productPrice"
                                />
                            </template>
                        </view>
                        <template v-if="isDiscount || isSeckill || isPresale">
                            <view class="ticket">
                                <view class="ticket-content">{{ isSeckill ? $t("秒杀价") : isPresale ? $t("预售") : $t("限时折扣") }}</view>
                            </view>
                        </template>
                        <template v-if="showOriginPrice">
                            <view class="del-box">
                                <format-price :is-bottom="false" :price-data="productOriginPrice" :font-style="{ 'text-decoration': 'line-through' }" />
                            </view>
                        </template>
                        <view class="name line2">
                            {{ productName }}
                        </view>
                        <view v-if="isDecoratedHeaderStyle && showQuantitySelector" class="sku-num flex align-center">
                            <template v-if="isSeckill && maxProductNumber > 0">
                                <uni-number-box v-model="productNumber" :min="1" :max="maxProductNumber" :width="60" />
                                <view class="stock"
                                    >{{ $t("限购") }}：<text style="color: var(--general)">{{ maxProductNumber }}{{ $t("件") }}</text>
                                </view>
                            </template>
                            <template v-else-if="isGroupon && grouponEnable && maxProductNumber > 0">
                                <tig-number-box v-model="productNumber" :min="1" :max="maxProductNumber" @overlimit="handleOverlimit" />
                                <view class="stock"
                                    >{{ $t("限购") }}：<text style="color: var(--general)">{{ maxProductNumber }}{{ $t("件") }}</text>
                                </view>
                            </template>
                            <template v-else>
                                <tig-number-box v-model="productNumber" :min="1" :max="limitNumber || productStock" @overlimit="handleOverlimit" />
                                <view class="stock"
                                    >{{ $t("库存") }}：<text v-if="productStock > 50" style="color: var(--general)">{{ $t("充足") }}</text>
                                    <text v-else-if="productStock === 0" style="color: var(--general)">{{ $t("缺货") }}</text>
                                    <text v-else style="color: var(--general)">{{ $t("仅剩") }}{{ productStock }}{{ $t("件") }}</text>
                                </view>
                            </template>
                        </view>
                    </view>
                </view>
                <view class="popup-close" @click="closePopup">
                    <image v-if="isDecoratedHeaderStyle" :src="staticResource('order/close.png')" />
                    <text v-else class="iconfont-h5 icon-chacha" style="color: #c8c9cc; font-size: 42rpx" />
                </view>
            </view>
            <template v-if="showBulkPurchaseTab">
                <view class="placeholder" />
                <view class="tab">
                    <view class="tab-item" :class="{ active: tabIndex === 0 }" @click="handleTabChange(0)">
                        {{ $t("单品采购") }}
                        <image v-show="tabIndex === 1" class="image" style="right: 0" :src="staticResource('product/shadow-right.png')" />
                    </view>
                    <view class="tab-item" :class="{ active: tabIndex === 1 }" @click="handleTabChange(1)">
                        {{ $t("批量采购") }}
                        <image v-show="tabIndex === 0" class="image" style="left: 0" :src="staticResource('product/shadow-left.png')" />
                    </view>
                    <view class="line" :style="`${tabIndex === 0 ? 'left: 0' : 'right: 0'}`" />
                    <view class="subline" :style="`${tabIndex === 0 ? 'right: 0' : 'left: 0'}`" />
                </view>
            </template>

            <view v-show="tabIndex !== 1">
                <view v-if="specificationList.length > 0" class="product-sku-info">
                    <view v-for="(item, index) in specificationList" :key="index" class="sku-item">
                        <view class="tit">
                            {{ item.attrName }}
                        </view>
                        <view class="sku-tag flex">
                            <template v-for="(value, subIdx) in item.attrList" :key="String(value.attributesId ?? subIdx)">
                                <view
                                    class="tag"
                                    :class="{
                                        checked: value.checked,
                                        disabled: !value.checked && (Number(value.skuStock) === 0 || value.subCardDisabled || value.notInGroupon),
                                        'tag--image': Boolean(value.attrPicThumb)
                                    }"
                                    @click="handleChecked(index, Number(subIdx))"
                                >
                                    <view v-if="value.attrPicThumb" class="tag-img"><tig-image :src="value.attrPicThumb" /></view>
                                    <view class="tag-text">{{ value.attrValue }}</view>
                                </view>
                            </template>
                        </view>
                    </view>
                </view>
            </view>

            <view v-show="isBatch">
                <template v-if="skuList.length > 1">
                    <view class="allskulist">
                        <template v-for="item in b2bSkuList" :key="item.skuId">
                            <view class="allskulist-item">
                                <view class="left">
                                    <view class="title line2">{{ item.title }}</view>
                                    <!-- <view class="title line2">{{ item.skuValue }}</view> -->
                                    <view class="desc">
                                        <view class="price">
                                            <format-price :price-data="item.price" />
                                        </view>
                                        <view class="line" />
                                        <view class="stock">{{ $t("库存：") }}{{ item.stock }}</view>
                                    </view>
                                </view>
                                <view class="right">
                                    <tig-number-box v-model="item.num" :button-width="30" :min="0" :max="item.skuStock" @change="handleNumberChange()" />
                                </view>
                            </view>
                        </template>
                    </view>
                </template>
            </view>

            <template v-if="additionAttribute.length > 0">
                <view class="product-sku-info attribute">
                    <template v-for="(item, index) in additionAttribute" :key="index">
                        <view class="attribute-item">
                            <view class="item-title">{{ item.attrName }}</view>
                            <view class="item-content">
                                <template v-for="subItem in item.attrList" :key="subItem.attributesId">
                                    <view
                                        class="item-attr"
                                        :class="{ checked: checkedAttrs[index] === subItem.attributesId }"
                                        @click.stop="handleAttrClick(index, subItem.attributesId)"
                                    >
                                        <view class="label">
                                            {{ subItem.attrValue }}
                                        </view>
                                        <format-price class="value" :price-data="subItem.attrPrice" />
                                    </view>
                                </template>
                            </view>
                        </view>
                    </template>
                </view>
            </template>
            <!-- 开团阶梯 如果是阶梯团渲染出来 -->
            <view v-if="isGroupon && grouponEnable && grouponInfo.groupType === 2 && currentSkuGrouponLaders.length" class="product-sku-info">
                <view class="sku-item">
                    <view class="tit">{{ $t("阶梯") }}</view>
                    <view class="sku-tag flex">
                        <template v-for="(lader, index) in currentSkuGrouponLaders" :key="index">
                            <view class="tag" :class="{ checked: currentGroupNumKey === lader.grouponNumKey }" @click="handleSkuLaderChange(lader)">
                                <text class="tag-text">{{ lader.grouponNum }}{{ $t("人团") }}</text>
                            </view>
                        </template>
                    </view>
                </view>
            </view>
        </scroll-view>

        <template v-if="productType === 4 && productIsBuy === 1">
            <view class="footer">
                <view class="exchange-btn">
                    <tig-button class="btn" :disabled="true">{{ $t("商品已购买") }} </tig-button>
                </view>
            </view>
        </template>
        <template v-else>
            <view
                class="footer"
                :class="{
                    shadow: isBatch,
                    'footer--a1-simple': isA1SimpleStyle || isA2Style,
                    'footer--a1-classic': isA1ClassicStyle,
                    'footer--a3-simple': isA3SimpleStyle,
                    'footer--a3-classic': isA3ClassicStyle
                }"
                :style="specificationFooterStyle"
            >
                <template v-if="isBatch">
                    <view class="b2b-btn">
                        <view class="desc">
                            <view
                                >{{ $t("已选") }}<text class="num">{{ productAmount.count }}</text
                                >{{ $t("件") }}</view
                            >
                            <view class="price">
                                <view>{{ $t("商品金额：") }}</view>
                                <format-price
                                    :font-style="{ fontSize: '32rpx', color: 'var(--general)' }"
                                    :currency-style="{
                                        fontSize: '22rpx',
                                        color: 'var(--general)'
                                    }"
                                    :decimals-style="{
                                        fontSize: '25rpx',
                                        color: 'var(--general)'
                                    }"
                                    :price-data="productAmount.total"
                                />
                            </view>
                        </view>
                        <view class="add_cart">
                            <view class="buy-cell">
                                <buy
                                    :id="Number(modelValue)"
                                    :disabled="skuItem.length === 0"
                                    :sku-item="skuItem"
                                    :extra-attr-ids="filterParams.extraAttrIds"
                                    :shop-id="productShopId"
                                    @callback="addCard"
                                >
                                    <view class="btn cart" :class="skuItem.length === 0 ? 'disabled-div' : ''">{{ $t("批量加入购物车") }}</view>
                                </buy>
                            </view>
                            <view class="buy-cell">
                                <buy
                                    :id="Number(modelValue)"
                                    :sku-item="skuItem"
                                    :disabled="skuItem.length === 0"
                                    :extra-attr-ids="filterParams.extraAttrIds"
                                    :is-quick="true"
                                    :shop-id="productShopId"
                                    :check-query-extra="checkQueryExtra"
                                    @callback="closePopup"
                                >
                                    <view class="btn buy-right" :class="skuItem.length === 0 ? 'disabled-div' : ''">{{ $t("立即购买") }}</view>
                                </buy>
                            </view>
                        </view>
                    </view>
                </template>
                <template v-else>
                    <template v-if="type === 'exchange'">
                        <view class="exchange-btn">
                            <buy
                                :id="exchangeDetail.id"
                                :sku-id="currentSku.skuId"
                                :disabled="productStock == 0"
                                :extra-attr-ids="filterParams.extraAttrIds"
                                :number="productNumber"
                                :is-quick="true"
                                :type="type"
                                :shop-id="productShopId"
                                @callback="closePopup"
                            >
                                <view class="btn" :class="productStock === 0 ? 'disabled-div' : ''">{{ $t("立即兑换") }}</view>
                            </buy>
                        </view>
                    </template>
                    <template v-else>
                        <template v-if="productType === 1 || (grouponEnable && isGroupon)">
                            <view v-if="grouponEnable && isGroupon" class="groupon-btn">
                                <buy
                                    :id="Number(modelValue)"
                                    :sku-id="currentSku.skuId"
                                    :disabled="productStock == 0"
                                    :extra-attr-ids="filterParams.extraAttrIds"
                                    :number="productNumber"
                                    :groupon-data="grouponBuyData"
                                    :is-quick="true"
                                    :shop-id="productShopId"
                                    :check-query-extra="checkQueryExtra"
                                    @callback="closePopup"
                                >
                                    <view class="btn" :class="productStock === 0 ? 'disabled-div' : ''">{{ $t("确定") }}</view>
                                </buy>
                            </view>
                            <template v-else-if="isPresale">
                                <view class="exchange-btn">
                                    <buy
                                        :id="Number(modelValue)"
                                        :sku-id="currentSku.skuId"
                                        :disabled="productStock == 0"
                                        :extra-attr-ids="filterParams.extraAttrIds"
                                        :number="productNumber"
                                        :is-quick="true"
                                        :shop-id="productShopId"
                                        :cycle-spec-id="selectedCycleSpecId"
                                        :check-query-extra="checkQueryExtra"
                                        @callback="closePopup"
                                    >
                                        <view class="btn buy-right" :class="productStock === 0 ? 'disabled-div' : ''">
                                            <template v-if="presaleInfo?.saleMode === 2">
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
                                                    :price-data="presaleInfo?.depositAmount || 0"
                                                />
                                                {{ $t("立即预定") }}
                                            </template>
                                            <template v-else>
                                                {{ $t("立即预购") }}
                                            </template>
                                        </view>
                                    </buy>
                                </view>
                            </template>
                            <!-- 二期会员标签：不在人群范围，弹层内加购+购买同样置灰，避免绕过底部限制下单 -->
                            <view v-else-if="memberRestricted" class="add_cart" :class="{ single: isCycleBuy || isSubCard }">
                                <view v-if="!isCycleBuy && !isSubCard" class="btn cart disabled-div">{{ $t("加入购物车") }}</view>
                                <view class="btn buy-right disabled-div">{{ $t("立即购买") }}</view>
                            </view>
                            <view v-else class="add_cart" :class="{ single: isCycleBuy || isSubCard }">
                                <view v-if="!isCycleBuy && !isSubCard" class="buy-cell">
                                    <buy
                                        :id="Number(modelValue)"
                                        :sku-id="currentSku.skuId"
                                        :disabled="productStock == 0"
                                        :extra-attr-ids="filterParams.extraAttrIds"
                                        :number="productNumber"
                                        :shop-id="productShopId"
                                        :cycle-spec-id="selectedCycleSpecId"
                                        @callback="addCard"
                                    >
                                        <view class="btn cart" :class="productStock === 0 ? 'disabled-div' : ''">{{ $t("加入购物车") }}</view>
                                    </buy>
                                </view>
                                <view class="buy-cell">
                                    <buy
                                        :id="Number(modelValue)"
                                        :sku-id="currentSku.skuId"
                                        :disabled="productStock == 0"
                                        :extra-attr-ids="filterParams.extraAttrIds"
                                        :number="productNumber"
                                        :is-quick="true"
                                        :shop-id="productShopId"
                                        :cycle-spec-id="selectedCycleSpecId"
                                        :check-query-extra="checkQueryExtra"
                                        @callback="closePopup"
                                    >
                                        <view class="btn buy-right" :class="productStock === 0 ? 'disabled-div' : ''">{{ $t("立即购买") }}</view>
                                    </buy>
                                </view>
                            </view>
                        </template>
                        <template v-else-if="productType === 5 || productType === 6">
                            <view class="exchange-btn">
                                <buy
                                    :id="modelValue"
                                    :sku-id="currentSku.skuId"
                                    :disabled="reservationSkuStock === 0 && !isSubCardReservationProduct"
                                    :stock="currentSku.skuStock"
                                    :extra-attr-ids="filterParams.extraAttrIds"
                                    :number="productNumber"
                                    :is-quick="true"
                                    :type="type"
                                    :shop-id="productShopId"
                                    :cycle-spec-id="selectedCycleSpecId"
                                    :check-query-extra="checkQueryExtra"
                                    @callback="closePopup"
                                >
                                    <view class="btn" :class="reservationSkuStock === 0 && !isSubCardReservationProduct ? 'disabled-div' : ''">
                                        {{ isSubCardReservationProduct ? $t("立即购买") : $t("立即预约") }}
                                    </view>
                                </buy>
                            </view>
                        </template>
                        <template v-else>
                            <view class="exchange-btn">
                                <buy
                                    :id="modelValue"
                                    :sku-id="currentSku.skuId"
                                    :disabled="productStock == 0"
                                    :extra-attr-ids="filterParams.extraAttrIds"
                                    :number="productNumber"
                                    :is-quick="true"
                                    :type="type"
                                    :shop-id="productShopId"
                                    :cycle-spec-id="selectedCycleSpecId"
                                    :check-query-extra="checkQueryExtra"
                                    @callback="closePopup"
                                >
                                    <view class="btn" :class="productStock === 0 ? 'disabled-div' : ''">{{ $t("立即购买") }}</view>
                                </buy>
                            </view>
                        </template>
                    </template>
                </template>
            </view>
        </template>
    </tig-popup>
</template>

<script setup lang="ts">
/* eslint-disable vue/no-side-effects-in-computed-properties */
import { computed, reactive, ref, watch, type PropType } from "vue";
import type { SkuList, SkuPromotion, ProductAmountItem, GrouponDetail } from "@/types/product/product";
import type { GrouponBuyData, GrouponLaderItem, GrouponSkuItem } from "@/types/groupon/groupon";
import { getProductDetail, getProductSkuDetail, getProductAmount, getBatchProductAvailability } from "@/api/product/product";
import { getExchangeDetail } from "@/api/exchange/exchange";
import buy from "@/components/product/buy.vue";
import { staticResource, isB2B } from "@/utils";
import { isShowB2bPriceText } from "@/utils/b2bPrice";
import { imageFormat } from "@/utils/format";
import { isReservationProductType, isSubCardReservationProduct as checkIsSubCardReservationProduct, shouldShowSoldOut } from "@/utils/productSoldOut";
import { useI18n } from "vue-i18n";
import { useRegionStore } from "@/store/region";
import { useConfigStore } from "@/store/config";

type AnyObject = Record<string, any>;

// 拼团活动详情 sku 信息（后端字段，当前文件仅用到这些）
interface GrouponPromotionItem {
    skuId: number;
    limitNum: number;
    grouponDetailId: number;
    isJoin: number;
    groupPrice?: string;
}

interface DecorateButtonConfig {
    /** A1：1-简约、2-经典；A2：1-经典、2-简约；A3：1-胶囊/经典、2-直角/简约 */
    buyBarStyle?: 1 | 2;
    /** 按钮圆角，单位 rpx */
    buttonRadius?: number;
}

const props = defineProps({
    modelValue: {
        type: [Number],
        default: 0
    },
    skuId: {
        type: [Number],
        default: 0
    },
    type: {
        type: [String],
        default: ""
    },
    shopId: {
        type: [Number],
        default: null
    },
    grouponEnable: {
        // 默认不传的情况是根据当前的产品是否有拼团活动来展示，如果传了false，则走默认的流程
        type: [Boolean],
        default: true
    },
    grouponRecordInfo: {
        // 拼团记录信息
        type: [Object],
        default: () => ({
            grouponPromotionRecordId: 0,
            minGroupNum: 0
        })
    },
    checkQueryExtra: {
        // 立即购买跳结算页时附加的 query（门店自提 tab 锁定配送方式），透传给 buy 组件
        type: String,
        default: ""
    },
    /** 商品详情装修风格：A1 / A2 / A3 */
    layoutStyle: {
        type: String as PropType<"A1" | "A2" | "A3">,
        default: "A2"
    },
    /** 与商品详情底部购买栏共用的按钮装修配置 */
    decorateConfig: {
        type: Object as PropType<DecorateButtonConfig>,
        default: () => ({})
    }
});

const emit = defineEmits(["sendValue", "addCardSucceed", "update:skuId", "loadEnd", "close"]);

const regionStore = useRegionStore();
const configStore = useConfigStore();

const { t } = useI18n();

const isA1Style = computed(() => props.layoutStyle === "A1");
const isA2Style = computed(() => props.layoutStyle === "A2");
const isA3Style = computed(() => props.layoutStyle === "A3");
// 三种装修风格统一使用新版规格弹窗骨架；A2 不再保留旧的小图头部和独立数量行。
const isDecoratedHeaderStyle = computed(() => isA1Style.value || isA2Style.value || isA3Style.value);
const isA1SimpleStyle = computed(() => isA1Style.value && props.decorateConfig.buyBarStyle !== 2);
const isA1ClassicStyle = computed(() => isA1Style.value && props.decorateConfig.buyBarStyle === 2);
const isA3ClassicStyle = computed(() => isA3Style.value && props.decorateConfig.buyBarStyle === 1);
const isA3SimpleStyle = computed(() => isA3Style.value && !isA3ClassicStyle.value);
const configuredButtonRadius = computed(() => {
    const radius = Number(props.decorateConfig.buttonRadius);
    return Number.isFinite(radius) && radius >= 0 ? radius : undefined;
});
const specificationButtonRadius = computed(() => {
    if (isA1SimpleStyle.value) return (configuredButtonRadius.value ?? 5) * 2;
    if (isA2Style.value) return configuredButtonRadius.value ?? 10;
    if (isA3SimpleStyle.value) return configuredButtonRadius.value ?? 6;
    return 100;
});
const specificationFooterStyle = computed<Record<string, string>>(() => ({
    "--spec-button-radius": `${specificationButtonRadius.value}rpx`
}));

const tabIndex = ref(0);

const showPopup = ref(false);

const closePopup = () => {
    emit("close");
    sendValue();
    showPopup.value = false;
};

const handleShowPopup = () => {
    showPopup.value = true;
};

const handlePreviewImg = (image: string) => {
    if (!image) return;

    let img = imageFormat(image);

    uni.previewImage({
        urls: [img],
        current: img
    });
};

const specificationList = ref<any[]>([]);
const skuList = ref<SkuList[]>([]);
const b2bSkuList = ref<SkuList[]>([]);
const currentSku = ref<SkuList>({} as SkuList);
const productPrice = ref("0");
const productOriginPrice = ref("0");
const productImage = ref("");
const picUrl = ref(""); // 商品图片
const productStock = ref(0);
const productName = ref("");
const productNumber = ref(1);
const maxProductNumber = ref(0);
const checkedValue = ref<any[]>([]);
/** 各规格组当前选中的 attributesId，用于同名规格值区分 */
const checkedAttrIds = ref<(number | null)[]>([]);
const exchangeDetail = ref<any>({});
const productType = ref(1);
const productIsBuy = ref(0);
const additionAttribute = ref<any[]>([]);
const checkedAttrs = ref<any[]>([]);
const productShopId = ref(0);
const isGroupon = ref(0);
const currentGroupNumKey = ref(0);
const isPresale = ref<number>(0);
// 预售信息（saleMode>0 时存在；定金预售 depositAmount 会随 SKU 联动）
const presaleInfo = ref<any>({});

// 是否是周期购
const isCycleBuy = defineModel<boolean>("isCycleBuy", { default: false });
// 是否是次卡
const isSubCard = defineModel<boolean>("isSubCard", { default: false });
// 商品限购
const limitNumber = ref(0);

/** 预售、周期购、预约到店/上门不参与批量采购 */
const showBulkPurchaseTab = computed(
    () =>
        skuList.value.length > 1 &&
        configStore.bulkPurchase === 1 &&
        productType.value !== 4 &&
        props.type !== "exchange" &&
        !(Number(presaleInfo.value?.saleMode) > 0) &&
        !isCycleBuy.value &&
        !isSubCard.value &&
        productType.value !== 5 &&
        productType.value !== 6
);
// 预约到店/上门商品，以及未配置头部数量控件的风格，不显示数量选择器。
const showQuantitySelector = computed(() => isDecoratedHeaderStyle.value && productType.value !== 5 && productType.value !== 6);
const batchSkuIds = computed(() => skuList.value.map((item: SkuList) => item.skuId).join(","));
const loadedBatchSkuIds = ref("");
const batchAvailabilityLoading = ref(false);
watch([showBulkPurchaseTab, batchSkuIds], ([ok, skuIds]) => {
    if (!ok) {
        tabIndex.value = 0;
        b2bSkuList.value = [];
        skuItem.value = [];
        productAmount.value = {
            count: 0,
            total: "0"
        } as ProductAmountItem;
        loadedBatchSkuIds.value = "";
        batchAvailabilityLoading.value = false;
        return;
    }
    if (skuIds) {
        getBatchProductAvailabilityList();
    }
});



const buildSkuAttrNameValue = (attrName: string, attrValue: string) => `${attrName}:${attrValue}`;

const parseSkuAttrNameValue = (value: string) => {
    const separatorIndex = String(value || "").indexOf(":");
    if (separatorIndex < 0) {
        return undefined;
    }

    return {
        attrName: value.slice(0, separatorIndex),
        attrValue: value.slice(separatorIndex + 1)
    };
};

const getSkuAttrMap = (skuItem?: Partial<SkuList>) => {
    const skuAttrMap = new Map<string, string>();
    const skuData = Array.isArray(skuItem?.skuData) ? skuItem.skuData : [];

    if (skuData.length > 0) {
        skuData.forEach((item: any) => {
            if (!item?.name) return;
            skuAttrMap.set(String(item.name), buildSkuAttrNameValue(String(item.name), String(item.value ?? "")));
        });
        return skuAttrMap;
    }

    String(skuItem?.skuValue || "")
        .split("|")
        .filter((item) => !!item)
        .forEach((item) => {
            const parsed = parseSkuAttrNameValue(item);
            if (!parsed) return;
            skuAttrMap.set(parsed.attrName, buildSkuAttrNameValue(parsed.attrName, parsed.attrValue));
        });

    return skuAttrMap;
};

const getOrderedSkuAttrValues = (skuItem?: Partial<SkuList>) => {
    const skuAttrMap = getSkuAttrMap(skuItem);
    return specificationList.value.map((item: any) => skuAttrMap.get(String(item.attrName)) || "");
};

const getCheckedValueBySku = (skuItem?: Partial<SkuList>) => {
    const skuAttrMap = getSkuAttrMap(skuItem);
    return specificationList.value.map((item: any) => skuAttrMap.get(String(item.attrName)) || "");
};

/**
 * 规格 attributesId 组合 ↔ SKU 映射。
 * 同名规格值导致 skuValue 重复时，按笛卡尔积顺序与 SKU 列表出现顺序一一对应。
 */
const skuSelectionMaps = computed(() => {
    const buckets = new Map<string, SkuList[]>();
    skuList.value.forEach((skuItem: SkuList) => {
        const orderedTokens = getOrderedSkuAttrValues(skuItem);
        if (orderedTokens.length === 0 || orderedTokens.some((token) => !token)) return;
        const skuKey = orderedTokens.join("|");
        const bucket = buckets.get(skuKey);
        if (bucket) bucket.push(skuItem);
        else buckets.set(skuKey, [skuItem]);
    });

    const matchCursor = new Map<string, number>();
    const consumeFromBucket = (skuKey: string) => {
        const bucket = buckets.get(skuKey);
        if (!bucket || bucket.length === 0) return undefined;
        const pos = matchCursor.get(skuKey) ?? 0;
        matchCursor.set(skuKey, pos + 1);
        return bucket[Math.min(pos, bucket.length - 1)];
    };

    const map = new Map<string, SkuList>();
    const attrIdsBySkuId = new Map<number, number[]>();

    const walk = (groupIndex: number, selectedAttrs: any[], tokens: string[]) => {
        if (groupIndex >= specificationList.value.length) {
            if (tokens.length === 0) return;
            const sku = consumeFromBucket(tokens.join("|"));
            if (!sku) return;
            const attrIds = selectedAttrs.map((attr) => Number(attr.attributesId));
            map.set(attrIds.join("|"), sku);
            attrIdsBySkuId.set(Number(sku.skuId), attrIds);
            return;
        }

        const group = specificationList.value[groupIndex];
        const attrList = Array.isArray(group?.attrList) ? group.attrList : [];
        if (attrList.length === 0) {
            walk(groupIndex + 1, selectedAttrs, tokens);
            return;
        }

        attrList.forEach((attr: any) => {
            walk(groupIndex + 1, [...selectedAttrs, attr], [...tokens, buildSkuAttrNameValue(String(attr.attrName), String(attr.attrValue))]);
        });
    };

    if (specificationList.value.length > 0) {
        walk(0, [], []);
    }

    return { map, attrIdsBySkuId };
});

const findSkuByCheckedValue = (selectedValues: string[]) => {
    const validSelectedValues = selectedValues.filter((item) => !!item);
    if (validSelectedValues.length === 0) {
        return undefined;
    }

    return skuList.value.find((skuItem: SkuList) => {
        const skuAttrValues = Array.from(getSkuAttrMap(skuItem).values());
        if (skuAttrValues.length !== validSelectedValues.length) {
            return false;
        }

        const skuAttrSet = new Set(skuAttrValues);
        return validSelectedValues.every((item) => skuAttrSet.has(item));
    });
};

const findSkuByCheckedAttrIds = (selectedAttrIds: (number | null)[], selectedValues?: string[]) => {
    const validAttrIds = selectedAttrIds.filter((id): id is number => id != null && Number(id) > 0);
    if (validAttrIds.length > 0 && validAttrIds.length === specificationList.value.length) {
        const matched = skuSelectionMaps.value.map.get(validAttrIds.join("|"));
        if (matched) return matched;
    }

    return findSkuByCheckedValue(selectedValues || checkedValue.value);
};

const getCheckedAttrIdsBySku = (skuItem?: Partial<SkuList>) => {
    if (!skuItem?.skuId) {
        return specificationList.value.map(() => null);
    }

    const mappedIds = skuSelectionMaps.value.attrIdsBySkuId.get(Number(skuItem.skuId));
    if (mappedIds && mappedIds.length === specificationList.value.length) {
        return mappedIds;
    }

    // 回退：按名称匹配各组第一个同名规格值
    const checkedTokens = getCheckedValueBySku(skuItem);
    return specificationList.value.map((group: any, index: number) => {
        const token = checkedTokens[index];
        const attr = (group.attrList || []).find((item: any) => buildSkuAttrNameValue(String(item.attrName), String(item.attrValue)) === token);
        return attr?.attributesId != null ? Number(attr.attributesId) : null;
    });
};

/** 根据当前已选规格，同步主图（任意规格组有图则取第一张有图的） */
const syncProductImageByChecked = () => {
    for (let i = 0; i < specificationList.value.length; i++) {
        const group = specificationList.value[i];
        if (!group?.attrList) continue;
        const attrId = checkedAttrIds.value[i];
        const attr =
            attrId != null
                ? group.attrList.find((subItem: any) => Number(subItem.attributesId) === Number(attrId))
                : group.attrList.find((subItem: any) => buildSkuAttrNameValue(String(subItem.attrName), String(subItem.attrValue)) === checkedValue.value[i]);
        if (attr?.attrPic) {
            productImage.value = attr.attrPic;
            return;
        }
    }
};

const applyDefaultSkuSelection = (skuItem?: SkuList) => {
    if (!skuItem) return;
    checkedValue.value = getCheckedValueBySku(skuItem);
    checkedAttrIds.value = getCheckedAttrIdsBySku(skuItem);
    syncProductImageByChecked();
};


// 拼团活动详细信息
const grouponInfo = ref<GrouponDetail>({
    productId: 0, // 商品ID
    grouponPromotionId: 0, // 拼团活动 ID
    grouponPromotionItemId: 0, // 拼团活动商品 ID
    grouponDetailId: 0
});
const grouponBuyData = ref<GrouponBuyData>({
    grouponPromotionId: 0,
    grouponPromotionItemId: 0,
    groupNumKey: 0,
    grouponDetailId: 0,
    price: 0,
    grouponPromotionRecordId: 0
});
// 拼团阶梯信息
const grouponSkus = ref<GrouponSkuItem[]>([]);
// 当前活动中可参团的 SKU id 集合（基于接口返回的 grouponPromotionDetails.isJoin）
const joinableSkuIds = computed<Set<number>>(() => {
    const details = grouponInfo.value?.grouponPromotionDetails;
    if (!details || !details.length) return new Set<number>();
    return new Set<number>(details.filter((d: any) => d.isJoin === 1).map((d: any) => d.skuId));
});
// 是否存在可参团的拼团活动（仅看接口数据，与 grouponEnable 无关）
// 用于默认选中规格：即使是从「加入购物车 / 选规格」入口打开（grouponEnable=false），
// 仍然按拼团最低价的 SKU 作为默认值
const hasJoinableGroupon = computed(() => {
    return !!(grouponInfo.value?.grouponPromotionItemId && joinableSkuIds.value.size > 0);
});
// 当前是否处于"拼团模式"：拼团开关开启 + 存在可参团活动
// 用于禁用未参团规格、拦截非法点击等
const isGrouponActive = computed(() => {
    return !!(props.grouponEnable && hasJoinableGroupon.value);
});
// 拼团活动中价格最低的阶梯所在的可参团 SKU id（用于默认选中该规格）
const cheapestJoinableSkuId = computed<number | null>(() => {
    const details = grouponInfo.value?.grouponPromotionDetails;
    if (!details || !details.length) return null;
    let best: { skuId: number; price: number } | null = null;
    for (const item of details as any[]) {
        if (item.isJoin !== 1 || !item.groupPrice) continue;
        let prices: any[] = [];
        try {
            prices = JSON.parse(item.groupPrice);
        } catch {
            continue;
        }
        for (const p of prices) {
            const price = Number(p.price);
            if (Number.isNaN(price)) continue;
            if (!best || price < best.price) {
                best = { skuId: item.skuId, price };
            }
        }
    }
    return best?.skuId ?? null;
});
// 当前sku的拼团信息
const currentSkuGroupon = computed<GrouponSkuItem | undefined>(() => {
    if (!isGroupon.value) {
        return undefined;
    }
    if (!grouponSkus.value) {
        return undefined;
    }
    const currentSkuId = currentSku.value?.skuId || 0;
    const res = grouponSkus.value.find((item) => item.skuId === currentSkuId);
    grouponBuyData.value.grouponDetailId = res?.grouponDetailId || 0;
    return res || undefined;
});
// 当前sku的拼团阶梯信息
const currentSkuGrouponLaders = computed<GrouponLaderItem[]>(() => {
    if (!isGroupon.value) {
        return [];
    }
    if (!currentSkuGroupon.value) {
        return [];
    }
    // 设置当前库存
    productStock.value = currentSkuGroupon.value.limitNum || 0;
    // 拿到当前阶梯
    const currentLader = currentSkuGroupon.value.laders.find((x) => x.grouponNumKey === currentGroupNumKey.value);
    productPrice.value = currentLader?.price as string;
    grouponInfo.value.grouponPrice = currentLader?.price as string;
    // 设置当前拼团活动详情id
    grouponBuyData.value.grouponDetailId = currentSkuGroupon.value.grouponDetailId;
    // 如果传入的拼团记录id，则不显示拼团阶梯
    if (props.grouponRecordInfo.grouponPromotionRecordId) {
        return currentLader ? [currentLader] : [];
    } else {
        return currentSkuGroupon.value.laders || [];
    }
});

const handleAttrClick = (index: number, id: number) => {
    if (checkedAttrs.value[index] && checkedAttrs.value[index] === id) {
        checkedAttrs.value[index] = null;
    } else {
        checkedAttrs.value[index] = id;
    }
    loadSkuPrice();
};

const __getProductDetail = async () => {
    try {
        let result: any = {};

        const params: any = {
            id: props.modelValue,
            shopId: null,
            nearestShopId: null
        };
        if (props.shopId === 0 || props.shopId) {
            params.shopId = props.shopId;
        } else {
            params.nearestShopId = regionStore.nearestShopInfo.shopId || null;
        }

        switch (props.type) {
            case "exchange":
                result = await getExchangeDetail(params);
                exchangeDetail.value = result.exchangeInfo;
                productStock.value = result.exchangeInfo.productStock;
                productPrice.value = exchangeDetail.value.discountsPrice || "0";
                productImage.value = exchangeDetail.value.picUrl || "";
                break;
            default:
                result = await getProductDetail(params);
                productStock.value = result.item.productStock;
                productPrice.value = result.item.productPrice || "0";
                picUrl.value = result.item.picUrl || "";
                productImage.value = result.item.picUrl || "";
                grouponInfo.value = result.grouponDetail;
                limitNumber.value = result.item.limitNumber || 0; // 商品限购
        }
        productType.value = result.item.productType;
        productIsBuy.value = result.item.isBuy;
        productName.value = result.item.productName || "";
        // 预售信息
        presaleInfo.value = {};
        if (result.item.saleMode && Number(result.item.saleMode) > 0) {
            isPresale.value = 1;
            presaleInfo.value.saleMode = Number(result.item.saleMode);
            if (result.item.presaleInfo) {
                presaleInfo.value.depositAmount = Number(result.item.presaleInfo.depositAmount ?? 0);
            }
        }
        specificationList.value = result.attrList.spe;
        additionAttribute.value = result.attrList.extra;
        skuList.value = result.skuList;
        productShopId.value = result.shopId || 0;

        setDefaultValue();
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
        emit("loadEnd");
    }
};

const reset = () => {
    productNumber.value = 1;
    checkedValue.value = [];
    checkedAttrIds.value = [];
    currentSku.value = {} as SkuList;
    checkedAttrs.value = [];
    tabIndex.value = 0;
    isGroupon.value = 0;
    presaleInfo.value = {};
    grouponInfo.value = {
        productId: 0,
        grouponPromotionId: 0,
        grouponPromotionItemId: 0,
        grouponDetailId: 0
    };
    grouponBuyData.value = {
        grouponPromotionId: 0,
        grouponPromotionItemId: 0,
        groupNumKey: 0,
        grouponDetailId: 0,
        price: 0,
        grouponPromotionRecordId: 0
    };
    grouponSkus.value = [];
    b2bSkuList.value = [];
    loadedBatchSkuIds.value = "";
    batchAvailabilityLoading.value = false;
    clearMarketingState();
};

const firstLoad = ref(false);
// 加载拼团信息
const loadGrouponInfo = () => {
    if (props.type) {
        firstLoad.value = false;
        emit("loadEnd");
        return;
    }

    if (!props.modelValue) {
        return;
    }

    // 如果产品库存为0
    if (productStock.value === 0) {
        return;
    }

    try {
        if (firstLoad.value) {
            // 拼团的产品价格就是原价
            productOriginPrice.value = productPrice.value;
            // 看是否有拼团价格
            if (grouponInfo.value.grouponPromotionDetails) {
                grouponBuyData.value.grouponPromotionId = grouponInfo.value.grouponPromotionId;
                grouponBuyData.value.grouponPromotionItemId = grouponInfo.value.grouponPromotionItemId;
                grouponSkus.value = grouponInfo.value.grouponPromotionDetails.map((skuItem: GrouponPromotionItem) => {
                    let skuGroupPrice;
                    // 拼团的阶梯信息
                    const grouponSkuItem: GrouponSkuItem = {
                        skuId: skuItem.skuId,
                        limitNum: skuItem.limitNum,
                        grouponDetailId: skuItem.grouponDetailId,
                        isJoin: skuItem.isJoin,
                        laders: []
                    };
                    // 拿到拼团价
                    if (skuItem.groupPrice) {
                        skuGroupPrice = JSON.parse(skuItem.groupPrice);
                    }
                    if (skuGroupPrice && skuGroupPrice.length) {
                        //  循环处理阶梯价格
                        grouponSkuItem.laders = skuGroupPrice.map((skuGroupPriceItem: AnyObject) => {
                            // 根据grouponNumKey找到拼团人数
                            const laderGrouponNum = grouponInfo.value.groupNums?.find((item: any) => item.groupNumKey === skuGroupPriceItem.groupNumKey);
                            return {
                                grouponNumKey: skuGroupPriceItem.groupNumKey,
                                grouponNum: laderGrouponNum?.groupNum,
                                price: skuGroupPriceItem.price
                            };
                        });
                    }
                    return grouponSkuItem;
                });
                // 兜底：用所有可参团阶梯里价格最低的那个，回填 currentGroupNum / grouponPrice，
                // 避免后续未选中可参团 SKU 时拼团标题展示为空
                const joinableLaders = grouponSkus.value.filter((sku) => sku.isJoin === 1).flatMap((sku) => sku.laders || []);
                if (joinableLaders.length > 0) {
                    const defaultLader = joinableLaders.reduce((min: any, item: any) => {
                        if (!min || (item.price !== undefined && Number(item.price) < Number(min.price))) {
                            return item;
                        }
                        return min;
                    }, null);
                    if (defaultLader) {
                        if (grouponInfo.value.currentGroupNum == null) {
                            grouponInfo.value.currentGroupNum = defaultLader.grouponNum;
                        }
                        if (grouponInfo.value.grouponPrice == null) {
                            grouponInfo.value.grouponPrice = defaultLader.price as string;
                        }
                    }
                }
            }
        }

        // 如果当前商品有参加拼团活动，则修改价格和库存
        if (grouponSkus.value && grouponSkus.value.length) {
            // 判断是否有参加拼团的信息
            const hasJoinGroupInfo = grouponSkus.value.some((item) => item.isJoin);
            if (!hasJoinGroupInfo) {
                // 没有参加拼团，则走默认的sku价格
                isGroupon.value = 0;
                loadSkuPrice();
                return;
            }
            const currentSkuId = currentSku.value?.skuId || 0;
            // 拿到当前sku的拼团的库存
            const currentGrouponSku = grouponSkus.value.filter((item: any) => item.skuId === currentSkuId)[0];
            // 有当前sku的拼团信息，且当前sku有参团
            if (currentGrouponSku && currentGrouponSku.isJoin === 1) {
                // 如果库存小于产品库存，则修改库存
                if (currentGrouponSku.limitNum < productStock.value) {
                    productStock.value = currentGrouponSku.limitNum;
                }
                // 如果有传递拼团的信息进来
                let currentLader;
                if (props.grouponRecordInfo.minGroupNum) {
                    currentLader = currentGrouponSku.laders.find((x) => x.grouponNum === props.grouponRecordInfo.minGroupNum);
                } else {
                    // 默认选中价格最低的阶梯
                    currentLader = currentGrouponSku.laders.reduce((minLader: GrouponLaderItem | null, item: GrouponLaderItem) => {
                        const itemPrice = Number(item?.price);
                        if (Number.isNaN(itemPrice)) {
                            return minLader;
                        }
                        if (!minLader) {
                            return item;
                        }
                        const minPrice = Number(minLader.price);
                        if (Number.isNaN(minPrice) || itemPrice < minPrice) {
                            return item;
                        }
                        return minLader;
                    }, null);
                }
                // 有拿到阶梯信息
                if (currentLader) {
                    isGroupon.value = 1;
                    currentGroupNumKey.value = currentLader.grouponNumKey;
                    productPrice.value = currentLader.price as string;
                    grouponInfo.value.grouponPrice = currentLader.price as string;
                    grouponBuyData.value.groupNumKey = currentGroupNumKey.value;
                    grouponBuyData.value.price = currentLader.price as number;
                    grouponBuyData.value.grouponDetailId = currentGrouponSku.grouponDetailId;
                    grouponInfo.value.currentGroupNum = currentLader.grouponNum;
                    // 如果当前传了拼团的值
                    if (props.grouponRecordInfo.grouponPromotionRecordId) {
                        grouponBuyData.value.grouponPromotionRecordId = props.grouponRecordInfo.grouponPromotionRecordId;
                    }
                    // 如果有设置限购
                    if (grouponInfo.value.orderLimitNum && grouponInfo.value.orderLimitNum > 0) {
                        maxProductNumber.value = grouponInfo.value.orderLimitNum > productStock.value ? productStock.value : grouponInfo.value.orderLimitNum;
                    }
                    if (checkedValue.value.length > 0) {
                        skuStr.value = checkedValue.value.map((item: any) => item.split(":")[1]).join(" ");
                    }
                } else {
                    // 没有参加拼团，则走默认的sku价格
                    isGroupon.value = 0;
                    loadSkuPrice();
                }
            } else {
                // 没有参加拼团，则走默认的sku价格
                isGroupon.value = 0;
                loadSkuPrice();
            }
            if (firstLoad.value) {
                sendValue();
            }
        } else {
            // 没有参加拼团，则走默认的sku价格
            isGroupon.value = 0;
            loadSkuPrice();
        }
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    } finally {
        firstLoad.value = false;
        emit("loadEnd");
    }
};

// 处理拼团阶梯变更事件
const handleSkuLaderChange = (lader: GrouponLaderItem) => {
    currentGroupNumKey.value = lader.grouponNumKey;
    productPrice.value = lader.price as string;
    // 设置拼团下单信息
    grouponBuyData.value.groupNumKey = currentGroupNumKey.value;
    grouponBuyData.value.price = lader.price as number;
    grouponInfo.value.currentGroupNum = lader.grouponNum;
    sendValue();
};

const refresh = () => {
    if (typeof props.modelValue === "number" && props.modelValue > -1) {
        reset();
        firstLoad.value = true;
        __getProductDetail();
    }
};

watch(
    () => props.modelValue,
    () => {
        if (!props.modelValue) return;
        refresh();
    },
    {
        immediate: true
    }
);

watch(
    () => props.grouponEnable,
    (_newValue) => {
        setDefaultValue();
    }
);

watch(
    () => props.grouponRecordInfo.grouponPromotionRecordId,
    (_newValue) => {
        setDefaultValue();
    }
);

const setDefaultValue = () => {
    if (specificationList.value.length > 0) {
        if (props.skuId > 0) {
            const skuItem = skuList.value.find((item: SkuList) => item.skuId === props.skuId);
            if (skuItem) {
                applyDefaultSkuSelection(skuItem);
            } else {
                // 历史订单中的 SKU 可能已不在当前可用规格列表中，回退到默认可用规格。
                checkedValue.value = [];
                checkedAttrIds.value = specificationList.value.map(() => null);
            }
        }
        if (checkedValue.value.length === 0) {
            // 优先按拼团最低价 SKU 选中（无论是否处于拼团入口，只要存在可参团活动）
            let defaultSkuItem: SkuList | undefined;
            if (hasJoinableGroupon.value) {
                if (cheapestJoinableSkuId.value != null) {
                    defaultSkuItem = skuList.value.find((item: SkuList) => item.skuId === cheapestJoinableSkuId.value);
                }
                if (!defaultSkuItem) {
                    defaultSkuItem = skuList.value.find((item: SkuList) => joinableSkuIds.value.has(item.skuId));
                }
            }
            // 普通多规格默认选中「有库存 SKU 中 skuPrice 最低的」，让详情页首屏显示规格最低价
            if (!defaultSkuItem) {
                const inStockSkus = skuList.value.filter((item: SkuList) => Number(item.skuStock ?? 0) > 0);
                if (inStockSkus.length > 0) {
                    defaultSkuItem = inStockSkus.reduce((min, item) => {
                        const itemPrice = Number(item.skuPrice);
                        const minPrice = Number(min.skuPrice);
                        if (!Number.isFinite(itemPrice)) return min;
                        if (!Number.isFinite(minPrice)) return item;
                        return itemPrice < minPrice ? item : min;
                    });
                }
            }
            let index = defaultSkuItem
                ? skuList.value.findIndex((item: any) => item.skuId === defaultSkuItem!.skuId)
                : skuList.value.findIndex((item: any) => item.skuStock > 0);
            index = index === -1 ? 0 : index;
            applyDefaultSkuSelection(skuList.value[index]);
        }
        // 防御性纠正：即便 props.skuId 指向一个未参团 SKU，也强制切到最便宜的可参团 SKU
        if (hasJoinableGroupon.value) {
            const currentSkuItem = findSkuByCheckedAttrIds(checkedAttrIds.value, checkedValue.value);
            const currentIsJoinable = currentSkuItem && joinableSkuIds.value.has(currentSkuItem.skuId);
            if (!currentIsJoinable && cheapestJoinableSkuId.value != null) {
                const target = skuList.value.find((s: SkuList) => s.skuId === cheapestJoinableSkuId.value);
                if (target) applyDefaultSkuSelection(target);
            }
        }
        updateValue();
    } else if (skuList.value.length > 0) {
        currentSku.value = skuList.value[0];
    }
    // 启用了拼团且有拼团活动
    if ((props.grouponEnable || firstLoad.value) && grouponInfo.value && grouponInfo.value.grouponPromotionItemId) {
        loadGrouponInfo();
    } else {
        loadSkuPrice();
    }
};
const handleChecked = async (index: number, subIdx: number) => {
    const checkedItem = specificationList.value[index].attrList[subIdx];
    if (!checkedItem || Number(checkedItem.skuStock) === 0 || checkedItem.subCardDisabled) {
        return;
    }
    if (checkedItem.notInGroupon) {
        uni.showToast({ title: t("该规格暂不参与本次拼团"), icon: "none" });
        return;
    }
    if (checkedItem.attrPic) {
        productImage.value = checkedItem.attrPic;
    } else if (index === 0) {
        productImage.value = picUrl.value;
    }
    checkedValue.value[index] = checkedItem.attrName + ":" + checkedItem.attrValue;
    checkedAttrIds.value[index] = checkedItem.attributesId != null ? Number(checkedItem.attributesId) : null;
    updateValue();
    // 切换 SKU 时先清理营销态，避免旧状态造成价格标签闪烁
    clearMarketingState();


    // 不启用拼团走默认逻辑
    if (!props.grouponEnable) {
        loadSkuPrice();
    } else {
        loadGrouponInfo();
    }
};

const updateValue = () => {
    const grouponActive = isGrouponActive.value;
    const joinSet = joinableSkuIds.value;
    specificationList.value.forEach((item: any, index: number) => {
        const checkedAttrId = checkedAttrIds.value[index];
        item.attrList.forEach((value: any) => {
            const attrNameValue = `${value.attrName}:${value.attrValue}`;
            const attrId = value.attributesId != null ? Number(value.attributesId) : null;

            // 优先按 attributesId 判断选中，避免同名规格值同时高亮
            value.checked = attrId != null && checkedAttrId != null ? checkedAttrId === attrId : checkedValue.value[index] === attrNameValue;

            const trialAttrIds = [...checkedAttrIds.value];
            const trialValues = [...checkedValue.value];
            trialAttrIds[index] = attrId;
            trialValues[index] = attrNameValue;
            const result = findSkuByCheckedAttrIds(trialAttrIds, trialValues);

            value.skuStock = result ? result.skuStock : 0;
            value.subCardDisabled = false;
            // 拼团模式下，该规格组合不在可参团 SKU 集合中即视为不可选
            value.notInGroupon = grouponActive && (!result || !joinSet.has(result.skuId));
        });
    });

    const selectedItem: any = findSkuByCheckedAttrIds(checkedAttrIds.value, checkedValue.value);
    if (selectedItem) {
        productOriginPrice.value = selectedItem.marketPrice || selectedItem.skuPrice;
        currentSku.value = selectedItem;
        // 预约商品先用规格库存；普通商品等可用性接口返回后再更新
        if (productType.value === 5 || productType.value === 6) {
            productStock.value = Number(selectedItem.skuStock ?? 0);
        }
    }
};

interface FilterParams {
    id: number;
    skuId?: number | null;
    extraAttrIds?: string;
    shopId?: number;
}
const filterParams = reactive<FilterParams>({
    id: 0
});

const skuStr = ref("");
const isSeckill = ref(0);
const promotionList = ref<SkuPromotion[]>([]);

const isDiscount = ref(0);
const showOriginPrice = computed(() => {
    // 预约商品不展示划线价，避免 SKU 切换时出现多余闪烁
    if (productType.value === 5 || productType.value === 6) return false;
    if (Number(configStore.showMarketprice) !== 1) return false;
    if (isShowB2bPriceText()) return false;
    return !!productOriginPrice.value && Number(productOriginPrice.value) > Number(productPrice.value);
});
const reservationSkuStock = computed(() => Number(currentSku.value?.skuStock ?? productStock.value ?? 0));

// 是否是次卡商品，并且是预约商品
const isSubCardReservationProduct = computed(() => {
    return checkIsSubCardReservationProduct({ productType: productType.value }, isSubCard.value);
});

const showOutsaleMask = computed(() => {
    return shouldShowSoldOut(
        {
            productType: productType.value,
            productStock: productStock.value,
            reservationSkuStock: reservationSkuStock.value
        },
        {
            stockField: isReservationProductType(productType.value) ? "reservationSkuStock" : "productStock",
            isSubCard: isSubCard.value
        }
    );
});

const clearMarketingState = () => {
    isSeckill.value = 0;
    isDiscount.value = 0;
    maxProductNumber.value = 0;
    seckillValue.value = {
        seckillEndTime: "",
        seckillSales: 0,
        seckillStock: 0,
        canJoin: true,
        restrictTip: ""
    };
    discountValue.value = {
        discountEndTime: "",
        discountPrice: 0,
        canJoin: true,
        restrictTip: ""
    };
    promotionList.value = [];
};

const loadSkuPrice = async () => {
    if (props.type) {
        firstLoad.value = false;
        emit("loadEnd");
        return;
    }

    if (!props.modelValue) {
        return;
    }

    filterParams.id = props.modelValue;
    filterParams.skuId = currentSku.value.skuId || null;
    filterParams.extraAttrIds = checkedAttrs.value.length > 0 ? checkedAttrs.value.filter((item) => !!item).join(",") : "";
    if (productShopId.value > -1) {
        filterParams.shopId = productShopId.value;
    }

    try {
        const result = await getProductSkuDetail(filterParams);
        productPrice.value = result.price;
        const skuMarketPrice = result?.sku?.marketPrice ?? result?.marketPrice;
        if (skuMarketPrice !== undefined && skuMarketPrice !== null && skuMarketPrice !== "") {
            productOriginPrice.value = String(skuMarketPrice);
        } else if (result.originPrice) {
            productOriginPrice.value = String(result.originPrice);
        }
        const apiStock = Number(result.stock);
        const selectedSkuStock = Number(currentSku.value?.skuStock);
        // 可售库存以可用性接口的 stock 为准；预约商品使用规格库存
        if (productType.value === 5 || productType.value === 6) {
            if (Number.isFinite(selectedSkuStock)) {
                productStock.value = selectedSkuStock;
            } else if (Number.isFinite(apiStock)) {
                productStock.value = apiStock;
            } else {
                productStock.value = 0;
            }
        } else {
            productStock.value = Number.isFinite(apiStock) ? apiStock : 0;
            if (currentSku.value) {
                currentSku.value.skuStock = productStock.value;
            }
            const matchedSku = skuList.value.find((item: SkuList) => Number(item.skuId) === Number(currentSku.value?.skuId));
            if (matchedSku) {
                matchedSku.skuStock = productStock.value;
            }
        }
        // SKU 联动定金（仅定金预售场景后端返回）
        if ((result as any)?.depositAmount !== undefined && (result as any)?.depositAmount !== null) {
            presaleInfo.value.depositAmount = Number((result as any).depositAmount);
        }
        if ((result as any)?.saleMode && Number((result as any).saleMode) > 0) {
            presaleInfo.value.saleMode = Number((result as any).saleMode);
        }
        if (checkedValue.value.length > 0) {
            skuStr.value = checkedValue.value.map((item: any) => item.split(":")[1]).join(" ");
        }

        // 是否存在营销活动
        promotionManage(result?.promotion || []);

        if (isSeckill.value && maxProductNumber.value > 0) {
            productNumber.value = productNumber.value > maxProductNumber.value ? maxProductNumber.value : productNumber.value;
        } else if (productStock.value === 0) {
            productNumber.value = 1;
        } else {
            productNumber.value = productNumber.value > productStock.value ? productStock.value : productNumber.value;
        }

        if (firstLoad.value) {
            sendValue();
        }
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    } finally {
        firstLoad.value = false;
        emit("loadEnd");
    }
};

const seckillValue = ref({
    seckillEndTime: "",
    seckillSales: 0,
    seckillStock: 0,
    // 二期会员标签（D1096）：false 时秒杀正常展示但置灰购买并提示
    canJoin: true,
    restrictTip: ""
});
const discountValue = ref({
    discountEndTime: "",
    discountPrice: 0,
    // false 时不享受限时折扣，按普通价格购买
    canJoin: true,
    restrictTip: ""
});

// 营销活动管理
const promotionManage = (promotion: SkuPromotion[]) => {
    // 排除秒杀 & 限时折扣 & 拼团
    // 商品详情优惠入口只展示可参与的商品促销；抽奖活动(type=8)不属于优惠券/促销标签。
    promotionList.value = promotion.filter((item: any) => ![1, 6, 7, 8].includes(Number(item.type)));

    const seckillData = promotion.find((item: any) => item.type === 1);
    const discountData = promotion.find((item: any) => item.type === 6);

    // 判断限时折扣存在
    if (discountData) {
        const canUseDiscount = (discountData as any).canJoin !== false;
        isDiscount.value = canUseDiscount ? 1 : 0;
        discountValue.value.discountEndTime = discountData.data.item.endTime!;
        discountValue.value.discountPrice = discountData.data.item.discountPrice!;
        discountValue.value.canJoin = canUseDiscount;
        discountValue.value.restrictTip = (discountData as any).restrictTip || "";

        // 商详接口为展示活动仍会返回折扣价；无参与资格时还原为该会员的普通成交价
        if (!canUseDiscount && !seckillData) {
            const currentPrice = Number(productPrice.value);
            const discountAmount = Number(discountData.data?.item?.discountPrice ?? 0);
            if (Number.isFinite(currentPrice) && Number.isFinite(discountAmount)) {
                productPrice.value = (currentPrice + discountAmount).toFixed(2);
            }
        }
    } else {
        isDiscount.value = 0;
        discountValue.value = {
            discountEndTime: "",
            discountPrice: 0,
            canJoin: true,
            restrictTip: ""
        };
    }
    // 判断秒杀存在
    if (seckillData) {
        isSeckill.value = 1;
        seckillValue.value.seckillEndTime = seckillData.data.seckillEndTime!;
        seckillValue.value.seckillSales = seckillData.data.item.seckillSales!;
        seckillValue.value.seckillStock = seckillData.data.item.seckillStock!;
        seckillValue.value.canJoin = (seckillData as any).canJoin !== false;
        seckillValue.value.restrictTip = (seckillData as any).restrictTip || "";
        maxProductNumber.value = seckillData.data.seckillLimitNum!;
        productStock.value = seckillData.data.item.seckillStock;
    } else {
        isSeckill.value = 0;
        seckillValue.value = {
            seckillEndTime: "",
            seckillSales: 0,
            seckillStock: 0,
            canJoin: true,
            restrictTip: ""
        };
    }
};

// 秒杀属于活动资格购买；限时折扣不满足资格时仍可按普通价格购买
const memberRestricted = computed(() => {
    if (isSeckill.value === 1) return seckillValue.value.canJoin === false;
    return false;
});

const sendValue = () => {
    emit("sendValue", {
        marketPrice: productOriginPrice.value,
        productOriginPrice: productOriginPrice.value,
        productPrice: productPrice.value,
        productStock: productStock.value,
        productNumber: productNumber.value,
        productSkuId: currentSku.value.skuId,
        skuStr: skuStr.value,
        isSeckill: isSeckill.value,
        seckillValue: seckillValue.value,
        promotionList: promotionList.value,
        isDiscount: isDiscount.value,
        discountValue: discountValue.value,
        grouponValue: grouponInfo.value,
        isGroupon: isGroupon.value,
        depositAmount: presaleInfo.value?.depositAmount
    });
};

const handlePopupClose = () => {
    closePopup();
};

const addCard = () => {
    closePopup();
    setTimeout(() => {
        emit("addCardSucceed", true);
    }, 500);
};

const handleOverlimit = (event: string) => {
    if (event === "minus") {
        uni.$u.toast(t("商品数量不能小于1"));
    }
    if (event === "plus") {
        if (limitNumber.value > 0) {
            uni.$u.toast(`${t("限购")}${limitNumber.value}${t("件，已达限购数")}`);
        } else {
            uni.$u.toast(t("库存不足"));
        }
    }
};

// b2b
const getBatchProductAvailabilityList = async () => {
    const skuIds = batchSkuIds.value;
    if (!skuIds || batchAvailabilityLoading.value || loadedBatchSkuIds.value === skuIds) {
        return;
    }
    try {
        batchAvailabilityLoading.value = true;
        const result = await getBatchProductAvailability({ skuIds });
        b2bSkuList.value = skuList.value.map((sku: SkuList) => {
            const newSku = {
                ...sku,
                num: 0,
                title: sku.skuValue.replace(/\|/g, ",")
            };

            const skuId = sku.skuId.toString();
            if (result[skuId]) {
                Object.assign(newSku, {
                    price: result[skuId].price,
                    stock: result[skuId].stock
                });
            }

            return newSku;
        });
        loadedBatchSkuIds.value = skuIds;
    } catch (error) {
        console.error(error);
    } finally {
        batchAvailabilityLoading.value = false;
    }
};

const isBatch = computed(() => {
    return showBulkPurchaseTab.value && tabIndex.value === 1;
});

const handleTabChange = (index: number) => {
    tabIndex.value = index;
};
const skuItem = ref<any>([]);
let delayTimer: number | null = null;
const handleNumberChange = () => {
    if (delayTimer) {
        clearTimeout(delayTimer);
    }
    delayTimer = setTimeout(() => {
        const skus = b2bSkuList.value.filter((item: any) => item.num);
        skuItem.value = skus.map((item: any) => {
            return {
                skuId: item.skuId,
                num: item.num
            };
        });
        __getProductAmount();
    }, 300);
};
const productAmount = ref<ProductAmountItem>({
    count: 0,
    total: "0"
} as ProductAmountItem);
const __getProductAmount = async () => {
    try {
        const result = await getProductAmount({
            id: props.modelValue,
            skuItem: skuItem.value
        });
        productAmount.value = result;
    } catch (error) {
        console.error(error);
    }
};

const popupHeight = computed(() => {
    return isB2B() ? "75vh" : "65vh";
});
const footerOffset = computed(() => {
    return isB2B() && tabIndex.value === 1 ? "170rpx" : "100rpx";
});
const height = computed(() => {
    return `calc(${popupHeight.value} - ${footerOffset.value} - 15px - var(--safe-bottom))`;
});
const paddingBottom = computed(() => {
    return "20rpx";
});

defineExpose({
    handleShowPopup,
    refresh
});
</script>

<style lang="scss" scoped>
.placeholder {
    background-color: #f7f8fa;
    padding: 10rpx;
}

.tab {
    width: 100%;
    display: flex;
    position: relative;
    .line,
    .subline {
        position: absolute;
        width: 50%;
        height: 2px;
        background-color: var(--general);
    }
    .line {
        top: 0;
    }
    .subline {
        bottom: 0;
        height: 1px;
        background-color: #eee;
    }

    .tab-item {
        width: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        height: 80rpx;
        font-size: 26rpx;
        font-weight: 500;
        position: relative;

        .image {
            width: 20rpx;
            height: 100%;
            position: absolute;
            top: 0;
        }
        &.active {
            color: var(--general);
        }
    }
}

.allskulist {
    padding: 0 20rpx;

    .allskulist-item {
        display: flex;
        align-items: center;
        padding-bottom: 25rpx;
        &:first-child {
            padding-top: 20rpx;
        }

        .left {
            font-size: 24rpx;
            flex: 1;
            .title {
                padding-bottom: 6rpx;
                padding-right: 40rpx;
            }

            .desc {
                display: flex;
                font-size: 22rpx;
                color: #c1c1c1;
                align-items: center;
                height: 35rpx;
                .line {
                    width: 1px;
                    height: 100%;
                    background-color: #dadada;
                    margin: 0 12rpx;
                    height: 16rpx;
                }
            }
        }
    }
}

.attribute {
    color: #333;
    .attribute-item {
        margin-bottom: 30rpx;
        .item-title {
            font-size: 28rpx;
            font-weight: 600;
            margin-bottom: 20rpx;
        }
        .item-content {
            display: flex;
            flex-wrap: wrap;
            font-size: 26rpx;
            gap: 20rpx;
            .item-attr {
                background-color: #f7f8fa;
                border-radius: 10rpx;
                overflow: hidden;
                padding: 10rpx 20rpx;
                line-height: 42rpx;
                border: 1px solid #f7f8fa;
                &:first-child {
                    margin-left: 0;
                }
                &.checked {
                    color: var(--general);
                    border: 1px solid var(--general);
                    box-sizing: border-box;
                }
            }
        }
    }
}

.specification {
    height: v-bind(height);
    padding-bottom: v-bind(paddingBottom);
    box-sizing: border-box;
}

.product-sku-box {
    padding: 30rpx 20rpx;
    .product-info {
        display: flex;
        width: 100%;
        .product-info-img {
            position: relative;
            width: 180rpx;
            height: 180rpx;
            border-radius: 20rpx;
            overflow: hidden;

            .product-status-box {
                position: absolute;
                left: 0;
                top: 0;
                z-index: 10;
                display: flex;
                align-items: center;
                justify-content: center;
                width: 100%;
                height: 100%;

                .outsale {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    width: 50%;
                    height: 50%;
                    line-height: 1;
                    border-radius: 100%;
                    background: rgba(0, 0, 0, 0.4);
                    color: #ffffff;
                    font-size: 20rpx;
                }
            }
        }
        .info {
            margin-left: 20rpx;
            padding-top: 20rpx;
            width: calc(100% - 180rpx);

            .ticket {
                background-color: var(--tag-bg);
                color: var(--tag-text);
                border-radius: 5rpx;
                white-space: nowrap;
                width: fit-content;
                padding: 5rpx;
                margin: 10rpx 0;
                .ticket-content {
                    font-size: 20rpx;
                }
            }

            .del-box {
                padding: 5rpx 0;
                display: template;
                font-size: 12px;
                color: #969799;
                text-decoration: line-through;
            }

            .price {
                color: var(--general);
                font-weight: bold;
                font-size: 36rpx;
                margin-bottom: 10rpx;
            }
            .name {
                color: #c3c0c0;
                font-size: 24rpx;
            }
        }
    }
}

/* A1 / A2 / A3 共用新版大图头部、头部数量和图片关闭按钮。 */
.specification--a1,
.specification--overseas {
    position: relative;

    .product-sku-box {
        position: relative;
        padding: 30rpx;

        .product-info {
            flex: 1;
            padding-right: 34rpx;

            .product-info-img {
                flex-shrink: 0;
                width: 220rpx;
                height: 220rpx;
            }

            .info {
                display: flex;
                flex-direction: column;
                justify-content: space-between;
                width: calc(100% - 220rpx);
                padding-top: 0;

                .price {
                    display: flex;
                    margin-bottom: 0;
                    color: #1a1a1a;
                    column-gap: 6rpx;
                }

                .ticket {
                    margin: 0;
                    color: var(--general);
                    background: transparent;
                }

                .name {
                    position: relative;
                    top: -8rpx;
                    color: #666;
                }

                .sku-num {
                    .stock {
                        margin-left: 30rpx;
                        color: #666;
                        font-size: 22rpx;
                        white-space: nowrap;
                    }
                }
            }
        }

        .popup-close {
            position: absolute;
            top: 0;
            right: 0;
            display: flex;
            width: 60rpx;
            height: 60rpx;
            align-items: flex-end;
            justify-content: flex-start;

            image {
                width: 40rpx;
                height: 40rpx;
            }
        }
    }
}

// 无数量控件时不再按图片高度分配头部剩余空间，避免价格和标题之间出现大段空白。
.specification--a1 .product-sku-box.product-sku-box--without-quantity,
.specification--overseas .product-sku-box.product-sku-box--without-quantity {
    .product-info .info {
        justify-content: flex-start;

        .name {
            top: 0;
            margin-top: 8rpx;
        }
    }
}

/* 三种装修风格统一使用标准商城的描边规格样式。 */
.specification--a1 {
    .product-sku-info {
        padding: 30rpx 30rpx 0;

        .sku-item {
            .tit {
                height: auto;
                color: #1a1a1a;
                font-size: 28rpx;
                line-height: 34rpx;
            }

            .sku-tag {
                gap: 20rpx;

                .tag {
                    margin: 10rpx 0;
                    color: #1a1a1a;
                    background: transparent;
                    border: 1px solid #c2c4cc;
                    border-radius: 4px;

                    &.checked {
                        color: var(--general);
                        border-color: var(--general);
                        border-width: 1.5px;
                        font-weight: 600;
                    }

                    &.disabled {
                        color: #c2c4cc;
                        background: transparent;
                        border: 1.5px dashed #e0e1e5;
                        font-weight: 400;
                    }

                    &.checked.disabled {
                        color: #c2c4cc;
                        border-color: var(--general);
                        border-style: solid;
                    }

                    &.tag--image {
                        width: 108rpx;
                        min-width: 108rpx;
                        height: 108rpx;
                        padding: 0;
                        overflow: hidden;

                        .tag-img {
                            width: 100%;
                            height: 100%;
                            border-radius: 0;
                        }

                        .tag-text {
                            display: none;
                        }
                    }
                }
            }
        }
    }
}

.product-sku-info {
    padding: 20rpx 20rpx 0 20rpx;
    box-sizing: border-box;
    .sku-item {
        margin-bottom: 30rpx;
        display: flex;
        flex-direction: column;
        gap: 20rpx;
        .tit {
            color: #999;
            height: 33rpx;
            line-height: 32rpx;
            font-size: 24rpx;
        }
        .sku-tag {
            display: flex;
            flex-direction: row;
            flex-wrap: wrap;
            .tag {
                margin: 10rpx 20rpx 10rpx 0;
                background-color: #f7f8fa;
                color: #333;
                height: 55rpx;
                cursor: pointer;
                min-width: 60rpx;
                text-align: center;
                position: relative;
                border-radius: 10rpx;
                font-size: 24rpx;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 0 10rpx;
                border: 1px solid #f7f8fa;
                .tag-img {
                    width: 45rpx;
                    height: 45rpx;
                    box-sizing: content-box;
                    border-radius: 5rpx;
                    overflow: hidden;
                }

                .tag-text {
                    padding: 10rpx;
                }
            }
            .checked {
                color: var(--general);
                border: 1px solid var(--general);
                box-sizing: border-box;
            }
            .disabled {
                border: 1px dashed #eee;
                color: #c8c9cc;
                cursor: not-allowed;
                background-color: #f7f8fa;
            }
            .checked.disabled {
                border: 1px solid var(--general);
                color: #c8c9cc;
            }
        }
    }

    .num-item {
        display: flex;

        align-items: center;
        justify-content: space-between;
        .tit {
            color: #999;
            height: 33rpx;
            line-height: 32rpx;
            font-size: 24rpx;
        }

        .sku-num {
            :deep(.uni-numbox) {
                border: 1rpx solid #c3c0c0;
                border-radius: 10rpx;
            }
            :deep(.uni-numbox-btns) {
                background-color: #fff !important;
                color: #333;
                border-radius: 10rpx;
            }
            :deep(.uni-numbox--text) {
                font-weight: 500 !important;
            }
            :deep(.uni-numbox__value) {
                background-color: #fff !important;
                color: #333;
                border-left: 1rpx solid #c3c0c0;
                border-right: 1rpx solid #c3c0c0;
            }
            .stock {
                font-size: 22rpx;
                margin-left: 30rpx;
                color: #c3c0c0;
            }
            .green-txt {
                color: var(--general);
            }
            .red-txt {
                color: $tig-color-error;
            }
        }
    }
}

.footer {
    background-color: #fff;
    width: 100%;
    box-sizing: border-box;
    padding-bottom: calc(var(--safe-bottom) + 15px);
    flex-shrink: 0;

    &.shadow {
        box-shadow: 0 0 10px rgba(125, 126, 128, 0.16);
    }

    .b2b-btn {
        background-color: #fff;
        .desc {
            padding: 0 20rpx;
            display: flex;
            height: 70rpx;
            justify-content: space-between;
            align-items: center;
            font-size: 26rpx;
            .num {
                color: var(--general);
                padding: 5rpx;
            }
            .price {
                display: flex;
                align-items: center;
            }
        }
    }

    .exchange-btn,
    .groupon-btn {
        width: 100%;
        box-sizing: border-box;
        padding: 0 28rpx;

        .btn {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 70rpx;
            width: 100%;
            background: var(--general);
            border-radius: 100rpx;
            color: var(--main-text);
            line-height: normal;
            text-align: center;
            color: #fff;
            font-size: 24.5rpx;
            font-weight: 600;
        }
    }

    .add_cart {
        box-sizing: border-box;
        display: flex;
        width: 100%;
        height: 70rpx;
        padding: 0 28rpx;
        justify-content: initial;

        .buy-cell {
            flex: 1;
            min-width: 0;
            height: 100%;

            :deep(.buy) {
                width: 100%;
                height: 100%;
            }
        }

        .btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 70rpx;
            line-height: normal;
            text-align: center;
            color: #fff;
            font-size: 24.5rpx;
            font-weight: 600;
        }

        > .btn {
            flex: 1;
            width: 0;
            min-width: 0;
        }

        .cart {
            background: var(--vice-bg);
            border-radius: 100rpx 0 0 100rpx;
            color: var(--vice-text);
        }
        .buy-right {
            background: var(--general);
            border-radius: 0 100rpx 100rpx 0;
            color: var(--main-text);
        }
    }
    .add_cart.single {
        .buy-right {
            border-radius: 100rpx;
        }
    }

    /* 分离式按钮由容器提供对称边距，子按钮 flex 等分，避免 vw + gap 产生左右取整偏差。 */
    &.footer--a1-simple,
    &.footer--a3-simple {
        .add_cart {
            box-sizing: border-box;
            justify-content: initial;
            padding: 0 28rpx;

            /* 间距加在父级容器上，避免依赖小程序跨组件样式穿透。 */
            .buy-cell + .buy-cell {
                margin-left: 14rpx;
            }

            .btn {
                width: 100%;
            }

            > .btn {
                flex: 1;
                width: 0;
                min-width: 0;
            }

            > .btn + .btn {
                margin-left: 14rpx;
            }
        }
    }

    /* A1 / A2 默认弹窗风格：按钮分离并使用主题色。 */
    &.footer--a1-simple {
        .exchange-btn,
        .groupon-btn {
            .btn {
                border-radius: var(--spec-button-radius);
            }
        }

        .add_cart {
            .cart {
                box-sizing: border-box;
                color: var(--general);
                background: #fff;
                border: 3rpx solid var(--general);
                border-radius: var(--spec-button-radius);
            }

            .buy-right {
                color: var(--main-text);
                background: var(--general);
                border-radius: var(--spec-button-radius);
            }
        }
    }

    /* A1 经典风：保持连体结构，颜色跟随主题。 */
    &.footer--a1-classic {
        .add_cart {
            .cart {
                color: var(--vice-text);
                background: var(--vice-bg);
                border: 0;
            }

            .buy-right {
                color: var(--main-text);
                background: var(--general);
            }
        }
    }

    /* A3 默认简约风：对齐跨境项目，分离式描边/实色按钮。 */
    &.footer--a3-simple {
        .add_cart {
            .cart {
                box-sizing: border-box;
                color: #1a1a1a;
                background: #fff;
                border: 2px solid #505259;
                border-radius: var(--spec-button-radius);
            }

            .buy-right {
                color: var(--main-text);
                background: var(--general);
                border-radius: var(--spec-button-radius);
            }
        }
    }

    /* A3 胶囊/经典风：跟随装修配置切回连体按钮。 */
    &.footer--a3-classic {
        .add_cart {
            .cart {
                border-radius: 100rpx 0 0 100rpx;
            }

            .buy-right {
                border-radius: 0 100rpx 100rpx 0;
            }
        }

        .add_cart.single .buy-right {
            border-radius: 100rpx;
        }
    }
}

.disabled-div {
    background-color: #ddd; // 灰色背景
    color: #aaa; // 灰色字体
    opacity: 0.6; // 降低透明度
    pointer-events: none;
}

.exchange-box {
    display: flex;
    align-items: center;
    .exchange-img {
        width: 35rpx;
        height: 35rpx;
        padding-right: 5rpx;
    }
    .exchange-icon {
        padding: 0 5rpx;
    }
    .exchange-text {
        font-size: 34rpx;
    }
}
</style>
