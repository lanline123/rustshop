<template>
    <view>
        <tig-layout ref="layoutRef" :immediate-log="false" :product-id="productId" :shop-id="product.shopId">
            <template v-if="!failLoad">
                <view v-show="!loadEndStatus">
                    <productNav
                        v-model="scrollTop"
                        v-model:active-module="activeModule"
                        :event="handleScrollTo"
                        :is-collect="titleInfoRef?.isCollect ?? false"
                        @share="titleInfoRef?.handleShare()"
                        @collect="titleInfoRef?.addCollect()"
                    />
                    <view class="product-area-wrapper">
                        <productImg
                            class="product-area"
                            :is-sub-card="isSubCard"
                            :video-list="videoList"
                            :pic-list="picList"
                            :product-info="product"
                            :seckill-value="seckillValue"
                            :groupon-value="grouponValue"
                            :product-stock="productStock"
                            :detail-loaded="detailLoaded"
                        />
                    </view>
                    <view
                        class="productDetail-content"
                        :style="{ 'padding-bottom': `${detailLoaded && (product.productStatus === 0 || productStock === 0) ? 160 : 100}rpx` }"
                    >
                        <productTitleInfo
                            ref="titleInfoRef"
                            v-model:is-cycle-buy="isCycleBuy"
                            v-model:show-coupon="showCoupon"
                            :is-seckill="isSeckill"
                            :is-groupon="isGroupon"
                            :is-presale="isPresale ? 1 : 0"
                            :promotion-list="promotionList"
                            :product-info="product"
                            :product-price="productPrice"
                            :is-discount="isDiscount"
                            :is-sub-card="isSubCard"
                            :type="type"
                            :exchange-integral="exchangeIntegral"
                            :product-origin-price="productOriginPrice!"
                            :cycle-info="cycleInfo"
                            :groupon-value="grouponValue"
                            :product-stock="productStock"
                            :activity-bg-image="activityBgImage"
                            :seckill-value="seckillValue"
                            :discount-value="discountValue"
                            :presale-info="productPresale || undefined"
                            :sold-count="product.virtualSales || 0"
                        />

                        <view v-if="isGroupon && totalGroupCount" class="group-card-row">
                            <view class="flex justify-between align-center">
                                <view class="group-num">{{ grouponValue.joinNumCount }}{{ $t("人在拼单，可直接参与") }}</view>
                                <view class="flex align-center group-desc" @click="handleShowMore"
                                    >{{ $t("查看更多") }}
                                    <view class="iconfont-h5 icon-gengduo1 more" />
                                </view>
                            </view>
                            <view class="group">
                                <productGroupon :groups="twoGroupList" padding="" @join-click="hanldeJoinGroup" />
                            </view>
                            <view class="instructions">
                                <text class="">{{ $t("拼团说明") }}</text>
                                <view class="process">
                                    <view class="process-item">
                                        <image class="process-image" :src="staticResource('groupon/groupon_open.png')" />
                                        <text>{{ $t("1.开团/参加") }}</text>
                                    </view>
                                    <view class="process-item">
                                        <image class="process-image" :src="staticResource('groupon/groupon_invite.png')" />
                                        <text>{{ $t("2.邀友参团") }}</text>
                                    </view>
                                    <view class="process-item">
                                        <image class="process-image" :src="staticResource('groupon/groupon_success.png')" />
                                        <text>{{ $t("3.人满成团") }}</text>
                                    </view>
                                </view>
                            </view>
                        </view>

                        <view class="product-card-row">
                            <view class="cart-item flex align-center justify-between" @click="showSpecification">
                                <view class="flex align-center">
                                    <view class="title"> {{ $t("规格") }} </view>
                                    <view class="label">
                                        <template v-if="isB2B() && attrList.spe?.length > 0">
                                            <template v-for="(item, index) in attrList.spe" :key="String(item.attrName) + '-' + index">
                                                <text>{{ String(item.attrList.length) + $t("种") + $t(String(item.attrName)) }}</text>
                                            </template>
                                        </template>
                                        <template v-else> {{ skuStr }} {{ productNumber }} {{ $t("件") }}</template>
                                    </view>
                                </view>
                                <view>
                                    <image class="icon-image" :src="staticResource('common/more.png')" />
                                </view>
                            </view>

                            <template v-if="isPresale && productPresale">
                                <template v-if="productPresale.saleMode === 2">
                                    <view class="cart-item flex align-center justify-between">
                                        <view class="flex align-center">
                                            <view class="title"> {{ $t("尾款") }} </view>
                                            <view class="label"> {{ PaymentTimeText }} </view>
                                        </view>
                                    </view>
                                </template>
                                <view class="cart-item flex align-center justify-between">
                                    <view class="flex align-center">
                                        <view class="title"> {{ $t("发货") }} </view>
                                        <view class="label"> {{ deliverTimeText }} </view>
                                    </view>
                                </view>
                            </template>

                            <template
                                v-if="configStore.showSelledCount || attrList.normal?.length > 0 || product.fixedShippingType === 1 || serviceList.length > 0"
                            >
                                <view class="card-row-flex">
                                    <template v-if="configStore.showSelledCount">
                                        <view class="cart-item flex align-center justify-between">
                                            <view class="flex align-center">
                                                <view class="title"> {{ $t("销量") }} </view>
                                                <view class="label"> {{ product.virtualSales }} {{ $t("件") }} </view>
                                            </view>
                                        </view>
                                    </template>

                                    <template v-if="attrList.normal?.length > 0">
                                        <view class="cart-item flex align-center justify-between" @click="showDrawer">
                                            <view class="flex align-center">
                                                <view class="title"> {{ $t("参数") }} </view>
                                                <view class="label attr_text line1">
                                                    <view class="shuxin">
                                                        {{ $t(shuxinText) }}
                                                    </view>
                                                </view>
                                            </view>
                                            <view>
                                                <image class="icon-image" ::src="staticResource('common/more.png')" />
                                            </view>
                                        </view>
                                    </template>

                                    <template
                                        v-if="
                                            product.fixedShippingType === 1 &&
                                            Number(product.fixedShippingFee) == 0 &&
                                            ![2, 3, 4, 5, 6, 8].includes(product.productType as number)
                                        "
                                    >
                                        <view class="cart-item flex align-center justify-between">
                                            <view class="flex align-center">
                                                <view class="title"> {{ $t("运费") }} </view>
                                                <view class="label attr_text line1">
                                                    <view class="shuxin">
                                                        <!-- 电子礼品卡、虚拟、付费、卡密商品详情页中运费为无需配送 -->
                                                        {{ [2, 3, 4, 8].includes(product.productType as number) ? $t("无需配送") : $t("包邮") }}
                                                    </view>
                                                </view>
                                            </view>
                                            <view>
                                                <image class="icon-image" ::src="staticResource('common/more.png')" />
                                            </view>
                                        </view>
                                    </template>
                                    <template v-if="product.productType === 7 || product.productType === 8">
                                        <view class="cart-item flex align-center justify-between">
                                            <view class="flex align-center">
                                                <view class="title"> {{ $t("说明") }} </view>
                                                <view class="label attr_text line1">
                                                    <view class="shuxin">
                                                        {{ $t("此商品不支持售后，如需售后请联系客服") }}
                                                    </view>
                                                </view>
                                            </view>
                                        </view>
                                    </template>
                                    <template v-if="serviceList.length > 0">
                                        <view class="cart-item">
                                            <view class="flex">
                                                <view class="title"> {{ $t("服务") }} </view>
                                                <view class="label flex-wrap align-center servicelist">
                                                    <template v-for="(item, index) in serviceList" :key="index">
                                                        <!-- <template > -->
                                                        <span class="flex align-center service-item"
                                                            ><tig-image v-if="item.icoImg" style="width: 15px; height: 15px" :src="item.icoImg" />{{
                                                                item.productServiceName
                                                            }}</span
                                                        >
                                                        <!-- </template> -->
                                                    </template>
                                                </view>
                                            </view>
                                        </view>
                                    </template>
                                </view>
                            </template>
                        </view>

                        <template v-if="shopId > -1 && type !== 'exchange' && ![2, 4].includes(configStore.storeEntryRule)">
                            <productShopInfo :store-info="storeInfo" :shop-id="shopId" :product-id="product.productId" :form-type="formType" />
                        </template>

                        <productComment class="evaluate-area" :product-id="product.productId" />

                        <productContent
                            :is-buy="product.isBuy"
                            :paid-content="product.paidContent"
                            :product-type="product.productType"
                            :desc-arr="descArr"
                            :product-id="product.productId"
                            class="product-detail-area"
                        />
                    </view>

                    <view :style="{ 'padding-bottom': configStore.safeBottom + 'rpx' }" class="bottom-bar-box">
                        <view class="bottom-bar flex align-center">
                            <view class="icon-box">
                                <view class="label" @click="handleToHome">
                                    <view>
                                        <image mode="widthFix" :src="staticResource('product/home.png')" class="img" />
                                    </view>
                                    <view class="label-text">
                                        <text>{{ $t("首页") }}</text>
                                    </view>
                                </view>

                                <view v-if="showService" class="label">
                                    <service :phone="shopInfo.kefuPhone" :product-id="productId" :shop-id="shopInfo.shopId">
                                        <view>
                                            <image mode="widthFix" :src="staticResource('product/service.png')" class="img" />
                                        </view>
                                        <view class="label-text">
                                            <text>{{ $t("客服") }}</text>
                                        </view>
                                    </service>
                                </view>

                                <view
                                    v-if="!isCycleBuy && !isPresale"
                                    class="label cart-position"
                                    @click.stop="toPage(userStore.token ? '/pages/cartJump/index' : '/pages/cart/index')"
                                >
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
                                    <tig-button :disabled="true" class="isBuyBtn">{{ $t("商品已购买") }} </tig-button>
                                </template>
                                <template v-else-if="(product.productStatus === 0 || productStock === 0) && !isSubCardReservationProduct">
                                    <view class="other-products-btn" @click="handleToProductList">{{ $t("查看其他商品") }}</view>
                                </template>
                                <template v-else>
                                    <template v-if="type === 'exchange'">
                                        <view class="other-products-btn" @click="showSpecification">{{ $t("立即兑换") }}</view>
                                    </template>
                                    <template v-else>
                                        <template v-if="isPresale && productPresale">
                                            <view class="other-products-btn" @click="showSpecification">
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
                                                    <view class="btn cart" @click="showSpecification">{{ $t("单独购买") }} </view>
                                                    <view v-if="isGroupon" class="btn buy" @click="showGrouponPoup">
                                                        {{ $t("我要开团") }}
                                                    </view>
                                                </template>
                                                <template v-else>
                                                    <template v-if="isCycleBuy || isSubCard">
                                                        <view class="other-products-btn" @click="showSpecification">{{ $t("立即购买") }}</view>
                                                    </template>
                                                    <template v-else>
                                                        <view class="btn cart" @click="showSpecification">{{ $t("加入购物车") }}</view>
                                                        <view class="btn buy" @click="showSpecification">{{ $t("立即购买") }} </view>
                                                    </template>
                                                </template>
                                            </template>
                                            <template v-else>
                                                <view class="other-products-btn" @click="showSpecification">{{
                                                    [5, 6].includes(product.productType as number) && !isSubCardReservationProduct
                                                        ? $t("立即预约")
                                                        : $t("立即购买")
                                                }}</view>
                                            </template>
                                        </template>
                                    </template>
                                </template>
                            </view>
                            <template v-if="detailLoaded && (product.productStatus === 0 || productStock === 0) && !isSubCardReservationProduct">
                                <view class="product-status">
                                    {{ $t(product.productStatus === 0 ? "商品已下架" : "商品已售罄") }}
                                </view>
                            </template>
                        </view>
                    </view>

                    <specification
                        ref="specificationRef"
                        v-model="specificationId"
                        v-model:is-cycle-buy="isCycleBuy"
                        v-model:is-sub-card="isSubCard"
                        v-model:sku-id="skuId"
                        v-model:shop-id="shopId"
                        :type="type"
                        :groupon-enable="grouponEnable"
                        :groupon-record-info="grouponRecordInfo"
                        @load-end="getLoadStatus"
                        @add-card-succeed="getShowAnimation"
                        @send-value="getValue"
                        @close="grouponEnable = true"
                    />

                    <productCoupon v-model="showCoupon" :product-id="Number(productId)" :promotion-list="promotionList" :shop-id="shopId" />
                    <tig-popup v-model:show="showAttrRef">
                        <scroll-view scroll-y="true" class="attr_table-box">
                            <view class="popup-title title">{{ $t("规格参数") }}</view>
                            <view class="attr_table">
                                <view v-for="(item, index) in attrList.normal" :key="index" class="item flex">
                                    <view class="lable">{{ $t(item.attrName) }}</view>
                                    <view v-for="(attr, attrIndex) in item.attrList" :key="attrIndex" class="value"> {{ attr.attrValue }} </view>
                                </view>
                            </view>
                        </scroll-view>
                    </tig-popup>
                    <!--更多拼团列表-->
                    <productGrouponMore
                        v-model="showMoreGroupon"
                        :product-id="product.productId"
                        :group-list="groupList"
                        :total="totalGroupCount"
                        @join-click="hanldeJoinGroup"
                    />
                    <tig-back-top :scroll-top="scrollTop" />
                    <animation v-model="showAnimation" :style-variable="styleVariable" :animation-position="animationPosition" />
                </view>
            </template>
            <template v-else>
                <error />
                <recommend title-bg="#fff" />
            </template>
        </tig-layout>
        <tig-loadingpage v-model="loadEndStatus" />
    </view>
