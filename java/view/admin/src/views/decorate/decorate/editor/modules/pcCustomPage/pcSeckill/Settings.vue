<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>秒杀</h3>
        </div>
        <CommonActivityFrameEdit v-model="module">
            <template #customizationTemplate>
                <div class="dec-edit-group-title">
                    <div class="label">展示样式</div>
                    <div class="value">{{ selectLabel.style[module.style] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.style">
                        <el-tooltip effect="light" placement="bottom" content="一行四个" :show-after="100">
                            <el-radio-button :value="1">一行四个</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="一行五个" :show-after="100">
                            <el-radio-button :value="2">一行五个</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="横向滑动" :show-after="100">
                            <el-radio-button :value="6">横向滑动</el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </template>
        </CommonActivityFrameEdit>
        <CommonTitleEdit
            v-model="module.title"
            decorateType="mobile"
            enable-more-type
            more-type-legacy="seckillCustom"
            countdown-label-placeholder="距结束"
        ></CommonTitleEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import CommonActivityFrameEdit from "@/views/decorate/decorate/src/modules/src/commonActivityFrame/Edit.vue";
import CommonTitleEdit from "@/views/decorate/decorate/src/modules/src/commonTitle/Edit.vue";
import { ref, computed } from "vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcSeckillModule } from "./types";
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcSeckillModule);

const selectLabel = ref<any>({
    style: {
        1: "一行四个",
        2: "一行五个",
        6: "横向滑动",
    },
});
</script>
<style lang="less" scoped>
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
