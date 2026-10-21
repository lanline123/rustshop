<template>
    <view class="cart-heard">
        <navbarContent fixed :placeholder="true" :need-status-bar="needStatusBar" :offset-top="offsetTop" :z-index="400" background="#fff" height="79.6rpx">
            <view class="cart-heard-content">
                <view class="cart-heard-left">
                    <view v-if="showBack" class="cart-heard-back iconfont icon-houtui" @click="handleBack" />
                    <view class="cart-heard-text">{{ $t("购物车") }}</view>
                </view>
                <view v-if="showEditButton" class="cart-heard-edit" :style="{ color: themeColor }" @click="handleEdit">
                    {{ isEditMode ? $t("完成") : $t("编辑") }}
                </view>
            </view>
        </navbarContent>
    </view>
</template>

<script setup lang="ts">
/**
 * 购物车顶栏
 * - 标题 + 右侧「编辑/完成」入口
 * - 可选返回键（cartJump 等非 tab 页）
 * - 外贸皮肤下可顶在 mainHeader 下方（offsetTop + 关闭自身状态栏）
 */
import navbarContent from "@/components/navbarContent/index.vue";

const props = defineProps({
    /** 是否显示编辑按钮 */
    showEditButton: {
        type: Boolean,
        default: true
    },
    /** 是否处于编辑模式 */
    isEditMode: {
        type: Boolean,
        default: false
    },
    /** 编辑按钮主题色 */
    themeColor: {
        type: String,
        default: "var(--general)"
    },
    /** 是否显示返回键（非 tab 购物车页需要） */
    showBack: {
        type: Boolean,
        default: false
    },
    /** 是否自定义返回事件 */
    customBack: {
        type: Boolean,
        default: false
    },
    /** 是否需要状态栏占位；叠在 mainHeader 下时关 */
    needStatusBar: {
        type: Boolean,
        default: true
    },
    /** fixed 顶偏移（px），通常为 mainHeader 实测高度 */
    offsetTop: {
        type: Number,
        default: 0
    }
});

const emit = defineEmits<{
    (e: "edit"): void;
    (e: "back"): void;
}>();

const handleEdit = () => {
    emit("edit");
};

let backLock = false;
const handleBack = () => {
    if (backLock) return;
    backLock = true;
    setTimeout(() => {
        backLock = false;
    }, 400);
    if (props.customBack) {
        emit("back");
        return;
    }
    uni.navigateBack({
        fail: () => {
            uni.switchTab({ url: "/pages/index/index" });
        }
    });
};
</script>

<style lang="scss" scoped>
.cart-heard {
    color: #1a1a1a;

    // 用 flex 而非 absolute：作为 navbarContent .navbar 的 flex 子元素，
    // 才能正确接收小程序端注入的 paddingRight（胶囊按钮宽度），避让胶囊
    .cart-heard-content {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        height: 100%;
        padding: 0 28rpx;
        box-sizing: border-box;
    }

    .cart-heard-left {
        display: flex;
        align-items: center;
        min-width: 0;
        flex: 1;
    }

    .cart-heard-back {
        flex-shrink: 0;
        font-size: 36rpx;
        line-height: 1;
        margin-right: 12rpx;
        padding: 8rpx 8rpx 8rpx 0;
    }

    .cart-heard-text {
        font-size: 32rpx;
        font-weight: 700;
        line-height: 42rpx;
        text-align: left;
    }

    .cart-heard-edit {
        flex-shrink: 0;
        font-size: 28rpx;
        font-weight: 400;
        line-height: 36rpx;
        margin-left: 20rpx;
        cursor: pointer;
    }
}
</style>
