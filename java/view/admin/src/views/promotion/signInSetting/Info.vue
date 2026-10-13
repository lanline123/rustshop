<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="name" :label="$t('名称')" :rules="[{ required: true, message: '请填写签到名称', trigger: 'blur' }]">
                        <TigInput classType="tig-form-input" v-model="formState.name" />
                    </el-form-item>
                    <el-form-item prop="points" :label="$t('赠送积分')" :rules="[{ required: true, validator: validatePoints, trigger: 'blur' }]">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.points" />
                    </el-form-item>
                    <el-form-item prop="sortOrder" :label="$t('排序')" :rules="[{ validator: validateSortOrder, trigger: 'blur' }]">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.sortOrder" />
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
import { ref, shallowRef, onMounted } from "vue"
import { useRouter } from 'vue-router'
import { message } from "ant-design-vue";
import { FormAddGallery } from '@/components/gallery'
import { SignInSettingFormState } from '@/types/promotion/signInSetting.d';
import { getSignInSetting, updateSignInSetting } from "@/api/promotion/signInSetting";

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
        default: ""
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === 'add' ? 'create' : 'update';
const formRef = shallowRef();
const formState = ref<SignInSettingFormState>({
    name: '',
    points: undefined,
    sortOrder: undefined,
    dayNum: undefined
});

const validatePoints = (_rule: any, value: unknown, callback: (error?: Error) => void) => {
    if (value === '' || value === null || value === undefined) return callback(new Error(t('请填写赠送积分')));
    if (!/^\d+$/.test(String(value)) || Number(value) <= 0) return callback(new Error(t('赠送积分请输入正整数')));
    callback();
};

const validateSortOrder = (_rule: any, value: unknown, callback: (error?: Error) => void) => {
    if (value === '' || value === null || value === undefined) return callback();
    if (!/^\d+$/.test(String(value)) || Number(value) < 0) return callback(new Error(t('排序只能填写非负整数')));
    callback();
};

onMounted(() => {
    if (action.value === 'detail') {
        fetchSignInSetting();
    } else {
        loading.value = false;
    }
});
const fetchSignInSetting = async () => {
    try {
        const result = await getSignInSetting(action.value, { id: id.value });
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


// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit('update:confirmLoading', true);
        const result = await updateSignInSetting(operation, {
            id: id.value,
            name: formState.value.name,
            points: formState.value.points,
            sortOrder: formState.value.sortOrder
        });
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
