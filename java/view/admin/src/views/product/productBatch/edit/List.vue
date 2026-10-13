/**
 * @file 商品批量修改
 * @description 批量修改多个商品的属性
 * @features
 * - 选择要修改的属性（价格/库存/分类等）
 * - 添加待修改商品
 * - 批量编辑选中商品
 * - 支持导入商品列表
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="wrapper_title">
                <el-form ref="formRef" :model="form" label-width="auto">
                    <el-form-item :label="$t('选择修改：')">
                        <el-checkbox-group v-model="form.type">
                            <el-checkbox v-for="item in checkList" :value="item" style="margin-bottom: 10px">{{ item.lable }}</el-checkbox>
                        </el-checkbox-group>
                    </el-form-item>
                    <el-form-item :label="$t('商品信息：')">
                        <div style="width: 100%">
                            <div class="list-table-tool-row">
                                <div class="list-table-tool-col">
                                    <el-space>
                                        <DialogForm
                                            :params="{ selectedIds: ids, isMultiple: true, apiList: selectProductApi }"
                                            isDrawer
                                            isSelf
                                            path="product/product/src/SelectProduct"
                                            :title="$t('选择商品')"
                                            width="800px"
                                            @okCallback="loadList"
                                        >
                                            <el-button type="primary">{{ $t('添加商品') }}</el-button>
                                        </DialogForm>
                                    </el-space>
                                </div>
                                <div class="list-table-tool-col"></div>
                            </div>
                            <div class="wrapper_table">
                                <ProductBatch
                                    :types="form.type"
                                    :productStatusList="productStatusList"
                                    :selectList="selectList"
                                    @callback="batchData"
                                    :categoryList="categoryList"
                                    :firstWordList="firstWordList"
                                    :brandList="brandList"
                                    :allBrandList="allBrandList"
                                    :shippingTplList="shippingTplList"
                                    :productServiceList="productServiceList"
                                ></ProductBatch>
                                <el-table :data="tableData" border style="width: 100%" loading>
                                    <el-table-column :label="$t('商品信息')">
                                        <template #default="{ row }">
                                            <div class="flex">
                                                <ProductCard :picThumb="row.picThumb" :productId="row.productId" :productName="row.productName"> </ProductCard>
                                            </div>
                                        </template>
                                    </el-table-column>
                                    <el-table-column v-for="item in form.type" width="180" align="center">
                                        <template #header>
                                            <div class="flex-column-center">
                                                <div>{{ item.lable }}</div>
                                            </div>
                                        </template>
                                        <template #default="{ row }">
                                            <div v-if="item.type == 'status'">
                                                <el-select v-model="row[item.key]">
                                                    <el-option v-for="option in productStatusList" :label="option.label" :value="option.value" />
                                                </el-select>
                                            </div>
                                            <div v-if="item.type == 'shippingTpl'">
                                                <el-select v-model="row.shippingTplId">
                                                    <el-option
                                                        v-for="option in shippingTplList"
                                                        :label="option.shippingTplName"
                                                        :value="option.shippingTplId"
                                                    />
                                                </el-select>
                                            </div>
                                            <div v-if="item.type == 'select'">
                                                <el-select v-model="row[item.key]">
                                                    <el-option v-for="option in selectList" :label="option.label" :value="option.value" />
                                                </el-select>
                                            </div>
                                            <div v-if="item.type == 'productService'">
                                                <el-select v-model="(row as any)[item.key]" multiple collapse-tags collapse-tags-tooltip clearable>
                                                    <el-option
                                                        v-for="option in productServiceList"
                                                        :key="option.productServiceId"
                                                        :label="option.productServiceName"
                                                        :value="option.productServiceId"
                                                    />
                                                </el-select>
                                            </div>
                                            <div v-if="item.type == 'input'">
                                                <el-form-item>
                                                    <TigInput v-model="row[item.key]" :placeholder="item.lable" />
                                                </el-form-item>
                                            </div>
                                            <div v-if="item.type == 'category'">
                                                <el-form-item>
                                                    <SelectCategory
                                                        :categoryId="(row as any)[item.key]"
                                                        :categoryList="categoryList"
                                                        @update:categoryId="(val: any) => ((row as any)[item.key] = val)"
                                                    ></SelectCategory>
                                                </el-form-item>
                                            </div>
                                            <div v-if="item.type == 'brand'">
                                                <el-form-item>
                                                    <SelectBrand
                                                        :brandId="(row as any)[item.key]"
                                                        :firstWordList="firstWordList"
                                                        :brandList="brandList"
                                                        :allBrandList="allBrandList"
                                                        @update:brandId="(val: number) => ((row as any)[item.key] = val)"
                                                    ></SelectBrand>
                                                </el-form-item>
                                            </div>
                                        </template>
                                    </el-table-column>
                                    <el-table-column fixed="right" :label="$t('操作')" width="90" align="center">
                                        <template #default="{ row }">
                                            <el-button link type="primary" size="small" @click="del(getRowId(row))"> {{ $t('删除') }} </el-button>
                                        </template>
                                    </el-table-column>
                                </el-table>
                                <div class="extra">{{ $t('一次最多添加100个商品') }}</div>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item label=" ">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()">{{ $t('提交') }} </el-button>
                        <el-button ref="submitBtn" class="form-submit-btn" @click="onReset()">{{ $t('重置') }}</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { onMounted, reactive, ref, shallowRef } from "vue";
import ProductBatch from "./src/ProductBatch.vue";
import { SelectBrand, SelectCategory } from "@/components/select";
import { getProductList, getShippingTplList, BatchProduct } from "@/api/product/product";
import { BatchProduct as vendorBatchProduct, getProductList as getVendorProductList } from "@/api/vendor/product";
import { getProductBatchServicesList, getProductServicesList } from "@/api/product/productServices";
import { useConfigStore } from "@/store/config";
import { Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { ProductCard } from "@/components/list";
import { getAllCategoryList } from "@/api/product/category";
import { CategoryFilterState } from "@/types/product/category";
import { getBrandSearch } from "@/api/product/brand";
import { BrandFilterState } from "@/types/product/brand";
import { ProductFilterParams, ProductFilterState } from "@/types/product/product";
import type { ProductServicesFilterState } from "@/types/product/productServices";
import { getAdminType } from "@/utils/storage";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const formRef = shallowRef();
const config = useConfigStore();
const adminType = getAdminType();
const isVendorAdmin = getAdminType() === "vendor";
const selectProductApi = isVendorAdmin ? getVendorProductList : getProductList;
const filterParams = reactive<ProductFilterParams>({
    page: 1,
    size: 100,
    sortField: "",
    sortOrder: "",
    keyword: ""
});
interface CheckList {
    lable: string;
    key: string;
    type: string;
}
const form = ref({
    type: [] as CheckList[]
});
const defaultCheckList: CheckList[] = [
    {
        lable: t('商品上下架状态'),
        key: "productStatus",
        type: "status"
    },
    {
        lable: t('商品名称'),
        key: "productName",
        type: "input"
    },
    {
        lable: t('商品分类'),
        key: "categoryId",
        type: "category"
    },
    {
        lable: t('商品品牌'),
        key: "brandId",
        type: "brand"
    },
    {
        lable: t('商品货号'),
        key: "productSn",
        type: "input"
    },
    {
        lable: t('条形编码'),
        key: "productTsn",
        type: "input"
    },
    {
        lable: t('商品售价'),
        key: "productPrice",
        type: "input"
    },
    {
        lable: t('市场价'),
        key: "marketPrice",
        type: "input"
    },
    {
        lable: t('运费模板'),
        key: "shippingTplId",
        type: "shippingTpl"
    },
    {
        lable: t('是否包邮'),
        key: "freeShipping",
        type: "select"
    },
    {
        lable: t('是否新品'),
        key: "isNew",
        type: "select"
    },
    {
        lable: t('是否精品'),
        key: "isBest",
        type: "select"
    },
    {
        lable: t('是否热销'),
        key: "isHot",
        type: "select"
    },
    {
        lable: t('排序'),
        key: "sortOrder",
        type: "input"
    },
    {
        lable: t('商品服务'),
        key: "productServiceIds",
        type: "productService"
    }
];
const vendorCheckList: CheckList[] = [
    {
        lable: t('商品上下架状态'),
        key: "productState",
        type: "status"
    },
    {
        lable: t('商品名称'),
        key: "productName",
        type: "input"
    },
    {
        lable: t('商品分类'),
        key: "productCategoryId",
        type: "category"
    },
    {
        lable: t('商品品牌'),
        key: "productBrandId",
        type: "brand"
    },
    {
        lable: t('商品简介'),
        key: "productBrief",
        type: "input"
    }
];
const checkList = ref<CheckList[]>(isVendorAdmin ? vendorCheckList : defaultCheckList);
const productStatusList = ref([
    {
        label: t('上架'),
        value: 1
    },
    {
        label: t('下架'),
        value: 0
    }
]);
const selectList = ref([
    {
        label: t('是'),
        value: 1
    },
    {
        label: t('否'),
        value: 0
    }
]);
const tableData = ref<any[]>([]);
const total = ref(0);
const loading = ref<boolean>(true);
const ids = ref<number[]>([]);
const getRowId = (product: any) => Number(product.productId ?? product.id ?? 0);
const normalizeProductRecord = (product: any) => ({
    ...product,
    // 商品列表及供应商列表的名称字段统一为 productName，供批量修改卡片展示。
    productName: product.productName || product.name || product.title || product.productTitle || ""
});
const productServiceList = ref<ProductServicesFilterState[]>([]);
const loadList = async (data: any) => {
    try {
        if (data) {
            ids.value.push(...data);
        }
        const result = await selectProductApi({ ids: ids.value.join(","), ...filterParams });
        result.records.map((item) => {
            if (item.shippingTplId == 0) {
                item.shippingTplId = "";
            }
        });
        tableData.value = (result.records || []).map(normalizeProductRecord);
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const batchData = (data: any) => {
    tableData.value.map((item) => {
        // 避免数组引用被多行共享（比如 productServiceIds），导致改一行影响所有行
        Object.entries(data || {}).forEach(([k, v]) => {
            (item as any)[k] = Array.isArray(v) ? [...v] : v;
        });
    });
};
const del = (id: number) => {
    tableData.value = tableData.value.filter((product: any) => getRowId(product) !== id);
    let idArr: any[] = [];
    tableData.value.map((item: any) => {
        idArr.push(getRowId(item));
    });
    ids.value = idArr;
};
const shippingTplList = ref<any[]>([]);
const categoryList = ref<CategoryFilterState[]>([]);
//首字母列表
const firstWordList = ref<string[]>([]);
// 实际数据
const brandList = ref<BrandFilterState[]>([]);
// 全部品牌
const allBrandList = ref<BrandFilterState[]>([]);
const loadFilter = async () => {
    try {
        const shippingTpl = await getShippingTplList();
        shippingTplList.value = shippingTpl;
        const category = await getAllCategoryList();
        categoryList.value = category;
        const brand = await getBrandSearch();
        firstWordList.value = brand.firstWordList;
        brandList.value = brand.brandList;
        allBrandList.value = brand.brandList;
        // 商品服务列表（用于多选下拉）
        const getServicesApi = adminType === "admin" ? getProductServicesList : getProductBatchServicesList;
        const services = await getServicesApi({
            page: 1,
            size: 999,
            sortField: "",
            sortOrder: "",
            keyword: ""
        });
        productServiceList.value = services.records || [];
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});

// 表单通过验证后提交
const onSubmit = async () => {
    if (tableData.value.length < 1) {
        message.error(t('未选择商品'));
        return;
    }
    if (form.value.type.length < 1) {
        message.error(t('未选择修改项'));
        return;
    }
    let data = filterProducts();
    if (validateProducts(data)) {
        try {
            const submitApi = isVendorAdmin ? vendorBatchProduct : BatchProduct;
            await submitApi({ editItems: data });
            message.success(t('操作成功'));
            onReset();
        } catch (error: any) {
            message.error(error.message);
        }
    }
};
const onReset = () => {
    tableData.value = [];
    form.value.type = [];
    ids.value = [];
};
// 提交前的验证函数
const validateProducts = (data: any) => {
    if (isVendorAdmin) return true;
    for (let product of data) {
        if (product.productName == "" || product.productSn == "") {
            message.error(t('商品名称或商品货号不能为空'));
            return false; // 验证失败，直接返回
        }
    }
    return true; // 验证通过
};
const filterProducts = () => {
    // 提取校验列表中所有的key
    const additionalKeys = ["productId", "id"];
    const keysFromValidation = form.value.type.map((item) => item.key);
    const keysToKeep = [...keysFromValidation, ...additionalKeys];
    // 处理商品列表，仅保留校验列表中的key对应的值
    let list = tableData.value.map((product: any) => {
        const filteredProduct: any = {};
        keysToKeep.forEach((key: any) => {
            if (product[key] !== undefined) {
                filteredProduct[key] = product[key];
            }
        });
        if (isVendorAdmin) {
            filteredProduct.id = Number(filteredProduct.productId ?? filteredProduct.id ?? product.productId ?? product.id);
            delete filteredProduct.productId;
            if (filteredProduct.productStatus !== undefined) {
                filteredProduct.productState = filteredProduct.productStatus;
                delete filteredProduct.productStatus;
            }
            if (filteredProduct.categoryId !== undefined) {
                filteredProduct.productCategoryId = filteredProduct.categoryId;
                delete filteredProduct.categoryId;
            }
            if (filteredProduct.brandId !== undefined) {
                filteredProduct.productBrandId = filteredProduct.brandId;
                delete filteredProduct.brandId;
            }
        }
        return filteredProduct;
    });
    return list;
};
</script>
<style lang="less" scoped>
.list-table-tool-row {
    margin-bottom: 20px;
}
.flex-column-center {
    display: flex;
    flex-direction: column;
    align-items: center;
}
</style>
