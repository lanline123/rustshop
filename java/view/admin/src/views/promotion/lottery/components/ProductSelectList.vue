<template>
    <div class="product-select-list">
        <div class="search-bar">
            <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit" inline>
                <el-form-item :label="$t('商品名称')">
                    <TigInput
                        v-model="filterParams.keyword"
                        name="keyword"
                        :placeholder="$t('输入商品名称/货号')"
                        clearable
                        @clear="onSearchSubmit"
                        @keyup.enter="onSearchSubmit"
                    >
                        <template #append>
                            <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                        </template>
                    </TigInput>
                </el-form-item>
                <el-form-item :label="$t('分类')">
                    <SelectCategory v-model:categoryId="filterParams.categoryId" @change="onSearchSubmit"></SelectCategory>
                </el-form-item>
            </el-form>
        </div>
        <div class="table-container">
            <a-spin :spinning="loading">
                <el-table
                    ref="tableRef"
                    :data="filterState"
                    :loading="loading"
                    @select="handleSelectChange"
                    @select-all="handleSelectAll"
                    row-key="productId"
                    class="single-select-table"
                >
                    <el-table-column type="selection" width="50" />
                    <el-table-column :label="$t('商品名称')" prop="productName" min-width="250">
                        <template #default="{ row }">
                            <div class="flex">
                                <div v-if="row.picThumb" class="span-pic">
                                    <img :src="imageFormat(row.picThumb)" height="50" width="50" alt="" style="object-fit: cover" />
                                </div>
                                <div class="span-product-con">
                                    <div class="span-product-name" style="word-break: break-all">
                                        {{ row.productName }}
                                    </div>
                                </div>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('商品信息')" width="180">
                        <template #default="{ row }">
                            <div>
                                <div>
                                    <span class="span-tit">{{ $t('编码：') }}</span>
                                    <span class="span-con">{{ row.productSn || "--" }}</span>
                                </div>
                                <div>
                                    <span class="span-tit">{{ $t('售价：') }}</span>
                                    <span class="span-con">{{ row.productPrice || 0.0 }}</span>
                                </div>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('规格')" width="200">
                        <template #default="{ row }">
                            <div v-if="hasMultiSku(row)">
                                <div class="sku-text"
                                    v-if="getSelectedSkuText(row)"
                                >
                                    {{ getSelectedSkuText(row) }}
                                </div>
                                <span v-else class="sku-tip">{{ $t('多规格商品，请选择规格') }}</span>
                            </div>
                            <span v-else class="span-con">--</span>
                        </template>
                    </el-table-column>
                    <template #empty>
                        <div class="empty-warp">
                            <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                        </div>
                    </template>
                </el-table>
            </a-spin>
            <div class="pagination-con" v-if="total > 0">
                <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
            </div>
        </div>

        <!-- 多规格选择弹窗 -->
        <el-dialog
            v-model="skuDialogVisible"
            :title="$t('选择商品规格')"
            width="600px"
            :close-on-click-modal="false"
            @closed="handleSkuDialogClosed"
        >
            <SelectSku
                v-if="skuDialogVisible && currentSkuProduct"
                ref="selectSkuRef"
                :productInfo="currentSkuProduct"
                :isMultiple="false"
                :show-stock-column="true"
            />
            <template #footer>
                <el-button @click="handleSkuDialogCancel">{{ $t('取消') }}</el-button>
                <el-button type="primary" @click="confirmSkuSelection">{{ $t('确认') }}</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue';
import { Pagination } from "@/components/list";
import { getProductList } from "@/api/product/product";
import { SelectCategory } from "@/components/select";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { ProductFilterState, ProductFilterParams } from "@/types/product/product";
import { imageFormat } from "@/utils/format";
import { ElMessage } from 'element-plus';
import SelectSku from "@/views/promotion/productGift/src/SelectSku.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();

const props = defineProps({
    selectedProductId: {
        type: Number,
        default: 0
    },
    selectedSkuId: {
        type: Number,
        default: 0
    },
    isDialog: {
        type: Boolean,
        default: false
    },
    visible: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['submitCallback', 'callback', 'close']);

const confirmLoading = defineModel('confirmLoading', {
    type: Boolean,
    default: false
});

const tableRef = ref<any>(null);
const selectedRow = ref<ProductFilterState | null>(null);
const selectedSkuIds = ref<number[]>([]);

// 多规格选择弹窗相关
const skuDialogVisible = ref(false);
const currentSkuProduct = ref<any>(null);
const selectSkuRef = ref<any>(null);
const skuDialogOpenedForRow = ref<ProductFilterState | null>(null);

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit
} = useListRequest<ProductFilterState, ProductFilterParams>({
    apiFunction: getProductList,
    idKey: "productId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        categoryId: "",
        productStatus: 1,
        checkStatus: 1,
        isDelete: 0,
        isLottery: 1,
    } as any
});

// 判断商品是否有多规格
const hasMultiSku = (row: ProductFilterState) => {
    const skuList: any[] | undefined = (row as any)?.productSku;
    if (!Array.isArray(skuList) || skuList.length === 0) return false;
    return skuList.some((sku: any) => {
        const skuData = sku?.skuData;
        if (Array.isArray(skuData)) return skuData.length > 0;
        if (skuData && typeof skuData === 'object') return Object.keys(skuData).length > 0;
        return Boolean(skuData);
    });
};

// 获取已选规格的文本
const getSelectedSkuText = (row: ProductFilterState) => {
    if (!selectedRow.value || selectedRow.value.productId !== row.productId) {
        return '';
    }
    if (selectedSkuIds.value.length === 0) {
        return '';
    }
    const skuId = selectedSkuIds.value[0];
    const sku = row.productSku?.find((s: any) => s.skuId === skuId);
    if (sku && sku.skuData) {
        return sku.skuData.map((d: any) => d.value).join(' / ');
    }
    return '';
};

