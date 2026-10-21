<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>搜索结果</h3>
            <div class="dec-edit-desc">展示PC端搜索结果页面样式预览</div>
        </div>

        <div class="dec-edit-section-title">内容样式</div>

        <!-- 筛选布局：左侧筛选 / 顶部筛选 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">筛选布局</div>
                <div class="value">{{ filterStyleLabels[filterStyle] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="filterStyle">
                    <el-radio-button :value="1">左侧筛选</el-radio-button>
                    <el-radio-button :value="2">顶部筛选</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <!-- 选择模板：一行五个 / 一行六个 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">选择模板</div>
                <div class="value">{{ filterTemplateLabels[filterTemplate] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="filterTemplate">
                    <el-radio-button :value="1">一行五个</el-radio-button>
                    <el-radio-button :value="2">一行六个</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <!-- 商品样式：扁平 / 卡片 / 描边 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">商品样式</div>
                <div class="value">{{ goodsStyleLabels[goodsStyle] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="goodsStyle">
                    <el-radio-button :value="1">扁平</el-radio-button>
                    <el-radio-button :value="2">卡片</el-radio-button>
                    <el-radio-button :value="3">描边</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <!-- 边角样式：直角 / 圆角 / 大圆角 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">边角样式</div>
                <div class="value">{{ goodsRadiusLabels[goodsRadiusStyle] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="goodsRadiusStyle">
                    <el-radio-button :value="1">直角</el-radio-button>
                    <el-radio-button :value="2">圆角</el-radio-button>
                    <el-radio-button :value="3">大圆角</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <!-- 购买样式：样式一 / 样式二 / 样式三 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">购买样式</div>
                <div class="value">{{ buyStyleLabels[buyStyle] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="buyStyle">
                    <el-radio-button :value="1">样式一</el-radio-button>
                    <el-radio-button :value="2">样式二</el-radio-button>
                    <el-radio-button :value="3">样式三</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-divider-line"></div>
        <div class="dec-edit-section-title">颜色设置</div>

        <!-- 背景颜色 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">背景颜色</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="resetBackgroundColor">重置</a>
                        <SelectColor v-model:color="backgroundColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>

        <div class="dec-divider-line"></div>
        <div class="dec-edit-section-title">组件设置</div>

        <!-- 上边距 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">上边距</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="componentMarginTop">
                    <el-radio-button :value="8">小</el-radio-button>
                    <el-radio-button :value="16">中</el-radio-button>
                    <el-radio-button :value="24">大</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <!-- 下边距 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">下边距</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="componentMarginBottom">
                    <el-radio-button :value="8">小</el-radio-button>
                    <el-radio-button :value="16">中</el-radio-button>
                    <el-radio-button :value="24">大</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-divider-line"></div>

        <!-- 商品店铺 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">商品店铺</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="showShopTagValue">
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">隐藏</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <!-- 已售数量 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">已售数量</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="showSalesValue">
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">隐藏</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <!-- 显示添加购物车 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">显示添加购物车</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="showCartButtonValue">
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">隐藏</el-radio-button>
                </el-radio-group>
            </div>
        </div>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
import { computed, onMounted } from "vue";
import { SelectColor } from "@/components/select";
import type { PcSearchResultModule } from "./types";
import { pcSearchResultDefaults } from "./types";

const module = defineModel<PcSearchResultModule>("module", { default: () => ({}) });

// 首次挂载补齐默认值，避免旧数据缺字段
onMounted(() => {
    const m = module.value;
    if (m.backgroundColor === undefined) m.backgroundColor = pcSearchResultDefaults.backgroundColor;
    if (m.filterStyle === undefined) m.filterStyle = pcSearchResultDefaults.filterStyle;
    if (m.filterTemplate === undefined) m.filterTemplate = pcSearchResultDefaults.filterTemplate;
    if (m.showFilterPanel === undefined) m.showFilterPanel = pcSearchResultDefaults.showFilterPanel;
    if (m.showPriceRange === undefined) m.showPriceRange = pcSearchResultDefaults.showPriceRange;
    if (m.showShopTag === undefined) m.showShopTag = pcSearchResultDefaults.showShopTag;
    if (m.showSales === undefined) m.showSales = pcSearchResultDefaults.showSales;
    if (m.showCartButton === undefined) m.showCartButton = pcSearchResultDefaults.showCartButton;
    if (m.goodsStyle === undefined) m.goodsStyle = pcSearchResultDefaults.goodsStyle;
    if (m.goodsRadiusStyle === undefined) m.goodsRadiusStyle = pcSearchResultDefaults.goodsRadiusStyle;
    if (m.buyStyle === undefined) m.buyStyle = pcSearchResultDefaults.buyStyle;
    if (m.componentMarginTop === undefined) m.componentMarginTop = pcSearchResultDefaults.componentMarginTop;
    if (m.componentMarginBottom === undefined) m.componentMarginBottom = pcSearchResultDefaults.componentMarginBottom;
});

// 双向映射：boolean 字段与"显示/隐藏"单选按钮
const showShopTagValue = computed({
    get: () => (module.value.showShopTag !== false ? 1 : 0),
    set: (v: number) => {
        module.value.showShopTag = Number(v) === 1;
    }
});
const showSalesValue = computed({
    get: () => (module.value.showSales !== false ? 1 : 0),
    set: (v: number) => {
        module.value.showSales = Number(v) === 1;
    }
});
const showCartButtonValue = computed({
    get: () => (module.value.showCartButton !== false ? 1 : 0),
    set: (v: number) => {
        module.value.showCartButton = Number(v) === 1;
    }
});

// 单选按钮直接绑定 module 上的字段（用 computed 稳定类型）
const filterStyle = computed({
    get: () => (module.value.filterStyle ?? pcSearchResultDefaults.filterStyle) as 1 | 2,
    set: (v) => (module.value.filterStyle = v)
});
const filterTemplate = computed({
    get: () => (module.value.filterTemplate ?? pcSearchResultDefaults.filterTemplate) as 1 | 2,
    set: (v) => (module.value.filterTemplate = v)
});
const goodsStyle = computed({
    get: () => module.value.goodsStyle ?? pcSearchResultDefaults.goodsStyle ?? 1,
    set: (v: number) => (module.value.goodsStyle = v)
});
const goodsRadiusStyle = computed({
    get: () => module.value.goodsRadiusStyle ?? pcSearchResultDefaults.goodsRadiusStyle ?? 1,
    set: (v: number) => (module.value.goodsRadiusStyle = v)
});
const buyStyle = computed({
    get: () => module.value.buyStyle ?? pcSearchResultDefaults.buyStyle ?? 1,
    set: (v: number) => (module.value.buyStyle = v)
});
const componentMarginTop = computed({
    get: () => module.value.componentMarginTop ?? pcSearchResultDefaults.componentMarginTop ?? 8,
    set: (v: number) => (module.value.componentMarginTop = v)
});
const componentMarginBottom = computed({
    get: () => module.value.componentMarginBottom ?? pcSearchResultDefaults.componentMarginBottom ?? 8,
    set: (v: number) => (module.value.componentMarginBottom = v)
});
const backgroundColor = computed({
    get: () => module.value.backgroundColor ?? pcSearchResultDefaults.backgroundColor ?? "#fff",
    set: (v: string) => (module.value.backgroundColor = v)
});

// 文案映射
const filterStyleLabels: Record<number, string> = { 1: "左侧筛选", 2: "顶部筛选" };
const filterTemplateLabels: Record<number, string> = { 1: "一行五个", 2: "一行六个" };
const goodsStyleLabels: Record<number, string> = { 1: "扁平", 2: "卡片", 3: "描边" };
const goodsRadiusLabels: Record<number, string> = { 1: "直角", 2: "圆角", 3: "大圆角" };
const buyStyleLabels: Record<number, string> = { 1: "样式一", 2: "样式二", 3: "样式三" };

const resetBackgroundColor = () => {
    module.value.backgroundColor = pcSearchResultDefaults.backgroundColor;
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
