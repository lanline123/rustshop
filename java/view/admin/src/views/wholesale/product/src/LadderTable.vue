/** * @file 阶梯表（通用售价） * @description el-table 实现：售价列跨所有阶梯行（span-method），操作列固定右侧；「数量 N 件及以上 + 值 +
单位」同在批发价列；未配类型售价时至少 1 档，每组 ≤20 档 */
<template>
    <el-table :data="ladders" class="ladder-el-table" :span-method="spanMethod">
        <el-table-column :label="$t('售价')" width="135" align="center" header-align="center">
            <template #default>
                <span class="retail-price">{{ retailPrice != null ? Number(retailPrice).toFixed(2) : "0.00" }}</span>
            </template>
        </el-table-column>
        <el-table-column :label="$t('批发价')" align="center" header-align="center">
            <template #default="{ row, $index }">
                <div class="ladder-inputs">
                    <div
                        class="input-with-tip"
                        :class="{ 'is-error': !!errors?.[$index]?.qty }"
                        style="width: 250px"
                        :data-error-key="`${errorKeyPrefix}:${$index}:qty`"
                    >
                        <TigInput
                            v-model="row.qty"
                            type="integer"
                            :min="1"
                            allowEmptyDecimal
                            width="250px"
                            :placeholder="$t('请输入')"
                            @input="onFieldInput($index, 'qty')"
                            @blur="onFieldBlur"
                        >
                            <template #prepend>{{ $t('数量') }}</template>
                            <template #append>{{ $t('件及以上') }}</template>
                        </TigInput>
                        <div v-if="errors?.[$index]?.qty" class="ladder-error-tip">{{ errors[$index]!.qty }}</div>
                    </div>
                    <div
                        class="input-with-tip"
                        :class="{ 'is-error': !!errors?.[$index]?.value }"
                        :style="{ width: valueWidth }"
                        :data-error-key="`${errorKeyPrefix}:${$index}:value`"
                    >
                        <TigInput
                            v-model="row.value"
                            type="decimal"
                            allowEmptyDecimal
                            :width="valueWidth"
                            :placeholder="unitPlaceholder"
                            @input="onFieldInput($index, 'value')"
                            @blur="onFieldBlur"
                        >
                            <template #append>{{ unitText }}</template>
                        </TigInput>
                        <div v-if="errors?.[$index]?.value" class="ladder-error-tip">{{ errors[$index]!.value }}</div>
                    </div>
                </div>
            </template>
        </el-table-column>
        <el-table-column :label="$t('操作')" width="120" align="center" header-align="center" fixed="right">
            <template #default="{ $index }">
                <template v-if="$index === ladders.length - 1">
                    <el-button v-if="ladders.length < 20" type="primary" circle size="small" class="circle-btn" @click="addLadder($index)">
                        <el-icon><Plus /></el-icon>
                    </el-button>
                    <el-button v-if="ladders.length > 1" type="danger" circle size="small" class="circle-btn" @click="removeLadder($index)">
                        <el-icon><Minus /></el-icon>
                    </el-button>
                </template>
                <span v-else class="dash">-</span>
            </template>
        </el-table-column>
    </el-table>
</template>
<script setup lang="ts">
import { computed } from "vue";
import type { TableColumnCtx } from "element-plus";
import { Plus, Minus } from "@element-plus/icons-vue";
import type { Ladder, LadderError } from "@/types/wholesale/price";
import { useConfigStore } from "@/store/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const configStore = useConfigStore();

const props = defineProps<{
    modelValue: Ladder[];
    priceMode: "discount" | "fixed";
    retailPrice?: number;
    errors?: (LadderError | undefined)[];
    /** 用于给输入框打 data-error-key（便于父级 scrollIntoView 定位），格式如 `general:{skuId}` */
    errorKeyPrefix?: string;
}>();
const emit = defineEmits(["update:modelValue", "clear-error", "blur"]);

const onFieldInput = (index: number, field: "qty" | "value") => {
    if (props.errors?.[index]?.[field]) emit("clear-error", { index, field });
};

/** 输入框失焦时通知父组件做一次静默校验 */
const onFieldBlur = () => emit("blur");

/** 货币符号取全局配置，不写死 */
const currencySymbol = computed(() => configStore.config.dollarSign || "¥");

const ladders = computed<Ladder[]>({
    get: () => props.modelValue,
    set: (val) => emit("update:modelValue", val)
});

const unitText = computed(() => (props.priceMode === "discount" ? t('折') : currencySymbol.value));
const unitPlaceholder = computed(() => (props.priceMode === "discount" ? t('折扣') : t('价格')));
/** 货币符号移到后缀后，折扣/价格输入框宽度统一 */
const valueWidth = "160px";

/** 售价列跨所有阶梯行（rowspan），其余列正常 */
const spanMethod = ({ columnIndex, rowIndex }: { columnIndex: number; rowIndex: number; row: Ladder; column: TableColumnCtx<Ladder> }) => {
    if (columnIndex === 0) {
        return rowIndex === 0 ? { rowspan: ladders.value.length, colspan: 1 } : { rowspan: 0, colspan: 0 };
    }
    return { rowspan: 1, colspan: 1 };
};

const addLadder = (index: number) => {
    if (ladders.value.length >= 20) return;
    const next = [...ladders.value];
    // 新档不预填，让用户自己输入；校验已处理 undefined
    next.splice(index + 1, 0, { qty: undefined, value: undefined } as unknown as Ladder);
    emit("update:modelValue", next);
};

const removeLadder = (index: number) => {
    if (ladders.value.length <= 1) return;
    const next = [...ladders.value];
    next.splice(index, 1);
    emit("update:modelValue", next);
};
</script>
<style scoped lang="less">
.ladder-el-table {
    .ladder-inputs {
        display: flex;
        align-items: flex-start;
        gap: 8px;
    }
    .input-with-tip {
        display: flex;
        flex-direction: column;
        gap: 2px;
        text-align: left;
    }
    .ladder-error-tip {
        color: #f56c6c;
        font-size: 12px;
        line-height: 16px;
        word-break: break-all;
    }
    :deep(.input-with-tip.is-error .el-input__wrapper) {
        box-shadow: 0 0 0 1px #f56c6c inset !important;
    }
    .dash {
        color: #909399;
    }
    .circle-btn {
        width: 20px;
        height: 20px;
        min-height: 20px;
        padding: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border: none;
        :deep(.el-icon) {
            font-size: 12px;
        }
    }
}
</style>
