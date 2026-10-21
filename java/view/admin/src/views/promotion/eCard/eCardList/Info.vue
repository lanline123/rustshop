<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="cardNumber" :label="$t('卡券名称')" :rules="[{ required: true, message: '电子卡券名称不能为空!' }]">
                        <TigInput classType="tig-form-input" v-model="formState.cardNumber" />
                    </el-form-item>
                    <el-form-item prop="cardPwd" :label="$t('卡券密码')" :rules="[{ required: true, message: '电子卡券密码不能为空!' }]">
                        <TigInput classType="tig-form-input" v-model="formState.cardPwd" />
                    </el-form-item>
                    <el-form-item :label="$t('是否使用')" prop="isUse">
                        <el-radio-group style="width: 100%" v-model="formState.isUse">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
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
import { ECardFormState } from "@/types/promotion/eCard.d";
import { getECard, updateECard } from "@/api/promotion/eCard";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    groupId: {
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
const formState = ref<ECardFormState>({
    groupId: props.groupId,
    isUse: 0
});

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchECard();
    } else {
        loading.value = false;
    }
});
const fetchECard = async () => {
    try {
        const result = await getECard(action.value, { id: id.value });
        Object.assign(formState.value, result);
    } catch (error: any) {
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
        const result = await updateECard(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
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
