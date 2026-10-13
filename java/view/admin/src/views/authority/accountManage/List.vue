/**
 * @file 账号管理列表页面
 * @description 平台账号管理
 * @features
 * - 账号列表展示（用户名、手机、邮箱、门店数、供应商数）
 * - 修改账号信息
 * - 修改密码
 * - 关联门店/供应商快捷跳转
 */
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
                                        <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('输入账号名称')"
                                            @keyup.enter="onSearchSubmit" clearable @clear="onSearchSubmit">
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span
                                                        class="iconfont icon-chakan1"></span></el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="adminId"
                            @sort-change="onSortChange">
                            <el-table-column width="200" :label="$t('账号名称')" prop="username">
                                <template #default="{ row }">
                                    <div class="avatar-username">
                                        <template v-if="extractContent(String(row.avatar)) == 'one'">
                                            <el-avatar :size="30" :src="imageFormat(row.avatar)" />
                                        </template>
                                        <template v-else-if="extractContent(String(row.avatar)) == 'def'">
                                            <el-avatar :size="30" :src="returnAvatar(row.avatar)" />
                                        </template>
                                        <template v-else>
                                            <el-avatar :size="30" src="" />
                                        </template>
                                        <div>
                                            <div>{{ row.username }}</div>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('手机')" prop="mobile">
                                <template #default="{ row }">
                                    <span v-if="row.mobile">
                                        <MobileCard :mobile="row.mobile"></MobileCard>
                                    </span>
                                    <span v-else>--</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('邮箱')" prop="email">
                                <template #default="{ row }">
                                    <span>{{ row.email || "-" }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('门店数')" prop="storeCount" align="center" v-if="isMerchant() || isStore()">
                                <template #default="{ row }">
                                    <router-link v-if="row.storeCount"
                                        :to="{ path: '/organize/store/list', query: { account: row.username } }">
                                        {{ row.storeCount }}
                                    </router-link>
                                    <span v-else>-</span>
                                </template>
                            </el-table-column>

                            <el-table-column :width="240" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DeleteRecord
                                        v-if="Number(row.isAdmin) !== 1"
                                        :params="{ adminId: row.adminId }"
                                        :requestApi="deleteAdminUser"
                                        :title="$t('确认删除账号？数据将永久删除，无法恢复')"
                                        :message="$t('删除成功')"
                                        @afterDelete="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('删除账号') }}</a>
                                    </DeleteRecord>
                                    <el-divider v-if="Number(row.isAdmin) !== 1" direction="vertical" />
                                    <DialogForm :params="{ act: 'detail', adminId: row.adminId }" isDrawer
                                        path="authority/accountManage/AccountInfo" :title="$t('修改账号信息')" width="600px"
                                        @okCallback="loadFilter">
                                        <a class="btn-link">{{ $t('修改账号') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DialogForm :params="{ act: 'detail', adminId: row.adminId }" isDrawer
                                        path="authority/accountManage/Password" :title="$t('修改密码')" width="600px"
                                        @okCallback="loadFilter">
                                        <a class="btn-link">{{ $t('修改密码') }}</a>
                                    </DialogForm>
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
import { onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import type { pageAdminUserFilterParams, AdminUserAccountListVO } from "@/types/authority/accountManage.d";
import { deleteAdminUser, getPageAdminUser } from "@/api/authority/accountManage";
import { useRouter } from "vue-router";
import { extractContent } from "@/utils/util";
import { imageFormat } from "@/utils/format";
import { returnAvatar } from "@/utils/avatar";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { Tag } from "@/components/form";
import { isMerchant, isS2b2c, isStore } from "@/utils/version";
import { useListRequest } from "@/hooks/useListRequest";
const router = useRouter();
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
    onBatchAction
} = useListRequest<AdminUserAccountListVO, pageAdminUserFilterParams>({
    apiFunction: getPageAdminUser,
    idKey: "adminId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: ""
    }
});
onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>
.avatar-username {
    display: flex;
    align-items: center;
    gap: 8px;
}
</style>
