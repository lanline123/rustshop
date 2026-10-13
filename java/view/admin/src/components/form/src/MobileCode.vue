/**
 * @file 手机验证码输入组件
 * @description 集成发送验证码功能的输入框组件，用于手机号验证场景
 * @features
 * - 集成滑块验证码防刷
 * - 60秒倒计时防重复发送
 * - 手机号格式校验（支持国内手机号）
 * - 自定义验证码发送API
 * - 错误回调通知父组件
 */
<template>
    <TigInput width="100%" size="large" :class="class" :placeholder="$t('请输入验证码')" v-model="mobileCode" @input="onInput">
        <template #suffix>
            <div v-if="ableSend" class="code-btn" @click.stop="onClick">{{ btnText }}</div>
            <div v-if="!ableSend" class="code-text">{{ codeText }}</div>
        </template>
    </TigInput>
    <Verify ref="verify" mode="pop" captchaType="blockPuzzle" :imgSize="{ width: '310px', height: '155px' }" @okCallback="okCallback"></Verify>
</template>
<script setup lang="ts">
import { ref, toRefs, onUnmounted, computed } from "vue";
import { message, Modal } from "ant-design-vue";
import Verify from "@/components/verifition/Verify.vue";
import { result } from "lodash-es";

/**
 * 组件属性定义
 * @property {string} class - 自定义样式类名
 * @property {string} mobileCode - 验证码值，支持 v-model
 * @property {string} mobile - 手机号
 * @property {Function} requestApi - 发送验证码的API函数
 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    class: String,
    mobileCode: { type: String, default: "" },
    mobile: { type: String, default: "" },
    requestApi: { type: Function, default: result },
});
const { mobile } = toRefs(props);
const emit = defineEmits(["update:mobileCode", "mobileErrorCallback"]);

/** 发送按钮文本 */
const btnText = ref(t('获取验证码'));
/** 验证码输入值 */
const mobileCode = ref("");
/** 发送类型标识 */
const sendType = ref(0);
/** 是否可以发送验证码 */
const ableSend = ref(true);
/** 滑块验证组件引用 */
const verify = ref();
/** 滑块验证token */
const verifyToken = ref(null);
/** 倒计时秒数 */
const countdown = ref(60);

/**
 * 输入事件处理
 * @description 同步验证码值到父组件
 */
const onInput = () => {
    emit("update:mobileCode", mobileCode.value);
};

/**
 * 点击发送验证码
 * @description 校验手机号 -> 滑块验证 -> 发送请求 -> 开始倒计时
 */
const onClick = async () => {
    if (mobile.value == "") {
        emit("mobileErrorCallback", t('手机号不能为空！'));
        return false;
    }
    if (!validatePhoneNumber(mobile.value)) {
        emit("mobileErrorCallback", t('手机号格式错误'));
        return;
    }
    if (!verifyToken.value) {
        verify.value.show();
        return;
    }
    try {
        const result = await props.requestApi({
            mobile: mobile.value,
            verifyToken: verifyToken.value,
            event:"login"
        });
        ableSend.value = false;
        verifyToken.value = null;
        startCountdown();
    } catch (error: any) {
        emit("mobileErrorCallback", error.message);
        return;
    } finally {
    }
};

/**
 * 手机号格式验证
 * @description 验证是否为有效的中国大陆手机号
 * @param {any} phoneNumber - 待验证的手机号
 * @returns {boolean} 是否为有效手机号
 */
const validatePhoneNumber = (phoneNumber: any) => {
    const internationalRegex = /^(\+?[1-9]\d{1,14})$/; // 定义支持的国际手机号格式正则表达式
    const regex = /^(?:\+?86)?1[3-9]\d{9}$/; // 国内手机号正则表达式
    // 判断是否符合国际手机号格式
    if (regex.test(phoneNumber)) {
        return true;
    } else {
        return false;
        // 判断是否符合国内手机号格式  需增加设置项
        // return internationalRegex.test(phoneNumber);
    }
};

/**
 * 倒计时文本
 * @returns {string} 格式化的倒计时文本
 */
const codeText = computed(() => {
    return countdown.value + t('秒后重发');
});

/** 倒计时定时器 */
const timer = ref();

/**
 * 开始倒计时
 * @description 60秒倒计时，结束后恢复发送按钮
 */
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

/**
 * 滑块验证成功回调
 * @description 验证成功后自动发送验证码
 * @param {any} e - 验证结果，包含 verifyToken
 */
const okCallback = (e: any) => {
    verifyToken.value = e.verifyToken;
    onClick();
};

// 组件卸载时清除定时器
onUnmounted(() => {
    clearInterval(timer.value);
});
</script>
<style lang="less" scoped>
.code-btn {
    color: #333;
    cursor: pointer;
}
</style>
