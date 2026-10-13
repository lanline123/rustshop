<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('图片橱窗') }}</h3>
            <div class="dec-edit-desc">{{ $t('可以快速的添加图片广告，推广营销常用模块') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>

        <!-- 图片设置 Tab -->
        <div v-if="activeName === 'picture'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('基本设置') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <PicList :isMultiple="true" v-model:photos="module.picList" :showText="false" decorateType="mobile"></PicList>
                </div>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('参数设置') }}</div>
            </div>

            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('列表样式') }}</div>
                    <div class="value">{{ selectLabel.listStyle[module.listStyle] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.listStyle">
                        <el-radio-button value="fixed">{{ $t('固定') }}</el-radio-button>
                        <el-radio-button value="scroll">{{ $t('滑动') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('排列布局') }}</div>
                    <div class="value">{{ $t('每行{rowNum}个', { rowNum: module.rowNum }) }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.rowNum">
                        <el-radio-button v-for="n in 6" :key="n" :value="n">{{ $t('每行{n}个', { n }) }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片比例') }}</div>
                    <div class="value">{{ selectLabel.picRatio[module.picRatio] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.picRatio">
                        <el-radio-button value="default">{{ $t('默认') }}</el-radio-button>
                        <el-radio-button value="3:2">3:2</el-radio-button>
                        <el-radio-button value="1:1">1:1</el-radio-button>
                        <el-radio-button value="3:4">3:4</el-radio-button>
                        <el-radio-button value="2:1">2:1</el-radio-button>
                        <el-radio-button value="16:9">16:9</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-edit-group dec-edit-group-block" v-if="module.picRatio !== 'default'">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片模式') }}</div>
                    <div class="value">{{ selectLabel.picFillType[module.picFillType] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.picFillType">
                        <el-tooltip effect="light" placement="bottom" :content="$t('充满')" :show-after="100">
                            <el-radio-button value="cover"><i class="iconfont-admin icon-tianchong"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('周边留白')" :show-after="100">
                            <el-radio-button value="contain"><i class="iconfont-admin icon-liubai"></i></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片圆角') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.picRadius"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="30"
                        />
                    </div>
                </div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片间距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.imgPadding"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="30"
                        />
                    </div>
                </div>
            </div>

            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('模块投影') }}</div>
                    <div class="value">{{ selectLabel.boxShadow[module.boxShadow] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.boxShadow">
                        <el-radio-button :value="0">{{ $t('无') }}</el-radio-button>
                        <el-radio-button :value="1">{{ $t('常规') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
        </div>

        <!-- 扩展设置 Tab（对齐图文介绍模块） -->
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="customProduct"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="imageTextComponent"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { PicList } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import { ModuleType, ImageShowcaseType } from "@/types/decorate/decorate.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([
    { label: t('图片设置'), name: "picture" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("picture");

const module = defineModel<ModuleType & ImageShowcaseType>("module", { default: () => ({}) });

const selectLabel = ref<Record<string, Record<string, string>>>({
    listStyle: {
        fixed: t('固定'),
        scroll: t('滑动')
    },
    picRatio: {
        default: t('默认比例'),
        "3:2": "3:2",
        "1:1": "1:1",
        "3:4": "3:4",
        "2:1": "2:1",
        "16:9": "16:9"
    },
    picFillType: {
        cover: t('充满'),
        contain: t('周边留白')
    },
    boxShadow: {
        0: t('无'),
        1: t('常规')
    }
});
</script>
