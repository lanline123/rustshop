/**
 * @file 管理后台登录页面
 * @description 商城后台管理系统登录入口
 * @features
 * - 支持密码登录和手机验证码登录两种方式
 * - 集成滑块验证码防机器人
 * - 记住登录状态功能
 * - 用户协议确认（用户服务协议、隐私协议、售后服务政策）
 * - 登录成功后进入店铺选择流程
 * - 响应式布局，适配移动端
 */
<template>
    <div class="page-container">
        <Logo></Logo>
        <div class="login-page-right">
            <div class="login-box">
                <h1 class="login-title">
                    <span class="login-title-welcome">{{ $t('欢迎登录') }}</span>
                    <span class="login-title-intro">{{ $t('商城后台管理系统') }}</span>
                </h1>
                <div class="login-tabs-warp">
                    <Transition mode="out-in" name="slide-fade">
                        <el-tabs v-if="!selectShopFlag" v-model="loginType" class="login-tabs">
                            <el-tab-pane name="password" :label="$t('密码登录')">
                                <el-form ref="passwordFormRef" :model="formState" @keyup.enter="formSubmit(passwordFormRef ?? null)">
                                    <div class="erro-message" v-if="errorMessage"><el-alert type="error" :title="errorMessage" show-icon /></div>
                                    <el-form-item class="login-row" prop="username" :rules="[{ required: true, message: '用户名不能为空!' }]">
                                        <TigInput width="100%" class="login-input" size="large" v-model="formState.username" :placeholder="$t('请输入用户名')" />
                                    </el-form-item>
                                    <el-form-item class="login-row" prop="password" :rules="[{ required: true, message: '密码不能为空!' }]">
                                        <TigInput
                                            width="100%"
                                            type="password"
                                            show-password
                                            class="login-input"
                                            size="large"
                                            v-model="formState.password"
                                            :placeholder="$t('请输入密码')"
                                        />
                                    </el-form-item>
                                    <el-form-item>
                                        <el-button
                                            ref="submitBtn"
                                            :loading="loading"
                                            class="login-submit-btn"
                                            :disabled="disabled"
                                            type="primary"
                                            @click="formSubmit(passwordFormRef ?? null)"
                                        > {{ $t('登 录') }} </el-button>
                                    </el-form-item>
                                    <el-form-item
                                        prop="agreement"
                                        :rules="[{ required: true, validator: validateAgreement, trigger: 'change' }]"
                                        :class="['agreement-item', { 'agreement-item--two': protocolCount === 2 }]"
                                    >
                                        <div>
                                            <div>
                                                <el-checkbox v-model="formState.remember" class="mb10">
                                                    <template #default>
                                                        <div class="flex" style="width: 100%; flex-wrap: wrap; line-height: 22px">
                                                            <span> {{ $t('记住登录状态') }} </span>
                                                        </div>
                                                    </template>
                                                </el-checkbox>
                                            </div>
                                            <div>
                                                <el-checkbox
                                                    v-model="formState.agreement"
                                                    v-if="
                                                        protocolState &&
                                                        (protocolState.afterSalesServiceShow ||
                                                            protocolState.privacyPolicyShow ||
                                                            protocolState.termsOfServiceShow)
                                                    "
                                                >
                                                    <template #default>
                                                        <div class="flex" style="width: 100%; flex-wrap: wrap; line-height: 22px">
                                                            <span> {{ $t('我已阅读并同意') }} </span>
                                                            <template v-if="protocolState.termsOfServiceShow == 1">
                                                                <a href="javascript:;" @click="onViewProtocolContent('termsOfService')"> {{ $t('《用户服务协议》') }} </a>
                                                            </template>
                                                            <template v-if="protocolState.privacyPolicyShow == 1">
                                                                <a href="javascript:;" @click="onViewProtocolContent('privacyPolicy')"> {{ $t('《用户隐私协议》') }} </a>
                                                            </template>
                                                            <template v-if="protocolState.afterSalesServiceShow == 1">
                                                                <a href="javascript:;" @click="onViewProtocolContent('afterSalesService')"> {{ $t('《售后服务政策》') }} </a>
                                                            </template>
                                                        </div>
                                                    </template>
                                                </el-checkbox>
                                            </div>
                                        </div>
                                    </el-form-item>
                                </el-form>
                                <Verify
                                    ref="verify"
                                    mode="pop"
                                    captchaType="blockPuzzle"
                                    :imgSize="{ width: '310px', height: '155px' }"
                                    @okCallback="okCallback"
                                ></Verify>
                            </el-tab-pane>
                            <el-tab-pane name="mobile" :label="$t('手机登录')">
                                <el-form ref="mobileFormRef" :model="formState" @keyup.enter="formSubmit(mobileFormRef ?? null)">
                                    <div class="erro-message" v-if="mobileErrorMessage"><el-alert type="error" :title="mobileErrorMessage" show-icon /></div>
                                    <el-form-item class="login-row" prop="mobile" :rules="[{ validator: validateMobile, trigger: 'blur' }]">
                                        <TigInput width="100%" class="login-input" size="large" v-model="formState.mobile" :placeholder="$t('请输入手机号')" />
                                    </el-form-item>
                                    <el-form-item
                                        class="login-row"
                                        prop="mobileCode"
                                        :rules="[{ required: true, message: '验证码不能为空!', trigger: 'blur' }]"
                                    >
                                        <MobileCode
                                            class="login-input"
                                            v-model:mobile="formState.mobile"
                                            v-model:mobileCode="formState.mobileCode"
                                            :requestApi="sendMobileCode"
                                            @mobileErrorCallback="mobileErrorCallback"
                                        ></MobileCode>
                                    </el-form-item>
                                    <el-form-item>
                                        <el-button
                                            ref="submitBtn"
                                            :loading="loading"
                                            class="login-submit-btn"
                                            :disabled="disabled"
                                            type="primary"
                                            @click="formSubmit(mobileFormRef ?? null)"
                                        > {{ $t('登 录') }} </el-button>
                                    </el-form-item>
                                    <el-form-item
                                        prop="agreement"
                                        :rules="[{ required: true, validator: validateAgreement, trigger: 'change' }]"
                                        :class="['agreement-item', { 'agreement-item--two': protocolCount === 2 }]"
                                    >
                                        <div>
                                            <div>
                                                <el-checkbox v-model="formState.remember" class="mb10">
                                                    <template #default>
                                                        <div class="flex" style="width: 100%; flex-wrap: wrap; line-height: 22px">
                                                            <span> {{ $t('记住登录状态') }} </span>
                                                        </div>
                                                    </template>
                                                </el-checkbox>
                                            </div>
                                            <div>
                                                <el-checkbox
                                                    v-model="formState.agreement"
                                                    v-if="
                                                        protocolState &&
                                                        (protocolState.afterSalesServiceShow ||
                                                            protocolState.privacyPolicyShow ||
                                                            protocolState.termsOfServiceShow)
                                                    "
                                                >
                                                    <template #default>
                                                        <div class="flex" style="width: 100%; flex-wrap: wrap; line-height: 22px">
                                                            <span> {{ $t('我已阅读并同意') }} </span>
                                                            <template v-if="protocolState.termsOfServiceShow == 1">
                                                                <a href="javascript:;" @click="onViewProtocolContent('termsOfService')"> {{ $t('《用户服务协议》') }} </a>
                                                            </template>
                                                            <template v-if="protocolState.privacyPolicyShow == 1">
                                                                <a href="javascript:;" @click="onViewProtocolContent('privacyPolicy')"> {{ $t('《用户隐私协议》') }} </a>
                                                            </template>
                                                            <template v-if="protocolState.afterSalesServiceShow == 1">
                                                                <a href="javascript:;" @click="onViewProtocolContent('afterSalesService')"> {{ $t('《售后服务政策》') }} </a>
                                                            </template>
                                                        </div>
                                                    </template>
                                                </el-checkbox>
                                            </div>
                                        </div>
                                    </el-form-item>
                                </el-form>
                            </el-tab-pane>
                        </el-tabs>
                        <div v-else>
                            <SelectShop v-model:selectShopFlag="selectShopFlag"></SelectShop>
                        </div>
                    </Transition>
                </div>
            </div>
        </div>
        <div class="dialog">
            <el-dialog :z-index="12" v-model="dialogVisible">
                <template #header></template>
                <div class="wrap">
                    <div class="head-box flex flex-justify-between flex-align-center">
                        <div class="close" @click="dialogVisible = false">
                            <i class="iconfont icon-cha1"></i>
                        </div>
                    </div>
                    <div class="content-wrap" v-loading="contentLoading">
                        <el-scrollbar height="400px">
                            <div class="content" v-html="content"></div>
                        </el-scrollbar>
                    </div>
                </div>
            </el-dialog>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, onMounted, nextTick } from "vue";