</template>

<script lang="ts" setup>
/**
 * 商品详情页
 * - 展示商品图片、标题、价格、规格、评论等完整信息
 * - 支持秒杀、拼团、周期购、预约服务等多种商品类型
 * - SKU选择、购物车、立即购买等核心交互
 * - 门店信息、优惠券领取、收藏分享等功能
 */
import productGroupon from "./src/productGroupon.vue";
import productNav from "./src/productNav.vue";
import productImg from "./src/productImg.vue";
import productTitleInfo from "./src/productTitleInfo.vue";
import productComment from "./src/productComment.vue";
import productShopInfo from "./src/productShopInfo.vue";
import specification from "@/components/product/specificationV1.vue";
import productCoupon from "./src/productCoupon.vue";
import productGrouponMore from "./src/productGrouponMore.vue";
import productContent from "./src/productContent.vue";
import animation from "./src/animation.vue";
import error from "./src/error.vue";
import recommend from "@/components/recommend/index.vue";
import service from "@/components/service/index.vue";
import { imageFormat } from "@/utils/format";
import { setPageMeta, setJsonLd, extractDescription, clearJsonLd } from "@/utils/seo";
import { nextTick, ref, getCurrentInstance, computed } from "vue";
import { onLoad, onShow, onReady, onUnload, onShareAppMessage, onShareTimeline } from "@dcloudio/uni-app";
import { getProductDetail } from "@/api/product/product";
import { getActiveStyle } from "@/api/product/productStyle";
import type { ProductActiveStyleVO } from "@/types/product/productStyle";
import { getExchangeDetail } from "@/api/exchange/exchange";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";
import { asyncGetCartCount } from "@/api/cart/cart";
import type {
    PicList,
    ProductItem,
    AttrList,
    SkuList,
    ServiceList,
    RankDetail,
    DescArr,
    SkuPromotion,
    VideoList,
    GrouponDetail
} from "@/types/product/product";
import type { PresaleInfo } from "@/types/presale/presale";
import type { ShopDetailItem } from "@/types/shop/shop";
import { getShopDetail } from "@/api/shop/shop";
import { getStoreDetail } from "@/api/store/store";
import type { StoreDetailData } from "@/types/store/store";
import { redirect, getElementRect, staticResource, isB2B, parseQueryString } from "@/utils";
import { resolveMiniScene } from "@/api/common";
import { useI18n } from "vue-i18n";
import { useScrollTop } from "@/hooks";
import { useRegionStore } from "@/store/region";
import type { GrouponRecordListParams, GrouponRecordItem } from "@/types/groupon/groupon";
import { getGrouponRecordList } from "@/api/groupon/groupon";
import { isSubCardReservationProduct as checkIsSubCardReservationProduct } from "@/utils/productSoldOut";

