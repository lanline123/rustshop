<template>
    <tig-popup v-model:show="modelValue" position="center" :height="'60vh'" background-color="transparent" :round="0" :show-close="false">
        <view class="address-pop">
            <view class="pop-center">{{ $t("您的地址未关联具体定位信息，请重新修改地址信息") }}</view>
            <view class="pop-btns">
                <tig-button class="btn" :plain="true" @click="modelValue = false">{{ $t("取消") }}</tig-button>
                <tig-button class="btn" @click="$emit('link')"> {{ $t("去修改") }} </tig-button>
            </view>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * 地址选择弹窗组件
 * - 弹窗展示收货地址列表
 * - 支持选择地址并回传
 */
const modelValue = defineModel({
    type: Boolean,
    default: false
});

defineEmits(["link"]);
</script>

<style lang="scss" scoped>
.address-pop {
    background-color: #fff;
    width: 590rpx;
    padding: 0 40rpx;
    border-radius: 16rpx;
    overflow: hidden;

    .pop-center {
        background-color: #fff;
        padding: 70rpx 40rpx 50rpx;
        font-size: 28rpx;
        line-height: 46rpx;
        text-align: center;
    }

    .pop-btns {
        display: flex;
        column-gap: 20rpx;
        padding-bottom: 40rpx;

        .btn {
            flex: 1;
            height: 65rpx;
        }
    }
}
</style>
