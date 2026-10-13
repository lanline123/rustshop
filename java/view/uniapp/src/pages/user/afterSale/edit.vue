<template>
    <tig-layout :need-safe-top="true">
        <view v-if="Object.keys(infoData).length" class="after-sale">
            <view class="after-sale-product">
                <template v-for="item in infoData.list" :key="item.itemId">
                    <view class="product-card-item">
                        <view class="product-card-item-left">
                            <view class="left-img">
                                <tig-image :src="pickPic(item)" />
                            </view>
                        </view>
                        <view class="product-card-item-right">
                            <view class="right-title line2">
                                {{ item.productName }}
                            </view>
                            <view class="right-price">
                                <format-price
                                    :show-text="false"
                                    :decimals-style="{
                                        fontSize: '24rpx',
                                        fontWeight: 'bold'
                                    }"
                                    :currency-style="{
                                        fontSize: '22rpx',
                                        fontWeight: 'bold'
                                    }"
                                    class="right-price-pricenum"
                                    :price-data="getDisplayPrice(item)"
                                />
                                <view class="right-price-quantity">x {{ item.quantity }}</view>
                            </view>
                        </view>
                        <view class="btn-box">
                            <view class="btn-box-content">
                                <text class="btn-box-content-text">{{ $t("可售后数量") }}{{ item.canApplyQuantity }}</text>
                                <uni-number-box v-model="item.number" :max="item.canApplyQuantity" :min="1" />
                            </view>
                        </view>
                    </view>
                </template>
            </view>

            <view class="after-sale-form">
                <uni-forms ref="formRef" :model-value="form" label-width="170rpx">
                    <uni-forms-item :label="$t('退款方式')" name="aftersaleType">
                        <picker style="height: 100%" :value="aftersaleTypeIndex" range-key="label" :range="aftersaleTypeList" @change="getAftersaleType">
                            <view class="form-item-content">
                                <view>
                                    <text v-if="aftersaleTypeIndex !== null" class="form-item-value">{{ aftersaleTypeList[aftersaleTypeIndex!].label }}</text>
                                    <text v-else class="form-item-text">{{ $t("请选择") }}</text>
                                </view>
                                <view class="form-item-icon">
                                    <text class="iconfont-h5 icon-gengduo1" style="font-size: 21rpx; color: #999" />
                                </view>
                            </view>
                        </picker>
                    </uni-forms-item>
                    <uni-forms-item v-if="afterSalesReasonEnabled" :label="$t('退款原因')" name="aftersaleReason">
                        <picker :value="aftersaleReasonIndex" :range="aftersaleReasonList" @change="getAftersaleReason">
                            <view class="form-item-content">
                                <view>
                                    <text v-if="form.aftersaleReason" class="form-item-value">{{ form.aftersaleReason }}</text>
                                    <text v-else class="form-item-text">{{ $t("请选择") }}</text>
                                </view>
                                <view class="form-item-icon">
                                    <text class="iconfont-h5 icon-gengduo1" style="font-size: 21rpx; color: #999" />
                                </view>
                            </view>
                        </picker>
                    </uni-forms-item>
                    <uni-forms-item :label="$t('问题描述')" name="description">
                        <uni-easyinput
                            v-model="form.description"
                            type="textarea"
                            primary-color="rgb(192, 196, 204)"
                            maxlength="100"
                            :input-border="false"
                            auto-height
                            :placeholder="$t('最多100字')"
                            placeholder-style="font-size: 26rpx;text-align: end;"
                        />
                    </uni-forms-item>
                    <uni-forms-item :label="$t('上传凭证')" name="description">
                        <view class="upload-box">
                            <view class="upload-text">{{ $t("最多上传5张图片") }}</view>
                            <upload v-model="fileLists" :limit="5" :is-value-array="true" />
                        </view>
                    </uni-forms-item>
                </uni-forms>
            </view>
            <tig-fixed-placeholder background-color="#fff">
                <view class="after-sale-btn-box">
                    <tig-button @click="handleSave"> {{ $t("提交") }} </tig-button>
                </view>
            </tig-fixed-placeholder>
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
import { pickPic } from "@/utils/format";
/**
 * 售后申请/编辑页
 * - 支持新建售后申请（传 orderId 和可选 itemId）
 * - 支持编辑已有售后申请（传 aftersaleId）
 * - 选择退款方式、退款原因、填写问题描述、上传凭证
 * - 订单状态为待发货(orderStatus=1)时不显示退货退款选项
 */
