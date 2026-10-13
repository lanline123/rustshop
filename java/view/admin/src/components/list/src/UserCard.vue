/** 用户信息卡片 - 显示用户名/手机号，手机号自动脱敏 */
<template>
    <div class="user" v-if="userValue!=''">
        {{ userValue }}
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";

const props = defineProps({
    /** 用户名或手机号 */
    user: { type: String, default: "" }
});

/** 是否显示完整信息 */
const showMobile = ref(false);
/** 是否为演示模式 */
const isDemo = ref(import.meta.env.VITE_IS_DEMO || 0);

/** 切换显示状态 */
const toggleVisibility = () => {
    showMobile.value = !showMobile.value;
};

/**
 * 计算显示的用户值
 * @description 如果是手机号则自动脱敏，否则原样显示
 */
const userValue = computed(() => {
    if (showMobile.value) {
        return props.user;
    } else {
        if (typeof props.user === "string") {
            if (props.user === "") {
                return "";
            } else {
                // 手机号码有效性检查
                const regex = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/;
                if (regex.test(props.user)) {
                    return props.user.replace(/(\d{3})\d{4}(\d{4})/, "$1****$2");
                } else {
                    return props.user; // 不是标准手机号，返回原值
                }
            }
        } else {
            return "";
        }
    }
});
</script>

<style scoped lang="less">

.user {
    width: 100px;
}
</style>

