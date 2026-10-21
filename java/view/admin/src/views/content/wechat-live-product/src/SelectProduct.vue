<template>
    <div class="container" style="position: relative; height: 100%">
        <div class="content_wrapper" style="width: 100%; height: 100%; position: absolute">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp" style="flex: 0; padding-right: 15px">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput v-model="filterParams.keyword" width="230px" :placeholder="$t('商品名称/商品ID')" @keyup.enter="onSearchSubmit" />
                                    <el-select v-model="filterParams.auditStatus" clearable :placeholder="$t('审核状态')" style="width: 140px" @change="onSearchSubmit">
                                        <el-option :value="0" :label="$t('待审核')" />
                                        <el-option :value="1" :label="$t('审核中')" />
                                        <el-option :value="2" :label="$t('审核通过')" />
                                        <el-option :value="3" :label="$t('审核驳回')" />
                                    </el-select>
                                    <el-button @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                    <el-button @click="resetParams">{{ $t('重置') }}</el-button>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <perfect-scrollbar class="table-container" style="flex: 1">
                    <a-spin :spinning="loading">
                        <div style="padding-right: 15px; box-sizing: border-box">
                            <el-table
                                ref="tableRef"
                                :data="filterState"
                                :loading="loading"
                                :total="total"
                                class="result-table"
                                row-key="id"
                                @selection-change="onSelectChange"
                            >
                                <el-table-column :selectable="isSelectable" type="selection" width="32" />
                                <el-table-column :label="$t('商品信息')" min-width="280">
                                    <template #default="{ row }">
                                        <div class="item-info">
                                            <Image :src="row.productImage" fit="cover" style="width: 40px; height: 40px; border-radius: 4px" />
                                            <div class="item-info__text">
                                                <div class="item-title">{{ row.productName }}</div>
                                                <div class="item-sub">{{ $t('商品ID：{productId}', { productId: row.productId }) }}</div>
                                            </div>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('价格')" width="110">
                                    <template #default="{ row }">
                                        <span class="price">¥{{ Number(row.price || 0).toFixed(2) }}</span>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('状态')" width="120">
                                    <template #default="{ row }">
                                        <el-tag :type="getAuditStatusType(row.auditStatus)">
                                            {{ row.auditStatus === 2 ? $t('通过') : row.auditStatus === 3 ? $t('驳回') : row.auditStatusText || "-" }}
                                        </el-tag>
                                    </template>
                                </el-table-column>
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
            <div class="selected-container">
                <el-tabs v-model="currentTab" tab-position="top">
                    <el-tab-pane :name="1" :label="tabName"></el-tab-pane>
                </el-tabs>
                <div class="product-list">
                    <div v-for="(item, index) in selectedRows" :key="item.id" class="product-item">
                        <div class="product-content">
                            <div class="product-image">
                                <Image :src="item.productImage" fit="cover" style="width: 60px; height: 60px" />
                            </div>
                            <div class="product-info">
                                <div class="product-title">{{ item.productName }}</div>
                                <div class="product-sku">{{ $t('商品ID：{productId}', { productId: item.productId }) }}</div>
                            </div>
                            <div class="delete-icon" @click="removeSelectedProduct(index)">
                                <el-icon><Delete /></el-icon>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { computed, nextTick, onMounted, ref } from "vue";
import { Delete } from "@element-plus/icons-vue";
import { Pagination } from "@/components/list";
import { Image } from "@/components/image";
import { useConfigStore } from "@/store/config";
import { message } from "ant-design-vue";
import { getWechatLiveProductList } from "@/api/content/wechatLiveProduct";
import type { WechatLiveProductFilterParams, WechatLiveProductState } from "@/types/content/wechatLiveProduct.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    selectedIds: {
        type: Array as () => number[],
        default: () => []
    },
    isMultiple: {
        type: Boolean,
        default: true
    }
});

const emit = defineEmits(["submitCallback", "okType"]);
emit("okType", false);

