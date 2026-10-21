/**
 * @file 角色编辑页面
 * @description 添加/编辑管理员角色
 * @features
 * - 角色名称和描述
 * - 权限树选择（多选）
 * - 关联管理员自动继承权限
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '角色名称不能为空!' }]" :label="$t('角色名称')" prop="roleName">
                        <TigInput width="100%" v-model="formState.roleName"/>
                    </el-form-item>
                    <el-form-item :label="$t('角色描述')" prop="roleDesc">
                        <TigInput width="100%" v-model="formState.roleDesc" :row="3" type="textarea"/>
                    </el-form-item>
                    <el-form-item label="" prop="authorityList">
                      <AuthoritySelect v-model="formState.authorityList" :admin-type="formState.adminType"></AuthoritySelect>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width:100%;margin-top:100px"/>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import {onMounted, ref, shallowRef} from "vue"
import {useRouter} from 'vue-router'
import {message} from "ant-design-vue";
import type {AdminRoleFormState} from '@/types/authority/adminRole.d';
import {getAdminRole, updateAdminRole} from "@/api/authority/adminRole";

import AuthoritySelect from "@/views/authority/AuthoritySelect.vue";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ''
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === 'add' ? 'create' : 'update';
const formRef = shallowRef();
const formState = ref<AdminRoleFormState>({});
const fetchAdminRole  = async () => {
    try {
        const result = await getAdminRole(action.value, { id: id.value });
        Object.assign(
          formState.value,
          result
        )
    } catch (error: any) {
        message.error(error.message);
        emit('close');
    } finally {
        loading.value = false;
    }
};


onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchAdminRole();
    } else {
        loading.value = false;
    }
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit('update:confirmLoading', true);
        const result = await updateAdminRole(operation, { id: id.value, ...formState.value });
        emit('submitCallback', result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit('update:confirmLoading', false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit()
};

defineExpose({ onFormSubmit });
</script>
