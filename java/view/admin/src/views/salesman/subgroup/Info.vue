<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="groupName" :label="$t('分组名称：')" :rules="[{ required: true, message: '分组名称不能为空!' }]">
                        <TigInput classType="tig-form-input" v-model="formState.groupName" clearable />
                    </el-form-item>
                    <el-form-item prop="describe" :label="$t('分组描述：')">
                        <TigInput classType="tig-form-input"
                            v-model="formState.describe"
                            :rows="5"
                            type="textarea"
                            :placeholder="$t('请输入分组描述')"
                        />
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type {groupFormState} from "@/types/salesman/subgroup.d";
import {getgroup, updategroup} from "@/api/salesman/subgroup";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0,
    },
    act: {
        type: String,
        default: "add",
    },
    isDialog: {
        type: Boolean,
        default: false,
    }
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === 'add' ? 'create' : 'update';
const formRef = shallowRef();
const formState = ref<groupFormState>({});

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchGroup();
    } else {
        loading.value = false;
    }
});

const fetchGroup = async () => {
    try {
        const result = await getgroup(action.value, { id: id.value });
        Object.assign(formState.value, result);
    } catch (error:any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updategroup(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error:any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
