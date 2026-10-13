<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('图文展示') }}</h3>
            <div class="dec-edit-desc">{{ $t('品牌展示，可快捷输入编辑，适合公告/品牌展示/店铺笔记等。') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('海报设置：宽度不限') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <PicList v-model:photo="module.picUrl" :showText="false" decorateType="mobile"></PicList>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题文字') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-input-group">
                        <el-input v-model="module.title" :placeholder="$t('请输入标题文字信息')"></el-input>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('介绍文字') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-input-group">
                        <el-input v-model="module.description" :rows="4" type="textarea" :placeholder="$t('请输入介绍文字信息')" />
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('参数设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片边框') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picBorder" show-input :show-input-controls="false" size="small" input-size="default" :max="10" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.picBorder > 0">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片边框颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.picBorderColor = '#eeeeee'">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.picBorderColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片圆角半径') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题对齐方式') }}</div>
                    <div class="value">{{ selectLabel.textAlignment[module.titTextAlignment as TextAlignmentType] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.titTextAlignment">
                        <el-tooltip effect="light" placement="bottom" :content="$t('左对齐')" :show-after="100">
                            <el-radio-button :value="'left'"><i class="iconfont-admin icon-alignLeft"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('居中对齐')" :show-after="100">
                            <el-radio-button :value="'center'"><i class="iconfont-admin icon-center"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('右对齐')" :show-after="100">
                            <el-radio-button :value="'right'"><i class="iconfont-admin icon-alignRight"></i></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题介绍间距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.titMarginBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="50" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题文字颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.titColor = '#333333'">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.titColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('介绍对齐方式') }}</div>
                    <div class="value">{{ selectLabel.textAlignment[module.descTextAlignment as TextAlignmentType] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.descTextAlignment">
                        <el-tooltip effect="light" placement="bottom" :content="$t('左对齐')" :show-after="100">
                            <el-radio-button :value="'left'"><i class="iconfont-admin icon-alignLeft"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('居中对齐')" :show-after="100">
                            <el-radio-button :value="'center'"><i class="iconfont-admin icon-center"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('右对齐')" :show-after="100">
                            <el-radio-button :value="'right'"><i class="iconfont-admin icon-alignRight"></i></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('介绍文字颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.descColor = '#333333'">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.descColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="customProduct"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="imageTextComponent"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref, watch } from "vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import { selectLabel } from "@/views/decorate/decorate/src/modules/editIndex";
import { ModuleType, ModuleImageTextComponentType, TextAlignmentType } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([
    { label: t('内容设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");
const module = defineModel<ModuleType & ModuleImageTextComponentType>("module", { default: () => ({}) });
</script>
