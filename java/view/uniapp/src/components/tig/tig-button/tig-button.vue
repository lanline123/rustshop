<template>
    <up-button
        :custom-style="customStyle"
        :type="type"
        :size="size"
        :plain="plain"
        :color="color"
        :shape="shape"
        :disabled="disabled"
        :text="displayText"
        :loading="loading"
        :loading-text="loadingText"
        :open-type="openType"
        @click="handleClick"
        @getphonenumber="getPhoneNumber"
    >
        <slot />
    </up-button>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { computed } from "vue";

const { t } = useI18n();
// #ifdef MP-WEIXIN
defineOptions({
    options: { virtualHost: true }
});
// #endif

const props = defineProps({
    shape: {
        type: String as () => "circle" | "square",
        default: "circle"
    },
    type: {
        type: String as () => "primary" | "success" | "warning" | "info",
        default: "primary"
    },
    color: {
        type: String,
        default: "var(--main-bg)"
    },
    text: {
        type: String,
        default: ""
    },
    disabled: {
        type: Boolean,
        default: false
    },
    loading: {
        type: Boolean,
        default: false
    },
    plain: {
        type: Boolean,
        default: false
    },
    loadingText: {
        type: String,
        default: ""
    },
    size: {
        type: String as () => "normal" | "large" | "small",
        default: "normal"
    },
    // 兼容小程序
    customStyle: {
        type: Object,
        default: () => ({})
    },
    openType: {
        type: String,
        default: ""
    }
});

const displayText = computed(() => props.text || t("确定"));

const emit = defineEmits(["click", "getPhoneNumber"]);

const handleClick = (val) => {
    emit("click", val);
};

const getPhoneNumber = (event: any) => {
    emit("getPhoneNumber", event);
};
</script>

<style lang="scss" scoped>
.u-button--norma {
    font-size: 24rpx;
}
</style>