const { t } = useI18n();

const regionStore = useRegionStore();

const { scrollTop } = useScrollTop();

const productId = ref(0);

const showAttrRef = ref(false);
const showMoreGroupon = ref(false);
const configStore = useConfigStore();
const specificationRef = ref();
const titleInfoRef = ref();
const specificationId = ref(0);
const animationPosition = ref({
    top: 0,
    left: 0
});
const layoutRef = ref();
const type = ref("");
const exchangeIntegral = ref(0);

/**
 * @description 显示规格参数弹窗
 */
const showDrawer = () => {
    showAttrRef.value = true;
};

/**
 * @description 显示更多拼团列表弹窗
 */
const handleShowMore = () => {
    showMoreGroupon.value = true;
};

/**
 * @description 显示规格选择弹窗（单独购买模式）
 */
const showSpecification = () => {
    if (specificationRef.value) {
        grouponEnable.value = false;
        specificationRef.value.handleShowPopup();
    }
};

/**
 * @description 显示规格选择弹窗（开团模式）
 * 用于发起新的拼团
 */
const showGrouponPoup = () => {
    if (specificationRef.value) {
        grouponEnable.value = true;
        grouponRecordInfo.value.minGroupNum = 0;
        grouponRecordInfo.value.grouponPromotionRecordId = 0;
        specificationRef.value.handleShowPopup();
    }
};

