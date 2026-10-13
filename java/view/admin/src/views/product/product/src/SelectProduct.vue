<template>
    <div class="container" style="position: relative; height: 100%">
        <div class="content_wrapper">
            <div class="" style="display: flex; flex-direction: column; width: 100%; height: 100%; position: absolute">
                <div class="list-table-tool lyecs-search-warp" style="flex: 0">
                    <el-form :model="filterParams" name="form" @finish="searchSubmit" @keyup.enter="searchSubmit" @submit.prevent>
                        <div class="list-table-tool-row" style="margin-bottom: 5px">
                            <div class="list-table-tool-col search-wrap">
                                <div v-if="inventoryFilterParams?.page" class="search-row">
                                    <TigInput class="search-input" v-model="filterParams.keyword" :placeholder="$t('请搜索关键字')" />
                                    <el-button class="btn" @click="searchSubmit">{{ $t('搜索') }}</el-button>
                                </div>
                                <div v-else class="search-row">
                                    <SelectCategory v-model:categoryId="filterParams.categoryId" class="search-category" />
                                    <!-- <SelectBrand v-model:brandId="filterParams.brandId"></SelectBrand> -->
                                    <el-select v-model="filterParams.productStatus" :placeholder="$t('选择状态')" class="search-status" @change="searchSubmit">
                                        <el-option :label="$t('上架')" :value="1"></el-option>
                                        <el-option :label="$t('下架')" :value="0"></el-option>
                                    </el-select>
                                    <TigInput class="search-keyword" v-model="filterParams.keyword" :placeholder="$t('商品名称/货号')" />
                                    <el-button class="btn" @click="searchSubmit">{{ $t('搜索') }}</el-button>
                                </div>
                            </div>
                            <div class="list-table-tool-col"></div>
                        </div>
                    </el-form>
                </div>
                <perfect-scrollbar class="table-container" style="flex: 1">
                    <a-spin :spinning="loading">
                        <div style="padding-right: 15px; box-sizing: border-box">
                            <el-table
                                ref="tableRef"
                                :class="isMultiple ? '' : 'hide-checkbox'"
                                :data="filterState"
                                :loading="loading"
                                :total="total"
                                class="result-table"
                                :row-key="idKey"
                                @select="selectRow"
                                @select-all="handleSelectAll"
                                @selection-change="onSelectChange"
                            >
                                <el-table-column :selectable="isSelectable" :reserve-selection="true" type="selection" width="32" />
                                <el-table-column :label="$t('商品信息')" prop="productId">
                                    <template #default="{ row }">
                                        <div class="flex">
                                            <div v-if="row.picThumb || row.productPicThumb" class="span-pic">
                                                <a :href="row.url" target="_blank"
                                                    ><img :src="imageFormat(row.picThumb || row.productPicThumb)" height="40" width="40"
                                                /></a>
                                            </div>
                                            <div>
                                                <p>{{ row.productName }}</p>
                                                <p class="gray" v-if="row.productSn">{{ $t('货号：{productSn}', { productSn: row.productSn }) }}</p>
                                            </div>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('商品规格')" prop="productSkuName" v-if="inventoryFilterParams.page" width="150"></el-table-column>
                                <el-table-column :label="$t('规格')" prop="skuId" v-if="isSku">
                                    <template #default="{ row }">
                                        <div class="flex">
                                            <p v-if="row.skuIds && row.skuIds.length > 0">{{ $t('已选：{length}个规格', { length: row.skuIds.length }) }}<!-- <a @click="row.skuIds.length = 0">清空</a> -->
                                            </p>
                                            <p v-else>--</p>
                                        </div>
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
        </div>
    </div>
    <el-dialog v-model="skuVisible" :title="$t('选择规格')" width="500" :before-close="closeSku" :close-on-click-modal="false">
        <SelectSku v-if="skuVisible" ref="skuRef" :productInfo="rowInfo" :isMultiple="isMultiple"></SelectSku>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="closeSku">{{ $t('取消') }}</el-button>
                <el-button type="primary" @click="submitSku">{{ $t('保存') }}</el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { SelectBrand, SelectCategory } from "@/components/select";
