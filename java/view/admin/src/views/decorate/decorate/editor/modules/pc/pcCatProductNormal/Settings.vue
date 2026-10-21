<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>
                分类商品（窄）设置
            </h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">选择分类</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-goods-group">
                    <SelectCategory v-model:categoryId="module.categoryId"></SelectCategory>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">标题名称</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                    <el-input v-model="module.title" placeholder="请输入标题名称"></el-input>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">标题短名称</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                    <el-input v-model="module.shortTitle" placeholder="请输入短名称"></el-input>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">标题英文简称</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                    <el-input v-model="module.enTitle" placeholder="请输入英文简称"></el-input>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">模块主颜色</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.color = ''">重置</a>
                        <SelectColor v-model:color="module.color"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">模块副颜色(渐变)</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.color2 = ''">重置</a>
                        <SelectColor v-model:color="module.color2"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">左侧轮播图</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">建议尺寸：492*764像素，推荐2张图以上</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList" :showDesc="true"></PicList>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">右侧四小图</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">建议尺寸：492*764像素，超过4张不显示</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList2" :showDesc="true"></PicList>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { PicList } from "@/components/decorate";
import { SelectCategory, SelectColor } from "@/components/select";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcCatProductNormalModule } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcCatProductNormalModule);
</script>
