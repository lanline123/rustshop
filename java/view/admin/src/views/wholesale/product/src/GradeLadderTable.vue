/** * @file 等级类型售价阶梯表 * @description el-table 实现：等级/售价列按等级跨阶梯行（span-method），操作列固定右侧；某等级可不配（留空=回落通用售价） */
<template>
    <div class="grade-ladder">
        <div v-if="gradeOptions.length === 0" class="empty-tip">{{ $t('无可用批发商等级') }}</div>
        <el-table v-else :data="tableRows" class="ladder-el-table" :span-method="spanMethod" :row-key="rowKey">
            <el-table-column :label="$t('批发商等级')" width="100" align="center" header-align="center">
                <template #default="{ row }">
                    <span>{{ row.gradeName }}</span>
                </template>
            </el-table-column>
            <el-table-column :label="$t('售价')" width="110" align="center" header-align="center">
                <template #default>
                    <span>{{ retailPrice != null ? Number(retailPrice).toFixed(2) : "0.00" }}</span>
                </template>
            </el-table-column>
            <el-table-column :label="$t('批发价')" min-width="480" align="center" header-align="center">
                <template #default="{ row }">
                    <span v-if="row.placeholder" class="gray">{{ $t('未配置（留空将回落到通用售价）') }}</span>
                    <div v-else class="ladder-inputs">
                        <div
                            class="input-with-tip"
                            :class="{ 'is-error': !!errors?.[row.gradeId]?.[row.ladderIndex]?.qty }"
                            style="width: 250px"
                            :data-error-key="`${errorKeyPrefix}:${row.gradeId}:${row.ladderIndex}:qty`"
                        >
                            <TigInput
                                v-model="row.item.qty"
                                type="integer"
                                :min="1"
                                allowEmptyDecimal
                                width="250px"
                                :placeholder="$t('请输入')"
                                @input="onFieldInput(row.gradeId, row.ladderIndex, 'qty')"
                                @blur="onFieldBlur"
                            >
                                <template #prepend>{{ $t('数量') }}</template>
                                <template #append>{{ $t('件及以上') }}</template>
                            </TigInput>
                            <div v-if="errors?.[row.gradeId]?.[row.ladderIndex]?.qty" class="ladder-error-tip">
                                {{ errors[row.gradeId]![row.ladderIndex]!.qty }}
                            </div>
                        </div>
                        <div
                            class="input-with-tip"
                            :class="{ 'is-error': !!errors?.[row.gradeId]?.[row.ladderIndex]?.value }"
                            :style="{ width: valueWidth }"
                            :data-error-key="`${errorKeyPrefix}:${row.gradeId}:${row.ladderIndex}:value`"
                        >
                            <TigInput
                                v-model="row.item.value"
                                type="decimal"
                                allowEmptyDecimal
                                :width="valueWidth"
                                :placeholder="unitPlaceholder"
                                @input="onFieldInput(row.gradeId, row.ladderIndex, 'value')"
                                @blur="onFieldBlur"
                            >
                                <template #append>{{ unitText }}</template>
                            </TigInput>
                            <div v-if="errors?.[row.gradeId]?.[row.ladderIndex]?.value" class="ladder-error-tip">
                                {{ errors[row.gradeId]![row.ladderIndex]!.value }}
                            </div>
                        </div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column :label="$t('操作')" width="120" align="center" header-align="center" fixed="right">
                <template #default="{ row }">
                    <el-button v-if="row.placeholder" type="primary" circle size="small" class="circle-btn" @click="addLadder(row.gradeId, row.gradeName)">
                        <el-icon><Plus /></el-icon>
                    </el-button>
                    <template v-else-if="row.ladderIndex === row.laddersLen - 1">
                        <el-button
                            v-if="row.laddersLen < 20"
                            type="primary"
                            circle
                            size="small"
                            class="circle-btn"
                            @click="addLadder(row.gradeId, row.gradeName)"
                        >
                            <el-icon><Plus /></el-icon>
                        </el-button>
                        <el-button type="danger" circle size="small" class="circle-btn" @click="removeLadder(row.gradeId, row.ladderIndex)">
                            <el-icon><Minus /></el-icon>
                        </el-button>
                    </template>
                    <span v-else class="dash">-</span>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>
<script setup lang="ts">
import { computed } from "vue";
import type { TableColumnCtx } from "element-plus";
import { Plus, Minus } from "@element-plus/icons-vue";
import type { Ladder, GradeLadder, LadderError } from "@/types/wholesale/price";
import type { WholesalerGradeVO } from "@/types/wholesale/grade";
import { useConfigStore } from "@/store/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const configStore = useConfigStore();
/** 货币符号取全局配置，不写死 */
const currencySymbol = computed(() => configStore.config.dollarSign || "¥");

