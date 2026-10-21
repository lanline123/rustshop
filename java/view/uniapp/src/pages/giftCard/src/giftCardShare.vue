<template>
    <view class="share">
        <tig-popup v-model:show="show" position="bottom">
            <view class="share-title"> {{ $t("分享") }} </view>
            <view class="share-content">
                <view class="share-item" @click="handleCopy">
                    <text class="iconfont-h5 icon-link" />
                    <!-- <image src="@/static/images/product/link.png" class="img"></image> -->
                    <text class="share-item-text">{{ $t("复制链接") }}</text>
                </view>
            </view>
        </tig-popup>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { copy } from "@/utils";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = defineProps<{
    modelValue: boolean;
    linkUrl: string;
}>();
const emits = defineEmits<{
    (e: "update:modelValue", value: boolean): void;
}>();

const link = computed(() => {
    return props.linkUrl;
});

const show = computed({
    get() {
        return props.modelValue;
    },
    set(value) {
        emits("update:modelValue", value);
    }
});

const handleCopy = () => {
    emits("update:modelValue", false);
    copy(link.value, () => {
        uni.showToast({
            title: t("复制成功"),
            icon: "none"
        });
    });
};
</script>

<style lang="scss" scoped>
.share-title {
    height: 100rpx;
    line-height: 100rpx;
    text-align: center;
    font-weight: bold;
}

.share-content {
    display: flex;
    padding-bottom: 30rpx;

    .share-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        &:not(:last-child) {
            border-right: 1rpx solid #ddd;
        }

        // .img {
        //     width: 70rpx;
        //     height: 70rpx;
        // }

        // .share-item-text {
        //     color: #2f2f2f;
        //     padding-top: 15rpx;
        // }

        .iconfont,
        .iconfont-h5 {
            font-size: 70rpx;
            color: #777;
        }

        .iconfont-h5 {
            font-size: 60rpx;
            padding-bottom: 15rpx;
        }
    }
}
</style>
