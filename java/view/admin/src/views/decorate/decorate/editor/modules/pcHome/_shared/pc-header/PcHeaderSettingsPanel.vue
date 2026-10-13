<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>PC头部设置</h3>
        </div>

        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">头部风格</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" :model-value="localData.styleType ?? 1" @update:model-value="handleStyleTypeChange">
                    <el-radio-button :value="1">简约风</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">底部分隔</div>
                <div class="value">{{ bottomDividerTypeLabel }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" :model-value="bottomDividerTypeModelValue" @update:model-value="handleBottomDividerTypeChange">
                    <el-radio-button :value="1">阴影</el-radio-button>
                    <el-radio-button :value="2">边框</el-radio-button>
                    <el-radio-button :value="0">不展示</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-edit-title-box">
            <div class="title">LOGO设置</div>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">LOGO图片</div>
                <div class="value"><a class="dec-color-reset" @click="resetLogo">默认logo</a></div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">提示：建议尺寸：320*120像素，推荐 PNG 透明底</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :is-multiple="false" :show-text="false" :show-link="false" :photo="logoPicDisplay" @update:photo="handleLogoPhotoChange" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">logo位置</div>
                <div class="value">{{ logoAlignLabel }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.logoAlign ?? 1"
                    @update:model-value="(v: RadioValue) => updateField('logoAlign', Number(v) as 1 | 2 | 3)"
                >
                    <el-radio-button :value="1">居左</el-radio-button>
                    <el-radio-button :value="2">居中</el-radio-button>
                    <el-radio-button :value="3">居右</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">logo宽度</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localData.logoWidth ?? 100"
                        :min="10"
                        :max="160"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateField('logoWidth', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">logo高度</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localData.logoHeight ?? 25"
                        :min="10"
                        :max="60"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateField('logoHeight', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>

        <div class="dec-divider-line"></div>

        <div class="dec-edit-title-box">
            <div class="title">购物车</div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">右侧购物车</div>
                <div class="value">{{ localData.rightCartShow === 0 ? "隐藏" : "显示" }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.rightCartShow ?? 1"
                    @update:model-value="(v: RadioValue) => updateField('rightCartShow', Number(v) as 0 | 1)"
                >
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">隐藏</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div v-if="(localData.rightCartShow ?? 1) !== 0" class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">购物车样式</div>
                <div class="value">{{ cartStyleLabel }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" :model-value="localData.cartStyle ?? 1" @update:model-value="handleCartStyleChange">
                    <el-radio-button :value="1">购物车</el-radio-button>
                    <el-radio-button :value="2">图片</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div v-if="(localData.rightCartShow ?? 1) !== 0 && (localData.cartStyle ?? 1) === 2" class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">添加图片</div>
                <div class="value"><a class="dec-color-reset" @click="resetCartPic">重置</a></div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :is-multiple="false" :show-text="false" :show-link="false" :photo="localData.cartPic || {}" @update:photo="handleCartPhotoChange" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">顶部菜单购物车</div>
                <div class="value">{{ localData.topMenuCartShow === 0 ? "隐藏" : "显示" }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.topMenuCartShow ?? 1"
                    @update:model-value="(v: RadioValue) => updateField('topMenuCartShow', Number(v) as 0 | 1)"
                >
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">隐藏</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-divider-line"></div>

        <div class="dec-edit-title-box">
            <div class="title">搜索条</div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">搜索条颜色</div>
                <div class="value">{{ (localData.searchColorType ?? 1) === 2 ? "自定义" : "跟随系统" }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.searchColorType ?? 1"
                    @update:model-value="(v: RadioValue) => updateField('searchColorType', Number(v) as 1 | 2)"
                >
                    <el-radio-button :value="1">系统</el-radio-button>
                    <el-radio-button :value="2">自定义</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div v-if="(localData.searchColorType ?? 1) === 2" class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">自定义颜色</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="updateField('searchColor', '')">重置</a>
                        <SelectColor :color="localData.searchColor || '#1a1a1a'" @update:color="(v: string) => updateField('searchColor', v)" />
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">所有商品分类</div>
                <div class="value">{{ localData.showCategory === 0 ? "关闭" : "开启" }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.showCategory ?? 1"
                    @update:model-value="(v: RadioValue) => updateField('showCategory', Number(v) as 0 | 1)"
                >
                    <el-radio-button :value="1">开启</el-radio-button>
                    <el-radio-button :value="0">关闭</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div v-if="(localData.showCategory ?? 1) !== 0" class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">首页分类</div>
                <div class="value">{{ showHomeCategoryLabel }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" :model-value="showHomeCategoryModelValue" @update:model-value="handleShowHomeCategoryChange">
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">隐藏</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div v-if="(localData.showCategory ?? 1) !== 0" class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">分类上间距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localData.categoryMarginTop ?? 0"
                        :min="0"
                        :max="60"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateField('categoryMarginTop', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">导航栏</div>
                <div class="value">{{ (localData.showMainNav ?? 1) === 0 ? "隐藏" : "显示" }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.showMainNav ?? 1"
                    @update:model-value="(v: RadioValue) => updateField('showMainNav', Number(v) as 0 | 1)"
                >
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">隐藏</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div v-if="(localData.showMainNav ?? 1) !== 0" class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">导航栏上间距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localData.mainNavPaddingTop ?? 0"
                        :min="0"
                        :max="60"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateField('mainNavPaddingTop', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>
        <div v-if="(localData.showMainNav ?? 1) !== 0" class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">导航栏下间距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localData.mainNavPaddingBottom ?? 0"
                        :min="0"
                        :max="60"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateField('mainNavPaddingBottom', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>

        <div class="dec-divider-line"></div>

        <div class="dec-edit-title-box">
            <div class="title">顶部提示条</div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">展示 benefit bar</div>
            </div>
            <div class="dec-edit-group-con">
                <el-switch :model-value="!!localData.showBenefitBar" @update:model-value="(v: SwitchValue) => updateField('showBenefitBar', v ? 1 : 0)" />
            </div>
        </div>

        <template v-if="!!localData.showBenefitBar">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">benefit bar 背景色</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="updateField('benefitBarBackgroundColor', '')">重置</a>
                            <SelectColor
                                :color="localData.benefitBarBackgroundColor || '#1a1a1a'"
                                @update:color="(v: string) => updateField('benefitBarBackgroundColor', v)"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </template>

        <template v-if="localData.styleType === 1">
            <div class="dec-divider-line"></div>

            <div class="dec-edit-title-box">
                <div class="title">顶部提示条（简约风）</div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">顶部菜单文字颜色</div>
                    <div class="value">{{ localData.topMenuTextColor ? "自定义" : "默认" }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="updateField('topMenuTextColor', '')">重置</a>
                            <SelectColor :color="localData.topMenuTextColor || '#ffffff'" @update:color="(v: string) => updateField('topMenuTextColor', v)" />
                        </div>
                    </div>
                </div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">顶部菜单文字悬浮颜色</div>
                    <div class="value">{{ localData.topMenuTextHoverColor ? "自定义" : "默认" }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="updateField('topMenuTextHoverColor', '')">重置</a>
                            <SelectColor
                                :color="localData.topMenuTextHoverColor || '#ffffff'"
                                @update:color="(v: string) => updateField('topMenuTextHoverColor', v)"
                            />
                        </div>
                    </div>
                </div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">站点导航</div>
                    <div class="value">{{ (localData.showTopSiteNav ?? 1) === 0 ? "隐藏" : "显示" }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group
                        class="dec-radio-group"
                        :model-value="localData.showTopSiteNav ?? 1"
                        @update:model-value="(v: RadioValue) => updateField('showTopSiteNav', Number(v) as 0 | 1)"
                    >
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">隐藏</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">会员信息</div>
                    <div class="value">{{ (localData.showTopMemberName ?? 1) === 0 ? "隐藏" : "显示" }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group
                        class="dec-radio-group"
                        :model-value="localData.showTopMemberName ?? 1"
                        @update:model-value="(v: RadioValue) => updateField('showTopMemberName', Number(v) as 0 | 1)"
                    >
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">隐藏</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">地区选择</div>
                    <div class="value">{{ (localData.showTopRegion ?? 1) === 0 ? "隐藏" : "显示" }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group
                        class="dec-radio-group"
                        :model-value="localData.showTopRegion ?? 1"
                        @update:model-value="(v: RadioValue) => updateField('showTopRegion', Number(v) as 0 | 1)"
                    >
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">隐藏</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-divider-line"></div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">显示右侧菜单（不含站点导航、购物车）</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-switch
                        :model-value="(localData.showTopMenuBar ?? 1) !== 0"
                        @update:model-value="(v: SwitchValue) => updateField('showTopMenuBar', v ? 1 : 0)"
                    />
                </div>
            </div>

            <template v-if="(localData.showTopMenuBar ?? 1) !== 0">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">右侧菜单</div>
                        <div class="value">{{ (localData.showTopUserMenu ?? 1) === 0 ? "隐藏" : "显示" }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.showTopUserMenu ?? 1"
                            @update:model-value="(v: RadioValue) => updateField('showTopUserMenu', Number(v) as 0 | 1)"
                        >
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">隐藏</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <template v-if="(localData.showTopUserMenu ?? 1) !== 0">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">站内消息</div>
                            <div class="value">{{ (localData.showTopMessage ?? 1) === 0 ? "隐藏" : "显示" }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="localData.showTopMessage ?? 1"
                                @update:model-value="(v: RadioValue) => updateField('showTopMessage', Number(v) as 0 | 1)"
                            >
                                <el-radio-button :value="1">显示</el-radio-button>
                                <el-radio-button :value="0">隐藏</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">我的订单</div>
                            <div class="value">{{ (localData.showTopOrders ?? 1) === 0 ? "隐藏" : "显示" }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="localData.showTopOrders ?? 1"
                                @update:model-value="(v: RadioValue) => updateField('showTopOrders', Number(v) as 0 | 1)"
                            >
                                <el-radio-button :value="1">显示</el-radio-button>
                                <el-radio-button :value="0">隐藏</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">会员中心</div>
                            <div class="value">{{ (localData.showTopMemberCenter ?? 1) === 0 ? "隐藏" : "显示" }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group
                                class="dec-radio-group"
                                :model-value="localData.showTopMemberCenter ?? 1"
                                @update:model-value="(v: RadioValue) => updateField('showTopMemberCenter', Number(v) as 0 | 1)"
                            >
                                <el-radio-button :value="1">显示</el-radio-button>
                                <el-radio-button :value="0">隐藏</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>

                    <div v-if="(localData.showTopMemberCenter ?? 1) !== 0" class="dec-edit-group dec-edit-group-block">
                        <div class="dec-edit-group-title">
                            <div class="label">会员中心菜单</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="pc-header-member-center-groups">
                                <div class="pc-header-member-center-group">
                                    <ModulePicList
                                        v-model:photos="memberCenterItems"
                                        :is-multiple="true"
                                        :show-image="false"
                                        :show-text="true"
                                        :show-desc="false"
                                        :show-link="true"
                                        :show-item-index="true"
                                        title="添加菜单"
                                        @change="handleMemberCenterItemsChange"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
            </template>
        </template>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
/**
 * PC 头部设置面板（编辑器侧，照搬 Vort-Admin pc-header，控件 Element 化）
 * 仅负责编辑 pageModule.pcHeader，随 PC 首页一起保存
 */
import { computed, onMounted, ref } from "vue";
import { storeToRefs } from "pinia";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import { getConfigBasic } from "@/api/setting/config";
import { isOverseas } from "@/utils/version";
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import ModulePicList from "../ModulePicList.vue";
import type { PcHeaderConfig, PicItem } from "./types";
import { pcHeaderDefaults } from "./types";

defineOptions({ name: "PcHeaderSettingsPanel" });

/** el-radio-group / el-switch @update:model-value 的入参类型 */
type RadioValue = string | number | boolean | undefined;
type SwitchValue = string | number | boolean;

const store = useDecorateEditorStore();
const { pageModule } = storeToRefs(store);

const localData = computed<PcHeaderConfig>(() => ({
    ...pcHeaderDefaults,
    ...(pageModule.value.pcHeader ?? {})
}));

function updateField<K extends keyof PcHeaderConfig>(key: K, value: PcHeaderConfig[K]) {
    store.updatePcHeader({ [key]: value } as Partial<PcHeaderConfig>);
}

// 直接绑定真实配置数组，避免映射副本导致标题和链接修改无法写回。
const memberCenterItems = computed({
    get: () => localData.value.memberCenterItems ?? [],
    set: (value: PcHeaderConfig["memberCenterItems"]) => updateField("memberCenterItems", value ?? [])
});

function handleStyleTypeChange(value: RadioValue) {
    updateField("styleType", (Number(value) === 2 ? 2 : 1) as 1 | 2);
}

const bottomDividerTypeModelValue = computed(() => {
    const raw = localData.value.bottomDividerType;
    if (raw === 0 || raw === 1 || raw === 2) return raw;
    // 保持历史默认表现：简约风=边框，跨境风=阴影
    return localData.value.styleType === 2 ? 1 : 2;
});

const bottomDividerTypeLabel = computed(() => {
    const labels: Record<number, string> = { 0: "不展示", 1: "阴影", 2: "边框" };
    return labels[bottomDividerTypeModelValue.value] || "";
});

function handleBottomDividerTypeChange(v: RadioValue) {
    const next = Number(v);
    updateField("bottomDividerType", (next === 1 ? 1 : next === 2 ? 2 : 0) as 0 | 1 | 2);
}

const logoAlignLabel = computed(() => {
    const labels: Record<number, string> = { 1: "居左", 2: "居中", 3: "居右" };
    return labels[localData.value.logoAlign ?? 1] || "";
});

const cartStyleLabel = computed(() => ((localData.value.cartStyle ?? 1) === 2 ? "图片" : "购物车"));

const showHomeCategoryModelValue = computed(() => localData.value.showHomeCategory ?? 1);
const showHomeCategoryLabel = computed(() => (showHomeCategoryModelValue.value === 0 ? "隐藏" : "显示"));

function handleShowHomeCategoryChange(v: RadioValue) {
    updateField("showHomeCategory", Number(v) as 0 | 1);
}

function handleLogoPhotoChange(v: PicItem | null | undefined) {
    updateField("logoPic", { picUrl: v?.picUrl || "", picThumb: v?.picThumb || "" });
}

// 默认 logo 取基础设置的商城 LOGO（标准版=商城LOGO，跨境=商城LOGO-黑底，字段均为 shopLogo）
const shopLogoUrl = ref("");

// 未单独配置头部 logo 时回显平台商城 LOGO，仅用于展示，不写入保存数据
const logoPicDisplay = computed<PicItem>(() => {
    const current = localData.value.logoPic;
    if (current?.picUrl || current?.picThumb) return current;
    if (!shopLogoUrl.value) return {};
    return { picUrl: shopLogoUrl.value, picThumb: shopLogoUrl.value };
});

async function getShopLogo() {
    try {
        const cfg = await getConfigBasic();
        shopLogoUrl.value = String(cfg?.shopLogo || "");
    } catch {
        shopLogoUrl.value = "";
    }
}

onMounted(getShopLogo);

function resetLogo() {
    // 清空后由预览与前台回退到平台商城 LOGO，避免把当前地址固化进装修数据
    updateField("logoPic", { picUrl: "", picThumb: "" });
}

function handleCartPhotoChange(v: PicItem | null | undefined) {
    // 选择图片时自动切换为「图片」样式，确保与 SVG 互斥
    updateField("cartStyle", 2);
    updateField("cartPic", { picUrl: v?.picUrl || "", picThumb: v?.picThumb || "" });
}

function handleCartStyleChange(v: RadioValue) {
    const next = Number(v) === 2 ? 2 : 1;
    updateField("cartStyle", next as 1 | 2);
    // 切回「购物车（SVG）」时清空图片，确保与图片互斥
    if (next === 1) {
        updateField("cartPic", { picUrl: "", picThumb: "" });
    }
}

function resetCartPic() {
    updateField("cartPic", { picUrl: "", picThumb: "" });
}

function handleBenefitNoticesChange() {
    // ModulePicList 原地 mutate group.notices 后抛 change，这里整体重新赋值触发 store 写回 + isDirty
    updateField("benefitNoticeGroups", [...(localData.value.benefitNoticeGroups ?? [])]);
}

function handleMemberCenterItemsChange(value: unknown[]) {
    // 使用 PicList 变更后的实际值写回，确保标题、链接和新增/删除均持久化。
    updateField("memberCenterItems", [...(value as PcHeaderConfig["memberCenterItems"])]);
}
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}

.pc-header-benefit-groups,
.pc-header-member-center-groups {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.pc-header-benefit-group,
.pc-header-member-center-group {
    padding: 10px 12px;
    border-radius: 2px;
    background-color: #fff;
    box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.2);
}

.pc-header-benefit-group__title {
    margin-bottom: 10px;
    font-size: 14px;
    color: #323233;
    font-weight: 500;
}

// 会员中心菜单复用跨境风顶部提示内容的分组容器，保持两处编辑体验一致。
</style>
