<template>
    <tig-layout :title="$t('提交订单')">
        <up-toast ref="uToastRef" />
        <template v-if="!isLoading">
            <template v-if="isSubCard">
                <addressInfo :data="addressList[0]" />
            </template>
            <template v-else-if="!canChangeDelivery">
                <template v-if="hasPickupDelivery || flowType === 9">
                    <pickupUserCard :user-pickup-ifon="userPickupIfon" border-radius="0 0 18rpx 18rpx" />
                </template>
                <template v-else-if="hasStoreDelivery || flowType === 10 || hasLocalDelivery">
                    <addressInfo :data="addressList[0]" />
                </template>
            </template>
            <template v-else>
                <deliveryTypeCard
                    v-model:delivery-type="deliveryType"
                    :has-local-delivery="hasLocalDelivery"
                    :has-pickup-delivery="hasPickupDelivery"
                    :has-store-delivery="hasStoreDelivery"
                    :get-address-info="addressList[0]"
                    :user-pickup-ifon="userPickupIfon"
                    @change="handleChangeDelivery"
                />
            </template>
        </template>

        <template v-if="paymentTypeList.length > 0">
            <paymentMode
                v-model:pay-type-id="formState.payTypeId"
                :available-payment-type="paymentTypeList"
                :delivery-type="deliveryType"
                :can-change-delivery="canChangeDelivery"
                :pickup-products="pickupProducts"
                :cart-list="cartListData"
                :shipping-type="formState.shippingType"
                @change="updateOrderCheck"
            />
        </template>

        <stroeCard
            v-model:shipping-type="formState.shippingType"
            v-model:reservation-date="reserveInfo.reservationDate"
            v-model:product-reservation-time-id="reserveInfo.productReservationTimeId"
            v-model:cycle-config="cycleConfig"
            v-model:cycle-order-info="cycleOrderInfo"
            class="store-card"
            :cart-list="cartListData"
            :shipping-type-list="shippingTypeData"
            :presale-info="productPresale"
            :total="totalData"
            :stores="stores"
            :pickup-tpl-data="pickupTplData"
            :delivery-type="deliveryType"
            :date-stock-list="dateStockList"
            :flow-type="flowType"
            :show-reserve-error="showReserveError"
            :is-sub-card="isSubCard"
            @change="updateOrderCheck"
            @change-product-extra="changeProductExtra"
        />

        <remark v-model="formState.buyerNote" />

        <template v-if="!useGiftCard && (configStore.useCoupon == 1 || configStore.usePoints == 1 || configStore.useSurplus == 1 || creditPaymentInfo)">
            <couponInfo
                ref="couponInfoRef"
                v-model:use-coupon-ids="formState.useCouponIds"
                v-model:select-user-coupon-ids="formState.selectUserCouponIds"
                v-model:use-point="formState.usePoint"
                :balance="userStore.userInfo.balance ?? 0"
                :balance-amount="Number(totalData?.balance) ?? 0"
                :credit-payment-info="creditPaymentInfo"
                :points="userStore.userInfo.points"
                :available-points="availablePoints"
                :points-amount="Number(totalData?.pointsAmount) ?? 0"
                :coupon-amount="Number(totalData?.discountCouponAmount) ?? 0"
                :coupon-list="couponListData"
                :flow-type="flowType"
                @send-balance-status="getBalanceStatus"
                @send-credit-status="getCreditStatus"
                @change="updateCoupon"
            />
        </template>

        <template v-if="configStore.canInvoice === 1 && !isOverseas()">
            <invoiceInfo v-model:invoice-info="formState.invoiceData" :get-address-info="addressList[0]" />
        </template>

        <view v-if="hasWholesaleLine && (toNum(wholesaleCouponExclude) > 0 || toNum(wholesalePointsExclude) > 0)" class="wholesale-exclude-tip">
            <view v-if="toNum(wholesaleCouponExclude) > 0" class="wholesale-exclude-row">
                <text class="wholesale-exclude-icon">!</text>
                <text class="wholesale-exclude-text"
                    >{{ $t("批发商品不参与店铺优惠券，已排除") }} <format-price :show-text="false" :is-bottom="false" :price-data="wholesaleCouponExclude"
                /></text>
            </view>
            <view v-if="toNum(wholesalePointsExclude) > 0" class="wholesale-exclude-row">
                <text class="wholesale-exclude-icon">!</text>
                <text class="wholesale-exclude-text"
                    >{{ $t("批发商品不赠送积分，已排除") }} <format-price :show-text="false" :is-bottom="false" :price-data="wholesalePointsExclude" />
                    {{ $t("对应积分") }}</text
                >
            </view>
        </view>

        <totalCard
            :total="totalData"
            :cart-list="cartListData"
            :flow-type="flowType"
            :delivery-type="deliveryType"
            :presale-info="productPresale"
            :card-config="cardConfig"
            :is-sub-card="isSubCard"
            @show-shipping-fee="handleShowShippingFee"
        />

        <template v-if="isBeyondDelivery">
            <view class="placeholder" />
        </template>

        <tig-fixed-placeholder background-color="#fff">
            <view class="submit-btn-box">
                <view class="submit-btn-price">
                    <view class="price-text">{{ $t(productPresale?.saleMode === 2 ? "定金" : "仍需支付") }}:</view>
                    <template v-if="flowType == 3">
                        <view class="points-box">
                            <view class="points-value">
                                {{ totalData?.exchangePoints }}
                                <view class="points-text">{{ $t(configStore.integralName) }} </view>
                            </view>
                            <view class="symbol">+</view>
                        </view>
                    </template>

                    <format-price
                        :font-style="{ fontWeight: 'bold', fontSize: '34rpx' }"
                        :decimals-style="{
                            fontSize: '24rpx',
                            fontWeight: 'bold'
                        }"
                        :currency-style="{
                            fontSize: '23rpx',
                            fontWeight: 'bold'
                        }"
                        :price-data="productPresale?.saleMode === 2 ? totalData?.actualPayDepositAmount : totalData?.unpaidAmount"
                    />
                </view>
                <view>
                    <tig-button class="btn" :disabled="submitDisabled || submitLoading" :loading="submitLoading" :loading-text="$t('提交中...')" @click="submit">
                        {{ $t(productPresale?.saleMode === 2 ? "支付定金" : "提交订单") }}
                    </tig-button>
                </view>

                <template v-if="isBeyondDelivery">
                    <view class="tips">
                        <text class="iconfont-h5" />
                        <text>{{ $t("已超出配送范围，请重新选择地址") }}</text>
                    </view>
                </template>
            </view>
        </tig-fixed-placeholder>

        <addressPop v-model="showAddressPop" @link="handleLink" />

        <shippingFeePop v-model="showShippingFee" :fee-type="shippingFeeType" :store-shipping-rule="storeShippingRule" :cart-list="cartListData" />
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 订单结算页（提交订单）
 * - 支持多种配送方式：商城配送、门店自提、同城配送
 * - 支持预约服务：预约到店(flowType=9)、预约上门(flowType=10)
 * - 支持周期购订单配置
 * - 选择支付方式、使用优惠券/积分/余额抵扣
 * - 可开具发票、添加备注
 * - 展示商品清单和价格明细
 */
