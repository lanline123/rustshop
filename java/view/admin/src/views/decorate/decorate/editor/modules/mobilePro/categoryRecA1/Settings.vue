<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>分类推荐A1</h3>
            <div class="dec-edit-desc">选择图片和链接，编辑标题和副标题，最多添加4个</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>

        <div v-if="activeName === 'content'">
            <div class="dec-edit-title-box">
                <div class="title">基本设置</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <PicList
                        :isMultiple="true"
                        :showText="true"
                        :showDesc="true"
                        :maxCount="4"
                        v-model:photos="module.picList"
                        decorateType="mobile"
                    ></PicList>
                </div>
            </div>

            <div class="dec-edit-title-box">
                <div class="title">参数设置</div>
            </div>

            <CommonImageFillEdit v-model="module.picFillType"></CommonImageFillEdit>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.picRadius"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="40"
                        />
                    </div>
                </div>
            </div>

            <CommonSubTitleEdit v-model="module.subTitle" title="分类标题"></CommonSubTitleEdit>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">副标题颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.subTitleColor = '#999999'">重置</a>
                            <SelectColor v-model:color="module.subTitleColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="categoryRec"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="categoryA1"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
import { ref, computed } from "vue";
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import CommonSubTitleEdit from "@/views/decorate/decorate/src/modules/src/commonSubTitle/Edit.vue";
import CommonImageFillEdit from "@/views/decorate/decorate/src/modules/src/commonImageFill/Edit.vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import ContentStyleEdit from "@/views/decorate/decorate/src/modules/src/contentStyle/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { CategoryRecA1Module } from "./types";

const tabs = ref([
    { label: "内容设置", name: "content" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");

// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，模板 v-model 直接写该 reactive 对象
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as CategoryRecA1Module);
</script>
