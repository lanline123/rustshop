/**
 * @file 发送会员选择器
 * @description 群发场景（站内信/推送）选择接收会员
 * @features
 * - 全部会员 / 部分会员 两级范围
 * - 部分会员支持「指定会员」「会员标签」「会员等级」三种方式，三者互斥，一次只能按其中一种圈选
 * - 指定会员、会员等级复用 MemberPicker，会员标签复用 FilterTagSelector
 */
<template>
    <div class="send-user-selector">
        <el-radio-group v-model="scope" class="scope-group" @change="emitChange">
            <el-radio :value="0">全部会员</el-radio>
            <el-radio :value="1">部分会员</el-radio>
        </el-radio-group>

        <div v-if="scope === 1" class="picker">
            <div class="picker-nav">
                <div
                    v-for="tab in tabs"
                    :key="tab.key"
                    :class="['picker-nav__item', { active: activeTab === tab.key }]"
                    @click="switchTab(tab.key)"
                >
                    {{ tab.label }}
                </div>
            </div>

            <div class="picker-main">
                <UserTablePicker
                    v-show="activeTab === 'user'"
                    ref="userPickerRef"
                    :model-value="selectedUsers"
                    force-certified-column
                    @update:model-value="onUsersChange"
                />

                <div v-if="MEMBER_TAG_AUDIENCE_READY" v-show="activeTab === 'tag'" class="tag-pane">
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
                    v-show="activeTab === 'rank'"
                    ref="rankPickerRef"
                    :model-value="selectedRanks"
                    @update:model-value="onRanksChange"
                />
            </div>

            <div class="picker-selected">
                <div class="picker-selected__header">
                    <span>已选：{{ selectedCount }}项</span>
                    <a class="picker-selected__clear" @click="clearAll">清空</a>
                </div>
                <div class="picker-selected__body">
                    <div v-if="activeTab === 'user' && selectedUserList.length > 0" class="selected-group">
                        <div class="selected-group__label">指定会员</div>
                        <div v-for="user in selectedUserList" :key="user.userId" class="selected-item">
                            <span class="selected-item__text">{{ getUserLabel(user) }}</span>
                            <el-icon class="selected-item__remove" @click="removeUser(user.userId)">
                                <CircleCloseFilled />
                            </el-icon>
                        </div>
                    </div>
                    <div v-if="activeTab === 'tag' && tagSummary.length > 0" class="selected-group">
                        <div class="selected-group__label">会员标签（{{ tagMatchModeLabel }}）</div>
                        <div v-for="entry in tagSummary" :key="entry.groupId" class="selected-item">
                            <el-tooltip
                                :content="`${entry.groupName}：${entry.tagNames}`"
                                placement="top-start"
                                popper-class="send-user-selector-tooltip"
                            >
                                <span class="selected-item__text">{{ entry.groupName }}：{{ entry.tagNames }}</span>
                            </el-tooltip>
                            <el-icon class="selected-item__remove" @click="removeTagGroup(entry)">
                                <CircleCloseFilled />
                            </el-icon>
                        </div>
                    </div>
                    <div v-if="activeTab === 'rank' && selectedRankList.length > 0" class="selected-group">
                        <div class="selected-group__label">会员等级</div>
                        <div v-for="rank in selectedRankList" :key="rank.rankId" class="selected-item">
                            <span class="selected-item__text">{{ getRankLabel(rank) }}</span>
                            <el-icon class="selected-item__remove" @click="removeRank(rank.rankId as number)">
                                <CircleCloseFilled />
                            </el-icon>
                        </div>
                    </div>
                    <div v-if="selectedCount === 0" class="selected-empty">暂未选择{{ activeTabLabel }}</div>
                </div>
                <div class="picker-selected__note">注：指定会员、会员标签、会员等级三者互斥，一次只能按其中一种方式圈选，切换方式会清空已选内容</div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { CircleCloseFilled } from "@element-plus/icons-vue";
