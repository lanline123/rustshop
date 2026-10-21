<template>
    <template v-for="(item, index) in modules" :key="index">
        <template v-if="item.isShow !== false">
            <!-- 用户信息 -->
            <template v-if="item.type === 'user-info' || item.type === 'userInfo'">
                <userInfo :data="item.module || {}" />
            </template>
            <!-- 用户信息 V2 -->
            <template v-else-if="item.type === 'user-info-v2' || item.type === 'userInfoV2'">
                <userInfoV2 :data="item.module || {}" />
            </template>
            <!-- 用户信息3 -->
            <template v-else-if="item.type === 'user-info3' || item.type === 'userInfo3'">
                <userInfo3 :data="item.module || {}" :immersive="immersive && index === 0" />
            </template>
            <!-- 订单入口 -->
            <template v-else-if="item.type === 'order-entry' || item.type === 'orderEntry'">
                <orderEntry :data="item.module || {}" />
            </template>
            <!-- 我的钱包 -->
            <template v-else-if="item.type === 'my-wallet' || item.type === 'myWallet'">
                <myWallet :data="item.module || {}" />
            </template>
            <!-- 我的钱包V2 -->
            <template v-else-if="item.type === 'my-wallet-v2' || item.type === 'myWalletV2'">
                <myWalletV2 :data="item.module || {}" />
            </template>
            <!-- 广告图 -->
            <template v-else-if="item.type === 'ad-image' || item.type === 'adImage'">
                <adImage :data="item.module || {}" />
            </template>
            <!-- 我的服务（layoutStyle + serviceList） -->
            <template v-else-if="item.type === 'my-service' || item.type === 'myService'">
                <myServiceV2 :data="item.module || {}" />
            </template>
            <!-- 固定功能区 -->
            <template v-else-if="item.type === 'fixed-functional-area' || item.type === 'fixedFunctionalArea'">
                <fixedFunctionalArea :data="item.module || {}" />
            </template>

            <!-- 历史协议兜底：广告位（bannerList） -->
            <template v-else-if="item.type === 'bannerList' && item.module && (item.module as any).bannerList?.picUrl">
                <advertising :data="(item.module as any).bannerList" />
            </template>
            <!-- 历史协议兜底：菜单（menus） -->
            <template v-else-if="item.type === 'menus' && item.module">
                <myService :menu-type="(item.module as any).menuType" :data="(item.module as any).menuList" />
            </template>
        </template>
    </template>
</template>

<script setup lang="ts">
/**
 * @description 会员中心装修模块容器
 * - 根据后台配置的模块类型动态渲染组件
 */
import type { PropType } from "vue";

import userInfo from "./userInfo/index.vue";
import userInfoV2 from "./userInfoV2/index.vue";
import userInfo3 from "./userInfo3/index.vue";
import orderEntry from "./orderEntry/index.vue";
import myWallet from "./myWallet/index.vue";
import myWalletV2 from "./myWalletV2/index.vue";
import adImage from "./adImage/index.vue";
import myServiceV2 from "./myServiceV2/index.vue";
import fixedFunctionalArea from "./fixedFunctionalArea/index.vue";

import advertising from "./advertising/index.vue";
import myService from "./myService/index.vue";

import type { MemberDecorateModule } from "@/types/decorate/member-decorate";

defineProps({
    modules: {
        type: Array as PropType<MemberDecorateModule[]>,
        default: () => []
    },
    // 沉浸式标题：仅作用于首个 userInfo3，使其渐变背景延伸到页面顶部
    immersive: {
        type: Boolean,
        default: false
    }
});
</script>

<style lang="scss" scoped></style>
