<template>
    <view class="group-list" :style="{ padding: padding }">
        <view v-for="item in groups" :key="item.grouponPromotionRecordId" class="group-item" :class="{ border: bottomBorder }">
            <view class="group-item-info" @click="showDetail(item)">
                <view class="info-image">
                    <tig-image :src="imageFormat(item.joinUser.avatar || configStore.defaultAvatar)" mode="aspectFill" />
                </view>
                <text class="info-name">{{ hideUserName(item.joinUser.username) }}</text>
            </view>
            <view class="group-item-operation">
                <view class="operation-time">
                    <view class="person">
                        <text>{{ $t("还差") }}</text>
                        <text class="num">{{ item.joinNumCount > item.minGroupNum ? 0 : item.minGroupNum - item.joinNumCount }}</text>
                        <text>{{ $t("人") }}</text>
                    </view>
                    <view class="countdown-box">
                        <template v-if="!isGroupEnd(item)">
                            <text>{{ $t("剩余") }}</text>
                            <!-- <tig-countdown color="#9A9A9A" splitor-color="#9A9A9A" :end-time="item.endTime" /> -->
                            <view class="countdown">
                                <commonCountdown countdown-type="specifyTime" :specify-time="dateStringToTimestamp(item.endTime)">
                                    <template #default="{ days, hours, minutes, seconds }">
                                        <view class="countdown-text">
                                            <text v-if="days">{{ days }}</text>
                                            <text v-if="days">{{ $t("天") }}&nbsp;</text>
                                            <text class="countdown-bg">{{ formatTime(hours) }}</text>
                                            <text>&nbsp;:&nbsp;</text>
                                            <text class="countdown-bg">{{ formatTime(minutes) }}</text>
                                            <text>&nbsp;:&nbsp;</text>
                                            <text class="countdown-bg">{{ formatTime(seconds) }}</text>
                                        </view>
                                    </template>
                                </commonCountdown>
                            </view>
                            <text>{{ $t("结束") }}</text>
                        </template>
                        <text v-else>{{ $t("已结束") }}</text>
                    </view>
                </view>
                <view>
                    <tig-button
                        color="var(--main-bg)"
                        :disabled="isMyGroup(item) || isGroupEnd(item)"
                        :custom-style="{ width: '136rpx', height: '59rpx', borderRadius: '6rpx', fontSize: '28rpx' }"
                        @click="handleJoinClick(item)"
                        >{{ $t("去参团") }}</tig-button
                    >
                </view>
            </view>
        </view>
    </view>
</template>
<script setup lang="ts">
/**
 * 拼团卡片组件
 * - 显示发起人头像和参团人数
 * - 倒计时显示还差几人成团
 * - 一键参团按钮
 */
import { imageFormat, hideUserName } from "@/utils/format";
import type { GrouponRecordItem } from "@/types/groupon/groupon";
import commonCountdown from "@/components/commonCountdown/index.vue";
import { redirect } from "@/utils";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";
const userStore = useUserStore();

interface IProps {
    groups: GrouponRecordItem[];
    padding: string;
    bottomBorder: boolean;
}

interface IEmits {
    (e: "joinClick", item: GrouponRecordItem): void;
}

withDefaults(defineProps<IProps>(), {
    groups: () => [],
    padding: "0 40rpx",
    bottomBorder: false
});

const emit = defineEmits<IEmits>();

const configStore = useConfigStore();

/*时间字符串转时间戳 */
const dateStringToTimestamp = (dateString: any) => {
    const dateParts = dateString.split(" ");
    const [year, month, day] = dateParts[0].split("-");
    const [hour, minute, second] = dateParts[1].split(":");
    const date = new Date(year, month - 1, day, hour, minute, second);
    return date.getTime();
};

const formatTime = (time: string): string => {
    return time?.toString().padStart(2, "0") || "00";
};

/**
 * 团是否结束了
 */
const isGroupEnd = (item: GrouponRecordItem) => {
    // 不是待成团的状态，拼团结束了
    if (item.joinStatus !== 1) {
        return true;
    }
    if (!item.endTime) {
        return true;
    }
    const endTime = dateStringToTimestamp(item.endTime);
    return endTime < new Date().getTime();
};

const isMyGroup = (item: GrouponRecordItem) => {
    // 如果joinUser是数组
    if (Array.isArray(item.joinUser)) {
        return item.joinUser.some((user) => user.leader && user.userId === userStore.userInfo?.userId);
    }
    return item.joinUser.leader && item.joinUser.userId === userStore.userInfo?.userId;
};

/*
 * 点击去参团
 */
const handleJoinClick = (item: GrouponRecordItem) => {
    emit("joinClick", item);
};

/**
 * 展示拼团详情
 * @param item 点击的拼团项目
 */
const showDetail = (item: GrouponRecordItem) => {
    redirect({
        url: `/pages/product/groupBuying?grouponPromotionRecordId=${item.grouponPromotionRecordId}`
    });
};
</script>

<style scoped lang="scss">
.group-list {
    flex: 1;
}

.group-item {
    display: flex;
    justify-content: space-between;
    // margin-top: 73rpx;
    padding: 36rpx 0;
    // padding-bottom: 10rpx;

    &:first-child {
    }

    &.border:not(:last-child) {
        border-bottom: 1rpx solid #f5f5f5;
    }

    &-info {
        display: flex;
        align-items: center;
        gap: 15rpx;
        flex-grow: 0;
        .info-name {
            font-weight: 500;
            font-size: 28rpx;
            color: #1a1a1a;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 150rpx;
        }
    }

    &-operation {
        display: flex;
        align-items: center;
        gap: 0 14rpx;
        flex-grow: 0;

        .operation-time {
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            gap: 14rpx;
            font-size: 24rpx;

            .person {
                font-weight: bold;
                text-align: right;
                display: flex;
                align-items: center;
                gap: 4rpx;

                .num {
                    font-weight: bold;
                    color: var(--main-bg);
                }
            }

            .countdown-box {
                font-weight: 500;
                font-size: 24rpx;
                color: #9a9a9a;
                display: flex;
                flex-wrap: wrap;

                uni-text {
                    white-space: nowrap;
                }

                .countdown {
                    padding: 0 3rpx;
                    white-space: nowrap;
                }
            }
        }
    }
}

.info-image {
    width: 72rpx;
    height: 72rpx;
    background: #ddecff;
    border-radius: 50%;
    overflow: hidden;
}
</style>
