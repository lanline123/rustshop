/** * @file 商品列表页面 * @description 商城后台商品管理列表，支持商品上下架、库存管理、批量操作等功能 * @features * -
商品状态Tab切换（出售中/自营/店铺/下架/审核/回收站） * - 多条件筛选（商品名称/货号/分类/品牌/类型/销售模式） * - 行内编辑（商品名称/货号/价格/库存/排序） * -
批量操作（上架/下架/移入回收站/还原/删除/审核） * - 商品推广功能 * - O2O门店版支持分配组织 * - S2B2C模式支持导入供应商商品 * -
预约类商品（上门/到店）库存日历管理 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <TigTabs v-model="activeKey" :tabs="productStatusList" @onTabChange="onTabChange"></TigTabs>
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" name="form">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
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
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div v-if="activeKey !== 2 && getAdminType() != 'shop' && isMerchant()" class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('店铺：') }}</span></label>
                                        <div class="control-container">
                                            <SelectShop v-model:shopId="filterParams.shopId" @onChange="SelectShopChange"></SelectShop>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('分类：') }}</span></label>
                                        <div class="control-container">
                                            <SelectCategory ref="selectCategoryRef" v-model:categoryId="filterParams.categoryId"></SelectCategory>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('品牌：') }}</span></label>
                                        <div class="control-container">
                                            <SelectBrand v-model:brandId="filterParams.brandId" @onChange="onSearchSubmit"></SelectBrand>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('类型 ：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.introType" clearable @change="onSearchSubmit">
                                                <el-option :label="$t('精品')" value="isBest" />
                                                <el-option :label="$t('热销')" value="isHot" />
                                                <el-option :label="$t('新品')" value="isNew" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('商品类型 ：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.productType" clearable @change="onSearchSubmit">
                                                <el-option :label="$t('普通商品')" :value="1" />
                                                <el-option :label="$t('卡密商品')" :value="3" />
                                                <el-option :label="$t('虚拟商品')" :value="2" />
                                                <el-option :label="$t('付费内容')" :value="4" />
                                                <el-option :label="$t('实体礼品卡')" :value="7" />
                                                <el-option :label="$t('电子礼品卡')" :value="8" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div v-if="!isStore() && !isOverseas()" class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('销售模式：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.saleMode" :placeholder="$t('全部')" clearable @change="onSearchSubmit">
                                                <el-option :label="$t('现货销售')" :value="0" />
                                                <el-option :label="$t('全款预售')" :value="1" />
                                                <el-option :label="$t('定金预售')" :value="2" />
                                            </el-select>
                                        </div>
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
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <!--o2o 门店端添加商品按钮  -->
                                    <DialogForm
                                        v-if="getAdminType() === 'shop' && storeSettingInfo.storeIndependentGoods == 1 && getShopType() > 1"
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="product/product/Info"
                                        :title="$t('添加商品')"
                                        width="950px"
                                        @okCallback="loadData"
                                    >
                                        <el-button type="primary">{{ $t('添加商品') }}</el-button>
                                    </DialogForm>
                                    <!--平台端&p店铺端添加商品按钮  -->
                                    <DialogForm
                                        v-if="(getAdminType() === 'shop' && getShopType() == 1) || getAdminType() === 'admin'"
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="product/product/Info"
                                        :title="$t('添加商品')"
                                        width="950px"
                                        @okCallback="loadData"
                                    >
                                        <el-button type="primary">{{ $t('添加商品') }}</el-button>
                                    </DialogForm>
                                    <!--o2o 门店端导入供应商商品按钮  -->
                                    <!--平台端&p店铺端导入供应商商品按钮  -->
                                    <!-- <DialogForm
                                        v-if="isS2b2c() && getAdminType() === 'shop' && storeSettingInfo.storeIndependentGoods == 1 && getShopType() > 1"
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="product/product/vendor/ProductList"
                                        title="导入供应商商品"
                                        width="1100px"
                                        :show-footer="false"
                                        @callback="loadFilter"
                                    >
                                        <el-button type="primary">导入供应商商品</el-button>
                                    </DialogForm> -->
                                    <el-popconfirm
                                        v-if="activeKey == 4"
                                        :title="$t('您确认要批量移入回收站吗？')"
                                        @confirm="onBatchSubmit('recycle')"
                                    >
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量移入回收站') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <el-popconfirm v-if="activeKey == 7" :title="$t('您确认要批量还原吗？')" @confirm="onBatchSubmit('restore')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量还原') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <el-popconfirm v-if="activeKey == 7" :title="$t('您确认要批量删除吗？')" @confirm="onBatchSubmit('del')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <el-popconfirm v-if="activeKey == 4" :title="$t('您确认要批量上架吗？')" @confirm="onBatchSubmit('up')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量上架') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <el-popconfirm v-if="activeKey == 1" :title="$t('您确认要批量下架吗？')" @confirm="onBatchSubmit('down')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量下架') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <el-popconfirm
                                        v-if="activeKey == 5 && getAdminType() === 'admin'"
                                        :title="$t('您确认要批量通过审核吗？')"
                                        @confirm="onBatchSubmit('audit')"
                                    >
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量通过审核') }}</el-button>
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
                        <el-table :data="filterState" :total="total" row-key="productId" @selection-change="onSelectChange" @sort-change="onSortChange">
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('商品名称')" prop="productId" sortable="custom" :min-width="420">
                                <template #default="{ row }">
                                    <div class="flex">
                                        <div v-if="row.picThumb" class="span-pic">
                                            <a :href="urlFormat({ path: 'product', sn: row.productSn, id: row.productId })" target="_blank">
                                                <img :src="imageFormat(row.picThumb)" height="68" width="68" alt="" style="object-fit: cover" />
                                            </a>
                                        </div>
                                        <div class="span-product-con">
                                            <div class="span-product-name" style="word-break: break-all">
                                                <PopForm
                                                    v-if="canInlineEditName(row)"
                                                    :max="productNameMaxLength"
                                                    v-model:org-value="row.productName"
                                                    :params="{ id: row.productId, field: 'productName' }"
                                                    :requestApi="updateProductFiled"
                                                    :label="$t('商品名称')"
                                                    type="textarea"
                                                >
                                                    <div>{{ row.productName }}</div>
                                                </PopForm>
                                                <div v-else>{{ row.productName }}</div>
                                            </div>
                                            <p v-if="row.brandName" class="span-product-brand">
                                                <span class="span-tit">{{ $t('商品：') }}</span><span class="span-con">{{ row.brandName }}</span>
                                            </p>
                                            <p v-if="row.shopSimple && isMerchant()" class="span-product-brand">
                                                <span class="span-tit">{{ $t('店铺：') }}</span><span class="span-con green">{{ row.shopSimple.shopTitle }}</span>
                                            </p>
                                            <p v-if="row.shop && isMerchant()" class="span-product-brand">
                                                <span class="span-tit">{{ $t('店铺：') }}</span><span class="span-con green">{{ row.shop.shopTitle }}</span>
                                            </p>
                                            <p v-if="row.type === 2"><span class="span-con green">{{ $t('【虚拟商品】') }}</span></p>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商品信息')" :min-width="120">
                                <template #default="{ row }">
                                    <div>
                                        <div>
                                            <span class="span-tit">{{ $t('编码：') }}</span>
                                            <div class="span-con">
                                                <PopForm
                                                    v-if="canInlineEditSn(row)"
                                                    v-model:org-value="row.productSn"
                                                    :params="{ id: row.productId, field: 'productSn' }"
                                                    :requestApi="updateProductFiled"
                                                    :label="$t('货号')"
                                                    :max="20"
                                                >
                                                    <div>{{ row.productSn || "--" }}</div>
                                                </PopForm>
                                                <div v-else>{{ row.productSn || "--" }}</div>
                                            </div>
                                        </div>
                                        <div>
                                            <span class="span-tit">{{ $t('售价：') }}</span>
                                            <div class="span-con">
                                                <ProductPricePop
                                                    v-if="canInlineEditPrice(row) && row.skuCount > 1"
                                                    :params="{ id: row.productId, productInfo: row }"
                                                    :requestApi="(data: any) => updateProductPrice(data, row)"
                                                    @callback="loadFilter"
                                                >
                                                    <div>{{ row.productPrice || 0.0 }}</div>
                                                </ProductPricePop>
                                                <PopForm
                                                    v-else-if="canInlineEditPrice(row)"
                                                    :extra="$t('请输入商品价格')"
                                                    isPrice
                                                    v-model:org-value="row.productPrice"
                                                    :params="{ id: row.productId, field: 'productPrice' }"
                                                    :requestApi="(data: any) => updateProductPrice(data, row)"
                                                    :label="$t('售价')"
                                                    type="decimal"
                                                >
                                                    <div>{{ row.productPrice || 0.0 }}</div>
                                                </PopForm>
                                                <div v-else>{{ row.productPrice || 0.0 }}</div>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column v-if="activeKey != 7 && activeKey != 5 && activeKey != 6" :label="$t('是否上架')">
                                <template #default="{ row }">
                                    <div class="status-switch">
                                        <div v-if="row.checkStatus == 1">
                                            <Switch
                                                v-model:checked="row.productStatus"
                                                :params="{ id: row.productId, field: 'productStatus' }"
                                                :requestApi="updateProductFiled"
                                                :beforeChange="() => beforeProductStatusChange(row)"
                                            />
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column v-if="activeKey === 5 || activeKey === 6" :label="$t('审核状态')">
                                <template #default="{ row }">
                                    <span v-if="row.checkStatus === 2" class="red tips-hover" :title="row.checkReason"
                                        >{{ $t('审核失败') }} <i class="ico-font">&#xe611;</i></span
                                    >
                                    <span v-if="row.checkStatus === 0" class="tips-hover gray" :title="row.checkReason">{{ $t('审核中') }}</span><br />
                                </template>
                            </el-table-column>
                            <el-table-column v-if="activeKey === 6" :label="$t('失败原因')" show-overflow-tooltip>
                                <template #default="{ row }">
                                    {{ row.checkReason }}
                                </template>
                            </el-table-column>
                            <el-table-column v-if="activeKey != 7" :label="$t('库存')" prop="productStock" sortable="custom">
                                <template #default="{ row }">
                                    <el-button
                                        type="primary"
                                        link
                                        v-if="(row.productType === 5 || row.productType === 6) && activeKey != 5"
                                        @click="goSetStock(row)"
                                        >{{ $t('去设置') }}</el-button
                                    >
                                    <div v-else>
                                        <ProductStockPop
                                            v-if="canInlineEditStock(row)"
                                            :params="{ id: row.productId, productInfo: row }"
                                            :requestApi="updateProductFiled"
                                            @callback="loadFilter"
                                        >
                                            <div>{{ row.productStock }}</div>
                                        </ProductStockPop>
                                        <div v-else>{{ row.productStock }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column v-if="activeKey != 7" :label="$t('排序')" prop="sortOrder" sortable="custom">
                                <template #header>
                                    <el-tooltip class="box-item" effect="light" placement="top" show-after="300">
                                        <template #content>
                                            <div style="width: 100px; padding: 5px 10px">{{ $t('排序数值越小，用户端展示越靠前') }}</div>
                                        </template>
                                        <div class="flex flex-align-center">
                                            <div>{{ $t('排序') }}</div>
                                            <el-icon style="margin-left: 5px" size="14" color="#999">
                                                <QuestionFilled />
                                            </el-icon>
                                        </div>
                                    </el-tooltip>
                                </template>
                                <template #default="{ row }">
                                    <PopForm
                                        v-if="canInlineEditSort(row)"
                                        v-model:org-value="row.sortOrder"
                                        :params="{ id: row.productId, field: 'sortOrder' }"
                                        :requestApi="updateProductFiled"
                                        :label="$t('排序')"
                                        type="integer"
                                    >
                                        <div>{{ row.sortOrder }}</div>
                                    </PopForm>
                                    <div v-else>{{ row.sortOrder }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="operationColumnWidth" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <DialogForm
                                            :maskClose="false"
                                            v-if="activeKey == 5 && row.checkStatus === 0"
                                            :params="{ act: 'detail', id: row.productId, examine: 1 }"
                                            :showClose="false"
                                            :showOnOk="false"
                                            isDrawer
                                            path="product/product/Info"
                                            :title="$t('商品详情')"
                                            width="900px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t('详情') }}</a>
                                        </DialogForm>
                                        <el-divider v-if="activeKey == 5 && row.checkStatus === 0 && getAdminType() === 'admin'" direction="vertical" />
                                        <DialogForm
                                            :maskClose="false"
                                            v-if="activeKey == 6 && row.checkStatus === 2"
                                            :params="{ act: 'again', id: row.productId }"
                                            dialogClass="noPadding"
                                            isDrawer
                                            path="product/product/Info"
                                            :title="$t('修改商品（重新审核）')"
                                            width="600px"
                                            @okCallback="loadData"
                                        >
                                            <a class="btn-link">{{ $t('重新审核') }}</a>
                                        </DialogForm>
                                        <DialogForm
                                            :maskClose="false"
                                            v-if="activeKey == 5 && row.checkStatus === 0 && getAdminType() === 'admin'"
                                            :params="{ act: 'detail', id: row.productId }"
                                            dialogClass="noPadding"
                                            isDrawer
                                            path="product/product/Examine"
                                            :title="$t('商品审核')"
                                            width="600px"
                                            @okCallback="loadData"
                                        >
                                            <a class="btn-link">{{ $t('去处理') }}</a>
                                        </DialogForm>
                                        <DialogForm
                                            :maskClose="false"
                                            v-if="activeKey != 7 && activeKey != 5 && activeKey != 6 && !row.isNeedReImportVendorProduct"
                                            :params="{
                                                act: 'detail',
                                                id: row.productId,
                                                shopId: row.shopId || 0,
                                                examine: getShopType() == 2 && !row.shopId ? 1 : 0,
                                                storeSettingInfo: storeSettingInfo
                                            }"
                                            isDrawer
                                            path="product/product/Info"
                                            :title="$t('编辑商品')"
                                            width="950px"
                                            @okCallback="loadData"
                                        >
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <DeleteRecord
                                            v-if="activeKey == 4 && row.isNeedReImportVendorProduct == 1"
                                            :params="{ productId: row.productId, vendorProductId: row.vendorProductId }"
                                            :requestApi="vendorProductReImport"
                                            :title="$t('确定重新导入该商品吗？')"
                                            :message="$t('操作成功')"
                                            @afterDelete="loadFilter"
                                        > {{ $t('重新导入') }} </DeleteRecord>
                                        <el-divider v-if="activeKey != 7 && activeKey != 5" direction="vertical" />
                                        <a
                                            v-if="activeKey != 7 && activeKey != 5 && activeKey != 6"
                                            class="btn-link"
                                            :href="urlFormat({ path: 'product', sn: row.productSn, id: row.productId })"
                                            target="_blank"
                                            >{{ $t('查看') }}</a
                                        >
                                        <el-divider v-if="activeKey != 7 && activeKey != 5 && activeKey != 6" direction="vertical" />
                                        <DeleteRecord
                                            v-if="activeKey === 7"
                                            :params="{ id: row.productId, field: 'isDelete', val: 0 }"
                                            :requestApi="updateProductFiled"
                                            :title="$t('确定还原该商品吗？')"
                                            @afterDelete="loadFilter"
                                        > {{ $t('还原') }} </DeleteRecord>
                                        <el-divider v-if="activeKey === 7" direction="vertical" />
                                        <DeleteRecord
                                            v-if="activeKey === 7"
                                            :params="{ id: row.productId, field: 'del', val: 0 }"
                                            :requestApi="updateProductFiled"
                                            :title="$t('确定彻底删除该商品吗？')"
                                            @afterDelete="loadFilter"
                                        > {{ $t('删除') }} </DeleteRecord>
                                        <el-dropdown
                                            trigger="hover"
                                            v-if="activeKey != 7 && activeKey != 5"
                                            :hide-on-click="false"
                                            :ref="(el: any) => setRowAiDropdownRef(el, row.productId)"
                                        >
                                            <el-icon class="btn-link" size="12">
                                                <MoreFilled />
                                            </el-icon>
                                            <template #dropdown>
                                                <el-dropdown-menu>
                                                    <ProductAdvertise v-if="activeKey != 7 && activeKey != 5 && activeKey != 6" :item="row" :title="$t('商品推广')">
                                                        <el-dropdown-item>
                                                            <el-button style="width: 100%" link type="primary">{{ $t('推广') }}</el-button>
                                                        </el-dropdown-item>
                                                    </ProductAdvertise>
                                                    <DeleteRecord
                                                        v-if="!row.productStatus"
                                                        :params="{ id: row.productId, isDelete: 1 }"
                                                        :requestApi="recycleProduct"
                                                        :title="$t('确定把该商品移入回收站吗？')"
                                                        @afterDelete="loadFilter"
                                                    >
                                                        <el-dropdown-item>
                                                            <el-button style="width: 100%" link type="primary">{{ $t('移入回收站') }}</el-button>
                                                        </el-dropdown-item>
                                                    </DeleteRecord>
                                                    <DeleteRecord
                                                        v-if="!(getAdminType() !== 'vendor' && row.vendorProductId > 0)"
                                                        :params="{ productId: row.productId }"
                                                        :requestApi="copyProduct"
                                                        :title="$t('确定复制该商品吗？')"
                                                        @afterDelete="loadFilter"
                                                    >
                                                        <el-dropdown-item>
                                                            <el-button style="width: 100%" link type="primary">{{ $t('复制') }}</el-button>
                                                        </el-dropdown-item>
                                                    </DeleteRecord>
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
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { Image } from "@/components/image";
import "@/style/css/list.less";
import { MoreFilled } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { computed, ref } from "vue";
import { QuestionFilled } from "@element-plus/icons-vue";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { message } from "ant-design-vue";
import { ElMessageBox } from "element-plus";
import { useConfigStore } from "@/store/config";
import { ProductFilterParams, ProductFilterState } from "@/types/product/product";
import {
    batchSubmit,
    recycleProduct,
    getProductList,
    updateProductFiled,
    vendorProductReImport,
    updateProduct,
    getProductWaitingCheckedCount,
    copyProduct
} from "@/api/product/product";
import { SelectBrand, SelectCategory, SelectShop } from "@/components/select";
import { imageFormat, urlFormat } from "@/utils/format";
import ProductStockPop from "./src/ProductStockPop.vue";
import ProductPricePop from "./src/ProductPricePop.vue";
import { updateProductPrice } from "@/utils/updateProductPrice";
import { ProductAdvertise } from "@/components/product";
import { isB2b, isMerchant, isOverseas, isS2b2c, isStore } from "@/utils/version";
import { getShopType, getAdminType } from "@/utils/storage";
import { useListRequest } from "@/hooks/useListRequest";
import { storeSettings } from "@/api/store/setting";
import type { Response } from "@/types/store/setting";
import { useRouter } from "vue-router";

import { useI18n } from "vue-i18n"
const { t, locale } = useI18n()
const router = useRouter();
const config: any = useConfigStore();

// 「商品编辑库存是否可修改」开关已随 getAdminBase 下发，直接读 config.productEditStockModify
const productNameMaxLength = computed(() => {
    const maxLength = Number(config.get("customProductNameLength"));
    return maxLength > 0 ? maxLength : 200;
});
/** 操作列宽度：英文文案更长，单独加宽 */
const operationColumnWidth = computed(() => {
    const isEn = String(locale.value).toLowerCase().startsWith("en");
    const isStoreAdmin = isStore() && getAdminType() === "admin";
    if (isEn) {
        return isStoreAdmin ? 280 : 220;
    }
    return isStoreAdmin ? 200 : 180;
});
/** 当前管理员类型值 */
const adminTypeValue = getAdminType();
/** 当前店铺类型值：1-店铺 / 2-门店 */
const shopTypeValue = getShopType();
/** 是否为供应商管理员 */
const isVendorAdmin = adminTypeValue === "vendor";
/** 是否为平台管理员 */
const isAdminUser = adminTypeValue === "admin";
/** 是否为店铺类型（多商户版） */
const isShopType1 = shopTypeValue === 1;
/** 是否为门店类型（O2O版） */
const isShopType2 = shopTypeValue === 2;
/** 待审核商品数量（用于Tab角标显示） */
const waitingCheckedCount = ref<number>(0);
/** 当前激活的Tab */
const activeKey = ref<number>(1);
/**
 * 使用列表请求Hook
 * @description 封装商品列表数据加载、筛选、排序、多选等通用逻辑
 */
