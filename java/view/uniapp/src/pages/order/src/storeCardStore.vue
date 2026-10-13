<template>
    <view v-for="(item, index) in cartList" :key="index" class="store-card">
        <template v-if="item.isNotReachMinOrderAmount">
            <view class="card-tip">
                <text class="iconfont-h5 icon-tishi" />
                {{ $t("还差") }}
                <format-price :price-data="item.minOrderAmountDiff" />
                {{ $t("就能起送") }}</view
            >
        </template>

        <view class="store-card-item" :style="{ paddingTop: item.isNotReachMinOrderAmount ? '0rpx' : '20rpx' }">
            <view class="store-card-item-title" @click="handleLink(`/pages/store/index?id=`, item.shopId)">
                <view v-if="item.shopId === 0" class="store-card-item-label">{{ $t("自营") }}</view>
                <view class="store-card-item-name">{{ item.shopTitle ? item.shopTitle : $t("自营") }}</view>
            </view>

            <template v-for="(subItem, subIndex) in item.carts" :key="subIndex">
                <view class="store-card-item-content">
                    <view class="content-item" @click="handleLink(`/pages/product/index?id=`, subItem.productId)">
                        <view class="store-card-item-content-left">
                            <tig-image class="store-card-item-img" :src="subItem.picThumb" />
                        </view>
                        <view class="store-card-item-content-right">
                            <view class="store-card-item-content-right-title line2">{{ subItem.productName }}</view>
                            <template v-if="hasSkuText(subItem.skuData)">
                                <view class="sku-card">
                                    <view class="sku-item">{{ getSkuText(subItem.skuData) }}</view>
                                </view>
                            </template>
                            <template v-if="subItem.activityInfo && activityList(subItem.activityInfo).length > 0">
                                <view class="activity-box">
                                    <template v-for="(activityItem, activityIndex) in activityList(subItem.activityInfo)" :key="activityIndex">
                                        <activityCard font-size="22rpx" :activity-info="activityItem" card-type="sub" />
                                    </template>
                                </view>
                            </template>
                            <!-- 附加规格费用 -->
                            <template v-if="subItem.extraSkuData && subItem.extraSkuData.length">
                                <view class="extraskudata-box" style="margin-top: 20rpx">
                                    <view class="extraskudata">
                                        <template v-for="(attr, attrIndex) in subItem.extraSkuData" :key="attrIndex">
                                            <view class="extraskudata-item">
                                                <view class="title">{{ attr.attrName }}</view>
                                                <view class="line-box">
                                                    <view class="line" />
                                                </view>
                                                <view class="content">
                                                    <text class="line1 content-text">{{ attr.attrValue }}</text>
                                                    <format-price :show-text="false" class="price" :price-data="attr.attrPrice" />
                                                    <view class="num">x{{ subItem.quantity }}</view>
                                                </view>
                                            </view>
                                        </template>
                                    </view>
                                </view>
                            </template>
                            <view class="right-price-box" :style="{ marginTop: getPriceMargin(subItem) + 'rpx' }">
                                <view class="store-card-item-content-right-price">
                                    <format-price
                                        :font-style="{ fontWeight: 'bold' }"
                                        :decimals-style="{
                                            fontSize: '20rpx',
                                            fontWeight: 'bold'
                                        }"
                                        :currency-style="{
                                            fontSize: '20rpx',
                                            fontWeight: 'bold'
                                        }"
                                        :price-data="subItem.price"
                                    />
                                    <template v-if="subItem.originPrice && subItem.originPrice !== subItem.price">
                                        <format-price
                                            :font-style="{
                                                fontWeight: '400',
                                                textDecoration: 'line-through',
                                                fontSize: '19rpx',
                                                color: '#969799',
                                                position: 'relative',
                                                bottom: '-2px',
                                                left: '5px'
                                            }"
                                            :is-bottom="false"
                                            :price-data="subItem.originPrice"
                                        />
                                    </template>
                                </view>
                                <view class="store-card-item-content-right-num"> x{{ subItem.quantity }}</view>
                            </view>
                        </view>
                    </view>
                </view>
                <template v-if="subItem.extraSkuAllData && subItem.extraSkuAllData.extra && subItem.extraSkuAllData.extra.length">
                    <view class="attr-box" @click="handleShowAttr(subItem)">
                        <view class="attr-label">{{ $t("附加") }}</view>
                        <template v-if="subItem.extraSkuData && subItem.extraSkuData.length">
                            <view class="attr-list">
                                <template v-for="(attr, attrIndex) in subItem.extraSkuData" :key="attrIndex">
                                    <view class="attr-item">
                                        <!-- <view class="sub-title">{{ attr.attrName }}</view> -->
                                        <view class="item-content">
                                            <text class="line1 content-text">{{ attr.attrValue }}</text>
                                            <view class="right">
                                                <format-price class="price" :price-data="attr.attrPrice" />
                                                <view class="num">x{{ subItem.quantity }}</view>
                                            </view>
                                        </view>
                                    </view>
                                </template>
                            </view>
                        </template>
                        <template v-else>
                            <view>{{ $t("可选附加规格") }}</view>
                        </template>

                        <text class="iconfont-h5 icon-xiajiantou1-copy" />
                    </view>
                </template>
            </template>

            <template v-if="item.gift && item.gift.length > 0">
                <view class="gift-box">
                    <view class="gift-title">{{ $t("赠品") }}</view>
                    <view class="gift-content">
                        <view v-for="(subItem, giftIndex) in item.gift" :key="giftIndex" class="gift-content-item">
                            <view class="gift-item-left">
                                <tig-image :src="subItem.picThumb" class="gift-img" />
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

            <!-- 预售流程卡（预售不混单：通常只会有一个店铺） -->
            <template v-if="presaleInfo">
                <presaleCard :presale-info="presaleInfo" :total="total" :delivery-type="deliveryType" />
            </template>

            <!-- 自提时间（非周期购时显示） -->
            <template v-if="deliveryType && deliveryType === 2 && !isCycleBuy && !isSubCard">
                <template v-if="shippingType && shippingType[item.shopId] && shippingType[item.shopId].pickupTime">
                    <view class="payment" @click="handleChangePickupTime(item.shopId)">
                        <view class="payment-title">{{ $t("自提时间") }}</view>

                        <view class="payment-content">
                            <view class="payment-text line1">
                                {{
                                    shippingType[item.shopId].pickupDateType !== undefined && shippingType[item.shopId].pickupDateType !== null
                                        ? pickupDateTypeMap[shippingType[item.shopId].pickupDateType as 0 | 1 | 2]
                                        : ""
                                }}{{ shippingType[item.shopId].pickupTime }}</view
                            >
                            <image lazy-load class="more-ico" :src="staticResource('common/more.png')" />
                        </view>
                    </view>
                </template>
            </template>

            <!-- 自提点（到店自提时始终显示） -->
            <template v-if="deliveryType && deliveryType === 2 && !isSubCard">
                <template v-if="shippingType && shippingType[item.shopId] && shippingType[item.shopId].pickupId">
                    <view class="payment" @click="handleChange(item.shopId)">
                        <view class="payment-title">{{ $t("自提点") }}</view>

                        <view class="payment-content">
                            <view class="payment-text line1">{{ shippingType[item.shopId].pickupTitle }} </view>
                            <image lazy-load class="more-ico" :src="staticResource('common/more.png')" />
                        </view>
                    </view>
                </template>
            </template>
        </view>

        <template v-if="(flowType === 9 || flowType === 10) && !(cycleConfig && Object.keys(cycleConfig).length > 0) && !isSubCard">
            <template v-if="dateStockList.length > 0">
                <view class="payment stock">
                    <view class="payment-title"><text class="iconfont-h5 icon-bitian" />{{ reserveTextMap[flowType] }}</view>

                    <view class="payment-content">
                        <view class="payment-text date-stock">
                            <template v-if="dateStockListGrouped.length > 1 && currentGroupIndex > 0">
                                <view class="pre" @click="handleChangeGroup(-1)">
                                    <text class="iconfont-h5 icon-a-gengduo20" />
                                </view>
                            </template>

                            {{ reserveTextValue }}

                            <template v-if="dateStockListGrouped.length > 1 && currentGroupIndex < dateStockListGrouped.length - 1">
                                <view class="next" @click="handleChangeGroup(1)">
                                    <text class="iconfont-h5 icon-a-gengduo20" />
                                </view>
                            </template>
                        </view>
                    </view>
                </view>
                <dateStockCard
                    v-model="reservationDate"
                    v-model:product-reservation-time-id="productReservationTimeId"
                    :date-stock-list="dateStockListGrouped[currentGroupIndex]"
                />
                <view v-if="showReserveError && (!reservationDate || productReservationTimeId === 0)" class="reserve-error-tip">
                    {{ $t("请选择") }}{{ reserveTextMap[flowType] }}
                </view>
            </template>
            <template v-else>
                <view class="payment stock no-stock">
                    <view class="payment-title"><text class="iconfont-h5 icon-bitian" />{{ reserveTextMap[flowType] }}</view>

                    <view class="payment-content">
                        <view class="payment-text">
                            {{ $t("暂无可选日期") }}
                        </view>
                        <text class="iconfont-h5 icon-a-gengduo20" />
                    </view>
                </view>
            </template>
        </template>
    </view>

    <tig-popup v-model:show="show">
        <view class="payment-popup">
            <view class="popup-title title">{{ $t("选择配送方式") }}</view>
            <view class="payment-btn">
                <template v-for="(item, index) in shippingTypeList[shippingTypeKey]" :key="index">
                    <view
                        class="payment-btn-item"
                        :class="{ active: getActive(index, item.shippingTypeId) }"
                        @click="handleShippingType(index, item.shippingTypeId)"
                        >{{ $t(item?.shippingTypeName) }}</view
                    >
                </template>
            </view>
            <view class="payment-desc">
                <view class="payment-desc-title">{{ $t(currentShippingType?.shippingTypeName || "") }}</view>
                <view class="payment-desc-text"> {{ $t(currentShippingType?.shippingTimeDesc || "") }} </view>
            </view>

            <tig-fixed-placeholder background-color="#fff">
                <view class="btn-box">
                    <tig-button @click="handlecConfirm"> {{ $t("提交") }}</tig-button>
                </view>
            </tig-fixed-placeholder>
        </view>
    </tig-popup>

    <tig-popup v-model:show="showAttr">
        <view class="attribute">
            <view class="popup-title title">{{ $t("附加") }}</view>
            <view class="attribute-list">
                <template v-for="(item, index) in currentAttr.extraSkuAllData?.extra" :key="index">
                    <view class="attribute-item">
                        <view class="item-title">{{ item.attrName }}</view>
                        <view class="item-content">
                            <template v-for="subItem in item.attrList" :key="subItem.attributesId">
                                <view class="item-attr-box">
                                    <view
                                        class="item-attr"
                                        :class="{ checked: checkedAttrs[Number(index)] === Number(subItem.attributesId) }"
                                        @click.stop="handleAttrClick(Number(index), Number(subItem.attributesId))"
                                    >
                                        <view class="label">
                                            {{ subItem.attrValue }}
                                        </view>
                                        <view class="line-box" />
                                        <format-price class="value" :price-data="subItem.attrPrice" />
                                    </view>
                                </view>
                            </template>
                        </view>
                    </view>
                </template>
            </view>

            <tig-fixed-placeholder background-color="#fff">
                <view class="attribute-btn">
                    <view class="desc">{{ isOverseas() ? $t("已选择{0}种", [checkedAttrs.length]) : `已选择${checkedAttrs.length}种` }}</view>
                    <view class="">
                        <tig-button style="width: 400rpx; height: 80rpx" @click="handleAttribute"> {{ $t("确定") }}</tig-button>
                    </view>
                </view>
            </tig-fixed-placeholder>
        </view>
    </tig-popup>

    <selectPickupTime
        v-model="showPickupTime"
        :shipping-type="shippingType"
        :current-shipping-data="currentShippingData"
        :time-list="timeList"
        :appointment-time="appointmentTime"
        @change="pickupTimeChange"
    />

