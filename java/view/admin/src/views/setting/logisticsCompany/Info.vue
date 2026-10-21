<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '请输入物流公司名称', trigger: 'blur' }]" :label="$t('物流公司名称')" prop="logisticsName">
                        <TigInput classType="tig-form-input" v-model="formState.logisticsName" />
                    </el-form-item>
                    <el-form-item v-if="isKdniao" :rules="[{ required: true, message: '请输入编号', trigger: 'blur' }]" :label="$t('编号')" prop="logisticsCode">
                        <TigInput classType="tig-form-input" v-model="formState.logisticsCode" />
                    </el-form-item>
                    <el-form-item v-else :rules="[{ required: true, message: '请输入编号', trigger: 'blur' }]" :label="$t('编号')" prop="kuaidi100Code">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.kuaidi100Code" />
                            <div class="extra">{{ $t("小写字母，如顺丰填 shunfeng") }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('物流公司描述')" prop="logisticsDesc">
                        <TigInput classType="tig-form-input" v-model="formState.logisticsDesc" :row="3" type="textarea" />
                    </el-form-item>
                    <el-form-item :label="$t('排序')" prop="sortOrder">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.sortOrder" />
                    </el-form-item>
                    <el-form-item :label="$t('是否启用')" prop="isEnabled">
                        <el-radio-group v-model="formState.isEnabled">
                            <el-radio :value="1">{{ $t("是") }}</el-radio>
                            <el-radio :value="0">{{ $t("否") }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t("提交") }} </el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { LogisticsCompanyFormState } from "@/types/setting/logisticsCompany";
import type { LogisticsProvider } from "@/types/setting/baseLogistics.d";
import { getLogisticsCompany, updateLogisticsCompany } from "@/api/setting/logisticsCompany";
import { getLogisticsProvider, LOGISTICS_PROVIDER_KDNIAO } from "@/api/setting/baseLogistics";

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
const formState = ref<LogisticsCompanyFormState>({
    sortOrder: 50,
    isEnabled: 1
});
// 按全局服务商切换编号字段：快递鸟 logisticsCode；快递100 kuaidi100Code
const provider = ref<LogisticsProvider>(LOGISTICS_PROVIDER_KDNIAO);
const isKdniao = computed(() => provider.value !== 2);
const fetchLogisticsCompany = async () => {
    try {
        const result = await getLogisticsCompany(action.value, { id: id.value });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(async () => {
    provider.value = await getLogisticsProvider();
    if (action.value === "detail") {
        fetchLogisticsCompany();
    } else {
        loading.value = false;
    }
});

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateLogisticsCompany(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t("操作成功"));
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
