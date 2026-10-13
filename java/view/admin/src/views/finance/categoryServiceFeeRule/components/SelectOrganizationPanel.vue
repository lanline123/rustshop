<template>
    <div class="organization-panel">
        <el-row class="organization-content">
            <el-col :span="18">
                <div class="table-row">
                    <div class="list-table-form">
                        <TigInput v-model="filterParams.keyword" width="210px" name="keyword" :placeholder="$t('{organizationLabel}名称', { organizationLabel })" @keyup.enter="onSearch">
                            <template #append>
                                <el-button @click="onSearch"><span class="iconfont icon-chakan1"></span></el-button>
                            </template>
                        </TigInput>
                    </div>
                    <div class="table-container">
                        <a-spin :spinning="loading">
                            <el-table
                                ref="tableRef"
                                :data="tableData"
                                :loading="loading"
                                row-key="id"
                                @select="onSelect"
                                @select-all="onSelectAll"
                            >
                                <el-table-column type="selection" width="40" reserve-selection />
                                <el-table-column :label="$t('{organizationLabel}名称', { organizationLabel })" min-width="160">
                                    <template #default="{ row }">
                                        {{ row.name || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column v-if="props.tabKey !== 'supplier'" :label="$t('{organizationLabel}地址', { organizationLabel })" min-width="220">
                                    <template #default="{ row }">
                                        {{ row.address || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column v-else :label="$t('联系人')" min-width="160">
                                    <template #default="{ row }">
                                        {{ row.contactName || "--" }}
                                        <span v-if="row.contactMobile"> / {{ row.contactMobile }}</span>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('{organizationLabel}状态', { organizationLabel })" width="110">
                                    <template #default="{ row }">
                                        <span :class="row.enabled ? 'status-on' : 'status-off'">●</span>
                                        {{ row.statusText || (row.enabled ? $t('开启') : $t('关闭')) }}
                                    </template>
                                </el-table-column>
                                <template #empty>
                                    <div class="empty-warp">
                                        <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                    </div>
                                </template>
                            </el-table>
                        </a-spin>
                        <div v-if="total > 0" class="pagination-con">
                            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                        </div>
                    </div>
                </div>
            </el-col>
            <el-col :span="6">
                <div class="select-row">
                    <div class="select-num-row">
                        <div class="label">{{ $t('已选择{length}个{organizationLabel}:', { length: selectedOrganizations.length, organizationLabel }) }}</div>
                        <el-button v-if="selectedOrganizations.length > 0" link type="primary" @click="clearSelected">{{ $t('清空') }}</el-button>
                    </div>
                    <div v-if="selectedOrganizations.length > 0" class="select-list">
                        <div v-for="item in selectedOrganizations" :key="item.id" class="select-item">
                            <div class="title">{{ item.name || "--" }}</div>
                            <div class="del-btn" @click="removeSelected(item)">
                                <el-icon color="#fb0606">
                                    <RemoveFilled />
                                </el-icon>
                            </div>
                        </div>
                    </div>
                    <div v-else class="empty">
                        <el-empty :image-size="80" :description="$t('暂无选中{organizationLabel}', { organizationLabel })" />
                    </div>
                </div>
            </el-col>
        </el-row>
    </div>
</template>

<script lang="ts" setup>
import { RemoveFilled } from "@element-plus/icons-vue";
import { computed, nextTick, onMounted, reactive, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { getShopList } from "@/api/shop/shop";
import { getSuppliersList } from "@/api/vendor/suppliers";
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { isStore } from "@/utils/version";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface OrganizationOption {
    id: number;
    name: string;
}

interface OrganizationRow extends OrganizationOption {
    address?: string;
    contactName?: string;
    contactMobile?: string;
    enabled?: boolean;
    statusText?: string;
}

type FeeRuleTabKey = "store" | "supplier";

const props = defineProps({
    tabKey: { type: String as () => FeeRuleTabKey, default: "store" }
});

const selectedOrganizations = defineModel<OrganizationOption[]>({ default: () => [] });

const config = useConfigStore();
const tableRef = ref();
const loading = ref(false);
const tableData = ref<OrganizationRow[]>([]);
const total = ref(0);
const filterParams = reactive({
    page: 1,
    size: config.get("pageSize"),
    keyword: ""
});

const organizationLabel = computed(() => {
    if (props.tabKey === "supplier") return t('供应商');
    return isStore() ? t('门店') : t('店铺');
});

const isRowSelected = (row: OrganizationRow) => selectedOrganizations.value.some((item) => item.id === row.id);

const getShopStatusText = (row: any) => row.statusText || row.shopStatusText || (row.status === 1 || row.shopStatus === 1 ? t('开业') : t('关闭'));

const mapShopRow = (row: any): OrganizationRow => ({
    id: Number(row.shopId),
    name: row.shopTitle || row.storeTitle || String(row.shopId),
    address: `${(row.shopRegionNames || []).join("")}${row.shopDetailedAddress || ""}`,
    enabled: row.status === 1 || row.shopStatus === 1,
    statusText: getShopStatusText(row)
});

const mapSupplierRow = (row: any): OrganizationRow => ({
    id: Number(row.vendorId),
    name: row.vendorName || String(row.vendorId),
    contactName: row.contactName,
    contactMobile: row.contactMobile,
    enabled: row.status === 1,
    statusText: row.status === 1 ? t('开启') : t('关闭')
});

const setTableSelection = () => {
    if (!tableRef.value) return;
    tableRef.value.clearSelection();
    tableData.value.forEach((row) => {
        if (isRowSelected(row)) {
            tableRef.value.toggleRowSelection(row, true);
        }
    });
};

const loadFilter = async () => {
    loading.value = true;
    try {
        if (props.tabKey === "supplier") {
            const result = await getSuppliersList({ ...filterParams } as any);
            tableData.value = (result.records || []).map(mapSupplierRow);
            total.value = result.total || 0;
        } else {
            const result = await getShopList({ ...filterParams, shopType: isStore() ? 2 : 1, select: 1 } as any);
            tableData.value = (result.records || []).map(mapShopRow);
            total.value = result.total || 0;
        }
        nextTick(setTableSelection);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onSearch = () => {
    filterParams.page = 1;
    loadFilter();
};

const onSelect = (selection: OrganizationRow[], row: OrganizationRow) => {
    const isSelected = selection.some((item) => item.id === row.id);
    const selectedIndex = selectedOrganizations.value.findIndex((item) => item.id === row.id);
    if (isSelected && selectedIndex === -1) {
        selectedOrganizations.value.push({ id: row.id, name: row.name });
    }
    if (!isSelected && selectedIndex > -1) {
        selectedOrganizations.value.splice(selectedIndex, 1);
    }
};

const onSelectAll = (selection: OrganizationRow[]) => {
    if (selection.length === 0) {
        tableData.value.forEach((row) => {
            const index = selectedOrganizations.value.findIndex((item) => item.id === row.id);
            if (index > -1) {
                selectedOrganizations.value.splice(index, 1);
            }
        });
        return;
    }
    selection.forEach((row) => {
        if (!isRowSelected(row)) {
            selectedOrganizations.value.push({ id: row.id, name: row.name });
        }
    });
};

const removeSelected = (item: OrganizationOption) => {
    const index = selectedOrganizations.value.findIndex((selected) => selected.id === item.id);
    if (index > -1) {
        selectedOrganizations.value.splice(index, 1);
        const row = tableData.value.find((current) => current.id === item.id);
        if (row) {
            tableRef.value?.toggleRowSelection(row, false);
        }
    }
};

const clearSelected = () => {
    selectedOrganizations.value = [];
    tableRef.value?.clearSelection();
};

watch(
    () => props.tabKey,
    () => {
        selectedOrganizations.value = [];
        filterParams.page = 1;
        filterParams.keyword = "";
        loadFilter();
    }
);

watch(
    () => selectedOrganizations.value,
    () => {
        nextTick(setTableSelection);
    },
    { deep: true }
);

onMounted(() => {
    loadFilter();
});
</script>

<style lang="less" scoped>
.organization-panel {
    width: 820px;
    border: 1px solid #ebeef5;
}

.organization-content {
}

.table-row {
    max-height: 500px;
    overflow-y: auto;
    padding: 10px;
    border-right: 1px solid #e4e4e4;

    &::-webkit-scrollbar {
        display: none;
    }
}

.list-table-form {
    margin-bottom: 20px;
}

.select-row {
    max-height: 500px;
    overflow-y: auto;
    padding: 10px;

    &::-webkit-scrollbar {
        display: none;
    }
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

.status-on {
    color: #52c41a;
    margin-right: 4px;
}

.status-off {
    color: #f5222d;
    margin-right: 4px;
}
</style>
