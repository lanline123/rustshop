<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>商品</h3>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">展示样式</div>
                <div class="value">{{ selectLabel.style[module.style] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.style">
                    <el-tooltip effect="light" placement="bottom" content="一行四个" :show-after="100">
                        <el-radio-button :value="1">一行四个</el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" content="一行五个" :show-after="100">
                        <el-radio-button :value="2">一行五个</el-radio-button>
                    </el-tooltip>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <CommonActivityFrameEdit v-model="module" type="product"></CommonActivityFrameEdit>
        <div class="dec-edit-group" v-if="module.style == 7">
            <div class="dec-edit-group-title">
                <div class="label">指示器颜色</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset">重置</a>
                        <SelectColor v-model:color="module.swiperPageColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <CommonTitleEdit v-model="module.title" type="product" decorateType="mobile"></CommonTitleEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ref, computed } from "vue";
import CommonTitleEdit from "@/views/decorate/decorate/src/modules/src/commonTitle/Edit.vue";
import { SelectColor } from "@/components/select";
import CommonActivityFrameEdit from "@/views/decorate/decorate/src/modules/src/commonActivityFrame/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcProductModule } from "./types";
const selectLabel = ref<any>({
    style: {
        1: "一行四个",
        2: "一行五个"
    },
    picPageType: {
        1: "样式一",
        2: "样式二",
        3: "样式三 "
    }
});
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcProductModule);
</script>
