<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('输入门店名称')">
                                        <template #append>
                                            <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                        </template>
                                    </TigInput>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            ref="tableRef"
                            :class="isMultiple ? '' : 'hide-checkbox'"
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="shopId"
                            @select="selectRow"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column :selectable="isSelectable" type="selection" width="32" />
                            <el-table-column :label="$t('门店名称')">
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
                                        <span v-if="row.status === 10 || row.status === 4" style="color: red">{{ row.statusText }}</span>
                                        <span v-else-if="row.status === 1" style="color: green">{{ row.statusText }}</span>
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
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { Image } from "@/components/image";
import { onMounted, reactive, ref } from "vue";
import { Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { ShopFilterParams, ShopFilterState } from "@/types/shop/shop.d";
import { getShopList } from "@/api/shop/shop";
import { Tag } from "@/components/form";
import StatusDot from "@/components/form/src/StatusDot.vue";
const props = defineProps({
    // 已选择的项，用于排除重复，禁止选择
    selectedIds: { type: Array, default: [] },
    shopInfo: { type: Object, default: {} },
    isMultiple: {
        type: Boolean,
        default: true
    }
});
const emit = defineEmits(["submitCallback", "okType"]);
const config: any = useConfigStore();
// 基本参数定义
const filterState = ref<ShopFilterState[]>([]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const shopInfo = ref<any>(props.shopInfo);
const filterParams = reactive<ShopFilterParams>({
    //初使化用于查询的参数
    shopType: 2,
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: ""
});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getShopList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
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
const onSearchSubmit = () => {
    loadFilter();
};
// 修改排序
const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
    filterParams.sortField = prop;
    filterParams.sortOrder = order == "ascending" ? "asc" : order == "descending" ? "desc" : "";
    loadFilter();
};
const tableRef: any = ref();
const onSelectChange = (e: any) => {
    if (props.isMultiple) {
        e.forEach((item: any) => {
            // 创建一个 Set 包含当前 selectedIds.value 的所有项
            const idsSet = new Set(selectedIds.value);
            // 尝试向 Set 中添加新的 shopId
            idsSet.add(item.shopId);
            // 使用 Set 的唯一性质，将其转换回数组赋给 selectedIds.value
            selectedIds.value = Array.from(idsSet);
        });
    }
};

const selectRow = (selection: any, val: any) => {
    //手动触发该事件
    if (!props.isMultiple) {
        // 单选
        tableRef.value.clearSelection();
        if (selection.length == 0) {
            selectedIds.value = []; // 清空data中绑定的selectedRow
            shopInfo.value = {}; // 清空data中绑定的shopInfo
            return;
        }
        tableRef.value.toggleRowSelection(val, true);
        selectedIds.value.length = 0;
        selectedIds.value.push(val.shopId);
        shopInfo.value = val;
    }
};
const isSelectable = (row: any, index: number) => {
    // 排除重复项
    return !props.selectedIds?.includes(row.shopId); // Column configuration not to be checked
};
// 弹窗回调
const onFormSubmit = () => {
    // 弹窗确认按钮提交
    if (!props.isMultiple) {
        let obj = {
            shopId: selectedIds.value[0],
            shopInfo: [shopInfo.value]
        };
        emit("submitCallback", obj);
    } else {
        emit("submitCallback", selectedIds);
    }
};
defineExpose({
    onFormSubmit
});
</script>
<style lang="less" scoped>
.ssdw {
    display: flex;
    align-items: center;
    gap: 4px;
}
.font-color {
    color: var(--tig-primary);
    cursor: pointer;
}
:deep(.hide-checkbox .el-table__header-wrapper .el-table__header .el-checkbox) {
    display: none;
}
</style>
