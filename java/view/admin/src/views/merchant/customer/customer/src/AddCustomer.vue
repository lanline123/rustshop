<!--
 * @file 店铺端 添加会员抽屉（D1116-shopMember，2026-07-10 CR：对齐平台「添加会员」抽屉，去会员等级）
 * @description 表单字段：会员名称/手机号码/邮箱地址，不含会员等级与密码。
 *   提交仍走店铺作用域接口 user/shopMember/add（shopId 由登录态解析）。受平台「店铺添加会员」开关门控（后端双守卫）。
 *   由 DialogForm 以抽屉打开，确认按钮触发 onFormSubmit。
-->
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '会员名称不能为空!' }]" label="会员名称" prop="username">
                        <TigInput classType="tig-form-input" v-model="formState.username" />
                    </el-form-item>
                    <el-form-item :rules="[{ required: true, message: '手机号码不能为空!' }]" label="手机号码" prop="mobile">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.mobile" />
                    </el-form-item>
                    <el-form-item label="邮箱地址" prop="email">
                        <TigInput classType="tig-form-input" v-model="formState.email" />
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { addShopCustomer } from "@/api/merchant/shopMember";
import type { ShopCustomerAddParams } from "@/types/merchant/shopMember.d";

defineProps({ isDialog: Boolean });
const emit = defineEmits(["submitCallback", "update:confirmLoading"]);

const formRef = ref();
const formState = reactive<ShopCustomerAddParams>({
    username: "",
    mobile: "",
    email: ""
});

const onFormSubmit = async () => {
    try {
        await formRef.value.validate();
    } catch {
        return;
    }
    emit("update:confirmLoading", true);
    try {
        const data: ShopCustomerAddParams = {
            username: (formState.username || "").trim(),
            mobile: (formState.mobile || "").trim(),
            email: (formState.email || "").trim()
        };
        const result = await addShopCustomer(data);
        message.success("操作成功");
        emit("submitCallback", result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

defineExpose({ onFormSubmit });
</script>
