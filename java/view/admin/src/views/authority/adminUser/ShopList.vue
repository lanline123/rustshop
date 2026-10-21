/** 店铺员工列表 - 管理店铺/门店的员工账号，支持添加、编辑、删除 */
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
                                            :placeholder="$t('输入员工名称')"
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
                                        :params="{ act: 'add', type: action ? action : '', suppliersId: id ? id : 0 }"
                                        isDrawer
                                        path="authority/adminUser/ShopInfo"
                                        :title="$t('添加员工')"
                                        width="800px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('添加员工') }}</el-button>
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
                            row-key="id"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" :selectable="canDeleteEmployee" />
                            <el-table-column width="200" :label="$t('员工名称')" prop="username">
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
                                            <div>{{ row.username || "-" }}</div>
                                            <div v-if="row.isAdmin == 1">
                                                <Tag :transparent="true" :text="getShopType() === 1 ? $t('店铺管理员') : $t('门店管理员')"> </Tag>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('权限组')" prop="roleName">
                                <template #default="{ row }">
                                    <span v-if="row.role" :class="row.role.roleName ? 'green' : 'gray'">{{ row.role.roleName || "-" }}</span>
                                    <span v-else class="gray">{{ $t('自定义权限') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('邮箱')" prop="email">
                                <template #default="{ row }">
                                    <span>{{ row.email || "-" }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('手机号')" prop="mobile">
                                <template #default="{ row }">
                                    <span>
                                        <MobileCard :mobile="row.adminUser?.mobile"></MobileCard>
                                    </span>
                                </template>
                            </el-table-column>
                            <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.id }"
                                        isDrawer
                                        path="authority/adminUser/ShopInfo"
                                        :title="$t('编辑员工')"
                                        width="800px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" v-if="canDeleteEmployee(row)" />
                                    <DeleteRecord v-if="canDeleteEmployee(row)" :params="{ id: row.id }" :requestApi="delShopUser" @afterDelete="loadFilter"
                                        >{{ $t('删除') }} </DeleteRecord>
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
import { onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import type { AdminUserFilterParams, AdminUserFilterState } from "@/types/authority/adminUser";
import { batchShopSubmit, delShopUser, getShopUserList } from "@/api/authority/adminUser";
import { useRouter } from "vue-router";
import { extractContent } from "@/utils/util";
import { imageFormat } from "@/utils/format";
import { returnAvatar } from "@/utils/avatar";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { Tag } from "@/components/form";
import { useListRequest } from "@/hooks/useListRequest";
import { getShopType } from "@/utils/storage";
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
} = useListRequest<AdminUserFilterState, AdminUserFilterParams>({
    apiFunction: getShopUserList,
    idKey: "id",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: ""
    }
});

/** 主账号不可删除，避免组织侧丢失关联 */
const canDeleteEmployee = (row: AdminUserFilterState) => Number(row.isAdmin) !== 1 && row.adminId !== 1;

/** 批量操作 - 删除选中员工 */
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchShopSubmit);
};

/** 跳转到操作日志页面 */
const toPage = (id: string | number) => {
    router.push("/authority/admin_log/list?userId=" + id);
};
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
onMounted(() => {
    if (action.value) {
        filterParams.suppliersId = id.value;
    }
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