const {
    /** 商品列表数据 */
    listData: filterState,
    /** 加载状态 */
    loading,
    /** 总数 */
    total,
    /** 选中的商品ID列表 */
    selectedIds,
    /** 筛选参数 */
    filterParams,
    /** 加载数据方法 */
    loadData: loadFilter,
    /** 搜索提交 */
    onSearchSubmit,
    /** 排序变更 */
    onSortChange,
    /** 选中状态变更 */
    onSelectChange,
    /** 批量操作 */
    onBatchAction
} = useListRequest<ProductFilterState, ProductFilterParams>({
    apiFunction: getProductList,
    idKey: "productId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        introType: "",
        isDelete: 0,
        searchShop: 0,
        shopId: "",
        categoryId: "",
        brandId: "",
        productStatus: 1,
        checkStatus: 1,
        type: 1
    }
});

/**
 * @description 从 URL query / history.state 初始化筛选条件，供库存预警等页面带条件跳转使用
 * 只认下列白名单键，未携带对应参数时保持 defaultParams 原有默认值
 * 相关商品 ids 优先读 history.state.relatedProductIds，避免一长串 ID 出现在地址栏
 */
const QUERY_STRING_KEYS = ["keyword", "introType", "ids", "searchStartTime", "searchEndTime"];
const QUERY_NUMBER_KEYS = [
    "shopId",
    "categoryId",
    "brandId",
    "productStatus",
    "checkStatus",
    "isDelete",
    "searchShop",
    "type",
    "reservationProduct",
    "showVendorProduct",
    "productType",
    "saleMode"
];

