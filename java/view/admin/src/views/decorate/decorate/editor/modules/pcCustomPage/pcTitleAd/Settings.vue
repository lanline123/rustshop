<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>文本标题</h3>
        </div>
        <CommonTitleEdit v-model="module.title" type="title" decorateType="mobile"></CommonTitleEdit>
        <CommonFrameEdit v-model="module.frame" type="titleAd"></CommonFrameEdit>

    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import CommonTitleEdit from "@/views/decorate/decorate/src/modules/src/commonTitle/Edit.vue";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcTitleAdModule } from "./types";
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcTitleAdModule);
</script>
