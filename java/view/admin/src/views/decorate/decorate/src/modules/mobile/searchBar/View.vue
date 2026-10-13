<template>
    <div :class="'module-ad-con module-search_bar'" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content" :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius">
            <div class="searchBar-warp">
                <div class="searchBar-con">
                    <div class="searchBar-item">
                        <div class="item-content">
                            <div class="item-search-con">
                                <div
                                    class="item-search-form"
                                    :style="allFormat.itemBackgroundColor + allFormat.itemBorderRadius + allFormat.itemHeight + allFormat.textColor"
                                >
                                    <el-icon style="margin-left: 10px" color="#999" :size="18"><Search /></el-icon>
                                    <div class="item-search-input">{{ module?.text }}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed } from "vue";
import { imageFormat } from "@/utils/format";
import { ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultIcoPic } from "@/views/decorate/decorate/src/modules/";
import { Search } from "@element-plus/icons-vue";
// 在modules加入要使用的模块
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType>("module");
const defaultModule = ref({
    text: t('搜索商品'),
    icoPic: defaultIcoPic,
    frame: defaultFrame
});
mergeDefaultModule(module.value, defaultModule.value);
const { frame } = module.value;
const frameFormat = computed(() => {
    return formatFrame(frame);
});
const allFormat = computed(() => {
    return {
        itemBackgroundColor: `background-color: ${module?.value?.frame?.itemBackgroundColor};`,
        itemHeight: `height: ${module?.value?.frame?.itemHeight}px;`,
        itemBorderRadius: `border-radius: ${module?.value?.frame?.itemRadius}px;`,
        textColor: `color: ${module?.value?.frame?.textColor};`
    };
});
</script>
