<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.shopTitle"
                                            name="shopTitle"
                                            :placeholder="$t('输入门店名称')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        >
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="areaStoreManagerId"
                            show-overflow-tooltip
                            @sort-change="onSortChange"
                        >
                            <el-table-column :width="80" :label="$t('序号')">
                                <template #default="{ row, $index }">
                                    {{ $index + 1 }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('门店LOGO')" prop="shopLogo">
                                <template #default="{ row }">
                                    <Image :src="row.shopLogo" fit="contain" style="height: 25px; width: 60px" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('门店名称')" prop="shopTitle">
                                <template #default="{ row }">
                                    {{ row.shopTitle }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('门店地址')" prop="shopDetailedAddress" min-width="200">
                                <template #default="{ row }">
                                    <span v-if="row.shopRegionNames && row.shopRegionNames.length > 0">
                                        {{ row.shopRegionNames.join("") }}
                                    </span>
                                    {{ row.shopDetailedAddress || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('联系电话')" prop="shopContactConfig">
                                <template #default="{ row }">
                                    <div v-if="row.shopContactConfig && row.shopContactConfig.length > 0">{{ row.shopContactConfig[0].values || "--" }}</div>
                                    <div v-else>--</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('门店状态')">
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
                            <el-table-column :width="100" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DeleteRecord :params="{ shopId: row.shopId, areaStoreManagerId: id }" :requestApi="areaStoreManagerRemoveShop" @afterDelete="loadFilter"
                                        >{{ $t('删除') }} </DeleteRecord>
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
import StatusDot from "@/components/form/src/StatusDot.vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import type { ResponseShopList, AreaStoreManagerVO } from "@/types/store/areaManage";
import { areaStoreManagerShopList, batchSubmit, areaStoreManagerRemoveShop } from "@/api/store/areaManage";
import { useListRequest } from "@/hooks/useListRequest";
import { getAdminType } from "@/utils/storage";
const adminType = getAdminType();
const config: any = useConfigStore();
// 父组件回调
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    }
});
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    onBatchAction,
    resetParams
} = useListRequest<AreaStoreManagerVO, ResponseShopList>({
    apiFunction: areaStoreManagerShopList,
    idKey: "areaStoreManagerId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        shopTitle: "",
        areaStoreManagerId: props.id
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();
</script>
