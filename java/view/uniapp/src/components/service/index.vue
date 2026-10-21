<template>
    <view @click="handleClick">
        <slot />
    </view>
</template>

<script setup lang="ts">
// #ifdef MP-WEIXIN
defineOptions({
    options: { virtualHost: true }
});
// #endif
import { useUserStore } from "@/store/user";
import { useI18n } from "vue-i18n";
import { handleLogin } from "@/utils/handleLogin";
import { isPro, copy } from "@/utils";
const userStore = useUserStore();
const { t } = useI18n();
const props = defineProps({
    productId: {
        type: Number,
        default: 0
    },
    shopId: {
        type: Number,
        default: 0
    },
    orderId: {
        type: Number,
        default: 0
    },
    phone: {
        type: String,
        default: ""
    }
});

const handleClick = () => {
    console.log(props);
    const token = uni.getStorageSync("token");
    if (!token) return handleLogin();
    let url = `/pages/im/index?token=${token}`;
    let type = userStore.serviceConfig.serviceType;
    let customUrl = userStore.serviceConfig.url;

    if (type === 0) {
        uni.showToast({
            title: t("暂无客服"),
            icon: "none"
        });
    } else if (type === 4) {
        if (props.productId) {
            url += `&productId=${props.productId}`;
        }
        if (props.shopId >= 0) {
            url += `&shopId=${props.shopId}`;
        }
        if (props.orderId) {
            url += `&orderId=${props.orderId}`;
        }
        uni.navigateTo({
            url
        });
    } else {
        if (!isPro() && props.shopId > 0) {
            if (!props.phone) {
                return uni.showToast({
                    title: t("暂无客服"),
                    icon: "none"
                });
            }
            return uni.showModal({
                title: t("客服电话"),
                content: props.phone,
                confirmText: t("复制"),
                success: function (res) {
                    if (res.confirm) {
                        copy(props.phone, () => {
                            uni.showToast({
                                title: t("复制成功"),
                                icon: "none"
                            });
                        });
                    }
                }
            });
        }
        // #ifdef H5
        window.open(customUrl);
        // #endif
        // #ifdef MP-WEIXIN
        if (type === 2 || type === 1) {
            //企业微信客服
            wx.openCustomerServiceChat({
                extInfo: { url: customUrl },
                corpId: userStore.serviceConfig.corpId,
                success(res: any) {},
                fail(res: any) {
                    uni.showToast({
                        title: res.errMsg,
                        duration: 1500,
                        icon: "none"
                    });
                }
            });
            return;
        }
        // #endif

        // #ifdef MP-WEIXIN || APP-PLUS || MP-ALIPAY
        uni.navigateTo({
            url: `/pages/webviewPage/index?url=${encodeURIComponent(customUrl)}`
        });
        // #endif
    }
};

defineExpose({
    handleClick
});
</script>

<style lang="scss" scoped></style>
