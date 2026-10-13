<template>
    <div class="module-ad-con module-member-account" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content">
            <div class="account-wrap" :style="wrapStyle">
                <div class="account-container" :style="containerStyle">
                    <div class="account-header">
                        <div class="account-title">{{ module.moduleTitle || "账号" }}</div>
                    </div>
                    <div class="account-list">
                        <div v-for="(item, idx) in accountList" :key="idx" class="account-item">
                            <div class="account-item-left">
                                <div class="account-item-icon">
                                    <img v-if="item.picUrl" :src="item.picUrl" :alt="item.picTitle" />
                                </div>
                                <div class="account-item-title">{{ item.picTitle }}</div>
                            </div>
                            <div class="account-item-right">
                                <span v-if="item.picDesc" class="account-item-subtitle">{{ item.picDesc }}</span>
                                <span class="iconfont-h5 icon-a-gengduo20"></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { accountDefaults } from "./index";
import type { AccountModule } from "./types";

const props = defineProps<{ module: AccountModule }>();
const module = props.module;

const defaultInnerPadding = 15;
mergeDefaultModule(module, accountDefaults);

const frame = computed(() => module.frame ?? {});
const frameFormat = computed(() => formatFrame(frame.value));
const accountList = computed(() => (module.accountList ?? []).filter((item) => item.isShow !== false));

const wrapStyle = computed(() => ({
    backgroundColor: frame.value.backgroundColor || "#ffffff",
    borderRadius: `${frame.value.boxRadius ?? 0}px`
}));

const containerStyle = computed(() => ({
    padding: `${frame.value.innerPadding ?? defaultInnerPadding}px`
}));
</script>
<style lang="less" scoped>
.account-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.account-container {
    box-sizing: border-box;
}
.account-header {
    margin-bottom: 10px;
}
.account-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
}
.account-list {
    display: flex;
    flex-direction: column;
}
.account-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 0;
    border-bottom: 1px solid #f5f5f5;
    &:last-child {
        border-bottom: none;
    }
}
.account-item-left {
    display: flex;
    align-items: center;
    gap: 12px;
}
.account-item-icon {
    width: 20px;
    height: 20px;
    flex-shrink: 0;
    img {
        width: 100%;
        height: 100%;
        object-fit: contain;
    }
}
.account-item-title {
    font-size: 15px;
    color: #1a1a1a;
}
.account-item-right {
    display: flex;
    align-items: center;
    gap: 4px;
    .icon-a-gengduo20 {
        font-size: 10px;
    }
}
.account-item-subtitle {
    font-size: 14px;
    color: #1a1a1a;
}
.account-item-arrow {
    font-size: 12px;
    color: #888b94;
}
</style>
