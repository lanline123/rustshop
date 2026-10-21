<template>
    <label class="check" @click="onCheckAll">
        <up-checkbox
            :shape="shape"
            :used-alone="true"
            :disabled="disabled"
            :active-color="checkedColor"
            :checked="checked"
            :icon-size="iconSize || undefined"
            :size="size"
            @change="onCheckAll"
        />
        {{ checkedText }}
    </label>
</template>

<script setup lang="ts">
import { useThemeStore } from "@/store/theme";
import { computed } from "vue";

const props = defineProps({
    checkedText: {
        type: String,
        default: ""
    },
    checkedSize: {
        type: Number,
        default: 0.8
    },
    checked: {
        type: Boolean,
        default: false
    },
    disabled: {
        type: Boolean,
        default: false
    },
    shape: {
        type: String,
        default: "circle"
    },
    size: {
        type: String,
        default: "15"
    },
    /** 勾选图标大小；留空则沿用 up-checkbox 自身默认值 */
    iconSize: {
        type: String,
        default: ""
    },
    /** 选中色；留空则取主题色 */
    activeColor: {
        type: String,
        default: ""
    }
});
const themeStore = useThemeStore();
const checkedColor = computed(() => {
    return props.activeColor || themeStore.themeStyle["--general"] || "#ee0a24";
});

const emit = defineEmits(["update:checked", "change"]);
const onCheckAll = () => {
    if (props.disabled) return;
    emit("update:checked", !props.checked);
    emit("change");
};
</script>

<style lang="scss" scoped>
:deep(.uni-checkbox-input) {
    padding: 6rpx;
}
.check {
    display: flex;
    align-items: center;
}
</style>
