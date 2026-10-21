<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('分类中心V2') }}</h3>
            <div class="dec-edit-desc">{{ $t('可切换展示布局：双列网格 / 图文列表') }}</div>
        </div>
        <div class="dec-spread-title">
            <div class="title">{{ $t('内容设置') }}</div>
        </div>
        <!-- 展示布局 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('展示布局') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.styleType">
                    <el-radio-button :value="2">{{ $t('双列网格') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('图文列表') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- 二级分类标签风格 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('二级分类标签风格') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.subTabStyle">
                    <el-radio-button :value="1">{{ $t('实心高亮') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('描边高亮') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- 文本对齐 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('文本对齐') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.textAlign">
                    <el-radio-button :value="1">{{ $t('左对齐') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('居中对齐') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- 文字粗细 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('文字粗细') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.textWeight">
                    <el-radio-button :value="1">{{ $t('常规体') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('加粗体') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- 商品名称行数 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('商品名称行数') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.goodsNameRow">
                    <el-radio-button :value="1">{{ $t('1行') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('2行') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- 显示商品名称 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('显示商品名称') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group v-model="module.showName">
                    <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <!-- 购买按钮样式：标题在上 + 等宽三列网格（对齐商品模块排列） -->
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('购买按钮样式') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group buy-btn-style-group" v-model="module.buyBtnStyle">
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    <el-radio-button :value="1">{{ $t('样式一') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('样式二') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('样式三') }}</el-radio-button>
                    <el-radio-button :value="4">{{ $t('样式四') }}</el-radio-button>
                    <el-radio-button :value="5">{{ $t('样式五') }}</el-radio-button>
                    <el-radio-button :value="6">{{ $t('样式六') }}</el-radio-button>
                    <el-radio-button :value="7">{{ $t('样式七') }}</el-radio-button>
                    <el-radio-button :value="8">{{ $t('样式八') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ModuleType } from "@/types/decorate/decorate.d";
const module = defineModel<ModuleType>("module", { default: () => ({}) });
</script>
<style lang="less" scoped>
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
