/** 供应商绑定店铺选择器 - 用于选择供应商绑定的店铺/门店 */
<template>
    <el-select v-model="shopId" :loading="loading" filterable :multiple="props.multiple"
        :placeholder="isStore() ? $t('请输入门店名称') : $t('请输入店铺名称')" clearable reserve-keyword @change="onChange">
        <el-option v-for="item in options" :key="item.shopId" :label="item.shopTitle" :value="item.shopId" />
    </el-select>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { vendorShopBindShopList } from "@/api/vendor/bindShop";
import { message } from "ant-design-vue";
import type { ShopSelectFilterParams } from "@/types/shop/shop.d";
import { isStore } from "@/utils/version";

const props = defineProps({
    modelValue: { type: [Number, String] },
    /** 是否多选 */
    multiple: { type: Boolean, default: false }
})

/** 加载状态 */
const loading = ref<boolean>(true);
/** 查询参数 */
const filterParams = reactive<ShopSelectFilterParams>({
    size: 30,
    keyword: ""
});
const emit = defineEmits(['update:modelValue', 'onChange'])
/** 选中的店铺ID */
const shopId = defineModel<number | string | number[]>("shopId");

/**
 * 选择变更处理
 * @param {any} e - 选中的值
 */
const onChange = (e: any) => {
    emit('update:modelValue', e)
    emit('onChange', e)
}

onMounted(() => {
    loadFilter()
})

/** 店铺选项列表 */
const options: any = ref([])

/**
 * 加载店铺列表
 * @param {number|string} query - 搜索关键字
 */
const loadFilter = async (query?: number | string) => {
    loading.value = true;
    filterParams.keyword = String(query || '');
    try {
        const result = await vendorShopBindShopList({ ...filterParams });
        options.value = result.records;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
}
</script>
<style lang="less" scoped></style>
