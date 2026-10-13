<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>分组轮播Banner</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="title">添加分组Banner轮播大图</div>
                    </div>
                    <div class="dec-edit-group-desc">
                        <div class="pic-change-desc">提示：您可以通过拖拽进行图片排序，建议尺寸：1180*940（高清）或750*479（低分辨率）</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <ModulePicList
                            v-model:photos="localData.picList"
                            :is-multiple="true"
                            :show-text="true"
                            :show-desc="false"
                            title="添加图片"
                            @change="handlePicListChange"
                        />
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">一组显示的数量</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-input-group">
                            <div class="flex" style="justify-content: flex-end; flex-wrap: wrap">
                                <el-input-number
                                    :model-value="clampedNumber"
                                    :min="1"
                                    :max="3"
                                    style="width: 100px"
                                    @update:model-value="handleNumberChange"
                                />
                                <div v-if="(localData.number ?? 0) > 3" class="yellow" style="width: 100%; text-align: right; margin-top: 10px">
                                    最多只显示3个
                                </div>
                            </div>
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
 * PC 分组轮播 Banner 模块 - 设置组件
 */
import { computed, ref } from "vue";
import type { PcGroupBannerModule } from "./types";
import { pcGroupBannerDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { ModulePicList, SettingsTabs, ModuleContainerSettings } from "../_shared";

defineOptions({ name: "PcGroupBannerSettings" });

const { localData, updateField } = useModuleSettings<PcGroupBannerModule>(pcGroupBannerDefaults);

// ==================== Tab 配置 ====================
const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

const clampedNumber = computed(() => {
    const raw = Number(localData.value.number ?? 3);
    if (!Number.isFinite(raw)) return 3;
    return Math.min(3, Math.max(1, raw));
});

function handleNumberChange(v: number | undefined) {
    const raw = Number(v ?? 3);
    updateField("number", Number.isFinite(raw) ? Math.min(3, Math.max(1, raw)) : 3);
}

function handlePicListChange() {
    updateField("picList", Array.isArray(localData.value.picList) ? localData.value.picList : []);
}
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}
</style>
