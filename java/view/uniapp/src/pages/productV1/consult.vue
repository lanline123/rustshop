<template>
    <tig-layout :title="$t('商品咨询')">
        <view class="product-info flex">
            <view class="product-info-img">
                <image lazy-load class="product-img" :src="imageFormat(product.picThumb || '')" />
            </view>
            <view>
                <view class="product-info-title line2">{{ product.productName }}</view>
                <template v-if="product.marketPrice && Number(product.marketPrice) > 0">
                    <view class="product-info-price">
                        <format-price :is-bottom="false" :price-data="product.marketPrice" />
                    </view>
                </template>
            </view>
        </view>
        <view class="product-consult-card">
            <view v-for="(item, index) in consultationList" :key="index" class="consult-item">
                <view class="consult-item-tit flex justify-between">
                    <view class="time">{{ item.addTime }}</view>
                    <view class="name">{{ item.username }}</view>
                </view>
                <view class="consult-con line1">
                    <text>Q</text>
                    {{ item.content }}
                </view>
                <view v-if="item.reply" class="consult-con line1 reply">
                    <text>A</text>
                    {{ item.reply.content }}
                </view>
            </view>
        </view>
        <tig-fixed-placeholder background-color="#fff">
            <view class="btn-box">
                <tig-button class="btn" @click="customerService">
                    {{ $t("咨询客服") }}
                </tig-button>
            </view>
        </tig-fixed-placeholder>
        <tig-popup v-model:show="consultationLoading" :round="20" :showClose="false">
            <view class="popup-content">
                <view class="popup-header">
                    <view class="flex">
                        <view class="product-Slot">
                            <slot name="left">
                                <view class="iconfont icon-houtui" @click="handleIcon" />
                            </slot>
                        </view>
                        <view class="product-info-img">
                            <image lazy-load class="product-img" :src="imageFormat(product.picThumb || '')" />
                        </view>
                        <view class="product-info-title">{{ $t(product.productName) }}</view>
                    </view>
                    <view class="popup-footer">
                        <tig-button
                            class="btn"
                            :disabled="!consultationMessage.trim()"
                            :custom-style="{
                                background: consultationMessage.trim() ? '#C3A769' : '#e6e6e6',
                                color: consultationMessage.trim() ? '#fff' : '#f7f7f7',
                                borderColor: 'transparent'
                            }"
                            @click="submitConsultation"
                            >{{ $t("提交") }}</tig-button
                        >
                    </view>
                </view>
                <view class="popup-body">
                    <view class="popup-item">
                        <view class="popup-item-title">{{ $t("咨询内容") }}:</view>
                        <up-textarea
                            v-model="consultationMessage"
                            input-align="right"
                            height="120"
                            :placeholder="$t('您可以对商品包装、规格、支付、发票、退换货和物流配送类的问题咨询客服')"
                            border="none"
                            maxlength="200"
                            style="background: #f5f5f5"
                            :focus="textareaFocus"
                        />
                        <view class="popup-item-footer"> {{ consultationMessage.length }}/200 </view>
                    </view>
                </view>
            </view>
        </tig-popup>
        <view v-if="page > 1" class="loading-box">
            <view v-if="loading" class="bottomLoading">
                <image lazy-load class="loading" :src="staticResource('common/loading.gif')" />
            </view>
        </view>
        <view class="loading-box">
            <!-- <view v-if="consultationList.length < 1">{{ $t("暂无咨询") }}</view> -->
            <view v-if="consultationList.length < 1" class="empty-box">
                <view class="pictrue">
                    <image :src="staticResource('common/data_empty.png')" />
                </view>
                <view class="txt">{{ $t("暂无咨询") }}</view>
            </view>
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 商品咨询页
 * - 展示商品的用户咨询问答列表
 * - 支持发布新咨询问题
 * - 显示商家回复内容
 */
