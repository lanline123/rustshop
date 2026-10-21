<template>
    <div class="container" style="position: relative; height: 100%">
        <div class="content_wrapper">
            <div style="display: flex; flex-direction: column; width: 100%; height: 100%; position: absolute">
                <div class="list-table-tool lyecs-search-warp" style="flex: 0">
                    <el-form :model="filterParams" name="form" @keyup.enter="searchSubmit" @submit.prevent>
                        <div class="list-table-tool-row" style="margin-bottom: 5px">
                            <div class="list-table-tool-col search-wrap">
                                <div class="search-row">
                                    <SelectCategory v-model:categoryId="filterParams.categoryId" class="search-category" @update:categoryId="searchSubmit" />
                                    <TigInput class="search-keyword" v-model="filterParams.keyword" :placeholder="$t('商品名称/货号')" />
                                    <el-button class="btn" @click="searchSubmit">{{ $t('搜索') }}</el-button>
                                </div>
                            </div>
                            <div class="list-table-tool-col"></div>
                        </div>
                    </el-form>
                </div>
                <perfect-scrollbar class="table-container" style="flex: 1">
                    <a-spin :spinning="loading">
                        <div style="padding-right: 15px; box-sizing: border-box">
                            <el-table :data="filterState" :loading="loading" :total="total" class="result-table" row-key="productId">
                                <el-table-column :label="$t('商品信息')" prop="productId">
                                    <template #default="{ row }">
                                        <div class="flex">
                                            <div v-if="row.picThumb || row.productPicThumb" class="span-pic">
                                                <a :href="row.url" target="_blank"
                                                    ><img :src="imageFormat(row.picThumb || row.productPicThumb)" height="40" width="40"
                                                /></a>
                                            </div>
                                            <div>
                                                <p>{{ row.productName }}</p>
                                                <p class="gray" v-if="row.productSn">{{ $t('货号：{productSn}', { productSn: row.productSn }) }}</p>
                                            </div>
                                        </div>
                                    </template>
                                </el-table-column>
                                <template #empty>
                                    <div class="empty-warp">
                                        <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                    </div>
                                </template>
                            </el-table>
                        </div>
                    </a-spin>
                </perfect-scrollbar>
                <div v-if="total > 0" class="pagination-con" style="flex-shrink: 0">
                    <Pagination
                        v-model:page="filterParams.page"
                        v-model:size="filterParams.size"
                        :total="total"
                        @callback="loadFilter"
                        layout="slot ,prev, pager, next"
                        :background="false"
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { SelectCategory } from "@/components/select";
import { onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import { imageFormat } from "@/utils/format";
import { Pagination } from "@/components/list";
import { ProductFilterParams, ProductFilterState } from "@/types/product/product";
import { useConfigStore } from "@/store/config";
import { getProductList } from "@/api/product/product";
import { getProductStyleDetail } from "@/api/promotion/productStyle";

const props = defineProps({
    id: { type: Number, default: 0 },
    isDialog: { type: Boolean, default: false }
});

const loading = ref(true);
const total = ref<number>(0);
const config = useConfigStore();
const filterState = ref<ProductFilterState[]>([]);
const ids = ref("");

const filterParams = ref<ProductFilterParams>({
    page: 1,
    size: (config as any).get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: "",
    isDelete: 0
});

const fetchStyleProducts = async () => {
    try {
        const result = await getProductStyleDetail({ id: props.id });
        const productIds = result.productIds || [];
        ids.value = productIds.join(",");
        if (ids.value) {
            loadFilter();
        } else {
            loading.value = false;
        }
    } catch (error: any) {
        message.error(error.message);
        loading.value = false;
    }
};

const loadFilter = async () => {
    if (!ids.value) {
        filterState.value = [];
        total.value = 0;
        loading.value = false;
        return;
    }
    loading.value = true;
    try {
        const params: any = { ...filterParams.value, ids: ids.value };
        if (!params.categoryId) delete params.categoryId;
        const result = await getProductList(params);
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const searchSubmit = () => {
    filterParams.value.page = 1;
    loadFilter();
};

onMounted(() => {
    fetchStyleProducts();
});
</script>

<style lang="less" scoped>
.pagination-con {
    background: #fff;
}

.search-wrap {
    width: 100%;
}

.search-row {
    display: flex;
    width: 100%;
    gap: 8px;
    align-items: center;

    .search-category {
        width: 25%;
        min-width: 100px;
    }

    .search-keyword {
        flex: 1;
        min-width: 120px;
    }

    .btn {
        flex-shrink: 0;
        white-space: nowrap;
        line-height: 1;
        min-width: 60px;
        height: auto;
        margin-left: 0;
    }
}
</style>