</template>

<script setup lang="ts">
/**
 * 店铺商品卡片组件（门店版，o2o 构建使用）
 * - 按店铺分组展示结算商品列表
 * - 显示商品信息、SKU、价格、数量
 * - 支持配送方式选择、自提时间选择
 * - 支持预约服务时间选择、周期购配置
 * - 显示商品附加服务和活动标签
 */
import { computed, ref, shallowRef } from "vue";
import { useI18n } from "vue-i18n";
import { staticResource, redirect, getSkuText, hasSkuText, isOverseas } from "@/utils";
import type { CartList, ShippingTypeItem, DateStockList } from "@/types/order/check";
import activityCard from "@/components/masonry/activityCard.vue";
import presaleCard from "./presaleCard.vue";
import selectPickupTime from "./pickupTime.vue";
import dateStockCard from "./dateStockCard.vue";
import type { PresaleInfo } from "@/types/presale/presale";
import type { Total } from "@/types/order/check";

interface Props {
    cartList: CartList[];
    shippingTypeList: { [key: string]: ShippingTypeItem[] };
    shippingType: {
        [key: string]: {
            typeId: number;
            shopId: number;
            typeName: string;
            pickupId?: number;
            pickupTitle?: string;
            pickupTime?: string;
            pickupDateType?: number; // 0 今日 1 明日 2 后日
        };
    };
    stores?: any;
    pickupTplData?: any;
    deliveryType: number;
    dateStockList: DateStockList[];
    flowType: number;
    showReserveError?: boolean;
    presaleInfo?: PresaleInfo | null;
    total?: Total | any;
    isSubCard?: boolean;
}

