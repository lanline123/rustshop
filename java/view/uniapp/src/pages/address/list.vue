<template>
    <tig-layout :title="$t('收货地址')">
        <view class="address-list">
            <up-radio-group v-model="isDefaultId" placement="column" @change="handleSetDefault">
                <template v-for="item in addressList" :key="item.addressId">
                    <view class="address-item" :class="{ 'is-selected': item.isSelected === 1 }" :style="item.isSelected === 1 ? { backgroundColor: selectedItemBg } : undefined">
                        <view class="address-item-content" @click="handleSelectAddress(item.addressId)">
                            <view class="address-item-middle">
                                <view class="user-info">
                                    <view class="name">{{ item.consignee }}</view>
                                    <view v-if="item.addressTag" class="address-tag">{{ item.addressTag }}</view>
                                    <view class="phone">{{ displayMobile(item) }}</view>
                                </view>
                                <view class="address-desc">
                                    <view class="txt">{{ displayAddress(item) }}</view>
                                    <view class="copy" @click.stop="copy(item.consignee + copyMobile(item) + displayAddress(item), copyTips)">{{ $t("复制") }}</view>
                                </view>
                            </view>
                            <view class="operation">
                                <view class="address-check" @click.stop>
                                    <up-radio active-color="var(--general)" :name="item.addressId" shape="square" :label="$t('默认地址')" />
                                </view>
                                <view class="btn-wrap">
                                    <view class="icon" @click.stop="handleEdit(item.addressId)">
                                        <text class="iconfont-h5 icon-edit" />
                                    </view>
                                    <view class="icon del" @click.stop="handleDel(item.addressId)">
                                        <text class="iconfont-h5 icon-delete" />
                                    </view>
                                </view>
                            </view>
                        </view>
                    </view>
                </template>
            </up-radio-group>
        </view>

        <view v-if="!isLoadMore && !isLoading && addressList.length === 0" class="address-empty">
            <empty-box center-content height="100%" background="transparent" mode="list" :text="$t('暂无地址！')" />
        </view>

        <loading-box v-model="isLoadMore" :page="filterParams.page" :length="addressList.length" />

        <tig-fixed-placeholder background-color="#fff">
            <view class="btn-box">
                <tig-button2 class="btn" style="height: 73rpx" theme @click="handleAdd">
                    <up-icon name="plus" color="#fff" style="margin-right: 10rpx" />
                    {{ $t("添加新地址") }}
                </tig-button2>
            </view>
        </tig-fixed-placeholder>

        <tig-loadingpage v-model="isLoading" />
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 收货地址列表页
 * - 展示用户所有收货地址
 * - 勾选「默认地址」写入 isDefault；点击卡片设置当前选中地址 isSelected
 * - 订单结算时可选择地址返回
 */
import { getAddressList, delAddress, selectedAddress, updateAddressData } from "@/api/user/address";
import type { AddressFilterResult } from "@/types/user/address";
import { computed, reactive, ref } from "vue";
import { onLoad, onUnload } from "@dcloudio/uni-app";
import { mobileConceal, prevRoute, copy, isOverseas } from "@/utils";
import { useList } from "@/hooks";
import { useI18n } from "vue-i18n";
import { useThemeStore } from "@/store/theme";

const { t } = useI18n();
const themeStore = useThemeStore();
const selectedItemBg = computed(() => themeStore.hexToRgba(themeStore.themeStyle["--general"], 0.1));

const isOverseasMode = isOverseas();


const displayMobile = (item: AddressFilterResult) => {
    return mobileConceal(item.mobile);
};

const copyMobile = (item: AddressFilterResult) => {
    return item.mobile;
};

const displayAddress = (item: AddressFilterResult) => {
    return [item.regionName, item.address].filter(Boolean).join(" ");
};

const filterParams = reactive({
    page: 1,
    size: 10
});
const {
    getList,
    data: addressList,
    isLoadMore,
    isLoading
} = useList<AddressFilterResult>(getAddressList, {
    params: filterParams,
    path: {
        dataKey: "records"
    },
    immediate: true,
    manageData: (data: any) => {
        isDefaultId.value = data.find((item: any) => item.isDefault === 1)?.addressId;
    }
});

onLoad(() => {
    const page = prevRoute();
    if (page && page === "pages/order/check") {
        uni.setStorageSync("link", true);
    } else {
        uni.removeStorageSync("link");
    }
    uni.$on("refreshData", () => {
        filterParams.page = 1;
        addressList.value = [];
        getList();
    });
});

const handleDel = (id: number) => {
    console.log("handleDel", id);
    uni.showModal({
        title: t("提示"),
        content: t("确定删除吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (res.confirm) {
                __delAddress(id);
            }
        }
    });
};
const __delAddress = async (id: number) => {
    try {
        await delAddress({ id });

        uni.showToast({
            title: t("删除成功"),
            icon: "none"
        });

        filterParams.page = 1;
        addressList.value = [];
        getList();
    } catch (error) {
        console.error(error);
    }
};

