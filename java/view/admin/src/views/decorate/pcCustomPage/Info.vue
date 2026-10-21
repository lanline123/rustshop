<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="decorateTitle" :label="$t('页面名称')" :rules="[{ required: true, message: '页面名称不能为空!' }]">
                        <TigInput classType="tig-form-input" v-model="formState.decorateTitle" />
                    </el-form-item>
                    <el-form-item prop="sortOrder" :label="$t('排序')">
                        <TigInput classType="tig-form-input" v-model="formState.sortOrder" />
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
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { updateDecorate, getDecorate } from "@/api/decorate/mobileDecorate";
import { useDecorateStore } from "@/store/decorate";
import type { PcCustomPageFormState } from "@/types/decorate/pcCustomPage";
import { updatePcCustomDecorate } from "@/api/decorate/pcCustomPage";

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
    decorateType: {
        type: Number,
        default: 1
    },
    isDialog: Boolean
});
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<PcCustomPageFormState>({
    decorateId: id.value
});
const loading = ref(true);
const { decorateInfo } = useDecorateStore();
const isDefault = ref(decorateInfo.isDefault);
const languageId = ref<any>(Number(decorateInfo.localeId) || 0);
const parentId = ref<number>(Number(decorateInfo.decorateId) || 0);

console.log(props);

const getDetail = async () => {
    try {
        const result = await getDecorate("detail", {
            id: id.value,
            decorateType: props.decorateType,
            localeId: !isDefault.value ? languageId.value : 0,
            parentId: !isDefault.value ? parentId.value : 0
        });
        formState.value.decorateTitle = result.decorateTitle;
        formState.value.sortOrder = result.sortOrder;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result =
            operation == "create"
                ? await updateDecorate(operation, { id: id.value, decorateType: props.decorateType, ...formState.value })
                : await updatePcCustomDecorate(formState.value);
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        getDetail();
    } else {
        loading.value = false;
    }
});
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
