<!--
 * @Author: LZH
 * @Date: 2025-06-13 09:37:26
 * @LastEditors: LZH
 * @Description: 商品详情页
-->
<template>
    <view :class="['product-detail-page', `product-detail-page--${productStyle}`]" :style="pageStyle">
        <tig-layout ref="layoutRef" :immediate-log="false" :overflow="!isOverseasStyle" :product-id="productId" :shop-id="product.shopId">
            <template v-if="!failLoad">
                <view v-show="!loadEndStatus">
                    <productNav
                        v-model="scrollTop"
                        v-model:active-module="activeModule"
                        :event="handleScrollTo"
                        :has-main-header="showMainHeader"
                        :is-collect="titleInfoRef?.isCollect ?? false"
                        :layout-style="productStyle"
                        :show-service="showService"
                        :product-id="productId"
                        :shop-info="shopInfo"
                        @share="titleInfoRef?.handleShare()"
                        @collect="titleInfoRef?.addCollect()"
                    />
                    <!-- 跨境风格（A3）：标题与已售置于主图上方，标题样式跟随装修配置 -->
                    <view v-if="isOverseasStyle" class="overseas-head">
                        <view
                            class="overseas-head__name"
                            :style="overseasNameStyle"
                            @click="showOverseasNameExpandBtn && (isOverseasNameExpanded = !isOverseasNameExpanded)"
                        >
                            <view class="overseas-head__name-inner">
                                <text v-if="isPresale" class="overseas-head__presale-tag">{{ $t("预售") }}</text>
                                {{ product.productName }}
                            </view>
                            <view v-if="showOverseasNameExpandBtn && !isOverseasNameExpanded" class="overseas-head__name-btn overseas-head__name-expand">
                                <text class="iconfont-h5 icon-a-gengduo20" />
                            </view>
                            <view v-if="showOverseasNameExpandBtn && isOverseasNameExpanded" class="overseas-head__name-btn overseas-head__name-collapse">
                                <text class="iconfont-h5 icon-a-gengduo20 iconTop" />
                            </view>
                        </view>
                        <view v-if="!isPresale && configStore.showSelledCount" class="overseas-head__sold">
                            {{ $t("已售") }}{{ product.virtualSales || 0 }}{{ $t("件") }}
                        </view>
                    </view>
                    <view class="product-area-wrapper">
                        <!-- A1 使用装修版主图；A2 使用 productV1 全宽主图；A3 按装修比例/圆角决定 -->
                        <productImgDecorate
                            v-if="useDecorateImg"
                            class="product-area"
                            :is-sub-card="isSubCard"
                            :video-list="videoList"
                            :pic-list="picList"
                            :product-info="product"
                            :seckill-value="seckillValue"
                            :groupon-value="grouponValue"
                            :product-stock="productStock"
                            :detail-loaded="detailLoaded"
                            :image-ratio="decorateConfig.imageRatio"
                            :image-margin="decorateConfig.imageMargin"
                            :image-mode="decorateConfig.imageMode"
                            :image-radius-style="decorateConfig.imageRadiusStyle"
                        />
                        <productImg
                            v-else
                            class="product-area"
                            :is-sub-card="isSubCard"
                            :video-list="videoList"
                            :pic-list="picList"
                            :product-info="product"
                            :seckill-value="seckillValue"
                            :groupon-value="grouponValue"
                            :product-stock="productStock"
                            :detail-loaded="detailLoaded"
                            :image-ratio="decorateConfig.imageRatio"
                            :image-margin="decorateConfig.imageMargin"
                            :image-mode="decorateConfig.imageMode"
                            :image-radius-style="decorateConfig.imageRadiusStyle"
                        />
                    </view>
                    <view
                        class="productDetail-content"
                        :class="{ 'productDetail-content--vortmall': isVortmallStyle, 'productDetail-content--overseas': isOverseasStyle }"
                        :style="{ 'padding-bottom': productContentBottomPadding }"
                    >
                        <view v-if="isWholesaleMode" class="wholesale-price-block">
                            <WholesalePriceTable
                                :model="wholesaleDisplayModel"
                                :price-list="wholesalePriceList"
                                :current-grade-id="wholesaleStore.identity.gradeId"
                                :retail-max-price="wholesaleRetailMax"
                                :moq="wholesaleMoq"
                            />
                            <!-- A3 已在主图上方显示商品名称，批发价格块不再重复渲染。 -->
                            <view v-if="!isOverseasStyle" class="wholesale-product-name">
                                <view class="wholesale-product-name-inner" :style="wholesaleProductNameStyle">{{ product.productName }}</view>
                            </view>
                        </view>

                        <!-- A1 批发态：标题组件会跳过内部插槽，预约信息和门店入口在价格块后独立渲染。 -->
                        <template v-if="isVortmallStyle && isWholesaleMode">
                            <view v-if="isReservation" class="vortmall-reserve-card">
                                <view class="reserve-row">
                                    <text class="reserve-label">{{ $t("预约方式") }}</text>
                                    <text class="reserve-value">{{ $t(reserveModeText) }}</text>
                                </view>
                                <view class="reserve-row">
                                    <text class="reserve-label">{{ $t("说明") }}</text>
                                    <text class="reserve-value">{{ $t(reserveTipText) }}</text>
                                </view>
                            </view>
                        </template>

                        <productTitleInfo
                            ref="titleInfoRef"
                            :layout-style="productStyle"
                            :title-color="decorateConfig.titleColor || ''"
                            :price-color="decorateConfig.priceColor || ''"
                            :market-price-color="decorateConfig.marketPriceColor || ''"
                            :title-size="decorateConfig.titleSize || ''"
                            :title-max-lines="decorateConfig.titleMaxLines || 0"
                            :is-wholesale-mode="isWholesaleMode"
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
                            :presale-payment-time-text="PaymentTimeText"
                            :presale-delivery-time-text="deliverTimeText"
                            :sold-count="product.virtualSales || 0"
                        >
                            <template #before-store-switch>
                                <view v-if="isVortmallStyle && isReservation" class="vortmall-reserve-card">
                                    <view class="reserve-row">
                                        <text class="reserve-label">{{ $t("预约方式") }}</text>
                                        <text class="reserve-value">{{ $t(reserveModeText) }}</text>
                                    </view>
                                    <view class="reserve-row">
                                        <text class="reserve-label">{{ $t("说明") }}</text>
                                        <text class="reserve-value">{{ $t(reserveTipText) }}</text>
                                    </view>
                                </view>
                            </template>
                            <!-- O2O A1：当前门店与切换入口位于价格和优惠券之间 -->
                            <template #store-switch>
                                <!-- 同一装修风格下门店卡统一使用 A1 标准样式；活动商品仅调整渲染位置，不改变卡片样式。 -->
                            </template>
                        </productTitleInfo>



                        <!-- A3 活动优惠券统一紧跟价格活动条，对齐海外版商详。 -->
                        <view
                            v-if="
                                !isWholesaleMode &&
                                isOverseasStyle &&
                                (isSeckill || isDiscount || isGroupon || isPresale || isCycleBuy) &&
                                promotionList.length > 0
                            "
                            class="overseas-activity-coupon-box"
                        >
                            <view class="overseas-activity-coupon" @click="showCoupon = true">
                                <view class="coupon-title line1">{{ promotionList[0]?.promotionName || $t("优惠活动") }}</view>
                                <view class="coupon-more">{{ $t("更多优惠券") }}<text class="iconfont-h5 icon-a-gengduo20" /></view>
                            </view>
                        </view>

                        <!-- A1 活动态标题不渲染标题组件内的门店插槽，改在活动标题下方补齐同一入口。 -->

                        <!-- A1 预售优惠券位于门店入口下方，对齐 vortmall 商详的信息顺序。 -->
                        <view v-if="showVortmallStandaloneCoupon" class="vortmall-presale-coupon" @click="showCoupon = true">
                            <view class="coupon-content">
                                <view class="coupon-title line1">{{ promotionList[0]?.promotionName || $t("优惠活动") }}</view>
                                <view class="coupon-more">{{ $t("更多优惠券") }}<text class="iconfont-h5 icon-a-gengduo20" /></view>
                            </view>
                        </view>

                        <!-- A1 标准风：服务保障使用与后台预览一致的独立卡片 -->
                        <serviceCard v-if="isVortmallStyle && serviceList.length > 0" :service-list="serviceList" :separated="showStoreSwitch" />

                        <merchantShopInfo
                            v-if="showMerchantShop && isVortmallStyle"
                            :shop-info="shopInfo"
                            variant="vortmall"
                            @refresh-shop-detail="getShopInfo"
                        />

                        <!-- A1 补齐旧版除规格外的交易提示；规格快捷入口按当前装修方案保留不展示。 -->
                        <view
                            v-if="!isWholesaleMode && isGroupon && totalGroupCount"
                            class="group-card-row"
                            :class="{ 'group-card-row--vortmall': isVortmallStyle }"
                        >
                            <view class="flex justify-between align-center">
                                <view class="group-num">{{ grouponValue.joinNumCount }}{{ $t("人在拼单，可直接参与") }}</view>
                                <view class="flex align-center group-desc" @click="handleShowMore"
                                    >{{ $t("查看更多") }}
                                    <view class="iconfont-h5 icon-gengduo1 more" />
                                </view>
                            </view>
                            <view class="group">
                                <productGroupon :groups="twoGroupList" padding="" :vortmall="isVortmallStyle" @join-click="handleJoinGroup" />
                            </view>
                            <view class="instructions">
                                <text class="">{{ $t("拼团说明") }}</text>
                                <view class="process">
                                    <view class="process-item">
                                        <image class="process-image" :src="staticResource('groupon/groupon_open.png')" />
                                        <text class="process-text-nowrap">{{ $t("1.开团/参加") }}</text>
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

                        <view v-if="!isOverseasStyle && !isVortmallStyle" class="product-card-row">
                            <view class="cart-item flex align-center justify-between" @click="onShowSpecification">
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

                                    <template v-if="attrList.normal?.length > 0 && !isOverseasStyle">
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
                                                <image class="icon-image" :src="staticResource('common/more.png')" />
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
                                                <image class="icon-image" :src="staticResource('common/more.png')" />
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
                                    <template v-if="serviceList.length > 0 && !isVortmallStyle">
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

                        <merchantShopInfo v-if="showMerchantShop && isO2oStyle" :shop-info="shopInfo" variant="o2o" @refresh-shop-detail="getShopInfo" />


                        <merchantShopInfo
                            v-if="showMerchantShop && isOverseasStyle"
                            :shop-info="shopInfo"
                            variant="overseas"
                            @refresh-shop-detail="getShopInfo"
                        />

                        <!-- A1 / A3：规格参数使用独立折叠卡，对齐后台预览与 VortMall -->
                        <attrCard
                            v-if="(isVortmallStyle || isOverseasStyle) && attrList.normal?.length > 0"
                            :normal="attrList.normal"
                            :overseas="isOverseasStyle"
                        />

                        <!-- A1 / A3 使用标准评价卡；A2 保留门店版评价与咨询入口 -->
                        <evaluateCard
                            v-if="configStore.productCommentEnabled === 1 && (isVortmallStyle || isOverseasStyle)"
                            class="evaluate-area"
                            :product-id="product.productId"
                            :overseas="isOverseasStyle"
                        />
                        <productComment
                            v-else-if="configStore.productCommentEnabled === 1"
                            class="evaluate-area"
                            :product-id="product.productId"
                            :shop-id="shopId > -1 ? shopId : undefined"
                        />

                        <!-- 跨境风格（A3）：售后服务 / 商品描述 / 商品详情 三张可展开卡片，对齐 tigshop-overseas -->
                        <template v-if="isOverseasStyle">
                            <afterSaleCard :id="product.productId" :overseas="true" />
                            <productDescription :product="product" :overseas="true" />
                            <descriptionCard
                                v-model:show-more="descShowMore"
                                :is-buy="product.isBuy"
                                :paid-content="product.paidContent"
                                :product-type="product.productType"
                                :desc-arr="descArr"
                                :product-id="product.productId"
                                :overseas="true"
                                class="product-detail-area"
                            />
                        </template>
                        <descriptionCard
                            v-else-if="isVortmallStyle"
                            v-model:show-more="descShowMore"
                            :is-buy="product.isBuy"
                            :paid-content="product.paidContent"
                            :product-type="product.productType"
                            :desc-arr="descArr"
                            :product-id="product.productId"
                            class="product-detail-area"
                        />
                        <productContent
                            v-else
                            :is-buy="product.isBuy"
                            :paid-content="product.paidContent"
                            :product-type="product.productType"
                            :desc-arr="descArr"
                            :product-id="product.productId"
                            class="product-detail-area"
                        />
                    </view>

                    <ProductActionBar
                        ref="actionBarRef"
                        :product="product"
                        :layout-style="productStyle"
                        :decorate-config="actionBarDecorate"
                        :product-stock="productStock"
                        :type="type"
                        :is-groupon="isGroupon"
                        :is-presale="isPresale"
                        :product-presale="productPresale"
                        :is-cycle-buy="isCycleBuy"
                        :is-sub-card="isSubCard"
                        :is-sub-card-reservation-product="isSubCardReservationProduct"
                        :is-reservation="isReservation"
                        :detail-loaded="detailLoaded"
                        :cart-count="cartCount"
                        :show-service="showService"
                        :shop-info="shopInfo"
                        :product-id="productId"
                        :member-restricted="memberRestricted"
                        :member-restrict-tip="memberRestrictTip"
                        :groupon-restricted="grouponValue?.canJoin === false"
                        @show-specification="onShowSpecification"
                        @show-groupon-popup="onShowGrouponPopup"
                        @to-home="handleToHome"
                        @to-product-list="handleToProductList"
                        @to-cart="toPage(userStore.token ? '/pages/cartJump/index' : '/pages/cart/index')"
                    />

                    <WholesaleSkuPopup
                        v-if="isWholesaleMode"
                        ref="wholesaleSkuRef"
                        :product="product"
                        :sku-list="skuList"
                        :attr-list="attrList"
                        :price-list="wholesalePriceList"
                        :current-grade-id="wholesaleStore.identity.gradeId"
                        :wholesale-price-mode="wholesaleProductPriceMode"
                        :buy-rule="wholesaleBuyRule"
                        @add-success="onWholesaleAddSuccess"
                        @display-change="wholesaleDisplayModel = $event"
                    />
                    <specification
                        v-if="!isWholesaleMode"
                        ref="specificationRef"
                        v-model="specificationId"
                        v-model:is-cycle-buy="isCycleBuy"
                        v-model:is-sub-card="isSubCard"
                        v-model:sku-id="skuId"
                        v-model:shop-id="shopId"
                        :type="type"
                        :groupon-enable="grouponEnable"
                        :groupon-record-info="grouponRecordInfo"
                        :layout-style="productStyle"
                        :decorate-config="actionBarDecorate"
                        @load-end="getLoadStatus"
                        @add-card-succeed="getShowAnimation"
                        @send-value="getValue"
                        @close="grouponEnable = true"
                    />

                    <productCoupon
                        v-if="!isWholesaleMode"
                        v-model="showCoupon"
                        :product-id="Number(productId)"
                        :promotion-list="promotionList"
                        :shop-id="shopId"
                    />
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
                        v-if="!isWholesaleMode"
                        v-model="showMoreGroupon"
                        :product-id="product.productId"
                        :group-list="groupList"
                        :total="totalGroupCount"
                        @join-click="handleJoinGroup"
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
import productImgDecorate from "./src/productImgDecorate.vue";
import productTitleInfo from "./src/productTitleInfo.vue";
import productComment from "./src/productComment.vue";
import evaluateCard from "./src/evaluateCard.vue";
import merchantShopInfo from "./src/merchantShopInfo.vue";
import specification from "@/components/product/specification.vue";
import productCoupon from "./src/productCoupon.vue";
import productGrouponMore from "./src/productGrouponMore.vue";
import productContent from "./src/productContent.vue";
import attrCard from "./src/attrCard.vue";
import afterSaleCard from "./src/afterSaleCard.vue";
import serviceCard from "./src/serviceCard.vue";
import productDescription from "./src/productDescription.vue";
import descriptionCard from "./src/descriptionCard.vue";
import animation from "./src/animation.vue";
import error from "./src/error.vue";
import recommend from "@/components/recommend/index.vue";
import ProductActionBar from "./components/ProductActionBar.vue";
import WholesaleSkuPopup from "@/components/wholesale/WholesaleSkuPopup.vue";
import WholesalePriceTable from "@/components/wholesale/WholesalePriceTable.vue";
import { useWholesale } from "./composables/useWholesale";
import { productImage } from "@/utils/format";
import { nextTick, ref, getCurrentInstance, computed, watch } from "vue";
import { onLoad, onShow, onReady, onUnload, onShareAppMessage, onShareTimeline } from "@dcloudio/uni-app";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";
import { useCartStore } from "@/store/cart";
import { asyncGetCartCount } from "@/api/cart/cart";
import { getH5ProductDetailDecorate } from "@/api/home/home";
import type { SkuPromotion, GrouponDetail } from "@/types/product/product";
import type { PresaleInfo } from "@/types/presale/presale";
import { redirect, staticResource, isB2B, parseQueryString, isMerchant, isO2o, isOverseas } from "@/utils";
import { resolveMiniScene } from "@/api/common";
import { useI18n } from "vue-i18n";
import { useScrollTop } from "@/hooks";
import { useProductDetail, useProductType, useProductAction, useProductStyle, type ProductStyle } from "./composables";

