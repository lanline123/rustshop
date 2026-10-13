/** 站内信发送表单 - 支持发送给全部会员，或按指定会员/会员标签/会员等级（三者互斥）圈选的部分会员 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '标题不能为空!' }]" :label="$t('标题')" prop="messageTitle">
                        <TigInput classType="tig-form-input" v-model="formState.messageTitle" />
                    </el-form-item>
                    <el-form-item :rules="[{ required: true, message: '内容不能为空!' }]" :label="$t('内容')" prop="messageContent">
                        <TigInput classType="tig-form-input" v-model="formState.messageContent" row="2" type="textarea" />
                    </el-form-item>
                    <el-form-item :label="$t('链接')" prop="messageLink">
                        <div class="link-field">
                            <SelectLink v-model="formState.messageLink" :excludeShopId="false"></SelectLink>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('发送会员')">
                        <SendUserSelector ref="sendUserRef" v-model="sendUserTarget" />
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
import { ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { MessageLogFormState } from "@/types/user/messageLog.d";
import type { SendUserTarget } from "@/types/user/memberAudience";
import { updateMessageLog } from "@/api/user/messageLog";
import { SelectLink } from "@/components/select";
import SendUserSelector from "@/components/SendUserSelector/index.vue";
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
        default: ""
    },
    isDialog: Boolean
});
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const sendUserRef = ref();
const formState = ref<MessageLogFormState>({});
const sendUserTarget = ref<SendUserTarget>({
    scope: 0,
    userIds: [],
    users: [],
    rankIds: [],
    tagIds: [],
    tagMatchMode: 0
});

/** 对齐后端 SendUserTypeEnum */
const SEND_USER_TYPE = { ALL: 0, SINGLE: 1, RANK: 2, PART: 3, TAG: 4 } as const;

/**
 * 把发送范围转换为接口字段
 * @description 选择器已保证指定会员/会员标签/会员等级三者互斥，故 target 中最多一组有值。
 *              sendUserTarget 非空时后端一律以它解析收件人，sendUserType 仅作来源标记，
 *              userIds / userRank 为兼容既有记录结构保留。
 */
const buildSendPayload = (): Record<string, unknown> => {
    const target = sendUserTarget.value;
    const base = { userIds: "", userRank: 0, sendUserTarget: target };
    if (target.scope === 0) {
        return { ...base, sendUserType: SEND_USER_TYPE.ALL };
    }
    if (target.userIds.length === 1) {
        return { ...base, sendUserType: SEND_USER_TYPE.SINGLE, userIds: String(target.userIds[0]) };
    }
    if (target.userIds.length > 1) {
        return { ...base, sendUserType: SEND_USER_TYPE.PART, userIds: target.users.map((user) => user.username ?? "").join(",") };
    }
    if (target.tagIds.length > 0) {
        return { ...base, sendUserType: SEND_USER_TYPE.TAG };
    }
    if (target.rankIds.length > 0) {
        return { ...base, sendUserType: SEND_USER_TYPE.RANK, userRank: target.rankIds[0] };
    }
    return { ...base, sendUserType: SEND_USER_TYPE.ALL };
};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    const targetResult = sendUserRef.value?.validate?.();
    if (targetResult && !targetResult.valid) {
        message.error(targetResult.message);
        return;
    }
    try {
        emit("update:confirmLoading", true);
        const result = await updateMessageLog(operation, { id: id.value, ...formState.value, ...buildSendPayload() });
        emit("submitCallback", result);
        message.success(t("操作成功"));
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

<style lang="less" scoped>
// 与标题、内容输入框（tig-form-input 450px）保持一致宽度
.link-field {
    width: 450px;
}

@media (max-width: 768px) {
    .link-field {
        width: 100%;
    }
}
</style>
