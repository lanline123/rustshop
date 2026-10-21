<template>
    <div class="logsBox" v-loading="loading">
        <div class="table-container">
            <a-spin :spinning="loading">
                <el-table :data="filterState" :loading="loading">
                    <el-table-column :label="$t('操作')" prop="operationType"></el-table-column>
                    <el-table-column :label="$t('操作时间')" prop="addTime"></el-table-column>
                    <el-table-column min-width="150" :label="$t('审核驳回原因')" prop="reviewRejectedReason" :formatter="(row: any) => row.reviewRejectedReason || '--'"></el-table-column>
                    <el-table-column :label="$t('操作人')" prop="addBy"></el-table-column>
                    <template #empty>
                        <div class="empty-warp">
                            <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                        </div>
                    </template>
                </el-table>
            </a-spin>
        </div>
    </div>
</template>

<script setup lang="ts">
import { productStockAllocateOperationDetail } from '@/api/product/inventoryTransfer'
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'

const props = defineProps({
    act: {
        type: String,
        default: 'detail'
    },
    id: {
        type: Number,
        default: null
    },
})

const loading = ref(true)
const filterState = ref([])

const getDetail = async () => {
    loading.value = true
    try {
        filterState.value = await productStockAllocateOperationDetail({ allocateId: props.id })
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
</style>