const { t } = useI18n();

const configStore = useConfigStore();
const userStore = useUserStore();
const cartStore = useCartStore();
const { scrollTop } = useScrollTop();
const instance = getCurrentInstance();
const systemInfo = ref<any>(uni.getSystemInfoSync());

// 页面级状态
const type = ref("");
const specificationRef = ref();
const titleInfoRef = ref();
const layoutRef = ref();
const actionBarRef = ref();
const showAttrRef = ref(false);
const showCoupon = ref(false);
const formType = ref(false);
const activeModule = ref(".product-area");
const animationPosition = ref({
    top: 0,
    left: 0
});
const showAnimation = ref(false);
const cartCount = ref<number>(0);

// ===== 商详装修配置（pageType=8）=====
interface ProductDecorateConfig {
    /** 展示风格：A1-标准 / A2-门店 / A3-跨境 */
    decorateStyle?: ProductStyle;
    /** 主图比例 */
    imageRatio?: "4:3" | "4:5" | "1:1";
    /** 轮播图左右边距（px） */
    imageMargin?: number | string;
    /** 主图显示模式：1-完整显示，2-铺满裁切 */
    imageMode?: number | string;
    /** 主图圆角 */
    imageRadiusStyle?: "square" | "round" | "large";
    /** 标题大小 */
    titleSize?: "large" | "medium" | "small";
    /** 标题颜色 */
    titleColor?: string;
    /** 标题最多显示行数 */
    titleMaxLines?: 1 | 2 | 3;
    /** 价格颜色 */
    priceColor?: string;
    /** 商品划线价颜色 */
    marketPriceColor?: string;
    /** 购买栏样式：1-圆润 2-直角 */
    buyBarStyle?: 1 | 2;
    /** 按钮圆角 */
    buttonRadius?: number;
    /** 底部是否显示首页入口 */
    showHome?: boolean;
    /** 底部是否显示客服入口 */
    showService?: boolean;
    /** 底部是否显示购物车入口 */
    showCart?: boolean;
    /** 是否显示店铺信息 */
    showShop?: boolean;
    /** 页面背景色 */
    backgroundColor?: string;
    /** 内容区块背景色 */
    contentBackgroundColor?: string;
    /** 是否显示 A3 顶部商城头部 */
    showHeader?: boolean;
}
const decorateConfig = ref<ProductDecorateConfig>({});
const decorateModuleType = ref<string | undefined>();
const decorateStyle = computed(() => decorateConfig.value.decorateStyle);

