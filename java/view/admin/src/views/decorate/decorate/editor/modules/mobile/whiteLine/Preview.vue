<template>
    <div :class="'module-ad-con module-image_hotarea'" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content" :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius">
            <template v-if="Object.values(module).length === 0">
                <div class="module-ad-empty empty-image_ad">
                    <div class="image-empty-bg"></div>
                    <div class="desc">空白分割线</div>
                </div>
            </template>
            <template v-else>
                <div class="white-line-warp">
                    <div class="white-line-con">
                        <template v-if="module.lineType === 2">
                            <div
                                class="white-line-item"
                                :class="{ 'white-line-item2': module?.lineStyle === 2, 'white-line-item3': module?.lineStyle === 3 }"
                            >
                                <div class="item-content">
                                    <div class="item-line"></div>
                                </div>
                            </div>
                        </template>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { whiteLineDefaults } from "./index";
import type { WhiteLineModule } from "./types";

const props = defineProps<{ module: WhiteLineModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, whiteLineDefaults);

const frameFormat = computed(() => {
    return formatFrame(module.frame ?? {});
});
</script>
<style lang="less" scoped>
.white-line-warp .white-line-con .white-line-item2 {
    border: 1px dashed #e5e5e5;
}
.white-line-warp .white-line-con .white-line-item3 {
    border: 1px dotted #e5e5e5;
}
</style>