import { computed, onBeforeUnmount, reactive, ref, shallowRef, watch } from "vue";
import { getOrderCheckData, updateOrderCheckData, orderSubmit, updateCouponData, getPaymentType, getShippingType, getStockCalendar } from "@/api/order/check";
import { getShopPickupTpl } from "@/api/store/store";
import type { CartList, Total, PaymentTypeItem, ShippingTypeItem, DateStockList, CreditPaymentInfo, StockCalendarParams } from "@/types/order/check";
import type { AddressFilterResult } from "@/types/user/address";
import type { UserPickupListRecord } from "@/types/store/userPicekup";
import type { PresaleInfo } from "@/types/presale/presale";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { getAddressList } from "@/api/user/address";
import { getUserPickupList } from "@/api/store/userPickup";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";
import { useI18n } from "vue-i18n";
import { isOverseas, redirect } from "@/utils";
import addressInfo from "./src/addressInfo.vue";
import paymentMode from "./src/paymentMode.vue";
import stroeCard from "./src/storeCard.vue";
import couponInfo from "./src/couponInfo.vue";
import invoiceInfo from "./src/invoiceInfo.vue";
import remark from "./src/remark.vue";
import totalCard from "./src/totalCard.vue";
import deliveryTypeCard from "./src/deliveryTypeCard.vue";
import addressPop from "./src/addressPop.vue";
import shippingFeePop from "./src/shippingFeePop.vue";
import pickupUserCard from "@/pages/order/src/pickupUserCard.vue";
import { getOrderPickupParams, filterDayList } from "@/utils/store";
import { useRegionStore } from "@/store/region";

// 微信小程序全局对象（仅在 MP-WEIXIN 环境存在，这里用于通过 TS 校验）
// #ifdef MP-WEIXIN
declare const wx: any;
// #endif

const { t } = useI18n();

const configStore = useConfigStore();

const userStore = useUserStore();

const regionStore = useRegionStore();

const cycleConfig = ref<any>({});
const cardConfig = ref<CycleConfig>({});

const useGiftCard = ref(false);

// 当前结算表单数据
interface IformState {
    addressId?: number;
    shopId?: number;
    shippingType: {
        [key: string]: {
            typeId: number;
            shopId: number;
            typeName: string;
            pickupId?: number;
            pickupTitle?: string;
            expectPickupTime?: string;
            pickupTime?: string;
            pickupDateType?: number; // 0 今日 1 明日 2 后日
        };
    };
    productExtra: any;
    payTypeId: number;
    usePoint: number;
    useBalance: number;
    /** 是否使用账期支付：0-不使用，1-使用 */
    useCredit: 0 | 1;
    useCouponIds: number[];
    selectUserCouponIds: number[];
    buyerNote: string;
    invoiceData: any;
    useDefaultCouponIds?: number;
    productReservationTimeId?: number;
    reservationDate?: string;
    deliveryType?: number;
    longitude?: number;
    latitude?: number;
    lotteryRecordId?: any;
}

// 当前结算表单数据
const formState = reactive<IformState>({
    shippingType: {},
    payTypeId: 0,
    usePoint: 0,
    useBalance: 0,
    useCredit: 0,
    useCouponIds: [],
    selectUserCouponIds: [],
    buyerNote: "",
    invoiceData: {},
    productExtra: {},
    longitude: regionStore.currentLocationInfo.longitude,
    latitude: regionStore.currentLocationInfo.latitude,
    lotteryRecordId: null
});

const isReserve = computed(() => {
    return flowType.value === 9 || flowType.value === 10;
});

const isCycleBuy = computed(() => {
    return !!(cycleConfig.value && Object.keys(cycleConfig.value).length > 0);
});

const showReserveError = ref(false);
const reserveInfo = reactive({
    productReservationTimeId: 0,
    reservationDate: ""
});

const userPickupIfon = ref<UserPickupListRecord>({} as UserPickupListRecord);
const isUserPickupListEmpty = ref(false);
const isOrderInfoLoaded = ref(false);

