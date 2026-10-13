<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="sign-in-rule-tip" v-if="tipVisible && continuousSignIn !== null">
                <div class="tips-item">
                    <div class="info flex">
                        <div class="icon mr10">
                            <el-icon size="18" color="#ed6a18"><WarnTriangleFilled /></el-icon>
                        </div>
                        <div class="tips-content">
                            <div class="tip-title">{{ $t("签到规则说明") }}</div>
                            <div>{{ $t("1、每天最多签到一次；按签到次数匹配对应积分奖励；") }}</div>
                            <div v-if="continuousSignIn === 1">
                                {{ $t("2、达到最后一天后重新从第一天开始；当前系统漏签会重置签到天数，若无需连续签到") }}<a class="tip-link" @click="goShoppingSettings">{{ $t("去关闭>") }}</a>
                            </div>
                            <div v-else>
                                {{ $t("2、达到最后一天后重新从第一天开始；当前系统漏签不会清零，按累计签到次数计算；如需连续签到请") }}<a class="tip-link" @click="goShoppingSettings">{{ $t("去开启>") }}</a>
                            </div>
                            <div>{{ $t("3、签到配置中的天数和积分决定各档位奖励。") }}</div>
                        </div>
                    </div>
                    <div class="btn" @click.stop="tipVisible = false">
                        <el-icon size="18"><Close /></el-icon>
                    </div>
                </div>
            </div>
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                name="keyword"
                                                v-model="filterParams.keyword"
                                                :placeholder="$t('输入关键词')"
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
                                            isDrawer
                                            @okCallback="loadFilter"
                                            :title="$t('新增签到')"
                                            width="600px"
                                            path="promotion/signInSetting/Info"
                                            :params="{ act: 'add' }"
                                        >
                                            <el-button type="primary">{{ $t("新增签到") }}</el-button>
                                        </DialogForm>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <el-space>
                                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                                <template #reference>
                                                    <el-button :disabled="selectedIds.length === 0">{{ $t("批量删除") }}</el-button>
                                                </template>
                                            </el-popconfirm>
                                            <span v-if="selectedIds.length > 0"
                                                >{{ $t("已选择：") }}<b>{{ selectedIds.length }}</b
                                                >{{ $t("项") }}</span
                                            >
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
                            row-key="id"
                            @selection-change="onSelectChange"
                            :total="total"
                            @sort-change="onSortChange"
                            :loading="loading"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('名称')" prop="name" :width="300"> </el-table-column>
                            <el-table-column :label="$t('赠送积分')" prop="points"> </el-table-column>
                            <el-table-column prop="sortOrder" sortable="custom">
                                <template #header>
                                    <el-tooltip effect="light" placement="top" show-after="300" trigger="hover">
                                        <template #content>
                                            <div style="width: 300px; padding: 8px; line-height: 22px; font-size: 14px">
                                                {{ $t("排序数字越小，签到项越靠前；排序相同时，后添加的签到项会优先显示。") }}
                                            </div>
                                        </template>
                                        <span class="flex flex-align-center" style="display: inline-flex; align-items: center; white-space: nowrap">
                                            <span>{{ $t("排序") }}</span>
                                            <el-icon style="margin-left: 5px" size="16" color="#969799"><QuestionFilled /></el-icon>
                                        </span>
                                    </el-tooltip>
                                </template>
                                <template #default="{ row }">
                                    <PopForm
                                        :label="$t('排序')"
                                        type="integer"
                                        :requestApi="updateSignInSettingField"
                                        v-model:org-value="row.sortOrder"
                                        @callback="loadFilter"
                                        :params="{ id: row.id, field: 'sortOrder' }"
                                        :required="false"
                                        :extra="$t('可选，填写后请输入非负整数')"
                                    >
                                        <div>{{ row.sortOrder ?? "-" }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="150">
                                <template #default="{ row }">
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('编辑签到')"
                                        width="600px"
                                        path="promotion/signInSetting/Info"
                                        :params="{ act: 'detail', id: row.id }"
                                    >
                                        <a class="btn-link">{{ $t("编辑") }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord @afterDelete="loadFilter" :requestApi="delSignInSetting" :params="{ id: row.id }">{{
                                        $t("删除")
                                    }}</DeleteRecord>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t("暂无数据") }}</div>
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
import { onMounted, ref } from "vue";
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { DeleteRecord, Pagination } from "@/components/list";
import { Close, QuestionFilled, WarnTriangleFilled } from "@element-plus/icons-vue";
import { useConfigStore } from "@/store/config";
import { getConfigShopping } from "@/api/setting/config";
import { useRouter } from "vue-router";
import { SignInSettingFilterState, SignInSettingFilterParams } from "@/types/promotion/signInSetting.d";
import { batchSignInSetting, delSignInSetting, getSignInSettingList, updateSignInSettingField } from "@/api/promotion/signInSetting";
const config: any = useConfigStore();
const router = useRouter();
const continuousSignIn = ref<number | null>(null);
const tipVisible = ref(true);

const goShoppingSettings = () => {
    router.push({ name: "baseShoppingManage" });
};

const loadShoppingConfig = async () => {
    try {
        const result = await getConfigShopping();
        continuousSignIn.value = Number(result.continuousSignIn ?? 0);
    } catch {
        continuousSignIn.value = 0;
    }
};
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
} = useListRequest<SignInSettingFilterState, SignInSettingFilterParams>({
    apiFunction: getSignInSettingList,
    idKey: "id",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        page: 1,
        size: config.get("pageSize")
    }
});

const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSignInSetting);
};

// 初始化加载
loadFilter();
onMounted(loadShoppingConfig);
</script>

<style scoped lang="less">
.sign-in-rule-tip {
    margin-bottom: 16px;
    .tips-item {
        border: #ffe6d7 1px solid;
        background-color: #ffefe6;
        padding: 10px 20px;
        line-height: 24px;
        border-radius: 4px;
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
    }
    .info {
        align-items: flex-start;
    }
    .icon {
        margin-top: 3px;
    }
    .tip-title {
        font-weight: 600;
    }
    .btn {
        margin-top: 3px;
        cursor: pointer;
        color: #909399;
    }
    .tip-link {
        color: var(--el-color-primary);
        cursor: pointer;
        margin-left: 4px;
    }
}
</style>
