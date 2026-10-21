<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>类目导航</h3>
            <div class="dec-edit-desc">选择图片和链接, 可编辑文字, 方便您快速定制类目导航</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'category'">
            <div class="dec-edit-title-box">
                <div class="title">内容设置</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <PicList :isMultiple="true" v-model:photos="module.categoryList" title="添加一组" decorateType="mobile"></PicList>
                </div>
            </div>
            <div class="dec-edit-title-box">
                <div class="title">参数设置</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">列表样式</div>
                    <div class="value">{{ selectLabel.listStyleType[module.listStyleType as any] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.listStyleType">
                        <el-tooltip effect="light" placement="bottom" content="固定" :show-after="100">
                            <el-radio-button :value="'list'"><i class="iconfont-admin icon-guding-guding"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="横向滑动" :show-after="100">
                            <el-radio-button :value="'slide'"><i class="iconfont-admin icon-kuaijiebiaoqian-youhuadong"></i></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">每屏显示数量</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.rowNum">
                        <el-radio-button :value="2">2个</el-radio-button>
                        <el-radio-button :value="3">3个</el-radio-button>
                        <el-radio-button :value="4">4个</el-radio-button>
                        <el-radio-button :value="5">5个</el-radio-button>
                        <el-radio-button :value="6">6个</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <AnimationEdit v-model="module.animation" type="categoryA1"></AnimationEdit>
            <div class="dec-divider-line"></div>
            <CommonImageFillEdit v-model="module.picFillType"></CommonImageFillEdit>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片百分比</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picPercent" show-input :show-input-controls="false" size="small" input-size="default" :max="100" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">分类直角-圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.radius" show-input :show-input-controls="false" size="small" input-size="default" :max="200" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">分类间距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.categoryPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <CommonSubTitleEdit v-model="module.subTitle" title="分类标题"></CommonSubTitleEdit>
            
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="categoryA1"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="categoryA1"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { PicList } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref, computed } from "vue";
import CommonSubTitleEdit from "@/views/decorate/decorate/src/modules/src/commonSubTitle/Edit.vue";
import CommonImageFillEdit from "@/views/decorate/decorate/src/modules/src/commonImageFill/Edit.vue";
import ContentStyleEdit from "@/views/decorate/decorate/src/modules/src/contentStyle/Edit.vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import AnimationEdit from "@/views/decorate/decorate/src/modules/src/animation/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { CategoryNavModule } from "./types";

const tabs = ref<any[]>([{ label: '导航内容', name: 'category' }, { label: '扩展设置', name: 'extend' }]);
const activeName = ref("category");
const selectLabel = ref<any>({
    listStyleType: {
        "list": "固定",
        "slide": "横向滑动"
    }
});
// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，
// 模板内 v-model 直接写该 reactive 对象（与旧 defineModel 等价，公共子组件零改造复用）
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as CategoryNavModule);
</script>
