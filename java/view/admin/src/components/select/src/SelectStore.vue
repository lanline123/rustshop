/** 门店选择器 - 用于选择O2O门店，支持远程搜索 */
<template>
    <el-select
        v-model="storeId"
        :loading="loading"
        filterable
        remote
        :remoteMethod="remoteMethod"
        :multiple="props.multiple"
        :placeholder="placeholder"
        clearable
        reserve-keyword
        @change="onChange"
    >
        <el-option v-for="item in options" :key="item.shopId" :label="item.shopTitle" :value="item.shopId" />
    </el-select>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from "vue";
import { getShopSelectList } from "@/api/shop/shop";
import { getShopList } from "@/api/shop/shop";
import { vendorShopBindShopList } from "@/api/vendor/bindShop";
import { isS2b2c } from "@/utils/version";
import { message } from "ant-design-vue";
import type { ShopSelectFilterParams } from "@/types/shop/shop.d";

const props = defineProps({
    modelValue: { type: [Number, String] },
    /** 是否多选 */
    multiple: { type: Boolean, default: false },
    placeholder: { type: String, default: "请输入门店名称" }
});

/** 加载状态 */
const loading = ref<boolean>(true);
/** 查询参数 */
const filterParams = reactive<ShopSelectFilterParams>({
    keyword: ""
});
const emit = defineEmits(["update:modelValue", "onChange"]);
/** 选中的门店ID */
const storeId = defineModel<number | string | number[]>("storeId");

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

/** 门店选项列表 */
const options: any = ref([]);

/**
 * 加载门店列表
 * @param {number|string} query - 搜索关键字
 */
const loadFilter = async (query?: number | string) => {
    loading.value = true;
    filterParams.keyword = String(query || "");
    try {
        let result: any;
        result = await getShopList({ ...filterParams, shopType: 2, size: 99, select: 1 });
        options.value = result.records;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

/**
 * 远程搜索方法
 * @param {string} query - 搜索关键字
 */
const remoteMethod = (query: string) => {
    loadFilter(query);
};
</script>
<style lang="less" scoped></style>
