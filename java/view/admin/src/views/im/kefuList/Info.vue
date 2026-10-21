/**
 * @file 客服编辑页面
 * @description 添加/编辑客服人员
 * @features
 * - 客服基本信息（客服名称、手机号、邮箱、头像）
 * - 启用/停用状态
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '客服名称不能为空!' }]" label="客服名称" prop="username">
                        <TigInput width="100%" v-model="formState.username" placeholder="请输入客服名称" />
                    </el-form-item>
                    <el-form-item
                        label="手机号"
                        prop="mobile"
                        :rules="[
                            { required: true, message: '手机号不能为空!' },
                            { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号!' }
                        ]"
                    >
                        <TigInput width="100%" v-model="formState.mobile" placeholder="请输入手机号" />
                    </el-form-item>
                    <el-form-item label="邮箱" prop="email">
                        <TigInput width="100%" v-model="formState.email" placeholder="请输入邮箱" />
                    </el-form-item>
                    <el-form-item label="客服头像" prop="avatar">
                        <UploadAvatar v-model:avatar="formState.avatar" :loading="loading" />
                    </el-form-item>
                    <el-form-item label="状态" prop="isUsing">
                        <el-radio-group v-model="formState.isUsing">
                            <el-radio :value="0">启用</el-radio>
                            <el-radio :value="1">停用</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">提交</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { UploadAvatar } from "@/components/gallery";
import type { AdminUserKefuFormState } from "@/types/im/kefu";
import { getAdminUserKefu, updateAdminUserKefu } from "@/api/im/adminUserKefu";

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
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
const formState = reactive<AdminUserKefuFormState>({
    username: "",
    mobile: "",
    email: "",
    avatar: "",
    isUsing: 0
});

const fetchDetail = async () => {
    try {
        const result = await getAdminUserKefu("detail", { id: id.value });
        Object.assign(formState, result);
        formState.mobile = result.adminUser?.mobile || result.mobile;
        formState.username = result.username || result.adminUser?.username;
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (operation === "update") {
        fetchDetail();
    } else {
        loading.value = false;
    }
});

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const submitData: AdminUserKefuFormState = {
            username: formState.username,
            mobile: formState.mobile,
            email: formState.email,
            avatar: formState.avatar,
            isUsing: formState.isUsing
        };
        if (operation === "update") {
            submitData.id = id.value;
        }
        const result = await updateAdminUserKefu(operation, submitData);
        emit("submitCallback", result);
        message.success("操作成功");
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

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
