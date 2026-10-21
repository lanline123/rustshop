<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="advanced-search-warp list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                     <label class="control-label"><span>{{ $t('订单编号：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.orderSn"
                                            name="keyword"
                                            :placeholder="$t('订单编号')"
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
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('订单状态：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.orderStatus" clearable @change="onSearchSubmit" :placeholder="$t('全部')">
                                            <el-option v-for="item in orderStatusList" :key="item.value" :label="item.label" :value="item.value" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>

                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('会员名称：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.username"
                                            :placeholder="$t('请输入会员名称')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        />
                                    </div>
                                </div>
                            </div>

                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('下单时间：') }}</span></label>
                                    <SelectTimeInterval
                                        v-model:end-date="filterParams.orderTimeEnd"
                                        v-model:start-date="filterParams.orderTimeStart"
                                        :clearable="true"
                                        type="date"
                                        value-format="YYYY-MM-DD"
                                        @callback="onSearchSubmit"
                                    />
                                </div>
                            </div>

                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('联系方式：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.mobile"
                                            :placeholder="$t('请输入联系方式')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        />
                                    </div>
                                </div>
                            </div>

                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('结算状态：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.settlementStatus" clearable @change="onSearchSubmit" :placeholder="$t('全部')">
                                            <el-option v-for="item in settlementStatusList" :key="item.value" :label="item.label" :value="item.value" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>

                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label" style="width: 100px;"><span>{{ $t('回款流水单号：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.settlementSn"
                                            :placeholder="$t('请输入回款流水单号')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        />
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
                </div>

                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="list" row-key="creditOrderId" :loading="loading">
                            <el-table-column :label="$t('订单编号')" prop="orderSn" min-width="120" />
                            <el-table-column :label="$t('下单时间')" prop="orderTime" min-width="120" />
                            <el-table-column :label="$t('订单状态')" prop="orderStatusText">
                                <template #default="{ row }">{{ row.orderStatusText || "-" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('会员名称')" prop="username" min-width="90">
                                <template #default="{ row }">
                                    {{ row.username || "-" }}
                                    <MobileCard :mobile="row.mobile"></MobileCard>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('公司名称')" min>
                                <template #default="{ row }">{{ row.companyName || "-" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('订单总额')">
                                <template #default="{ row }">{{ priceFormat(row.orderAmount) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('结算状态')" prop="settlementStatusText" />
                            <el-table-column :label="$t('回款流水单号')">
                                <template #default="{ row }">{{ row.settlementSn || "-" }}</template>
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
import { Pagination } from "@/components/list";
import { priceFormat } from "@/utils/format";
import { isStore } from "@/utils/version";
import { SelectTimeInterval } from "@/components/select";
import { getCreditOrderList } from "@/api/termPay/creditOrder";
import type { CreditOrderRecord, CreditOrderListQuery } from "@/types/termPay/creditOrder.d";
import MobileCard from "@/components/list/src/MobileCard.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const loading = ref(false);

const orderStatusList = reactive([
    { value: -1, label: t('全部'), isShow: true },
    { value: 0, label: t('待支付'), isShow: true },
    { value: 1, label: !isStore() ? t('待发货') : t('待发货/待备货'), isShow: true },
    { value: 2, label: !isStore() ? t('已发货') : t('已发货/待核销'), isShow: true },
    { value: 3, label: t('已取消'), isShow: true },
    { value: 4, label: t('无效'), isShow: true },
    { value: 5, label: !isStore() ? t('已完成') : t('已完成/已核销'), isShow: true },
    { value: -2, label: t('已删除'), isShow: true }
]);

const settlementStatusList = [
    { value: -1, label: t('全部') },
    { value: 0, label: t('待结算') },
    { value: 1, label: t('已结算') }
];

const filterParams = reactive({
    page: 1,
    size: 10,
    /** 接口字段：订单号（模糊） */
    orderSn: "",
    orderStatus: -1 as number,
    /** 接口字段：下单开始/结束（YYYY-MM-DD） */
    orderTimeStart: "",
    orderTimeEnd: "",
    /** 接口字段：会员用户名（模糊） */
    username: "",
    /** 接口字段：结算状态：0/1 */
    settlementStatus: -1 as -1 | 0 | 1,
    settlementSn: "",
    mobile: ""
});

const list = ref<CreditOrderRecord[]>([]);
const total = ref(0);

const onSearchSubmit = () => {
    filterParams.page = 1;
    loadList();
};

const resetParams = () => {
    filterParams.page = 1;
    filterParams.size = 10;
    filterParams.orderSn = "";
    filterParams.orderStatus = -1;
    filterParams.orderTimeStart = "";
    filterParams.orderTimeEnd = "";
    filterParams.username = "";
    filterParams.settlementStatus = -1;
    filterParams.settlementSn = "";
    filterParams.mobile = "";
    loadList();
};

const loadList = async () => {
    loading.value = true;
    try {
        const params: CreditOrderListQuery = {
            page: filterParams.page,
            size: filterParams.size,
            orderSn: filterParams.orderSn || undefined,
            username: filterParams.username || undefined,
            settlementStatus: filterParams.settlementStatus === -1 ? undefined : filterParams.settlementStatus,
            orderTimeStart: filterParams.orderTimeStart || undefined,
            orderTimeEnd: filterParams.orderTimeEnd || undefined,
            orderStatus: filterParams.orderStatus === -1 ? undefined : filterParams.orderStatus,
            settlementSn: filterParams.settlementSn || undefined,
            mobile: filterParams.mobile || undefined
        };

        const res = await getCreditOrderList(params);
        list.value = res.records || [];
        total.value = res.total || 0;
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
}

.textR {
    text-align: right;
}
</style>
