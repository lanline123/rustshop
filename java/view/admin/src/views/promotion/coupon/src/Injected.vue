<template>
    <div class="coupon-issue-drawer" v-loading="submitting">
        <div class="issue-layout">
            <div class="issue-nav">
                <div v-for="item in issueTabs" :key="item.value" class="issue-nav__item"
                    :class="{ 'is-active': activeTab === item.value }" @click="switchTab(item.value)">
                    {{ item.label }}
                </div>
            </div>

            <div class="issue-main">
                <UserTablePicker
                    v-show="activeTab === 'USER'"
                    ref="userPickerRef"
                    :model-value="selectedUsers"
                    :request-api="getCouponIssueUsers"
                    @update:model-value="onUsersChange"
                />
                <div v-if="MEMBER_TAG_AUDIENCE_READY" v-show="activeTab === 'TAG'" class="issue-tag-pane">
                    <FilterTagSelector
                        ref="tagSelectorRef"
                        :initial-tag-ids="tagIds"
                        :initial-match-mode="tagMatchMode === 1 ? 'all' : 'any'"
                        :show-selected-panel="false"
                        show-group-filter
                        @change="onTagChange"
                    />
                </div>
                <RankTablePicker
                    v-show="activeTab === 'RANK'"
                    ref="rankPickerRef"
                    :model-value="selectedRanks"
                    :request-api="getCouponConfig"
                    @update:model-value="onRanksChange"
                />
            </div>

            <div class="issue-selected">
                <div class="issue-selected__title">{{ $t('已选：') }}{{ selectedTitle }}</div>
                <template v-if="activeTab === 'TAG'">
                    <div v-if="tagSummary.length > 0" class="issue-selected__list">
                        <div v-for="entry in tagSummary" :key="entry.groupId" class="issue-selected__item">
                            <el-tooltip
                                :content="`${entry.groupName}：${entry.tagNames}`"
                                placement="top-start"
                                popper-class="coupon-issue-selected-tooltip"
                            >
                                <span class="issue-selected__text">{{ entry.groupName }}：{{ entry.tagNames }}</span>
                            </el-tooltip>
                            <div class="issue-selected__remove" @click="removeTagGroup(entry)"></div>
                        </div>
                    </div>
                    <div v-else class="issue-selected__empty">{{ $t('暂未选择标签') }}</div>
                </template>
                <template v-else>
                    <div v-if="selectedItems.length > 0" class="issue-selected__list">
                        <div v-for="item in selectedItems" :key="getSelectedKey(item)" class="issue-selected__item">
                            <span class="issue-selected__text">{{ getSelectedLabel(item) }}</span>
                            <div class="issue-selected__remove" @click="removeSelected(item)"></div>
                        </div>
                    </div>
                    <div v-else class="issue-selected__empty">
                        {{ $t('暂未选择{activeTabUSER}', { activeTabUSER: activeTab === "USER" ? $t('会员') : $t('等级') }) }}
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { computed, createVNode, ref } from "vue";
import { Modal, message } from "ant-design-vue";
import { getCouponConfig, getCouponIssueUsers, previewCouponIssue, sendCouponIssue } from "@/api/promotion/coupon";
import type { CouponManualIssueParam, CouponManualIssuePreviewVO } from "@/types/promotion/coupon.d";
import { RankTablePicker, UserTablePicker } from "@/components/MemberPicker";
import FilterTagSelector from "@/views/user/user/FilterTagSelector.vue";
import type { FilterTagChangeResult, FilterTagGroupSummary } from "@/views/user/user/FilterTagSelector.vue";
import type { MemberPickerRank, MemberPickerRankMap, MemberPickerUser, MemberPickerUserMap } from "@/types/user/memberPicker";
import { MEMBER_TAG_AUDIENCE_READY } from "@/utils/memberAudienceGate";

import { useI18n } from "vue-i18n";
const { t } = useI18n();

type IssueTab = "USER" | "TAG" | "RANK";

const props = defineProps({
    id: {
        type: Number,
        default: 0
    }
});

const emit = defineEmits(["submitCallback", "update:confirmLoading", "okType"]);

const activeTab = ref<IssueTab>("USER");
const submitting = ref(false);
const userPickerRef = ref<InstanceType<typeof UserTablePicker> | null>(null);
const rankPickerRef = ref<InstanceType<typeof RankTablePicker> | null>(null);
const tagSelectorRef = ref<InstanceType<typeof FilterTagSelector> | null>(null);

const selectedUsers = ref<MemberPickerUserMap>({});
const selectedRanks = ref<MemberPickerRankMap>({});
const tagIds = ref<number[]>([]);
const tagMatchMode = ref<number>(0);
const tagSummary = ref<FilterTagGroupSummary[]>([]);

