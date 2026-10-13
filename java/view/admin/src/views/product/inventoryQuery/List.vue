<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" name="form" @keyup.enter="onSearchSubmit" @submit.prevent>
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('库存搜索：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput v-model="filterParams.keyword"
                                                      :placeholder="$t('请输入商品名称/商品规格')" clearable>
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span
                                                        class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <label class="control-label"></label>
                                    <div class="control-container">
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                        <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total"
                                  @sort-change="onSortChange">
                            <el-table-column :label="$t('商品名称')" prop="productName" min-width="250">
                                <template #default="{ row }">
                                    <div class="productNameBox">
                                        <div v-if="row.productPicThumb" class="span-pic">
                                            <img :src="imageFormat(row.productPicThumb)" height="40" width="40" />
                                        </div>
                                        <div><span class="span-text">{{ row.productName }}</span></div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商品规格')" prop="productSkuName" :formatter="(row: any) => row.productSkuName || '--'"></el-table-column>
                            <!--                            <el-table-column label="单位" prop="productUnit" :formatter="(row: any) => row.productUnit || '&#45;&#45;'"></el-table-column>-->
                            <el-table-column :label="$t('可用库存')" prop="currentProductStock"></el-table-column>
                            <el-table-column :label="$t('成本')" prop="productCost" :formatter="(row: any) => row.productCost === 0 || row.productCost ? row.productCost : '--'"></el-table-column>
                            <el-table-column :label="$t('操作')" prop="desc">
                                <template #default="{ row }">
                                    <DialogForm :maskClose="false"
                                                :showClose="false"
                                                :showFooter="false"
                                                :params="{
                                                    act: 'detail',
                                                    id: row.productId,
                                                    sukId: row.productSkuId
                                                }" isDrawer path="product/inventoryQuery/Logs" :title="$t('出入库明细')" width="950px"
                                                @okCallback="loadFilter">
                                        <a class="btn-link">{{ $t('出入库明细') }}</a>
                                    </DialogForm>
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
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import '@/style/css/list.less'
import {useConfigStore} from "@/store/config";
import {Pagination} from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { useListRequest } from "@/hooks/useListRequest";
import { InventoryFilterState, InventoryFilterParams } from "@/types/product/inventoryQuery";
import { getInventoryList } from "@/api/product/inventoryQueryList";
import { imageFormat } from "@/utils/format";
const config: any = useConfigStore();

const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    resetParams,
    onBatchAction
} = useListRequest<InventoryFilterState, InventoryFilterParams>({
    apiFunction: getInventoryList,
    idKey: "productId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        keyword: '',
        stockSearch: 1, // 库存查询列表单独加
    }
});

// 初始化加载
loadFilter();
</script>
<style lang="less" scoped>
.productNameBox{
    display: flex;
    align-items: center;
}
:deep(.el-table .cell) {
    display: flex;
    align-items: center;
}

:deep(.el-table .caret-wrapper) {
    margin-top: 5px;
}

:deep(.el-select__wrapper) {
    width: 110px;
}
</style>
