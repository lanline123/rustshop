<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('分类中心V1') }}</h3>
            <div class="dec-edit-desc">{{ $t('展示分组分类导航，左侧菜单栏 + 右侧内容区布局') }}</div>
        </div>
        <div class="dec-spread-title">
            <div class="title">{{ $t('内容设置') }}</div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('搜索框占位文字') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-input v-model="module.searchPlaceholder" :placeholder="$t('请输入搜索框占位文字')" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('展示分类名称') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-switch v-model="module.showCategoryName" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('显示"推荐"入口') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-switch v-model="module.showRecommend" />
            </div>
        </div>
        <div v-if="module.showRecommend !== false" class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('"推荐"文案') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-input v-model="module.recommendText" type="textarea" :rows="2" :placeholder="$t('请输入推荐入口文案')" />
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ModuleType } from "@/types/decorate/decorate.d";
const module = defineModel<ModuleType>("module", { default: () => ({}) });
</script>
