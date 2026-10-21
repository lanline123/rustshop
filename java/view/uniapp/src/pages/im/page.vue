<template>
    <view class="page-content">
        <tig-layout :title="''" :bg-color="'#f6f6f6'">
            <im-nav :title="titleText" :background="'#f6f6f6'" :border-bottom="false" @read-all="handleReadAll" />
            <!-- <tig-navbar :title="`${titleText}`" :background="'#f6f6f6'" :border-bottom="false">
                <template #right>
                    <view class="read-all" @click="handleReadAll">
                        <image class="clear" :src="staticResource('im/clear.png')" mode="aspectFill" />
                        <text class="clear-text">全部已读</text>
                    </view>
                </template>
            </tig-navbar> -->
            <view class="content-wrapper">
                <view class="card system-card">
                    <view v-for="(sys, index) in systemMessages" :key="index" class="system-item" @click="handleSystemClick(sys)">
                        <view class="icon-box">
                            <image class="icon-image" :src="staticResource(sys.icon)" mode="aspectFit" />
                            <u-badge
                                v-if="sys.badge > 0 || sys.dot"
                                :is-dot="sys.dot"
                                :value="sys.badge"
                                type="error"
                                bg-color="#e12c2b"
                                absolute
                                :offset="[-2, -2]"
                            />
                        </view>
                        <view class="sys-content">
                            <text class="sys-title">{{ sys.title }}</text>
                        </view>
                        <u-icon name="arrow-right" color="#333333" size="16" />
                    </view>
                </view>
                <view class="card chat-card">
                    <template v-if="msgList.length > 0">
                        <view v-for="(item, index) in msgList" :key="index" class="chat-item" @click="handleLink(item)">
                            <view class="avatar-box">
                                <image
                                    class="avatar"
                                    :src="
                                        item.shop
                                            ? staticResource(item.shop.shopLogo)
                                            : imageFormat(configStore.baseInfo.icoImg) || staticResource('im/default_shop.png')
                                    "
                                    mode="aspectFit"
                                />
                                <u-badge type="error" max="99" :value="item.unreadServantMessageCount || 0" absolute :offset="[-2, -2]" />
                            </view>

                            <view class="chat-content">
                                <view class="top-row">
                                    <view class="name-box">
                                        <text class="name">{{ item.shop ? item.shop.shopTitle : configStore.baseInfo.shopTitle || $t("平台自营") }}</text>
                                        <view v-if="isOfficial(item)" class="official-tag">{{ $t("官方") }}</view>
                                    </view>
                                    <text class="time">{{ formatTime(item.lastUpdateTime) }}</text>
                                </view>
                                <view class="bottom-row">
                                    <text class="msg-preview">{{ getMessagePreview(item) }}</text>
                                </view>
                            </view>
                        </view>
                    </template>
                    <template v-if="msgList.length === 0 && isLoadEnd">
                        <view class="empty-box">
                            <u-empty mode="message" :text="$t('暂无消息')" />
                        </view>
                    </template>
                </view>
            </view>
        </tig-layout>
    </view>
</template>

<script lang="ts" setup>
/**
 * 消息页面（Tab页）
 * - 消息中心Tab展示
 * - 会话列表和未读数
 */
import { ref, reactive } from "vue";
import { onShow, onReachBottom } from "@dcloudio/uni-app";
import { getConversation } from "@/api/im/im";
import { imageFormat } from "@/utils/format";
import { useConfigStore } from "@/store/config";
import { staticResource } from "@/utils";
import { getUnreadCount } from "@/api/user/user";
import { setAllMessageRead, updateOrderMessageRead } from "@/api/user/messageLog";
import { setRead } from "@/api/im/im";
import imNav from "./src/imNav.vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const configStore = useConfigStore();
const token = uni.getStorageSync("token");

const filterParams = reactive({
    page: 1,
    size: 15
});
const total = ref(0);
const isLoadEnd = ref(false);
const msgList = ref<any[]>([]);

const systemMessages = ref<any[]>([
    { title: t("订单通知"), icon: "im/order-notification.png", badge: 0, dot: false, path: "/pages/user/ordermessage/index", type: 1 },
    { title: t("站内消息"), icon: "im/in-site-message.png", badge: 0, dot: false, path: "/pages/user/messageLog/index", type: 2 },
    { title: t("资讯中心"), icon: "im/consult.png", badge: 0, dot: false, path: "/pages/article/news/list?id=bzgg", type: 3 }
]);

const _getConversation = async () => {
    try {
        const result = await getConversation(filterParams);
        msgList.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        console.error(error);
    } finally {
        isLoadEnd.value = true;
    }
};

const handleLink = (item: any) => {
    if (item.unreadServantMessageCount && item.unreadServantMessageCount > 0) {
        _setRead(item.shopId, item.lastServantId);
    }
    uni.navigateTo({
        url: `/pages/im/index?token=${token}&shopId=${item.shopId}&conversationId=${item.conversationId}`
    });
};

const handleSystemClick = async (item: any) => {
    if (item.type == 1) {
        await updateOrderMessageRead();
    }
    uni.navigateTo({
        url: item.path
    });
};

