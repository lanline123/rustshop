<template>
    <view class="error-page">
        <view class="error-content">
            <view class="error-image" />
            <view class="error-text">{{ $t("页面失效") }}</view>
            <view class="error-btn-box">
                <view theme class="error-btn" @click="handleClick">{{ $t("进店逛逛") }}</view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 商品下架/异常页组件
 * - 显示商品不存在或已下架提示
 * - 提供返回首页按钮
 */
import { redirect } from "@/utils";
import { staticResource } from "@/utils";
import { computed } from "vue";

const handleClick = () => {
    redirect({ url: "/pages/index/index" });
};

const errorImg = computed(() => {
    return `url("${staticResource("product/noDetail.png")}")`;
});
</script>

<style lang="scss" scoped>
.error-content {
    padding: 48rpx 0 200rpx;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;

    .error-image {
        display: inline-block;
        width: 181rpx;
        height: 171rpx;
        margin-bottom: 48rpx;
        background-image: v-bind(errorImg);
        background-repeat: no-repeat;
        background-size: cover;
        background-position: 50%;
    }

    .error-btn-box {
        display: flex;
        justify-content: center;
        font-size: 24.5rpx;
        .error-btn {
            height: 53rpx;
            line-height: 53rpx;
            padding: 0 20rpx;
            background-color: var(--main-bg);
            color: var(--main-text);
        }
    }

    .error-text {
        margin: 0 0 6.66667vw;
        color: #646464;
    }
}
</style>
