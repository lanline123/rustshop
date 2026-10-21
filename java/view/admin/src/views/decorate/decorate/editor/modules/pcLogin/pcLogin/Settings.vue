<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t("登录页") }}</h3>
            <div class="dec-edit-desc">{{ $t("选择 PC 登录页模式并配置背景，保存发布后前台按此渲染") }}</div>
        </div>

        <div class="dec-edit-section-title">{{ $t("登录模式") }}</div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("页面模式") }}</div>
                <div class="value">{{ loginModeLabels[localData.loginMode ?? pcLoginDefaults.loginMode ?? 2] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.loginMode ?? pcLoginDefaults.loginMode"
                    @update:model-value="(v: string | number) => updateField('loginMode', Number(v) as 1 | 2)"
                >
                    <el-radio-button :value="1">{{ $t("经典版") }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t("简约版") }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <div class="dec-divider-line"></div>
        <div class="dec-edit-section-title">{{ $t("头部设置") }}</div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("头部高度") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localData.headerHeight ?? pcLoginDefaults.headerHeight"
                        :min="50"
                        :max="120"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateField('headerHeight', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("logo宽度") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localData.logoWidth ?? pcLoginDefaults.logoWidth"
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
                <div class="label">{{ $t("logo高度") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localData.logoHeight ?? pcLoginDefaults.logoHeight"
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

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("头部阴影") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-switch
                    :model-value="localData.headerShadow ?? pcLoginDefaults.headerShadow"
                    @update:model-value="(v: string | number | boolean) => updateField('headerShadow', Boolean(v))"
                />
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("头部欢迎语") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-switch
                    :model-value="localData.showWelcome ?? pcLoginDefaults.showWelcome"
                    @update:model-value="(v: string | number | boolean) => updateField('showWelcome', Boolean(v))"
                />
            </div>
        </div>

        <div class="dec-divider-line"></div>
        <div class="dec-edit-section-title">{{ $t("背景设置") }}</div>

        <div class="dec-edit-group bg-pic-group">
            <div class="bg-pic-row">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t("背景图") }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-pic-group">
                        <PicSelect :photo="localData.backgroundPic" @update:photo="(v: PcLoginModule['backgroundPic']) => updateField('backgroundPic', v)" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">{{ $t("建议高度不超过 580px、宽度不超过 1700px，可配合下方背景色使用") }}</div>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("背景色") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="updateField('backgroundColor', '')">{{ $t("重置") }}</a>
                        <SelectColor :color="localData.backgroundColor || ''" @update:color="(v: string) => updateField('backgroundColor', v)" />
                    </div>
                </div>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t("登录区高度") }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="Math.max(620, localData.warpHeight || pcLoginDefaults.warpHeight || 620)"
                        :min="620"
                        :max="800"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateField('warpHeight', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { PicSelect } from "@/components/decorate";
import { SelectColor } from "@/components/select";
import { useModuleSettings } from "../../../composables/useModuleSettings";
import type { PcLoginModule } from "./types";
import { pcLoginDefaults } from "./types";

const { t } = useI18n();
const { localData, updateField } = useModuleSettings<PcLoginModule>(pcLoginDefaults);

const loginModeLabels = computed<Record<number, string>>(() => ({ 1: t("经典版"), 2: t("简约版") }));
</script>

<style lang="scss" scoped>
.dec-edit-section-title {
    font-size: 14px;
    color: #323233;
    font-weight: 600;
    padding: 6px 16px;
}

.dec-edit-desc {
    color: #999;
    font-size: 12px;
    padding: 4px 16px 8px;
}

.pic-change-desc {
    color: #999;
    font-size: 12px;
    line-height: 18px;
}

.bg-pic-group {
    flex-direction: column;
    align-items: stretch;
}

.bg-pic-row {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-start;
    width: 100%;
}
</style>
