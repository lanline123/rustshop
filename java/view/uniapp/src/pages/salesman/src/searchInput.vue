<template>
    <view class="input-box">
        <up-input
            v-model="keyword"
            :custom-style="customStyle"
            :clearable="clearable"
            :font-size="fontSize"
            :border="border"
            :prefix-icon="prefixIcon"
            :prefix-icon-style="prefixIconStyle"
            :placeholder="placeholder"
            @confirm="confirm"
            @clear="clear"
        />
    </view>
</template>

<script setup lang="ts">
/**
 * 搜索输入框组件
 * - 带搜索图标的输入框
 * - 支持清空和提交搜索
 */
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const props = defineProps({
    modelValue: { type: String, default: "" },
    customStyle: {
        type: Object,
        default: () => ({ "background-color": "#f7f8fa", "border-radius": "36rpx", padding: "10rpx 15rpx", height: "inherit" })
    },
    prefixIcon: {
        type: String,
        default: "search"
    },
    prefixIconStyle: {
        type: [Object, String],
        default: "font-size: 20px;"
    },
    clearable: {
        type: Boolean,
        default: true
    },
    fontSize: {
        type: String,
        default: "14px"
    },
    border: {
        type: String,
        default: "none"
    },
    placeholder: {
        type: String,
        default: "商品搜索：请输入关键字"
    }
});
const placeholder = ref(t(props.placeholder));
const emit = defineEmits(["update:modelValue", "confirm", "clear"]);
const keyword = computed({
    get() {
        return props.modelValue;
    },
    set(value) {
        emit("update:modelValue", value);
    }
});
const confirm = (val: any) => {
    emit("confirm", val);
};
const clear = () => {
    emit("update:modelValue", "");
    emit("clear");
};
</script>

<style lang="scss" scoped>
.input-box {
    width: 100%;
}
</style>
