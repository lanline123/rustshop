<template>
    <view class="empty-box" :class="{ 'empty-box--center-content': centerContent }" :style="{ height, background }">
        <up-empty :icon="staticResource('common/empty-box.png')" :mode="mode" :text="displayText" width="240rpx" height="240rpx" />
    </view>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { computed } from "vue";
import { staticResource } from "@/utils";

const { t } = useI18n();
const props = defineProps({
    mode: {
        type: String,
        default: "search"
    },
    height: {
        type: String,
        default: "300rpx"
    },
    background: {
        type: String,
        default: "#fff"
    },
    text: {
        type: String,
        default: ""
    },
    centerContent: {
        type: Boolean,
        default: false
    }
});

const displayText = computed(() => props.text || t("暂无数据"));
</script>

<style lang="scss" scoped>
.empty-box--center-content {
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
}
</style>
