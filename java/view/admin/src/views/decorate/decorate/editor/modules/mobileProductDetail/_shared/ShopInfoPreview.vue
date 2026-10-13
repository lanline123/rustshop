<template>
    <div class="pd-shop-preview" :class="`pd-shop-preview--${variant}`">
        <div class="pd-shop-preview__title">
            <span class="pd-shop-preview__logo">
                <img
                    class="pd-shop-preview__logo-image"
                    src="https://oss.tigshop.com/img/gallery/202506/1749453162yrntnPb4FvQcg9Wjco.png"
                    alt="Tigshop"
                />
            </span>
            <span class="pd-shop-preview__name">{{ $t("商城自营") }}</span>
        </div>
        <div class="pd-shop-preview__actions">
            <span>{{ $t("收藏店铺") }}</span>
            <span><i class="iconfont-h5 icon-dianpu"></i>{{ $t("进店逛逛") }}</span>
        </div>
    </div>
</template>

<script setup lang="ts">
withDefaults(
    defineProps<{
        variant?: "vortmall" | "o2o" | "overseas";
    }>(),
    {
        variant: "vortmall"
    }
);
</script>

<style lang="scss" scoped>
.pd-shop-preview {
    padding: 12px 10px;
    background: var(--dec-pd-content-bg, var(--dec-pd2-content-bg, var(--dec-pd3-content-bg, #fff)));

    &--vortmall {
        margin-top: 10px;
    }

    &--overseas {
        margin-top: 8px;
    }

    &--o2o {
        margin: 10px 0;
        border-radius: 10px;
    }

    &__title {
        display: flex;
        align-items: center;
        min-width: 0;
    }

    &__logo {
        display: flex;
        flex: 0 0 31px;
        align-items: center;
        justify-content: center;
        width: 31px;
        height: 31px;
        margin-right: 10px;
        overflow: hidden;
        border-radius: 50%;

        img {
            display: block;
            width: 100%;
            height: 100%;
            object-fit: contain;
        }
    }

    &__name {
        overflow: hidden;
        font-size: 14px;
        font-weight: 600;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    &__actions {
        display: flex;
        gap: 16px;
        margin-top: 12px;

        span {
            display: flex;
            flex: 1;
            align-items: center;
            justify-content: center;
            height: 32px;
            border: 1px solid #d7d7d7;
            border-radius: 5px;
            color: #686868;
            font-size: 12px;
        }

        i {
            margin-right: 4px;
        }
    }
}

.pd-shop-preview__logo-image {
    object-fit: contain;
}
</style>
