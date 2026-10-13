<template>
    <div class="container">
        <div class="table-container" style="padding: 12px">
            <a-spin :spinning="loading">
                <el-table :data="listData" row-key="id" :loading="loading">
                    <el-table-column :label="$t('优惠券名称')" prop="couponName" min-width="150" />
                    <el-table-column :label="$t('发放数量')" prop="couponCount" align="center" />
                    <el-table-column :label="$t('发放时间')" min-width="120">
                        <template #default="{ row }">
                            {{ formatTimestamp(row.issueTime) }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('操作')" width="100" fixed="right">
                        <template #default="{ row }">
                            <DialogForm
                                :params="{ batchId: row.id, couponId: row.couponId }"
                                :show-on-ok="false"
                                :showFooter="false"
                                isDrawer
                                path="promotion/coupon/src/SendLogDetail"
                                :title="$t('发放明细')"
                                width="700px"
                            >
                                <a class="btn-link">{{ $t('查看') }}</a>
                            </DialogForm>
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
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { getCouponIssueBatchList } from "@/api/promotion/coupon";
import { useConfigStore } from "@/store/config";
import dayjs from "dayjs";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    couponId: {
        type: Number,
        default: 0
    }
});

const config: any = useConfigStore();
const loading = ref(false);
const listData = ref<any[]>([]);
const total = ref(0);

const filterParams = reactive({
    page: 1,
    size: config.get("pageSize"),
    couponId: props.couponId
});

const formatTimestamp = (ts: number) => {
    if (!ts) return "--";
    return dayjs.unix(ts).format("YYYY-MM-DD HH:mm:ss");
};

const loadData = async () => {
    loading.value = true;
    try {
        const result = await getCouponIssueBatchList({ ...filterParams });
        listData.value = result.records || [];
        total.value = result.total || 0;
    } catch (error: any) {
        message.error(error.message || t('加载失败'));
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadData();
});
</script>
