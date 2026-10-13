/**
 * @file 批量设置起批量抽屉
 * @description 对所选批发商品统一设置起批量；调用批量接口 /product/buyRule/batchSave 一次性下发
 *   统一字段 moq：dimension=1 加总起批；dimension=2 每 SKU 起批（同一 moq 套用到所有 SKU）
 * UI 与单个起批量设置（BuyRuleDrawer）保持一致，仅作用对象为所选多个商品
 */
<template>
    <div class="container" v-loading="loading">
        <div class="content_wrapper">
            <el-form label-width="100px">
                <el-form-item :label="$t('起批维度')">
                    <el-select v-model="dimension" style="width: 180px">
                        <el-option :value="1" :label="$t('按 SPU 起批')" />
                        <el-option :value="2" :label="$t('按 SKU 起批')" />
                    </el-select>
                    <div class="extra">
                        <div>{{ $t('按 SPU 起批：以商品整体为单位，不同规格可混批，合计数量达标即可下单。') }}</div>
                        <div>{{ $t('按 SKU 起批：以单个规格为单位，每个规格需单独满足起批数量要求。') }}</div>
                    </div>
                </el-form-item>
                <el-form-item :label="$t('起批数量')">
                    <TigInput v-model="moq" type="integer" :min="1" width="180px" />
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref } from "vue";
import { message } from "ant-design-vue";
import { batchSaveBuyRule } from "@/api/wholesale/product";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    productIds: { type: Array as () => number[], default: () => [] }
});

const loading = ref<boolean>(false);
const dimension = ref<number>(1);
const moq = ref<number>(1);

const onSubmit = async () => {
    if (!props.productIds || props.productIds.length === 0) {
        message.warning(t('未选择商品'));
        return;
    }
    // TigInput(type=integer) 回传字符串，统一 Number 化后再比较/提交
    const moqNum = Number(moq.value);
    if (!moqNum || moqNum < 1) {
        message.warning(t('起批数量需为 ≥1 的整数'));
        return;
    }
    try {
        emit("update:confirmLoading", true);
        await batchSaveBuyRule({
            productIds: props.productIds,
            dimension: dimension.value,
            moq: moqNum
        });
        message.success(t('保存成功'));
        emit("submitCallback", true);
    } catch (error: any) {
        // 5313 起批规则不合法
        message.error(error.message || t('保存失败'));
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => onSubmit();
defineExpose({ onFormSubmit });
</script>
<style scoped lang="less">
.extra {
    color: #909399;
    font-size: 12px;
    line-height: 18px;
}
</style>