import { ref, nextTick } from "vue";
import { imageFormat } from "@/utils/format";
import { onLoad, onReachBottom } from "@dcloudio/uni-app";
import { getProductConsultationList, getProductDetail, addConsultation } from "@/api/product/product";
import type { ProductConsultationItem, ProductItem } from "@/types/product/product";
import { staticResource } from "@/utils";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const product = ref<ProductItem>({
    productId: 0,
    productStock: 0
});
const loading = ref(false);
const page = ref<number>(1);
const size = ref(10);
const total = ref(0);
const loaded = ref(false);
const consultationLoading = ref(false);
const consultationMessage = ref<string>("");
const consultationList = ref<ProductConsultationItem[]>([]);
const consultationTotal = ref(0);
const textareaFocus = ref(false);
const __getProductDetail = async (id: string) => {
    try {
        let result = await getProductDetail({ id });
        product.value = result.item;
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};
const fetchConsultation = async (reset = false) => {
    if (reset) {
        page.value = 1;
    }
    if (page.value > 1) {
        loaded.value = true;
    }
    uni.showLoading({
        title: t("加载中")
    });
    if (page.value === 1) {
        consultationList.value = [];
    }
    try {
        const result = await getProductConsultationList({ productId: productId.value, page: page.value, size: size.value });
        consultationList.value = [...consultationList.value, ...result.records];
        total.value = result.total;
        consultationTotal.value = result.total;
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    } finally {
        loaded.value = false;
        uni.hideLoading();
    }
};
const productId = ref<any>("");
onLoad((option) => {
    if (option) {
        const { id } = option;
        if (id) {
            productId.value = id;
            __getProductDetail(id);
            fetchConsultation(true);
        }
    }
});
const customerService = () => {
    consultationLoading.value = true;
    nextTick(() => {
        setTimeout(() => {
            textareaFocus.value = true;
        }, 100); // 延迟以确保弹窗已渲染
    });
};
const submitConsultation = async () => {
    if (!consultationMessage.value.trim()) {
        uni.showToast({
            title: t("请输入咨询内容"),
            icon: "none"
        });
        return;
    }
    try {
        await addConsultation({
            productId: productId.value,
            content: consultationMessage.value,
            type: 2 // 2表示商品咨询
        });
        consultationMessage.value = "";
        consultationLoading.value = false;
        uni.showToast({
            title: t("请您耐心等待客服人员的回复，您可在会员中心查看留言回复消息动态")
        });
        fetchConsultation(true);
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};
const handleIcon = () => {
    consultationLoading.value = false;
};
onReachBottom(() => {
    if (page.value < Math.ceil(total.value / size.value)) {
        page.value++;
        fetchConsultation();
    }
});
</script>

<style lang="scss" scoped>
.loading-box {
    padding: 30rpx;
    font-size: 24rpx;
}

.product-info {
    border-top: 1rpx solid #eee;
    padding: 20rpx;
    background-color: #fff;
    margin-bottom: 20rpx;
    gap: 24rpx;

    .product-info-img {
        flex-shrink: 0;
        image {
            width: 160rpx;
            height: 160rpx;
        }
    }

    .product-info-title {
        font-size: 26rpx;
    }

    .product-info-price {
        color: var(--general);
        font-weight: bold;
        font-size: 36rpx;
        display: flex;
        align-items: center;
        line-height: 36rpx;
        margin-top: 10rpx;

        :deep(.util) {
            font-size: 24rpx;
        }
    }
}

.popup-content {
    .popup-header {
        display: flex;
        justify-content: space-between;
        padding: 20rpx;

        .product-Slot {
            display: flex;
            align-items: center;
            margin-right: 20rpx;

            .iconfont {
                font-size: 32rpx;
                color: #333;
                cursor: pointer;
            }
        }

        .product-info-img {
            width: 80rpx;
            height: 80rpx;
            border-radius: 10px;
            margin-right: 16rpx;
            flex-shrink: 0;
            overflow: hidden;
            image {
                border-radius: 10px;
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
        }

        .product-info-title {
            display: flex;
            align-items: center;
        }

        .popup-footer {
            display: flex;
            align-items: center;

            .btn {
                height: 48rpx;
            }
        }
    }

    .popup-body {
        padding: 20rpx;

        .popup-item {
            // display: flex;
            // align-items: center;
            margin-bottom: 20rpx;

            .popup-item-title {
                font-size: 28rpx;
                color: #333;
                margin-right: 10rpx;
                padding-bottom: 10rpx;
            }

            .popup-item-footer {
                text-align: right;
                color: #999;
                font-size: 24rpx;
                margin-top: 4rpx;
            }
        }
    }
}

.product-consult-card {
    .consult-item {
        padding: 20rpx;
        background-color: #fff;
        margin-bottom: 20rpx;

        .consult-item-tit {
            padding: 10rpx 0;

            .time,
            .name {
                color: #666;
                font-size: 22rpx;
            }
        }

        .consult-con {
            padding: 10rpx 0;
            color: #666;
            font-size: 22rpx;

            text {
                display: inline-block;
                width: 35rpx;
                height: 35rpx;
                text-align: center;
                line-height: 35rpx;
                background-color: $tig-color-error;
                color: #fff;
                font-size: 22rpx;
                border-radius: 10rpx;
                margin-right: 8rpx;
            }
        }
    }
}

.btn-box {
    padding: 25rpx;
    .btn {
        background: #c3a769;
    }
}
</style>
