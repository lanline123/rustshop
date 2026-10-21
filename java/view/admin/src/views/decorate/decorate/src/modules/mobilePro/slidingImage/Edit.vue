<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('小图滑动') }}</h3>
            <div class="dec-edit-desc">{{ $t('图片可横向轮播显示, 尺寸不限, 可设置背景图片') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('内容设置') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <PicList :isMultiple="true" v-model:photos="module.picList" :title="$t('添加图片+链接')" showDesc showColor decorateType="mobile"></PicList>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('图片样式设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('背景渐变方向') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.backgroundGradientType">
                        <el-tooltip effect="light" placement="bottom" :content="$t('上下')" :show-after="100">
                            <el-radio-button :value="'upDown'">{{ $t('上下') }}</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('左右')" :show-after="100">
                            <el-radio-button :value="'leftRight'">{{ $t('左右') }}</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('斜向')" :show-after="100">
                            <el-radio-button :value="'diagonal'">{{ $t('斜向') }}</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('单色')" :show-after="100">
                            <el-radio-button :value="'purity'">{{ $t('单色') }}</el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片宽度') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picWidth" show-input :show-input-controls="false" size="small" input-size="default" :max="50" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片间距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picMargin" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片内距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="10" />
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
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('缩小比例') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picReducePercent" show-input :show-input-controls="false" size="small" input-size="default" :max="1.00" :step="0.01" />
                    </div>
                </div>
            </div>
            <CommonSubTitleEdit v-model="module.subTitle" :title="$t('标题')"></CommonSubTitleEdit>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label"> {{ $t('描述文字') }} </div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-radio-group">
                        <el-radio-group class="dec-radio-group" v-model="module.showDesc">
                            <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                            <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showDesc == 1">
                <div class="dec-edit-group-title">
                    <div class="label"> {{ $t('描述文字颜色') }} </div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.descColor = '#ffffff'">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.descColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle"></ModuleStyleEdit>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('轮播扩展设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label"> {{ $t('自动轮播') }} </div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-radio-group">
                        <el-radio-group class="dec-radio-group" v-model="module.autoPlay">
                            <el-radio-button :value="1">{{ $t('开启') }}</el-radio-button>
                            <el-radio-button :value="0">{{ $t('关闭') }}</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label"> {{ $t('轮播时间间隔') }} </div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.interval" show-input :show-input-controls="false" size="small" input-size="default" :max="10" />
                    </div>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref, reactive } from "vue";
import CommonSubTitleEdit from "../../src/commonSubTitle/Edit.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import { ModuleType, ModuleSlidingImageType } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([
    { label: t('内容设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");
const module = defineModel<ModuleType & ModuleSlidingImageType>("module", { default: () => ({}) });
</script>