const { t } = useI18n();
const props = defineProps<Props>();

const emit = defineEmits(["update:shippingType", "change", "changeProductExtra"]);

const reservationDate = defineModel("reservationDate", {
    type: String,
    default: ""
});

const productReservationTimeId = defineModel("productReservationTimeId", {
    type: Number,
    default: 0
});

const cycleConfig = defineModel<any>("cycleConfig", {
    default: () => ({})
});


const dateStockListGrouped = computed(() => {
    const grouped: { [key: string]: DateStockList[] } = {};

    let arr = [];

    props.dateStockList.forEach((item) => {
        const arr = item.stockDate.split("-");

        const yearMonth = `${arr[0]}-${arr[1]}`;

        if (!grouped[yearMonth]) {
            grouped[yearMonth] = [];
        }

        grouped[yearMonth].push({ ...item, day: arr[2], weekDay: getWeekDay(item.stockDate) });
    });

    // 将对象转换为二维数组，按年月排序
    arr = Object.keys(grouped)
        .sort()
        .map((yearMonth) => grouped[yearMonth]);

    return arr;
});

const currentGroupIndex = ref(0);

const handleChangeGroup = (direction: number) => {
    currentGroupIndex.value = currentGroupIndex.value + direction;
    reservationDate.value = "";
    productReservationTimeId.value = 0;
};

