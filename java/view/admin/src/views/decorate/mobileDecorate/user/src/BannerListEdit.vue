<template>
    <div class="dec-spread-title">
        <div class="title">{{ $t('广告图设置') }}</div>
    </div>
    <div class="dec-edit-group dec-edit-group-block">
        <div class="dec-edit-group-title">
            <div class="title">{{ $t('添加图片') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <PicList v-model:photo="module.bannerList" decorateType="mobile"></PicList>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/views/decorate/decorate/src/css/decorate.less";
import { DecorateUserBanner } from "@/types/decorate/decorateDiscrete.d";
import { ref } from "vue";
import { PicList } from "@/components/decorate";
const module = defineModel<DecorateUserBanner>("module", {
    type: Object as () => DecorateUserBanner,
    default: {
        bannerList: {
            picUrl: ""
        }
    }
});
</script>
<style lang="less" scoped></style>