import { onLoad } from "@dcloudio/uni-app";
import { computed, ref, reactive } from "vue";
import { getAftersalesEdit, updateAfterSales, getAftersalesConfig, aftersalesViewRecord, aftersalesUpdate } from "@/api/user/afterSale";
import upload from "@/components/upload/index.vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const orderId = ref<number | null>();
const itemId = ref<number | null>();
const aftersaleId = ref<number | null>();

onLoad(async (options) => {
    if (options) {
        if (options.orderId) {
            orderId.value = options.orderId;
            itemId.value = options.itemId;
            await __getAftersalesEdit();
        }
        if (options.aftersaleId) {
            aftersaleId.value = options.aftersaleId;
            await __getAftersalesDetail();
        }
        await __getAftersalesConfig();
    }
});

const infoData = ref<{ list: any; order: any }>({
    list: [],
    order: {}
});

/**
 * 预约上门订单判断（用于将“上门服务费”合并到单品回显价格中）
 */
const isReserveOrder = computed(() => {
    const order: any = infoData.value?.order || {};
    const orderType = Number(order?.orderType ?? 0);
    const isReservationHome = Boolean(order?.isReservationHome);
    return isReservationHome || orderType === 9 || orderType === 10;
});

/**
 * 上门服务费金额（项目内其它页面以 shippingFee 作为上门服务费字段）
 */
const visitFeeAmount = computed(() => {
    const order: any = infoData.value?.order || {};
    return Number(order?.shippingFee ?? 0);
});

/**
 * 获取商品显示价格
 * @param item 商品信息
 * @returns 商品显示价格（商品价格 + 附加价 + 上门服务费分摊单价）
 */
const getDisplayPrice = (item: any) => {
    const orderItems = infoData.value?.order?.items || [];
    const matched = orderItems.find((orderItem: any) => {
        return orderItem?.itemId === item?.itemId || orderItem?.orderItemId === item?.orderItemId || orderItem?.skuId === item?.skuId;
    });
    const rawExtraSkuData = matched?.extraSkuData ?? item?.extraSkuData ?? [];
    let extraSkuData = rawExtraSkuData;
    if (typeof rawExtraSkuData === "string") {
        try {
            extraSkuData = JSON.parse(rawExtraSkuData);
        } catch {
            extraSkuData = [];
        }
    }
    if (!Array.isArray(extraSkuData)) {
        extraSkuData = [];
    }
    const extraPrice = extraSkuData.reduce((sum: number, extra: any) => {
        const raw = extra?.totalAttrPrice ?? extra?.attrPrice ?? 0;
        return sum + Number(raw || 0);
    }, 0);

    // 上门服务费为“订单级”费用：按商品数量均摊到单品单价，避免多商品时每行叠加导致金额翻倍
    let visitFeeSharePerUnit = 0;
    if (isReserveOrder.value && visitFeeAmount.value > 0) {
        const list: any[] = infoData.value?.list || [];
        const totalQty = list.reduce((sum, it) => {
            const qty = Number(it?.quantity ?? it?.canApplyQuantity ?? it?.number ?? 0);
            return sum + (Number.isFinite(qty) ? qty : 0);
        }, 0);
        if (totalQty > 0) {
            visitFeeSharePerUnit = visitFeeAmount.value / totalQty;
        }
    }

    return Number(item?.price || 0) + extraPrice + visitFeeSharePerUnit;
};
const aftersaleTypeList = ref<any[]>([]);
const __getAftersalesEdit = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        if (orderId.value) {
            const result = await getAftersalesEdit({
                itemId: itemId.value,
                orderId: orderId.value
            });
            infoData.value.list = result.list;
            infoData.value.order = result.order;
            infoData.value.list.forEach((item: any) => {
                item.number = item.canApplyQuantity;
            });
        }
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
    }
};

