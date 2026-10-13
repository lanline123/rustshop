<template>
    <perfect-scrollbar class="decorate-edit-con">
        <!-- 标题 -->
        <div class="dec-edit-title">
            <h3>营销通栏</h3>
            <div class="dec-edit-desc">左侧分类导航 / 中间焦点区 / 右侧用户区</div>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <!-- ==================== 左侧分类导航 ==================== -->
                <div class="dec-edit-title-box">
                    <div class="title">左侧分类导航</div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">背景颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateLeftField('backgroundColor', '#ffffff')">重置</a>
                                <SelectColor
                                    :color="leftConfig.backgroundColor || '#ffffff'"
                                    @update:color="(v: string) => updateLeftField('backgroundColor', v)"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">文字颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateLeftField('textColor', '#333333')">重置</a>
                                <SelectColor :color="leftConfig.textColor || '#333333'" @update:color="(v: string) => updateLeftField('textColor', v)" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ==================== 中间焦点区 ==================== -->
                <div class="dec-edit-title-box">
                    <div class="title">中间焦点区</div>
                </div>

                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="title">轮播广告</div>
                    </div>
                    <div class="dec-edit-group-desc">
                        <div class="pic-change-desc">提示：建议尺寸：780*360（按实际主题可调整）</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <ModulePicList
                            v-model:photos="centerBanners"
                            :is-multiple="true"
                            :show-text="true"
                            :show-desc="false"
                            :show-link="true"
                            title="添加轮播"
                            @change="handleCenterBannersChange"
                        />
                    </div>
                </div>

                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="title">推荐卡位</div>
                    </div>
                    <div class="dec-edit-group-desc">
                        <div class="pic-change-desc">共 6 个卡位：1-2 位于轮播右侧，3-6 位于下方</div>
                    </div>

                    <div class="dec-edit-group-con">
                        <div v-for="(module, idx) in editableModules" :key="idx" class="dec-hero-card-editor">
                            <div class="dec-hero-card-editor__header">卡位 {{ idx + 1 }}</div>

                            <div class="dec-hero-card-editor__row">
                                <div class="dec-hero-card-editor__label">标题</div>
                                <div class="dec-hero-card-editor__con">
                                    <el-input
                                        :model-value="module.moduleTitle || ''"
                                        placeholder="请输入标题"
                                        clearable
                                        @update:model-value="(v: string) => updateCenterModule(idx, { moduleTitle: v })"
                                    />
                                </div>
                            </div>

                            <div class="dec-hero-card-editor__row">
                                <div class="dec-hero-card-editor__label">子标题</div>
                                <div class="dec-hero-card-editor__con">
                                    <el-input
                                        :model-value="module.moduleSubTitle || ''"
                                        placeholder="文案"
                                        clearable
                                        @update:model-value="(v: string) => updateCenterModule(idx, { moduleSubTitle: v })"
                                    />
                                </div>
                            </div>

                            <div class="dec-hero-card-editor__row">
                                <div class="dec-hero-card-editor__label">子标题背景</div>
                                <div class="dec-hero-card-editor__con">
                                    <div class="dec-color-group dec-color-group--dual">
                                        <div class="dec-color-button">
                                            <a class="dec-color-reset" @click="resetModuleSubTitleBg(idx)">重置</a>
                                            <SelectColor
                                                :color="module.moduleSubTitleBgColorA || DEFAULT_SUBTITLE_BG_COLOR_A"
                                                @update:color="(v: string) => updateCenterModule(idx, { moduleSubTitleBgColorA: v })"
                                            />
                                            <span class="dec-color-sep">-</span>
                                            <SelectColor
                                                :color="module.moduleSubTitleBgColorB || DEFAULT_SUBTITLE_BG_COLOR_B"
                                                @update:color="(v: string) => updateCenterModule(idx, { moduleSubTitleBgColorB: v })"
                                            />
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="dec-hero-card-editor__row">
                                <div class="dec-hero-card-editor__label">渐变方向</div>
                                <div class="dec-hero-card-editor__con">
                                    <el-radio-group
                                        class="dec-radio-group dec-radio-group--mini"
                                        :model-value="module.moduleSubTitleBgType || 'leftRight'"
                                        @update:model-value="
                                            (v: string | number | boolean | undefined) => updateCenterModule(idx, { moduleSubTitleBgType: v as GradientType })
                                        "
                                    >
                                        <el-radio-button value="leftRight">左右</el-radio-button>
                                        <el-radio-button value="upDown">上下</el-radio-button>
                                        <el-radio-button value="diagonal">斜向</el-radio-button>
                                    </el-radio-group>
                                </div>
                            </div>

                            <div class="dec-hero-card-editor__row">
                                <div class="dec-hero-card-editor__label">链接</div>
                                <div class="dec-hero-card-editor__con">
                                    <SelectLink
                                        :model-value="normalizeLink(module.link)"
                                        decorateType="pc"
                                        @update:model-value="(v: LinkItem) => updateCenterModule(idx, { link: v })"
                                    />
                                </div>
                            </div>

                            <div class="dec-hero-card-editor__row">
                                <div class="dec-hero-card-editor__label">显示价格</div>
                                <div class="dec-hero-card-editor__con">
                                    <el-switch
                                        :model-value="String(module.showPrice) !== '0'"
                                        @update:model-value="(v: string | number | boolean) => updateCenterModule(idx, { showPrice: v ? 1 : 0 })"
                                    />
                                </div>
                            </div>

                            <!-- 商品来源切换 -->
                            <div class="dec-hero-card-editor__row">
                                <div class="dec-hero-card-editor__label">商品来源</div>
                                <div class="dec-hero-card-editor__con">
                                    <el-radio-group
                                        class="dec-radio-group dec-radio-group--mini"
                                        :model-value="module.productSource || 'custom'"
                                        @update:model-value="
                                            (v: string | number | boolean | undefined) =>
                                                handleCenterModuleProductSourceChange(idx, v as 'custom' | 'productGroup')
                                        "
                                    >
                                        <el-radio-button value="custom">自定义商品</el-radio-button>
                                        <el-radio-button value="productGroup">商品分组</el-radio-button>
                                    </el-radio-group>
                                </div>
                            </div>

                            <!-- 自定义商品设置 -->
                            <div v-if="(module.productSource || 'custom') === 'custom'" class="dec-hero-products-edit">
                                <div class="dec-hero-products-title">选择商品</div>
                                <div class="dec-hero-products-list">
                                    <div v-for="(prod, pIdx) in (module.products || []).slice(0, 2)" :key="pIdx" class="dec-hero-product-item">
                                        <div class="dec-hero-product-item__left">
                                            <DialogForm
                                                isDrawer
                                                title="选择商品"
                                                width="600px"
                                                path="product/product/src/SelectProduct"
                                                :params="{ selectedIds: prod.productId ? [prod.productId] : [], isMultiple: false }"
                                                :data="{ moduleIndex: idx, productIndex: pIdx }"
                                                @okCallback="handleSelectProduct"
                                            >
                                                <div class="dec-hero-product-img-box" :title="prod.productName || '点击选择商品'">
                                                    <img
                                                        v-if="prod.pic?.picThumb || prod.pic?.picUrl"
                                                        class="dec-hero-product-img"
                                                        :src="productImage(prod.pic)"
                                                        alt=""
                                                        @error="handleImgError"
                                                    />
                                                    <div v-else class="dec-hero-product-img-empty">
                                                        <i class="ico-decorate icon-dec-jia" style="font-size: 18px; color: var(--tig-primary)"></i>
                                                    </div>
                                                    <div
                                                        v-if="prod.productId || prod.productName || prod.pic?.picThumb || prod.pic?.picUrl"
                                                        class="dec-hero-product-delete"
                                                        @click.stop.prevent="handleRemoveProduct(idx, pIdx)"
                                                    >
                                                        <i class="ico-decorate icon-dec-cha" style="font-size: 10px"></i>
                                                    </div>
                                                </div>
                                            </DialogForm>
                                        </div>
                                        <div class="dec-hero-product-item__right">
                                            <div class="dec-hero-product-row">
                                                <el-input
                                                    :model-value="prod.label || ''"
                                                    placeholder="标签文案"
                                                    size="small"
                                                    style="flex: 1"
                                                    @update:model-value="(v: string) => updateModuleProduct(idx, pIdx, { label: v })"
                                                />
                                            </div>

                                            <div class="dec-hero-product-row dec-hero-product-row-bg-color">
                                                <span class="dec-hero-product-label">标签背景</span>
                                                <div class="dec-color-group dec-color-group--dual dec-color-group--compact">
                                                    <div class="dec-color-button">
                                                        <a class="dec-color-reset" @click="resetLabelBg(idx, pIdx)">重置</a>
                                                        <SelectColor
                                                            :color="prod.labelBgColorA || DEFAULT_LABEL_BG_COLOR_A"
                                                            @update:color="(v: string) => updateModuleProduct(idx, pIdx, { labelBgColorA: v })"
                                                        />
                                                        <SelectColor
                                                            :color="prod.labelBgColorB || DEFAULT_LABEL_BG_COLOR_B"
                                                            @update:color="(v: string) => updateModuleProduct(idx, pIdx, { labelBgColorB: v })"
                                                        />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="dec-hero-products-tip">提示：点击图片选择商品，可自定义标签文案和价格</div>
                            </div>

                            <!-- 商品分组选择 -->
                            <div v-if="module.productSource === 'productGroup'" class="dec-hero-products-edit">
                                <div class="dec-hero-products-title">选择商品分组</div>
                                <div class="dec-hero-group-select">
                                    <SelectProductGroup
                                        :product-group-id="module.productGroupId ? Number(module.productGroupId) : undefined"
                                        @on-change="(id: number | string) => handleSelectProductGroup(idx, id)"
                                    />
                                    <div v-if="module.productGroupId" class="dec-hero-group-info">
                                        <div class="dec-hero-group-info__row">
                                            <span class="dec-hero-group-info__name">{{ module.productGroupName || "未命名分组" }}</span>
                                            <a class="dec-hero-group-info__clear" @click.stop.prevent="handleClearProductGroup(idx)">清除</a>
                                        </div>
                                    </div>
                                </div>
                                <div class="dec-hero-products-tip">提示：选择商品分组后，将自动获取该分组前 2 个商品展示</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <!-- ==================== 右侧会员卡 ==================== -->
                <div class="dec-edit-title-box">
                    <div class="title">右侧会员卡</div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">付费会员</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-switch
                            :model-value="rightConfig.premiumMember?.show === true"
                            @update:model-value="
                                (v: boolean | string | number) => updateRightField('premiumMember', { ...rightConfig.premiumMember, show: Boolean(v) })
                            "
                        />
                    </div>
                </div>

                <template v-if="rightConfig.premiumMember?.show === true">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">标题</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-input
                                :model-value="rightConfig.premiumMember?.title ?? '会员'"
                                placeholder="会员"
                                clearable
                                @update:model-value="(v: string) => updateRightField('premiumMember', { ...rightConfig.premiumMember, title: v })"
                            />
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">描述</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-input
                                :model-value="rightConfig.premiumMember?.description ?? ''"
                                placeholder="请输入描述"
                                clearable
                                @update:model-value="(v: string) => updateRightField('premiumMember', { ...rightConfig.premiumMember, description: v })"
                            />
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">跳转链接</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <SelectLink
                                :model-value="normalizeLink(rightConfig.premiumMember?.link)"
                                decorateType="pc"
                                @update:model-value="(v: LinkItem) => updateRightField('premiumMember', { ...rightConfig.premiumMember, link: v })"
                            />
                        </div>
                    </div>
                </template>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">未登录标题文字</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input
                            :model-value="rightConfig.user?.nickname || ''"
                            placeholder="如：欢迎来商城"
                            clearable
                            @update:model-value="(v: string) => updateRightUserField('nickname', v)"
                        />
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">背景渐变色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group dec-color-group--dual">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="resetUserCardGradient">重置</a>
                                <SelectColor
                                    :color="rightConfig.userCardGradientColorA || '#FFEDE1'"
                                    @update:color="(v: string) => updateRightField('userCardGradientColorA', v)"
                                />
                                <span class="dec-color-sep">-</span>
                                <SelectColor
                                    :color="rightConfig.userCardGradientColorB || '#FCF9F8'"
                                    @update:color="(v: string) => updateRightField('userCardGradientColorB', v)"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">渐变方向</div>
                        <div class="value">{{ gradientTypeLabels[rightConfig.userCardGradientType || "upDown"] }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="rightConfig.userCardGradientType || 'upDown'"
                            @update:model-value="(v: string | number | boolean | undefined) => updateRightField('userCardGradientType', v as GradientType)"
                        >
                            <el-radio-button value="upDown">上下</el-radio-button>
                            <el-radio-button value="leftRight">左右</el-radio-button>
                            <el-radio-button value="diagonal">斜向</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- ==================== 快捷入口区 ==================== -->
                <div class="dec-edit-title-box">
                    <div class="title">快捷入口区</div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">区块标题</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input
                            :model-value="rightConfig.shopsTitle || '店铺街'"
                            placeholder="请输入标题"
                            clearable
                            @update:model-value="(v: string) => updateRightField('shopsTitle', v)"
                        />
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">角标文字</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input
                            :model-value="rightConfig.shopsSubTitle || ''"
                            placeholder="如：HOT、推荐"
                            clearable
                            @update:model-value="(v: string) => updateRightField('shopsSubTitle', v)"
                        />
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">角标背景色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateRightField('shopsSubTitleBg', '#FF4E4E')">重置</a>
                                <SelectColor
                                    :color="rightConfig.shopsSubTitleBg || '#FF4E4E'"
                                    @update:color="(v: string) => updateRightField('shopsSubTitleBg', v)"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">查看更多文字</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input
                            :model-value="rightConfig.shopsMoreText || '查看全部'"
                            placeholder="如：查看全部、更多"
                            clearable
                            @update:model-value="(v: string) => updateRightField('shopsMoreText', v)"
                        />
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">查看更多链接</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <SelectLink
                            :model-value="normalizeLink(rightConfig.shopsMoreLink)"
                            decorateType="pc"
                            @update:model-value="(v: LinkItem) => updateRightField('shopsMoreLink', v)"
                        />
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">入口文字颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateRightField('shopsTextColor', '#a06a38')">重置</a>
                                <SelectColor
                                    :color="rightConfig.shopsTextColor || '#a06a38'"
                                    @update:color="(v: string) => updateRightField('shopsTextColor', v)"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="title">入口列表</div>
                    </div>
                    <div class="dec-edit-group-desc">
                        <div class="pic-change-desc">提示：固定 4 个入口，可拖拽排序</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <ModulePicList
                            v-model:photos="rightShortcuts"
                            :is-multiple="true"
                            :show-text="true"
                            :show-desc="false"
                            :show-link="true"
                            :max-count="4"
                            :show-add-button="false"
                            title="添加入口"
                            @change="handleRightShortcutsChange"
                        />
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <!-- ==================== 整体样式 ==================== -->
                <div class="dec-edit-title-box">
                    <div class="title">整体样式</div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">圆角</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="styleConfig.panelRadius ?? 8"
                                :max="30"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateStyleField('panelRadius', (Array.isArray(v) ? v[0] : v) ?? 8)"
                            />
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">背景色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateStyleField('panelBackgroundColor', '#ffffff')">重置</a>
                                <SelectColor
                                    :color="styleConfig.panelBackgroundColor || '#ffffff'"
                                    @update:color="(v: string) => updateStyleField('panelBackgroundColor', v)"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">内间距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="styleConfig.gap ?? 16"
                                :max="30"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateStyleField('gap', (Array.isArray(v) ? v[0] : v) ?? 16)"
                            />
                        </div>
                    </div>
                </div>
            </template>

            <template #extend>
                <div class="dec-edit-title-box">
                    <div class="title">外边距</div>
                </div>
                <ModuleContainerSettings :model-value="localData.frame" @update:model-value="updateField('frame', $event)" />
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script setup lang="ts">
/**
 * PC 营销通栏（pc-home-hero） - 设置组件
 * - 商品选择走 Tigshop 现成弹窗 product/product/src/SelectProduct
 * - 商品分组用 SelectProductGroup 内联下拉（选中后经分组详情接口补全名称）
 */
import { computed, ref } from "vue";
import { cloneDeep } from "lodash";
import { SelectColor, SelectLink } from "@/components/select";
import SelectProductGroup from "@/components/select/src/SelectProductGroup.vue";
import { DialogForm } from "@/components/dialog";
import type { LinkItem, PicListItem, GradientType } from "../_shared/types";
import type { PcHomeHeroCenterCard, PcHomeHeroModule, PcHomeHeroStyle } from "./types";
import { defaultPremiumMemberLink, pcHomeHeroDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { ModuleContainerSettings, ModulePicList, SettingsTabs } from "../_shared";
import { getProductList } from "@/api/product/product";
import { getProductGroup } from "@/api/product/productGroup";
import { productImage } from "@/utils/format";
import emptyImgBg from "@/assets/decorate/empty-img-bg.png";

defineOptions({ name: "PcHomeHeroSettings" });

const { localData, updateField } = useModuleSettings<PcHomeHeroModule>(pcHomeHeroDefaults);
if (!localData.value.right?.premiumMember?.link?.link) {
    updateField("right", {
        ...localData.value.right,
        premiumMember: {
            ...localData.value.right?.premiumMember,
            link: { ...defaultPremiumMemberLink }
        }
    });
}

// ==================== Tab 配置 ====================
const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

// ==================== 计算属性 ====================
const leftConfig = computed(() => localData.value.left || {});
const centerConfig = computed(() => localData.value.center || {});
const rightConfig = computed(() => localData.value.right || {});
const styleConfig = computed<PcHomeHeroStyle>(() => localData.value.style || {});

const centerBanners = computed<PicListItem[]>({
    get: () => (centerConfig.value.banners || []) as PicListItem[],
    set: (value) => updateCenterField("banners", value)
});

// 默认子标题背景渐变色
const DEFAULT_SUBTITLE_BG_COLOR_A = "#FF4E4E";
const DEFAULT_SUBTITLE_BG_COLOR_B = "#E1251B";

// 默认标签背景渐变色
const DEFAULT_LABEL_BG_COLOR_A = "#FF4E4E";
const DEFAULT_LABEL_BG_COLOR_B = "#E1251B";

// 统一管理的 6 个模块卡片
const editableModules = computed<PcHomeHeroCenterCard[]>(() => {
    const raw = (centerConfig.value.modules || []) as PcHomeHeroCenterCard[];
    const list = raw.map((it) => ({ ...it }));
    while (list.length < 6) {
        list.push({
            moduleTitle: "标题",
            moduleSubTitle: "子标题",
            moduleSubTitleBgColorA: DEFAULT_SUBTITLE_BG_COLOR_A,
            moduleSubTitleBgColorB: DEFAULT_SUBTITLE_BG_COLOR_B,
            moduleSubTitleBgType: "leftRight",
            showPrice: 1,
            products: [{}, {}]
        });
    }
    return list.slice(0, 6);
});

const rightShortcuts = computed<PicListItem[]>({
    get: () => (rightConfig.value.shortcuts || []) as PicListItem[],
    set: (value) => updateRightField("shortcuts", value)
});

// ==================== 更新方法 ====================
function updateLeftField<K extends keyof NonNullable<PcHomeHeroModule["left"]>>(key: K, value: NonNullable<PcHomeHeroModule["left"]>[K]) {
    updateField("left", {
        ...localData.value.left,
        [key]: value
    });
}

function updateCenterField<K extends keyof NonNullable<PcHomeHeroModule["center"]>>(key: K, value: NonNullable<PcHomeHeroModule["center"]>[K]) {
    updateField("center", {
        ...localData.value.center,
        [key]: value
    });
}

function updateRightField<K extends keyof NonNullable<PcHomeHeroModule["right"]>>(key: K, value: NonNullable<PcHomeHeroModule["right"]>[K]) {
    updateField("right", {
        ...localData.value.right,
        [key]: value
    });
}

function updateRightUserField<K extends keyof NonNullable<NonNullable<PcHomeHeroModule["right"]>["user"]>>(
    key: K,
    value: NonNullable<NonNullable<PcHomeHeroModule["right"]>["user"]>[K]
) {
    updateRightField("user", {
        ...localData.value.right?.user,
        [key]: value
    });
}

function updateStyleField<K extends keyof PcHomeHeroStyle>(key: K, value: PcHomeHeroStyle[K]) {
    updateField("style", {
        ...localData.value.style,
        [key]: value
    });
}

function handleCenterBannersChange() {
    updateCenterField(
        "banners",
        (centerBanners.value || []).map((it) => ({ ...it }))
    );
}

function handleRightShortcutsChange() {
    updateRightField(
        "shortcuts",
        (rightShortcuts.value || []).map((it) => ({ ...it }))
    );
}

function normalizeLink(raw?: unknown): LinkItem {
    if (!raw) return {};
    if (typeof raw === "string") return { link: raw, name: "" };
    return raw as LinkItem;
}

// 渐变方向标签
const gradientTypeLabels: Record<string, string> = {
    upDown: "上下",
    leftRight: "左右",
    diagonal: "斜向"
};

// 重置用户卡片渐变颜色
function resetUserCardGradient() {
    updateField("right", {
        ...localData.value.right,
        userCardGradientColorA: "#FFEDE1",
        userCardGradientColorB: "#FCF9F8"
    });
}

/** 切换商品来源时清空另一来源的数据，避免 Preview 短暂合并两套商品 */
function handleCenterModuleProductSourceChange(index: number, v: "custom" | "productGroup") {
    if (v === "productGroup") {
        updateCenterModule(index, {
            productSource: "productGroup",
            products: [{}, {}],
            productId: undefined,
            subPic: undefined,
            priceText: undefined
        });
        return;
    }
    const defaultModules = pcHomeHeroDefaults.center?.modules ?? [];
    const srcProducts = defaultModules[index]?.products;
    const products = srcProducts && srcProducts.length > 0 ? (cloneDeep(srcProducts) as NonNullable<PcHomeHeroCenterCard["products"]>) : [{}, {}];

    updateCenterModule(index, {
        productSource: "custom",
        productGroupId: undefined,
        productGroupName: undefined,
        productGroupNum: undefined,
        products,
        productId: undefined,
        subPic: undefined,
        priceText: undefined
    });
}

// 更新模块配置
function updateCenterModule(index: number, patch: Partial<PcHomeHeroCenterCard>) {
    const raw = ((localData.value.center?.modules || []) as PcHomeHeroCenterCard[]).map((it) => ({ ...it }));
    while (raw.length <= index) raw.push({});
    raw[index] = { ...raw[index], ...patch };
    updateCenterField("modules", raw);
}

// 更新模块内的商品
function updateModuleProduct(moduleIndex: number, productIndex: number, patch: Record<string, unknown>) {
    const modules = ((localData.value.center?.modules || []) as PcHomeHeroCenterCard[]).map((it) => ({ ...it }));
    while (modules.length <= moduleIndex) modules.push({});

    const currentModule = modules[moduleIndex]!;
    const products = (currentModule.products || []).map((p) => ({ ...p }));
    while (products.length <= productIndex) products.push({});

    products[productIndex] = { ...products[productIndex], ...patch };
    currentModule.products = products;

    updateCenterField("modules", modules);
}

// 重置子标题背景渐变色
function resetModuleSubTitleBg(index: number) {
    updateCenterModule(index, {
        moduleSubTitleBgColorA: DEFAULT_SUBTITLE_BG_COLOR_A,
        moduleSubTitleBgColorB: DEFAULT_SUBTITLE_BG_COLOR_B,
        moduleSubTitleBgType: "leftRight"
    });
}

// 重置标签背景渐变色
function resetLabelBg(moduleIndex: number, productIndex: number) {
    updateModuleProduct(moduleIndex, productIndex, {
        labelBgColorA: DEFAULT_LABEL_BG_COLOR_A,
        labelBgColorB: DEFAULT_LABEL_BG_COLOR_B
    });
}

// 选择商品后的处理（DialogForm okCallback：result 为选中 id 数组，data 为触发时传入的卡位定位）
async function handleSelectProduct(result: Array<string | number>, data: { moduleIndex: number; productIndex: number }) {
    if (!result || result.length === 0 || !data) return;

    const productId = result[0];

    try {
        const res = await getProductList({
            ids: String(productId),
            size: 1,
            page: 1
        });

        if (res.records && res.records.length > 0) {
            const product = res.records[0]!;
            updateModuleProduct(data.moduleIndex, data.productIndex, {
                productId: undefined,
                productSn: product.productSn || "",
                productName: product.productName,
                pic: {
                    picUrl: product.picThumb,
                    picThumb: product.picThumb
                },
                price: product.productPrice ? `¥${product.productPrice}` : ""
            });
        }
    } catch (error) {
        console.error("获取商品信息失败:", error);
    }
}

// 移除商品
function handleRemoveProduct(moduleIndex: number, productIndex: number) {
    updateModuleProduct(moduleIndex, productIndex, {
        productId: undefined,
        productSn: undefined,
        productName: undefined,
        pic: undefined,
        price: ""
    });
}

// 选择商品分组后：经分组详情接口补全名称（SelectProductGroup 的 onChange 仅回传 id）
async function handleSelectProductGroup(moduleIndex: number, groupId: number | string) {
    if (!groupId) {
        handleClearProductGroup(moduleIndex);
        return;
    }
    let groupName = "";
    try {
        const detail = (await getProductGroup("detail", { id: groupId })) as Record<string, unknown>;
        groupName = typeof detail?.productGroupName === "string" ? detail.productGroupName : "";
    } catch {
        groupName = "";
    }
    updateCenterModule(moduleIndex, {
        productGroupId: String(groupId),
        productGroupName: groupName
    });
}

// 清除商品分组
function handleClearProductGroup(moduleIndex: number) {
    updateCenterModule(moduleIndex, {
        productGroupId: undefined,
        productGroupName: undefined
    });
}

// 图片加载失败处理
function handleImgError(e: Event) {
    const img = e.target as HTMLImageElement | null;
    if (!img) return;
    if (img.src === emptyImgBg) return;
    img.onerror = null;
    img.src = emptyImgBg;
}
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}

.dec-hero-card-editor {
    padding: 16px;
    border: 1px solid #eee;
    border-radius: 4px;
    margin-bottom: 16px;
    background: #fff;

    &:last-child {
        margin-bottom: 0;
    }

    &__header {
        font-size: 14px;
        color: #333;
        font-weight: 700;
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #f0f0f0;
    }

    &__row {
        display: flex;
        align-items: center;
        margin-bottom: 16px;

        &:last-child {
            margin-bottom: 0;
        }
    }

    &__label {
        width: 70px;
        flex-shrink: 0;
        color: #666;
        font-size: 13px;
    }

    &__con {
        flex: 1;
        min-width: 0;
    }
}

.dec-hero-products-edit {
    margin-top: 16px;
    padding-top: 0;
    border-top: none;

    .dec-hero-products-title {
        font-size: 13px;
        color: #666;
        margin-bottom: 12px;
    }
}

.dec-hero-products-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}

