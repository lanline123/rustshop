<template>
    <view class="delivery-type-box">
        <template v-if="hasLocalDelivery && hasPickupDelivery && hasStoreDelivery">
            <view class="delivery-type">
                <view class="delivery-menu">
                    <view class="menu-item" :class="{ acitve: deliveryType === 1 }" @click="handleChange(1)">{{
                        $t(configStore.defaultLogisticsName || "商家配送")
                    }}</view>
                    <view class="menu-item" :class="{ acitve: deliveryType === 2 }" @click="handleChange(2)">{{ $t("到店自提") }}</view>
                    <view class="menu-item" :class="{ acitve: deliveryType === 3 }" @click="handleChange(3)">{{ $t("同城配送") }}</view>
                    <view v-show="deliveryType === 3" class="menu-bg-box right new">
                        <image :src="staticResource('store/delivery-menu-newbg.png')" />
                    </view>
                    <view v-show="deliveryType === 2" class="menu-bg-box middle">
                        <image :src="staticResource('store/delivery-menu-middlebg.png')" />
                    </view>
                    <view v-show="deliveryType === 1" class="menu-bg-box left new">
                        <image :src="staticResource('store/delivery-menu-newbg.png')" />
                    </view>
                </view>
            </view>
        </template>

        <template v-else>
            <view class="delivery-type">
                <view class="delivery-menu">
                    <template v-for="item in menuList" :key="item.id">
                        <view class="menu-item" :class="{ acitve: deliveryType === item.id }" @click="handleChange(item.id)">{{ item.name }}</view>
                    </template>

                    <view v-show="currentIndex === 1" class="menu-bg-box right">
                        <image :src="staticResource('store/delivery-menu-bg.png')" />
                    </view>
                    <view v-show="currentIndex === 0" class="menu-bg-box left">
                        <image :src="staticResource('store/delivery-menu-bg.png')" />
                    </view>
                </view>
            </view>
        </template>

        <view class="card-box">
            <template v-if="deliveryType === 1 || deliveryType === 3">
                <view class="address-box card" @click="handleLink('/pages/address/list')">
                    <view class="address-info">
                        <template v-if="Object.keys(getAddressInfo).length === 0">
                            <view class="address-text line1">{{ $t("请选择收货地址") }}</view>
                        </template>
                        <template v-else>
                            <view class="user-info">
                                <text>{{ getAddressInfo.consignee }}</text>
                                <text>{{ getAddressInfo.mobile }}</text>
                            </view>
                            <view class="address-text line1">{{ getAddressInfo.regionName }} {{ getAddressInfo.address }}</view>
                        </template>
                    </view>
                    <view class="icon-box"><text class="iconfont-h5 icon-a-gengduo20" /></view>
                    <view class="pickup-bg">
                        <image :src="staticResource('store/pickup-bg.png')" />
                    </view>
                </view>
            </template>
            <template v-else-if="deliveryType === 2">
                <pickupUserCard :user-pickup-ifon="userPickupIfon" />
            </template>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 配送方式切换卡片组件
 * - 支持商家配送、到店自提、同城配送三种方式切换
 * - 根据配送方式显示对应的收货地址或自提联系人信息
 * - 带动画效果的Tab切换
 */
import { computed, type PropType } from "vue";
import { useI18n } from "vue-i18n";
import type { AddressFilterResult } from "@/types/user/address";
import { redirect, staticResource } from "@/utils";
import type { UserPickupListRecord } from "@/types/store/userPicekup";
import pickupUserCard from "@/pages/order/src/pickupUserCard.vue";
import { useConfigStore } from "@/store/config";

const { t } = useI18n();
const configStore = useConfigStore();

