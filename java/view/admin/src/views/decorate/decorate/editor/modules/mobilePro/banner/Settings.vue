<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>首焦轮播</h3>
            <div class="dec-edit-desc">图片个性轮播展示，可设置搜索框，导航，背景颜色可随图片轮播切换</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'banner'">
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">商城logo设置</div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片设置</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="module.logoPic"></PicSelect>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片高度</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.logoHeight" show-input :show-input-controls="false" size="small" input-size="default" :max="500" />
                    </div>
                </div>
            </div>
            <BannerContent v-model="module.bannerContent"></BannerContent>
            <div class="dec-divider-line"></div>
            <BannerSearch v-model="module.searchContent"></BannerSearch>
            <div class="dec-divider-line"></div>
            <BannerNav v-model="module.navContent"></BannerNav>
        </div>
        <div v-if="activeName === 'style'">
            <BannerStyle v-model="module.bannerStyle"></BannerStyle>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="banner"></ModuleStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { PicSelect } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref, computed } from "vue";
import BannerContent from "@/views/decorate/decorate/src/modules/mobilePro/banner/src/BannerContent.vue";
import BannerSearch from "@/views/decorate/decorate/src/modules/mobilePro/banner/src/BannerSearch.vue";
import BannerNav from "@/views/decorate/decorate/src/modules/mobilePro/banner/src/BannerNav.vue";
import BannerStyle from "@/views/decorate/decorate/src/modules/src/styleEdit/BannerStyle.vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { BannerModule } from "./types";
const tabs = ref<any[]>([
    { label: "基础设置", name: "banner" },
    { label: "样式设置", name: "style" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("banner");
// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，模板 v-model 直接写该 reactive 对象
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as BannerModule);
</script>
