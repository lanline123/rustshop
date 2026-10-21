<template>
    <view class="pickup-info-box" :style="{ borderRadius }" @click="handleLink('/pages/store/contacts/list')">
        <view class="pickup-info">
            <template v-if="userPickupIfon && Object.keys(userPickupIfon).length !== 0">
                <view class="info-label">{{ $t("联系人") }}</view>
                <view class="info-content">{{ userPickupIfon.consignee }} {{ userPickupIfon.mobile }}</view>
            </template>
            <template v-else>
                <view class="info-tip">{{ $t("请选择自提联系人") }}</view>
            </template>
        </view>
        <view class="icon-box"><text class="iconfont-h5 icon-a-gengduo20" /></view>
        <view class="pickup-bg">
            <image :src="staticResource('store/pickup-bg.png')" />
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 自提联系人卡片组件
 * - 显示自提联系人姓名、手机号
 * - 点击可跳转选择或添加联系人
 */
import { type PropType } from "vue";
import type { UserPickupListRecord } from "@/types/store/userPicekup";
import { redirect, staticResource } from "@/utils";

defineProps({
    userPickupIfon: {
        type: Object as PropType<UserPickupListRecord>,
        default: () => ({})
    },
    borderRadius: {
        type: String,
        default: "0"
    }
});

const handleLink = (url: string) => {
    redirect({ url });
};
</script>

<style lang="scss" scoped>
.pickup-info-box {
    background: #fff;
    padding: 30rpx;
    overflow: hidden;
    width: 100%;
    display: flex;
    align-items: center;
    column-gap: 10rpx;
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

    .pickup-info {
        display: flex;
        align-items: center;
        flex: 1;
        font-size: 28rpx;
        color: #262626;

        .info-content {
            flex: 1;
            display: flex;
            justify-content: flex-end;
        }

        .info-tip {
            color: #6a6a6a;
        }
    }

    .icon-box {
        display: flex;
        align-items: center;
        .icon-a-gengduo20 {
            font-size: 19rpx;
            color: #8c8c8c;
        }
    }
}
</style>
