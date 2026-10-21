/** 账号详情页面 - 展示主账号信息及关联的店铺/门店/供应商列表 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('主账号名称')">
                        <p class="mr10">{{ formState.username }}</p>
                        <DialogForm
                            :params="{ act: 'detail', id: id, type: type }"
                            isDrawer
                            path="authority/accountManage/ChangeAccount"
                            :title="$t('更换主账号')"
                            width="600px"
                            @okCallback="_getMainAccount"
                        >
                            <a class="btn-link">{{ $t('更换主账号') }}</a>
                        </DialogForm>
                        <el-divider direction="vertical" />
                        <DialogForm
                            :params="{ act: 'detail', id: id, type: type }"
                            isDrawer
                            path="authority/accountManage/AccountInfo"
                            :title="$t('修改账号信息')"
                            width="600px"
                            @okCallback="_getMainAccount"
                        >
                            <a class="btn-link">{{ $t('修改账号信息') }}</a>
                        </DialogForm>
                        <el-divider direction="vertical" />
                        <DialogForm
                            :params="{ act: 'detail', id: id, type: type }"
                            isDrawer
                            path="authority/accountManage/Password"
                            :title="$t('修改密码')"
                            width="600px"
                        >
                            <a class="btn-link">{{ $t('修改密码') }}</a>
                        </DialogForm>
                    </el-form-item>
                    <el-form-item :label="$t('手机号')"> <MobileCard :mobile="formState.mobile"></MobileCard> </el-form-item>
                    <el-form-item :label="name + $t('列表')">
                        <el-table :data="tableData" style="width: 100%">
                            <el-table-column prop="logo" :label="name + 'LOGO'" width="150">
                                <template #default="{ row }">
                                    <Image v-if="row.logo" :src="row.logo" fit="contain" style="height: 25px; width: 60px" />
                                    <p v-else>{{ $t('暂无图片') }}</p>
                                </template>
                            </el-table-column>
                            <el-table-column prop="name" :label="$t('店铺名称')" v-if="type == 'shop'" />
                            <el-table-column prop="name" :label="$t('门店名称')" v-if="type == 'store'" />
                            <el-table-column prop="name" :label="$t('供应商名称')" v-if="type == 'vendor'" />
                            <el-table-column prop="address" :label="$t('状态')" v-if="type !== 'vendor'">
                                <template #default="{ row }">
                                    <template v-if="row.status == 10">
                                        <StatusDot color="red" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 4">
                                        <StatusDot color="red" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 1">
                                        <StatusDot color="green" :flicker="true"></StatusDot>
                                    </template>
                                    <span v-if="row.status === 10" style="color: red">{{ $t('关店') }}</span>
                                    <span v-if="row.status === 4" style="color: red">{{ $t('暂停营业') }}</span>
                                    <span v-if="row.status === 1" style="color: green">{{ $t('开业') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column prop="address" :label="$t('状态')" v-if="type == 'vendor'">
                                <template #default="{ row }">
                                    <template v-if="row.status == 2">
                                        <StatusDot color="red" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 1">
                                        <StatusDot color="green" :flicker="true"></StatusDot>
                                    </template>
                                    <span v-if="row.status === 2" style="color: red">{{ $t('暂停供应') }}</span>
                                    <span v-if="row.status === 1" style="color: green">{{ $t('正常供应') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column prop="adminUsername" :label="$t('主账号名称')" />
                            <el-table-column prop="addTime" :label="$t('创建时间')">
                                <template #default="{ row }">
                                    {{ row.addTime || '--' }}
                                </template>
                            </el-table-column>
                        </el-table>
                        <div v-if="total > 0" class="pagination-con">
                            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                        </div>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import MobileCard from "@/components/list/src/MobileCard.vue";
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { onMounted, ref, shallowRef, computed } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { accountManageParams, accountManageListParams } from "@/types/authority/accountManage.d";
import { getMainAccount, getPageShopOrVendor } from "@/api/authority/accountManage";
import { Image } from "@/components/image";
import StatusDot from "@/components/form/src/StatusDot.vue";
/** 关联列表数据 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tableData = ref<accountManageListParams[]>([]);

const emit = defineEmits(["callback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    type: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const formRef = shallowRef();
/** 表单数据 */
const formState = ref<accountManageParams>({
    adminId: 0
});

/** 根据类型获取名称 */
const name = computed(() => {
    let name = "";
    if (props.type == "shop") {
        name = t('店铺');
    } else if (props.type == "store") {
        name = t('门店');
    } else if (props.type == "pickup") {
        name = t('自提点');
    } else if (props.type == "vendor") {
        name = t('供应商');
    }
    return name;
});

/** 获取主账号信息 */
const _getMainAccount = async () => {
    try {
        const result = await getMainAccount({ id: id.value, type: props.type });
        formState.value = result;
        _getPageShopOrVendor(result.adminId);
        emit("callback", result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
/** 分页参数 */
const filterParams = ref<any>({
    page: 1,
    size: 10
});
const total = ref<number>(0);

/** 获取关联的店铺/门店/供应商列表 */
const _getPageShopOrVendor = async (id: number) => {
    try {
        const result = await getPageShopOrVendor({ adminId: id, type: props.type, ...filterParams.value });
        tableData.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

_getMainAccount();

/** 刷新列表 */
const loadFilter = () => {
    _getPageShopOrVendor(formState.value.adminId);
};
</script>
<style lang="less" scoped>
.pagination-con {
    width: 100%;
    display: flex;
    justify-content: flex-end;
}
</style>
