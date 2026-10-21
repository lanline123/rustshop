<template>
    <!-- 简单模式：只传 styleType，使用预设样式 -->
    <template v-if="!btnStyle">
        <a v-if="styleType !== 0" href="javascript:;" class="dec-buy-btn lyecs-buyBtn">
            <template v-if="styleType === 1">
                <span class="dec-buy-btn__icon iconfont-pc icon-gouwuche" :style="{ color: iconColor }"></span>
            </template>
            <template v-else-if="styleType === 2">
                <span class="dec-buy-btn__icon iconfont-pc icon-gouwuche1" :style="{ color: iconColor }"></span>
            </template>
            <template v-else-if="styleType === 3">
                <span class="dec-buy-btn__icon iconfont-pc icon-jia" :style="{ color: iconColor }"></span>
            </template>
            <template v-else-if="styleType === 4">
                <span class="dec-buy-btn__icon iconfont-pc icon-jia1" :style="{ color: iconColor }"></span>
            </template>
            <template v-else-if="styleType === 5">
                <span class="dec-buy-btn__text" :style="filledTextStyle">购买</span>
            </template>
            <template v-else-if="styleType === 6">
                <span class="dec-buy-btn__text dec-buy-btn__text--outline" :style="outlineTextStyle">购买</span>
            </template>
            <template v-else-if="styleType === 7">
                <span class="dec-buy-btn__text dec-buy-btn__text--rush" :style="filledTextStyle">马上抢</span>
            </template>
            <template v-else-if="styleType === 8">
                <span class="dec-buy-btn__text dec-buy-btn__text--rush2" :style="outlineTextStyle">马上抢</span>
            </template>
        </a>
    </template>
    <!-- 完整模式：传 btnStyle 对象，使用自定义样式 -->
    <template v-else>
        <a
            v-if="btnStyle.showBtn === 1 && btnStyle.btnType !== 0"
            href="javascript:;"
            class="dec-buy-btn dec-buy-btn--custom lyecs-buyBtn"
            :style="customBtnStyle"
        >
            <!-- 图标 -->
            <template v-if="btnStyle.btnType === 1">
                <span class="dec-buy-btn__icon iconfont-pc icon-gouwuche" :style="{ color: btnStyle.iconColor }"></span>
            </template>
            <template v-else-if="btnStyle.btnType === 2">
                <span class="dec-buy-btn__icon iconfont-pc icon-gouwuche1" :style="{ color: btnStyle.iconColor }"></span>
            </template>
            <template v-else-if="btnStyle.btnType === 3">
                <span class="dec-buy-btn__icon iconfont-pc icon-jia" :style="{ color: btnStyle.iconColor }"></span>
            </template>
            <template v-else-if="btnStyle.btnType === 4">
                <span class="dec-buy-btn__icon iconfont-pc icon-jia1" :style="{ color: btnStyle.iconColor }"></span>
            </template>
            <!-- 文字 -->
            <span v-if="btnStyle.text" class="dec-buy-btn__text-custom" :style="{ color: btnStyle.textColor }">
                {{ btnStyle.text }}
            </span>
        </a>
    </template>
</template>

<script setup lang="ts">
/**
 * 装修模块购买按钮（移植自 Vort-Admin _shared/buy-button/ModuleBuyButton）
 * vort-icon 图标替换为 Tigshop 装修画布通用的 iconfont-pc 字体图标
 */
import { computed } from "vue";

defineOptions({ name: "ModuleBuyButton" });

/**
 * 购买按钮样式配置
 */
interface BuyBtnStyle {
    showBtn?: number;
    btnType?: number;
    text?: string;
    textColor?: string;
    iconColor?: string;
    gradientType?: "upDown" | "leftRight" | "diagonal" | "purity";
    gradientColorA?: string;
    gradientColorB?: string;
    radius?: number;
    paddingTopBottom?: number;
    paddingLeftRight?: number;
}

const props = withDefaults(
    defineProps<{
        /** 简单模式：预设样式类型 */
        styleType?: number;
        /** 简单模式：图标颜色 */
        iconColor?: string;
        /** 完整模式：自定义样式配置 */
        btnStyle?: BuyBtnStyle;
    }>(),
    {
        styleType: 0,
        iconColor: "#f23030",
        btnStyle: undefined
    }
);

const filledTextStyle = computed(() => ({
    background: props.iconColor,
    borderColor: props.iconColor
}));

const outlineTextStyle = computed(() => ({
    borderColor: props.iconColor,
    color: props.iconColor
}));

// 渐变方向映射
const gradientDirectionMap: Record<string, string> = {
    upDown: "to bottom",
    leftRight: "to right",
    diagonal: "135deg",
    purity: "to right" // 纯色时使用同一颜色
};

/**
 * 计算自定义按钮样式
 */
const customBtnStyle = computed(() => {
    if (!props.btnStyle) return {};

    const style = props.btnStyle;
    const gradientDir = gradientDirectionMap[style.gradientType || "leftRight"];
    const colorA = style.gradientColorA || "#72B76C";
    const colorB = style.gradientType === "purity" ? colorA : style.gradientColorB || "#72B76C";

    return {
        background: `linear-gradient(${gradientDir}, ${colorA}, ${colorB})`,
        borderRadius: `${style.radius ?? 15}px`,
        padding: `${style.paddingTopBottom ?? 3}px ${style.paddingLeftRight ?? 5}px`
    };
});
</script>

<style lang="less" scoped>
.dec-buy-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    text-decoration: none;
    color: #f23030;

    &__icon {
        color: #f23030;
        font-size: 18px;
        margin-right: 5px;
        line-height: 1;
    }

    &__text {
        padding: 4px 12px;
        background: linear-gradient(135deg, #ff6b6b 0%, #f23030 100%);
        color: #fff;
        font-size: 12px;
        border-radius: 12px;

        &--outline {
            background: transparent;
            border: 1px solid #f23030;
            color: #f23030;
        }

        &--rush,
        &--rush2 {
            background: linear-gradient(135deg, #ff9500 0%, #ff6b00 100%);
        }

        &--rush2 {
            background: transparent;
            border: 1px solid #ff6b00;
            color: #ff6b00;
        }
    }

    // 自定义样式模式
    &--custom {
        color: inherit;

        .dec-buy-btn__icon {
            color: inherit;
            margin-right: 3px;
        }
    }

    &__text-custom {
        font-size: 12px;
        white-space: nowrap;
    }
}
</style>