const handleEdit = (id: number) => {
    uni.navigateTo({
        url: "/pages/address/editRegion?id=" + id
    });
};
const handleAdd = () => {
    const isFirstAddress = addressList.value.length === 0;
    uni.navigateTo({
        url: isFirstAddress ? "/pages/address/editRegion?isSelected=1" : "/pages/address/editRegion"
    });
};

const isDefaultId = ref();

const handleSetDefault = async () => {
    const addressId = isDefaultId.value;
    if (!addressId) return;
    const current = addressList.value.find((item) => item.addressId === addressId);
    if (current?.isDefault === 1) return;

    const prevDefaultId = addressList.value.find((item) => item.isDefault === 1)?.addressId;
    uni.showLoading({
        title: t("切换中")
    });
    try {
        await updateAddressData({ id: addressId, addressId, isDefault: 1 });
        addressList.value.forEach((item) => {
            item.isDefault = item.addressId === addressId ? 1 : 0;
        });
    } catch (error: any) {
        console.error(error);
        isDefaultId.value = prevDefaultId;
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    } finally {
        uni.hideLoading();
    }
};

const handleSelectAddress = (id: number) => {
    __selectedAddress(id);
};

const copyTips = () => {
    uni.showToast({
        title: t("复制成功"),
        icon: "none"
    });
};

const __selectedAddress = async (id: number) => {
    uni.showLoading({
        title: t("切换中")
    });
    const page = prevRoute();
    try {
        await selectedAddress({ id });

        if (page && page === "pages/order/check") {
            uni.$emit("refreshAddress");
            return uni.navigateBack();
        }

        filterParams.page = 1;
        addressList.value = [];
        getList();
    } catch (error: any) {
        console.error(error);
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    } finally {
        uni.hideLoading();
    }
};

onUnload(() => {
    uni.$off("refreshData");
});
</script>

<style lang="scss" scoped>
.address-empty {
    height: calc(100vh - var(--nav-height) - 120rpx - var(--safe-bottom, 0px));
}

.btn-box {
    padding: 25rpx;

    .btn {
        font-size: 28rpx;
    }
}

.address-list {
    border-top: 1rpx solid #e5e7eb;

    .address-item {
        background-color: #fff;
        padding: 16rpx 32rpx;

        .address-item-content {
            .address-item-middle {
                width: 100%;
                .user-info {
                    display: flex;
                    align-items: center;
                    font-size: 28rpx;
                    margin-right: 16rpx;
                    font-weight: 700;

                    .address-tag {
                        margin-right: 20rpx;
                        padding: 2rpx 12rpx;
                        font-size: 20rpx;
                        font-weight: normal;
                        color: var(--general);
                        border: 1rpx solid var(--general);
                        border-radius: 6rpx;
                        line-height: 1.6;
                    }
                    line-height: 40rpx;
                    text-align: left;
                    color: #000;
                    word-break: break-word;
                }
                .name {
                    margin-right: 10rpx;
                }

                .phone {
                    position: relative;
                    font-size: 24.5rpx;
                    font-weight: 400;
                    line-height: 33rpx;
                    text-align: left;
                    color: #1a1a1a;
                    white-space: nowrap;
                    word-break: break-word;
                }

                /* @vort-if overseas */
                .invalid-tag {
                    align-self: center;
                    margin-left: 16rpx;
                    padding: 2rpx 10rpx;
                    border: 1rpx solid #ff4d4f;
                    border-radius: 6rpx;
                    color: #ff4d4f;
                    font-size: 20rpx;
                    font-weight: 400;
                    line-height: 1.4;
                }
                /* @vort-endif */

                .address-desc {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;
                    margin-top: 8rpx;
                    .txt {
                        font-size: 24.5rpx;
                        font-weight: 400;
                        line-height: 33rpx;
                        text-align: left;
                        display: -webkit-box;
                        margin-bottom: 20rpx;
                        color: #505259;
                        word-break: break-all;
                    }
                    .copy {
                        font-size: 24.5rpx;
                        font-weight: 400;
                        line-height: 33rpx;
                        text-align: left;
                        margin-left: 8rpx;
                        white-space: nowrap;
                        color: #1869f5;
                    }
                }
            }

            .operation {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding-top: 18rpx;
                box-sizing: border-box;
                border-top: 1rpx solid #e5e7eb;
                .address-check {
                    :deep(.u-radio__text) {
                        font-size: 25rpx !important;
                        font-weight: 400 !important;
                        line-height: 35rpx !important;
                        text-align: left !important;
                        color: #1a1a1a !important;
                    }
                }
                .btn-wrap {
                    display: flex;
                    align-items: center;
                    .icon {
                        width: 40rpx;
                        height: 40rpx;
                        .iconfont-h5 {
                            font-size: 33rpx;
                        }
                    }
                    .del {
                        margin-left: 16rpx;
                        .iconfont-h5 {
                            font-size: 40rpx;
                        }
                    }
                }
            }
        }
    }
}
</style>
