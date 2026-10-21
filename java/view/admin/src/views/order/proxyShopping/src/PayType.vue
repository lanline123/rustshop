<template>
    <div class="proxy-paytype">
        <template v-if="viewList.length > 0">
            <el-radio-group :model-value="modelValue" class="paytype-group" @change="onChange">
                <el-radio v-for="item in viewList" :key="Number(item.typeId)" :label="Number(item.typeId)" :disabled="item.disabled === true">
                    {{ item.typeName || "-" }}
                </el-radio>
            </el-radio-group>
        </template>
        <template v-else>
            <div class="gray">{{ $t('暂无可用支付方式') }}</div>
        </template>
    </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { ProxyPaymentType } from "@/types/order/proxyShopping.d";

const props = defineProps<{
    payTypeId?: number;
    paymentTypeList: ProxyPaymentType[];
}>();

const emit = defineEmits<{
    (e: "change", payTypeId: number): void;
}>();

const list = computed(() => (Array.isArray(props.paymentTypeList) ? props.paymentTypeList : []));
const viewList = computed(() => {
    // 仅展示 isShow=true 的项（缺失时视为展示）
    return (list.value || []).filter((p) => p?.isShow !== false);
});
const modelValue = computed(() => (props.payTypeId ? Number(props.payTypeId) : 0));

const onChange = (val: any) => {
    const id = Number(val ?? 0);
    if (!Number.isFinite(id) || id <= 0) return;
    emit("change", id);
};
</script>

<style scoped lang="less">
.proxy-paytype {
    width: 100%;
}
.paytype-group {
    display: flex;
    flex-wrap: wrap;
    gap: 12px 18px;
}
</style>
