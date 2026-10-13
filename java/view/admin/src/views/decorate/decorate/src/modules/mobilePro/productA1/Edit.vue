<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('商品 A1') }}</h3>
            <div class="dec-edit-desc">{{ $t('基于商品模块增强：支持每个商品独立配置标签和卖点描述') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>

        <div v-if="activeName === 'content'">
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('选择模板') }}</div>
                <div class="value">{{ styleLabel[module.style as number] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.style">
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行一个')" :show-after="100">
                        <el-radio-button :value="1"><i class="ico-decorate icon-dec-up2end"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行两个')" :show-after="100">
                        <el-radio-button :value="2"><i class="ico-decorate icon-dec-small"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行三个')" :show-after="100">
                        <el-radio-button :value="3"><i class="ico-decorate icon-dec-three"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('详细列表')" :show-after="100">
                        <el-radio-button :value="4"><i class="ico-decorate icon-dec-list"></i></el-radio-button>
                    </el-tooltip>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <CommonActivityFrameEdit v-model="frameModule" type="product" section="content" use-goods-radius-slider hide-text-align hide-buy-btn-style>
            <template #afterShowName>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('商品名称对齐') }}</div>
                        <div class="value">{{ alignLabel[module.nameAlign || 1] }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.nameAlign">
                            <el-radio-button :value="1">{{ $t('左对齐') }}</el-radio-button>
                            <el-radio-button :value="2">{{ $t('居中对齐') }}</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </template>
            <template #afterShowPrice>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('价格对齐') }}</div>
                        <div class="value">{{ alignLabel[module.priceAlign || 1] }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.priceAlign">
                            <el-radio-button :value="1">{{ $t('左对齐') }}</el-radio-button>
                            <el-radio-button :value="2">{{ $t('居中对齐') }}</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('显示划线价') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.showMarketPrice">
                            <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                            <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </template>
            <template #afterGoodsPadding>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('商品卡片间距') }}</div>
                        <div class="value">{{ module.itemGap }}px</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider v-model="module.itemGap" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                        </div>
                    </div>
                </div>
            </template>
        </CommonActivityFrameEdit>

        <div class="dec-divider-line"></div>
        <div class="dec-spread-title">
            <div class="title">{{ $t('购买按钮') }}</div>
        </div>
        <BuyBtnEdit v-model="buyBtnStyleProxy" :show-wrap-option="true"></BuyBtnEdit>

        <div class="dec-divider-line"></div>
        <div class="dec-spread-title">
            <div class="title">{{ $t('商品标签与卖点') }}</div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('显示商品标签') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.showProductLabel">
                    <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-if="module.showProductLabel">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('标签背景色') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.labelBackgroundColor = ''">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.labelBackgroundColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group" v-if="module.showProductLabel">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('标签文字色') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.labelTextColor = ''">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.labelTextColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group" v-if="module.showProductLabel">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('标签圆角值') }}</div>
                <div class="value">{{ module.labelRadius }}px</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.labelRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group dec-edit-group-block" v-if="module.showProductLabel">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('圆角应用方式') }}</div>
                <div class="value">{{ labelRadiusStyleLabel[module.labelRadiusStyle as number] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.labelRadiusStyle">
                    <el-radio-button :value="0">{{ $t('全圆角') }}</el-radio-button>
                    <el-radio-button :value="1">{{ $t('左边圆角') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('右边圆角') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('对角 ↖↘') }}</el-radio-button>
                    <el-radio-button :value="4">{{ $t('对角 ↗↙') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-if="module.showProductLabel">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('标签字号') }}</div>
                <div class="value">{{ module.labelFontSize }}px</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.labelFontSize" show-input :show-input-controls="false" size="small" input-size="default" :min="9" :max="16" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group dec-edit-group-block" v-if="module.showProductLabel">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('标签位置') }}</div>
                <div class="value">{{ labelPositionLabel[module.labelPosition || "top-left"] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.labelPosition">
                    <el-radio-button value="top-left">{{ $t('左上角') }}</el-radio-button>
                    <el-radio-button value="top-right">{{ $t('右上角') }}</el-radio-button>
                    <el-radio-button value="bottom-left">{{ $t('左下角') }}</el-radio-button>
                    <el-radio-button value="bottom-right">{{ $t('右下角') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-if="module.showProductLabel">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('水平边距') }}</div>
                <div class="value">{{ module.labelOffsetX ?? 0 }}px</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.labelOffsetX" show-input :show-input-controls="false" size="small" input-size="default" :min="-10" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group" v-if="module.showProductLabel">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('垂直边距') }}</div>
                <div class="value">{{ module.labelOffsetY ?? 0 }}px</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.labelOffsetY" show-input :show-input-controls="false" size="small" input-size="default" :min="-10" :max="30" />
                </div>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('显示卖点描述') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.showBrief">
                    <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div v-if="module.showBrief">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('卖点字号') }}</div>
                    <div class="value">{{ briefFontSize }}px</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="briefFontSize" show-input :show-input-controls="false" size="small" input-size="default" :min="9" :max="16" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('卖点文字色') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="briefColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="briefColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('卖点粗细') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="briefFontBold">
                        <el-radio-button :value="400">{{ $t('常规体') }}</el-radio-button>
                        <el-radio-button :value="700">{{ $t('加粗体') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('卖点徽章背景色') }}</div>

                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.briefBackgroundColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.briefBackgroundColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="dec-divider-line"></div>
        <div class="dec-spread-title">
            <div class="title">{{ $t('每商品标签与卖点配置') }}</div>
        </div>
        <div class="dec-edit-group-desc" v-if="productCount === 0">
            <div>{{ $t('请先在上方「选择商品」中添加商品后再来配置标签与卖点') }}</div>
        </div>
        <div class="product-meta-list" v-if="productCount > 0">
            <div class="meta-card" v-for="index in productIndexes" :key="index">
                <div class="meta-card-title">{{ $t('第{index1}个商品', { index1: index + 1 }) }}<span v-if="isDynamic" class="meta-card-tag">{{ $t('动态拉取') }}</span></div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('标签') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-input-group">
                            <el-input
                                v-model="tagListProxy[index]"
                                :placeholder="$t('如：新品 / 折扣')"
                                maxlength="10"
                            ></el-input>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('卖点') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-input-group">
                            <el-input
                                type="textarea"
                                v-model="briefListProxy[index]"
                                :placeholder="$t('如：猜你喜欢')"
                                :autosize="{ minRows: 2, maxRows: 5 }"
                            ></el-input>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        </div>

        <div v-if="activeName === 'extend'">
            <CommonActivityFrameEdit v-model="frameModule" type="product" section="container" transparent-background></CommonActivityFrameEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed, ref, watch, type Ref } from "vue";
