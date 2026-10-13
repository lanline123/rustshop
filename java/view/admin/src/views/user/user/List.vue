/** * @file 客户列表页面 * @description 商城后台客户管理列表，支持客户信息管理和批量操作 * @features * - 多条件筛选（用户名/手机号/邮箱/来源/余额/积分） * -
客户标签功能（颜色标签+备注） * - 批量操作（批量打标签/批量修改等级/批量增加积分/批量增加余额） * - 客户导入功能 * - 查看客户详情/订单/账目明细 * -
支持启用/禁用客户 * - B2B模式支持企业认证状态显示 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @keyup.enter="onSearchSubmit">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("客户信息：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入客户名称/手机号/邮箱')"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("客户筛选：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <el-select v-model="filterParams.fromTag" clearable @change="onSearchSubmit">
                                                <el-option :value="1" :label="$t('微信公众号')" />
                                                <el-option :value="2" :label="$t('微信小程序')" />
                                                <el-option :value="3" label="H5" />
                                                <el-option :value="4" label="PC" />
                                                <el-option :value="5" label="APP" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("余额大于：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <TigInput type="decimal" allowEmptyDecimal v-model="filterParams.balance" clearable @clear="onSearchSubmit" />
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("积分大于：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <TigInput type="integer" allowEmptyDecimal v-model="filterParams.pointsGt" clearable @clear="onSearchSubmit" />
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("积分小于：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <TigInput type="integer" allowEmptyDecimal v-model="filterParams.pointsLt" clearable @clear="onSearchSubmit" />
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ $t("客户标签：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <el-select v-model="tagFilterType" @change="onTagFilterChange" style="min-width: 200px">
                                                <el-option :label="$t('全部')" value="all" />
                                                <el-option :label="tagFilterDisplayText" value="specify">
                                                    <span @click="openTagFilterDialog">{{ tagFilterDisplayText }}</span>
                                                </el-option>
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <div class="control-container">
                                            <el-button type="primary" plain @click="onSearchSubmit">{{ $t("搜索") }}</el-button>
                                            <el-button plain @click="onResetSearch">{{ $t("重置") }}</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="user/user/Info"
                                        :title="$t('添加客户')"
                                        width="560px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t("添加客户") }}</el-button>
                                    </DialogForm>
                                    <!-- <el-popconfirm title="您确认要批量删除所选数据吗？" @confirm="onBatchSubmit('del')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">批量删除</el-button>
                                        </template>
                                    </el-popconfirm> -->
                                    <DialogForm
                                        :params="{ ids: selectedIds }"
                                        isDrawer
                                        path="user/user/BatchBindTags"
                                        :title="$t('批量打标签')"
                                        width="800px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button :disabled="selectedIds.length === 0">{{ $t("批量打标签") }}</el-button>
                                    </DialogForm>
                                    <DialogForm
                                        :params="{}"
                                        isDrawer
                                        path="user/user/src/ImportUser"
                                        :title="$t('导入客户')"
                                        width="560px"
                                        :showOnOk="false"
                                        @okCallback="loadFilter"
                                        @callback="loadFilter"
                                    >
                                        <el-button>{{ $t("导入客户") }}</el-button>
                                    </DialogForm>
                                    <DialogForm
                                        :params="{ act: 'setRank', ids: selectedIds }"
                                        isDrawer
                                        path="user/user/BatchRank"
                                        :title="$t('批量修改客户等级')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button :disabled="selectedIds.length === 0">{{ $t("批量修改客户等级") }}</el-button>
                                    </DialogForm>
                                    <el-dropdown trigger="click" :disabled="selectedIds.length === 0">
                                        <el-button :disabled="selectedIds.length === 0">
                                            {{ $t("更多操作") }}
                                            <el-icon class="el-icon--right">
                                                <ArrowDown />
                                            </el-icon>
                                        </el-button>
                                        <template #dropdown>
                                            <el-dropdown-menu>
                                                <el-dropdown-item>
                                                    <BatchAddPoints :ids="selectedIds" @callBack="loadFilter">
                                                        <span>{{ $t("批量增加客户积分") }}</span>
                                                    </BatchAddPoints>
                                                </el-dropdown-item>
                                                <el-dropdown-item>
                                                    <BatchAddBalance :ids="selectedIds" @callBack="loadFilter">
                                                        <span>{{ $t("批量增加客户余额") }}</span>
                                                    </BatchAddBalance>
                                                </el-dropdown-item>
                                            </el-dropdown-menu>
                                        </template>
                                    </el-dropdown>
                                    <span v-if="selectedIds.length > 0">
                                        {{ $t("已选择：") }}<b>{{ selectedIds.length }}</b> {{ $t("项") }}
                                    </span>
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
                            row-key="userId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :width="220" :label="$t('客户名称')" prop="username">
                                <template #default="{ row }">
                                    <div class="avatar-info">
                                        <el-avatar :size="30" :src="imageFormat(row.avatar)" />
                                        <div class="info">
                                            <div class="flex align-center">
                                                <div class="mr10" v-if="row.mark">
                                                    <el-tooltip :disabled="!row.adminNote" :content="row.adminNote" effect="light" placement="top">
                                                        <SignTag class="check-box tag" :value="row.mark"></SignTag>
                                                    </el-tooltip>
                                                </div>
                                                <UserCard v-if="row.username != row.mobile" :user="row.username" :isCompanyAuth="row.isCompanyAuth"></UserCard>
                                            </div>
                                            <MobileCard :mobile="row.mobile"></MobileCard>
                                            <div v-if="row.fromTagName">
                                                <Tag
                                                    :color="row.fromTag === 2 || row.fromTag === 5 ? '#31c19e' : '#1890FF'"
                                                    :text="row.fromTagName"
                                                    :transparent="false"
                                                    style="margin-left: 0"
                                                >
                                                </Tag>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="140" :label="$t('客户等级')" prop="rankName">
                                <template #default="{ row }">
                                    <div class="rank-container">
                                        <el-image v-if="row.rankLogo" :src="imageFormat(row.rankLogo)" class="image" fit="contain" />
                                        <div class="text">{{ row.rankName }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="140" :label="$t('是否认证')" prop="isCompanyAuth" v-if="isB2b() || isStore()">
                                <template #default="{ row }">
                                    <div>
                                        <div v-if="row.isCompanyAuth" style="color: #67c23a">{{ $t("已认证") }}</div>
                                        <div v-else style="color: #f50">{{ $t("未认证") }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="140" :label="$t('是否启用')" prop="status" v-if="adminType == 'admin'">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.status" :params="{ id: row.userId, field: 'status' }" :requestApi="updateUserField" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('余额')" prop="balance" sortable="custom">
                                <template #default="{ row }">
                                    <ul>
                                        <li>{{ row.balance }}</li>
                                        <li v-if="row.frozenBalance > 0" style="color: #999999">
                                            {{ $t("(冻结：{frozenBalance})", { frozenBalance: row.frozenBalance }) }}
                                        </li>
                                    </ul>
                                </template>
                            </el-table-column>
                            <el-table-column :width="130" :label="$t('消费积分')" prop="points" sortable="custom"></el-table-column>
                            <el-table-column :width="140" :label="$t('累计消费次数')" prop="orderCount" sortable="custom"></el-table-column>
                            <el-table-column :width="140" :label="$t('累计消费金额')" prop="orderAmount" sortable="custom"></el-table-column>
                            <el-table-column :width="160" :label="$t('注册日期')" prop="regTime" sortable="custom"></el-table-column>
                            <el-table-column :width="250" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <div class="flex flex-align-center">
                                        <DialogForm
                                            :params="{ act: 'detail', id: row.userId }"
                                            isDrawer
                                            path="user/user/Detail"
                                            :title="$t('客户管理')"
                                            width="800px"
                                            @okCallback="loadFilter"
                                            @callback="loadFilter"
                                            :showClose="false"
                                            :showOnOk="false"
                                        >
                                            <a class="btn-link">{{ $t("查看") }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm
                                            :params="{ ids: [row.userId], userId: row.userId }"
                                            isDrawer
                                            path="user/user/BatchBindTags"
                                            :title="$t('打标签')"
                                            width="800px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t("标签") }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm
                                            :params="{ act: 'user', id: row.userId }"
                                            :showOnOk="false"
                                            isDrawer
                                            path="order/order/List"
                                            :title="row.username + $t(' - 查看订单')"
                                            width="1200px"
                                        >
                                            <a class="btn-link">{{ $t("查看订单") }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm
                                            :params="{ id: row.userId }"
                                            isDrawer
                                            path="user/user/AccountLog"
                                            :title="$t('客户') + row.username + $t('的账目明细')"
                                            width="800px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t("账目明细") }}</a>
                                        </DialogForm>
                                        <!-- <el-divider direction="vertical" /> -->
                                        <!-- <DeleteRecord :params="{ id: row.userId }" :requestApi="delUser" @afterDelete="loadFilter">删除 </DeleteRecord> -->
                                    </div>
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
                <!-- <div v-if="selectedIds.length > 0" class="selected-action-warp selected-warp-left">
                    <div class="selected-action">
                        <el-space>
                            <span
                                >已选择：<b>{{ selectedIds.length }}</b> 项</span
                            >
                            <el-popconfirm title="您确认要批量删除所选数据吗？" @confirm="onBatchSubmit('del')">
                                <template #reference>
                                    <el-button>批量删除</el-button>
                                </template>
                            </el-popconfirm>
                        </el-space>
                    </div>
                </div> -->
            </div>
        </div>
        <!-- 指定标签弹窗 -->
        <el-dialog v-model="tagDialogVisible" :title="$t('选择标签')" width="900px" class="tag-filter-dialog" @close="onTagDialogClose" destroy-on-close>
            <FilterTagSelector ref="filterTagSelectorRef" :initialTagIds="filterParams.tagIds" :initialMatchMode="tagMatchMode" />
            <template #footer>
                <el-button @click="tagDialogVisible = false">{{ $t("取消") }}</el-button>
                <el-button type="primary" @click="onTagDialogConfirm">{{ $t("确定") }}</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { UserFilterParams, UserFilterState } from "@/types/user/user.d";
import { batchSubmit, delUser, getUserList, updateUserField } from "@/api/user/user";
import { imageFormat } from "@/utils/format";
import { Tag } from "@/components/form";
import MobileCard from "@/components/list/src/MobileCard.vue";
import UserCard from "@/components/list/src/UserCard.vue";
import { isB2b, isStore } from "@/utils/version";
import { ArrowDown } from "@element-plus/icons-vue";
import BatchAddPoints from "./src/BatchAddPoints.vue";
import BatchAddBalance from "./src/BatchAddBalance.vue";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const config: any = useConfigStore();
import { useListRequest } from "@/hooks/useListRequest";
import { getAdminType } from "@/utils/storage";
import EditSign from "@/views/user/user/src/EditSign.vue";
import SignTag from "@/views/user/user/src/SignTag.vue";
import { ref, onMounted, computed } from "vue";
import { getMemberTagGroupList } from "@/api/user/userTags";
import type { UserTagFilterState } from "@/types/user/userTags.d";
import FilterTagSelector from "./FilterTagSelector.vue";
import type { FilterTagResult } from "./FilterTagSelector.vue";

/** 当前管理员类型 */
const adminType = getAdminType();
const TAG_MATCH_MODE_MAP = {
    any: 0,
    all: 1
} as const;