// ===== 商详风格决策（A1 / A2 / A3 三风格切换入口）=====
// 优先 decorateStyle；缺失时用模块 type（productDetailA1/A2/A3）反推；再兜底运行环境
const { productStyle, isVortmallStyle, isO2oStyle, isOverseasStyle } = useProductStyle({
    decorateStyle,
    moduleType: decorateModuleType
});

// A1 / A3 始终使用装修版轮播，配置异步到达时只更新属性，不切换整个组件。
const useDecorateImg = computed(() => isVortmallStyle.value || isOverseasStyle.value);

// 页面底色覆盖模块间距；内容底色控制各内容卡片。旧数据未配置内容底色时保持白色。
const pageStyle = computed<Record<string, string>>(() => {
    const configuredPageBackground = decorateConfig.value.backgroundColor;
    // A2 历史默认值为 transparent，会透出 tig-layout 的白底，看起来像“背景色无效”；统一迁移为门店风浅灰底。
    const pageBackground =
        productStyle.value === "A2" && (!configuredPageBackground || configuredPageBackground === "transparent") ? "#f5f5f5" : configuredPageBackground;
    const contentBackground = decorateConfig.value.contentBackgroundColor || "#ffffff";
    const fallbackPageBackground = isVortmallStyle.value ? "#f7f7f7" : "#f5f5f5";
    const style = {
        "--product-page-bg": pageBackground || fallbackPageBackground,
        "--product-content-bg": contentBackground,
        "--product-title-color": decorateConfig.value.titleColor || "#1a1a1a",
        "--product-price-color": decorateConfig.value.priceColor || "#2a3145",
        "--product-market-price-color": decorateConfig.value.marketPriceColor || "#999"
    };
    return pageBackground ? { ...style, backgroundColor: pageBackground } : style;
});

