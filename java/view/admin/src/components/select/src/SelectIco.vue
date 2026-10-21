/**
 * @file 图标选择器组件
 * @description 用于选择系统图标的弹窗选择器
 * @features
 * - 弹窗选择图标
 * - 图标预览显示
 * - 支持清空选择
 * - 动态加载自定义图标CSS
 */
<template>
    <DialogForm isDrawer path="ico/List" :params="{ selectedIco: ico }" style="width: 100%;" :title="$t('选择图标')" width="550px" @okCallback="onOk">
        <div class="input-with-clear">
            <TigInput v-model="ico" :readonly="true" class="tig-link-select-input" :placeholder="$t('请选择图标')" type="text">

                <template #prepend>
                    <span class="prepend-ico" :class="ico"></span>
                </template>
                <template #suffix>
                    <span class="el-icon-circle-close input-clear-icon" @click.stop="onClear">
                        &#10754;
                    </span>
                </template>
            </TigInput>
            
        </div>
    </DialogForm>
</template>
<script lang="ts" setup>
import { computed } from "vue"
import { DialogForm } from '@/components/dialog'
import { useConfigStore } from "@/store/config";
import { useLoadCss } from '@/utils/domUtils'

const configStore = useConfigStore();
if (configStore.config.icoDefinedCss) {
    useLoadCss(configStore.config.icoDefinedCss)
}
// 传值
const props = defineProps({
    modelValue: {
        type: String,
        default: ''
    }
});
const emit = defineEmits(['update:modelValue'])
const ico = computed({
    get: () => props.modelValue,
    set: (newValue) => emit('update:modelValue', newValue)
});
const onOk = (e:string) => {
    ico.value = e
}
const onClear = () => {
    ico.value = '';
}
</script>

<style lang="less" scoped>
.input-with-clear {
    position: relative;
    width: 100%;
    display: inline-block;
}

.input-clear-icon {
    cursor: pointer;
    color: #c0c4cc;
    font-size: 14px;
    padding: 0 5px;
}

.input-clear-icon:hover {
    color: #909399;
}

.input-with-clear :deep(.el-input-group__prepend) {
    padding: 0 12px;
}
.prepend-ico{width: 20px;text-align: center;}
</style>
