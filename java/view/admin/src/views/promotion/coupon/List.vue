/** * @file 优惠券列表页面 * @description 商城后台优惠券管理，支持多种优惠券类型创建与管理 * @features * - 快速创建入口（满减券/折扣券/新人专享券/会员专享券） *
- 优惠券列表管理（名称/金额/折扣/门槛/有效期） * - 行内编辑（优惠券名称/金额/折扣/门槛） * - 是否展示开关控制 * - 批量删除功能 * - 支持多商户店铺筛选 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="bonus-wrap">
                <p class="subtitle">{{ $t('按使用场景快速创建') }}</p>
                <div class="card-wrap">
                    <div class="card-wrap__container">
                        <div class="item-bg coupon-cards-item">
                            <div>
                                <div class="title">{{ $t('满减券') }}</div>
                                <div class="desc">
                                    <p>例：满100{{ config.config.amountUnit }}减20{{ config.config.amountUnit }}<br />{{ $t('便于合理控制活动成本') }}</p>
                                </div>
                            </div>
                            <DialogForm
                                isDrawer
                                @okCallback="loadFilter"
                                :title="$t('添加满减券')"
                                width="1040px"
                                path="promotion/coupon/Info"
                                :params="{ act: 'add', couponType: 1 }"
                            >
                                <el-button type="primary">{{ $t('立即新建') }}</el-button>
                            </DialogForm>
                        </div>
                        <div class="item-bg coupon-cards-item">
                            <div>
                                <div class="title">{{ $t('折扣券') }}</div>
                                <div class="desc">
                                    <p>例：满100{{ config.config.amountUnit }}打9折<br />{{ $t('提高店铺销量和客单价') }}</p>
                                </div>
                            </div>
                            <DialogForm
                                isDrawer
                                @okCallback="loadFilter"
                                :title="$t('添加折扣券')"
                                width="1040px"
                                path="promotion/coupon/Info"
                                :params="{ act: 'add', couponType: 2 }"
                            >
                                <el-button type="primary">{{ $t('立即新建') }}</el-button>
                            </DialogForm>
                        </div>
                        <div class="item-bg coupon-cards-item item-bg2">
                            <div>
                                <div class="title">{{ $t('新人专享券') }}</div>
                                <div class="desc">
                                    <p>{{ $t('仅店铺新客可领，提高新客') }}{{ $t('首单转化') }}</p>
                                </div>
                            </div>
                            <DialogForm
                                isDrawer
                                @okCallback="loadFilter"
                                :title="$t('添加新人专享券')"
                                width="1040px"
                                path="promotion/coupon/Info"
                                :params="{ act: 'add', isNewUser: 1 }"
                            >
                                <el-button type="primary">{{ $t('立即新建') }}</el-button>
                            </DialogForm>
                        </div>
                        <div class="item-bg coupon-cards-item item-bg2">
                            <div>
                                <div class="title">{{ $t('会员专享券') }}</div>
                                <div class="desc">
                                    <p>{{ $t('限会员可领的优惠券，丰富会') }}{{ $t('员权益，凸显会员身份价值') }}</p>
                                </div>
                            </div>
                            <DialogForm
                                isDrawer
                                @okCallback="loadFilter"
                                :title="$t('添加会员专享券')"
                                width="1040px"
                                path="promotion/coupon/Info"
                                :params="{ act: 'add', isNewUser: 2 }"
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
                                <div v-if="filterParams.shopId == -1" class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <SelectShop v-model:shopId="filterParams.shopId"></SelectShop>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入优惠券名称')"
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
                                        <el-space>
                                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                                <template #reference>
                                                    <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                                </template>
                                            </el-popconfirm>
                                            <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                        </el-space>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <el-popconfirm title="您确认要批量删除所选数据吗？" @confirm="onBatchSubmit('del')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">批量删除</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <span v-if="selectedIds.length > 0">
                                        已选择：<b>{{ selectedIds.length }}</b> 项
                                    </span>
                                </el-space>
                            </div>
                        </div> -->
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            row-key="couponId"
                            @selection-change="onSelectChange"
                            :total="total"
                            @sort-change="onSortChange"
                            :loading="loading"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('优惠券名称')" prop="couponId" :width="200">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <PopForm
                                            :label="$t('优惠券名称')"
                                            type="textarea"
                                            :requestApi="updateCouponFiled"
                                            v-model:org-value="row.couponName"
                                            :params="{ id: row.couponId, field: 'couponName' }"
                                        >
                                            <div>{{ row.couponName }}</div>
                                        </PopForm>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('发放类型')">
                                <template #default="{ row }">
                                    {{ row.couponType == 1 ? $t('满减券') : $t('折扣券') }}
                                    <span v-if="row.isNewUser == 1" class="green">{{ $t('(新人专享)') }}</span>
                                    <span v-if="row.limitUserRank.length" class="green">{{ $t('(会员专享)') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('优惠券金额(折扣)')" prop="couponMoney" sortable="custom">
                                <template #default="{ row }">
                                    <PopForm
                                        v-if="row.couponType == 1"
                                        :label="$t('优惠券金额')"
                                        type="decimal"
                                        :requestApi="updateCouponFiled"
                                        v-model:org-value="row.couponMoney"
                                        :params="{ id: row.couponId, field: 'couponMoney' }"
                                    >
                                        <div>{{ row.couponMoney || "0" }}</div>
                                    </PopForm>
                                    <PopForm
                                        v-if="row.couponType == 2"
                                        :label="$t('优惠券折扣')"
                                        type="decimal"
                                        :requestApi="updateCouponFiled"
                                        v-model:org-value="row.couponDiscount"
                                        :params="{ id: row.couponId, field: 'couponDiscount' }"
                                    >
                                        <div>{{ $t('{couponDiscount}折', { couponDiscount: row.couponDiscount || "0" }) }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('优惠门槛')" prop="minOrderAmount" sortable="custom">
                                <template #default="{ row }">
                                    <PopForm
                                        v-if="row.reduceType == 1"
                                        :label="$t('优惠门槛')"
                                        :type="row.couponUnit == 1 ? 'decimal' : 'integer'"
                                        :requestApi="updateCouponFiled"
                                        v-model:org-value="row.minOrderAmount"
                                        :params="{ id: row.couponId, field: 'minOrderAmount' }"
                                    >
                                        <div>{{ row.minOrderAmount || "0" }}{{ row.couponUnit == 1 ? config.config.amountUnit : $t('件') }}</div>
                                    </PopForm>
                                    <div v-else>{{ $t('无门槛券') }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否公开展示')" prop="isShow" sortable="custom">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.isShow" :requestApi="updateCouponFiled" :params="{ id: row.couponId, field: 'isShow' }" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('活动时间')" prop="time">
                                <template #default="{ row }">
                                    <template v-if="row.sendType === 0">
                                        <template v-if="(row.delayDay ?? 0) === 0">
                                            领券后立即生效，有效期{{ row.useDay ?? 0 }}天
                                        </template>
                                        <template v-else>
                                            领券后{{ row.delayDay }}天生效，有效期{{ row.useDay ?? 0 }}天
                                        </template>
                                    </template>
                                    <template v-else> {{ row.useStartDate }} - {{ row.useEndDate }} </template>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('状态')" width="120">
                                <template #default="{ row }">
                                    {{ couponStatusText(row.status) }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" width="220">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <DialogForm
                                            isDrawer
                                            @okCallback="loadFilter"
                                            :title="$t('编辑优惠券')"
                                            width="1040px"
                                            path="promotion/coupon/Info"
                                            :params="{ act: 'detail', id: row.couponId }"
                                        >
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <!-- 操作达到 3 个及以上时，第 3 个及之后收进更多 -->
                                        <template v-if="!isCouponExpired(row)">
                                            <el-divider direction="vertical" />
                                            <DialogForm
                                                :key="`coupon-inject-${row.couponId}`"
                                                isDrawer
                                                @okCallback="loadFilter"
                                                :title="$t('优惠券发放')"
                                                width="1000px"
                                                path="promotion/coupon/src/Injected"
                                                :params="{ id: row.couponId }"
                                            >
                                                <a class="btn-link">{{ $t('发放') }}</a>
                                            </DialogForm>
                                            <el-divider direction="vertical" />
                                            <el-dropdown
                                                :ref="(el: any) => setMoreDropdownRef(el, row.couponId)"
                                                trigger="hover"
                                            >
                                                <el-icon class="btn-link" size="12">
                                                    <MoreFilled />
                                                </el-icon>
                                                <template #dropdown>
                                                    <el-dropdown-menu>
                                                        <el-dropdown-item @click.capture="closeMoreDropdown(row.couponId)">
                                                            <DialogForm
                                                                :params="{ couponId: row.couponId }"
                                                                :show-on-ok="false"
                                                                :showFooter="false"
                                                                isDrawer
                                                                path="promotion/coupon/SendLog"
                                                                :title="$t('发放记录')"
                                                                width="700px"
                                                            >
                                                                <el-button style="width: 100%" link type="primary">{{ $t('发放记录') }}</el-button>
                                                            </DialogForm>
                                                        </el-dropdown-item>
                                                        <DeleteRecord @afterDelete="loadFilter" :requestApi="delCoupon" :params="{ id: row.couponId }">
                                                            <el-dropdown-item>
                                                                <el-button style="width: 100%" link type="primary">{{ $t('删除') }}</el-button>
                                                            </el-dropdown-item>
                                                        </DeleteRecord>
                                                    </el-dropdown-menu>
                                                </template>
                                            </el-dropdown>
                                        </template>
                                        <template v-else>
                                            <el-divider direction="vertical" />
                                            <DialogForm
                                                :key="`coupon-send-log-${row.couponId}`"
                                                :params="{ couponId: row.couponId }"
                                                :show-on-ok="false"
                                                :showFooter="false"
                                                isDrawer
                                                path="promotion/coupon/SendLog"
                                                :title="$t('发放记录')"
                                                width="700px"
                                            >
                                                <a class="btn-link">{{ $t('发放记录') }}</a>
                                            </DialogForm>
                                            <el-divider direction="vertical" />
                                            <el-dropdown trigger="hover">
                                                <el-icon class="btn-link" size="12">
                                                    <MoreFilled />
                                                </el-icon>
                                                <template #dropdown>
                                                    <el-dropdown-menu>
                                                        <DeleteRecord @afterDelete="loadFilter" :requestApi="delCoupon" :params="{ id: row.couponId }">
                                                            <el-dropdown-item>
                                                                <el-button style="width: 100%" link type="primary">{{ $t('删除') }}</el-button>
                                                            </el-dropdown-item>
                                                        </DeleteRecord>
                                                    </el-dropdown-menu>
                                                </template>
                                            </el-dropdown>
                                        </template>
                                    </el-space>
                                </template>
                            </el-table-column>
                            <!-- <el-table-column label="操作" fixed="right" :width="150">
                                <template #default="{ row }">
                                    <DialogForm isDrawer @okCallback="loadFilter" title="编辑示例模板" width="800px" path="product/example/Info"
                                        :params="{ act: 'edit', id: row.exampleId }">
                                        <a class="btn-link">编辑</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord @afterDelete="loadFilter" :requestApi="delCoupon" :params="{ id: row.exampleId }">删除</DeleteRecord>
                                </template>
                            </el-table-column> -->
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
import { MoreFilled } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { DeleteRecord, Switch, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { CouponFilterState, CouponFilterParams } from "@/types/promotion/coupon.d";
import { getCouponList, batchSubmit, updateCouponFiled, delCoupon } from "@/api/promotion/coupon";
import { SelectShop } from "@/components/select";
import { useListRequest } from "@/hooks/useListRequest";
import { useI18n } from "vue-i18n";
const config: any = useConfigStore();
const { t } = useI18n();

/**
 * 使用列表请求Hook
 * @description 封装优惠券列表数据加载、筛选、排序、多选等通用逻辑
 */
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
} = useListRequest<CouponFilterState, CouponFilterParams>({
    apiFunction: getCouponList,
    idKey: "couponId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        isShow: -1,
        shopId: 0,
        page: 1,
        size: config.get("pageSize")
    }
});
/**
 * 批量操作处理
 * @param {string} action - 操作类型（del-删除）
 */
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

const isCouponExpired = (row: any) => {
    if (!row.useEndDate) return false;
    return new Date(row.useEndDate).getTime() < Date.now();
};

/** 列表状态文案：1 进行中 / 2 未开始 / 3 过期（不直接使用后端 statusName） */
const couponStatusText = (status?: number) => {
    if (status === 2) return t("未开始");
    if (status === 3) return t("过期");
    return t("进行中");
};

const moreDropdownRefs = new Map<number, any>();
const setMoreDropdownRef = (el: any, key: number) => {
    if (el) {
        moreDropdownRefs.set(key, el);
    } else {
        moreDropdownRefs.delete(key);
    }
};
const closeMoreDropdown = (key: number) => {
    moreDropdownRefs.get(key)?.handleClose?.();
};

loadFilter();
</script>
<style scoped>
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
    padding: 20px 16px 17px;
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
    box-sizing: border-box;
}

.bonus-wrap .coupon-cards-item {
    min-width: 180px;
    width: 210px;
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
    white-space: normal;
    word-break: break-word;
    overflow-wrap: break-word;
    width: 100%;
}

.bonus-wrap .coupon-cards-item .desc p {
    margin: 0;
    white-space: normal;
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
    width: 240px !important;
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
</style>