// 装修字段可能以字符串形式下发，统一归一：布尔（"false" 视为 false）、数字
const toDecorateBool = (val: unknown) => {
    if (val === undefined || val === null || val === "") return undefined;
    return !(val === false || val === "false" || val === 0 || val === "0");
};
const toDecorateNumber = (val: unknown) => {
    if (val === undefined || val === null || val === "") return undefined;
    return Number(val);
};

// A3 头部是否展示由运行环境、装修风格与显隐配置共同决定；兼容接口下发 false / 0 / "0"。
const showMainHeader = computed(() => isOverseas() && isOverseasStyle.value && toDecorateBool(decorateConfig.value.showHeader) !== false);

// A1 / A2 / A3 功能区保持一致：O2O 普通商品均可查看当前门店并切换门店。
// shopId 未落地时 productShopInfo 会显示商城兜底信息，仍可携带 productId 进入门店列表。
const showStoreSwitch = computed(() => isO2o() && type.value !== "exchange");

// productTitleInfo 的门店插槽位于普通商品标题分支；活动态与活动背景态需由页面补渲染。
const showVortmallActivityStoreSwitch = computed(
    () =>
        isVortmallStyle.value &&
        showStoreSwitch.value &&
        !!(isSeckill.value || isDiscount.value || isGroupon.value || isCycleBuy.value || isSubCard.value || isPresale.value || activityBgImage.value)
);

