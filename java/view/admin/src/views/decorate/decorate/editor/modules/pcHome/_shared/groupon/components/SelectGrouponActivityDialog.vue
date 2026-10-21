<template>
    <div class="groupon-activity-selector">
        <div class="groupon-activity-selector__inner">
            <div class="groupon-activity-selector__toolbar list-table-tool lyecs-search-warp">
                <el-form :model="filterParams" name="form">
                    <div class="advanced-search-warp list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>活动名称：</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.grouponName"
                                            name="grouponName"
                                            placeholder="输入活动名称"
                                            clearable
                                            @keyup.enter="onSearchSubmit"
                                            @clear="onSearchSubmit"
                                        >
                                            <template #append>
                                                <el-button @click="onSearchSubmit">
                                                    <span class="iconfont icon-chakan1"></span>
                                                </el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>活动状态：</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.status" placeholder="请选择活动状态" @change="onSearchSubmit">
                                            <el-option :value="1" label="活动进行中" />
                                            <el-option :value="3" label="活动未开始" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <label class="control-label"></label>
                                    <div class="control-container">
                                        <el-button type="primary" plain @click="onSearchSubmit">搜索</el-button>
                                        <el-button plain @click="resetParams">重置</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </el-form>
            </div>

            <perfect-scrollbar ref="psRef" class="groupon-activity-selector__table table-container" :options="psOptions" :watch-options="true">
                <a-spin :spinning="loading">
                    <div :style="{ boxSizing: 'border-box', paddingRight: needScrollY ? '15px' : '0' }">
                        <el-table
                            ref="tableRef"
                            class="result-table hide-checkbox"
                            :data="activityList"
                            row-key="grouponPromotionId"
                            @selection-change="onSelectChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column label="活动名称" prop="name" :width="150" />
                            <el-table-column label="起始日期" prop="startTime" min-width="150" />
                            <el-table-column label="截止日期" prop="endTime" min-width="150" />
                            <el-table-column label="拼团类型">
                                <template #default="{ row }">
                                    {{ row.groupType == 1 ? "普通团" : row.groupType == 2 ? "阶梯团" : "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column label="状态" prop="statusName" width="100" />
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">暂无数据</div>
                                </div>
                            </template>
                        </el-table>
                    </div>
                </a-spin>
            </perfect-scrollbar>

            <div v-if="total > 0" class="groupon-activity-selector__pagination pagination-con">
                <Pagination
                    v-model:page="filterParams.page"
                    v-model:size="filterParams.size"
                    :total="total"
                    layout="slot ,prev, pager, next"
                    :background="false"
                    @callback="loadActivityList"
                />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * 选择拼团活动（供 DialogForm 加载的内容组件）
 * 样式对齐移动端装修选择活动弹层，但独立实现，不复用 SelectActivity
 */
import "@/style/css/list.less";
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, useTemplateRef, watch } from "vue";
import { message } from "ant-design-vue";
import type { TableInstance } from "element-plus";
import { Pagination } from "@/components/list";
import { getGrouponList } from "@/api/promotion/groupon";
import type { GrouponFilterState } from "@/types/promotion/groupon";
import type { GrouponActivityItem } from "../types";

defineOptions({ name: "SelectGrouponActivityDialog" });

const props = withDefaults(
    defineProps<{
        /** 拼团类型过滤：1=普通拼团 2=阶梯拼团 */
        groupType?: number | null;
        /** 已选活动（用于回显，兼容单对象或数组） */
        selected?: GrouponActivityItem | null;
        selectedItems?: GrouponActivityItem[];
    }>(),
    {
        groupType: null,
        selected: null,
        selectedItems: () => []
    }
);

const emit = defineEmits<{
    (e: "submitCallback", value: GrouponActivityItem): void;
}>();

const tableRef = useTemplateRef<TableInstance>("tableRef");
const activityList = ref<GrouponFilterState[]>([]);
const loading = ref(false);
const total = ref(0);
const tempSelected = ref<GrouponActivityItem | null>(null);

const filterParams = reactive({
    page: 1,
    size: 10,
    grouponName: "",
    status: 1 as number
});

const initialSelected = (): GrouponActivityItem | null => {
    if (props.selectedItems?.length) {
        return props.selectedItems[0];
    }
    return props.selected || null;
};

tempSelected.value = initialSelected();

const psRef = ref<any>(null);
const needScrollY = ref(false);
let ro: ResizeObserver | null = null;

const psOptions = computed(() => ({
    suppressScrollY: !needScrollY.value
}));

const getPsEl = (): HTMLElement | null => {
    const inst: any = psRef.value;
    if (!inst) return null;
    return (inst.$el ?? inst) as HTMLElement;
};

const calcNeedScroll = () => {
    nextTick(() => {
        const el = getPsEl();
        if (!el) return;
        needScrollY.value = el.scrollHeight - el.clientHeight > 1;
    });
};

const toActivityItem = (row: GrouponFilterState): GrouponActivityItem => ({
    grouponPromotionId: String(row.grouponPromotionId),
    name: row.name || "",
    startTime: row.startTime,
    endTime: row.endTime,
    groupType: row.groupType
});

const setTabelDefaultSelect = () => {
    nextTick(() => {
        if (!tableRef.value) return;
        tableRef.value.clearSelection();
        const selectedId = tempSelected.value?.grouponPromotionId;
        if (!selectedId) return;
        activityList.value.forEach((row) => {
            if (String(row.grouponPromotionId) === String(selectedId)) {
                tableRef.value?.toggleRowSelection(row, true);
            }
        });
    });
};

const loadActivityList = async () => {
    loading.value = true;
    try {
        const result = await getGrouponList({
            page: filterParams.page,
            size: filterParams.size,
            grouponName: filterParams.grouponName,
            status: filterParams.status,
            groupType: props.groupType ?? undefined
        });
        activityList.value = result.records || [];
        total.value = result.total || 0;
        setTabelDefaultSelect();
    } catch (error: any) {
        message.error(error?.message || "获取拼团活动列表失败");
        activityList.value = [];
        total.value = 0;
    } finally {
        loading.value = false;
        calcNeedScroll();
    }
};

/** 单选：仅保留最后勾选的一项 */
const onSelectChange = (rows: GrouponFilterState[]) => {
    if (rows.length > 1) {
        const delRow = rows.shift();
        tableRef.value?.toggleRowSelection(delRow, false);
    }
    const row = rows[0];
    tempSelected.value = row?.grouponPromotionId ? toActivityItem(row) : null;
};

const onSearchSubmit = () => {
    filterParams.page = 1;
    loadActivityList();
};

const resetParams = () => {
    filterParams.page = 1;
    filterParams.grouponName = "";
    filterParams.status = 1;
    loadActivityList();
};

const onFormSubmit = () => {
    if (!tempSelected.value) {
        message.warning("请选择拼团活动");
        return;
    }
    emit("submitCallback", tempSelected.value);
};

onMounted(() => {
    loadActivityList();
    nextTick(() => {
        const el = getPsEl();
        if (!el || typeof ResizeObserver === "undefined") return;
        ro = new ResizeObserver(() => calcNeedScroll());
        ro.observe(el);
        if (el.firstElementChild) {
            ro.observe(el.firstElementChild);
        }
        calcNeedScroll();
    });
});

onBeforeUnmount(() => {
    ro?.disconnect();
    ro = null;
});

watch([() => loading.value, () => total.value, () => activityList.value.length], () => {
    calcNeedScroll();
});

defineExpose({
    onFormSubmit
});
</script>

<style lang="less" scoped>
.groupon-activity-selector {
    position: relative;
    width: 100%;
    height: 100%;

    &__inner {
        position: absolute;
        display: flex;
        flex-direction: column;
        row-gap: 20px;
        width: 100%;
        height: 100%;
    }

    &__toolbar {
        flex: 0;
    }

    &__table {
        flex: 1;
        min-height: 0;
    }

    &__pagination {
        flex-shrink: 0;
        background: #fff;
    }
}

:deep(.hide-checkbox .el-table__header-wrapper .el-table__header .el-checkbox) {
    display: none;
}
</style>
