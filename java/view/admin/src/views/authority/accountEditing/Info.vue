/** 修改密码表单 - 通过短信验证码验证后修改登录密码 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('手机号：')">
                        {{ formState.mobile }}
                    </el-form-item>
                    <el-form-item :label="$t('获取验证码：')" prop="code" :rules="[{ required: true, message: '验证码不能为空!', trigger: 'blur' }]">
                        <TigInput classType="tig-form-input" v-model="formState.code">
                            <template #append>
                                <div v-if="ableSend" class="code-btn" @click.stop="onCode">{{ btnText }}</div>
                                <div v-if="!ableSend" class="code-text">{{ codeText }}</div>
                            </template>
                        </TigInput>
                    </el-form-item>
                    <!-- <el-form-item label="旧密码：" prop="oldPassword" :rules="[{ required: true, message: '旧密码不能为空!', trigger: 'blur' }]">
                        <TigInput classType="tig-form-input" type="password" v-model="formState.oldPassword" />
                    </el-form-item> -->
                    <el-form-item :label="$t('新密码：')" prop="password" :rules="[{ required: true, validator: validatePassword, trigger: 'blur' }]">
                        <TigInput classType="tig-form-input" type="password" v-model="formState.password" />
                        <div class="extra">{{ $t('密码长度至少为8位，并且不能是纯数字或纯字母。') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('确认密码：')" prop="pwdConfirm" :rules="[{ required: true, validator: validatePwd, trigger: 'blur' }]">
                        <TigInput classType="tig-form-input" type="password" v-model="formState.pwdConfirm" />
                        <div class="extra">{{ $t('请再次输入密码，确保与上次输入一致。') }}</div>
                    </el-form-item>
                </el-form>
                <Verify ref="verify" mode="pop" captchaType="blockPuzzle" :imgSize="{ width: '310px', height: '155px' }" @okCallback="okCallback"></Verify>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef, computed } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { MobileCode } from "@/components/form";
import { AccountPassWordEditingFormState } from "@/types/authority/accountEditing";
import { modifyManageAccounts, getAdminCode } from "@/api/authority/accountEditing";
import Verify from "@/components/verifition/Verify.vue";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
import { useUserStore } from "@/store/user";
const { logout } = useUserStore();
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
const formState = ref<AccountPassWordEditingFormState>({
    mobile: props.form.mobile,
    code: "",
    // oldPassword: "",
    password: "",
    pwdConfirm: ""
});
const btnText = ref(t('获取验证码'));
/** 是否可以发送验证码 */
const ableSend = ref(true);
/** 倒计时秒数 */
const countdown = ref(60);
const verify = ref();
/** 滑块验证 token */
const verifyToken = ref(null);

/** 滑块验证成功回调 */
const okCallback = (e: any) => {
    verifyToken.value = e.verifyToken;
    onCode();
};

/** 获取短信验证码 */
const onCode = async () => {
    if (!verifyToken.value) {
        verify.value.show();
        return;
    }
    try {
        const result = await getAdminCode({ type: "code", verifyToken: verifyToken.value });
        message.success(t('验证码发送成功'));
        ableSend.value = false;
        verifyToken.value = null;
        startCountdown();
    } catch (error: any) {
        message.error(error.message);
    }
};

/** 确认密码校验 - 检查两次密码是否一致 */
const validatePwd = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('确认密码不能为空')));
        return;
    }
    // 判断是否符合国际手机号格式
    if (formState.value.password == value) {
        callback();
    } else {
        callback(t('两次密码输入不一致'));
    }
};

/** 密码格式校验 - 至少8位，不能是纯数字或纯字母 */
const validatePassword = (rule: any, value: any, callback: any) => {
    if (value === "") {
        callback(new Error(t('请输入密码')));
    } else if (value.length < 8) {
        callback(new Error(t('密码长度至少为8位')));
    } else if (/^\d+$/.test(value) || /^[a-zA-Z]+$/.test(value)) {
        callback(new Error(t('密码不能是纯数字或纯字母')));
    } else {
        callback();
    }
};

/** 倒计时显示文字 */
const codeText = computed(() => {
    return countdown.value + t('秒后重发');
});
const timer = ref();

/** 启动倒计时 */
const startCountdown = () => {
    timer.value = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
            clearInterval(timer.value);
            ableSend.value = true;
            countdown.value = 60;
        }
    }, 1000);
};

/** 表单提交 - 修改密码 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        // 手机号已脱敏，接口不需要 mobile 参数，先注释掉
        const { mobile, ...submitData } = formState.value;
        const result = await modifyManageAccounts({ modifyType: 2, ...submitData });
        emit("submitCallback", result);
        message.success(t('修改成功'));
        logout();
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
const errorMessage = ref("");
const mobileErrorCallback = (e: any) => {
    errorMessage.value = e;
};

defineExpose({ onFormSubmit });
</script>
