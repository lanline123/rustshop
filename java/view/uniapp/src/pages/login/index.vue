<template>
    <view>
        <baseLogin v-model:csrf-token="csrfToken" v-model:login-type="loginType" :decorate-config="decorateConfig" />
    </view>
</template>

<script lang="ts" setup>
/**
 * 登录页面
 * - 整合多种登录方式的入口页面
 * - 初始化CSRF令牌
 * - 根据平台展示不同登录选项
 * - 拉取移动端登录页装修配置（decorateSn=mobileUserLogin），下发核心组件按兜底应用
 */
import { ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { csrfCreate } from "@/api/login/login";
import { getMobileLoginDecorate } from "@/api/home/home";
import baseLogin from "./src/baseLogin.vue";
import { useUserStore } from "@/store/user";

const userStore = useUserStore();

const loginType = ref("password");

const oauthCallbackData = ref({});

// 登录页装修配置（仅颜色 + 边距）；未配置 / 失败时为空对象 → 核心组件全程兜底、样式零变化
// 数据来源：新模块化装修（home/home/mobileLogin，Decorate 页 H5+pageType=5），取 moduleList 中 type=mobileLogin 的 module
const decorateConfig = ref<Record<string, string>>({});

const fetchLoginDecorate = async (previewId?: string | number) => {
    try {
        const result = await getMobileLoginDecorate(previewId);
        const moduleList = Array.isArray(result?.moduleList) ? result.moduleList : [];
        const item = moduleList.find((m) => m.type === "mobileLogin") ?? moduleList[0];
        decorateConfig.value = item?.module || {};
    } catch (error) {
        decorateConfig.value = {};
    }
};

onLoad((options) => {
    if (options) {
        if (options.loginType) {
            loginType.value = options.loginType;
        }
        if (options.url) {
            let decodedUrl = options.url;
            try {
                decodedUrl = decodeURIComponent(options.url);
            } catch (error) {
                console.error("URL 解码失败，使用原始值:", error);
            }
            uni.setStorageSync("URL", decodedUrl);
        }
        if (options.code) {
            oauthCallbackData.value = options;
        }
    }
    getCsrfCreateData();
    // 后台"预览"按钮打开 /pages/login/index?previewId=xxx，带上则取草稿版装修
    fetchLoginDecorate(options?.previewId);
});

onShow(() => {
    if (uni.getStorageSync("token") || userStore.token) {
        uni.reLaunch({
            url: "/pages/index/index"
        });
    }
});

const csrfToken = ref("");

const getCsrfCreateData = async () => {
    try {
        const result = await csrfCreate();
        csrfToken.value = result;
    } catch (error) {
        console.error("获取CSRF Token失败:", error);
    }
};
</script>

<style>
page {
    background: #fff !important;
}
</style>
