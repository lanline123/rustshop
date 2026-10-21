<template>
    <tig-layout @login-success="loginSuccess">
        <template v-if="!showLogin">
            <tig-navbar v-if="!isOverseasSkin" :show-left="false" :title="$t('购物车')" />
            <view v-if="!isLoad" class="cart-box shoppingCart">
                <template v-if="cartList.length > 0">
                    <view class="top-text">
                        <view class="fl">
                            {{ $t("已选商品") }}
                            <text class="select-num">{{ total.checkedCount }}</text>
                            {{ $t("件") }}
                        </view>
                        <view class="fr administrate acea-row row-center-wrapper" @click="cartManageFun">
                            <text class="manage-btn">{{ !cartManage ? $t("管理") : $t("完成") }}</text>
                        </view>
                    </view>
                    <view class="cart_list_wrap">
                        <template v-for="(item, index) in cartList" :key="item.cartId">
                            <view class="cart_table">
                                <view class="cart_store_title noborder">
                                    <tig-checkbox v-model:checked="item.isChecked" size="20" @change="onCheckAllItem(Number(index))" />
                                    <view class="store_label" @click="handleToShop(item.shopId)">{{ item.shopTitle ? item.shopTitle : $t("自营") }}</view>
                                </view>
                                <view class="goods-list-cart">
                                    <uni-swipe-action>
                                        <template v-for="goods in item.carts" :key="goods.productId">
                                            <view class="cart_item">
                                                <uni-swipe-action-item :threshold="0" auto-close>
                                                    <view class="cart_list_con" :class="{ cartItemDisabled: isCartProductDisabled(goods) }">
                                                        <tig-checkbox
                                                            v-model:checked="goods.isChecked"
                                                            size="20"
                                                            class="check-item"
                                                            :disabled="cartManage ? false : isCartProductDisabled(goods)"
                                                            @change="onChangeCheck"
                                                        />
                                                        <navigator :url="getProductUrl(goods, item.shopId)" class="photo">
                                                            <view class="photo-img">
                                                                <image :src="imageFormat(goods.picThumb)" />
                                                                <image
                                                                    v-if="cartProductStyleMap[goods.productId]?.borderImage"
                                                                    class="product-style-border"
                                                                    :src="cartProductStyleMap[goods.productId].borderImage"
                                                                    mode="scaleToFill"
                                                                />
                                                                <view v-if="goods.stock === 0" class="cart-notice-row">{{ $t("已售罄") }}</view>
                                                                <view v-if="goods.productStatus === 0" class="cart-notice-row">{{ $t("已下架") }}</view>
                                                            </view>
                                                        </navigator>
                                                        <view class="cart-row">
                                                            <navigator target="_blank" :url="getProductUrl(goods, item.shopId)">
                                                                <view class="name line2">
                                                                    {{ goods.productName }}
                                                                </view>
                                                            </navigator>
                                                            <view class="sku-card-box">
                                                                <template v-if="hasSkuText(goods.skuData)">
                                                                    <view class="sku-card">
                                                                        <view class="sku-item line1">{{ getSkuText(goods.skuData) }}</view>
                                                                    </view>
                                                                </template>
                                                            </view>

                                                            <template v-if="goods.activityInfo">
                                                                <view class="activity-box">
                                                                    <template
                                                                        v-for="subItem in formatActivityList(goods.activityInfo, item.usedPromotions)"
                                                                        :key="subItem.promotionId"
                                                                    >
                                                                        <activityCard :activity-info="subItem" card-type="sub" />
                                                                    </template>
                                                                </view>
                                                            </template>

                                                            <view
                                                                class="cart-price"
                                                                :class="{
                                                                    'cart-price-margin': (!goods.skuData || goods.skuData.length === 0) && !goods.activityInfo
                                                                }"
                                                            >
                                                                <view class="price-one">
                                                                    <!-- <format-price
                                                                        :showText="false"
                                                                        :fontStyle="{ fontWeight: 'bold' }"
                                                                        :decimalsStyle="{
                                                                            fontSize: '22rpx',
                                                                            fontWeight: 'bold'
                                                                        }"
                                                                        :currencyStyle="{
                                                                            fontSize: '23rpx',
                                                                            fontWeight: 'bold'
                                                                        }"
                                                                        :priceData="
                                                                            goods.originPrice && goods.originPrice !== goods.price
                                                                                ? goods.originPrice
                                                                                : goods.price
                                                                        "
                                                                    ></format-price> -->
                                                                    <format-price
                                                                        :show-text="false"
                                                                        :font-style="{ fontWeight: 'bold' }"
                                                                        :decimals-style="{
                                                                            fontSize: '22rpx',
                                                                            fontWeight: 'bold'
                                                                        }"
                                                                        :currency-style="{
                                                                            fontSize: '23rpx',
                                                                            fontWeight: 'bold'
                                                                        }"
                                                                        :price-data="goods.price"
                                                                    />
                                                                </view>
                                                                <view class="cart-num-box">
                                                                    <WholesaleCartStepper
                                                                        v-if="wholesaleCart.isWholesaleLine(goods)"
                                                                        v-model:value="goods.quantity"
                                                                        :multiple="goods.wholesaleMultiple"
                                                                        :max="goods.stock"
                                                                        :disabled="isCartProductDisabled(goods) || goods.stock === 0"
                                                                        @change="(q: number) => updateCartItem(goods.cartId, q)"
                                                                        @delete="handleDel(goods.cartId)"
                                                                    />
                                                                    <tig-number-box
                                                                        v-if="isRetailLine(goods)"
                                                                        v-model="goods.quantity"
                                                                        :disabled="isCartProductDisabled(goods) || goods.stock === 0"
                                                                        :min="1"
                                                                        :button-width="30"
                                                                        :max="goods.stock"
                                                                        input-width="30"
                                                                        button-size="30"
                                                                        @change="updateCartItem(goods.cartId, goods.quantity)"
                                                                        @overlimit="handleOverlimit"
                                                                    />
                                                                </view>
                                                            </view>
                                                            <template v-if="goods.price && goods.originPrice !== goods.price">
                                                                <view class="origin-price">
                                                                    {{ $t("每件预计到手") }}
                                                                    <format-price :show-text="false" :is-bottom="false" :price-data="goods.price" />
                                                                </view>
                                                            </template>
                                                            <template v-if="goods.extraSkuData && goods.extraSkuData.length">
                                                                <view class="extraskudata" @click="handleAttr(goods)">
                                                                    <template v-if="goods.extraSkuData.length === 1">
                                                                        <template v-for="attr in goods.extraSkuData" :key="attr">
                                                                            <view class="extraskudata-item">
                                                                                <view class="title line1">{{ attr.attrName }}</view>
                                                                                <view class="line-box">
                                                                                    <view class="line" />
                                                                                </view>
                                                                                <view class="content">
                                                                                    <text class="line1 content-text">{{ attr.attrValue }}</text>
                                                                                    <format-price
                                                                                        :show-text="false"
                                                                                        class="price"
                                                                                        :price-data="attr.totalAttrPrice"
                                                                                    />
                                                                                    <text class="iconfont-h5 icon-xiajiantou1-copy" />
                                                                                </view>
                                                                            </view>
                                                                        </template>
                                                                    </template>
                                                                    <template v-else>
                                                                        <view class="extraskudata-item">
                                                                            <view class="title">{{ $t("附加") }}</view>
                                                                            <view class="line-box">
                                                                                <view class="line" />
                                                                            </view>
                                                                            <view class="content">
                                                                                <text class="line1 content-text"
                                                                                    >{{ `已选${goods.extraSkuData.length}项` }}
                                                                                    <format-price
                                                                                        :show-text="false"
                                                                                        class="price"
                                                                                        :price-data="goods.serviceFee"
                                                                                    />
                                                                                </text>

                                                                                <text class="iconfont-h5 icon-xiajiantou1-copy" />
                                                                            </view>
                                                                        </view>
                                                                    </template>
                                                                </view>
                                                            </template>
                                                        </view>
                                                    </view>
                                                    <template #right>
                                                        <view class="cart-move-box">
                                                            <view class="btn-collect" @click="handleCollect(goods.productId, goods.cartId)">
                                                                <text>{{ $t("移入收藏") }}</text>
                                                            </view>
                                                            <view class="btn-del" @click="handleDel(goods.cartId)"
                                                                ><text>{{ $t("删除") }}</text></view
                                                            >
                                                        </view>
                                                    </template>
                                                </uni-swipe-action-item>
                                            </view>
                                        </template>
                                    </uni-swipe-action>
                                </view>

                                <template v-if="item.gift.length > 0">
                                    <view class="gift-box">
                                        <view class="gift-title">{{ $t("赠品") }}</view>
                                        <view class="gift-content">
                                            <view v-for="subItem in item.gift" :key="subItem.giftId" class="gift-content-item">
                                                <view class="gift-item-left">
                                                    <image :src="imageFormat(subItem.picThumb)" />
                                                </view>
                                                <view class="gift-item-right">
                                                    <view class="gift-item-name line1">{{ subItem.productName }}</view>
                                                    <template v-if="hasSkuText(subItem.skuData)">
                                                        <view class="sku-card">
                                                            <view class="sku-item">{{ getSkuText(subItem.skuData) }}</view>
                                                        </view>
                                                    </template>
                                                    <view class="gift-item-num">x{{ subItem.num }}</view>
                                                </view>
                                            </view>
                                        </view>
                                    </view>
                                </template>
                            </view>
                        </template>
                    </view>

                    <view class="checkOutBar" :style="checkOutBarStyle">
                        <view class="bar-check">
                            <view class="checkbox-pad">
                                <tig-checkbox v-model:checked="allChecked" size="20" @change="onCheckAll" />
                                <text class="checkbox-text" @click="onCheckAllText">{{ $t("全选") }}</text>
                            </view>
                        </view>
                        <view v-if="cartManage" class="edit-cart-action">
                            <view class="l_a_tool lt_delete" @click="checkClearCart">{{ $t("清空购物车") }}</view>
                            <view class="l_a_tool" :class="{ ltDisable: cartIds.length === 0 }" @click="delCartItem">{{ $t("删除勾选商品") }}</view>
                        </view>
                        <view v-if="!cartManage" class="cart-total-box">
                            <view class="cart-total">
                                <view :class="'btn-checkout ' + (total.checkedCount == 0 ? 'unable_btn' : '')" @click="handleCheckout">{{ $t("去结算") }}</view>
                                <view class="item-total">
                                    <view class="item-total-amount">
                                        <text class="txt">{{ $t("合计") }}：</text>
                                        <format-price
                                            :show-text="false"
                                            :price-data="total.discountAfter"
                                            :font-style="{ fontWeight: 'bold', fontSize: '30rpx' }"
                                            :currency-style="{
                                                fontSize: '22rpx',
                                                fontWeight: 'bold'
                                            }"
                                            :decimals-style="{
                                                fontSize: '22rpx',
                                                fontWeight: 'bold'
                                            }"
                                        />
                                    </view>
                                    <view class="item-total-desc">
                                        <view class=""
                                            >{{ $t("共优惠") }}：<format-price :show-text="false" :is-bottom="false" :price-data="total.discounts"
                                        /></view>
                                        <view class="discounts-btn" @click="handleShowDiscountInfo">
                                            {{ $t("优惠明细") }}
                                            <text class="iconfont-h5 icon-xiajiantou1-copy" :class="{ 'icon-active': showDiscountInfo }" />
                                        </view>
                                    </view>
                                </view>
                            </view>
                        </view>
                    </view>
                </template>

                <template v-if="cartList.length === 0 && !isLoad">
                    <cartEmpty />
                </template>
                <recommend @callback="getCartList" />
            </view>
        </template>
        <template v-else>
            <toLogin />
        </template>

        <discountInfo v-model="showDiscountInfo" :total="total" />

        <detail v-model="showDetail" :detail="currentDetail" />

        <deliveryProductPop
            v-model="showDeliveryProductPop"
            :delivery-products="deliveryProducts"
            :pickup-product-data="pickupProductData"
            :local-delivery-products="localDeliveryProducts"
            @order-check-type="handleOrderCheckType"
        />
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 购物车页面
 * - 按店铺分组展示购物车商品
 * - 支持单选/全选商品、修改数量、删除商品
 * - 显示商品优惠信息和价格计算
 * - 提供结算入口和推荐商品展示
 */
