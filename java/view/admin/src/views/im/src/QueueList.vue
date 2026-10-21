<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp" style="margin-bottom: 0">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('咨询来源：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.userFrom" clearable>
                                                <el-option value="" :label="$t('全部')" />
                                                <el-option value="pc" label="pc" />
                                                <el-option value="h5" label="h5" />
                                                <el-option value="miniProgram" :label="$t('小程序')" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <el-button type="primary" @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="brandId" @sort-change="onSortChange">
                            <el-table-column :label="$t('客户')" prop="brandId">
                                <template #default="{ row }">
                                    <div class="kehu">
                                        <el-avatar class="av" :src="imageFormat(row.user.avatar)"></el-avatar>
                                        <div class="title">{{ row.user?.nickname }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('最新两条消息')">
                                <template #default="{ row }">
                                    <div class="message-list">
                                        <div class="item" v-for="it in row.userLastTwoMessage.slice(0, 2)">
                                            <StatusDot color="#44bb00" :flicker="false"></StatusDot>
                                            <span>{{ it.content.content }}</span>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column prop="addTime" :width="160" :label="$t('进入排队时间')"></el-table-column>
                            <el-table-column :width="100" :label="$t('排队时长')" sortable="custom">
                                <template #default="{ row }">
                                    <RelativeTime :time="row.addTime" />
                                </template>
                            </el-table-column>
                            <el-table-column :width="100" :label="$t('咨询来源')" prop="userFrom"></el-table-column>
                            <el-table-column :width="140" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DeleteRecord
                                        :title="$t('您确定要接入会话吗？')"
                                        :message="$t('接入成功')"
                                        :params="{ conversationId: row.id }"
                                        :requestApi="transferConversation"
                                        @afterDelete="loadFilter"
                                        >{{ $t('接入会话') }}</DeleteRecord
                                    >
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ conversationId: row.id }" :requestApi="delConversation" @afterDelete="loadFilter">{{ $t('删除') }}</DeleteRecord>
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
import { PopForm } from "@/components/pop-form";
import { onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { Image } from "@/components/image";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { QueueListFilterParams, SearchInfoList } from "@/types/im/im";
import { getWaitServantList, transferConversation, delConversation } from "@/api/im/conversation";
import { imageFormat } from "@/utils/format";
import { RelativeTime } from "@/components/form";
import StatusDot from "@/components/form/src/StatusDot.vue";

const config: any = useConfigStore();
// 基本参数定义
const filterState = ref<SearchInfoList[]>([]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const filterParams = reactive<QueueListFilterParams>({
    //初使化用于查询的参数
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: "",
    firstWord: "",
    userFrom: ""
});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getWaitServantList({ ...filterParams });
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

defineExpose({
    loadFilter
});
</script>
<style lang="less" scoped>
.batch {
    margin-top: 20px;
}

.kehu {
    display: flex;
    align-items: center;
    gap: 8px;
    .av {
        min-width: 40px;
        min-height: 40px;
    }
    .title {
        /* 占满剩余宽度 */
        flex-grow: 1;
        /* 文字不换行 */
        white-space: nowrap;
        /* 超出部分隐藏 */
        overflow: hidden;
        /* 超出部分显示为省略号 */
        text-overflow: ellipsis;
    }
}
</style>
