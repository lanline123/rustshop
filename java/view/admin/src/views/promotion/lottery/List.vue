<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="bonus-wrap">
                <p class="subtitle">{{ $t('按使用场景快速创建') }}</p>
                <div class="card-wrap">
                    <div class="card-wrap__container">
                        <div class="item-bg coupon-cards-item">
                            <div>
                                <div class="title">{{ $t('抽奖活动') }}</div>
                                <div class="desc">
                                    <p>{{ $t('例：创建抽奖活动，活动') }}<br />{{ $t('可设置商品、积分、优惠券等') }} <el-popover
                                            class="box-item"
                                            title=""
                                            placement="right"
                                            width="273"
                                            style="padding: 0"
                                        >
                                            <img src="../../../assets/lottery/lotteryTest.png" alt="" width="250">
                                            <template #reference>
                                                <span class="btn-link">{{ $t('示例') }}</span>
                                            </template>
                                        </el-popover>
                                    </p>
                                </div>
                            </div>
                            <DialogForm
                                isDrawer
                                @okCallback="loadFilter"
                                :title="$t('新建大转盘活动')"
                                width="1200px"
                                path="promotion/lottery/Info"
                                :params="{ act: 'add', id: 0 }"
                                :bodyStyle="{ padding: '0' }"
                                :showClose="false"
                                :showOnOk="false"
                            >
                                <el-button type="primary">{{ $t('立即新建') }}</el-button>
                            </DialogForm>
                        </div>
                    </div>
                </div>
            </div>
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.activityName"
                                                name="activityName"
                                                :placeholder="$t('输入活动名称')"
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
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            row-key="activityId"
                            :total="total"
                            @sort-change="onSortChange"
                            :loading="loading"
                        >
                            <el-table-column :label="$t('活动名称')" prop="activityName" min-width="100">
                                <template #default="{ row }">
                                    <div>{{ row.activityName }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('状态')" prop="status" width="120">
                                <template #default="{ row }">
                                    <div>{{ getStatusTagText(row.status) }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('参与人数')" prop="participantCount" width="120">
                                <template #default="{ row }">
                                    <DialogForm
                                        isDrawer
                                        :title="$t('用户管理')"
                                        width="1200px"
                                        path="promotion/lottery/UserManage"
                                        :params="{ activityId: row.activityId, activityName: row.activityName }"
                                        :showFooter="false"
                                    >
                                        <a class="btn-link">{{ row.participantCount || 0 }}</a>
                                    </DialogForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="createTime">
                                <template #default="{ row }">
                                    {{ row.createTime }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('开始时间')" prop="startTime">
                                <template #default="{ row }">
                                    {{ row.startTime }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('结束时间')" prop="endTime">
                                <template #default="{ row }">
                                    {{ row.endTime }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" width="220">
                                <template #default="{ row }">
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('编辑大转盘活动')"
                                        width="1200px"
                                        path="promotion/lottery/Info"
                                        :bodyStyle="{ padding: '0' }"
                                        :params="{ act: 'edit', id: row.activityId }"
                                        :showFooter="false"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <template v-if="row.status === 1">
                                        <el-divider direction="vertical" />
                                        <el-popconfirm :title="$t('确认要结束该活动吗？')" @confirm="handleEndActivity(row)">
                                            <template #reference>
                                                <a class="btn-link">{{ $t('结束活动') }}</a>
                                            </template>
                                        </el-popconfirm>
                                    </template>
                                    <template v-if="row.canDelete !== false && row.status !== 1">
                                        <el-divider direction="vertical" />
                                        <DeleteRecord @afterDelete="loadFilter" :requestApi="handleDeleteActivity" :params="{ activityId: row.activityId }">{{ $t('删除') }}</DeleteRecord>
                                    </template>
                                    <el-divider direction="vertical" />
                                    <DialogForm
                                        isDrawer
                                        :title="$t('用户管理')"
                                        width="1200px"
                                        path="promotion/lottery/UserManage"
                                        :params="{ activityId: row.activityId, activityName: row.activityName }"
                                        :showFooter="false"
                                    >
                                        <a class="btn-link">{{ $t('用户管理') }}</a>
                                    </DialogForm>
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
import { PopForm } from "@/components/pop-form";
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { LotteryActivityListItem, LotteryActivityListParams } from "@/types/promotion/luckyWheel.d";
import {
    getLotteryActivityList,
    deleteLotteryActivity,
    endLotteryActivity,
} from "@/api/promotion/lottery";
import { useListRequest } from "@/hooks/useListRequest";
import { message } from "ant-design-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();

const getStatusTagType = (status: number) => {
    if (status === 0) return "warning";
    if (status === 1) return "success";
    return "info";
};

const getStatusTagText = (status: number) => {
    if (status === 0) return t('未开始');
    if (status === 1) return t('进行中');
    return t('已结束');
};
const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
} = useListRequest<LotteryActivityListItem, LotteryActivityListParams>({
    apiFunction: getLotteryActivityList as any,
    idKey: "activityId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        activityName: "",
        page: 1,
        size: config.get("pageSize")
    } as any
});

// 结束活动
const handleEndActivity = async (row: any) => {
    try {
        await endLotteryActivity(row.activityId);
        message.success(t('活动已结束'));
        await loadFilter();
    } catch (error: any) {
        message.error(error.message);
        console.error(error);
    }
};

// 删除活动
const handleDeleteActivity = async (params: { activityId: number }) => {
    return deleteLotteryActivity(params.activityId);
};

// 初始化加载
loadFilter();
</script>

<style>
/*大转盘列表类型选择*/
.lottery-status-cell {
    display: flex;
    align-items: center;
    height: 32px; /* 固定状态单元高度，避免刷新时短暂“整行增高” */
}
.lottery-status-tag {
    height: 22px;
    line-height: 22px;
}

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
    background: var(--tig-item-active-bg);
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

@media only screen and (max-width: 767px) {
    .bonus-wrap .card-wrap__container {
        flex-wrap: wrap;
    }
    .bonus-wrap .item-bg {
        width: 47% !important;
        box-sizing: border-box;
        margin: 0;
        margin-top: 10px;
    }
    .bonus-wrap .item-bg:nth-child(2n) {
        margin-left: 3%;
    }
}

:deep(.ant-drawer-body) {
    padding: 0 !important;
}
</style>