const product = ref<ProductItem>({
    productId: 0,
    productStock: 0
});

// 预售信息（saleMode>0 时存在）
const productPresale = computed<PresaleInfo | null>(() => {
    const info = (product.value as any)?.presaleInfo;
    const saleMode = Number((product.value as any)?.saleMode ?? info?.saleMode ?? 0);
    if (saleMode > 0 && info) return info as PresaleInfo;
    return null;
});
const isPresale = computed(() => Number(productPresale.value?.saleMode ?? 0) > 0);
const attrList = ref<AttrList>({} as AttrList);
const skuList = ref<SkuList[]>([]);
const picList = ref<PicList[]>([]);
const serviceList = ref<ServiceList[]>([]);
const rankDetail = ref<RankDetail>({});
const descArr = ref<DescArr[]>([]);
const checkedValue = ref<string[]>([]);
const videoList = ref<VideoList[]>([]);
const failLoad = ref(false);
const grouponEnable = ref(true);
const activityStyleData = ref<ProductActiveStyleVO | null>(null);
const activityBgImage = computed(() => activityStyleData.value?.activityBackgroundImage || "");
// 详情接口（商品/兑换）已返回并完成数据落地后，才允许子组件做“是否售罄”的判断
const detailLoaded = ref(false);

const shopId = ref<number>(-1);
const formType = ref(false);

// 规格组件加载完成（用于避免它先触发结束 loading，而详情接口还没返回）
const specLoaded = ref(false);


// 是否是周期购
const cycleInfo = ref<any>({});
const isCycleBuy = ref(true);

// 是否是次卡
const isSubCard = ref(false);

// 是否是次卡商品，并且是预约商品
const isSubCardReservationProduct = computed(() => {
    return checkIsSubCardReservationProduct(product.value as Record<string, unknown>, isSubCard.value);
});

const fetchActivityStyle = async (pid: number) => {
    try {
        const result = await getActiveStyle(pid);
        activityStyleData.value = result;
    } catch {
        activityStyleData.value = null;
    }
};

/**
 * @description 获取商品详情
 * O2O场景下优先使用指定门店ID，否则使用最近门店ID
 * 支持普通商品和积分兑换商品两种模式
 * @param id 商品ID或包含sn的对象
 */
const __getProductDetail = async (id: number | object) => {
    try {
        loadEndStatus.value = true;
        detailLoaded.value = false;
        let result: any = {};

        let params: any = {
            shopId: null,
            nearestShopId: null
        };

        if (typeof id === "object") {
            params = { ...params, ...id };
        } else {
            params.id = id;
        }
        if (shopId.value > -1) {
            params.shopId = shopId.value;
        } else {
            params.nearestShopId = regionStore.nearestShopInfo.shopId || null;
        }

        switch (type.value) {
            case "exchange":
                if (typeof id === "object") return;
                result = await getExchangeDetail(params);
                exchangeIntegral.value = result.exchangeInfo.exchangeIntegral;
                productStock.value = Number(result.exchangeInfo?.productStock ?? 0);
                productPrice.value = result.exchangeInfo.discountsPrice || "0";
                specificationId.value = result.exchangeInfo.id;
                break;
            default:
                result = await getProductDetail(params);
                productStock.value = Number(result.item?.productStock ?? 0);
                productPrice.value = result.item.productPrice || "0";
                specificationId.value = result.item.productId;
        }
        productId.value = result.item.productId;
        product.value = result.item;
        attrList.value = result.attrList;
        picList.value = result.picList;
        videoList.value = result.videoList;
        rankDetail.value = result.rankDetail;
        skuList.value = result.skuList;
        checkedValue.value = result.checkedValue;
        descArr.value = result.descArr;
        serviceList.value = result.serviceList;
        getGrouponRecordsParams.productId = product.value.productId;
        uni.setNavigationBarTitle({
            title: result.item.productName
        });
        // #ifdef H5
        const shopTitle = configStore.baseInfo.shopTitle;
        const productTitle = `${result.item.productName} - ${shopTitle}`;
        const productDesc = extractDescription(result.item.productBrief || result.item.productDesc || result.item.productName);
        const productImage = imageFormat(result.item.picUrl || "");

        setPageMeta({
            title: productTitle,
            description: productDesc,
            keywords: result.item.productName,
            image: productImage,
            type: "product"
        });

        setJsonLd({
            "@context": "https://schema.org",
            "@type": "Product",
            name: result.item.productName,
            description: productDesc,
            image: productImage,
            url: window.location.href,
            offers: {
                "@type": "Offer",
                price: result.item.productPrice,
                priceCurrency: "CNY",
                availability: result.item.productStock > 0 ? "https://schema.org/InStock" : "https://schema.org/OutOfStock"
            }
        });
        // #endif

        // 优先使用传入的 shopId，如果没有则使用接口返回的
        const finalShopId = shopId.value > -1 ? shopId.value : (result.shopId ?? -1);
        shopId.value = finalShopId;

        if (finalShopId > -1) {
            getStoreDetailData(finalShopId);
            getShopInfo(finalShopId);
        }

        fetchActivityStyle(result.item.productId);

        // 数据已落地，允许展示“已售罄”遮罩（最终是否售罄由库存判断决定）
        detailLoaded.value = true;
        nextTick(() => {
            layoutRef.value?.handleLog();
        });
        // 只有“规格组件加载完成 + 详情接口落地完成”同时满足，才结束 loading
        if (specLoaded.value) loadEndStatus.value = false;
    } catch (error: any) {
        console.error(error);
        failLoad.value = true;
        loadEndStatus.value = false;
    } finally {
        // 成功场景下由 specLoaded/detailLoaded 共同控制 loading；失败则直接结束
        if (failLoad.value) {
            loadEndStatus.value = false;
        } else if (detailLoaded.value && specLoaded.value) {
            loadEndStatus.value = false;
        } else {
            loadEndStatus.value = true;
        }
    }
};