import { ref, computed, shallowRef } from "vue";
import activityCard from "@/components/masonry/activityCard.vue";
import discountInfo from "@/components/product/discountInfo.vue";
import recommend from "@/components/recommend/index.vue";
import detail from "./src/detail.vue";
import toLogin from "./src/toLogin.vue";
import cartEmpty from "./src/cartEmpty.vue";
import deliveryProductPop from "./src/deliveryProductPop.vue";
import { onShow } from "@dcloudio/uni-app";
import { useConfigStore } from "@/store/config";
import { useTabbarStore } from "@/store/tabbar";
import { getCart, updateCartItemData, updateCartCheck, clearCart, removeCartItemData } from "@/api/cart/cart";
import { updateCollectProduct } from "@/api/product/product";
import type { updateCartCheckitem } from "@/types/cart/cart";
import { getDeliveryOptions } from "@/api/store/store";
import { useI18n } from "vue-i18n";
import { imageFormat } from "@/utils/format";
import { redirect, getSkuText, hasSkuText, toStoreOrShopHome } from "@/utils";
import { useProductStyle } from "@/hooks/useProductStyle";
import { useWholesaleCart } from "@/hooks/useWholesaleCart";
import WholesaleCartStepper from "@/components/wholesale/WholesaleCartStepper.vue";

