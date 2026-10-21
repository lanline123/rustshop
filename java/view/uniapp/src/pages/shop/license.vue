<template>
    <tig-layout :title="$t('工商资质')" background="#fff">
        <view class="license-page">
            <view class="doc-title">{{ $t("网店经营者营业执照信息") }}</view>
            <view class="doc-notice">{{ $t("根据相关法律法规要求，网店经营相关资质信息公示如下：") }}</view>
            <view class="cert-box">
                <view class="watermark" aria-hidden="true">
                    <text v-for="n in 24" :key="n">{{ watermarkText }}</text>
                </view>
                <image v-if="licenseUrl" class="cert-img" :src="licenseUrl" mode="widthFix" />
                <view v-else-if="!loading" class="cert-empty">{{ $t("暂无营业执照") }}</view>
            </view>
            <view class="doc-notes">
                <view>{{ $t("注：以上营业执照信息来源于卖家自行申报或市监部门系统数据，具体以市监部门登记为准。") }}</view>
                <view>{{ $t("经营者需确保信息真实有效，平台也将定期核查，如与实际不符，为避免违规，请联系当地市监部门或平台客服更新。") }}</view>
            </view>
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * @description 营业执照公示页
 * - 店铺信息页、门店信息弹层点击资质后进入
 * - 只认 businessLicensePic，公示页展示真实执照原图
 */
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { getShopDetail } from "@/api/shop/shop";
import { getStoreDetail } from "@/api/store/store";
import { imageFormat } from "@/utils/format";
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const configStore = useConfigStore();
const loading = ref(true);
const shopId = ref(0);
const isStore = ref(false);
const licensePic = ref("");

const licenseUrl = computed(() => (licensePic.value ? imageFormat(licensePic.value) : ""));
const watermarkText = computed(() => `${configStore.baseInfo.shopTitle || ""}${t("营业执照信息公示专用")}`);

const loadLicense = async () => {
    loading.value = true;
    licensePic.value = "";
    if (!shopId.value) {
        loading.value = false;
        return;
    }
    try {
        const result: any = isStore.value
            ? await getStoreDetail({ shopId: shopId.value })
            : await getShopDetail(shopId.value);
        const pic = result?.businessLicensePic;
        licensePic.value = typeof pic === "string" && pic.trim() ? pic.trim() : "";
    } catch (error) {
        console.error(error);
    } finally {
        loading.value = false;
    }
};

onLoad((options) => {
    isStore.value = options?.source === "store";
    if (options?.shopId) {
        shopId.value = Number(options.shopId);
        loadLicense();
    } else {
        loading.value = false;
    }
});
</script>

<style lang="scss" scoped>
.license-page {
    min-height: 100%;
    padding: 32rpx 32rpx 60rpx;
    background: #fff;
    box-sizing: border-box;
}

.doc-title {
    font-size: 36rpx;
    font-weight: 700;
    line-height: 50rpx;
    color: #1a1a1a;
}

.doc-notice {
    margin-top: 16rpx;
    margin-bottom: 32rpx;
    font-size: 24rpx;
    line-height: 36rpx;
    color: #1a1a1a;
}

.cert-box {
    position: relative;
    overflow: hidden;
    padding: 24rpx;
    min-height: 280rpx;
}

.watermark {
    position: absolute;
    inset: -80rpx;
    z-index: 0;
    display: flex;
    flex-wrap: wrap;
    gap: 80rpx 40rpx;
    pointer-events: none;
    transform: rotate(-22deg);

    text {
        font-size: 22rpx;
        color: rgba(0, 0, 0, 0.06);
        white-space: nowrap;
        line-height: 1;
    }
}

.cert-img {
    position: relative;
    z-index: 1;
    display: block;
    width: 100%;
}

.cert-empty {
    position: relative;
    z-index: 1;
    padding: 80rpx 0;
    text-align: center;
    font-size: 26rpx;
    color: #999;
}

.doc-notes {
    margin-top: 40rpx;
    font-size: 22rpx;
    line-height: 36rpx;
    color: #999;

    view + view {
        margin-top: 12rpx;
    }
}
</style>
