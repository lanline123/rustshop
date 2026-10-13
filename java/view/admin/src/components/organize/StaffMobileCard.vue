<template>
    <div class="mobile" v-if="mobileValue != '--'">
        {{ mobileValue }}
        <i
            v-if="canToggle"
            @click.stop="toggleVisibility"
            class="iconfont-admin icon-cu"
            :class="[showMobile ? 'icon-yanjing-kai' : 'icon-yanjing-guan', loading ? 'is-loading' : '']"
        ></i>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { showMobile as showMobileApi } from "@/api/organize/serviceStaff";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    /** 服务人员ID（与详情接口一致） */
    id?: number;
    /** 列表/详情里展示的手机号（通常为脱敏/或明文，组件内部仍会脱敏显示） */
    mobile?: string;
}>();

const showMobile = ref(false);
const loading = ref(false);
const realMobile = ref<string | undefined>(undefined);

const isDemo = ref(import.meta.env.VITE_IS_DEMO || 0);

const canToggle = computed(() => typeof props.id === "number" && props.id > 0 && !!props.mobile);

watch(
    () => [props.id, props.mobile],
    () => {
        // 列表行变更时重置本地状态，避免复用上一行的明文手机号
        showMobile.value = false;
        loading.value = false;
        realMobile.value = undefined;
    }
);

const maskMobile = (mobile?: string): string => {
    if (typeof mobile !== "string") return "--";

    const numericMobile = mobile.replace(/\D/g, "");
    if (numericMobile.length <= 6) return mobile;

    const isLandline = /^0\d{2,3}-\d{7,8}$/.test(mobile);
    if (isLandline) {
        const [areaCode, mainNumber] = mobile.split("-");
        if (mainNumber && mainNumber.length > 4) {
            const hideCount = mainNumber.length - 4;
            const regex = new RegExp(`(.{2})(.{${hideCount}})(.{2})`);
            return areaCode + "-" + mainNumber.replace(regex, "$1" + "*".repeat(hideCount) + "$3");
        }
        return mobile;
    }

    let digitCount = 0;
    let masked = "";
    for (const char of mobile) {
        if (/[0-9]/.test(char)) {
            digitCount++;
            const shouldHide = digitCount > 3 && digitCount <= numericMobile.length - 4;
            masked += shouldHide ? "*" : char;
        } else {
            masked += char;
        }
    }
    return masked;
};

const mobileValue = computed(() => {
    if (showMobile.value) return realMobile.value ?? props.mobile ?? "--";
    return maskMobile(props.mobile);
});

const extractMobileFromResponse = (res: unknown): string | undefined => {
    if (typeof res === "string") return res;
    if (res && typeof res === "object") {
        const anyRes = res as any;
        return anyRes.mobile ?? anyRes.phone;
    }
    return undefined;
};

const toggleVisibility = async () => {
    if (loading.value) return;

    if (isDemo.value == 1) {
        message.error(t('您无权限查看'));
        return;
    }

    if (!canToggle.value) return;

    // 已展示：直接隐藏
    if (showMobile.value) {
        showMobile.value = false;
        return;
    }

    // 未展示：如果已拉取过明文，直接展示
    if (realMobile.value) {
        showMobile.value = true;
        return;
    }

    loading.value = true;
    try {
        const res = await showMobileApi({ id: props.id as number });
        const mobile = extractMobileFromResponse(res);
        if (!mobile) {
            message.error(t('获取手机号失败'));
            return;
        }
        realMobile.value = mobile;
        showMobile.value = true;
    } catch (e: any) {
        message.error(e?.message || t('获取手机号失败'));
    } finally {
        loading.value = false;
    }
};
</script>

<style scoped lang="less">
.mobile {
    display: inline-flex;
    flex-direction: row;
    gap: 6px;
    align-items: center;

    .icon-cu {
        cursor: pointer;
    }

    .is-loading {
        cursor: not-allowed;
        opacity: 0.6;
        pointer-events: none;
    }
}
</style>