const loadEndStatus = ref(true);

/**
 * @description 规格组件加载完成回调，隐藏加载页
 */
const getLoadStatus = () => {
    specLoaded.value = true;
    if (detailLoaded.value) {
        loadEndStatus.value = false;
    }
};

const storeInfo = ref<StoreDetailData>({} as StoreDetailData);

/**
 * @description 获取门店详情信息
 * O2O场景特有，用于显示门店地址、营业时间等
 * @param shopId 门店ID
 */
const getStoreDetailData = async (shopId: number) => {
    try {
        const result = await getStoreDetail({ shopId });
        storeInfo.value = result;
    } catch (error) {
        console.error("Error fetching store details:", error);
    }
};

const skuStr = ref<string>("");
const productPrice = ref<string>("0");
const productOriginPrice = ref<string | number | null>(null);
const productStock = ref<number>(0);
const isSeckill = ref<number>(0);
const seckillValue = ref<any>({});
const productNumber = ref<number>(1);
const promotionList = ref<SkuPromotion[]>([]);
const isDiscount = ref<number>(0);
const isGroupon = ref<number>(0);
const discountValue = ref<any>({});
const discountEndTime = ref<string>("");
const grouponValue = ref<any>({});

interface Ival {
    marketPrice?: string | null;
    productOriginPrice: string | null;
    productPrice: string;
    productStock: number;
    productNumber: number;
    productSkuId?: number;
    skuStr?: string;
    isSeckill: number;
    seckillValue: any;
    isDiscount: number;
    discountValue: any;
    discountEndTime: string;
    promotionList: SkuPromotion[];
    grouponValue: GrouponDetail;
    depositAmount?: number;
}

/**
 * @description 接收规格组件传递的商品信息
 * 包含价格、库存、促销活动、拼团信息等
 * @param val 商品规格相关数据
 */
const getValue = (val: Ival) => {
    console.log("getValue", val);
    if (val.productSkuId && val.skuStr) {
        skuStr.value = val.skuStr;
    }
    // 规格组件回传的库存可能是 string/undefined，这里统一转 number，保证后续判断稳定
    productStock.value = Number((val as any)?.productStock ?? 0);
    promotionList.value = val.promotionList;
    productPrice.value = val.productPrice;
    productOriginPrice.value = val.cycleInfo?.originalPrice || val.productOriginPrice;
    product.value.marketPrice = val.marketPrice || val.productOriginPrice || product.value.marketPrice;
    productNumber.value = val.productNumber;
    isSeckill.value = val.isSeckill;
    seckillValue.value = val.seckillValue;
    isDiscount.value = val.isDiscount;
    discountValue.value = val.discountValue;
    discountEndTime.value = val.discountEndTime;
    grouponValue.value = val.grouponValue;
    if (grouponValue.value?.productId) {
        isGroupon.value = 1;
    } else {
        isGroupon.value = 0;
    }

    // 预售优先：预售商品不再叠加秒杀/折扣/拼团活动，避免标题区同时显示秒杀价与预售价
    if (isPresale.value) {
        isSeckill.value = 0;
        isDiscount.value = 0;
        isGroupon.value = 0;
    }

    if (isGroupon.value) {
        getGrouponRecords();
    }


    // 预售（SKU 联动定金）
    if (val.depositAmount !== undefined && val.depositAmount !== null && isPresale.value) {
        if (!(product.value as any).presaleInfo) (product.value as any).presaleInfo = {} as PresaleInfo;
        (product.value as any).presaleInfo.depositAmount = Number(val.depositAmount);
    }
};

// 定金预售：尾款时间文案
const PaymentTimeText = computed(() => {
    const info = productPresale.value;
    if (!info || Number(info.saleMode) !== 2) return "";

    if (Number(info.finalPaymentType) === 1) {
        return info.finalPaymentRelativeDays ? t("付定金{0}天后开始", [info.finalPaymentRelativeDays]) : "";
    }
    return info.finalPaymentStartTime ? `${info.finalPaymentStartTime}${t("开始")}` : "";
});

// 预售：发货时间文案
const deliverTimeText = computed(() => {
    const info = productPresale.value;
    if (!info) return "";

    if (Number(info.deliveryType) === 1) {
        const days = info.deliveryRelativeDays ? `${info.deliveryRelativeDays}${t("天")}` : "";
        const hours = info.deliveryRelativeHours ? `${info.deliveryRelativeHours}${t("小时")}` : "";
        return `${Number(info.saleMode) === 1 ? t("付全款") : t("付尾款")}${days}${hours}${t("后发货")}`;
    }
    return info.deliveryDate ? `${info.deliveryDate}${t("开始")}` : "";
});
const cartCount = ref<number>(0);

