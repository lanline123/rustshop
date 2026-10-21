<template>
    <view class="product-nav">
        <view class="product-nav-container">
            <view class="product-nav-left product-nav-icon" @click="handleBack">
                <text class="iconfont icon-houtui" :class="{ change: modelValue > contentThresholdValue }" />
            </view>
            <view class="product-nav-bg" :style="{ opacity: contentOpacity }" />
            <view class="product-nav-middle">
                <view
                    v-for="item in moduleList"
                    :key="item.label"
                    class="middle-text"
                    :class="{ active: item.value === activeModule }"
                    @click="handleClick(item.value)"
                    >{{ $t(item.label) }}</view
                >
            </view>
        </view>
    </view>
    <view class="placeholder" />
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useConfigStore } from "@/store/config";
const configStore = useConfigStore();

const props = defineProps({
    modelValue: {
        type: Number,
        default: 0
    },
    activeModule: {
        type: String,
        default: ""
    },
    event: {
        type: Function,
        default: () => {}
    },
    title: {
        type: String,
        default: ""
    }
});
const emit = defineEmits(["update:modelValue", "update:activeModule"]);
const moduleList = ref([
    {
        label: props.title,
        value: ".product-detail-area"
    }
]);
const contentThresholdValue = ref(50);
const contentOpacity = computed(() => {
    let opacity = 0;
    if (props.modelValue > contentThresholdValue.value) {
        opacity = 1;
    } else if (props.modelValue < contentThresholdValue.value && props.modelValue > 0) {
        opacity = props.modelValue / contentThresholdValue.value;
    }
    return opacity;
});

const handleClick = (val: string) => {
    emit("update:activeModule", val);
    props.event(val);
};

const handleBack = () => {
    // #ifdef H5
    history.back();
    // #endif

    // #ifndef H5
    uni.navigateBack();
    // #endif
};

const iconInfo = computed(() => {
    return configStore.menuButtonInfo;
});
</script>

<style lang="scss" scoped>
.product-nav {
    position: fixed;
    left: 0;
    // top: v-bind(topNum);
    top: 0;
    z-index: 10;
    width: 100%;
    height: var(--nav-height);

    .product-nav-bg {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: var(--nav-height);
        background: #fff;
        z-index: 1;
    }

    .product-nav-container {
        display: flex;
        position: relative;
        justify-content: center;
        align-items: center;
        width: 100%;

        .product-nav-icon {
            color: #fff;
            position: absolute;
            z-index: 20;
            // padding-top: v-bind(paddingTop);
            // #ifdef MP-WEIXIN
            top: v-bind("iconInfo.top + 'px'");
            height: v-bind("iconInfo.height + 'px'");
            display: flex;
            align-items: center;
            padding-top: 0;
            // #endif
        }

        .product-nav-left {
            left: 20rpx;
            .icon-houtui {
                // font-weight: bold;
                font-size: 38rpx;
                color: #333;
                // &.change {
                //     color: #333;
                // }
            }
        }

        .product-nav-right {
            right: 20rpx;
            // #ifdef MP-WEIXIN
            right: v-bind("iconInfo.width + 20 + 'px'");
            // #endif
            .icon-gengduo {
                font-size: 44rpx;
                &.change {
                    color: #333;
                }
            }
        }

        .product-nav-middle {
            position: relative;
            z-index: 10;
            box-sizing: content-box;
            // padding: 10px 60rpx;
            display: flex;
            align-items: center;
            width: 100%;
            justify-content: space-around;
            font-size: 32rpx;
            height: var(--nav-height);
            // background-color: #fff;
            // #ifdef MP-WEIXIN
            height: v-bind("iconInfo.height + 'px'");
            padding-bottom: 0;
            padding-right: v-bind("iconInfo.width + 40 + 'px'");
            // #endif
            // padding-top: v-bind(paddingTop);
            .middle-text {
                &.active {
                    color: var(--general);
                    font-weight: 600;
                }
            }
        }
    }
}
.placeholder {
    width: 100%;
    height: 0;
    padding-top: var(--nav-height);
}
</style>
