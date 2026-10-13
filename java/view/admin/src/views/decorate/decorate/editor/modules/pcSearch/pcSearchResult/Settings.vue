<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t("搜索结果") }}</h3>
            <div class="dec-edit-desc">{{ $t("展示PC端搜索结果页面样式预览") }}</div>
        </div>

        <div class="dec-edit-section-title">{{ $t("内容样式") }}</div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("筛选布局") }}</div>
                <div class="value">{{ filterStyleLabels[filterStyleModelValue] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="filterStyleModelValue"
                    @update:model-value="(v: string | number) => updateField('filterStyle', Number(v))"
                >
                    <el-radio-button :value="1">{{ $t("左侧筛选") }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t("顶部筛选") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("选择模板") }}</div>
                <div class="value">{{ filterTemplateLabels[filterTemplateModelValue] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" :model-value="filterTemplateModelValue" @update:model-value="handleFilterTemplateChange">
                    <el-radio-button :value="1">{{ $t("一行五个") }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t("一行六个") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("商品样式") }}</div>
                <div class="value">{{ goodsStyleLabels[localData.goodsStyle ?? pcSearchResultDefaults.goodsStyle ?? 1] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.goodsStyle ?? pcSearchResultDefaults.goodsStyle"
                    @update:model-value="(v: string | number) => updateField('goodsStyle', Number(v))"
                >
                    <el-radio-button :value="1">{{ $t("扁平") }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t("卡片") }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t("描边") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("边角样式") }}</div>
                <div class="value">{{ goodsRadiusLabels[localData.goodsRadiusStyle ?? pcSearchResultDefaults.goodsRadiusStyle ?? 1] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.goodsRadiusStyle ?? pcSearchResultDefaults.goodsRadiusStyle"
                    @update:model-value="(v: string | number) => updateField('goodsRadiusStyle', Number(v))"
                >
                    <el-radio-button :value="1">{{ $t("直角") }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t("圆角") }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t("大圆角") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("购买样式") }}</div>
                <div class="value">{{ buyStyleLabels[localData.buyStyle ?? pcSearchResultDefaults.buyStyle ?? 1] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.buyStyle ?? pcSearchResultDefaults.buyStyle"
                    @update:model-value="(v: string | number) => updateField('buyStyle', Number(v))"
                >
                    <el-radio-button :value="1">{{ $t("样式一") }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t("样式二") }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t("样式三") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-divider-line"></div>
        <div class="dec-edit-section-title">{{ $t("颜色设置") }}</div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("背景颜色") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="resetBackgroundColor">{{ $t("重置") }}</a>
                        <SelectColor
                            :color="localData.backgroundColor || '#fff'"
                            @update:color="(v: string) => updateField('backgroundColor', v)"
                        />
                    </div>
                </div>
            </div>
        </div>

        <div class="dec-edit-section-title">{{ $t("组件设置") }}</div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("上边距") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.componentMarginTop ?? pcSearchResultDefaults.componentMarginTop"
                    @update:model-value="(v: string | number) => updateField('componentMarginTop', Number(v))"
                >
                    <el-radio-button :value="8">{{ $t("小") }}</el-radio-button>
                    <el-radio-button :value="16">{{ $t("中") }}</el-radio-button>
                    <el-radio-button :value="24">{{ $t("大") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("下边距") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.componentMarginBottom ?? pcSearchResultDefaults.componentMarginBottom"
                    @update:model-value="(v: string | number) => updateField('componentMarginBottom', Number(v))"
                >
                    <el-radio-button :value="8">{{ $t("小") }}</el-radio-button>
                    <el-radio-button :value="16">{{ $t("中") }}</el-radio-button>
                    <el-radio-button :value="24">{{ $t("大") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-divider-line"></div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("商品店铺") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.showShopTag !== false ? 1 : 0"
                    @update:model-value="(v: string | number) => updateField('showShopTag', Number(v) === 1)"
                >
                    <el-radio-button :value="1">{{ $t("显示") }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t("隐藏") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("已售数量") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.showSales !== false ? 1 : 0"
                    @update:model-value="(v: string | number) => updateField('showSales', Number(v) === 1)"
                >
                    <el-radio-button :value="1">{{ $t("显示") }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t("隐藏") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("显示添加购物车") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.showCartButton !== false ? 1 : 0"
                    @update:model-value="(v: string | number) => updateField('showCartButton', Number(v) === 1)"
                >
                    <el-radio-button :value="1">{{ $t("显示") }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t("隐藏") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { SelectColor } from "@/components/select";
import { useModuleSettings } from "../../../composables/useModuleSettings";
import type { PcSearchResultModule } from "./types";
import { pcSearchResultDefaults } from "./types";

const { t } = useI18n();
const { localData, updateField, store } = useModuleSettings<PcSearchResultModule>(pcSearchResultDefaults);

const filterStyleLabels = computed<Record<number, string>>(() => ({ 1: t("左侧筛选"), 2: t("顶部筛选") }));
const filterTemplateLabels = computed<Record<number, string>>(() => ({ 1: t("一行五个"), 2: t("一行六个") }));
const goodsStyleLabels = computed<Record<number, string>>(() => ({ 1: t("扁平"), 2: t("卡片"), 3: t("描边") }));
const goodsRadiusLabels = computed<Record<number, string>>(() => ({ 1: t("直角"), 2: t("圆角"), 3: t("大圆角") }));
const buyStyleLabels = computed<Record<number, string>>(() => ({ 1: t("样式一"), 2: t("样式二"), 3: t("样式三") }));

const filterStyleModelValue = computed(() => {
    if (localData.value.filterStyle) return localData.value.filterStyle;
    return pcSearchResultDefaults.filterStyle ?? 2;
});

const filterTemplateModelValue = computed(() => {
    if (localData.value.filterTemplate) return localData.value.filterTemplate;
    return pcSearchResultDefaults.filterTemplate ?? 2;
});

const handleFilterTemplateChange = (v: string | number) => {
    const value = Number(v) as 1 | 2;
    updateField("filterTemplate", value);
    store.patchActiveModuleData({ filterTemplate: value });
};

const resetBackgroundColor = () => {
    updateField("backgroundColor", pcSearchResultDefaults.backgroundColor);
};
</script>

<style lang="scss" scoped>
.dec-edit-section-title {
    font-size: 14px;
    color: #323233;
    font-weight: 600;
    padding: 6px 16px;
}

.dec-edit-desc {
    color: #999;
    font-size: 12px;
    padding: 4px 16px 8px;
}
</style>
