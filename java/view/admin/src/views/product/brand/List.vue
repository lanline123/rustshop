/** * @file 品牌列表页面 * @description 商品品牌管理列表，支持品牌管理和品牌审核两个Tab切换。 * @features * - 品牌管理Tab：查看、添加、编辑、删除品牌 * -
品牌审核Tab：审核待审核的品牌（多商户/门店/供应链版） * - 多条件筛选（品牌名称、店铺/门店、显示状态、热门状态、审核状态、首字母） * -
行内编辑（品牌名称、首字母、排序、显示状态、热销状态） * - 批量操作（删除、更新首字母） * - 海外版支持批量翻译品牌名称 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="tabs-box" v-if="adminType === 'admin' && (isMerchant() || isStore() || isS2b2c())">
                    <el-tabs v-model="activeKey" tab-position="top" @tab-change="onTabChange">
                        <el-tab-pane :name="1" :label="$t('品牌管理')"></el-tab-pane>
                        <el-tab-pane :name="2">
                            <template #label>
                                <el-badge :value="count" color="#f33" :offset="[5, -3]" :hidden="count === 0"> {{ $t('品牌审核') }} </el-badge>
                            </template>
                        </el-tab-pane>
                    </el-tabs>
                </div>
                <!-- <el-badge :value="item.count" color="#f33" :offset="[-5, 5]" :hidden="item.count === 0">
                    <div class="item" @click="onTabChange(item.value)" :class="modelValue == item.value ? 'active' : ''">
                        {{ item.label }}
                    </div>
                </el-badge> -->
                <div class="list-table-tool lyecs-search-warp">
                    <div class="advanced-search-warp list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('商品品牌：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="keyword"
                                            :placeholder="$t('输入品牌名称')"
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
                            <div v-if="adminType != 'shop' && isMerchant()" class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('店铺：') }}</span></label>
                                    <div class="control-container">
                                        <SelectShop v-model:shopId="filterParams.shopId" @onChange="onSearchSubmit"> </SelectShop>
                                    </div>
                                </div>
                            </div>
                            <div v-if="adminType != 'shop' && isStore()" class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('门店：') }}</span></label>
                                    <div class="control-container">
                                        <SelectShop
                                            v-model:shopId="filterParams.shopId"
                                            :type="2"
                                            :placeholder="$t('请输入门店名称')"
                                            @onChange="onSearchSubmit"
                                        ></SelectShop>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('是否显示：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.isShow" clearable @change="onSearchSubmit">
                                            <!-- <el-option :value="-1" label="请选择..."/> -->
                                            <el-option :value="0" :label="$t('不显示')" />
                                            <el-option :value="1" :label="$t('显示')" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field" v-if="adminType === 'admin'">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('是否热门：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.brandIsHot" clearable @change="onSearchSubmit">
                                            <!-- <el-option :value="-1" label="请选择..."/> -->
                                            <el-option :value="0" :label="$t('否')" />
                                            <el-option :value="1" :label="$t('是')" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field" v-if="adminType === 'shop' || activeKey === 2">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('审核状态：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.status" clearable @change="onSearchSubmit">
                                            <el-option :value="0" :label="$t('待审核')" />
                                            <el-option :value="1" :label="$t('审核通过')" />
                                            <el-option :value="2" :label="$t('审核拒绝')" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('首字母：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput v-model="filterParams.firstWord" @keyup.enter="onSearchSubmit" clearable @clear="onSearchSubmit" />
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
                </div>
                <div class="list-table-tool-row">
                    <div class="list-table-tool-col">
                        <el-space>
                            <DialogForm
                                v-if="activeKey === 1"
                                :params="{ act: 'add' }"
                                isDrawer
                                path="product/brand/Info"
                                :title="$t('添加品牌')"
                                width="600px"
                                @okCallback="loadFilter"
                            >
                                <el-button type="primary">{{ $t('添加品牌') }}</el-button>
                            </DialogForm>
                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                <template #reference>
                                    <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                </template>
                            </el-popconfirm>
                            <el-button :loading="false" :disabled="selectedIds.length === 0" @click="onUpdateFirstWorld">{{ $t('批量更新品牌首字母') }} </el-button>
                            <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                        </el-space>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="brandId"
                            show-overflow-tooltip
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :width="200" :label="$t('品牌名称')" prop="brandId">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.brandName"
                                        :max="30"
                                        :params="{ id: row.brandId, field: 'brandName' }"
                                        :requestApi="updateBrandField"
                                        :label="$t('品牌名称')"
                                        type="textarea"
                                    >
                                        {{ row.brandName }}
                                    </PopForm>
                                    <p v-if="row.shop" class="span-product-brand">
                                        <span class="span-tit">{{ getShopType() === 2 ? $t('门店') : $t('店铺') }}：</span
                                        ><span class="span-con green">{{ row.shop.shopTitle }}</span>
                                    </p>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('品牌缩略图')" width="100">
                                <template #default="{ row }">
                                    <Image v-if="row.brandLogo" :src="imageFormat(row.brandLogo)" fit="contain" style="height: 25px; width: 60px" />
                                    <span v-else>{{ $t('暂无图片') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('首字母')">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.firstWord"
                                        :len="1"
                                        :params="{ id: row.brandId, field: 'firstWord' }"
                                        :requestApi="updateBrandField"
                                        :label="$t('首字母')"
                                        type="input"
                                    >
                                        <div>{{ row.firstWord || "-" }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否热销')" v-if="adminType === 'admin' && activeKey === 1">
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.brandIsHot"
                                        :params="{ id: row.brandId, field: 'brandIsHot' }"
                                        :requestApi="updateBrandField"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否显示')" prop="isShow" sortable="custom">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.isShow" :params="{ id: row.brandId, field: 'isShow' }" :requestApi="updateBrandField" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('审核状态')" prop="status" v-if="adminType === 'shop' || activeKey === 2">
                                <template #default="{ row }">
                                    {{ row.statusText }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('审核备注')" prop="rejectRemark" v-if="adminType === 'shop' || activeKey === 2">
                                <template #default="{ row }">
                                    {{ row.rejectRemark || "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom" v-if="adminType === 'admin' && activeKey === 1">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.sortOrder"
                                        :params="{ id: row.brandId, field: 'sortOrder' }"
                                        :requestApi="updateBrandField"
                                        :extra="$t('默认值为50，数值越小，排序越靠前')"
                                        :label="$t('排序')"
                                        type="integer"
                                    >
                                        <div>{{ row.sortOrder }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        v-if="activeKey === 1 && row.status != 0"
                                        :params="{ act: 'detail', id: row.brandId }"
                                        isDrawer
                                        path="product/brand/Info"
                                        :title="$t('编辑品牌')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider v-if="activeKey === 1 && row.status != 0" direction="vertical" />
                                    <DialogForm
                                        v-if="adminType === 'shop' && activeKey === 1 && row.status === 0"
                                        :params="{ act: 'detail', id: row.brandId, examine: 1 }"
                                        isDrawer
                                        :showClose="false"
                                        :showOnOk="false"
                                        path="product/brand/Info"
                                        :title="$t('品牌详情')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('详情') }}</a>
                                    </DialogForm>
                                    <el-divider v-if="adminType === 'shop' && activeKey === 1 && row.status === 0" direction="vertical" />
                                    <DialogForm
                                        v-if="activeKey === 2 && row.status != 0"
                                        :params="{ act: 'detail', id: row.brandId, examine: 1 }"
                                        isDrawer
                                        :showClose="false"
                                        :showOnOk="false"
                                        path="product/brand/Info"
                                        :title="$t('品牌详情')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('详情') }}</a>
                                    </DialogForm>
                                    <el-divider v-if="activeKey === 2 && row.status != 0" direction="vertical" />
                                    <DialogForm
                                        v-if="activeKey === 2 && row.status === 0"
                                        dialogClass="noPadding"
                                        :params="{ act: 'detail', id: row.brandId }"
                                        isDrawer
                                        path="product/brand/Examine"
                                        :title="$t('品牌审核')"
                                        width="600px"
                                        @okCallback="_getBrandAuditWaitNum"
                                    >
                                        <a class="btn-link">{{ $t('去处理') }}</a>
                                    </DialogForm>
                                    <el-divider v-if="activeKey === 2 && row.status === 0" direction="vertical" />
                                    <DeleteRecord :params="{ id: row.brandId }" :requestApi="delBrand" @afterDelete="loadFilter">{{ $t('删除') }} </DeleteRecord>
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
import { imageFormat } from "@/utils/format";
import { Image } from "@/components/image";
import { computed, ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { SelectShop } from "@/components/select";
import { BrandFilterParams, BrandFilterState } from "@/types/product/brand";
import { batchSubmit, delBrand, getBrandList, updateBrandField, updateFirstWorld, getAuditBrandList, getBrandAuditWaitNum } from "@/api/product/brand";
import { useListRequest } from "@/hooks/useListRequest";
import { isOverseas, isMerchant, isStore, isS2b2c } from "@/utils/version";
import { getAdminType, getShopType } from "@/utils/storage";
/** 当前管理员类型 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const adminType = getAdminType();
const config: any = useConfigStore();
/** 当前激活的Tab：1-品牌管理，2-品牌审核 */
const activeKey = ref<1 | 2>(1);
/** 待审核品牌数量 */
const count = ref(0);
/** API函数映射，根据Tab切换不同的接口 */
const apiFunctions = {
    1: getBrandList, // 品牌管理
    2: getAuditBrandList // 待审核品牌
};

