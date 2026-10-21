<template>
    <div class="filter-tag-selector">
        <div class="main-layout">
            <div class="left-box">
                <div class="left-tools">
                    <div class="match-mode">
                        <el-radio-group v-model="matchMode">
                            <el-radio value="any">{{ $t('满足任一') }}</el-radio>
                            <el-radio value="all">{{ $t('满足全部') }}</el-radio>
                        </el-radio-group>
                    </div>
                    <div class="filter-bar">
                        <el-select v-if="props.showGroupFilter" v-model="filterGroupId" class="group-filter">
                            <el-option :value="0" :label="$t('全部标签')" />
                            <el-option v-for="group in allGroups" :key="group.id" :label="group.groupName" :value="group.id!" />
                        </el-select>
                        <TigInput v-model="searchKeyword" :placeholder="$t('按回车搜索标签')" clearable style="width: 300px">
                            <template #append>
                                <el-button><span class="iconfont icon-chakan1"></span></el-button>
                            </template>
                        </TigInput>
                    </div>
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
            <div v-if="props.showSelectedPanel" class="selected-panel">
                <div class="panel-header">
                    <span>{{ $t('已选{selectedGroupCount}项', { selectedGroupCount }) }}</span>
                    <a class="clear-btn" @click="clearAll">{{ $t('清空') }}</a>
                </div>
                <div class="selected-list">
                    <div v-for="entry in selectedSummary" :key="entry.groupId" class="selected-group">
                        <div class="group-label">{{ entry.groupName }} ({{ matchModeLabel }})</div>
                        <div class="selected-item">
                            <el-tooltip :content="entry.tagNames" placement="top-start"
                                popper-class="filter-tag-selector-tooltip" :disabled="!isTextOverflow(entry.groupId)">
                                <span :ref="(el) => setItemTextRef(entry.groupId, el as Element | null)"
                                    class="item-text">{{ entry.tagNames }}</span>
                            </el-tooltip>
                            <el-icon class="item-remove" @click="removeGroupSelection(entry.groupId)">
                                <CircleCloseFilled />
                            </el-icon>
                        </div>
                    </div>
                    <div v-if="selectedSummary.length === 0" class="empty-text">{{ $t('暂未选择标签') }}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, computed, nextTick, onBeforeUnmount, onMounted, watch } from "vue";
import { CircleCloseFilled } from "@element-plus/icons-vue";
import { getMemberTagGroupList } from "@/api/user/userTags";
import type { UserTagFilterState, TagItem } from "@/types/user/userTags.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
export interface FilterTagResult {
    tagIds: number[];
    matchMode: "any" | "all";
}

/** 按标签组聚合的已选摘要 */
export interface FilterTagGroupSummary {
    groupId: number;
    groupName: string;
    tagNames: string;
    tagIds: number[];
}

export interface FilterTagChangeResult extends FilterTagResult {
    summary: FilterTagGroupSummary[];
}

const props = withDefaults(
    defineProps<{
        initialTagIds?: number[];
        initialMatchMode?: "any" | "all";
        /** 是否显示右侧「已选」面板，嵌入其它选择器时可关闭 */
        showSelectedPanel?: boolean;
        /** 是否显示标签组下拉筛选 */
        showGroupFilter?: boolean;
    }>(),
    {
        showSelectedPanel: true,
        showGroupFilter: false
    }
);

const emit = defineEmits<{
    (e: "confirm", result: FilterTagResult): void;
    (e: "cancel"): void;
    (e: "change", result: FilterTagChangeResult): void;
}>();

const matchMode = ref<"any" | "all">(props.initialMatchMode ?? "any");
const allGroups = ref<UserTagFilterState[]>([]);
const activeGroupId = ref<number | null>(null);
const selectedTagIds = ref<number[]>([...(props.initialTagIds ?? [])]);
const searchKeyword = ref("");
const filterGroupId = ref<number>(0);

const matchModeLabel = computed(() => (matchMode.value === "any" ? t('满足任一') : t('满足全部')));

const loadGroups = async () => {
    try {
        const res = await getMemberTagGroupList({ page: 1, size: 100, sortField: "", sortOrder: "" });
        allGroups.value = res.records || [];
        if (allGroups.value.length > 0 && !activeGroupId.value) {
            activeGroupId.value = allGroups.value[0].id!;
        }
    } catch (_) {
        /* ignore */
    }
};

