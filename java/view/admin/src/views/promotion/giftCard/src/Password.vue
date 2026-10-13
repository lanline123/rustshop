<template>
    <div class="mobile">
        {{ passwordValue }}
        <i @click="toggleVisibility" class="iconfont-admin icon-cu" :class="[show ? 'icon-yanjing-kai' : 'icon-yanjing-guan']"></i>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { getGiftCardPasswrod } from "@/api/promotion/giftCard";

const props = defineProps<{ password: string | undefined; cardId: number }>();
const passwordValue = ref(props.password);
const show = ref(false);

const toggleVisibility = () => {
    if (show.value) {
        passwordValue.value = props.password;
        show.value = !show.value;
        return;
    }
    getGiftCardPasswrod({ cardId: props.cardId })
        .then((res) => {
            passwordValue.value = res;
            show.value = true;
        })
        .catch((error: any) => {
            message.error(error.message);
        });
};
watch(
    () => props.password,
    (newValue) => {
        passwordValue.value = newValue;
    }
);
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
}
</style>
