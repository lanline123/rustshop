<template>
    <tig-layout :title="$t('收货地址')">
        <view class="address-list">
            <up-radio-group v-model="isCheckedId" placement="column" @change="getCurrentAddress">
                <uni-swipe-action>
                    <template v-for="item in addressList" :key="item.addressId">
                        <view class="address-item">
                            <uni-swipe-action-item :threshold="0">
                                <view class="address-item-content" @click="handleSelectAddress(item.addressId)">
                                    <view class="address-item-left flex-center">
                                        <up-radio active-color="var(--general)" :name="item.addressId" />
                                    </view>
                                    <view class="address-item-middle">
                                        <view class="user-info">
                                            <view class="name">{{ item.consignee }}</view>

                                            <view class="phone"
                                                >{{ mobileConceal(item.mobile) }} <view v-if="item.isDefault === 1" class="tag">{{ $t("默认") }}</view></view
                                            >
                                        </view>
                                        <view class="address line1">{{ item.regionName }} {{ item.address }}</view>
                                    </view>
                                    <view class="address-item-right flex-center" @click.stop="handleEdit(item.addressId)">
                                        <view class="iconfont icon-bianji" />
                                    </view>
                                </view>
                                <template #right>
                                    <view class="address-del" @click.stop="handleDel(item.addressId)"> {{ $t("删除") }} </view>
                                </template>
                            </uni-swipe-action-item>
                        </view>
                    </template>
                </uni-swipe-action>
            </up-radio-group>
        </view>

        <empty-box v-if="!isLoadMore && !isLoading && addressList.length === 0" background="transparent" mode="list" :text="$t('暂无地址！')" />

        <loading-box v-model="isLoadMore" :page="filterParams.page" :length="addressList.length" />

        <tig-fixed-placeholder background-color="#fff">
            <view class="btn-box">
                <tig-button class="btn" @click="handleAdd"> <text class="iconfont icon-dizhi" /> {{ $t("添加新地址") }} </tig-button>
            </view>
        </tig-fixed-placeholder>

        <tig-loadingpage v-model="isLoading" />
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 收货地址列表页
 * - 展示用户所有收货地址
 * - 支持选择默认地址、编辑、删除操作
 * - 订单结算时可选择地址返回
 */
import { getAddressList, delAddress, selectedAddress } from "@/api/user/address";
import type { AddressFilterResult } from "@/types/user/address";
import { reactive, ref } from "vue";
import { onLoad, onUnload } from "@dcloudio/uni-app";
import { mobileConceal, prevRoute } from "@/utils";
import { useList } from "@/hooks";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

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
        isCheckedId.value = data.find((item: any) => item.isSelected === 1)?.addressId;
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

const isCheckedId = ref();
const getCurrentAddress = () => {
    __selectedAddress();
};
const handleSelectAddress = (id: number) => {
    isCheckedId.value = id;
    getCurrentAddress();
};

const __selectedAddress = async () => {
    uni.showLoading({
        title: t("切换中")
    });
    const page = prevRoute();
    try {
        await selectedAddress({ id: isCheckedId.value });
        filterParams.page = 1;
        addressList.value = [];

        if (page && page === "pages/order/check") {
            uni.$emit("refreshAddress");

            return uni.navigateBack();
        }

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
.btn-box {
    padding: 25rpx;

    .btn {
        font-size: 28rpx;
    }
}

.address-list {
    padding: 20rpx;

    .address-item {
        border-radius: 10rpx;
        background-color: #fff;
        margin-bottom: 20rpx;

        .address-item-content {
            display: flex;
            padding: 20rpx;
            .flex-center {
                display: flex;
                justify-content: center;
                align-items: center;
            }

            .address-item-left {
                width: 60rpx;
            }

            .address-item-middle {
                width: 100%;
                padding-left: 10rpx;
                .user-info {
                    color: #333;
                    display: block;
                    font-size: 30rpx;
                    font-weight: bold;
                    display: flex;
                }
                .name {
                    margin-right: 10rpx;
                }

                .phone {
                    position: relative;
                    .tag {
                        position: absolute;
                        right: -70rpx;
                        top: 0;
                        font-size: 20rpx;
                        font-weight: normal;
                        padding: 4rpx 8rpx;
                        background-color: var(--general);
                        border-radius: 10rpx;
                        color: #fff;
                    }
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

                .address {
                    color: #666;
                    display: block;
                    font-size: 24rpx;
                    overflow: hidden;
                    padding-top: 16rpx;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                    max-width: 500rpx;
                }
            }

            .address-item-right {
                width: 60rpx;
            }
        }
    }
    .address-del {
        height: 100%;
        background-color: var(--general);
        width: 100rpx;
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 22rpx;
        position: relative;
        z-index: 9;
    }
}
</style>
