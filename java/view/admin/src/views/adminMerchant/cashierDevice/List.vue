<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="container">
                <div class="lyecs-table-list-warp">
                    <div class="list-table-tool lyecs-search-warp">
                        <el-form :model="filterParams" name="cashierDeviceFilterForm">
                            <div class="advanced-search-warp list-table-tool-row">
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <div class="form-group">
                                            <label class="control-label"><span>{{ $t('设备名称：') }}</span></label>
                                            <div class="control-container">
                                                <TigInput
                                                    v-model="filterParams.deviceName"
                                                    name="deviceName"
                                                    :placeholder="$t('请输入设备名称')"
                                                    @keyup.enter="onSearchSubmit"
                                                    clearable
                                                    @clear="onSearchSubmit"
                                                />
                                            </div>
                                        </div>
                                    </div>
                                    <div class="simple-form-field">
                                        <div class="form-group">
                                            <label class="control-label"><span>{{ $t('设备码：') }}</span></label>
                                            <div class="control-container">
                                                <TigInput
                                                    v-model="filterParams.deviceCode"
                                                    name="deviceCode"
                                                    :placeholder="$t('请输入设备码')"
                                                    @keyup.enter="onSearchSubmit"
                                                    clearable
                                                    @clear="onSearchSubmit"
                                                />
                                            </div>
                                        </div>
                                    </div>
                                    <div class="simple-form-field">
                                        <div class="form-group">
                                            <label class="control-label"><span>{{ $t('设备状态：') }}</span></label>
                                            <div class="control-container">
                                                <el-select
                                                    v-model="filterParams.status"
                                                    :placeholder="$t('全部')"
                                                    clearable
                                                    @change="onSearchSubmit"
                                                    @clear="() => { filterParams.status = -1; onSearchSubmit(); }"
                                                >
                                                    <el-option :label="$t('全部')" :value="-1" />
                                                    <el-option :label="$t('启用')" :value="1" />
                                                    <el-option :label="$t('停用')" :value="2" />
                                                </el-select>
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
                            </div>
                            <div class="list-table-tool-row">
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <div class="form-group">
                                            <DialogForm
                                                :params="{ act: 'create' }"
                                                isDrawer
                                                path="adminMerchant/cashierDevice/Info"
                                                :title="$t('新增设备')"
                                                width="600px"
                                                @okCallback="loadFilter"
                                            >
                                                <el-button type="primary">{{ $t('新增设备') }}</el-button>
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
                                row-key="id"
                            >
                                <el-table-column :label="$t('设备名称')">
                                    <template #default="{ row }">
                                        {{ row.deviceName || row.username || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('设备码')">
                                    <template #default="{ row }">
                                        {{ row.deviceCode || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('绑定打印机')">
                                    <template #default="{ row }">
                                        {{ row.printCount || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('最后使用时间')" :width="200">
                                    <template #default="{ row }">
                                        {{ row.lastUsedTimeText || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('设备状态')" prop="status">
                                    <template #default="{ row }">
                                        <div class="status-switch">
                                            <div>
                                                <Switch
                                                    :checked="row.status"
                                                    :switchingCode="[2, 1]"
                                                    :params="{ id: row.id, field: 'status' }"
                                                    :requestApi="updateCashierDeviceStatus"
                                                    @callback="loadFilter"
                                                />
                                            </div>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('操作')" fixed="right" width="140">
                                    <template #default="{ row }">
                                        <DialogForm
                                            :params="{ act: 'update', id: row.id }"
                                            isDrawer
                                            path="adminMerchant/cashierDevice/Info"
                                            :title="$t('编辑设备')"
                                            width="600px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm
                                            :params="{ id: row.id }"
                                            isDrawer
                                            :showOnOk="false"
                                            path="adminMerchant/cashierDevice/Detail"
                                            :title="$t('设备详情')"
                                            width="600px"
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
                        <div v-if="total > 0" class="pagination-con">
                            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted } from "vue";
import { DialogForm } from "@/components/dialog";
import { Pagination, Switch } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { getCashierDeviceList, updateCashierDeviceStatus } from "@/api/setting/cashier";

const config: any = useConfigStore();
// 设备状态：1 启用、2 停用；全部(-1) 时不下发 status，由后端返回全量
const getList = (params: any) =>
    getCashierDeviceList({
        ...params,
        deviceName: (params.deviceName || "").trim(),
        deviceCode: (params.deviceCode || "").trim(),
        status: params.status === -1 ? "" : params.status
    });
const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<any, any>({
    apiFunction: getList,
    idKey: "id",
    defaultParams: {
        deviceName: "",
        deviceCode: "",
        status: -1,
        page: 1,
        size: config.get("pageSize")
    }
});

// 初始化加载
onMounted(() => {
    loadFilter();
});
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
</style>
