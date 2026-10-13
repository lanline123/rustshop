/** 供应商选择器 - 用于选择供应商，支持远程搜索 */
<template>
    <el-select
        v-model="vendorId"
        :loading="loading"
        filterable
        :multiple="props.multiple"
        :placeholder="placeholder"
        clearable
        remote
        reserve-keyword
        :remote-method="loadFilter"
        @change="onChange"
    >
        <el-option v-for="item in options" :key="item.vendorId" :label="item.vendorName" :value="item.vendorId" />
    </el-select>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from "vue";
import { getSuppliersList } from "@/api/vendor/suppliers";
import { message } from "ant-design-vue";
import type { ShopSelectFilterParams } from "@/types/shop/shop.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    modelValue: { type: [Number, String] },
    /** 是否多选 */
    multiple: { type: Boolean, default: false },
    placeholder: { type: String, default: "请输入供应商名称" }
});

/** 加载状态 */
const loading = ref<boolean>(true);
/** 查询参数 */
const filterParams = reactive<ShopSelectFilterParams>({
    keyword: "",
    size: 30
});
const emit = defineEmits(["update:modelValue", "onChange"]);
/** 选中的供应商ID */
const vendorId = defineModel<number | string | number[]>("vendorId");

/**
 * 选择变更处理
 * @param {any} e - 选中的值
 */
const onChange = (e: any) => {
    emit("update:modelValue", e);
    emit("onChange", e);
};

onMounted(() => {
    loadFilter();
});

/** 供应商选项列表 */
const options: any = ref([
    {
        vendorId: -2,
        vendorName: t('全部供应商')
    }
]);

/**
 * 加载供应商列表
 * @param {number|string} query - 搜索关键字
 */
const loadFilter = async (query?: number | string) => {
    loading.value = true;
    filterParams.keyword = String(query || "");
    try {
        const result = await getSuppliersList({ ...filterParams });
        options.value.length = 0;
        options.value.push(...result.records);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
</script>
<style lang="less" scoped></style>