import FilterTagSelector from "@/views/user/user/FilterTagSelector.vue";
import type { FilterTagChangeResult, FilterTagGroupSummary } from "@/views/user/user/FilterTagSelector.vue";
import { RankTablePicker, UserTablePicker } from "@/components/MemberPicker";
import { getUserRankList } from "@/api/user/userRank";
import type { MemberPickerRank, MemberPickerRankMap, MemberPickerUser, MemberPickerUserMap } from "@/types/user/memberPicker";
import type { SendUserTarget } from "@/types/user/memberAudience";
import { MEMBER_TAG_AUDIENCE_READY } from "@/utils/memberAudienceGate";

const props = defineProps<{
    modelValue?: SendUserTarget | null;
}>();

const emit = defineEmits<{
    (e: "update:modelValue", value: SendUserTarget): void;
}>();

type TabKey = "user" | "tag" | "rank";

const tabs: { key: TabKey; label: string }[] = [
    { key: "user", label: "指定会员" },
    ...(MEMBER_TAG_AUDIENCE_READY ? [{ key: "tag" as TabKey, label: "会员标签" }] : []),
    { key: "rank", label: "会员等级" }
];

const scope = ref<number>(0);
const activeTab = ref<TabKey>("user");

const selectedUsers = ref<MemberPickerUserMap>({});
const selectedRanks = ref<MemberPickerRankMap>({});
const tagIds = ref<number[]>([]);
const tagMatchMode = ref<number>(0);
const tagSummary = ref<FilterTagGroupSummary[]>([]);

const userPickerRef = ref<InstanceType<typeof UserTablePicker> | null>(null);
const rankPickerRef = ref<InstanceType<typeof RankTablePicker> | null>(null);
const tagSelectorRef = ref<InstanceType<typeof FilterTagSelector> | null>(null);

/** rankId -> rankName，用于回显「已选」等级名称 */
const rankNameMap = ref<Record<number, string>>({});

const selectedUserList = computed(() => Object.values(selectedUsers.value));
const selectedRankList = computed(() => Object.values(selectedRanks.value));
const tagMatchModeLabel = computed(() => (tagMatchMode.value === 1 ? "满足全部" : "满足任一"));

const tabLabel = (key: TabKey) => tabs.find((tab) => tab.key === key)?.label ?? "";
const activeTabLabel = computed(() => tabLabel(activeTab.value));

const countOf = (key: TabKey) => {
    if (key === "user") return selectedUserList.value.length;
    if (key === "tag") return tagIds.value.length;
    return selectedRankList.value.length;
};

/** 三种方式互斥，已选数量只统计当前方式 */
const selectedCount = computed(() => countOf(activeTab.value));

const getUserLabel = (user: MemberPickerUser) => user.memberName || user.username || user.mobile || `会员${user.userId}`;
const getRankLabel = (rank: MemberPickerRank) => rank.rankName || rankNameMap.value[rank.rankId as number] || `等级${rank.rankId}`;

const buildValue = (): SendUserTarget => {
    const isPart = scope.value === 1;
    const byUser = isPart && activeTab.value === "user";
    const byTag = isPart && activeTab.value === "tag";
    const byRank = isPart && activeTab.value === "rank";
    return {
        scope: scope.value,
        userIds: byUser ? selectedUserList.value.map((user) => user.userId) : [],
        users: byUser
            ? selectedUserList.value.map((user) => ({
                  userId: user.userId,
                  username: user.username || user.memberName || "",
                  mobile: user.mobile
              }))
            : [],
        rankIds: byRank ? selectedRankList.value.map((rank) => rank.rankId as number) : [],
        tagIds: byTag ? [...tagIds.value] : [],
        tagMatchMode: byTag ? tagMatchMode.value : 0
    };
};

// 标记「本次 modelValue 变更来自组件自身 emit」，避免回写触发 syncFromModel 抹掉内部选择态
let internalUpdate = false;

const emitChange = () => {
    internalUpdate = true;
    emit("update:modelValue", buildValue());
};

