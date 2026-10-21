<template>
    <view>
        <template v-if="dot">
            <uni-swiper-dot
                :bottom="Number(bottom)"
                :dots-styles="dotsStyles"
                :info="modelValue"
                :current="current"
                :mode="mode"
                field="content"
                @click-item="clickItem"
            >
                <swiper
                    :style="{ height: getHeight }"
                    :current="swiperDotIndex"
                    circular
                    :indicator-dots="false"
                    :autoplay="true"
                    :interval="3000"
                    :display-multiple-items="displayMultipleItems"
                    :previous-margin="previousMargin"
                    :vertical="vertical"
                    :duration="duration"
                    @change="swiperChange"
                >
                    <swiper-item v-for="(item, key) in modelValue" :key="key">
                        <slot :item="item" />
                    </swiper-item>
                </swiper>
            </uni-swiper-dot>
        </template>
        <template v-else>
            <swiper
                :style="{ height: getHeight }"
                :current="swiperDotIndex"
                circular
                :indicator-dots="false"
                :autoplay="autoplay"
                :interval="interval"
                :display-multiple-items="displayMultipleItems"
                :previous-margin="previousMargin"
                :vertical="vertical"
                :duration="duration"
                @change="swiperChange"
            >
                <swiper-item v-for="(item, key) in modelValue" :key="key">
                    <slot :item="item" />
                </swiper-item>
            </swiper>
        </template>
    </view>
</template>

<script lang="ts" setup>
import { ref, computed } from "vue";
const props = defineProps({
    modelValue: {
        type: Array,
        default: () => []
    },
    selectColor: {
        type: String,
        default: ""
    },
    color: {
        type: String,
        default: ""
    },
    mode: {
        type: String,
        default: "dot"
    },
    height: {
        type: [Number, String],
        default: 150
    },
    bottom: {
        type: [String, Number],
        default: 10
    },
    dot: {
        type: Boolean,
        default: true
    },
    displayMultipleItems: {
        type: Number,
        default: 1
    },
    interval: {
        type: Number,
        default: 3000
    },
    autoplay: {
        type: Boolean,
        default: true
    },
    previousMargin: {
        type: String,
        default: "0"
    },
    vertical: {
        type: Boolean,
        default: false
    },
    duration: {
        type: Number,
        default: 500
    }
});

const emit = defineEmits(["change"]);

const current = ref(0);

const dotsStyles = computed(() => {
    if (props.color)
        return {
            selectedBackgroundColor: props.selectColor,
            selectedBorder: props.selectColor,
            backgroundColor: props.color,
            bottom: props.bottom
        };

    return { selectedBackgroundColor: props.selectColor, selectedBorder: props.selectColor, bottom: props.bottom, border: "none" };
});

const getHeight = computed(() => {
    if (typeof props.height === "string") {
        return props.height;
    }
    return props.height + "rpx";
});
const swiperChange = (e: any) => {
    current.value = e.detail.current;
    emit("change", e.detail.current);
};

const swiperDotIndex = ref(0);
const clickItem = (e: any) => {
    swiperDotIndex.value = e;
};
</script>

<style lang="scss" scoped></style>
