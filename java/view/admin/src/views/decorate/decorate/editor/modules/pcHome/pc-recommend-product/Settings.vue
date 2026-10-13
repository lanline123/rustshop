<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>推荐商品</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标题</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input
                            :model-value="localData.titleName"
                            placeholder="请输入标题（可不填）"
                            clearable
                            style="width: 240px"
                            @update:model-value="(v: string) => updateField('titleName', v)"
                        />
                    </div>
                </div>

                <div class="dec-divider-line"></div>

                <div class="dec-edit-title-box">
                    <div class="title">商品设置</div>
                </div>
                <CommonProductEdit v-model="localData.products" :number-max="50" />
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
 * PC 推荐商品模块 - 设置组件
 *
 * 商品池选择复用旧公共 commonProduct/Edit.vue（字段结构与 Vort ProductSelectConfig 同构）。
 * 该组件对 products 是原地 mutate（不 emit update:modelValue），localData 是副本，
 * 这里 deep watch 统一调 updateField 同步 store（与 _shared/ModulePicList 同思路）。
 */
import { ref, watch } from "vue";
import CommonProductEdit from "@/views/decorate/decorate/src/modules/src/commonProduct/Edit.vue";
import type { PcRecommendProductModule } from "./types";
import { pcRecommendProductDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { SettingsTabs, ModuleContainerSettings } from "../_shared";

defineOptions({ name: "PcRecommendProductSettings" });

const { localData, updateField } = useModuleSettings<PcRecommendProductModule>(pcRecommendProductDefaults);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

watch(
    () => localData.value.products,
    () => {
        updateField("products", localData.value.products);
    },
    { deep: true }
);
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}
</style>
