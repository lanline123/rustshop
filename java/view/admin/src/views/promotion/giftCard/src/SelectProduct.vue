<template>
    <div class="container" style="position: relative; height: 100%">
        <div class="content_wrapper" style="width: 100%; height: 100%; position: absolute">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp" style="flex: 0; padding-right: 15px">
                    <el-form :model="filterParams" name="form" @finish="searchSubmit">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <SelectCategory v-model:categoryId="filterParams.categoryId" style="width: 150px"></SelectCategory>
                                    <!-- <SelectBrand v-model:brandId="filterParams.brandId"></SelectBrand> -->
                                    <TigInput width="230px" v-model="filterParams.keyword" :placeholder="$t('商品名称/货号')" @keyup.enter="searchSubmit" />
                                    <el-button @click="searchSubmit">{{ $t('搜索') }}</el-button>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <perfect-scrollbar class="table-container" style="flex: 1">
                    <a-spin :spinning="loading">
                        <div style="padding-right: 15px; box-sizing: border-box">
                            <el-table
                                ref="tableRef"
                                :class="isMultiple ? '' : 'hide-checkbox'"
                                :data="filterState"
                                :loading="loading"
                                :total="total"
                                class="result-table"
                                :row-key="(row: any) => row.productId + '-' + row.skuId"
                                @select="selectRow"
                                @selection-change="onSelectChange"
                            >
                                <el-table-column :selectable="isSelectable" type="selection" width="32" />
                                <el-table-column :label="$t('商品信息')" min-width="260">
                                    <template #default="{ row }">
                                        <div class="flex product-cell">
                                            <div v-if="row.picUrl" class="span-pic">
                                                <a :href="row.url" target="_blank"
                                                    ><img class="product-img" :src="imageFormat(row.picUrl)" height="40" width="40"
                                                /></a>
                                            </div>
                                            <div class="product-info">
                                                <p class="product-name">{{ row.productName }}</p>
                                                <p class="gray">{{ $t('货号：{productSn}', { productSn: row.productSn }) }}</p>
                                            </div>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('规格')" width="180" :show-overflow-tooltip="true">
                                    <template #default="{ row }">
                                        <div class="sku-text">{{ getSkuInfo(row.skuData) }}</div>
                                    </template>
                                </el-table-column>
                                <template #empty>
                                    <div class="empty-warp">
                                        <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                    </div>
                                </template>
                            </el-table>
                        </div>
                    </a-spin>
                </perfect-scrollbar>
                <div v-if="total > 0" class="pagination-con" style="flex-shrink: 0">
                    <Pagination
                        v-model:page="filterParams.page"
                        v-model:size="filterParams.size"
                        :total="total"
                        @callback="onPageChanged"
                        layout="slot ,prev, pager, next"
                        :background="false"
                    />
                </div>
            </div>
            <div class="selected-container">
                <el-tabs v-model="cuurentIndex" tab-position="top" @tab-change="onTabChange">
                    <el-tab-pane :name="1" :label="tabName"></el-tab-pane>
                </el-tabs>
                <div class="product-list">
                    <div v-for="(item, index) in selectedProducts" :key="item.productId + '-' + item.skuId" class="product-item">
                        <div class="product-content">
                            <div class="product-image">
                                <img :src="imageFormat(item.picUrl)" alt="商品图片" />
                            </div>
                            <div class="product-info">
                                <div class="product-title">{{ item.productName }}</div>
                                <div class="product-sku">{{ getSkuInfo(item.skuData) }}</div>
                            </div>
                            <div class="delete-icon" @click="removeSelectedProduct(index)">
                                <el-icon><Delete /></el-icon>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { SelectCategory } from "@/components/select";
import { onMounted, reactive, ref, nextTick, computed } from "vue";
import { message } from "ant-design-vue";
import { imageFormat } from "@/utils/format";
import { Pagination } from "@/components/list";
import { ProductFilterParams, SkuProductFilterState } from "@/types/product/product";
import { useConfigStore } from "@/store/config";
import { getSkuProductList } from "@/api/product/product";
import { Delete } from "@element-plus/icons-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    // 已选择的项，用于排除重复，禁止选择
    selectedIds: { type: Array, default: [] },
    selectedProducts: { type: Array, default: [] },
    modelValue: {
        type: Object,
        default: {}
    },
    // 业务筛选参数：由外部弹窗传入（例如：抽奖奖品选择商品）
    productType: {
        type: Array,
        default: () => []
    },
    isSaleMode: {
        type: Number,
        default: undefined
    },
    isMultiple: {
        type: Boolean,
        default: true
    },
    excludeProductTypes: {
        type: Array,
        default: () => []
    }
});
const emit = defineEmits(["submitCallback", "okType", "update:modelValue"]);
emit("okType", false);
// 全局存储已选中的商品ID（用于跨页保持）
const selectedIds = ref<any[]>([]);
// 存储已选商品的详细信息（仅当前显示的商品）
const selectedProducts = ref<any[]>([]);
const isPaginationChange = ref(false); // 防止翻页时触发onSelectChange事件的标志
const loading = ref(true); //列表加载中
const total = ref<number>(0);
const config = useConfigStore();
const filterState = ref<SkuProductFilterState[]>([]);
const filterParams = reactive<ProductFilterParams>({
    page: 1,
    size: (config as any).get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: "",
    categoryId: undefined
});
const cuurentIndex = ref(1);
const tabName = computed(() => {
    if (selectedProducts.value.length == 0) {
        return t('已选择商品');
    }
    return t('已选择商品(') + selectedProducts.value.length + ")";
});

