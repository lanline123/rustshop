/** 配送方式选择器 - 用于选择订单配送方式 */
<template>
    <el-select v-model="selectedCompanyId" class="full-width" filterable :empty-values="[0, undefined, null,'']">
        <el-option
            v-for="company in shippingTypeCompanies"
            :key="company.shippingTypeId"
            :label="company.shippingTypeName"
            :value="company.shippingTypeId" />
    </el-select>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import { getShippingTypeAll } from "@/api/setting/shippingType";
import { ShippingTypeFilterState } from "@/types/setting/shippingType";

const props = defineProps({
    id: { type: Number, default: null },
});
const emit = defineEmits(["update:id", "change"]);

/** 配送方式列表 */
const shippingTypeCompanies = ref<ShippingTypeFilterState[]>([]);

/** 选中的配送方式ID，支持 v-model 双向绑定 */
const selectedCompanyId = computed({
    get: () => props.id,
    set: (newValue) => {
        emit("update:id", newValue);
        updateDefalutId(newValue)
    },
});

onMounted(async () => {
    try {
        const result = await getShippingTypeAll({ paging: false });
        shippingTypeCompanies.value = result.records;
        updateDefalutId(props.id)
    } catch (error: any) {
        message.error(error.message);
    }
});

/**
 * 更新默认配送ID
 * @param {number} typeId - 配送方式ID
 */
const updateDefalutId = (typeId:number) => {
    const selected = shippingTypeCompanies.value.find((item) => item.shippingTypeId === typeId);
    emit("change", selected ? selected.shippingDefaultId : 0);
};
</script>

<style lang="less" scoped>
.full-width {
    width: 100%;
}
</style>
