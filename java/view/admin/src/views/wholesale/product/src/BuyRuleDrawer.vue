/**
 * @file 起批量设置抽屉
 * @description 起批维度 SPU/SKU；统一字段 moq：
 *   dimension=1 加总起批；dimension=2 每 SKU 起批（同一 moq 应用到所有 SKU），不再逐 SKU 配置
 * 回填：起批维度/起批数取自 buyRule/detail
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
import { ref, onMounted } from "vue";
import { message } from "ant-design-vue";
import { getBuyRuleDetail, saveBuyRule } from "@/api/wholesale/product";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({ productId: { type: Number, default: 0 } });

const loading = ref<boolean>(true);
const dimension = ref<number>(1);
const moq = ref<number>(1);

onMounted(async () => {
    try {
        const ruleDetail = await getBuyRuleDetail({ productId: props.productId });
        dimension.value = ruleDetail.dimension === 2 ? 2 : 1;
        // 优先取统一字段 moq；兼容旧字段（SPU=moqSpu / SKU=首条 skuRules.moqSku）
        const fallback = dimension.value === 1 ? ruleDetail.moqSpu : ruleDetail.skuRules?.[0]?.moqSku;
        const val = ruleDetail.moq ?? fallback;
        moq.value = val && val >= 1 ? val : 1;
    } catch (error: any) {
        message.error(error.message || t('加载失败'));
    } finally {
        loading.value = false;
    }
});

const onSubmit = async () => {
    // TigInput(type=integer) 回传字符串，统一 Number 化后再比较/提交
    const moqNum = Number(moq.value);
    if (!moqNum || moqNum < 1) {
        message.warning(t('起批数量需为 ≥1 的整数'));
        return;
    }
    try {
        emit("update:confirmLoading", true);
        await saveBuyRule({
            productId: props.productId,
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
