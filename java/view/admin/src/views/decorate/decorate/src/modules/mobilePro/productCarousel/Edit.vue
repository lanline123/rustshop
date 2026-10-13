<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('商品轮播') }}</h3>
            <div class="dec-edit-desc">{{ $t('真·轮播经典商品卡：中间大卡两侧露出；选择商品后自动加载商品信息，可拖拽排序') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>

        <div v-if="activeName === 'content'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('选择商品') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <div class="dec-goods-group">
                        <ProductInfoSelect v-model:productList="productList" :isMultiple="true"></ProductInfoSelect>
                    </div>
                </div>
                <div class="dec-edit-group-desc">
                    <div>{{ $t('提示：选择商品后自动加载商品信息；可通过拖拽进行商品排序') }}</div>
                </div>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('参数设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('模块标题') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showModuleTitle">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('隐藏') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showModuleTitle">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题文字') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-input v-model="module.moduleTitle" :placeholder="$t('请输入模块标题')" maxlength="20" show-word-limit></el-input>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品展示样式') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.cardStyle">
                        <el-radio-button value="noBorderWhite">{{ $t('无边白底') }}</el-radio-button>
                        <el-radio-button value="shadow">{{ $t('卡片投影') }}</el-radio-button>
                        <el-radio-button value="border">{{ $t('描边白底') }}</el-radio-button>
                        <el-radio-button value="transparent">{{ $t('无边透底') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品圆角') }}</div>
                    <div class="value">{{ module.goodsRadius }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.goodsRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片下圆角') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.picBottomRadius">
                        <el-radio-button :value="1">{{ $t('圆角') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('直角') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片比例') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productPicRatio">
                        <el-radio-button value="default">{{ $t('默认') }}</el-radio-button>
                        <el-radio-button value="1:1">1:1</el-radio-button>
                        <el-radio-button value="3:4">3:4</el-radio-button>
                        <el-radio-button value="3:2">3:2</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('文本样式') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.textWeight">
                        <el-radio-button :value="400">{{ $t('常规体') }}</el-radio-button>
                        <el-radio-button :value="700">{{ $t('加粗体') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('文本对齐') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.textAlign">
                        <el-radio-button value="left">{{ $t('左对齐') }}</el-radio-button>
                        <el-radio-button value="center">{{ $t('居中') }}</el-radio-button>
                        <el-radio-button value="right">{{ $t('右对齐') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品外间距') }}</div>
                    <div class="value">{{ module.itemGap }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.itemGap" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品内间距') }}</div>
                    <div class="value">{{ module.innerPadding }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.innerPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('单卡宽度') }}</div>
                    <div class="value">{{ module.cardWidthPercent }}%</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.cardWidthPercent" show-input :show-input-controls="false" size="small" input-size="default" :min="60" :max="95" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('自动播放') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.autoplay">
                        <el-radio-button :value="1">{{ $t('开') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('关') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('显示内容') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品名称') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showName">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品描述') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showDesc">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品价格') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showPrice">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品原价') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showMarketPrice">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品销量') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showSales">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <BuyBtnEdit v-model="buyBtnStyleProxy"></BuyBtnEdit>
        </div>

        <div v-if="activeName === 'extend'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('颜色设置') }}</div>
            </div>
            <div class="dec-edit-group" v-for="row in colorRows" :key="row.key">
                <div class="dec-edit-group-title">
                    <div class="label">{{ row.label }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group" need-refresh="true">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="resetColor(row.key, row.default)">{{ $t('重置') }}</a>
                            <SelectColor :color="(module as any)[row.key]" @update:color="(v: string) => ((module as any)[row.key] = v)"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('模块参数') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('模块左右边距') }}</div>
                    <div class="value">{{ frame.boxPadding }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.boxPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('模块上边距') }}</div>
                    <div class="value">{{ frame.boxPaddingTop }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.boxPaddingTop" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('模块下边距') }}</div>
                    <div class="value">{{ frame.boxPaddingBottom }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.boxPaddingBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题与内容间距') }}</div>
                    <div class="value">{{ module.titleContentGap }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.titleContentGap" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('模块背景色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="frame.backgroundColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="frame.backgroundColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('模块背景图：宽度750px，高度不限') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="moduleBgImageProxy" :default-value="defaults.moduleBgImage"></PicSelect>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('背景图模式') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.bgImageMode">
                        <el-radio-button value="normal">{{ $t('常规充满') }}</el-radio-button>
                        <el-radio-button value="repeat">{{ $t('平铺') }}</el-radio-button>
                        <el-radio-button value="fixed">{{ $t('固定') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed, ref } from "vue";
import { cloneDeep } from "lodash";
import Tabs from "@/components/tabs/Index.vue";
import { SelectColor } from "@/components/select";
import { PicSelect, ProductInfoSelect } from "@/components/decorate";
import BuyBtnEdit from "../../src/buyBtn/Edit.vue";
import { ModuleType, ProductCarouselType, PruductList, ModuleFrameType, BuyButtonStyleType } from "@/types/decorate/decorate.d";
import { defaultProductCarousel } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & ProductCarouselType>("module", { default: () => ({}) });

const tabs = ref<any[]>([
    { label: t('商品设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");

const defaults = defaultProductCarousel;

// 购买按钮：保证 buyBtnStyle 始终是合法对象（兼容历史草稿里曾为数字的情况）；缺省回退默认（默认显示）
const buyBtnStyleProxy = computed<BuyButtonStyleType>({
    get() {
        const v = module.value.buyBtnStyle;
        if (!v || typeof v !== "object") {
            module.value.buyBtnStyle = cloneDeep(defaults.buyBtnStyle) as BuyButtonStyleType;
        }
        return module.value.buyBtnStyle as BuyButtonStyleType;
    },
    set(v: BuyButtonStyleType) {
        module.value.buyBtnStyle = v;
    }
});

// 复用 ProductInfoSelect（组合推荐同款选品组件：拖拽排序 + 标准添加按钮 + 自动带商品信息）
const productList = computed<PruductList[]>({
    get() {
        if (!Array.isArray(module.value.productList)) {
            module.value.productList = [];
        }
        return module.value.productList as PruductList[];
    },
    set(v: PruductList[]) {
        module.value.productList = v;
    }
});

const frame = computed<ModuleFrameType>({
    get() {
        if (!module.value.frame) {
            module.value.frame = { ...(defaults.frame as any) };
        }
        return module.value.frame as ModuleFrameType;
    },
    set(v) {
        module.value.frame = v;
    }
});

type ColorRowDef = { label: string; key: keyof ProductCarouselType; default: string };
const colorRows: ColorRowDef[] = [
    { label: t('模块标题色'), key: "moduleTitleColor", default: defaults.moduleTitleColor },
    { label: t('商品标题色'), key: "productTitleColor", default: defaults.productTitleColor },
    { label: t('商品描述文字色'), key: "descColor", default: defaults.descColor },
    { label: t('商品价格色'), key: "priceColor", default: defaults.priceColor },
    { label: t('商品原价色'), key: "marketPriceColor", default: defaults.marketPriceColor },
    { label: t('商品销量色'), key: "salesColor", default: defaults.salesColor },
    { label: t('角标色'), key: "badgeColor", default: defaults.badgeColor }
];

const resetColor = (key: keyof ProductCarouselType, defaultValue: string) => {
    (module.value as any)[key] = defaultValue;
};

const moduleBgImageProxy = computed({
    get() {
        if (!module.value.moduleBgImage) {
            module.value.moduleBgImage = { picUrl: "", picThumb: "" };
        }
        return module.value.moduleBgImage as { picUrl?: string; picThumb?: string };
    },
    set(v: { picUrl?: string; picThumb?: string }) {
        module.value.moduleBgImage = v;
    }
});
</script>
<style lang="less" scoped></style>
