<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('商品滑动') }}</h3>
            <div class="dec-edit-desc">{{ $t('横向滑动主题商品卡：逐卡渐变底色 + 逐卡标题 + 商品图 + 价格') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>

        <div v-if="activeName === 'content'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('选择商品') }}</div>
            </div>
            <CommonProductEdit v-model="module.products"></CommonProductEdit>

            <div class="dec-spread-title">
                <div class="title">{{ $t('商品卖点描述设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('逐卡标题文字') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="cardTextExpand">
                        <el-radio-button :value="1">{{ $t('展开') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('收起') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <template v-if="cardTextExpand">
                <div class="dec-edit-group" v-if="productCount === 0">
                    <div class="dec-edit-group-desc">
                        <div>{{ $t('请先在上方「选择商品」中添加商品后再来配置逐卡标题') }}</div>
                    </div>
                </div>
                <div class="product-brief-list" v-if="productCount > 0">
                    <div class="brief-card" v-for="index in productIndexes" :key="index">
                        <span class="brief-card-label">{{ $t('第{index1}个商品', { index1: index + 1 }) }}<span v-if="isDynamic" class="brief-card-dynamic">{{ $t('动态拉取') }}</span>
                        </span>
                        <el-input v-model="cardTitleListProxy[index]" :placeholder="$t('如：服饰专场')" maxlength="8" show-word-limit></el-input>
                    </div>
                </div>
            </template>

            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('展示模式') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品层叠样式') }}</div>
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
                    <div class="label">{{ $t('图片填充') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productPicFillType">
                        <el-radio-button value="cover">{{ $t('充满') }}</el-radio-button>
                        <el-radio-button value="contain">{{ $t('周边留白') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品底角') }}</div>
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
                    <div class="label">{{ $t('图片下平台') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showImagePlatform">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
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
                    <div class="label">{{ $t('商品层间距') }}</div>
                    <div class="value">{{ module.layerGap }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.layerGap" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品间间距') }}</div>
                    <div class="value">{{ module.productItemGap }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productItemGap" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
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
                        <el-slider v-model="module.cardRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('单卡宽度') }}</div>
                    <div class="value">{{ module.productPercent }}%</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productPercent" show-input :show-input-controls="false" size="small" input-size="default" :min="30" :max="60" />
                    </div>
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
                    <div class="label">{{ $t('商品特征') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showFeature">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品卖点') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showBrief">
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
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('划入查看') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showSlidePeek">
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
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商详跳转模式') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.detailJumpMode">
                        <el-radio-button value="default">{{ $t('默认') }}</el-radio-button>
                        <el-radio-button value="native">{{ $t('原生') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
        </div>

        <div v-if="activeName === 'extend'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('颜色设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('逐卡背景渐变') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="cardColorExpand">
                        <el-radio-button :value="1">{{ $t('展开') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('收起') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <template v-if="cardColorExpand">
                <div class="dec-edit-group" v-if="productCount === 0">
                    <div class="dec-edit-group-desc">
                        <div>{{ $t('请先在「商品基本设置」中添加商品后再来配置逐卡渐变') }}</div>
                    </div>
                </div>
                <div class="product-brief-list" v-if="productCount > 0">
                    <div class="brief-card" v-for="index in productIndexes" :key="index">
                        <span class="brief-card-label">{{ $t('第{index1}个商品', { index1: index + 1 }) }}<a class="brief-card-reset" @click="resetCardGradient(index)">{{ $t('重置') }}</a>
                        </span>
                        <div class="dec-gradient-pair">
                            <SelectColor v-model:color="cardGradientStartListProxy[index]"></SelectColor>
                            <span class="gradient-sep">~</span>
                            <SelectColor v-model:color="cardGradientEndListProxy[index]"></SelectColor>
                        </div>
                    </div>
                </div>
            </template>

            <div class="dec-divider-line"></div>
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
                <div class="title">{{ $t('模块设置') }}</div>
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
                    <div class="label">{{ $t('卡片区圆角') }}</div>
                    <div class="value">{{ frame.boxRadius }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="frame.boxRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('模块内部背景色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.innerBgColor = defaults.innerBgColor">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.innerBgColor"></SelectColor>
                        </div>
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
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import Tabs from "@/components/tabs/Index.vue";
import { SelectColor } from "@/components/select";
import { PicSelect } from "@/components/decorate";
import CommonProductEdit from "../../src/commonProduct/Edit.vue";
import { ModuleType, ProductSlideType, ModuleFrameType } from "@/types/decorate/decorate.d";
import { defaultProductSlide } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & ProductSlideType>("module", { default: () => ({}) });

const tabs = ref<any[]>([
    { label: t('商品基本设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");
const cardTextExpand = ref(1);
const cardColorExpand = ref(1);

const defaults = defaultProductSlide;

const gradientFallback = [
    ["#B6A8F5", "#8E7BF0"],
    ["#FF8FB8", "#FF5E96"],
    ["#FFD98A", "#FBC25A"],
    ["#C7BEF8", "#9C8CF0"]
];

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

const ensureArr = (key: "cardTitleList" | "cardGradientStartList" | "cardGradientEndList") => {
    if (!Array.isArray(module.value[key])) {
        module.value[key] = [] as string[];
    }
};
const ensureAll = () => {
    ensureArr("cardTitleList");
    ensureArr("cardGradientStartList");
    ensureArr("cardGradientEndList");
};

const cardTitleListProxy = computed({
    get() {
        ensureArr("cardTitleList");
        const arr = module.value.cardTitleList as string[];
        while (arr.length < productCount.value) arr.push("");
        return arr;
    },
    set(v: string[]) {
        module.value.cardTitleList = v;
    }
});

const cardGradientStartListProxy = computed({
    get() {
        ensureArr("cardGradientStartList");
        const arr = module.value.cardGradientStartList as string[];
        while (arr.length < productCount.value) arr.push(gradientFallback[arr.length % gradientFallback.length][0]);
        return arr;
    },
    set(v: string[]) {
        module.value.cardGradientStartList = v;
    }
});

const cardGradientEndListProxy = computed({
    get() {
        ensureArr("cardGradientEndList");
        const arr = module.value.cardGradientEndList as string[];
        while (arr.length < productCount.value) arr.push(gradientFallback[arr.length % gradientFallback.length][1]);
        return arr;
    },
    set(v: string[]) {
        module.value.cardGradientEndList = v;
    }
});

const resetCardGradient = (index: number) => {
    ensureAll();
    const pair = gradientFallback[index % gradientFallback.length];
    (module.value.cardGradientStartList as string[])[index] = pair[0];
    (module.value.cardGradientEndList as string[])[index] = pair[1];
};

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
        ensureAll();
        const oldTitle = [...(module.value.cardTitleList as string[])];
        const oldStart = [...(module.value.cardGradientStartList as string[])];
        const oldEnd = [...(module.value.cardGradientEndList as string[])];
        module.value.cardTitleList = newIds.map((id) => {
            const oldIdx = prevIdToIdx.get(id);
            return oldIdx !== undefined ? oldTitle[oldIdx] || "" : "";
        });
        module.value.cardGradientStartList = newIds.map((id, i) => {
            const oldIdx = prevIdToIdx.get(id);
            return oldIdx !== undefined ? oldStart[oldIdx] || gradientFallback[i % gradientFallback.length][0] : gradientFallback[i % gradientFallback.length][0];
        });
        module.value.cardGradientEndList = newIds.map((id, i) => {
            const oldIdx = prevIdToIdx.get(id);
            return oldIdx !== undefined ? oldEnd[oldIdx] || gradientFallback[i % gradientFallback.length][1] : gradientFallback[i % gradientFallback.length][1];
        });
        prevProductIds = [...newIds];
    },
    { immediate: true }
);

watch(
    () => [(module.value?.products as any)?.productSelectType, (module.value?.products as any)?.productNumber],
    () => {
        ensureAll();
        const n = productCount.value;
        const t = module.value.cardTitleList as string[];
        const s = module.value.cardGradientStartList as string[];
        const e = module.value.cardGradientEndList as string[];
        while (t.length < n) t.push("");
        while (s.length < n) s.push(gradientFallback[s.length % gradientFallback.length][0]);
        while (e.length < n) e.push(gradientFallback[e.length % gradientFallback.length][1]);
        if (t.length > n) t.length = n;
        if (s.length > n) s.length = n;
        if (e.length > n) e.length = n;
    }
);

type ColorRowDef = { label: string; key: keyof ProductSlideType; default: string };

const productColorRows: ColorRowDef[] = [
    { label: t('商品标题色'), key: "cardTitleColor", default: defaults.cardTitleColor },
    { label: t('商品卖点色'), key: "briefColor", default: defaults.briefColor },
    { label: t('商品描述文字色'), key: "descColor", default: defaults.descColor },
    { label: t('商品价格色'), key: "productPriceColor", default: defaults.productPriceColor },
    { label: t('商品原价色'), key: "productMarketPriceColor", default: defaults.productMarketPriceColor },
    { label: t('商品销量色'), key: "productSalesColor", default: defaults.productSalesColor },
    { label: t('角标色'), key: "badgeColor", default: defaults.badgeColor },
    { label: t('按钮文字色'), key: "buyBtnTextColor", default: defaults.buyBtnTextColor }
];

const resetColor = (key: keyof ProductSlideType, defaultValue: string) => {
    (module.value as any)[key] = defaultValue;
};

const resetGradient = (
    startKey: keyof ProductSlideType,
    endKey: keyof ProductSlideType,
    defaultStart: string,
    defaultEnd: string
) => {
    (module.value as any)[startKey] = defaultStart;
    (module.value as any)[endKey] = defaultEnd;
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
            justify-content: space-between;
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
            .brief-card-reset {
                font-size: 12px;
                font-weight: 400;
                color: var(--theme-color, #ff6a00);
                cursor: pointer;
            }
        }
    }
}
</style>
