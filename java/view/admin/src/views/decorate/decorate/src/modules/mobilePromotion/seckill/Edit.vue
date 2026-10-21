<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('秒杀') }}</h3>
        </div>
        <CommonActivityFrameEdit v-model="module"></CommonActivityFrameEdit>
        <CommonTitleEdit v-model="module.title" decorateType="mobile"></CommonTitleEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import CommonActivityFrameEdit from "../../src/commonActivityFrame/Edit.vue";
import CommonTitleEdit from "../../src/commonTitle/Edit.vue";
import { ModuleActivityType } from "@/types/decorate/decorate.d";
const module = defineModel<ModuleActivityType>("module", { default: () => ({}) });
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
