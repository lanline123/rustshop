<template>
    <up-popup
        v-model:show="show"
        :close-on-click-overlay="maskClick"
        :overlay="overlay"
        :mode="position"
        :round="round"
        :bg-color="backgroundColor"
        :custom-style="{ ...customStyle }"
        :z-index="zIndex"
        :safe-area-inset-top="false"
        :safe-area-inset-bottom="false"
        :closeable="showClose"
        @close="popupClose"
    >
        <slot />
    </up-popup>
</template>

<script setup lang="ts">
import { computed, type PropType } from "vue";

const props = defineProps({
    show: {
        type: Boolean,
        default: false
    },
    showClose: {
        type: Boolean,
        default: true
    },
    overlay: {
        type: Boolean,
        default: true
    },
    position: {
        type: String as PropType<"top" | "bottom" | "center" | "right" | "left">,
        default: "bottom"
    },
    round: {
        type: [String, Number],
        default: 10
    },
    backgroundColor: {
        type: String,
        default: "#fff"
    },
    customStyle: {
        type: Object,
        default: () => ({})
    },
    maskClick: {
        type: Boolean,
        default: true
    },
    zIndex: {
        type: Number,
        default: 201
    },
    height: {
        type: String,
        default: "50vh"
    }
});

const emit = defineEmits(["update:show", "change", "handleShade", "close"]);

const show = computed({
    get() {
        return props.show;
    },
    set(val) {
        emit("update:show", val);
    }
});

const popupClose = () => {
    emit("close");
    emit("update:show", false);
};
</script>
<style lang="scss">
.a {
    overflow: hidden;
}
</style>