const reserveTextValue = computed(() => {
    let str = "";

    if (reservationDate.value) {
        const arr = reservationDate.value.split("-");
        str = `${arr[0]}${t("年")}${padZero(Number(arr[1]))}${t("月")}`;
    } else if (dateStockListGrouped.value && dateStockListGrouped.value.length > 0) {
        const arr = dateStockListGrouped.value[currentGroupIndex.value][0].stockDate.split("-");
        str = `${arr[0]}${t("年")}${padZero(Number(arr[1]))}${t("月")}`;
    }

    return str;
});

const weekDays = [t("周日"), t("周一"), t("周二"), t("周三"), t("周四"), t("周五"), t("周六")];

// 计算周几的工具函数
const getWeekDay = (dateString: string) => {
    const date = new Date(dateString);
    return weekDays[date.getDay()];
};

const padZero = (num: number) => {
    return num < 10 ? `0${num}` : num;
};

const show = ref(false);

const shippingTypeKey = ref(0);

const pickupDateTypeMap = {
    0: t("今日"),
    1: t("明日"),
    2: t("后日")
};

const reserveTextMap: Record<number, string> = {
    9: t("到店时间"),
    10: t("上门时间")
};

// 配送时间标题映射（根据 deliveryType）
const deliveryTimeTextMap: Record<number, string> = {
    1: t("配送时间"), // 商家配送
    2: t("自提时间"), // 到店自提
    3: t("配送时间") // 同城配送
};

