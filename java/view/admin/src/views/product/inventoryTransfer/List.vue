/**
 * @file 库存调拨列表
 * @description 门店间库存调拨管理
 * @features
 * - 按单据号、经办人搜索
 * - 按调拨状态筛选
 * - 创建调拨单
 * - 审核调拨申请
 */
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
                                        <label class="control-label"><span>{{ $t('单据号：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput v-model="filterParams.keyword" name="productName"
                                                      :placeholder="$t('请搜索单据号')" clearable>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('经办人：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.addBy" :placeholder="$t('请选择经办人')">
                                                <el-option v-for="(item, index) in addByList" :key="index" :label="item"
                                                           :value="item"></el-option>
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
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        :params="{ act: 'add' }" isDrawer path="product/inventoryTransfer/Info" :title="$t('新建调拨单')"
                                        width="950px" @okCallback="loadFilter">
                                        <el-button type="primary">{{ $t('新建调拨单') }}</el-button>
                                    </DialogForm>
                                    <DialogForm :maskClose="false"
                                                :params="{ templateFileApi: getAllocateTemplate, importSubmitApi: productStockAllocateImport, importErrorData: getAllocateImportErrorData, importTitle: '导入调拨单' }"
                                                isDrawer
                                                path="product/goodsArePutIntoStorage/src/InductsDialog"
                                                :title="$t('导入调拨单')"
                                                width="600px"
                                                @okCallback="onOk"
                                                @closeCallback="onOk"
                                                :showClose="false"
                                                :showOnOk="false">
                                        <el-button>{{ $t('导入调拨单') }}</el-button>
                                    </DialogForm>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="tabs">
                    <el-tabs v-model="status" class="demo-tabs" @tab-click="changeTab">
                        <el-tab-pane v-for="item in tabsList" :key="item.name" :label="item.label" :name="item.name">
                            <div class="list-table-tool-row">
                                <div class="list-table-tool-col">
                                    <div class="flex">
                                        <el-popconfirm :title="$t('您确认要批量作废吗？')" v-if="[0, 1, 2, 5].includes(status)"
                                                       @confirm="onCancelAllocate">
                                            <template #reference>
                                                <el-button :disabled="selectedIds.length === 0">{{ $t('作废') }}</el-button>
                                            </template>
                                        </el-popconfirm>
                                        <el-button @click="handleExport(selectedIds)" :disabled="selectedIds.length === 0">{{ $t('批量导出') }}</el-button>
                                        <el-button @click="handleExport([])" :loading="Exportloading">{{ $t('全部导出') }}</el-button>
<!--                                        <el-popconfirm title="您确认要批量出库吗？" v-if="status === 2"-->
<!--                                                       @confirm="onBatchSubmit('del')">-->
<!--                                            <template #reference>-->
<!--                                                <el-button :disabled="selectedIds.length === 0">出库</el-button>-->
<!--                                            </template>-->
<!--                                        </el-popconfirm>-->
                                    </div>
                                </div>
                            </div>
                        </el-tab-pane>
                    </el-tabs>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total"
                                  @sort-change="onSortChange"  @selection-change="onSelectChange" row-key="allocateId">
                            <el-table-column type="selection" width="32" :selectable="selectable" v-if="[0, 1, 2, 5].includes(status)" :reserve-selection="true" />
                            <el-table-column :label="$t('单据号')" prop="allocateCode"></el-table-column>
                            <el-table-column :label="$t('出库组织')" prop="outboundOrganizationName"></el-table-column>
                            <el-table-column :label="$t('入库组织')" prop="warehousingOrganizationName"></el-table-column>
                            <el-table-column :label="$t('单据状态')" prop="status" :formatter="(row: any) => getStatusName(row)"></el-table-column>
                            <el-table-column :label="$t('经办人')" prop="addBy"></el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="addTime"></el-table-column>
                            <el-table-column :label="$t('操作')" prop="act" width="220">
                                <template #default="{ row }">
                                    <div class="actBox">
                                        <DialogForm :maskClose="false"
                                                    :showClose="false"
                                                    :showOnOk="false"
                                                    :params="{
                                                        act: 'detail',
                                                        inOrOut: 1,
                                                        id: row.allocateId,
                                                        rowDetail: row,
                                                    }" isDrawer path="product/inventoryTransfer/Detail" :title="$t('详情')" width="950px"
                                                    @okCallback="loadFilter">
                                            <a class="btn-link">{{ $t('详情') }}</a>
                                        </DialogForm>
                                        <el-divider  v-if="[1, 2, 5].includes(row.status)" direction="vertical" />
                                        <el-popconfirm :title="$t('您确认要作废吗？')" v-if="[1, 2, 5].includes(row.status)"
                                                       @confirm="onCancelAllocateInfo(row.allocateId)">
                                            <template #reference>
                                                <a class="btn-link">{{ $t('作废') }}</a>
                                            </template>
                                        </el-popconfirm>
                                        <el-divider  v-if="row.status === 1 && +shopId === row.warehousingOrganizationId" direction="vertical" />
<!--                                        outboundOrganizationId出库组织； warehousingOrganizationId 入库组织-->
                                        <DialogForm v-if="row.status === 1 && +shopId === row.warehousingOrganizationId"
                                                    :maskClose="false"
                                                    :showClose="false"
                                                    :showOnOk="false"
                                                    :params="{
                                                    id: row.allocateId,
                                                    rowDetail: row,
                                                }" isDrawer path="product/inventoryTransfer/Detail" :title="$t('库存调拨审核')" width="950px"
                                                    @okCallback="loadFilter">
<!--                                            入库组织是当前用户才可以审核-->
                                            <a class="btn-link">{{ $t('审核') }}</a>
                                        </DialogForm>
                                        <el-divider v-if="row.status === 2  && +shopId === row.outboundOrganizationId" direction="vertical" />
                                        <DialogForm v-if="row.status === 2  && +shopId === row.outboundOrganizationId"
                                                    :maskClose="false"
                                                    :params="{
                                                        act: 'detail',
                                                        inOrOut: 2,
                                                        id: row.allocateId,
                                                        rowDetail: row,
                                                    }" isDrawer path="product/inventoryTransfer/InAndOutOfTheWarehouse" :title="$t('调拨出库')" width="950px"
                                                    @okCallback="loadFilter">
<!--                                            出库组织是当前用户才能调拨出库-->
                                            <a class="btn-link">{{ $t('调拨出库') }}</a>
                                        </DialogForm>
                                        <el-divider v-if="row.status === 3 && +shopId === row.warehousingOrganizationId" direction="vertical" />
                                        <DialogForm v-if="row.status === 3 && +shopId === row.warehousingOrganizationId"
                                                    :maskClose="false"
                                                    :params="{
                                                        act: 'detail',
                                                        inOrOut: 1,
                                                        id: row.allocateId,
                                                        rowDetail: row,
                                                    }" isDrawer path="product/inventoryTransfer/InAndOutOfTheWarehouse" :title="$t('调拨入库')" width="950px"
                                                    @okCallback="loadFilter">
