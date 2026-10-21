<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" name="form">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('活动名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入活动名称')"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('活动状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select
                                                v-model="filterParams.activeState"
                                                :placeholder="$t('请选择活动状态')"
                                                clearable
                                                @clear="onSearchSubmit"
                                                @change="onSearchSubmit"
                                            >
                                                <el-option :label="$t('活动进行中')" :value="1" />
                                                <el-option :label="$t('活动已结束')" :value="2" />
                                                <el-option :label="$t('活动未开始')" :value="3" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <div class="control-container">
                                            <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                            <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="promotion/productActivity/limitdiscount/Info"
                                        :title="$t('新增限时折扣活动')"
                                        width="1040px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('新增限时折扣活动') }}</el-button>
                                    </DialogForm>
                                    <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            row-key="discountId"
                            @selection-change="onSelectChange"
                            :total="total"
                            @sort-change="onSortChange"
                            :loading="loading"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('活动名称')" prop="promotionName" min-width="200">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <div>{{ row.promotionName || "--" }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('起始日期')" prop="startTime" sortable="custom" width="200"> </el-table-column>
                            <el-table-column :label="$t('截止日期')" prop="endTime" sortable="custom" width="200"> </el-table-column>
                            <el-table-column :label="$t('状态')" prop="statusName" width="100"></el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" width="150">
                                <template #default="{ row }">
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('编辑限时折扣')"
                                        width="1040px"
                                        path="promotion/productActivity/limitdiscount/Info"
                                        :params="{ act: 'detail', id: row.discountId }"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord @afterDelete="loadFilter" :requestApi="delLimitdiscount" :params="{ id: row.discountId }">{{ $t('删除') }}</DeleteRecord>
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
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { LimitdiscountFilterState, LimitdiscountFilterParams } from "@/types/promotion/limitdiscount.d";
import { getLimitdiscountList, batchSubmit, delLimitdiscount } from "@/api/promotion/limitdiscount";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
import { useListRequest } from "@/hooks/useListRequest";
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
} = useListRequest<LimitdiscountFilterState, LimitdiscountFilterParams>({
    apiFunction: getLimitdiscountList,
    idKey: "discountId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: ""
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();
// 基本参数定义
// const filterState = ref<LimitdiscountFilterState[]>();
// const loading = ref<boolean>(true);
// const total = ref<number>(0);
// const selectedIds = ref<number[]>([]);
// const filterParams = reactive<LimitdiscountFilterParams>({
//     page: 1,
//     size: config.get("pageSize"),
//     sortField: "",
//     sortOrder: "",
//     keyword: ""
// });
// 获取列表的查询结果
// const loadFilter = async () => {
//     loading.value = true;
//     try {
//         const result = await getLimitdiscountList({ ...filterParams });
//         filterState.value = result.records;
//         total.value = result.total;
//     } catch (error: any) {
//         message.error(error.message);
//     } finally {
//         loading.value = false;
//     }
// };
// onMounted(() => {
//     loadFilter();
// });

// 参数查询
// const onSearchSubmit = () => {
//     loadFilter();
// };
// // 修改排序
// const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
//     filterParams.sortField = prop;
//     filterParams.sortOrder = order == "ascending" ? "asc" : order == "descending" ? "desc" : "";
//     loadFilter();
// };

// // 批量操作
// const onBatchSubmit = async (action: string) => {
//     try {
//         const result = await batchSubmit(action, { ids: selectedIds.value });
//         message.success(t('操作成功'));
//         loadFilter();
//     } catch (error: any) {
//         message.error(error.message);
//     }
// };
// // 多选操作
// const onSelectChange = (e: any[]) => {
//     selectedIds.value = e.map((item) => item.discountId);
// };
</script>
<style>
/*优惠券列表类型选择*/
.bonus-wrap .card-wrap {
    display: -ms-flexbox;
    display: flex;
    margin-bottom: 24px;
    -ms-flex-pack: start;
    justify-content: flex-start;
    overflow: hidden;
    z-index: -1;
}

.bonus-wrap .card-wrap__container {
    transition: transform 0.6s ease-in-out;
    display: -ms-flexbox;
    display: flex;
}

.bonus-wrap .item-bg {
    position: relative;
    white-space: nowrap;
    padding: 20px 27px 17px;
    display: -ms-flexbox;
    display: flex;
    -ms-flex-direction: column;
    flex-direction: column;
    background: rgba(51, 136, 255, 0.07);
    margin-right: 15px;
    text-align: center;
    -ms-flex-align: center;
    align-items: center;
    -ms-flex-pack: justify;
    justify-content: space-between;
}

.bonus-wrap .coupon-cards-item {
    min-width: 166px;
    width: 170px;
}

.bonus-wrap .coupon-cards-item .title {
    font-size: 16px;
    color: #333;
}

.bonus-wrap .coupon-cards-item .desc {
    padding-top: 10px;
    line-height: 18px;
    font-size: 12px;
    color: #666;
}

.bonus-wrap .coupon-cards-item button {
    margin-top: 15px;
    padding: 0 16px;
}

.bonus-wrap .subtitle {
    font-weight: 500;
    font-size: 14px;
    color: #323233;
    line-height: 20px;
    margin-bottom: 16px;
}

.bonus-wrap .item-bg2 {
    background: #fff;
    border: 1px solid #ebedf0;
    box-sizing: border-box;
    width: 223px !important;
}
</style>
