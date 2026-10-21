<template>
    <perfect-scrollbar class="decorate-edit-con">
        <!-- 标题 -->
        <div class="dec-edit-title">
            <h3>商品分组</h3>
            <div class="l">个性轮播展示，可自动切换，效果突出醒目，默认一行2个</div>
        </div>

        <!-- Tab 切换 -->
        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <!-- 商品设置 Tab -->
            <template #product>
                <div class="dec-edit-title-box">
                    <div class="title">内容设置</div>
                </div>

                <!-- 商品来源已下移到 GroupListEdit 内部控制 -->
                <GroupListEdit v-model:productGroups="productGroupsModel" :is-multiple="true" title="添加一组" />

                <div class="dec-edit-title-box">
                    <div class="title">商品参数设置</div>
                </div>

                <!-- 商品样式 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品样式</div>
                        <div class="value">{{ cardStyleLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.productCardStyle"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('productCardStyle', Number(v))"
                        >
                            <el-radio-button :value="1">扁平</el-radio-button>
                            <el-radio-button :value="2">卡片</el-radio-button>
                            <el-radio-button :value="3">描边</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 列表样式 -->
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">展示样式</div>
                        <div class="value">{{ listStyleLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group flex-nowrap"
                            :model-value="localData.listStyle"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('listStyle', Number(v))"
                        >
                            <el-tooltip effect="light" placement="bottom" content="一行四个" :show-after="100">
                                <el-radio-button :value="4">一行四个</el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" content="一行五个" :show-after="100">
                                <el-radio-button :value="6">一行五个</el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" content="一行六个" :show-after="100">
                                <el-radio-button :value="7">一行六个</el-radio-button>
                            </el-tooltip>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 商品内边距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品内部边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.cardPadding || 0"
                                :max="10"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('cardPadding', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 商品圆角半径 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品圆角半径</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.cardRadius ?? 4"
                                :max="20"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('cardRadius', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <!-- 商品标题 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品标题</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.showProductName"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('showProductName', Number(v))"
                        >
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 标题行数（条件显示） -->
                <template v-if="localData.showProductName === 1">
                    <div class="dec-edit-group bg-grey">
                        <div class="dec-edit-group-title">
                            <div class="label">标题行数</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="localData.productNameRow"
                                @update:model-value="(v: string | number | boolean | undefined) => updateField('productNameRow', Number(v))"
                            >
                                <el-radio-button :value="1">一行</el-radio-button>
                                <el-radio-button :value="2">两行</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>

                    <div class="dec-edit-group bg-grey">
                        <div class="dec-edit-group-title">
                            <div class="label">标题粗细</div>
                            <div class="value">{{ localData.productNameWeight === 700 ? "加粗体" : "常规体" }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="localData.productNameWeight"
                                @update:model-value="(v: string | number | boolean | undefined) => updateField('productNameWeight', Number(v))"
                            >
                                <el-tooltip effect="light" placement="bottom" content="常规体" :show-after="100">
                                    <el-radio-button :value="400">常规</el-radio-button>
                                </el-tooltip>
                                <el-tooltip effect="light" placement="bottom" content="加粗体" :show-after="100">
                                    <el-radio-button :value="700">加粗</el-radio-button>
                                </el-tooltip>
                            </el-radio-group>
                        </div>
                    </div>

                    <div class="dec-edit-group bg-grey">
                        <div class="dec-edit-group-title">
                            <div class="label">标题大小</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    :model-value="localData.productNameSize || 16"
                                    :min="12"
                                    :max="16"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    @update:model-value="(v: number | number[]) => updateField('productNameSize', Array.isArray(v) ? v[0] : v)"
                                />
                            </div>
                        </div>
                    </div>
                </template>

                <!-- 商品价格 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品价格</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.showProductPrice"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('showProductPrice', Number(v))"
                        >
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 商品划线价 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品划线价</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.crossedOutPrice"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('crossedOutPrice', Number(v))"
                        >
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 已售 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">已售</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.showSales"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('showSales', Number(v))"
                        >
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 商品店铺 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品店铺</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.showShopTag"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('showShopTag', Number(v))"
                        >
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 购买按钮样式 -->
                <BuyBtnSettings :model-value="localData.buyBtnStyle" @update:model-value="updateField('buyBtnStyle', $event)" />
            </template>

            <!-- 扩展设置 Tab -->
            <template #extend>
                <div class="dec-edit-title-box">
                    <div class="title">商品颜色设置</div>
                </div>

                <!-- 商品间距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品间距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.productColor?.infoLeftRightPadding || 0"
                                :max="20"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => handleProductColorChange('infoLeftRightPadding', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 商品背景颜色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品背景颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="handleProductColorChange('backgroundColor', '')">重置</a>
                                <SelectColor
                                    :color="localData.productColor?.backgroundColor || '#ffffff'"
                                    @update:color="(v: string) => handleProductColorChange('backgroundColor', v)"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <!-- 商品标题颜色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品标题颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="handleProductColorChange('titleColor', '')">重置</a>
                                <SelectColor
                                    :color="localData.productColor?.titleColor || '#19191b'"
                                    @update:color="(v: string) => handleProductColorChange('titleColor', v)"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 商品价格颜色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品价格颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="handleProductColorChange('priceColor', '')">重置</a>
                                <SelectColor
                                    :color="localData.productColor?.priceColor || ''"
                                    @update:color="(v: string) => handleProductColorChange('priceColor', v)"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 商品划线价颜色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品划线价颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="handleProductColorChange('crossedOutPriceColor', '')">重置</a>
                                <SelectColor
                                    :color="localData.productColor?.crossedOutPriceColor || '#999999'"
                                    @update:color="(v: string) => handleProductColorChange('crossedOutPriceColor', v)"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-title-box">
                    <div class="title">分组标题设置</div>
                </div>

                <!-- 分组标题间距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">分组标题间距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.groupTitle?.marginLeftRight || 0"
                                :max="50"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => handleGroupTitleChange('marginLeftRight', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 分组标题 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">分组标题</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.groupTitle?.showTitle"
                            @update:model-value="(v: string | number | boolean | undefined) => handleGroupTitleChange('showTitle', Number(v))"
                        >
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 标题设置（条件显示） -->
                <template v-if="localData.groupTitle?.showTitle === 1">
                    <div class="dec-edit-group bg-grey">
                        <div class="dec-edit-group-title">
                            <div class="label">标题大小</div>
                            <div class="value">{{ titleSizeLabel }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="localData.groupTitle?.bigTitleFontSize"
                                @update:model-value="(v: string | number | boolean | undefined) => handleGroupTitleChange('bigTitleFontSize', Number(v))"
                            >
                                <el-tooltip effect="light" placement="bottom" content="小(14号)" :show-after="100">
                                    <el-radio-button :value="14">小</el-radio-button>
                                </el-tooltip>
                                <el-tooltip effect="light" placement="bottom" content="中(16号)" :show-after="100">
                                    <el-radio-button :value="16">中</el-radio-button>
                                </el-tooltip>
                                <el-tooltip effect="light" placement="bottom" content="大(18号)" :show-after="100">
                                    <el-radio-button :value="18">大</el-radio-button>
                                </el-tooltip>
                            </el-radio-group>
                        </div>
                    </div>

                    <div class="dec-edit-group bg-grey">
                        <div class="dec-edit-group-title">
                            <div class="label">标题默认颜色</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group">
                                <div class="dec-color-button">
                                    <a class="dec-color-reset" @click="handleGroupTitleChange('titleColor', '')">重置</a>
                                    <SelectColor
                                        :color="localData.groupTitle?.titleColor || '#444444'"
                                        @update:color="(v: string) => handleGroupTitleChange('titleColor', v)"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="dec-edit-group bg-grey">
                        <div class="dec-edit-group-title">
                            <div class="label">标题选中颜色</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group">
                                <div class="dec-color-button">
                                    <a class="dec-color-reset" @click="handleGroupTitleChange('activeTitleColor', '')">重置</a>
                                    <SelectColor
                                        :color="localData.groupTitle?.activeTitleColor || '#FF0F23'"
                                        @update:color="(v: string) => handleGroupTitleChange('activeTitleColor', v)"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="dec-edit-group bg-grey">
                        <div class="dec-edit-group-title">
                            <div class="label">标题选中背景色</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group">
                                <div class="dec-color-button">
                                    <a class="dec-color-reset" @click="handleGroupTitleChange('activeTitleBgColor', '')">重置</a>
                                    <SelectColor
                                        :color="localData.groupTitle?.activeTitleBgColor || '#ffebf1'"
                                        @update:color="(v: string) => handleGroupTitleChange('activeTitleBgColor', v)"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="dec-edit-group bg-grey">
                        <div class="dec-edit-group-title">
                            <div class="label">标题上下内边距</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    :model-value="localData.groupTitle?.titlePaddingTopBottom ?? 8"
                                    :max="30"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    @update:model-value="(v: number | number[]) => handleGroupTitleChange('titlePaddingTopBottom', Array.isArray(v) ? v[0] : v)"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="dec-edit-group bg-grey">
                        <div class="dec-edit-group-title">
                            <div class="label">标题左右内边距</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    :model-value="localData.groupTitle?.titlePaddingLeftRight ?? 12"
                                    :max="30"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    @update:model-value="(v: number | number[]) => handleGroupTitleChange('titlePaddingLeftRight', Array.isArray(v) ? v[0] : v)"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="dec-edit-group bg-grey">
                        <div class="dec-edit-group-title">
                            <div class="label">标题圆角</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    :model-value="localData.groupTitle?.titleRadius ?? 4"
                                    :max="20"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    @update:model-value="(v: number | number[]) => handleGroupTitleChange('titleRadius', Array.isArray(v) ? v[0] : v)"
                                />
                            </div>
                        </div>
                    </div>
                </template>

                <!-- 标题分割线颜色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标题分割线颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="handleGroupTitleChange('splitLineColor', 'transparent')">重置</a>
                                <SelectColor
                                    :color="localData.groupTitle?.splitLineColor || 'transparent'"
                                    @update:color="(v: string) => handleGroupTitleChange('splitLineColor', v)"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 模块样式设置 -->
                <ModuleStyleSettings :model-value="localData.moduleStyle" @update:model-value="updateField('moduleStyle', $event)" />

                <!-- 内容样式设置 -->
                <ContentStyleSettings :model-value="localData.contentStyle" @update:model-value="updateField('contentStyle', $event)" />
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
/**
 * 商品分组模块 - 设置组件（移植自 Vort pc-product-group）
 * 使用 SettingsTabs 实现商品设置/扩展设置的 Tab 切换
 */
import { ref, computed } from "vue";
import { SelectColor } from "@/components/select";
import type { ProductGroupModule, ProductColorConfig, GroupTitleConfig, ProductGroupItem } from "./types";
import { productGroupDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { SettingsTabs } from "../_shared";
import GroupListEdit from "./components/GroupListEdit.vue";
import BuyBtnSettings from "./components/BuyBtnSettings.vue";
import ModuleStyleSettings from "./components/ModuleStyleSettings.vue";
import ContentStyleSettings from "./components/ContentStyleSettings.vue";

defineOptions({ name: "PcProductGroupSettings" });

// 使用通用的模块设置 Composable
const { localData, updateField, syncToStore } = useModuleSettings<ProductGroupModule>(productGroupDefaults);

// ==================== Tab 配置 ====================
const activeTab = ref("product");
const settingsTabs = [
    { name: "product", label: "商品设置" },
    { name: "extend", label: "扩展设置" }
];

// ==================== 选项标签 ====================
const cardStyleLabels: Record<number, string> = {
    1: "扁平",
    2: "卡片",
    3: "描边"
};

const listStyleLabels: Record<number, string> = {
    4: "一行四个",
    6: "一行五个",
    7: "一行六个"
};

const titleSizeLabels: Record<number, string> = {
    14: "小(14号)",
    16: "中(16号)",
    18: "大(18号)"
};

// ==================== 计算属性 ====================
const cardStyleLabel = computed(() => cardStyleLabels[localData.value.productCardStyle ?? 1] || "");
const listStyleLabel = computed(() => listStyleLabels[localData.value.listStyle ?? 2] || "");
const titleSizeLabel = computed(() => titleSizeLabels[localData.value.groupTitle?.bigTitleFontSize ?? 14] || "");

// v-model:productGroups 的适配：类型上避免 undefined，并在 set 时同步回 store
const productGroupsModel = computed<ProductGroupItem[]>({
    get: () => localData.value.productGroups ?? [],
    set: (v) => {
        updateField("productGroups", v);
        syncToStore();
    }
});

// ==================== 事件处理 ====================

/**
 * 更新商品颜色配置
 */
function handleProductColorChange(key: keyof ProductColorConfig, value: unknown) {
    const productColor = { ...localData.value.productColor, [key]: value };
    updateField("productColor", productColor);
}

/**
 * 更新分组标题配置
 */
function handleGroupTitleChange(key: keyof GroupTitleConfig, value: unknown) {
    const groupTitle = { ...localData.value.groupTitle, [key]: value };
    updateField("groupTitle", groupTitle);
}
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}

.dec-range-group {
    display: flex;
    align-items: center;
}

.bg-grey {
    background-color: #f7f8fa;
}

.flex-nowrap {
    width: 100%;
}
</style>
