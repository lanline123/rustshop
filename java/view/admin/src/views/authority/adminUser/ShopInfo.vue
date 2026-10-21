/** 店铺员工编辑表单 - 添加/编辑店铺或门店的员工信息及权限 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '员工名称不能为空!' }]" :label="$t('员工名称')" prop="username">
                        <TigInput width="100%" v-model="formState.username" />
                    </el-form-item>
                    <!-- <el-form-item :rules="[{ required: true, message: '请选择绑定会员' }]" prop="userId" label="绑定会员">
                        <el-select
                            v-model="formState.userId"
                            filterable
                            remote
                            reserve-keyword
                            placeholder="可输入手机号查询绑定会员"
                            :remote-method="remoteMethod"
                            :loading="loading"
                            style="width: 240px"
                        >
                            <el-option :key="userInfo.userId" :label="userInfo.username" :value="userInfo.userId" />
                        </el-select>
                    </el-form-item> -->
                    <el-form-item :label="$t('手机号')" prop="mobile" :rules="[{ required: true, message: '手机号不能为空!' }]">
                        <TigInput width="100%" v-model="formState.mobile" />
                    </el-form-item>
                    <el-form-item :label="$t('邮箱')" prop="email">
                        <TigInput width="100%" v-model="formState.email" />
                    </el-form-item>
                    <el-form-item :label="$t('头像')" prop="avatar">
                        <UploadAvatar v-model:avatar="formState.avatar" :avatarType="avatarType" :loading="loading"></UploadAvatar>
                    </el-form-item>
                    <el-form-item :label="$t('是否停用')" prop="isUsing">
                        <el-radio-group style="width: 100%" v-model="formState.isUsing">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <!-- <el-form-item label="设置密码" prop="password">
                        <TigInput width="100%" v-model="formState.password" type="password" />
                    </el-form-item>
                    <el-form-item label="确认密码" prop="pwdConfirm">
                        <TigInput width="100%" v-model="formState.pwdConfirm" type="password" />
                    </el-form-item> -->
                    <el-form-item :label="$t('员工角色')" prop="roleId" :rules="[{ validator: validateRoleId, trigger: 'change' }]">
                        <el-select v-model="formState.roleId" :placeholder="$t('请选择角色')" style="width: 100%">
                            <el-option v-for="item in roleList" :label="item.roleName" :value="item.roleId"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item v-if="isLegacyCustomAuth" label="">
                        <fieldset disabled style="border: none; padding: 0; margin: 0; opacity: 0.7;">
                            <AuthoritySelect v-model="formState.authList"></AuthoritySelect>
                        </fieldset>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef, watch, reactive, computed } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { DefaultAvatar, FormAddGallery, UploadAvatar } from "@/components/gallery";
import { AdminUserFormState, AdminUserRoleListItem } from "@/types/authority/adminUser";
import { getShopUser, updateShopUser, getAdminUserConfig, getAdminUserSearch } from "@/api/authority/adminUser";
import { buildShopUserCreatePayload, buildShopUserUpdatePayload, isValidShopUserRoleId } from "./shopUserPayload";

import AuthoritySelect from "@/views/authority/AuthoritySelect.vue";
import { extractContent } from "@/utils/util";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const adminType = ref(localStorage.getItem("adminType"));
const validateRoleId = (_rule: unknown, value: number | null | undefined, callback: (error?: Error) => void) => {
    isValidShopUserRoleId(value) ? callback() : callback(new Error(t("请选择员工角色")));
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

const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
/** 表单数据 */
const formState = reactive<AdminUserFormState>({
    userId: ""
});
const roleList = ref<AdminUserRoleListItem[]>();
const hasLegacyAuth = ref(false);
const isLegacyCustomAuth = computed(() => hasLegacyAuth.value && !formState.roleId);
const userInfo = ref<any>({});

/** 远程搜索用户 */
const remoteMethod = async (query: string) => {
    if (query) {
        const result = await getAdminUserSearch(query);
        if (result !== null) {
            userInfo.value = result;
        }
    } else {
        userInfo.value = {};
    }
};
const avatarType = ref(true);

/** 获取员工详情 */
const fetchAdminUser = async () => {
    try {
        const result = await getShopUser(action.value, { id: id.value });
        userInfo.value = result.user;
        Object.assign(formState, result);
        if(result.authList == null){
            formState.authList = [];
        }
        hasLegacyAuth.value = !isValidShopUserRoleId(formState.roleId) && Array.isArray(formState.authList) && formState.authList.length > 0;
        if (!isValidShopUserRoleId(formState.roleId)) {
            formState.roleId = undefined as any;
        }
        formState.mobile = result.adminUser?.mobile;
        let temp = extractContent(String(formState.avatar));
        if (temp == "one") {
            avatarType.value = true;
        } else if (temp == "def") {
            avatarType.value = false;
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

/** 获取权限组配置 */
const fetchAdminUserConfit = async () => {
    try {
        const result = await getAdminUserConfig();
        roleList.value = result;
    } catch (error: any) {
        message.error(error.message);
    }
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchAdminUser();
    } else {
        loading.value = false;
    }
    fetchAdminUserConfit();
});

/** 表单提交 - 创建或更新员工 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const avatar = formState.avatar || formState.defAvatar || "";
        const payloadSource = { ...formState, avatar };
        const payload = operation === "create"
            ? buildShopUserCreatePayload(payloadSource)
            : buildShopUserUpdatePayload(id.value, payloadSource);
        const result = await updateShopUser(operation, payload);
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

/** 对外暴露的表单提交方法 */
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.width100 {
    width: 100%;
}
</style>
