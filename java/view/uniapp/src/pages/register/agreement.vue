<template>
    <tig-layout :title="$t('注册协议')">
        <view class="agreement-box">
            <rich-text :nodes="content" />
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
import { getArticle } from "@/api/article/article";
import { onLoad } from "@dcloudio/uni-app";
import { ref } from "vue";

const content = ref("");

const __getApplyShopAgreement = async () => {
    try {
        const result = await getArticle({ articleSn: "zcxy" }, "issueInfo");
        content.value = result.item.content!;
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
    line-height: 45rpx;
}
</style>
