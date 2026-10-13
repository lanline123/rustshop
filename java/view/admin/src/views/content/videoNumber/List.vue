<template>
    <div class="page-alert">
        <el-icon size="18" color="#ed6a18"><WarnTriangleFilled /></el-icon>
        <span
            >{{ $t('关联与小程序同主体的视频号ID。添加后，小程序可直接跳转至视频号直播间') }} <el-popover placement="bottom" :width="800" trigger="hover">
                <template #reference>
                    <span class="example-link">{{ $t('示例') }}</span>
                </template>
                <img src="@/assets/live/videoId.png" alt="示例" style="width: 100%; display: block" />
            </el-popover>
        </span>
    </div>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入视频号ID/昵称')"
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
                                        <label class="control-label"><span>{{ $t('关联时间：') }}</span></label>
                                        <SelectTimeInterval
                                            v-model:end-date="filterParams.relationTimeEnd"
                                            v-model:start-date="filterParams.relationTimeStart"
                                            :clearable="false"
                                            type="date"
                                            value-format="YYYY-MM-DD"
                                        />
                                    </div>
                                </div>
                            </div>

                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="control-container">
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                        <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        isDrawer
                                        :title="$t('关联视频号')"
                                        width="550px"
                                        path="content/videoNumber/Info"
                                        :params="{ act: 'add', existingFinderIds: filterState.map((item) => item.finderId) }"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('关联视频号') }}</el-button>
                                    </DialogForm>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>

                <div class="table-container">
                    <el-table :data="filterState" :loading="loading" row-key="id" @sort-change="onSortChange">
                        <el-table-column :label="$t('视频号ID')" prop="finderId" />
                        <el-table-column :label="$t('关联时间')" prop="relationTime" sortable="custom" />
                        <el-table-column :label="$t('操作')" width="180" fixed="right">
                            <template #default="{ row }">
                                <el-space :size="4">
                                    <DialogForm
                                        isDrawer
                                        :title="$t('编辑视频号')"
                                        width="550px"
                                        path="content/videoNumber/Info"
                                        :params="{
                                            act: 'detail',
                                            id: row.id,
                                            existingFinderIds: filterState.filter((item) => item.id !== row.id).map((item) => item.finderId)
                                        }"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary" link>{{ $t('编辑') }}</el-button>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <el-button type="primary" link @click="handleUnbind(row)">{{ $t('解除关联') }}</el-button>
                                </el-space>
                            </template>
                        </el-table-column>

                        <template #empty>
                            <div class="empty-warp">
                                <div class="empty-bg">{{ $t('暂无数据') }}</div>
                            </div>
                        </template>
                    </el-table>

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
import { ElMessageBox } from "element-plus";
import { message } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { SelectTimeInterval } from "@/components/select";
import { useListRequest } from "@/hooks/useListRequest";
import { delWechatVideoAccount, getWechatVideoAccountList } from "@/api/content/wechatVideoAccount";
import type { WechatVideoAccountFilterParams, WechatVideoAccountState } from "@/types/content/wechatVideoAccount.d";
import { WarnTriangleFilled } from "@element-plus/icons-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();

const statusOptions = [
    { label: t('全部'), value: undefined },
    { label: t('草稿'), value: 0 },
    { label: t('预告中'), value: 1 },
    { label: t('直播中'), value: 2 },
    { label: t('已结束'), value: 3 },
    { label: t('禁播'), value: 4 },
    { label: t('暂停'), value: 5 },
    { label: t('异常'), value: 6 },
    { label: t('已过期'), value: 7 }
];

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    resetParams
} = useListRequest<WechatVideoAccountState, WechatVideoAccountFilterParams>({
    apiFunction: getWechatVideoAccountList,
    idKey: "id",
    defaultParams: {
        keyword: "",
        finderId: "",
        accountStatus: undefined,
        relationTimeStart: "",
        relationTimeEnd: "",
        sortField: "",
        sortOrder: "",
        page: 1,
        size: config.get("pageSize")
    }
});

const handleUnbind = async (row: WechatVideoAccountState) => {
    try {
        await ElMessageBox.confirm(t('确认解除视频号【{nickName}】的关联吗？', { nickName: row.nickName || row.finderId }), t('提示'), {
            confirmButtonText: t('确认'),
            cancelButtonText: t('取消'),
            type: "warning"
        });
        await delWechatVideoAccount({ id: row.id });
        message.success(t('解除关联成功'));
        loadFilter();
    } catch (error: any) {
        if (error === "cancel" || error === "close") return;
        message.error(error.message || t('解除关联失败'));
    }
};

loadFilter();
</script>

<style scoped lang="less">
.page-title {
    margin-bottom: 12px;
    color: #333;
    font-size: 18px;
    font-weight: 600;
}

.page-alert {
    margin-bottom: 16px;
    color: rgb(16, 16, 16);
    font-size: 14px;
    line-height: 34px;
    height: 34px;
    background-color: rgb(255, 239, 230);
    padding: 4px 20px;
    display: flex;
    align-items: center;
    gap: 8px;
}
.example-link {
    color: var(--el-color-primary);
    cursor: pointer;
    margin-left: 4px;
}
</style>