const onUsersChange = (value: MemberPickerUserMap) => {
    selectedUsers.value = value;
    emitChange();
};

const onRanksChange = (value: MemberPickerRankMap) => {
    selectedRanks.value = value;
    emitChange();
};

const removeUser = (userId: number) => {
    const next = { ...selectedUsers.value };
    delete next[userId];
    selectedUsers.value = next;
    userPickerRef.value?.syncSelection();
    emitChange();
};

const removeRank = (rankId: number) => {
    const next = { ...selectedRanks.value };
    delete next[rankId];
    selectedRanks.value = next;
    rankPickerRef.value?.syncSelection();
    emitChange();
};

const onTagChange = (result: FilterTagChangeResult) => {
    tagIds.value = [...result.tagIds];
    tagMatchMode.value = result.matchMode === "all" ? 1 : 0;
    tagSummary.value = result.summary;
    emitChange();
};

/** 反向同步标签选择器内部状态（在外部「已选」面板移除、清空时） */
const applyTagIds = (nextTagIds: number[]) => {
    tagIds.value = nextTagIds;
    tagSelectorRef.value?.reset(nextTagIds, tagMatchMode.value === 1 ? "all" : "any");
};

const removeTagGroup = (entry: FilterTagGroupSummary) => {
    applyTagIds(tagIds.value.filter((id) => !entry.tagIds.includes(id)));
    tagSummary.value = tagSummary.value.filter((item) => item.groupId !== entry.groupId);
    emitChange();
};

/** 清空某一种选择方式下的已选内容 */
const clearTab = (key: TabKey) => {
    if (key === "user") {
        selectedUsers.value = {};
        userPickerRef.value?.syncSelection();
    } else if (key === "tag") {
        applyTagIds([]);
        tagSummary.value = [];
    } else {
        selectedRanks.value = {};
        rankPickerRef.value?.syncSelection();
    }
};

const clearAll = () => {
    tabs.forEach((tab) => clearTab(tab.key));
    emitChange();
};

/** 切换选择方式：三者互斥，切换即清空其他方式的已选内容 */
const switchTab = (key: TabKey) => {
    if (key === activeTab.value) return;
    tabs.filter((tab) => tab.key !== key).forEach((tab) => clearTab(tab.key));
    activeTab.value = key;
    emitChange();
};

/** 供父表单提交前调用 */
const validate = (): { valid: boolean; message?: string } => {
    if (scope.value === 1 && selectedCount.value === 0) {
        return { valid: false, message: `请选择${activeTabLabel.value}` };
    }
    return { valid: true };
};

/**
 * 推导回显时应激活的选择方式
 * @description 历史数据可能同时存有多组，按「指定会员 > 会员标签 > 会员等级」取其一，保证互斥；
 *              三组都为空时沿用当前方式，避免刚切换到空白方式就被重置
 */
const resolveTab = (v?: SendUserTarget | null): TabKey => {
    if ((v?.users?.length ?? 0) > 0 || (v?.userIds?.length ?? 0) > 0) return "user";
    if (MEMBER_TAG_AUDIENCE_READY && (v?.tagIds?.length ?? 0) > 0) return "tag";
    if ((v?.rankIds?.length ?? 0) > 0) return "rank";
    return activeTab.value;
};

/** 根据外部传入的值回显选择态 */
const syncFromModel = (v?: SendUserTarget | null) => {
    scope.value = v?.scope ?? 0;
    activeTab.value = resolveTab(v);

    tagIds.value = activeTab.value === "tag" && v?.tagIds ? [...v.tagIds] : [];
    tagMatchMode.value = activeTab.value === "tag" ? (v?.tagMatchMode ?? 0) : 0;

    const users = activeTab.value === "user" ? (v?.users ?? []) : [];
    selectedUsers.value = users.reduce<MemberPickerUserMap>((map, user) => {
        map[user.userId] = { userId: user.userId, username: user.username, mobile: user.mobile };
        return map;
    }, {});

    const rankIds = activeTab.value === "rank" ? (v?.rankIds ?? []) : [];
    selectedRanks.value = rankIds.reduce<MemberPickerRankMap>((map, rankId) => {
        map[rankId] = { rankId, rankName: rankNameMap.value[rankId] };
        return map;
    }, {});

    nextTick(() => {
        userPickerRef.value?.syncSelection();
        rankPickerRef.value?.syncSelection();
        tagSelectorRef.value?.reset([...tagIds.value], tagMatchMode.value === 1 ? "all" : "any");
    });
};

