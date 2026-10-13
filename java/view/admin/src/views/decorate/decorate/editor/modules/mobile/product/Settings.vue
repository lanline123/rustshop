<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>商品</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-desc">
                <div>提示：仅“一行两个”模板类型支持<b style="color: #333">瀑布流</b>展示，为了展示效果，请尽量将瀑布流组件置于底部，一个页面不要超过一个</div>
            </div>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">选择模板</div>
                <div class="value">{{ selectLabel.style[module.style] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.style">
                    <el-tooltip effect="light" placement="bottom" content="一行一个" :show-after="100">
                        <el-radio-button :value="1"><i class="ico-decorate icon-dec-up2end"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" content="一行两个" :show-after="100">
                        <el-radio-button :value="2"><i class="ico-decorate icon-dec-small"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" content="一行三个" :show-after="100">
                        <el-radio-button :value="3"><i class="ico-decorate icon-dec-three"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" content="一大两小" :show-after="100">
                        <el-radio-button :value="4"><i class="ico-decorate icon-dec-hybrid"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" content="详细列表" :show-after="100">
                        <el-radio-button :value="5"><i class="ico-decorate icon-dec-list"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" content="横向滑动" :show-after="100">
                        <el-radio-button :value="6"><i class="ico-decorate icon-dec-swipe"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" content="轮播滑动" :show-after="100">
                        <el-radio-button :value="7"><i class="ico-decorate icon-dec-indicator-1"></i></el-radio-button>
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
import type { ProductModule } from "./types";
const selectLabel = ref<any>({
    style: {
        1: "一行一个",
        2: "一行两个",
        3: "一行三个",
        4: "一大两小",
        5: "详细列表",
        6: "横向滑动",
        7: "轮播滑动"
    },
    picPageType: {
        1: "样式一",
        2: "样式二",
        3: "样式三 "
    }
});
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as ProductModule);
</script>