const getSkuInfo = (skuData: string) => {
    if (!skuData || !skuData.length) {
        return "-";
    }
    try {
        const skuInfo = JSON.parse(skuData);
        return skuInfo
            .map((item: any) => {
                return item.name + ": " + item.value;
            })
            .join("; ");
    } catch {
        return "-";
    }
};

// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        if (filterParams.categoryId === 0) {
            filterParams.categoryId = "";
        }
        const result = await getSkuProductList({
            ...(filterParams as any),
            ...(props.productType?.length ? { productType: props.productType } : {}),
            ...(props.isSaleMode !== undefined ? { isSaleMode: props.isSaleMode } : {}),
            ...(props.excludeProductTypes?.length ? { excludeProductTypes: props.excludeProductTypes } : {})
        } as any);
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        initChecked();
        loading.value = false;
    }
};

const onPageChanged = async (page: number) => {
    await loadFilter();
};

const initChecked = () => {
    isPaginationChange.value = true;
    nextTick(() => {
        // 更新已选商品的选中状态
        if (selectedProducts.value.length > 0 && tableRef.value) {
            filterState.value.forEach((item) => {
                const isSelected = selectedProducts.value.some((selected) => selected.productId === item.productId && selected.skuId === item.skuId);
                if (isSelected) {
                    tableRef.value.toggleRowSelection(item, true);
                }
            });
        }
        isPaginationChange.value = false;
    });
};

const onTabChange = (value: number) => {
    cuurentIndex.value = value;
};

// 参数查询
const searchSubmit = () => {
    loadFilter();
};

const tableRef: any = ref();
const onSelectChange = (selectedRows: any) => {
    // 如果是翻页导致的事件触发，则不做任何处理
    if (isPaginationChange.value) {
        return;
    }

    if (props.isMultiple) {
        // 多选情况下更新selectedIds和selectedProducts
        // 先清空当前页的数据
        const currentPageProductIds = filterState.value.map((item) => `${item.productId}-${item.skuId}`);
        selectedIds.value = selectedIds.value.filter((item) => !currentPageProductIds.includes(`${item.productId}-${item.skuId}`));
        selectedProducts.value = selectedProducts.value.filter((item) => !currentPageProductIds.includes(`${item.productId}-${item.skuId}`));

        // 添加当前选中的数据
        selectedIds.value.push(
            ...selectedRows.map((item: any) => ({
                productId: item.productId,
                skuId: item.skuId
            }))
        );
        selectedProducts.value.push(...selectedRows);

        emit("okType", selectedIds.value.length > 0);
    }
};

const selectRow = (selection: any, val: any) => {
    if (!props.isMultiple) {
        // 单选
        tableRef.value.clearSelection();
        if (selection.length == 0) {
            selectedIds.value = []; // 清空data中绑定的selectedRow
            selectedProducts.value = [];
            return;
        }
        tableRef.value.toggleRowSelection(val, true);
        selectedIds.value.length = 0;
        selectedIds.value.push({
            productId: val.productId,
            skuId: val.skuId
        });
        selectedProducts.value = [val];
        emit("okType", selectedIds.value.length > 0);
    } else {
        const indexInSelectedIds = selectedIds.value.findIndex((item: any) => item.productId === val.productId && item.skuId === val.skuId);
        const indexInSelectedProducts = selectedProducts.value.findIndex((item: any) => item.productId === val.productId && item.skuId === val.skuId);
        const isSelected = indexInSelectedIds > -1 && indexInSelectedProducts > -1;
        if (!isSelected) {
            // 添加到选中列表
            selectedIds.value.push({
                productId: val.productId,
                skuId: val.skuId
            });
            selectedProducts.value.push(val);
        } else {
            // 从选中列表中移除
            if (indexInSelectedIds > -1) {
                selectedIds.value.splice(indexInSelectedIds, 1);
            }
            if (indexInSelectedProducts > -1) {
                selectedProducts.value.splice(indexInSelectedProducts, 1);
            }
        }
    }
    emit("update:modelValue", val);
};

const isSelectable = (row: any, index: number) => {
    // 排除重复项
    // return !props.selectedIds?.some((item: any) => {
    //     return item.productId === row.productId && item.skuId === row.skuId;
    // });
    return true;
};

