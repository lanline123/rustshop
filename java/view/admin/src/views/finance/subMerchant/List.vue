<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ shopTypeName }}：</span></label>
                                        <div class="control-container">
                                            <SelectStore
                                                v-if="isStore()"
                                                v-model:storeId="filterParams.shopId"
                                                :placeholder="shopNamePlaceholder"
                                                @onChange="onSearchSubmit"
                                            ></SelectStore>
                                            <SelectShop
                                                v-else
                                                v-model:shopId="filterParams.shopId"
                                                :placeholder="shopNamePlaceholder"
                                                @onChange="onSearchSubmit"
                                            ></SelectShop>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('子商户号：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.subMchId"
                                                :placeholder="$t('输入子商户号')"
                                                clearable
                                                @clear="onSearchSubmit"
                                                @keyup.enter="onSearchSubmit"
                                            />
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select clearable :placeholder="$t('请选择状态')" v-model="filterParams.status" @change="onSearchSubmit">
                                                <el-option :value="0" :label="$t('待激活')" />
                                                <el-option :value="1" :label="$t('已激活')" />
                                                <el-option :value="2" :label="$t('已冻结')" />
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
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="finance/subMerchant/BindForm"
                                        :title="$t('绑定子商户')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('绑定子商户') }}</el-button>
                                    </DialogForm>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" row-key="id">
                            <el-table-column :label="shopTypeName" min-width="180">
                                <template #default="{ row }">
                                    <div>{{ row.shopName || "-" }}</div>
                                    <div class="extra">ID: {{ row.shopId }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('子商户号')" min-width="160">
                                <template #default="{ row }">
                                    <span>{{ row.subMchId }}</span>
                                    <a class="btn-link" style="margin-left: 6px" @click="copyText(row.subMchId)">{{ $t('复制') }}</a>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商户名称')" prop="merchantName" min-width="140" />
                            <el-table-column :label="$t('支付渠道')" width="100">
                                <template #default="{ row }">
                                    <span v-if="!['WECHAT', 'ALIPAY', 'HUIFU'].includes(row.payChannel)">{{ row.payChannel || "-" }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('来源')" width="110">
                                <template #default="{ row }">
                                    <span v-if="row.applicationId">{{ $t('进件创建') }}</span>
                                    <span v-else>{{ $t('手动绑定') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('状态')" width="100">
                                <template #default="{ row }">
                                    <span :class="getStatusClass(row.status)">{{ row.statusDesc || getStatusText(row.status) }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="createTime" width="170" />
                            <el-table-column :width="120" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DeleteRecord
                                        :params="{ id: row.id }"
                                        :requestApi="unbindSubMerchant"
                                        :title="$t('确认解绑该子商户？')"
                                        :message="$t('解绑成功')"
                                        @afterDelete="loadFilter"
                                        >{{ $t('解绑') }} </DeleteRecord>
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
import { DeleteRecord, Pagination } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { SelectShop, SelectStore } from "@/components/select";
import { isStore } from "@/utils/version";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { getSubMerchantList, unbindSubMerchant } from "@/api/finance/subMerchant";
import type { SubMerchantItem, SubMerchantListParams } from "@/types/finance/subMerchant.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const shopTypeName = isStore() ? t("门店") : t("店铺");
const shopNamePlaceholder = isStore() ? t("请输入门店名称") : t("请输入店铺名称");

const getStatusClass = (status?: number) => {
    const map: Record<number, string> = { 0: "yellow", 1: "green", 2: "red" };
    return map[status ?? 0] || "gray";
};
const getStatusText = (status?: number) => {
    const map: Record<number, string> = { 0: t('待激活'), 1: t('已激活'), 2: t('已冻结') };
    return map[status ?? 0] || "";
};

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<SubMerchantItem, SubMerchantListParams>({
    apiFunction: getSubMerchantList,
    idKey: "id",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        shopId: "",
        subMchId: "",
        payChannel: "",
        status: ""
    }
});

const copyText = async (text: string) => {
    const value = text ?? "";
    // navigator.clipboard 仅在安全上下文（HTTPS/localhost）可用，HTTP 下需降级到 execCommand
    try {
        if (window.isSecureContext && navigator.clipboard) {
            await navigator.clipboard.writeText(value);
            message.success(t('已复制'));
            return;
        }
        throw new Error("clipboard unavailable");
    } catch {
        const textarea = document.createElement("textarea");
        textarea.value = value;
        textarea.style.position = "fixed";
        textarea.style.opacity = "0";
        document.body.appendChild(textarea);
        textarea.select();
        try {
            const successful = document.execCommand("copy");
            successful ? message.success(t('已复制')) : message.error(t('复制失败'));
        } catch {
            message.error(t('复制失败'));
        } finally {
            document.body.removeChild(textarea);
        }
    }
};

loadFilter();
</script>
