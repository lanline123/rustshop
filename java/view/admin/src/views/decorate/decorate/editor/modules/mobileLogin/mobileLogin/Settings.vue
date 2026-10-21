<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t("登录页") }}</h3>
            <div class="dec-edit-desc">{{ $t("自定义登录页颜色和内容间距") }}</div>
        </div>

        <div class="dec-edit-section-title">{{ $t("颜色") }}</div>
        <div class="dec-edit-group" v-for="item in colorFields" :key="item.field">
            <div class="dec-edit-group-title">
                <div class="label">{{ item.label }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="onResetColor(item.field)">{{ $t("重置") }}</a>
                        <SelectColor :color="(localData[item.field] as string) || ''" @update:color="(v: string) => updateField(item.field, v)"></SelectColor>
                    </div>
                </div>
            </div>
        </div>

        <div class="dec-divider-line"></div>
        <div class="dec-edit-section-title">{{ $t("边距") }}</div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("内容左右边距(px)") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="num(localData.contentPaddingX, 20)"
                        :max="60"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateField('contentPaddingX', pick(v))"
                    />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("内容顶部间距(px)") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="num(localData.contentPaddingTop, 0)"
                        :max="100"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateField('contentPaddingTop', pick(v))"
                    />
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { SelectColor } from "@/components/select";
import { useModuleSettings } from "../../../composables/useModuleSettings";
import type { MobileLoginModule } from "./types";
import { mobileLoginDefaults } from "./types";

const { localData, updateField } = useModuleSettings<MobileLoginModule>(mobileLoginDefaults);
const { t } = useI18n();

// 名称直接点明对应登录页哪个元素，避免运营看不懂
const colorFields = computed<{ field: keyof MobileLoginModule; label: string }[]>(() => [
    { field: "titleColor", label: t("标题颜色") },
    { field: "inputTextColor", label: t("输入框文字颜色") },
    { field: "placeholderColor", label: t("输入框提示文字颜色") },
    { field: "linkColor", label: t("链接文字颜色（立即注册、协议）") },
    { field: "buttonColor", label: t("登录按钮颜色") }
]);

// 重置取默认值：主题色字段（linkColor/buttonColor）默认即空 → 重置为空、跟随主题；
// 其余固定色字段默认为具体色值 → 重置回默认色，不会被清空
const onResetColor = (field: keyof MobileLoginModule) => {
    updateField(field, (mobileLoginDefaults as Record<string, string>)[field] ?? "");
};

// 边距按 px 数字存取：Admin 直接用 px，uniapp 侧再按 px*2 rpx 换算
// 兼容历史字符串(如 "40rpx")：读时抽取数字，回退默认
const num = (v: unknown, fallback = 0) => {
    if (v === "" || v === undefined || v === null) return fallback;
    const n = typeof v === "number" ? v : parseInt(String(v).replace(/[^\d.]/g, ""), 10);
    return Number.isFinite(n) ? n : fallback;
};
// el-slider 值取标量后原样写回（数字，含 0）
const pick = (v: number | number[]) => (Array.isArray(v) ? v[0] : v);
</script>

<style lang="scss" scoped>
.dec-edit-section-title {
    padding: 6px 16px;
    color: #323233;
    font-size: 14px;
    font-weight: 600;
}
</style>
