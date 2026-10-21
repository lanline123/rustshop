<template>
    <view v-for="(item, index) in cartList" :key="index" class="store-card">
        <view class="store-card-item">
            <view class="store-card-item-title" @click="handleLink(`/pages/shop/index?shopId=`, item.shopId)">
                <view v-if="item.shopId === 0" class="store-card-item-label">{{ $t("自营") }}</view>
                <view class="store-card-item-name">{{ item.shopTitle ? item.shopTitle : $t("自营") }}</view>
            </view>

            <template v-if="item.carts.length === 1">
                <template v-for="(subItem, subIndex) in item.carts" :key="subIndex">
                    <productItem :product="subItem" />
                    <template v-if="subItem.extraSkuAllData && subItem.extraSkuAllData.extra && subItem.extraSkuAllData.extra.length">
                        <view class="attr-box" @click="handleShowAttr(subItem)">
                            <view class="attr-label">{{ $t("附加") }}</view>

                            <template v-if="subItem.extraSkuData && subItem.extraSkuData.length">
                                <view class="attr-list">
                                    <template v-for="(attr, attrIndex) in subItem.extraSkuData" :key="attrIndex">
                                        <view class="attr-item">
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
            </template>

            <template v-else>
                <view class="store-card-item-content2-box">
                    <view class="store-card-item-content2 flex">
                        <template v-for="(subItem, subIndex) in item.carts" :key="subIndex">
                            <view class="content-item" @click="handleLink(`/pages/product/index?id=`, subItem.productId)">
                                <view class="store-card-item-img">
                                    <tig-image :src="pickSkuPic(subItem)" />
                                </view>
                                <view class="item-price flex justify-center">
                                    <format-price :integer-style="{ fontSize: '18px', lineHeight: '24px' }" :price-data="subItem.price" />
                                </view>
                            </view>
                        </template>
                        <view class="more-btn" @click="handleProductPopup(item.carts)">
                            <view class="">{{ item.carts.length }} {{ $t("件") }}</view>
                            <view class="iconfont-h5 icon-fanhui" />
                        </view>
                    </view>
                </view>
            </template>

            <template v-if="item.gift && item.gift.length > 0">
                <view class="gift-box">
                    <view class="gift-title">{{ $t("赠品") }}</view>
                    <view class="gift-content">
                        <view v-for="(subItem, giftIndex) in item.gift" :key="giftIndex" class="gift-content-item">
                            <view class="gift-item-left">
                                <tig-image :src="pickSkuPic(subItem)" class="gift-img" />
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

            <view class="payment">
                <view class="payment-title">{{ $t("配送方式") }}</view>
                <view class="payment-content">
                    <view class="payment-text">{{ getShippingTypeText(item.shopId, item.noShipping) }}</view>
                </view>
            </view>

            <!-- 预售流程卡（预售不混单：只在第一个店铺下展示一次） -->
            <presaleCard v-if="presaleInfo && index === 0" :presale-info="presaleInfo" :total="total" :delivery-type="deliveryType" />
        </view>
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

    <tig-popup v-model:show="showProductPopup" :show-close="false">
        <view class="product-popup">
            <view class="popup-title-box">
                <view class="title"> {{ $t("所有商品") }}{{ `(${currentPopupProducts.length})` }} </view>
                <view class="close-icon" @click="showProductPopup = false">
                    <image :src="staticResource('order/close.png')" />
                </view>
            </view>
            <scroll-view scroll-y="true" class="popup-content">
                <view class="popup-content-inner">
                    <template v-for="(item, index) in currentPopupProducts" :key="index">
                        <view class="popup-content-item">
                            <productItem :product="item" />
                        </view>
                    </template>
                </view>
            </scroll-view>
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
                    <view class="desc">{{ isOverseas() ? $t(`已选择{0}种`, checkedAttrs.length) : `已选择${checkedAttrs.length}种` }}</view>
                    <view class="">
                        <tig-button style="width: 400rpx; height: 80rpx" @click="handleAttribute"> {{ $t("确定") }}</tig-button>
                    </view>
                </view>
            </tig-fixed-placeholder>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * 店铺商品卡片组件（通用版，标准/跨境构建使用）
 * - 按店铺分组展示结算商品列表
 * - 单商品展示大图详情行，多商品横滑缩略图 + 全部商品弹层
 * - 显示赠品、配送方式、预售流程
 * - 支持商品附加规格选择
 */
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { getSkuText, hasSkuText, isOverseas, staticResource } from "@/utils";
import { pickSkuPic } from "@/utils/format";
import type { Cart, CartList, ShippingTypeItem } from "@/types/order/check";
import productItem from "./productItem.vue";
import presaleCard from "./presaleCard.vue";
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
        };
    };
    deliveryType?: number;
    presaleInfo?: PresaleInfo | null;
    total?: Total | any;
}

const { t } = useI18n();
const props = defineProps<Props>();

const emit = defineEmits(["update:shippingType", "change", "changeProductExtra"]);

