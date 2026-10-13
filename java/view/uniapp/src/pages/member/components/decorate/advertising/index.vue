<template>
    <view class="advertising-box">
        <view class="advertising-content">
            <image class="advertising-image" :src="imageFormat(data.picUrl)" mode="widthFix" @click="handleClick(data)" />
        </view>
    </view>
</template>

<script setup lang="ts">
import { imageFormat, urlFormat } from "@/utils/format";
import { redirect } from "@/utils";
const props = defineProps({
    data: {
        type: Object as any,
        default: () => ({})
    }
});

const handleClick = (item: any) => {
    if (item?.picLink) {
        redirect({ url: urlFormat(item.picLink) });
    }
};
</script>

<style lang="scss" scoped>
.advertising-box {
    border-radius: 18rpx;
    overflow: hidden;
    .advertising-content {
        display: flex;
        justify-content: center;

        .advertising-image {
            width: 100%;
        }
    }
}
</style>
