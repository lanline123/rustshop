<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>分类导航3</h3>
            <div class="dec-edit-desc">选择图片和链接，可编辑文字，方便轻松设置</div>
        </div>
        <div v-if="activeName === 'category'">
            <div class="dec-edit-title-box">
                <div class="title">内容设置</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <PicList :isMultiple="true" v-model:photos="module.picList" title="添加一组" decorateType="mobile" :showDesc="true"></PicList>
                </div>
            </div>
            <div class="dec-edit-title-box">
                <div class="title">参数设置</div>
            </div>
        
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片背景色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group" need-refresh="true">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.imageBackgroundColor = defaultFrame.backgroundColor">重置</a>
                            <SelectColor v-model:color="module.imageBackgroundColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">标题内容</div>
                </div>
                <div class="dec-edit-group-con">
                <div class="dec-input-group">
                    <el-input v-model="module.titleText" placeholder="请输入标题内容"></el-input>
                </div>
              </div>
            </div>


            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">显示查看更多</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showMore">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showMore == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">查看更多文字</div>
                    </div>
                    <div class="dec-edit-group-con">
                    <div class="dec-input-group">
                        <el-input v-model="module.showMoreText" placeholder="请输入查看更多文字"></el-input>
                    </div>
                </div>       
            </div>
            <div class="dec-edit-group" v-if="module.showMore == 1">
                <div class="dec-edit-group-title">
                    <div class="label">更多链接</div>
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
import { computed, ref } from "vue";
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import { SelectLink } from "@/components/select";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcCategoryNav3Module } from "./types";

const activeName = ref("category");
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcCategoryNav3Module);
</script>
