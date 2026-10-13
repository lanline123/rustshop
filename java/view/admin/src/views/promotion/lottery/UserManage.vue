<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" name="form">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('会员信息：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.userName"
                                                name="userName"
                                                :placeholder="$t('请输入用户名称')"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
<!--                                                <template #append>-->
<!--                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>-->
<!--                                                </template>-->
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('参与时间：') }}</span></label>
                                        <SelectTimeInterval
                                            v-model:end-date="filterParams.endTime"
                                            v-model:start-date="filterParams.startTime"
                                            :clearable="true"
                                            type="date"
                                            value-format="YYYY-MM-DD"
                                        ></SelectTimeInterval>
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <div class="control-container">
                                            <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                            <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>

                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="userId" height="560">
                        <el-table-column :label="$t('序号')" width="80">
                            <template #default="{ $index }">
                                {{ (filterParams.page - 1) * filterParams.size + $index + 1 }}
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
<!--                                        ID：{{ row.userId || "-" }}-->
<!--                                    </div>-->
                                    </div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('参与次数')" prop="totalCount" />
                        <el-table-column :label="$t('最后参与时间')" prop="lastLotteryTime" />
                        <el-table-column :label="$t('操作')" fixed="right" width="150">
                            <template #default="{ row }">
                                <DialogForm
                                    isDrawer
                                    :title="$t('参与详情')"
                                    width="1200px"
                                    path="promotion/lottery/UserDetail"
                                    :params="{
                                        activityId: props.activityId,
                                        activityName: props.activityName,
                                        userId: row.userId,
                                        userName: row.userName
                                    }"
                                    :showFooter="false"
                                >
                                    <a class="btn-link">{{ $t('详情') }}</a>
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
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
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
import { DialogForm } from "@/components/dialog";
import { getLotteryActivityUserManage } from "@/api/promotion/lottery";
import { SelectTimeInterval } from "@/components/select";
import { imageFormat } from "@/utils/format";

const props = defineProps({
    activityId: { type: Number, default: 0 },
    activityName: { type: String, default: "" },
    isDialog: Boolean
});

type UserManageRow = {
    userId: number;
    userName: string;
    avatar?: string;
    totalCount: number;
    lastLotteryTime: string;
};

const config: any = useConfigStore();

const filterParams = reactive({
    userName: "",
    startTime: "",
    endTime: "",
    page: 1,
    size: config.get("pageSize") || 10
});

const loading = ref(false);
const total = ref(0);
const filterState = ref<UserManageRow[]>([]);

const buildTimeParams = () => {
    const start = filterParams.startTime ? `${filterParams.startTime} 00:00:00` : "";
    const end = filterParams.endTime ? `${filterParams.endTime} 23:59:59` : "";
    return { startTime: start, endTime: end };
};

const loadFilter = async () => {
    loading.value = true;
    try {
        const { startTime, endTime } = buildTimeParams();
        const result = await getLotteryActivityUserManage({
            activityId: props.activityId,
            userName: filterParams.userName?.trim() || "",
            startTime,
            endTime,
            page: filterParams.page,
            size: filterParams.size
        });
        filterState.value = (result.records || []) as any;
        total.value = result.total || 0;
    } finally {
        loading.value = false;
    }
};

const onSearchSubmit = () => {
    filterParams.page = 1;
    loadFilter();
};

const resetParams = () => {
    filterParams.userName = "";
    filterParams.startTime = "";
    filterParams.endTime = "";
    filterParams.page = 1;
    filterParams.size = config.get("pageSize") || 10;
    loadFilter();
};

// DialogForm 若启用确认按钮会调用该方法；本页面是纯列表，所以留空即可
const onFormSubmit = () => {};
defineExpose({ onFormSubmit });

onMounted(() => {
    loadFilter();
});
</script>


