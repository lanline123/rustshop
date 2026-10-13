/** * @file 商品SKU配送费配置组件 * @description 按规格设置配送费，支持比例模式和固定金额模式 */
<template>
    <div class="delivery-fee-config">
        <el-form ref="deliveryFeeFormRef" :model="{ deliveryFeeList }" label-width="100px">
            <el-form-item label="配送费明细" v-if="deliveryFeeList.length > 0" prop="deliveryFeeList">
                <div class="lyecs-form-table">
                    <el-table border :data="deliveryFeeList" style="width: 100%">
                        <!-- 多规格：动态规格列 -->
                        <el-table-column v-for="(attrName, colIdx) in specColumnNames" :key="colIdx" :label="attrName" align="center">
                            <template #default="{ row }">
                                {{ row.attrs?.[colIdx]?.attrValue ?? "" }}
                            </template>
                        </el-table-column>
                        <!-- 单规格：规格名称列 -->
                        <el-table-column v-if="specColumnNames.length === 0" label="规格名称" prop="skuName" min-width="200">
                            <template #default="{ row }">{{ row.skuName || "默认规格" }}</template>
                        </el-table-column>

                        <!-- 配送费用列 -->
                        <el-table-column prop="deliveryFeeValue" label="配送费用" min-width="240">
                            <template #header>
                                <div class="flex flex-align-center">
                                    <div>配送费用</div>
                                    <el-tooltip effect="light" placement="top">
                                        <template #content>
                                            <div style="width: 330px; padding: 5px 10px; line-height: 1.8">
                                                <p>配送费是配送员完成本店订单配送后获得的报酬，由商家承担，全额支付给配送员，平台不抽取。</p>
                                                <p>按规格设置：每个商品规格单独设置一个配送费；</p>
                                                <p>两种模式（二选一）：按该规格一口价的百分比，或固定金额；</p>
                                                <p>订单计算规则：用户所选规格的配送费 × 购买件数，多商品订单按各行累加。修改后仅对新订单生效。</p>
                                            </div>
                                        </template>
                                        <el-icon style="margin-left: 5px; cursor: pointer" size="14" color="#999"><QuestionFilled /></el-icon>
                                    </el-tooltip>
                                    <BatchFeeEditor
                                        v-if="!disabled"
                                        ref="batchEditorRef"
                                        v-model:visible="batchEditorVisible"
                                        feeLabel="配送费用"
                                        @confirm="onBatchConfirm"
                                    />
                                </div>
                            </template>
                            <template #default="{ row, $index }">
                                <el-form-item label="" :prop="`deliveryFeeList.${$index}.deliveryFeeValue`" :rules="getDeliveryFeeRules($index)">
                                    <TigInput v-model="row.deliveryFeeValue" type="decimal" width="220px" :disabled="disabled" @change="emitChange">
                                        <template #prepend>
                                            <el-select v-model="row.deliveryFeeType" style="width: 76px" :disabled="disabled" @change="onFeeTypeChange($index)">
                                                <el-option label="比例" :value="FeeType.PERCENT" />
                                                <el-option label="固定" :value="FeeType.FIXED" />
                                            </el-select>
                                        </template>
                                        <template #append>{{ row.deliveryFeeType === FeeType.PERCENT ? "%" : config.dollarSign }}</template>
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
import type { SkuDeliveryFeeState } from "@/types/finance/staffEarnings.d";
import { useConfigStore } from "@/store/config";
import BatchFeeEditor from "./BatchFeeEditor.vue";
import { FeeType } from "./serviceFeeConstants";

const config = useConfigStore().config;

interface DeliveryFeeRow extends SkuDeliveryFeeState {
    attrs?: Array<{ attrName: string; attrValue: string }>;
}

interface ProductSkuItem {
    skuId?: number;
    productSkuId?: number;
    skuName?: string;
    specValueNames?: string;
    attrValueStr?: string;
    attrs?: Array<{ attrName: string; attrValue: string }>;
    deliveryFeeType?: number;
    deliveryFeeValue?: number;
}