/**
 * @description 获取购物车商品数量（用于底部栏角标显示）
 */
const _getCartCount = async () => {
    try {
        const result = await asyncGetCartCount();
        cartCount.value = result;
    } catch (error) {
        console.error(error);
    }
};

/**
 * @description 页面跳转
 * @param url 目标页面路径
 */
const toPage = (url: string) => {
    redirect({ url });
};

const showCoupon = ref(false);

/**
 * @description 计算商品参数名称展示文本
 */
const shuxinText = computed(() => {
    if (attrList.value.normal && attrList.value.normal.length) {
        const names = attrList.value.normal.map((item: any) => item.attrName);
        return names.join("/");
    }
    return "";
});

/**
 * @description 跳转首页
 */
const handleToHome = () => {
    uni.switchTab({ url: "/pages/index/index" });
};

/**
 * @description 商品售罄/下架时跳转商品列表查看其他商品
 */
const handleToProductList = () => {
    uni.navigateTo({ url: "/pages/search/result" });
};

const instance = getCurrentInstance();
const systemInfo = ref<any>(uni.getSystemInfoSync());

/**
 * @description 滚动到指定模块位置
 * @param module 模块选择器（如.product-area）
 */
const handleScrollTo = (module: string) => {
    const query = uni.createSelectorQuery().in(instance);
    query.select(module).boundingClientRect();
    query.selectViewport().scrollOffset(() => {});
    query.exec((res) => {
        uni.pageScrollTo({
            scrollTop: res[0].top + res[1].scrollTop - 50,
            duration: 0
        });
    });
};
const activeModule = ref(".product-area");
let productObserver: any, evaluateObserver: any, productDetailObserver: any;

/**
 * @description 创建交叉观察器，用于导航栏高亮切换
 * @param observer 观察器实例
 * @param module 模块选择器
 */
const getObserver = (observer: any, module: string) => {
    observer.relativeToViewport({ bottom: -(systemInfo.value.windowHeight - 200) }).observe(module, (res: any) => {
        if (res.intersectionRatio > 0) {
            activeModule.value = module;
        }
    });
};
onReady(() => {
    productObserver = uni.createIntersectionObserver(instance);
    evaluateObserver = uni.createIntersectionObserver(instance);
    productDetailObserver = uni.createIntersectionObserver(instance);
    getObserver(productObserver, ".product-area");
    getObserver(evaluateObserver, ".evaluate-area");
    getObserver(productDetailObserver, ".product-detail-area");
});
onUnload(() => {
    // #ifdef H5
    clearJsonLd();
    // #endif
    if (productObserver) {
        productObserver?.disconnect();
        productObserver = null;
    }
    if (evaluateObserver) {
        evaluateObserver?.disconnect();
        evaluateObserver = null;
    }

    if (productDetailObserver) {
        productDetailObserver?.disconnect();
        productDetailObserver = null;
    }

    const pages = getCurrentPages();
    const index = pages.findIndex((item) => item.route === "pages/product/index");
    if (index === -1) {
        uni.$off("refreshStoreInfo");
    }

    // #ifdef  H5 || APP
    uni.closePreviewImage();
    // #endif
});

const skuId = ref(0);

/**
 * @description 保存分销员推广商品关系
 * 用于追踪分销员推广业绩
 * @param salesmanId 分销员ID
 * @param productId 商品ID
 */
const saveSalesmanProducts = (salesmanId: string, productId: string) => {
    let salesmanProducts = uni.getStorageSync("salesmanProducts") || [];
    if (salesmanProducts.length > 0) {
        const index = salesmanProducts.findIndex((item: any) => item.productId == productId);
        if (index !== -1) {
            salesmanProducts[index].salesmanId = salesmanId;
        }
    }
    salesmanProducts = [...salesmanProducts, { salesmanId, productId }];
    uni.setStorageSync("salesmanProducts", salesmanProducts);
};

const init = async (option: any) => {
    if (option) {
        // #ifdef MP-WEIXIN
        if (option.scene) {
            const sceneStr = decodeURIComponent(String(option.scene));
            Object.assign(option, parseQueryString(sceneStr));
            if (option.s && !option.salesmanId) {
                option.salesmanId = option.s;
            }
            if (!option.id && sceneStr && !sceneStr.includes("=")) {
                try {
                    const resolved = await resolveMiniScene(sceneStr);
                    if (resolved?.id) {
                        option.id = String(resolved.id);
                    }
                    if (resolved?.salesmanId) {
                        option.salesmanId = String(resolved.salesmanId);
                    }
                } catch (error) {
                    console.error(error);
                }
            }
            // 短码场景下若最终仍未解析出商品 ID，避免页面空白且无任何反馈
            if (!option.id && !option.sn) {
                uni.showToast({ title: t("商品参数无效，请重新进入"), icon: "none", duration: 2000 });
                setTimeout(() => {
                    const pages = getCurrentPages();
                    if (pages.length > 1) {
                        uni.navigateBack();
                    } else {
                        uni.switchTab({ url: "/pages/index/index" });
                    }
                }, 1500);
                return;
            }
        }
        // #endif

        if (option.type) {
            type.value = option.type;
        }
        if (option.shopId) {
            shopId.value = Number(option.shopId);
            formType.value = true;
        }

        if (option.sn) {
            __getProductDetail({ sn: option.sn });
        } else if (option.id) {
            __getProductDetail(Number(option.id));
        }
        if (option.skuId) {
            skuId.value = Number(option.skuId);
        }
        if (option.salesmanId) {
            saveSalesmanProducts(option.salesmanId, option.id);
        }
    }
};