const aftersaleReasonList = ref<any[]>([]);
const afterSalesReasonEnabled = ref(false);

/**
 * 获取售后配置（退款方式和退款原因选项）
 * - 待发货订单(orderStatus=1)不显示退货退款(aftersaleType=1)选项
 */
const __getAftersalesConfig = async () => {
    try {
        const result = await getAftersalesConfig({ orderId: orderId.value });
        aftersaleReasonList.value = Array.isArray(result.aftersaleReason) ? result.aftersaleReason : [];
        afterSalesReasonEnabled.value = Number(result.afterSalesReasonEnabled) === 1;
        const order = infoData.value?.order;
        const orderStatus = Number(order?.orderStatus);
        const isPickup = !!order?.isPickup;
        Object.keys(result.aftersaleType).forEach((key) => {
            // 退货退款(aftersaleType=1)限制：待发货订单 或 自提订单未完成提货时不可选
            if (Number(key) === 1 && (orderStatus === 1 || (isPickup && orderStatus <= 2))) {
                return;
            } else {
                aftersaleTypeList.value.push({
                    value: Number(key),
                    label: result.aftersaleType[key]
                });
            }
        });

        if (aftersaleId.value) {
            aftersaleTypeIndex.value = aftersaleTypeList.value.findIndex((item: any) => item.value === form.aftersaleType);
            aftersaleReasonIndex.value = aftersaleReasonList.value.findIndex((item: any) => item === form.aftersaleReason);
        }
    } catch (error) {
        console.error(error);
    }
};

const __getAftersalesDetail = async () => {
    try {
        const result = await aftersalesViewRecord(aftersaleId.value!);
        infoData.value.list = result.aftersalesItems;
        infoData.value.order = result.orders;
        orderId.value = result.orderId ?? result.orders?.orderId ?? orderId.value;
        form.aftersaleId = result.aftersaleId;
        form.description = result.description;
        fileLists.value = result.pics;
        form.aftersaleReason = result.aftersaleReason;
        form.aftersaleType = result.aftersaleType;
    } catch (error) {
        console.error(error);
    }
};

interface Iform {
    items: Item[];
    pics: any[];
    orderId: number;
    aftersaleType: number;
    aftersaleReason: string;
    description: string;
    aftersaleId?: number;
}

interface Item {
    orderItemId: number;
    number: number;
}
const form = reactive<Iform>({
    items: [],
    pics: [],
    orderId: 0,
    aftersaleType: 0,
    aftersaleReason: "",
    description: ""
});

interface FileLists {
    name: string;
    extname: string;
    url: string;
}
const fileLists = ref<FileLists[]>([]);
const aftersaleTypeIndex = ref<null | number>(null);

const getAftersaleType = (e: any) => {
    aftersaleTypeIndex.value = e.detail.value;
    form.aftersaleType = aftersaleTypeList.value[aftersaleTypeIndex.value!].value;
};

const aftersaleReasonIndex = ref<null | number>(null);
const getAftersaleReason = (e: any) => {
    aftersaleReasonIndex.value = e.detail.value;
    form.aftersaleReason = aftersaleReasonList.value[aftersaleReasonIndex.value!];
};

/**
 * 提交售后申请
 * - 校验必填项（退款方式、退款原因、问题描述）
 * - 根据是否有 aftersaleId 判断是新建还是编辑
 * - 新建用 itemId，编辑用 orderItemId
 */
const handleSave = async () => {
    if (form.aftersaleType === 0)
        return uni.showToast({
            title: t("请选择退款方式"),
            icon: "none"
        });
    if (afterSalesReasonEnabled.value && form.aftersaleReason === "")
        return uni.showToast({
            title: t("请选择退款原因"),
            icon: "none"
        });
    if (form.description === "")
        return uni.showToast({
            title: t("请填写问题描述"),
            icon: "none"
        });
    form.pics = fileLists.value;
    form.orderId = infoData.value.order.orderId;
    form.items = [];
    if (form.aftersaleId) {
        // 编辑模式：使用 orderItemId
        infoData.value.list.forEach((item: any) => {
            form.items.push({ orderItemId: item.orderItemId, number: item.number });
        });
        edit();
    } else {
        // 新建模式：使用 itemId
        infoData.value.list.forEach((item: any) => {
            form.items.push({ orderItemId: item.itemId, number: item.number });
        });
        add();
    }
};

