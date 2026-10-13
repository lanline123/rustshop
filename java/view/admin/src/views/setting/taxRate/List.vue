<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" name="taxRateFilterForm">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('国家/地区：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.regionName"
                                                name="regionName"
                                                :placeholder="$t('输入国家/地区名称')"
                                                clearable
                                                @keyup.enter="onSearchSubmit"
                                                @clear="onSearchSubmit"
                                            >
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('是否生效：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.isActive" clearable :placeholder="$t('请选择')" @change="onSearchSubmit">
                                                <el-option :value="1" :label="$t('是')" />
                                                <el-option :value="0" :label="$t('否')" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('创建时间：') }}</span></label>
                                            <SelectTimeInterval
                                                v-model:end-date="filterParams.endTime"
                                                v-model:start-date="filterParams.startTime"
                                                :clearable="false"
                                                type="date"
                                                value-format="YYYY-MM-DD"
                                            ></SelectTimeInterval>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <label class="control-label"></label>
                                    <div class="control-container">
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                        <el-button plain @click="onReset">{{ $t('重置') }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="setting/taxRate/Info"
                                        :title="$t('新增税率规则')"
                                        width="480px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('新增税率规则') }}</el-button>
                                    </DialogForm>
                                    <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchDelete">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
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
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="taxRateId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('国家/地区')" min-width="180" prop="regionName" />
                            <el-table-column :width="150" prop="rate" sortable="custom">
                                <template #header>
                                    <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover">
                                        <template #content>
                                            <div style="width: 350px; padding: 10px; line-height: 24px; font-size: 14px">
                                                <p>{{ $t('税率规则：') }}</p>
                                                <p>{{ $t('1、每个国家或地区(一级)只能设置一条税率数据') }}</p>
                                                <p>{{ $t('2、税率的计算根据客户下单的收货地址进行计算') }}</p>
                                                <p>{{ $t('3、用户支付的税费=订单支付金额*税率') }}</p>
                                            </div>
                                        </template>
                                        <span class="flex flex-align-center" style="display: inline-flex; align-items: center; white-space: nowrap">
                                            <span>{{ $t('税率(%)') }}</span>
                                            <el-icon style="margin-left: 5px" size="16" color="#969799"><QuestionFilled /></el-icon>
                                        </span>
                                    </el-tooltip>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否生效')" min-width="120" prop="isActive">
                                <template #default="{ row }">
                                    <el-switch
                                        v-model="row.isActive"
                                        :active-value="1"
                                        :inactive-value="0"
                                        @change="onToggleStatus(row, $event)"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('备注')" min-width="220" prop="remark" show-overflow-tooltip />
                            <el-table-column :label="$t('创建时间')" min-width="170" prop="createTime" sortable="custom" />
                            <el-table-column :label="$t('操作')" fixed="right" width="150">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.taxRateId }"
                                        isDrawer
                                        path="setting/taxRate/Info"
                                        :title="$t('编辑税率规则')"
                                        width="480px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord
                                        :params="{ id: row.taxRateId, taxRateId: row.taxRateId }"
                                        :requestApi="delTaxRate"
                                        @afterDelete="loadFilter"
                                    > {{ $t('删除') }} </DeleteRecord>
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
import { message } from "ant-design-vue";
import { QuestionFilled } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination } from "@/components/list";
import { SelectTimeInterval } from "@/components/select";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { delTaxRate, getTaxRateList, updateTaxRate } from "@/api/setting/taxRate";
import type { TaxRateFilterParams, TaxRateFilterState } from "@/types/setting/taxRate";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
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
    resetParams
} = useListRequest<TaxRateFilterState, TaxRateFilterParams>({
    apiFunction: getTaxRateList,
    idKey: "taxRateId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        regionName: "",
        isActive: "",
        startTime: "",
        endTime: "",
        page: 1,
        size: config.get("pageSize")
    }
});

const onReset = () => {
    resetParams();
};

const onBatchDelete = async () => {
    if (selectedIds.value.length === 0) {
        return;
    }
    try {
        await Promise.all(selectedIds.value.map((id) => delTaxRate({ id, taxRateId: id })));
        message.success(t('批量删除成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message || t('批量删除失败'));
    }
};

const onToggleStatus = async (row: TaxRateFilterState, value: number | string | boolean) => {
    const oldValue = Number(value) === 1 ? 0 : 1;
    try {
        await updateTaxRate("update", {
            id: row.taxRateId,
            taxRateId: row.taxRateId,
            regionId: row.regionId,
            regionName: row.regionName,
            rate: row.rate,
            isActive: Number(value),
            remark: row.remark
        });
        message.success(t('生效状态更新成功'));
    } catch (error: any) {
        row.isActive = oldValue;
        message.error(error.message || t('生效状态更新失败'));
    }
};

loadFilter();
</script>
