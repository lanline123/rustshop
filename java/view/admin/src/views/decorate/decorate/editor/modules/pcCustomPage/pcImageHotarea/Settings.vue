<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>绘制热区</h3>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">提示：添加图片后，点击图片进入绘制</div>
            </div>
            <div class="dec-edit-group-con">
                <PcImageHotarea v-model:photos="module.picList"></PcImageHotarea>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">图片边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.imgPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <CommonFrameEdit v-model="module.frame"></CommonFrameEdit>
        <CommonTitleEdit v-model="module.title" decorateType="mobile"></CommonTitleEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ref, computed } from "vue";
import CommonTitleEdit from "@/views/decorate/decorate/src/modules/src/commonTitle/Edit.vue";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import PcImageHotarea from "@/components/decorate/src/PcImageHotarea.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcImageHotareaModule } from "./types";
const selectLabel = ref({
    picType: {
        1: "一行一个",
        2: "轮播",
        3: "横向滑动-大图",
        4: "横向滑动-小图",
        5: "一行两个",
        6: "一行三个",
        7: "一行四个"
    },
    picPageType: {
        1: "样式一",
        2: "样式二",
        3: "样式三 "
    }
});
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcImageHotareaModule);
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
