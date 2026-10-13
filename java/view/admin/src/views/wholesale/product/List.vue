/** * @file 批发商品列表 * @description 添加/移出/批量删除 + 批发价/起批量/倍数购买 入口 */
<template>
    <div class="container">
        <div class="page-tips-wrapper" v-if="tipVisible">
            <div class="tips-item">
                <div class="info flex flex-align-center">
                    <div class="icon mr10">
                        <el-icon size="18" color="#ed6a18"><WarnTriangleFilled /></el-icon>
                    </div>
                    <div class="tips-content">{{ $t('批发类商品与{scopeName}各类营销活动互斥，不可叠加优惠', { scopeName }) }}</div>
                </div>
                <div class="btn" @click.stop="tipVisible = false">
                    <el-icon size="18"><Close /></el-icon>
                </div>
            </div>
        </div>
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="list-table-tool-row advanced-search-warp">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('商品名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入商品名称/货号')"
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
                                        <label class="control-label"><span>{{ $t('起批维度：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.moqDimension" clearable :placeholder="$t('请选择起批维度')" @change="onSearchSubmit">
                                                <el-option :value="1" label="SPU" />
                                                <el-option :value="2" label="SKU" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <div class="control-container">
                                            <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                            <el-button plain @click="onReset">{{ $t('重置') }}</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        isDrawer
                                        :showOnOk="false"
                                        @callback="loadData"
                                        :title="$t('添加批发商品')"
                                        width="1000px"
                                        path="wholesale/product/src/AddProductDialog"
                                    >
                                        <el-button type="primary">{{ $t('添加批发商品') }}</el-button>
                                    </DialogForm>
                                    <DialogForm
                                        isDrawer
                                        :disabled="selectedIds.length === 0"
                                        @okCallback="loadData"
                                        :title="$t('批量设置起批量')"
                                        width="600px"
                                        path="wholesale/product/src/BatchBuyRuleDrawer"
                                        :params="{ productIds: selectedIds }"
                                    >
                                        <el-button :disabled="selectedIds.length === 0">{{ $t('批量设置起批量') }}</el-button>
                                    </DialogForm>
                                    <el-popconfirm :title="$t('确认要批量删除所选批发商品吗？')" @confirm="onBatchDelete">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <span v-if="selectedIds.length > 0"
                                        >{{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }}</span
                                    >
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="listData" row-key="productId" @selection-change="onSelectChange">
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('商品名称')" min-width="240">
                                <template #default="{ row }">
                                    <div class="flex">
                                        <img v-if="row.picThumb" :src="imageFormat(row.picThumb)" height="40" width="40" class="mr10" />
                                        <div>
                                            <p>{{ row.productName }}</p>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商品信息')" min-width="220">
                                <template #default="{ row }">
                                    <div class="text-nowrap">{{ $t('编码：{productSn}', { productSn: row.productSn || "--" }) }}</div>
                                    <div class="text-nowrap">{{ $t('售价：{retailPriceRange}', { retailPriceRange: row.retailPriceRange || "--" }) }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('批发价')" min-width="140">
                                <template #default="{ row }">
                                    <span v-if="row.wholesalePriceDisplay">{{ row.wholesalePriceDisplay }}</span>
                                    <span v-else class="gray">{{ $t('未设置') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('起批维度')" width="100">
                                <template #default="{ row }">
                                    <span v-if="row.moqDimensionName">{{ row.moqDimensionName }}</span>
                                    <span v-else class="unset">{{ $t('未设置') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('起批数量')" width="100">
                                <template #default="{ row }">
                                    <span v-if="row.moqDisplay != null">{{ row.moqDisplay }}</span>
                                    <span v-else class="unset">{{ $t('未设置') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('倍数购买')" width="100">
                                <template #default="{ row }">
                                    <span v-if="row.multipleSet">{{ $t('已设置') }}</span>
                                    <span v-else class="unset">{{ $t('未设置') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('库存')" prop="stock" width="90" />
                            <el-table-column :label="$t('上下架状态')" width="100">
                                <template #default="{ row }">{{ row.shelfStatus === 1 ? $t('上架') : $t('下架') }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" width="280">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <DialogForm
                                            isDrawer
                                            @okCallback="loadData"
                                            :title="$t('批发价设置')"
                                            width="1100px"
                                            path="wholesale/product/src/PriceDrawer"
                                            :params="{ productId: row.productId, productName: row.productName }"
                                        >
                                            <a class="btn-link">{{ $t('批发价') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm
                                            isDrawer
                                            @okCallback="loadData"
                                            :title="$t('起批量设置')"
                                            width="700px"
                                            path="wholesale/product/src/BuyRuleDrawer"
                                            :params="{ productId: row.productId }"
                                        >
                                            <a class="btn-link">{{ $t('起批量') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm
                                            isDrawer
                                            @okCallback="loadData"
                                            :title="$t('倍数购买设置')"
                                            width="800px"
                                            path="wholesale/product/src/MultipleDrawer"
                                            :params="{ productId: row.productId }"
                                        >
                                            <a class="btn-link">{{ $t('倍数购买') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <el-dropdown :ref="(el: any) => setDropdownRef(row.productId, el)" trigger="hover">
                                            <el-icon class="btn-link" size="12"><MoreFilled /></el-icon>
                                            <template #dropdown>
                                                <el-dropdown-menu>
                                                    <el-dropdown-item>
                                                        <DeleteRecord
                                                            :params="{ productIds: [row.productId] }"
                                                            :requestApi="batchDeleteWholesaleProduct"
                                                            :title="$t('确认删除该批发商品？')"
                                                            @afterDelete="loadData"
                                                        >
                                                            <el-button style="width: 100%" link type="primary" @click="closeDropdown(row.productId)"
                                                                >{{ $t('删除') }}</el-button
                                                            >
                                                        </DeleteRecord>
                                                    </el-dropdown-item>
                                                </el-dropdown-menu>
                                            </template>
                                        </el-dropdown>
                                    </el-space>
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
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadData" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import "@/style/css/list.less";
import { MoreFilled, WarnTriangleFilled, Close } from "@element-plus/icons-vue";
import { message } from "ant-design-vue";
import { ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination } from "@/components/list";
import { useListRequest } from "@/hooks/useListRequest";
import { useConfigStore } from "@/store/config";
import { imageFormat } from "@/utils/format";
import { getAdminType, getShopType } from "@/utils/storage";
import { getWholesaleProductPage, batchDeleteWholesaleProduct } from "@/api/wholesale/product";
import type { WholesaleProductVO, WholesaleProductPageQuery } from "@/types/wholesale/product";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const tipVisible = ref<boolean>(true);
const scopeName = getAdminType() === "shop" ? (getShopType() === 2 ? t('门店') : t('店铺')) : t('平台');
const { listData, loading, total, selectedIds, filterParams, loadData, onSearchSubmit, onSelectChange, resetParams } = useListRequest<
    WholesaleProductVO,
    WholesaleProductPageQuery
>({
    apiFunction: getWholesaleProductPage,
    idKey: "productId",
    defaultParams: {
        keyword: "",
        moqDimension: undefined,
        page: 1,
        size: config.get("pageSize")
    }
});

// 操作栏下拉为 hover 触发，DialogForm 触发器带 @click.stop 会拦截冒泡，hide-on-click 失效，
// 故按行持有 el-dropdown 实例，点击菜单项时手动收起，避免抽屉打开后下拉仍悬浮显示。
const dropdownRefs = new Map<number, any>();
const setDropdownRef = (id: number, el: any) => {
    if (el) dropdownRefs.set(id, el);
    else dropdownRefs.delete(id);
};
const closeDropdown = (id: number) => {
    dropdownRefs.get(id)?.handleClose?.();
};

const onReset = () => {
    resetParams();
};

const onBatchDelete = async () => {
    if (selectedIds.value.length === 0) {
        message.warning(t('请至少选择一项'));
        return;
    }
    try {
        await batchDeleteWholesaleProduct({ productIds: selectedIds.value as number[] });
        message.success(t('操作成功'));
        loadData();
    } catch (error: any) {
        message.error(error.message || t('操作失败'));
    }
};

loadData();
</script>
<style lang="less" scoped>
.unset {
    color: #d9d9d9;
}
.text-nowrap {
    white-space: nowrap;
}
.page-tips-wrapper {
    margin-bottom: 12px;
    .tips-item {
        border: #ffe6d7 1px solid;
        cursor: pointer;
        background-color: #ffefe6;
        padding: 10px 20px;
        line-height: 24px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .icon,
        .btn {
            margin-top: 5px;
        }
    }
}
</style>
