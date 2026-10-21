<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>商品</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-title">
                        <div class="label">展示样式</div>
                        <div class="value">{{ styleLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group flex-nowrap" :model-value="localData.style" @update:model-value="(v: string | number | boolean | undefined) => updateField('style', Number(v))">
                            <el-radio-button :value="1">一行四个</el-radio-button>
                            <el-radio-button :value="2">一行五个</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.goodsPadding ?? 5"
                                :max="30"
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
                    <div class="title">商品设置</div>
                </div>
                <CommonProductEdit v-model="productsModel" />

                <div class="dec-edit-title-box">
                    <div class="title">标题设置</div>
                </div>
                <ModuleTitleSettings :model-value="localData.title" @update:model-value="updateField('title', $event)" />
            </template>

            <template #extend>
                <div class="dec-edit-title-box">
                    <div class="title">容器设置</div>
                </div>
                <ModuleContainerSettings :model-value="localData.frame" show-inner-padding @update:model-value="updateField('frame', $event)" />
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script setup lang="ts">
/**
 * PC 商品模块 - 设置组件
 * 商品选择复用旧公共子组件 commonProduct/Edit.vue（字段结构与 Vort ProductSelectConfig 同构）；
 * 它对传入对象原地修改不 emit，这里 deep watch localData.products 统一经 updateField 同步 store
 */
import { computed, ref, watch } from "vue";
import CommonProductEdit from "@/views/decorate/decorate/src/modules/src/commonProduct/Edit.vue";
import type { ModuleProductsType } from "@/types/decorate/decorate.d";
import type { PcProductModule } from "./types";
import { pcProductDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { ModuleTitleSettings, ModuleContainerSettings, SettingsTabs } from "../_shared";

defineOptions({ name: "PcProductSettings" });

const { localData, updateField } = useModuleSettings<PcProductModule>(pcProductDefaults);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

// 旧 ModuleProductsType 与本模块 ProductSelectConfig 字段同构，仅 productCategoryId 类型口径不同，经 computed 转换
const productsModel = computed<ModuleProductsType>({
    get: () => (localData.value.products ?? {}) as ModuleProductsType,
    set: (v) => updateField("products", v)
});

watch(
    () => localData.value.products,
    () => {
        updateField("products", localData.value.products);
    },
    { deep: true }
);

const styleLabel = computed(() => {
    const v = Number(localData.value.style ?? 1);
    return v === 2 ? "一行五个" : "一行四个";
});
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}
</style>