const { t } = useI18n();

const configStore = useConfigStore();

const tabbarStore = useTabbarStore();

const wholesaleCart = useWholesaleCart();

/**
 * 是否按零售计步器渲染该行
 * - 批发判定包在 wholesale 切割块内，关闭批发时退化为恒 true（所有行走零售控件），行为零变化
 */
const isRetailLine = (goods: any): boolean => {
    if (wholesaleCart.isWholesaleLine(goods)) return false;
    return true;
};

const isCartLineInvalid = (product: any) => product.sku?.isEnabled === 0 || product.productStatus === 0;
const isCartProductDisabled = (product: any) => product.isDisabled || isCartLineInvalid(product);

const cartList = ref<any>([]);
const flatCartProducts = computed(() => cartList.value.flatMap((shop: any) => shop?.carts ?? []));
const { styleMap: cartProductStyleMap, fetchStyles: fetchCartProductStyles, resetStyles: resetCartProductStyles } = useProductStyle(flatCartProducts);

const total = ref<any>({
    checkedCount: 0,
    discountAfter: 0,
    discountCouponAmount: 0,
    discountProductPromotionAmount: 0,
    discountSeckillAmount: 0,
    discountTimeDiscountAmount: 0,
    discounts: 0,
    productAmount: 0,
    totalCount: 0
});

const cartManage = ref(false);

const allChecked = ref(false);

/**
 * @description 全选/取消全选购物车商品
 * 遍历所有店铺下的商品，将未禁用的商品选中状态与全选状态同步
 */
const onCheckAll = () => {
    cartList.value.forEach((item: any) => {
        item.carts.forEach((product: any) => {
            if (!isCartProductDisabled(product)) {
                product.isChecked = allChecked.value;
            }
        });
    });
    updateCheckbox();
    updateCheckData();
};

/**
 * @description 点击全选文字触发全选切换
 */
const onCheckAllText = () => {
    allChecked.value = !allChecked.value;
    onCheckAll();
};

/**
 * @description 店铺级别全选/取消全选
 * @param index 店铺在购物车列表中的索引
 */
const onCheckAllItem = (index: number) => {
    const item = cartList.value[index];
    item.carts.forEach((product: any) => {
        if (!isCartProductDisabled(product)) {
            product.isChecked = item.isChecked;
        }
    });
    updateCheckbox();
    updateCheckData();
};

/**
 * @description 单个商品选中状态变更处理
 */
const onChangeCheck = () => {
    updateCheckbox();
    updateCheckData();
};

/**
 * @description 更新复选框联动状态
 * 根据商品选中情况更新店铺全选状态和总全选状态
 */
const updateCheckbox = () => {
    cartList.value.forEach((item: any) => {
        const validItem = item.carts.filter((product: any) => !isCartProductDisabled(product));
        item.isChecked = validItem.every((product: any) => product.isChecked === true);
    });
    allChecked.value = cartList.value.every((item: any) => item.isChecked === true);
};

/**
 * @description 同步选中状态到服务器
 * 管理模式下不同步，避免影响正常结算流程
 */
const updateCheckData = async () => {
    if (cartManage.value) return;
    try {
        const checkData: updateCartCheckitem[] = [];
        cartList.value.forEach((item: any) => {
            item.carts.forEach((product: any) => {
                checkData.push({
                    cartId: product.cartId,
                    isChecked: product.isChecked ? 1 : 0
                });
            });
        });
        await updateCartCheck({ data: checkData });
        getCartList();
    } catch (error) {
        console.error(error);
    }
};

/**
 * @description 登录成功回调，刷新购物车数据
 */
const loginSuccess = () => {
    getCartList();
};
onShow(() => {
    uni.hideTabBar();
    getCartList();
});
const isLoad = ref(true);
const showLogin = ref(!uni.getStorageSync("token"));

/**
 * @description 获取购物车列表数据
 * 包含商品信息、店铺分组、优惠计算等
 * 未登录时显示登录引导页
 */
const getCartList = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await getCart();
        // console.log(result);
        total.value = result.total;
        cartList.value = result.cartList;
        resetCartProductStyles();
        fetchCartProductStyles(flatCartProducts.value);
        showLogin.value = false;
        updateCheckbox();
    } catch (error: any) {
        console.error(error);
        if (error.code === 401) {
            showLogin.value = true;
        }
    } finally {
        isLoad.value = false;
        uni.hideLoading();
    }
};

