/**
 * @file 询价管理列表
 * @description 商品询价记录管理页面
 * @features
 * - 按状态筛选（已回复/未回复）
 * - 按商品ID和手机号搜索
 * - 查看询价详情和回复
 * - 批量删除询价记录
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.status" clearable @change="onSearchSubmit">
                                                <el-option :value="0" :label="$t('未回复')" />
                                                <el-option :value="1" :label="$t('已回复')" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('商品名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入商品名称/货号')"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('手机号：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.mobile"
                                                name="mobile"
                                                :placeholder="$t('输入询价人手机号')"
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
                            :loading="loading"
                            :total="total"
                            row-key="id"
                            @selection-change="onSelectChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('商品名称')" prop="productId" :min-width="180">
                                <template #default="{ row }">
                                    <div class="flex">
                                        <div v-if="row.product.picThumb" class="span-pic">
                                            <a :href="urlFormat({ path: 'product', sn: row.product.productSn })" target="_blank"
                                                ><img :src="imageFormat(row.product.picThumb)" height="40" width="40" alt=""
                                            /></a>
                                        </div>
                                        <div class="span-product-con">
                                            <div class="span-product-name">
                                                <div>{{ $t('商品名称：{productName}', { productName: row.product.productName }) }}</div>
                                                <div>{{ $t('商品SN：{productSn}', { productSn: row.product.productSn }) }}</div>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('询价内容')" prop="content"></el-table-column>
                            <el-table-column :width="100" :label="$t('状态')">
                                <template #default="{ row }">
                                    <span v-if="row.status === 1" class="green">{{ $t('已回复') }}</span>
                                    <span v-else class="gray">{{ $t('未回复') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :width="150" :label="$t('联系电话')">
                                <template #default="{ row }">
                                    <MobileCard :mobile="row.mobile"></MobileCard>
                                </template>
                            </el-table-column>
                            <el-table-column :width="150" :label="$t('创建时间')" prop="createTime"></el-table-column>
                            <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.id }"
                                        isDrawer
                                        :showOnOk="false"
                                        path="product/enquiry/Info"
                                        :title="isReplyable(row) ? $t('回复询价') : $t('查看')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ isReplyable(row) ? $t('回复') : $t('详情') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.id }" :requestApi="delPriceInquiry" @afterDelete="loadFilter">{{ $t('删除') }} </DeleteRecord>
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
import { DialogForm } from "@/components/dialog";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { PriceInquiryFilterParams, PriceInquiryFilterState } from "@/types/product/enquiry";
import { batchSubmit, delPriceInquiry, getPriceInquiryList } from "@/api/product/enquiry";
import { useListRequest } from "@/hooks/useListRequest";
import { imageFormat, urlFormat } from "@/utils/format";
const config: any = useConfigStore();
// 使用列表请求 Hook
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSelectChange,
    onBatchAction,
    resetParams
} = useListRequest<PriceInquiryFilterState, PriceInquiryFilterParams>({
    apiFunction: getPriceInquiryList,
    idKey: "id",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        status: "",
        mobile: "",
        keyword: ""
    }
});

// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

const isReplyable = (row: PriceInquiryFilterState) => {
    return row.status !== 1 && Number(row.userId) > 0;
};

// 初始化加载
loadFilter();
</script>
