/** 会员留言/反馈列表 - 管理会员的建议、投诉、商品咨询等留言，支持回复 */
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
                                        <label class="control-label"
                                            ><span>{{ $t("留言内容：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入留言内容')"
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
                                        <label class="control-label"
                                            ><span>{{ $t("留言类型：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <el-select
                                                v-model="magType"
                                                multiple
                                                clearable
                                                :placeholder="$t('请选择')"
                                                @clear="onSearchSubmit"
                                                @change="changeType"
                                            >
                                                <el-option v-for="item in options" :label="item.label" :value="item.value" :key="item.value" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("回复状态：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <el-select
                                                v-model="filterParams.status"
                                                clearable
                                                :placeholder="$t('请选择')"
                                                @clear="onSearchSubmit"
                                                @change="onSearchSubmit"
                                            >
                                                <el-option v-for="item in statusOptions" :label="item.label" :value="item.value" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <div class="control-container">
                                            <el-button type="primary" plain @click="onSearchSubmit">{{ $t("搜索") }}</el-button>
                                            <el-button plain @click="resetParams">{{ $t("重置") }}</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-if="canModifyFeedback" class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <div class="simple-form-field">
                                        <div class="form-group">
                                            <el-space>
                                                <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                                    <template #reference>
                                                        <el-button :disabled="selectedIds.length === 0">{{ $t("批量删除") }}</el-button>
                                                    </template>
                                                </el-popconfirm>
                                                <span v-if="selectedIds.length > 0">
                                                    {{ $t("已选择：") }}<b>{{ selectedIds.length }}</b> {{ $t("项") }}
                                                </span>
                                            </el-space>
                                        </div>
                                    </div>
                                </el-space>
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
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column v-if="canModifyFeedback" type="selection" width="32" />
                            <el-table-column :width="120" :label="$t('会员名称')" prop="username"></el-table-column>
                            <el-table-column :label="$t('类型')" prop="typeName" min-width="220">
                                <template #default="{ row }">
                                    <div class="feedback-type-name">{{ row.typeName }}</div>
                                    <DialogForm
                                        v-if="row.orderSn"
                                        :params="{ act: 'detail', id: row.orderId }"
                                        :showClose="false"
                                        :showOnOk="false"
                                        :title="$t('订单详情 ') + row.orderSn"
                                        isDrawer
                                        :style="{ display: 'inline-block' }"
                                        path="order/order/Info"
                                        width="880px"
                                        @callback="loadFilter"
                                    >
                                        <a class="feedback-type-link">
                                            {{ row.orderSn }}
                                        </a>
                                    </DialogForm>
                                    <div v-if="isPlatformAdmin && row.shopId && row.shop" class="feedback-type-meta">
                                        {{ row.shop.shopTitle }}
                                    </div>
                                    <div class="feedback-type-meta" v-if="row.productId > 0">
                                        {{ row.product?.productName || "--" }}
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('留言内容')" prop="content" min-width="200">
                                <template #default="{ row }">
                                    {{ row.content }}
                                </template>
                            </el-table-column>
                            <el-table-column :width="160" :label="$t('留言时间')" prop="addTime"></el-table-column>
                            <el-table-column :label="$t('回复')" prop="reply.content" />
                            <el-table-column v-if="canOpenFeedback" :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        v-if="canOpenFeedback"
                                        :params="{ act: 'detail', id: row.id }"
                                        isDrawer
                                        path="user/feedback/Info"
                                        :title="$t('回复会员留言')"
                                        width="560px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t("回复") }}</a>
                                    </DialogForm>
                                    <template v-if="canModifyFeedback">
                                        <el-divider direction="vertical" />
                                        <DeleteRecord :params="{ id: row.id }" :requestApi="delFeedback" @afterDelete="loadFilter"
                                            >{{ $t("删除") }}
                                        </DeleteRecord>
                                    </template>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t("暂无数据") }}</div>
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
import { computed, onMounted, ref, watch } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { FeedbackFilterParams, FeedbackFilterState } from "@/types/user/feedback.d";
import { batchSubmit, delFeedback, getFeedbackList } from "@/api/user/feedback";
import { useRouter } from "vue-router";
import { useListRequest } from "@/hooks/useListRequest";
import { useI18n } from "vue-i18n";
import { getAdminType } from "@/utils/storage";
import { useUserStore } from "@/store/user";

const { t } = useI18n();
const adminType = getAdminType();
const isPlatformAdmin = adminType === "admin";
const isShopOrStoreAdmin = adminType === "shop" || adminType === "store";
const limitedFeedbackTypes = [2];
const allOptions = [
    {
        label: t("建议"),
        value: 0
    },
    {
        label: t("投诉"),
        value: 1
    },
    {
        label: t("商品"),
        value: 2
    },
    {
        label: t("其他"),
        value: 3
    },
    {
        label: t("店铺投诉"),
        value: 4
    },
    {
        label: t("订单问题"),
        value: 5
    },
    {
        label: t("订单咨询"),
        value: 6
    }
];
const options = computed(() => (isShopOrStoreAdmin ? allOptions.filter((item) => limitedFeedbackTypes.includes(item.value)) : allOptions));
const statusOptions = [
    {
        label: t("待回复"),
        value: 0
    },
    {
        label: t("已回复"),
        value: 1
    },
    {
        label: t("无效"),
        value: 2
    }
];
const config: any = useConfigStore();
const userStore = useUserStore();
const canModifyFeedback = computed(() => {
    const authList = userStore.userInfo?.authList || [];
    return authList.includes("all") || authList.includes("feedbackModifyManage");
});
const canOpenFeedback = computed(() => {
    const authList = userStore.userInfo?.authList || [];
    return canModifyFeedback.value || (isShopOrStoreAdmin && (authList.includes("all") || authList.includes("feedbackManage")));
});
// 基本参数定义
const query = useRouter().currentRoute.value.query;
const defaultFeedbackTypes = "";
const magType = ref<number[]>([]);
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
    onBatchAction,
    resetParams: resetListParams
} = useListRequest<FeedbackFilterState, FeedbackFilterParams>({
    apiFunction: getFeedbackList,
    idKey: "id",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        type: defaultFeedbackTypes,
        status: "",
        page: 1,
        size: config.get("pageSize"),
        excludeReply: true
    }
});
watch(filterParams, (newVal) => {
    if (!newVal.type) {
        magType.value = [];
    }
});
const changeType = (value: number[]) => {
    const allowedTypes = isShopOrStoreAdmin ? limitedFeedbackTypes : allOptions.map((item) => item.value);
    const selectedTypes = value.filter((type) => allowedTypes.includes(type));
    magType.value = selectedTypes;
    filterParams.type = magType.value.join(",");
    loadFilter();
};
const resetParams = () => {
    resetListParams();
    magType.value = [];
};
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};
onMounted(() => {
    if (query && query.type) {
        let typeArr: number[];
        if (typeof query.type === "string") {
            typeArr = query.type.split(",").map(Number);
        } else if (Array.isArray(query.type)) {
            // 如果 query.type 是数组, 直接将它们转换为数字数组
            typeArr = query.type.map(Number);
        } else {
            // 处理 query.type 为其他类型的情况
            typeArr = [];
        }
        const allowedTypes = isShopOrStoreAdmin ? limitedFeedbackTypes : allOptions.map((item) => item.value);
        const safeTypes = typeArr.filter((type) => allowedTypes.includes(type));
        magType.value = safeTypes;
        filterParams.type = magType.value.join(",");
    }
    if (query && query.status) {
        filterParams.status = Number(query.status) as any;
    }
    loadFilter();
});
</script>
<style lang="less" scoped>
.feedback-type-name {
    line-height: 22px;
    white-space: nowrap;
}

.feedback-type-link {
    display: block;
    line-height: 20px;
    white-space: nowrap;
}

.feedback-type-meta {
    color: #999;
    font-size: 12px;
    line-height: 18px;
    white-space: normal;
    word-break: break-word;
}
</style>