/**
 * @description 获取用户自提联系人列表
 * 用于到店自提和预约上门服务场景，获取用户已保存的联系人信息
 * 预约到店场景如无联系人则引导添加，次卡下单阶段除外
 */
const getUserPickupListData = async () => {
    try {
        const result = await getUserPickupList({ page: 1, size: 2 });
        isUserPickupListEmpty.value = result.records.length === 0;
        userPickupIfon.value = !isUserPickupListEmpty.value ? result.records[0] : ({} as UserPickupListRecord);

        if (isOrderInfoLoaded.value && flowType.value === 9 && isUserPickupListEmpty.value && !isSubCard.value) {
            redirect({
                url: "/pages/store/contacts/list"
            });
        }
    } catch (error) {
        console.error(error);
    }
};

const pickupTplData = shallowRef<any>({});

/**
 * @description 获取门店自提时间配置模板
 * 包含各门店的自提时间段、预约时间限制等配置
 */
const getShopPickupTplData = async () => {
    try {
        const result = await getShopPickupTpl({ shopId: formState.shopId });

        for (const item of result) {
            pickupTplData.value[item.shopId] = item;
        }
    } catch (error) {
        console.error(error);
    }
};

const addressList = ref<AddressFilterResult[]>([]);
const showAddressPop = ref(false);
const hasPickupDelivery = ref(false); // 是否有到店自提

/**
 * @description 获取用户收货地址列表
 * 商城配送和同城配送场景需要收货地址
 */
const getAddressListData = async () => {
    try {
        const result = await getAddressList({ page: 1, size: 5 });
        if (result.records.length > 0) {
            const records = result.records;
            // 默认选中用户在地址/定位页切换的收货地址：优先按地址 id 精确匹配，其次坐标，再次地址文案，否则回退第一条
            const selName = regionStore.currentSelectedName;
            const selAddressId = Number(regionStore.currentSelectedAddressId) || 0;
            const lat = Number(regionStore.currentLocationInfo?.latitude) || 0;
            const lng = Number(regionStore.currentLocationInfo?.longitude) || 0;
            let matched = selAddressId ? records.find((r) => Number(r.addressId) === selAddressId) : undefined;
            if (!matched && lat && lng) {
                matched = records.find((r) => Number(r.latitude) === lat && Number(r.longitude) === lng);
            }
            if (!matched && selName) {
                matched = records.find((r) => r.address === selName);
            }
            addressList.value = matched ? [matched, ...records.filter((r) => r !== matched)] : records;
            formState.addressId = addressList.value[0].addressId;
        }
    } catch (error) {
        console.error(error);
    }
};

const checkLanLon = () => {
    if (!addressList.value[0]) {
        return true;
    }

    return addressList.value[0]?.latitude && addressList.value[0]?.longitude;
};

const handleLink = () => {
    showAddressPop.value = false;
    redirect({
        url: `/pages/address/editRegion?id=${addressList.value[0]?.addressId}&type=check`
    });
};

const paymentTypeList = ref<PaymentTypeItem[]>([]);

/**
 * @description 获取可用的支付方式列表
 * 礼品卡订单使用特定flowType获取支付方式
 */
const getPaymentTypeData = async () => {
    try {
        const result = useGiftCard.value ? await getPaymentType(flowType.value) : await getPaymentType();
        formState.payTypeId = result[0].typeId;
        paymentTypeList.value = result;
    } catch (e) {
        console.log(e);
    }
};

const shippingTypeData = ref<{ [key: string]: ShippingTypeItem[] }>({});

/**
 * @description 获取各店铺可用配送方式
 * 按店铺ID分组，支持多店铺不同配送方式
 */
const getShippingTypeData = async () => {
    try {
        const result = await getShippingType({ flowType: flowType.value, shopId: formState.shopId });
        shippingTypeData.value = result;
        for (const key in result) {
            if (result[key] && result[key].length > 0) {
                formState.shippingType[key] = {
                    typeId: result[key][0].shippingTypeId,
                    shopId: result[key][0].shopId,
                    typeName: result[key][0].shippingTypeName
                };
            }
        }
    } catch (e) {
        console.log(e);
    }
};

/**
 * @description 更新自提配送信息
 * 将用户选择的自提门店和自提时间同步到配送类型数据中
 * 处理自提时间计算逻辑：今日/明日/后日
 */
const updateShippingType = () => {
    if (stores.value && stores.value.length > 0) {
        for (const key in formState.shippingType) {
            const index = stores.value.findIndex((item: any) => item.shopId === Number(key));
            if (index !== -1) {
                formState.shippingType[key].pickupId = stores.value[index].pickupId;
                formState.shippingType[key].pickupTitle = stores.value[index].pickupTitle;
            }

            if (pickupTplData.value && pickupTplData.value[key] && pickupTplData.value[key].pickupTimeStatus === 1) {
                const timeList = filterDayList(pickupTplData.value[key].timeList || [], pickupTplData.value[key].appointmentTime);

                if (
                    pickupTplData.value[key].appointmentTime &&
                    pickupTplData.value[key].appointmentTime.timeUnit === "d" &&
                    pickupTplData.value[key].appointmentTime.timeNum > 0
                ) {
                    const time = pickupTplData.value[key].timeList[0];
                    formState.shippingType[key].pickupTime = `${time.startTime} - ${time.endTime}`;
                    formState.shippingType[key].pickupDateType = pickupTplData.value[key].appointmentTime.timeNum;
                    continue;
                }

                if (timeList.length === 0) {
                    const time = pickupTplData.value[key].timeList[0];
                    formState.shippingType[key].pickupTime = `${time.startTime}-${time.endTime}`;
                    formState.shippingType[key].pickupDateType = 1;
                    continue;
                }

                const time = timeList[0];
                formState.shippingType[key].pickupTime = `${time.startTime} - ${time.endTime}`;
                formState.shippingType[key].pickupDateType = 0;
            }
        }
    }

    // console.log("formState.shippingType", formState.shippingType);
};

