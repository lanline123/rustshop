<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="advanced-search-warp list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.username"
                                            :placeholder="$t('请输入会员名称')"
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
                            <div class="simple-form-field">
                                <div class="control-container">
                                    <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                    <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="control-container">
                    <DialogForm
                        :title="$t('添加授信会员')"
                        isDrawer
                        width="600px"
                        path="order/termPay/termMember/Info"
                        :params="{ act: 'add' }"
                        :bodyStyle="{ padding: '24px 24px 0 0' }"
                        @okCallback="loadList"
                    >
                        <el-button type="primary">{{ $t('添加授信会员') }}</el-button>
                    </DialogForm>
                    <DeleteRecord
                        class="ml8"
                        :disabled="selectedIds.length === 0"
                        :requestApi="deleteCreditUserBatch"
                        :params="{ ids: selectedIds }"
                        :title="$t('确认删除选中的授信会员？')"
                        :content="$t('删除后此会员将无法使用账期支付功能')"
                        :message="$t('删除成功')"
                        @afterDelete="onDeleteSuccess"
                    >
                        <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                    </DeleteRecord>
                </div>

                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table ref="tableRef" :data="list" row-key="creditUserId" :loading="loading" @selection-change="onSelectionChange">
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('会员名称')" min-width="220">
                                <template #default="{ row }">
                                    <template v-if="row.type === 1">
                                        <Tag :transparent="true" :text="$t('个人')" />
                                    </template>
                                    <template v-else>
                                        <Tag color="#409EFF" :transparent="false" :text="$t('企业')" />
                                    </template>
                                    <span class="ml8">{{ row.username }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('授信额度')">
                                <template #default="{ row }">{{ money(row.creditLimit) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('可用额度')">
                                <template #default="{ row }">{{ money(row.availableAmount) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('已使用额度')">
                                <template #default="{ row }">{{ money(row.usedAmount) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('结算账期')" prop="settlementTypeText" />
                            <el-table-column :label="$t('状态')" width="120">
                                <template #default="{ row }">
                                    <div class="status-switch">
                                        <div>
                                            <Switch
                                                v-model:checked="row.status"
                                                :params="{ id: row.creditUserId, field: 'status' }"
                                                :requestApi="updateCreditUserField"
                                            />
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" width="160">
                                <template #default="{ row }">
                                    <div class="op">
                                        <DialogForm
                                            :title="$t('修改授信会员')"
                                            isDrawer
                                            width="600px"
                                            path="order/termPay/termMember/Info"
                                            :bodyStyle="{ padding: '24px 24px 0 0' }"
                                            :params="{ act: 'edit', creditUserId: row.creditUserId }"
                                            @okCallback="loadList"
                                            :showClose="false"
                                        >
                                            <a class="btn-link">{{ $t('修改') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DeleteRecord
                                            :requestApi="deleteCreditUser"
                                            :params="{ id: row.creditUserId }"
                                            :title="$t('确认删除该授信会员？')"
                                            :content="$t('删除后此会员将无法使用账期支付功能')"
                                            :message="$t('删除成功')"
                                            @afterDelete="onDeleteSuccess"
                                        >
                                            <a class="btn-link">{{ $t('删除') }}</a>
                                        </DeleteRecord>
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
                </div>

                <div v-if="total > 0" class="pagination-con">
                    <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadList" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { Tag } from "@/components/form";
import { deleteCreditUser, getCreditUserList, updateCreditUserField } from "@/api/termPay/creditUser";
import type { CreditUserListQuery, CreditUserRecord } from "@/types/termPay/creditUser.d";
import { useConfigStore } from "@/store/config";

const config: any = useConfigStore();

const loading = ref(false);
const total = ref(0);
const list = ref<CreditUserRecord[]>([]);
const selectedIds = ref<number[]>([]);
const tableRef = ref();

const filterParams = reactive<CreditUserListQuery>({
    page: 1,
    size: config.get("pageSize"),
    username: ""
});

const money = (n: number) => {
    const v = Number.isFinite(Number(n)) ? Number(n) : 0;
    return v.toFixed(2);
};

const onSelectionChange = (rows: CreditUserRecord[]) => {
    selectedIds.value = rows.map((r) => r.creditUserId);
};

const onSearchSubmit = () => {
    filterParams.page = 1;
    loadList();
};

const resetParams = () => {
    filterParams.page = 1;
    filterParams.size = config.get("pageSize");
    filterParams.username = "";
    loadList();
};

const loadList = async () => {
    loading.value = true;
    try {
        const params: CreditUserListQuery = {
            page: filterParams.page,
            size: filterParams.size,
            username: filterParams.username || undefined
        };
        const res = await getCreditUserList(params);
        list.value = res.records || [];
        total.value = res.total || 0;
        // 重新加载后清空选择（避免批量操作误删）
        selectedIds.value = [];
        tableRef.value?.clearSelection?.();
    } finally {
        loading.value = false;
    }
};

const deleteCreditUserBatch = async (params: { ids: number[] }) => {
    const ids = params?.ids || [];
    for (const id of ids) {
        await deleteCreditUser({ id });
    }
    return null;
};

const onDeleteSuccess = () => {
    selectedIds.value = [];
    loadList();
};

onMounted(() => {
    loadList();
});
</script>

<style scoped lang="less">
.ml8 {
    margin-left: 8px;
}
.op {
    display: flex;
    align-items: center;
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
