<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <!-- <div style="margin-bottom: 10px; color: #666;">
                    <span v-if="props.activityName">活动：{{ props.activityName }}</span>
                    <span v-if="props.userName || props.userId" style="margin-left: 12px;">
                        会员：{{ props.userName || "-" }}（ID：{{ props.userId || "-" }}）
                    </span>
                </div> -->

                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="recordId" height="600">
                        <el-table-column :label="$t('序号')" width="80">
                            <template #default="{ $index }">
                                {{ (page.page - 1) * page.size + $index + 1 }}
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('会员信息')">
                            <template #default="{ row }">
                                <div style="display:flex;align-items:center;gap:10px;">
                                    <el-avatar :size="36" :src="imageFormat(row.avatar)">
                                        {{ (row.userName || '-').slice(0, 1) }}
                                    </el-avatar>
                                    <div style="display:flex;flex-direction:column;">
                                        <div style="font-weight:500;">
                                            {{ row.userName || "-" }}
                                        </div>
<!--                                    <div style="color:#999;">-->
<!--                                        ID：{{ props.userId || "-" }}-->
<!--                                    </div>-->
                                    </div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('参与时间')" prop="createTime" />
                        <el-table-column :label="$t('获得奖品')">
                            <template #default="{ row }">
                                {{ row.prizeName || $t('未中奖') }}
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('奖品状态')" width="150">
                            <template #default="{ row }">
                                {{ row.statusText || "-" }}
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
                        <Pagination v-model:page="page.page" v-model:size="page.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { reactive, ref, onMounted } from "vue";
import { useConfigStore } from "@/store/config";
import { Pagination } from "@/components/list";
import { getLotteryActivityUserRecord } from "@/api/promotion/lottery";
import { imageFormat } from "@/utils/format";

const props = defineProps({
    activityId: { type: Number, default: 0 },
    activityName: { type: String, default: "" },
    userId: { type: Number, default: 0 },
    userName: { type: String, default: "" },
    isDialog: Boolean
});

type UserDetailRow = {
    recordId: number;
    userName: string;
    avatar?: string;
    createTime: string;
    prizeName: string;
    statusText: string;
};

const config: any = useConfigStore();
const page = reactive({
    page: 1,
    size: config.get("pageSize") || 10
});

const loading = ref(false);
const total = ref(0);
const filterState = ref<UserDetailRow[]>([]);

const loadFilter = async () => {
    if (!props.activityId || !props.userId) {
        filterState.value = [];
        total.value = 0;
        return;
    }
    loading.value = true;
    try {
        const result = await getLotteryActivityUserRecord({
            activityId: props.activityId,
            userId: props.userId,
            page: page.page,
            size: page.size
        });
        filterState.value = (result.records || []) as any;
        total.value = result.total || 0;
    } finally {
        loading.value = false;
    }
};

const onFormSubmit = () => {};
defineExpose({ onFormSubmit });

onMounted(() => {
    loadFilter();
});
</script>


