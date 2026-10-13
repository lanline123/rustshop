/** * @file 添加批发商品（DialogForm 内容组件） * @description 由通用抽屉 DialogForm 通过 path 加载。商品库选择器：支持列表内单个商品逐个添加， *
也支持勾选多个批量添加（batchAdd 一次提交）。已是批发商品的行禁用勾选并展示「已是批发商品」。 * 每次新增成功 emit("callback") 通知父级刷新列表。 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" name="form" @keyup.enter="onSearchSubmit" @submit.prevent>
                        <div class="list-table-tool-row advanced-search-warp">
                            <div class="list-table-tool-col search-wrap">
                                <div class="search-row">
                                    <SelectCategory v-model:categoryId="filterParams.categoryId" class="search-category" />
                                    <el-select v-model="filterParams.productStatus" :placeholder="$t('选择状态')" class="search-status" @change="onSearchSubmit">
                                        <el-option :label="$t('上架')" :value="1" />
                                        <el-option :label="$t('下架')" :value="0" />
                                    </el-select>
                                    <TigInput v-model="filterParams.keyword" class="search-keyword" :placeholder="$t('商品名称/货号')" />
                                    <el-button class="btn" @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <el-button :disabled="selectedIds.length === 0" :loading="batchAdding" @click="onBatchAdd"> {{ $t('批量添加批发商品') }} </el-button>
                                    <span v-if="selectedIds.length > 0"
                                        >{{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }}</span
                                    >
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table ref="tableRef" :data="listData" row-key="productId" @selection-change="onTableSelectChange">
                            <el-table-column type="selection" width="42" :selectable="isSelectable" :reserve-selection="true" />
                            <el-table-column :label="$t('商品名称')" min-width="320">
                                <template #default="{ row }">
                                    <div class="flex">
                                        <img v-if="row.picThumb" :src="imageFormat(row.picThumb)" class="mr10 product-image" />
                                        <div>
                                            <p>{{ row.productName }}</p>
                                            <p class="gray">{{ $t('货号：{productSn}', { productSn: row.productSn }) }}</p>
                                            <p class="price">{{ priceFormat(row.productPrice) }}</p>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" width="160">
                                <template #default="{ row }">
                                    <span v-if="addedIds.has(row.productId)" class="gray">{{ $t('已是批发商品') }}</span>
                                    <a v-else class="btn-link" @click="onAddOne(row)">{{ $t('添加批发商品') }}</a>
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
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadData" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import "@/style/css/list.less";
import { ref, onMounted, nextTick } from "vue";
import { message } from "ant-design-vue";
import { SelectCategory } from "@/components/select";
import { Pagination } from "@/components/list";
import { imageFormat, priceFormat } from "@/utils/format";
import { useListRequest } from "@/hooks/useListRequest";
import { useConfigStore } from "@/store/config";
import { getSelectableWholesaleProductPage, addWholesaleProduct, batchAddWholesaleProduct } from "@/api/wholesale/product";
import type { SelectableWholesaleProductQuery } from "@/types/wholesale/product";
import { resolveBatchAddSelection, summarizeBatchAddResult } from "@/views/wholesale/contractLogic";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface ProductRow {
    productId: number;
    productName?: string;
    productPrice?: number | string;
    picThumb?: string;
    productSn?: string;
}

interface ProductFilter {
    page: number;
    size: number;
    keyword: string;
    categoryId: number | string;
    productStatus?: number;
}

const emit = defineEmits(["callback"]);

const config: any = useConfigStore();
const tableRef = ref<any>();
const batchAdding = ref(false);
const selectedIds = ref<number[]>([]);
/** 本次会话已添加的 productId 集合（接口已剔除已配置批发，故仅用于新增成功后即时标记本页行） */
const addedIds = ref<Set<number>>(new Set());

const initialParams: ProductFilter = {
    page: 1,
    size: config.get("pageSize"),
    keyword: "",
    categoryId: "",
    productStatus: 1
};

