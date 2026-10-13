<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="bonus-wrap">
                <p class="subtitle">{{ $t('按使用场景创建') }}</p>
                <div class="card-wrap">
                    <div class="card-wrap__container">
                        <div class="item-bg coupon-cards-item">
                            <div>
                                <div class="title">{{ $t('商品边框') }}</div>
                                <div class="desc">
                                    <p>
                                        {{ $t('为商品列表页的商品图片批量统一') }}{{ $t('添加边框样式。') }}
                                        <el-popover placement="right-end" :width="300" trigger="click">
                                            <template #reference>
                                                <a class="btn-link">{{ $t('示例') }}</a>
                                            </template>
                                            <img src="@/assets/promotion/productStyle/borderStyle.png" style="width: 280px" />
                                        </el-popover>
                                    </p>
                                </div>
                            </div>
                            <DialogForm
                                isDrawer
                                :showFooter="false"
                                @okCallback="loadFilter"
                                :title="$t('新增边框背景')"
                                width="1000px"
                                path="promotion/productStyle/Info"
                                :params="{ act: 'add', styleType: 1 }"
                            >
                                <el-button type="primary">{{ $t('立即创建') }}</el-button>
                            </DialogForm>
                        </div>
                        <div class="item-bg coupon-cards-item">
                            <div>
                                <div class="title">{{ $t('活动背景') }}</div>
                                <div class="desc">
                                    <p>
                                        {{ $t('为活动商品或普通商品的模块商品') }}{{ $t('详情页批量设置统一背景图。') }}
                                        <el-popover placement="right-end" :width="300" trigger="click">
                                            <template #reference>
                                                <a class="btn-link">{{ $t('示例') }}</a>
                                            </template>
                                            <img :src="activityStylePreview" style="width: 280px" />
                                        </el-popover>
                                    </p>
                                </div>
                            </div>
                            <DialogForm
                                isDrawer
                                :showFooter="false"
                                @okCallback="loadFilter"
                                :title="$t('新增活动背景')"
                                width="1000px"
                                path="promotion/productStyle/Info"
                                :params="{ act: 'add', styleType: 2 }"
                            >
                                <el-button type="primary">{{ $t('立即创建') }}</el-button>
                            </DialogForm>
                        </div>
                    </div>
                </div>
            </div>
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('活动名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('请输入活动名称搜索')"
                                                clearable
                                                @keyup.enter="onSearchSubmit"
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
                                        <label class="control-label"><span>{{ $t('活动类型：') }}</span></label>
                                        <div class="control-container">
                                            <el-select
                                                v-model="filterParams.styleType"
                                                :placeholder="$t('全部')"
                                                clearable
                                                @clear="onSearchSubmit"
                                                @change="onSearchSubmit"
                                            >
                                                <el-option :label="$t('商品边框')" :value="1" />
                                                <el-option :label="$t('活动背景')" :value="2" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('活动状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select
                                                v-model="filterParams.activityStatus"
                                                :placeholder="$t('全部')"
                                                clearable
                                                @clear="onSearchSubmit"
                                                @change="onSearchSubmit"
                                            >
                                                <el-option :label="$t('活动未开始')" :value="1" />
                                                <el-option :label="$t('活动进行中')" :value="2" />
                                                <el-option :label="$t('活动已结束')" :value="3" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('活动时间：') }}</span></label>
                                        <SelectTimeInterval
                                            v-model:start-date="filterParams.startTime"
                                            v-model:end-date="filterParams.endTime"
                                            :clearable="false"
                                            type="date"
                                            value-format="YYYY-MM-DD"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <el-button type="primary" @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                    <el-button @click="resetParams">{{ $t('重置') }}</el-button>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            row-key="styleId"
                            @selection-change="onSelectChange"
                            :total="total"
                            @sort-change="onSortChange"
                            :loading="loading"
                        >
                            <el-table-column label="ID" prop="styleId" width="60" />
                            <el-table-column :label="$t('名称')" prop="styleName" min-width="150">
                                <template #default="{ row }">
                                    <div>{{ row.styleName || "--" }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('类型')" prop="styleTypeName">
                                <template #default="{ row }">
                                    {{ row.styleTypeName }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商品数')" prop="productCount" sortable="custom">
                                <template #default="{ row }">
                                    <DialogForm
                                        isDrawer
                                        :showFooter="false"
                                        :title="$t('{styleName} - 商品列表', { styleName: row.styleName })"
                                        width="600px"
                                        path="promotion/productStyle/src/StyleProductList"
                                        :params="{ id: row.styleId }"
                                    >
                                        <a class="btn-link">{{ row.productCount }}</a>
                                    </DialogForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('活动状态')">
                                <template #default="{ row }">
                                    <span>{{ row.activityStatusName || "--" }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('起止时间')">
                                <template #default="{ row }">
                                    <span v-if="row.startTime && row.endTime"> {{ row.startTime }} - {{ row.endTime }} </span>
                                    <span v-else>--</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否开启')" prop="status">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.status" :requestApi="updateProductStyleField" :params="{ id: row.styleId, field: 'status' }" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="addTime">
                                <template #default="{ row }">
                                    {{ row.addTime ? row.addTime : "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" width="120">
                                <template #default="{ row }">
                                    <DialogForm
                                        isDrawer
                                        :showFooter="false"
                                        @okCallback="loadFilter"
                                        :title="row.styleType === 1 ? $t('编辑边框背景') : $t('编辑活动背景')"
                                        width="1000px"
                                        path="promotion/productStyle/Info"
                                        :params="{ act: 'detail', id: row.styleId, styleType: row.styleType }"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord @afterDelete="loadFilter" :requestApi="delProductStyle" :params="{ id: row.styleId }"> {{ $t('删除') }} </DeleteRecord>
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
import { DeleteRecord, Switch, Pagination } from "@/components/list";
import { SelectTimeInterval } from "@/components/select";
import { useConfigStore } from "@/store/config";
import type { ProductStyleFilterState, ProductStyleFilterParams } from "@/types/promotion/productStyle";
import { getProductStyleList, updateProductStyleField, delProductStyle } from "@/api/promotion/productStyle";
import { useListRequest } from "@/hooks/useListRequest";
import { formatTimestamp } from "@/utils/time";
import { isOverseas } from "@/utils/version";
import activityStyleImage from "@/assets/promotion/productStyle/activityStyle.png";
import activityStyleOverseasImage from "@/assets/promotion/productStyle/activityStyleOverseas.png";

const config: any = useConfigStore();
const activityStylePreview = isOverseas() ? activityStyleOverseasImage : activityStyleImage;

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
    resetParams
} = useListRequest<ProductStyleFilterState, ProductStyleFilterParams>({
    apiFunction: getProductStyleList,
    idKey: "styleId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        styleType: "",
        activityStatus: "",
        startTime: "",
        endTime: ""
    }
});


loadFilter();
</script>

<style scoped lang="less">
.status-active {
    color: #67c23a;
}
.status-ended {
    color: #f56c6c;
}
.status-pending {
    color: #e6a23c;
}
.status-closed {
    color: #909399;
}

.bonus-wrap .card-wrap {
    display: flex;
    margin-bottom: 24px;
    justify-content: flex-start;
    overflow: hidden;
}
.bonus-wrap .card-wrap__container {
    transition: transform 0.6s ease-in-out;
    display: flex;
}
.bonus-wrap .item-bg {
    position: relative;
    padding: 20px 27px 17px;
    display: flex;
    flex-direction: column;
    background: var(--tig-item-active-bg);
    margin-right: 15px;
    text-align: center;
    align-items: center;
    justify-content: space-between;
    box-sizing: border-box;
}
.bonus-wrap .coupon-cards-item {
    min-width: 220px;
    width: 280px;
}
.bonus-wrap .coupon-cards-item .title {
    font-size: 16px;
    color: #333;
    font-weight: 600;
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
