/**
 * @file 库存调整表单
 * @description 调整商品库存数量
 * @features
 * - 减少库存（不能超过当前库存）
 * - 增加库存（无上限）
 * - 添加调整备注
 */
<template>
    <div class="adjustment">
        <el-form v-if="!loading" ref="formRef" :model="formState" label-width="110px" name="form_in_modal">
            <el-form-item :label="$t('入库类型')">
                <el-radio-group v-model="formState.adjustType">
                    <el-radio v-for="item in typeList" :label="item.value">
                        <div class="radioBox">
                            <div class="label">{{ item.label }}</div>
                            <div class="tip">{{ item.tip }}</div>
                        </div>
                    </el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item
                :label="$t('调整数量')"
                prop="quantity"
                :rules="[
                    { required: true, message: '请输入变更库存数量' },
                    { pattern: /^-?\d+$/, message: '请输入有效的整数' }
                ]"
            >
                <el-input-number v-if="formState.adjustType === 1" v-model="formState.quantity" :max="props.currentProductStock" />
                <TigInput
                    v-else
                    v-model="formState.quantity"
                    @input="handleInput"
                    @keypress="validateNumberInput"
                />
            </el-form-item>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, shallowRef } from "vue";
import { FormState } from '@/types/product/inventoryAdjustment'
import { productStockAdjust } from '@/api/product/inventoryAdjustment'
import { message } from "ant-design-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    act: {
        type: String,
        default: 'add'
    },
    productId: {
        type: Number,
        default: null
    },
    skuId: {
        type: Number,
        default: null
    },
    // 库存数量
    currentProductStock: {
        type: Number,
        default: null
    }
})
const formRef = shallowRef()
const loading = ref(true)
const formState = ref<FormState>({
    adjustType: 1,
    quantity: 1,
})
const typeList = [
    {
        label: t('增量更新'),
        value: 1,
        tip: t('在现有库存上增加或减少配额数量，调整数量为负数表示减少')
    },
    {
        label: t('覆盖更新'),
        value: 0,
        tip: t('将原有库存数量进行覆盖，覆盖后更新为调整数量填写的库存数量')
    }
]

const handleInput = (value: string) => {
    // 移除非数字字符（不保留负号）
    const cleanedValue = value.replace(/[^\d]/g, '');
    formState.value.quantity = cleanedValue as any;
};


const validateNumberInput = (event: KeyboardEvent) => {
    // 允许数字、退格、删除、方向键等
    const allowedKeys = [
        'Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', 'Tab'
    ];

    const isNumber = /^[0-9]$/;
    const isAllowedKey = allowedKeys.includes(event.key);

    // 只允许数字和控制键，不允许负号
    if (!isNumber.test(event.key) && !isAllowedKey) {
        event.preventDefault();
    }
};

onMounted(()=>{
    loading.value = false
})
const onFormSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await productStockAdjust({...formState.value, productId: props.productId, skuId: props.skuId})
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
}

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
:deep(.el-radio-group){
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    gap: 30px;
    margin-bottom: 20px;
    .el-radio{
        margin-right: 0;
        display: flex;
        align-self: flex-start;
        .el-radio__input{
            //align-self: flex-start;
        }
        .el-radio__label{
            align-self: flex-start;
        }
        .tip{
            font-size: 13px;
            color: rgb(151, 151, 161);
        }
    }
}

.radioBox{
    display: flex;
    flex-direction: column;
    gap: 0;
}
</style>
