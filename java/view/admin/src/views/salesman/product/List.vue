<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="advanced-search-warp list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('商品名称：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.productName"
                                            name="productName"
                                            :placeholder="$t('输入商品名称/货号')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        ></TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('商品状态：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.status" clearable @change="onSearchSubmit">
                                            <el-option :label="$t('上架')" :value="1" />
                                            <el-option :label="$t('下架')" :value="0" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('推广类型：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.salesmanProductType" clearable @change="onSearchSubmit">
                                            <el-option v-for="(item, index) in salesmanProductTypeList" :label="item" :value="index" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                            </div>
                            <div class="simple-form-field">
                                <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :total="total" row-key="productId" @selection-change="onSelectChange" @sort-change="onSortChange">
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('商品')" prop="productId" :min-width="150">
                                <template #default="{ row }">
                                    <div class="flex">
                                        <div v-if="row.picThumb" class="span-pic">
                                            <a :href="urlFormat({ path: 'product', sn: row.productSn })" target="_blank"
                                                ><img :src="imageFormat(row.picThumb)" height="50" width="50"
                                            /></a>
                                        </div>
                                        <div class="span-product-con">
                                            <div class="span-product-name">
                                                <!-- <PopForm
                                                    :max="100"
                                                    v-model:org-value="row.productName"
                                                    :params="{ id: row.productId, field: 'productName' }"
                                                    :requestApi="updateSalesmanProductFiled"
                                                    label="商品名称"
                                                    type="textarea"> -->
                                                <div>{{ row.productName }}</div>
                                                <!-- </PopForm> -->
                                            </div>
                                            <p v-if="row.brandName" class="span-product-brand">
                                                <span class="span-tit">{{ $t('商品：') }}</span><span class="span-con">{{ row.brandName }}</span>
                                            </p>
                                            <p v-if="row.storeTitle" class="span-product-brand">
                                                <span class="span-tit">{{ $t('店铺：') }}</span><span class="span-con green">{{ row.storeTitle }}</span>
                                            </p>
                                            <p v-if="row.suppliersName" class="span-product-brand">
                                                <span class="span-tit">{{ $t('供应商：') }}</span><span class="span-con green">{{ row.suppliersName }}</span>
                                            </p>
                                            <p v-if="row.type === 2"><span class="span-con green">{{ $t('【虚拟商品】') }}</span></p>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="`${$t('金额')}(${config.config.dollarSignCn})`">
                                <template #default="{ row }">
                                    {{ priceFormat(row.productPrice) }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('库存')">
                                <template #default="{ row }">
                                    {{ row.productStock }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否参与推广')" width="130">
                                <template #default="{ row }">
                                    {{ row.salesmanProduct && row.salesmanProduct.isJoin == 1 ? $t('参与') : $t('不参与') }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('佣金')" width="130">
                                <template #default="{ row }">
                                    {{ row.commissionType == 1 ? $t('默认佣金比例') : row.commissionType == 2 ? $t('自定义佣金比例') : $t('自定义佣金金额') }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商品佣金')" width="250">
                                <template #default="{ row }">
                                    {{ row.salesmanProduct?.productCommission.productCommission || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('下级分销员邀请奖励')" width="250">
                                <template #default="{ row }">
                                    {{ row.salesmanProduct?.productCommission.subCommission || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商品状态')">
                                <template #default="{ row }">
                                    {{ row.productStatus === 1 ? $t('上架') : $t('下架') }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作时间')">
                                <template #default="{ row }">
                                    {{ row.salesmanProduct?.updateTime }}
                                </template>
                            </el-table-column>
                            <el-table-column :width="120" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <DialogForm
                                            :maskClose="false"
                                            :params="{
                                                act: row.salesmanProduct?.salesmanProductId ? 'detail' : 'add',
                                                id: row.productId,
                                                salesmanProduct: row.salesmanProduct
                                            }"
                                            isDrawer
                                            path="salesman/product/Info"
                                            :title="$t('设置商品佣金')"
                                            width="800px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t('设置佣金') }}</a>
                                        </DialogForm>
                                    </el-space>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
                <div v-if="selectedIds.length > 0" class="selected-action-warp selected-warp-left">
                    <div class="selected-action">
                        <el-space>
                            <span
                                >{{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }}</span
                            >
                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                <template #reference> </template>
                            </el-popconfirm>
                        </el-space>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { ref } from "vue";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { productFilterParams, productFilterState } from "@/types/salesman/product";
import { batchSubmit, getSalesmanProductList, getSalesmanProductnConfig } from "@/api/salesman/product";
import { imageFormat, urlFormat, priceFormat } from "@/utils/format";
const config: any = useConfigStore();
import { useListRequest } from "@/hooks/useListRequest";
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
    onBatchAction,
    resetParams
} = useListRequest<productFilterState, productFilterParams>({
    apiFunction: getSalesmanProductList,
    idKey: "productId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        productName: "",
        status: "",
        salesmanProductType: ""
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();

const salesmanProductTypeList = ref();
const _getSalesmanProductnConfig = async () => {
    try {
        const result = await getSalesmanProductnConfig();
        salesmanProductTypeList.value = result;
    } catch (error: any) {
        message.error(error.message);
    }
};
_getSalesmanProductnConfig();
</script>
<style lang="less" scoped>
.status-switch {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    width: 150px;
}

.status-switch > div {
    word-break: keep-all;
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
}
.list-table-tool {
    margin-bottom: 0;
}
</style>
