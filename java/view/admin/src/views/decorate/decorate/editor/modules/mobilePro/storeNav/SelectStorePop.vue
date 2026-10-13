/** * @file 门店导航 - 指定店铺选择弹窗 * @description 供装修编辑器「门店导航」模块挑选要展示的门店，支持关键词搜索、分页与数量上限 */
<template>
    <div class="select-store-content">
        <div class="list-table-form">
            <TigInput width="240px" v-model="filterParams.keyword" name="keyword" placeholder="请输入搜索关键字" @keyup.enter="onSearch" />
            <el-button type="primary" @click="onSearch">搜索</el-button>
        </div>
        <div class="table-container">
            <a-spin :spinning="loading">
                <el-table ref="tableRef" :data="records" row-key="shopId" @select="onSelect" @select-all="onSelectAll">
                    <el-table-column type="selection" width="48" reserve-selection />
                    <el-table-column label="门店名称" min-width="200">
                        <template #default="{ row }">
                            <div class="shop-name-cell">
                                <Image v-if="row.shopLogo" :src="row.shopLogo" fit="contain" class="shop-logo" />
                                <span>{{ row.shopTitle || "--" }}</span>
                            </div>
                        </template>
                    </el-table-column>
                    <template #empty>
                        <div class="empty-warp">
                            <div v-if="!loading" class="empty-bg">暂无数据</div>
                        </div>
                    </template>
                </el-table>
            </a-spin>
            <div v-if="total > 0" class="pagination-con">
                <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, reactive, onMounted, nextTick } from "vue";
import { message } from "ant-design-vue";
import { Image } from "@/components/image";
import { Pagination } from "@/components/list";
import { getShopList } from "@/api/shop/shop";
import { useConfigStore } from "@/store/config";
import { STORE_NAV_MAX_STORE, type StoreNavShopItem } from "./types";

const props = defineProps({
    /** 已选门店，用于回显 */
    selectedList: {
        type: Array as () => StoreNavShopItem[],
        default: () => []
    },
    /** 最多可选数量 */
    maxSelection: {
        type: Number,
        default: STORE_NAV_MAX_STORE
    }
});

const emit = defineEmits(["submitCallback", "update:confirmLoading"]);
const config: any = useConfigStore();
const tableRef = ref<any>(null);
const loading = ref<boolean>(true);
const records = ref<any[]>([]);
const total = ref<number>(0);
/** 弹窗内的选择结果，确认后才回写模块 */
const selectedList = ref<StoreNavShopItem[]>(props.selectedList.map((item) => ({ ...item })));

const filterParams = reactive<Record<string, any>>({
    shopType: 2,
    status: 1,
    page: 1,
    size: config.get("pageSize"),
    keyword: "",
    includeSelfOperated: 1
});

const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getShopList({ ...filterParams });
        records.value = result.records;
        total.value = result.total;
        nextTick(() => {
            syncTableSelection();
        });
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

/** 把当前页的行勾选状态与 selectedList 对齐，并回填 shopLogo */
const syncTableSelection = () => {
    if (!tableRef.value) return;
    records.value.forEach((row) => {
        const selected = selectedList.value.find((item) => item.shopId === row.shopId);
        if (selected) {
            if (row.shopLogo) selected.shopLogo = row.shopLogo;
            if (row.shopTitle) selected.shopTitle = row.shopTitle;
            tableRef.value.toggleRowSelection(row, true);
        } else {
            tableRef.value.toggleRowSelection(row, false);
        }
    });
};

const toStoreItem = (row: any, fallback?: StoreNavShopItem): StoreNavShopItem => ({
    shopId: row?.shopId ?? fallback?.shopId,
    shopTitle: row?.shopTitle || fallback?.shopTitle || "",
    shopLogo: row?.shopLogo || fallback?.shopLogo || ""
});

const addSelected = (row: any) => {
    if (selectedList.value.some((item) => item.shopId === row.shopId)) return true;
    if (selectedList.value.length >= props.maxSelection) {
        return false;
    }
    selectedList.value.push(toStoreItem(row));
    return true;
};

const onSelect = (selection: any[], row: any) => {
    if (selection.includes(row)) {
        if (!addSelected(row)) {
            message.warning(`最多支持${props.maxSelection}个门店`);
            nextTick(() => {
                tableRef.value?.toggleRowSelection(row, false);
            });
        }
        return;
    }
    const index = selectedList.value.findIndex((item) => item.shopId === row.shopId);
    if (index > -1) {
        selectedList.value.splice(index, 1);
    }
};

const onSelectAll = (selection: any[]) => {
    if (selection.length === 0) {
        records.value.forEach((row) => {
            const index = selectedList.value.findIndex((item) => item.shopId === row.shopId);
            if (index > -1) {
                selectedList.value.splice(index, 1);
            }
        });
        return;
    }
    const overflow = selection.filter((row) => !addSelected(row));
    if (overflow.length > 0) {
        message.warning(`最多支持${props.maxSelection}个门店`);
        nextTick(() => {
            overflow.forEach((row) => tableRef.value?.toggleRowSelection(row, false));
        });
    }
};

const onFormSubmit = () => {
    // 提交时用当前页记录补齐 shopLogo，确保写入装修数据
    const result = selectedList.value.map((item) => {
        const row = records.value.find((r) => r.shopId === item.shopId);
        return toStoreItem(row, item);
    });
    emit("submitCallback", result);
};

onMounted(() => {
    loadFilter();
});

defineExpose({
    onFormSubmit
});
</script>
<style lang="less" scoped>
.select-store-content {
    padding: 10px;

    .list-table-form {
        margin-bottom: 16px;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .table-container {
        min-height: 360px;
    }
}

.shop-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;
}

.shop-logo {
    width: 28px;
    height: 28px;
    flex-shrink: 0;
    border-radius: 4px;
}
</style>