onLoad(async (option) => {
    uni.$on("refreshStoreInfo", (id: number) => {
        if (id > -1) {
            shopId.value = id;

            nextTick(() => {
                if (option?.sn) {
                    __getProductDetail({ sn: option.sn });
                } else if (option?.id) {
                    __getProductDetail(Number(option.id));
                }
                specificationRef.value?.refresh();
            });
        }
    });

    await init(option);
});

onShow(() => {
    _getCartCount();
});

const showAnimation = ref(false);

/**
 * @description 触发加入购物车动画
 * @param val 是否显示动画
 */
const getShowAnimation = async (val: boolean) => {
    await queryRect(".cart-position");
    await getAnimationPosition(".cart");

    showAnimation.value = val;
    _getCartCount();
};

/**
 * @description 获取动画起始位置
 * @param selector 元素选择器
 */
const getAnimationPosition = async (selector: any) => {
    const data = await getElementRect(selector);
    if (data) {
        const { width, height, top, left } = data;
        animationPosition.value.top = top - height / 2;
        animationPosition.value.left = left + width / 2;
    }
};

const cartRect = ref({
    left: 0,
    top: 0
});

const queryRect = async (selector: any) => {
    const data = await getElementRect(selector);
    if (data) {
        const { width, height, top, left } = data;
        cartRect.value.top = top - height / 2;
        cartRect.value.left = left + width / 2;
    }
};

const styleVariable = computed(() => {
    return {
        "--x": cartRect.value.left - animationPosition.value.left + "px",
        "--y": "8px"
    };
});

onShareAppMessage(() => {
    return {
        title: product.value.productName,
        imageUrl: imageFormat(product.value.picUrl!)
    };
});

onShareTimeline(() => {
    return {
        title: product.value.productName,
        imageUrl: imageFormat(product.value.picUrl!)
    };
});

const shopInfo = ref<ShopDetailItem>({} as ShopDetailItem);

/**
 * @description 获取店铺信息
 * 用于显示客服入口和店铺相关信息
 * @param id 店铺ID
 */
const getShopInfo = async (id: number) => {
    try {
        const result = await getShopDetail(id);
        shopInfo.value = result;
    } catch (error: any) {
        console.error(error);
    }
};

const userStore = useUserStore();

/**
 * @description 判断是否显示客服入口
 * 店铺配置了客服入口显示在商品详情页时显示
 */
const showService = computed(() => {
    if (Object.keys(shopInfo.value).length > 0 && shopInfo.value.shopId !== 0) {
        if (shopInfo.value.kefuInlet && shopInfo.value.kefuInlet.length > 0 && shopInfo.value.kefuInlet.includes(1)) {
            return true;
        }
        return false;
    } else {
        return userStore.serviceConfig.show;
    }
});

const grouponRecordInfo = ref<{
    grouponPromotionRecordId: number;
    minGroupNum: number;
}>({
    grouponPromotionRecordId: 0,
    minGroupNum: 0
});

const getGrouponRecordsParams: GrouponRecordListParams & { excludeEnded: boolean } = {
    page: 1,
    size: 10,
    // isMine: false,
    joinStatus: 1,
    dateType: 0, // Fix: add missing required property
    productId: 0,
    excludeEnded: true
};
const groupList = ref<GrouponRecordItem[]>([]);

/**
 * @description 首页展示的拼团数据（最多2条）
 */
const twoGroupList = ref<GrouponRecordItem[]>([]);
const totalGroupCount = ref(0);

/**
 * @description 获取当前商品的拼团记录列表
 * 用于显示可参与的拼团
 */
const getGrouponRecords = async () => {
    try {
        const res = await getGrouponRecordList(getGrouponRecordsParams);
        if (res.records) {
            groupList.value = res.records;
            totalGroupCount.value = res.total;
            if (groupList.value && groupList.value.length) {
                if (groupList.value.length > 1) {
                    twoGroupList.value = groupList.value.slice(0, 2);
                } else {
                    twoGroupList.value = groupList.value;
                }
            }
        }
    } catch (error) {
        console.log(error);
    }
};

/**
 * @description 加入现有拼团
 * @param item 要加入的拼团记录
 */
const hanldeJoinGroup = (item: GrouponRecordItem) => {
    showMoreGroupon.value = false;
    nextTick(() => {
        grouponRecordInfo.value.grouponPromotionRecordId = item.grouponPromotionRecordId;
        grouponRecordInfo.value.minGroupNum = item.minGroupNum;
        grouponEnable.value = true;
        specificationRef.value.handleShowPopup();
    });
};
</script>
<style lang="scss" scoped>
.product-area-wrapper {
    position: relative;
}

.badgecolor {
    :deep(.uni-badge--error) {
        background-color: var(--general);
    }
}

