/**
 * @file 订单操作日志组件
 * @description 展示订单操作历史记录
 * @features
 * - 分页展示操作日志列表
 * - 显示操作者、操作时间、备注
 * - 支持添加新日志
 */
<template>
    <div class="container-card">
        <div class="title"> {{ $t('操作日志') }} <DialogForm
              :params="{ act: 'detail', id: filterParams.orderId, valueName: 'addNote' }"
              isDrawer
              path="order/order/src/EditRemark"
              :title="$t('添加日志')"
              width="600px"
              @okCallback="loadFilter">
                <el-button bg class="buttonColor" size="small" text type="primary"> {{ $t('添加日志') }} </el-button>
            </DialogForm>
        </div>
        <el-table v-loading="loading" :data="orderLog">
            <el-table-column :label="$t('操作者')" prop="operator" width="120px">
                <template #default="{ row }">{{ row.operator || $t('系统') }}</template>
            </el-table-column>
            <el-table-column :label="$t('操作时间')" prop="logTime" width="160px"></el-table-column>
            <el-table-column :label="$t('备注')" prop="description"></el-table-column>
        </el-table>
        <div class="pagination-con">
            <Pagination v-model:page="filterParams.page" v-model:size="logSize" :total="total" @callback="loadFilter" />
        </div>
    </div>
</template>
<script setup lang="ts">

import {DialogForm} from "@/components/dialog";
import {Pagination} from "@/components/list";
import {onMounted, reactive, ref} from "vue";
import {getOrderLogList} from "@/api/order/order";
import {message} from "ant-design-vue";
const props = defineProps({
    orderId: {
        type: Number,
        default: 0,
    }
});
const filterParams = reactive({
    page: 1,
    orderId: props.orderId,
    parentOrderId: 0,
});
const loading = ref<boolean>(true);
const logSize = ref(10);
const total = ref<number>(0);
const orderLog = ref<object[]>([]);
const loadFilter = async () => {
    try {
        loading.value = true
        const resLog = await getOrderLogList(filterParams);
        orderLog.value = resLog.records;
        total.value = resLog.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false
    }
};

onMounted(()=>{
    loadFilter();
})

defineExpose({ loadFilter });
</script>
<style scoped lang="less">
.container-card {
    border: 1px solid #ececec;
    /* 添加淡淡的边框 */
    box-shadow: 2px 2px 5px rgba(203, 193, 193, 0.2);
    border-radius: 2px;
    padding: 20px;
    background: #fff;
    margin-bottom: 20px;

    .title {
        font-size: 16px;
        line-height: 25px;
        color: #323233;
        margin-bottom: 15px;
        font-weight: 700;
    }
}
</style>
