<!--
 * @file 店铺端 会员管理（D1116-shopMember，2026-07-10 CR：文案「客户→会员」，添加/导入改抽屉对齐平台）
 * @description 本店会员列表 + 多条件筛选 + 添加/导入。字段与筛选对齐 user/shopMember/list。
 *   脱敏由后端服务端完成（列表直接返回打码串），前端直接展示。
 *   平台总开关关闭 → 冻结态（顶部横幅 + 写操作置灰）；添加/导入按平台 allowAdd/allowImport 门控。
 *   添加会员抽屉对齐平台 user/user/Info.vue（去会员等级）；导入会员抽屉对齐平台 ImportUser.vue，均走店铺作用域接口。
-->
<template>
    <div class="container">
        <el-alert
            v-if="frozen"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 12px"
            title="平台暂未开放店铺会员功能，当前所有会员功能处于冻结状态。如有疑问请联系平台客服。"
        />

        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row advanced-search-warp">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>关键字：</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                placeholder="输入会员名称/手机号"
                                                clearable
                                                @keyupEnter="onSearchSubmit"
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
                                        <label class="control-label"><span>累计消费：</span></label>
                                        <div class="control-container range-container">
                                            <TigInput type="decimal" v-model="filterParams.minConsume" width="110px" placeholder="最低值" />
                                            <span class="range-sep">-</span>
                                            <TigInput type="decimal" v-model="filterParams.maxConsume" width="110px" placeholder="最高值" />
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>入会时间：</span></label>
                                        <SelectTimeInterval
                                            type="date"
                                            v-model:startDate="filterParams.joinStartDate"
                                            v-model:endDate="filterParams.joinEndDate"
                                            clearable
                                        />
                                    </div>
                                </div>
                                <div class="simple-form-field field-consume">
                                    <div class="form-group">
                                        <label class="control-label"><span>最近消费时间：</span></label>
                                        <SelectTimeInterval
                                            type="date"
                                            v-model:startDate="filterParams.consumeStartDate"
                                            v-model:endDate="filterParams.consumeEndDate"
                                            clearable
                                        />
                                    </div>
                                    <el-radio-group v-model="quickRange" class="quick-range" @change="onQuickRange">
                                        <el-radio-button v-for="item in quickOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio-button>
                                    </el-radio-group>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <el-button type="primary" plain @click="onSearchSubmit">搜索</el-button>
                                        <el-button plain @click="onReset">重置</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>

                <div v-if="showAdd || showImport" class="list-table-tool-btns">
                    <DialogForm
                        v-if="showAdd"
                        :disabled="frozen"
                        isDrawer
                        title="添加会员"
                        width="560px"
                        path="merchant/customer/customer/src/AddCustomer"
                        @okCallback="loadFilter"
                    >
                        <el-button type="primary" :disabled="frozen">添加会员</el-button>
                    </DialogForm>
                    <DialogForm
                        v-if="showImport"
                        :disabled="frozen"
                        isDrawer
                        title="导入会员"
                        width="560px"
                        :showOnOk="false"
                        path="merchant/customer/customer/src/ImportCustomer"
                        @okCallback="loadFilter"
                        @callback="loadFilter"
                    >
                        <el-button :disabled="frozen">导入会员</el-button>
                    </DialogForm>
                </div>

                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="userId">
                            <el-table-column label="会员信息" min-width="200">
                                <template #default="{ row }">
                                    <div class="user-cell">
                                        <el-avatar :size="36" :src="avatarFormat(row.avatar)">
                                            <el-icon><UserFilled /></el-icon>
                                        </el-avatar>
                                        <div class="user-meta">
                                            <div class="name">{{ row.nickname || "--" }}</div>
                                            <div class="mobile">{{ row.mobile || "--" }}</div>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column label="累计消费" min-width="110">
                                <template #default="{ row }">{{ priceFormat(Number(row.orderAmount) || 0) }}</template>
                            </el-table-column>
                            <el-table-column label="订单数" min-width="80">
                                <template #default="{ row }">{{ row.orderCount ?? 0 }}</template>
                            </el-table-column>
                            <el-table-column label="入会时间" min-width="160">
                                <template #default="{ row }">{{ row.joinTime ? timestampToDate(row.joinTime) : "--" }}</template>
                            </el-table-column>
                            <el-table-column label="最近消费" min-width="160">
                                <template #default="{ row }">{{ row.lastConsumeTime ? timestampToDate(row.lastConsumeTime) : "--" }}</template>
                            </el-table-column>
                            <el-table-column fixed="right" label="操作" width="130">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <DialogForm
                                            :params="{ userId: row.userId, nickname: row.nickname }"
                                            isDrawer
                                            :title="`${row.nickname || '会员'} · 会员详情`"
                                            width="720px"
                                            path="merchant/customer/customer/src/Detail"
                                            :showOnOk="false"
                                        >
                                            <a class="btn-link">详情</a>
                                        </DialogForm>
                                        <template v-if="!frozen">
                                            <el-divider direction="vertical" />
                                            <DialogForm
                                                :params="{ userId: row.userId }"
                                                isDrawer
                                                title="打标签"
                                                width="800px"
                                                path="user/user/BatchBindTags"
                                                @okCallback="loadFilter"
                                            >
                                                <a class="btn-link">标签</a>
                                            </DialogForm>
                                        </template>
                                    </el-space>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp"><div v-if="!loading" class="empty-bg">暂无数据</div></div>
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
import { computed, ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { SelectTimeInterval } from "@/components/select";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { avatarFormat, priceFormat } from "@/utils/format";
import { UserFilled } from "@element-plus/icons-vue";
import { formattedDate, dateToTimestamp, timestampToDate } from "@/utils/time";
import { getDays } from "@/utils/util";
import { getShopCustomerList } from "@/api/merchant/shopMember";
import type { ShopCustomerFilterParams, ShopCustomerItem } from "@/types/merchant/shopMember.d";

/** 列表页展示态筛选参数（提交前转换为后端契约字段） */
interface CustomerListDisplayParams {
    keyword?: string;
    minConsume?: number | string;
    maxConsume?: number | string;
    joinStartDate?: string;
    joinEndDate?: string;
    consumeStartDate?: string;
    consumeEndDate?: string;
    page?: number;
    size?: number;
}

const configStore = useConfigStore();
const quickRange = ref<string>("");

// 平台总开关关闭 → 冻结态（顶部横幅 + 写操作置灰）
const frozen = computed<boolean>(() => Number(configStore.config.isOpenShopMember) !== 1);
// 新增/导入能力由平台子开关下发
const showAdd = computed<boolean>(() => Number(configStore.config.shopMemberAllowAdd) === 1);
const showImport = computed<boolean>(() => Number(configStore.config.shopMemberAllowImport) === 1);

const quickOptions = [
    { value: "today", label: "今天" },
    { value: "yesterday", label: "昨天" },
    { value: "7days", label: "近7天" },
    { value: "30days", label: "近30天" },
    { value: "halfYear", label: "近半年" }
];

// 展示态参数 → 后端契约字段（关键字直传 keyword + 日期转 Unix 秒）
const toApiParams = (p: CustomerListDisplayParams): ShopCustomerFilterParams => {
    const params: ShopCustomerFilterParams = { page: p.page, size: p.size, userType: 0 };
    const kw = (p.keyword || "").trim();
    if (kw) params.keyword = kw;
    if (p.minConsume !== "" && p.minConsume != null) params.orderAmountMin = p.minConsume;
    if (p.maxConsume !== "" && p.maxConsume != null) params.orderAmountMax = p.maxConsume;
    if (p.joinStartDate) params.joinTimeStart = dateToTimestamp(p.joinStartDate);
    if (p.joinEndDate) params.joinTimeEnd = dateToTimestamp(p.joinEndDate);
    if (p.consumeStartDate) params.lastConsumeTimeStart = dateToTimestamp(p.consumeStartDate);
    if (p.consumeEndDate) params.lastConsumeTimeEnd = dateToTimestamp(p.consumeEndDate);
    return params;
};

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<ShopCustomerItem, CustomerListDisplayParams>({
    apiFunction: (p: CustomerListDisplayParams) => getShopCustomerList(toApiParams(p)),
    idKey: "userId",
    defaultParams: {
        page: 1,
        size: configStore.get("pageSize"),
        keyword: "",
        minConsume: "",
        maxConsume: "",
        joinStartDate: "",
        joinEndDate: "",
        consumeStartDate: "",
        consumeEndDate: ""
    }
});

// 快捷区间作用于「最近消费时间」
const onQuickRange = (value: string) => {
    const fmtStart = (d: Date) => formattedDate(d, "YYYY-MM-DD 00:00:00");
    const fmtEnd = (d: Date) => formattedDate(d, "YYYY-MM-DD 23:59:59");
    const today = new Date();
    switch (value) {
        case "today":
            filterParams.consumeStartDate = fmtStart(today);
            filterParams.consumeEndDate = fmtEnd(today);
            break;
        case "yesterday":
            filterParams.consumeStartDate = fmtStart(getDays(1, "sub"));
            filterParams.consumeEndDate = fmtEnd(getDays(1, "sub"));
            break;
        case "7days":
            filterParams.consumeStartDate = fmtStart(getDays(7, "sub"));
            filterParams.consumeEndDate = fmtEnd(today);
            break;
        case "30days":
            filterParams.consumeStartDate = fmtStart(getDays(30, "sub"));
            filterParams.consumeEndDate = fmtEnd(today);
            break;
        case "halfYear":
            filterParams.consumeStartDate = fmtStart(getDays(180, "sub"));
            filterParams.consumeEndDate = fmtEnd(today);
            break;
    }
    onSearchSubmit();
};

const onReset = () => {
    quickRange.value = "";
    resetParams();
};

loadFilter();
</script>

<style lang="less" scoped>
// 提高特异性，压过全局 .list-table-tool .simple-form-field .form-group { width:100% }
// 生产构建 CSS 顺序可能导致 scoped 被全局覆盖，从而快捷区间掉到下一行
.list-table-tool .simple-form-field.field-consume {
    // 独占一行，保证「最近消费时间 + 快捷区间」有足够横向空间
    flex: 1 1 100%;
    flex-wrap: nowrap;
    align-items: center;
    :deep(.form-group) {
        width: auto !important;
        flex: 0 0 auto;
        max-width: none;
    }
    // 全局 control-label 的 break-spaces / 固定宽度会逼「最近消费时间：」换行
    :deep(.control-label) {
        width: auto !important;
        white-space: nowrap !important;
    }
    // 禁止快捷按钮被挤压换行，保持与日期选择器同一行
    :deep(.quick-range) {
        margin-left: 12px;
        flex-shrink: 0;
        flex-wrap: nowrap;
    }
}
.list-table-tool-btns {
    padding: 0 0 12px;
    display: flex;
    gap: 10px;
}
.range-container {
    display: flex;
    align-items: center;
    .range-sep {
        margin: 0 8px;
        color: #999;
    }
}
.user-cell {
    display: flex;
    align-items: center;
    gap: 10px;
    .user-meta {
        display: flex;
        flex-direction: column;
        .name {
            font-size: 13px;
        }
        .mobile {
            color: #999;
            font-size: 12px;
        }
    }
}
</style>
