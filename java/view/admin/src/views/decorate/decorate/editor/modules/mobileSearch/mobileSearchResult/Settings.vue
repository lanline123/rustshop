<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>搜索结果</h3>
            <div class="dec-edit-desc">展示搜索结果页面样式预览</div>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>

                <template v-if="isStoreResult">
                    <div class="dec-divider-line"></div>
                    <div class="dec-spread-title">
                        <div class="title">门店信息</div>
                    </div>

                    <div class="dec-edit-group" v-for="item in storeInfoSwitchFields" :key="item.field">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ item.label }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="asBool(localData[item.field], true) ? 1 : 0"
                                @update:model-value="(v: string | number) => updateField(item.field, Number(v))"
                            >
                                <el-radio-button :value="1">显示</el-radio-button>
                                <el-radio-button :value="0">不显示</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>

                    <div class="dec-divider-line"></div>
                    <div class="dec-spread-title">
                        <div class="title">门店名称</div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">门店名称颜色</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group" need-refresh="true">
                                <div class="dec-color-button">
                                    <a class="dec-color-reset" @click="onResetColor('storeShopTitleColor')">重置</a>
                                    <SelectColor
                                        :color="localData.storeShopTitleColor || ''"
                                        @update:color="(v: string) => updateField('storeShopTitleColor', v)"
                                    ></SelectColor>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">门店名称字号</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    :model-value="num(localData.storeShopTitleFontSize, 15)"
                                    :min="12"
                                    :max="24"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    @update:model-value="(v: number | number[]) => updateField('storeShopTitleFontSize', pick(v))"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">门店名称粗细</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="storeShopTitleFontBold"
                                @update:model-value="(v: string | number) => updateField('storeShopTitleFontBold', Number(v))"
                            >
                                <el-radio-button :value="400">常规</el-radio-button>
                                <el-radio-button :value="500">中粗</el-radio-button>
                                <el-radio-button :value="700">加粗</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">门店Logo圆角</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    :model-value="num(localData.storeLogoRadius, 50)"
                                    :max="50"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    @update:model-value="(v: number | number[]) => updateField('storeLogoRadius', pick(v))"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="dec-divider-line"></div>
                    <div class="dec-spread-title">
                        <div class="title">门店商品</div>
                    </div>

                    <div class="dec-edit-group" v-for="item in storeProductSwitchFields" :key="item.field">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ item.label }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="asBool(localData[item.field], true) ? 1 : 0"
                                @update:model-value="(v: string | number) => updateField(item.field, Number(v))"
                            >
                                <el-radio-button :value="1">显示</el-radio-button>
                                <el-radio-button :value="0">不显示</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>

                    <div v-if="showStoreProductName" class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">门店商品名称行数</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="storeProductNameRow"
                                @update:model-value="(v: string | number) => updateField('storeProductNameRow', Number(v))"
                            >
                                <el-radio-button :value="1">1行</el-radio-button>
                                <el-radio-button :value="2">2行</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">门店商品间距</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    :model-value="num(localData.storeProductGap, 8)"
                                    :max="30"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    @update:model-value="(v: number | number[]) => updateField('storeProductGap', pick(v))"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="dec-divider-line"></div>
                    <div class="dec-spread-title">
                        <div class="title">布局间距</div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">门店间距</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    :model-value="num(localData.storeGap, 10)"
                                    :max="30"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    @update:model-value="(v: number | number[]) => updateField('storeGap', pick(v))"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">门店上下内间距</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    :model-value="num(localData.storePaddingTopBottom, 12)"
                                    :max="30"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    @update:model-value="(v: number | number[]) => updateField('storePaddingTopBottom', pick(v))"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">门店左右内间距</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    :model-value="num(localData.storePaddingLeftRight, 12)"
                                    :max="30"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    @update:model-value="(v: number | number[]) => updateField('storePaddingLeftRight', pick(v))"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">门店圆角</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    :model-value="num(localData.storeCardRadius, 8)"
                                    :max="30"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    @update:model-value="(v: number | number[]) => updateField('storeCardRadius', pick(v))"
                                />
                            </div>
                        </div>
                    </div>
                </template>

                <template v-else>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">布局样式</div>
                            <div class="value">{{ layoutStyle === 1 ? "国内版" : "海外版" }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="layoutStyle"
                                @update:model-value="(v: string | number) => updateField('layoutStyle', Number(v))"
                            >
                                <el-radio-button :value="1">国内版</el-radio-button>
                                <el-radio-button :value="2">海外版</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">列表样式</div>
                            <div class="value">{{ listStyle === 1 ? "双列网格" : "单列列表" }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="listStyle"
                                @update:model-value="(v: string | number) => updateField('listStyle', Number(v))"
                            >
                                <el-radio-button :value="1">双列网格</el-radio-button>
                                <el-radio-button :value="2">单列列表</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">商品标题颜色</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group" need-refresh="true">
                                <div class="dec-color-button">
                                    <a class="dec-color-reset" @click="onResetColor('productTitleColor')">重置</a>
                                    <SelectColor
                                        :color="localData.productTitleColor || ''"
                                        @update:color="(v: string) => updateField('productTitleColor', v)"
                                    ></SelectColor>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">商品标题行数</div>
                            <div class="value">{{ productTitleLine === 1 ? "1行" : "2行" }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="productTitleLine"
                                @update:model-value="(v: string | number) => updateField('productTitleLine', Number(v))"
                            >
                                <el-radio-button :value="1">1行</el-radio-button>
                                <el-radio-button :value="2">2行</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>

                    <!-- 购物车按钮样式（仅双列） -->
                    <div v-if="listStyle === 1" class="dec-edit-group dec-edit-group-block">
                        <div class="dec-edit-group-title">
                            <div class="label">购物车按钮样式（双列）</div>
                            <div class="value">{{ buyBtnStyleLabel }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group buy-btn-style-group"
                                :model-value="buyBtnStyle"
                                @update:model-value="(v: string | number) => updateField('buyBtnStyle', Number(v))"
                            >
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

                    <!-- 仅双列：是否显示优惠券 -->
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">显示优惠券（双列）</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-switch
                                v-if="listStyle === 1"
                                :model-value="asBool(localData.showCoupon, true)"
                                @update:model-value="(v: boolean | string | number) => updateField('showCoupon', Boolean(v))"
                            />
                            <div v-else class="dec-edit-tip">单列模式不显示优惠券</div>
                        </div>
                    </div>

                    <!-- 仅单列：已售 / 店铺标签 / 加购按钮 -->
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">显示已售文字</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-switch
                                v-if="listStyle === 2"
                                :model-value="asBool(localData.showSales, true)"
                                @update:model-value="(v: boolean | string | number) => updateField('showSales', Boolean(v))"
                            />
                            <div v-else class="dec-edit-tip">双列模式不显示销量</div>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">显示店铺标签</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-switch
                                v-if="listStyle === 2"
                                :model-value="asBool(localData.showShopTag, true)"
                                @update:model-value="(v: boolean | string | number) => updateField('showShopTag', Boolean(v))"
                            />
                            <div v-else class="dec-edit-tip">双列模式不显示店铺标签</div>
                        </div>
                    </div>

                    <div v-if="listStyle === 2" class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">显示添加购物车（单列）</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-switch
                                :model-value="asBool(localData.showCartButton, true)"
                                @update:model-value="(v: boolean | string | number) => updateField('showCartButton', Boolean(v))"
                            />
                        </div>
                    </div>
                </template>
            </template>

            <template #extend>
                <div class="dec-edit-group" v-for="item in colorFields" :key="item.field">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ item.label }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group" need-refresh="true">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="onResetColor(item.field)">重置</a>
                                <SelectColor
                                    :color="(localData[item.field] as string) || ''"
                                    @update:color="(v: string) => onColorChange(item.field, v)"
                                ></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group" v-for="item in rangeFields" :key="item.field">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ item.label }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="num(localData[item.field], item.fallback)"
                                :max="item.max"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField(item.field, pick(v))"
                            />
                        </div>
                    </div>
                </div>
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
/**
 * 移动端搜索结果模块 - 设置组件
 * Tab：内容设置 / 扩展设置（配色与间距）
 */
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { SelectColor } from "@/components/select";
import { useModuleSettings } from "../../../composables/useModuleSettings";
import { SettingsTabs } from "../../pcHome/_shared";
import type { MobileSearchResultModule } from "./types";
import { mobileSearchResultDefaults } from "./types";

const { localData, updateField, updateFields, store } = useModuleSettings<MobileSearchResultModule>(mobileSearchResultDefaults);
const { t } = useI18n();

/** 门店搜索仅多门店版本可配；其它版本该开关不存在，字段缺省即商品搜索 */
const storeStyleAvailable = ref(false);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

// 历史草稿 / C 端接口曾把 boolean、number 存成字符串，统一归一化后再比较
const asBool = (v: unknown, fallback = true) => {
    if (v === undefined || v === null || v === "") return fallback;
    if (typeof v === "boolean") return v;
    if (typeof v === "number") return v !== 0;
    if (typeof v === "string") {
        const s = v.trim().toLowerCase();
        if (s === "true" || s === "1") return true;
        if (s === "false" || s === "0") return false;
    }
    return fallback;
};
const searchResultType = computed(() => Number(localData.value.searchResultType ?? 1));
/** 非多门店版本即便有历史值也按商品搜索呈现 */
const isStoreResult = computed(() => storeStyleAvailable.value && searchResultType.value === 2);
const storeShopTitleFontBold = computed(() => Number(localData.value.storeShopTitleFontBold ?? 700));
const storeProductNameRow = computed(() => Number(localData.value.storeProductNameRow ?? 1));
const layoutStyle = computed(() => Number(localData.value.layoutStyle ?? 1));
const listStyle = computed(() => Number(localData.value.listStyle ?? 1));
const productTitleLine = computed(() => Number(localData.value.productTitleLine ?? 2));
const buyBtnStyle = computed(() => Number(localData.value.buyBtnStyle ?? 0));

const buyBtnStyleLabel = computed(() => {
    const style = buyBtnStyle.value;
    if (style === 0) return "不显示";
    return `样式${["", "一", "二", "三", "四", "五", "六", "七", "八"][style] ?? ""}`;
});

const storeInfoSwitchFields: { field: keyof MobileSearchResultModule; label: string }[] = [
    { field: "storeShowRating", label: "显示门店评分" },
    { field: "storeShowMinOrder", label: "显示起送价" },
    { field: "storeShowDeliveryFee", label: "显示配送费" },
    { field: "storeShowDistance", label: "显示距离" },
    { field: "storeShowFollow", label: "显示关注按钮" }
];

const storeProductSwitchFields: { field: keyof MobileSearchResultModule; label: string }[] = [
    { field: "storeShowProductName", label: "显示门店商品名称" },
    { field: "storeShowProductPrice", label: "显示门店商品价格" }
];

const showStoreProductName = computed(() => asBool(localData.value.storeShowProductName, true));

const colorFields = computed<{ field: keyof MobileSearchResultModule; label: string }[]>(() => [
    { field: "backgroundColor", label: "模块背景色" },
    { field: "searchItemBackgroundColor", label: "搜索背景色" },
    { field: "itemBackgroundColor", label: isStoreResult.value ? "门店背景色" : "商品背景色" },
    { field: "themeColor", label: "主题色（价格、按钮、优惠券）" }
]);

// 上下、左右内间距分开配置；旧 padding 字段继续作为历史草稿兜底
const rangeFields = computed<{ field: keyof MobileSearchResultModule; label: string; max: number; fallback: number }[]>(() => [
    { field: "searchItemPaddingLeftRight", label: "搜索项左右内间距", max: 50, fallback: 10 },
    { field: "margin", label: isStoreResult.value ? "门店列表外边距" : "商品列表外边距", max: 50, fallback: 0 },
    { field: "paddingTopBottom", label: "模块上下内间距", max: 50, fallback: 10 },
    { field: "paddingLeftRight", label: "模块左右内间距", max: 50, fallback: 10 },
    { field: "productImageRadius", label: t("商品卡圆角"), max: 50, fallback: 4 }
]);

const onResetColor = (field: keyof MobileSearchResultModule) => {
    const value = (mobileSearchResultDefaults as Record<string, unknown>)[field as string] as string | undefined;
    onColorChange(field, value ?? "");
};

// 主题色联动价格颜色（一次性更新，避免防抖把后一次写入丢掉）
const onColorChange = (field: keyof MobileSearchResultModule, value: string) => {
    if (field === "themeColor") {
        updateFields({ themeColor: value, priceColor: value });
        return;
    }
    updateField(field, value);
};

// 兼容历史字符串(如 "10px")：读时抽取数字，回退默认
const num = (v: unknown, fallback = 0) => {
    if (v === "" || v === undefined || v === null) return fallback;
    const n = typeof v === "number" ? v : parseInt(String(v).replace(/[^\d.]/g, ""), 10);
    return Number.isFinite(n) ? n : fallback;
};

// 旧装修只有单一 padding：新字段不存在时分别继承，避免打开设置后视觉间距回到默认 10px。
const storedModuleData = (store.activeModuleData ?? {}) as Partial<MobileSearchResultModule>;
if (!Object.prototype.hasOwnProperty.call(storedModuleData, "paddingTopBottom")) {
    localData.value.paddingTopBottom = num(storedModuleData.padding, 10);
}
if (!Object.prototype.hasOwnProperty.call(storedModuleData, "paddingLeftRight")) {
    localData.value.paddingLeftRight = num(storedModuleData.padding, 10);
}
// el-slider 值取标量后原样写回（数字，含 0）
const pick = (v: number | number[]) => (Array.isArray(v) ? v[0] : v);
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
        width: 100%;
        padding: 0 8px;
    }
}

.dec-edit-tip {
    color: #999;
    font-size: 12px;
}

</style>
