<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>
                分类模块（宽）设置
            </h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">选择分类</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-goods-group">
                    <SelectCategory :multiple="true" v-model:categoryId="module.catIds"></SelectCategory>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { SelectCategory } from "@/components/select";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcCatWideModule } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcCatWideModule);
</script>