import "@/style/css/login.less";
import { notification } from "ant-design-vue";
import { useRouter } from "vue-router";
import { MobileCode } from "@/components/form";
import type { FormInstance } from "element-plus";
import { adminSignin, sendMobileCode } from "@/api/authority/login";
import { getCsrf } from "@/api/common/common";
import Verify from "@/components/verifition/Verify.vue";
import SelectShop from "@/components/merchant/SelectShop.vue";
import Logo from "./src/Logo.vue";
import { getLoginProtocol, getLoginProtocolContent } from "@/api/vendor/setting";

/** 滑块验证码组件引用 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const verify = ref();
/** 登录类型标识（已弃用） */
const landingType = ref(true);
/** 是否显示店铺选择界面 */
const selectShopFlag = ref(false);
const router = useRouter();
const fullPath = router.currentRoute.value.fullPath;
/** 是否为商家登录入口 */
const merchantLogin = fullPath.includes("merchantLogin");
/** 当前登录方式：password-密码登录 / mobile-手机登录 */
const loginType = ref("password");
/** 通用错误信息 */
const errorMessage = ref("");
/** 密码登录错误信息 */
const passwordErrorMessage = ref("");
/** 手机登录错误信息 */
const mobileErrorMessage = ref("");
/** 登录按钮加载状态 */
const loading = ref(false);
/** 协议弹窗显示状态 */
const dialogVisible = ref(false);
/** 协议内容HTML */
const content = ref();
/** 协议内容加载状态 */
const contentLoading = ref(true);

