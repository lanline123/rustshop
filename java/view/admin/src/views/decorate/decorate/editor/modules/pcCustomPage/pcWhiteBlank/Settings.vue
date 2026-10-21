<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>空白辅助</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">背景颜色</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset"
                            @click="module.backgroundColor = defaultFrame.backgroundColor">重置</a>
                        <SelectColor v-model:color="module.backgroundColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">高度</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.blankHeight" show-input :show-input-controls="false" size="small"
                        input-size="default" :max="100" />
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { SelectColor } from "@/components/select";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcWhiteBlankModule } from "./types";
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcWhiteBlankModule);
</script>
<style lang="less">
.handle {
    box-sizing: border-box;
    position: absolute;
    background: #fff;
    border: 1px solid #155bd4;
}

.vue-drag-resize-rotate {
    touch-action: none;
    position: absolute;
    box-sizing: border-box;
    border: 1px solid transparent;
    background: rgba(21, 91, 212, 0.6);
    color: #fff;
}

.vue-drag-resize-rotate.active {
    background: rgba(21, 91, 212, 0.9);
    border: 1px solid #155bd4;
}

.vue-drag-resize-rotate:hover {
    border: 1px solid #155bd4;
}
</style>
