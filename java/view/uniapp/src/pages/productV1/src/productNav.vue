<template>
    <view class="product-nav">
        <view class="product-nav-container">
            <view class="product-nav-left product-nav-icon" :style="{ opacity: iconOpacity }" @click="handleBack">
                <text class="iconfont icon-houtui" :class="{ change: modelValue > contentThresholdValue }" />
            </view>

            <view class="product-nav-middle" :style="{ opacity: contentOpacity }" @click.stop>
                <view
                    v-for="item in moduleList"
                    :key="item.label"
                    class="middle-text"
                    :class="{ active: item.value === activeModule }"
                    @click="handleClick(item.value)"
                    >{{ $t(item.label) }}</view
                >
            </view>
            <view class="product-nav-right-group" :style="{ opacity: iconOpacity }">
                <template v-if="modelValue <= contentThresholdValue">
                    <view class="product-nav-icon action-btn" @click="$emit('share')">
                        <text class="iconfont-h5 icon-fenxiang" :class="{ change: modelValue > contentThresholdValue }" />
                    </view>
                    <view class="product-nav-icon action-btn" @click="$emit('collect')">
                        <text class="iconfont-h5" :class="[isCollect ? 'icon-yishoucang' : 'icon-shoucang1', { change: modelValue > contentThresholdValue }]" />
                    </view>
                </template>
                <view class="product-nav-icon action-btn" @click="handelShowMenu">
                    <text class="iconfont-h5 icon-gengduo2" :class="{ 'change icon-gengduo': modelValue > contentThresholdValue }" />
                </view>
            </view>
        </view>
    </view>
    <menuPop v-model="showMenu" />
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 商品详情底部导航栏组件
 * - 客服咨询、购物车入口按钮
 * - 加入购物车、立即购买按钮
 * - 显示购物车商品数量角标
 */
import { computed, ref } from "vue";
import { useConfigStore } from "@/store/config";
import menuPop from "./menuPop.vue";

const { t } = useI18n();
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
    isCollect: {
        type: Boolean,
        default: false
    }
});
const emit = defineEmits(["update:modelValue", "update:activeModule", "share", "collect"]);
const moduleList = ref([
    {
        label: t("商品"),
        value: ".product-area"
    },
    {
        label: t("评价"),
        value: ".evaluate-area"
    },
    {
        label: t("详情"),
        value: ".product-detail-area"
    }
]);
const contentThresholdValue = ref(250);
const iconThresholdValue = contentThresholdValue.value - 80;
const iconOpacity = computed(() => {
    let opacity = 1;
    if (props.modelValue > iconThresholdValue) {
        opacity = 0;
    } else if (props.modelValue < iconThresholdValue && props.modelValue > 0) {
        opacity = (iconThresholdValue - props.modelValue) / iconThresholdValue;
    }
    if (props.modelValue > contentThresholdValue.value) {
        opacity = 1;
    }
    return opacity;
});
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
const showMenu = ref(false);
const handelShowMenu = () => {
    showMenu.value = true;
};
const handleBack = () => {
    // #ifdef H5
    history.back();
    // #endif

    // #ifndef H5
    uni.navigateBack();
    // #endif
};

const paddingTop = computed(() => {
    let num = configStore.saveTop + 10;
    // #ifdef MP-WEIXIN
    num = configStore.menuButtonInfo.top;
    // #endif
    return num + "px";
});

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

    .product-nav-container {
        display: flex;
        position: relative;
        justify-content: center;
        width: 100%;

        .product-nav-icon {
            color: #fff;
            position: absolute;
            z-index: 20;
            padding-top: v-bind(paddingTop);
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
                font-weight: bold;
                font-size: 38rpx;

                &.change {
                    color: #333;
                }
            }
        }

        .product-nav-right-group {
            position: absolute;
            right: 20rpx;
            z-index: 20;
            display: flex;
            align-items: center;
            gap: 16rpx;
            padding-top: v-bind(paddingTop);
            // #ifdef MP-WEIXIN
            right: v-bind("iconInfo.width + 20 + 'px'");
            top: v-bind("iconInfo.top + 'px'");
            height: v-bind("iconInfo.height + 'px'");
            padding-top: 0;
            // #endif

            .action-btn {
                position: static;
                padding-top: 0;
                background: rgba(0, 0, 0, 0.4);
                border-radius: 12rpx;
                width: 64rpx;
                height: 64rpx;
                display: flex;
                align-items: center;
                justify-content: center;
                &:has(.change) {
                    background: transparent;
                    width: auto;
                    height: auto;
                }

                .icon-fenxiang,
                .icon-shoucang1,
                .icon-yishoucang,
                .icon-gengduo {
                    font-size: 38rpx;
                    &.change {
                        color: #333;
                    }
                }
                .icon-gengduo2 {
                    font-size: 48rpx;
                }

                .icon-yishoucang {
                    color: #fe9600;
                }
            }
        }

        .product-nav-middle {
            box-sizing: content-box;
            padding: 10px 60rpx;
            display: flex;
            align-items: center;
            width: 100%;
            justify-content: space-around;
            font-size: 32rpx;
            background-color: #fff;
            // #ifdef MP-WEIXIN
            height: v-bind("iconInfo.height + 'px'");
            padding-bottom: 0;
            padding-right: v-bind("iconInfo.width + 40 + 'px'");
            // #endif
            padding-top: v-bind(paddingTop);
            .middle-text {
                &.active {
                    color: var(--general);
                    font-weight: 600;
                }
            }
        }
    }
}
</style>
