<template>
    <tig-layout :title="$t('发票详情')">
        <view class="invoiceInfo">
            <view class="invoiceInfo-status">
                <view class="">{{ $t("审核状态") }}：</view>
                <view class="special-text">{{ formatText }}</view>
            </view>
            <view class="invoiceInfo-content">
                <view class="invoiceInfo-content-item">
                    <view class="item-label">{{ $t("开票金额") }}</view>
                    <view class="item-value"> <format-price :show-text="false" :is-bottom="false" :price-data="invoiceData.amount" /></view>
                </view>
                <view class="invoiceInfo-content-item">
                    <view class="item-label">{{ $t("订单编号") }}</view>
                    <view class="item-value"> {{ invoiceData.orderSn }}</view>
                </view>
                <block v-if="invoiceData.invoiceType == 1">
                    <view class="invoiceInfo-content-item">
                        <view class="item-label">{{ $t("发票抬头") }}</view>
                        <view class="item-value">{{ invoiceData.titleType == 1 ? $t("个人") : $t("企业") }}</view>
                    </view>
                    <view class="invoiceInfo-content-item">
                        <view class="item-label">{{ invoiceData.titleType == 1 ? $t("个人姓名") : $t("企业名称") }}</view>
                        <view class="item-value">{{ invoiceData.companyName }}</view>
                    </view>
                    <template v-if="invoiceData.applyReply && invoiceData.status === 2">
                        <view class="invoiceInfo-content-item">
                            <view class="item-label">{{ $t("驳回原因") }}</view>
                            <view class="item-value">{{ invoiceData.applyReply }}</view>
                        </view>
                    </template>
                    <block v-if="invoiceData.titleType == 2">
                        <view class="invoiceInfo-content-item">
                            <view class="item-label">{{ $t("单位名称") }}</view>
                            <view class="item-value">{{ invoiceData.companyName }}</view>
                        </view>
                        <view class="invoiceInfo-content-item">
                            <view class="item-label">{{ $t("纳税人识号") }}</view>
                            <view class="item-value">{{ invoiceData.companyCode }}</view>
                        </view>
                        <view class="invoiceInfo-content-item">
                            <view class="item-label">{{ $t("单位地址") }}</view>
                            <view class="item-value">{{ invoiceData.companyAddress }}</view>
                        </view>
                        <view class="invoiceInfo-content-item">
                            <view class="item-label">{{ $t("单位电话") }}</view>
                            <view class="item-value">{{ invoiceData.companyPhone }}</view>
                        </view>
                        <view class="invoiceInfo-content-item">
                            <view class="item-label">{{ $t("开户银行") }}</view>
                            <view class="item-value">{{ invoiceData.companyBank }}</view>
                        </view>
                        <view class="invoiceInfo-content-item">
                            <view class="item-label">{{ $t("银行账户") }}</view>
                            <view class="item-value">{{ invoiceData.companyAccount }}</view>
                        </view>
                    </block>
                </block>
                <block v-if="invoiceData.invoiceType == 2">
                    <view class="invoiceInfo-content-item">
                        <view class="item-label">{{ $t("单位名称") }}</view>
                        <view class="item-value">
                            {{ invoiceData.companyName }}
                        </view>
                    </view>
                    <view class="invoiceInfo-content-item">
                        <view class="item-label">{{ $t("纳税识别码") }}</view>
                        <view class="item-value">
                            {{ invoiceData.companyCode }}
                        </view>
                    </view>
                    <view class="invoiceInfo-content-item">
                        <view class="item-label">{{ $t("注册地址") }}</view>
                        <view class="item-value">
                            {{ invoiceData.companyAddress }}
                        </view>
                    </view>
                    <view class="invoiceInfo-content-item">
                        <view class="item-label">{{ $t("注册电话") }}</view>
                        <view class="item-value">
                            {{ invoiceData.companyPhone }}
                        </view>
                    </view>
                    <view class="invoiceInfo-content-item">
                        <view class="item-label">{{ $t("开户银行") }}</view>
                        <view class="item-value">
                            {{ invoiceData.companyBank }}
                        </view>
                    </view>
                    <view class="invoiceInfo-content-item">
                        <view class="item-label">{{ $t("银行账户") }}</view>
                        <view class="item-value">
                            {{ invoiceData.companyAccount }}
                        </view>
                    </view>
                    <block v-if="filePicList.length && invoiceData?.status == 1">
                        <view class="invoiceInfo-content-item">
                            <view class="item-label">{{ $t("发票预览") }}</view>
                            <view class="image-box">
                                <view class="image" @click="toImages">
                                    <!-- <tig-image :src="filePicList[0].picThumb" mode="widthFill" /> -->
                                    <view class="image-bg" :style="{ backgroundImage: `url(${imageFormat(filePicList[0].picThumb)})` }" />
                                    <view class="img-num">{{ $t("共{0}张", [fileListNum]) }}</view>
                                </view>
                                <view class="img-tip">{{ $t("点击预览发票") }}</view>
                            </view>
                        </view>
                    </block>
                </block>
                <block v-if="invoiceData.invoiceType == 1">
                    <block v-if="invoiceData.invoicingTime">
                        <view class="invoiceInfo-content-item">
                            <view class="item-label">{{ $t("申请时间") }}</view>
                            <view class="item-value">{{ invoiceData.invoicingTime }}</view>
                        </view>
                    </block>
                    <block v-if="invoiceData.addTime">
                        <view class="invoiceInfo-content-item">
                            <view class="item-label">{{ $t("开票时间") }}</view>
                            <view class="item-value">{{ invoiceData.addTime }}</view>
                        </view>
                    </block>
                    <block v-if="filePicList.length && invoiceData?.status == 1">
                        <view class="invoiceInfo-content-item">
                            <view class="item-label">{{ $t("发票预览") }}</view>
                            <view class="image-box">
                                <view class="image" @click="toImages">
                                    <!-- <tig-image :src="filePicList[0].picThumb" mode="widthFill" /> -->
                                    <view class="image-bg" :style="{ backgroundImage: `url(${imageFormat(filePicList[0].picThumb)})` }" />
                                    <view class="img-num">{{ $t("共{0}张", [fileListNum]) }}</view>
                                </view>
                                <view class="img-tip">{{ $t("点击预览发票") }}</view>
                            </view>
                        </view>
                    </block>
                </block>
            </view>

            <tig-fixed-placeholder background-color="#fff">
                <view class="btn-box">
                    <tig-button @click="onSubmit"> {{ $t("重新申请") }} </tig-button>
                </view>
            </tig-fixed-placeholder>
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 发票详情页
 * - 展示发票审核状态、开票金额、订单编号
 * - 显示发票抬头信息（普通/增值税专用）
 * - 已开票发票支持预览图片
 * - 提供重新申请入口
 */