let delayTimer: number | null = null; // 延时定时器

/**
 * @description 启动延时定时器，防抖处理商品数量更新
 * @param cartId 购物车项ID
 * @param quantity 更新后的数量
 */
const startDelayTimer = (cartId: number, quantity: number) => {
    if (delayTimer) {
        clearTimeout(delayTimer);
    }
    delayTimer = setTimeout(() => {
        __updateCartItemData(cartId, quantity);
    }, 300);
};

/**
 * @description 数量超限提示处理
 * @param event 超限类型：minus-低于最小值，plus-超过库存
 */
const handleOverlimit = (event: string) => {
    if (event && event === "minus") {
        uni.$u.toast(t("商品数量不能小于1"));
    }
    if (event && event === "plus") {
        uni.$u.toast(t("库存不足"));
    }
};

/**
 * @description 更新购物车商品数量（防抖入口）
 * @param cartId 购物车项ID
 * @param quantity 目标数量
 */
const updateCartItem = async (cartId: number, quantity: number) => {
    startDelayTimer(cartId, quantity);
};

/**
 * @description 实际执行购物车商品数量更新
 * @param cartId 购物车项ID
 * @param quantity 目标数量
 */
const __updateCartItemData = async (cartId: number, quantity: number) => {
    try {
        await updateCartItemData({ cartId, data: { quantity } });
        getCartList();
    } catch (error: any) {
        console.error(error);
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 800
        });
        setTimeout(() => {
            getCartList();
        }, 800);
    }
};

/**
 * @description 切换购物车管理模式
 * 管理模式下可批量删除商品，非管理模式下可正常结算
 */
const cartManageFun = () => {
    cartManage.value = !cartManage.value;
};

/**
 * @description 清空购物车确认弹窗
 */
const checkClearCart = () => {
    uni.showModal({
        title: t("提示"),
        content: t("确认要清空购物车吗？"),
        success: async (res) => {
            if (res.confirm) {
                await clearCart();
                getCartList();
            }
        }
    });
};

/**
 * @description 计算当前选中的购物车项ID列表
 * @returns 选中的cartId数组
 */
const cartIds = computed(() => {
    const cartiDs: number[] = [];
    cartList.value.forEach((item: any) => {
        item.carts.forEach((product: any) => {
            if (product.isChecked) cartiDs.push(product.cartId);
        });
    });
    return cartiDs || [];
});

/**
 * @description 判断商品是否为预售
 * 兼容后端可能的字段：saleMode/productSaleMode/presaleInfo
 */
const isPresaleGoods = (goods: any) => {
    const saleMode = Number(goods?.saleMode ?? goods?.productSaleMode ?? 0);
    if (saleMode > 0) return true;
    // 部分接口可能直接带 presaleInfo
    if (goods?.presaleInfo) return true;
    return false;
};

/**
 * @description 获取当前勾选的商品列表（扁平化）
 */
const getSelectedGoods = () => {
    const selected: any[] = [];
    cartList.value.forEach((shopGroup: any) => {
        shopGroup.carts.forEach((goods: any) => {
            if (goods.isChecked) {
                selected.push(goods);
            }
        });
    });
    return selected;
};

/**
 * @description 批量删除选中的购物车商品
 */
const delCartItem = () => {
    if (cartIds.value.length == 0) return uni.$u.toast(t("请选择要删除的商品"));
    uni.showModal({
        title: t("提示"),
        content: t("确认要删除指定的商品吗？"),
        success: async (res) => {
            if (res.confirm) {
                try {
                    await removeCartItemData({ cartIds: cartIds.value });

                    getCartList();
                } catch (error) {
                    console.error(error);
                }
            }
        }
    });
};

/**
 * @description 去结算入口
 * O2O场景下需先获取配送方式选项（商城配送/到店自提/同城配送）
 */
const handleCheckout = () => {
    if (total.value.checkedCount == 0) return;
    if (configStore.closeOrder === 1) return uni.$u.toast(t("商城已关闭下单"));

    // 批发结算门禁：本地校验倍数 + 起批量（前端自生成文案，不读后端 wholesaleHint）
    const checkedLines = (cartList.value || []).flatMap((shop: any) => shop?.carts ?? []).filter((c: any) => c.isChecked);
    const wholesaleTip = wholesaleCart.validateCheckout(checkedLines);
    if (wholesaleTip) {
        uni.showToast({ title: wholesaleTip, icon: "none", duration: 2500 });
        return;
    }

    // 预售不混单：预售商品必须单独结算（且通常单门店）
    const selectedGoods = getSelectedGoods();
    const presaleGoods = selectedGoods.filter((g) => isPresaleGoods(g));
    if (presaleGoods.length > 0) {
        // 只保留第一个预售商品，其它一律取消勾选
        const keep = presaleGoods[0];
        cartList.value.forEach((shopGroup: any) => {
            shopGroup.carts.forEach((goods: any) => {
                goods.isChecked = goods.cartId === keep.cartId;
            });
        });
        updateCheckbox();
        updateCheckData();
        uni.showToast({
            title: t("预售商品需单独结算"),
            icon: "none",
            duration: 1500
        });
        return;
    }
    getDeliveryOptionsData();
};

/**
 * @description 移入收藏并删除购物车商品
 * @param productId 商品ID
 * @param cartId 购物车项ID
 */
const handleCollect = async (productId: number, cartId: number) => {
    try {
        await updateCollectProduct({
            productId
        });
    } catch (error) {
        console.error(error);
    } finally {
        await removeCartItemData({ cartIds: [cartId] });
        getCartList();
    }
};

/**
 * @description 删除单个购物车商品
 * @param cartId 购物车项ID
 */
const handleDel = (cartId: number) => {
    uni.showModal({
        title: t("提示"),
        content: t("确认要删除该商品吗？"),
        success: async (res) => {
            if (res.confirm) {
                try {
                    await removeCartItemData({ cartIds: [cartId] });

                    getCartList();
                } catch (error) {
                    console.error(error);
                }
            }
        }
    });
};

/**
 * @description 跳转到店铺/门店详情页
 * O2O 进入门店首页，普通版本进入店铺首页。
 */
