<template>
    <div class="content_wrapper">
        <div class="container">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('申请时间：') }}</span></label>
                                        <SelectTimeInterval
                                            type="date"
                                            v-model:start-date="filterParams.addTimeStart"
                                            v-model:end-date="filterParams.addTimeEnd"
                                        ></SelectTimeInterval>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('提现状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.status" clearable>
                                                <el-option v-for="(item, key) in statusList" :key="String(key)" :value="key" :label="item" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <el-button type="primary" @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="shopId" @sort-change="onSortChange">
                            <el-table-column :width="200" :label="$t('申请时间')" prop="addTime"></el-table-column>
                            <el-table-column :label="`${$t('金额')}(${config.config.dollarSign})`" prop="amount"></el-table-column>
                            <el-table-column :label="`${$t('手续费')}(${config.config.dollarSign})`" prop="handlingFee">
                                <template #default="{ row }"> {{ row.handlingFee ? row.handlingFee : "-" }} </template>
                            </el-table-column>
                            <el-table-column :label="$t('手续费率')" prop="handlingRate">
                                <template #default="{ row }"> {{ row.handlingRate ? row.handlingRate : "-" }} <span v-if="row.handlingRate">%</span> </template>
                            </el-table-column>
                            <el-table-column :label="$t('状态')" sortable="custom">
                                <template #default="{ row }">
                                    <span>{{ row.statusText }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('到账方式')">
                                <template #default="{ row }">
                                    {{ row.accountData?.accountTypeText || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('备注')" prop="remark"></el-table-column>
                            <el-table-column :label="$t('拒绝理由')" prop="auditRemark">
                                <template #default="{ row }">
                                    {{ row.auditRemark || "--" }}
                                </template>
                            </el-table-column>
                            <!-- <el-table-column :width="120" fixed="right" label="操作">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.shopId }"
                                        isDrawer
                                        path="shop/shop/Info"
                                        title="提现详情"
                                        width="700px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">详情</a>
                                    </DialogForm>
                                </template>
                            </el-table-column> -->
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
import { DialogForm } from "@/components/dialog";
import { onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { Image } from "@/components/image";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { WithdrawFilterParams, WithdrawFilterState } from "@/types/merchant/capitalfund/withdraw.d";
import { getWithdrawList, getWithdrawConfig } from "@/api/merchant/capitalfund/withdraw";
import { CustomSwitch } from "@/components/switch";
import { SelectTimeInterval } from "@/components/select";

const config: any = useConfigStore();
// 基本参数定义
const filterState = ref<WithdrawFilterState[]>([]);
const loading = ref<boolean>(false);
const total = ref<number>(0);
const filterParams = reactive<WithdrawFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    status: "",
    addTimeStart: "",
    addTimeEnd: ""
});
const statusList = ref();
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getWithdrawList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
        const config = await getWithdrawConfig();
        statusList.value = config?.data?.statusList || config?.statusList || {};
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});

// 参数查询
const onSearchSubmit = () => {
    loadFilter();
};
// 修改排序
const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
    filterParams.sortField = prop;
    filterParams.sortOrder = order == "ascending" ? "asc" : order == "descending" ? "desc" : "";
    loadFilter();
};
</script>
<style lang="less" scoped></style>
