<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('大图滑动') }}</h3>
            <div class="dec-edit-desc">{{ $t('图片可自动横向循环滚动显示，支持手指滑动，可设置背景图片') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('内容设置') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <PicList :isMultiple="true" v-model:photos="module.picList" :title="$t('添加图片+链接')" showDesc decorateType="mobile"></PicList>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('参数设置') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('每行显示数量') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.rowNum">
                        <el-radio-button :value="1">{{ $t('1个') }}</el-radio-button>
                        <el-radio-button :value="1.2">{{ $t('1.2个') }}</el-radio-button>
                        <el-radio-button :value="1.5">{{ $t('1.5个') }}</el-radio-button>
                        <el-radio-button :value="2">{{ $t('2个') }}</el-radio-button>
                        <el-radio-button :value="2.2">{{ $t('2.2个') }}</el-radio-button>
                        <el-radio-button :value="2.5">{{ $t('2.5个') }}</el-radio-button>
                        <el-radio-button :value="3">{{ $t('3个') }}</el-radio-button>
                        <el-radio-button :value="3.2">{{ $t('3.2个') }}</el-radio-button>
                        <el-radio-button :value="3.5">{{ $t('3.5个') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('滚动效果') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.slidingEffect">
                        <el-tooltip effect="light" placement="bottom" :content="$t('自动滚动')" :show-after="100">
                            <el-radio-button :value="'auto'">{{ $t('自动滚动') }}</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('手动滚动')" :show-after="100">
                            <el-radio-button :value="'manual'">{{ $t('手动滚动') }}</el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('滚动速度') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.slidingSpeed" show-input :show-input-controls="false" size="small" input-size="default" :max="60" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片上距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picMarginTop" show-input :show-input-controls="false" size="small" input-size="default" :max="80" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片间距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picMargin" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片圆角') }}</div>
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
import { ref } from "vue";
import TitleDescEdit from "./src/TitleDescEdit.vue";
import SlidingLargeImageTitle from "./src/SlidingLargeImageTitle.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import CommonImageFillEdit from "../../src/commonImageFill/Edit.vue";
import { ModuleType, ModuleSlidingLargeImageType } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([
    { label: t('内容设置'), name: "content" },
    { label: t('标题设置'), name: "title" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");
const module = defineModel<ModuleType & ModuleSlidingLargeImageType>("module", { default: () => ({}) });
</script>
