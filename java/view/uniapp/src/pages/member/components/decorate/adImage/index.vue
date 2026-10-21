<template>
    <view v-if="pickPic(data.pic)" class="dec-ad-image" :style="cssVars">
        <view class="dec-ad-image__wrapper" @click="handleClick">
            <tig-image class="dec-ad-image__img" :src="productImage(data.pic)" mode="widthFix" />
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";

import { redirect } from "@/utils";
import { pickPic, productImage, urlFormat } from "@/utils/format";
import { useFrameVars, type ModuleDataWithFrame } from "@/utils/decorateFrame";

interface PicItem {
    picUrl?: string;
    picThumb?: string;
}

interface PicLink {
    link?: string;
    name?: string;
}

interface AdImageData extends ModuleDataWithFrame {
    pic?: PicItem;
    picLink?: PicLink;
}

const props = defineProps<{
    data: AdImageData;
}>();

const cssVars = computed(() =>
    useFrameVars(props.data, {
        backgroundColor: "",
        boxRadius: 12,
        innerPadding: 0,
        boxPadding: 5,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    })
);

const handleClick = () => {
    const link = (props.data.picLink?.link as string | undefined)?.trim() || "";
    if (link) {
        redirect({ url: urlFormat(props.data.picLink as any) });
    }
};
</script>

<style lang="scss" scoped>
.dec-ad-image {
    box-sizing: border-box;
    padding-left: var(--dec-box-padding, 10rpx);
    padding-right: var(--dec-box-padding, 10rpx);
    padding-top: var(--dec-box-padding-top, 20rpx);
    padding-bottom: var(--dec-box-padding-bottom, 0);

    &__wrapper {
        background-color: var(--dec-bg-color, transparent);
        border-radius: var(--dec-border-radius, 24rpx);
        padding: var(--dec-inner-padding, 0);
        overflow: hidden;
    }

    &__img {
        width: 100%;
        display: block;
    }
}
</style>
