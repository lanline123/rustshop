/** * @file 导入批发商（同步） * @description el-form：①批发商等级 ②选择会员（三栏：Tab / 搜索+多选 / 已选）；提交 importWholesaler */
<template>
    <div class="import-issue-drawer" v-loading="submitting">
        <div class="content_wrapper">
            <el-form ref="formRef" :model="formState" :rules="rules" label-width="auto" class="import-form">
                <el-form-item prop="gradeId" :label="$t('批发商等级')">
                    <el-select v-model="formState.gradeId" :placeholder="$t('请选择批发商等级')" style="width: 500px">
                        <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName"
                            :value="item.gradeId" />
                    </el-select>
                </el-form-item>
                <el-form-item :label="$t('选择会员')" class="issue-form-item">
                    <div class="issue-layout">
            <div class="issue-nav">
                <div v-for="item in issueTabs" :key="item.value" class="issue-nav__item"
                    :class="{ 'is-active': activeTab === item.value }" @click="switchTab(item.value)">
                    {{ item.label }}
                </div>
            </div>

            <div class="issue-main">
                <template v-if="activeTab === 'USER'">
                    <div class="issue-search-card">
                        <div class="issue-search-row">
                            <TigInput v-model="userFilter.keyword" name="keyword" :placeholder="$t('会员名称/手机号')" clearable
                                @keyup.enter="onUserSearch" @clear="onUserSearch" width="250px">
                                <template #append>
                                    <el-button @click="onUserSearch"><span
                                            class="iconfont icon-chakan1"></span></el-button>
                                </template>
                            </TigInput>
                            <el-select style="width: 250px" v-model="userFilter.fromTag" clearable :placeholder="$t('请选择')"
                                @change="onUserSearch">
                                <el-option :value="1" :label="$t('微信公众号')" />
                                <el-option :value="2" :label="$t('微信小程序')" />
                                <el-option :value="3" label="H5" />
                                <el-option :value="4" label="PC" />
                                <el-option :value="5" label="APP" />
                            </el-select>
                        </div>
                        <div class="issue-search-actions">
                            <el-button type="primary" @click="onUserSearch">{{ $t('搜索') }}</el-button>
                            <el-button @click="resetUserFilter">{{ $t('重置') }}</el-button>
                        </div>
                    </div>

                    <div class="issue-table-card">
                        <el-table ref="userTableRef" :data="userList" :loading="userLoading" row-key="userId"
                            height="100%" @selection-change="onUserSelectionChange">
                            <el-table-column type="selection" width="44" reserve-selection :selectable="isUserSelectable" />
                            <el-table-column :label="$t('会员名称')" min-width="220">
                                <template #default="{ row }">
                                    <div class="user-info">
                                        <el-avatar :size="30" :src="imageFormat(row.avatar)" />
                                        <div class="user-info__main">
                                            <div class="user-name">{{ row.nickname || row.username || "--" }}</div>
                                            <MobileCard :mobile="row.mobile" />
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column v-if="!isMerchant() && !isB2CPro()" :label="$t('是否认证')" min-width="120">
                                <template #default="{ row }">
                                    <span :class="row.isCompanyAuth === 1 ? 'status-green' : 'status-orange'">
                                        {{ row.isCompanyAuth === 1 ? $t('已认证') : $t('未认证') }}
                                    </span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否启用')" min-width="120">
                                <template #default="{ row }">
                                    {{ row.statusName || (row.status === 1 ? $t('启用') : $t('禁用')) }}
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!userLoading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </div>

                    <div class="pagination-con" v-if="userTotal > 0">
                        <Pagination v-model:page="userFilter.page" v-model:size="userFilter.size" :total="userTotal"
                            @callback="loadUserList" />
                    </div>
                </template>

                <template v-else>
                    <div class="issue-search-card">
                        <div class="issue-search-row">
                            <TigInput v-model="rankFilter.keyword" name="rankKeyword" :placeholder="$t('等级名称')" clearable
                                @keyup.enter="applyRankFilter" @clear="applyRankFilter">
                                <template #append>
                                    <el-button @click="applyRankFilter"><span
                                            class="iconfont icon-chakan1"></span></el-button>
                                </template>
                            </TigInput>
                            <el-select style="width: 250px" v-model="rankFilter.rankId" clearable :placeholder="$t('请选择')"
                                @change="applyRankFilter">
                                <el-option v-for="item in rankOptions" :key="item.rankId"
                                    :label="item.rankLevel != null ? `vip${item.rankLevel}` : '--'" :value="item.rankId" />
                            </el-select>
                        </div>
                        <div class="issue-search-actions">
                            <el-button type="primary" @click="applyRankFilter">{{ $t('搜索') }}</el-button>
                            <el-button @click="resetRankFilter">{{ $t('重置') }}</el-button>
                        </div>
                    </div>

                    <div class="issue-table-card">
                        <el-table ref="rankTableRef" :data="rankDisplayList" row-key="rankId" height="100%"
                            @selection-change="onRankSelectionChange">
                            <el-table-column type="selection" width="44" reserve-selection />
                            <el-table-column :label="$t('等级')" min-width="100">
                                <template #default="{ row }">
                                    <div class="level_txt">vip{{ row.rankLevel }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('等级名称')" min-width="220">
                                <template #default="{ row }">
                                    {{ row.rankName || "--" }}
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </div>
                </template>
            </div>

            <div class="issue-selected">
                <div class="issue-selected__title">{{ $t('已选：{selectedCount}{activeTabUSER}', { selectedCount, activeTabUSER: activeTab === "USER" ? "位会员" : "个等级" }) }}
                </div>
                <div v-if="selectedItems.length > 0" class="issue-selected__list">
                    <div v-for="item in selectedItems" :key="getSelectedKey(item)" class="issue-selected__item">
                        <span class="issue-selected__text">{{ getSelectedLabel(item) }}</span>
                        <div class="issue-selected__remove" @click="removeSelected(item)"></div>
                    </div>
                </div>
                <div v-else class="issue-selected__empty">{{ $t('暂未选择{activeTabUSER}', { activeTabUSER: activeTab === "USER" ? "会员" : "等级" }) }}</div>
            </div>
                    </div>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { computed, nextTick, onMounted, reactive, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import type { FormRules } from "element-plus";
import { Pagination } from "@/components/list";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { useConfigStore } from "@/store/config";
import { getCouponConfig } from "@/api/promotion/coupon";
import { getGradeList } from "@/api/wholesale/grade";
import { getWholesaleSetting } from "@/api/wholesale/setting";
import { getSelectableUserPage, importWholesaler } from "@/api/wholesale/wholesaler";
import type { CouponRankList } from "@/types/promotion/coupon.d";
import type { WholesalerGradeVO } from "@/types/wholesale/grade";
import type { WholesalerImportDTO, WholesalerImportResultVO, WholesalerSelectableUserQuery, WholesalerSelectableUserVO } from "@/types/wholesale/wholesaler";
import { imageFormat } from "@/utils/format";
import { isMerchant, isB2CPro } from "@/utils/version";
import { summarizeImportResult } from "@/views/wholesale/contractLogic";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
type IssueTab = "USER" | "RANK";

const emit = defineEmits(["submitCallback", "update:confirmLoading", "okType"]);

const config: any = useConfigStore();
const formRef = shallowRef();
const activeTab = ref<IssueTab>("USER");
const userLoading = ref(false);
const submitting = ref(false);
const userTotal = ref(0);
const userList = ref<WholesalerSelectableUserVO[]>([]);
const rankOptions = ref<CouponRankList[]>([]);
const rankDisplayList = ref<CouponRankList[]>([]);
const gradeOptions = ref<WholesalerGradeVO[]>([]);
const formState = reactive<{ gradeId: number | undefined }>({
    gradeId: undefined
});
const rules = reactive<FormRules>({
    gradeId: [{ required: true, message: t('请选择批发商等级'), trigger: "change" }]
});
const userTableRef = ref();
const rankTableRef = ref();

const selectedUsers = ref<Record<number, WholesalerSelectableUserVO>>({});
const selectedRanks = ref<Record<number, CouponRankList>>({});

const issueTabs: { label: string; value: IssueTab }[] = [
    { label: t('指定会员'), value: "USER" },
    { label: t('会员等级'), value: "RANK" }
];

const userFilter = reactive<WholesalerSelectableUserQuery>({
    page: 1,
    size: config.get("pageSize"),
    keyword: "",
    fromTag: ""
});

const rankFilter = reactive({
    keyword: "",
    rankId: undefined as number | undefined
});

const selectedItems = computed(() => {
    return activeTab.value === "USER" ? Object.values(selectedUsers.value) : Object.values(selectedRanks.value);
});

const selectedCount = computed(() => selectedItems.value.length);

const canSubmit = computed(() => {
    return activeTab.value === "USER" ? Object.keys(selectedUsers.value).length > 0 : Object.keys(selectedRanks.value).length > 0;
});

const emitOkType = () => {
    emit("okType", canSubmit.value);
};

const getSelectedKey = (item: WholesalerSelectableUserVO | CouponRankList) => {
    return activeTab.value === "USER" ? String((item as WholesalerSelectableUserVO).userId) : String((item as CouponRankList).rankId);
};

const getSelectedLabel = (item: WholesalerSelectableUserVO | CouponRankList) => {
    if (activeTab.value === "USER") {
        const user = item as WholesalerSelectableUserVO;
        return user.nickname || user.username || user.mobile || t('会员{userId}', { userId: user.userId });
    }
    const rank = item as CouponRankList;
    const name = rank.rankName || t('等级{rankId}', { rankId: rank.rankId });
    return `${name}(${rank.userCount ?? 0})`;
};

const isUserSelectable = (row: WholesalerSelectableUserVO) => !row.isWholesaler;

const syncUserSelection = async () => {
    await nextTick();
    if (!userTableRef.value) return;
    userList.value.forEach((row) => {
        userTableRef.value.toggleRowSelection(row, !!selectedUsers.value[row.userId]);
    });
};

const syncRankSelection = async () => {
    await nextTick();
    if (!rankTableRef.value) return;
    rankDisplayList.value.forEach((row) => {
        rankTableRef.value.toggleRowSelection(row, !!selectedRanks.value[row.rankId as number]);
    });
};

const loadUserList = async () => {
    userLoading.value = true;
    try {
        const result = await getSelectableUserPage({
            ...userFilter,
            keyword: userFilter.keyword?.trim() || ""
        });
        userList.value = result.records || [];
        userTotal.value = result.total || 0;
        await syncUserSelection();
    } catch (error: any) {
        message.error(error.message || t('会员加载失败'));
    } finally {
        userLoading.value = false;
    }
};

const loadRankOptions = async () => {
    try {
        const result = await getCouponConfig();
        rankOptions.value = Array.isArray(result) ? result : [];
        applyRankFilter();
    } catch (error: any) {
        message.error(error.message || t('会员等级加载失败'));
    }
};

const resolveDefaultGradeId = (defaultGradeId?: number) => {
    if (defaultGradeId && gradeOptions.value.some((item) => item.gradeId === defaultGradeId)) {
        return defaultGradeId;
    }
    const defaultGrade = gradeOptions.value.find((item) => item.isDefault);
    if (defaultGrade?.gradeId) {
        return defaultGrade.gradeId;
    }
    return gradeOptions.value[0]?.gradeId;
};

const loadGradeOptions = async () => {
    try {
        const [gradeList, setting] = await Promise.all([
            getGradeList({ status: 1, withUserCount: false }),
            getWholesaleSetting()
        ]);
        gradeOptions.value = gradeList || [];
        formState.gradeId = resolveDefaultGradeId(setting?.wholesaleDefaultGradeId);
    } catch (error: any) {
        message.error(error.message || t('批发商等级加载失败'));
    }
};

const applyRankFilter = async () => {
    const keyword = rankFilter.keyword.trim();
    rankDisplayList.value = rankOptions.value.filter((item) => {
        const keywordMatch = !keyword || item.rankName?.includes(keyword);
        const rankMatch = !rankFilter.rankId || item.rankId === rankFilter.rankId;
        return keywordMatch && rankMatch;
    });
    await syncRankSelection();
};

const onUserSearch = () => {
    userFilter.page = 1;
    loadUserList();
};

const resetUserFilter = () => {
    userFilter.keyword = "";
    userFilter.fromTag = "";
    userFilter.page = 1;
    loadUserList();
};

const resetRankFilter = () => {
    rankFilter.keyword = "";
    rankFilter.rankId = undefined;
    applyRankFilter();
};

const switchTab = (tab: IssueTab) => {
    activeTab.value = tab;
    emitOkType();
    if (tab === "RANK") {
        syncRankSelection();
        return;
    }
    syncUserSelection();
};

const onUserSelectionChange = (selection: WholesalerSelectableUserVO[]) => {
    const currentIds = userList.value.map((item) => item.userId);
    currentIds.forEach((id) => {
        if (selectedUsers.value[id]) {
            delete selectedUsers.value[id];
        }
    });
    selection.filter((item) => !item.isWholesaler).forEach((item) => {
        selectedUsers.value[item.userId] = item;
    });
    selectedUsers.value = { ...selectedUsers.value };
    emitOkType();
};

const onRankSelectionChange = (selection: CouponRankList[]) => {
    const currentIds = rankDisplayList.value.map((item) => item.rankId as number);
    currentIds.forEach((id) => {
        if (selectedRanks.value[id]) {
            delete selectedRanks.value[id];
        }
    });
    selection.forEach((item) => {
        if (item.rankId) {
            selectedRanks.value[item.rankId] = item;
        }
    });
    selectedRanks.value = { ...selectedRanks.value };
    emitOkType();
};

const removeSelected = async (item: WholesalerSelectableUserVO | CouponRankList) => {
    if (activeTab.value === "USER") {
        const user = item as WholesalerSelectableUserVO;
        delete selectedUsers.value[user.userId];
        selectedUsers.value = { ...selectedUsers.value };
        await syncUserSelection();
    } else {
        const rank = item as CouponRankList;
        if (rank.rankId) {
            delete selectedRanks.value[rank.rankId];
        }
        selectedRanks.value = { ...selectedRanks.value };
        await syncRankSelection();
    }
    emitOkType();
};

const buildPayload = (): WholesalerImportDTO | null => {
    const gradeId = formState.gradeId as number;
    if (activeTab.value === "USER") {
        const userIds = Object.keys(selectedUsers.value).map((id) => Number(id));
        if (userIds.length === 0) {
            message.warning(t('请选择会员'));
            return null;
        }
        return { userIds, gradeId };
    }
    const rankIds = Object.keys(selectedRanks.value).map((id) => Number(id));
    if (rankIds.length === 0) {
        message.warning(t('请选择会员等级'));
        return null;
    }
    return { rankIds, gradeId };
};

const showImportResult = (result: WholesalerImportResultVO) => {
    const summary = summarizeImportResult(result);
    if (summary.severity === "warning") {
        message.warning(t('导入完成：成功 {successCount} 个，跳过 {skippedCount} 个，失败 {failedCount} 个。{failedDetails}', {
            successCount: result.successCount,
            skippedCount: result.skippedCount,
            failedCount: result.failedCount,
            failedDetails: summary.failedDetails
        }));
        return;
    }
    message.success(t('操作成功'));
};

const doImport = async (payload: WholesalerImportDTO) => {
    try {
        emit("update:confirmLoading", true);
        submitting.value = true;
        const result = await importWholesaler(payload);
        showImportResult(result);
        emit("submitCallback", result);
    } catch (error: any) {
        if (error?.code === 5340 && error?.data) {
            const result = error.data as WholesalerImportResultVO;
            showImportResult(result);
            emit("submitCallback", result);
            return;
        }
        message.error(error.message || t('导入失败'));
        throw error;
    } finally {
        emit("update:confirmLoading", false);
        submitting.value = false;
    }
};

const onFormSubmit = async () => {
    await formRef.value.validate();
    const payload = buildPayload();
    if (!payload) return;
    await doImport(payload);
};

onMounted(async () => {
    emitOkType();
    await Promise.all([loadUserList(), loadRankOptions(), loadGradeOptions()]);
});

defineExpose({
    onFormSubmit
});
</script>

<style scoped lang="less">
.import-issue-drawer {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 680px;
    background: #fff;
}

.import-form {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
}

.issue-form-item {
    flex: 1;
    min-height: 0;
    margin-bottom: 0;

    :deep(.el-form-item__content) {
        flex: 1;
        min-height: 0;
        align-items: stretch;
    }
}

.issue-layout {
    display: grid;
    grid-template-columns: 140px minmax(0, 1fr) 260px;
    width: 100%;
    height: calc(100vh - 260px);
    min-height: 520px;
    border-top: 1px solid #f0f2f5;
}

.issue-nav {
    border-right: 1px solid #f0f2f5;
    padding: 14px 10px;
}

.issue-nav__item {
    margin-bottom: 10px;
    padding: 12px 14px;
    border-radius: 4px;
    color: #303133;
    cursor: pointer;
    transition: all 0.2s ease;
}

.issue-nav__item.is-active {
    background: #f1f6ff;
    color: #155bd4;
    font-weight: 600;
}

.issue-main {
    display: flex;
    flex-direction: column;
    min-width: 0;
    padding: 12px;
    gap: 12px;
}

.issue-search-card,
.issue-table-card,
.issue-selected {
    border: 1px solid #f0f2f5;
    background: #fff;
}

.issue-search-card {
    padding: 12px;
    background: #f5f7fa;
}

.issue-search-row {
    display: flex;
    gap: 12px;
    align-items: center;
}

.issue-search-actions {
    margin-top: 12px;
    display: flex;
}

.issue-table-card {
    flex: 1;
    min-height: 0;
    overflow: hidden;
}

.issue-selected {
    border-left: 1px solid #f0f2f5;
    padding: 18px 16px;
}

.issue-selected__title {
    margin-bottom: 16px;
    font-size: 16px;
    color: #303133;
}

.issue-selected__list {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.issue-selected__item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 10px 12px;
    border-radius: 4px;
    background: #fafafa;
}

.issue-selected__text {
    min-width: 0;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #303133;
}

.issue-selected__remove {
    width: 12px;
    height: 12px;
    background: #f00;
    border-radius: 50%;
    position: relative;
    cursor: pointer;

    &::after {
        content: "";
        display: block;
        width: 50%;
        height: 2px;
        background: #fff;
        position: absolute;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
    }
}

.issue-selected__empty {
    color: #909399;
    padding-top: 8px;
}

.pagination-con {
    justify-content: flex-end;
    margin: 0;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
}

.user-avatar {
    flex-shrink: 0;
    background: #f3f6fb;
    color: #5b6b8a;
}

.user-info__main {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.user-name {
    color: #303133;
    line-height: 20px;
}

.user-subline-group {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.user-subline {
    color: #909399;
    line-height: 20px;
}

.status-orange {
    color: #f59a23;
}

.status-green {
    color: #67c23a;
}

.rank-name {
    color: #f06a20;
}

.level_txt {
    font-weight: 600;
    color: #c95a0a;
    font-size: 14px;
}

:deep(.el-table th.el-table__cell) {
    background: #f7f8fa;
}


@media only screen and (max-width: 1400px) {
    .issue-layout {
        grid-template-columns: 120px minmax(0, 1fr) 220px;
    }
}
</style>
