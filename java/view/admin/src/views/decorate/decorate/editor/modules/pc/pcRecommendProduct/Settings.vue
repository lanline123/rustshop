<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>
                推荐商品-瀑布流
            </h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">标题名称</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-input-group">
                    <el-input v-model="module.titleName" placeholder="请输入标题名称"></el-input>
                </div>
            </div>
        </div>
        <CommonProductEdit v-model="module.products" :numberMin="20" :numberStep="10"></CommonProductEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import CommonProductEdit from "@/views/decorate/decorate/src/modules/src/commonProduct/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcRecommendProductModule } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcRecommendProductModule);
</script>