import { cloneDeep } from "lodash";
import CommonActivityFrameEdit from "../../src/commonActivityFrame/Edit.vue";
import BuyBtnEdit from "../../src/buyBtn/Edit.vue";
import Tabs from "@/components/tabs/Index.vue";
import { SelectColor } from "@/components/select";
import { ModuleType, ModuleActivityType, ProductA1Type, ModuleSubTitleType, BuyButtonStyleType } from "@/types/decorate/decorate.d";
import { defaultProductA1 } from "@/views/decorate/decorate/src/modules/";

// 排除 ModuleActivityType.buyBtnStyle（number），改由 ProductA1Type.buyBtnStyle（BuyButtonStyleType 对象）承载，避免类型交叉成 never
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & Omit<ModuleActivityType, "buyBtnStyle"> & ProductA1Type>("module", { default: () => ({}) });

// 公共框架编辑器已通过 hide-buy-btn-style 隐藏数字型 buyBtnStyle 设置，类型断言安全
const frameModule = module as unknown as Ref<ModuleActivityType & ModuleType>;

// 购买按钮：保证 buyBtnStyle 始终是合法对象（兼容历史草稿里曾为数字的情况）
const buyBtnStyleProxy = computed<BuyButtonStyleType>({
    get() {
        const v = module.value.buyBtnStyle;
        if (!v || typeof v !== "object") {
            module.value.buyBtnStyle = cloneDeep(defaultProductA1.buyBtnStyle) as BuyButtonStyleType;
        }
        return module.value.buyBtnStyle as BuyButtonStyleType;
    },
    set(v: BuyButtonStyleType) {
        module.value.buyBtnStyle = v;
    }
});

