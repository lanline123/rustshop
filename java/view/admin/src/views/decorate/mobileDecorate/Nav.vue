<template>
    <div class="container">
        <!-- ==================== 列表模式 ==================== -->
        <div v-if="viewMode === 'list'" class="content_wrapper">
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
                                            :placeholder="$t('输入导航名称')"
                                            clearable
                                            @keyup.enter="onSearchSubmit"
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
                                    <!-- 新增导航：进入导航编辑器（新建模式），编辑器内自动弹出模板市场 -->
                                    <el-button type="primary" @click="onCreateNav">{{ $t('新增导航') }}</el-button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :total="total" @sort-change="onSortChange" :loading="loading">
                            <el-table-column :label="$t('导航名称')" prop="decorateId" :min-width="300">
                                <template #default="{ row }">
                                    <el-space>
                                        <el-tooltip v-if="row.isHome === 1" effect="light" placement="top" :content="$t('当前默认导航')">
                                            <span class="default-page-icon">
                                                <SvgIcon name="decorate-home" />
                                            </span>
                                        </el-tooltip>
                                        <PopForm
                                            :label="$t('导航名称')"
                                            type="textarea"
                                            :requestApi="updateDecorateFiled"
                                            v-model:org-value="row.decorateTitle"
                                            :params="{ id: row.decorateId, field: 'decorateTitle' }"
                                            :max="50"
                                        >
                                            <div>{{ row.decorateTitle }}</div>
                                        </PopForm>
                                    </el-space>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('发布状态')" prop="status" sortable="custom" :width="150">
                                <template #default="{ row }">
                                    <span class="text-status" :class="{ success: row.status == 1, default: row.status != 1 }">{{ row.status == 1 ? $t('已发布') : $t('未发布') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('更新时间')" prop="updateTime" sortable="custom" :min-width="180">
                                <template #default="{ row }">
                                    {{ row.updateTime }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="240">
                                <template #default="{ row }">
                                    <el-space>
                                        <a class="btn-link" @click="onEditNav(row)">{{ $t('编辑') }}</a>
                                        <template v-if="row.isHome === 1">
                                            <el-divider direction="vertical" />
                                            <DialogForm
                                                @okCallback="loadFilter"
                                                :title="$t('更换默认导航')"
                                                width="800px"
                                                :showFooter="false"
                                                path="decorate/mobileDecorate/SwitchDefaultPage"
                                                :params="{ currentRow: row, decorateType: 1 }"
                                            >
                                                <a class="btn-link">{{ $t('更换') }}</a>
                                            </DialogForm>
                                        </template>
                                        <el-divider direction="vertical" />
                                        <a class="btn-link" @click="onCopy(row)">{{ $t('复制') }}</a>
                                        <template v-if="row.isHome !== 1 || isTemplateExport">
                                            <el-divider direction="vertical" />
                                            <el-dropdown trigger="hover" :hide-on-click="false">
                                                <el-icon class="btn-link" size="12"><MoreFilled /></el-icon>
                                                <template #dropdown>
                                                    <el-dropdown-menu>
                                                        <el-dropdown-item v-if="row.isHome !== 1">
                                                            <DeleteRecord @afterDelete="loadFilter" :requestApi="delDecorate" :params="{ id: row.decorateId }"
                                                                >{{ $t('删除') }}</DeleteRecord
                                                            >
                                                        </el-dropdown-item>
                                                        <el-dropdown-item v-if="isTemplateExport">
                                                            <a class="btn-link" @click="onExport(row)">{{ $t('导出') }}</a>
                                                        </el-dropdown-item>
                                                    </el-dropdown-menu>
                                                </template>
                                            </el-dropdown>
                                        </template>
                                    </el-space>
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

        <!-- ==================== 编辑模式（独立组件） ==================== -->
        <div v-else class="content_wrapper decoratePgaeWrap">
            <NavEdit :id="editId" :title="editTitle" :is-add="isAddNav" @back="backToList" />
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import "@/views/decorate/decorate/src/css/decorate.less";
import { ref } from "vue";
import { message } from "ant-design-vue";
import { MoreFilled } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { getDecorateList, delDecorate, updateDecorateFiled, updateDecorate, getDecorate } from "@/api/decorate/mobileDecorate";
import { copyDomText } from "@/utils/util";
import type { FilterState, FilterParams } from "@/types/decorate/mobileDecorate.d";
import NavEdit from "./NavEdit.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();

// 演示环境标识：控制「导出」入口显隐
const isTemplateExport = import.meta.env.VITE_IS_TEMPLATE_EXPORT || 0;

// ===================== 视图模式 =====================
const viewMode = ref<"list" | "edit">("list");

// ===================== 列表模式 =====================
const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange
} = useListRequest<FilterState, FilterParams>({
    apiFunction: getDecorateList,
    idKey: "decorateId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        keyword: "",
        decorateType: 1,
        pageType: 7,
        sortField: "",
        sortOrder: ""
    }
});

loadFilter();

// ===================== 编辑模式 =====================
const editId = ref<number>(0);
const editTitle = ref<string>("");
// 新建标识：true=新增导航（编辑器内自动弹模板市场），false=编辑既有导航
const isAddNav = ref<boolean>(false);

/** 新增导航：进入编辑器新建模式，编辑器内自动弹出模板市场 */
const onCreateNav = () => {
    editId.value = 0;
    editTitle.value = t('新增导航');
    isAddNav.value = true;
    viewMode.value = "edit";
};

/** 进入编辑 */
const onEditNav = (row: FilterState) => {
    editId.value = row.decorateId;
    editTitle.value = row.decorateTitle || t('编辑导航');
    isAddNav.value = false;
    viewMode.value = "edit";
};

/** 返回列表 */
const backToList = () => {
    viewMode.value = "list";
    editId.value = 0;
    editTitle.value = "";
    isAddNav.value = false;
    loadFilter();
};

/** 复制导航 */
const onCopy = async (row: FilterState) => {
    try {
        await updateDecorate("copy", { id: row.decorateId });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};

/** 导出导航详情 JSON（仅演示环境，复制到剪贴板） */
const onExport = async (row: FilterState) => {
    loading.value = true;
    try {
        const result = await getDecorate("detail", {
            id: row.decorateId,
            decorateType: 1,
            localeId: 0,
            parentId: 0
        });
        const jsonText = JSON.stringify(result, null, 2);
        try {
            await navigator.clipboard.writeText(jsonText);
        } catch {
            copyDomText(jsonText);
        }
        message.success(t('模板详情JSON已复制到剪贴板'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
</script>
<style lang="less" scoped>
.default-page-icon {
    display: inline-flex;
    align-items: center;
    color: var(--el-color-primary);
    :deep(.icon) {
        width: 18px;
        height: 18px;
    }
}

.text-status {
    &.success {
        &:before {
            content: "";
            display: inline-block;
            width: 6px;
            height: 6px;
            background-color: #52c41a;
            border-radius: 50%;
            margin-right: 4px;
            transform: translateY(-2px);
        }
    }
    &.default {
        &:before {
            content: "";
            display: inline-block;
            width: 6px;
            height: 6px;
            background-color: #929292;
            border-radius: 50%;
            margin-right: 4px;
            transform: translateY(-2px);
        }
    }
}
</style>
