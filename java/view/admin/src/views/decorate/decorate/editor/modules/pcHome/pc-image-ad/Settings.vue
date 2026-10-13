<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>图片广告</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">选择模板</div>
                        <div class="value">{{ picTypeLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" :model-value="localData.picType" @update:model-value="(v: string | number | boolean | undefined) => updateField('picType', Number(v))">
                            <el-tooltip effect="light" placement="bottom" content="一行一个" :show-after="100">
                                <el-radio-button :value="1"><i class="ico-decorate icon-dec-up2end"></i></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" content="轮播" :show-after="100">
                                <el-radio-button :value="2"><i class="ico-decorate icon-dec-carousel"></i></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" content="一行两个" :show-after="100">
                                <el-radio-button :value="5"><i class="ico-decorate icon-dec-cuberow"></i></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" content="一行三个" :show-after="100">
                                <el-radio-button :value="6"><i class="ico-decorate icon-dec-cuberow2"></i></el-radio-button>
                            </el-tooltip>
                            <el-tooltip effect="light" placement="bottom" content="一行四个" :show-after="100">
                                <el-radio-button :value="7"><i class="ico-decorate icon-dec-cuberow1"></i></el-radio-button>
                            </el-tooltip>
                        </el-radio-group>
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="title">添加图片</div>
                    </div>
                    <div class="dec-edit-group-desc">
                        <div class="pic-change-desc" v-if="localData.picType === 1">提示：您可以通过拖拽进行图片排序，单图占宽为100%，建议宽1500px，高度自适应</div>
                        <div class="pic-change-desc" v-if="localData.picType === 2">提示：您可以通过拖拽进行图片排序，宽不小于1500px（选择轮播时可开启通宽排版）</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <ModulePicList :is-multiple="true" v-model:photos="localData.picList" @change="handlePicListChange" />
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <template v-if="localData.picType === 2">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">指示器样式</div>
                            <div class="value">{{ indicatorLabel }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group class="dec-radio-group" :model-value="localData.picPageType" @update:model-value="(v: string | number | boolean | undefined) => updateField('picPageType', Number(v))">
                                <el-tooltip effect="light" placement="bottom" content="样式一" :show-after="100">
                                    <el-radio-button :value="1"><i class="ico-decorate icon-dec-indicator-1"></i></el-radio-button>
                                </el-tooltip>
                                <el-tooltip effect="light" placement="bottom" content="样式二" :show-after="100">
                                    <el-radio-button :value="2"><i class="ico-decorate icon-dec-indicator-2"></i></el-radio-button>
                                </el-tooltip>
                                <el-tooltip effect="light" placement="bottom" content="样式三" :show-after="100">
                                    <el-radio-button :value="3"><i class="ico-decorate icon-dec-indicator-3"></i></el-radio-button>
                                </el-tooltip>
                            </el-radio-group>
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">指示器颜色</div>
                            <div class="value"></div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group">
                                <div class="dec-color-button">
                                    <a class="dec-color-reset" @click="handleResetColor">重置</a>
                                    <SelectColor :color="localData.swiperPageColor || '#333333'" @update:color="(v: string) => updateField('swiperPageColor', v)" />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="dec-edit-group pic-change-desc">
                        <div class="dec-edit-group-title">
                            <div class="label">轮播一行显示</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-select-group">
                                <el-select :model-value="localData.swiperPreView || 1" style="width: 80px" @update:model-value="(v: number) => updateField('swiperPreView', Number(v))">
                                    <el-option :value="1" label="1个" />
                                    <el-option :value="2" label="2个" />
                                    <el-option :value="3" label="3个" />
                                    <el-option :value="4" label="4个" />
                                </el-select>
                            </div>
                        </div>
                    </div>
                </template>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">图片边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.imgPadding || 0"
                                :max="30"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('imgPadding', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">图片边角</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" :model-value="localData.picRadioStyle" @update:model-value="(v: string | number | boolean | undefined) => updateField('picRadioStyle', Number(v))">
                            <el-radio-button :value="1">直角</el-radio-button>
                            <el-radio-button :value="2">圆角</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <div class="dec-edit-title-box">
                    <div class="title">标题设置</div>
                </div>
                <ModuleTitleSettings :model-value="localData.title" @update:model-value="updateField('title', $event)" />
            </template>

            <template #extend>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">是否通宽排版</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" :model-value="localData.isFluxWidth" @update:model-value="(v: string | number | boolean | undefined) => updateField('isFluxWidth', Number(v))">
                            <el-radio-button :value="1">是</el-radio-button>
                            <el-radio-button :value="0">否</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <div class="dec-edit-title-box">
                    <div class="title">容器设置</div>
                </div>
                <ModuleContainerSettings
                    :model-value="localData.frame"
                    :hide-horizontal-spacing="localData.isFluxWidth === 1"
                    @update:model-value="updateField('frame', $event)"
                />
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script setup lang="ts">
/**
 * PC 图片广告模块 - 设置组件
 * 说明：精简 picType（不提供 3/4 横向滑动），与 Vort 保持一致
 */
import { computed, ref } from "vue";
import { SelectColor } from "@/components/select";
import type { PcImageAdModule } from "./types";
import { pcImageAdDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { ModuleTitleSettings, ModuleContainerSettings, SettingsTabs, ModulePicList } from "../_shared";

defineOptions({ name: "PcImageAdSettings" });

const { localData, updateField } = useModuleSettings<PcImageAdModule>(pcImageAdDefaults);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

function handleResetColor() {
    updateField("swiperPageColor", "#333333");
}

function handlePicListChange() {
    updateField("picList", localData.value.picList);
}

const picTypeLabels: Record<number, string> = {
    1: "一行一个",
    2: "轮播",
    5: "一行两个",
    6: "一行三个",
    7: "一行四个"
};

const indicatorLabels: Record<number, string> = {
    1: "样式一",
    2: "样式二",
    3: "样式三"
};

const picTypeLabel = computed(() => picTypeLabels[localData.value.picType ?? 1] || "");
const indicatorLabel = computed(() => indicatorLabels[localData.value.picPageType ?? 1] || "");
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}

.dec-radio-group {
    .ico-decorate {
        font-size: 18px;
    }
}
</style>
