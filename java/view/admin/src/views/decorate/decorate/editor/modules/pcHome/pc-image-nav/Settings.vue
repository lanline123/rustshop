<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>图文导航</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">显示模式</div>
                        <div class="value">{{ navTypeLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" :model-value="localData.navType" @update:model-value="(v: string | number | boolean | undefined) => updateField('navType', Number(v))">
                            <el-radio-button :value="1">固定显示</el-radio-button>
                            <el-radio-button :value="2">滑动分页</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">样式</div>
                        <div class="value">{{ navStyleLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" :model-value="localData.navStyle" @update:model-value="(v: string | number | boolean | undefined) => updateField('navStyle', Number(v))">
                            <el-radio-button :value="1">图文</el-radio-button>
                            <el-radio-button :value="2">仅图片</el-radio-button>
                            <el-radio-button :value="3">仅文字</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <div class="dec-edit-group pic-change-desc">
                    <div class="dec-edit-group-title">
                        <div class="label">一行显示</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-select-group">
                            <el-select :model-value="localData.rowNum" style="width: 80px" @update:model-value="(v: number) => updateField('rowNum', Number(v))">
                                <el-option :value="3" label="3个" />
                                <el-option :value="4" label="4个" />
                                <el-option :value="5" label="5个" />
                                <el-option :value="6" label="6个" />
                            </el-select>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group pic-change-desc">
                    <div class="dec-edit-group-title">
                        <div class="label">显示行数</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-select-group">
                            <el-select :model-value="localData.colNum" style="width: 80px" @update:model-value="(v: number) => updateField('colNum', Number(v))">
                                <el-option :value="1" label="1行" />
                                <el-option :value="2" label="2行" />
                                <el-option :value="3" label="3行" />
                            </el-select>
                        </div>
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="title">{{ picListSectionTitle }}</div>
                    </div>
                    <div class="dec-edit-group-desc">
                        <div class="pic-change-desc">{{ picListSectionTip }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <ModulePicList
                            v-model:photos="localData.picList"
                            :is-multiple="true"
                            :show-text="isNavStyleText"
                            :show-image="isNavStyleImage"
                            :show-desc="false"
                            :title="picListSectionTitle"
                            @change="handlePicListChange"
                        />
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">背景颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="handleResetBackgroundColor">重置</a>
                                <SelectColor :color="localData.backgroundColor || '#ffffff'" @update:color="(v: string) => updateField('backgroundColor', v)" />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">元素边距</div>
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

                <div v-if="isNavStyleImage" class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">图片边角</div>
                        <div class="value">{{ radioStyleLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" :model-value="localData.radioStyle" @update:model-value="(v: string | number | boolean | undefined) => updateField('radioStyle', Number(v))">
                            <el-radio-button :value="1">直角</el-radio-button>
                            <el-radio-button :value="2">圆角</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 指示器配置仅在滑动分页（navType=2）时有意义，固定显示无轮播不展示 -->
                <template v-if="Number(localData.navType) === 2">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">指示器样式</div>
                            <div class="value">{{ picPageTypeLabel }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group class="dec-radio-group flex-nowrap" :model-value="localData.picPageType" @update:model-value="(v: string | number | boolean | undefined) => updateField('picPageType', Number(v))">
                                <el-tooltip effect="light" placement="bottom" content="样式一" :show-after="100">
                                    <el-radio-button :value="1"><i class="ico-decorate icon-dec-indicator-1"></i></el-radio-button>
                                </el-tooltip>
                                <el-tooltip effect="light" placement="bottom" content="样式二" :show-after="100">
                                    <el-radio-button :value="2"><i class="ico-decorate icon-dec-indicator-2"></i></el-radio-button>
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
                                    <a class="dec-color-reset" @click="handleResetIndicatorColor">重置</a>
                                    <SelectColor :color="localData.swiperPageColor || '#333333'" @update:color="(v: string) => updateField('swiperPageColor', v)" />
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
            </template>

            <template #extend>
                <div class="dec-edit-title-box">
                    <div class="title">容器设置</div>
                </div>
                <ModuleContainerSettings :model-value="localData.frame" @update:model-value="updateField('frame', $event)" />
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script setup lang="ts">
/**
 * PC 图文导航模块 - 设置组件
 */
import { computed, ref } from "vue";
import { SelectColor } from "@/components/select";
import type { PcImageNavModule } from "./types";
import { pcImageNavDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { ModuleContainerSettings, SettingsTabs, ModulePicList } from "../_shared";

defineOptions({ name: "PcImageNavSettings" });

const { localData, updateField } = useModuleSettings<PcImageNavModule>(pcImageNavDefaults);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

const navTypeLabels: Record<number, string> = {
    1: "固定显示",
    2: "滑动分页"
};

const navStyleLabels: Record<number, string> = {
    1: "图片文字",
    2: "仅图片",
    3: "仅文字"
};

const radioStyleLabels: Record<number, string> = {
    1: "直角",
    2: "圆角"
};

const picPageTypeLabels: Record<number, string> = {
    1: "样式一",
    2: "样式二"
};

const navTypeLabel = computed(() => navTypeLabels[localData.value.navType ?? 1] || "");
const navStyleLabel = computed(() => navStyleLabels[localData.value.navStyle ?? 1] || "");
const radioStyleLabel = computed(() => radioStyleLabels[localData.value.radioStyle ?? 1] || "");
const picPageTypeLabel = computed(() => picPageTypeLabels[localData.value.picPageType ?? 1] || "");

const isNavStyleImage = computed(() => (localData.value.navStyle ?? 1) !== 3);
const isNavStyleText = computed(() => (localData.value.navStyle ?? 1) !== 2);

const picListSectionTitle = computed(() => {
    const style = localData.value.navStyle ?? 1;
    if (style === 3) return "添加导航项";
    return "添加图片";
});
const picListSectionTip = computed(() => {
    const style = localData.value.navStyle ?? 1;
    if (style === 3) return "提示：您可以通过拖拽进行排序";
    return "提示：您可以通过拖拽进行图片排序";
});

function handlePicListChange() {
    updateField("picList", localData.value.picList);
}

function handleResetBackgroundColor() {
    updateField("backgroundColor", "#ffffff");
}

function handleResetIndicatorColor() {
    updateField("swiperPageColor", "#333333");
}
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
