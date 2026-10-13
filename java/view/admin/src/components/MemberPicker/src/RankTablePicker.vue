/**
 * @file 会员等级表格选择器
 * @description 搜索 + 表格复选选择会员等级，等级数据量小，一次拉取后前端过滤
 * @features
 * - 数据源可替换（默认会员等级列表接口）
 * - v-model 为 { [rankId]: 等级 } 的映射，便于父级跨 Tab 汇总已选
 */
<template>
    <div class="member-table-picker">
        <div class="picker-search">
            <div class="picker-search__row">
                <TigInput
                    v-model="filter.keyword"
                    name="rankKeyword"
                    placeholder="等级名称"
                    clearable
                    width="250px"
                    @keyup.enter="applyFilter"
                    @clear="applyFilter"
                >
                    <template #append>
                        <el-button @click="applyFilter"><span class="iconfont icon-chakan1"></span></el-button>
                    </template>
                </TigInput>
                <el-select v-model="filter.rankId" class="picker-search__select" clearable placeholder="请选择" @change="applyFilter">
                    <el-option v-for="item in options" :key="item.rankId" :label="getRankLevelText(item)" :value="item.rankId!" />
                </el-select>
            </div>
            <div class="picker-search__actions">
                <el-button type="primary" @click="applyFilter">搜索</el-button>
                <el-button @click="onReset">重置</el-button>
            </div>
        </div>

        <div class="picker-table">
            <el-table ref="tableRef" :data="displayList" :loading="loading" row-key="rankId" height="100%" @selection-change="onSelectionChange">
                <el-table-column type="selection" width="44" reserve-selection />
                <el-table-column label="等级" min-width="90">
                    <template #default="{ row }">
                        <div class="level_txt">{{ getRankLevelText(row) }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="等级名称" min-width="160">
                    <template #default="{ row }">
                        {{ row.rankName || "--" }}
                    </template>
                </el-table-column>
                <template #empty>
                    <div class="empty-warp">
                        <div v-if="!loading" class="empty-bg">暂无数据</div>
                    </div>
                </template>
            </el-table>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { getUserRankList } from "@/api/user/userRank";
import type { MemberPickerRank, MemberPickerRankMap } from "@/types/user/memberPicker";

const props = defineProps<{
    /** 已选等级映射，key 为 rankId */
    modelValue?: MemberPickerRankMap;
    /** 等级数据源，可返回数组、{ records } 或 { userRank: { records } } */
    requestApi?: () => Promise<any>;
}>();

const emit = defineEmits<{
    (e: "update:modelValue", value: MemberPickerRankMap): void;
}>();

const tableRef = ref();
const loading = ref(false);
const options = ref<MemberPickerRank[]>([]);
const displayList = ref<MemberPickerRank[]>([]);

const filter = reactive({
    keyword: "",
    rankId: undefined as number | undefined
});

const selected = computed<MemberPickerRankMap>(() => props.modelValue ?? {});

const getRankLevelText = (row: MemberPickerRank) => (row.rankLevel != null ? `vip${row.rankLevel}` : row.rankName || "--");

/** 不同接口的返回结构不一致，统一取出等级数组 */
const normalizeRanks = (result: any): MemberPickerRank[] => {
    if (Array.isArray(result)) return result;
    if (Array.isArray(result?.records)) return result.records;
    if (Array.isArray(result?.userRank?.records)) return result.userRank.records;
    if (Array.isArray(result?.userRank)) return result.userRank;
    return [];
};

const syncSelection = async () => {
    await nextTick();
    if (!tableRef.value) return;
    displayList.value.forEach((row) => {
        tableRef.value.toggleRowSelection(row, !!selected.value[row.rankId as number]);
    });
};

const applyFilter = async () => {
    const keyword = filter.keyword.trim();
    displayList.value = options.value.filter((item) => {
        const keywordMatch = !keyword || item.rankName?.includes(keyword);
        const rankMatch = !filter.rankId || item.rankId === filter.rankId;
        return keywordMatch && rankMatch;
    });
    await syncSelection();
};

const loadList = async () => {
    loading.value = true;
    try {
        const requestApi = props.requestApi ?? (() => getUserRankList({ page: 1, size: 100, sortField: "", sortOrder: "" } as any));
        options.value = normalizeRanks(await requestApi());
        await applyFilter();
    } catch (error: any) {
        message.error(error.message || "会员等级加载失败");
    } finally {
        loading.value = false;
    }
};

const onReset = () => {
    filter.keyword = "";
    filter.rankId = undefined;
    applyFilter();
};

const onSelectionChange = (selection: MemberPickerRank[]) => {
    const next: MemberPickerRankMap = { ...selected.value };
    displayList.value.forEach((row) => {
        delete next[row.rankId as number];
    });
    selection.forEach((row) => {
        if (row.rankId) {
            next[row.rankId] = row;
        }
    });
    emit("update:modelValue", next);
};

onMounted(() => {
    loadList();
});

defineExpose({ refresh: loadList, syncSelection });
</script>

<style lang="less" scoped>
@import "./picker.less";

.picker-search__select {
    width: 200px;
}

.level_txt {
    font-weight: 600;
    color: #c95a0a;
    font-size: 14px;
}
</style>