const showVortmallStandaloneCoupon = computed(
    () =>
        isVortmallStyle.value &&
        promotionList.value.length > 0 &&
        !!(isSeckill.value || isDiscount.value || isGroupon.value || isCycleBuy.value || isSubCard.value || isPresale.value || activityBgImage.value)
);

// 底栏装修配置
const actionBarDecorate = computed(() => ({
    buyBarStyle: toDecorateNumber(decorateConfig.value.buyBarStyle) as 1 | 2 | undefined,
    buttonRadius: toDecorateNumber(decorateConfig.value.buttonRadius),
    showHome: toDecorateBool(decorateConfig.value.showHome),
    showService: toDecorateBool(decorateConfig.value.showService),
    showCart: toDecorateBool(decorateConfig.value.showCart)
}));

/**
 * @description 拉取商详装修配置（pageType=8）
 * 匹配 productDetailA1/A2/A3；decorateStyle 缺失时按 type 回填，保证风格与模块一致
 */
const fetchDecorateConfig = async (previewId?: string | number) => {
    try {
        const result = await getH5ProductDetailDecorate(previewId);
        const moduleList = Array.isArray(result?.moduleList) ? result.moduleList : [];
        const item = moduleList.find((m) => ["productDetailA1", "productDetailA2", "productDetailA3"].includes(m.type ?? "")) ?? moduleList[0];
        const moduleData = { ...(item?.module || {}) } as ProductDecorateConfig;
        const type = item?.type ?? "";
        decorateModuleType.value = type || undefined;
        // 接口未回 decorateStyle 时，按模块 type 回填，避免 A1 误落成门店风
        if (!moduleData.decorateStyle) {
            if (type === "productDetailA1") moduleData.decorateStyle = "A1";
            else if (type === "productDetailA2") moduleData.decorateStyle = "A2";
            else if (type === "productDetailA3") moduleData.decorateStyle = "A3";
        }
        decorateConfig.value = moduleData;
    } catch (error) {
        console.error(error);
        decorateConfig.value = {};
        decorateModuleType.value = undefined;
    }
};
// ===== 商品详情数据 =====
const detail = useProductDetail({
    type,
    onDetailLoaded: (result?: any) => {
        // 落地批发字段；命中批发态时 specification 被 v-if 移除、不触发 @load-end，手动置位以关闭 loading 遮罩
        wholesale.populate(result);
        if (wholesale.isWholesaleMode.value) {
            specLoaded.value = true;
        }
        nextTick(() => {
            layoutRef.value?.handleLog();
        });
    }
});
const {
    productId,
    specificationId,
    skuId,
    product,
    picList,
    videoList,
    attrList,
    skuList,
    serviceList,
    descArr,
    productPrice,
    productStock,
    exchangeIntegral,
    promotionList,
    shopId,
    storeInfo,
    shopInfo,
    cycleInfo,
    isCycleBuy,
    isSubCard,
    activityBgImage,
    loadEndStatus,
    detailLoaded,
    specLoaded,
    failLoad,
    getProductDetail,
    getShopInfo,
    getLoadStatus,
    clearJsonLd
} = detail;

const showMerchantShop = computed(
    () =>
        isMerchant() &&
        type.value !== "exchange" &&
        toDecorateBool(decorateConfig.value.showShop) !== false &&
        Object.keys(shopInfo.value).length > 0 &&
        Number.isInteger(shopInfo.value.shopId) &&
        shopInfo.value.shopId >= 0
);

// 跨境风格（A3）商品详情卡展开态
const descShowMore = ref(false);

// A3 固定购买栏高 120rpx（H5 其他风格保持原 100rpx）；售罄提示再额外占用 60rpx，避免遮住最后一个模块的底部间距。
const productContentBottomPadding = computed(() => {
    const actionBarHeight = isOverseasStyle.value ? 120 : 100;
    const hasProductStatus = detailLoaded.value && (product.productStatus === 0 || productStock.value === 0);
    const statusHeight = hasProductStatus ? 60 : 0;
    let paddingBottom = `${actionBarHeight + statusHeight}rpx`;

    // #ifdef MP-WEIXIN
    // 微信端底栏实际为 110rpx；安全区是 px。A3 内部已有 35rpx，仅补足超出的部分。
    const miniActionBarHeight = isOverseasStyle.value ? 120 : 110;
    const windowWidth = systemInfo.value.windowWidth || uni.getSystemInfoSync().windowWidth || 375;
    const overseasBaseBottom = (35 * windowWidth) / 750;
    const safeBottom = isOverseasStyle.value ? Math.max(configStore.safeBottom - overseasBaseBottom, 0) : configStore.safeBottom;
    paddingBottom = `calc(${miniActionBarHeight + statusHeight}rpx + ${safeBottom}px)`;
    // #endif

    return paddingBottom;
});

// A3 标题装修：字号与 Admin 预览的 14 / 15 / 16px 对应为 28 / 30 / 32rpx。
const overseasTitleSizeMap: Record<NonNullable<ProductDecorateConfig["titleSize"]>, number> = {
    small: 28,
    medium: 30,
    large: 32
};
const isOverseasNameExpanded = ref(false);
const showOverseasNameExpandBtn = ref(false);