.dec-hero-product-item {
    display: flex;
    gap: 10px;
    align-items: flex-start;
    padding: 10px;
    border: 1px solid #f5f5f5;
    border-radius: 4px;
    background: #fafafa;
    flex: 1;
    min-width: 200px;

    &__left {
        flex-shrink: 0;
    }

    &__right {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 8px;
    }
}

.dec-hero-product-img-box {
    position: relative;
    width: 60px;
    height: 60px;
    border-radius: 4px;
    cursor: pointer;
    background: #fff;
    border: 1px dashed #d9d9d9;
    transition: border-color 0.2s;

    &:hover {
        border-color: var(--tig-primary);

        .dec-hero-product-delete {
            opacity: 1;
        }
    }
}

.dec-hero-product-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 4px;
}

.dec-hero-product-img-empty {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fafafa;
    border-radius: 4px;
}

.dec-hero-product-delete {
    position: absolute;
    top: -6px;
    right: -6px;
    width: 18px;
    height: 18px;
    background: #ff4d4f;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    cursor: pointer;
    opacity: 0;
    transition: opacity 0.2s;
    z-index: 1;

    &:hover {
        background: #ff7875;
    }
}

.dec-hero-product-row {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 100%;
}

.dec-hero-product-row-bg-color {
    margin-top: -4px;
}