<!--                                            入库组织是当前用户才能调拨入库-->
                                            <a class="btn-link">{{ $t('调拨入库') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm :maskClose="false"
                                                    :showClose="false"
                                                    :showOnOk="false"
                                                    :params="{
                                                    act: 'detail',
                                                    id: row.allocateId,
                                                }" isDrawer path="product/inventoryTransfer/Logs" :title="$t('库存调拨记录')" width="750px"
                                                    @okCallback="loadFilter">
                                            <a class="btn-link">{{ $t('记录') }}</a>
                                        </DialogForm>
                                    </div>
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
import { InventoryFilterParams, InventoryFilterState, } from "@/types/product/inventoryTransfer";
import { ref, onMounted, reactive, computed } from "vue";
import { productStockAllocateRecordPage, getAllocateTemplate, productStockAllocateImport, getAllocateImportErrorData, cancelAllocate, exportAllocate, getAddByList, auditAllocate } from "@/api/product/inventoryTransfer";
import { message } from "ant-design-vue";
import requestExport from "@/utils/export";
import { useImStore } from "@/store/im";
import { useUserStore } from "@/store/user";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();

const userInfo: any = computed(() => useUserStore().userInfo);

const shopId = computed(()=>{
    return localStorage.getItem("shopId") || userInfo.value.shopId;
})

// 1=待审核，2待调出，3=待调入,4=已完成，5=已驳回，6=已作废
const status = ref(0);
const tabsList = ref([
    {
        label: t('全部'),
        name: 0
    },
    {
        label: t('待审核'),
        name: 1
    },
    {
        label: t('待调出'),
        name: 2
    },
    {
        label: t('待调入'),
        name: 3
    },
    {
        label: t('已完成'),
        name: 4
    },
    {
        label: t('审核驳回'),
        name: 5
    },
    {
        label: t('已作废'),
        name: 6
    },
])
const addByList = ref<any>([])

const getStatusName = (row: any) => {
    switch (row.status) {
        case 1:
            return t('待审核');
        case 2:
            return t('待调出');
        case 3:
            return t('待调入');
        case 4:
            return t('已完成');
        case 5:
            return t('审核驳回');
        case 6:
            return t('已作废');
    }
};

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    resetParams,
    selectedIds,
    onBatchAction
} = useListRequest<InventoryFilterState, InventoryFilterParams>({
    apiFunction: productStockAllocateRecordPage,
    idKey: "allocateId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        addBy: '',
        keyword: ''
    },
});

loadFilter()

const selectable = (row: any) => {
    return [1, 2, 5].includes(row.status)
};

const changeTab = (e: any) => {
    status.value = e.props.name;
    filterParams.status = status.value === 0 ? "" : status.value;
    loadFilter()
};
// 批量操作
const onCancelAllocate = async () => {
    if (selectedIds.value.length === 0) {
        message.warning(t('请至少选择一项'));
        return;
    }
    try {
        await cancelAllocate(selectedIds.value);
        message.success(t('操作成功'));
        loadFilter();
        selectedIds.value = [];
    } catch (error: any) {
        message.error(error.message || t('操作失败'));
    }
};
const onCancelAllocateInfo = async (id: number) => {
    try {
        await cancelAllocate([id]);
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message || t('操作失败'));
    }
};

// 导入
const onOk = async (value: any) => {
    loadFilter();
};



const Exportloading = ref<boolean>(false);
const handleExport = async (allocateIds: any[] = []) => {
    Exportloading.value = true;
    try {
        const result = await exportAllocate({allocateIds: allocateIds});
        Exportloading.value = false;
        requestExport(result, t('调拨单导出'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        Exportloading.value = false;
    }
};


// 获取经办人列表
const getAddByLists = async () => {
    try {
        const result = await getAddByList({});
        addByList.value = result || [];
    } catch (error: any) {
        message.error(error.message);
    }
}

// 审核通过
const onAuditSuccess = async (value: any) => {
    try {
        loading.value = true;
        await auditAllocate({id: value.allocateId, auditType: 2});
        message.success(t('审核通过成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
}

onMounted(() => {
    getAddByLists()
});

</script>
<style lang="less" scoped>
:deep(.el-table .cell) {
    display: flex;
    align-items: center;
}

:deep(.el-table .caret-wrapper) {
    margin-top: 5px;
}


.status-switch {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    width: 150px;
}

.status-switch>div {
    word-break: keep-all;
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
}

.actBox{
    display: flex;
    align-items: center;
}
</style>