const props = defineProps<{
    modelValue: GradeLadder[];
    gradeOptions: WholesalerGradeVO[];
    priceMode: "discount" | "fixed";
    retailPrice?: number;
    errors?: Record<number, (LadderError | undefined)[]>;
    /** 用于给输入框打 data-error-key（便于父级 scrollIntoView 定位），格式如 `grade:{skuId}` */
    errorKeyPrefix?: string;
}>();
const emit = defineEmits(["update:modelValue", "clear-error", "blur"]);

const getLadderItem = (gradeId: number, index: number) => props.modelValue.find((g) => g.gradeId === gradeId)?.ladders[index];

const onFieldInput = (gradeId: number, index: number, field: "qty" | "value") => {
    if (props.errors?.[gradeId]?.[index]?.[field]) emit("clear-error", { gradeId, index, field });
};

/** 输入框失焦时通知父组件做一次静默校验 */
const onFieldBlur = () => emit("blur");

const unitText = computed(() => (props.priceMode === "discount" ? t('折') : currencySymbol.value));
const unitPlaceholder = computed(() => (props.priceMode === "discount" ? t('折扣') : t('价格')));
/** 货币符号移到后缀后，折扣/价格输入框宽度统一 */
const valueWidth = "160px";

const getLadders = (gradeId: number) => props.modelValue.find((g) => g.gradeId === gradeId)?.ladders || [];

/** 扁平化行：每个等级若已配置则按阶梯逐行展开，未配置则占 1 行占位；等级/售价列用 rowspan 合并 */
interface GradeRow {
    gradeId: number;
    gradeName: string;
    rowspan: number;
    placeholder: boolean;
    ladderIndex: number;
    item: Ladder | null;
    laddersLen: number;
}
const tableRows = computed<GradeRow[]>(() => {
    const rows: GradeRow[] = [];
    for (const grade of props.gradeOptions) {
        const ladders = getLadders(grade.gradeId);
        if (ladders.length === 0) {
            rows.push({ gradeId: grade.gradeId, gradeName: grade.gradeName, rowspan: 1, placeholder: true, ladderIndex: -1, item: null, laddersLen: 0 });
        } else {
            ladders.forEach((item, i) => {
                rows.push({
                    gradeId: grade.gradeId,
                    gradeName: grade.gradeName,
                    rowspan: i === 0 ? ladders.length : 0,
                    placeholder: false,
                    ladderIndex: i,
                    item,
                    laddersLen: ladders.length
                });
            });
        }
    }
    return rows;
});

const rowKey = (row: GradeRow) => `${row.gradeId}-${row.ladderIndex}`;

/** 等级 / 售价列按等级跨行合并（rowspan=0 的行由 el-table 自动隐藏） */
const spanMethod = ({ rowIndex, columnIndex }: { columnIndex: number; rowIndex: number; row: GradeRow; column: TableColumnCtx<GradeRow> }) => {
    if (columnIndex === 0 || columnIndex === 1) {
        const r = tableRows.value[rowIndex];
        return r.rowspan > 0 ? { rowspan: r.rowspan, colspan: 1 } : { rowspan: 0, colspan: 0 };
    }
    return { rowspan: 1, colspan: 1 };
};

const addLadder = (gradeId: number, gradeName: string) => {
    const next = [...props.modelValue];
    let entry = next.find((g) => g.gradeId === gradeId);
    if (!entry) {
        entry = { gradeId, gradeName, ladders: [] };
        next.push(entry);
    }
    if (entry.ladders.length >= 20) return;
    // 新档不预填，让用户自己输入；校验已处理 undefined（!l.qty 兜底）
    entry.ladders.push({ qty: undefined, value: undefined } as unknown as Ladder);
    emit("update:modelValue", next);
};

const removeLadder = (gradeId: number, index: number) => {
    const next = [...props.modelValue];
    const entry = next.find((g) => g.gradeId === gradeId);
    if (!entry) return;
    entry.ladders.splice(index, 1);
    // 删空后移除整个等级条目，回落到「未配置」占位
    const filtered = entry.ladders.length === 0 ? next.filter((g) => g.gradeId !== gradeId) : next;
    emit("update:modelValue", filtered);
};
</script>
<style scoped lang="less">
.grade-ladder {
    .empty-tip {
        color: #909399;
        padding: 12px;
    }
}
.ladder-el-table {
    .gray {
        color: #909399;
    }
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