import { nextTick, onMounted, reactive, ref, watch, computed } from "vue";
import { message } from "ant-design-vue";
import { imageFormat } from "@/utils/format";
import { Pagination } from "@/components/list";
import { ProductFilterParams, ProductFilterState } from "@/types/product/product";
import { useConfigStore } from "@/store/config";
import { getProductList } from "@/api/product/product";
import SelectSku from "@/views/promotion/productGift/src/SelectSku.vue";
import { isOverseas, isStore } from "@/utils/version";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    // 已选择的项，用于排除重复，禁止选择
    selectedIds: { type: Array, default: [] },
    modelValue: {
        type: Object,
        default: {}
    },
    isMultiple: {
        type: Boolean,
        default: true
    },
    // 是否选择规格, 默认单选
    isSku: {
        type: Boolean,
        default: false
    },
    //是否自营商品
    isSelf: {
        type: Number,
        default: 0
    },
    //是否是预约类商品
    reservationProduct: {
        type: Number,
        default: 0
    },
    apiList: {
        type: Function,
        default: getProductList
    },
    // 库存查询参数
    inventoryFilterParams: {
        type: Object,
        default: () => {
            return {};
        }
    },
    // 是否过滤预售商品
    isFilterPresaleProduct: {
        type: Boolean,
        default: false
    },
    // 其他参数
    otherParams: {
        type: Object,
        default: () => {
            return {};
        }
    },
    // 默认选中的商品ID列表（多选模式下初始勾选）
    defaultSelectedIds: {
        type: Array,
        default: () => []
    },
    // 已选商品对应的规格 ID，用于打开选择器时恢复多规格勾选
    defaultSelectedSkuIds: {
        type: Object,
        default: () => ({})
    },
    // 装修选品：列表查询不带 shopId，其它入口保持原逻辑
    excludeShopId: {
        type: Boolean,
        default: false
    }
});
const linkSelectData = defineModel("linkSelectData");
const emit = defineEmits(["submitCallback", "callback", "okType", "update:modelValue"]);
emit("okType", false);
const selectedIds = ref<number[]>([]);
// 恢复选中状态时，防止 onSelectChange 覆盖 selectedIds
const isRestoringSelection = ref(false);
const loading = ref(true); //列表加载中
const total = ref<number>(0);
const config = useConfigStore();
const filterState = ref<ProductFilterState[]>([]);
// 选中的数据
const selectionList = ref<ProductFilterState[]>([]);
const filterParams = ref<ProductFilterParams>({
    page: 1,
    size: (config as any).get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: "",
    isDelete: 0,
    productStatus: 1,
    checkStatus: 1,
    ...(props.excludeShopId ? {} : { shopId: props.isSelf ? 0 : Number(localStorage.getItem("shopId") || 0) }),
    showVendorProduct: props.isSelf ? 0 : 1,
    reservationProduct: props.reservationProduct ? 1 : 0
});

watch(
    () => props.inventoryFilterParams,
    (val) => {
        if (val.page) {
            filterParams.value = { ...val };
        }
    },
    { deep: true, immediate: true }
);

const idKey = computed(() => {
    return props.inventoryFilterParams?.page ? "inventoryId" : "productId";
});
const restoreSelection = async () => {
    const currentId = Number(linkSelectData.value?.id || props.modelValue?.id || 0);
    if (!currentId) {
        return;
    }
    selectedIds.value = [currentId];
    emit("okType", true);
    await nextTick();
    const currentRow = filterState.value.find((item) => item[idKey.value] === currentId || item.productId === currentId);
    if (currentRow) {
        isRestoringSelection.value = true;
        tableRef.value?.clearSelection();
        tableRef.value?.toggleRowSelection(currentRow, true);
        isRestoringSelection.value = false;
    }
};

const defaultSelectionInited = ref(false);
const restoreDefaultSelection = async () => {
    if (!props.isMultiple || props.defaultSelectedIds.length === 0) return;
    // 首次加载时，用 defaultSelectedIds 初始化 selectedIds（统一转为 number）
    if (!defaultSelectionInited.value) {
        defaultSelectionInited.value = true;
        selectedIds.value = props.defaultSelectedIds.map((id: any) => Number(id));
        emit("okType", selectedIds.value.length > 0);
    }
    // 每次加载新页数据后，对当前页中属于已选列表的行恢复勾选
    await nextTick();
    isRestoringSelection.value = true;
    filterState.value.forEach((row) => {
        if (selectedIds.value.includes(Number(row[idKey.value]))) {
            const skuIds = props.defaultSelectedSkuIds?.[String(row.productId)];
            if (Array.isArray(skuIds)) row.skuIds = skuIds.map((id: any) => Number(id));
            tableRef.value?.toggleRowSelection(row, true);
        }
    });
    isRestoringSelection.value = false;
};

// 供应链商品接口的 liveRequest 已提示业务错误，避免权限不足等错误重复弹出。
const shouldShowLoadError = () => {
    return true;
};
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        filterParams.value.isFilterPresaleProduct = props.isFilterPresaleProduct ? 1 : 0;
        const apiFunc = props.apiList || getProductList;
        const requestParams: Record<string, any> = { ...filterParams.value, ...props.otherParams };
        if (props.excludeShopId) {
            delete requestParams.shopId;
        }
        const result = await apiFunc(requestParams);
        filterState.value = result.records.map((item: any, index: number) => {
            const normalized = { ...item };
            if (normalized.productId === undefined && normalized.id !== undefined) {
                normalized.productId = normalized.id;
                normalized.productSn = normalized.skus?.[0]?.skuSn ?? "";
                normalized.productStock = normalized.sumSkuStock ?? 0;
            }
            normalized.inventoryId = normalized.productId + normalized.productSkuId;
            return normalized;
        });
        total.value = result.total;
        await restoreSelection();
        await restoreDefaultSelection();
    } catch (error: any) {
        if (shouldShowLoadError()) message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});
