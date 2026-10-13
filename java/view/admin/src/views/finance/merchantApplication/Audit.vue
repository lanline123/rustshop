<template>
    <div class="lyecs-table-list-warp">
        <div class="list-table-tool lyecs-search-warp">
            <el-form :model="filterParams">
                <div class="advanced-search-warp list-table-tool-row">
                    <div class="simple-form-warp">
                        <div class="simple-form-field">
                            <div class="form-group">
                                <label class="control-label"><span>{{ shopTypeName }}：</span></label>
                                <div class="control-container">
                                    <SelectStore v-if="isStore()" v-model:storeId="filterParams.shopId" :placeholder="shopNamePlaceholder" @onChange="onSearchSubmit"></SelectStore>
                                    <SelectShop v-else v-model:shopId="filterParams.shopId" :placeholder="shopNamePlaceholder" @onChange="onSearchSubmit"></SelectShop>
                                </div>
                            </div>
                        </div>
                        <div class="simple-form-field">
                            <div class="form-group">
                                <label class="control-label"><span>{{ $t('主体类型：') }}</span></label>
                                <div class="control-container">
                                    <el-select clearable :placeholder="$t('请选择主体类型')" v-model="filterParams.applicantType" @change="onSearchSubmit">
                                        <el-option value="SHOP" :label="shopTypeName" />
                                        <el-option value="VENDOR" :label="$t('供应商')" />
                                    </el-select>
                                </div>
                            </div>
                        </div>
                        <div class="simple-form-field">
                            <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                        </div>
                        <div class="simple-form-field">
                            <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                        </div>
                    </div>
                </div>
            </el-form>
        </div>
        <div class="table-container">
            <a-spin :spinning="loading">
                <el-table :data="filterState" :loading="loading" row-key="id">
                    <el-table-column :label="$t('申请主体')" min-width="180" fixed="left">
                        <template #default="{ row }">
                            <div>
                                {{ row.applicantName || row.shopName || row.merchantName }}
                                <span class="applicant-type"> （{{ row.applicantType === "VENDOR" ? $t('供应商') : shopTypeName }}） </span>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('商户名称')" prop="merchantName" min-width="150" />
                    <el-table-column :label="$t('支付渠道')" width="90">
                        <template #default="{ row }">{{ formatPayChannel(row.payChannel) }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('主体类型')" width="100">
                        <template #default="{ row }">
                            <span v-if="row.organizationType === '2'" disable-transitions>{{ $t('企业') }}</span>
                            <span v-else-if="row.organizationType === '4'" disable-transitions>{{ $t('个体工商户') }}</span>
                            <span v-else-if="row.organizationType === '2401'" disable-transitions>{{ $t('个人') }}</span>
                            <span v-else>-</span>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('状态')" width="150">
                        <template #default="{ row }">
                            <span :class="getStatusClass(row.status)">{{ row.statusDesc || row.status }}</span>
                            <el-tooltip v-if="row.rejectReason && row.status !== 'FINISH'" :content="(row.reasonLabel || $t('失败原因')) + '：' + row.rejectReason" placement="top">
                                <el-icon style="margin-left: 4px; color: #f56c6c"><QuestionFilled /></el-icon>
                            </el-tooltip>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('子商户号')" min-width="150">
                        <template #default="{ row }">{{ row.subMchId || "—" }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('申请时间')" prop="createTime" width="170" />
                    <el-table-column :width="230" fixed="right" :label="$t('操作')">
                        <template #default="{ row }">
                            <DialogForm
                                :params="{ act: 'detail', id: row.id }"
                                isDrawer
                                :showFooter="false"
                                path="finance/merchantApplication/DetailForm"
                                :title="$t('进件详情')"
                                width="760px"
                            >
                                <a class="btn-link">{{ $t('详情') }}</a>
                            </DialogForm>
                            <template v-if="row.status === 'PENDING_REVIEW'">
                                <el-divider direction="vertical" />
                                <a class="btn-link" @click="handleApprove(row)">{{ $t('通过并提交进件') }}</a>
                                <el-divider direction="vertical" />
                                <DialogForm
                                    :params="{ id: row.id }"
                                    isDrawer
                                    path="finance/merchantApplication/RejectForm"
                                    :title="$t('审核拒绝')"
                                    width="480px"
                                    :okText="$t('确认拒绝')"
                                    @okCallback="loadFilter"
                                >
                                    <a class="btn-link btn-danger">{{ $t('拒绝') }}</a>
                                </DialogForm>
                            </template>
                            <template v-if="row.status === 'NEED_SIGN' && row.signUrl">
                                <el-divider direction="vertical" />
                                <a class="btn-link" @click="copySignUrl(row.signUrl)">{{ $t('签约链接') }}</a>
                            </template>
                            <template v-if="!['FINISH', 'REJECTED', 'DRAFT', 'PENDING_REVIEW'].includes(row.status) && row.applymentId">
                                <el-divider direction="vertical" />
                                <a class="btn-link" @click="handleRefresh(row)">{{ $t('刷新状态') }}</a>
                            </template>
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
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { message } from "ant-design-vue";
import { ElMessageBox } from "element-plus";
import { QuestionFilled } from "@element-plus/icons-vue";
import { Pagination } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { SelectShop, SelectStore } from "@/components/select";
import { isStore } from "@/utils/version";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { getMerchantApplicationList, refreshMerchantApplication, approveMerchantApplication } from "@/api/finance/merchantApplication";
import type { MerchantApplicationItem, MerchantApplicationListParams } from "@/types/finance/merchantApplication.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const shopTypeName = isStore() ? t("门店") : t("店铺");
const shopNamePlaceholder = isStore() ? t("请输入门店名称") : t("请输入店铺名称");

