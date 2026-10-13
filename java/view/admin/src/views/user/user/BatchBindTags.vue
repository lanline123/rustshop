<template>
    <div class="batch-bind-tags">
        <div class="main-layout">
            <div class="left-box">
                <div class="filter-bar">
                    <TigInput v-model="searchKeyword" :placeholder="$t('按回车搜索标签 / 标签值')" clearable style="width: 240px"
                        @keyup.enter="handleSearchSubmit" @clear="handleSearchClear">
                        <template #append>
                            <el-button @click="handleSearchSubmit"><span
                                    class="iconfont icon-chakan1"></span></el-button>
                        </template>
                    </TigInput>
                </div>
                <div class="selector-body">
                    <div class="groups-panel">
                        <div class="panel-title">{{ $t('标签') }}</div>
                        <div class="group-list">
                            <div v-for="group in displayGroups" :key="group.id"
                                :class="['group-item', { active: activeGroupId === group.id }]"
                                @click="activeGroupId = group.id!">
                                {{ group.groupName }}
                            </div>
                            <div v-if="displayGroups.length === 0" class="empty-text">{{ $t('暂无标签') }}</div>
                        </div>
                    </div>
                    <div class="tags-panel">
                        <div class="panel-title">{{ $t('标签值') }}</div>
                        <div :key="activeGroupId ?? 'empty'" class="tags-list">
                            <el-tag v-for="tag in activeGroupTags" :key="tag.id"
                                :class="['tag-item', { active: isSelected(tag.id!) }]" @click="toggleTag(tag)">
                                {{ tag.tagName }}
                            </el-tag>
                            <div v-if="!activeGroupId" class="empty-text">{{ $t('请选择左侧标签组') }}</div>
                            <div v-else-if="activeGroupTags.length === 0" class="empty-text">{{ $t('暂无标签值') }}</div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="selected-panel">
                <div class="panel-header">
                    <span>{{ $t('已选{selectedGroupCount}个标签', { selectedGroupCount }) }}</span>
                    <a class="clear-btn" @click="clearAll">{{ $t('清空') }}</a>
                </div>
                <div class="selected-list">
                    <div v-for="entry in selectedSummary" :key="entry.groupId" class="selected-item">
                        <el-tooltip :content="`${entry.groupName}：${entry.tagNames}`" placement="top-start"
                            popper-class="batch-bind-tags-tooltip" :disabled="!isTextOverflow(entry.groupId)">
                            <span :ref="(el) => setItemTextRef(entry.groupId, el as Element | null)"
                                class="item-text">{{
                                entry.groupName }}：{{ entry.tagNames }}</span>
                        </el-tooltip>
                        <el-icon class="item-remove" @click="removeGroupSelection(entry.groupId)">
                            <CircleCloseFilled />
                        </el-icon>
                    </div>
                    <div v-if="selectedSummary.length === 0" class="empty-text">{{ $t('暂未选择标签') }}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, computed, nextTick, onBeforeUnmount, onMounted, watch } from "vue";
import { message } from "ant-design-vue";
import { CircleCloseFilled } from "@element-plus/icons-vue";
import { getMemberTagGroupList, bindUserTags, batchBindUserTags, getUserMemberTagList } from "@/api/user/userTags";
import type { UserTagFilterParams, UserTagFilterState, TagItem } from "@/types/user/userTags.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    ids: { type: Array, default: () => [] },
    userId: { type: Number, default: 0 },
    isDialog: Boolean
});

const groupOptions = ref<UserTagFilterState[]>([]);
const allGroups = ref<UserTagFilterState[]>([]);
const activeGroupId = ref<number | null>(null);
const selectedTagIds = ref<number[]>([]);
const searchKeyword = ref("");
const appliedKeyword = ref("");
const isSingleUser = computed(() => !!props.userId || (Array.isArray(props.ids) && props.ids.length === 1));

const normalizeKeyword = (value: string) => value.trim();

const buildListParams = (keyword = ""): UserTagFilterParams => ({
    page: 1,
    size: 100,
    sortField: "",
    sortOrder: "",
    ...(keyword ? { keyword } : {})
});