const config: any = useConfigStore();
const loading = ref(true);
const total = ref(0);
const tableRef = ref();
const filterState = ref<WechatLiveProductState[]>([]);
const selectedRows = ref<WechatLiveProductState[]>([]);
const isPaginationChange = ref(false);
const currentTab = ref(1);
const filterParams = ref<WechatLiveProductFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    keyword: "",
    auditStatus: undefined,
    liveRoomId: undefined,
    onSaleStatus: undefined,
    sortField: "",
    sortOrder: ""
});

const tabName = computed(() => {
    return selectedRows.value.length > 0 ? t('已选择商品({length})', { length: selectedRows.value.length }) : t('已选择商品');
});

const getAuditStatusType = (status: number) => {
    const typeMap: Record<number, string> = {
        0: "warning",
        1: "primary",
        2: "success",
        3: "danger"
    };
    return typeMap[status] || "info";
};

const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getWechatLiveProductList(filterParams.value);
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message || t('加载直播商品失败'));
    } finally {
        await initChecked();
        loading.value = false;
    }
};

const onSearchSubmit = () => {
    filterParams.value.page = 1;
    loadFilter();
};

const resetParams = () => {
    filterParams.value = {
        page: 1,
        size: config.get("pageSize"),
        keyword: "",
        auditStatus: undefined,
        liveRoomId: undefined,
        onSaleStatus: undefined,
        sortField: "",
        sortOrder: ""
    };
    loadFilter();
};

const initChecked = async () => {
    isPaginationChange.value = true;
    await nextTick();
    if (!tableRef.value) {
        isPaginationChange.value = false;
        return;
    }
    filterState.value.forEach((item) => {
        const isSelected = selectedRows.value.some((selected) => selected.id === item.id);
        tableRef.value.toggleRowSelection(item, isSelected);
    });
    isPaginationChange.value = false;
};

const onSelectChange = (rows: WechatLiveProductState[]) => {
    if (isPaginationChange.value) return;

    const currentPageIds = filterState.value.map((item) => item.id);
    selectedRows.value = selectedRows.value.filter((item) => !currentPageIds.includes(item.id));
    selectedRows.value.push(...rows);
    emit("okType", selectedRows.value.length > 0);
};

const isSelectable = (row: WechatLiveProductState) => {
    return !props.selectedIds.includes(row.id);
};

const removeSelectedProduct = (index: number) => {
    const removedRow = selectedRows.value[index];
    selectedRows.value.splice(index, 1);

    if (tableRef.value) {
        const row = filterState.value.find((item) => item.id === removedRow.id);
        if (row) {
            tableRef.value.toggleRowSelection(row, false);
        }
    }

    emit("okType", selectedRows.value.length > 0);
};

const onFormSubmit = () => {
    emit("submitCallback", selectedRows.value);
};

onMounted(() => {
    loadFilter();
});

defineExpose({
    onFormSubmit
});
</script>

<style lang="less" scoped>
.content_wrapper {
    display: flex;
    gap: 20px;

    .lyecs-table-list-warp {
        flex: 1;
        margin-bottom: 0;
    }

    .selected-container {
        width: 430px;
        background: #f5f7fa;
        padding: 12px 20px;
        box-sizing: border-box;

        .product-list {
            max-height: calc(100% - 54px);
            overflow-y: auto;

            .product-item {
                margin-bottom: 10px;
                background: #fff;
                border-radius: 4px;
                padding: 10px;
            }
        }
    }
}

.item-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.item-info__text {
    min-width: 0;
}

.item-title {
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.item-sub {
    margin-top: 4px;
    color: #999;
    font-size: 12px;
}

.price {
    color: #ff4d4f;
    font-weight: 500;
}

.product-content {
    display: flex;
    align-items: center;
}

.product-image {
    margin-right: 10px;
}

.product-info {
    flex: 1;
    min-width: 0;
}

.product-title {
    font-weight: 500;
    margin-bottom: 5px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.product-sku {
    font-size: 12px;
    color: #666;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.delete-icon {
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #999;

    &:hover {
        color: #f56c6c;
    }
}
</style>
