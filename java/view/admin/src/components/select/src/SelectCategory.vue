/**
 * @file 商品分类选择器组件
 * @description 级联选择器形式的商品分类选择
 * @features
 * - 级联多级分类选择
 * - 支持单选和多选模式
 * - 支持任意层级选择
 * - 分类数据缓存（store）
 * - 支持店铺切换后重新加载
 */
<template>
    <el-cascader
        style="max-width: 100%"
        ref="cascaderRef"
        v-model="categoryId"
        :options="options"
        :props="cascaderProps"
        :teleported="teleported"
        class="cascader"
        clearable
        :placeholder="$t('选择分类')"
        @change="onChange"
        @clear="onClear"
        @visible-change="visibleChange"
    />
</template>
<script lang="ts" setup>
import { computed, onMounted, ref, PropType } from "vue";
import { ElCascader } from "element-plus";
import { message } from "ant-design-vue";
import { getAllCategoryList } from "@/api/product/category";
import { useCategoryStore } from "@/store/category";
import { CategoryFilterState } from "@/types/product/category";
// 传值
const props = defineProps({
    multiple: { type: Boolean, default: false },
    checkStrictly: { type: Boolean, default: true },
    categoryList: { type: Array as PropType<CategoryFilterState[]>, default: () => [] },
    /** 过滤掉指定名称的分类（用于“上级分类”避免同名） */
    excludeName: { type: String, default: "" },
    /** 是否将下拉面板挂载到 body；抽屉内建议关闭以继承抽屉层级 */
    teleported: { type: Boolean, default: true }
});
const categoryId = defineModel<number | string | number[]>("categoryId");
const categoryName = defineModel<any>("categoryName");
const cascaderProps = { label: "categoryName", value: "categoryId", children: "children", multiple: props.multiple, checkStrictly: props.checkStrictly };
const categoryStore = useCategoryStore();
//选项卡
const rawOptions = ref<CategoryFilterState[]>([]);
const normalized = (v?: string) => (v || "").trim().toLowerCase();
const options = computed<CategoryFilterState[]>(() => {
    const exclude = normalized(props.excludeName);
    if (!exclude) return rawOptions.value;
    const filterTree = (list: CategoryFilterState[]): CategoryFilterState[] => {
        return (list || [])
            .filter((node) => normalized((node as any).categoryName) !== exclude)
            .map((node) => {
                const children = (node as any).children as CategoryFilterState[] | undefined;
                if (children && children.length) {
                    const nextChildren = filterTree(children);
                    // 不直接修改原对象，避免影响 store 缓存
                    return { ...(node as any), children: nextChildren.length ? nextChildren : undefined } as CategoryFilterState;
                }
                return node;
            });
    };
    return filterTree(rawOptions.value);
});
const loading = ref(false);
const loaded = ref(false);
// 当前值
// 手动控制显示和隐藏
const open = ref(false);
// 给父组件传值
// 加载分类
onMounted(() => {
    if (props.categoryList.length > 1) {
        rawOptions.value = props.categoryList;
    } else {
        loadCategory();
    }
});
const loadCategory = async (shopId?: number) => {
    if (loaded.value === true) return;
    loading.value = true;
    // if (categoryStore.categoryTree !== null) {
    //     options.value = categoryStore.categoryTree;
    //     return;
    // }
    try {
        const result = await getAllCategoryList(shopId);
        rawOptions.value = result;
        categoryStore.categoryTree = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
        loaded.value = true;
    }
};
// 监听变化
const cascaderRef = ref();
const onChange = (ids: any) => {
    if (props.multiple == true) {
        const newArray: number[] = ids.map((subArray: any) => subArray[subArray.length - 1]);
        categoryId.value = newArray;
    } else {
        const item = cascaderRef.value.getCheckedNodes();
        categoryName.value = item[0] ? item[0].text : "";
        // 注意categoryName要先于categoryId设置，因为选择链接的地方监听了categoryId
        categoryId.value = ids ? ids[ids.length - 1] : 0;
    }
};
const SelectShopChange = (e: any) => {
    if (e) {
        loaded.value = false;
        categoryStore.categoryTree = null;
        if (props.multiple == true) {
            categoryId.value = [];
        } else {
            categoryId.value = "";
        }
    }
};
const visibleChange = (e: any) => {
    if (e) {
        loadCategory();
    }
};
const onClear = () => {
    if (props.multiple == true) {
        categoryId.value = [];
    } else {
        categoryId.value = 0;
        categoryName.value = "";
    }
};

// 处理分类数组
function dealCatArr(arr: any) {
    arr = arr.flat();
    arr = buildTree(arr, 0);
    arr = processArray(arr);
    return arr;
}

// 定义一个递归函数，用于处理多维数组并返回新的数组
function processArray(arr: any[]): any[] {
    return arr.map((item) => {
        if (Array.isArray(item) || typeof item === "object") {
            if (item.children) {
                item.children = processArray(item.children);
            }
            item.isLeaf = false; // 修改为新的值
        }
        return item;
    });
}

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
defineExpose({
    SelectShopChange,
    loadCategory,
    loaded
});
</script>
<style lang="less" scoped></style>
