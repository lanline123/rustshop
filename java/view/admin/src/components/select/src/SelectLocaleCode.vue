/**
 * @file 语言识别码选择器组件
 * @description 用于多语言配置场景选择语言代码
 * @features
 * - 下拉选择语言代码
 * - 支持搜索过滤
 * - 显示语言名称和代码
 */
<template>
    <div style="width: 100%;">
        <el-select
            v-model="modelValue"
            filterable
            :placeholder="$t('请选择语言识别码')"
            size="default"
            >
            <el-option
                v-for="item in codeList"
                :key="item.name"
                :label="item.name +'-'+ item.code"
                :value="item.code"
            />
        </el-select>
    </div>
</template>
<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { message } from "ant-design-vue";
import { getLocalesRelationConfig } from "@/api/multilingual/verbalAssociation";

const emit = defineEmits(['update:modelValue'])
const modelValue = defineModel<string>("modelValue", { type: String, default: '' });
const codeList = ref<any[]>([]);
const _getLocalesRelationConfig = async () => {
    try {
        const result = await getLocalesRelationConfig();
        codeList.value = result;
    } catch (error: any) {
        message.error(error.message);
    }
}
onMounted(() => {
    _getLocalesRelationConfig()
});
</script>

<style lang="less" scoped>

</style>
