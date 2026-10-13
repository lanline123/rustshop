/**
 * @file 库存日历列表
 * @description 商品库存日历管理，按日期查看和设置库存
 * @features
 * - 按商品名称搜索
 * - 按门店范围筛选库存
 * - 批量设置库存
 * - 日历视图展示每日库存
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="loadFilter">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('商品名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.productName"
                                                name="productName"
                                                :placeholder="$t('输入商品名称')"
                                                @keyup.enter="loadFilter"
                                                clearable
                                                @clear="loadFilter"
                                            >
                                                <template #append>
                                                    <el-button @click="loadFilter"><span class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label" style="width: 100px"><span>{{ $t('选择门店：') }}</span></label>
                                        <div class="control-container">
                                            <SelectStore
                                                v-model:storeId="selectedStoreId"
                                                class="store-select"
                                                :placeholder="$t('请选择门店')"
                                                @onChange="handleStoreChange"
                                            />
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="filter-item">
                                <div class="simple-form-field action-field">
                                    <div class="form-group total-group">
                                        <div class="total">{{ $t('共{total}条', { total }) }}<span v-show="selectedGoods.length">{{ $t(',已选择{length}条', { length: selectedGoods.length }) }}</span>
                                        </div>
                                        <div class="value action-btns">
                                            <el-space wrap>
                                                <DialogForm
                                                    :params="{
                                                        presetProductId: selectedGoods[0]?.productInfo?.productId ?? 0,
                                                        presetSkuIds: selectedGoods[0]?.productInfo?.skuId ? [selectedGoods[0].productInfo.skuId] : []
                                                    }"
                                                    path="product/inventoryCalendar/BatchSetInventory"
                                                    :title="$t('批量设置库存')"
                                                    :maskClose="false"
                                                    :showFooter="true"
                                                    :width="dialogWidth"
                                                    @callback="onBatchDialogCallback"
                                                    @okCallback="loadFilter"
                                                >
                                                    <el-button type="primary" v-if="getAdminType() === 'admin'">{{ $t('统一设置库存') }}</el-button>
                                                </DialogForm>

                                                <DialogForm
                                                    :params="{ productInfo: selectedGoods[0]?.productInfo ?? null, shopInfo: selectedGoods[0]?.shopInfo }"
                                                    path="product/inventoryCalendar/ModifyInventory"
                                                    :title="$t('单个设置库存')"
                                                    :maskClose="false"
                                                    :showFooter="true"
                                                    :width="dialogWidth"
                                                    @okCallback="loadFilter"
                                                >
                                                    <el-button :disabled="!selectedGoods.length" @click="checkSelection">{{ $t('按时段设置库存') }}</el-button>
                                                </DialogForm>
                                            </el-space>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field period-field">
                                    <el-space wrap class="period-space">
                                        <el-select v-model="filterParams.timeType" class="time-type-select" @change="changeTimeType">
                                            <el-option :label="$t('按周')" value="week" />
                                            <el-option :label="$t('按月')" value="month" />
                                        </el-select>
                                        <el-button :disabled="isPrevButtonDisabled" @click="changePeriod('prev')"
                                            >{{ $t('上一{timeTypeweek}', { timeTypeweek: filterParams.timeType == "week" ? "周" : "月" }) }}</el-button
                                        >
                                        <div class="time-box" v-if="filterParams.timeType == 'week'">
                                            <el-date-picker
                                                class="period-date-picker"
                                                value-format="YYYY-MM-DD"
                                                v-model="filterParams.startDate"
                                                type="date"
                                                :disabled-date="disabledWeekDate"
                                                @change="changeDatePeriod()"
                                            />
                                            <p>~</p>
                                            <el-date-picker
                                                disabled
                                                class="period-date-picker"
                                                value-format="YYYY-MM-DD"
                                                v-model="filterParams.endDate"
                                                type="date"
                                            />
                                        </div>
                                        <div class="time-box" v-if="filterParams.timeType == 'month'">
                                            <el-date-picker
                                                class="period-date-picker"
                                                value-format="YYYY-MM"
                                                v-model="filterParams.startDate"
                                                type="month"
                                                :disabled-date="disabledMonthDate"
                                            />
                                        </div>
                                        <el-button @click="changePeriod('next')">{{ $t('下一{timeTypeweek}', { timeTypeweek: filterParams.timeType == "week" ? "周" : "月" }) }}</el-button>
                                    </el-space>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-scroll-wrap">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            @select="selectRow"
                            ref="tableRef"
                            :highlight-current-row="false"
                            :cell-class-name="getCellClassName"
                            border
                        >
                            <el-table-column type="selection" :width="isMobile ? 40 : 35" align="center" class="check-head" fixed="left"> </el-table-column>
                            <el-table-column :label="$t('商品信息')" align="center" :width="isMobile ? 160 : 290" fixed="left">
                                <template #default="{ row }">
                                    <div class="product-info">
                                        <div class="product-image">
                                            <Image :src="imageFormat(row.productInfo.productImage)" fit="contain" :width="isMobile ? 32 : 40" :height="isMobile ? 32 : 40" />
                                        </div>
                                        <div class="product-name">{{ row.productInfo?.productName }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('规格')" align="center" :width="isMobile ? 90 : 180" :fixed="isMobile ? false : 'left'">
                                <template #default="{ row }">
                                    <span class="ellipsis">{{ formatSpecValue(row.productInfo?.specValue) }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('门店')" align="center" :width="isMobile ? 90 : 150" :fixed="isMobile ? false : 'left'">
                                <template #default="{ row }">
                                    <span class="ellipsis">{{ row.shopInfo?.shopName }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column
                                v-for="(day, index) in dayList"
                                :key="day"
                                align="center"
                                class="hover-box"
                                :width="dayColumnWidth"
                                :min-width="dayColumnMinWidth"
                            >
                                <template #header>
                                    <div class="day">
                                        <div v-if="filterParams.timeType == 'week'" class="week">{{ mapLabel(day) }}</div>
                                        <div class="date">{{ formatDay(day) }}</div>
                                    </div>
                                </template>
                                <template #default="{ row }">
                                    <div
                                        class="cell-box"
                                        :class="{ 'cell-box--touch': isMobile }"
                                        v-if="!getDayData(row, day)?.totalStock"
                                        @mouseenter="!isMobile && (buttonHoverStates[getButtonHoverKey('set', row, day)] = true)"
                                        @mouseleave="!isMobile && (buttonHoverStates[getButtonHoverKey('set', row, day)] = false)"
                                    >
                                        <DialogForm
                                            :params="{
                                                type: 'set',
                                                stockDate: day,
                                                shopId: row.shopInfo?.shopId,
                                                productId: row.productInfo?.productId,
                                                skuId: row.productInfo?.skuId
                                            }"
                                            path="product/inventoryCalendar/SetInventory"
                                            :title="$t('设置库存')"
                                            :maskClose="false"
                                            :showFooter="true"
                                            :width="dialogWidth"
                                            @okCallback="loadFilter"
                                        >
                                            <el-button
                                                :type="isCellActionActive('set', row, day) ? 'primary' : 'info'"
                                                :disabled="!isCellActionActive('set', row, day)"
                                                link
                                                :icon="isCellActionActive('set', row, day) ? Setting : ''"
                                            >
                                                <span>{{ isCellActionActive('set', row, day) ? $t('设置库存') : $t('未设置') }}</span>
                                            </el-button>
                                        </DialogForm>
                                    </div>
                                    <div
                                        class="cell-box"
                                        :class="{ 'cell-box--touch': isMobile }"
                                        v-else
                                        @mouseenter="!isMobile && (buttonHoverStates[getButtonHoverKey('edit', row, day)] = true)"
                                        @mouseleave="!isMobile && (buttonHoverStates[getButtonHoverKey('edit', row, day)] = false)"
                                    >
                                        <p>{{ $t('可预约数：{availableStock}', { availableStock: getDayData(row, day)?.availableStock }) }}</p>
                                        <p v-if="isMobile" class="used-stock">已预约数：{{ getDayData(row, day)?.usedStock ?? 0 }}</p>
                                        <DialogForm
                                            :params="{
                                                type: 'edit',
                                                stockDate: day,
                                                shopId: row.shopInfo?.shopId,
                                                productId: row.productInfo?.productId,
                                                skuId: row.productInfo?.skuId
                                            }"
                                            path="product/inventoryCalendar/SetInventory"
                                            :title="$t('修改库存')"
                                            :maskClose="false"
                                            :showFooter="true"
                                            :width="editDialogWidth"
                                            @okCallback="loadFilter"
                                        >
                                            <el-button
                                                :type="isCellActionActive('edit', row, day) ? 'primary' : 'info'"
                                                :disabled="!isCellActionActive('edit', row, day)"
                                                :icon="isCellActionActive('edit', row, day) ? Edit : ''"
                                                link
                                            >
                                                <span>
                                                    {{ isCellActionActive('edit', row, day)
                                                            ? $t('修改库存')
                                                            : `已预约数：${getDayData(row, day)?.usedStock ?? 0}` }}
                                                </span>
                                            </el-button>
                                        </DialogForm>
                                    </div>
                                </template>
                            </el-table-column>
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
import { Edit, Setting } from "@element-plus/icons-vue";
import "@/style/css/list.less";
import { Image } from "@/components/image";
import { imageFormat } from "@/utils/format";
import { Pagination, ProductCard } from "@/components/list";
import { computed, onMounted, onUnmounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { useConfigStore } from "@/store/config";
import { getAdminType } from "@/utils/storage";
import { isStore } from "@/utils/version";
import { getDays } from "@/utils/util";
import { formattedDate } from "@/utils/time";
import { getInventoryCalendarList } from "@/api/product/inventoryCalendar";
import { InventoryCalendarFilterParams, InventoryCalendarFilterDateStock } from "@/types/product/inventoryCalendar";
import { SelectStore } from "@/components/select";
import dayjs from "dayjs";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { productName } = useRouter().currentRoute.value.query;
const config: any = useConfigStore();
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedGoods = ref<any>([]);
const tableRef = ref();
// 为每个按钮单独管理 hover 状态
const buttonHoverStates = ref<Record<string, boolean>>({});
const dayList = ref<string[]>([]);
const selectedStoreId = ref<number | string>("");
const isMobile = ref(document.body.clientWidth < 768);
const dialogWidth = computed(() => (isMobile.value ? "95%" : "600px"));
const editDialogWidth = computed(() => (isMobile.value ? "95%" : "40%"));
const dayColumnWidth = computed(() => {
    if (isMobile.value) {
        return filterParams.value.timeType === "month" && dayList.value.length > 7 ? 120 : undefined;
    }
    return filterParams.value.timeType === "month" && dayList.value.length > 7 ? 240 : undefined;
});
const dayColumnMinWidth = computed(() => {
    if (isMobile.value) {
        return filterParams.value.timeType === "week" || dayList.value.length <= 7 ? 100 : undefined;
    }
    return filterParams.value.timeType === "week" || dayList.value.length <= 7 ? 120 : undefined;
});
const handleResize = () => {
    isMobile.value = document.body.clientWidth < 768;
};
const filterParams = ref<InventoryCalendarFilterParams>({
    page: 1,
    size: 10,
    shopScopeType: 0,
    shopIds: "",
    productName: "",
    timeType: "week", // week,month
    startDate: formattedDate(new Date(), "YYYY-MM-DD"),
    endDate: formattedDate(getDays(6, "add"), "YYYY-MM-DD")
});
const filterState = ref([]);
const settingDialog = ref({
    visible: false,
    title: t('设置库存'),
    type: "set"
});

// 判断上一周期按钮是否禁用
const isPrevButtonDisabled = computed(() => {
    if (filterParams.value.timeType === "week") {
        // 按周显示时，如果开始时间是今天，则禁用上一周按钮
        const today = formattedDate(new Date(), "YYYY-MM-DD");
        return filterParams.value.startDate === today;
    } else {
        // 按月显示时，如果开始时间是当月，则禁用上一月按钮
        const currentMonth = formattedDate(new Date(), "YYYY-MM");
        return filterParams.value.startDate === currentMonth;
    }
});

// 处理周期变更（上一周期/下一周期）
const changePeriod = (direction: "prev" | "next") => {
    if (filterParams.value.timeType === "week") {
        // 周模式处理
        const startDate = dayjs(filterParams.value.startDate);
        const offset = direction === "prev" ? -7 : 7;
        const newStartDate = startDate.add(offset, "day");
        filterParams.value.startDate = newStartDate.format("YYYY-MM-DD");
        filterParams.value.endDate = newStartDate.add(6, "day").format("YYYY-MM-DD");
    } else {
        // 月模式处理
        const startDate = dayjs(filterParams.value.startDate);
        const offset = direction === "prev" ? -1 : 1;
        const newStartDate = startDate.add(offset, "month");
        filterParams.value.startDate = newStartDate.format("YYYY-MM");
    }
    loadFilter();
};
// 禁用本周之前的日期
const disabledWeekDate = (date: any) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return date.getTime() < today.getTime();
};

// 禁用本月之前的月份
const disabledMonthDate = (date: any) => {
    const currentMonth = new Date();
    currentMonth.setDate(1); // 设置为当月第一天
    currentMonth.setHours(0, 0, 0, 0);
    return date.getTime() < currentMonth.getTime();
};

const changeTimeType = () => {
    if (filterParams.value.timeType == "week") {
        filterParams.value.startDate = formattedDate(new Date(), "YYYY-MM-DD");
        filterParams.value.endDate = formattedDate(getDays(6, "add"), "YYYY-MM-DD");
    } else {
        filterParams.value.startDate = formattedDate(new Date(), "YYYY-MM");
    }
    loadFilter();
};
//时间选择器时间发生变化时，重新加载数据
const changeDatePeriod = () => {
    if (filterParams.value.timeType == "week" && filterParams.value.startDate) {
        filterParams.value.endDate = dayjs(filterParams.value.startDate).add(6, "day").format("YYYY-MM-DD");
        loadFilter();
    }
};

const handleStoreChange = (storeId: number | string | number[]) => {
    const currentStoreId = Array.isArray(storeId) ? storeId[0] : storeId;
    const hasStoreSelected = currentStoreId !== undefined && currentStoreId !== null && currentStoreId !== "";
    selectedStoreId.value = hasStoreSelected ? currentStoreId : "";
    filterParams.value.shopIds = hasStoreSelected ? String(currentStoreId) : "";
    filterParams.value.shopScopeType = hasStoreSelected ? 1 : 0;
    loadFilter();
};
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getInventoryCalendarList({ ...filterParams.value });
        // 规范化每行数据，构建按日期快速索引的映射
        const normalizedRecords = result.records.map((record: any) => {
            const dayMap: any = {};
            record.dateStockList?.forEach((item: InventoryCalendarFilterDateStock) => {
                dayMap[item.stockDate] = item;
            });
            return { ...record, _dayMap: dayMap };
        });
        filterState.value = normalizedRecords;

        dayList.value = normalizedRecords[0]?.dateStockList.map((i: InventoryCalendarFilterDateStock) => i.stockDate);

        total.value = result.total;
        selectedGoods.value = [];
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

// 取某一行在指定日期的库存数据
const getDayData = (row: any, day: string) => {
    return row?._dayMap?.[day];
};

const getButtonHoverKey = (type: "set" | "edit", row: any, day: string) => {
    return `${type}-${row?.productInfo?.productId || 0}-${row?.productInfo?.skuId || 0}-${row?.shopInfo?.shopId || 0}-${day}`;
};

// H5 下始终可点；PC 仍按单元格 hover 切换操作文案
const isCellActionActive = (type: "set" | "edit", row: any, day: string) => {
    return isMobile.value || !!buttonHoverStates.value[getButtonHoverKey(type, row, day)];
};

// 根据索引返回周几（表头插槽用）
const mapLabel = (day: string) => {
    const weekDays = [t('周日'), t('周一'), t('周二'), t('周三'), t('周四'), t('周五'), t('周六')];
    return weekDays[dayjs(day).day()];
};

// 表头显示MM-DD
const formatDay = (day: string) => {
    return dayjs(day).format("MM-DD");
};

const formatSpecValue = (specValue?: string) => {
    if (!specValue) {
        return "-";
    }
    return specValue
        .split("|")
        .map((item) => item.trim())
        .filter(Boolean)
        .join(";");
};

// const onSelectChange = (selected: any) => {
//     selectedGoods.value = selected;
// };

const selectRow = (selection: any, val: any) => {
    tableRef.value.clearSelection();
    if (selection.length == 0) {
        selectedGoods.value = []; // 清空data中绑定的selectedRow
        return;
    }
    tableRef.value.toggleRowSelection(val, true);
    selectedGoods.value.length = 0;
    selectedGoods.value.push(val);
};

const syncSelectedGoodsByProduct = (payload: { productId?: number; skuId?: number; skuIds?: number[] }) => {
    const targetProductId = Number(payload?.productId || 0);
    if (targetProductId <= 0) {
        return;
    }
    const targetSkuId = Number(payload?.skuId || payload?.skuIds?.[0] || 0);
    const currentShopId = Number(selectedGoods.value?.[0]?.shopInfo?.shopId || 0);
    const matchedRows = filterState.value.filter((row: any) => Number(row?.productInfo?.productId) === targetProductId);
    if (!matchedRows.length) {
        tableRef.value?.clearSelection();
        selectedGoods.value = [];
        message.warning(t('当前列表中未找到该商品，已清空勾选项'));
        return;
    }
    const targetRow =
        (currentShopId > 0 &&
            matchedRows.find(
                (row: any) =>
                    Number(row?.shopInfo?.shopId) === currentShopId &&
                    (!targetSkuId || Number(row?.productInfo?.skuId) === targetSkuId)
            )) ||
        (targetSkuId > 0 && matchedRows.find((row: any) => Number(row?.productInfo?.skuId) === targetSkuId)) ||
        (currentShopId > 0 && matchedRows.find((row: any) => Number(row?.shopInfo?.shopId) === currentShopId)) ||
        matchedRows[0];
    tableRef.value?.clearSelection();
    tableRef.value?.toggleRowSelection(targetRow, true);
    selectedGoods.value = [targetRow];
};

const onBatchDialogCallback = (payload: any) => {
    if (payload?.type !== "productChange") {
        return;
    }
    syncSelectedGoodsByProduct(payload);
};

const isSelectable = (row: any, index: number) => {
    // 排除重复项
};

const checkSelection = () => {
    if (!selectedGoods.value || selectedGoods.value.length === 0) {
        message.error(t('请至少选择一项商品'));
        return false;
    }
    return true;
};

// 针对动态渲染的日期列应用hover效果
const getCellClassName = ({ row, column, rowIndex, columnIndex }: any): string => {
    if (columnIndex >= 4) {
        return "day-cell";
    }
    return "";
};
onMounted(() => {
    if (productName) {
        filterParams.value.productName = productName as string;
    }
    window.addEventListener("resize", handleResize);
    loadFilter();
});

onUnmounted(() => {
    window.removeEventListener("resize", handleResize);
});
</script>
<style scoped lang="less">
.filter-item {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;

    .total-group {
        display: flex;
        align-items: center;
        gap: 15px;
        color: #9598a6;
    }

    .time-box {
        display: flex;
        align-items: center;

        p {
            margin: 0 5px;
        }
    }
}

.time-type-select {
    width: 90px;
}

.period-date-picker {
    width: 160px;
}

.store-select {
    width: 180px;
}

.search-item {
    justify-content: start;
}

.content_wrapper {
    :deep(.el-select) {
        min-width: 50px !important;
    }
}

.day {
    display: flex;
    flex-direction: column;
    align-items: center;
    line-height: 16px;
    padding: 10px;
}

.product-info {
    display: flex;
    align-items: center;
    padding: 0 10px;
    .product-image {
        width: 40px;
        height: 40px;
        flex-shrink: 0;
    }
    .product-name {
        flex: 1;
        text-align: left;
        margin-left: 15px;
    }
}

//隐藏表头复选框
:deep(.el-table__header-wrapper) {
    .el-table-column--selection > .cell {
        display: none;
    }
}

// 动态列的单元格hover高亮效果
:deep(.day-cell) {
    transition: all 0.2s ease;
    cursor: pointer;
    position: relative;
    overflow: hidden;

    &::after {
        content: "";
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
        border: 2px solid transparent;
        border-radius: 4px;
        opacity: 0;
        transition: all 0.3s ease;
        z-index: 1;
        pointer-events: none;
    }

    &:hover {
        background-color: #f0f9ff !important;
        z-index: 10;

        &::after {
            opacity: 1;
            border-color: #409eff;
        }
    }
}

// 仅对表格最后一行：给高亮层留出底部空间，避免压到横向滚动条区域
:deep(.el-table__body tr:last-child td.day-cell::after) {
    display: block;
    height: calc(100% - 10px);
}

.cell-box {
    width: 100%;
    height: 58px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 10px 0;
}

:deep(.el-table__cell) {
    padding: 0 !important;
}
:deep(.el-table .cell) {
    padding: 0 !important;
}
.el-table td.el-table__cell div {
    box-sizing: unset;
}
.ellipsis {
    width: 90%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding: 0 15px;
}

// 避免单元格 hover 高亮层盖住 Element Plus 表格滚动条（导致滚动条无法拖拽/点击）
:deep(.el-table__body-wrapper) {
    .el-scrollbar__bar {
        z-index: 50;
    }
}

@media only screen and (max-width: 767px) {
    .list-table-tool {
        :deep(.simple-form-warp) {
            width: 100%;
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        :deep(.simple-form-field) {
            width: 100%;
            margin-right: 0;
        }

        :deep(.control-container) {
            width: 100%;
            padding-right: 0 !important;

            .el-input,
            .el-select {
                width: 100% !important;
            }
        }

        :deep(.control-label) {
            width: auto !important;
            margin-bottom: 4px;
        }
    }

    .filter-item {
        flex-direction: column;
        align-items: stretch;
        gap: 12px;

        .action-field,
        .period-field {
            width: 100%;
        }

        .total-group {
            flex-direction: column;
            align-items: flex-start;
            gap: 10px;
            width: 100%;
        }

        .action-btns {
            width: 100%;

            :deep(.el-space) {
                width: 100%;
                flex-wrap: wrap;
            }

            :deep(.el-button) {
                margin: 0;
            }
        }

        .period-space {
            width: 100%;
        }

        .time-box {
            flex-wrap: wrap;
            gap: 6px;
        }
    }

    .time-type-select {
        width: 88px;
    }

    .period-date-picker {
        width: 130px;
    }

    .store-select {
        width: 100%;
    }

    .table-scroll-wrap {
        width: 100%;
        overflow-x: auto;
        -webkit-overflow-scrolling: touch;
    }

    .product-info {
        padding: 0 6px;

        .product-image {
            width: 32px;
            height: 32px;
        }

        .product-name {
            margin-left: 8px;
            font-size: 12px;
            line-height: 1.3;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            white-space: normal;
            word-break: break-all;
        }
    }

    .day {
        padding: 6px 4px;
        font-size: 12px;
    }

    .cell-box {
        height: auto;
        min-height: 58px;
        padding: 8px 4px;
        font-size: 12px;

        p {
            margin: 0 0 4px;
            line-height: 1.3;
        }

        .used-stock {
            color: #9598a6;
        }

        &.cell-box--touch {
            :deep(.el-button) {
                pointer-events: auto;
            }
        }
    }

    .ellipsis {
        padding: 0 6px;
        font-size: 12px;
    }

    // 触屏无 hover，去掉日期格 hover 高亮层避免误挡点击
    :deep(.day-cell) {
        &:hover {
            background-color: transparent !important;

            &::after {
                opacity: 0;
            }
        }
    }

    .pagination-con {
        overflow-x: auto;
    }
}
</style>