const handleToShop = (id: number) => {
    toStoreOrShopHome(id, { navType: "navigateTo" });
};

const getProductUrl = (goods: any, shopId?: number) => {
    let url = `/pages/product/index?id=${goods.productId}`;
    if (goods.skuId > 0) {
        url += `&sku_id=${goods.skuId}`;
    }
    if (shopId && shopId > 0) {
        url += `&shopId=${shopId}`;
    }
    return url;
};

const formatActivityList = (data: any, usedPromotions: any = []) => {
    const activityList: any[] = [];
    const list = Array.isArray(data) ? data : [];
    const usedList = Array.isArray(usedPromotions) ? usedPromotions : [];
    const couponList = list.filter((item: any) => item.type === 2);
    const promotionList = list.filter((item: any) => item.type !== 2);
    const usedCouponList = usedList.filter((item: any) => item.type === 2);
    if (promotionList.length > 0) activityList.push(promotionList[0]);
    // usedCouponList：使用的优惠券；从 couponList 中找到第一个“已使用”的优惠券展示
    if (couponList.length > 0) {
        const usedKeySet = new Set(usedCouponList.map((x: any) => x?.promotionId).filter((x: any) => x !== undefined && x !== null));
        const usedCoupon = couponList.find((c: any) => usedKeySet.has(c?.promotionId));
        activityList.push(usedCoupon || couponList[0]);
    }

    return activityList;
};

const showDetail = ref(false);
const currentDetail = ref<any>({});
const handleAttr = (item: any) => {
    showDetail.value = true;
    currentDetail.value = item;
};

const showDiscountInfo = ref(false);
const handleShowDiscountInfo = () => {
    if (showDetail.value) showDetail.value = false;
    showDiscountInfo.value = !showDiscountInfo.value;
};

const showDeliveryProductPop = ref(false);

const deliveryProducts = shallowRef<any[]>([]); // 支持商城配送的商品
const pickupProducts = shallowRef<any[]>([]); // 支持到店自提的商品
const pickupProductData = shallowRef<any[]>([]); // 自提商品扁平化数据
const localDeliveryProducts = shallowRef<any[]>([]); // 支持同城配送的商品

/**
 * @description 获取O2O配送方式选项
 * 根据商品支持的配送方式决定是否需要用户选择
 * deliveryType: 1-全部支持自提或配送 2-部分自提部分配送 3-全部配送 4-全部自提
 */
const getDeliveryOptionsData = async () => {
    try {
        const result = await getDeliveryOptions();

        if (result.deliveryType === 2) {
            // 需要用户选择配送方式
            deliveryProducts.value = result.deliveryProducts;
            pickupProducts.value = result.pickupProducts;
            localDeliveryProducts.value = result.shopDeliveryProducts;
            pickupProductData.value = [];
            result.pickupProducts.forEach((item: any) => {
                pickupProductData.value = [...pickupProductData.value, ...item.products];
            });
            showDeliveryProductPop.value = true;
        } else {
            // 直接跳转结算页
            redirect({
                url: `/pages/order/check`
            });
        }
    } catch (error) {
        console.error("Error fetching delivery options:", error);
    }
};

/**
 * @description 根据配送方式筛选购物车商品
 * 取消不支持当前配送方式的商品选中状态
 * @param type 配送方式：1-商城配送 2-到店自提 3-同城配送
 */
const changeCart = async (type: number) => {
    const deliveryIds = deliveryProducts.value.map((item: any) => item.productId);
    const pickupIds = pickupProductData.value.map((item: any) => item.productId);
    const localIds = localDeliveryProducts.value.map((item: any) => item.productId);

    const typeMap = {
        1: { keep: deliveryIds, cancel: [...pickupIds, ...localIds] },
        2: { keep: pickupIds, cancel: [...deliveryIds, ...localIds] },
        3: { keep: localIds, cancel: [...deliveryIds, ...pickupIds] }
    };

    const config = typeMap[type as keyof typeof typeMap];
    if (!config) return;

    const keepSet = new Set(config.keep);
    const cancelIds = config.cancel.filter((id: number) => !keepSet.has(id));
    const cancelIdsSet = new Set(cancelIds);

    // 批量更新购物车商品的选中状态
    cartList.value.forEach((item: any) => {
        item.carts.forEach((product: any) => {
            if (cancelIdsSet.has(product.productId)) {
                product.isChecked = false;
            }
        });
    });

    updateCheckbox();
    await updateCheckData();
};

/**
 * @description 用户选择配送方式后跳转结算页
 * @param type 配送方式：1-商城配送 2-到店自提 3-同城配送
 */
const handleOrderCheckType = async (type: number) => {
    await changeCart(type);

    redirect({
        url: `/pages/order/check?deliveryType=${type}`
    });
};

const checkOutBarStyle = computed(() => {
    return {
        bottom: tabbarStore.currentActiveValue > -1 ? "var(--tabbar-height)" : "0",
        paddingBottom: tabbarStore.currentActiveValue > -1 ? "0" : "var(--safe-bottom)",
        height: tabbarStore.currentActiveValue > -1 ? "var(--checkOutBar-height)" : "calc(var(--checkOutBar-height) + var(--safe-bottom))"
    };
});
</script>
<style lang="scss" scoped>
.extraskudata {
    background-color: #f7f8fa;
    width: 100%;
    padding: 10rpx 15rpx;
    border-radius: 10rpx;
    margin-top: 20rpx;
    color: #5c5c5c;
    .extraskudata-item {
        overflow: hidden;
        font-size: 22rpx;
        display: flex;
        line-height: 40rpx;

        .title {
            max-width: 150rpx;
        }
        .content {
            flex: 1;
            display: flex;
            align-items: center;
            overflow: hidden;
            position: relative;
            padding-right: 20rpx;
            .price {
                color: #555;
            }
            .content-text {
                flex: 1;
            }
            .icon-xiajiantou1-copy {
                position: absolute;
                right: -10rpx;
                top: 50%;
                transform: translateY(-55%) rotate(-90deg);
            }
        }

        .line-box {
            width: 25rpx;
            position: relative;
            .line {
                width: 1rpx;
                height: 15rpx;
                background-color: #c0c0c0;
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
            }
        }
    }
}

