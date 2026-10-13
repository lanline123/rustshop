<template>
    <view class="fixed-placeholder">
        <view class="content" :class="{ border: border }">
            <slot />
        </view>
        <view class="placeholder" :style="{ paddingTop }" />
    </view>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, onMounted, ref } from "vue";
import { addUnit, getElementRect } from "@/utils";

const props = defineProps({
    height: {
        type: [Number, String],
        default: 120
    },
    backgroundColor: {
        type: String,
        default: "transparent"
    },
    border: {
        type: Boolean,
        default: true
    }
});

const instance = getCurrentInstance();

const contentHeight = ref(0);

const getContentRect = async () => {
    try {
        const result = await getElementRect(".content", instance);
        // console.log("getElementRect result:", result);
        contentHeight.value = result ? result.height : 0;
    } catch (e) {}
};

onMounted(() => {
    getContentRect();
});

const height = computed(() => {
    return addUnit(props.height, "rpx");
});

const paddingTop = computed(() => {
    return `calc(${height.value} + var(--safe-bottom))`;
});
</script>

<style lang="scss" scoped>
.placeholder {
    width: 100%;
    height: 0;
}
.content {
    width: 100%;
    height: v-bind(height);
    position: fixed;
    bottom: 0;
    left: 0;
    background-color: v-bind("props.backgroundColor");
    box-sizing: content-box;
    z-index: 10;
    box-shadow: 0 0 10px rgba(125, 126, 128, 0.16);
    padding-bottom: var(--safe-bottom);

    &.border {
        box-shadow: none;
    }
}
.border {
    border-top: 1px solid #eee;
}
</style>
