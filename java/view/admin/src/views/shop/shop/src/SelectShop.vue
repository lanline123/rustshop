<template>
    <div class="container" style="position: relative; height: 100%">
        <div class="content_wrapper">
            <div class="" style="position: absolute; top: 0; bottom: 0; overflow-y: auto; width: 100%">
                <div class="list-table-tool lyecs-search-warp" style="position: absolute; top: 0">
                    <el-form :model="filterParams" name="form" @finish="searchSubmit">
                        <div class="list-table-tool-row" style="margin-bottom: 5px">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput v-model="filterParams.keyword" :placeholder="$t('请输入搜索关键字')" />
                                    <el-button @click="searchSubmit">{{ $t("搜索") }}</el-button>
                                </el-space>
                            </div>
                            <div class="list-table-tool-col"></div>
                        </div>
                    </el-form>
                </div>
                <perfect-scrollbar class="table-container" style="position: absolute; top: 50px; bottom: 50px; overflow-y: auto; width: 100%">
                    <a-spin :spinning="loading">
                        <div style="padding-right: 15px; box-sizing: border-box">
                            <el-table
                                :class="isMultiple ? '' : 'hide-checkbox'"
                                ref="tableRef"
                                class="result-table"
                                :data="filterState"
                                row-key="shopId"
                                @select="selectRow"
                                @selection-change="onSelectChange"
                                :total="total"
                                :loading="loading"
                            >
                                <el-table-column :selectable="isSelectable" type="selection" width="32" />
                                <el-table-column :label="isStore() ? $t('门店名称') : $t('店铺名称')" prop="shopTitle"></el-table-column>
                                <template #empty>
                                    <div class="empty-warp">
                                        <div v-if="!loading" class="empty-bg">{{ $t("暂无数据") }}</div>
                                    </div>
                                </template>
                            </el-table>
                        </div>
                    </a-spin>
                </perfect-scrollbar>
                <div
                    class="pagination-con"
                    v-if="total > 0"
                    style="position: absolute; bottom: 0; justify-content: right; display: flex; width: 100%; margin: 0"
                >
                    <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { SelectCategory, SelectShop } from "@/components/select";
import { nextTick, onMounted, reactive, ref } from "vue";
import { Modal, message, type TableColumnsType } from "ant-design-vue";
import { imageFormat } from "@/utils/format";
import { Pagination } from "@/components/list";
import { ShopFilterParams, ShopFilterState } from "@/types/shop/shop.d";
import { useConfigStore } from "@/store/config";
import { getShopList } from "@/api/shop/shop";
import { isStore } from "@/utils/version";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const props = defineProps({
    // 已选择的项，用于排除重复，禁止选择
    selectedIds: { type: Array, default: [] },
    isMultiple: {
        type: Boolean,
        default: true
    },
    // 列表是否包含平台自营门店
    includeSelfOperated: {
        type: Boolean,
        default: false
    }
});
const linkSelectData = defineModel<any>("linkSelectData");
const emit = defineEmits(["submitCallback", "okType"]);
emit("okType", false);
// 基本参数定义
const columns: TableColumnsType = [{ title: t("商品信息"), key: "shopId", sorter: true }];
const activeKey = ref(1);
const loading = ref(true); //列表加载中

const selectedIds = ref<number[]>([]); //钩选项
const total = ref<number>(0);
const config: any = useConfigStore();
const filterState = ref<ShopFilterState[]>([]);
const filterParams = reactive<ShopFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: "",
    shopType: isStore() ? 2 : 1,
    ...(props.includeSelfOperated ? { includeSelfOperated: 1 } : {}),
    ...(isStore() ? { status: 1 } : {})
});
const restoreSelection = async () => {
    // 平台自营门店 shopId 为 0，不能按假值判断
    const rawId = linkSelectData.value?.id;
    const currentId = rawId === undefined || rawId === null || rawId === "" ? NaN : Number(rawId);
    if (Number.isNaN(currentId)) {
        return;
    }
    selectedIds.value = [currentId];
    emit("okType", true);
    await nextTick();
    const currentRow = filterState.value.find((item) => item.shopId === currentId);
    if (currentRow) {
        tableRef.value?.clearSelection();
        tableRef.value?.toggleRowSelection(currentRow, true);
    }
};

// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getShopList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
        await restoreSelection();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});
// 参数查询
const searchSubmit = (values: any) => {
    loadFilter();
};

const onSelectChange = (e: any[]) => {
    if (props.isMultiple) {
        selectedIds.value = e.map((item) => item.shopId);
        emit("okType", selectedIds.value.length > 0);
    }
};
const tableRef: any = ref();
const selectRow = (selection: any, val: any) => {
    if (!props.isMultiple) {
        tableRef.value.clearSelection();
        if (selection.length == 0) {
            selectedIds.value = []; // 清空data中绑定的selectedRow
            return;
        }
        tableRef.value.toggleRowSelection(val, true);
        selectedIds.value.length = 0;
        selectedIds.value.push(val.shopId);
        emit("okType", selectedIds.value.length > 0);
        linkSelectData.value = {
            path: isStore() ? "store" : "shop",
            label: isStore() ? t("门店") : t("店铺"),
            id: val.shopId,
            name: val.shopTitle,
            data: {
                id: val.shopId,
                name: val.shopTitle
            }
        };
    }
};
const isSelectable = (row: any) => {
    // 排除重复项
    return !props.selectedIds.includes(row.shopId); // Column configuration not to be checked
};
// 弹窗回调
const onFormSubmit = () => {
    // 弹窗确认按钮提交
    console.log(selectedIds.value);
    emit("submitCallback", selectedIds.value);
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
.content_wrapper {
    :deep(.el-table__header-wrapper .el-table-column--selection .el-checkbox) {
        display: none !important;
    }
}
</style>