// 预约类型时间标题映射（根据 flowType）
const reserveTimeTextMap: Record<number, string> = {
    9: t("到店时间"), // 预约到店
    10: t("上门时间") // 预约上门
};

// 是否是周期购订单
const isCycleBuy = computed(() => {
    return Object.keys(cycleConfig.value).length > 0;
});


// 获取配送时间标题
const deliveryTimeTitle = computed(() => {
    // 预约类型优先使用 flowType
    if (props.flowType === 9 || props.flowType === 10) {
        return reserveTimeTextMap[props.flowType];
    }
    // 其他使用 deliveryType
    return deliveryTimeTextMap[props.deliveryType] || t("配送时间");
});

const currentShippingType = computed<ShippingTypeItem | null>(() => {
    return props.shippingTypeList && props.shippingTypeList[shippingTypeKey.value] && props.shippingTypeList[shippingTypeKey.value][currentIndex.value]
        ? props.shippingTypeList[shippingTypeKey.value][currentIndex.value]
        : null;
});

const handleChange = (id: number) => {
    let params = `id=${id}`;

    if (props.stores) {
        params += `&stores=${encodeURIComponent(JSON.stringify(props.stores))}`;
    }

    redirect({ url: `/pages/store/selfPick?${params}` });
};

const currentIndex = ref(0);
const getActive = (index: number, id: number) => {
    if (currentTypeID.value) {
        return currentTypeID.value === id;
    } else {
        return props.shippingType[shippingTypeKey.value]?.typeId === id;
    }
};
const currentTypeID = ref<null | number>();
const handleShippingType = (index: number, id: number) => {
    currentIndex.value = index;
    currentTypeID.value = id;
};
const handlecConfirm = () => {
    const list = JSON.parse(JSON.stringify(props.shippingType));
    const { shippingTypeId, shippingTypeName } = props.shippingTypeList[shippingTypeKey.value][currentIndex.value];
    list[shippingTypeKey.value].shopId = shippingTypeKey.value;
    list[shippingTypeKey.value].typeId = shippingTypeId;
    list[shippingTypeKey.value].typeName = shippingTypeName;
    emit("update:shippingType", list);
    emit("change");
    show.value = false;
};

const activityList = (data: any): any[] => {
    return data.filter((item: any) => item.type === 1 || item.type === 6);
};

const getPriceMargin = (data: any) => {
    if (data.extraSkuData && data.extraSkuData.length > 0) {
        return 0;
    } else if (data.skuData.length > 0 && data.activityInfo && activityList(data.activityInfo).length > 0) {
        return 0;
    } else if (data.skuData.length === 0 && (!data.activityInfo || activityList(data.activityInfo).length === 0)) {
        return 55;
    } else {
        return 10;
    }
};
const handleLink = (url: string, id: number) => {
    if (id === 0) return;
    uni.navigateTo({
        url: url + id
    });
};

