<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" name="receiptPrintFilterForm">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('打印机名称/编码')"
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
                                        <label class="control-label" style="width: 100px"
                                            ><span>{{ $t("打印机类型：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <el-select v-model="filterParams.platform" clearable :placeholder="$t('全部')" @change="onSearchSubmit">
                                                <el-option :value="1" :label="$t('飞鹅云')" />
                                                <el-option :value="2" :label="$t('商米云')" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <label class="control-label"></label>
                                    <div class="control-container">
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t("搜索") }}</el-button>
                                        <el-button plain @click="onReset">{{ $t("重置") }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <DialogForm
                                            :params="{ act: 'add' }"
                                            isDrawer
                                            path="setting/config/receiptPrintSetting/Info"
                                            :title="$t('新增打印机')"
                                            width="600px"
                                            @okCallback="loadFilter"
                                        >
                                            <el-button type="primary">{{ $t("新增打印机") }}</el-button>
                                        </DialogForm>
                                    </div>
                                </div>
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
                            row-key="printId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <!-- <el-table-column type="selection" width="32" /> -->
                            <el-table-column :label="$t('名称')">
                                <template #default="{ row }">
                                    <div>{{ row.printName }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('打印机类型')" prop="platformText">
                                <template #default="{ row }">
                                    <div>{{ $t(row.platformText) }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('打印机编码')" prop="printSn">
                                <template #default="{ row }">
                                    <div>{{ row.printSn }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('绑定设备')" prop="deviceCode">
                                <template #default="{ row }">
                                    <div>{{ resolveDeviceName(row) }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('更新时间')" prop="updateTime">
                                <template #default="{ row }">
                                    <div>{{ row.updateTime }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('状态')" prop="status" sortable="custom">
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.status"
                                        :switchingCode="[2, 1]"
                                        :params="{ id: row.printId, field: 'status' }"
                                        :requestApi="printUpdateStatus"
                                        @updateData="loadFilter"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :width="200" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.printId }"
                                        isDrawer
                                        path="setting/config/receiptPrintSetting/Info"
                                        :title="$t('编辑')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t("编辑") }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.printId }"
                                        isDrawer
                                        path="setting/config/receiptPrintSetting/Design"
                                        :title="$t('购物小票信息')"
                                        width="1350px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t("排版设计") }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.printId }" :requestApi="printDelete" @afterDelete="loadFilter"
                                        >{{ $t("删除") }}
                                    </DeleteRecord>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t("暂无数据") }}</div>
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
import { ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { getPrintList, printUpdateStatus, printDelete } from "@/api/setting/receiptPrint";
import { getCashierDeviceList } from "@/api/setting/cashier";
import { useListRequest } from "@/hooks/useListRequest";
import type { PrintListItem, PrintListParams, PrintListResponse } from "@/types/setting/receiptPrint";

const config: any = useConfigStore();
const deviceNameByCode = ref<Record<string, string>>({});
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
} = useListRequest<PrintListResponse, PrintListParams>({
    // @ts-ignore
    apiFunction: getPrintList,
    idKey: "logisticsId",
    defaultParams: {
        id: void 0,
        sortField: "",
        sortOrder: "",
        keyword: "",
        platform: void 0,
        printName: "",
        printSn: "",
        status: void 0,
        page: 1,
        size: config.get("pageSize")
    }
});

const onReset = () => {
    resetParams();
};

// 列表接口只回传 deviceCode 时，用设备列表补出设备名称展示
const resolveDeviceName = (row: PrintListItem) => {
    if (row.deviceName) {
        return row.deviceName;
    }
    if (!row.deviceCode) {
        return "--";
    }
    return deviceNameByCode.value[row.deviceCode] || row.deviceCode;
};

const fetchCashierDevices = async () => {
    try {
        const result: any = await getCashierDeviceList({ page: 1, size: 200 });
        deviceNameByCode.value = (result?.records || []).reduce((map: Record<string, string>, device: any) => {
            if (device.deviceCode) {
                map[device.deviceCode] = device.deviceName || device.deviceCode;
            }
            return map;
        }, {});
    } catch {
        deviceNameByCode.value = {};
    }
};

loadFilter();
fetchCashierDevices();
</script>
