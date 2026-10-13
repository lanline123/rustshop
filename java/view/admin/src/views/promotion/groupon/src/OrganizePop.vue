<template>
    <div>
        <el-row class="orfanize-content">
            <el-col :span="3">
                <div class="tabs-row">
                    <div class="tabs" v-for="item in tabs" :class="{ active: item.value === currentTab }"
                        @click="currentTab = item.value">
                        {{ item.name }}
                    </div>
                </div>
            </el-col>
            <el-col :span="15">
                <div class="table-row">
                    <div class="list-table-form" v-if="currentTab === 2">
                        <div class="form-tool-row">
                            <TigInput width="210px" v-model="filterParams.keyword" name="keyword" :placeholder="$t('门店名称')">
                                <template #append>
                                    <el-button @click="loadFilter"><span
                                            class="iconfont icon-chakan1"></span></el-button>
                                </template>
                            </TigInput>
                        </div>
                        <div class="form-tool-row">
                            <SslectArea v-model:areaStoreManagerId="filterParams.areaStoreManagerId"
                                @onChange="loadFilter"></SslectArea>
                        </div>
                        <div class="form-tool-row">
                            <SelectRegion v-model:selectedIds="shopRegionIds" @change="onRegionChange" @clear="onClear"
                                style="width: 210px; margin-bottom: 10px"></SelectRegion>
                        </div>
                    </div>
                    <div class="table-container">
                        <a-spin :spinning="loading">
                            <el-table v-show="currentTab === 1" ref="tableRefOne" :data="tableData" :loading="loading"
                                :total="total" row-key="shopId" @selection-change="onSelectChange" @select="onSelect"
                                @select-all="onSelectAll">
                                <el-table-column type="selection" width="32" reserve-selection />
                                <el-table-column :label="$t('组织名称')">
                                    <template #default="{ row }">
                                        {{ row.shopTitle || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('编号')">
                                    <template #default="{ row }"> -- </template>
                                </el-table-column>
                            </el-table>
                            <el-table ref="tableRef" v-show="currentTab === 2" :data="filterState" :loading="loading"
                                :total="total" row-key="shopId" @selection-change="onSelectChange" @select="onSelect"
                                @select-all="onSelectAll">
                                <el-table-column type="selection" width="32" reserve-selection />
                                <el-table-column :label="$t('门店名称')" width="200">
                                    <template #default="{ row }">
                                        {{ row.shopTitle || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('门店地址')">
                                    <template #default="{ row }">
                                        <span v-if="row.shopRegionNames">
                                            {{ row.shopRegionNames.join("") }}
                                        </span>
                                        {{ row.shopDetailedAddress || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('门店状态')" width="100">
                                    <template #default="{ row }">
                                        <template v-if="row.status == 10">
                                            <StatusDot color="red" :flicker="true"></StatusDot>
                                        </template>
                                        <template v-if="row.status == 4">
                                            <StatusDot color="red" :flicker="true"></StatusDot>
                                        </template>
                                        <template v-if="row.status == 1">
                                            <StatusDot color="green" :flicker="true"></StatusDot>
                                        </template>
                                        <span v-if="row.status === 10 || row.status === 4" style="color: red">{{
                                            row.statusText }}</span>
                                        <span v-else-if="row.status === 1" style="color: green">{{ row.statusText
                                        }}</span>
                                    </template>
                                </el-table-column>
                                <template #empty>
                                    <div class="empty-warp">
                                        <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                    </div>
                                </template>
                            </el-table>
                        </a-spin>
                        <div v-if="total > 0 && currentTab === 2" class="pagination-con">
                            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                                @callback="loadFilter" />
                        </div>
                    </div>
                </div>
            </el-col>
            <el-col :span="6">
                <div class="select-row">
                    <div class="select-num-row">
                        <div class="lable">{{ $t('已选择{length}个门店:', { length: selectedIds.length }) }}</div>
                        <div class="btn" v-if="selectedIds.length > 0">
                            <el-button link type="primary" @click="clearSelected">{{ $t('清空') }}</el-button>
                        </div>
                    </div>
                    <div class="select-list" v-if="selectedIds.length > 0">
                        <div class="select-item" v-for="item in selectedIds" :key="item.shopId">
                            <div class="title">{{ item.shopTitle || "--" }}</div>
                            <div class="del-btn" @click="removeSelected(item)">
                                <el-icon color="#fb0606">
                                    <RemoveFilled />
                                </el-icon>
                            </div>
                        </div>
                    </div>
                    <div class="empty" v-else>
                        <el-empty :image-size="80" :description="$t('暂无选中门店')" />
                    </div>
                </div>
            </el-col>
        </el-row>
    </div>
</template>

<script lang="ts" setup>
import { RemoveFilled } from "@element-plus/icons-vue";
import StatusDot from "@/components/form/src/StatusDot.vue";
import { ref, reactive, onMounted, watch, nextTick } from "vue";
import { ShopFilterParams, ShopFilterState } from "@/types/shop/shop.d";
import { getShopList, getPlatformAllocation, getStoreListByProductId } from "@/api/shop/shop";
import { useConfigStore } from "@/store/config";
import { message } from "ant-design-vue";
import { SelectRegion } from "@/components/select";
import { Pagination } from "@/components/list";
import { SslectArea } from "@/components/select";
import { allocationProduct } from "@/api/product/product";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tableRef = ref<any>(null);
const tableRefOne = ref<any>(null);
const props = defineProps({
    selectedIds: {
        type: Array,
        default: []
    },
    productId: {
        type: String,
        default: ""
    },
    type: {
        type: String,
        default: ""
    }
});

const tabs = ref([
    {
        name: t('商城'),
        value: 1
    },
    {
        name: t('门店'),
        value: 2
    }
]);

const tableData = [
    {
        shopId: 0,
        shopTitle: t('商城自营')
    }
];

const currentTab = ref(1);
const filterState = ref<ShopFilterState[]>([]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<any[]>(props.selectedIds || []);
const emit = defineEmits(["submitCallback", "okType", "update:confirmLoading"]);
const config: any = useConfigStore();

const filterParams = reactive<ShopFilterParams>({
    shopType: 1,
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: "",
    areaStoreManagerId: "",
    shopRegionId: ""
});

// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getShopList({ ...filterParams, productId: props.productId });
        filterState.value = result.records;
        total.value = result.total;
        nextTick(() => {
            setTableSelection();
        });
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const _getPlatformAllocation = async () => {
    try {
        selectedIds.value = [];
        const result = await getPlatformAllocation({ shopId: 0, productId: props.productId });
        if (result == true) {
            selectedIds.value.push({
                shopId: 0,
                shopTitle: t('商城自营')
            });
        }
        nextTick(() => {
            setTableSelectionOne(selectedIds.value[0]);
        });
    } catch (error: any) {
        message.error(error.message);
    }
};

const _getStoreListByProductId = async () => {
    try {
        const result = await getShopList({ productId: props.productId });
        if (result.code == 0 && result.records.length > 0) {
            result.records.forEach((item: any) => {
                selectedIds.value.push({
                    shopId: item.shopId,
                    shopTitle: item.shopTitle
                });
            });
        }

        await loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};

onMounted(() => {
    // _getPlatformAllocation();
    _getStoreListByProductId();
});

// 处理单个选中/取消选中
const onSelect = (selection: any[], row: any) => {
    // 如果row被选中，则添加到selectedIds；如果取消选中，则从selectedIds中移除
    const isSelected = selection.includes(row);
    if (isSelected) {
        // 选中
        if (!selectedIds.value.some((item) => item.shopId === row.shopId)) {
            selectedIds.value.push({
                shopId: row.shopId,
                shopTitle: row.shopTitle
            });
        }
    } else {
        // 取消选中
        const index = selectedIds.value.findIndex((item) => item.shopId === row.shopId);
        if (index > -1) {
            selectedIds.value.splice(index, 1);
        }
    }
};

// 处理全选/取消全选
const onSelectAll = (selection: any[]) => {
    // selection是当前页选中的所有行，如果全选，则selection包含当前页所有行；如果取消全选，则为空数组
    if (selection.length === 0) {
        // 取消全选：从selectedIds中移除当前页的所有行
        filterState.value.forEach((row) => {
            const index = selectedIds.value.findIndex((item) => item.shopId === row.shopId);
            if (index > -1) {
                selectedIds.value.splice(index, 1);
            }
        });
    } else {
        // 全选：将当前页的所有行添加到selectedIds（去重）
        selection.forEach((row) => {
            if (!selectedIds.value.some((item) => item.shopId === row.shopId)) {
                selectedIds.value.push({
                    shopId: row.shopId,
                    shopTitle: row.shopTitle
                });
            }
        });
    }
};

// selection-change事件可能仍然需要，但我们现在不再用它来更新selectedIds
const onSelectChange = (selection: any[]) => {
    // 这个事件在选中状态变化时触发，包括编程设置选中状态时也会触发
};

const removeSelected = (item: any) => {
    const index = selectedIds.value.findIndex((selected) => selected.shopId === item.shopId);
    if (index > -1) {
        selectedIds.value.splice(index, 1);
        // 如果当前页有该行，则同时更新表格的选中状态
        const row = filterState.value.find((row) => row.shopId === item.shopId);
        if (row && tableRef.value) {
            tableRef.value.toggleRowSelection(row, false);
        }
    }
    if (item.shopId === 0) {
        if (tableRefOne.value) {
            tableRefOne.value.clearSelection();
        }
    }
};

const clearSelected = () => {
    selectedIds.value = [];
    if (tableRef.value) {
        tableRef.value.clearSelection();
    }
    if (tableRefOne.value) {
        tableRefOne.value.clearSelection();
    }
};

const shopRegionIds = ref<any[]>([]);
const onRegionChange = (regionIds: any[]) => {
    shopRegionIds.value = regionIds;
    if (regionIds.length > 0) {
        filterParams.shopRegionId = regionIds[regionIds.length - 1].regionId;
    }
    loadFilter();
};

const onClear = () => {
    filterParams.shopRegionId = "";
    loadFilter();
};
const onSubmit = async () => {
    if (selectedIds.value.length === 0) {
        message.error(t('请选择门店'));
        return;
    }
    try {
        emit("update:confirmLoading", true);
        emit("submitCallback", selectedIds.value);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
const onFormSubmit = () => {
    onSubmit();
};

// 设置表格的选中状态
const setTableSelection = () => {
    if (!tableRef.value) return;

    // 清除当前页的所有选中状态
    tableRef.value.clearSelection();

    // 遍历当前页数据，如果该行在selectedIds中，则设置为选中
    filterState.value.forEach((row) => {
        const isSelected = selectedIds.value.some((item) => item.shopId === row.shopId);
        if (isSelected) {
            tableRef.value.toggleRowSelection(row, true);
        }
    });
};
// 设置表格的选中状态
const setTableSelectionOne = (item: any) => {
    if (!tableRefOne.value) return;
    // 清除当前页的所有选中状态
    tableRefOne.value.clearSelection();
    tableRefOne.value.toggleRowSelection(item, true);
};

defineExpose({
    onFormSubmit
});
</script>

<style lang="less" scoped>
.orfanize-content {
    .tabs-row {
        height: calc(100vh - 126px);
        border-right: 1px solid #e4e4e4;
        padding-left: 10px;
        padding-top: 20px;

        .tabs {
            padding: 10px 15px;
            cursor: pointer;
        }

        .active {
            color: var(--tig-primary);
            background-color: var(--tig-item-active-bg);
            font-weight: 600;
        }
    }

    .table-row {
        height: calc(100vh - 126px);
        overflow-y: auto;
        padding: 10px;
        border-right: 1px solid #e4e4e4;

        &::-webkit-scrollbar {
            display: none;
        }

        .list-table-form {
            margin-bottom: 20px;
            display: flex;
            gap: 10px;
        }
    }

    .select-row {
        height: calc(100vh - 126px);
        overflow-y: auto;
        padding: 10px;

        &::-webkit-scrollbar {
            display: none;
        }

        .select-num-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 10px 0 20px 0;
        }

        .select-list {
            .select-item {
                display: flex;
                align-items: center;
                justify-content: space-between;
                color: var(--tig-primary);
                background-color: var(--tig-item-active-bg);
                padding: 12px;
                margin-bottom: 10px;
            }
        }
    }
}
</style>
