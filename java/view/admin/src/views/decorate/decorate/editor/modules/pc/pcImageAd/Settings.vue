<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>PC图片广告</h3>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">添加图片</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc" v-if="module.picType == 1">提示：您可以通过拖拽进行图片排序，单图占屏幕宽为100%，建议宽750px，高度自适应</div>
                <div class="pic-change-desc" v-if="module.picType == 2">
                    提示：您可以通过拖拽进行图片排序，建议手机端尺寸750x350px，高度统一；PC端宽不小于1500px（PC端选择时轮播默认通宽）
                </div>
                <div class="pic-change-desc" v-if="module.picType == 3">提示：您可以通过拖拽进行图片排序，单图占屏幕宽为80%，建议宽度600px，高度统一</div>
                <div class="pic-change-desc" v-if="module.picType == 4">提示：您可以通过拖拽进行图片排序，单图占屏幕宽为40%，建议宽度300px，高度统一</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList"></PicList>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">上边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxPaddingTop" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">下边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxPaddingBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed, ref } from "vue";
import { PicList } from "@/components/decorate";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcImageAdModule } from "./types";

const selectLabel = ref<any>({
    picType: {
        1: "一行一个",
        2: "轮播",
        3: "横向滑动-大图",
        4: "横向滑动-小图",
        5: "一行两个",
        6: "一行三个",
        7: "一行四个",
    },
    picPageType: {
        1: "样式一",
        2: "样式二",
        3: "样式三 ",
    },
});
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcImageAdModule);
</script>