const cartListData = ref<CartList[]>([]);
const totalData = ref<Total>();
// 预售信息（本项目约束：预售不混单，结算时最多只有一个预售配置）
const productPresale = ref<PresaleInfo | null>(null);
const couponListData = ref<any>([]);
/** 批发行排除的券抵扣金额（结算页"批发商品不参与店铺优惠券"提示） */
const wholesaleCouponExclude = ref<string>("");
/** 批发行排除的积分金额（结算页"批发商品不赠送积分"提示） */
const wholesalePointsExclude = ref<string>("");
/** 是否存在批发行：任一 cart 行 isWholesaleLine===true 即视为有批发 */
const hasWholesaleLine = computed(() => {
    return cartListData.value.some((shop) => (shop.carts || []).some((c) => c.isWholesaleLine));
});
/** 字符串金额转 number（避免模板里写复杂表达式） */
const toNum = (v: string | undefined | null): number => {
    if (!v) return 0;
    const n = Number(v);
    return Number.isNaN(n) ? 0 : n;
};
const tmplIdsData = ref<any>([]);
const flowType = ref<number>(1);
const availablePoints = ref(0);
const creditPaymentInfo = ref<CreditPaymentInfo | null>(null);
const showShippingFee = ref(false);
const storeShippingRule = shallowRef<any>(null);
const shopDeliveryData = ref<any>([]);

// 是否是次卡
const isSubCard = ref<boolean>(false);

const shippingFeeType = computed(() => (flowType.value === 10 ? "homeService" : "localDelivery"));

const hasStoreDelivery = ref(false); // 是否有商城配送
const hasLocalDelivery = ref(false); // 是否有同城配送

const pickupProducts = shallowRef<any[]>([]);

/**
 * 门店详情页顶部 tab 携带的配送类型白名单（URL 参数 tabDeliveryTypes 解析而来）
 * 用于把结算页可选的配送方式（快递/自提/同城）再收敛一层，只保留当前 tab 允许的
 * 门店 tab 使用的配送类型枚举：1=到店自提 2=上门服务 3=预约到店 4=同城配送 5=快递配送
 */
const tabDeliveryTypes = ref<number[]>([]);

/**
 * 结算页 deliveryType (1/2/3) 与门店 tab deliveryType (1/4/5) 的映射
 * 1 快递配送 ↔ tab 5；2 门店自提 ↔ tab 1；3 同城配送 ↔ tab 4
 */
const CHECK_TO_TAB_DELIVERY_MAP: Record<number, number> = {
    1: 5,
    2: 1,
    3: 4
};

/**
 * 判断结算页某个 deliveryType (1/2/3) 是否被当前门店 tab 允许
 * tabDeliveryTypes 为空表示不做过滤（例如来自 cart 页 / 其它入口）
 */
const isDeliveryAllowedByTab = (checkDeliveryType: number): boolean => {
    if (tabDeliveryTypes.value.length === 0) return true;
    const mapped = CHECK_TO_TAB_DELIVERY_MAP[checkDeliveryType];
    if (mapped === undefined) return true;
    return tabDeliveryTypes.value.includes(mapped);
};

/**
 * 当后端默认 deliveryType 被当前 tab 过滤掉时，在剩余可用的配送方式里挑一个作为兜底
 * 优先取当前实际拉到的可用配送方式（避免选到一个没有商品可下单的方式）
 */
const pickFallbackDeliveryType = (): number => {
    if (hasStoreDelivery.value && isDeliveryAllowedByTab(1)) return 1;
    if (hasPickupDelivery.value && isDeliveryAllowedByTab(2)) return 2;
    if (hasLocalDelivery.value && isDeliveryAllowedByTab(3)) return 3;
    return 0;
};

/**
 * 把当前 deliveryType 收敛到 tab 白名单内（首次加载与每次 update 回包后都执行），
 * 防止后端回包把配送方式带回被 tab 禁用的类型（如门店自提 tab 被切回快递）
 */
const enforceTabDeliveryType = () => {
    if (tabDeliveryTypes.value.length === 0 || isDeliveryAllowedByTab(deliveryType.value)) return;
    const fallback = pickFallbackDeliveryType();
    if (fallback > 0) {
        deliveryType.value = fallback;
        formState.deliveryType = fallback;
    }
};

/**
 * @description 判断用户是否可以切换配送方式
 * 当有2种或以上配送方式可用时允许切换
 */
const canChangeDelivery = computed(() => {
    const availableDeliveryTypes = [hasStoreDelivery.value, hasLocalDelivery.value, hasPickupDelivery.value].filter(Boolean).length;

    // 当有2种或以上配送方式时，用户可以切换配送方式
    return availableDeliveryTypes >= 2;
});

/**
 * @description 判断同城配送是否超出配送范围
 * 仅在同城配送模式下检查
 */
const isBeyondDelivery = computed(() => {
    if (!shopDeliveryData.value || shopDeliveryData.value.length === 0) {
        return false;
    }

    if (deliveryType.value === 3) {
        const index = shopDeliveryData.value.findIndex((item: any) => item.isOutOfRange);

        if (index !== -1) {
            return true;
        }
    }
    return false;
}); // 是否超出配送范围

const isLoading = ref(false);

const mergeShippingType = (current: any, incoming: any) => {
    if (!incoming) return current;

    const merged = { ...current };
    for (const shopId in incoming) {
        merged[shopId] = {
            ...(current[shopId] || {}),
            ...incoming[shopId]
        };
    }
    return merged;
};