const props = withDefaults(
    defineProps<{
        productList: ProductSkuItem[];
        modelValue: SkuDeliveryFeeState[];
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
    "update:modelValue": [value: SkuDeliveryFeeState[]];
}>();

const deliveryFeeFormRef = shallowRef();
const deliveryFeeList = ref<DeliveryFeeRow[]>([]);
let isInternalUpdate = false;

const specColumnNames = computed(() => {
    const first = deliveryFeeList.value[0];
    if (first?.attrs?.length) {
        return first.attrs.map((a) => a.attrName);
    }
    return [];
});

// --- 批量编辑 ---
const batchEditorVisible = ref(false);
const batchEditorRef = ref<InstanceType<typeof BatchFeeEditor>>();

const onBatchConfirm = (payload: { serviceFeeType: number; serviceFeeValue: number }) => {
    deliveryFeeList.value.forEach((item) => {
        item.deliveryFeeType = payload.serviceFeeType;
        item.deliveryFeeValue = Number(payload.serviceFeeValue) || 0;
    });
    emitChange();
};

// --- 表单校验 ---
const getDeliveryFeeRules = (index: number) => [
    {
        validator: (_rule: any, value: any, callback: any) => {
            if (value === "" || value === null || value === undefined) {
                return callback(new Error("请输入配送费用"));
            }
            const num = Number(value);
            if (isNaN(num)) {
                return callback(new Error("请输入有效数字"));
            }
            const row = deliveryFeeList.value[index];
            if (row?.deliveryFeeType === FeeType.PERCENT) {
                if (num < 0 || num > 100) {
                    return callback(new Error("比例范围为 0~100%"));
                }
            } else if (num < 0) {
                return callback(new Error("金额不能小于 0"));
            }
            callback();
        },
        trigger: "change"
    }
];

const validateDeliveryFee = async () => {
    await deliveryFeeFormRef.value?.validate();
};

// --- 数据同步 ---
const emitChange = () => {
    isInternalUpdate = true;
    emit(
        "update:modelValue",
        deliveryFeeList.value.map((item) => ({
            skuId: item.skuId,
            skuName: item.skuName,
            deliveryFeeType: item.deliveryFeeType,
            deliveryFeeValue: item.deliveryFeeValue
        }))
    );
    nextTick(() => {
        isInternalUpdate = false;
    });
};

const resolveSkuId = (item: ProductSkuItem) => item.skuId ?? item.productSkuId ?? 0;
const resolveSkuName = (item: ProductSkuItem) => item.skuName || item.specValueNames || item.attrValueStr || "默认规格";

const buildRow = (item: ProductSkuItem): DeliveryFeeRow => {
    const skuId = resolveSkuId(item);
    const existing = skuId > 0
        ? props.modelValue?.find((f) => f.skuId === skuId)
        : (props.modelValue?.length === 1 && props.modelValue[0]?.skuId === 0 ? props.modelValue[0] : undefined);
    const hasDefault = props.defaultFeeRate > 0;
    return {
        skuId,
        skuName: resolveSkuName(item),
        attrs: item.attrs || [],
        deliveryFeeType: existing?.deliveryFeeType ?? item.deliveryFeeType ?? (hasDefault ? FeeType.PERCENT : FeeType.FIXED),
        deliveryFeeValue: existing?.deliveryFeeValue ?? item.deliveryFeeValue ?? (hasDefault ? props.defaultFeeRate : 0)
    };
};

const getStructureKey = (list: ProductSkuItem[]) => list.map((item) => resolveSkuId(item)).join(",");

let lastStructureKey = "";

const resetToDefaultRow = () => {
    if (deliveryFeeList.value.length !== 1 || deliveryFeeList.value[0].skuId !== 0) {
        const hasDefault = props.defaultFeeRate > 0;
        deliveryFeeList.value = [
            {
                skuId: 0,
                skuName: "默认规格",
                deliveryFeeType: hasDefault ? FeeType.PERCENT : FeeType.FIXED,
                deliveryFeeValue: hasDefault ? props.defaultFeeRate : 0
            }
        ];
        emitChange();
    }
    lastStructureKey = "";
};

/** 三级回退匹配：skuId → 精确 attrs → 部分 attrs */
const findMatchInOldList = (item: ProductSkuItem, oldList: DeliveryFeeRow[]): DeliveryFeeRow | undefined => {
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

const rebuildDeliveryFeeList = (newList: ProductSkuItem[]) => {
    const oldList = deliveryFeeList.value;
    deliveryFeeList.value = newList.map((item) => {
        const match = findMatchInOldList(item, oldList);
        if (match) {
            return {
                skuId: resolveSkuId(item),
                skuName: resolveSkuName(item),
                attrs: item.attrs || [],
                deliveryFeeType: match.deliveryFeeType,
                deliveryFeeValue: match.deliveryFeeValue
            };
        }
        return buildRow(item);
    });
    lastStructureKey = getStructureKey(newList);
    emitChange();
};

const syncDisplayNames = (newList: ProductSkuItem[]) => {
    newList.forEach((item, i) => {
        const row = deliveryFeeList.value[i];
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

const syncModelToList = (newVal: SkuDeliveryFeeState[]) => {
    newVal.forEach((item) => {
        const target = deliveryFeeList.value.find((row) => row.skuId === item.skuId);
        if (target) {
            target.deliveryFeeType = item.deliveryFeeType;
            target.deliveryFeeValue = item.deliveryFeeValue;
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
            rebuildDeliveryFeeList(newList);
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
        if (!newVal?.length || !deliveryFeeList.value.length) return;
        syncModelToList(newVal);
    },
    { immediate: true }
);

const onFeeTypeChange = (index: number) => {
    deliveryFeeList.value[index].deliveryFeeValue = 0;
    emitChange();
};

defineExpose({ validateDeliveryFee });
</script>

<style lang="less" scoped>
.delivery-fee-config {
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
