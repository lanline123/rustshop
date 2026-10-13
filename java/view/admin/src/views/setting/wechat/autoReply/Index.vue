<template>
    <div class="container">
        <div class="content_wrapper">
            <el-tabs v-model="activeTab" class="lyecs-tabs" tab-position="top">
                <el-tab-pane :label="$t('被关注回复')" name="subscribe">
                    <AutoReplyTab
                        v-if="activeTab === 'subscribe' || mountedTabs.subscribe"
                        :hint="$t('当用户关注此公众号时，可自动回复一条消息')"
                        :fetcher="getSubscribeReply"
                        :saver="saveSubscribeReply"
                    />
                </el-tab-pane>
                <el-tab-pane :label="$t('登录成功回复')" name="loginSuccess">
                    <AutoReplyTab
                        v-if="activeTab === 'loginSuccess' || mountedTabs.loginSuccess"
                        text-only
                        :hint="$t('当用户公众号登录时，可自动回复一条消息')"
                        :fetcher="getLoginSuccessReply"
                        :saver="saveLoginSuccessReply"
                    />
                </el-tab-pane>
                <el-tab-pane :label="$t('收到消息回复')" name="default">
                    <AutoReplyTab
                        v-if="activeTab === 'default' || mountedTabs.default"
                        :hint="$t('当用户发送消息时，自动回复固定内容')"
                        :fetcher="getDefaultReply"
                        :saver="saveDefaultReply"
                    />
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { reactive, ref, watch } from "vue";
import AutoReplyTab from "../components/AutoReplyTab.vue";
import {
    getSubscribeReply,
    saveSubscribeReply,
    getLoginSuccessReply,
    saveLoginSuccessReply,
    getDefaultReply,
    saveDefaultReply
} from "@/api/setting/wechat";

const activeTab = ref<"subscribe" | "loginSuccess" | "default">("subscribe");

// 一旦某个 tab 被打开过，就保留挂载，避免切换时数据丢失
const mountedTabs = reactive<{ subscribe: boolean; loginSuccess: boolean; default: boolean }>({
    subscribe: true,
    loginSuccess: false,
    default: false
});

watch(activeTab, (val) => {
    mountedTabs[val] = true;
});
</script>
