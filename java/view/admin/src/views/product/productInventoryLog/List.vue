/**
 * @file 商品库存变动日志
 * @description 商品库存变动记录查询
 * @features
 * - 按关键词搜索
 * - 按变动类型（入库/出库/调拨）筛选
 * - 展示变动详情和时间
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('输入关键词')" clearable @clear="onSearchSubmit">
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="tabs flex">
                                            <TigTabs v-model="filterParams.type" :tabs="tabs" @onTabChange="onTabChange"></TigTabs>
                                            <!-- <template v-for="item in tabs">
                                                <div
                                                    class="item"
                                                    @click="onTabChange(item.value)"
                                                    :class="filterParams.type == item.value ? 'active' : ''"
                                                >
                                                    {{ item.label }}
                                                </div>
                                            </template> -->
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="logId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :width="360" :label="$t('商品名称')" prop="productName">
                                <template #default="{ row }">
                                    <div class="product-cell">
                                        <div class="product-cell__sn">{{ row.sn }}</div>
                                        <div class="ellipsis product-cell__name">{{ row.productName }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="360" :label="$t('规格')" prop="skuValue">
                                <template #default="{ row }">
                                    <div class="ellipsis">{{ row.skuValue || "-" }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('变化前库存')" prop="oldNumber" :min-width="100"></el-table-column>
                            <el-table-column :label="$t('变化后库存')" prop="number" :min-width="100"></el-table-column>
                            <el-table-column :label="$t('变化量')" prop="changeNumber"></el-table-column>
                            <el-table-column :label="$t('操作')" prop="desc"></el-table-column>
                            <el-table-column :width="200" :label="$t('操作时间')" prop="addTime"></el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div class="pagination-con" v-if="total > 0">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { ProductInventoryLogFilterParams, ProductInventoryLogFilterState } from "@/types/product/productInventoryLog.d";
import { batchSubmit, getProductInventoryLogList } from "@/api/product/productInventoryLog";
import { Pagination } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref([
    {
        value: 0,
        label: t('全部'),
        isShow: true
    },
    {
        value: 1,
        label: t('入库'),
        isShow: true
    },
    {
        value: 2,
        label: t('出库'),
        isShow: true
    }
]);
const config: any = useConfigStore();
// 基本参数定义
const filterState = ref<ProductInventoryLogFilterState[]>();
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const filterParams = reactive<ProductInventoryLogFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: "",
    type: 0
});
const onTabChange = (val: number) => {
    filterParams.type = val;
    filterParams.page = 1;
    loadFilter();
};
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getProductInventoryLogList({ ...filterParams });

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

// 批量操作
const onBatchSubmit = async (action: string) => {
    try {
        const result = await batchSubmit(action, { ids: selectedIds.value });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};
// 多选操作
const onSelectChange = (e: Object[]) => {
    selectedIds.value = e.map((item: any) => item.logId);
};
</script>
<style lang="less" scoped>
.tabs {
    flex-wrap: wrap;
    gap: 10px !important;
    .item {
        min-width: 55px;
        padding: 0 8px;
        text-align: center;
        height: 25px;
        line-height: 25px;
        color: #333;
        margin-right: 5px;
        font-size: 14px;
        border-radius: 2px;
        border: 1px solid #fff;
        cursor: pointer;
        &:hover {
            color: var(--tig-primary);
        }
    }
    .active {
        color: var(--tig-primary);
        background: var(--tig-item-active-bg);
        border: 1px solid var(--tig-primary);
    }
}

.ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    box-orient: vertical;
    width: 95%;
}

.product-cell {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    &__sn {
        color: #999;
        font-size: 12px;
        line-height: 1.4;
    }
    &__name {
        line-height: 1.4;
    }
}
</style>
