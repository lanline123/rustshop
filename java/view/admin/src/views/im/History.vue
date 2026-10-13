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
                                        <label class="control-label"><span>时间筛选：</span></label>
                                        <div class="flex flex-align-center">
                                            <el-select v-model="filterParams.timeType" style="width: 160px; margin-right: 10px">
                                                <el-option label="会话开始时间" :value="1" />
                                                <el-option label="最后会话时间" :value="3" />
                                            </el-select>
                                            <SelectTimeInterval
                                                value-format="YYYY-MM-DD HH:mm:ss"
                                                v-model:start-date="filterParams.startTime"
                                                v-model:end-date="filterParams.endTime"
                                            />
                                            <div style="margin-left: 10px">
                                                <SelectableGroup :options="fastTimeOptions" v-model="fastTime" />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>客户姓名：</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.username"
                                                placeholder="请输入客户姓名"
                                                clearable
                                                @keyup.enter="onSearchSubmit"
                                                @clear="onSearchSubmit"
                                            />
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>接待客服：</span></label>
                                        <div class="control-container">
                                            <SelectServant v-model="filterParams.lastServantId" style="width: 100%" />
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>会话来源：</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.userFrom" clearable placeholder="全部" @change="onSearchSubmit">
                                                <el-option label="全部" value="" />
                                                <el-option label="pc" value="pc" />
                                                <el-option label="h5" value="h5" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>会话状态：</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.status" clearable placeholder="全部" @change="onSearchSubmit">
                                                <el-option label="全部" value="" />
                                                <el-option label="会话中" value="1" />
                                                <el-option label="已结束" value="2" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>会话备注：</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.remark"
                                                placeholder="关键词搜索"
                                                clearable
                                                @keyup.enter="onSearchSubmit"
                                                @clear="onSearchSubmit"
                                            />
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"></label>
                                        <div class="control-container">
                                            <el-button type="primary" @click="onSearchSubmit">搜索</el-button>
                                            <el-button @click="resetClick">重置</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>

                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="id">
                            <el-table-column label="客户名称" min-width="160">
                                <template #default="{ row }">
                                    {{ row.user?.nickname || row.user?.username || "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column label="接待客服" min-width="120">
                                <template #default="{ row }">
                                    {{ row.servant?.username || "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column prop="userFrom" label="会话来源" min-width="100" />
                            <el-table-column prop="addTime" label="开始时间" min-width="170" />
                            <el-table-column prop="lastUpdateTime" label="最后会话时间" min-width="170" />
                            <el-table-column :width="100" fixed="right" label="操作">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ id: row.id, currentConversationData: row }"
                                        isDrawer
                                        :showClose="false"
                                        :showOnOk="false"
                                        path="im/src/HistoryInfo"
                                        title="会话详情"
                                    >
                                        <a class="btn-link">查看详情</a>
                                    </DialogForm>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">暂无数据</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { DialogForm } from "@/components/dialog";
import { onMounted, reactive, ref, watch } from "vue";
import { SelectTimeInterval } from "@/components/select";
import SelectableGroup from "@/components/form/src/SelectableGroup.vue";
import dayjs from "dayjs";
import { useConfigStore } from "@/store/config";
import { message } from "ant-design-vue";
import type { SearchInfoList } from "@/types/im/im.d";
import { getConsultHistory } from "@/api/im/conversation";
import SelectServant from "@/components/select/src/SelectServant.vue";
import { Pagination } from "@/components/list";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
function getStartEndTime(s: any) {
    const n = dayjs(),
        o: any = { 1: [0, "day"], 2: [-1, "day"], 3: [-6, "day"], 4: [-29, "day"], 5: [-1, "year"] };
    if (!o[s]) throw new Error(t('无效的时间选项'));
    const [t, u] = o[s],
        st = n.add(t, u).startOf("day").format("YYYY-MM-DD HH:mm:ss"),
        et = n.endOf("day").format("YYYY-MM-DD HH:mm:ss");
    return { startTime: st, endTime: et };
}

const config: any = useConfigStore();
const filterState = ref<SearchInfoList[]>([]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const filterParams = reactive({
    page: 1,
    size: config.get("pageSize"),
    timeType: 1,
    startTime: "",
    endTime: "",
    username: "",
    lastServantId: "",
    userFrom: "",
    status: "",
    remark: ""
});

const loadFilter = async () => {
    loading.value = true;
    try {
        const cleanedParams = cleanParams(filterParams);
        const result = await getConsultHistory({ ...cleanedParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

function cleanParams(params: any) {
    return Object.fromEntries(Object.entries(params).filter(([_, value]) => value !== null && value !== "" && value !== undefined));
}

const onSearchSubmit = () => {
    filterParams.page = 1;
    loadFilter();
};

const resetClick = () => {
    filterParams.page = 1;
    filterParams.size = config.get("pageSize");
    filterParams.timeType = 1;
    filterParams.startTime = "";
    filterParams.endTime = "";
    filterParams.username = "";
    filterParams.lastServantId = "";
    filterParams.userFrom = "";
    filterParams.status = "";
    filterParams.remark = "";
    fastTime.value = null;
    loadFilter();
};

onMounted(() => {
    loadFilter();
});

const fastTime = ref<number | null>(null);
const fastTimeOptions = ref([
    { value: 1, label: t('今') },
    { value: 2, label: t('昨') },
    { value: 3, label: t('近7天') },
    { value: 4, label: t('近30天') },
    { value: 5, label: t('近一年') }
]);

watch(
    fastTime,
    (newValue) => {
        if (!newValue) return;
        const { startTime, endTime } = getStartEndTime(newValue);
        filterParams.startTime = startTime;
        filterParams.endTime = endTime;
        loadFilter();
    },
    { immediate: true }
);
</script>
