<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>商品轮播</h3>
            <div class="dec-edit-desc">真·轮播经典商品卡：中间大卡两侧露出；选择商品后自动加载商品信息，可拖拽排序</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>

        <div v-if="activeName === 'content'">
            <div class="dec-edit-title-box">
                <div class="title">选择商品</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <div class="dec-goods-group">
                        <ProductInfoSelect v-model:productList="productList" :isMultiple="true"></ProductInfoSelect>
                    </div>
                </div>
                <div class="dec-edit-group-desc">
                    <div>提示：选择商品后自动加载商品信息；可通过拖拽进行商品排序</div>
                </div>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-edit-title-box">
                <div class="title">参数设置</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">模块标题</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showModuleTitle">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">隐藏</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showModuleTitle">
                <div class="dec-edit-group-title">
                    <div class="label">标题文字</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-input v-model="module.moduleTitle" placeholder="请输入模块标题" maxlength="20" show-word-limit></el-input>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">商品展示样式</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.cardStyle">
                        <el-radio-button value="noBorderWhite">无边白底</el-radio-button>
                        <el-radio-button value="shadow">卡片投影</el-radio-button>
                        <el-radio-button value="border">描边白底</el-radio-button>
                        <el-radio-button value="transparent">无边透底</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品圆角</div>
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
                    <div class="label">图片下圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.picBottomRadius">
                        <el-radio-button :value="1">圆角</el-radio-button>
                        <el-radio-button :value="0">直角</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">图片比例</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productPicRatio">
                        <el-radio-button value="default">默认</el-radio-button>
                        <el-radio-button value="1:1">1:1</el-radio-button>
                        <el-radio-button value="3:4">3:4</el-radio-button>
                        <el-radio-button value="3:2">3:2</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">文本样式</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.textWeight">
                        <el-radio-button :value="400">常规体</el-radio-button>
                        <el-radio-button :value="700">加粗体</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">文本对齐</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.textAlign">
                        <el-radio-button value="left">左对齐</el-radio-button>
                        <el-radio-button value="center">居中</el-radio-button>
                        <el-radio-button value="right">右对齐</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品外间距</div>
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
                    <div class="label">商品内间距</div>
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
                    <div class="label">单卡宽度</div>
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
                    <div class="label">自动播放</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.autoplay">
                        <el-radio-button :value="1">开</el-radio-button>
                        <el-radio-button :value="0">关</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-edit-title-box">
                <div class="title">显示内容</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品名称</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showName">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品描述</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showDesc">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品价格</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showPrice">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品原价</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showMarketPrice">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品销量</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showSales">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <BuyBtnEdit v-model="buyBtnStyleProxy"></BuyBtnEdit>
        </div>

        <div v-if="activeName === 'extend'">
            <div class="dec-edit-title-box">
                <div class="title">颜色设置</div>
            </div>
            <div class="dec-edit-group" v-for="row in colorRows" :key="row.key">
                <div class="dec-edit-group-title">
                    <div class="label">{{ row.label }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group" need-refresh="true">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="resetColor(row.key, row.default)">重置</a>
                            <SelectColor :color="(module as any)[row.key]" @update:color="(v: string) => ((module as any)[row.key] = v)"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-title-box">
                <div class="title">模块参数</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">模块左右边距</div>
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
                    <div class="label">模块上边距</div>
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
                    <div class="label">模块下边距</div>
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
                    <div class="label">标题与内容间距</div>
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
                    <div class="label">模块背景色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="frame.backgroundColor = ''">重置</a>
                            <SelectColor v-model:color="frame.backgroundColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">模块背景图：宽度750px，高度不限</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="moduleBgImageProxy" :default-value="defaults.moduleBgImage"></PicSelect>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">背景图模式</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.bgImageMode">
                        <el-radio-button value="normal">常规充满</el-radio-button>
                        <el-radio-button value="repeat">平铺</el-radio-button>
                        <el-radio-button value="fixed">固定</el-radio-button>
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
import BuyBtnEdit from "@/views/decorate/decorate/src/modules/src/buyBtn/Edit.vue";
import { ProductCarouselType, PruductList, ModuleFrameType, BuyButtonStyleType } from "@/types/decorate/decorate.d";
import { defaultProductCarousel } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { ProductCarouselModule } from "./types";

// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，
// 模板内 v-model 直接写该 reactive 对象（与旧 defineModel 等价，公共子组件零改造复用）
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as ProductCarouselModule);

const tabs = ref<any[]>([
    { label: "商品设置", name: "content" },
    { label: "扩展设置", name: "extend" }
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
    { label: "模块标题色", key: "moduleTitleColor", default: defaults.moduleTitleColor },
    { label: "商品标题色", key: "productTitleColor", default: defaults.productTitleColor },
    { label: "商品描述文字色", key: "descColor", default: defaults.descColor },
    { label: "商品价格色", key: "priceColor", default: defaults.priceColor },
    { label: "商品原价色", key: "marketPriceColor", default: defaults.marketPriceColor },
    { label: "商品销量色", key: "salesColor", default: defaults.salesColor },
    { label: "角标色", key: "badgeColor", default: defaults.badgeColor }
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
