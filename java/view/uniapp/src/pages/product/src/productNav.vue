<template>
    <!-- 装修风格：贴合式双层导航（A1 标准风 / A3 跨境风） -->
    <view v-if="isDecorateNav" class="product-nav product-nav--decorated" :class="{ 'product-nav--overseas': isOverseasNav }" :style="decoratedNavStyle">
        <view class="product-nav-container decorated">
            <view class="nav-left" @click="handleBack">
                <text class="iconfont-h5 icon-fanhui" />
            </view>
            <view class="product-nav-middle decorated">
                <view v-if="modelValue <= decorateTabThreshold" class="middle-title">{{ $t("商品详情") }}</view>
                <view v-else class="module-list">
                    <view
                        v-for="item in moduleList"
                        :key="item.label"
                        class="module-text"
                        :class="{ active: item.value === activeModule }"
                        @click="handleClick(item.value)"
                        >{{ $t(item.label) }}</view
                    >
                </view>
            </view>
            <view class="nav-collect" @click="$emit('collect')">
                <text class="iconfont-h5" :class="isCollect ? 'icon-yishoucang1' : 'icon-shoucang2'" />
            </view>
            <view class="nav-more" @click.stop="showQuickMenu = !showQuickMenu">
                <text class="iconfont-h5 icon-gengduo2" />
                <view class="more-menu-box" :class="{ showMenu: showQuickMenu }">
                    <view class="more-menu">
                        <view class="menu-item" @click.stop="handleSearch">
                            <view class="iconfont-h5 item-icon icon-a-34" />
                            <view class="item-name">{{ $t("搜索") }}</view>
                        </view>
                        <view class="menu-item" @click.stop="handleQuickShare">
                            <view class="iconfont-h5 item-icon icon-af" />
                            <view class="item-name">{{ $t("分享") }}</view>
                        </view>
                        <service v-if="showService" :phone="shopInfo.kefuPhone" :product-id="productId" :shop-id="shopInfo.shopId">
                            <view class="menu-item">
                                <view class="iconfont-h5 item-icon icon-a-22222" />
                                <view class="item-name">{{ $t("客服") }}</view>
                            </view>
                        </service>
                    </view>
                </view>
                <view v-show="showQuickMenu" class="shade-box" @click.stop="showQuickMenu = false" />
            </view>
        </view>
    </view>

    <!-- 默认风格：悬浮式导航 -->
    <view v-else class="product-nav" :style="{ top: navTop + 'px' }">
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
                    <view class="product-nav-icon action-btn" :class="{ 'action-btn--plain': modelValue > contentThresholdValue }" @click="$emit('share')">
                        <text class="iconfont-h5 icon-fenxiang" :class="{ change: modelValue > contentThresholdValue }" />
                    </view>
                    <view class="product-nav-icon action-btn" :class="{ 'action-btn--plain': modelValue > contentThresholdValue }" @click="$emit('collect')">
                        <text class="iconfont-h5" :class="[isCollect ? 'icon-yishoucang' : 'icon-shoucang1', { change: modelValue > contentThresholdValue }]" />
                    </view>
                </template>
                <view class="product-nav-icon action-btn" :class="{ 'action-btn--plain': modelValue > contentThresholdValue }" @click="handelShowMenu">
                    <text class="iconfont-h5 icon-gengduo2" :class="{ 'change icon-gengduo': modelValue > contentThresholdValue }" />
                </view>
            </view>
        </view>
    </view>
    <view v-if="isDecorateNav && !isOverseasNav" class="product-nav-placeholder" />
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
import type { PropType } from "vue";
import { useConfigStore } from "@/store/config";
import service from "@/components/service/index.vue";
import type { ShopDetailItem } from "@/types/shop/shop";
import { redirect } from "@/utils";
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
    },
    // 布局风格变体：A1 / A3 使用贴合式双层导航
    layoutStyle: {
        type: String,
        default: ""
    },
    // A3 商城头部显示时，二级导航吸附在它的实测高度之后。
    hasMainHeader: {
        type: Boolean,
        default: false
    },
    showService: {
        type: [Boolean, Number],
        default: true
    },
    productId: {
        type: [String, Number],
        default: ""
    },
    shopInfo: {
        type: Object as PropType<ShopDetailItem>,
        default: () => ({})
    }
});