const loadGroupOptions = async () => {
    try {
        const res = await getMemberTagGroupList(buildListParams());
        groupOptions.value = res.records || [];
    } catch (_) {
        /* ignore */
    }
};

const loadGroups = async () => {
    try {
        const res = await getMemberTagGroupList(buildListParams(appliedKeyword.value));
        allGroups.value = res.records || [];
    } catch (_) {
        allGroups.value = [];
    }
};

const displayGroups = computed(() => allGroups.value);

const activeGroupTags = computed(() => {
    if (!activeGroupId.value) return [];
    const group = displayGroups.value.find((g) => g.id === activeGroupId.value);
    if (!group?.tags) return [];
    return group.tags;
});

const syncActiveGroup = () => {
    const groups = displayGroups.value;
    if (groups.length === 0) {
        activeGroupId.value = null;
        return;
    }
    if (!groups.some((group) => group.id === activeGroupId.value)) {
        activeGroupId.value = groups[0].id ?? null;
    }
};

const handleSearchSubmit = async () => {
    const keyword = normalizeKeyword(searchKeyword.value);
    searchKeyword.value = keyword;
    appliedKeyword.value = keyword;
    await loadGroups();
    syncActiveGroup();
};

const handleSearchClear = async () => {
    searchKeyword.value = "";
    appliedKeyword.value = "";
    await loadGroups();
    syncActiveGroup();
};

const currentUserId = computed(() => {
    if (props.userId) return Number(props.userId);
    if (Array.isArray(props.ids) && props.ids.length === 1) return Number(props.ids[0]);
    return 0;
});

const isSelected = (tagId: number) => selectedTagIds.value.includes(tagId);

const toggleTag = (tag: TagItem) => {
    const idx = selectedTagIds.value.indexOf(tag.id!);
    if (idx >= 0) {
        selectedTagIds.value.splice(idx, 1);
    } else {
        selectedTagIds.value.push(tag.id!);
    }
};

const selectedSummary = computed(() => {
    const result: { groupId: number; groupName: string; tagNames: string }[] = [];
    for (const group of groupOptions.value) {
        if (!group.tags) continue;
        const selected = group.tags.filter((t) => selectedTagIds.value.includes(t.id!));
        if (selected.length > 0) {
            result.push({
                groupId: group.id!,
                groupName: group.groupName!,
                tagNames: selected.map((t) => t.tagName).join("、")
            });
        }
    }
    return result;
});

const selectedGroupCount = computed(() => selectedSummary.value.length);
const itemTextRefs = new Map<number, HTMLElement>();
const overflowMap = ref<Record<number, boolean>>({});

const setItemTextRef = (groupId: number, el: Element | null) => {
    if (el instanceof HTMLElement) {
        itemTextRefs.set(groupId, el);
        return;
    }
    itemTextRefs.delete(groupId);
};

const updateOverflowState = () => {
    const nextMap: Record<number, boolean> = {};
    itemTextRefs.forEach((el, groupId) => {
        nextMap[groupId] = el.scrollWidth > el.clientWidth;
    });
    overflowMap.value = nextMap;
};

const isTextOverflow = (groupId: number) => !!overflowMap.value[groupId];

const clearAll = () => {
    selectedTagIds.value = [];
};

const removeGroupSelection = (groupId: number) => {
    const group = groupOptions.value.find((g) => g.id === groupId);
    if (!group?.tags) return;
    const groupTagIds = group.tags.map((t) => t.id!);
    selectedTagIds.value = selectedTagIds.value.filter((id) => !groupTagIds.includes(id));
};

const loadSelectedTagsByUser = async () => {
    if (!isSingleUser.value) {
        selectedTagIds.value = [];
        return;
    }
    const userId = currentUserId.value;
    if (!userId) {
        selectedTagIds.value = [];
        return;
    }
    try {
        const userTags = await getUserMemberTagList({ userId });
        selectedTagIds.value = Array.from(new Set((userTags || []).map((item) => item.tagId).filter(Boolean)));
    } catch (error: any) {
        selectedTagIds.value = [];
        message.error(error.message || t('加载用户标签失败'));
    }
};

onMounted(async () => {
    await loadGroupOptions();
    await loadGroups();
    await loadSelectedTagsByUser();
    syncActiveGroup();
    await nextTick();
    updateOverflowState();
    window.addEventListener("resize", updateOverflowState);
});

