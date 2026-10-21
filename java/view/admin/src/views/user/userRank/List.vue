/** 特殊会员等级列表 - 管理固定等级会员组，不随积分变化 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="notice-warp">
                        <p>{{ $t('提示：特殊会员组不会随会员等级积分的变化而变化。') }}</p>
                    </div>
                    <el-form :model="filterParams">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('输入等级名称')" @keyup.enter="onSearchSubmit">
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
                                                path="user/userRank/Info"
                                                :title="$t('添加会员等级')"
                                                width="600px"
                                                @okCallback="loadFilter"
                                            >
                                                <el-button type="primary">{{ $t('添加会员等级') }}</el-button>
                                            </DialogForm>
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
                            row-key="rankId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :width="200" :label="$t('等级名称')" prop="rankName">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <PopForm
                                            v-model:org-value="row.rankName"
                                            :max="10"
                                            :params="{ id: row.rankId, field: 'rankName' }"
                                            :requestApi="updateUserRankField"
                                            :label="$t('等级名称')"
                                            type="textarea"
                                        >
                                            <div>{{ row.rankName }}</div>
                                        </PopForm>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('等级标志')">
                                <template #default="{ row }">
                                    <Image :src="row.rankIco" fit="contain" style="height: 25px; width: 60px" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('积分下限')">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.minGrowthPoints"
                                        :params="{ id: row.rankId, field: 'minGrowthPoints' }"
                                        :requestApi="updateUserRankField"
                                        :label="$t('积分下限')"
                                        type="input"
                                    >
                                        <div>{{ row.minGrowthPoints || "0" }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('积分上限')">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.maxGrowthPoints"
                                        :params="{ id: row.rankId, field: 'maxGrowthPoints' }"
                                        :requestApi="updateUserRankField"
                                        :label="$t('积分上限')"
                                        type="input"
                                    >
                                        <div>{{ row.maxGrowthPoints || "-" }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('折扣(%)')">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.discount"
                                        :minNum="1"
                                        :maxNum="100"
                                        :params="{ id: row.rankId, field: 'discount' }"
                                        :requestApi="updateUserRankField"
                                        :label="$t('折扣(%)')"
                                        type="input"
                                    >
                                        <div>{{ row.discount || "0" }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('等级类型')" prop="rankTypeName"> </el-table-column>
                            <el-table-column :label="$t('显示价格')" prop="showPrice">
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.showPrice"
                                        :params="{ id: row.rankId, field: 'showPrice' }"
                                        :requestApi="updateUserRankField"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.rankId }"
                                        isDrawer
                                        path="user/userRank/Info"
                                        :title="$t('编辑会员等级')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.rankId }" :requestApi="delUserRank" @afterDelete="loadFilter">{{ $t('删除') }} </DeleteRecord>
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
import { UserRankFilterParams, UserRankFilterState } from "@/types/user/userRank.d";
import { batchSubmit, delUserRank, getUserRankList, updateUserRankField } from "@/api/user/userRank";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
// 基本参数定义
const filterState = ref(<UserRankFilterState[]>[]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const advancedSearch = ref<boolean>(false);
const filterParams = reactive<UserRankFilterParams>({
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
        const result = await getUserRankList({ ...filterParams });
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
        await loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};
// 多选操作
const onSelectChange = (e: UserRankFilterState[]) => {
    selectedIds.value = e.map((item: any) => item.rankId);
};
</script>
<style lang="less" scoped>
.notice-warp {
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}
</style>