// A1 标准风与 A3 跨境风共用贴合式双层导航；A2 门店风保留原悬浮导航。
const isDecorateNav = computed(() => props.layoutStyle === "A1" || props.layoutStyle === "A3");
const isOverseasNav = computed(() => props.layoutStyle === "A3");
const navTop = computed(() => (props.hasMainHeader ? configStore.commonHeaderHeight : 0));
const decoratedSafeTop = computed(() => {
    // #ifdef MP-WEIXIN
    if (isDecorateNav.value && !props.hasMainHeader) {
        return Math.max(configStore.menuButtonInfo.top || configStore.saveTop || 0, 0);
    }
    // #endif
    return 0;
});
const decoratedSafeRight = computed(() => {
    // #ifdef MP-WEIXIN
    if (isDecorateNav.value && !props.hasMainHeader) {
        return Math.max(configStore.menuButtonInfo.width || 0, 0);
    }
    // #endif
    return 0;
});
const decoratedNavStyle = computed(() => ({
    top: `${navTop.value}px`,
    "--product-nav-safe-top": `${decoratedSafeTop.value}px`,
    "--product-nav-safe-right": `${decoratedSafeRight.value}px`,
    "--product-nav-placeholder-height": `calc(77rpx + ${decoratedSafeTop.value}px)`
}));
// 与项目 D 一致：页面开始滚动后，标题切换为商详模块 tabs。
const decorateTabThreshold = computed(() => navTop.value);
const emit = defineEmits(["update:modelValue", "update:activeModule", "share", "collect"]);
// 评价锚点随配置动态更新。
const moduleList = computed(() => {
    const list = [
        {
            label: t("商详"),
            value: ".product-area"
        }
    ];
    if (configStore.productCommentEnabled === 1) {
        list.push({
            label: t("评价"),
            value: ".evaluate-area"
        });
    }
    list.push({
        label: t("详情"),
        value: ".product-detail-area"
    });
    return list;
});
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
const showQuickMenu = ref(false);
const handelShowMenu = () => {
    showMenu.value = true;
};
const handleSearch = () => {
    showQuickMenu.value = false;
    redirect({ url: "/pages/search/index" });
};
const handleQuickShare = () => {
    showQuickMenu.value = false;
    emit("share");
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
                &.action-btn--plain {
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
            background-color: var(--product-content-bg, #fff);
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

/* A1 / A3 装修风格：贴合式双层导航（对齐 VortMall 商品详情页） */
.product-nav--decorated {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 999;
    width: 100%;

    .product-nav-container.decorated {
        position: static;
        display: flex;
        align-items: center;
        justify-content: flex-start;
        background-color: var(--product-content-bg, #fff);
        box-sizing: border-box;
        height: calc(77rpx + var(--product-nav-safe-top, 0px));
        padding: var(--product-nav-safe-top, 0px) calc(28rpx + var(--product-nav-safe-right, 0px)) 0 28rpx;

        .nav-left {
            width: 56rpx;
            height: 56rpx;
            display: flex;
            align-items: center;
            margin-right: 6rpx;

            .icon-fanhui {
                font-size: 30rpx;
                color: #1a1a1a;
            }
        }

        .product-nav-middle.decorated {
            flex: 1;
            display: flex;
            align-items: baseline;
            justify-content: flex-start;
            gap: 12px;
            font-size: 12px;
            padding: 0;
            width: auto;
            background-color: transparent;

            .middle-title {
                font-size: 28rpx;
                font-weight: 700;
                color: #1a1a1a;
            }

            .module-list {
                display: inline-flex;
                flex: 1;
                gap: 12px;
                color: #000;
                font-size: 12px;
                overflow-x: auto;
                scrollbar-width: none;
                align-items: center;

                .module-text {
                    line-height: 64rpx;
                    color: #1a1a1a;
                    position: relative;
                    white-space: nowrap;

                    &.active {
                        &::after {
                            content: "";
                            position: absolute;
                            width: 24px;
                            height: 2px;
                            bottom: 0;
                            left: 50%;
                            transform: translateX(-50%);
                            background-color: #1a1a1a;
                            z-index: 10;
                        }
                    }
                }
            }
        }

        .nav-collect {
            display: flex;
            align-items: center;
            margin-right: 20rpx;

            .icon-shoucang2 {
                font-size: 36rpx;
                color: #1a1a1a;
            }

            .icon-yishoucang1 {
                font-size: 36rpx;
                color: #ff0f23;
            }
        }

        .nav-more {
            position: relative;

            .icon-gengduo2 {
                font-size: 44rpx;
                color: #1a1a1a;
            }

            .shade-box {
                position: fixed;
                top: 0;
                left: 0;
                z-index: 1001;
                width: 100vw;
                height: 100vh;
            }

            .more-menu-box {
                position: absolute;
                top: 77rpx;
                right: 0;
                z-index: 1002;
                min-width: 143rpx;
                min-height: 28rpx;
                max-width: 100vw;
                max-height: 100vh;
                overflow-x: hidden;
                overflow-y: auto;
                color: rgba(0, 0, 0, 0.87);
                background-color: #fff;
                border-radius: 7rpx;
                box-shadow: rgba(0, 0, 0, 0.08) 0 7rpx 49rpx 0;
                opacity: 0;
                transform: scale(0);
                transform-origin: top right;
                transition:
                    opacity 279ms cubic-bezier(0.4, 0, 0.2, 1),
                    transform 186ms cubic-bezier(0.4, 0, 0.2, 1);

                &.showMenu {
                    opacity: 1;
                    transform: scale(1);
                }

                .more-menu {
                    min-width: 143rpx;
                    padding: 0 21rpx;

                    .menu-item {
                        display: flex;
                        align-items: center;
                        height: 84rpx;
                        gap: 14rpx;
                        border-bottom: 2rpx solid #f0f1f2;
                        font-size: 25rpx;
                        white-space: nowrap;

                        &:last-child {
                            border-bottom: none;
                        }

                        .item-icon {
                            font-size: 35rpx;
                        }
                    }
                }
            }
        }
    }
}

/* A3 与跨境项目一致：两层头部均在文档流内 sticky，避免 fixed 导航在测高失败时覆盖商城头部。 */
.product-nav--decorated.product-nav--overseas {
    position: sticky;
}

.product-nav-placeholder {
    width: 100%;
    height: var(--product-nav-placeholder-height, 77rpx);
}
</style>