// “全部客户”(mark=0) 时不向后端传递 mark 参数（仅本页面生效）
const getUserListWithMarkFilter = (params: UserFilterParams) => {
    const cleanParams: any = { ...params, userType: 0 };
    if (cleanParams.mark === 0 || cleanParams.mark === "0") {
        delete cleanParams.mark;
    }
    if (!cleanParams.tagIds || cleanParams.tagIds.length === 0) {
        delete cleanParams.tagIds;
        delete cleanParams.tagMatchMode;
    } else {
        cleanParams.tagMatchMode = TAG_MATCH_MODE_MAP[tagMatchMode.value];
    }
    return getUserList(cleanParams);
};
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
    resetParams
} = useListRequest<UserFilterState, UserFilterParams>({
    apiFunction: getUserListWithMarkFilter,
    idKey: "userId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        fromTag: "",
        mark: 0,
        balance: "",
        pointsLt: "",
        pointsGt: "",
        rankId: 0,
        tagIds: [],
        page: 1,
        size: config.get("pageSize")
    }
});

const tagGroupOptions = ref<UserTagFilterState[]>([]);
const tagFilterType = ref("all");
const tagDialogVisible = ref(false);
const tagMatchMode = ref<"any" | "all">("any");
const filterTagSelectorRef = ref<InstanceType<typeof FilterTagSelector> | null>(null);

