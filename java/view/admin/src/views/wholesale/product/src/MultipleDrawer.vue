/** * @file 倍数购买设置抽屉 * @description 倍数永远按 SKU 配置，与起批维度无关；multiple=1 或空表示不限制 * 回填：规格列按 skuSpecText
拆分动态展示，价格取自价格详情，各 SKU 倍数取自 buyRule/detail * 倍数购买列头带批量编辑（参考商品编辑页价格批量设置：PopForm + isDirect） */
<template>
    <div class="container" v-loading="loading">
        <div class="content_wrapper">
            <el-alert
                class="mb10"
                type="primary"
                :closable="false"
                :title="$t('支持基础单位按设置数量的倍数购买，1或空表示不限制;例如，设置4，则购买时必须以4的倍数购买')"
                show-icon
            />
            <el-table :data="skuList" row-key="skuId">
                <template v-if="specColumns.length">
                    <el-table-column v-for="col in specColumns" :key="col" :label="col" min-width="120" align="center" header-align="center">
                        <template #default="{ row }">{{ row.specs[col] || "--" }}</template>
                    </el-table-column>
                </template>
                <el-table-column v-else :label="$t('规格')" min-width="160" align="center" header-align="center">
                    <template #default="{ row }">{{ row.specText || $t('统一规格') }}</template>
                </el-table-column>
                <el-table-column :label="$t('价格')" min-width="100" align="center" header-align="center">
                    <template #default="{ row }">{{ row.retailPrice != null && row.retailPrice !== "" ? Number(row.retailPrice) : "--" }}</template>
                </el-table-column>
                <el-table-column :label="$t('倍数购买')" width="220" align="center" header-align="center">
                    <template #header>
                        <PopForm :isDirect="true" :isHover="false" type="integer" v-model:org-value="batchMultiple" @callback="onBatchApply" :label="$t('倍数购买')">
                            <span>{{ $t('倍数购买') }}</span>
                        </PopForm>
                    </template>
                    <template #default="{ row }">
                        <TigInput v-model="multipleMap[row.skuId]" type="integer" :min="1" allowEmptyDecimal :placeholder="$t('请输入')" width="160px">
                            <template #append>{{ $t('倍') }}</template>
                        </TigInput>
                    </template>
                </el-table-column>
                <template #empty>
                    <div class="empty-warp"><div class="empty-bg">{{ $t('该商品无可设置规格') }}</div></div>
                </template>
            </el-table>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { message } from "ant-design-vue";
import { PopForm } from "@/components/pop-form";
import { getWholesalePriceDetail, getBuyRuleDetail, saveMultiple } from "@/api/wholesale/product";
import type { WholesalePriceDetailVO } from "@/types/wholesale/price";
import type { WholesaleProductBuyRuleVO } from "@/types/wholesale/product";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({ productId: { type: Number, default: 0 } });

interface SkuRow {
    skuId: number;
    specText: string;
    specs: Record<string, string>;
    retailPrice: string;
}

const loading = ref<boolean>(true);
const skuList = ref<SkuRow[]>([]);
const specColumns = ref<string[]>([]);
const multipleMap = reactive<Record<number, string>>({});
const batchMultiple = ref<string>("");

/** 解析规格文案「尺码:40|颜色:天空蓝」→ { 尺码: '40', 颜色: '天空蓝' } */
const parseSpec = (text: string): Record<string, string> => {
    const map: Record<string, string> = {};
    (text || "").split("|").forEach((part) => {
        if (!part) return;
        const idx = part.indexOf(":");
        if (idx > -1) map[part.slice(0, idx)] = part.slice(idx + 1);
        else map[part] = "";
    });
    return map;
};

/** 由价格详情 + 起批规则构建表格 SKU 行与动态规格列 */
const buildSkuTable = (priceDetail: WholesalePriceDetailVO, ruleDetail: WholesaleProductBuyRuleVO): { rows: SkuRow[]; columns: string[] } => {
    const isMultiSpec = !!priceDetail.isMultiSpec;
    const colSet: string[] = [];
    let rows = (priceDetail.skus || []).map((s) => {
        if (!isMultiSpec) {
            return { skuId: s.skuId, specText: t('统一规格'), specs: {}, retailPrice: s.retailPrice };
        }
        const specs = parseSpec(s.skuSpecText);
        Object.keys(specs).forEach((k) => {
            if (!colSet.includes(k)) colSet.push(k);
        });
        return { skuId: s.skuId, specText: s.skuSpecText, specs, retailPrice: s.retailPrice };
    });
    // 单规格商品未配置批发价时 price/detail 不下发 skus，前端补一条「统一规格」占位。
    // skuId 优先取 buyRule/detail 返回的真实 skuId（单规格仅一条规则），保证倍数回显/保存的 skuId 一致；都没有再退 0
    if (!isMultiSpec && rows.length === 0) {
        const fallbackSkuId = ruleDetail.skuRules?.[0]?.skuId ?? 0;
        rows = [{ skuId: fallbackSkuId, specText: t('统一规格'), specs: {}, retailPrice: "" }];
    }
    return { rows, columns: isMultiSpec ? colSet : [] };
};

/** 按 buyRule/detail 回填各 SKU 倍数：multiple>=1 原样展示（含 1 倍）；0/未配置显示为空 */
const fillMultipleMap = (rows: SkuRow[], ruleDetail: WholesaleProductBuyRuleVO) => {
    const ruleMap: Record<number, number> = {};
    (ruleDetail.skuRules || []).forEach((r) => {
        ruleMap[r.skuId] = r.multiple && r.multiple >= 1 ? r.multiple : 0;
    });
    rows.forEach((s) => {
        const v = ruleMap[s.skuId];
        multipleMap[s.skuId] = v && v >= 1 ? String(v) : "";
    });
};

/** 拉取价格/起批规则并回填 SKU 列表与各 SKU 倍数 */
const loadDetail = async () => {
    const [priceDetail, ruleDetail] = await Promise.all([
        getWholesalePriceDetail({ productId: props.productId }),
        getBuyRuleDetail({ productId: props.productId })
    ]);
    const { rows, columns } = buildSkuTable(priceDetail, ruleDetail);
    skuList.value = rows;
    specColumns.value = columns;
    fillMultipleMap(rows, ruleDetail);
};

onMounted(async () => {
    try {
        await loadDetail();
    } catch (error: any) {
        message.error(error.message || t('加载失败'));
    } finally {
        loading.value = false;
    }
});

/** 列头批量编辑：把输入值套用到所有 SKU */
const onBatchApply = () => {
    skuList.value.forEach((s) => {
        multipleMap[s.skuId] = batchMultiple.value;
    });
};

const onSubmit = async () => {
    try {
        emit("update:confirmLoading", true);
        await saveMultiple({
            productId: props.productId,
            // 空 → 不限制（multiple=1）
            skuMultiples: skuList.value.map((s) => ({ skuId: s.skuId, multiple: Number(multipleMap[s.skuId]) || 1 }))
        });
        message.success(t('保存成功'));
        emit("submitCallback", true);
    } catch (error: any) {
        message.error(error.message || t('保存失败'));
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => onSubmit();
defineExpose({ onFormSubmit });
</script>
