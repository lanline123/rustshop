<template>
    <tig-layout :title="titleMap[articleSn as keyof typeof titleMap]" :custom-back="fromLoginPopup" @back="handleBack">
        <view class="rich-box">
            <rich-text class="rich-text" :nodes="articleData.content" />
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 商城协议页面
 * - 展示用户协议、隐私政策等协议内容
 * - 富文本渲染协议详情
 * - 支持返回上一页并通知同意状态
 */
import { onLoad, onBackPress } from "@dcloudio/uni-app";
import { ref } from "vue";
import { getArticle } from "@/api/article/article";
import { useUserStore } from "@/store/user";

const { t } = useI18n();
const userStore = useUserStore();
const fromLoginPopup = ref(false);

onLoad((options) => {
    if (options && options.articleSn) {
        articleSn.value = options.articleSn as "fwxy" | "ysxy";
        getArticleData();
    }
    // 标记是否来自登录弹窗
    if (options && options.from === "loginPopup") {
        fromLoginPopup.value = true;
    }
});

// 处理返回逻辑
const handleBack = () => {
    if (fromLoginPopup.value) {
        // 跳转到我的页面并打开登录弹窗
        uni.reLaunch({
            url: "/pages/member/index",
            success: () => {
                userStore.setAuthType("login");
            }
        });
    }
};

// 拦截系统返回按钮（安卓物理返回键等）
onBackPress(() => {
    if (fromLoginPopup.value) {
        handleBack();
        return true; // 阻止默认返回行为
    }
    return false;
});

const articleSn = ref<"fwxy" | "ysxy">("fwxy");

const articleData = ref({
    content: ""
});

const titleMap = {
    fwxy: t("服务协议"),
    ysxy: t("隐私政策")
};

const getArticleData = async () => {
    try {
        const result = await getArticle({ articleSn: articleSn.value }, "issueInfo");
        articleData.value = result.item;
    } catch (error) {
        console.error("获取文章失败:", error);
    }
};
</script>

<style>
page {
    background-color: #fff;
}
</style>

<style lang="scss" scoped>
.rich-box {
    padding: 24rpx 24rpx;

    .rich-text {
        overflow: hidden;
        :deep(p) {
            font-size: 24rpx;
            line-height: 2;
        }
        :deep(img) {
            max-width: 100%;
            width: 100%;
            display: block;
        }
    }
}
</style>
