<template>
    <view class="address-info">
        <view class="address-content" @click="handleAddressMore">
            <view class="address-box">
                <template v-if="data && Object.keys(data).length > 0">
                    <view class="user-info">
                        <view class="name">
                            <view class="consignee">{{ data.consignee }}</view>
                            <view class="mobile">{{ displayMobile }}</view>
                        </view>
                    </view>
                    <view class="address">{{ displayAddress }}</view>
                </template>
                <template v-else>
                    <view class="user-info">
                        <view class="name">{{ $t("请选择收货地址") }}</view>
                    </view>
                </template>
            </view>
            <view class="iconfont-h5 icon-fanhui" />
        </view>
    </view>
</template>

<script lang="ts" setup>
/**
 * 收货地址信息展示组件（样式对齐海外版）
 * - 显示收货人姓名、手机号、详细地址
 * - 点击跳转地址列表选择或添加地址
 */
import { computed } from "vue";
import { isOverseas } from "@/utils";

const props = defineProps({
    data: {
        type: Object,
        default: () => ({})
    }
});

const isOverseasMode = isOverseas();

const displayMobile = computed(() => {
    return props.data.mobile;
});

const displayAddress = computed(() => {
    return [props.data.regionName, props.data.address].filter(Boolean).join(" ");
});

const handleAddressMore = () => {
    uni.navigateTo({
        url: "/pages/address/list"
    });
};
</script>

<style lang="scss" scoped>
.address-info {
    margin-bottom: 14rpx;

    .address-content {
        display: flex;
        align-items: center;
        justify-content: space-between;
        background-color: #fff;
        position: relative;
        padding-bottom: 4px;
        padding-right: 20rpx;

        .icon-fanhui {
            font-size: 17rpx;
            color: #888b94;
            transform: rotate(180deg);
        }
    }

    .address-box {
        padding: 20rpx;
        display: flex;
        flex-direction: column;
    }

    .user-info {
        color: #333;
        display: block;
        font-size: 24.5rpx;
        font-weight: bold;
        display: flex;
    }
    .name {
        margin-right: 10rpx;
        flex: 1;
        display: flex;
        align-items: center;
        column-gap: 21rpx;

        .mobile {
            font-weight: 400;
        }

        /* @vort-if overseas */
        .invalid-tag {
            padding: 2rpx 10rpx;
            border: 1rpx solid #ff4d4f;
            border-radius: 6rpx;
            color: #ff4d4f;
            font-size: 20rpx;
            font-weight: 400;
            line-height: 1.4;
        }
        /* @vort-endif */
    }

    .address {
        color: #505259;
        display: block;
        font-size: 24.5rpx;
        flex: 1;
        margin-top: 6px;
    }
}
</style>
