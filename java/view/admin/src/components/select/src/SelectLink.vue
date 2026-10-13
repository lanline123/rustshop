/**
 * @file 链接选择器组件
 * @description 用于页面装修等场景选择跳转链接
 * @features
 * - 弹窗选择链接类型和目标
 * - 显示已选链接名称
 * - 支持清空选择
 * - 支持禁用状态
 */
<template>
    <DialogForm v-if="!disabled" :params="{ act: 'edit', id: 1, type: decorateType, currentValue: linkData || {}, excludeShopId }" isDrawer path="link/LinkSelect" style="width: 100%" :title="$t('选择链接')" width="700px" @okCallback="onSelect">
        <div class="input-with-clear">
            <TigInput
                v-model="linkName"
                :readonly="true"
                class="tig-link-select-input"
                clearable
                :placeholder="$t('请选择链接地址')"
                type="text"
                :validate-event="false"
            ></TigInput>
            <span class="el-icon-circle-close input-clear-icon" @click.stop="clear"> &#10754; </span>
        </div>
    </DialogForm>
    <div v-else class="input-with-clear">
        <TigInput
            v-model="linkName"
            :readonly="true"
            class="tig-link-select-input"
            clearable
            :placeholder="$t('请选择链接地址')"
            type="text"
            :validate-event="false"
            :disabled="disabled"
        ></TigInput>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from "vue";
import { DialogForm } from "@/components/dialog";
import type { LinkType } from "@/types/decorate/decorate";
const props = defineProps({
    disabled: { type: Boolean, default: false },
    decorateType: { type: String, default: "" },
    // 默认 true：装修选链不带 shopId；非装修入口传 false
    excludeShopId: { type: Boolean, default: true },
});
const linkData = defineModel<LinkType>("modelValue");
const linkName = computed(() => {
    const label = linkData.value?.label;
    if (!label) return "";
    const name = linkData.value?.name;
    // 仅当存在与 label 不同的具体目标名时才拼接，避免出现 "选择门店|选择门店" 或 "label|undefined"
    if (name && name !== label) {
        return label + "|" + name;
    }
    return label;
});
const onSelect = (e: LinkType) => {
    linkData.value = e;
};
const clear = () => {
    linkData.value = {};
};
</script>

<style lang="less" scoped>
.input-with-clear {
    position: relative;
    width: 100%;
    display: inline-block;
}

.input-clear-icon {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%) scale(1.2);
    cursor: pointer;
    color: #c0c4cc;
}
:deep(.el-input__inner){
    padding-right: 15px;
}

.input-clear-icon:hover {
    color: #909399;
}
</style>