/**
 * @description 获取订单结算信息
 * 包含商品列表、价格汇总、优惠券、配送选项等完整结算数据
 * O2O场景下会返回商城配送/到店自提/同城配送的商品分组
 */
const getOrderInfo = async () => {
    try {
        const result = await getOrderCheckData({ flowType: flowType.value, ...formState });

        const { cartList, total, couponList, tmplIds, item, useCouponIds, selectUserCouponIds, deliveryOption = {}, creditPaymentInfo: cpi } = result;
        isOrderInfoLoaded.value = true;
        // 次卡下单阶段不强制要求联系人，联系人为空也不跳转联系人列表。
        if (flowType.value === 9 && isUserPickupListEmpty.value && !isSubCard.value) {
            return redirect({
                url: "/pages/store/contacts/list"
            });
        }
        // 周期购/次卡不去请求预约模板
        if (isReserve.value && !isCycleBuy.value && !isSubCard.value) {
            getStockCalendarData({
                productId: cartList[0]?.carts[0]?.productId,
                shopId: cartList[0]?.shopId,
                skuId: cartList[0]?.carts[0]?.skuId
            });
        }

        if (item) {
            const currentShippingType = formState.shippingType;
            Object.assign(formState, item);
            formState.shippingType = mergeShippingType(currentShippingType, item.shippingType);

            if (item.deliveryType) {
                deliveryType.value = item.deliveryType;

                if (!isSubCard.value && item.deliveryType === 3 && !checkLanLon()) {
                    showAddressPop.value = true;
                }
            }
        }

        if (deliveryOption && Object.keys(deliveryOption).length > 0) {
            if (deliveryOption.deliveryProducts && deliveryOption.deliveryProducts.length > 0 && isDeliveryAllowedByTab(1)) {
                hasStoreDelivery.value = true;
            }

            if (deliveryOption.pickupProducts && deliveryOption.pickupProducts.length > 0 && isDeliveryAllowedByTab(2)) {
                hasPickupDelivery.value = true;

                pickupProducts.value = deliveryOption?.pickupProducts || [];

                stores.value = getOrderPickupParams(deliveryOption?.pickupProducts);
                updateShippingType();
            }

            if (deliveryOption.shopDeliveryProducts && deliveryOption.shopDeliveryProducts.length > 0 && isDeliveryAllowedByTab(3)) {
                hasLocalDelivery.value = true;
            }

            shopDeliveryData.value = deliveryOption?.shopDeliveryData;
        }

        // 如果后端默认的 deliveryType 被当前 tab 过滤掉了，兜底切换到 tab 允许的第一个可选项
        enforceTabDeliveryType();

        if (useCouponIds && useCouponIds.length > 0) {
            formState.useCouponIds = useCouponIds;
        }
        if (selectUserCouponIds && selectUserCouponIds.length > 0) {
            formState.selectUserCouponIds = selectUserCouponIds;
        }

        if (total?.storeShippingRule) {
            storeShippingRule.value = total.storeShippingRule;
        }

        cartListData.value = cartList;
        totalData.value = total;
        productPresale.value = (result as any)?.productPresale ?? null;
        couponListData.value = couponList;
        tmplIdsData.value = tmplIds;
        creditPaymentInfo.value = cpi ?? null;
        wholesaleCouponExclude.value = result.couponExcludeAmount || "";
        wholesalePointsExclude.value = result.pointsExcludeAmount || "";
        // 抽奖订单无可用积分
        availablePoints.value = formState.lotteryRecordId ? 0 : result.availablePoints;

        // 是预约到店无需弹窗添加地址,并且不是到店自提
        const isLink = uni.getStorageSync("link");
        if (addressList.value.length === 0 && !isLink && flowType.value !== 9 && !hasPickupDelivery.value && !isSubCard.value) {
            return redirect({
                url: "/pages/address/list"
            });
        }
    } catch (error: any) {
        if (error.message === "用户地址不存在") {
            const isLink = uni.getStorageSync("link");
            if (!isLink && flowType.value !== 9 && !hasPickupDelivery.value && !isSubCard.value) {
                return redirect({
                    url: "/pages/address/list"
                });
            }
            return;
        }
        console.error(error);
    } finally {
        isLoading.value = false;
    }
};

const dateStockList = ref<DateStockList[]>([]);
const getStockCalendarData = async (params: StockCalendarParams) => {
    try {
        const result = await getStockCalendar(params);
        dateStockList.value = result.dateStockList;
    } catch (error) {
        console.error(error);
    }
};

/**
 * @description 更新商品附加服务选项
 * @param data 附加服务数据
 */
const changeProductExtra = async (data: any) => {
    formState.productExtra = data;
    const result = await updateOrderCheck();
    if (result) {
        cartListData.value = result.cartList;
    }
};

/**
 * @description 切换配送方式
 * @param val 配送方式：1-商城配送 2-门店自提 3-同城配送
 */
const handleChangeDelivery = (val: number) => {
    formState.deliveryType = val;

    updateOrderCheck();
};

const hasStoreShippingRule = computed(() => {
    const rule = storeShippingRule.value as any;
    if (!rule) return false;
    if (Array.isArray(rule)) return rule.length > 0;
    if (typeof rule === "object") return Object.keys(rule).length > 0;
    return false;
});

const handleShowShippingFee = () => {
    if (!hasStoreShippingRule.value) return;
    showShippingFee.value = true;
};

/**
 * @description 更新订单结算数据
 * 切换配送方式、地址、支付方式等时重新计算订单金额
 */