/** 密码登录表单引用 */
const passwordFormRef = ref<FormInstance>();
/** 手机登录表单引用 */
const mobileFormRef = ref<FormInstance>();
/**
 * 登录表单数据
 * @property {string} username - 用户名
 * @property {string} password - 密码
 * @property {boolean} remember - 是否记住登录状态
 * @property {boolean} agreement - 是否同意协议
 * @property {string|null} verifyToken - 滑块验证token
 */
let formState = ref<any>({
    username: merchantLogin ? import.meta.env.VITE_DEFAULT_STORE_NAME || "" : import.meta.env.VITE_DEFAULT_USER_NAME || "",
    password: merchantLogin ? import.meta.env.VITE_DEFAULT_STORE_PASSWORD || "" : import.meta.env.VITE_DEFAULT_USER_PASSWORD || "",
    remember: false,
    agreement: true,
    verifyToken: null
});
/** 验证码唯一标识 */
const captchaUid = ref("");
/**
 * 提交登录表单
 * @description 执行登录验证和请求，处理各种登录结果
 * @param {FormInstance|null} e - 表单实例
 */
const formSubmit = async (e: FormInstance | null) => {
    if (!e) {
        return;
    }
    await _getCsrf();
    await e.validate();
    loading.value = true;
    try {
        const result = await adminSignin(
            {
                loginType: loginType.value,
                captchaUid: captchaUid.value,
                ...formState.value
            },
            csrfToken.value
        );
        // 设置Token
        localStorage.setItem("accessToken", result.token ?? "");
        localStorage.setItem("adminType", result.adminType ?? "");
        selectShopFlag.value = true;
    } catch (error: any) {
        if (error.code == 1002) {
            verify.value.show();
        } else if (error.code > 0) {
            notification["error"]({
                message: t('错误'),
                description: error.message
            });
            if (loginType.value == "password") {
                passwordErrorMessage.value = error.message;
            } else {
                mobileErrorMessage.value = error.message;
            }
        }
    } finally {
        setTimeout(() => {
            loading.value = false;
        }, 600);
    }
};
/** 提交按钮引用 */
const submitBtn = ref();
/**
 * 滑块验证成功回调
 * @description 验证成功后自动提交登录表单
 * @param {Object} e - 验证结果对象
 * @param {string} e.verifyToken - 验证成功token
 */