.gift-box {
    font-size: 26rpx;
    position: relative;
    margin: 0 40rpx;
    padding-top: 15rpx;
    margin-top: 10rpx;

    &::after {
        content: " ";
        position: absolute;
        pointer-events: none;
        box-sizing: border-box;
        -webkit-transform-origin: center;
        transform-origin: center;
        top: 0;
        left: 0;
        width: 100%;
        bottom: auto;
        -webkit-transform: scaleY(0.5);
        transform: scaleY(0.5);
        border-top: 1px solid #ebedf0;
    }

    .gift-content-item {
        width: 100%;
        display: flex;
        font-size: 24rpx;
        padding-top: 35rpx;
        padding-bottom: 15rpx;
        position: relative;

        .gift-item-left {
            width: 80rpx;
            height: 80rpx;
            border-radius: 8rpx;
            overflow: hidden;
            margin-right: 20rpx;

            image {
                width: 100%;
                height: 100%;
            }
        }

        .gift-item-right {
            width: calc(100% - 100rpx);
        }
        .gift-item-num {
            font-size: 22rpx;
            color: #969799;
            position: absolute;
            bottom: 15rpx;
            right: 0;
        }
    }
}

.select-num {
    color: var(--general);
}

.shoppingCart {
    --checkOutBar-height: 100rpx;
    padding-bottom: var(--checkOutBar-height);
}

.shoppingCart .labelNav {
    height: 76rpx;
    padding: 0 30rpx;
    font-size: 22rpx;
    color: #8c8c8c;
    position: fixed;
    left: 0;
    width: 100%;
    box-sizing: border-box;
    background-color: #f5f5f5;
    z-index: 5;
}
.shoppingCart .labelNav .item .iconfont {
    font-size: 25rpx;
    margin-right: 10rpx;
}
.shoppingCart .nav {
    width: 100%;
    height: 80rpx;
    background-color: #fff;
    padding: 0 30rpx;
    box-sizing: border-box;
    font-size: 28rpx;
    color: #282828;
    position: fixed;
    left: 0;
    z-index: 5;
}
.shoppingCart .nav .administrate {
    font-size: 26rpx;
    color: #282828;
    width: 110rpx;
    height: 46rpx;
    border-radius: 6rpx;
    border: 1px solid #868686;
}
.shoppingCart .list {
    margin-top: 171rpx;
}
.shoppingCart .list .item {
    padding: 25rpx 30rpx;
    background-color: #fff;
    margin-bottom: 15rpx;
}
.shoppingCart .list .item .picTxt {
    width: 627rpx;
    position: relative;
}
.shoppingCart .list .item .picTxt .pictrue {
    width: 160rpx;
    height: 160rpx;
}
.shoppingCart .list .item .picTxt .pictrue image {
    width: 100%;
    height: 100%;
    border-radius: 6rpx;
}
.shoppingCart .list .item .picTxt .text {
    width: 444rpx;
    font-size: 28rpx;
    color: #282828;
}
.shoppingCart .list .item .picTxt .text .infor {
    font-size: 24rpx;
    color: #868686;
    margin-top: 16rpx;
}
.shoppingCart .list .item .picTxt .text .money {
    font-size: 32rpx;
    color: #282828;
    margin-top: 28rpx;
}
.shoppingCart .list .item .picTxt .carnum {
    height: 47rpx;
    position: absolute;
    bottom: 7rpx;
    right: 0;
}
.shoppingCart .list .item .picTxt .carnum view {
    border: 1rpx solid #a4a4a4;
    width: 66rpx;
    text-align: center;
    height: 100%;
    line-height: 40rpx;
    font-size: 28rpx;
    color: #a4a4a4;
}
.shoppingCart .list .item .picTxt .carnum .reduce {
    border-right: 0;
    border-radius: 3rpx 0 0 3rpx;
}
.shoppingCart .list .item .picTxt .carnum .reduce.on {
    border-color: #e3e3e3;
    color: #dedede;
}
.shoppingCart .list .item .picTxt .carnum .plus {
    border-left: 0;
    border-radius: 0 3rpx 3rpx 0;
}
.shoppingCart .list .item .picTxt .carnum .num {
    color: #282828;
}
.shoppingCart .invalidGoods {
    background-color: #fff;
}
.shoppingCart .invalidGoods .goodsNav {
    width: 100%;
    height: 66rpx;
    padding: 0 30rpx;
    box-sizing: border-box;
    font-size: 28rpx;
    color: #282828;
}
.shoppingCart .invalidGoods .goodsNav .iconfont {
    color: #424242;
    font-size: 28rpx;
    margin-right: 17rpx;
}
.shoppingCart .invalidGoods .goodsNav .del {
    font-size: 26rpx;
    color: #999;
}
.shoppingCart .invalidGoods .goodsNav .del .icon-shanchu1 {
    color: #999;
    font-size: 33rpx;
    vertical-align: -2rpx;
    margin-right: 8rpx;
}
.shoppingCart .invalidGoods .goodsList .item {
    padding: 20rpx 30rpx;
    border-top: 1rpx solid #f5f5f5;
}
.shoppingCart .invalidGoods .goodsList .item .invalid {
    font-size: 22rpx;
    color: #fff;
    width: 70rpx;
    height: 36rpx;
    background-color: #aaa;
    border-radius: 3rpx;
    text-align: center;
    line-height: 36rpx;
}
.shoppingCart .invalidGoods .goodsList .item .pictrue {
    width: 140rpx;
    height: 140rpx;
}
.shoppingCart .invalidGoods .goodsList .item .pictrue image {
    width: 100%;
    height: 100%;
    border-radius: 6rpx;
}
.shoppingCart .invalidGoods .goodsList .item .text {
    width: 433rpx;
    font-size: 28rpx;
    color: #999;
    height: 140rpx;
}
.shoppingCart .invalidGoods .goodsList .item .text .infor {
    font-size: 24rpx;
}
.shoppingCart .invalidGoods .goodsList .item .text .end {
    font-size: 26rpx;
    color: #bbb;
}
.footer {
    width: 100%;
    height: 96rpx;
    background-color: #fafafa;
    position: fixed;
    bottom: 0;
    padding: 0 30rpx;
    box-sizing: border-box;
    border-top: 1rpx solid #eee;
}
.footer .checkAll {
    font-size: 28rpx;
    color: #282828;
    margin-left: 16rpx;
}
.footer checkbox .wx-checkbox-input {
    background-color: #fafafa;
}
.footer .money {
    font-size: 30rpx;
}
.footer .placeOrder {
    color: #fff;
    font-size: 30rpx;
    width: 226rpx;
    height: 70rpx;
    border-radius: 50rpx;
    text-align: center;
    line-height: 70rpx;
    margin-left: 22rpx;
}
.footer .button .bnt {
    font-size: 28rpx;
    color: #999;
    border-radius: 50rpx;
    border: 1px solid #999;
    width: 160rpx;
    height: 60rpx;
    text-align: center;
    line-height: 60rpx;
}
.footer .button form ~ form {
    margin-left: 17rpx;
}

