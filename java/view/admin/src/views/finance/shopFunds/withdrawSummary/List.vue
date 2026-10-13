<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="container">
                <div class="lyecs-table-list-warp">
                    <div class="list-table-tool lyecs-search-warp">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入{shopTypeName}名称', { shopTypeName })"
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
                                        <TigTabs v-model="filterParams.status" :tabs="shopStatus" @onTabChange="onChange"></TigTabs>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="table-container">
                        <a-spin :spinning="loading">
                            <el-table :data="filterState" :loading="loading" :total="total" row-key="shopId" @sort-change="onSortChange">
                                <el-table-column :label="$t('{shopTypeName}名称', { shopTypeName })">
                                    <template #default="{ row }">
                                        {{ row.shopTitle || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="`${shopTypeName}LOGO`">
                                    <template #default="{ row }">
                                        <Image :src="row.shopLogo" fit="contain" style="height: 40px; width: 40px" />
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('{shopTypeName}状态', { shopTypeName })" align="center">
                                    <template #default="{ row }">
                                        <template v-if="row.status == 10 || row.status == 4">
                                            <StatusDot color="red" :flicker="true"></StatusDot>
                                        </template>
                                        <template v-else-if="row.status == 1">
                                            <StatusDot color="green" :flicker="true"></StatusDot>
                                        </template>
                                        <span v-if="row.status === 10 || row.status === 4" style="color: red">{{ row.statusText || "--" }}</span>
                                        <span v-else-if="row.status === 1" style="color: green">{{ row.statusText || "--" }}</span>
                                        <span v-else>{{ row.statusText || "--" }}</span>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('{shopTypeName}剩余资金', { shopTypeName })" sortable="custom" prop="shopMoney" min-width="100">
                                    <template #default="{ row }">
                                        {{ priceFormat(row.shopMoney) }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('已提现金额')" sortable="custom" prop="withdrawnAmount">
                                    <template #default="{ row }">
                                        {{ priceFormat(row.withdrawnAmount) }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('待提现金额')" sortable="custom" prop="pendingWithdrawAmount">
                                    <template #default="{ row }">
                                        {{ priceFormat(row.pendingWithdrawAmount) }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('开店时间')" prop="addTime">
                                    <template #default="{ row }">
                                        {{ row.addTime ?? "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :width="120" fixed="right" :label="$t('操作')">
                                    <template #default="{ row }">
                                        <DialogForm
                                            :params="{
                                                shopId: row.shopId,
                                                shopTitle: row.shopTitle,
                                                pendingWithdrawAmount: row.pendingWithdrawAmount,
                                                withdrawnAmount: row.withdrawnAmount
                                            }"
                                            :showClose="false"
                                            :showOnOk="false"
                                            isDrawer
                                            path="finance/shopFunds/withdrawSummary/Detail"
                                            :title="$t('提现详情')"
                                            width="1200px"
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
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { Image } from "@/components/image";
import StatusDot from "@/components/form/src/StatusDot.vue";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { priceFormat } from "@/utils/format";
import { timestampToDate } from "@/utils/time";
import type { ShopWithdrawSummaryFilterParams, ShopWithdrawSummaryItem } from "@/types/merchant/capitalfund/withdrawSummary";
import { getShopWithdrawSummaryList } from "@/api/merchant/capitalfund/withdrawSummary";
import { computed, ref } from "vue";
import { isMerchant, isS2b2c, isStore } from "@/utils/version";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();

const shopTypeName = computed(() => {
    return isStore() ? t('门店') : isMerchant() ? t('店铺') : t('店铺');
});

const shopStatus = ref<any[]>([
    { label: t('全部'), value: "", isShow: true },
    { label: t('开业'), value: 1, isShow: true },
    { label: t('停业'), value: 4, isShow: true },
    { label: t('打烊'), value: 10, isShow: true }
]);

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange
} = useListRequest<ShopWithdrawSummaryItem, ShopWithdrawSummaryFilterParams>({
    apiFunction: getShopWithdrawSummaryList,
    idKey: "shopId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        status: "",
        shopType: isMerchant() ? 1 : isStore() ? 2 : 1
    }
});

// 初始化加载
loadFilter();

const onChange = (status: number | string) => {
    // @ts-expect-error 允许 string/number
    filterParams.status = status;
    filterParams.page = 1;
    loadFilter();
};
</script>

<style lang="less" scoped></style>