const tabs = ref<any[]>([
    { label: t('基础设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");

const styleLabel: Record<number, string> = {
    1: t('一行一个'),
    2: t('一行两个'),
    3: t('一行三个'),
    4: t('详细列表')
};

const alignLabel: Record<number, string> = {
    1: t('左对齐'),
    2: t('居中对齐')
};

const labelRadiusStyleLabel: Record<number, string> = {
    0: t('全圆角'),
    1: t('左边圆角'),
    2: t('右边圆角'),
    3: t('对角 ↖↘'),
    4: t('对角 ↗↙')
};

const labelPositionLabel: Record<string, string> = {
    "top-left": t('左上角'),
    "top-right": t('右上角'),
    "bottom-left": t('左下角'),
    "bottom-right": t('右下角')
};

const ensureBriefStyle = () => {
    if (!module.value.briefStyle) {
        module.value.briefStyle = { showTitle: 1, titleColor: "#FF6600", titleFontSize: 11, titleFontBold: 400 };
    }
};
const briefFontSize = computed<number>({
    get() {
        ensureBriefStyle();
        return (module.value.briefStyle as ModuleSubTitleType).titleFontSize ?? 11;
    },
    set(v) {
        ensureBriefStyle();
        (module.value.briefStyle as ModuleSubTitleType).titleFontSize = v;
    }
});
const briefColor = computed<string>({
    get() {
        ensureBriefStyle();
        return (module.value.briefStyle as ModuleSubTitleType).titleColor ?? "#FF6600";
    },
    set(v) {
        ensureBriefStyle();
        (module.value.briefStyle as ModuleSubTitleType).titleColor = v;
    }
});
const briefFontBold = computed<number>({
    get() {
        ensureBriefStyle();
        return (module.value.briefStyle as ModuleSubTitleType).titleFontBold ?? 400;
    },
    set(v) {
        ensureBriefStyle();
        (module.value.briefStyle as ModuleSubTitleType).titleFontBold = v;
    }
});

const productCount = computed(() => {
    const p: any = module.value?.products;
    if (!p) return 0;
    if (p.productSelectType === 1) return p.productIds?.length || 0;
    return p.productNumber || 0;
});
const productIndexes = computed<number[]>(() => Array.from({ length: productCount.value }, (_, i) => i));

const isDynamic = computed(() => {
    const t = (module.value?.products as any)?.productSelectType;
    return t === 2 || t === 3;
});

const ensureArray = (key: "tagList" | "briefList") => {
    if (!Array.isArray((module.value as any)[key])) {
        (module.value as any)[key] = [];
    }
};

const tagListProxy = computed({
    get() {
        ensureArray("tagList");
        const arr = module.value.tagList as string[];
        while (arr.length < productCount.value) arr.push("");
        return arr;
    },
    set(v: string[]) {
        module.value.tagList = v;
    }
});

const briefListProxy = computed({
    get() {
        ensureArray("briefList");
        const arr = module.value.briefList as string[];
        while (arr.length < productCount.value) arr.push("");
        return arr;
    },
    set(v: string[]) {
        module.value.briefList = v;
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
        ensureArray("tagList");
        ensureArray("briefList");
        const oldTags = [...(module.value.tagList as string[])];
        const oldBriefs = [...(module.value.briefList as string[])];
        module.value.tagList = newIds.map((id) => {
            const oldIdx = prevIdToIdx.get(id);
            return oldIdx !== undefined ? oldTags[oldIdx] || "" : "";
        });
        module.value.briefList = newIds.map((id) => {
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
        ensureArray("tagList");
        ensureArray("briefList");
        const n = productCount.value;
        const tagArr = module.value.tagList as string[];
        const briefArr = module.value.briefList as string[];
        while (tagArr.length < n) tagArr.push("");
        while (briefArr.length < n) briefArr.push("");
        if (tagArr.length > n) tagArr.length = n;
        if (briefArr.length > n) briefArr.length = n;
    }
);
</script>
<style lang="less" scoped>
.product-meta-list {
    padding: 0 10px 15px 10px;
    .meta-card {
        margin-top: 12px;
        background-color: #fff;
        border-radius: 4px;
        box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.15);
        padding-bottom: 8px;
        .meta-card-title {
            padding: 10px 12px;
            font-size: 13px;
            font-weight: 600;
            border-bottom: 1px solid #f0f0f0;
            display: flex;
            align-items: center;
            gap: 8px;
            .meta-card-tag {
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
.dec-edit-group-desc {
    padding: 0 12px 10px 12px;
    color: #999;
    font-size: 12px;
}
</style>
