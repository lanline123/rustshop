<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>Banner设置</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">Banner布局</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.bannerStyle">
                    <el-radio-button :value="1">默认风格</el-radio-button>
                    <el-radio-button :value="2">简约风格</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">添加Banner轮播大图</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">提示：您可以通过拖拽进行图片排序，建议尺寸：1180*940像素（高清）或750*479像素（低分辨率）</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList"></PicList>
            </div>
        </div>
        <template v-if="module.bannerStyle == 1">
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="title">添加Banner右侧轮播小图</div>
                </div>
                <div class="dec-edit-group-desc">
                    <div class="pic-change-desc">建议尺寸：380*300像素，三个一组，请最少添加3-6个</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicList :isMultiple="true" v-model:photos="module.picList2"></PicList>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="title">添加右侧会员栏图标</div>
                </div>
                <div class="dec-edit-group-desc">
                    <div class="pic-change-desc">建议尺寸：60*60像素，最多显示6个</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicList :isMultiple="true" v-model:photos="module.picList3"></PicList>
                </div>
            </div>
        </template>
        <template v-if="module.bannerStyle == 2">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">是否显示分类栏</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-radio-group">
                        <el-radio-group class="dec-radio-group" v-model="module.showCat">
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </div>
        </template>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { PicList } from "@/components/decorate";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcBannerModule } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcBannerModule);
</script>