const formatPayChannel = (val?: string) => {
    const map: Record<string, string> = { WECHAT: t("微信"), ALIPAY: t("支付宝"), HUIFU: t("汇付天下") };
    return map[val ?? ""] || val || t("微信");
};

const getStatusClass = (status?: string) => {
    const map: Record<string, string> = {
        DRAFT: "gray",
        PENDING_REVIEW: "yellow",
        SUBMITTED: "black",
        CHECKING: "black",
        AUDITING: "black",
        ACCOUNT_NEED_VERIFY: "yellow",
        NEED_SIGN: "yellow",
        FINISH: "green",
        REJECTED: "red"
    };
    return map[status ?? ""] ?? "gray";
};

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<MerchantApplicationItem, MerchantApplicationListParams>({
    apiFunction: getMerchantApplicationList,
    idKey: "id",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        shopId: "",
        applicantType: "",
        status: "PENDING_REVIEW"
    }
});

const copySignUrl = async (url: string) => {
    try {
        await navigator.clipboard.writeText(url);
        message.success(t('已复制签约链接'));
    } catch {
        message.error(t('复制失败'));
    }
};

const handleRefresh = async (row: MerchantApplicationItem) => {
    if (!row.id) return;
    try {
        await refreshMerchantApplication({ id: row.id });
        message.success(t('状态已刷新'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message || t('刷新失败'));
    }
};

const handleApprove = async (row: MerchantApplicationItem) => {
    if (!row.id) return;
    try {
        await ElMessageBox.confirm(t('确认审核通过并提交进件吗？'), t('审核通过'), {
            type: "warning",
            confirmButtonText: t('确认通过'),
            cancelButtonText: t('取消')
        });
    } catch {
        return;
    }
    try {
        await approveMerchantApplication({ id: row.id });
        message.success(t('已通过并提交进件'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message || t('操作失败'));
    }
};

loadFilter();
</script>

<style lang="less" scoped>
.black {
    color: #333;
}
.applicant-type {
    margin-left: 2px;
}
</style>
