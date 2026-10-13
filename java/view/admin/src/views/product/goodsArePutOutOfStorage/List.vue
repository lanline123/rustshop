/** * @file 商品出库列表 * @description 商品出库单管理，查看和管理出库记录 * @features * - 按出库单号、商品名称搜索 * - 按出库时间、出库类型、经办人筛选 * -
查看出库单详情 * - 导出出库记录 */
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
                                        <label class="control-label"><span>{{ $t('出库搜索：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('请输入出库单号/商品名称')" clearable> </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('出库时间：') }}</span></label>
                                        <SelectTimeInterval
                                            v-model:end-date="filterParams.endTime"
                                            v-model:start-date="filterParams.beginTime"
                                            :clearable="false"
                                            type="date"
                                            value-format="YYYY-MM-DD"
                                        ></SelectTimeInterval>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('出库类型：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.type" :placeholder="$t('请选择出库类型')">
                                                <el-option v-for="(item, index) in typeList" :key="index" :label="item.label" :value="item.value"></el-option>
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('经办人：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.addBy" :placeholder="$t('请选择经办人')">
                                                <el-option v-for="(item, index) in addByList" :key="index" :label="item" :value="item"></el-option>
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
                                        :params="{ act: 'add', inOrOut: 2 }"
                                        isDrawer
                                        path="product/goodsArePutIntoStorage/Info"
                                        :title="$t('商品出库')"
                                        width="950px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('商品出库') }}</el-button>
                                    </DialogForm>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" tooltip-effect="dark myTooltips" @sort-change="onSortChange">
                            <el-table-column :label="$t('出库单号')" prop="stockCode" min-width="180"></el-table-column>
                            <el-table-column :label="$t('出库类型')" prop="type">
                                <template #default="{ row }">
                                    {{ typeList.find((item) => item.value === row.type)?.label || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('出库时间')" prop="addTime"></el-table-column>
                            <el-table-column :label="$t('备注')" prop="comment" show-overflow-tooltip min-width="150">
                                <template #default="{ row }">
                                    <div class="line1">{{ row.comment || "--" }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('经办人')" prop="addBy"></el-table-column>
                            <el-table-column :label="$t('操作')" prop="desc">
                                <template #default="{ row }">
                                    <DialogForm
                                        :maskClose="false"
                                        :showClose="false"
                                        :showOnOk="false"
                                        :params="{
                                            act: 'detail',
                                            inOrOut: 2,
                                            id: row.productStockId
                                        }"
                                        isDrawer
                                        path="product/goodsArePutIntoStorage/Detail"
                                        :title="$t('详情')"
                                        width="950px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('详情') }}</a>
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
import "@/style/css/list.less";
import { useConfigStore } from "@/store/config";
import { Pagination } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { useListRequest } from "@/hooks/useListRequest";
import { InventoryFilterState, InventoryFilterParams } from "@/types/product/goodsArePutIntoStorage";
import { getInventoryRecordList } from "@/api/product/goodsArePutIntoStorage";
import { onMounted, ref } from "vue";
import { SelectTimeInterval } from "@/components/select";
import { message } from "ant-design-vue";
import { getAddByList } from "@/api/product/inventoryTransfer";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const typeList = [
    {
        label: t('过期出库'),
        value: 21
    },
    {
        label: t('订单出库'),
        value: 22
    },
    {
        label: t('其他出库'),
        value: 23
    },
    {
        label: t('调拨出库'),
        value: 24
    },
];


const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    resetParams
} = useListRequest<InventoryFilterState, InventoryFilterParams>({
    apiFunction: getInventoryRecordList,
    idKey: "productId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        inOrOut: 2, // 出库
        endTime: "",
        beginTime: "",
        type: "",
        keyword: "",
        addBy: ""
    }
});

const addByList = ref<any>([]);

// 获取经办人列表
const getAddByLists = async () => {
    try {
        const result = await getAddByList({});
        addByList.value = result || [];
    } catch (error: any) {
        message.error(error.message);
    }
};

onMounted(() => {
    getAddByLists();
});

loadFilter();
</script>
<style lang="less" scoped>
:deep(.el-table .cell) {
    display: flex;
    align-items: center;
}

:deep(.el-table .caret-wrapper) {
    margin-top: 5px;
}

.introTypeSelect {
    :deep(.el-select__wrapper) {
        width: 110px;
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
<style>
.myTooltips {
    max-width: 50%;
}
</style>
