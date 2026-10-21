<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('限时特惠') }}</h3>
            <div class="dec-edit-desc">{{ $t('整体背景图容器 + 右侧横滑商品（标题/按钮等文字烧在背景图内）') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>

        <div v-if="activeName === 'content'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('商品设置') }}</div>
            </div>
            <CommonProductEdit v-model="module.products"></CommonProductEdit>

            <div class="dec-spread-title">
                <div class="title">{{ $t('商品卖点') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('显示卖点角标') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showBrief">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <template v-if="module.showBrief">
                <div class="dec-edit-group" v-if="productCount === 0">
                    <div class="dec-edit-group-desc">
                        <div>{{ $t('请先在上方「选择商品」中添加商品后再来配置卖点文字') }}</div>
                    </div>
                </div>
                <div class="product-brief-list" v-if="productCount > 0">
                    <div class="brief-card" v-for="index in productIndexes" :key="index">
                        <span class="brief-card-label">{{ $t('第{index1}个商品', { index1: index + 1 }) }}<span v-if="isDynamic" class="brief-card-dynamic">{{ $t('动态拉取') }}</span>
                        </span>
                        <el-input v-model="productBriefListProxy[index]" :placeholder="$t('如：新品 / 折扣')" maxlength="6" show-word-limit></el-input>
                    </div>
                </div>
            </template>

            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('展示模式') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品图片比例') }}</div>
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
                    <div class="label">{{ $t('图片模式') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productPicFillType">
                        <el-radio-button value="cover">{{ $t('填充') }}</el-radio-button>
                        <el-radio-button value="contain">{{ $t('周边留白') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片圆角') }}</div>
                    <div class="value">{{ module.productImgRadius }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productImgRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('卡片圆角') }}</div>
                    <div class="value">{{ module.cardRadius }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.cardRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品百分比') }}</div>
                    <div class="value">{{ module.productPercent }}%</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productPercent" show-input :show-input-controls="false" size="small" input-size="default" :min="20" :max="50" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品外边距') }}</div>
                    <div class="value">{{ module.productItemGap }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productItemGap" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品内边距') }}</div>
                    <div class="value">{{ module.productItemPadding }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productItemPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('文字对齐') }}</div>
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
                    <div class="label">{{ $t('文字样式') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.textWeight">
                        <el-radio-button :value="400">{{ $t('常规体') }}</el-radio-button>
                        <el-radio-button :value="700">{{ $t('加粗体') }}</el-radio-button>
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
                    <el-radio-group class="dec-radio-group" v-model="module.showProductName">
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
            <template v-if="module.showPrice">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('价格字号') }}</div>
                        <div class="value">{{ module.priceFontSize }}px</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider v-model="module.priceFontSize" show-input :show-input-controls="false" size="small" input-size="default" :min="10" :max="24" />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('价格粗体') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.priceBold">
                            <el-radio-button :value="1">{{ $t('粗体') }}</el-radio-button>
                            <el-radio-button :value="0">{{ $t('常规') }}</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </template>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('划线原价') }}</div>
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
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品评价') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showComment">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('购买按钮样式') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.buyBtnStyle">
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
        </div>

        <div v-if="activeName === 'extend'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('商品配色') }}</div>
            </div>
            <div class="dec-edit-group" v-for="row in productColorRows" :key="row.key">
                <div class="dec-edit-group-title">
                    <div class="label">{{ row.label }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group" need-refresh="true">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="resetColor(row.key, row.default)">{{ $t('重置') }}</a>
                            <SelectColor
                                :color="(module as any)[row.key]"
                                @update:color="(v: string) => ((module as any)[row.key] = v)"
                            ></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('卖点背景渐变') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-gradient-pair">
                        <a class="dec-color-reset" @click="resetGradient('briefGradientStart', 'briefGradientEnd', defaults.briefGradientStart, defaults.briefGradientEnd)">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.briefGradientStart"></SelectColor>
                        <span class="gradient-sep">~</span>
                        <SelectColor v-model:color="module.briefGradientEnd"></SelectColor>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('按钮背景渐变') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-gradient-pair">
                        <a class="dec-color-reset" @click="resetGradient('buyBtnGradientStart', 'buyBtnGradientEnd', defaults.buyBtnGradientStart, defaults.buyBtnGradientEnd)">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.buyBtnGradientStart"></SelectColor>
                        <span class="gradient-sep">~</span>
                        <SelectColor v-model:color="module.buyBtnGradientEnd"></SelectColor>
                    </div>
                </div>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('容器设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('容器背景图') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="containerBgImageProxy" :default-value="defaults.containerBgImage"></PicSelect>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('外层兜底渐变') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-gradient-pair">
                        <a class="dec-color-reset" @click="resetGradient('boxGradientStart', 'boxGradientEnd', defaults.boxGradientStart, defaults.boxGradientEnd)">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.boxGradientStart"></SelectColor>
                        <span class="gradient-sep">~</span>
                        <SelectColor v-model:color="module.boxGradientEnd"></SelectColor>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品区左偏移') }}</div>
                    <div class="value">{{ module.productAreaLeft }}%</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productAreaLeft" show-input :show-input-controls="false" size="small" input-size="default" :max="60" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-desc">
                    <div>{{ $t('左侧让出背景图文字区的宽度；该区域作为「查看更多」点击热区') }}</div>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('查看更多链接') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="lyecs-link-select">
                        <SelectLink v-model="module.moreLink"></SelectLink>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品区上下边距') }}</div>
                    <div class="value">{{ module.productAreaPaddingY }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productAreaPaddingY" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('圆角') }}</div>
                    <div class="value">{{ frame.boxRadius }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.boxRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('左右边距') }}</div>
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
                    <div class="label">{{ $t('上边距') }}</div>
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
                    <div class="label">{{ $t('下边距') }}</div>
                    <div class="value">{{ frame.boxPaddingBottom }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.boxPaddingBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import Tabs from "@/components/tabs/Index.vue";
import { SelectColor, SelectLink } from "@/components/select";
import { PicSelect } from "@/components/decorate";
import CommonProductEdit from "../../src/commonProduct/Edit.vue";
import { ModuleType, LimitedSpecialType, ModuleFrameType } from "@/types/decorate/decorate.d";
import { defaultLimitedSpecial } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & LimitedSpecialType>("module", { default: () => ({}) });

const tabs = ref<any[]>([
    { label: t('基础设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");

const defaults = defaultLimitedSpecial;

const frame = computed<ModuleFrameType>({
    get() {
        if (!module.value.frame) {
            module.value.frame = { ...(defaults.frame as ModuleFrameType) };
        }
        return module.value.frame as ModuleFrameType;
    },
    set(v) {
        module.value.frame = v;
    }
});

const productCount = computed(() => {
    const p: any = module.value?.products;
    if (!p) return 0;
    if (p.productSelectType === 1) return p.productIds?.length || 0;
    return Number(p.productNumber) || 0;
});
const productIndexes = computed<number[]>(() => Array.from({ length: productCount.value }, (_, i) => i));

const isDynamic = computed(() => {
    const t = (module.value?.products as any)?.productSelectType;
    return t === 2 || t === 3;
});

const ensureProductBriefList = () => {
    if (!Array.isArray(module.value.productBriefList)) {
        module.value.productBriefList = [];
    }
};

const productBriefListProxy = computed({
    get() {
        ensureProductBriefList();
        const arr = module.value.productBriefList as string[];
        while (arr.length < productCount.value) arr.push("");
        return arr;
    },
    set(v: string[]) {
        module.value.productBriefList = v;
    }
});

let prevProductIds: number[] = [];
watch(
    () => {
        const ids = (module.value?.products as any)?.productIds;
        return Array.isArray(ids) ? [...ids] : [];
    },
    (newIds) => {
        const t = (module.value?.products as any)?.productSelectType;
        if (t !== 1) {
            prevProductIds = [];
            return;
        }
        if (!newIds) return;
        if (prevProductIds.length === 0 && newIds.length > 0) {
            prevProductIds = [...newIds];
            return;
        }
        const prevIdToIdx = new Map<number, number>();
        prevProductIds.forEach((id, i) => prevIdToIdx.set(id, i));
        ensureProductBriefList();
        const oldBriefs = [...(module.value.productBriefList as string[])];
        module.value.productBriefList = newIds.map((id) => {
            const oldIdx = prevIdToIdx.get(id);
            return oldIdx !== undefined ? oldBriefs[oldIdx] || "" : "";
        });
        prevProductIds = [...newIds];
    },
    { immediate: true }
);

watch(
    () => [(module.value?.products as any)?.productSelectType, (module.value?.products as any)?.productNumber],
    () => {
        ensureProductBriefList();
        const n = productCount.value;
        const arr = module.value.productBriefList as string[];
        while (arr.length < n) arr.push("");
        if (arr.length > n) arr.length = n;
    }
);

type ColorRowDef = { label: string; key: keyof LimitedSpecialType; default: string };

const productColorRows: ColorRowDef[] = [
    { label: t('卡片背景色'), key: "cardBgColor", default: defaults.cardBgColor },
    { label: t('商品名称色'), key: "productTitleColor", default: defaults.productTitleColor },
    { label: t('商品价格色'), key: "productPriceColor", default: defaults.productPriceColor },
    { label: t('商品原价色'), key: "productMarketPriceColor", default: defaults.productMarketPriceColor },
    { label: t('销量/评价色'), key: "productSalesColor", default: defaults.productSalesColor },
    { label: t('卖点文字色'), key: "briefTextColor", default: defaults.briefTextColor },
    { label: t('按钮文字色'), key: "buyBtnTextColor", default: defaults.buyBtnTextColor }
];

const resetColor = (key: keyof LimitedSpecialType, defaultValue: string) => {
    (module.value as any)[key] = defaultValue;
};

const resetGradient = (
    startKey: keyof LimitedSpecialType,
    endKey: keyof LimitedSpecialType,
    defaultStart: string,
    defaultEnd: string
) => {
    (module.value as any)[startKey] = defaultStart;
    (module.value as any)[endKey] = defaultEnd;
};

const containerBgImageProxy = computed({
    get() {
        if (!module.value.containerBgImage) {
            module.value.containerBgImage = { picUrl: "", picThumb: "" };
        }
        return module.value.containerBgImage as { picUrl?: string; picThumb?: string };
    },
    set(v: { picUrl?: string; picThumb?: string }) {
        module.value.containerBgImage = v;
    }
});
</script>
<style lang="less" scoped>
.dec-gradient-pair {
    display: flex;
    align-items: center;
    gap: 8px;
    .gradient-sep {
        color: #999;
    }
}
.product-brief-list {
    padding: 0 10px 10px;
    .brief-card {
        background-color: #fff;
        border-radius: 4px;
        box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.15);
        padding: 8px 10px 10px;
        margin-top: 10px;
        .brief-card-label {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 6px;
            .brief-card-dynamic {
                font-size: 11px;
                font-weight: 400;
                color: #999;
                padding: 1px 6px;
                background-color: #f5f7fa;
                border-radius: 2px;
            }
        }
    }
}
</style>