.dec-hero-products-tip {
    font-size: 12px;
    color: #999;
    margin-top: 8px;
}

.dec-hero-group-select {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.dec-hero-group-info {
    padding: 10px 12px;
    background: #f7f8fa;
    border-radius: 4px;
    font-size: 13px;
    cursor: pointer;
    border: 1px solid #eee;
    transition: all 0.2s;

    &:hover {
        border-color: var(--tig-primary);
    }

    &__row {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    &__name {
        color: #333;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    &__clear {
        color: #ff4d4f;
        cursor: pointer;
        font-size: 12px;
        flex-shrink: 0;

        &:hover {
            color: #ff7875;
        }
    }
}

.dec-color-group--dual {
    .dec-color-button {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    &.dec-color-group--compact {
        .dec-color-button {
            gap: 4px;
        }

        .dec-color-reset {
            font-size: 11px;
        }
    }
}

.dec-hero-product-label {
    font-size: 12px;
    color: #666;
    flex-shrink: 0;
    margin-right: 8px;
    padding-left: 4px;
}

.dec-radio-group--mini {
    :deep(.el-radio-button__inner) {
        height: auto;
        line-height: 1.5;
        padding: 5px 8px;
        font-size: 12px;
    }
}

.dec-color-sep {
    color: #999;
    margin: 0 2px;
}
</style>