// 单选处理
const handleSelectChange = (selection: ProductFilterState[], row: ProductFilterState) => {
    // 清除其他选中，只保留当前行
    tableRef.value?.clearSelection();
    if (selection.length > 0) {
        tableRef.value?.toggleRowSelection(row, true);

        // 如果是多规格商品，弹出规格选择
        if (hasMultiSku(row)) {
            selectedRow.value = row;
            selectedSkuIds.value = [];
            skuDialogOpenedForRow.value = row;
            currentSkuProduct.value = {
                productSku: row.productSku,
                skuIds: []
            };
            skuDialogVisible.value = true;
        } else {
            selectedRow.value = row;
            selectedSkuIds.value = [];
        }
    } else {
        selectedRow.value = null;
        selectedSkuIds.value = [];
    }
};

// 取消规格选择：允许直接关闭；若未选规格则回滚本次商品选择
const handleSkuDialogCancel = () => {
    skuDialogVisible.value = false;
};

const handleSkuDialogClosed = () => {
    // 关闭规格弹窗后，若依然没有选中规格，则清掉本次选择，避免“选中商品但无法取消/关闭外层”
    if (selectedSkuIds.value.length === 0 && skuDialogOpenedForRow.value) {
        tableRef.value?.clearSelection();
        selectedRow.value = null;
    }
    skuDialogOpenedForRow.value = null;
    currentSkuProduct.value = null;
};

// 禁止全选
const handleSelectAll = (selection: ProductFilterState[]) => {
    tableRef.value?.clearSelection();
    if (selectedRow.value) {
        const row = filterState.value.find((item: ProductFilterState) => item.productId === selectedRow.value?.productId);
        if (row) {
            tableRef.value?.toggleRowSelection(row, true);
        }
    }
};

// 确认规格选择
const confirmSkuSelection = () => {
    if (selectSkuRef.value) {
        const skuIds = selectSkuRef.value.skuIds;
        if (!skuIds || skuIds.length === 0) {
            ElMessage.warning(t('请选择商品规格'));
            return;
        }
        selectedSkuIds.value = skuIds;
        skuDialogVisible.value = false;
    }
};

// 回显已选中的商品
const restoreSelection = () => {
    if (props.selectedProductId && filterState.value.length > 0) {
        const row = filterState.value.find((item: ProductFilterState) => item.productId === props.selectedProductId);
        if (row) {
            nextTick(() => {
                tableRef.value?.toggleRowSelection(row, true);
                selectedRow.value = row;
                if (props.selectedSkuId) {
                    selectedSkuIds.value = [props.selectedSkuId];
                }
            });
        }
    }
};

// 监听数据加载完成后回显
watch(() => filterState.value, () => {
    restoreSelection();
}, { immediate: true });

// 表单提交方法（供DialogForm组件调用）
const onFormSubmit = async () => {
    if (!selectedRow.value) {
        ElMessage.warning(t('请选择一个商品'));
        return Promise.reject('未选择商品');
    }

    // 如果是多规格商品，必须选择规格
    if (hasMultiSku(selectedRow.value) && selectedSkuIds.value.length === 0) {
        ElMessage.warning(t('请选择商品规格'));
        return Promise.reject('未选择商品规格');
    }

    // 构建返回数据
    const result: any = {
        productId: selectedRow.value.productId,
        productName: selectedRow.value.productName,
        productSn: selectedRow.value.productSn,
        productPrice: selectedRow.value.productPrice,
        picThumb: selectedRow.value.picThumb,
        productStock: selectedRow.value.productStock
    };

    // 统一使用 JSON 格式存储 prizeValue
    const prizeValueObj: any = { productId: selectedRow.value.productId };

    // 如果有选择规格，添加规格信息
    if (selectedSkuIds.value.length > 0) {
        const skuId = selectedSkuIds.value[0];
        result.skuId = skuId;
        result.skuText = getSelectedSkuText(selectedRow.value);
        prizeValueObj.skuId = skuId;

        // 将已选规格的库存写入 productStock
        const sku = selectedRow.value.productSku?.find((s: any) => Number(s?.skuId) === Number(skuId));
        if (sku && sku.skuStock != null) {
            result.productStock = sku.skuStock;
        }
    } else if (selectedRow.value.productSku?.length) {
        // result.skuId = selectedRow.value.productSku[0].skuId;
        const sku0 = selectedRow.value.productSku[0] as any;
        prizeValueObj.skuId = sku0.skuId;
        if (sku0 && sku0.skuStock != null) {
            result.productStock = sku0.skuStock;
        }
    }

    result.prizeValueJson = JSON.stringify(prizeValueObj);

    emit('submitCallback', result);
};

onMounted(() => {
    loadFilter();
});

defineExpose({
    onFormSubmit
});
</script>

<style scoped lang="less">
.product-select-list {
    padding: 20px;

    .search-bar {
        margin-bottom: 15px;
    }

    .table-container {
        margin-top: 10px;
    }

    .pagination-con {
        margin-top: 15px;
    }

    .span-pic {
        margin-right: 10px;
        flex-shrink: 0;

        img {
            border: 1px solid #eee;
        }
    }

    .span-product-con {
        flex: 1;

        .span-product-name {
            font-size: 14px;
            color: #333;
            line-height: 1.4;
        }
    }

    .span-tit {
        color: #999;
    }

    .span-con {
        color: #333;
    }

    .sku-tip {
        color: #f56c6c;
        font-size: 14px;
    }

    // 隐藏表头的全选复选框，实现单选效果
    .single-select-table {
        :deep(.el-table__header-wrapper .el-table__header .el-checkbox) {
            display: none;
        }
    }
}
</style>