const updateOrderCheck = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await updateOrderCheckData({ flowType: flowType.value, ...formState });


        if (result.item) {
            const currentShippingType = formState.shippingType;
            Object.assign(formState, result.item);
            formState.shippingType = mergeShippingType(currentShippingType, result.item.shippingType);
            deliveryType.value = result.item.deliveryType;
            enforceTabDeliveryType();

            if (!isSubCard.value && deliveryType.value === 3 && !checkLanLon()) {
                showAddressPop.value = true;
            }
        }

        if (result.deliveryOption && Object.keys(result.deliveryOption).length > 0) {
            if (result.deliveryOption.pickupProducts && result.deliveryOption.pickupProducts.length > 0) {
                pickupProducts.value = result.deliveryOption?.pickupProducts || [];
            }

            shopDeliveryData.value = result.deliveryOption?.shopDeliveryData;
        }

        if (result.total?.storeShippingRule) {
            storeShippingRule.value = result.total.storeShippingRule;
        }

        cartListData.value = result.cartList;
        totalData.value = result.total;
        productPresale.value = (result as any)?.productPresale ?? productPresale.value;
        // 抽奖订单无可用积分
        availablePoints.value = formState.lotteryRecordId ? 0 : result.availablePoints;
        return result;
    } catch (error: any) {
        showToast(error.message);
    } finally {
        uni.hideLoading();
    }
};

/**
 * @description 更新优惠券使用状态并重新计算订单金额
 */
const updateCoupon = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await updateCouponData(formState);

        couponListData.value = result.couponList;
        totalData.value = result.total;
        productPresale.value = (result as any)?.productPresale ?? productPresale.value;
        cartListData.value = result.cartList;
        formState.useCouponIds = result.useCouponIds;
        formState.selectUserCouponIds = result.selectUserCouponIds;

        return result;
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
    }
};

/**
 * @description 切换余额抵扣状态
 * @param status true-使用余额 false-不使用余额
 */
const getBalanceStatus = (status: boolean) => {
    if (status) {
        // 使用余额时：互斥关闭账期支付
        formState.useBalance = Number(userStore.userInfo.balance || 0);
        formState.useCredit = 0;
        if (couponInfoRef?.value) {
            couponInfoRef.value.isCredit = false;
        }
    } else {
        formState.useBalance = 0;
    }
    updateOrderCheck();
};

/**
 * @description 账期支付使用状态变更回调（与余额互斥）
 */
const getCreditStatus = (status: boolean) => {
    if (status) {
        formState.useCredit = 1;
        formState.useBalance = 0;
        if (couponInfoRef?.value) {
            couponInfoRef.value.isBalance = false;
        }
    } else {
        formState.useCredit = 0;
    }
    updateOrderCheck();
};

/**
 * @description 判断提交按钮是否禁用
 * 检查：配送范围、配送方式、最低起送金额等
 */
const submitDisabled = computed(() => {
    if (isBeyondDelivery.value) {
        return true;
    }

    if (!formState.shippingType) {
        return true;
    }

    // if (isReserve.value && (!reserveInfo.productReservationTimeId || !reserveInfo.reservationDate)) {
    //     return true;
    // }

    for (const item of cartListData.value) {
        if (item.noShipping === 0 && !formState.shippingType[item.shopId]) {
            return true;
        }
    }

    if (cartListData.value.length > 0) {
        const index = cartListData.value.findIndex((item) => item.isNotReachMinOrderAmount);
        if (index !== -1) {
            return true;
        }
    }

    return false;
});

const submitLoading = ref(false);

/**
 * 根据天数偏移获取对应的年月日
 * @param daysOffset 距今天的天数偏移（0=今天，1=明天，2=后天...）
 * @returns 年月日字符串，如 "2025-09-12"
 */
function getDateYMD(daysOffset: number): string {
    const date = new Date();
    date.setDate(date.getDate() + daysOffset);

    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, "0");
    const day = date.getDate().toString().padStart(2, "0");

    return `${year}-${month}-${day}`;
}

/**
 * @description 格式化提交数据
 * 根据不同配送方式处理表单数据：
 * - 门店自提(2)：删除地址ID，设置自提时间
 * - 商城配送(1)：清理自提相关字段
 * - 同城配送(3)：清空配送类型数据
 */
const formatFormState = () => {
    const shippingTypeEntries = Object.entries(formState.shippingType);
    const pickupFieldsToDelete = ["expectPickupTime", "pickupId", "pickupTitle", "pickupTime", "pickupDateType"] as const;

    if (deliveryType.value === 2) {
        delete formState.addressId;

        shippingTypeEntries.forEach(([key, shippingInfo]) => {
            if (isSubCard.value) {
                pickupFieldsToDelete.forEach((field) => {
                    delete formState.shippingType[key][field];
                });
                formState.shippingType[key].typeName = t("到店自提");
                formState.shippingType[key].typeId = deliveryType.value;
                return;
            }

            if (shippingInfo?.pickupTime) {
                const pickupDateType = shippingInfo.pickupDateType ?? 0;
                const dateYMD = getDateYMD(pickupDateType);
                formState.shippingType[key].expectPickupTime = `${dateYMD} ${shippingInfo.pickupTime}`;
            }

            formState.shippingType[key].typeName = t("到店自提");
            formState.shippingType[key].typeId = deliveryType.value;
        });
    } else if (deliveryType.value === 1) {
        shippingTypeEntries.forEach(([key]) => {
            pickupFieldsToDelete.forEach((field) => {
                delete formState.shippingType[key][field];
            });
        });

        cartListData.value.forEach((item) => {
            if (item.noShipping === 1) {
                formState.shippingType[item.shopId].typeName = t("无需配送");
            }
        });
    } else if (deliveryType.value === 3) {
        formState.shippingType = {};
    }

    if (isReserve.value && !isSubCard.value) {
        // 周期购 + 预约类型：不需要选择预约日期，仅需要预约时间段ID（以及 cycleOrderInfo.selectedTimeSlot）
        formState.productReservationTimeId = reserveInfo.productReservationTimeId;
        if (!isCycleBuy.value) {
            formState.reservationDate = reserveInfo.reservationDate;
        } else {
            delete formState.reservationDate;
        }
    } else if (!isSubCard.value) {
        delete formState.productReservationTimeId;
        delete formState.reservationDate;
    }


    formState.deliveryType = deliveryType.value;
};