const okCallback = (e: any) => {
    formState.value.verifyToken = e.verifyToken;
    if (passwordFormRef.value) {
        formSubmit(passwordFormRef.value);
    }
};
/**
 * 手机号验证错误回调
 * @description 触发手机号字段的验证
 */
const mobileErrorCallback = () => {
    if (!mobileFormRef.value) return;
    mobileFormRef.value.validateField("mobile");
};
/** 提交按钮禁用状态（预留扩展） */
const disabled = computed(() => {
    return false;
});
/**
 * 手机号格式验证器
 * @description 验证中国大陆手机号格式
 * @param {any} rule - 验证规则
 * @param {any} value - 输入值
 * @param {Function} callback - 回调函数
 */
const validateMobile = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('手机号不能为空')));
        return;
    }
    const internationalRegex = /^(\+?[1-9]\d{1,14})$/; // 定义支持的国际手机号格式正则表达式
    const regex = /^(?:\+?86)?1[3-9]\d{9}$/; // 国内手机号正则表达式
    // 判断是否符合国际手机号格式
    if (regex.test(value)) {
        callback();
    } else {
        callback(t('手机号格式错误'));
        // 判断是否符合国内手机号格式  需增加设置项
        // return internationalRegex.test(phoneNumber);
    }
};
/**
 * 协议勾选验证器
 * @description 验证用户是否已同意相关协议
 * @param {any} rule - 验证规则
 * @param {any} value - 勾选状态
 * @param {Function} callback - 回调函数
 */
const validateAgreement = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('请阅读并同意用户服务协议、用户隐私协议及售后服务政策')));
        return;
    }
    callback();
};
onMounted(() => {
    const accessToken = localStorage.getItem("accessToken");
    const shopId = localStorage.getItem("shopId");
    if (accessToken && !shopId) {
        nextTick(() => {
            selectShopFlag.value = true;
        });
    }
});
/** CSRF令牌 */
const csrfToken = ref("");
/**
 * 获取CSRF令牌
 * @description 用于防止跨站请求伪造
 */
const _getCsrf = async () => {
    try {
        const result = await getCsrf();
        csrfToken.value = result;
    } catch (error) {
    } finally {
    }
};
/** 协议配置状态（用户服务协议、隐私协议、售后服务政策的显示开关） */
const protocolState = ref<any>();
/**
 * 获取登录协议配置
 * @description 获取需要展示的协议列表配置
 */
const _getLoginProtocol = async () => {
    try {
        const result = await getLoginProtocol();
        result.termsOfServiceShow = 1;
        protocolState.value = result;
    } catch (error: any) {}
};
_getLoginProtocol();

/**
 * 计算需要显示的协议数量
 * @returns {number} 协议数量（0-3）
 */