const overseasNameStyle = computed<Record<string, string>>(() => {
    const titleSize = overseasTitleSizeMap[decorateConfig.value.titleSize || "small"] || overseasTitleSizeMap.small;
    const configuredLines = toDecorateNumber(decorateConfig.value.titleMaxLines);
    const titleMaxLines = configuredLines && configuredLines >= 1 && configuredLines <= 3 ? configuredLines : 2;

    const style: Record<string, string> = {
        color: decorateConfig.value.titleColor || "#1a1a1a",
        fontSize: `${titleSize}rpx`,
        lineHeight: "1.45",
        wordBreak: "break-all"
    };
    if (!isOverseasNameExpanded.value) {
        style.display = "-webkit-box";
        style.WebkitBoxOrient = "vertical";
        style.WebkitLineClamp = String(titleMaxLines);
        style.overflow = "hidden";
    }
    return style;
});

const measureOverseasName = (retries = 3) => {
    if (!isOverseasStyle.value || !product.value?.productName) return;
    const titleSize = overseasTitleSizeMap[decorateConfig.value.titleSize || "small"] || overseasTitleSizeMap.small;
    const configuredLines = toDecorateNumber(decorateConfig.value.titleMaxLines);
    const titleMaxLines = configuredLines && configuredLines >= 1 && configuredLines <= 3 ? configuredLines : 2;
    const lineHeight = uni.upx2px(titleSize * 1.45);
    uni.createSelectorQuery()
        .in(instance)
        .select(".overseas-head__name-inner")
        .boundingClientRect((rect: any) => {
            if (rect && rect.height > 0) {
                const overflows = rect.height > lineHeight * (titleMaxLines + 0.25);
                showOverseasNameExpandBtn.value = overflows;
                if (overflows) isOverseasNameExpanded.value = false;
                else isOverseasNameExpanded.value = false;
            } else if (retries > 0) {
                setTimeout(() => measureOverseasName(retries - 1), 200);
            }
        })
        .exec();
};

const checkOverseasNameOverflow = () => {
    if (!isOverseasStyle.value || !product.value?.productName) return;
    // 先展开测量完整文本高度，再恢复到默认折叠状态。
    isOverseasNameExpanded.value = true;
    nextTick(() => setTimeout(() => measureOverseasName(), 150));
};

watch(
    [() => product.value?.productName, productStyle, () => decorateConfig.value.titleSize, () => decorateConfig.value.titleMaxLines],
    () => checkOverseasNameOverflow(),
    { flush: "post" }
);

// 批发价格块沿用商详装修的标题颜色、字号和最大行数配置。
const wholesaleTitleSizeMap: Record<NonNullable<ProductDecorateConfig["titleSize"]>, number> = {
    small: 28,
    medium: 31,
    large: 36
};
const wholesaleProductNameStyle = computed<Record<string, string>>(() => {
    const titleSize = wholesaleTitleSizeMap[decorateConfig.value.titleSize || "medium"] || wholesaleTitleSizeMap.medium;
    const configuredLines = toDecorateNumber(decorateConfig.value.titleMaxLines);
    const titleMaxLines = configuredLines && configuredLines >= 1 && configuredLines <= 3 ? configuredLines : 2;
    const style: Record<string, string> = {
        color: decorateConfig.value.titleColor || "#1a1a1a",
        fontSize: `${titleSize}rpx`,
        lineHeight: "42rpx",
        display: "-webkit-box",
        "-webkit-box-orient": "vertical",
        // 使用显式属性名，避免 uni-app H5 将 WebkitLineClamp 转换失败。
        "-webkit-line-clamp": String(titleMaxLines),
        lineClamp: String(titleMaxLines),
        overflow: "hidden",
        // 行数约束放在无 padding 的 inner 节点上，避免 padding 参与 line-clamp 计算。
        maxHeight: `${42 * titleMaxLines}rpx`
    };
    // uni-view 在部分 H5/小程序渲染器中对 line-clamp 只截断文本、不收缩节点高度，补充高度上限避免名称撑破价格卡。
    return style;
});

// ===== 商品类型判断 =====
const { isReservation, isPresale, productPresale, isSubCardReservationProduct, reserveModeText, reserveTipText } = useProductType({
    product,
    isSubCard
});

// ===== 商品操作 / 拼团 =====
const action = useProductAction({ specificationRef });
const {
    showMoreGroupon,
    grouponEnable,
    grouponRecordInfo,
    groupList,
    twoGroupList,
    totalGroupCount,
    showSpecification,
    showGrouponPopup,
    handleShowMore,
    handleJoinGroup,
    getGrouponRecords
} = action;

// ===== B2B 批发能力（身份驱动，与零售 specification 互斥）=====
const wholesaleSkuRef = ref<InstanceType<typeof WholesaleSkuPopup>>();
const wholesale = useWholesale({ product, skuList, productPrice });
const {
    wholesaleStore,
    wholesalePriceList,
    wholesaleProductPriceMode,
    wholesaleBuyRule,
    wholesaleDisplayModel,
    wholesaleRetailMax,
    wholesaleMoq,
    onWholesaleAddSuccess
} = wholesale;

const isWholesaleMode = computed(() => {
    return wholesale.isWholesaleMode.value;
    return false;
});

// 批发身份接口与商品详情接口可能存在竞态：详情先返回时，批发模式尚未切换，
// specification 会先挂载后又被批发态移除，导致其 loadEnd 事件无法触发。监听两
// 个闸门，确保身份异步完成后也能关闭详情页 loading 遮罩。
watch(
    [isWholesaleMode, detailLoaded],
    ([wholesaleMode, loaded]) => {
        if (wholesaleMode && loaded && !specLoaded.value) {
            getLoadStatus();
        }
    },
    { flush: "post" }
);

/**
 * @description 规格入口（加购 / 立即购买 / 单独购买）：批发态短路唤起批发弹层，否则委托零售逻辑
 * @param isQuick true=立即购买（加车后进结算页）；其余（含点击事件对象）=加入购物车
 */
const onShowSpecification = (isQuick?: unknown) => {
    if (isWholesaleMode.value) {
        wholesaleSkuRef.value?.handleShowPopup(isQuick === true);
        return;
    }
    showSpecification();
};

