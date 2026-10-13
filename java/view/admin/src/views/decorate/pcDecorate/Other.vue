<template>
    <div class="container">
        <div class="content_wrapper decoratePgaeWrap">
            <div class="top-wrapper">
                <div class="flex">
                    <!-- 会员登录背景已迁移至"PC装修 → 登录页装修"(pageType=5)，此处下线保留兼容 -->
                    <div class="left-box tab-name" @click="type = 'pcUserLogin'" v-if="false">
                        <div :class="'left-cont tab-item ' + (type == 'pcUserLogin' ? 'current' : '')">{{ $t('会员登录背景') }}</div>
                    </div>
                </div>
            </div>
            <div class="flex box-wrapper">
                <div class="left-wrapper">
                    <!-- 会员登录背景已迁移至"PC装修 → 登录页装修"(pageType=5)，此处下线保留兼容 -->
                    <div class="left-box tab-name" @click="type = 'pcUserLogin'" v-if="false">
                        <div :class="'left-cont tab-item ' + (type == 'pcUserLogin' ? 'current' : '')">{{ $t('会员登录背景') }}</div>
                    </div>
                </div>
                <div class="tab-box" v-if="false">
                    <PcUserLogin></PcUserLogin>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/views/decorate/decorate/src/css/decorate.less";
import { isOverseas } from "@/utils/version";
import { ref } from "vue";
import PcUserLogin from "./src/PcUserLogin.vue";
import PcIndexTips from "./src/PcIndexTips/Index.vue";

const type = ref(isOverseas() ? "PcIndexTips" : "pcUserLogin");
</script>