const protocolCount = computed(() => {
    if (!protocolState.value) return 0;
    let count = 0;
    if (protocolState.value.termsOfServiceShow == 1) count++;
    if (protocolState.value.privacyPolicyShow == 1) count++;
    if (protocolState.value.afterSalesServiceShow == 1) count++;
    return count;
});

/**
 * 查看协议内容
 * @description 打开协议弹窗并加载指定协议内容
 * @param {string} code - 协议代码：termsOfService/privacyPolicy/afterSalesService
 */
const onViewProtocolContent = (code: string) => {
    dialogVisible.value = true;
    _getLoginProtocolContent(code);
};

/**
 * 获取协议详细内容
 * @param {string} code - 协议代码
 */
const _getLoginProtocolContent = async (code: string) => {
    contentLoading.value = true;
    try {
        let result = await getLoginProtocolContent({ code });
        content.value = result.content;
    } catch (error: any) {
    } finally {
        contentLoading.value = false;
    }
};
</script>

<style lang="less" scoped>
.page-container {
    display: flex;
    height: 100%;
    :deep(.el-checkbox) {
        align-items: flex-start;
        .el-checkbox__input {
            margin-top: 3px;
        }
    }
}

.login-page-left {
    background: url("@/style/images/login-page-bg.png") center;
    background-size: cover;
    flex: 1.5;
    position: relative;
    height: 100%;
}

.login-page-right {
    -webkit-box-flex: 2;
    -webkit-flex: 2;
    -ms-flex: 2;
    flex: 2;
}

.login-box {
    height: 100%;
    display: -webkit-box;
    display: -webkit-flex;
    display: -ms-flexbox;
    display: flex;
    -webkit-box-align: center;
    -webkit-align-items: center;
    -ms-flex-align: center;
    align-items: center;
    -webkit-box-pack: center;
    -webkit-justify-content: center;
    -ms-flex-pack: center;
    justify-content: center;
    -webkit-box-orient: vertical;
    -webkit-box-direction: normal;
    -webkit-flex-direction: column;
    -ms-flex-direction: column;
    flex-direction: column;
}

.login-title {
    text-align: left;
    width: 456px;
    margin: 30px auto;
}

.login-title-welcome {
    font-family: PingFangSC-Medium;
    font-size: 30px;
    color: #111;
    letter-spacing: 0;
    margin-right: 17px;
}

.login-tabs-warp {
    width: 460px;
    overflow: hidden;
    border-radius: 12px;
    border: 1px solid #dee0e3;
    padding-top: 20px;
    min-height: 460px;
    -webkit-box-shadow:
        0 4px 8px rgba(31, 35, 41, 0.03),
        0 3px 6px -6px rgba(31, 35, 41, 0.05),
        0 6px 18px 6px rgba(31, 35, 41, 0.03);
    box-shadow:
        0 4px 8px rgba(31, 35, 41, 0.03),
        0 3px 6px -6px rgba(31, 35, 41, 0.05),
        0 6px 18px 6px rgba(31, 35, 41, 0.03);
    position: relative;
    :deep(.el-tabs__nav-wrap::after) {
        display: none;
    }
}

.login-tabs {
    :deep(.el-tabs__nav) {
        margin: 0 33px;
    }

    :deep(.el-form) {
        margin: 33px;
    }

    :deep(.login-row) {
        margin-bottom: 35px;
    }

    :deep(.el-tabs__item) {
        font-size: 18px;
        font-weight: 600;
        height: 50px;
    }
}

.erro-message {
    margin-bottom: 20px;
}

.login-submit-btn {
    width: 100%;
    height: 50px;
    font-size: 16px;
    margin-top: 5px;
}

.captcha-img {
    margin-left: 15px;
    width: 200px;
    height: 58px;
}