const selectedTagNames = computed(() => {
    if (!filterParams.tagIds || filterParams.tagIds.length === 0) return [];
    const tagIds = new Set(filterParams.tagIds);
    return tagGroupOptions.value
        .flatMap((group) => group.tags || [])
        .filter((tag) => tag.id && tagIds.has(tag.id))
        .map((tag) => tag.tagName)
        .filter((name): name is string => !!name);
});

const tagFilterDisplayText = computed(() => {
    if (!filterParams.tagIds || filterParams.tagIds.length === 0) {
        return t("指定标签");
    }
    if (selectedTagNames.value.length === 0) {
        return t("指定标签（已选{length}项）", { length: filterParams.tagIds.length });
    }
    if (selectedTagNames.value.length <= 2) {
        return t("指定标签：{join}", { join: selectedTagNames.value.join("、") });
    }
    return t("指定标签：{join}等{length}项", { join: selectedTagNames.value.slice(0, 2).join("、"), length: selectedTagNames.value.length });
});

const openTagFilterDialog = () => {
    tagFilterType.value = "specify";
    tagDialogVisible.value = true;
};

const onTagFilterChange = (val: string) => {
    if (val === "specify") {
        tagDialogVisible.value = true;
        return;
    }
    if (val !== "specify") {
        filterParams.tagIds = [];
        tagMatchMode.value = "any";
        onSearchSubmit();
    }
};