// 从已选商品列表中删除商品
const removeSelectedProduct = (index: number) => {
    const removedProduct = selectedProducts.value[index];
    selectedProducts.value.splice(index, 1);
    selectedIds.value = selectedIds.value.filter((item) => !(item.productId === removedProduct.productId && item.skuId === removedProduct.skuId));

    // 同步更新表格中的选中状态
    if (tableRef.value) {
        const row = filterState.value.find((item) => item.productId === removedProduct.productId && item.skuId === removedProduct.skuId);
        if (row) {
            tableRef.value.toggleRowSelection(row, false);
        }
    }

    emit("okType", selectedIds.value.length > 0);
};

// 根据selectedIds初始化已选中的商品信息
const initSelectedProducts = async () => {
    if (!props.selectedIds || props.selectedIds.length === 0) {
        return;
    }
    try {
        // 如果API调用失败，使用默认值
        const initialProducts = [...props.selectedIds];
        selectedIds.value = initialProducts.map((item: any) => {
            return {
                productId: item.productId,
                skuId: item.skuId
            };
        });
        selectedProducts.value = initialProducts;
        emit("okType", selectedIds.value.length > 0);
    } catch (error) {
        console.error("初始化已选商品信息失败:", error);
    }
};

onMounted(async () => {
    initSelectedProducts();
    loadFilter();
    // initChecked();
});

// 弹窗回调
const onFormSubmit = () => {
    // 弹窗确认按钮提交
    emit("submitCallback", selectedProducts.value);
};

// 修改排序

defineExpose({
    onFormSubmit
});
</script>
<style lang="less" scoped>
.table-container {
    .span-pic {
        display: flex;
        align-items: center;
        flex-shrink: 0;
        margin-right: 8px;
    }
    .product-cell {
        align-items: center;
        min-width: 0;
    }
    .product-info {
        display: flex;
        flex-direction: column;
        justify-content: center;
        min-width: 0;
        flex: 1;
        .product-name {
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            overflow: hidden;
            text-overflow: ellipsis;
            min-width: 0;
            word-break: break-word;
        }
    }
}

.content_wrapper {
    display: flex;
    gap: 20px;
    min-width: 0;
    .lyecs-table-list-warp {
        flex: 1;
        min-width: 0;
        margin-bottom: 0;
    }
    .selected-container {
        width: 430px;
        flex-shrink: 0;
        background: #f5f7fa;
        padding: 12px 20px 12px 20px;
        box-sizing: border-box;

        .product-list {
            max-height: calc(100% - 54px);
            overflow-y: auto;

            .product-item {
                margin-bottom: 10px;
                background: white;
                border-radius: 4px;
                padding: 10px;

                .product-content {
                    display: flex;
                    align-items: center;

                    .product-image {
                        width: 60px;
                        height: 60px;
                        margin-right: 10px;
                        flex-shrink: 0;

                        img {
                            width: 100%;
                            height: 100%;
                            object-fit: cover;
                        }
                    }

                    .product-info {
                        flex: 1;
                        min-width: 0;

                        .product-title {
                            font-weight: 500;
                            margin-bottom: 5px;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            white-space: nowrap;
                        }

                        .product-sku {
                            font-size: 12px;
                            color: #666;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            white-space: nowrap;
                        }
                    }

                    .delete-icon {
                        width: 30px;
                        height: 30px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        cursor: pointer;
                        color: #999;
                        flex-shrink: 0;

                        &:hover {
                            color: #f56c6c;
                        }
                    }
                }
            }
        }
    }
}

.product-img {
    object-fit: cover;
}

.sku-text {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.pagination-con {
    background: #fff;
}

:deep(.hide-checkbox .el-table__header-wrapper .el-table__header .el-checkbox) {
    display: none;
}

@media only screen and (max-width: 767px) {
    .content_wrapper {
        flex-direction: column;
        gap: 0;
        overflow: hidden;

        .lyecs-table-list-warp {
            flex: 1;
            width: 100%;
            min-width: 0;
            min-height: 0;
            gap: 8px;
            overflow: hidden;
        }

        .list-table-tool {
            padding-right: 0 !important;

            :deep(.el-space) {
                flex-wrap: wrap;
            }

            :deep(.el-space__item) {
                margin-bottom: 8px;
            }
        }

        .table-container {
            min-height: min(240px, 40vh);

            > div {
                padding-right: 0 !important;
            }
        }

        .selected-container {
            width: 100%;
            flex-shrink: 0;
            height: auto;
            max-height: 28vh;
            min-height: 0;
            padding: 8px 12px;
            border-top: 1px solid #e4e7ed;
            overflow: hidden;

            .product-list {
                max-height: calc(28vh - 46px);
                overflow-y: auto;
            }
        }
    }
}
</style>
