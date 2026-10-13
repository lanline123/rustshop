<template>
    <tig-layout :title="$t('客户信息')" :need-safe-bottom="false">
        <view class="detail-page">
            <view v-if="loading" class="loading">{{ $t("加载中") }}</view>
            <template v-else>
                <view class="card profile">
                    <view class="avatar"><tig-image :src="imageFormat(detail.avatar || configStore.defaultAvatar)" /></view>
                    <view class="profile-info">
                        <view class="name">{{ detail.nickname || "-" }}</view>
                    </view>
                    <view class="remark-entry" @click="openRemarkEditor"
                        ><text>{{ detail.remark || $t("备注") }}</text
                        ><up-icon name="edit-pen" size="18" color="#999"
                    /></view>
                </view>
                <tig-popup v-model:show="showRemarkEditor" position="bottom" :round="10" :show-close="false"
                    ><view class="remark-editor"
                        ><view class="remark-editor-header"
                            ><view class="remark-editor-title">{{ $t("备注") }}</view
                            ><view class="remark-close" @click="showRemarkEditor = false"><up-icon name="close" size="20" color="#999" /></view></view
                        ><up-textarea class="remark-textarea" v-model="remarkDraft" :placeholder="$t('请输入备注')" :maxlength="255" count /><view
                            class="remark-actions"
                            ><tig-button @click="saveRemark">{{ $t("确定") }}</tig-button></view
                        ></view
                    ></tig-popup
                >
                <view class="card statistics">
                    <view class="stat"
                        ><text>{{ detail.orderCount || 0 }}</text
                        ><view>{{ $t("订单数量") }}</view></view
                    >
                    <view class="stat"
                        ><text>{{ money(detail.orderAmount) }}</text
                        ><view class="stat-label"
                            >{{ $t("成交金额（元）") }}<up-icon name="question-circle" color="#969799" size="14" @click="showAmountTip" /></view
                    ></view>
                    <view class="stat"
                        ><text>{{ money(detail.averageOrderAmount) }}</text
                        ><view>{{ $t("客单价（元）") }}</view></view
                    >
                    <view class="stat"
                        ><text>{{ formatMinute(detail.lastOrderTime) || "-" }}</text
                        ><view>{{ $t("最后成交时间") }}</view></view
                    >
                </view>
                <tig-popup v-model:show="showAmountTipPopup" position="center" :round="12" :show-close="false">
                    <view class="amount-tip-popup">
                        <view class="amount-tip-title">{{ $t("成交金额说明") }}</view>
                        <view class="amount-tip-content">{{ $t("选定时间内，商城该客户所有订单的实际成交金额，并剔除退款金额") }}</view>
                        <tig-button class="amount-tip-button" @click="showAmountTipPopup = false">{{ $t("知道了") }}</tig-button>
                    </view>
                </tig-popup>
                <view class="card relation">
                    <view class="relation-title">{{ $t("客户关系") }}</view>
                    <view class="relation-row"
                        ><text>{{ $t("客户绑定渠道") }}</text
                        ><text>{{ detail.channel || "-" }}</text></view
                    >
                    <view class="relation-row"
                        ><text>{{ $t("绑定时间") }}</text
                        ><text>{{ formatMinute(detail.bindTime) || "-" }}</text></view
                    >
                </view>
            </template>
        </view>
    </tig-layout>
