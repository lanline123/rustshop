/**
 * @file 库存调整日志
 * @description 查看商品库存调整历史记录
 * @features
 * - 按调整类型筛选
 * - 展示调整前后数量
 * - 显示操作账号和时间
 */
<template>
    <div class="logsBox" v-loading="loading">
        <div class="searchBox">
            <el-select v-model="filterParams.adjustType" :placeholder="$t('调整类型')" clearable @change="onSearchSubmit" @clear="onClear" style="width: 230px;">
                <el-option v-for="item in adjustTypeList" :key="item.value" :value="item.value" :label="item.label"></el-option>
            </el-select>
        </div>
        <div class="table-container">
            <a-spin :spinning="loading">
                <el-table :data="searchState" :loading="loading" :total="total">
                    <el-table-column :label="$t('调整类型')" prop="adjustType" :formatter="(row) => adjustTypeList.find(item => item.value === row.adjustType)?.label || '--'"></el-table-column>
                    <el-table-column :label="$t('调整前数量')" prop="changeBeforeQuantity" :formatter="(row) => (row.changeBeforeQuantity === 0 ? 0 : row.changeBeforeQuantity || '--')"></el-table-column>
                    <el-table-column :label="$t('调整后数量')" prop="changeAfterQuantity" :formatter="(row) => (row.changeAfterQuantity === 0 ? 0 : row.changeAfterQuantity || '--')"></el-table-column>
                    <el-table-column :label="$t('操作账号')" prop="addBy" :formatter="(row) => row.addBy || '--'"></el-table-column>
                    <el-table-column :label="$t('创建时间')" prop="addTime" :formatter="(row) => row.addTime || '--'"></el-table-column>
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
import { getStockAdjustDetail } from '@/api/product/inventoryAdjustment'
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    act: {
        type: String,
        default: 'detail'
    },
    productId: {
        type: Number,
        default: null
    },
    skuId: {
        type: Number,
        default: null
    }
})

const loading = ref(true)
const filterState = ref([])
const searchState = ref([])
const filterParams = ref({})
const adjustTypeList = [
    {
        value: -1,
        label: t('全部')
    },
    {
        value: 1,
        label: t('增量更新')
    },
    {
        value: 0,
        label: t('覆盖更新')
    }
]

const onSearchSubmit = (e) => {
    if(e === -1) {
        searchState.value = filterState.value
        return
    }
    searchState.value = filterState.value.filter(item => item.adjustType === e)
}
const onClear = () => {
    console.log(22222);
    searchState.value = filterState.value
}

const getDetail = async () => {
    loading.value = true
    try {
        filterState.value = await getStockAdjustDetail({ productId: props.productId, skuId: props.skuId })
        searchState.value = filterState.value
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
