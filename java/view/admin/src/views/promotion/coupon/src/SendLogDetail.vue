<template>
    <div class="container">
        <div class="table-container" style="padding: 12px">
            <a-spin :spinning="loading">
                <el-table :data="listData" row-key="id" :loading="loading">
                    <el-table-column :label="$t('会员信息')" min-width="220">
                        <template #default="{ row }">
                            <div class="user-info">
                                <el-avatar :size="30" :src="imageFormat(row.avatar)" />
                                <div class="user-info__main">
                                    <div class="user-name">{{ row.username || row.nickname || "--" }}</div>
                                    <MobileCard :mobile="row.mobile" />
                                </div>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('是否使用')" min-width="100" align="center">
                        <template #default="{ row }">
                            <div>{{ row.used === 1 ? $t('已使用') : $t('未使用') }}</div>
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
import { Pagination } from "@/components/list";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { getCouponIssueLogList } from "@/api/promotion/coupon";
import { useConfigStore } from "@/store/config";
import { imageFormat } from "@/utils/format";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    batchId: {
        type: Number,
        default: 0
    },
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
    batchId: props.batchId,
    couponId: props.couponId
});

const loadData = async () => {
    loading.value = true;
    try {
        const result = await getCouponIssueLogList({ ...filterParams });
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

<style scoped>
.user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
}

.user-info__main {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.user-name {
    color: #303133;
    line-height: 20px;
}
</style>
