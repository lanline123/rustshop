/** 站内信发送记录列表 - 管理站内信发送、撤回、删除 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('输入标题')" clearable @clear="onSearchSubmit">
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <DialogForm
                                            :params="{ act: 'add' }"
                                            isDrawer
                                            path="user/messageLog/Info"
                                            :title="$t('发送站内信')"
                                            width="1040px"
                                            @okCallback="loadFilter"
                                        >
                                            <el-button type="primary">{{ $t('发送站内信') }}</el-button>
                                        </DialogForm>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <el-popconfirm :title="$t('您确认要批量撤回所选数据吗？')" @confirm="onBatchSubmit('recall')">
                                            <template #reference>
                                                <el-button :disabled="selectedIds.length === 0">{{ $t('批量撤回') }}</el-button>
                                            </template>
                                        </el-popconfirm>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <el-space>
                                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                                <template #reference>
                                                    <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                                </template>
                                            </el-popconfirm>
                                            <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                        </el-space>
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
                            row-key="messageLogId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('标题')" prop="messageTitle"></el-table-column>
                            <el-table-column :label="$t('内容')" prop="messageContent" min-width="120"></el-table-column>
                            <el-table-column :label="$t('h5链接')" min-width="120">
                                <template #default="{ row }">
                                    <div v-if="row.messageLink">
                                        <a :href="formatH5Link(row.messageLink)" target="_blank">{{ formatH5Link(row.messageLink) }}</a>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('pc链接')" min-width="120">
                                <template #default="{ row }">
                                    <div v-if="row.messageLink">
                                        <a :href="formatPcLink(row.messageLink)" target="_blank">{{ formatPcLink(row.messageLink) }}</a>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="100" :label="$t('发送类型')" prop="sendTypeName"></el-table-column>
                            <el-table-column :width="100" :label="$t('发送状态')" prop="messageType">
                                <template #default="{ row }">
                                    <span :style="{ color: row.messageType === 0 ? 'green' : 'red' }">{{ row.statusName }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :width="160" :label="$t('发送日期')" prop="sendTime"></el-table-column>
                            <el-table-column :width="120" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DeleteRecord
                                        v-if="row.isRecall == 0"
                                        :params="{ id: row.messageLogId }"
                                        :title="$t('确认要撤回该条消息吗？')"
                                        :requestApi="recallMessageLog"
                                        @afterDelete="loadFilter"
                                        >{{ $t('撤回') }} </DeleteRecord>
                                    <el-divider v-if="row.isRecall == 0" direction="vertical" />
                                    <DeleteRecord :params="{ id: row.messageLogId }" :requestApi="delMessageLog" @afterDelete="loadFilter">{{ $t('删除') }} </DeleteRecord>
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
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import type { MessageLogFilterParams, MessageLogFilterState } from "@/types/user/messageLog";
import { batchSubmit, delMessageLog, getMessageLogList, recallMessageLog } from "@/api/user/messageLog";
import { useListRequest } from "@/hooks/useListRequest";
const config: any = useConfigStore();

function parseLinkData(raw: any): any {
    if (!raw) return null;
    if (typeof raw === "object") return raw;
    if (typeof raw === "string") {
        try {
            return JSON.parse(raw);
        } catch {
            return raw;
        }
    }
    return raw;
}

function getLinkLabel(raw: any): string {
    const params = parseLinkData(raw);
    if (!params || typeof params === "string") return "";
    return params.label ? params.label + (params.name ? " | " + params.name : "") : "";
}

function toQueryString(data: Record<string, any>): string {
    if (!data || !Object.keys(data).length) return "";
    const qs = Object.entries(data)
        .filter(([, v]) => v !== undefined && v !== null && v !== "")
        .map(([k, v]) => `${k}=${v}`)
        .join("&");
    return qs ? `?${qs}` : "";
}

function formatH5Link(raw: any): string {
    const params = parseLinkData(raw);
    if (!params || typeof params === "string") return params || "";
    if (!params.path) return "";
    const h5Domain = config.get("h5Domain") || "";
    const id = params.data?.id;
    const urlParameter = params.data ? toQueryString(params.data) : "";
    switch (params.path) {
        case "product":
            return `${h5Domain}/pages/product/index${urlParameter}`;
        case "list":
            return `${h5Domain}/pages/search/result?categoryId=${id}`;
        case "article":
            return `${h5Domain}/pages/article/news/detail?id=${id}`;
        case "shop":
            return `${h5Domain}/pages/shop/index?shopId=${id}`;
        case "brand": {
            if (id) {
                const d = { ...params.data, brand_id: id };
                delete d.id;
                return `${h5Domain}/pages/search/result${toQueryString(d)}`;
            }
            return `${h5Domain}/pages/search/result${urlParameter}`;
        }
        case "coupon":
            return `${h5Domain}/pages/coupon/detail${urlParameter}`;
        case "base":
            return `${h5Domain}${params.link || "/pages/index/index"}`;
        case "custom":
            return params.appLink || "";
        case "decorate":
            return `${h5Domain}/pages/customPage/index?id=${id}`;
        case "default":
            return `${h5Domain}${params.link || ""}`;
        case "order":
            return `${h5Domain}/pages/user/order/info?id=${params.id}`;
        default:
            return `${h5Domain}/${params.path}`;
    }
}

function formatPcLink(raw: any): string {
    const params = parseLinkData(raw);
    if (!params || typeof params === "string") return params || "";
    if (!params.path) return "";
    const pcDomain = config.get("pcDomain") || "";
    const id = params.data?.id || params.id;
    const sn = params.sn || params.data?.sn;
    switch (params.path) {
        case "product":
            return `${pcDomain}/item/${sn || id}`;
        case "exchange":
            return `${pcDomain}/exchange/${id}`;
        case "list":
            return `${pcDomain}/list/?cat=${id}`;
        case "article":
            return `${pcDomain}/article/news/list?articleId=${id}`;
        case "brand":
            return `${pcDomain}/list?brand=${id}`;
        case "coupon":
            return `${pcDomain}/coupon/list/?id=${id}`;
        case "shop":
            return `${pcDomain}/shop/${id}`;
        case "store":
            return `${pcDomain}/store/?id=${id}`;
        case "base":
            return `${pcDomain}/${params.link || ""}`;
        case "order":
            return `${pcDomain}/member/order/info?id=${params.id || id}`;
        case "custom":
            return params.link || "";
        default:
            return `${pcDomain}/${params.path}`;
    }
}
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
    onBatchAction
} = useListRequest<MessageLogFilterState, MessageLogFilterParams>({
    apiFunction: getMessageLogList,
    idKey: "messageLogId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: ""
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};
// 初始化加载
loadFilter();
</script>