onBeforeUnmount(() => {
    window.removeEventListener("resize", updateOverflowState);
});

watch(
    () => [props.userId, props.ids],
    async () => {
        await loadSelectedTagsByUser();
    },
    { deep: true }
);

watch(displayGroups, () => {
    syncActiveGroup();
});

watch(
    selectedSummary,
    async () => {
        await nextTick();
        updateOverflowState();
    },
    { deep: true }
);

const onFormSubmit = async () => {
    if (selectedTagIds.value.length === 0) {
        message.warning(t('请至少选择一个标签'));
        return;
    }
    try {
        emit("update:confirmLoading", true);
        if (isSingleUser.value && currentUserId.value) {
            await bindUserTags({
                userId: currentUserId.value,
                tagIds: selectedTagIds.value
            });
            message.success(t('打标签成功'));
        } else {
            await batchBindUserTags({
                userIds: props.ids as number[],
                tagIds: selectedTagIds.value
            });
            message.success(t('批量打标签成功'));
        }
        emit("submitCallback");
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.batch-bind-tags {
    display: flex;
    flex-direction: column;
}

.main-layout {
    display: flex;
    gap: 16px;
    min-height: 480px;
}

.left-box {
    flex: 1;
    display: flex;
    flex-direction: column;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    overflow: hidden;

    .filter-bar {
        display: flex;
        gap: 10px;
        padding: 12px 16px;
        border-bottom: 1px solid #e8e8e8;
        background: #f7f8fa;
    }

    .selector-body {
        display: flex;
        flex: 1;
        min-height: 0;
    }
}

.groups-panel {
    width: 160px;
    border-right: 1px solid #e8e8e8;

    .panel-title {
        padding: 12px 16px;
        font-weight: 600;
        border-bottom: 1px solid #e8e8e8;
        color: #303133;
    }

    .group-list {
        overflow-y: auto;
        max-height: 400px;
    }

    .group-item {
        padding: 10px 16px;
        cursor: pointer;
        color: #606266;
        font-size: 14px;
        transition: background-color 0.2s;

        &:hover {
            background: #f5f7fa;
        }

        &.active {
            background: var(--el-color-primary-light-9);
            color: var(--el-color-primary);
        }
    }
}

.tags-panel {
    flex: 1;

    .panel-title {
        padding: 12px 16px;
        font-weight: 600;
        border-bottom: 1px solid #e8e8e8;
        color: #303133;
    }

    .tags-list {
        padding: 16px;
        display: flex;
        flex-wrap: wrap;
        align-content: flex-start;
        gap: 8px;

        :deep(.tag-item.el-tag) {
            cursor: pointer;
            margin: 0;
            transition: none;
            border: none;
            background: #f7f8fa;
            color: #606266;
            padding: 0 12px;

            &:hover {
                color: var(--el-color-primary);
            }
        }

        :deep(.tag-item.el-tag.active) {
            background: var(--el-color-primary-light-9);
            color: var(--el-color-primary);
        }

        .empty-text {
            width: 100%;
        }
    }
}

.selected-panel {
    width: 220px;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    background: #fff;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;

    .panel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        background: #f7f8fa;
        border-bottom: 1px solid #e8e8e8;
        font-weight: 600;
        color: #303133;

        .clear-btn {
            color: var(--el-color-primary);
            cursor: pointer;
            font-weight: normal;
            font-size: 13px;
        }
    }

    .selected-list {
        padding: 12px 16px;
        background: #fff;
        overflow-y: auto;
        flex: 1;

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

            .item-text {
                line-height: 20px;
                flex: 1;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
            }

            .item-remove {
                cursor: pointer;
                color: #c0c4cc;
                margin-left: 8px;
                flex-shrink: 0;

                &:hover {
                    color: #909399;
                }
            }
        }
    }
}

.empty-text {
    padding: 16px;
    color: #909399;
    font-size: 13px;
    text-align: center;
}
</style>

<style lang="less">
.batch-bind-tags-tooltip {
    max-width: 300px;
    white-space: normal;
    word-break: break-all;
}
</style>