// 参数查询
const searchSubmit = () => {
    loadFilter();
};
const tableRef: any = ref();
const skuRef: any = ref();

const onSelectChange = (e: any) => {
    if (props.isMultiple) {
        // 恢复选中状态期间，不要用 el-table 的局部 selection 覆盖完整的 selectedIds
        if (isRestoringSelection.value) return;
        let idsSet: any = [];
        e.forEach((item: any) => {
            idsSet.push(item[idKey.value]);
        });
        selectedIds.value = idsSet;
        // reserve-selection 模式下，e 包含所有页的选中行，同步更新 selectionList
        selectionList.value = e;
        emit("okType", selectedIds.value.length > 0);
    }
};
const rowInfo = ref<any>({});
const skuVisible = ref<boolean>(false);
const selectRow = (selection: any, val: any) => {
    // 获取选中的数据
    selectionList.value = selection;
    //手动触发该事件
    if (selection.includes(val)) {
        if (props.isSku && val.productSku !== null && val.productSku.length > 1) {
            rowInfo.value = val;
            skuVisible.value = true;
        }
    } else {
        val.skuIds = [];
    }
    if (!props.isMultiple) {
        // 单选
        tableRef.value.clearSelection();
        if (selection.length == 0) {
            selectedIds.value = []; // 清空data中绑定的selectedRow
            return;
        }
        tableRef.value.toggleRowSelection(val, true);
        selectedIds.value.length = 0;
        selectedIds.value.push(val[idKey.value]);
        emit("okType", selectedIds.value.length > 0);
    }
    linkSelectData.value = {
        path: "product",
        label: t('商品'),
        name: val.productName,
        id: val[idKey.value],
        sn: val.productSn,
        data: {
            id: val[idKey.value],
            sn: val.productSn
        }
    };
    emit("update:modelValue", val);
};
//监听权选
const handleSelectAll = (selection: any) => {
    // 获取选中的数据
    selectionList.value = selection;
    console.log(selectionList, "selectionList");
    //需要选择规格
    if (props.isSku) {
        if (selection.length != 0) {
            filterState.value.map((item: any) => {
                let skuIds = [];
                if (item.productSku && item.productSku.length > 1) {
                    skuIds = item.productSku.map((v: any) => v.skuId);
                }
                item.skuIds = skuIds;
            });
        } else {
            filterState.value.map((item: any) => {
                item.skuIds = [];
            });
        }
    }
};
const isSelectable = (row: any, index: number) => {
    // 排除重复项
    return !props.selectedIds?.includes(row[idKey.value]); // Column configuration not to be checked
};
const closeSku = () => {
    tableRef.value.toggleRowSelection(rowInfo.value, false);
    selectedIds.value = [];
    rowInfo.value = "";
    skuVisible.value = false;
};
const submitSku = () => {
    filterState.value.forEach((item) => {
        if (item.productId == rowInfo.value.productId) {
            item.skuIds = skuRef.value.skuIds;
        }
    });
    rowInfo.value = "";
    skuVisible.value = false;
};
// 弹窗回调
const onFormSubmit = () => {
    // tableRef.value?.clearSelection();
    // 弹窗确认按钮提交
    if (props.isSku) {
        const selectes = selectionList.value
            .filter((item) => selectedIds.value.includes(item.productId))
            .map((item) => {
                const result: any = { productId: item.productId };
                if (item.skuIds) {
                    result.skuIds = item.skuIds;
                }
                return result;
            });
        emit("submitCallback", selectes);
        // 选择库存列表的数据
    } else if (props.inventoryFilterParams?.page) {
        console.log(selectedIds, "selectedIds");
        console.log(selectionList, 999);
        const selecteState = selectionList.value.filter((item) => selectedIds.value.includes(item.inventoryId));
        emit("submitCallback", selecteState);
    } else {
        emit("submitCallback", selectedIds.value);
    }
};

// 修改排序

defineExpose({
    onFormSubmit
});
</script>
<style lang="less" scoped>
.pagination-con {
    background: #fff;
}

:deep(.hide-checkbox .el-table__header-wrapper .el-table__header .el-checkbox) {
    display: none;
}

.search-wrap {
    width: 100%;
}

.search-row {
    display: flex;
    width: 100%;
    gap: 8px;
    align-items: center;

    .search-category {
        width: 25%;
        min-width: 100px;
    }

    .search-status {
        width: 15%;
        min-width: 80px;
    }

    .search-keyword {
        flex: 1;
        min-width: 120px;
    }

    .search-input {
        flex: 1;
        min-width: 150px;
    }

    .btn {
        flex-shrink: 0;
        white-space: nowrap;
        line-height: 1;
        min-width: 60px;
        height: auto;
        margin-left: 0;
    }
}
</style>