const { listData, loading, total, filterParams, loadData, onSearchSubmit } = useListRequest<ProductRow, ProductFilter>({
    apiFunction: (params: ProductFilter) => {
        // 仅透传接口文档定义的查询参数，分类为空时不下发
        const query: SelectableWholesaleProductQuery = {
            page: params.page,
            size: params.size,
            keyword: params.keyword || undefined,
            productStatus: params.productStatus
        };
        if (params.categoryId) query.categoryId = params.categoryId;
        return getSelectableWholesaleProductPage(query);
    },
    idKey: "productId",
    defaultParams: initialParams
});

const isSelectable = (row: ProductRow) => !addedIds.value.has(row.productId);

/** reserve-selection 模式下 el-table 会回传跨页全部已选行 */
const onTableSelectChange = (selection: ProductRow[]) => {
    selectedIds.value = selection.map((row) => row.productId);
};

/** 单个添加 */
const onAddOne = async (row: ProductRow) => {
    try {
        await addWholesaleProduct({ productId: row.productId });
        markAdded(row.productId);
        message.success(t('添加批发商品成功'));
        emit("callback");
    } catch (error: any) {
        // 5311：已配置批发能力，同样标记为已添加
        if (error?.code === 5311) {
            markAdded(row.productId);
            message.info(t('该商品已是批发商品'));
        } else if (error?.code === 5314 || error?.code === 5315) {
            message.error(error.message || t('添加批发商品失败'));
            loadData();
        } else {
            message.error(error.message || t('添加批发商品失败'));
        }
    }
};

/** 批量添加（batchAdd 一次提交，最多 50 个） */
const onBatchAdd = async () => {
    const ids = selectedIds.value.filter((id) => !addedIds.value.has(id));
    if (ids.length === 0) {
        message.warning(t('请勾选未添加的商品'));
        return;
    }
    if (ids.length > 50) {
        message.warning(t('批量添加单次最多 50 个商品'));
        return;
    }
    batchAdding.value = true;
    try {
        const result = await batchAddWholesaleProduct({ productIds: ids });
        const failedItems = result?.failedItems || [];
        const summary = summarizeBatchAddResult(result);
        const failedIds = new Set(failedItems.map((item) => item.productId));
        ids.filter((id) => !failedIds.has(id)).forEach((id) => markAdded(id));
        if (failedItems.length > 0) await loadData();
        tableRef.value?.clearSelection();
        await nextTick();
        const restoredIds = resolveBatchAddSelection(failedItems, listData.value.map((row) => row.productId));
        listData.value.filter((row) => restoredIds.includes(row.productId)).forEach((row) => tableRef.value?.toggleRowSelection(row, true));
        selectedIds.value = restoredIds;
        if (summary.severity === "warning") {
            message.warning(t('批量添加完成：成功 {successCount} 个，跳过 {skippedCount} 个，失败 {failedCount} 个。{failedDetails}', {
                successCount: summary.successCount,
                skippedCount: summary.skippedCount,
                failedCount: summary.failedCount,
                failedDetails: summary.failedDetails
            }));
        } else {
            message.success(t('批量添加批发商品成功'));
        }
        emit("callback");
    } catch (error: any) {
        message.error(error.message || t('批量添加批发商品失败'));
    } finally {
        batchAdding.value = false;
    }
};

const markAdded = (productId: number) => {
    const next = new Set(addedIds.value);
    next.add(productId);
    addedIds.value = next;
    selectedIds.value = selectedIds.value.filter((id) => id !== productId);
};

onMounted(() => {
    loadData();
});
</script>
<style lang="less" scoped>
.search-wrap {
    width: 100%;
}
.search-row {
    display: flex;
    width: 100%;
    gap: 8px;
    align-items: center;
    .search-category {
        width: 25%;
        min-width: 100px;
    }
    .search-status {
        width: 15%;
        min-width: 80px;
    }
    .search-keyword {
        width: 220px;
        flex-shrink: 0;
    }
    .btn {
        flex-shrink: 0;
        white-space: nowrap;
        line-height: 1;
        min-width: 60px;
        height: auto;
        margin-left: 0;
    }
}
.gray {
    color: #999;
}
.price {
    color: var(--tig-primary);
}
.pagination-con {
    justify-content: flex-end;
}
.product-image {
    width: 40px;
    height: 40px;
    border: 1px solid #eee;
}
</style>
