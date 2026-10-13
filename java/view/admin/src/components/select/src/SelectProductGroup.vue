/** 商品分组选择器 - 用于选择商品所属分组 */
<template>
    <el-select
        v-model="productGroupId"
        :allowClear="true"
        :fieldNames="fieldNames"
        :filter-option="filterOption"
        clearable
        filterable
        :placeholder="$t('选择商品分组')"
        popper-class="brand-select"
        show-search
        @change="onChange"
        @visible-change="loadProductGroupClientList"
    >
        <template #default>
            <div class="option">
                <div class="right">
                    <div class="scroll-container">
                        <el-option v-for="item in options" :key="item.productGroupId" :label="item.productGroupName" :value="item.productGroupId">
                            {{ item.productGroupName }}
                        </el-option>
                    </div>
                </div>
            </div>
        </template>
        <template #empty>
            <a-spin :spinning="!loaded" size="small" style="width: 100%; padding-top: 150px" />
        </template>
    </el-select>
</template>
<script lang="ts" setup>
import { onMounted, ref, defineModel } from "vue";
import { message } from "ant-design-vue";
import { getProductGroupClientList } from "@/api/product/productGroup";
import { ProductGroupFilterState } from "@/types/product/productGroup";

const emit = defineEmits(["update:productGroupId", "update:productGroupIds", "update:productGroupForm", "onChange"]);
/** 选中的分组ID */
const productGroupId = defineModel<number | string | number[]>("productGroupId");
/** 选中的分组完整信息 */
const brandForm = defineModel<any>("productGroupForm");
const fieldNames = { label: "productGroupName", value: "productGroupId" };

/**
 * 过滤选项
 * @param {string} input - 输入的搜索文本
 * @param {any} option - 选项数据
 */
const filterOption = (input: string, option: any) => {
    return option.brandName.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

/**
 * 选择变更处理
 * @param {any} e - 选中的值
 */
const onChange = (e: any) => {
    options.value.map((subArray: any) => {
        if (subArray.brandId == e) {
            brandForm.value = subArray;
        }
    });
    emit("update:productGroupIds", e);
    emit("onChange", e);
};

onMounted(() => {
    loadProductGroupClientList();
});

/** 数据加载完成标识 */
const loaded = ref(false);
/** 分组选项列表 */
const options = ref<ProductGroupFilterState[]>([]);
/** 全部分组列表（备份） */
const allProductGroupList = ref<ProductGroupFilterState[]>([]);

/** 加载商品分组列表 */
const loadProductGroupClientList = async () => {
    try {
        const result = await getProductGroupClientList();
        options.value = result;
        allProductGroupList.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loaded.value = true;
    }
};
</script>

<style lang="less" scoped>
.option {
    display: flex;
    overflow: hidden;
    /* 确保所有溢出都在子元素中处理 */
    max-height: 590px !important;
    height: 320px;

    .right {
        overflow: auto;
        flex: 1;
    }
}

</style>
