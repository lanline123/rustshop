<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('分类导航3') }}</h3>
            <div class="dec-edit-desc">{{ $t('选择图片和链接，可编辑文字，方便轻松设置') }}</div>
        </div>
        <div v-if="activeName === 'category'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('内容设置') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <PicList :isMultiple="true" v-model:photos="module.picList" :title="$t('添加一组')" decorateType="mobile" :showDesc="true"></PicList>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('参数设置') }}</div>
            </div>
        
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片背景色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group" need-refresh="true">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.imageBackgroundColor = defaultFrame.backgroundColor">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.imageBackgroundColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题内容') }}</div>
                </div>
                <div class="dec-edit-group-con">
                <div class="dec-input-group">
                    <el-input v-model="module.titleText" :placeholder="$t('请输入标题内容')"></el-input>
                </div>
              </div>
            </div>


            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('显示查看更多') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showMore">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showMore == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('查看更多文字') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                    <div class="dec-input-group">
                        <el-input v-model="module.showMoreText" :placeholder="$t('请输入查看更多文字')"></el-input>
                    </div>
                </div>       
            </div>
            <div class="dec-edit-group" v-if="module.showMore == 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('更多链接') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-link-group">
                        <div class="lyecs-link-select">
                            <SelectLink v-model="module.moreLink" decorateType="pc"></SelectLink>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import { ref } from "vue";
import CommonSubTitleEdit from "../../src/commonSubTitle/Edit.vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import { ModuleType, ModuleCategoryNav3Type } from "@/types/decorate/decorate.d";
import { SelectLink } from '@/components/select';
import { defaultFrame } from "@/views/decorate/decorate/src/modules/";

const activeName = ref("category");
const module = defineModel<ModuleType & ModuleCategoryNav3Type>("module", { default: () => ({}) });
</script>
