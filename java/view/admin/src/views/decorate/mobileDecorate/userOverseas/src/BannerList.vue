<template>
    <div class="banner-box">
        <img v-if="module.bannerList.picUrl" :src="imageFormat(module.bannerList.picUrl)" />
        <div class="banner-text" v-else> {{ $t('广告图区域') }} </div>
    </div>
</template>
<script lang="ts" setup>
import "@/views/decorate/decorate/src/css/decorate.less";
import { imageFormat } from "@/utils/format";
import { DecorateUserBanner } from "@/types/decorate/decorateDiscrete.d";
const props = defineProps({
    module: {
        type: Object as () => DecorateUserBanner,
        default: () => ({
            bannerList: {
                picUrl: ""
            }
        })
    }
});
</script>
<style lang="less" scoped>
.banner-box {
    margin: 9px 0 9px 0;
    position: relative;
    img {
        width: 100%;
        border-radius: 5px;
    }
    &::before {
        content: "";
        height: 100%;
        position: absolute;
        box-sizing: border-box;
        z-index: 99;
        cursor: pointer;
    }
    .banner-text{
        width: 100%;
        height: 100px;
        background-color: #fff;
        display: flex;
        justify-content: center; /* 水平居中 */
        align-items: center; /* 垂直居中 */
    }
}
</style>
