<template>
    <view class="page-content">
        <tig-layout :title="''" :bg-color="'#f6f6f6'">
            <tig-navbar :title="$t('订单通知')" />
            <view class="msg-list">
                <view v-for="(item, index) in messageList" :key="index" class="msg-item" @click="__addMessageRead(item)">
                    <view class="header">
                        <view class="store-info">
                            <image
                                v-if="item.orderInfo?.shop"
                                class="avatar"
                                :src="item.orderInfo?.shop.shopLogo || staticResource('im/self_shop.png')"
                                mode="aspectFill"
                            />
                            <image v-else class="avatar" :src="staticResource('im/default_shop.png')" mode="aspectFill" />
                            <text v-if="item.orderInfo?.shop" class="name">{{ $t(item.orderInfo?.shop.shopTitle) }}</text>
                            <text v-else class="name">{{ $t(item.orderInfo?.shopName || "平台自营") }}</text>
                        </view>
                        <text v-if="item.addTime" class="time">{{ formatTime(item.addTime) }}</text>
                    </view>

                    <view class="content">
                        <view class="info">
                            <view class="msg-title">{{ item.title }}</view>
                            <view class="row">
                                <text class="label">{{ $t("店铺名称") }}</text>
                                <text v-if="item.orderInfo?.shop" class="name">{{ $t(item.orderInfo?.shop.shopTitle) }}</text>
                                <text v-else class="name">{{ $t(item.orderInfo?.shopName || "平台自营") }}</text>
                            </view>
                            <view class="row">
                                <text class="label">{{ $t("订单状态") }}</text>
                                <text class="value">{{ item.orderInfo?.orderStatusName }}</text>
                            </view>
                        </view>
                        <image class="product-img" :src="productImage(item?.orderInfo?.items?.[0])" mode="aspectFill" />
                    </view>

                    <view class="footer">
                        <view class="btn" @click="__addMessageRead(item)">{{ $t("查看详情") }}</view>
                    </view>
                </view>
            </view>

            <template v-if="messageList.length === 0 && !isLoading">
                <view class="empty-box">
                    <u-empty mode="message" :text="$t('暂无消息')" />
                </view>
            </template>
        </tig-layout>
    </view>
</template>

<script lang="ts" setup>
/**
 * 订单通知消息列表页
 * - 展示与订单相关的通知消息
 * - 显示店铺信息、订单状态、商品图片
 * - 点击查看详情跳转对应链接
 */
import { reactive } from "vue";
import { getMessageList, addMessageRead } from "@/api/user/messageLog";
import type { UserMsgFilterState } from "@/types/user/messageLog";
import { staticResource, redirect } from "@/utils";
import { useList } from "@/hooks";
import { urlFormat } from "@/utils/format";
import { useI18n } from "vue-i18n";
import { productImage } from "@/utils/format";

const { t } = useI18n();

// 格式化时间显示：今天、昨天或原始时间
const formatTime = (dateStr: string): string => {
    if (!dateStr) return "";

    const inputDate = new Date(dateStr.replace(/-/g, "/"));
    const today = new Date();
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);

    // 获取日期部分进行比较（去掉时分秒）
    const inputDateStr = inputDate.toDateString();
    const todayStr = today.toDateString();
    const yesterdayStr = yesterday.toDateString();

    if (inputDateStr === todayStr) {
        return t("今天");
    } else if (inputDateStr === yesterdayStr) {
        return t("昨天");
    }
    return dateStr;
};

const filterParams = reactive({
    //初使化用于查询的参数
    page: 1,
    size: 10,
    unread: 0,
    type: 1
});

const { data: messageList, isLoading: isLoading } = useList<UserMsgFilterState>(getMessageList, {
    params: filterParams,
    path: {
        dataKey: "records"
    },
    immediate: true
});

// const __delMessage = (id: number, index: number) => {
//     uni.showModal({
//         title: t("提示"),
//         content: t("确定删除此条站内信吗？"),
//         success: async (res) => {
//             if (res.confirm) {
//                 deleteMsg(id, index);
//             }
//         }
//     });
// };

// const deleteMsg = async (id: number, index: number) => {
//     try {
//         await delMessage({ id: id });
//         resetState();
//         await getList();
//         uni.showToast({
//             title: t("删除成功"),
//             icon: "none"
//         });
//     } catch (error: any) {
//         uni.showToast({
//             title: error.message,
//             icon: "none",
//             duration: 1000
//         });
//     }
// };

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
</script>

<style scoped lang="scss">
.msg-list {
    padding: 20rpx;
}

.msg-item {
    background-color: #fff;
    border-radius: 14rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;

    .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-bottom: 20rpx;
        margin-bottom: 20rpx;
        border-bottom: 1rpx solid #f4f4f4;

        .store-info {
            display: flex;
            align-items: center;

            .avatar {
                width: 40rpx;
                height: 40rpx;
                border-radius: 4rpx;
                margin-right: 12rpx;
            }

            .name {
                font-size: 26rpx;
                color: #2a2a2a;
            }
        }

        .time {
            font-size: 24rpx;
            color: #8c8c8c;
        }
    }
    .content {
        display: flex;
        justify-content: space-between;
        margin-bottom: 24rpx;

        .info {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: flex-start;

            .msg-title {
                font-size: 34rpx;
                font-weight: bold;
                color: #333;
                margin-bottom: 20rpx;
            }

            .row {
                display: flex;
                margin-bottom: 15rpx;
                font-size: 26rpx;

                &:last-child {
                    margin-bottom: 0;
                }

                .label {
                    color: #9a9a9a;
                    margin-right: 15rpx;
                    width: 110rpx;
                }

                .value {
                    color: #6a6a6a;
                    flex: 1;
                }
            }
        }

        .product-img {
            width: 120rpx;
            height: 120rpx;
            border-radius: 8rpx;
            margin-left: 20rpx;
            background-color: #f0f0f0;
        }
    }

    .footer {
        display: flex;
        justify-content: flex-start;

        .btn {
            background-color: #ffebf1;
            color: #ff0f23;
            font-size: 24rpx;
            font-weight: bold;
            padding: 8rpx 24rpx;
            border-radius: 8rpx;
        }
    }
}
</style>
