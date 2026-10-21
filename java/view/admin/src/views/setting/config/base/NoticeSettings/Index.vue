/**
 * @file 通知设置入口页
 * @description 管理各类通知渠道设置的入口
 * @features
 * - 短信通知设置
 * - 邮件通知设置
 * - 钉钉通知设置
 * - 企业微信通知设置
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <el-tabs v-model="activeKey" class="lyecs-tabs" tab-position="top" @tab-change="onTabChange">
                <el-tab-pane :key="1" :label="$t('短信通知设置')" name="Msg"></el-tab-pane>
                <el-tab-pane :key="2" :label="$t('邮件通知设置')" name="Email"></el-tab-pane>
                <el-tab-pane :key="3" :label="$t('钉钉通知设置')" name="Ding"></el-tab-pane>
                <el-tab-pane :key="4" :label="$t('企业微信通知设置')" name="WxWork"></el-tab-pane>
            </el-tabs>
            <Msg v-if="activeKey === 'Msg'"></Msg>
            <Email v-if="activeKey === 'Email'"></Email>
            <Ding v-if="activeKey === 'Ding'"></Ding>
            <WxWork v-if="activeKey === 'WxWork'"></WxWork>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { ref } from "vue";
import { Msg, Email, Ding, WxWork } from "./src/index";
const activeKey = ref("Msg");
const onTabChange = (val: string) => {
    activeKey.value = val;
};
</script>
<style lang="less" scoped>
.content_wrapper{
    min-height: calc(100vh - 260px);
}

</style>