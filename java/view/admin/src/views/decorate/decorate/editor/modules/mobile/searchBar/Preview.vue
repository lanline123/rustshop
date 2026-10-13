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
import { computed } from "vue";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { Search } from "@element-plus/icons-vue";
import { searchBarDefaults } from "./index";
import type { SearchBarModule } from "./types";

const props = defineProps<{ module: SearchBarModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, searchBarDefaults);
const { frame } = module;
const frameFormat = computed(() => {
    return formatFrame(frame);
});
const allFormat = computed(() => {
    return {
        itemBackgroundColor: `background-color: ${module?.frame?.itemBackgroundColor};`,
        itemHeight: `height: ${module?.frame?.itemHeight}px;`,
        itemBorderRadius: `border-radius: ${module?.frame?.itemRadius}px;`,
        textColor: `color: ${module?.frame?.textColor};`
    };
});
</script>
