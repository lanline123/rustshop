<template>
    <div @click="switchChange" :class="'table-switch table-switch-' + (selectChecked ? 'on' : 'off') + (disabled ? ' disabled' : '')">
        <i></i>
    </div>
</template>
<script setup lang="ts">
// 不使用a-switch，增加表格渲染速度
import { computed, toRefs } from "vue";
import { message } from "ant-design-vue";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    requestApi: {
        type: Function
    },
    field: {
        type: String,
        default: ""
    },
    params: {
        type: Object,
        default: {} as any
    },
    checked: { type: [Boolean, Number], default: Number },
    switchingCode: {
        type: Array,
        default: [0, 1]
    },
    disabled: { type: Boolean, default: false }
});
const selectChecked = computed(() => {
    return checked.value == props.switchingCode[1] ? 1 : 0;
});
const emit = defineEmits(["update:checked", "callback", "updateData"]);
const { checked } = toRefs(props);
const switchChange = () => {
    if (props.disabled) {
        return;
    }
    // 没有请求接口或者没有字段
    if ((!props.requestApi && typeof props.requestApi !== "function") || !props.field) {
        return;
    }
    const value = checked.value == props.switchingCode[1] ? props.switchingCode[0] : props.switchingCode[1];
    props
        .requestApi({ ...props.params, [props.field]: value })
        .then((result: any) => {
            emit("update:checked", value);
            emit("callback");
            emit("updateData");
            message.success(t('操作成功'));
        })
        .catch((error: any) => {
            message.error(error.message);
        });
};
</script>
<style scoped lang="scss">
.table-switch.disabled {
    cursor: not-allowed;
    opacity: 0.4;
}
</style>