.servicelist {
    flex: 1;
    column-gap: 20rpx;
    row-gap: 5rpx;

    .service-item {
        font-size: 22rpx;
        line-height: 38rpx;
        color: #81838e;
        column-gap: 8rpx;

        .base-image {
            padding-right: 10rpx;
        }
    }
}
.icon-image {
    width: 35rpx;
    height: 35rpx;
}
.productDetail-content {
    padding: 0 20rpx;

    .group-card-row {
        background-color: #fff;
        padding: 20rpx;
        border-radius: 20rpx;
        margin: 20rpx 0;

        .group-num {
            color: #1a1a1a;
            font-size: 28rpx;
            font-weight: bold;
        }

        .group-desc {
            color: #3a3a3a;
            font-size: 28rpx;
        }

        .more {
            padding-left: 8rpx;
            color: #818181;
            font-size: 24rpx;
        }

        .group {
            padding: 18rpx 0;
        }

        .instructions {
            padding-top: 40rpx;
            padding-bottom: 46rpx;
            width: 100%;
            border-top: 2rpx solid #f5f5f5;

            text {
                width: 109rpx;
                height: 27rpx;
                font-weight: bold;
                font-size: 28rpx;
                color: #2a2a2a;
                line-height: 56rpx;
            }

            .process {
                display: flex;
                justify-content: space-between;
                margin-top: 25rpx;

                .process-item {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    gap: 22rpx 0;
                    position: relative;

                    // 创建三个小圆点的连接线效果
                    &:not(:last-child)::after {
                        position: absolute;
                        display: block;
                        content: " ";
                        width: 45rpx;
                        height: 10rpx;
                        background: radial-gradient(circle, #a1a1a1 3rpx, transparent 3rpx);
                        background-size: 15rpx 10rpx;
                        background-repeat: repeat-x;
                        right: -90rpx;
                        top: 30rpx;
                    }

                    text {
                        width: 129rpx;
                        height: 23rpx;
                        font-weight: 400;
                        font-size: 24rpx;
                        color: #595959;
                        line-height: 38rpx;
                    }

                    .process-image {
                        width: 64rpx;
                        height: 64rpx;
                        background: #f5f5f5;
                        border-radius: 50%;
                    }
                }
            }
        }
    }

    .product-card-row {
        background-color: #fff;
        padding: 20rpx;
        border-radius: 20rpx;
        margin: 20rpx 0;
        display: flex;
        flex-direction: column;
        row-gap: 25rpx;

        .card-row-flex {
            display: flex;
            flex-direction: column;
            row-gap: 25rpx;
        }

        .cart-item {
            font-size: 26rpx;

            .title {
                min-width: 110rpx;
                color: #81838e;
                padding-right: 10rpx;
            }

            .attr_text {
                display: block;
                width: 66vw;
            }

            .label {
                .tips {
                    color: var(--general);
                    font-size: 24rpx;
                    margin-top: 5rpx;
                }
            }
        }

        .traceability-item {
            padding: 10rpx;
            background-color: #fdf6f0;
            color: #ff9627;
            font-size: 24rpx;

            image {
                width: 115rpx;
                height: 35rpx;
                margin-right: 20rpx;
            }
        }

        .tab-box {
            padding: 10rpx 0 20rpx 0;
            margin-bottom: 20rpx;
            font-size: 24rpx;

            .active {
                color: var(--general);
            }
        }

        .default {
            color: #9b9c9f;
            font-size: 24rpx;
            padding: 10rpx;

            .desc-pic-item {
                .slide-image {
                    display: block;
                    width: 100%;
                }
            }
        }
    }

    .coupon-list {
        background-color: #fff;
        padding: 20rpx;
        border-radius: 20rpx;
        align-items: center;

        .title {
            width: 90rpx;
            color: #81838e;
        }

        .coupon-item {
            position: relative;
            display: inline-block;
            margin-right: 20rpx;
            padding: 0 18rpx 0 24rpx;
            height: 36rpx;
            line-height: 35rpx;
            color: #fff;
            font-size: 20rpx;
            background: var(--general);

            &::before {
                content: "";
                position: absolute;
                left: -3px;
                top: 0;
                bottom: 0;
                width: 6px;
                height: 100%;
                color: var(--general);
                background: var(--general);
                -webkit-mask: radial-gradient(circle at 2px, #0000 2px, var(--general) 0);
                -webkit-mask-position: -3px;
                -webkit-mask-size: 100% 6px;
            }

            &::after {
                content: "";
                content: "";
                position: absolute;
                right: -3px;
                top: 0;
                bottom: 0;
                width: 6px;
                height: 100%;
                color: #fff;
                background: var(--general);
                -webkit-mask: radial-gradient(circle at 2px, #0000 2px, var(--general) 0);
                -webkit-mask-position: 5px;
                -webkit-mask-size: 100% 6px;
            }
        }
    }
}
.bottom-bar-box {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 110rpx;
    background-color: #fff;
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
    }
}
.attr_table-box {
    height: 60vh;
}
.attr_table {
    padding: 0 20rpx;
    .item {
        margin-bottom: 5rpx;
        font-size: 22rpx;
        .lable {
            display: inline-block;
            width: 200rpx;
            border: 1px solid #f5f4ef;
            background: #f5f4ef;
            padding: 10rpx 20rpx;
        }
        .value {
            flex: 1;
            border: 1px solid #f5f4ef;
            padding: 10rpx 20rpx;
        }
    }
}

.balance {
    border-radius: 0 0 18rpx 18rpx;
    background: #fff;
    margin-bottom: 20rpx;
    padding: 30rpx 32rpx 25rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.coupon-container-content {
    height: 100%;
    padding-bottom: 30rpx;
}
.button-position {
    position: fixed;
    bottom: 30rpx;
    left: 0;
    right: 0;
    padding: 0 30rpx;
    padding-bottom: env(safe-area-inset-bottom) !important;
}

.coupon-btn-con {
    font-size: 22rpx;
    padding: 10rpx 25rpx;
    border-radius: 40rpx;

    color: #fff;
    background-image: -webkit-linear-gradient(left, #609dde 20%, #6781da);
    &:active {
        opacity: 0.7;
    }
    &.disabled {
        background-image: -webkit-linear-gradient(left, #aaa 20%, #aaa);
    }
}

.balance-title {
    display: flex;

    .balance-text {
        font-size: 22rpx;
        padding-left: 15rpx;
        display: flex;
        align-items: flex-end;
    }
}

.shuxin {
    white-space: nowrap; /* 保持文本在一行内 */
    overflow: hidden; /* 隐藏溢出的文本 */
    text-overflow: ellipsis;
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
</style>