</template>
<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";
import { getCustomerDetail, updateCustomerRemark } from "@/api/salesman/salesmanCenter";
import type { CustomerDetail } from "@/types/salesman/customer";
import { useConfigStore } from "@/store/config";
import { imageFormat } from "@/utils/format";
import { useSafeAreaInsets } from "@/hooks";
import { ensureViewCustomerInfo } from "@/pages/salesman/src/salesmanPrivacy";
const { t } = useI18n();
const { safeBottom } = useSafeAreaInsets();
const loading = ref(true);
const configStore = useConfigStore();
const detail = ref<CustomerDetail>({ userId: 0 });
const showAmountTipPopup = ref(false);
const showAmountTip = () => (showAmountTipPopup.value = true);
const showRemarkEditor = ref(false);
const remarkDraft = ref("");
const money = (value?: number) => Number(value || 0).toFixed(2);
const formatMinute = (value?: string | null) => (value ? value.replace(/:\d{2}(?=\s*$)/, "") : "");
const openRemarkEditor = () => {
    remarkDraft.value = detail.value.remark || "";
    showRemarkEditor.value = true;
};
const saveRemark = async () => {
    const remark = remarkDraft.value.trim();
    try {
        await updateCustomerRemark({ userId: detail.value.userId, remark });
        detail.value.remark = remark;
        showRemarkEditor.value = false;
    } catch {
        uni.showToast({ title: t("保存失败"), icon: "none" });
    }
};
onLoad(async (options: any) => {
    const allowed = await ensureViewCustomerInfo();
    if (!allowed) {
        loading.value = false;
        return;
    }
    const userId = Number(options?.userId);
    if (!userId) {
        uni.showToast({ title: t("客户不存在"), icon: "none" });
        loading.value = false;
        return;
    }
    try {
        detail.value = await getCustomerDetail(userId);
    } catch {
        uni.showToast({ title: t("加载失败"), icon: "none" });
    } finally {
        loading.value = false;
    }
});
</script>
<style lang="scss" scoped>
.detail-page {
    min-height: 0;
    padding: 24rpx;
    box-sizing: border-box;
}
.card {
    margin-bottom: 24rpx;
    padding: 28rpx;
    border-radius: 10rpx;
    background: #fff;
}
.profile {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
}
.avatar {
    display: block;
    width: 96rpx;
    height: 96rpx;
    margin-right: 24rpx;
    border-radius: 50%;
    overflow: hidden;
    flex: none;
    :deep(.base-image) {
        opacity: 1;
    }
    :deep(.base-image-box) {
        width: 100%;
        height: 100%;
    }
}
.name {
    margin-bottom: 12rpx;
    color: #333;
    font-size: 34rpx;
    font-weight: 600;
}
.mobile {
    color: #999;
    font-size: 26rpx;
}
.remark-entry {
    display: flex;
    align-items: center;
    gap: 10rpx;
    margin-top: 22rpx;
    width: 100%;
    padding-top: 20rpx;
    border-top: 1px solid #eee;
    color: #999;
    font-size: 26rpx;
}
.remark-editor {
    display: flex;
    flex-direction: column;
    height: 80vh;
    box-sizing: border-box;
    padding: 32rpx 28rpx v-bind("safeBottom + 20 + 'px'");
}
.remark-editor-title {
    font-size: 36rpx;
    font-weight: 600;
}
.remark-actions {
    margin-top: auto;
    margin-left: -28rpx;
    margin-right: -28rpx;
    padding: 28rpx 28rpx 0;
    border-top: 1px solid #eee;
}
.remark-editor-header {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 28rpx;
}
.remark-close {
    position: absolute;
    right: 0;
    top: 0;
    padding: 4rpx;
}
.remark-textarea {
    flex: none;
    height: 280rpx;
    background: #f8f8f8;
    border: none;
    border-radius: 20rpx;
}
.remark-textarea :deep(.u-textarea) {
    background: #f8f8f8;
    border: none;
}
.remark-actions :deep(.tig-button) {
    width: 100%;
}
.remark-actions :deep(button) {
    flex: 1;
}
.statistics {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 38rpx 24rpx;
}
.stat {
    color: #999;
    font-size: 24rpx;
    text {
        display: block;
        color: #333;
        font-size: 32rpx;
        line-height: 48rpx;
        font-weight: 600;
    }
}
.stat-label {
    display: inline-flex;
    align-items: center;
    gap: 6rpx;
}
.amount-tip-popup {
    width: 620rpx;
    min-height: 460rpx;
    padding: 72rpx 32rpx 52rpx;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
}
.amount-tip-title {
    color: #333;
    font-size: 32rpx;
    font-weight: 600;
    text-align: center;
}
.amount-tip-content {
    margin-top: 24rpx;
    color: #666;
    font-size: 28rpx;
    line-height: 1.6;
}
.amount-tip-button {
    margin-top: auto;
    width: 100%;
}
.relation {
    padding-top: 0;
}
.relation-title {
    padding: 24rpx 0 18rpx;
    color: #333;
    font-size: 32rpx;
    font-weight: 700;
}
.relation-row {
    display: flex;
    justify-content: space-between;
    gap: 30rpx;
    padding: 24rpx 0;
    color: #333;
    text:last-child {
        color: #999;
        text-align: right;
    }
}
.loading {
    padding: 100rpx 0;
    text-align: center;
    color: #999;
}
</style>
