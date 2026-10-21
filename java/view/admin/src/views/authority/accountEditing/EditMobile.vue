/** 修改手机号表单 - 先验证当前绑定号，再填写新号验证码后换绑 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form ref="formRef" :model="formState" label-width="auto">
                    <template v-if="isVerifyStep">
                        <el-form-item :label="$t('当前手机号：')">
                            <span>{{ maskedBoundMobile }}</span>
                        </el-form-item>
                        <el-form-item :label="$t('获取验证码：')" prop="code" :rules="[{ required: true, message: $t('验证码不能为空!'), trigger: 'blur' }]">
                            <TigInput classType="tig-form-input" v-model="formState.code" :placeholder="$t('请输入验证码')">
                                <template #append>
                                    <div v-if="ableSend" class="code-btn" @click.stop="onCode">{{ btnText }}</div>
                                    <div v-if="!ableSend" class="code-text">{{ codeText }}</div>
                                </template>
                            </TigInput>
                        </el-form-item>
                    </template>
                    <template v-else>
                        <el-form-item :label="$t('新手机号：')" prop="mobile" :rules="[{ required: true, validator: validateMobile, trigger: 'blur' }]">
                            <TigInput classType="tig-form-input" v-model="formState.mobile" :placeholder="$t('请输入新手机号')" />
                        </el-form-item>
                        <el-form-item :label="$t('获取验证码：')" prop="code" :rules="[{ required: true, message: $t('验证码不能为空!'), trigger: 'blur' }]">
                            <TigInput classType="tig-form-input" v-model="formState.code" :placeholder="$t('请输入验证码')">
                                <template #append>
                                    <div v-if="ableSend" class="code-btn" @click.stop="onCode">{{ btnText }}</div>
                                    <div v-if="!ableSend" class="code-text">{{ codeText }}</div>
                                </template>
                            </TigInput>
                        </el-form-item>
                    </template>
                </el-form>
                <Verify ref="verify" mode="pop" captchaType="blockPuzzle" :imgSize="{ width: '310px', height: '155px' }" @okCallback="okCallback"></Verify>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, ref, shallowRef, watch } from "vue";
import { message } from "ant-design-vue";
import { AccountMobileEditingFormState } from "@/types/authority/accountEditing";
import { modifyManageAccounts, getAdminCode } from "@/api/authority/accountEditing";
import Verify from "@/components/verifition/Verify.vue";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "okText"]);
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    form: {
        type: Object,
        default: {}
    },
    isDialog: Boolean
});
const formRef = shallowRef();
/** 表单数据 */
const formState = ref<AccountMobileEditingFormState>({
    mobile: "",
    code: ""
});
const boundMobile = computed(() => String(props.form?.mobile || "").trim());
const hasBoundMobile = computed(() => !!boundMobile.value);
/** 1 验证当前绑定号，2 填写新号；未绑定则直接进入第 2 步 */
const step = ref(hasBoundMobile.value ? 1 : 2);
const isVerifyStep = computed(() => hasBoundMobile.value && step.value === 1);

watch(
    isVerifyStep,
    (val) => {
        emit("okText", val ? "下一步" : "确认");
    },
    { immediate: true }
);
/** 验证码按钮文字 */
const btnText = ref(t("获取验证码"));
/** 是否可以发送验证码 */
const ableSend = ref(true);
/** 倒计时秒数 */
const countdown = ref(60);
const verify = ref();
/** 滑块验证 token */
const verifyToken = ref(null);

/** 当前绑定号脱敏展示，已脱敏则原样显示 */
const maskedBoundMobile = computed(() => {
    const mobile = boundMobile.value;
    if (!mobile || mobile.includes("*")) return mobile;
    const digits = mobile.replace(/\D/g, "");
    if (digits.length < 7) return mobile;
    return mobile.replace(/(\d{3})\d+(\d{4})/, "$1****$2");
});

/** 滑块验证成功回调 */
const okCallback = (e: any) => {
    verifyToken.value = e.verifyToken;
    onCode();
};

/** 获取短信验证码：第一步发当前绑定号，第二步发新号 */
const onCode = async () => {
    if (!isVerifyStep.value) {
        await formRef.value.validateField("mobile");
    }
    if (!verifyToken.value) {
        verify.value.show();
        return;
    }
    try {
        const params: Record<string, unknown> = { type: "code", verifyToken: verifyToken.value };
        if (!isVerifyStep.value) {
            params.mobile = formState.value.mobile;
        }
        await getAdminCode(params);
        message.success(t("验证码发送成功"));
        ableSend.value = false;
        verifyToken.value = null;
        startCountdown();
    } catch (error: any) {
        message.error(error.message);
    }
};

const resetCodeState = () => {
    formState.value.code = "";
    ableSend.value = true;
    countdown.value = 60;
    verifyToken.value = null;
    if (timer.value) {
        clearInterval(timer.value);
        timer.value = undefined;
    }
};

/** 旧号验证通过后进入填写新号 */
const goToModifyStep = () => {
    step.value = 2;
    resetCodeState();
    formRef.value?.clearValidate?.();
};

/** 表单提交 - 第一步校验当前绑定号，第二步换绑 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        if (isVerifyStep.value) {
            await modifyManageAccounts({ modifyType: 4, code: formState.value.code });
            goToModifyStep();
            return;
        }
        const result = await modifyManageAccounts({ modifyType: 3, mobile: formState.value.mobile, code: formState.value.code });
        emit("submitCallback", result);
        message.success(t("修改成功"));
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

/** 手机号格式校验 */
const validateMobile = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t("手机号不能为空")));
        return;
    }
    const regex = /^(?:\+?86)?1[3-9]\d{9}$/;
    if (regex.test(value)) {
        callback();
    } else {
        callback(t("手机号格式错误"));
    }
};

/** 倒计时显示文字 */
const codeText = computed(() => {
    return countdown.value + t("秒后重发");
});
const timer = ref();

/** 启动倒计时 */
const startCountdown = () => {
    if (timer.value) {
        clearInterval(timer.value);
    }
    timer.value = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
            clearInterval(timer.value);
            timer.value = undefined;
            ableSend.value = true;
            countdown.value = 60;
        }
    }, 1000);
};

onBeforeUnmount(() => {
    if (timer.value) {
        clearInterval(timer.value);
    }
});

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.code-btn {
    cursor: pointer;
}
</style>
