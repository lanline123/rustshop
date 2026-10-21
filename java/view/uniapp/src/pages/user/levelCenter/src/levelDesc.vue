<template>
    <view class="level_desc_container">
        <!-- 等级说明 -->
        <view class="level_card">
            <view class="card_title">{{ $t("等级说明") }}</view>
            <view class="card_info_wapper">
                <view class="list list_head">
                    <view class="level_icon_label">{{ $t("标识") }}</view>
                    <view class="level_name_label">{{ $t("会员名称") }}</view>
                    <view class="level_condition_label">{{ $t("升级条件") }}</view>
                </view>
                <view class="list">
                    <template v-for="(item, index) in list" :key="item?.rankId">
                        <view class="list_item">
                            <view class="level_icon">
                                <view
                                    class="icon"
                                    :class="`card${item?.rankLevel}`"
                                    :style="{ 'background-image': `url(${staticResource(`user/level_${item?.rankLevel}.png`)})` }"
                                />
                            </view>
                            <view class="level_name">{{ $t(item?.rankName) }}</view>
                            <view class="level_condition">{{ handelGrowth(item) }}</view>
                        </view>
                    </template>
                </view>
            </view>
        </view>
        <view class="level_card">
            <view class="card_title">{{ $t("保级说明") }}</view>
            <view class="card_info_wapper">
                <view class="list list_head">
                    <view class="level_icon_label">{{ $t("标识") }}</view>
                    <view class="level_name_label">{{ $t("会员名称") }}</view>
                    <view class="level_condition_label">{{ $t("保级条件") }}</view>
                </view>
                <view class="list">
                    <!-- <template v-for="(item, index) in relegationRule" :key="index">
                        <view class="list_item">
                            <view class="level_name">{{ $t(item?.rankName) }}</view>
                            <view class="level_condition line1">{{ $t(item?.condition) }}</view>
                        </view>
                    </template> -->
                    <template v-for="(item, index) in list" :key="item?.rankId">
                        <view class="list_item">
                            <view class="level_icon">
                                <view
                                    class="icon"
                                    :class="`card${item?.rankLevel}`"
                                    :style="{ 'background-image': `url(${staticResource(`user/level_${item?.rankLevel}.png`)})` }"
                                />
                            </view>
                            <view class="level_name">{{ $t(item?.rankName) }}</view>
                            <view class="level_condition line1">{{ handelGrowth(item) }}</view>
                        </view>
                    </template>
                </view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { ref, watchEffect } from "vue";
import { staticResource } from "@/utils";
import { useI18n } from "vue-i18n";
import type { Rank } from "@/types/user/level";
import type { PropType } from "vue";

const { t } = useI18n();
const props = defineProps({
    list: {
        type: Array as PropType<Rank[]>,
        default: () => []
    },
    rankConfig: {
        type: Object,
        default: () => {}
    }
});

const relegationRule = ref<any[]>([]);

const handleRelegationRuleData = (level: Rank[], rankConfig: any) => {
    level?.forEach((item) => {
        if (rankConfig?.data?.type === 1) {
            relegationRule.value.push({ ...item, condition: t("不降级") });
        } else if (rankConfig?.data?.type === 2) {
            relegationRule.value.push({
                ...item,
                condition: rankConfig.data?.rankAfterMonth + t("个月后，按近") + rankConfig.data?.useMonth + t("个月成长值重新计算")
            });
        }
    });
};

watchEffect(() => {
    handleRelegationRuleData(props.list, props.rankConfig);
});
const handelGrowth = (item: Rank) => {
    const growth = item?.minGrowthPoints;
    if (growth === 0) {
        return t("无门槛");
    }
    // rankType === 2 为消费模式，其余为成长值模式
    if (item?.rankType === 2) {
        return t("消费满") + growth;
    }
    return growth + t("成长值");
};
</script>
<style scoped lang="scss">
.level_desc_container {
    display: flex;
    flex-direction: column;
    row-gap: 24rpx;
    margin-top: 32rpx;
    .level_card {
        width: 100%;
        padding: 40rpx 24rpx;
        background: #fff;
        border-radius: 14rpx;
        .card_title {
            color: #323233;
            font-size: 32rpx;
            font-weight: bold;
            margin-bottom: 48rpx;
            text-align: center;
        }
        .list {
            width: 100%;
            .list_item {
                display: flex;
                height: 72rpx;
                font-size: 24rpx;
                color: #724804;
                text-align: center;
                &:nth-child(2n) {
                    background-color: #fcf5e8;
                }
                .level_icon {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 72rpx;
                    width: 96rpx;
                    padding: 20rpx;
                    .icon {
                        width: 44rpx;
                        height: 32rpx;
                        background-repeat: no-repeat;
                        background-size: 100% 100%;
                    }
                }

                .level_name {
                    width: 184rpx;
                    // padding: 20rpx;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                .level_condition {
                    text-align: left;
                    flex: 1;
                    min-width: 184rpx;
                    // padding: 20rpx;
                    white-space: nowrap;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
            }
        }
        .list_head {
            display: flex;
            align-items: center;
            font-size: 24rpx;
            text-align: center;
            color: #724804;
            background-color: #fcf5e8;
            .level_icon_label {
                width: 96rpx;
                padding: 20rpx;
            }
            .level_name_label {
                width: 184rpx;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 20rpx;
            }
            .level_condition_label {
                text-align: left;
                flex: 1;
                min-width: 184rpx;
                display: flex;
                align-items: center;
                justify-content: center;
                // padding: 20rpx;
            }
        }
    }
}
</style>
