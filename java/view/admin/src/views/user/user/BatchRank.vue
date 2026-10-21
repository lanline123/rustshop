/** 批量修改客户等级 - 为多个客户批量设置客户等级 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('客户等级')" prop="rankId" :rules="[{ required: true, message: '请选择客户等级!' }]">
                        <SelectRankList v-model:modelValue="formState.rankId"></SelectRankList>
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
import { onMounted, ref, shallowRef } from "vue";
import { SelectRankList } from "@/components/select";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { batchSubmit } from "@/api/user/user";

import { useI18n } from "vue-i18n";
const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    ids: {
        type: Array,
        default: []
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
const ids = ref<any[]>(props.ids);
const formRef = shallowRef();
const formState = ref<any>({
    rankId: ""
});

onMounted(() => {
    loading.value = false;
});

/** 表单提交 - 批量更新客户等级 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await batchSubmit(action.value, { ids: ids.value, rankId: formState.value.rankId });
        emit("submitCallback", result);
        message.success(t("操作成功"));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

/** 对外暴露的表单提交方法 */
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped></style>
