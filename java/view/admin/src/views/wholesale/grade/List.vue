/**
 * @file 批发商等级管理
 * @description 列表 + 新增/编辑抽屉；状态即时启停；删除（软删；已绑定客户数>0 前端拦截，默认等级后端拦截）；默认等级只读反映（设默认在批发设置页）
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form @submit.native.prevent>
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadData"
                                        :title="$t('新增批发商等级')"
                                        width="600px"
                                        path="wholesale/grade/Info"
                                        :params="{ id: 0 }"
                                    >
                                        <el-button type="primary">{{ $t('新增批发商等级') }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="listData" row-key="gradeId">
                            <el-table-column :label="$t('批发商等级')" min-width="200">
                                <template #default="{ row }">
                                    <div class="flex flex-align-center" style="gap: 8px; flex-wrap: wrap">
                                        <span>{{ row.gradeName }}</span>
                                        <el-tag v-if="row.isDefault" size="small" effect="plain" type="primary" disable-transitions>{{ $t('默认等级') }}</el-tag>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('已绑定客户数')" prop="userCount" min-width="120" align="center">
                                <template #default="{ row }">
                                    <a v-if="(row.userCount ?? 0) > 0" class="btn-link" @click="goWholesalerList(row.gradeId)">{{ row.userCount }}</a>
                                    <span v-else>0</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('状态')" min-width="100" align="center">
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.status"
                                        :requestApi="gradeStatusApi"
                                        :params="{ gradeId: row.gradeId }"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="addTime" min-width="180" />
                            <el-table-column :label="$t('操作')" fixed="right" width="150" align="center">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <DialogForm
                                            isDrawer
                                            @okCallback="loadData"
                                            :title="$t('编辑批发商等级')"
                                            width="600px"
                                            path="wholesale/grade/Info"
                                            :params="{ id: row.gradeId, detail: row }"
                                        >
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <a class="btn-link" @click="onDelete(row)">{{ $t('删除') }}</a>
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
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import "@/style/css/list.less";
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { message, Modal } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import { Switch } from "@/components/list";
import { getGradeList, setGradeStatus, deleteGrade } from "@/api/wholesale/grade";
import type { WholesalerGradeVO } from "@/types/wholesale/grade";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const router = useRouter();
const listData = ref<WholesalerGradeVO[]>([]);
const loading = ref<boolean>(false);

/** 跳转批发商列表，按等级筛选 */
const goWholesalerList = (gradeId: number) => {
    router.push({
        path: "/wholesale/wholesaler/list",
        query: { gradeId: String(gradeId) }
    });
};

const loadData = async () => {
    loading.value = true;
    try {
        listData.value = (await getGradeList({ withUserCount: true })) || [];
    } catch (error: any) {
        message.error(error.message || t('加载失败'));
    } finally {
        loading.value = false;
    }
};

/** 启停：禁用默认等级时后端返回 warning，透传提示 */
const gradeStatusApi = async (p: any) => {
    const res: any = await setGradeStatus({ gradeId: p.gradeId, status: p.val });
    if (res && res.warning) {
        message.warning(res.warning);
    }
    return res;
};

/** 删除：默认等级、已绑定客户数>0 时前端拦截；否则二次确认后软删 */
const onDelete = (row: WholesalerGradeVO) => {
    if (row.isDefault) {
        Modal.warning({
            title: t('提示'),
            content: t('默认等级不能删除，如需删除请前往批发商设置修改默认批发商'),
            okText: t('确定'),
            maskClosable: true
        });
        return;
    }
    if ((row.userCount ?? 0) > 0) {
        Modal.warning({
            title: t('删除失败'),
            content: t('该类型下已有批发商用户。在删除该类型之前，必须先删除该类型下的所有批发商用户。'),
            okText: t('确定'),
            maskClosable: true
        });
        return;
    }
    Modal.confirm({
        title: t('确认删除该批发商等级？'),
        okText: t('确定'),
        okType: "danger",
        maskClosable: true,
        async onOk() {
            try {
                await deleteGrade({ gradeId: row.gradeId });
                message.success(t('操作成功'));
                loadData();
            } catch (error: any) {
                message.error(error.message || t('删除失败'));
            }
        }
    });
};

onMounted(() => {
    loadData();
});
</script>
