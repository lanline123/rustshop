<template>
    <view class="to-login">
        <image :src="staticResource('common/login.png')" mode="widthFix" />
        <view class="to-login-text">{{ $t("暂未登录") }}</view>
        <view style="width: 114px">
            <tig-button style="height: 60rpx" @click="handleLogin">{{ $t("去登录") }}</tig-button>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 购物车登录引导组件
 * - 未登录状态显示登录提示
 * - 点击跳转登录页面
 */
import { staticResource } from "@/utils";
import { handleLogin } from "@/utils/handleLogin";
</script>

<style lang="scss" scoped>
.to-login {
    width: 100%;
    height: 100%;
    background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    position: fixed;

    .to-login-text {
        color: rgba(153, 153, 153, 1);
        font-size: 26rpx;
        padding-bottom: 40rpx;
    }
}
</style>