const issueTabs: { label: string; value: IssueTab }[] = [
    { label: t("指定会员"), value: "USER" },
    ...(MEMBER_TAG_AUDIENCE_READY ? [{ label: t("会员标签"), value: "TAG" as IssueTab }] : []),
    { label: t("会员等级"), value: "RANK" }
];

const selectedItems = computed(() => {
    return activeTab.value === "USER" ? Object.values(selectedUsers.value) : Object.values(selectedRanks.value);
});

const tagMatchModeLabel = computed(() => (tagMatchMode.value === 1 ? t("满足全部") : t("满足任一")));

const selectedTitle = computed(() => {
    if (activeTab.value === "USER") return t("{count}位会员", { count: Object.keys(selectedUsers.value).length });
    if (activeTab.value === "TAG") return t("{count}个标签（{matchMode}）", { count: tagIds.value.length, matchMode: tagMatchModeLabel.value });
    return t("{count}个等级", { count: Object.keys(selectedRanks.value).length });
});

const canSubmit = computed(() => {
    if (activeTab.value === "USER") return Object.keys(selectedUsers.value).length > 0;
    if (activeTab.value === "TAG") return tagIds.value.length > 0;
    return Object.keys(selectedRanks.value).length > 0;
});

const emitOkType = () => {
    emit("okType", canSubmit.value);
};

const getSelectedKey = (item: MemberPickerUser | MemberPickerRank) => {
    return activeTab.value === "USER" ? String((item as MemberPickerUser).userId) : String((item as MemberPickerRank).rankId);
};

const getSelectedLabel = (item: MemberPickerUser | MemberPickerRank) => {
    if (activeTab.value === "USER") {
        const user = item as MemberPickerUser;
        return user.memberName || user.username || user.mobile || t("会员{userId}", { userId: user.userId });
    }
    const rank = item as MemberPickerRank;
    return rank.rankName || t("等级{rankId}", { rankId: rank.rankId });
};

/** 清空某一种发放方式下的已选内容 */
const clearTabSelection = async (tab: IssueTab) => {
    if (tab === "USER") {
        selectedUsers.value = {};
        await userPickerRef.value?.syncSelection();
    } else if (tab === "TAG") {
        tagIds.value = [];
        tagSummary.value = [];
        tagSelectorRef.value?.reset([], tagMatchMode.value === 1 ? "all" : "any");
    } else {
        selectedRanks.value = {};
        await rankPickerRef.value?.syncSelection();
    }
};

/** 切换发放方式：指定会员、会员标签、会员等级三者互斥，切换即清空其他方式的已选内容 */
const switchTab = async (tab: IssueTab) => {
    if (tab === activeTab.value) return;
    activeTab.value = tab;
    emitOkType();
    for (const item of issueTabs.filter((issueTab) => issueTab.value !== tab)) {
        await clearTabSelection(item.value);
    }
};

const onUsersChange = (value: MemberPickerUserMap) => {
    selectedUsers.value = value;
    emitOkType();
};

const onRanksChange = (value: MemberPickerRankMap) => {
    selectedRanks.value = value;
    emitOkType();
};

const onTagChange = (result: FilterTagChangeResult) => {
    tagIds.value = [...result.tagIds];
    tagMatchMode.value = result.matchMode === "all" ? 1 : 0;
    tagSummary.value = result.summary;
    emitOkType();
};

const removeTagGroup = (entry: FilterTagGroupSummary) => {
    const nextTagIds = tagIds.value.filter((id) => !entry.tagIds.includes(id));
    tagIds.value = nextTagIds;
    tagSummary.value = tagSummary.value.filter((item) => item.groupId !== entry.groupId);
    // 标签选择器自身维护勾选态，移除后需反向同步
    tagSelectorRef.value?.reset(nextTagIds, tagMatchMode.value === 1 ? "all" : "any");
    emitOkType();
};

const removeSelected = async (item: MemberPickerUser | MemberPickerRank) => {
    if (activeTab.value === "USER") {
        const next = { ...selectedUsers.value };
        delete next[(item as MemberPickerUser).userId];
        selectedUsers.value = next;
        await userPickerRef.value?.syncSelection();
    } else {
        const rankId = (item as MemberPickerRank).rankId;
        const next = { ...selectedRanks.value };
        if (rankId) {
            delete next[rankId];
        }
        selectedRanks.value = next;
        await rankPickerRef.value?.syncSelection();
    }
    emitOkType();
};

