<template>
    <div class="settings-tabs">
        <!-- Tab 导航栏 -->
        <div class="settings-tabs-nav">
            <div v-for="tab in tabs" :key="tab.name" :class="['settings-tabs-item', { active: tab.name === modelValue }]" @click="handleTabClick(tab.name)">
                {{ tab.label }}
            </div>
        </div>
        <!-- Tab 内容区 -->
        <div class="settings-tabs-content">
            <slot :name="modelValue" />
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * 装修模块设置面板 Tab 切换组件（移植自 Vort-Admin）
 * 统一新版 PC 首页模块设置面板的"内容设置/扩展设置"双 Tab 风格
 */

export interface SettingsTabItem {
    /** Tab 的唯一标识 */
    name: string;
    /** Tab 显示的文本 */
    label: string;
}

defineOptions({ name: "SettingsTabs" });

const props = defineProps<{
    /** Tab 配置列表 */
    tabs: SettingsTabItem[];
    /** 当前激活的 Tab name */
    modelValue: string;
}>();

const emit = defineEmits<{
    (e: "update:modelValue", name: string): void;
    (e: "change", name: string): void;
}>();

function handleTabClick(name: string) {
    if (name !== props.modelValue) {
        emit("update:modelValue", name);
        emit("change", name);
    }
}
</script>

<style lang="less" scoped>
.settings-tabs {
    width: 100%;
}

.settings-tabs-nav {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 48px;
    border-bottom: 1px solid #dcdee0;
    background-color: #fff;
    box-shadow: rgba(93, 113, 127, 0.08) 0px 5px 10px 0px;
    margin-bottom: 10px;
}

.settings-tabs-item {
    line-height: 47px;
    height: 47px;
    max-width: 48%;
    width: 100%;
    text-align: center;
    cursor: pointer;
    color: #333;
    font-size: 14px;
    border-bottom: 2px solid transparent;
    transition: all 0.2s ease;

    &:hover {
        color: #155bd4;
    }

    &.active {
        color: #155bd4;
        font-weight: 500;
        border-bottom-color: #155bd4;
    }
}

.settings-tabs-content {
    padding: 0;
}
</style>