.cart-box .top-text {
    height: 80rpx;
    background: #fff;
    overflow: hidden;
    padding: 0 25rpx;
}
.cart-box .top-text .fl {
    float: left;
    line-height: 80rpx;
}
.cart-box .top-text .fr {
    float: right;
}
.cart-box .top-text .manage-btn {
    line-height: 80rpx;
}

.cart_list_wrap {
    padding: 20rpx;
    display: flex;
    flex-direction: column;
    gap: 20rpx;
}
.cart_list_wrap .cart_table {
    border-radius: 10rpx;
    background: #fff;
    padding-bottom: 20rpx;
}
.cart_list_wrap .cart_store_title {
    min-height: 90rpx;
    -moz-box-align: center;
    align-items: center;
    box-sizing: border-box;
    display: flex;
    min-height: 90rpx;
    padding: 20rpx 20rpx 0 20rpx;
    position: relative;
    z-index: 1;
}
.cart_list_wrap .cart_store_title::before {
    background-color: #e5e5e5;
    bottom: 0;
    content: "";
    height: 1rpx;
    left: 0;
    pointer-events: none;
    position: absolute;
    right: 0;
    z-index: 1;
}
.cart_list_wrap .cart_store_title.noborder::before {
    display: none;
}
.cart_list_wrap .cart_store_title.free-weight-tit {
    padding: 0 20rpx 15rpx;
    min-height: 80rpx;
    font-size: 24rpx;
}
.cart_list_wrap .cart_store_title.free-weight-tit .tip {
    background-color: #e93b3d;
    border-radius: 2px;
    color: #fff;
    display: inline-block;
    font-size: 22rpx;
    height: 32rpx;
    line-height: 32rpx;
    margin-right: 20rpx;
    padding: 0 8rpx;
    vertical-align: middle;
}

.cart_list_wrap .cart_store_title .store_label {
    display: inline-block;
    font-weight: bold;
    color: #333;
    font-size: 28rpx;
    padding-left: 15rpx;
}

.goods-list-cart .cart_item {
    overflow: hidden;

    // &.cart_item_disabled {
    //     opacity: 0.5;
    // }
}
.goods-list-cart .cart_item .cart_list_con {
    display: flex;
    min-height: 180rpx;
    padding: 24rpx 20rpx 10rpx 80rpx;
    position: relative;
    z-index: 1;
    &.cart_item_disabled {
        opacity: 0.5;
    }
}
.goods-list-cart .cart_item .check-item {
    display: block;
    position: absolute;
    top: 0;
    z-index: 1;
    height: 100%;
    left: 0;
    width: 80rpx;
    padding-left: 20rpx;
    padding-top: 86rpx;
    box-sizing: border-box;
    text-align: center;
}
.goods-list-cart .cart_item .photo {
    display: block;
    width: 180rpx;
    height: 180rpx;
    margin-bottom: 20rpx;
    margin-left: 10rpx;
    box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.04);

    .photo-img {
        width: 180rpx;
        height: 180rpx;
        box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.04);
        position: relative;
        border-radius: 15rpx;
        overflow: hidden;

        image {
            width: 100%;
            height: 100%;
        }

        .product-style-border {
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            z-index: 1;
            pointer-events: none;
        }
    }
    .cart-notice-row {
        position: absolute;
        width: 180rpx;
        bottom: 0;
        z-index: 2;
    }
}

.goods-list-cart .cart_item .cart-row {
    padding-left: 18rpx;
    width: calc(100% - 200rpx);
    .activity-box {
        display: flex;
    }
}
.goods-list-cart .cart_item .cart-row .name {
    font-size: 27rpx;
    margin-bottom: 10rpx;
    overflow: hidden;
    color: #323233;
}

.goods-list-cart .cart_item {
    .sku-card-box {
        height: 48rpx;
        margin: 15rpx 0 8rpx;
        box-sizing: content-box;
    }
    .sku-card {
        display: inline-block;
        background-color: #f7f8fa;
        font-size: 22rpx;
        padding: 8rpx 12rpx;
        border-radius: 5rpx;
        color: #969799;
        margin: 0;
    }
}
.goods-list-cart .cart_item .cart-price {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 10rpx;
    position: relative;

    &.cart-price-margin {
        margin-top: 20rpx;
    }
}
.origin-price {
    font-size: 21rpx;
    color: var(--ump-icon);
    padding-top: 5rpx;
}
.goods-list-cart .cart_item .price-one {
    font-size: 30rpx;
    color: var(--general);
}