import { onLoad } from "@dcloudio/uni-app";
import { computed, ref } from "vue";
import type { InvoiceAttachment, OrderListInvoiceData } from "@/types/user/order";
import { useI18n } from "vue-i18n";
import { imageFormat } from "@/utils/format";

const { t } = useI18n();

const invoiceData = ref<OrderListInvoiceData>({} as OrderListInvoiceData);
const filePicList = ref<InvoiceAttachment[]>([]);
const fileListNum = computed(() => {
    return filePicList.value.length;
});
onLoad((options) => {
    if (options && options.invoiceData) {
        invoiceData.value = JSON.parse(decodeURIComponent(options.invoiceData));
        if (invoiceData.value.invoiceAttachment.length) {
            filePicList.value = invoiceData.value.invoiceAttachment;
        }
    }
});
const formatText = computed(() => {
    switch (invoiceData.value.status) {
        case 0:
            return t("待审核");
        case 1:
            return t("通过");
        default:
            return t("失败/作废");
    }
});
const onSubmit = () => {
    uni.redirectTo({
        url: "/pages/user/invoiceManagement/applyInvoice?type=update&orderId=" + invoiceData.value.orderId
    });
};
const toImages = () => {
    const fileList = encodeURIComponent(JSON.stringify(filePicList.value));
    uni.navigateTo({
        url: "/pages/user/invoiceManagement/src/imageView?fileList=" + fileList
    });
};
</script>

<style lang="scss" scoped>
.btn-box {
    padding: 25rpx;
}
.invoiceInfo {
    padding: 20rpx;

    .invoiceInfo-status {
        display: flex;
        border: 1px solid #e5e5e5;
        background: #fefee3;
        padding: 20px 10px 20px 20px;
        margin-bottom: 20rpx;
    }

    .invoiceInfo-content {
        padding: 2rpx 20rpx;
        background-color: #fff;
        border-radius: 20rpx;

        .invoiceInfo-content-item {
            display: flex;
            border-bottom: 1px solid #f5f5f5;
            padding: 30rpx 0;

            &:last-child {
                border-bottom: none;
            }

            .item-label {
                padding-left: 10rpx;
                width: 160rpx;
                text-align: start;
                color: #999;
            }
            .item-value {
                margin-left: 10rpx;
            }
            .image {
                margin-left: 10rpx;
            }
        }
        .image-box {
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .image {
            position: relative;
            display: flex;
            flex-direction: column;
            align-items: center;
            width: 400rpx;
            height: 260rpx;
            border-radius: 10rpx;
            overflow: hidden;

            .image-bg {
                width: 100%;
                height: 100%;
                background-size: contain;
                background-position: center;
                background-repeat: no-repeat;
            }

            .img-num {
                position: absolute;
                bottom: 0;
                height: 40rpx;
                text-align: center;
                width: 100%;
                background-color: rgba(0, 0, 0, 0.5);
                z-index: 200;
                color: #fff;
                font-weight: normal;
            }
        }
        .img-tip {
            color: #999;
            font-weight: normal;
        }
    }
}

.special-text {
    color: var(--general);
}
</style>
