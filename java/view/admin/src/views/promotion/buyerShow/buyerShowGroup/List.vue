<template>
    <div class="lyecs-table-list-warp">
        <div class="simple-form-field">
            <div class="form-group">
                <DialogForm
                    :params="{ act: 'add' }"
                    isDrawer
                    :delayed="20000"
                    path="promotion/buyerShow/buyerShowGroup/Info"
                    :title="$t('新建分组')"
                    width="600px"
                    @okCallback="loadFilter"
                >
                    <el-button type="primary">{{ $t('新建分组') }}</el-button>
                </DialogForm>
            </div>
        </div>
        <div class="table-container">
            <a-spin :spinning="loading">
                <el-table :data="filterState" row-key="rechargeId" @selection-change="onSelectChange" :total="total" @sort-change="onSortChange" :loading="loading">
                    <el-table-column :label="$t('分组名称')" prop="groupName"></el-table-column>
                    <el-table-column :label="$t('关联商品分组')" prop="productGroupName">
                        <template #default="{ row }">
                            <div>{{ row.productGroupName?.length ? row.productGroupName.join(' ') : $t('无') }}</div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('买家秀数量')" prop="buyerShowCount"></el-table-column>
<!--                    <el-table-column label="排序" prop="sortOrder" sortable="custom">-->
<!--                        <template #default="{ row }">-->
<!--                            <PopForm label="排序" type="integer" :requestApi="updateRechargeSettingFiled" v-model:org-value="row.sortOrder"-->
<!--                                     :params="{ id: row.rechargeId, field: 'sortOrder' }" extra="默认值为50，数值越小，排序越靠前">-->
<!--                                <div>{{ row.sortOrder }}</div>-->
<!--                            </PopForm>-->
<!--                        </template>-->
<!--                    </el-table-column>-->
                    <el-table-column :label="$t('操作')" fixed="right" :width="150">
                        <template #default="{ row }">
                            <DialogForm isDrawer @okCallback="loadFilter" :title="$t('编辑分组')" width="600px" path="promotion/buyerShow/buyerShowGroup/Info"
                                        :params="{ act: 'detail', id: row.buyerShowGroupId }">
                                <a class="btn-link">{{ $t('编辑') }}</a>
                            </DialogForm>
                            <el-divider direction="vertical" />
                            <DeleteRecord @afterDelete="loadFilter" :requestApi="deleteBuyerShowGroup" :params="{ id: row.buyerShowGroupId }">{{ $t('删除') }}</DeleteRecord>
                        </template>
                    </el-table-column>
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
</template>

<script setup lang="ts">
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination } from "@/components/list";
import { useListRequest } from "@/hooks/useListRequest";
import { getBuyerShowGroupList, deleteBuyerShowGroup } from "@/api/promotion/buyShow";
import { useConfigStore } from "@/store/config";
import { PopForm } from "@/components/pop-form";
import { buyerShowGroupFilterState, buyerShowGroupFilterParams } from "@/types/promotion/buyerShowGroup";

const config: any = useConfigStore();

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSortChange,
    onSelectChange,
} = useListRequest<buyerShowGroupFilterState, buyerShowGroupFilterParams>({
    apiFunction: getBuyerShowGroupList,
    idKey: "promotionId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: ""
    }
});

// 初始化
loadFilter()
</script>


<style scoped lang="less">

</style>
