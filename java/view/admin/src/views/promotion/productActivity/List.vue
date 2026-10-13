<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="bonus-wrap">
                <p class="subtitle">{{ $t('按优惠方式快速创建') }}</p>
                <div class="card-wrap">
                    <div class="card-wrap__container">
                        <div class="item-bg coupon-cards-item">
                            <div>
                                <div class="title">{{ $t('满减') }}</div>
                                <div class="desc">
                                    <p>例：100{{ config.config.amountUnit }}减20{{ config.config.amountUnit }}<br>{{ $t('便于合理控制活动成本') }}</p>
                                </div>
                            </div>
                            <DialogForm isDrawer @okCallback="loadFilter" :title="$t('添加减免优惠活动')" width="1040px"
                                path="promotion/productActivity/Info" :params="{ act: 'add', promotionType: 1 }">
                                <el-button type="primary">{{ $t('立即新建') }}</el-button>
                            </DialogForm>
                        </div>
                        <div class="item-bg coupon-cards-item">
                            <div>
                                <div class="title">{{ $t('折扣') }}</div>
                                <div class="desc">
                                    <p>例：100{{ config.config.amountUnit }}打9折<br>{{ $t('提高店铺销量和客单价') }}</p>
                                </div>
                            </div>
                            <DialogForm isDrawer @okCallback="loadFilter" :title="$t('添加折扣优惠活动')" width="1040px"
                                path="promotion/productActivity/Info" :params="{ act: 'add', promotionType: 2 }">
                                <el-button type="primary">{{ $t('立即新建') }}</el-button>
                            </DialogForm>
                        </div>
                        <div class="item-bg coupon-cards-item item-bg2">
                            <div>
                                <div class="title">{{ $t('赠品') }}</div>
                                <div class="desc">
                                    <p>{{ $t('据会员等级可领的赠品，丰富会') }}<br>{{ $t('员权益，凸显会员身份价值') }}</p>
                                </div>
                            </div>
                            <DialogForm isDrawer @okCallback="loadFilter" :title="$t('添加赠品优惠活动')" width="1040px"
                                path="promotion/productActivity/Info" :params="{ act: 'add', promotionType: 0 }">
                                <el-button type="primary">{{ $t('立即新建') }}</el-button>
                            </DialogForm>
                        </div>
                    </div>
                </div>
            </div>
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('活动名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput name="keyword" v-model="filterParams.keyword" :placeholder="$t('输入活动名称')">
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('是否显示：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.isGoing" clearable>
                                                <el-option v-for="(item, key) in promotionStatus" :value="key+ 1" :label="item" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <el-button type="primary" @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="promotionId" @selection-change="onSelectChange" :total="total"
                            @sort-change="onSortChange" :loading="loading">
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('活动名称')" prop="promotionName" :width="150">
                                <template #default="{ row }">
                                    <div style="position: relative;">
                                        <div>{{ row.promotionName || '--' }}</div>
                                    </div>
                                    <p class="order-tips-p">
                                        <span class="green">
                                            {{ row.promotionType == 1 ? $t('减免') : row.promotionType == 2 ? $t('折扣') : $t('赠品') }}
                                        </span>
                                    </p>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('起始日期')" prop="startTime" width="150" sortable="custom">
                            </el-table-column>
                            <el-table-column :label="$t('截止日期')" prop="endTime" width="150" sortable="custom">
                            </el-table-column>
                            <el-table-column :label="$t('金额下限')" prop="minOrderAmount" sortable="custom">
                                <template #default="{ row }">
                                    <!-- <PopForm label="金额下限" type="input" :requestApi="updateProductActivityFiled" v-model:org-value="row.minOrderAmount"
                                        :params="{ id: row.promotionId, field: 'minOrderAmount' }"> -->
                                        <div>{{ row.minOrderAmount }}</div>
                                    <!-- </PopForm> -->
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('金额上限')" prop="maxOrderAmount" sortable="custom">
                                <template #default="{ row }">
                                    <!-- <PopForm label="金额上限" type="input" :requestApi="updateProductActivityFiled" v-model:org-value="row.maxOrderAmount"
                                        :params="{ id: row.promotionId, field: 'maxOrderAmount' }"> -->
                                        <div>{{ row.maxOrderAmount }}</div>
                                    <!-- </PopForm> -->
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom">
                                <template #default="{ row }">
                                    <PopForm :label="$t('排序')" type="input" :requestApi="updateProductActivityFiled" v-model:org-value="row.sortOrder"
                                        :params="{ id: row.promotionId, field: 'sortOrder' }" :extra="$t('默认值为50，数值越小，排序越靠前')">
                                        <div>{{ row.sortOrder }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('状态')" prop="productStatus"></el-table-column>
                            <el-table-column :label="$t('是否启用')" prop="isShow" sortable="custom">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.isAvailable" :requestApi="updateProductActivityFiled"
                                        :params="{ id: row.promotionId, field: 'isAvailable' }" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right">
                                <template #default="{ row }">
                                    <DialogForm isDrawer @okCallback="loadFilter" :title="$t('编辑优惠活动')" width="1040px"
                                        path="promotion/productActivity/Info" :params="{ act: 'detail', id: row.promotionId }">
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord @afterDelete="loadFilter" :requestApi="delProductActivity"
                                        :params="{ id: row.promotionId }">{{ $t('删除') }}</DeleteRecord>
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
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                            @callback="loadFilter" />
                    </div>
                </div>
                <div class="selected-action-warp selected-warp-left" v-if="selectedIds.length > 0">
                    <div class="selected-action">
                        <el-space>
                            <span>{{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }}</span>
                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                <template #reference><el-button>{{ $t('批量删除') }}</el-button></template>
                            </el-popconfirm>
                        </el-space>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import '@/style/css/list.less'
import { DialogForm } from '@/components/dialog'
import { PopForm } from '@/components/pop-form'
import { ref, reactive, onMounted } from 'vue';
import { DeleteRecord, Switch, Pagination } from '@/components/list';
import { Image } from '@/components/image';
import { message } from 'ant-design-vue'
import { useConfigStore } from "@/store/config";
import { ProductActivityFilterState, ProductActivityFilterParams } from '@/types/promotion/productActivity.d';
import { getProductActivityList, batchSubmit, updateProductActivityFiled, delProductActivity, getProductActivityConfig } from "@/api/promotion/productActivity";
import { SelectShop } from '@/components/select'
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config:any = useConfigStore();
// 基本参数定义
const filterState = ref<ProductActivityFilterState[]>();
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const filterParams = reactive<ProductActivityFilterParams>({
    page: 1,
    size: config.get('pageSize'),
    sortField: '',
    sortOrder: '',
    keyword: ''
});
const promotionStatus = ref<string[]>([])
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getProductActivityList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
        const config = await getProductActivityConfig()
        promotionStatus.value = config.promotionStatus
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
}
onMounted(() => {
    loadFilter();
});

// 参数查询
const onSearchSubmit = () => {
    loadFilter()
};
// 修改排序
const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
    filterParams.sortField = prop;
    filterParams.sortOrder = order == 'ascending' ? 'asc' : order == 'descending' ? 'desc' : '';
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
const onSelectChange = (e: ProductActivityFilterState[]) => {
    selectedIds.value = e.map((item) => item.promotionId);
};
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
    z-index: -1
}

.bonus-wrap .card-wrap__container {
    transition: transform .6s ease-in-out;
    display: -ms-flexbox;
    display: flex
}

.bonus-wrap .item-bg {
    position: relative;
    white-space: nowrap;
    padding: 20px 27px 17px;
    display: -ms-flexbox;
    display: flex;
    -ms-flex-direction: column;
    flex-direction: column;
    background: rgba(51, 136, 255, .07);
    margin-right: 15px;
    text-align: center;
    -ms-flex-align: center;
    align-items: center;
    -ms-flex-pack: justify;
    justify-content: space-between
}

.bonus-wrap .coupon-cards-item {
    min-width: 166px;
    width: 170px
}

.bonus-wrap .coupon-cards-item .title {
    font-size: 16px;
    color: #333
}

.bonus-wrap .coupon-cards-item .desc {
    padding-top: 10px;
    line-height: 18px;
    font-size: 12px;
    color: #666
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
    margin-bottom: 16px
}

.bonus-wrap .item-bg2 {
    background: #fff;
    border: 1px solid #ebedf0;
    box-sizing: border-box;
    width: 223px !important
}</style>