/**
 * @description 开团入口：批发态短路唤起批发弹层（批发不参与拼团开团，退化为加购），否则委托零售开团
 */
const onShowGrouponPopup = () => {
    if (isWholesaleMode.value) {
        wholesaleSkuRef.value?.handleShowPopup(false);
        return;
    }
    showGrouponPopup();
};

// ===== SKU 回传状态（number 语义，兼容 productTitleInfo / specification props）=====
const skuStr = ref<string>("");
const productOriginPrice = ref<string | number | null>(null);
const productNumber = ref<number>(1);
const isSeckill = ref<number>(0);
const seckillValue = ref<any>({});
const isDiscount = ref<number>(0);
const discountValue = ref<any>({});
const discountEndTime = ref<string>("");
const isGroupon = ref<number>(0);
const grouponValue = ref<any>({});

// 秒杀属于活动资格购买；限时折扣不满足资格时仍可按普通价格购买
const memberRestricted = computed(() => {
    if (isSeckill.value) return seckillValue.value?.canJoin === false;
    return false;
});
const memberRestrictTip = computed(() => {
    return (isSeckill.value ? seckillValue.value?.restrictTip : discountValue.value?.restrictTip) || "";
});


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
 * @description 显示规格参数弹窗
 */
const showDrawer = () => {
    showAttrRef.value = true;
};

/**
 * @description 接收规格组件传递的商品信息
 * 包含价格、库存、促销活动、拼团信息等
 * @param val 商品规格相关数据
 */
