<template>
    <div class="page-alert">
        <el-icon size="18" color="#ed6a18"><WarnTriangleFilled /></el-icon>
        <span>
            {{ $t("初次开通，需提交小程序审核，通过后方可正常访问直播间，请先前往“基础信息“提交审核。") }}
            <a href="https://live.weixin.qq.com/livemp/login" target="_blank" rel="noopener noreferrer">{{ $t("前往提审") }}</a>
        </span>
    </div>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入商品名称/商品ID')"
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
                                        <label class="control-label"
                                            ><span>{{ $t("状态：") }}</span></label
                                        >
                                        <div class="control-container">
                                            <el-select v-model="filterParams.auditStatus" clearable :placeholder="$t('请选择')" @change="onSearchSubmit">
                                                <el-option :value="0" :label="$t('待审核')" />
                                                <el-option :value="1" :label="$t('审核中')" />
                                                <el-option :value="2" :label="$t('审核通过')" />
                                                <el-option :value="3" :label="$t('审核驳回')" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="control-container">
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t("搜索") }}</el-button>
                                        <el-button plain @click="resetParams">{{ $t("重置") }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        :params="{
                                            selectedIds: selectedProductPairs,
                                            selectedProducts: selectedProductsRaw,
                                            isMultiple: true,
                                            productType: [],
                                            isSaleMode: undefined
                                        }"
                                        isDrawer
                                        path="promotion/giftCard/src/SelectProduct"
                                        :title="$t('选择商品')"
                                        width="1000px"
                                        @okCallback="onSelectProductOk"
                                    >
                                        <el-button type="primary">{{ $t("导入商品") }}</el-button>
                                    </DialogForm>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :total="total" :loading="loading" row-key="id" @sort-change="onSortChange">
                            <el-table-column :label="$t('商品信息')">
                                <template #default="{ row }">
                                    <div class="product-info">
                                        <Image :src="row.productImage" fit="cover" style="width: 50px; height: 50px; border-radius: 4px" />
                                        <div class="product-detail">
                                            <div class="product-name">{{ row.productName }}</div>
                                            <div class="product-id">ID: {{ row.productId }}</div>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('价格')" prop="price" sortable="custom">
                                <template #default="{ row }">
                                    <span class="price">¥{{ Number(row.price || 0).toFixed(2) }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('类型')">
                                <template #default>
                                    <span>{{ $t("商品") }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('库存')" prop="stock" sortable="custom"></el-table-column>
                            <el-table-column :label="$t('状态')">
                                <template #default="{ row }">
                                    <el-tag :type="getAuditStatusType(row.auditStatus)">
                                        {{ row.auditStatus === 2 ? $t("通过") : row.auditStatus === 3 ? $t("驳回") : row.auditStatusText }}
                                    </el-tag>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t("暂无数据") }}</div>
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
import { Image } from "@/components/image";
import { Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import type { WechatLiveProductState, WechatLiveProductFilterParams } from "@/types/content/wechatLiveProduct.d";
import { getWechatLiveProductList, importWechatLiveProduct } from "@/api/content/wechatLiveProduct";
import { DialogForm } from "@/components/dialog";
import { computed, ref, h } from "vue";
import { WarnTriangleFilled } from "@element-plus/icons-vue";

import { useI18n } from "vue-i18n";
const { t } = useI18n();
const config: any = useConfigStore();

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    resetParams
} = useListRequest<WechatLiveProductState, WechatLiveProductFilterParams>({
    apiFunction: getWechatLiveProductList,
    idKey: "id",
    defaultParams: {
        keyword: "",
        liveRoomId: undefined,
        auditStatus: undefined,
        sortField: "",
        sortOrder: "",
        page: 1,
        size: config.get("pageSize")
    }
});

// 初始化加载
loadFilter();

const selectedProductsRaw = ref<any[]>([]);

const selectedProductPairs = computed(() => {
    return selectedProductsRaw.value.map((item) => ({
        productId: Number(item.productId),
        skuId: Number(item.skuId)
    }));
});

const onSelectProductOk = async (selectedProducts: any[] = []) => {
    const safeList = Array.isArray(selectedProducts) ? selectedProducts : [];
    selectedProductsRaw.value = safeList;

    const goodsItemMap = new Map<string, { productId: number; productSkuId: number }>();
    safeList.forEach((item) => {
        const productId = Number(item.productId || 0);
        const productSkuId = Number(item.skuId || 0);
        if (productId <= 0) return;
        goodsItemMap.set(`${productId}-${productSkuId}`, { productId, productSkuId });
    });
    const goodsItems = Array.from(goodsItemMap.values());

    if (goodsItems.length === 0) {
        message.warning(t("请选择商品"));
        return;
    }

    try {
        await importWechatLiveProduct({
            goodsItems
        });
        message.success(t("导入成功"));
        loadFilter();
    } catch (error: any) {
        message.error({
            content: h("span", [
                t("导入失败，请检查是否拥有小程序直播资格，可根据"),
                h(
                    "a",
                    {
                        href: "https://developers.weixin.qq.com/miniprogram/product/live/access-requirement.html",
                        target: "_blank",
                        rel: "noopener noreferrer",
                        style: "color: var(--el-color-primary); text-decoration: none;"
                    },
                    t("《微信小程序直播功能准入要求》")
                ),
                t("查看是否达到要求")
            ]),
            duration: 5
        });
    } finally {
        selectedProductsRaw.value = [];
    }
};

// 获取审核状态标签类型
const getAuditStatusType = (status: number) => {
    const typeMap: Record<number, string> = {
        0: "warning",
        1: "primary",
        2: "success",
        3: "danger"
    };
    return typeMap[status] || "info";
};
</script>

<style lang="less" scoped>
.product-info {
    display: flex;
    align-items: center;
    gap: 12px;
    .product-detail {
        flex: 1;
        .product-name {
            font-weight: 500;
            line-height: 1.4;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
        }
        .product-id {
            color: #999;
            font-size: 12px;
            margin-top: 4px;
        }
    }
}
.price {
    color: #ff4d4f;
    font-weight: 500;
}
.room-id {
    color: #999;
    font-size: 12px;
}

.page-alert {
    margin-bottom: 16px;
    color: rgb(16, 16, 16);
    font-size: 14px;
    line-height: 20px;
    min-height: 34px;
    background-color: rgb(255, 239, 230);
    padding: 4px 20px;
    display: flex;
    align-items: center;
    gap: 8px;
}
</style>
