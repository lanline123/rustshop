<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>限时秒杀设置</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品间距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.goodsGap ?? 10"
                                :max="40"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('goodsGap', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <div class="dec-edit-title-box">
                    <div class="title">标题设置</div>
                </div>
                <ModuleTitleSettings
                    :model-value="localData.title"
                    enable-title-vertical-padding
                    hide-title-align
                    enable-more-type
                    countdown-label-placeholder="倒计时"
                    @update:model-value="(v) => updateField('title', v)"
                />
            </template>

            <template #extend>
                <div class="dec-edit-title-box">
                    <div class="title">外边距</div>
                </div>
                <ModuleContainerSettings :model-value="localData.frame" show-inner-padding @update:model-value="updateField('frame', $event)" />
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script setup lang="ts">
/**
 * PC 首页秒杀模块 - 设置组件
 * 说明：PC首页独有的秒杀模块，支持标题设置与容器设置
 */
import { ref } from "vue";
import type { PcHomeSeckillModule } from "./types";
import { pcHomeSeckillDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { SettingsTabs, ModuleContainerSettings, ModuleTitleSettings } from "../_shared";

defineOptions({ name: "PcHomeSeckillSettings" });

const { localData, updateField } = useModuleSettings<PcHomeSeckillModule>(pcHomeSeckillDefaults);

// ==================== Tab 配置 ====================
const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}

.dec-range-group {
    display: flex;
    align-items: center;
}
</style>
