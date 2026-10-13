/** * @file 商品SKU服务费配置组件 * @description 按规格设置服务费，支持比例模式和固定金额模式 */
<template>
    <div class="service-fee-config">
        <el-form ref="serviceFeeFormRef" :model="{ serviceFeeList }" label-width="100px">
            <el-form-item :label="$t('服务费明细')" v-if="serviceFeeList.length > 0" prop="serviceFeeList">
                <div class="lyecs-form-table">
                    <el-table :data="serviceFeeList" style="width: 100%">
                        <!-- 多规格：动态规格列 -->
                        <el-table-column v-for="(attrName, colIdx) in specColumnNames" :key="colIdx" :label="attrName" align="center">
                            <template #default="{ row }">
                                {{ row.attrs?.[colIdx]?.attrValue ?? "" }}
                            </template>
                        </el-table-column>
                        <!-- 单规格：规格名称列 -->
                        <el-table-column v-if="specColumnNames.length === 0" :label="$t('规格名称')" prop="skuName" min-width="200">
                            <template #default="{ row }">{{ row.skuName || $t('默认规格') }}</template>
                        </el-table-column>

                        <!-- 服务费用列 -->
                        <el-table-column prop="serviceFeeValue" :label="$t('服务费用')" min-width="240">
                            <template #header>
                                <div class="flex flex-align-center">
                                    <div>{{ $t('服务费用') }}</div>
                                    <el-tooltip effect="light" placement="top">
                                        <template #content>
                                            <div style="width: 330px; padding: 5px 10px; line-height: 1.8">
                                                <p>{{ $t('服务费是服务员完成本店订单服务后获得的报酬，由商家承担，全额支付给服务员，平台不抽取。') }}</p>
                                                <p>{{ $t('按规格设置：每个商品规格单独设置一个服务费；') }}</p>
                                                <p>{{ $t('两种模式（二选一）：按该规格一口价的百分比，或固定金额；') }}</p>
                                                <p>{{ $t('订单计算规则：用户所选规格的服务费 × 购买件数，多商品订单按各行累加。修改后仅对新订单生效。') }}</p>
                                                <p>{{ $t('周期购商品服务费按百分比进行扣除，取周期价进行计算。') }}</p>
                                            </div>
                                        </template>
                                        <el-icon style="margin-left: 5px; cursor: pointer" size="14" color="#999"><QuestionFilled /></el-icon>
                                    </el-tooltip>
                                    <BatchFeeEditor v-if="!disabled" ref="batchEditorRef" v-model:visible="batchEditorVisible" @confirm="onBatchConfirm" />
                                </div>
                            </template>
                            <template #default="{ row, $index }">
                                <el-form-item label="" :prop="`serviceFeeList.${$index}.serviceFeeValue`" :rules="getServiceFeeRules($index)">
                                    <TigInput v-model="row.serviceFeeValue" type="decimal" width="220px" :disabled="disabled" @change="emitChange">
                                        <template #prepend>
                                            <el-select v-model="row.serviceFeeType" style="width: 76px" :disabled="disabled" @change="onFeeTypeChange($index)">
                                                <el-option :label="$t('比例')" :value="FeeType.PERCENT" />
                                                <el-option :label="$t('固定')" :value="FeeType.FIXED" />
                                            </el-select>
                                        </template>
                                        <template #append>{{ row.serviceFeeType === FeeType.PERCENT ? "%" : config.dollarSign }}</template>
                                    </TigInput>
                                </el-form-item>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-form-item>
        </el-form>
    </div>
</template>

<script lang="ts" setup>
import { ref, shallowRef, computed, watch, nextTick } from "vue";
import { QuestionFilled } from "@element-plus/icons-vue";
import type { SkuServiceFeeState } from "@/types/finance/staffEarnings.d";
import { useConfigStore } from "@/store/config";
import BatchFeeEditor from "./BatchFeeEditor.vue";
import { FeeType } from "./serviceFeeConstants";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore().config;

interface ServiceFeeRow extends SkuServiceFeeState {
    attrs?: Array<{ attrName: string; attrValue: string }>;
}

interface ProductSkuItem {
    skuId?: number;
    productSkuId?: number;
    skuName?: string;
    specValueNames?: string;
    attrValueStr?: string;
    attrs?: Array<{ attrName: string; attrValue: string }>;
    serviceFeeType?: number;
    serviceFeeValue?: number;
}

