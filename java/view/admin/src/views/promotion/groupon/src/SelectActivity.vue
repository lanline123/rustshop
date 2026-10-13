<template>
    <div class="container" style="position: relative; height: 100%">
        <div class="content_wrapper">
            <div class="" style="display: flex; flex-direction: column; row-gap: 20px; width: 100%; height: 100%; position: absolute">
                <div class="list-table-tool lyecs-search-warp" style="flex: 0">
                    <el-form :model="filterParams" name="form">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('活动名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.grouponName"
                                                name="grouponName"
                                                :placeholder="$t('输入活动名称')"
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
                                        <label class="control-label"><span>{{ $t('活动状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.status" :placeholder="$t('请选择活动状态')" @change="onSearchSubmit">
                                                <el-option :label="$t('活动进行中')" :value="1" />
                                                <!-- <el-option label="活动已结束" :value="2" /> -->
                                                <el-option :label="$t('活动未开始')" :value="3" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <div class="control-container">
                                            <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                            <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <perfect-scrollbar ref="psRef" class="table-container" style="flex: 1" :options="psOptions" :watch-options="true">
                    <a-spin :spinning="loading">
                        <div :style="{ boxSizing: 'border-box', paddingRight: needScrollY ? '15px' : '0' }">
                            <el-table
                                ref="tableRef"
                                class="result-table hide-checkbox"
                                :data="filterState"
                                :loading="loading"
                                :total="total"
                                row-key="grouponPromotionId"
                                @selection-change="onSelectChange"
                            >
                                <el-table-column type="selection" width="32" />
                                <el-table-column :label="$t('活动名称')" prop="name" :width="150"></el-table-column>
                                <el-table-column :label="$t('起始日期')" prop="startTime" min-width="150"> </el-table-column>
                                <el-table-column :label="$t('截止日期')" prop="endTime" min-width="150"> </el-table-column>
                                <el-table-column :label="$t('拼团类型')">
                                    <template #default="{ row }">
                                        {{ row.groupType == 1 ? $t('普通团') : row.groupType == 2 ? $t('阶梯团') : "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('状态')" prop="statusName" width="100"></el-table-column>
                                <template #empty>
                                    <div class="empty-warp">
                                        <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                    </div>
                                </template>
                            </el-table>
                        </div>
                    </a-spin>
                </perfect-scrollbar>
                <div v-if="total > 0" class="pagination-con" style="flex-shrink: 0">
                    <Pagination
                        v-model:page="filterParams.page"
                        v-model:size="filterParams.size"
                        :total="total"
                        @callback="loadFilter"
                        layout="slot ,prev, pager, next"
                        :background="false"
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { computed, onBeforeUnmount, onMounted, reactive, ref, useTemplateRef, nextTick, watch, type PropType } from "vue";
import { message } from "ant-design-vue";
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { GrouponFilterParams, GrouponFilterState } from "@/types/promotion/groupon";
import { getGrouponList } from "@/api/promotion/groupon";
import { TableInstance } from "element-plus";

const props = defineProps({
    selectedItems: {
        type: Array as PropType<Array<GrouponFilterState>>,
        default: []
    },
    groupType: {
        type: Number || null,
        default: null
    }
});

const emit = defineEmits(["submitCallback"]);
const selectedItems = ref<GrouponFilterState[]>([]);
const loading = ref(true); //列表加载中
const total = ref<number>(0);
const config = useConfigStore();
const filterState = ref<GrouponFilterState[]>([]);
const filterParams = reactive<GrouponFilterParams>({
    page: 1,
    size: (config as any).get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: "",
    groupType: props.groupType,
    status: 1
});

const tableRef = useTemplateRef<TableInstance>("tableRef");
const psRef = ref<any>(null);
const needScrollY = ref(false);
let ro: ResizeObserver | null = null;

const psOptions = computed(() => ({
    // 根据实际内容高度决定是否展示纵向滚动条
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
        // 容差 1px，避免边框/小数像素导致误判
        needScrollY.value = el.scrollHeight - el.clientHeight > 1;
    });
};

const setTabelDefaultSelect = () => {
    nextTick(() => {
        if (props.selectedItems.length > 0 && tableRef.value) {
            // 清除当前页的所有选中状态
            tableRef.value.clearSelection();
            // 遍历表格数据，根据 props.selectedItems 设置选中状态
            filterState.value.forEach((row) => {
                const isSelected = props.selectedItems.some((item) => item.grouponPromotionId === row.grouponPromotionId);
                if (isSelected) {
                    tableRef.value?.toggleRowSelection(row, true);
                }
            });
        }
    });
};

// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getGrouponList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
        // 数据加载完成后，设置选中状态
        setTabelDefaultSelect();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
        calcNeedScroll();
    }
};
onMounted(() => {
    loadFilter();
    nextTick(() => {
        const el = getPsEl();
        if (!el || typeof ResizeObserver === "undefined") return;
        ro = new ResizeObserver(() => calcNeedScroll());
        ro.observe(el);
        // 内容变化但容器尺寸不变时也需要重新判断
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

watch([() => loading.value, () => total.value, () => filterState.value.length], () => {
    calcNeedScroll();
});
// 参数查询
const onSearchSubmit = () => {
    loadFilter();
};
const resetParams = () => {
    filterParams.grouponName = "";
    filterParams.status = 1;
    loadFilter();
};

const onSelectChange = (e: GrouponFilterState[]) => {
    if (e.length > 1) {
        let delRow = e.shift();
        tableRef?.value?.toggleRowSelection(delRow, false);
        selectedItems.value = e;
    } else {
        selectedItems.value = e;
    }
};

// 弹窗回调
const onFormSubmit = () => {
    emit("submitCallback", selectedItems.value);
};

defineExpose({
    onFormSubmit
});
</script>
<style lang="less" scoped>
.pagination-con {
    background: #fff;
}

:deep .hide-checkbox .el-table__header-wrapper .el-table__header .el-checkbox {
    display: none;
}
</style>
