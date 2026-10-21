<template>
    <tig-layout :title="$t('站内信')">
        <view class="message-main">
            <view class="head-tabs-box">
                <view class="tabs flex align-center justify-between">
                    <view class="flex align-end">
                        <view class="tab" :class="{ active: filterParams.unread === 0 }" @click="readReadFn(0)">{{ $t("全部消息") }}</view>
                        <view class="tab" :class="{ active: filterParams.unread === 1 }" @click="readReadFn(1)">{{ $t("只看未读") }}</view>
                    </view>
                    <view class="clear-unread flex align-end" @click="addMessageAllReadFn">
                        <text>{{ $t("一键已读") }}</text>
                        <text class="iconfont-h5 icon-auth" style="font-size: 36rpx; color: #999" />
                    </view>
                </view>
            </view>
            <view v-if="messageList.length > 0" class="messages-list">
                <uni-swipe-action>
                    <block v-for="(item, index) in messageList" :key="item.messageId">
                        <view class="move-item">
                            <uni-swipe-action-item :threshold="0" auto-close>
                                <view class="mitem" @click="__addMessageRead(item)">
                                    <view class="message">
                                        <view class="title">
                                            <text v-if="item.isRead === 0" class="dot" />
                                            <text>{{ item.title }}</text>
                                        </view>
                                        <view class="txt">{{ item.content }}</view>
                                        <view class="time">{{ item.addTime }}</view>
                                    </view>
                                </view>
                                <template #right>
                                    <view class="message-move-box">
                                        <view class="btn-del" @click="__delMessage(item.messageId, index)"
                                            ><text>{{ $t("删除") }}</text></view
                                        >
                                    </view>
                                </template>
                            </uni-swipe-action-item>
                        </view>
                    </block>
                </uni-swipe-action>
            </view>
            <view v-if="!isLoading && total === 0" class="empty-box">
                <view class="pictrue"><image :src="staticResource('common/data_empty.png')" /></view>
                <view class="txt">{{ $t("暂无站内消息！") }}</view>
            </view>
            <loading-box :model-value="!isLoading" :page="filterParams.page" :length="messageList.length" />
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 站内信列表页
 * - 全部消息/只看未读 Tab切换
 * - 支持一键已读
 * - 左滑删除消息
 * - 点击消息标记已读并跳转链接
 */
import { reactive } from "vue";
import { getMessageList, addMessageRead, addMessageAllRead, delMessage } from "@/api/user/messageLog";
import type { UserMsgFilterState } from "@/types/user/messageLog";
import { staticResource, redirect } from "@/utils";
import { useList } from "@/hooks";
import { useI18n } from "vue-i18n";
import { urlFormat } from "@/utils/format";
const { t } = useI18n();

const filterParams = reactive({
    //初使化用于查询的参数
    page: 1,
    size: 10,
    unread: 0
});

const {
    data: messageList,
    isLoading,
    isLoadMore,
    total,
    resetState,
    getList
} = useList<UserMsgFilterState>(getMessageList, {
    params: filterParams,
    path: {
        dataKey: "records"
    },
    immediate: true
});

const __delMessage = (id: number, index: number) => {
    uni.showModal({
        title: t("提示"),
        content: t("确定删除此条站内信吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (res.confirm) {
                deleteMsg(id, index);
            }
        }
    });
};

const deleteMsg = async (id: number, index: number) => {
    try {
        await delMessage({ id: id });
        resetState();
        await getList();
        uni.showToast({
            title: t("删除成功"),
            icon: "none"
        });
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    }
};

const __addMessageRead = async (data: UserMsgFilterState) => {
    try {
        if (data.isRead !== 1) {
            await addMessageRead(data.messageId);
            data.isRead = 1;
        }

        redirect({
            url: urlFormat(data.link)
        });
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    }
};

const addMessageAllReadFn = async () => {
    if (isLoading.value || isLoadMore.value) return;
    try {
        await addMessageAllRead();
        filterParams.unread = 0;
        resetState();
        await getList();
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    }
};

const readReadFn = async (type: number) => {
    if (isLoading.value || isLoadMore.value) return;
    filterParams.unread = type;
    try {
        resetState();
        await getList();
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    }
};
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
            font-size: 22rpx;
            margin-right: 40rpx;
            color: #666;
        }
        .active {
            color: var(--general);
            font-weight: bold;
            font-size: 26rpx;
        }
        .clear-unread {
            font-size: 26rpx;
            color: #999;
        }
    }
}

.messages-list {
    position: relative;
    padding: 0 20rpx;
    margin-top: 120rpx;
    .move-item {
        margin-bottom: 10rpx;
    }
    .mitem {
        position: relative;
        border-bottom: 1rpx solid #dfdfdf;
        border-radius: 10rpx;
        .message {
            position: relative;
            padding: 30rpx 20rpx;
            background-color: #fff;
            border-radius: 20rpx;
            .title {
                white-space: nowrap; /* 不换行 */
                overflow: hidden; /* 隐藏多余的文字 */
                text-overflow: ellipsis; /* 超出部分显示省略号 */
                position: relative;
                height: 46rpx;
                color: #333;
                font-weight: 700;
                font-size: 32rpx;
                display: flex;
                width: 430rpx;
                .dot {
                    display: inline-block;
                    width: 10rpx;
                    height: 10rpx;
                    border-radius: 100rpx;
                    background-color: var(--general);
                    margin-right: 10rpx;
                }
            }
            .txt {
                color: #999;
                line-height: 36rpx;
                padding-top: 10rpx;
                font-size: 24rpx;
            }
            .time {
                position: absolute;
                top: 20rpx;
                right: 20rpx;
                color: #666;
                font-weight: 400;
                font-size: 22rpx;
            }
        }
    }
    .unread .message {
        background-color: #f4f4f4;
        box-shadow: 0 0 8rpx #e7e7e7;
    }
}
.message-move-box {
    width: 150rpx;
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