const props = withDefaults(
    defineProps<{
        productList: ProductSkuItem[];
        modelValue: SkuServiceFeeState[];
        disabled: boolean;
        defaultFeeRate: number;
    }>(),
    {
        productList: () => [],
        modelValue: () => [],
        disabled: false,
        defaultFeeRate: 0
    }
);

const emit = defineEmits<{
    "update:modelValue": [value: SkuServiceFeeState[]];
}>();

const serviceFeeFormRef = shallowRef();
const serviceFeeList = ref<ServiceFeeRow[]>([]);
let isInternalUpdate = false;

const specColumnNames = computed(() => {
    const first = serviceFeeList.value[0];
    if (first?.attrs?.length) {
        return first.attrs.map((a) => a.attrName);
    }
    return [];
});

// --- 批量编辑 ---
const batchEditorVisible = ref(false);
const batchEditorRef = ref<InstanceType<typeof BatchFeeEditor>>();

const onBatchConfirm = (payload: { serviceFeeType: number; serviceFeeValue: number }) => {
    serviceFeeList.value.forEach((item) => {
        item.serviceFeeType = payload.serviceFeeType;
        item.serviceFeeValue = Number(payload.serviceFeeValue) || 0;
    });
    emitChange();
};

// --- 表单校验 ---
const getServiceFeeRules = (index: number) => [
    {
        validator: (_rule: any, value: any, callback: any) => {
            if (value === "" || value === null || value === undefined) {
                return callback(new Error(t('请输入服务费用')));
            }
            const num = Number(value);
            if (isNaN(num)) {
                return callback(new Error(t('请输入有效数字')));
            }
            const row = serviceFeeList.value[index];
            if (row?.serviceFeeType === FeeType.PERCENT) {
                if (num < 0 || num > 100) {
                    return callback(new Error(t('比例范围为 0~100%')));
                }
            } else if (num < 0) {
                return callback(new Error(t('金额不能小于 0')));
            }
            callback();
        },
        trigger: "change"
    }
];

const validateServiceFee = async () => {
    await serviceFeeFormRef.value?.validate();
};

// --- 数据同步 ---
const emitChange = () => {
    isInternalUpdate = true;
    emit(
        "update:modelValue",
        serviceFeeList.value.map((item) => ({
            skuId: item.skuId,
            skuName: item.skuName,
            serviceFeeType: item.serviceFeeType,
            serviceFeeValue: item.serviceFeeValue
        }))
    );
    nextTick(() => {
        isInternalUpdate = false;
    });
};

const resolveSkuId = (item: ProductSkuItem) => item.skuId ?? item.productSkuId ?? 0;
const resolveSkuName = (item: ProductSkuItem) => item.skuName || item.specValueNames || item.attrValueStr || t('默认规格');

const buildRow = (item: ProductSkuItem): ServiceFeeRow => {
    const skuId = resolveSkuId(item);
    const existing = skuId > 0
        ? props.modelValue?.find((f) => f.skuId === skuId)
        : (props.modelValue?.length === 1 && props.modelValue[0]?.skuId === 0 ? props.modelValue[0] : undefined);
    const hasDefault = props.defaultFeeRate > 0;
    return {
        skuId,
        skuName: resolveSkuName(item),
        attrs: item.attrs || [],
        serviceFeeType: existing?.serviceFeeType ?? item.serviceFeeType ?? (hasDefault ? FeeType.PERCENT : FeeType.FIXED),
        serviceFeeValue: existing?.serviceFeeValue ?? item.serviceFeeValue ?? (hasDefault ? props.defaultFeeRate : 0)
    };
};

const getStructureKey = (list: ProductSkuItem[]) => list.map((item) => resolveSkuId(item)).join(",");

let lastStructureKey = "";

const resetToDefaultRow = () => {
    if (serviceFeeList.value.length !== 1 || serviceFeeList.value[0].skuId !== 0) {
        const hasDefault = props.defaultFeeRate > 0;
        serviceFeeList.value = [
            {
                skuId: 0,
                skuName: t('默认规格'),
                serviceFeeType: hasDefault ? FeeType.PERCENT : FeeType.FIXED,
                serviceFeeValue: hasDefault ? props.defaultFeeRate : 0
            }
        ];
        emitChange();
    }
    lastStructureKey = "";
};

