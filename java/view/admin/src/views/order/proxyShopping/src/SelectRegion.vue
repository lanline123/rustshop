/** 地区选择器 - 省市区级联选择（代客下单专用接口） */
<template>
    <el-cascader
        placement="right"
        style="width: 100%"
        :props="cascaderProps"
        :placeholder="$t('请选择地区')"
        :options="options"
        v-model="selectedIds"
        clearable
        @change="onChange"
        @clear="onClear"
    />
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { message } from "ant-design-vue";
import { getProxyShoppingRegionTreeList } from "@/api/proxyShopping/address";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
let cachedRegionTree: any[] | null = null;

const props = defineProps({
    /** 是否可以选择任意层级 */
    checkStrictly: { type: Boolean, default: false }
});

/** 选中的地区ID数组 */
const selectedIds = defineModel<number | string | number[]>("selectedIds");
/** 级联选择器配置 */
const cascaderProps = {
    checkStrictly: props.checkStrictly,
    label: "regionName",
    value: "regionId",
    children: "children"
};

/** 地区选项列表 */
const options = ref<any[]>([]);
/** 数据加载中标识 */
const loading = ref(false);
/** 选中的地区完整信息 */
const selectedRegions = ref<any[]>([]);

const emit = defineEmits(["update:modelValue", "change", "clear"]);

onMounted(() => {
    if (cachedRegionTree?.length) {
        options.value = cachedRegionTree;
        return;
    }
    loadRegion();
});

/** 加载地区数据（模块级缓存，避免重复请求） */
const loadRegion = async () => {
    if (loading.value) return;
    loading.value = true;
    try {
        const result = await getProxyShoppingRegionTreeList();
        cachedRegionTree = Array.isArray(result) ? result : [];
        options.value = cachedRegionTree;
    } catch (error: any) {
        message.error(error?.message || t('地区数据加载失败'));
    } finally {
        loading.value = false;
    }
};

/**
 * 选择变更处理
 * @param {number[]} ids - 选中的地区ID数组
 */
const onChange = (ids: number[]) => {
    selectedIds.value = ids;
    selectedRegions.value = findSelectedRegions(ids, options.value);
    emit("change", selectedRegions.value);
    emit("update:modelValue", ids);
};

/** 清空选择 */
const onClear = () => {
    selectedIds.value = [];
    selectedRegions.value = [];
    emit("clear");
};

/**
 * 根据ID数组查找对应的地区信息
 * @param {number[]} ids - 地区ID数组
 * @param {any[]} options - 地区选项树
 * @returns {any[]} 地区信息数组
 */
const findSelectedRegions = (ids: number[], options: any[]): any[] => {
    const result: any[] = [];
    if (ids && ids.length > 0) {
        for (const id of ids) {
            const region = findRegionById(id, options);
            if (region) {
                result.push(region);
            }
        }
    }
    return result;
};

/**
 * 递归查找指定ID的地区
 * @param {number} id - 地区ID
 * @param {any[]} options - 地区选项树
 * @returns {any} 地区信息或 null
 */
const findRegionById = (id: number, options: any[]): any => {
    for (const option of options) {
        if (option.regionId === id) {
            return option;
        }
        if (option.children && option.children.length > 0) {
            const found = findRegionById(id, option.children);
            if (found) {
                return found;
            }
        }
    }
    return null;
};
</script>

<style lang="less" scoped>
.cascader {
    width: 100%;
}
</style>