const props = defineProps({
    getAddressInfo: {
        type: Object as PropType<AddressFilterResult>,
        default: () => ({})
    },
    userPickupIfon: {
        type: Object as PropType<UserPickupListRecord>,
        default: () => ({})
    },
    hasStoreDelivery: {
        // 是否有商城配送
        type: Boolean,
        default: false
    },
    hasLocalDelivery: {
        // 是否有同城配送
        type: Boolean,
        default: false
    },
    hasPickupDelivery: {
        // 是否有到店自提
        type: Boolean,
        default: false
    }
});

const menuList = computed(() => {
    const list = [];
    // 保持与三 tab 全开时一致的固定顺序：商家配送(1) → 到店自提(2) → 同城配送(3)
    if (props.hasStoreDelivery) {
        list.push({ id: 1, name: t(configStore.defaultLogisticsName || "商家配送") });
    }
    if (props.hasPickupDelivery) {
        list.push({ id: 2, name: t("到店自提") });
    }
    if (props.hasLocalDelivery) {
        list.push({ id: 3, name: t("同城配送") });
    }
    return list;
});

const currentIndex = computed(() => {
    return menuList.value.findIndex((item) => item.id === deliveryType.value);
});

const deliveryType = defineModel("deliveryType", {
    type: Number,
    default: 1
});

const emit = defineEmits(["change"]);

const handleChange = (val: number) => {
    deliveryType.value = val;
    emit("change", val);
};

const handleLink = (url: string) => {
    redirect({ url });
};
</script>

<style lang="scss" scoped>
.delivery-type-box {
    padding-top: 20rpx;

    .delivery-type {
        .delivery-menu {
            display: flex;
            height: 98rpx;
            align-items: center;
            position: relative;
            background-color: #efefef;
            border-radius: 68rpx 68rpx 0rpx 0rpx;

            .menu-item {
                flex: 1;
                height: 100%;
                font-weight: 600;
                font-size: 32rpx;
                color: #6a6a6a;
                display: flex;
                align-items: center;
                justify-content: center;
                position: relative;
                z-index: 1;

                &.acitve {
                    color: #2a2a2a;
                }
            }

            .menu-bg-box {
                position: absolute;
                height: 101%;
                width: 426rpx;
                z-index: 0;

                &.new {
                    width: 289rpx;

                    &.right {
                        image {
                            transform: scaleX(-1);
                        }
                    }
                    &.left {
                        left: 0;

                        image {
                            transform: none;
                        }
                    }
                }

                image {
                    width: 100%;
                    height: 100%;
                }

                &.right {
                    right: 0;
                }

                &.left {
                    left: 0;

                    image {
                        transform: scaleX(-1);
                    }
                }

                &.middle {
                    left: 50%;
                    top: 0;
                    transform: translateX(-50%);
                    width: 348rpx;
                    height: 106%;

                    image {
                        transform: none;
                    }
                }
            }
        }
    }

    .card-box {
        // padding-top: 27rpx;
        background: linear-gradient(0deg, #f4f3f3, #fcfcfc);
    }

    .card {
        border-radius: 0;
        background: #fff;
        padding: 30rpx;
        overflow: hidden;
    }

    .address-box {
        width: 100%;
        display: flex;
        align-items: center;
        column-gap: 30rpx;
        position: relative;

        .pickup-bg {
            position: absolute;
            left: 0;
            bottom: 0;
            width: 100%;
            height: 6rpx;

            image {
                width: 100%;
                height: 100%;
                display: block;
            }
        }

        .address-info {
            flex: 1;
            display: flex;
            flex-direction: column;
            row-gap: 27rpx;
            overflow: hidden;

            .user-info {
                display: flex;
                column-gap: 30rpx;
                font-weight: bold;
                font-size: 28rpx;
                color: #262626;
                align-items: center;
            }

            .address-text {
                font-weight: 500;
                font-size: 28rpx;
                color: #6a6a6a;
            }
        }

        .icon-box {
            display: flex;
            align-items: center;
            .icon-a-gengduo20 {
                font-size: 35rpx;
                color: #8c8c8c;
            }
        }
    }
}
</style>
