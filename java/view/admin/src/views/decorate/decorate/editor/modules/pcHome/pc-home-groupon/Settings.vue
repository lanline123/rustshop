<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>拼团</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <!-- 模板 -->
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">选择模板</div>
                        <div class="value">{{ styleLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con dec-radio-group--template-nowrap">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.style"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('style', Number(v))"
                        >
                            <el-radio-button :value="2">一行四个</el-radio-button>
                            <el-radio-button :value="3">一行五个</el-radio-button>
                            <el-radio-button :value="4">一行六个</el-radio-button>
                            <el-radio-button :value="6">横向滑动</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <!-- 拼团类型 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">拼团类型</div>
                        <div class="value">{{ groupTypeLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.groupType"
                            @update:model-value="(v: string | number | boolean | undefined) => handleGroupTypeChange(Number(v))"
                        >
                            <el-radio-button :value="1">普通拼团</el-radio-button>
                            <el-radio-button :value="2">阶梯拼团</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 选择活动 -->
                <div class="dec-edit-group">
                    <DialogForm
                        isDrawer
                        title="选择活动"
                        width="600px"
                        path="decorate/decorate/editor/modules/pcHome/_shared/groupon/components/SelectGrouponActivityDialog"
                        :params="{ selected: localData.grouponActivityItem || null, groupType: localData.groupType }"
                        @okCallback="handleSelectActivityOk"
                    >
                        <el-button type="primary">选择活动</el-button>
                    </DialogForm>
                </div>

                <!-- 已选活动 -->
                <div class="dec-edit-group dec-edit-group--activity-selected" v-if="selectedActivity">
                    <div class="groupon-activity-list">
                        <div class="groupon-activity">
                            {{ selectedActivity.name }}
                            <div class="btn-close" @click="removeActivity">
                                <el-icon :size="14">
                                    <CircleCloseFilled />
                                </el-icon>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <!-- 商品卡片样式 -->
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">商品卡片样式</div>
                        <div class="value">{{ cardStyleLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.cardStyle"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('cardStyle', Number(v))"
                        >
                            <el-radio-button :value="1">样式一</el-radio-button>
                            <el-radio-button :value="2">样式二</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 文本对齐 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">文本对齐</div>
                        <div class="value">{{ textAlignLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.textAlign"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('textAlign', Number(v))"
                        >
                            <el-radio-button :value="1">左对齐</el-radio-button>
                            <el-radio-button :value="2">居中对齐</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 显示商品名称 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">显示商品名称</div>
                        <div class="value">{{ showNameLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.showName"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('showName', Number(v))"
                        >
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 显示商品价格 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">显示商品价格</div>
                        <div class="value">{{ showPriceLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="localData.showPrice"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('showPrice', Number(v))"
                        >
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 商品边距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.goodsPadding ?? 0"
                                :max="50"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('goodsPadding', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <div class="dec-edit-title-box">
                    <div class="title">标题设置</div>
                </div>
                <!-- 标题设置 -->
                <ModuleTitleSettings
                    :model-value="localData.title"
                    enable-title-vertical-padding
                    hide-title-align
                    enable-more-type
                    countdown-label-placeholder="距结束"
                    @update:model-value="(v) => updateField('title', v)"
                />
            </template>

            <template #extend>
                <div class="dec-edit-title-box">
                    <div class="title">容器设置</div>
                </div>

                <!-- 背景颜色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">背景颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateField('backgroundColor', '#ffffff')">重置</a>
                                <SelectColor :color="localData.backgroundColor || '#ffffff'" @update:color="(v: string) => updateField('backgroundColor', v)" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 圆角 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">圆角</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.boxRadius ?? 0"
                                :max="30"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('boxRadius', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 内边距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">内边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.innerPadding ?? 0"
                                :max="30"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('innerPadding', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 左右边距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">左右边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.boxPadding ?? 0"
                                :max="30"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('boxPadding', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 上边距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">上边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.boxPaddingTop ?? 5"
                                :max="30"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('boxPaddingTop', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 下边距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">下边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.boxPaddingBottom ?? 5"
                                :max="30"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('boxPaddingBottom', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script setup lang="ts">
/**
 * PC 首页拼团模块 - 设置组件（移植自 Vort pc-home-groupon）
 * 说明：对齐前台 pcGroupon 页面的字段配置
 */
import { computed, ref } from "vue";
import { CircleCloseFilled } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { SelectColor } from "@/components/select";
import type { PcHomeGrouponModule } from "./types";
import { pcHomeGrouponDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { ModuleTitleSettings, SettingsTabs } from "../_shared";
import type { GrouponActivityItem } from "../_shared/groupon";

defineOptions({ name: "PcHomeGrouponSettings" });

const { localData, updateField, updateFields } = useModuleSettings<PcHomeGrouponModule>(pcHomeGrouponDefaults);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

const selectedActivity = computed(() => localData.value.grouponActivityItem);

const handleGroupTypeChange = (v: unknown) => {
    // 一次性更新，避免防抖 patch 被后一次调用覆盖
    updateFields({
        groupType: typeof v === "number" ? v : Number(v),
        // 切换类型时清除已选活动
        grouponActivityItem: null
    });
};

const handleSelectActivityOk = (payload?: GrouponActivityItem) => {
    if (payload) {
        updateField("grouponActivityItem", payload);
    }
};

const removeActivity = () => {
    updateField("grouponActivityItem", null);
};

// ==================== 文案显示 ====================
const styleLabels: Record<number, string> = {
    2: "一行四个",
    3: "一行五个",
    4: "一行六个",
    6: "横向滑动"
};
const groupTypeLabels: Record<number, string> = { 1: "普通拼团", 2: "阶梯拼团" };
const cardStyleLabels: Record<number, string> = { 1: "样式一", 2: "样式二" };
const textAlignLabels: Record<number, string> = { 1: "左对齐", 2: "居中对齐" };
const showLabels: Record<number, string> = { 0: "不显示", 1: "显示" };

const styleLabel = computed(() => styleLabels[localData.value.style ?? 4] || "一行六个");
const groupTypeLabel = computed(() => groupTypeLabels[localData.value.groupType ?? 1] || "普通拼团");
const cardStyleLabel = computed(() => cardStyleLabels[localData.value.cardStyle ?? 1] || "样式一");
const textAlignLabel = computed(() => textAlignLabels[localData.value.textAlign ?? 1] || "左对齐");
const showNameLabel = computed(() => showLabels[localData.value.showName ?? 1] || "显示");
const showPriceLabel = computed(() => showLabels[localData.value.showPrice ?? 1] || "显示");
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}

.dec-range-group {
    display: flex;
    align-items: center;
}

.groupon-activity-list {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .groupon-activity {
        width: 100%;
        padding: 0 10px;
        height: 25px;
        line-height: 25px;
        font-size: 12px;
        background-color: rgb(223 225 232 / 40%);
        border-radius: 2px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 10px;

        .btn-close {
            cursor: pointer;
            color: #fb0606;
            line-height: 1;
            display: inline-flex;
            align-items: center;
        }

        &:hover {
            background-color: rgb(223 225 232 / 60%);
        }
    }
}

/* 已选活动回显区：压缩上下留白（避免视觉“太高”） */
.dec-edit-group--activity-selected {
    padding-top: 6px;
    padding-bottom: 6px;
}

// 模板选择：保持单行显示，空间不足时横向滚动
.dec-radio-group--template-nowrap {
    :deep(.el-radio-group) {
        flex-wrap: nowrap;
    }
}
</style>
