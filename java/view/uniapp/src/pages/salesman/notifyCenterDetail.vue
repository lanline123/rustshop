<template>
    <tig-layout :title="navbarTitle">
        <view class="notify-box">
            <!-- <rich-text :nodes="detailData.content" /> -->
            <up-parse :content="detailData.content" />
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 通知详情页
 * - 展示分销员通知消息详情
 * - 富文本渲染消息内容
 */
import { ref } from "vue";
import { salesmanContentDetail } from "@/api/salesman/salesman";
import { onLoad } from "@dcloudio/uni-app";
import type { SalesmanContentListFilterResult } from "@/types/salesman/salesman";

const { t } = useI18n();
const navbarTitle = ref(t("详情"));

const detailData = ref<SalesmanContentListFilterResult>({} as SalesmanContentListFilterResult);
const getSalesmanContentDetail = async (id: number) => {
    try {
        const result = await salesmanContentDetail(id);
        detailData.value = result;
        navbarTitle.value = result.title;
    } catch (error) {
        console.log(error);
    }
};

onLoad((option) => {
    if (option && option.id) {
        getSalesmanContentDetail(Number(option.id));
    }
});
</script>

<style>
page {
    background-color: #fff;
}
</style>
<style lang="scss" scoped>
.notify-box {
    padding: 20rpx;
    line-height: 45rpx;
}
</style>
