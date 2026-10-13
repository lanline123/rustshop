<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>分类中心V2</h3>
            <div class="dec-edit-desc">可切换展示布局：双列网格 / 图文列表</div>
        </div>
        <div class="dec-edit-title-box">
            <div class="title">内容设置</div>
        </div>
        <!-- 展示布局 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">展示布局</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.styleType">
                    <el-radio-button :value="2">双列网格</el-radio-button>
                    <el-radio-button :value="3">图文列表</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- 二级分类标签风格 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">二级分类标签风格</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.subTabStyle">
                    <el-radio-button :value="1">实心高亮</el-radio-button>
                    <el-radio-button :value="2">描边高亮</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- 文本对齐 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">文本对齐</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.textAlign">
                    <el-radio-button :value="1">左对齐</el-radio-button>
                    <el-radio-button :value="2">居中对齐</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- 文字粗细 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">文字粗细</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.textWeight">
                    <el-radio-button :value="1">常规体</el-radio-button>
                    <el-radio-button :value="2">加粗体</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- 商品名称行数 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">商品名称行数</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.goodsNameRow">
                    <el-radio-button :value="1">1行</el-radio-button>
                    <el-radio-button :value="2">2行</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- 显示商品名称 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">显示商品名称</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.showName">
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">不显示</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- 购买按钮样式：标题在上 + 等宽三列网格（对齐商品模块排列） -->
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">购买按钮样式</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group buy-btn-style-group" v-model="module.buyBtnStyle">
                    <el-radio-button :value="0">不显示</el-radio-button>
                    <el-radio-button :value="1">样式一</el-radio-button>
                    <el-radio-button :value="2">样式二</el-radio-button>
                    <el-radio-button :value="3">样式三</el-radio-button>
                    <el-radio-button :value="4">样式四</el-radio-button>
                    <el-radio-button :value="5">样式五</el-radio-button>
                    <el-radio-button :value="6">样式六</el-radio-button>
                    <el-radio-button :value="7">样式七</el-radio-button>
                    <el-radio-button :value="8">样式八</el-radio-button>
                </el-radio-group>
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
import { cateCenterV2Defaults } from "./index";
import type { CateCenterV2Module } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as CateCenterV2Module);

/** 颜色项留空即跟随主题色 / 原默认色，故默认值统一为空串 */
const colorFields: { field: keyof CateCenterV2Module; label: string }[] = [
    { field: "themeColor", label: "主题色（价格、按钮、选中态）" },
    { field: "sideBgColor", label: "一级分类栏背景色" },
    { field: "sideTextColor", label: "一级分类文字色" },
    { field: "sideActiveTextColor", label: "一级分类选中文字色" },
    { field: "sideActiveBgColor", label: "一级分类选中背景色" },
    { field: "sideIndicatorColor", label: "一级分类选中指示条颜色" },
    { field: "subTabTextColor", label: "二级标签文字色" },
    { field: "subTabBgColor", label: "二级标签背景色" },
    { field: "subTabActiveTextColor", label: "二级标签选中文字色" },
    { field: "subTabActiveBgColor", label: "二级标签选中背景色" },
    { field: "goodsListBgColor", label: "商品区背景色" },
    { field: "productTitleColor", label: "商品标题色" },
    { field: "priceColor", label: "商品价格色" },
    { field: "buyBtnColor", label: "购买按钮色" }
];

const onColorChange = (field: keyof CateCenterV2Module, value: string) => {
    module.value[field] = value;
};

const onResetColor = (field: keyof CateCenterV2Module) => {
    onColorChange(field, ((cateCenterV2Defaults as Record<string, unknown>)[field as string] as string) ?? "");
};
</script>
<style lang="less" scoped>
/* 强制 3 列等宽，行与行边框贴合，避免默认 wrap 导致列线错位 */
.buy-btn-style-group {
    width: 100%;

    :deep(.el-radio-button) {
        flex: 0 0 33.333333%;
        max-width: 33.333333%;
        margin-bottom: -1px;
        box-sizing: border-box;
    }

    :deep(.el-radio-button__inner) {
        padding: 0 8px;
    }

    /* 覆盖全局 first/last 圆角，改为整块四角圆角 */
    :deep(.el-radio-button:first-child .el-radio-button__inner),
    :deep(.el-radio-button:last-of-type .el-radio-button__inner) {
        border-radius: 0;
    }

    :deep(.el-radio-button:nth-child(1) .el-radio-button__inner) {
        border-radius: 2px 0 0 0;
    }

    :deep(.el-radio-button:nth-child(3) .el-radio-button__inner) {
        border-radius: 0 2px 0 0;
    }

    :deep(.el-radio-button:nth-child(7) .el-radio-button__inner) {
        border-radius: 0 0 0 2px;
    }

    :deep(.el-radio-button:nth-child(9) .el-radio-button__inner) {
        border-radius: 0 0 2px 0;
    }
}
</style>
