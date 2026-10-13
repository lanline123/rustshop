<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="mb20">
                <div class="title">
                    <p class="xian"></p> {{ $t('商品来源') }} </div>
                <div>
                    <RadioType v-model:modelValue="type" :radioList="radioList"></RadioType>
                </div>
            </div>
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" name="form">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('商品名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入商品名称/货号')"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field" v-if="adminType == 'admin'">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('供应商：') }}</span></label>
                                        <div class="control-container">
                                            <SelectVendor v-model:vendorId="filterParams.vendorId" @onChange="onSearchSubmit"></SelectVendor>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('分类：') }}</span></label>
                                        <div class="control-container">
                                            <SelectCategory ref="selectCategoryRef" v-model:categoryId="filterParams.productCategoryId"></SelectCategory>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('品牌：') }}</span></label>
                                        <div class="control-container">
                                            <SelectBrand v-model:brandId="filterParams.productBrandId" @onChange="onSearchSubmit"></SelectBrand>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('商品导入：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.isCanImport" :placeholder="$t('是否可导入')">
                                                <el-option :label="$t('可导入')" :value="1" />
                                                <el-option :label="$t('不可导入')" :value="0" />
                                            </el-select>
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
                        <div class="list-table-tool-row" v-if="adminType == 'vendor'">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <el-popconfirm v-if="activeKey == 1" :title="$t('您确认要批量断供吗？')" @confirm="onBatchSubmit('outage')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量断供') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                </el-space>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <el-popconfirm :title="$t('您确认要批量导入吗？')" @confirm="onBatchImport">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量导入') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <el-popconfirm :title="$t('您确认要批量上架吗？')" @confirm="onBatchUp('up')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量上架') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :total="total" row-key="id" @selection-change="onSelectChange" @sort-change="onSortChange">
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('商品名称')" prop="id" :min-width="320">
                                <template #default="{ row }">
                                    <div class="flex">
                                        <div v-if="row.picThumb" class="span-pic">
                                            <img :src="imageFormat(row.picThumb)" height="68" width="68" alt="" />
                                        </div>
                                        <div class="span-product-con">
                                            <div class="span-product-name" style="word-break: break-all">
                                                <div>{{ row.productName }}</div>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="100" :label="$t('商品分类')" prop="productCategoryName" align="center">
                                <template #default="{ row }">
                                    <div>{{ row.productCategoryName || "--" }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="120" :label="$t('供应商')" prop="vendorName" align="center">
                                <template #default="{ row }">
                                    <div>{{ row.vendorName }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="100" :label="$t('供货价')" prop="supplyPrice" sortable="custom" align="center">
                                <template #default="{ row }">
                                    <div>{{ row.supplyPrice }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="120" :label="$t('商品状态')" prop="productStatus">
                                <template #default="{ row }">
                                    <div v-if="row.productStatus === 1" class="green">{{ $t('上架') }}</div>
                                    <div v-else class="red">{{ $t('下架') }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="160" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <el-space>
                                        <DialogForm
                                            v-if="row.isImported === 1"
                                            :maskClose="false"
                                            :params="{ act: 'detail', id: row.productId, type: 'vendor', examine: 1 }"
                                            isDrawer
                                            path="product/product/Info"
                                            :title="$t('编辑商品')"
                                            width="950px"
                                            @okCallback="changeProduct"
                                        >
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <DialogForm
                                            v-else
                                            :maskClose="false"
                                            :showClose="false"
                                            :showOnOk="false"
                                            :params="{ act: 'detail', id: row.id, examine: 1, type: 'vendor' }"
                                            isDrawer
                                            path="vendor/supplier/product/Info"
                                            :title="$t('查看商品')"
                                            width="1000px"
                                            @okCallback="changeProduct"
                                        >
                                            <a class="btn-link">{{ $t('查看') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DeleteRecord
                                            v-if="row.isImported === 0"
                                            :params="{ vendorProductIds: [row.id] }"
                                            :requestApi="vendorProductImport"
                                            :title="$t('确定导入该商品吗？')"
                                            :message="$t('操作成功')"
                                            @afterDelete="changeProduct"
                                        > {{ $t('导入') }} </DeleteRecord>
                                        <DeleteRecord
                                            v-if="row.isImported === 1 && row.productStatus === 0 && !row.isNeedReImportVendorProduct"
                                            :params="{ id: row.productId, field: 'productStatus', val: 1 }"
                                            :requestApi="updateProductFiled"
                                            :title="$t('确定上架该商品吗？')"
                                            :message="$t('操作成功')"
                                            @afterDelete="changeProduct"
                                        > {{ $t('上架') }} </DeleteRecord>
                                        <DeleteRecord
                                            v-if="row.isImported === 1 && row.productStatus === 0 && row.isNeedReImportVendorProduct == 1"
                                            :params="{ productId: row.productId, vendorProductId: row.id }"
                                            :requestApi="vendorProductReImport"
                                            :title="$t('确定重新导入该商品吗？')"
                                            :message="$t('操作成功')"
                                            @afterDelete="changeProduct"
                                        > {{ $t('重新导入') }} </DeleteRecord>
                                        <DeleteRecord
                                            v-if="row.isImported === 1 && row.productStatus === 1"
                                            :params="{ id: row.productId, field: 'productStatus', val: 0 }"
                                            :requestApi="updateProductFiled"
                                            :title="$t('确定下架该商品吗？')"
                                            :message="$t('操作成功')"
                                            @afterDelete="changeProduct"
                                        > {{ $t('下架') }} </DeleteRecord>
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
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { RadioType } from "@/components/radio";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { ref } from "vue";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { ProductFilterParams, ProductFilterState } from "@/types/vendor/product";
import { SelectTimeInterval } from "@/components/select";
import { vendorProductImport, getProductClientPage } from "@/api/vendor/product";
import { SelectBrand, SelectCategory, SelectVendor } from "@/components/select";
import { imageFormat, urlFormat } from "@/utils/format";
import { isMerchant, isOverseas, isS2b2c, isStore } from "@/utils/version";
import { useListRequest } from "@/hooks/useListRequest";
import { updateProductFiled, batchSubmit, batchImport, vendorProductReImport } from "@/api/product/product";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const emit = defineEmits(["callback"]);
const waitingCheckedCount = ref<number>(0);
const adminType = ref(localStorage.getItem("adminType"));
const activeKey = ref<number>(1);
const type = ref<number>(1);
const radioList = [{ key: 1, title: "普通商品", desc: isStore() ? "物流/自提/同城配送" : "物流配送" }];
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
} = useListRequest<ProductFilterState, ProductFilterParams>({
    apiFunction: getProductClientPage,
    idKey: "id",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        vendorId: "",
        productCategoryId: "",
        productBrandId: "",
        isCanImport: ""
    }
});

// 初始化加载
loadFilter();
// 获取列表的查询结果
// const _getProductWaitingCheckedCount = async () => {
//     try {
//         const waitingCount = await getProductWaitingCheckedCount();
//         waitingCheckedCount.value = waitingCount;
//         // 找到对应的对象并更新 count
//         productStatusList.value = productStatusList.value.map((status) => {
//             if (status.value === 5) {
//                 status.count = waitingCheckedCount.value || 0;
//             }
//             return status;
//         });
//     } catch (error: any) {
//         message.error(error.message);
//     }
// };
// _getProductWaitingCheckedCount();

// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
    emit("callback");
};

const onBatchUp = async (action: string) => {
    let ids: any = [];
    filterState.value.forEach((item: any) => {
        if (selectedIds.value.includes(item.id) && item.productId && item.productId > 0 && item.productStatus == 0) {
            ids.push(item.productId);
        }
    });
    if (ids.length === 0) {
        message.error(t('当前选中商品没有符合条件的商品'));
        return;
    }
    try {
        await batchSubmit(action, { ids });
        message.success(t('操作成功'));
        loadFilter();
        selectedIds.value = [];
        emit("callback");
    } catch (error: any) {
        message.error(error.message || t('操作失败'));
    }
};

const changeProduct = () => {
    loadFilter();
    emit("callback");
};

const onBatchImport = async () => {
    try {
        await batchImport({ vendorProductIds: [...selectedIds.value] });
        message.success(t('操作成功'));
        selectedIds.value = [];
        changeProduct();
    } catch (error: any) {
        message.error(error.message || t('操作失败'));
    }
};
</script>
<style lang="less" scoped>
.title {
    background-color: #f5f7fa;
    font-size: 14px;
    font-weight: bold;
    padding: 12px 10px;
    display: flex;
    align-items: center;
    margin-bottom: 20px;

    .xian {
        width: 3px;
        height: 14px;
        background-color: var(--tig-primary);
        margin-right: 5px;
    }
}
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
</style>