const getValue = (val: Ival) => {
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
        getGrouponRecords(product.value.productId as number);
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

/**
 * @description 获取购物车商品数量（用于底部栏角标显示）
 */
const _getCartCount = async () => {
    try {
        const result = await asyncGetCartCount();
        const count = Number(result ?? 0);
        cartCount.value = count;
        // A3 跨境风格的顶部购物车使用全局 store 角标，需与详情页本地数量同步。
        cartStore.setCartCount(count);
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

/**
 * @description 保存分销员推广商品关系
 * 用于追踪分销员推广业绩
 * @param salesmanId 分销员ID
 * @param pid 商品ID
 */
const saveSalesmanProducts = (salesmanId: string, pid: string) => {
    let salesmanProducts = uni.getStorageSync("salesmanProducts") || [];
    if (salesmanProducts.length > 0) {
        const index = salesmanProducts.findIndex((item: any) => item.productId == pid);
        if (index !== -1) {
            salesmanProducts[index].salesmanId = salesmanId;
        }
    }
    salesmanProducts = [...salesmanProducts, { salesmanId, productId: pid }];
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
            getProductDetail({ sn: option.sn });
        } else if (option.id) {
            getProductDetail(String(option.id));
        }
        if (option.skuId) {
            skuId.value = Number(option.skuId);
        }
        if (option.salesmanId) {
            const parsedSalesmanId = Number(option.salesmanId);
            if (Number.isInteger(parsedSalesmanId) && parsedSalesmanId > 0) {
                saveSalesmanProducts(String(parsedSalesmanId), String(option.id));
                uni.setStorageSync("bindSalesmanId", parsedSalesmanId);
            }
        }
    }
};

onLoad(async (option) => {
    fetchDecorateConfig(option?.previewId);

    uni.$on("refreshStoreInfo", (id: number) => {
        if (id > -1) {
            shopId.value = id;

            nextTick(() => {
                if (option?.sn) {
                    getProductDetail({ sn: option.sn });
                } else if (option?.id) {
                    getProductDetail(String(option.id));
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

const cartRect = ref({
    left: 0,
    top: 0
});

/**
 * @description 触发加入购物车动画
 * 底部操作栏已抽为子组件，节点位置须通过子组件自身实例查询，
 * 以兼容小程序自定义组件的节点边界
 * @param val 是否显示动画
 */
const getShowAnimation = async (val: boolean) => {
    // 购物车入口（飞入终点）
    const cartPos = await actionBarRef.value?.getCartPositionRect();
    if (cartPos) {
        cartRect.value.top = cartPos.top - cartPos.height / 2;
        cartRect.value.left = cartPos.left + cartPos.width / 2;
    }
    // 加购按钮（飞出起点）
    const cartBtn = await actionBarRef.value?.getCartBtnRect();
    if (cartBtn) {
        animationPosition.value.top = cartBtn.top - cartBtn.height / 2;
        animationPosition.value.left = cartBtn.left + cartBtn.width / 2;
    }

    showAnimation.value = val;
    _getCartCount();
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
        imageUrl: productImage(product.value)
    };
});

onShareTimeline(() => {
    return {
        title: product.value.productName,
        imageUrl: productImage(product.value)
    };
});

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
</script>
<style lang="scss" scoped>
.product-area-wrapper {
    position: relative;
    background: var(--product-content-bg, #fff);
}

/* @vort-if common.wholesale */
.wholesale-price-block {
    overflow: hidden;
    background: var(--product-content-bg, #fff);
    border-radius: 12rpx;
}

.wholesale-product-name {
    padding: 0 28rpx 24rpx;
    color: var(--product-title-color, #1a1a1a);
    font-size: 30rpx;
    font-weight: 600;
    line-height: 42rpx;
    word-break: break-all;
}
/* @vort-endif */

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
    background: var(--product-page-bg, #f5f5f5);

    .group-card-row {
        background-color: var(--product-content-bg, #fff);
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
        background-color: var(--product-content-bg, #fff);
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
        background-color: var(--product-content-bg, #fff);
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
    background: var(--product-content-bg, #fff);
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

/* 标准风 A1：白底通栏、弱化门店风卡片圆角，对齐后台预览 */
.product-detail-page--A1 {
    background: var(--product-page-bg, #f7f7f7);

    .productDetail-content--vortmall {
        padding: 0;
        background: var(--product-page-bg, #f7f7f7);

        .product-card-row {
            margin: 16rpx 0 0;
            border-radius: 0;
            padding: 24rpx 32rpx;
        }

        .group-card-row:not(.group-card-row--vortmall) {
            margin: 16rpx 0 0;
            border-radius: 0;
        }

        .group-card-row--vortmall {
            margin: 20rpx 20rpx 14rpx;
            border-radius: 20rpx;

            .group-desc .more {
                white-space: nowrap;
            }

            .process-text-nowrap {
                width: auto;
                white-space: nowrap;
            }
        }

        :deep(.attr-card-box),
        :deep(.evaluate-area) {
            margin: 14rpx 0;
            border-radius: 0;
        }

        :deep(.product-detail-area) {
            margin: 14rpx 0 28rpx;
            border-radius: 0;
        }
    }

    .product-area-wrapper {
        background: var(--product-content-bg, #fff);
    }
}

.vortmall-presale-coupon {
    padding: 20rpx 28rpx 35rpx;
    background: var(--product-content-bg, #fff);

    .coupon-content {
        display: flex;
        align-items: center;
        width: 100%;
        padding: 21rpx;
        border-radius: 14rpx;
        background: #f7f8fc;
    }

    .coupon-title {
        flex: 1;
        color: var(--general);
        font-size: 25rpx;
        font-weight: 700;
    }

    .coupon-more {
        flex-shrink: 0;
        display: flex;
        align-items: center;
        gap: 8rpx;
        margin-left: 10rpx;
        color: #333;
        font-size: 24rpx;

        .iconfont-h5 {
            color: #888b94;
            font-size: 16rpx;
        }
    }
}

.vortmall-reserve-card {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
    margin-top: 20rpx;
    padding: 22rpx;
    border-radius: 14rpx;
    background: #f7f8fc;

    .reserve-row {
        display: flex;
        gap: 16rpx;
        line-height: 36rpx;
    }

    .reserve-label {
        flex: 0 0 112rpx;
        color: #999;
        font-size: 24rpx;
    }

    .reserve-value {
        flex: 1;
        color: #1a1a1a;
        font-size: 25rpx;
    }
}

.product-detail-page--A2 {
    background: var(--product-page-bg, #f5f5f5);

    .productDetail-content {
        background: var(--product-page-bg, #f5f5f5);
    }

    /* A2 批发价格块是内容区首个卡片，与主图之间保持统一模块间距。 */
    .wholesale-price-block {
        margin-top: 20rpx;
        border-radius: 20rpx;

        :deep(.wholesale-price-table) {
            padding-top: 20rpx;
        }
    }
}

/* 跨境风 A3：标题+已售在主图上方，价格卡片在主图下方（对齐 tigshop-overseas 商品详情页） */
.product-detail-page--A3 {
    background: var(--product-page-bg, #f5f5f5);

    .overseas-head {
        background: var(--product-content-bg, #fff);
        padding: 20rpx 28rpx 12rpx;

        .overseas-head__name {
            position: relative;
            font-size: 30rpx;
            font-weight: 600;
            color: #1a1a1a;
            line-height: 1.45;
            word-break: break-all;

            .overseas-head__name-inner {
                display: inline;
            }

            .overseas-head__name-btn {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                color: #333;

                .iconfont-h5 {
                    font-size: 24rpx;
                }

                .icon-a-gengduo20 {
                    transform: rotate(90deg);
                }

                .iconTop {
                    transform: rotate(-90deg);
                }
            }

            .overseas-head__name-expand {
                position: absolute;
                right: 0;
                bottom: 0;
                padding-left: 40rpx;
                background: linear-gradient(to right, rgba(255, 255, 255, 0) 0%, var(--product-content-bg, #fff) 40%);
            }

            .overseas-head__name-collapse {
                margin-left: 8rpx;
                vertical-align: middle;
            }

            .overseas-head__presale-tag {
                display: inline;
                margin-right: 8rpx;
                padding: 3rpx 12rpx;
                font-size: 24rpx;
                color: #fff;
                background: #e47200;
                border-radius: 4rpx;
            }
        }

        .overseas-head__sold {
            margin-top: 12rpx;
            font-size: 24rpx;
            color: #888b94;
        }
    }

    .product-area-wrapper {
        background: var(--product-content-bg, #fff);
    }

    .productDetail-content--overseas {
        padding: 0;

        :deep(.product-title-info) {
            margin-top: 0;
            border-radius: 0;
        }

        /* 价格与优惠券同处一张白底卡片 */
        :deep(.title-info-top) {
            border-radius: 0;
            padding: 20rpx 28rpx;
        }

        .group-card-row {
            width: 100%;
            margin: 14rpx 0 0;
            box-sizing: border-box;
            border-radius: 20rpx;
            background: var(--product-content-bg, #fff);

            .group {
                display: flex;
                width: 100%;
            }

            :deep(.group-list) {
                width: 100%;
                min-width: 0;
            }
        }

        /* A3 卡片间距由各子组件内部绘制，兼容微信小程序样式隔离。 */
    }
}

.overseas-activity-coupon-box {
    padding: 21rpx 28rpx 35rpx;
    background: #fff;
}

.overseas-activity-coupon {
    display: flex;
    align-items: center;
    gap: 16rpx;
    padding: 20rpx 24rpx;
    border-radius: 14rpx;
    background: #f7f8fc;

    .coupon-title {
        flex: 1;
        color: #1a1a1a;
        font-size: 25rpx;
        font-weight: 700;
    }

    .coupon-more {
        flex-shrink: 0;
        display: flex;
        align-items: center;
        gap: 8rpx;
        color: #333;
        font-size: 24rpx;

        .iconfont-h5 {
            color: #888b94;
            font-size: 16rpx;
        }
    }
}
</style>
