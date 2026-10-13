<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>空白分割</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">分割类型</div>
                <div class="value">{{ selectLabel.lineType[module.lineType || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.lineType">
                    <el-radio-button :value="1">空白</el-radio-button>
                    <el-radio-button :value="2">分割线</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">选择样式</div>
                <div class="value">{{ selectLabel.lineStyle[module.lineStyle || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.lineStyle">
                    <el-radio-button :value="1">实线</el-radio-button>
                    <el-radio-button :value="2">虚线</el-radio-button>
                    <el-radio-button :value="3">点状线</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <CommonFrameEdit v-model="module.frame" type="line"></CommonFrameEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { WhiteLineModule } from "./types";

// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，
// 模板内 v-model 直接写该 reactive 对象（与旧 defineModel 等价，公共子组件零改造复用）
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as WhiteLineModule);

const selectLabel = {
    lineType: {
        1: "空白",
        2: "分割线"
    },
    lineStyle: {
        1: "实线",
        2: "虚线",
        3: "点状线"
    }
} as Record<string, Record<string | number, string>>;
</script>
