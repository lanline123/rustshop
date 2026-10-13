<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>公告</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">公告内容</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-input v-model="module.text" style="width: 240px" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">公告图标</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <PicSelect v-model:photo="module.icoPic " :default-value="{picUrl:'https://oss.tigshop.com/img/gallery/202508/1754895669bP3iJSUiXSBoRB1VQq.png'}"></PicSelect>
            </div>
        </div>
        <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">公告文字效果</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.nociceEffect">
                        <el-tooltip effect="light" placement="bottom" content="滚动" :show-after="100">
                            <el-radio-button :value="'scroll'">滚动</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="静止" :show-after="100">
                            <el-radio-button :value="'still'">静止</el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
        <CommonFrameEdit v-model="module.frame" type="notice"></CommonFrameEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { PicSelect } from '@/components/decorate'
import { ref, computed } from "vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { NoticeModule } from "./types";
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as NoticeModule);
const selectLabel = ref<any>({
    lineType: {
        1: "空白",
        2: "分割线",
    },
    lineStyle: {
        1: "实线",
        2: "虚线",
        3: "点状线",
    }
});
</script>
<style lang="less">
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
