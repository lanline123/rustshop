<template>
    <tig-layout :title="$t('商家入驻协议')">
        <view class="agreement-box">
            <rich-text :nodes="content" />
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
import { getApplyShopAgreement } from "@/api/user/merchantEnter";
import { onLoad } from "@dcloudio/uni-app";
import { ref } from "vue";

const content = ref("");

const __getApplyShopAgreement = async () => {
    try {
        const result = await getApplyShopAgreement();
        content.value = result;
    } catch (error) {
        console.error(error);
    }
};

onLoad(() => {
    __getApplyShopAgreement();
});
</script>

<style>
page {
    background-color: #fff;
}
</style>
<style lang="scss" scoped>
.agreement-box {
    padding: 0 30rpx 15rpx;
    // line-height: 38rpx;
}
</style>
