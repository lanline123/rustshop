<template>
    <template v-if="imgUrl">
        <view class="copyright">
            <view class="copyright-bg" :style="{ backgroundImage: `url(${imgUrl})` }" />
        </view>
    </template>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useConfigStore } from "@/store/config";
import { isUrl } from "@/utils";
import { baseUrl } from "@/utils/request";

const configStore = useConfigStore();

const urlFormat = (url: string) => {
    if (!url) {
        return "";
    }
    if (isUrl(url)) {
        return url;
    }
    return `${baseUrl ? baseUrl : location.origin}${url}`;
};

const imgUrl = computed(() => {
    let url = urlFormat(configStore.defaultTechSupport);
    if (configStore.poweredBy == 0) {
        url = configStore.poweredByLogo;
    }

    return url;
});
</script>

<style lang="scss" scoped>
.copyright {
    padding: 40rpx 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    row-gap: 20rpx;

    .copyright-bg {
        max-width: 100%;
        overflow: hidden;
        background-position: 50%;
        width: 320rpx;
        height: 32rpx;
        background-repeat: no-repeat;
        background-size: contain;
    }

    .shop-company {
        color: #dfdfdf;
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 26rpx;
    }
}
</style>
