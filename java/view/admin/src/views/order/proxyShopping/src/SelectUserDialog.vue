<template>
    <SelectUser v-model:userId="tmpUserId" v-model:user="tmpUser" :multiple="false" :showTips="showTips" />
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import SelectUser from "./SelectUser.vue";

const props = withDefaults(
    defineProps<{
        userId?: number;
        showTips?: boolean;
        visible?: boolean;
    }>(),
    {
        showTips: false
    }
);

const emit = defineEmits<{
    submitCallback: [Record<string, any>, boolean?];
    okType: [boolean];
}>();

// DialogForm 会向内容组件透传 confirmLoading 的 v-model，这里接住避免告警
defineModel("confirmLoading", {
    type: Boolean,
    default: false
});

const tmpUserId = ref<number | undefined>(undefined);
const tmpUser = ref<Record<string, any> | undefined>(undefined);

watch(
    () => props.visible,
    (val) => {
        if (val) {
            tmpUserId.value = props.userId;
            tmpUser.value = undefined;
            emit("okType", !!tmpUserId.value);
        }
    }
);

watch(
    () => tmpUserId.value,
    (val) => {
        emit("okType", !!val);
    }
);

watch(
    () => tmpUser.value,
    (val) => {
        // 以“是否拿到完整用户对象”为准：避免仅有 userId 但缺少回显字段
        emit("okType", !!val?.userId);
    }
);

const onFormSubmit = () => {
    if (!tmpUser?.value?.userId) {
        emit("okType", false);
        return;
    }
    emit("submitCallback", { ...(tmpUser.value as any) }, true);
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less"></style>