const show = ref(false);
const shippingTypeKey = ref(0);

const currentShippingType = computed<ShippingTypeItem | null>(() => {
    return props.shippingTypeList && props.shippingTypeList[shippingTypeKey.value] && props.shippingTypeList[shippingTypeKey.value][currentIndex.value]
        ? props.shippingTypeList[shippingTypeKey.value][currentIndex.value]
        : null;
});

const currentIndex = ref(0);
const currentTypeID = ref<null | number>();

const getActive = (index: number, id: number) => {
    if (currentTypeID.value) {
        return currentTypeID.value === id;
    } else {
        return props.shippingType[shippingTypeKey.value]?.typeId === id;
    }
};

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

const getShippingTypeText = (id: number, noShipping: number) => {
    return noShipping === 1 ? t("无需配送") : t(props.shippingType?.[id]?.typeName || "") || t("该收货地址不支持配送");
};

const handleLink = (url: string, id: number) => {
    // 商品 id 无效时不跳转；门店支持平台自营 shopId=0
    if (id === 0 && !url.includes("/pages/store/")) return;
    uni.navigateTo({
        url: url + id
    });
};

const showProductPopup = ref(false);
const currentPopupProducts = ref<Cart[]>([]);

const handleProductPopup = (carts: Cart[]) => {
    currentPopupProducts.value = carts;
    showProductPopup.value = true;
};

const showAttr = ref(false);
const currentAttr = ref<any>({});
const checkedAttrs = ref<any[]>([]);

const handleShowAttr = (item: any) => {
    currentAttr.value = item;
    checkedAttrs.value = item.extraSkuData.length ? item.extraSkuData.map((row: any) => row.attributesId) : [];
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
</script>

<style lang="scss" scoped>
.product-popup {
    height: 88vh;
    background-color: #fff;
    border-top-left-radius: 28rpx;
    border-top-right-radius: 28rpx;
    display: flex;
    flex-direction: column;
    padding-bottom: var(--safe-bottom);
    max-width: 100vw;
    overflow: hidden;

    .popup-title-box {
        display: flex;
        justify-content: center;
        margin: 35rpx 28rpx 28rpx;

        .title {
            flex: 1;
            display: flex;
            justify-content: center;
            font-size: 32rpx;
            line-height: 42rpx;
            font-weight: 700;
        }

        .close-icon {
            display: flex;
            align-items: center;

            image {
                width: 35rpx;
                height: 35rpx;
            }
        }
    }

    .popup-content {
        flex: 1;
        height: 0;

        .popup-content-inner {
            padding: 0rpx 28rpx 37rpx;
        }

        .popup-content-item {
            width: 100%;
            overflow: hidden;
            margin-bottom: 21rpx;
        }
    }
}

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
        align-items: center;
        height: 36rpx;
        line-height: 36rpx;
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

    .sku-item {
        width: auto;
        max-width: none;
    }
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
        padding-top: 15rpx;
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
    background: #f7f8fc;
    border-radius: 4px;
    margin: 16px 0 0;
    padding: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .payment-title {
        font-weight: 400;
        font-size: 14px;
        line-height: 20px;
        color: #505259;
    }

    .payment-content {
        display: flex;
        align-items: center;
        justify-content: center;

        .payment-text {
            font-weight: 700;
            font-size: 14px;
            line-height: 20px;
            color: #000;
        }
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
            font-size: 24.5rpx;
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
}

.store-card {
    box-sizing: border-box;
    background-color: #fff;
    padding: 20rpx 28rpx;
    font-size: 24.5rpx;
    margin-bottom: 14rpx;

    .store-card-item-title {
        display: flex;
        align-items: center;
        padding-bottom: 10rpx;

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

    .store-card-item-content2-box {
        position: relative;
    }

    .store-card-item-content2 {
        align-items: stretch;
        gap: 21rpx;
        overflow: hidden;
        overflow-x: auto;
        scrollbar-width: none;
        padding-right: 200rpx;
        width: 100%;

        .more-btn {
            display: flex;
            position: absolute;
            right: 0;
            top: 0;
            align-items: center;
            justify-content: flex-end;
            width: 196rpx;
            padding-left: 14rpx;
            background: linear-gradient(270deg, #fff 78.7%, hsla(0, 0%, 100%, 0));
            height: 100%;
            z-index: 1;

            .icon-fanhui {
                width: 28rpx;
                font-size: 17rpx;
                color: #1a1a1a;
                transform: rotate(180deg);
                display: flex;
                align-items: center;
                justify-content: flex-start;
            }
        }

        .content-item {
            display: flex;
            flex-direction: column;
            width: 155rpx;
        }

        .store-card-item-img {
            width: 155rpx;
            height: 155rpx;
            border-radius: 7rpx;
            overflow: hidden;
        }

        .item-price {
            font-size: 21rpx;
            font-weight: 400;
            line-height: 28rpx;
            color: rgb(26, 26, 26);
        }
    }
}
</style>
