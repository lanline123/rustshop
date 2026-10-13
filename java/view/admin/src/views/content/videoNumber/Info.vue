<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <!-- <div class="page-tip">请填写与小程序同主体的视频号ID，提交后系统将按接口实时同步账号状态。</div> -->

                <el-form v-if="!loading" ref="formRef" :model="formState" :rules="rules" label-width="110px">
                    <el-form-item :label="$t('视频号ID')" prop="finderId">
                        <TigInput v-model="formState.finderId" width="360px" :placeholder="$t('请输入视频号ID')" />
                        <!-- <div class="form-extra">请输入视频号唯一标识，仅校验是否为空和是否重复。</div> -->
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 80px" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { FormRules } from "element-plus";
import { getWechatVideoAccountDetail, updateWechatVideoAccount } from "@/api/content/wechatVideoAccount";
import type { WechatVideoAccountFormState } from "@/types/content/wechatVideoAccount.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface RuleForm extends WechatVideoAccountFormState {}

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
    record: {
        type: Object,
        default: () => null
    },
    existingFinderIds: {
        type: Array as () => string[],
        default: () => []
    },
    isDialog: {
        type: Boolean,
        default: false
    }
});

const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act || "add"));
const id = ref<number>(props.isDialog ? props.id : Number(query.id || 0));
const operation = action.value === "add" ? "create" : "update";
const loading = ref<boolean>(true);
const formRef = shallowRef();
const formState = ref<WechatVideoAccountFormState>({
    id: id.value,
    finderId: "",
    nickName: ""
});

const rules = reactive<FormRules<RuleForm>>({
    finderId: [{ required: true, message: t('请输入视频号ID'), trigger: "blur" }]
});

const loadDetail = async () => {
    try {
        const result = await getWechatVideoAccountDetail({ id: id.value });
        formState.value = {
            id: result.id,
            finderId: result.finderId,
            nickName: result.nickName
        };
    } catch (error: any) {
        message.error(error.message || t('获取详情失败'));
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (props.record) {
        formState.value = {
            id: Number((props.record as Record<string, unknown>).id || props.id || 0),
            finderId: String((props.record as Record<string, unknown>).finderId || ""),
            nickName: String((props.record as Record<string, unknown>).nickName || "")
        };
        loading.value = false;
        return;
    }

    if (action.value === "add") {
        loading.value = false;
    } else {
        loadDetail();
    }
});

const onSubmit = async () => {
    await formRef.value?.validate();

    const finderId = formState.value.finderId.trim();
    const isDuplicate = props.existingFinderIds.includes(finderId);
    if (isDuplicate) {
        message.error(t('该视频号ID已存在，请勿重复关联'));
        return;
    }

    try {
        emit("update:confirmLoading", true);
        const payload =
            action.value === "add"
                ? {
                      finderId,
                      nickName: formState.value.nickName || ""
                  }
                : {
                      id: formState.value.id,
                      finderId,
                      nickName: formState.value.nickName || ""
                  };
        await updateWechatVideoAccount(operation, payload as WechatVideoAccountFormState);
        emit("submitCallback", {
            id: formState.value.id,
            finderId,
            nickName: formState.value.nickName || ""
        });
        message.success(action.value === "add" ? t('关联成功') : t('保存成功'));
    } catch (error: any) {
        message.error(error.message || t('提交失败'));
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
.page-tip {
    margin-bottom: 20px;
    padding: 12px 16px;
    color: #606266;
    line-height: 22px;
    background: #f7f8fa;
    border-radius: 6px;
}

.form-extra {
    margin-top: 6px;
    color: #909399;
    font-size: 12px;
    line-height: 20px;
}
</style>