const onTagDialogConfirm = () => {
    const result = filterTagSelectorRef.value?.getResult();
    if (result) {
        filterParams.tagIds = result.tagIds;
        tagMatchMode.value = result.matchMode;
    }
    tagDialogVisible.value = false;
    if (filterParams.tagIds && filterParams.tagIds.length > 0) {
        tagFilterType.value = "specify";
    } else {
        tagFilterType.value = "all";
    }
    onSearchSubmit();
};

const onTagDialogClose = () => {
    if (!filterParams.tagIds || filterParams.tagIds.length === 0) {
        tagFilterType.value = "all";
    }
};

const onResetSearch = () => {
    tagFilterType.value = "all";
    tagMatchMode.value = "any";
    resetParams();
};

const loadTagGroups = async () => {
    try {
        const res = await getMemberTagGroupList({ page: 1, size: 100, sortField: "", sortOrder: "" });
        tagGroupOptions.value = res.records || [];
    } catch (_) {
        /* ignore */
    }
};
onMounted(() => loadTagGroups());

const signList = [
    {
        value: 0
    },
    {
        value: 1,
        color: "#ff5050"
    },
    {
        value: 2,
        color: "#ffba1f"
    },
    {
        value: 3,
        color: "#2ecf92"
    },
    {
        value: 4,
        color: "#16afff"
    },
    {
        value: 5,
        color: "#792dff"
    }
];
// 批量操作
/**
 * @description 批量操作处理，支持批量删除等操作
 * @param action 操作类型：'del'-批量删除
 */
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载客户列表
loadFilter();
</script>
<style lang="less" scoped>
.avatar-info {
    display: flex;
    flex-direction: row;
    width: 100%;
    padding: 0 8px;
    align-items: center;
    gap: 14px;

    .info {
        width: 110px;
        display: flex;
        flex-direction: column;
    }
}

.rank-container {
    display: flex;
    align-items: center;
    /* 垂直居中 */

    .image {
        width: 18px;
        height: 18px;
    }

    .text {
        margin-left: 3px;
    }
}

:deep(.tag-filter-dialog .el-dialog__header) {
    border-bottom: none;
}

:deep(.tag-filter-dialog .el-dialog__footer) {
    border-top: none;
}
</style>
