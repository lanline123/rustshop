<template>
    <div class="logsBox" v-loading="loading">
        <div class="table-container">
            <a-spin :spinning="loading">
                <el-table :data="filterState" :loading="loading">
                    <el-table-column :label="$t('商品名称')" prop="productName" min-width="250">
                        <template #default="{ row }">
                            <div class="productNameBox">
                                <div v-if="row.productPicThumb" class="span-pic">
                                    <img :src="imageFormat(row.productPicThumb)" height="40" width="40" />
                                </div>
                                <div><span class="span-text">{{ row.productName }}</span></div>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('出入库类型')" prop="desc" :formatter="(row: any) => row.desc || '--'"></el-table-column>
                    <el-table-column :label="$t('商品规格')" prop="productSkuName" :formatter="(row: any) => row.productSkuName || '--'"></el-table-column>
                    <el-table-column :label="$t('出入库数量')" prop="quantity" align="center">
                        <template #default="{ row }">
<!--                            inOrOut 1入库 2出库-->
                            <div>{{ row.inOrOut === 2 ? '-' + row.quantity : row.quantity }}</div>
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
                <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="getDetail" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { getInventoryDetail } from '@/api/product/inventoryQueryList'
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { imageFormat } from "@/utils/format";
import { Pagination } from "@/components/list";

const props = defineProps({
    act: {
        type: String,
        default: 'detail'
    },
    id: {
        type: Number,
        default: null
    },
    sukId: {
        type: Number,
        default: null
    }
})
const filterParams = ref({
    page: 1,
    size: 10
})
const total = ref(0)
const loading = ref(true)
const filterState = ref([])

const getDetail = async () => {
    loading.value = true
    try {
        const result = await getInventoryDetail({ productId: props.id, skuId: props.sukId, ...filterParams.value })
        console.log(result, 9999);
        filterState.value = result?.records || []
        total.value = result?.total || 0
    } catch (error: any) {
        message.error(error.message)
    } finally {
        loading.value = false
    }
}
onMounted(() => {
    getDetail()
})
</script>

<style scoped lang="less">
.searchBox{
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
}

.productNameBox{
    display: flex;
    align-items: center;
}
</style>