const buildIssuePayload = (): CouponManualIssueParam | null => {
    if (!props.id) {
        message.error(t('优惠券ID不能为空'));
        return null;
    }
    if (activeTab.value === "USER") {
        const userIds = Object.keys(selectedUsers.value).map((id) => Number(id));
        if (userIds.length === 0) {
            message.warning(t('请选择会员'));
            return null;
        }
        return {
            couponId: props.id,
            issueType: "USER",
            userIds
        };
    }
    if (activeTab.value === "TAG") {
        if (tagIds.value.length === 0) {
            message.warning(t("请选择会员标签"));
            return null;
        }
        return {
            couponId: props.id,
            issueType: "TAG",
            tagIds: [...tagIds.value],
            tagMatchMode: tagMatchMode.value
        };
    }
    const rankIds = Object.keys(selectedRanks.value).map((id) => Number(id));
    if (rankIds.length === 0) {
        message.warning(t('请选择会员等级'));
        return null;
    }
    return {
        couponId: props.id,
        issueType: "RANK",
        rankIds
    };
};

const createFailDetailContent = (preview: CouponManualIssuePreviewVO) => {
    const highlightStyle = { color: '#faad14', fontWeight: '600' };
    const lineStyle = { lineHeight: '32px', color: '#303133' };
    const summary = [
        createVNode("div", { style: lineStyle }, [
            t('本次优惠券发放客户数总计：'),
            createVNode("span", { style: highlightStyle }, `${preview.memberCount}`),
            t('位')
        ]),
        createVNode("div", { style: lineStyle }, [
            t('优惠券发放总量总计：'),
            createVNode("span", { style: highlightStyle }, `${preview.couponCount}`),
            t('张')
        ]),
        preview.failCount > 0
            ? createVNode("div", { style: lineStyle }, [t('发放失败：'), createVNode("span", { style: highlightStyle }, `${preview.failCount}`), t('张')])
            : null,
        createVNode("div", { style: { ...lineStyle, marginTop: '6px' } }, t('请确认是否发放？'))
    ].filter(Boolean);

    return createVNode("div", { style: { minWidth: '420px' } }, [createVNode("div", null, summary)]);
};

const doSend = async (payload: CouponManualIssueParam) => {
    try {
        emit("update:confirmLoading", true);
        submitting.value = true;
        const result = await sendCouponIssue(payload);
        if (result.failCount > 0) {
            message.warning(t('优惠券发放完成，成功 {successCount} 张，失败 {failCount} 张', { successCount: result.successCount, failCount: result.failCount }));
        } else {
            message.success(t('优惠券发放成功，共为 {successCount} 位会员发放 {couponCount} 张优惠券', { successCount: result.successCount, couponCount: result.couponCount }));
        }
        emit("submitCallback", result);
    } catch (error: any) {
        message.error(error.message || t('优惠券发放失败'));
        throw error;
    } finally {
        emit("update:confirmLoading", false);
        submitting.value = false;
    }
};

const onFormSubmit = async () => {
    const payload = buildIssuePayload();
    if (!payload) return;

    try {
        emit("update:confirmLoading", true);
        const preview = await previewCouponIssue(payload);
        emit("update:confirmLoading", false);

        Modal.confirm({
            title: t('发放确认'),
            okText: t('确定发放'),
            cancelText: t('取消'),
            centered: true,
            content: createFailDetailContent(preview),
            async onOk() {
                await doSend(payload);
            }
        });
    } catch (error: any) {
        emit("update:confirmLoading", false);
        message.error(error.message || t('发放预览失败'));
    }
};

emitOkType();

defineExpose({
    onFormSubmit
});
</script>

<style scoped lang="less">
.coupon-issue-drawer {
    height: 100%;
    min-height: 680px;
    background: #fff;
}

.issue-layout {
    display: grid;
    grid-template-columns: 140px minmax(0, 1fr) 260px;
    height: calc(100vh - 174px);
    min-height: 620px;
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

// 复用会员标签选择器，去掉其自带外框，融入抽屉分栏容器
.issue-tag-pane {
    flex: 1;
    min-width: 0;
    min-height: 0;
    display: flex;

    :deep(.filter-tag-selector) {
        flex: 1;
        min-width: 0;
        min-height: 0;
    }

    :deep(.main-layout) {
        flex: 1;
        gap: 0;
        min-height: 0;
    }

    :deep(.groups-panel),
    :deep(.tags-panel) {
        display: flex;
        flex-direction: column;
        min-height: 0;
    }

    :deep(.group-list) {
        flex: 1;
        max-height: none;
    }

    :deep(.tags-list) {
        flex: 1;
        min-height: 0;
        overflow-y: auto;
    }
}

.issue-selected {
    border: 1px solid #f0f2f5;
    border-left: 1px solid #f0f2f5;
    background: #fff;
    padding: 18px 16px;
    overflow-y: auto;
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
    //color: #f56c6c;
    width: 12px;
    height: 12px;
    background: #f00;
    border-radius: 50%;
    position: relative;
    //box-sizing: border-box;
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

@media only screen and (max-width: 1400px) {
    .issue-layout {
        grid-template-columns: 120px minmax(0, 1fr) 220px;
    }
}
</style>

<style lang="less">
.coupon-issue-selected-tooltip {
    max-width: 300px;
    white-space: normal;
    word-break: break-all;
}
</style>
