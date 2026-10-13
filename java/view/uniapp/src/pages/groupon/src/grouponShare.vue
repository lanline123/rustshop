<template>
    <view class="share">
        <tig-popup v-model:show="show" position="bottom">
            <view class="share-title"> {{ $t("分享") }} </view>
            <view class="share-content">
                <view v-if="showShareFriend" class="share-item" @click="handleShareFriend">
                    <text class="iconfont-h5 icon-haoyou" />
                    <!-- <image :src="staticResource('product/share.png')" class="img"></image> -->
                    <text class="share-item-text">{{ $t("分享给好友") }}</text>
                </view>
                <!-- #ifdef H5 -->
                <view class="share-item" @click="handleCopy">
                    <text class="iconfont-h5 icon-link" />
                    <!-- <image src="@/static/images/product/link.png" class="img"></image> -->
                    <text class="share-item-text">{{ $t("复制链接") }}</text>
                </view>
                <!-- #endif -->
                <view class="share-item" @click="handlePoster">
                    <text class="iconfont-h5 icon-tupian" />
                    <!-- <image :src="staticResource('product/link.png')" class="img"></image> -->
                    <text class="share-item-text">{{ $t("生成商品海报") }}</text>
                </view>
            </view>
        </tig-popup>
        <poster
            v-if="showPoster"
            v-model="showPoster"
            :product-id="productId"
            :product-info="data"
            :product-price="productPrice"
            :groupon-promotion-id="grouponPromotionId"
            :groupon-promotion-record-id="grouponPromotionRecordId"
        />
        <guide v-model="showGuide" />
    </view>
</template>

<script setup lang="ts">
/**
 * 拼团分享组件
 * - 生成拼团分享海报
 * - 复制分享链接
 * - 微信分享引导
 */
import { computed, ref, watch } from "vue";
import type { ProductItem } from "@/types/product/product";
import poster from "@/pages/groupon/src/poster.vue";
import guide from "@/pages/groupon/src/guide.vue";
import { copy } from "@/utils";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const configStore = useConfigStore();
const userStore = useUserStore();
const props = defineProps<{
    modelValue: boolean;
    productId: number;
    productInfo: ProductItem;
    productPrice: string | number;
    grouponPromotionId?: number;
    grouponPromotionRecordId: number;
}>();
const emits = defineEmits<{
    (e: "update:modelValue", value: boolean): void;
}>();

const data = ref<ProductItem>(props.productInfo);
watch(
    () => props.productInfo,
    (newValue) => {
        data.value = newValue;
    }
);

const show = computed({
    get() {
        return props.modelValue;
    },
    set(value) {
        emits("update:modelValue", value);
    }
});

const showPoster = ref(false);
const handlePoster = () => {
    emits("update:modelValue", false);
    if (configStore.isIdentity === 1) {
        if (Object.keys(userStore.userInfo).length === 0) {
            let str = t("登录后查看");
            uni.showToast({
                title: t(str),
                icon: "none"
            });
            return;
        }
        if (userStore.userInfo.isCompanyAuth !== 1) {
            let str = t("实名后查看");
            uni.showToast({
                title: t(str),
                icon: "none"
            });
            return;
        }
    }
    showPoster.value = true;
};
const handleCopy = () => {
    emits("update:modelValue", false);
    // #ifdef H5
    h5Copy();
    // #endif
};

const h5Copy = () => {
    const url = `${location.origin}/mobile/pages/product/groupBuying?grouponPromotionRecordId=${props.grouponPromotionRecordId}`;
    copy(url, () => {
        uni.showToast({
            title: t("复制成功"),
            icon: "none"
        });
    });
};

const showShareFriend = computed(() => {
    if (configStore.XClientType === "miniProgram" || configStore.XClientType === "wechat") {
        return true;
    }
    return false;
});

const showGuide = ref(false);
const handleShareFriend = () => {
    emits("update:modelValue", false);
    showGuide.value = true;
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
