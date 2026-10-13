<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>Banner设置</h3>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">添加Banner轮播图</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">提示：您可以通过拖拽进行图片排序，建议尺寸：1264*280像素</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList"></PicList>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { PicList } from "@/components/decorate";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcBanner2Module } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcBanner2Module);
</script>
