<template>
    <div
        :class="'module-ad-con module-search_bar'"
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom + frameFormat.backgroundColor"
    >
        <div class="module-ad-content" :style="allFormat.itemBackgroundColor + ' '">
            <div class="oversea-sousuo-box">
                <div class="oversea-sousuo" :style="allFormat.itemHeight + allFormat.itemBorderRadius">
                    <div class="placeholder-text">{{ module?.text }}</div>
                    <div class="sousuo-icon">
                        <div class="iconfont-h5 icon-sousuo2" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { searchBar2Defaults } from "./index";
import type { SearchBar2Module } from "./types";

const props = defineProps<{ module: SearchBar2Module }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, searchBar2Defaults);

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

<style lang="less" scoped>
.oversea-sousuo-box {
    .oversea-sousuo {
        border: 1px solid #1a1a1a;
        border-radius: 4px;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 0 12px;

        .placeholder-text {
            color: #9c9ea6;
        }

        .sousuo-icon {
            flex: 1;
            display: flex;
            justify-content: flex-end;
            
            .icon-sousuo2 {
                font-size: 20px;
            }
        }
    }
}
</style>
