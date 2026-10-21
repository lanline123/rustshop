<template>
    <tig-layout :title="$t('卡管理')">
        <view class="card-main">
            <view class="head-tabs-box">
                <view class="tabs flex align-center justify-between">
                    <view class="flex align-end">
                        <view
                            v-for="(tab, index) in tabs"
                            :key="index"
                            class="tab"
                            :class="{ active: activeTab === tab.accountType }"
                            @click="actionClick(tab.accountType)"
                            >{{ $t(tab.title) }}</view
                        >
                    </view>
                    <view class="clear-unread flex align-end" @click="__addAccountNo">
                        <text>{{ $t("添加账号") }}</text>
                        <text class="iconfont-h5 icon-auth" style="font-size: 36rpx; color: #999" />
                    </view>
                </view>
            </view>
            <view v-if="filterState.length > 0" class="card-list">
                <uni-swipe-action>
                    <block v-for="(item, index) in filterState" :key="index">
                        <view class="move-item">
                            <uni-swipe-action-item :threshold="0" auto-close>
                                <view class="mitem">
                                    <view class="card">
                                        <view class="txt-item">
                                            <view class="tit">{{ $t("真实姓名") }}</view>
                                            <view class="txt">{{ item.accountName }}</view>
                                        </view>
                                        <view class="txt-item">
                                            <view class="tit">{{ $t("身份证号码") }}</view>
                                            <view class="txt">{{ maskNumber(item.identity) }}</view>
                                        </view>
                                        <view class="txt-item">
                                            <view class="tit">{{ $t("账户号码") }}</view>
                                            <view class="txt">{{ maskNumber(item.accountNo) }}</view>
                                        </view>
                                        <view v-if="filterParams.accountType === 1" class="txt-item">
                                            <view class="tit">{{ $t("银行卡详情") }}</view>
                                            <view class="txt">{{ item.bankName }}</view>
                                        </view>
                                    </view>
                                </view>
                                <template #right>
                                    <view class="card-move-box">
                                        <view class="btn-edit" @click="__editAccountNo(item.accountId)"
                                            ><text>{{ $t("编辑") }}</text></view
                                        >
                                        <view class="btn-del" @click="__delAccount(item, index)"
                                            ><text>{{ $t("删除") }}</text></view
                                        >
                                    </view>
                                </template>
                            </uni-swipe-action-item>
                        </view>
                    </block>
                </uni-swipe-action>
            </view>
            <view v-if="filterState.length === 0 && loadend" class="empty-box card-empty">
                <view class="pictrue"><image :src="staticResource('common/data_empty.png')" /></view>
                <view class="txt">{{ $t("暂无数据") }}</view>
            </view>
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { onLoad, onUnload } from "@dcloudio/uni-app";
import { delAccount, getAccountNoList } from "@/api/user/account";
import type { AccountNoFilterParams, AccountInfo } from "@/types/user/account";
import { useI18n } from "vue-i18n";
import { maskNumber, staticResource } from "@/utils";

const { t } = useI18n();

const tabs = [
    { title: t("银行卡"), accountType: 1 },
    { title: t("支付宝"), accountType: 2 },
    { title: t("微信"), accountType: 3 }
];
const activeTab = ref(1);
const actionClick = async (value: number) => {
    if (value === activeTab.value) return;
    activeTab.value = value;
    filterParams.page = 1;
    filterParams.accountType = value;
    __getAccountNoList();
};

const loaded = ref(false);
const loadend = ref(false);
const filterParams = reactive<AccountNoFilterParams>({
    //初使化用于查询的参数
    page: 1,
    accountType: 1
});
const filterState = ref<AccountInfo[]>([]);

const __getAccountNoList = async () => {
    if (filterParams.page > 1) {
        loaded.value = true;
    } else {
        loadend.value = false;
        uni.showLoading({
            title: t("加载中")
        });
    }
    try {
        const result = await getAccountNoList({ ...filterParams });
        filterState.value = result.records;
    } catch (error: any) {
        console.error(error.message);
    } finally {
        uni.hideLoading();
        loaded.value = false;
        loadend.value = true;
    }
};

const __editAccountNo = (id: number) => {
    console.log(id);
    uni.navigateTo({
        url: `/pages/user/account/cardManagement/edit?id=${id}`
    });
};

const __addAccountNo = () => {
    uni.navigateTo({
        url: `/pages/user/account/cardManagement/edit?accountType=${activeTab.value}`
    });
};

const __delAccount = (data: AccountInfo, index: number) => {
    uni.showModal({
        title: t("提示"),
        content: t("确定删除这个账户吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (res.confirm) {
                deleteAcNo(data, index);
            }
        }
    });
};

const deleteAcNo = async (data: AccountInfo, index: number) => {
    try {
        const result = await delAccount({ ...data });
        filterState.value.splice(index, 1);
        uni.showToast({
            title: t("操作成功"),
            icon: "success"
        });
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    }
};

onLoad((option: any) => {
    if (option?.accountType) {
        const type = Number(option.accountType);
        activeTab.value = type;
        filterParams.accountType = type;
    }
    __getAccountNoList();
    uni.$on("refreshData", (data) => {
        activeTab.value = data?.type;
        filterParams.accountType = data?.type;
        __getAccountNoList();
    });
});

onUnload(() => {
    uni.$off("refreshData");
});
</script>

<style lang="scss" scoped>
.head-tabs-box {
    position: fixed;
    width: 100%;
    z-index: 88;
    top: var(--nav-height);
    .tabs {
        background-color: #fff;
        padding: 30rpx;
        .tab {
            font-size: 26rpx;
            margin-right: 40rpx;
            color: #666;
        }
        .active {
            color: var(--general);
            font-weight: bold;
            font-size: 32rpx;
        }
        .clear-unread {
            font-size: 26rpx;
            color: #999;
        }
    }
}
.card-empty {
    margin-top: 120rpx;
}
.card-list {
    position: relative;
    padding: 0 20rpx;
    margin-top: 120rpx;
    .move-item {
        margin-bottom: 10rpx;
    }
    .mitem {
        position: relative;
        border-radius: 10rpx;
        .card {
            position: relative;
            padding: 30rpx 20rpx;
            background-color: #fff;
            border-radius: 20rpx;
            .txt-item {
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin-bottom: 10rpx;
                &:last-child {
                    margin-bottom: 0;
                }
                .tit {
                    font-size: 24rpx;
                }
                .txt {
                    font-size: 22rpx;
                    color: #999;
                }
            }
        }
    }
}
.card-move-box {
    width: 300rpx;
    height: 100%;
    color: #fff;
    text-align: center;
    vertical-align: middle;
    display: flex;
    text {
        display: block;
        position: absolute;
        top: 50%;
        margin-top: -20rpx;
        font-size: 24rpx;
        text-align: center;
        width: 100%;
    }
    .btn-edit {
        width: 150rpx;
        background: var(--vice-bg);
        color: var(--general);
        display: inline-block;
        height: 100%;
        vertical-align: middle;
        display: table-cell;
        position: relative;
    }
    .btn-del {
        width: 150rpx;
        background: var(--general);
        display: inline-block;
        height: 100%;
        vertical-align: middle;
        display: table-cell;
        position: relative;
    }
}
</style>
