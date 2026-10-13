<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.prevent="onSearchSubmit">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('组名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput v-model="filterParams.groupName" name="groupName" :placeholder="$t('输入组名称')"
                                                clearable @keydown.enter.prevent="onSearchSubmit" @clear="onSearchSubmit">
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span
                                                            class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
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
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm :params="{ act: 'add' }" isDrawer path="user/userTags/Info" :title="$t('新增标签')"
                                        width="560px" @okCallback="loadFilter">
                                        <el-button type="primary">{{ $t('新增标签') }}</el-button>
                                    </DialogForm>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="id"
                            @sort-change="onSortChange">
                            <el-table-column :label="$t('组名称')" prop="groupName" min-width="150">
                                <template #default="{ row }">
                                    <span class="group-name-cell">
                                        <el-tag v-if="(row.isTop ?? 0) > 0" class="top-tag" effect="light" size="small">
                                            <SvgIcon name="setting-top" color="#ff4b4b" class="top-tag-icon" /> {{ $t('置顶') }} </el-tag>
                                        <span>{{ row.groupName }}</span>
                                    </span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('标签值')" prop="tagValues" min-width="250">
                                <template #default="{ row }">

                                    <span>{{ row.tagValues || '--' }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column prop="customerCount" min-width="120">
                                <template #header>
                                    <span>{{ $t('人数') }}</span>
                                    <el-tooltip :content="$t('符合标签的人数')" placement="top">
                                        <el-icon style="margin-left: 5px" size="14" color="#999">
                                            <QuestionFilled />
                                        </el-icon>
                                    </el-tooltip>
                                </template>
                                <template #default="{ row }">
                                    <span>{{ row.customerCount ?? 0 }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="createTime" min-width="160"
                                sortable="custom"></el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" width="200">
                                <template #default="{ row }">
                                    <div class="flex flex-align-center">
                                        <DialogForm :params="{ act: 'detail', id: row.id }" isDrawer
                                            path="user/userTags/Info" :title="$t('编辑标签')" width="560px"
                                            @okCallback="loadFilter">
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <a class="btn-link" @click="handleDelete(row.id)">{{ $t('删除') }}</a>
                                        <el-divider direction="vertical" />
                                        <a class="btn-link" @click="handleSetTop(row)">{{ (row.isTop ?? 0) > 0 ? $t('取消置顶') : $t('置顶') }}</a>
                                    </div>
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
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                            @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { UserTagFilterParams, UserTagFilterState } from "@/types/user/userTags.d";
import { getMemberTagGroupList, delMemberTagGroup, setTopMemberTagGroup, cancelTopMemberTagGroup } from "@/api/user/userTags";
import { useListRequest } from "@/hooks/useListRequest";
import { message } from "ant-design-vue";
import { ElMessageBox } from "element-plus";
import { QuestionFilled } from "@element-plus/icons-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    resetParams
} = useListRequest<UserTagFilterState, UserTagFilterParams>({
    apiFunction: getMemberTagGroupList,
    idKey: "id",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        groupName: "",
        page: 1,
        size: config.get("pageSize")
    }
});

const handleDelete = async (id: number) => {
    try {
        await ElMessageBox.confirm(t('确认要删除标签吗？删除后无法恢复！'), "", {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            type: "warning"
        });
        await delMemberTagGroup({ id });
        message.success(t('删除成功'));
        loadFilter();
    } catch (error: any) {
        if (error === "cancel") return;
        message.error(error.message);
    }
};

const handleSetTop = async (row: UserTagFilterState) => {
    if (!row?.id) return;
    const isTopped = (row.isTop ?? 0) > 0;
    try {
        if (isTopped) {
            await cancelTopMemberTagGroup({ id: row.id });
            message.success(t('取消置顶成功'));
        } else {
            await setTopMemberTagGroup({ id: row.id });
            message.success(t('置顶成功'));
        }
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};

loadFilter();
</script>

<style scoped lang="less">
.group-name-cell {
    display: inline-flex;
    align-items: center;
    gap: 8px;
}

.top-tag {
    :deep(&.el-tag) {
        background: rgba(255, 75, 75, 0.08);
        border: none;
        border-radius: 4px;
        color: #ff4b4b;
        transition: none;
    }

    .top-tag-icon {
        width: 14px;
        height: 14px;
        vertical-align: -2px;
        margin-right: 2px;
    }
}
</style>
