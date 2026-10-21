<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>分类导航</h3>
        </div>
        <div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">每屏显示数量</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.rowNum">
                        <el-radio-button :value="2">2个</el-radio-button>
                        <el-radio-button :value="3">3个</el-radio-button>
                        <el-radio-button :value="4">4个</el-radio-button>
                        <el-radio-button :value="5">5个</el-radio-button>
                        <el-radio-button :value="6">6个</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="categoryA1"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="categoryA1"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed, ref } from "vue";
import ContentStyleEdit from "@/views/decorate/decorate/src/modules/src/contentStyle/Edit.vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcCategoryNavModule } from "./types";

const activeName = ref("category");

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcCategoryNavModule);
</script>
