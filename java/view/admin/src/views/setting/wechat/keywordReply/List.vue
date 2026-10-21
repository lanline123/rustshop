<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <el-alert
                    v-if="!kefuEnabled"
                    type="warning"
                    show-icon
                    :closable="false"
                    :title="$t('未检测到客服消息发送能力，回复全部将降级为仅发送首条')"
                    style="margin-bottom: 12px"
                />
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('规则名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.ruleName"
                                                name="ruleName"
                                                :placeholder="$t('输入规则名称')"
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
                                        <label class="control-label"><span>{{ $t('状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select
                                                v-model="filterParams.enabled"
                                                :placeholder="$t('请选择状态')"
                                                clearable
                                                @clear="onSearchSubmit"
                                                @change="onSearchSubmit"
                                            >
                                                <el-option :value="1" :label="$t('启用')" />
                                                <el-option :value="0" :label="$t('停用')" />
                                            </el-select>
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
                                    <el-tooltip v-if="addDisabled" :content="$t('规则总数已达上限 200，请先删除一些规则')" placement="top">
                                        <span>
                                            <el-button type="primary" disabled>{{ $t('添加回复') }}</el-button>
                                        </span>
                                    </el-tooltip>
                                    <DialogForm
                                        v-else
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="setting/wechat/keywordReply/Info"
                                        :title="$t('添加回复')"
                                        width="1200px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('添加回复') }}</el-button>
                                    </DialogForm>
                                    <el-popconfirm :title="$t('您确认要批量启用所选数据吗？')" @confirm="onBatchSetEnabled(1)">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量启用') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <el-popconfirm :title="$t('您确认要批量停用所选数据吗？')" @confirm="onBatchSetEnabled(0)">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量停用') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchDelete">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                </el-space>
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
                            row-key="id"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('规则名称')" prop="ruleName" min-width="180" show-overflow-tooltip />
                            <el-table-column :label="$t('关键词')" min-width="220" show-overflow-tooltip>
                                <template #default="{ row }">
                                    <span>{{ formatKeywords(row.keywords) }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('回复内容')" min-width="160">
                                <template #default="{ row }">
                                    <span>{{ formatReplySummary(row) }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('启用状态')" min-width="100">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.enabled" :params="{ id: row.id }" :requestApi="setEnabledWrap" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="createdAt" min-width="170" />
                            <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.id }"
                                        isDrawer
                                        path="setting/wechat/keywordReply/Info"
                                        :title="$t('编辑回复')"
                                        width="1200px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.id }" :requestApi="delSingleWrap" @afterDelete="loadFilter">{{ $t('删除') }}</DeleteRecord>
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
import { computed, onMounted, ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { listKeywordRule, delKeywordRule, setKeywordRuleEnabled, getKefuCapability } from "@/api/setting/wechat";
import type { KeywordRuleVO, KeywordRuleListDTO } from "@/types/setting/wechat.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();

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
    resetParams
} = useListRequest<KeywordRuleVO, KeywordRuleListDTO>({
    apiFunction: listKeywordRule,
    idKey: "id",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        ruleName: "",
        enabled: undefined
    }
});

// 列表展示：关键字以中文逗号拼接（后端返回数组，前端自行拼接）
const formatKeywords = (keywords?: string[] | null) => {
    if (!keywords || keywords.length === 0) return "-";
    return keywords
        .map((k) => (k ?? "").trim())
        .filter(Boolean)
        .join(",");
};

// 列表展示：按消息类型拼接回复数量，如「1 文本 2 图文」；全部为 0 时兜底为占位符
const formatReplySummary = (row: KeywordRuleVO) => {
    const parts: string[] = [];
    if ((row.textReplyCount ?? 0) > 0) parts.push(t('{textReplyCount} 文本', { textReplyCount: row.textReplyCount }));
    if ((row.newsReplyCount ?? 0) > 0) parts.push(t('{newsReplyCount} 图文', { newsReplyCount: row.newsReplyCount }));
    return parts.length ? parts.join(",") : "-";
};

// 适配 DeleteRecord（params 直传）→ 后端要求 ids 数组
const delSingleWrap = (params: { id: number }) => delKeywordRule([params.id]);

// 适配 Switch（追加 val）→ 后端要求 (ids, enabled)
const setEnabledWrap = (params: { id: number; val: 0 | 1 }) => setKeywordRuleEnabled([params.id], params.val);

// 批量删除
const onBatchDelete = async () => {
    if (selectedIds.value.length === 0) {
        message.warning(t('请至少选择一项'));
        return;
    }
    try {
        await delKeywordRule(selectedIds.value as number[]);
        message.success(t('操作成功'));
        selectedIds.value = [];
        loadFilter();
    } catch (error: any) {
        message.error(error.message || t('操作失败'));
    }
};

// 批量启停（enabled=1 启用 / 0 停用）
const onBatchSetEnabled = async (enabled: 0 | 1) => {
    if (selectedIds.value.length === 0) {
        message.warning(t('请至少选择一项'));
        return;
    }
    try {
        await setKeywordRuleEnabled(selectedIds.value as number[], enabled);
        message.success(t('操作成功'));
        selectedIds.value = [];
        loadFilter();
    } catch (error: any) {
        message.error(error.message || t('操作失败'));
    }
};

// 客服消息能力（§3.4 降级提示）
const kefuEnabled = ref(true);
const loadCapability = async () => {
    try {
        const res = await getKefuCapability();
        kefuEnabled.value = !!res?.kefuEnabled;
    } catch {
        // 探测失败不打扰用户
        kefuEnabled.value = true;
    }
};

// 达到全局 200 条上限时禁用添加按钮
const addDisabled = computed(() => (total.value ?? 0) >= 200);

onMounted(() => {
    loadCapability();
});

loadFilter();
</script>