const handleReadAll = async () => {
    try {
        await setAllMessageRead();
        _getUnreadCount();
        _getConversation();
    } catch (error: any) {
        console.error(error);
    }
};

const getMessagePreview = (item: any) => {
    if (!item.lastMessage || item.lastMessage.length === 0) return "";
    const msg = item.lastMessage[0];
    if (msg.messageType == "text") {
        return msg.content.content.replace(/<[^>]+>/g, "");
    }
    return `[${msg.messageTypeText}]`;
};

const isOfficial = (item: any) => {
    return item.shopId == 0;
};

const formatTime = (timeStr: string) => {
    if (!timeStr) return "";
    const date = new Date(timeStr);
    const now = new Date();
    if (isNaN(date.getTime())) return timeStr;

    const isToday = date.getDate() === now.getDate() && date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear();

    if (isToday) {
        return `${date.getHours().toString().padStart(2, "0")}:${date.getMinutes().toString().padStart(2, "0")}`;
    } else {
        const yesterday = new Date(now);
        yesterday.setDate(yesterday.getDate() - 1);
        if (date.getDate() === yesterday.getDate() && date.getMonth() === yesterday.getMonth() && date.getFullYear() === yesterday.getFullYear()) {
            return `${t("昨天")} ${date.getHours().toString().padStart(2, "0")}:${date.getMinutes().toString().padStart(2, "0")}`;
        }
        return `${date.getMonth() + 1}-${date.getDate()}`;
    }
};
const titleText = ref<string>("");
const _getUnreadCount = async () => {
    const result = await getUnreadCount();
    systemMessages.value[0].badge = result.orderMessageCount > 999 ? 999 : result.orderMessageCount;
    systemMessages.value[1].dot = result.userMessageCount > 0;
    titleText.value = result.imMessageCount > 0 ? `${t("消息")}(${result.imMessageCount})` : t("消息");
};

const _setRead = async (shopId: number, servantId: number) => {
    try {
        await setRead({
            shopId: shopId,
            servantId: servantId
        });
    } catch (error: any) {
        console.error(error);
    }
};

onShow(() => {
    _getUnreadCount();
    _getConversation();
});

onReachBottom(() => {
    if (filterParams.page < Math.ceil(total.value / filterParams.size)) {
        filterParams.page++;
        _getConversation();
    }
});
</script>

<style scoped lang="scss">
.page-content {
    min-height: 100vh;
    width: 100%;
    background-color: #f6f6f6;
    box-sizing: border-box;
}

.read-all {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    font-size: 20rpx;
    margin-top: 8rpx;
    .clear {
        width: 25rpx;
        height: 25rpx;
        margin-bottom: 5rpx;
    }
}

.content-wrapper {
    padding: 20rpx 0 0;
    display: flex;
    flex-direction: column;
    min-height: calc(100vh - var(--nav-height));
    box-sizing: border-box;
}

.card {
    background: #fff;
    overflow: hidden;
    margin-bottom: 20rpx;
    padding: 0 30rpx;

    &.chat-card {
        flex: 1;
        display: flex;
        flex-direction: column;
        margin-bottom: 0;
    }
}

.system-item {
    display: flex;
    align-items: center;
    padding: 30rpx 0;
    position: relative;
    border-bottom: 1rpx solid #e5e5e5;

    &:last-child {
        border-bottom: none;
    }

    .icon-box {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-right: 24rpx;
        position: relative;
        flex-shrink: 0;
        .icon-image {
            width: 92rpx;
            height: 92rpx;
        }
    }

    .sys-content {
        flex: 1;
        font-size: 30rpx;
        color: #333;
        font-weight: 500;
    }
}

.chat-item {
    display: flex;
    padding: 30rpx 0;
    position: relative;
    background-color: #fff;
    border-bottom: 1rpx solid #e5e5e5;

    &:last-child {
        border-bottom: none;
    }

    .avatar-box {
        width: 90rpx;
        height: 90rpx;
        margin-right: 24rpx;
        position: relative;
        flex-shrink: 0;

        .avatar {
            width: 100%;
            height: 100%;
            border-radius: 50%;
            background-color: #eee;
        }
    }

    .chat-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        overflow: hidden;
        padding-top: 4rpx;
        padding-bottom: 4rpx;

        .top-row {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .name-box {
                display: flex;
                align-items: center;
                flex: 1;
                overflow: hidden;
                margin-right: 20rpx;

                .name {
                    font-size: 30rpx;
                    color: #1e1e1e;
                    font-weight: 500;
                    margin-right: 12rpx;
                    overflow: hidden;
                    white-space: nowrap;
                    text-overflow: ellipsis;
                }

                .official-tag {
                    font-size: 20rpx;
                    color: #f4290d;
                    border: 1rpx solid #f4290d;
                    padding: 1rpx 6rpx;
                    border-radius: 4rpx;
                    line-height: 1;
                    flex-shrink: 0;
                    background-color: rgba(255, 85, 0, 0.05);
                }
            }

            .time {
                font-weight: 400;
                font-size: 22rpx;
                color: #333333;
                flex-shrink: 0;
            }
        }

        .bottom-row {
            .msg-preview {
                font-size: 26rpx;
                color: #999;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
                display: block;
                width: 78%;
            }
        }
    }
}

.empty-box {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}
</style>