const applyRelatedProductIds = () => {
    const stateIds = (history.state as { relatedProductIds?: unknown })?.relatedProductIds;
    if (!Array.isArray(stateIds) || !stateIds.length) return;
    const ids = stateIds.map((id) => Number(id)).filter((id) => Number.isFinite(id) && id > 0);
    if (ids.length) {
        filterParams.ids = ids.join(",");
    }
};

const applyRouteQuery = () => {
    const query = router.currentRoute.value.query;
    QUERY_STRING_KEYS.forEach((key) => {
        const value = query[key];
        if (typeof value === "string" && value !== "") {
            (filterParams as any)[key] = value;
        }
    });
    QUERY_NUMBER_KEYS.forEach((key) => {
        const value = query[key];
        if (typeof value === "string" && value !== "" && !Number.isNaN(Number(value))) {
            (filterParams as any)[key] = Number(value);
        }
    });
    applyRelatedProductIds();
};

// 初始化加载
applyRouteQuery();
loadFilter();
/**
 * @description 商品状态Tab切换处理
 * 根据Tab值设置不同的筛选条件（上架/下架/审核/回收站等）
 * @param val Tab值（1-出售中,2-自营,3-店铺,4-下架,5-待审核,6-审核失败,7-回收站）
 */
const onTabChange = (val: number) => {
    activeKey.value = val;
    filterParams.productStatus = val >= 1 && val <= 3 ? 1 : val >= 4 && val <= 6 ? 0 : undefined;
    filterParams.checkStatus = val >= 1 && val <= 4 ? 1 : val === 5 ? 0 : val === 6 ? 2 : undefined;
    filterParams.shopId = val === 2 ? 0 : val === 3 ? "" : "";
    filterParams.isDelete = val === 7 ? 1 : 0;
    filterParams.searchShop = val === 3 ? 1 : 0;
    filterParams.type = val == 1 ? 1 : val == 5 ? 2 : null;
    filterParams.productType = undefined;
    filterParams.page = 1;
    if (val === 5 || val === 6) {
        delete filterParams.productStatus;
    }
    Object.keys(filterParams).forEach((key) => {
        if ((filterParams as any)[key] === undefined) delete (filterParams as any)[key];
    });
    loadFilter();
};

