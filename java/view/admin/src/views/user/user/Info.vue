/** * @file 客户信息表单 * @description 添加/编辑客户基本信息 * @features * - 设置客户名称、手机、邮箱 * - 设置/修改密码 * - 选择客户等级 * -
设置账户状态（启用/禁用） */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item
                        v-if="operation === 'create'"
                        :rules="[{ required: true, message: '客户名称不能为空!' }]"
                        :label="$t('客户名称')"
                        prop="username"
                    >
                        <TigInput classType="tig-form-input" v-model="formState.username" />
                    </el-form-item>
                    <el-form-item :label="$t('手机号码')" prop="mobile">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.mobile" />
                    </el-form-item>
                    <el-form-item :label="$t('邮箱地址')" prop="email">
                        <TigInput classType="tig-form-input" v-model="formState.email" />
                    </el-form-item>
                    <template v-if="operation === 'create'">
                        <el-form-item :rules="[{ required: true, message: '新密码不能为空!' }]" :label="$t('新密码')" prop="password">
                            <TigInput classType="tig-form-input" v-model="formState.password" />
                        </el-form-item>
                        <el-form-item :rules="[{ required: true, message: '确认密码不能为空!' }]" :label="$t('确认密码')" prop="pwdConfirm">
                            <TigInput classType="tig-form-input" v-model="formState.pwdConfirm" />
                        </el-form-item>
                    </template>
                    <template v-else>
                        <el-form-item :label="$t('新密码')" prop="password">
                            <TigInput classType="tig-form-input" v-model="formState.password" />
                        </el-form-item>
                        <el-form-item :label="$t('确认密码')" prop="pwdConfirm">
                            <TigInput classType="tig-form-input" v-model="formState.pwdConfirm" />
                        </el-form-item>
                    </template>
                    <el-form-item :label="$t('客户等级')" prop="rankId">
                        <SelectRankList v-model="formState.rankId"></SelectRankList>
                        <!-- <el-select v-model="formState.rankId" style="width: 100%">
                            <el-option
                                v-for="item in options"
                                :key="item.rankId"
                                :label="item.rankName"
                                :value="item.rankId"
                            />
                        </el-select> -->
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t("提交") }}</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { nextTick, onMounted, ref, shallowRef } from "vue";
import { SelectRankList } from "@/components/select";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { UserFormState } from "@/types/user/user.d";
import { getUser, updateUser } from "@/api/user/user";
// 父组件回调
import { useI18n } from "vue-i18n";
const { t } = useI18n();
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
const formState = ref<UserFormState>({
    balance: 0,
    frozenBalance: 0,
    avatar: "",
    password: "",
    pwdConfirm: ""
});

/**
 * @description 获取客户详情数据，用于编辑模式下回填表单
 * 获取失败时关闭弹窗并提示错误
 */
const fetchUser = async () => {
    try {
        loading.value = true;
        const result = await getUser(action.value, { id: id.value });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchUser();
    } else {
        loading.value = false;
    }
});

/**
 * @description 表单验证通过后提交客户数据
 * 根据operation决定是新增还是更新操作
 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateUser(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t("操作成功"));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

/**
 * @description 表单提交入口，供父组件调用
 */
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped></style>
