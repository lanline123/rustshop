<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('首焦轮播A2-多门店首页') }}</h3>
            <div class="dec-edit-desc">{{ $t('图片个性轮播展示，可设置搜索框，导航，背景颜色可随图片轮播切换') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'banner'">
            <BannerContent v-model="module.bannerContent"></BannerContent>
            <div class="dec-divider-line"></div>
            <BannerSearch v-model="module.searchContent"></BannerSearch>
            <!-- <div class="dec-divider-line"></div> -->
            <!-- <BannerNav v-model="module.navContent"></BannerNav> -->
        </div>
        <div v-if="activeName === 'style'">
            <BannerStyle v-model="module.bannerStyle" type="bannerA2"></BannerStyle>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="banner"></ModuleStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { PicSelect } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref } from "vue";
import BannerContent from "./src/BannerContent.vue";
import BannerSearch from "./src/BannerSearch.vue";
import BannerNav from "./src/BannerNav.vue";
import BannerStyle from "../../src/styleEdit/BannerStyle.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import { ModuleType, ModuleBannerType } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([
    { label: t('基础设置'), name: "banner" },
    { label: t('样式设置'), name: "style" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("banner");
const module = defineModel<ModuleType & ModuleBannerType>("module", { default: () => ({}) });
</script>