const showAttr = ref(false);
const currentAttr = ref<any>({});
const checkedAttrs = ref<any[]>([]);
const handleShowAttr = (item: any) => {
    currentAttr.value = item;
    checkedAttrs.value = item.extraSkuData.length ? item.extraSkuData.map((item: any) => item.attributesId) : [];
    showAttr.value = true;
};
const handleAttrClick = (index: number, id: number) => {
    if (checkedAttrs.value[index] && checkedAttrs.value[index] === id) {
        checkedAttrs.value[index] = null;
    } else {
        checkedAttrs.value[index] = id;
    }
};
const handleAttribute = () => {
    emit("changeProductExtra", {
        cartId: currentAttr.value.cartId,
        extraAttrIds: checkedAttrs.value.filter((item) => !!item).join(",")
    });
    showAttr.value = false;
};

const showPickupTime = ref(false);

const currentShopId = ref<number | null>(null);


const currentShippingData = shallowRef<any>({});

const timeList = shallowRef<any[]>([]);

const appointmentTime = shallowRef({});

const handleChangePickupTime = async (shopId: number) => {
    currentShopId.value = shopId;

    if (props.pickupTplData) {
        appointmentTime.value = props.pickupTplData[shopId]?.appointmentTime || {};
        timeList.value = props.pickupTplData[shopId].timeList || [];
    }

    currentShippingData.value = props.shippingType ? props.shippingType[shopId] || {} : {};
    showPickupTime.value = true;
    // console.log("timeList", timeList.value);
    // console.log("currentShippingData", currentShippingData.value);
    // console.log("appointmentTime", appointmentTime.value);
};

const pickupTimeChange = (data: any) => {
    if (currentShopId.value !== null) {
        const list = JSON.parse(JSON.stringify(props.shippingType));
        list[currentShopId.value].pickupTime = data.pickupTime;
        list[currentShopId.value].pickupDateType = data.pickupDateType;

        emit("update:shippingType", list);
        emit("change");
    }
    showPickupTime.value = false;
};
</script>

