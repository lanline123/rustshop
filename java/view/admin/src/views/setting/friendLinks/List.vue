<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="keyword"
                                            :placeholder="$t('输入链接名称')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        >
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <DialogForm
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="setting/friendLinks/Info"
                                        :title="$t('添加友情链接')"
                                        width="580px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('添加友情链接') }}</el-button>
                                    </DialogForm>
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
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="linkId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('链接名称')">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.linkTitle"
                                        :max="10"
                                        :params="{ id: row.linkId, field: 'linkTitle' }"
                                        :requestApi="updateFriendLinksField"
                                        :label="$t('链接名称')"
                                        type="input"
                                    >
                                        <div>{{ row.linkTitle }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('链接地址')" prop="linkUrl">
                                <template #default="{ row }">
                                    <a :href="row.linkUrl" target="_blank">{{ row.linkUrl }}</a>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('链接LOGO')">
                                <template #default="{ row }">
                                    <Image :src="row.linkLogo" fit="contain" style="height: 25px; width: 60px" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.sortOrder"
                                        :params="{ id: row.linkId, field: 'sortOrder' }"
                                        :requestApi="updateFriendLinksField"
                                        :extra="$t('默认值为50，数值越小，排序越靠前')"
                                        :label="$t('排序')"
                                        type="integer"
                                    >
                                        <div>{{ row.sortOrder }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.linkId }"
                                        isDrawer
                                        path="setting/friendLinks/Info"
                                        :title="$t('编辑友情链接')"
                                        width="580px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.linkId }" :requestApi="delFriendLinks" @afterDelete="loadFilter">{{ $t('删除') }} </DeleteRecord>
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
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import type { FriendLinksFilterParams, FriendLinksFilterState } from "@/types/setting/friendLinks.d";
import { batchSubmit, delFriendLinks, getFriendLinksList, updateFriendLinksField } from "@/api/setting/friendLinks";
import { Image } from "@/components/image";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
import { useListRequest } from "@/hooks/useListRequest";
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    onBatchAction,
    resetParams
} = useListRequest<FriendLinksFilterState, FriendLinksFilterParams>({
    apiFunction: getFriendLinksList,
    idKey: "linkId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        page: 1,
        size: config.get("pageSize")
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();
// 基本参数定义
// const filterState = ref(<FriendLinksFilterState[]>[]);
// const loading = ref<boolean>(true);
// const total = ref<number>(0);
// const selectedIds = ref<number[]>([]);
// const filterParams = reactive<FriendLinksFilterParams>({
//     //初始化用于查询的参数
//     page: 1,
//     size: config.get("pageSize"),
//     sortField: "",
//     sortOrder: "",
//     keyword: ""
// });
// // 获取列表的查询结果
// const loadFilter = async () => {
//     loading.value = true;
//     try {
//         const result = await getFriendLinksList({ ...filterParams });
//         filterState.value = result.records;
//         total.value = result.total;
//     } catch (error: any) {
//         message.error(error.message);
//     } finally {
//         loading.value = false;
//     }
// };
// onMounted(() => {
//     loadFilter();
// });

// // 参数查询
// const onSearchSubmit = () => {
//     loadFilter();
// };
// // 修改排序
// const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
//     filterParams.sortField = prop;
//     filterParams.sortOrder = order == "ascending" ? "asc" : order == "descending" ? "desc" : "";
//     loadFilter();
// };

// // 批量操作
// const onBatchSubmit = async (action: string) => {
//     try {
//         const result = await batchSubmit(action, { ids: selectedIds.value });
//         message.success(t('操作成功'));
//         loadFilter();
//     } catch (error: any) {
//         message.error(error.message);
//     }
// };
// // 多选操作
// const onSelectChange = (e: FriendLinksFilterState[]) => {
//     selectedIds.value = e.map((item) => item.linkId);
// };
</script>
