<template>
    <div class="proxy-delivery-method">
        <div v-for="(store, idx) in cartList || []" :key="store.shopId ?? idx" class="store-block">
            <template v-if="store.noShipping === 1">
                <el-radio-group class="shipping-radio-group" :model-value="0">
                    <el-radio :value="0" disabled>{{ $t('无需配送') }}</el-radio>
                </el-radio-group>
            </template>
            <template v-else-if="shippingTypeList(Number(store.shopId || 0)).length > 0">
                <el-radio-group class="shipping-radio-group"
                    v-model="selectedTypeIdByShop[String(Number(store.shopId || 0))]"
                    @update:modelValue="(val: any) => onRadioChange(Number(store.shopId || 0), val)">
                    <el-radio v-for="item in shippingTypeList(Number(store.shopId || 0))" :key="item.shippingTypeId"
                        :value="Number(item.shippingTypeId || 0)"
                        :disabled="isItemDisabled(item)">
                        {{ item.shippingTypeName }}
                    </el-radio>
                </el-radio-group>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { ProxyCartStoreItem, ProxyStoreShippingTypeData, ProxyStoreShippingTypeItem } from "@/types/order/proxyShopping.d";
import { reactive, watchEffect } from "vue";

const props = withDefaults(
    defineProps<{
        cartList?: ProxyCartStoreItem[];
        shippingTypeData?: ProxyStoreShippingTypeData;
        /** 可用的配送方式 ID 列表（来自 deliveryOption 能力位），null 表示不限制 */
        allowedTypeIds?: number[] | null;
        /** 锁定到指定配送方式 ID：仅该项可选，其余禁用；null/0 表示不锁定 */
        lockedTypeId?: number | null;
    }>(),
    {
        cartList: () => [],
        shippingTypeData: undefined,
        allowedTypeIds: null,
        lockedTypeId: null
    }
);

const isItemDisabled = (item: ProxyStoreShippingTypeItem) => {
    if (props.lockedTypeId != null && props.lockedTypeId > 0) {
        return Number(item.shippingTypeId || 0) !== props.lockedTypeId;
    }
    if (!props.allowedTypeIds || props.allowedTypeIds.length === 0) return false;
    return !props.allowedTypeIds.includes(Number(item.shippingTypeId || 0));
};

const emit = defineEmits<{

    change: [shippingTypeId: number];
}>();

const shippingTypeModel = defineModel<Record<string, { typeId: number; shopId: number; typeName: string }>>("shippingType", { default: () => ({}) });

// v-model 必须绑定到可赋值表达式，这里用 shopId -> typeId 的中间态
const selectedTypeIdByShop = reactive<Record<string, number>>({});
watchEffect(() => {
    const model = shippingTypeModel.value || {};
    for (const [key, val] of Object.entries(model)) {
        selectedTypeIdByShop[key] = Number((val as any)?.typeId || 0);
    }
});

const shippingTypeList = (shopId: number) => {
    const list = props.shippingTypeData?.[shopId];
    return Array.isArray(list) ? list : [];
};

const isActiveShippingType = (shopId: number, shippingTypeId: number) => {
    const sid = Number(shopId);
    const tid = Number(shippingTypeId || 0);
    if (!tid) return false;
    const key = String(sid);
    return Number(shippingTypeModel.value?.[key]?.typeId || 0) === tid;
};

const onRadioChange = (shopId: number, shippingTypeId: any) => {
    const sid = Number(shopId);
    const tid = Number(shippingTypeId || 0);
    if (!tid) return;
    const item = shippingTypeList(sid).find((x) => Number(x?.shippingTypeId || 0) === tid);
    if (!item) return;
    onSelectShippingType(sid, item);
};

const onSelectShippingType = (shopId: number, item: ProxyStoreShippingTypeItem) => {
    const sid = Number(shopId);
    const tid = Number(item.shippingTypeId || 0);
    if (!tid) return;
    const key = String(sid);
    if (isActiveShippingType(sid, tid)) return;
    shippingTypeModel.value = {
        ...(shippingTypeModel.value || {}),
        [key]: {
            typeId: tid,
            shopId: sid,
            typeName: String(item.shippingTypeName || "")
        }
    };
    emitChange(item);
};

const emitChange = (item: ProxyStoreShippingTypeItem) => {

    emit("change", Number(item.shippingTypeId || 0));
};
</script>

<style scoped lang="less">
.proxy-delivery-method {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.store-title {
    font-size: 12px;
    color: #606266;
    margin-bottom: 8px;
}

.shipping-radio-group {
    display: flex;
    flex-wrap: wrap;
}

.shipping-radio-group :deep(.el-radio) {
    // margin: 0 8px 8px 0;
}

.tips {
    font-size: 12px;
}

.gray {
    color: #909399;
}
</style>