// 使用计算属性返回当前选中的API函数
const currentApiFunction = computed(() => apiFunctions[activeKey.value]);

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
} = useListRequest<BrandFilterState, BrandFilterParams>({
    apiFunction: currentApiFunction,
    idKey: "brandId",
    defaultParams: {
        page: 1,
        isShow: "",
        brandIsHot: "",
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        firstWord: "",
        shopId: "",
        status: adminType == "admin" ? 1 : undefined
    }
});
/**
 * @description 批量操作处理
 * @param action 操作类型：'del'-批量删除
 */
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();
/**
 * @description Tab切换处理
 * @param val Tab值：1-品牌管理，2-品牌审核
 */
const onTabChange = (val: 1 | 2) => {
    activeKey.value = val;
    filterParams.page = 1;
    if (val === 2) {
        filterParams.status = 0;
    } else {
        filterParams.status = adminType == "admin" ? 1 : -1;
    }
    loadFilter();
    if (isMerchant()) {
        _getBrandAuditWaitNum();
    }
};
/**
 * @description 获取待审核品牌数量，用于Tab角标显示
 */
const _getBrandAuditWaitNum = async () => {
    try {
        const result = await getBrandAuditWaitNum();
        count.value = result;
    } catch (error: any) {
        message.error(error.message);
    }
    loadFilter();
};
if (isMerchant()) {
    _getBrandAuditWaitNum();
}

/**
 * @description 批量更新品牌首字母
 * 根据品牌名称自动生成首字母
 */
const onUpdateFirstWorld = async () => {
    try {
        const result = await updateFirstWorld();
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};
</script>
