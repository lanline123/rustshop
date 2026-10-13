<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>分类中心V1</h3>
            <div class="dec-edit-desc">展示分组分类导航，左侧菜单栏 + 右侧内容区布局</div>
        </div>
        <div class="dec-edit-title-box">
            <div class="title">内容设置</div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">搜索框占位文字</div>
            </div>
            <div class="dec-edit-group-con">
                <el-input v-model="module.searchPlaceholder" placeholder="请输入搜索框占位文字" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">展示分类名称</div>
            </div>
            <div class="dec-edit-group-con">
                <el-switch v-model="module.showCategoryName" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">显示"推荐"入口</div>
            </div>
            <div class="dec-edit-group-con">
                <el-switch v-model="module.showRecommend" />
            </div>
        </div>
        <div v-if="module.showRecommend !== false" class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">"推荐"文案</div>
            </div>
            <div class="dec-edit-group-con">
                <el-input v-model="module.recommendText" type="textarea" :rows="2" placeholder="请输入推荐入口文案" />
            </div>
        </div>
        <div class="dec-edit-title-box">
            <div class="title">颜色设置</div>
        </div>
        <div class="dec-edit-group" v-for="item in colorFields" :key="item.field">
            <div class="dec-edit-group-title">
                <div class="label">{{ item.label }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="onResetColor(item.field)">重置</a>
                        <SelectColor :color="(module[item.field] as string) || ''" @update:color="(v: string) => onColorChange(item.field, v)"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { SelectColor } from "@/components/select";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import { cateCenterV1Defaults } from "./index";
import type { CateCenterV1Module } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as CateCenterV1Module);

/** 颜色项留空即跟随主题色 / 原默认色，故默认值统一为空串 */
const colorFields: { field: keyof CateCenterV1Module; label: string }[] = [
    { field: "sideBgColor", label: "一级分类栏背景色" },
    { field: "sideTextColor", label: "一级分类文字色" },
    { field: "sideActiveTextColor", label: "一级分类选中文字色" },
    { field: "sideActiveBgColor", label: "一级分类选中背景色" },
    { field: "groupTitleColor", label: "分组标题色" },
    { field: "categoryNameColor", label: "分类名称色" }
];

const onColorChange = (field: keyof CateCenterV1Module, value: string) => {
    module.value[field] = value;
};

const onResetColor = (field: keyof CateCenterV1Module) => {
    onColorChange(field, ((cateCenterV1Defaults as Record<string, unknown>)[field as string] as string) ?? "");
};
</script>