/** 重置筛选条件并刷新列表 */
const resetParams = () => {
    filterParams.sortField = "";
    filterParams.sortOrder = "";
    filterParams.keyword = "";
    filterParams.introType = "";
    filterParams.isDelete = 0;
    filterParams.searchShop = 0;
    filterParams.shopId = "";
    filterParams.categoryId = "";
    filterParams.brandId = "";
    filterParams.productStatus = 1;
    filterParams.checkStatus = 1;
    filterParams.productType = undefined;
    filterParams.saleMode = undefined;
    delete (filterParams as { ids?: string }).ids;
    onTabChange(activeKey.value);
};
/** 商品操作确认事件（上架/下架等） */
const confirmEvent = async (act: string, id: string) => {
    try {
        const result = await updateProduct(act, { productId: id });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};
/** 商品状态Tab列表配置 */
const productStatusList = ref([
    {
        label: t('出售中的商品'),
        value: 1,
        isShow: true
    },
    {
        label: t('自营商品'),
        value: 2,
        isShow: getAdminType() === "admin" && (isMerchant() || isStore() || isS2b2c())
    },
    {
        label: t('店铺商品'),
        value: 3,
        isShow: getAdminType() === "admin" && isMerchant()
    },
    {
        label: t('门店商品'),
        value: 3,
        isShow: getAdminType() === "admin" && isStore()
    },
    {
        label: t('已下架商品'),
        value: 4,
        isShow: true
    },
    {
        label: t('商品回收站'),
        value: 7,
        isShow: true
    },
    {
        label: t('待审核商品'),
        value: 5,
        count: 0,
        isShow: isMerchant() || isStore() || isS2b2c() //getAdminType() === "admin" && (isMerchant() || isStore() || isS2b2c())
    },
    {
        label: t('审核失败商品'),
        value: 6,
        isShow: getAdminType() === "shop"
    }
]);
console.log("getAdminType()", getAdminType());
/** 分类选择组件引用 */
const selectCategoryRef = ref();
/** 导入供应商商品弹窗引用 */
const importVendorProductDialogRef = ref();
/**
 * 跳转到库存日历设置页面
 * @description 预约类商品（上门/到店）需要通过库存日历设置可预约库存
 * @param {any} row - 商品行数据
 */
const goSetStock = (row: any) => {
    //判断是门店还是自营
        router.push({
            path: "/product/inventory-calendar/list",
            query: { productName: row.productName }
        });
};

/**
 * 店铺选择变更回调
 * @description 联动更新分类选择器的店铺筛选
 * @param {number} val - 店铺ID
 */
const SelectShopChange = (val: number) => {
    selectCategoryRef.value.SelectShopChange(val);
    onSearchSubmit();
};

/**
 * @description 获取待审核商品数量
 * 用于Tab标签上显示待审核商品数量角标
 */
const _getProductWaitingCheckedCount = async () => {
    try {
        const waitingCount = await getProductWaitingCheckedCount();
        waitingCheckedCount.value = waitingCount;
        // 找到对应的对象并更新 count
        productStatusList.value = productStatusList.value.map((status) => {
            if (status.value === 5) {
                status.count = waitingCheckedCount.value || 0;
            }
            return status;
        });
    } catch (error: any) {
        message.error(error.message);
    }
};
_getProductWaitingCheckedCount();

/** 刷新列表数据和待审核数量 */
const loadData = () => {
    loadFilter();
    _getProductWaitingCheckedCount();
};


/** 行级"更多"下拉实例集合，用于点击项后主动收起悬浮下拉 */
const rowAiDropdownRefs = new Map<number, any>();
const setRowAiDropdownRef = (el: any, key: number) => {
    if (el) {
        rowAiDropdownRefs.set(key, el);
    } else {
        rowAiDropdownRefs.delete(key);
    }
};

/** 门店设置信息（控制门店商品编辑权限） */
const storeSettingInfo = ref<Response>({});

/**
 * 判断是否为供应商可编辑的商品
 * @description 供应商只能编辑自己上传的商品
 * @param {any} row - 商品行数据
 * @returns {boolean} 是否可编辑
 */
const hasVendorEditableId = (row: any) => {
    return (isVendorAdmin && row.vendorProductId == null) || row.vendorProductId < 0;
};

/**
 * @description 判断商品名称是否可行内编辑
 * 根据管理员类型、店铺类型、商品来源等条件判断
 */
const canInlineEditName = (row: any): boolean => {
    if (hasVendorEditableId(row)) return true;
    // 是门店但不是待审核商品
    if (isShopType2 && (storeSettingInfo.value.storeAssignProductName === 1 || row.shopId) && activeKey.value != 5) return true;
    // 是店铺但不是供应商商品
    if (isShopType1 && row.vendorProductId == null && activeKey.value != 5) return true;
    if (isAdminUser && row.vendorProductId == null) return true;
    return false;
};

/** 判断货号是否可行内编辑 */
const canInlineEditSn = (row: any): boolean => {
    if (hasVendorEditableId(row)) return true;
    // 是门店
    if (isShopType2 && row.shopId && activeKey.value != 5) return true;
    // 是店铺但不是供应商商品
    if (isShopType1 && row.vendorProductId == null && activeKey.value != 5) return true;
    if (isAdminUser && row.vendorProductId == null) return true;
    return false;
};

/** 判断价格是否可行内编辑 */
const canInlineEditPrice = (row: any): boolean => {
    if (hasVendorEditableId(row)) return true;
    // 是门店
    if (isShopType2 && (storeSettingInfo.value.storeAssignProductPrice === 1 || row.shopId) && activeKey.value != 5) return true;
    // 是店铺但不是待审核商品
    if (isShopType1 && activeKey.value != 5) return true;
    if (isAdminUser && row.vendorProductId == null) return true;
    return false;
};

/** 判断库存是否可行内编辑（卡密商品不可编辑） */
const canInlineEditStock = (row: any): boolean => {
    // 商品编辑库存开关关闭时，禁止列表快捷改库存（库存请走库存调整）
    if (config.productEditStockModify === 0) return false;
    if (row.productType === 3) return false;
    if (hasVendorEditableId(row)) return true;
    // 是门店
    if (isShopType2 && row.vendorProductId == null && (storeSettingInfo.value.storeUseSoloProductStock === 1 || row.shopId) && activeKey.value != 5)
        return true;
    // 是店铺但不是待审核商品
    if (isShopType1 && row.vendorProductId == null && activeKey.value != 5) return true;
    if (isAdminUser && row.vendorProductId == null) return true;
    return false;
};

/** 判断排序是否可行内编辑 */
const canInlineEditSort = (row: any): boolean => {
    if (hasVendorEditableId(row)) return true;
    // 是门店
    if (isShopType2 && row.shopId && activeKey.value != 5) return true;
    // 是店铺但不是待审核商品
    if (isShopType1 && activeKey.value != 5) return true;
    if (isAdminUser) return true;
    return false;
};
/**
 * 获取门店设置信息
 * @description 用于判断门店商品的编辑权限（名称/价格/库存等）
 */
const _storeSettings = async () => {
    try {
        const result = await storeSettings();
        storeSettingInfo.value = result;
    } catch (error: any) {
        message.error(error.message);
    }
};

/**
 * @description 批量操作处理
 * @param action 操作类型（recycle/restore/del/up/down/audit）
 */
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

/**
 * @description 商品上架开关切换前的拦截判断
 * 供应商商品已修改时阻止上架，需要重新导入
 * @returns 是否允许切换
 */
const beforeProductStatusChange = async (row: any): Promise<boolean> => {
    if (row.vendorProductId && row.productStatus === 0 && row.isNeedReImportVendorProduct) {
        // 弹出提示框，使用 alert 只显示一个关闭按钮
        await ElMessageBox.alert("因供应商商品已修改，请重新导入后进行上架！", t('提示'), {
            confirmButtonText: t('关闭'),
            type: "warning"
        });
        // 返回 false 阻止切换和请求
        return false;
    }
    return true;
};
</script>
<style lang="less" scoped>
:deep(.el-table .cell) {
    display: flex;
    align-items: center;
}

:deep(.el-table .caret-wrapper) {
    margin-top: 5px;
}

.status-switch {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    width: 150px;
}

.status-switch > div {
    word-break: keep-all;
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
}

.presale-tag {
    display: inline-block;
    margin-right: 8px;
    color: var(--tig-primary);
    font-size: inherit;
    line-height: inherit;
    vertical-align: baseline;
    white-space: nowrap;
    flex-shrink: 0;
}
</style>