const displayGroups = computed(() => {
    let groups = allGroups.value;
    if (filterGroupId.value) {
        groups = groups.filter((g) => g.id === filterGroupId.value);
    }
    if (searchKeyword.value) {
        const kw = searchKeyword.value.toLowerCase();
        groups = groups.filter(
            (g) => g.groupName?.toLowerCase().includes(kw) || g.tags?.some((t) => t.tagName?.toLowerCase().includes(kw))
        );
    }
    return groups;
});

const activeGroupTags = computed(() => {
    if (!activeGroupId.value) return [];
    const group = allGroups.value.find((g) => g.id === activeGroupId.value);
    if (!group?.tags) return [];
    if (searchKeyword.value) {
        const kw = searchKeyword.value.toLowerCase();
        return group.tags.filter((t) => t.tagName?.toLowerCase().includes(kw));
    }
    return group.tags;
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

const selectedSummary = computed<FilterTagGroupSummary[]>(() => {
    const result: FilterTagGroupSummary[] = [];
    for (const group of allGroups.value) {
        if (!group.tags) continue;
        const selected = group.tags.filter((t) => selectedTagIds.value.includes(t.id!));
        if (selected.length > 0) {
            result.push({
                groupId: group.id!,
                groupName: group.groupName!,
                tagNames: selected.map((t) => t.tagName).join("、"),
                tagIds: selected.map((t) => t.id!)
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
    const group = allGroups.value.find((g) => g.id === groupId);
    if (!group?.tags) return;
    const groupTagIds = group.tags.map((t) => t.id!);
    selectedTagIds.value = selectedTagIds.value.filter((id) => !groupTagIds.includes(id));
};

const getResult = (): FilterTagResult => ({
    tagIds: [...selectedTagIds.value],
    matchMode: matchMode.value
});

const reset = (tagIds?: number[], mode?: "any" | "all") => {
    selectedTagIds.value = [...(tagIds ?? [])];
    matchMode.value = mode ?? "any";
};

onMounted(async () => {
    await loadGroups();
    await nextTick();
    updateOverflowState();
    window.addEventListener("resize", updateOverflowState);
});

onBeforeUnmount(() => {
    window.removeEventListener("resize", updateOverflowState);
});

watch(
    selectedSummary,
    async () => {
        await nextTick();
        updateOverflowState();
    },
    { deep: true }
);

// 标签组筛选后当前激活组可能被隐藏，重新指向第一个可见组
watch(displayGroups, (groups) => {
    if (groups.length === 0) {
        activeGroupId.value = null;
        return;
    }
    if (!groups.some((group) => group.id === activeGroupId.value)) {
        activeGroupId.value = groups[0].id ?? null;
    }
});

watch([selectedTagIds, matchMode, selectedSummary], () => {
    emit("change", { ...getResult(), summary: selectedSummary.value });
}, { deep: true });

defineExpose({ getResult, reset });
</script>

<style lang="less" scoped>
.filter-tag-selector {
    display: flex;
    flex-direction: column;
}

.main-layout {
    display: flex;
    gap: 16px;
    min-height: 420px;
}

.left-box {
    flex: 1;
    display: flex;
    flex-direction: column;
    border: 1px solid #eeeeee;
    border-radius: 4px;
    overflow: hidden;

    .left-tools {
        background: #f7f8fa;
        border-bottom: 1px solid #eeeeee;
    }

    .match-mode {
        padding: 12px 16px 10px;
        border-bottom: 1px solid #eeeeee;
    }

    .filter-bar {
        display: flex;
        gap: 10px;
        padding: 10px 16px 12px;

        .group-filter {
            width: 140px;
            flex-shrink: 0;
        }
    }

    .selector-body {
        display: flex;
        flex: 1;
        min-height: 0;
    }
}

.groups-panel {
    width: 160px;
    border-right: 1px solid #eeeeee;

    .panel-title {
        padding: 12px 16px;
        font-weight: 600;
        border-bottom: 1px solid #eeeeee;
        color: #303133;
    }

    .group-list {
        overflow-y: auto;
        max-height: 340px;
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
        border-bottom: 1px solid #eeeeee;
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
    border: 1px solid #eeeeee;
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
        border-bottom: 1px solid #eeeeee;
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

        .selected-group {
            margin-bottom: 12px;

            .group-label {
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
            border-radius: 6px;
            background: #f7f8fa;
            color: #606266;
            font-size: 13px;

            .item-text {
                line-height: 20px;
                flex: 1;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
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
.filter-tag-selector-tooltip {
    max-width: 300px;
    white-space: normal;
    word-break: break-all;
}
</style>
