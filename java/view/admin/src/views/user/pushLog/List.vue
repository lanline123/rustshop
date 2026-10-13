/** 消息推送记录列表 - 管理消息推送（APP/微信等）的发送、撤回、删除 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('输入标题')" @keyup.enter="onSearchSubmit">
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <DialogForm
                                                :params="{ act: 'add' }"
                                                isDrawer
                                                path="user/pushLog/Info"
                                                :title="$t('添加推送')"
                                                width="600px"
                                                @okCallback="loadFilter"
                                            >
                                                <el-button type="primary">{{ $t('添加推送') }}</el-button>
                                            </DialogForm>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <el-popconfirm :title="$t('您确认要批量撤回所选数据吗？')" @confirm="onBatchSubmit('recall')">
                                                <template #reference>
                                                    <el-button :disabled="selectedIds.length === 0">{{ $t('批量撤回') }}</el-button>
                                                </template>
                                            </el-popconfirm>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <el-space>
                                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                                <template #reference>
                                                    <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                                </template>
                                            </el-popconfirm>
                                            <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                        </el-space>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="messageLogId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('标题')" prop="messageTitle"></el-table-column>
                            <el-table-column :label="$t('内容')" prop="messageContent"></el-table-column>
                            <el-table-column :label="$t('链接')" prop="messageLink"></el-table-column>
                            <el-table-column :width="100" :label="$t('发送类型')" prop="sendTypeName"></el-table-column>
                            <el-table-column :width="100" :label="$t('发送状态')" prop="brandId">
                                <template #default="{ row }">
                                    <span :style="{ color: row.isRecall === 0 ? 'green' : 'red' }">{{ row.statusName }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :width="160" :label="$t('发送日期')" prop="sendTime"></el-table-column>
                            <el-table-column :width="100" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DeleteRecord :params="{ id: row.brandId }" :requestApi="recallPushLog" @afterDelete="loadFilter">{{ $t('撤回') }} </DeleteRecord>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.brandId }" :requestApi="delPushLog" @afterDelete="loadFilter">{{ $t('删除') }} </DeleteRecord>
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
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { PushLogFilterParams, PushLogFilterState } from "@/types/user/pushLog";
import { batchSubmit, delPushLog, getPushLogList, recallPushLog } from "@/api/user/pushLog";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
// 基本参数定义
const filterState = ref<PushLogFilterState[]>([]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const advancedSearch = ref<boolean>(false);
const filterParams = reactive<PushLogFilterParams>({
    //初使化用于查询的参数
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: ""
});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getPushLogList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});

// 参数查询
const onSearchSubmit = () => {
    loadFilter();
};
// 修改排序
const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
    filterParams.sortField = prop;
    filterParams.sortOrder = order == "ascending" ? "asc" : order == "descending" ? "desc" : "";
    loadFilter();
};

// 批量操作
const onBatchSubmit = async (action: string) => {
    try {
        const result = await batchSubmit(action, { ids: selectedIds.value });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};
// 多选操作
const onSelectChange = (e: PushLogFilterState[]) => {
    selectedIds.value = e.map((item) => item.brandId);
};

// 更新首字母
// const onUpdateFirstWorld = async () => {
//     try {
//         const result = await updateFirstWorld();
//         message.success(t('操作成功'));
//         loadFilter();
//     } catch (error) {
//         message.error(error.message);
//     }
// }
</script>
