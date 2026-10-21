<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>公告</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">公告内容</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input
                            :model-value="localData.text"
                            style="width: 240px"
                            placeholder="请输入公告内容"
                            @update:model-value="(v: string) => updateField('text', v)"
                        />
                    </div>
                </div>

                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">公告图标</div>
                        <div class="value"><a class="dec-color-reset" @click="resetIcon">重置</a></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <PicList
                            :is-multiple="false"
                            :show-text="false"
                            :show-link="false"
                            :photo="localData.icoPic || {}"
                            @update:photo="(v: any) => updateField('icoPic', { picUrl: v?.picUrl || '', picThumb: v?.picThumb || '' })"
                        />
                    </div>
                </div>
            </template>

            <template #extend>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">文字颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateFrameField('textColor', '#555555')">重置</a>
                                <SelectColor :color="localData.frame?.textColor || '#555555'" @update:color="(v: string) => updateFrameField('textColor', v)" />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">组件背景</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateFrameField('itemBackgroundColor', '#ffffff')">重置</a>
                                <SelectColor
                                    :color="localData.frame?.itemBackgroundColor || '#ffffff'"
                                    @update:color="(v: string) => updateFrameField('itemBackgroundColor', v)"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">组件圆角</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.frame?.itemRadius || 0"
                                :max="50"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateFrameField('itemRadius', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">组件高度</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.frame?.itemHeight || 35"
                                :min="20"
                                :max="80"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateFrameField('itemHeight', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

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
 * PC 公告模块 - 设置组件
 * 图标选择复用 PicList 单图模式（图库弹窗），替代 Vort 的 GalleryPicker
 */
import { ref } from "vue";
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import type { PcNoticeModule } from "./types";
import type { ModuleFrame } from "../_shared/types";
import { pcNoticeDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { SettingsTabs, ModuleContainerSettings } from "../_shared";

defineOptions({ name: "PcNoticeSettings" });

const DEFAULT_ICON_URL = "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202508/1754895669bP3iJSUiXSBoRB1VQq.png";

const { localData, updateField } = useModuleSettings<PcNoticeModule>(pcNoticeDefaults);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

function resetIcon() {
    updateField("icoPic", { picUrl: DEFAULT_ICON_URL, picThumb: DEFAULT_ICON_URL });
}

function updateFrameField<K extends keyof ModuleFrame>(key: K, value: ModuleFrame[K]) {
    updateField("frame", { ...localData.value.frame, [key]: value });
}
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}
</style>