<style lang="scss" scoped>
.attribute {
    min-height: 50vh;
    padding: 0rpx 40rpx 65px;
    overflow: hidden;
    overflow-y: auto;
    .title {
        padding-left: 0;
        height: 90rpx;
        line-height: 90rpx;
        padding-bottom: 40rpx;
    }
    .attribute-list {
        .attribute-item {
            .item-title {
                font-size: 24rpx;
                font-weight: 600;
                color: #212121;
                padding-bottom: 20rpx;
            }

            .item-content {
                display: flex;
                flex-direction: column;
                gap: 20rpx;
                padding-bottom: 30rpx;
                .item-attr-box {
                    display: flex;
                    .item-attr {
                        display: flex;
                        padding: 10rpx 25rpx;
                        background-color: #f2f2f2;
                        border-radius: 50rpx;
                        font-size: 24rpx;
                        align-items: center;
                        .line-box {
                            padding: 0 15rpx;
                            position: relative;
                            height: 100%;
                            &::after {
                                content: "";
                                display: block;
                                width: 1px;
                                height: 70%;
                                background-color: #bbbbbb;
                                position: absolute;
                                top: 50%;
                                transform: translateY(-50%);
                            }
                        }
                        &.checked {
                            color: var(--general);
                            background-color: var(--vice-bg);
                            .line-box {
                                &::after {
                                    background-color: var(--general);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    .attribute-btn {
        height: 100%;
        width: 100%;
        padding: 20rpx 0;
        padding-left: 70rpx;
        padding-right: 10rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .desc {
            font-size: 30rpx;
        }
    }
}

.attr-box {
    padding-top: 10rpx;
    display: flex;
    justify-content: space-between;
    position: relative;
    padding-right: 30rpx;
    .more-ico {
        width: 36rpx;
        height: 36rpx;
        padding-left: 10rpx;
    }

    .icon-xiajiantou1-copy {
        position: absolute;
        top: 13rpx;
        right: -5rpx;
        transform: rotate(-90deg);
    }
}
.attr-list {
    flex: 1;
    overflow: hidden;
    .attr-item {
        display: flex;
        // justify-content: space-between;
        align-items: center;
        height: 36rpx;
        line-height: 36rpx;
        .sub-title {
            padding-right: 30rpx;
        }
        .item-content {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: flex-end;
            overflow: hidden;

            .content-text {
                flex: 1;
                text-align: end;
            }
            .right {
                display: flex;
                padding-left: 10rpx;
                color: #242424;
                .num {
                    padding-left: 10rpx;
                }
            }
        }
    }
}

.sku-card {
    display: inline-flex;
    font-size: 22rpx;
    border-radius: 5rpx;
    color: #969799;
    margin: 10rpx 0;
}

.activity-box {
    margin-bottom: 10rpx;
}

.gift-box {
    margin-top: 10rpx;
    padding-top: 15rpx;
    font-size: 26rpx;
    position: relative;

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

.btn-box {
    padding: 25rpx;
}
.payment {
    border-radius: 18rpx;
    background: #fff;
    padding: 30rpx 0rpx 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    column-gap: 20rpx;

    &.stock {
        padding: 0 30rpx;

        .payment-title {
            position: relative;
            padding-left: 10rpx;

            .icon-bitian {
                color: var(--general);
                font-size: 40rpx;
                position: absolute;
                bottom: 50%;
                left: 0;
                transform: translateX(-50%) translateY(50%);
            }
        }
    }

    &.no-stock {
        padding-bottom: 30rpx;

        .payment-content {
            color: rgb(192, 196, 204);
            align-items: center;
            column-gap: 5rpx;
        }

        .icon-a-gengduo20 {
            font-size: 26rpx;
            position: relative;
            top: 1rpx;
        }
    }

    .payment-content {
        display: flex;
        align-items: center;
        justify-content: center;
        flex: 1;
        overflow: hidden;
        column-gap: 20rpx;

        .payment-text {
            flex: 1;
            overflow: hidden;
            text-align: right;
        }

        .date-stock {
            display: flex;
            align-items: center;
            justify-content: flex-end;

            .pre,
            .next {
                display: flex;
                align-items: center;
                padding-left: 15rpx;
            }

            .pre {
                transform: scaleX(-1);
            }

            .icon-a-gengduo20 {
                font-size: 19rpx;
                color: #8c8c8c;
            }
        }
    }

    .more-ico {
        width: 36rpx;
        height: 36rpx;
        padding-left: 10rpx;
    }
}

.payment-popup {
    padding: 40rpx 40rpx 65px;
    height: 50vh;
    .title {
        padding-left: 0;
        height: 90rpx;
        line-height: 50rpx;
        padding-bottom: 40rpx;
    }

    .payment-btn {
        margin-bottom: 40rpx;
        .payment-btn-item {
            background: #f2f2f2;
            color: #282828;
            padding: 13rpx 36rpx;
            border-radius: 50rpx;
            font-size: 25rpx;
            display: inline-block;
            margin-right: 28rpx;
            margin-bottom: 20rpx;

            &.active {
                color: #fff;
                background-color: var(--general);
            }

            &.disabled {
                color: #bbb;
            }
        }
    }

    .payment-desc {
        border-top: 1rpx solid #f2f2f2;
        padding-top: 20rpx;

        .payment-desc-text {
            font-weight: bold;
        }
    }

    .button-position {
        position: fixed;
        bottom: 30rpx;
        left: 0;
        right: 0;
        padding: 0 30rpx;
        padding-bottom: env(safe-area-inset-bottom) !important;
    }
}

.store-card {
    box-sizing: border-box;
    background-color: #fff;
    border-radius: 0;
    margin-top: 20rpx;
    margin-bottom: 14rpx;

    .store-card-item {
        padding: 20rpx 30rpx 20rpx;
    }

    .card-tip {
        background: #fef4ea;
        border-radius: 0;
        height: 54rpx;
        display: flex;
        align-items: center;
        font-size: 24rpx;
        color: #ff9c00;
        padding: 0 28rpx;
        margin-bottom: 20rpx;

        text {
            color: var(--general);
        }

        .icon-tishi {
            font-size: 30rpx;
            padding-right: 6rpx;
            color: #ff9c00;
        }
    }

    .store-card-item-title {
        min-height: 52rpx;
        display: flex;
        align-items: center;

        .store-card-item-label {
            background-color: var(--general);
            border-radius: 4rpx;
            color: #fff;
            font-size: 22rpx;
            height: 32rpx;
            line-height: 32rpx;
            margin-right: 20rpx;
            padding: 0 8rpx;
        }

        .store-card-item-name {
            font-weight: bold;
            color: #333;
            font-size: 28rpx;
        }
    }

    .store-card-item-content {
        padding: 20rpx 0 0rpx;

        .content-item {
            display: flex;
        }

        .store-card-item-content-left {
            width: 165rpx;
            height: 165rpx;
            border-radius: 15rpx;
            overflow: hidden;
            box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.04);
            .store-card-item-img {
                width: inherit;
                height: inherit;
            }
        }

        .store-card-item-content-right {
            width: calc(100% - 165rpx);
            padding-left: 18rpx;
            .store-card-item-content-right-title {
                font-weight: normal;
                font-size: 28rpx;
                width: 100%;
            }

            .extraskudata-box {
                display: flex;
                width: 100%;

                .extraskudata {
                    background-color: #f7f8fa;
                    padding: 10rpx 15rpx;
                    border-radius: 10rpx;
                    margin-top: 5rpx;
                    color: #969799;
                    display: flex;
                    flex-direction: column;

                    .extraskudata-item {
                        overflow: hidden;
                        font-size: 22rpx;
                        display: flex;
                        line-height: 40rpx;

                        .content {
                            flex: 1;
                            display: flex;
                            align-items: center;
                            overflow: hidden;

                            .price {
                                color: #555;
                                padding-left: 10rpx;
                            }

                            .content-text {
                                flex: 1;
                            }

                            .num {
                                padding-left: 10rpx;
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
            }

            .right-price-box {
                width: 100%;
                display: flex;
                justify-content: space-between;
                &.special-margin {
                    margin-top: 60rpx;
                }

                &.special-margin2 {
                    margin-top: 40rpx;
                }
            }

            .store-card-item-content-right-price {
                font-size: 28rpx;
                display: inline-block;
                margin-right: 10rpx;
            }

            .store-card-item-content-right-num {
                display: flex;
                align-items: center;
                font-size: 22rpx;
                color: #969799;
            }
        }

        .flex {
            display: flex;

            & :deep(.navigator-wrap) {
                display: flex;
            }
        }
    }
}

.reserve-error-tip {
    color: #ff4d4f;
    font-size: 24rpx;
    padding: 0 30rpx 30rpx;
}
.cycle-text-box {
    padding: 30rpx 0;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    .cycle-text-line {
        font-weight: 500;
        font-size: 24rpx;
        color: #ee0a24;
        margin-top: 10rpx;
        text-align: right;
        padding-right: 40rpx;
        white-space: nowrap;
    }
    .selected {
        color: #2a2a2a;
    }
}
.cycleTextLine {
    align-items: center;
    display: flex;
    align-content: flex-end;
    .next {
        margin-left: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        .icon-a-gengduo20 {
            font-size: 19rpx;
            color: #8c8c8c;
        }
    }

    .cycleText {
        font-weight: 500;
        font-size: 24rpx;
        color: #1a1a1a;
    }
}
</style>
