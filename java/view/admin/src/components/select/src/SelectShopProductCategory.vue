/** 店铺商品分类选择器 - 级联选择店铺自定义的商品分类 */
<template>
    <el-cascader
        ref="cascaderRef"
        v-model="categoryId"
        :options="options"
        :props="cascaderProps"
        class="cascader"
        clearable
        :placeholder="$t('选择分类')"
        @change="onChange"
        @clear="onClear"
        @visible-change="visibleChange"
    />
</template>
<script lang="ts" setup>
import { onMounted, ref, PropType } from "vue";
import { ElCascader } from "element-plus";
import { message } from "ant-design-vue";
import { getAllShopProductCategoryList } from "@/api/product/shopProductCategory";
import { useShopProductCategoryStore } from "@/store/shopProductCategory";
import type { ShopProductCategoryFilterState } from "@/types/product/shopProductCategory";

const props = defineProps({
    /** 是否多选 */
    multiple: { type: Boolean, default: false },
    /** 预设分类列表 */
    categoryList: { type: Array as PropType<ShopProductCategoryFilterState[]>, default: () => [] }
});

/** 选中的分类ID */
const categoryId = defineModel<any>("categoryId");
/** 选中的分类名称 */
const categoryName = defineModel<any>("categoryName");
/** 级联选择器配置 */
const cascaderProps = { label: "categoryName", value: "categoryId", children: "children", multiple: props.multiple, checkStrictly: true };
const categoryStore = useShopProductCategoryStore();
/** 分类选项列表 */
const options = ref<ShopProductCategoryFilterState[]>([]);
/** 加载状态 */
const loading = ref(false);
/** 数据已加载标识 */
const loaded = ref(false);
/** 下拉显示状态 */
const open = ref(false);

onMounted(() => {
    if (props.categoryList.length > 1) {
        options.value = props.categoryList;
    } else {
        loadCategory();
    }
});

/** 加载分类数据（优先从 store 缓存获取） */
const loadCategory = async () => {
    if (loaded.value === true) return;
    loading.value = true;
    if (categoryStore.shopProductCategoryTree !== null) {
        options.value = categoryStore.shopProductCategoryTree;
        return;
    }
    try {
        const result = await getAllShopProductCategoryList();
        options.value = result;
        categoryStore.shopProductCategoryTree = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
        loaded.value = true;
    }
};

/** 级联选择器组件引用 */
const cascaderRef = ref();

/**
 * 选择变更处理
 * @param {any} ids - 选中的ID路径数组
 */
const onChange = (ids: any) => {
    if (props.multiple == true) {
        categoryId.value = ids?.map((subArray: any) => subArray[subArray.length - 1]);
    } else {
        const item = cascaderRef.value.getCheckedNodes();
        categoryName.value = item[0] ? item[0].text : "";
        categoryId.value = ids ? ids[ids.length - 1] : 0;
    }
};

/**
 * 下拉显示变更处理
 * @param {any} e - 显示状态
 */
const visibleChange = (e: any) => {
    if (e) {
        loadCategory();
    }
};

/** 清空选择 */
const onClear = () => {
    if (props.multiple == true) {
        categoryId.value = [];
    } else {
        categoryId.value = 0;
        categoryName.value = "";
    }
};

/**
 * 处理分类数组（扁平化后重建树）
 * @param {any} arr - 分类数组
 */
function dealCatArr(arr: any) {
    arr = arr.flat();
    arr = buildTree(arr, 0);
    arr = processArray(arr);
    return arr;
}

/**
 * 递归处理数组，设置 isLeaf 属性
 * @param {any[]} arr - 分类数组
 */
function processArray(arr: any[]): any[] {
    return arr?.map((item) => {
        if (Array.isArray(item) || typeof item === "object") {
            if (item.children) {
                item.children = processArray(item.children);
            }
            item.isLeaf = false;
        }
        return item;
    });
}

/**
 * 构建分类树
 * @param {any} categories - 扁平分类数组
 * @param {number} parentId - 父级ID
 */
function buildTree(categories: any, parentId = 0) {
    const tree = [];
    for (const category of categories) {
        if (category.parentId === parentId) {
            const children = buildTree(categories, category.categoryId);
            if (children.length) {
                category.children = children;
            }
            tree.push(category);
        }
    }
    return tree;
}
</script>
<style lang="less" scoped></style>
