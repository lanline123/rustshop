<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>大图滑动</h3>
            <div class="dec-edit-desc">图片可自动横向循环滚动显示，支持手指滑动，可设置背景图片</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-title-box">
                <div class="title">内容设置</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <PicList :isMultiple="true" v-model:photos="module.picList" title="添加图片+链接" showDesc decorateType="mobile"></PicList>
                </div>
            </div>
            <div class="dec-edit-title-box">
                <div class="title">参数设置</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">每行显示数量</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.rowNum">
                        <el-radio-button :value="1">1个</el-radio-button>
                        <el-radio-button :value="1.2">1.2个</el-radio-button>
                        <el-radio-button :value="1.5">1.5个</el-radio-button>
                        <el-radio-button :value="2">2个</el-radio-button>
                        <el-radio-button :value="2.2">2.2个</el-radio-button>
                        <el-radio-button :value="2.5">2.5个</el-radio-button>
                        <el-radio-button :value="3">3个</el-radio-button>
                        <el-radio-button :value="3.2">3.2个</el-radio-button>
                        <el-radio-button :value="3.5">3.5个</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">滚动效果</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.slidingEffect">
                        <el-tooltip effect="light" placement="bottom" content="自动滚动" :show-after="100">
                            <el-radio-button :value="'auto'">自动滚动</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="手动滚动" :show-after="100">
                            <el-radio-button :value="'manual'">手动滚动</el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">滚动速度</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.slidingSpeed" show-input :show-input-controls="false" size="small" input-size="default" :max="60" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片上距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picMarginTop" show-input :show-input-controls="false" size="small" input-size="default" :max="80" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片间距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picMargin" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <CommonImageFillEdit v-model="module.picFillType"></CommonImageFillEdit>
            <TitleDescEdit v-model="module.picTitleDesc"></TitleDescEdit>
        </div>
        <div v-if="activeName === 'title'">
            <SlidingLargeImageTitle v-model="module.moduleTitle"></SlidingLargeImageTitle>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="slideLargeImage"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { PicList } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref, computed } from "vue";
import TitleDescEdit from "@/views/decorate/decorate/src/modules/mobilePro/slideLargeImage/src/TitleDescEdit.vue";
import SlidingLargeImageTitle from "@/views/decorate/decorate/src/modules/mobilePro/slideLargeImage/src/SlidingLargeImageTitle.vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import ContentStyleEdit from "@/views/decorate/decorate/src/modules/src/contentStyle/Edit.vue";
import CommonImageFillEdit from "@/views/decorate/decorate/src/modules/src/commonImageFill/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { SlideLargeImageModule } from "./types";
const tabs = ref<any[]>([
    { label: "内容设置", name: "content" },
    { label: "标题设置", name: "title" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");
// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，
// 模板内 v-model 直接写该 reactive 对象（与旧 defineModel 等价，公共子组件零改造复用）
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as SlideLargeImageModule);
</script>
