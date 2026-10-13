/**
 * @file 账号信息编辑页面
 * @description 当前登录管理员的个人信息管理
 * @features
 * - 修改密码
 * - 修改/绑定手机号
 * - 修改头像（上传或选择默认头像）
 * - 修改邮箱
 * - API Token 管理
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <el-tabs v-model="activeTab" class="lyecs-tabs" tab-position="top">
                <el-tab-pane :label="$t('个人信息')" name="info">
                    <a-spin :spinning="loading">
                        <el-form ref="formRef" :model="formState" label-width="100px" style="max-width: 500px">
                            <el-form-item :label="$t('管理员名称')" prop="username">
                                <p>{{ formState.username }}</p>
                                <DialogForm
                                    v-if="formState.mobile"
                                    :title="$t('修改密码')"
                                    isDrawer
                                    :params="{ id: userInfo.adminId, form: userInfo }"
                                    path="authority/accountEditing/Info"
                                    width="550px"
                                    @okCallback="loadFilter"
                                >
                                    <el-button key="primary" type="primary" link>{{ $t("修改密码") }}</el-button>
                                </DialogForm>
                            </el-form-item>
                            <el-form-item :label="$t('手机号')" prop="mobile">
                                <MobileCard v-if="formState.mobile" :mobile="formState.mobile"></MobileCard>
                                <DialogForm
                                    :title="formState.mobile ? $t('修改手机号') : $t('绑定手机号')"
                                    isDrawer
                                    :params="{ id: userInfo.adminId, form: userInfo }"
                                    path="authority/accountEditing/EditMobile"
                                    width="550px"
                                    @okCallback="loadFilter"
                                >
                                    <el-button key="primary" type="primary" link>{{ formState.mobile ? $t("修改手机号") : $t("绑定手机号") }}</el-button>
                                </DialogForm>
                            </el-form-item>
                            <el-form-item :label="$t('用户头像')" prop="avatar">
                                <UploadAvatar v-model:avatar="formState.avatar" :avatarType="avatarType" :loading="loading"></UploadAvatar>
                            </el-form-item>
                            <el-form-item :label="$t('邮箱')" prop="email" v-if="getAdminType() != 'shop'">
                                <TigInput classType="tig-form-input" v-model="formState.email" />
                            </el-form-item>
                            <el-form-item>
                                <el-button :loading="confirmLoading" size="large" type="primary" @click="onSubmit">{{ $t("提 交") }}</el-button>
                            </el-form-item>
                        </el-form>
                    </a-spin>
                </el-tab-pane>
                <el-tab-pane label="API Token" name="apiToken">
                    <ApiToken />
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { reactive, ref } from "vue";
import { DefaultAvatar, FormAddGallery, UploadAvatar } from "@/components/gallery";
import { DialogForm } from "@/components/dialog";
import { message } from "ant-design-vue";
import { AccountEditingFormState } from "@/types/authority/accountEditing.d";
import { useUserStore } from "@/store/user";
import { getAdminInfo, adminInfoSubmit } from "@/api/authority/accountEditing";
import { extractContent } from "@/utils/util";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { getAdminType } from "@/utils/storage";
import ApiToken from "./ApiToken.vue";

import { useI18n } from "vue-i18n";
const { t } = useI18n();
const activeTab = ref("info");
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const loading = ref<boolean>(false);
const userStore = useUserStore();
const userInfo = reactive<any>(JSON.parse(localStorage.getItem("user") || "{}").userInfo);
const formState = ref<AccountEditingFormState>({
    avatar: "",
    defAvatar: "",
    username: "",
    email: "",
    id: "",
    mobile: ""
});
const avatarType = ref(true);
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getAdminInfo({
            id: userInfo.adminId
        });
        formState.value.mobile = String(getAdminType() != "shop" ? result.mobile : result.adminUser?.mobile || "");
        formState.value.username = String(getAdminType() != "shop" ? result.username : result.adminUser?.username || "");
        formState.value.email = String(result.email);
        formState.value.id = String(result.id);

        // shop 端提交走 merchant/adminUserShop/modifyUser，只更新 admin_user_shop.avatar，
        // 因此回显也必须读 result.avatar（admin_user_shop 表），不能读 result.adminUser.avatar（admin_user 表），
        // 否则提交后会变回旧头像。
        const resolvedUser = getAdminType() != "shop" ? result : result.adminUser || result;
        const resolvedAvatar = (result as any)?.avatar ?? (resolvedUser as any)?.avatar ?? "";
        formState.value.avatar = String(resolvedAvatar || "");

        let temp = extractContent(String(formState.value.avatar));
        if (temp == "one") {
            avatarType.value = true;
        } else if (temp == "def") {
            avatarType.value = false;
        }
        userStore.setUserInfo(resolvedUser);
        // 同步更新 localStorage.user（很多页面直接从这里取用户信息）
        localStorage.setItem("user", JSON.stringify({ userInfo: resolvedUser }));
        Object.assign(userInfo, resolvedUser);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        const result = await adminInfoSubmit({ ...formState.value });
        message.success(t("修改成功"));
        await loadFilter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};

loadFilter();
</script>

<style lang="less" scoped>
p {
    line-height: 16px;
    margin-right: 20px;
}

.el-button {
    margin-right: 15px;
}

.avatar-type {
    display: flex;
    flex-direction: row;
    gap: 10px;
    width: 100%;
    align-items: center;
}
.tips {
    color: #1456f0;
    margin-right: 2px;
    cursor: pointer;
}
</style>
