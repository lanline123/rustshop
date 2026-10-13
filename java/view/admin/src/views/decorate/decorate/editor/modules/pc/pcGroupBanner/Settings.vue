<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>分组Banner设置</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">Banner布局</div>
            </div>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">添加分组Banner轮播大图</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">提示：您可以通过拖拽进行图片排序，建议尺寸：1180*940像素（高清）或750*479像素（低分辨率）</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList"></PicList>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">一组显示的数量</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-input-group">
                    <div class="flex" style="justify-content: flex-end; flex-wrap: wrap">
                        <el-input
                            type="number"
                            style="width: 100px"
                            :min="1"
                            :max="3"
                            :step="1"
                            v-model.number="module.number"
                            placeholder=""></el-input>
                        <div v-if="module.number! > 3" class="yellow" style="width: 100%; text-align: right; margin-top: 10px">
                            最多只显示3个
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { PicList } from "@/components/decorate";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcGroupBannerModule } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcGroupBannerModule);
</script>
