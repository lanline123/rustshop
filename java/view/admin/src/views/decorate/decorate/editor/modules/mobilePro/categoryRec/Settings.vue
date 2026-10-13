<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>分类推荐</h3>
            <div class="dec-edit-desc">每个标签都支持 图标/文字/背景色设置</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'category'">
            <div class="dec-edit-title-box">
                <div class="title">内容设置</div>
            </div>
            <CategoryGroupEdit :isMultiple="true" v-model="module.categoryList" title="添加一组"></CategoryGroupEdit>
            <div class="dec-edit-title-box">
                <div class="title">参数设置</div>
            </div>
            <AnimationEdit v-model="module.animation" type="categoryA1"></AnimationEdit>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">分类内部间距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.categoryPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="10" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">分类圆角大小</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.radius" show-input :show-input-controls="false" size="small" input-size="default" :max="25" />
                    </div>
                </div>
            </div>
            <CommonSubTitleEdit v-model="module.subTitle" title="分类标题" type="categoryRec"></CommonSubTitleEdit>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label"> 分类背景颜色 </div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.backgroundColor = '#ffffff'">重置</a>
                            <SelectColor v-model:color="module.backgroundColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">分类边框设置</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.catBorder">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group bg-grey" v-if="module.catBorder === 1">
                <div class="dec-edit-group-title">
                    <div class="label">分类边框粗细</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.catBorderWidth" show-input :show-input-controls="false" size="small" input-size="default" :max="25" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group bg-grey" v-if="module.catBorder === 1">
                <div class="dec-edit-group-title">
                    <div class="label"> 分类边框颜色 </div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.catBorderColor = '#ffffff'">重置</a>
                            <SelectColor v-model:color="module.catBorderColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="categoryRec" :top-padding-max="100"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="categoryA1"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import Tabs from "@/components/tabs/Index.vue";
import { ref, computed } from "vue";
import CommonSubTitleEdit from "@/views/decorate/decorate/src/modules/src/commonSubTitle/Edit.vue";
import ContentStyleEdit from "@/views/decorate/decorate/src/modules/src/contentStyle/Edit.vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import AnimationEdit from "@/views/decorate/decorate/src/modules/src/animation/Edit.vue";
import CategoryGroupEdit from "@/views/decorate/decorate/src/modules/mobilePro/categoryRec/src/CategoryGroupEdit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { CategoryRecModule } from "./types";
const tabs = ref<any[]>([
    { label: "导航内容", name: "category" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("category");
const selectLabel = ref<any>({
    listStyleType: {
        list: "固定",
        slide: "横向滑动"
    }
});
// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，模板 v-model 直接写该 reactive 对象
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as CategoryRecModule);
</script>
