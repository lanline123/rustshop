/** * @file 账期结算 * @description 根据 docs/账期支付/账期结算.md 实现客户列表与结算入口（无接口，先用 mock 数据） */
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
                                            v-model="filterParams.keyword"
                                            :placeholder="$t('请输入客户名称')"
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

                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="pageList" row-key="userId" :loading="loading">
                            <el-table-column :label="$t('客户名称')">
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
                            <el-table-column :label="$t('结算账期')">
                                <template #default="{ row }">{{ row.settlementDays || "-" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('剩余账期天数')" min-width="95">
                                <template #default="{ row }">{{ row.remainingDays ?? "-" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('结算建议')">
                                <template #default="{ row }">{{ Number(row.remainingDays || 0) === 0 ? $t('建议结算') : "-" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" width="160">
                                <template #default="{ row }">
                                    <div class="buttonStyle">
                                        <DialogForm
                                            :title="row.username"
                                            isDrawer
                                            width="1020px"
                                            :showClose="false"
                                            :params="{ userId: row.userId, username: row.username }"
                                            path="order/termPay/termPay/WaitPay"
                                            @callback="onDrawerCallback"
                                        >
                                            <a class="btn-link">{{ $t('去结算') }}</a>
                                            <template #footer="{ close, onOk, confirmLoading, okDisabled }">
                                                <el-button @click="close">{{ $t('取消') }}</el-button>
                                                <el-button type="primary" :disabled="okDisabled" :loading="confirmLoading" @click="onOk">{{ $t('确认结算') }}</el-button>
                                            </template>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm
                                            :title="row.username"
                                            isDrawer
                                            width="1200px"
                                            :showFooter="false"
                                            :showOnOk="false"
                                            :showClose="false"
                                            :params="{ userId: row.userId, username: row.username }"
                                            path="order/termPay/termPay/src/HistoryTerm"
                                        >
                                            <a class="btn-link">{{ $t('历史结算') }}</a>
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
                </div>

                <div v-if="total > 0" class="pagination-con">
                    <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="onPageChange" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { Pagination } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { getCreditSettlementUserList } from "@/api/termPay/creditSettlement";
import type { CreditSettlementUserListQuery, CreditSettlementUserRecord } from "@/types/termPay/creditSettlement.d";
import { Tag } from "@/components/form";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const loading = ref(false);

const filterParams = reactive({
    page: 1,
    size: 10,
    keyword: ""
});

const total = ref(0);
const pageList = ref<CreditSettlementUserRecord[]>([]);

const money = (n: number) => {
    const v = Number.isFinite(n) ? n : 0;
    return v.toFixed(2);
};

const onSearchSubmit = () => {
    filterParams.page = 1;
    loadList();
};

const onPageChange = () => {
    loadList();
};

const resetParams = () => {
    filterParams.page = 1;
    filterParams.size = 10;
    filterParams.keyword = "";
    loadList();
};

// 历史结算通过抽屉打开（见 template 中 DialogForm）

const onDrawerCallback = (result: any) => {
    // WaitPay/ThisPay 内部可通过 callback 通知外层刷新
    if (result?.type === "settlementSuccess") {
        message.success(t('结算成功'));
        loadList();
    }
};

const displayName = (u: CreditSettlementUserRecord) => {
    return u.username;
};

const loadList = async () => {
    loading.value = true;
    try {
        const params: CreditSettlementUserListQuery = {
            page: filterParams.page,
            size: filterParams.size
        };
        params.keyword = filterParams.keyword || undefined;

        const res = await getCreditSettlementUserList(params);
        total.value = res.total || 0;
        pageList.value = res.records || [];
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadList();
});
</script>

<style scoped lang="less">
.table-container {
    overflow-x: auto;

    .custom-table {
        width: 100%;
        border-collapse: collapse;
        background: #fff;
        border: 1px solid #f0f0f0;

        thead th {
            background-color: #f5f6fa;
            font-weight: 600;
            text-align: left;
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;
            white-space: nowrap;
        }

        tbody td {
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;
            white-space: nowrap;
        }

        tbody tr:hover td {
            background: #fafafa;
        }
    }
}

.textR {
    text-align: right !important;
}

.textC {
    text-align: center !important;
}

.buttonStyle {
    display: flex;
    align-items: center;
    // gap: 8px;
    // justify-content: center;
}
</style>