const add = async () => {
    try {
        const payload: Record<string, any> = { ...form };
        if (!afterSalesReasonEnabled.value) delete payload.aftersaleReason;
        await updateAfterSales(payload);
        uni.showToast({
            title: t("提交成功")
        });
        setTimeout(() => {
            uni.$emit("afterSalesSuccess");

            uni.redirectTo({
                url: "/pages/user/afterSale/list"
            });
        }, 1500);
    } catch (error: any) {
        console.error(error);
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};

const edit = async () => {
    try {
        const payload: Record<string, any> = { ...form };
        if (!afterSalesReasonEnabled.value) delete payload.aftersaleReason;
        await aftersalesUpdate(payload);
        uni.showToast({
            title: t("修改成功")
        });
        setTimeout(() => {
            uni.redirectTo({
                url: "/pages/user/afterSale/list"
            });
        }, 1500);
    } catch (error: any) {
        console.error(error);
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};
</script>

<style lang="scss" scoped>
.after-sale {
    .after-sale-product {
        background-color: #fff;
        border-radius: 0 0 20rpx 20rpx;
        padding: 20rpx;
        margin-bottom: 20rpx;

        .product-card-item {
            display: flex;
            position: relative;
            margin-bottom: 25rpx;
            &:last-child {
                margin-bottom: 0;
            }

            .product-card-item-left {
                .left-img {
                    width: 160rpx;
                    height: 160rpx;
                    border-radius: 10rpx;
                    overflow: hidden;
                    margin-right: 20rpx;
                    box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.04);
                }
            }

            .product-card-item-right {
                font-size: 24rpx;

                .right-title {
                    font-weight: bold;
                }

                .right-price {
                    display: flex;
                    font-size: 28rpx;
                    padding-top: 20rpx;
                    .right-price-pricenum {
                        font-weight: bold;
                        color: var(--general);
                        padding-right: 10rpx;
                    }

                    .right-price-quantity {
                        color: #999;
                    }
                }
            }

            .btn-box {
                position: absolute;
                right: 0;
                bottom: 0;

                .btn-box-content {
                    display: flex;
                    // align-items: center;
                    .btn-box-content-text {
                        font-size: 24rpx;
                        color: #999;
                        display: flex;
                        align-items: end;
                        padding-right: 10rpx;
                    }
                }
            }
        }
    }

    .after-sale-form {
        background-color: #fff;
        .form-item-content {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            height: 72rpx;

            .form-item-icon {
                padding-left: 15rpx;
            }

            .form-item-text {
                color: #969799;
            }
        }

        :deep(.uni-easyinput__content-textarea) {
            min-height: 50rpx;
            height: 50rpx;
        }

        :deep(.uni-input-input) {
            text-align: end;
        }

        .hint {
            background-color: #f5f5f5;
            padding: 10rpx 25rpx;
            color: #9c9c9c;
            font-size: 25rpx;
        }

        :deep(.uni-file-picker__header) {
            padding-top: 17rpx;

            .file-title {
                color: #9c9c9c;
            }
        }
    }
    :deep(.special-item) {
        &.uni-forms-item {
            border-bottom: none;
            margin-bottom: 0;
        }
    }
    :deep(.uni-forms-item) {
        margin: 10rpx 25rpx 10rpx 25rpx;
        border-bottom: 1px solid #eee;
        &:last-child {
            border-bottom: none;
            padding-bottom: 20rpx;
        }
    }

    :deep(.file-picker__box) {
        padding-top: 30%;
    }
    :deep(.file-picker__progress) {
        display: none;
    }

    :deep(.icon-del-box) {
        width: 40rpx;
        height: 40rpx;
    }

    .after-sale-btn-box {
        padding: 25rpx;
    }
    :deep(.uni-file-picker__header) {
        padding-right: 15rpx;
    }
}
.upload-box {
    padding-top: 55rpx;
    position: relative;

    .upload-text {
        position: absolute;
        top: 10rpx;
        font-size: 22rpx;
        color: #969799;
    }
}
</style>
