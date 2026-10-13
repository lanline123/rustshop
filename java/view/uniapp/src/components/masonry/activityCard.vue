<template>
    <view class="promotion" :class="{ subbg: cardType != 'list', 'promotion-disabled': activityInfo.canJoin === false }" :style="cssVars">
        <view v-if="activityInfo.type && activityInfo.type != 2 && getActivityText(activityInfo.type)" class="activity">{{
            $t(getActivityText(activityInfo.type))
        }}</view>
        <view v-if="activityInfo.type && activityInfo.type == 2" class="ticket">
            <view class="ticket-type"
                >{{ $t("券") }}
                <text class="ticket-line" />
            </view>
            <view class="ticket-content">{{ activityInfo.data.promotionDesc }}</view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { computed } from "vue";
import { adjustBrightness } from "@/utils/themeColor";

const { t } = useI18n();
const props = defineProps({
    activityInfo: {
        type: Object,
        default: () => ({})
    },
    cardType: {
        type: String,
        default: "list"
    },
    fontSize: {
        type: String,
        default: "23rpx"
    },
    /** 主题色（装修下发；空则回落站点 --tag-*） */
    themeColor: {
        type: String,
        default: ""
    }
});

const isCssVar = (color: string): boolean => /^var\(/.test(color);

/** CSS 变量：跟随主题色（对齐 vortmall activityCard） */
const cssVars = computed(() => {
    if (!props.themeColor) return {};
    if (isCssVar(props.themeColor)) {
        return {
            "--activity-theme-color": props.themeColor,
            "--activity-theme-color-80": "var(--ump-border)",
            "--activity-theme-bg": "var(--tag-bg)"
        };
    }
    return {
        "--activity-theme-color": props.themeColor,
        "--activity-theme-color-80": adjustBrightness(props.themeColor, 0.8),
        "--activity-theme-bg": adjustBrightness(props.themeColor, 0.9)
    };
});

//1秒杀2优惠券3满减4折扣5满赠6限时折扣
type ActivityInfo = {
    [key: number]: string;
};
const activitType: ActivityInfo = {
    1: t("秒杀价"),
    2: "券",
    3: t("满减"),
    4: t("折扣"),
    5: t("满赠"),
    6: t("限时折扣")
};
const getActivityText = (type: number) => {
    return activitType[type];
};
</script>

<style lang="scss" scoped>
.promotion {
    padding-bottom: 2rpx;
    overflow: hidden;
    font-size: v-bind("props.fontSize");
    display: flex;
    margin-bottom: 20rpx;
    margin-right: 5rpx;

    .activity {
        max-width: 250rpx;
        /* 有主题色时用派生色；未传时保持原 solid 主色 */
        background-color: var(--activity-theme-bg, var(--general));
        color: var(--activity-theme-color, var(--main-text));
        padding: 0 8rpx;
        border-radius: 5rpx;
        margin-right: 10rpx;
        white-space: nowrap;
    }
    .ticket {
        display: flex;
        background-color: var(--activity-theme-bg, var(--tag-bg));
        color: var(--activity-theme-color, var(--tag-text));
        border-radius: 5rpx;
        padding-bottom: 2rpx;
        width: 100%;

        .ticket-type {
            padding: 0 6rpx;
            // max-width: 50rpx;
            position: relative;

            .ticket-line {
                position: absolute;
                display: inline-block;
                height: 100%;
                opacity: 0.5;
                width: 2px;
                top: 0;
                right: 0rpx;
                transform: scaleY(0.7);
                background: linear-gradient(
                    to bottom,
                    var(--activity-theme-color-80, var(--ump-border, #c9c9ff)) 20%,
                    var(--activity-theme-color-80, var(--ump-border, #c9c9ff)) 20%,
                    var(--activity-theme-color-80, var(--ump-border, #c9c9ff)) 20%,
                    rgba(255, 255, 255, 0) 20%,
                    rgba(255, 255, 255, 0) 35%,
                    var(--activity-theme-color-80, rgba(136, 76, 255, 1)) 35%,
                    var(--activity-theme-color-80, var(--ump-border, #c9c9ff)) 35%,
                    var(--activity-theme-color-80, var(--ump-border, #c9c9ff)) 35%,
                    var(--activity-theme-color-80, var(--ump-border, #c9c9ff)) 60%,
                    rgba(255, 255, 255, 0) 60%,
                    rgba(255, 255, 255, 0) 55%,
                    rgba(255, 255, 255, 0) 75%,
                    var(--activity-theme-color-80, var(--ump-border, #c9c9ff)) 75%,
                    var(--activity-theme-color-80, var(--ump-border, #c9c9ff)) 75%,
                    var(--activity-theme-color-80, var(--ump-border, #c9c9ff)) 100%
                );
            }
        }
        .ticket-content {
            padding: 0 8rpx;
            max-width: 200rpx;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    }
    &.subbg {
        margin: 10rpx;
        margin-bottom: 0;
        margin-left: 0;
        padding: 0;
        .activity {
            background-color: var(--activity-theme-bg, var(--tag-bg));
            color: var(--activity-theme-color, var(--tag-text));
        }
    }
    &.promotion-disabled {
        opacity: 0.5;
    }
}
</style>
