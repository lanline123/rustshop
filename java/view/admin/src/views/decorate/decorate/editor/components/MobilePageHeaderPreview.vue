<template>
    <div v-if="variant === 'overseas'" class="dec-mobile-page-header dec-mobile-page-header--overseas" :style="headerStyle">
        <div class="dec-mobile-page-header__overseas-left">
            <i class="iconfont-h5 icon-caidan"></i>
            <img class="dec-mobile-page-header__logo" :src="logo" alt="Tigshop" />
        </div>
        <div class="dec-mobile-page-header__overseas-right">
            <i class="iconfont-h5 icon-xinjianwenbenwendang"></i>
            <span class="dec-mobile-page-header__cart">
                <i class="iconfont-h5 icon-gouwuche4"></i>
                <span v-if="cartCount" class="dec-mobile-page-header__cart-badge">{{ cartCount }}</span>
            </span>
        </div>
    </div>

    <div v-else class="dec-mobile-page-header dec-mobile-page-header--domestic" :style="headerStyle">
        <i class="iconfont-h5 icon-fanhui dec-mobile-page-header__back"></i>
        <div class="dec-mobile-page-header__title">{{ title }}</div>
    </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

type HeaderVariant = "domestic" | "overseas";

const props = withDefaults(
    defineProps<{
        variant?: HeaderVariant;
        title?: string;
        backgroundColor?: string;
        cartCount?: number | string;
    }>(),
    {
        variant: "domestic",
        title: "",
        backgroundColor: "#ffffff",
        cartCount: ""
    }
);

const logo = "https://oss.tigshop.com/img/gallery/202506/1749453162yrntnPb4FvQcg9Wjco.png";
const headerStyle = computed(() => ({ backgroundColor: props.backgroundColor || "#ffffff" }));
</script>

<style lang="scss" scoped>
.dec-mobile-page-header {
    width: 100%;
    color: #1a1a1a;
    box-sizing: border-box;

    .iconfont-h5 {
        color: #1a1a1a;
        font-style: normal;
        line-height: 1;
    }

    &--domestic {
        position: relative;
        display: flex;
        align-items: center;
        height: 50px;
        padding: 0;
    }

    &__back {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 50px;
        height: 50px;
        font-size: 0;

        &::before {
            width: 11px;
            height: 11px;
            border-bottom: 1.5px solid currentColor;
            border-left: 1.5px solid currentColor;
            content: "";
            transform: rotate(45deg);
        }
    }

    &__title {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        overflow: hidden;
        max-width: 70%;
        font-size: 16px;
        font-weight: 500;
        line-height: 1.4;
        white-space: nowrap;
        text-overflow: ellipsis;
    }

    &--overseas {
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 39px;
        padding: 0 11px;
    }

    &__overseas-left,
    &__overseas-right {
        display: flex;
        align-items: center;
    }

    &__overseas-left {
        gap: 8px;

        .icon-caidan {
            font-size: 21px;
        }
    }

    &__logo {
        display: block;
        width: 77px;
        height: auto;
    }

    &__overseas-right {
        gap: 14px;

        .icon-xinjianwenbenwendang {
            font-size: 19px;
        }

        .icon-gouwuche4 {
            font-size: 21px;
        }
    }

    &__cart {
        position: relative;
        display: inline-flex;
        align-items: center;
    }

    &__cart-badge {
        position: absolute;
        top: -8px;
        right: -8px;
        display: flex;
        align-items: center;
        justify-content: center;
        min-width: 14px;
        height: 14px;
        padding: 0 3px;
        border-radius: 8px;
        background: #333;
        color: #fff;
        font-size: 8px;
        line-height: 1;
    }
}
</style>
