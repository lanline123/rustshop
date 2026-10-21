<template>
    <div class="DetailsGoodsSold">
        <el-form :model="filterParams">
            <div class="filtrate-menu">
                <el-form-item class="mr-10">
                    <SelectTimeInterval type="date" v-model:start-date="filterParams.startTime" v-model:end-date="filterParams.endTime"></SelectTimeInterval>
                </el-form-item>
                <el-form-item class="mr-10">
                    <TigInput v-model="filterParams.keyword" :placeholder="$t('输入商品名称或商品编号')"> </TigInput>
                </el-form-item>
                <el-form-item class="mr-10">
                    <el-button plain @click="handleSearch">{{ $t('搜索') }}</el-button>
                </el-form-item>
                <el-form-item class="mr-10">
                    <el-button plain @click="handleExport" :loading="Exportloading">{{ $t('导出EXCEL') }}</el-button>
                </el-form-item>
            </div>
        </el-form>

        <div class="table-container">
            <a-spin :spinning="loading">
                <el-table :data="filterState" :total="total" @sort-change="onSortChange">
                    <el-table-column :label="$t('商品名称')" prop="productId" :min-width="320">
                        <template #default="{ row }">
                            <div class="flex">
                                <div v-if="row.picThumb" class="span-pic">
                                    <a :href="urlFormat({ path: 'product', sn: row.productSn })" target="_blank"
                                        ><img :src="imageFormat(row.picThumb)" height="50" width="50"
                                    /></a>
                                </div>
                                <div class="span-product-con">
                                    <div class="span-product-name">{{ row.productName }}</div>
                                </div>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="productSn" :label="$t('商品编号')"> </el-table-column>
                    <el-table-column porp="quantity" :label="$t('购买数量')">
                        <template #default="{ row }">
                            {{ row.quantity }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('单价')">
                        <template #default="{ row }">
                            {{ priceFormat(row.price) }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('小计')">
                        <template #default="{ row }">
                            {{ priceFormat(row.price * row.quantity) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="addTime" width="200" sortable="custom" :label="$t('下单时间')">
                        <template #default="{ row }">
                            {{ row.orders?.addTime }}
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
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from "vue";
import { SelectTimeInterval } from "@/components/select";
import { useConfigStore } from "@/store/config";
import { imageFormat, urlFormat, priceFormat } from "@/utils/format";
import { Pagination } from "@/components/list";
import { getDays, formatDate } from "@/utils/util";
import { formattedDate } from "@/utils/time";
import { message } from "ant-design-vue";
import type { SaleDetaillistFilterParams, SaleDetaillistFilterResult } from "@/types/panel/statisticsSale";
import { getSaleDetaillist, exportSaleDetaillis } from "@/api/panel/statisticsSale";
import requestExport from "@/utils/export";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore();

const loading = ref<boolean>(false);
const filterState = ref<SaleDetaillistFilterResult[]>();
const total = ref<number>(0);
const filterParams = reactive<SaleDetaillistFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    keyword: "",
    startTime: formattedDate(getDays(30, "sub"), "YYYY-MM-DD"),
    endTime: formattedDate(new Date(), "YYYY-MM-DD")
});

const getData = async () => {
    loading.value = true;
    try {
        const result = await getSaleDetaillist(filterParams);
            filterState.value = result.records;
            total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
        console.error(error);
    } finally {
        loading.value = false;
    }
};

const handleSearch = () => {
    getData();
};


const Exportloading = ref<boolean>(false)
const handleExport = async () => {
  Exportloading.value = true;
  try {
    const result = await exportSaleDetaillis({ ...filterParams, isExport: "1" });
    Exportloading.value = false;
    requestExport(result,t('销售明细导出'))
  } catch (error:any) {
    message.error(error.message)
  } finally {
    Exportloading.value = false;
  }
};

onMounted(() => {
    getData();
});

// 修改排序
const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
    filterParams.sortField = prop;
    filterParams.sortOrder = order == "ascending" ? "asc" : order == "descending" ? "desc" : "";
    getData();
};

const loadFilter = () => {
    getData()
};
</script>

<style lang="less" scoped>
.filtrate-menu {
    display: flex;
    // margin-bottom: 20px;

    .mr-10 {
        margin-right: 5px;
    }
}

.span-product-name {
    margin-left: 5px
}
</style>