/** 三级回退匹配：skuId → 精确 attrs → 部分 attrs */
const findMatchInOldList = (item: ProductSkuItem, oldList: ServiceFeeRow[]): ServiceFeeRow | undefined => {
    const newSkuId = resolveSkuId(item);
    const newAttrs = item.attrs || [];

    if (newSkuId > 0) {
        const match = oldList.find((r) => r.skuId === newSkuId);
        if (match) return match;
    }

    if (newAttrs.length === 0 || newAttrs.every((a) => !a.attrValue)) return undefined;

    const exactMatch = oldList.find(
        (old) =>
            old.attrs?.length === newAttrs.length &&
            old.attrs!.every((a, i) => a.attrName === newAttrs[i].attrName && a.attrValue === newAttrs[i].attrValue)
    );
    if (exactMatch) return exactMatch;

    const partialMatch = oldList.find(
        (old) =>
            old.attrs &&
            old.attrs.length > 0 &&
            old.attrs.length < newAttrs.length &&
            old.attrs.every((oldAttr) =>
                newAttrs.some((n) => oldAttr.attrName === n.attrName && oldAttr.attrValue === n.attrValue)
            )
    );
    if (partialMatch) return partialMatch;

    return oldList.find(
        (old) =>
            old.attrs &&
            old.attrs.length > 0 &&
            old.attrs.length > newAttrs.length &&
            newAttrs.every((newAttr) =>
                old.attrs!.some((o) => o.attrName === newAttr.attrName && o.attrValue === newAttr.attrValue)
            )
    );
};

const rebuildServiceFeeList = (newList: ProductSkuItem[]) => {
    const oldList = serviceFeeList.value;
    serviceFeeList.value = newList.map((item) => {
        const match = findMatchInOldList(item, oldList);
        if (match) {
            return {
                skuId: resolveSkuId(item),
                skuName: resolveSkuName(item),
                attrs: item.attrs || [],
                serviceFeeType: match.serviceFeeType,
                serviceFeeValue: match.serviceFeeValue
            };
        }
        return buildRow(item);
    });
    lastStructureKey = getStructureKey(newList);
    emitChange();
};

const syncDisplayNames = (newList: ProductSkuItem[]) => {
    newList.forEach((item, i) => {
        const row = serviceFeeList.value[i];
        if (!row) return;
        row.skuName = resolveSkuName(item);
        const newAttrs = item.attrs || [];
        if (row.attrs && row.attrs.length === newAttrs.length) {
            newAttrs.forEach((a, j) => {
                row.attrs![j].attrName = a.attrName;
                row.attrs![j].attrValue = a.attrValue;
            });
        } else {
            row.attrs = newAttrs;
        }
    });
};

const syncModelToList = (newVal: SkuServiceFeeState[]) => {
    newVal.forEach((item) => {
        const target = serviceFeeList.value.find((row) => row.skuId === item.skuId);
        if (target) {
            target.serviceFeeType = item.serviceFeeType;
            target.serviceFeeValue = item.serviceFeeValue;
        }
    });
};

watch(
    () => props.productList,
    (newList) => {
        if (!newList || newList.length === 0) {
            resetToDefaultRow();
            return;
        }
        const newKey = getStructureKey(newList);
        if (newKey !== lastStructureKey) {
            rebuildServiceFeeList(newList);
        } else {
            syncDisplayNames(newList);
        }
    },
    { immediate: true, deep: true }
);

watch(
    () => props.modelValue,
    (newVal) => {
        if (isInternalUpdate) return;
        if (!newVal?.length || !serviceFeeList.value.length) return;
        syncModelToList(newVal);
    },
    { immediate: true }
);

const onFeeTypeChange = (index: number) => {
    serviceFeeList.value[index].serviceFeeValue = 0;
    emitChange();
};

defineExpose({ validateServiceFee });
</script>

<style lang="less" scoped>
.service-fee-config {
    .lyecs-form-table {
        width: 100%;
        :deep(.el-table__body .cell) {
            overflow: visible !important;
        }
    }

    :deep(.el-select) {
        min-width: 76px;
    }

    :deep(.el-input-group__append, .el-input-group__prepend) {
        padding: 0 10px;
    }
}
</style>