.login-tabs {
    :deep(.el-input__wrapper) {
        background: #f7f8fa;
        height: 60px;
        border-radius: 9px;
        box-shadow: 0 0 0 1px #f7f8fa inset;
    }

    :deep(.el-form-item.is-error .el-input__wrapper) {
        box-shadow: 0 0 0 1px var(--el-color-danger) inset;
    }

    :deep(.el-input__wrapper.is-focus) {
        box-shadow: 0 0 0 1px var(--el-input-focus-border-color) inset;
    }
}

.login-form {
    margin-top: 20px;
    padding: 0 33px;
}

.login-form li {
    margin-bottom: 35px;
    display: flex;
}

.fm-button {
    background-color: #ff9000;
    background-image: -webkit-gradient(linear, left top, right top, from(#ff9000), to(#ff9000));
    background-image: linear-gradient(90deg, #ff9000, #ff9000);
    border: 1px solid #ff9000;
    border-radius: 3px;
    font-size: 20px;
    height: 42px;
    line-height: 42px;
    outline: none;
    display: block;
    color: #fff;
    width: 100%;
    cursor: pointer;
}

.login-form .fm-button {
    background: #3d7fff;
    border-radius: 30px;
    border: none;
    margin-top: 14px;
    height: 60px;
    line-height: 60px;
    margin-bottom: 20px;
}

.login-form .fm-button:hover {
    color: #fff;
}

.sendMobileMsg {
    align-items: center;
}

.code-button {
    background: #3d7fff;
    border-radius: 20px;
    border: none;
    height: 40px;
    line-height: 40px;
    color: #fff;
    width: 150px;
    vertical-align: middle;
    margin-left: 10px;
}

.code-button:hover {
    color: #fff;
}

.error-info {
    width: 389px;
    background: #ffe6e6;
    border-radius: 6px;
    font-family: PingFangSC-Regular;
    font-size: 14px;
    color: red;
    align-items: center;
    font-size: 14px;
    text-indent: 10px;
    text-align: left;
    margin-bottom: 15px;
    padding: 10px 0;
}

.other_info {
    text-align: left;
}

.remember {
    margin-right: 20px;
}

.login-title-intro {
    font-family: PingFangSC-Regular;
    font-size: 20px;
    color: #666;
    font-weight: normal;
    letter-spacing: 0;
}

@media only screen and (max-width: 767px) {
    .page-container {
        flex-wrap: wrap;
        display: block;
        .login-page-left {
            height: 80px;
            background: #fff;
            padding-top: 20px;
            text-align: center;
            img {
                height: 50px;
            }
        }
    }
    .login-title {
        width: 100%;
        box-sizing: border-box;
        padding: 20px 20px 0;
        .login-title-welcome {
            font-size: 26px;
        }
    }
    .login-tabs-warp {
        width: 100%;
        box-sizing: border-box;
        box-shadow: none;
        padding: 20px;
        border: 0;
    }
    .login-tabs :deep(.el-tabs__nav) {
        margin: 0;
    }
    .login-tabs .el-form {
        margin: 0;
    }
}

.slide-fade-enter-active {
    transition: all 0.2s ease-out;
}

.slide-fade-leave-active {
    transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
    transform: translateX(20px);
    opacity: 0;
}

.agreement-item {
    padding-bottom: 15px;
    :deep(.el-form-item__error) {
        padding-top: 20px;
    }
    &--two {
        :deep(.el-form-item__error) {
            padding-top: 0;
        }
    }
}

.wrap {
    padding: 0 20px;
    .head-box {
        padding: 10px 0 10px 0;
        font-size: 16px;
        .title {
            color: #1f2026;
            font-weight: 500;
        }
        .close {
            margin-left: auto;
            cursor: pointer;
            &:hover {
                color: var(--tig-primary);
            }
        }
    }
    .content-wrap {
        min-height: 300px;
        max-height: 700px;
        overflow-y: auto;
    }
}
.dialog {
    :deep(.el-dialog__header) {
        display: none;
    }
    :deep(.el-dialog) {
        width: 760px;
    }
}
</style>
