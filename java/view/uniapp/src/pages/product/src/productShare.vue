<template>
    <view class="share">
        <tig-popup v-model:show="show" position="bottom">
            <view class="share-title"> {{ $t("分享") }} </view>
            <view class="share-content">
                <view v-if="showShareFriend" class="share-item" @click="handleShareFriend">
                    <text class="iconfont-h5 icon-haoyou" />
                    <!-- <image src="@/static/images/product/share.png" class="img"></image> -->
                    <text class="share-item-text">{{ $t("分享给好友") }}</text>
                </view>
                <!-- #ifdef H5 || APP-PLUS -->
                <view class="share-item" @click="handleCopy">
                    <text class="iconfont-h5 icon-link" />
                    <!-- <image src="@/static/images/product/link.png" class="img"></image> -->
                    <text class="share-item-text">{{ $t("复制链接") }}</text>
                </view>
                <!-- #endif -->
                <view class="share-item" @click="handlePoster">
                    <text class="iconfont-h5 icon-tupian" />
                    <!-- <image src="@/static/images/product/poster.png" class="img"></image> -->
                    <text class="share-item-text">{{ $t("生成商品海报") }}</text>
                </view>
            </view>
        </tig-popup>
        <poster v-if="showPoster" v-model="showPoster" :product-id="productId" :product-info="data" :product-price="productPrice" />
        <guide v-model="showGuide" />
    </view>
</template>

<script setup lang="ts">
/**
 * 商品分享组件
 * - 分享到微信好友、朋友圈
 * - 生成商品海报图片
 * - 复制商品链接
 */
import { computed, ref, watch } from "vue";
import type { ProductItem } from "@/types/product/product";
import poster from "@/components/product/poster.vue";
import guide from "@/components/product/guide.vue";
import { copy, appendQueryParam } from "@/utils";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";
import { getCurrentSalesmanId } from "@/utils/salesman";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const configStore = useConfigStore();
const userStore = useUserStore();
const props = defineProps<{
    modelValue: boolean;
    productId: number;
    productInfo: ProductItem;
    productPrice: string | number;
    layoutStyle?: string;
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
        let str = t("登录后查看");
        if (Object.keys(userStore.userInfo).length === 0) {
            uni.showToast({
                title: t(str),
                icon: "none"
            });
            return;
        }
        if (!userStore.userInfo.isCompanyAuth) {
            str = t("实名后查看");
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
    // #ifdef H5 || APP-PLUS
    h5Copy();
    // #endif
};

/**
 * 若链接未携带 shopId，则补 shopId=0 代表自营；已有 shopId 时保持不变
 */
const ensureShopId = (url: string): string => {
    if (!url) return url;
    return /[?&]shopId=/.test(url) ? url : appendQueryParam(url, "shopId", 0);
};

/**
 * 获取分享链接：分享人是分销员且商品参与分销时，携带分销员ID用于佣金归因
 */
const getShareUrl = (): string => {
    const isDistribution = props.productInfo.isDistribution;
    const salesmanId = getCurrentSalesmanId();
    const attachSalesman = isDistribution && salesmanId;
    let shareUrl = "";
    // #ifdef H5
    shareUrl = attachSalesman ? appendQueryParam(window.location.href, "salesmanId", salesmanId) : window.location.href;
    // #endif
    // #ifdef APP-PLUS
    const h5Domain = configStore.baseInfo.h5Domain || "";
    const baseUrl = `${h5Domain}/pages/product/index?id=${props.productId}`;
    shareUrl = attachSalesman ? appendQueryParam(baseUrl, "salesmanId", salesmanId) : baseUrl;
    // #endif
    return ensureShopId(shareUrl);
};

const h5Copy = () => {
    copy(getShareUrl(), () => {
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

const isOverseasLayout = computed(() => props.layoutStyle === "A3");

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
        padding: 0 10rpx;

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

        .fb-icon {
            width: 70rpx;
            height: 70rpx;
            margin-bottom: 15rpx;
            background-color: #777;
            mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1024 1024'%3E%3Cpath fill='%23fff' d='M725.333333 85.333333v170.666667h-85.333333c-29.44 0-42.666667 34.56-42.666667 64V426.666667h128v170.666666h-128v341.333334h-170.666666v-341.333334H298.666667v-170.666666h128V256a170.666667 170.666667 0 0 1 170.666666-170.666667h128z'/%3E%3C/svg%3E")
                no-repeat center / 56rpx 56rpx;
            -webkit-mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1024 1024'%3E%3Cpath fill='%23fff' d='M725.333333 85.333333v170.666667h-85.333333c-29.44 0-42.666667 34.56-42.666667 64V426.666667h128v170.666666h-128v341.333334h-170.666666v-341.333334H298.666667v-170.666666h128V256a170.666667 170.666667 0 0 1 170.666666-170.666667h128z'/%3E%3C/svg%3E")
                no-repeat center / 56rpx 56rpx;
        }
    }
}
</style>
