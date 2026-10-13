<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>优惠券</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <!-- 显示标题栏 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">显示标题栏</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-switch
                            :model-value="(localData.showTitle ?? 0) === 1"
                            @update:model-value="(v: string | number | boolean) => updateField('showTitle', v ? 1 : 0)"
                        />
                    </div>
                </div>

                <!-- 标题内容（仅显示标题栏时） -->
                <template v-if="localData.showTitle === 1">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">标题内容</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-input
                                :model-value="localData.title"
                                placeholder="请输入标题内容"
                                style="width: 240px"
                                @update:model-value="(v: string) => updateField('title', v)"
                            />
                        </div>
                    </div>

                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">描述内容</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-input
                                type="textarea"
                                :model-value="localData.desc"
                                placeholder="请输入描述内容, 字数不超过500"
                                :maxlength="500"
                                style="width: 240px"
                                @update:model-value="(v: string) => updateField('desc', v)"
                            />
                        </div>
                    </div>
                </template>
            </template>

            <template #extend>
                <div class="dec-edit-title-box">
                    <div class="title">容器设置</div>
                </div>

                <!-- 组件边距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">组件边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.itemPadding ?? 10"
                                :max="30"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('itemPadding', Array.isArray(v) ? v[0] : v)"
                            />
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
                                :model-value="localData.boxRadius ?? 8"
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

                <!-- 左右边距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">左右边距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.boxPadding ?? 10"
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
 * PC 优惠券模块 - 共享设置组件
 */
import { ref } from "vue";
import type { PcCouponModule } from "./types";
import { pcCouponDefaults } from "./defaults";
import { useModuleSettings } from "../../../../composables";
import { SettingsTabs } from "../index";

defineOptions({ name: "PcCouponSettings" });

const { localData, updateField } = useModuleSettings<PcCouponModule>(pcCouponDefaults);

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
</style>