/**
 * @description 验证配送方式和联系信息
 * 根据订单类型和配送方式检查必填信息：
 * - 预约到店服务(flowType=9)：需要联系人
 * - 预约上门服务(flowType=10)：需要收货地址
 * - 快递/同城配送：需要收货地址
 * - 门店自提：需要联系人
 * @returns 验证是否通过
 */
const validateDeliveryAndContact = (): boolean => {

    // 预约服务验证
    if (isReserve.value) {
        if (flowType.value === 9) {
            // 到店服务 - 需要联系人信息
            if (Object.keys(userPickupIfon.value).length === 0) {
                uni.showToast({
                    title: t("请选择联系人信息"),
                    icon: "none"
                });
                return false;
            }
        } else if (flowType.value === 10) {
            // 上门服务 - 需要收货地址
            if (addressList.value.length === 0) {
                uni.showToast({
                    title: t("请选择收货地址"),
                    icon: "none"
                });
                return false;
            }
        }
        return true;
    }

    // 普通订单配送方式验证
    switch (deliveryType.value) {
        case 1: // 快递配送
        case 3: // 同城配送
            if (addressList.value.length === 0) {
                uni.showToast({
                    title: t("请选择收货地址"),
                    icon: "none"
                });
                return false;
            }
            break;

        case 2: // 门店自提
            if (Object.keys(userPickupIfon.value).length === 0) {
                uni.showToast({
                    title: t("请选择联系人信息"),
                    icon: "none"
                });
                return false;
            }
            break;
    }

    return true;
};

/**
 * @description 提交订单入口
 * 校验配送方式、联系信息、支付方式后提交订单
 * 微信小程序需先请求订阅消息授权
 */
const submit = async () => {
    if (submitLoading.value) return;

    if (!isSubCard.value && deliveryType.value === 3 && !checkLanLon()) {
        return (showAddressPop.value = true);
    }

    // 验证配送方式和联系信息
    if (!validateDeliveryAndContact()) {
        return;
    }

    if (formState.payTypeId === 0) {
        return uni.showToast({
            title: t("请选择付款方式"),
            icon: "none"
        });
    }

    if (submitDisabled.value) {
        return;
    }

    // 预约到店/上门：非周期购且非次卡才需要选择预约日期+时间段
    if (!isCycleBuy.value && !isSubCard.value && isReserve.value && (reserveInfo.productReservationTimeId === 0 || !reserveInfo.reservationDate)) {
        // 显示错误提示
        showReserveError.value = true;
        // 滚动到对应位置
        const query = uni.createSelectorQuery();
        query
            .select(".store-card")
            .boundingClientRect((data: any) => {
                uni.pageScrollTo({
                    scrollTop: data.top,
                    duration: 300
                });
            })
            .exec();
        return uni.showToast({
            title: t("请填写上门时间"),
            icon: "none"
        });
    }

    formatFormState();
    // console.log("提交订单参数", formState);
    // return;

    submitLoading.value = true;
    // #ifdef MP-WEIXIN
    //小程序调用订阅消息需要授权模板
    wx.requestSubscribeMessage({
        tmplIds: tmplIdsData.value,
        complete: () => {
            submitOrder();
        }
    });
    // #endif

    // #ifdef APP-PLUS || H5 || MP-QQ || MP-TOUTIAO || MP-BAIDU || MP-ALIPAY
    submitOrder();
    // #endif
};
/**
 * @description 执行订单提交
 * 根据返回类型跳转不同页面：
 * - returnType=2 且有拼团ID：跳转拼团详情
 * - returnType=2 无拼团：跳转支付成功页
 * - 其他：跳转支付页
 * 特殊错误处理：5002-需绑定微信，5003-预约时间已满
 */
const submitOrder = async () => {
    try {
        const result = await orderSubmit(formState);
        if (result.returnType === 2) {
            // 如果有拼团记录id，则跳转到拼团详情页
            if (result.grouponPromotionRecordId) {
                redirect({
                    url: `/pages/product/groupBuying?grouponPromotionRecordId=${result.grouponPromotionRecordId}`,
                    mode: "redirectTo"
                });
            } else {
                redirect({
                    url: `/pages/order/payStatus?id=${result.orderId}`,
                    mode: "redirectTo"
                });
            }
        } else {
            redirect({
                url: `/pages/order/pay?orderId=${result.orderId}`,
                mode: "redirectTo"
            });
        }
    } catch (error: any) {
        console.error(error);

        if (configStore.XClientType === "wechat" && error.code === 5002) {
            uni.setStorageSync("bindWechatFlowType", flowType.value);
            return redirect({
                url: `/pages/user/profile/index?bindWechat=true&flowType=${flowType.value}`
            });
        } else if (error.code === 5003) {
            showToast(error.message);
            reserveInfo.productReservationTimeId = 0;
            reserveInfo.reservationDate = "";
            getOrderInfo();
        } else {
            showToast(error.message);

            setTimeout(() => {
                redirect({
                    url: `/pages/cart/index`,
                    mode: "redirectTo"
                });
            }, 1500);
        }
    } finally {
        submitLoading.value = false;
    }
};

