<template>
    <view class="container">
        <template v-if="loaded">
            <tig-layout>
                <view class="type-title">{{ $t("选择主体类型和经营类目") }}</view>
                <template v-if="configStore.personApplyEnabled === 1">
                    <view class="type-box" :class="{ active: type === 1 }" @click="type = 1">
                        <text class="iconfont-h5 icon-geren icon-size" /> {{ $t("个人") }}
                    </view>
                </template>

                <view class="type-box" :class="{ active: type === 2 }" @click="type = 2"
                    ><text class="iconfont-h5 icon-qiye icon-size" /> {{ $t("企业") }}
                </view>

                <tig-fixed-placeholder :border="false" background-color="#fff">
                    <view class="btn-box">
                        <tig-button class="btn" @click="handleAffirm"> {{ $t("确认") }} </tig-button>
                        <view class="agreement">
                            <tig-checkbox v-model:checked="isChecked" />
                            <text class="agreement-text" @click="isChecked = !isChecked">{{ $t("我已阅读并同意") }}</text>
                            <text class="agreement-link" @click="handleLink">《{{ $t("商户入驻协议") }}》</text>
                        </view>
                    </view>
                </tig-fixed-placeholder>
            </tig-layout>
        </template>
    </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { getMyMerchant } from "@/api/user/merchantEnter";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";
import { useConfigStore } from "@/store/config";

const { t } = useI18n();

const configStore = useConfigStore();

const type = ref(1);
const isReapply = ref(false);
const id = ref(0);
const isChecked = ref(false);
const handleAffirm = () => {
    if (!isChecked.value) {
        uni.showToast({
            title: t("请先阅读并同意协议"),
            icon: "none"
        });
        return;
    }

    if (id.value > 0 && isReapply.value) {
        uni.navigateTo({
            url: `/pages/user/merchantEnter/index?id=${id.value}&type=${type.value}&isReapply=${isReapply.value}`
        });
        return;
    }
    uni.navigateTo({
        url: `/pages/user/merchantEnter/index?type=${type.value}`
    });
};
const loaded = ref(false);
const _getMyMerchant = async () => {
    try {
        const result = await getMyMerchant();
        if (result && Object.keys(result).length > 0) {
            if (!isReapply.value) {
                uni.redirectTo({
                    url: `/pages/user/merchantEnter/index?id=${result.merchantApplyId}&type=${result.type}`
                });
            } else {
                id.value = result.merchantApplyId;
            }
        }
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    } finally {
        loaded.value = true;
    }
};

const handleLink = () => {
    uni.navigateTo({
        url: "/pages/user/merchantEnter/agreement"
    });
};

onLoad(async (options) => {
    if (options && options.type) {
        isReapply.value = true;
    }
});

onShow(async () => {
    _getMyMerchant();
});
</script>
<style lang="scss" scoped>
.container {
    padding: 50rpx;
}
.type-title {
    padding-top: 50rpx;
    font-weight: 600;
    font-size: 42rpx;
    line-height: 56rpx;
    display: block;
    padding-bottom: 63rpx;
    color: var(--general);
}
.type-box {
    width: 650rpx;
    height: 270rpx;
    background-color: rgba(255, 255, 255, 0.2); /* 白色背景，50%透明度 */
    border-radius: 20rpx;
    backdrop-filter: blur(4px);
    margin-bottom: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 34rpx;
    color: var(--general);
    box-shadow: 0rpx 8rpx 18rpx 0rpx rgba(0, 0, 0, 0.08);

    &.active {
        background-color: rgba(255, 255, 255, 1); /* 白色背景，50%透明度 */
    }
}

.icon-size {
    font-size: 40rpx;
    padding-right: 10rpx;
}

.btn-box {
    padding: 25rpx;
    position: relative;

    .btn {
        font-size: 28rpx;
    }

    .agreement {
        position: absolute;
        right: 0;
        bottom: 0;
        width: 100%;
        height: 100%;
        z-index: 1;
        width: 100%;
        top: -100%;
        display: flex;
        align-items: center;
        justify-content: center;

        .agreement-link {
            color: var(--general);
        }
    }
}
</style>