.goods-list-cart .cart_item .cart_promotion {
    margin: 10rpx 0 0 0;
    padding-right: 0;
    position: relative;
}
.goods-list-cart .cart_item .cart_promotion .goods_promotion {
    background-color: #fef5f5;
    padding: 20rpx;
    position: relative;
    color: #999;
    font-size: 24rpx;
}
.goods-list-cart .cart_item .cart_promotion .goods_promotion image {
    width: 50rpx;
    height: 50rpx;
    display: inline-block;
}
.goods-list-cart .cart_item .cart_promotion .goods_promotion .dd {
    padding-bottom: 6rpx;
}
.goods-list-cart .cart_item .cart_promotion .goods_promotion .dd:last-child {
    padding-bottom: 0;
}
.goods-list-cart .cart_item .cart_promotion .goods_promotion .g_gift_list navigator,
.goods-list-cart .cart_item .goods_promotion .g_gift_list view {
    display: inline-block;
}
.goods-list-cart .cart_item .cart_promotion .goods_promotion .g_gift_list .g_num {
    padding: 0 4rpx;
}
.goods-list-cart .cart_item .cart_promotion .goods_promotion::before {
    border-color: transparent transparent #fef5f5;
    border-style: solid;
    border-width: 10rpx;
    content: "";
    height: 0;
    left: 66rpx;
    position: absolute;
    top: -20rpx;
    width: 0;
    z-index: 1;
}
.goods-list-cart .cart_item .cart_promotion .goods_promotion .icon_r {
    background: transparent;
    border: 2rpx solid #c9c9c9;
    color: #999;
    font-size: 20rpx;
    line-height: 30rpx;
    border-radius: 6rpx;
    font-weight: normal;
    display: inline-block;
    padding: 0 10rpx;
    margin-right: 10rpx;
    vertical-align: top;
}

.goods-list-cart .cart_item .cart_list_con {
    width: 690rpx;
}
.goods-list-cart .cart_item movable-area {
    width: 690rpx;
    height: auto;
}
.goods-list-cart .cart_item movable-view {
    width: 995rpx;
    height: auto;
    position: relative;
}
.goods-list-cart .cart_item .cart-move-box {
    width: 300rpx;
    height: 100%;
    color: #fff;
    text-align: center;
    vertical-align: middle;
    display: flex;
}
.goods-list-cart .cart_item .cart-move-box text {
    display: block;
    position: absolute;
    top: 50%;
    margin-top: -20rpx;
    font-size: 24rpx;
    text-align: center;
    width: 100%;
}
.goods-list-cart .cart_item .cart-move-box .btn-del {
    width: 150rpx;
    display: inline-block;
    height: 100%;
    vertical-align: middle;
    display: table-cell;
    position: relative;
    background: var(--general);
    color: var(--main-text);
}
.goods-list-cart .cart_item .cart-move-box .btn-collect {
    width: 150rpx;
    background: var(--vice-bg);
    color: var(--vice-text);
    display: inline-block;
    height: 100%;
    vertical-align: middle;
    display: table-cell;
    position: relative;
}

.checkOutBar {
    width: 100%;
    background-color: #fff;
    position: fixed;
    bottom: 0;
    padding: 0;
    border-top: 1rpx solid #eee;
    display: flex;
    border-bottom: 1rpx solid #eee;
    z-index: 88;
}

.bar-check {
    padding-top: 10rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 160rpx;
    .bar-check-text {
        font-size: 26rpx;
        position: relative;
        top: -1px;
    }
    .checkbox-pad {
        text-align: center;
        padding-left: 25rpx;
        width: 100%;
        display: flex;
        align-items: center;
        .checkbox-text {
            font-size: 24rpx;
        }
    }
}
.cart-total-box {
    width: 100%;
}
.cart-total-box .btn-checkout {
    background: var(--general);
    color: #fff;
    display: block;
    float: right;
    font-size: 32rpx;
    font-weight: 700;
    height: 100rpx;
    line-height: 100rpx;
    margin-left: 20rpx;
    text-align: center;
    width: 220rpx;
}
.cart-total-box .btn-checkout.unable_btn {
    background: #d7d7d7;
    color: #999;
}
.cart-total-box .item-total {
    display: flex;
    flex-direction: column;
    align-items: end;
    justify-content: center;
}

.cart-total-box .item-total .item-total-amount {
    padding-top: 10rpx;
    text-align: right;
    color: var(--general);
    font-size: 27rpx;
    .txt {
        font-weight: normal;
        color: #666;
    }

    .explain-text {
        font-size: 22rpx;
    }
}
.cart-total-box .item-total .item-total-desc {
    padding-top: 6rpx;
    color: #888;
    font-size: 22rpx;
    display: flex;
    align-items: center;

    .discounts-btn {
        padding: 2rpx 15rpx;
        background-color: #f2f3f5;
        border-radius: 20rpx;
        overflow: hidden;
        color: rgb(50, 50, 51);
        padding-right: 5rpx;
        margin-left: 10rpx;
        display: flex;
        align-items: center;

        .icon-xiajiantou1-copy {
            display: inline-block;
            transform: rotate(180deg);
            position: relative;
            left: 0rpx;
            transition: all 0.3s;
            &.icon-active {
                transform: rotate(0deg);
            }
        }
    }
}

.edit-cart-action {
    width: 100%;
    text-align: right;
}
.edit-cart-action .l_a_tool {
    display: inline-block;
    text-align: center;
    color: #fff;
    background: var(--general);
    font-size: 24rpx;
    height: 60rpx;
    line-height: 60rpx;
    border-radius: 60rpx;
    margin-top: 20rpx;
    margin-right: 20rpx;
    padding: 0 30rpx;

    &.lt_disable {
        opacity: 0.5;
    }
}
.edit-cart-action .l_a_tool.lt_delete {
    background: #eee;
    color: #666;
}

.cart-notice-row {
    color: white;
    font-weight: 500;
    padding: 0 16rpx;
    height: 36rpx;
    line-height: 36rpx;
    background-color: #333;
    font-size: 20rpx;
    border-radius: 4rpx;
    text-align: center;
    opacity: 0.9;
}
.image_mask_sold_out {
    position: absolute;
    width: 80%;
    height: 80%;
    top: 10%;
    left: 10%;
    z-index: 2;
}
.image_mask_sold_out image {
    width: 100% !important;
    height: 100% !important;
}

:deep(.cart_item) {
    .button-group--right {
        right: -1px;
    }
}
</style>