watch(
    () => props.modelValue,
    (v) => {
        if (internalUpdate) {
            internalUpdate = false;
            return;
        }
        syncFromModel(v);
    },
    { immediate: true }
);

onMounted(async () => {
    try {
        const result: any = await getUserRankList({ sortField: "", sortOrder: "" } as any);
        const records: MemberPickerRank[] = result?.userRank?.records || result?.records || result?.userRank || [];
        const map: Record<number, string> = {};
        records.forEach((rank) => {
            if (rank.rankId != null) map[rank.rankId] = rank.rankName || "";
        });
        rankNameMap.value = map;
        // 回显时若等级名称此前缺失，用加载到的名称补齐
        const patched: MemberPickerRankMap = {};
        Object.keys(selectedRanks.value).forEach((key) => {
            const rankId = Number(key);
            patched[rankId] = { rankId, rankName: map[rankId] || selectedRanks.value[rankId]?.rankName };
        });
        selectedRanks.value = patched;
    } catch (_) {
        /* ignore */
    }
});

defineExpose({ validate });
</script>

<style lang="less" scoped>
.send-user-selector {
    width: 100%;

    .scope-group {
        margin-bottom: 12px;

        :deep(.el-radio) {
            margin-right: 40px;
        }
    }
}

.picker {
    display: flex;
    height: 480px;
    border: 1px solid #eeeeee;
    border-radius: 4px;
    background: #fff;
    overflow: hidden;
}

.picker-nav {
    width: 108px;
    flex-shrink: 0;
    border-right: 1px solid #eeeeee;
}

.picker-nav__item {
    padding: 10px 16px;
    color: #606266;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
        background: #f5f7fa;
    }

    &.active {
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
    }
}

.picker-main {
    flex: 1;
    min-width: 0;
    display: flex;
    padding: 12px;
}

// 复用会员标签选择器，去掉其自带外框，融入当前分栏容器
.tag-pane {
    flex: 1;
    min-width: 0;
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

    :deep(.left-box) {
        border-color: #eeeeee;
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

.picker-selected {
    width: 180px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    border-left: 1px solid #eeeeee;
}

.picker-selected__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 14px;
    background: #f7f8fa;
    border-bottom: 1px solid #eeeeee;
    color: #303133;
    font-size: 14px;
    font-weight: 600;

    .picker-selected__clear {
        color: var(--el-color-primary);
        font-size: 13px;
        font-weight: normal;
        cursor: pointer;
    }
}

.picker-selected__body {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding: 12px 14px;

    .selected-group {
        margin-bottom: 12px;

        &__label {
            color: #909399;
            font-size: 12px;
            margin-bottom: 8px;
        }
    }

    .selected-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 7px 12px;
        margin-bottom: 8px;
        border-radius: 6px;
        background: #f7f8fa;
        color: #606266;
        font-size: 13px;

        &:last-child {
            margin-bottom: 0;
        }

        &__text {
            flex: 1;
            min-width: 0;
            line-height: 20px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        &__remove {
            margin-left: 8px;
            flex-shrink: 0;
            color: #c0c4cc;
            cursor: pointer;

            &:hover {
                color: #909399;
            }
        }
    }

    .selected-empty {
        color: #909399;
        font-size: 13px;
    }
}

.picker-selected__note {
    padding: 12px 14px;
    color: #909399;
    font-size: 12px;
    line-height: 18px;
}
</style>

<style lang="less">
.send-user-selector-tooltip {
    max-width: 300px;
    white-space: normal;
    word-break: break-all;
}
</style>
