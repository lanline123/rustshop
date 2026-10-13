<template>
    <tig-popup v-model:show="show" position="center" background-color="transparent" :show-close="false" :mask-click="false">
        <view class="popupBox">
            <view class="title">{{ $t("提醒") }}</view>
            <view class="tip">{{ $t("买家秀功能已暂停使用，您仍可通过商品评价功能查看真实反馈") }}</view>
            <view class="btn" @click="handleLink('home')">{{ $t("返回首页") }}</view>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * 买家秀关闭提示组件
 * - 买家秀功能关闭时的提示
 */
import { redirect } from "@/utils";
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";

const buyerShowEnabled = defineModel("buyerShowEnabled", {
    type: Number,
    default: 1
});

const show = ref(false);

const handleLink = (type: string) => {
    if (type === "home") {
        redirect({ url: "/pages/index/index" });
    } else {
        // #ifdef H5
        history.back();
        // #endif

        // #ifndef H5
        uni.navigateBack();
        // #endif
    }
};

const open = () => {
    show.value = true;
};

const close = () => {
    show.value = false;
};

defineExpose({
    open,
    close
});

onLoad(() => {
    if (buyerShowEnabled.value !== 1) {
        setTimeout(() => {
            open();
        }, 100);
    }
});
</script>

<style scoped lang="scss">
.popupBox {
    padding: 45rpx 80rpx;
    box-sizing: border-box;
    font-size: 32rpx;
    display: flex;
    flex-direction: column;
    align-content: center;
    justify-content: center;
    width: 590rpx;
    height: 340rpx;
    background: #ffffff;
    border-radius: 16rpx;
    .title {
        text-align: center;
        font-weight: bold;
        font-size: 32rpx;
        color: #262626;
        margin-bottom: 40rpx;
    }
    .tip {
        font-weight: 500;
        font-size: 28rpx;
        color: #8c8c8c;
        margin-bottom: 76rpx;
        text-align: center;
    }
    .btn {
        text-align: center;
        // 下划线
        text-decoration: underline;
        color: var(--general);
        font-weight: 500;
        font-size: 28rpx;
    }
}
</style>