const couponInfoRef = ref();

const uToastRef = ref<any>(null);

/**
 * 显示toast提示
 * @param message 提示内容
 */
const showToast = (message: string) => {
    uToastRef.value?.show({
        type: "default",
        message: message,
        duration: 1500,
        position: "center"
    });
};

onShow(async () => {
    if (couponInfoRef?.value) {
        couponInfoRef.value.isBalance = false;
        couponInfoRef.value.isCredit = false;
        formState.useBalance = 0;
        formState.useCredit = 0;
    }
    initPageData();
});

const initPageData = async () => {
    try {
        uni.showLoading({
            title: t("加载中")
        });

        isLoading.value = true;
        isOrderInfoLoaded.value = false;

        let arr = [getAddressListData(), getPaymentTypeData(), getShippingTypeData(), userStore.getUserInfo(), getUserPickupListData()];

        if (flowType.value !== 9 && flowType.value !== 10) {
            arr = [...arr, getShopPickupTplData()];
        }

        await Promise.all(arr);
        await getOrderInfo();
    } catch (error: any) {
        console.error(error);
        showToast(error.message);
        setTimeout(() => {
            redirect({
                url: `/pages/cart/index`,
                mode: "redirectTo"
            });
        }, 1500);
    } finally {
        uni.hideLoading();
    }
};

const deliveryType = ref<number>(1); // 1 快递配送 2 门店自提

const stores = ref<any>(null);

onLoad((options) => {
    uni.$on("refreshAddress", async () => {
        await getAddressListData();
        updateOrderCheck();
    });

    uni.$on("refreshUserPickup", () => {
        getUserPickupListData();
    });

    uni.$on("refreshStorePickup", async (data: any) => {
        stores.value = data;
        updateShippingType();
    });

    if (options) {
        if (options.id) {
            formState.shopId = Number(options.id);
        }

        if (options.flowType) {
            flowType.value = Number(options.flowType);
            // 如果是12则是礼品卡流程
            if (flowType.value == 12) {
                useGiftCard.value = true;
            }
        }

        if (options.stores) {
            stores.value = JSON.parse(decodeURIComponent(options.stores));
            deliveryType.value = 2;
        }

        if (options.deliveryType) {
            formState.deliveryType = Number(options.deliveryType);
        }
        if (options.lotteryRecordId) {
            formState.lotteryRecordId = options.lotteryRecordId;
        }
        if (options.tabDeliveryTypes) {
            tabDeliveryTypes.value = String(options.tabDeliveryTypes)
                .split(",")
                .map((v: string) => Number(v))
                .filter((v: number) => Number.isFinite(v));
        }
    }
});

// 监听预约时间选择，选择后清除错误提示
watch(
    () => [reserveInfo.productReservationTimeId, reserveInfo.reservationDate],
    ([newTimeId, newDate]) => {
        if (Number(newTimeId) > 0 && (newDate || isCycleBuy.value)) {
            showReserveError.value = false;
        }
    }
);

// 周期购场景不展示预约选择块，避免遗留错误提示
watch(
    () => isCycleBuy.value,
    (val) => {
        if (val) showReserveError.value = false;
    }
);

onBeforeUnmount(() => {
    uni.removeStorageSync("link");
    uni.$off("refreshAddress");
    uni.$off("refreshUserPickup");
    uni.$off("refreshStorePickup");
});
</script>

<style lang="scss" scoped>
.placeholder {
    height: 74rpx;
    width: 100%;
}

.submit-btn-box {
    background-color: #fff;
    width: 100%;
    height: 100%;
    bottom: 0;
    padding: 0 30rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: relative;

    .tips {
        color: var(--general);
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        padding: 20rpx 20rpx;
        background-color: var(--primary-light-9);
        transform: translateY(-100%);
        font-size: 26rpx;
    }

    .submit-btn-price {
        color: var(--general);
        font-size: 32rpx;
        font-weight: bold;
        display: flex;
        align-items: center;
        column-gap: 10rpx;

        .price-text {
            font-weight: normal;
            color: #323233;
        }

        .points-box {
            display: flex;
            font-size: 34rpx;
            column-gap: 10rpx;
            .points-value {
                display: flex;
                align-items: flex-end;
                column-gap: 4rpx;
            }

            .points-text {
                font-size: 24rpx;
                position: relative;
                bottom: 4rpx;
            }
        }
    }

    .btn {
        width: 200rpx;
    }

    :deep(.btn .u-button__loading-text),
    :deep(.btn .u-button__text) {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        display: inline-block;
    }
}

/* @vort-if common.wholesale */
.wholesale-exclude-tip {
    margin: 20rpx 20rpx 0;
    padding: 16rpx 20rpx;
    background: rgba(255, 173, 79, 0.12);
    border-radius: 12rpx;

    .wholesale-exclude-row {
        display: flex;
        align-items: flex-start;
        font-size: 24rpx;
        color: #b76900;
        line-height: 1.5;

        & + .wholesale-exclude-row {
            margin-top: 8rpx;
        }
    }

    .wholesale-exclude-icon {
        flex-shrink: 0;
        width: 28rpx;
        height: 28rpx;
        line-height: 28rpx;
        text-align: center;
        background: #ff9a3c;
        color: #fff;
        border-radius: 50%;
        font-size: 20rpx;
        font-weight: bold;
        margin-right: 10rpx;
        margin-top: 4rpx;
    }

    .wholesale-exclude-text {
        flex: 1;
    }
}
/* @vort-endif */
</style>
