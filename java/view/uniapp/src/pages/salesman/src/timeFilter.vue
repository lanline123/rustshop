<template>
    <view>
        <view class="time-filter">
            <view class="search-box">
                <view class="status-bar" />
                <view class="search-input-box">
                    <view class="input">
                        <searchInput
                            v-model="filterParams.keyword"
                            style="width: 100%"
                            :placeholder="displayPlaceholder"
                            @confirm="$emit('change')"
                            @clear="$emit('change')"
                        />
                    </view>
                </view>
            </view>
            <view class="time-filter__inner">
                <view class="time-filter-tags">
                    <view class="time-filter-tags__item" :class="{ 'time-filter-tags__item--active': dateTabActive === 0 }" @click="dateTab(0)">{{
                        $t("全部")
                    }}</view>
                    <view class="time-filter-tags__item" :class="{ 'time-filter-tags__item--active': dateTabActive === 1 }" @click="dateTab(1)">{{
                        $t("今日")
                    }}</view>
                    <view class="time-filter-tags__item" :class="{ 'time-filter-tags__item--active': dateTabActive === 2 }" @click="dateTab(2)">{{
                        $t("昨日")
                    }}</view>
                    <view class="time-filter-tags__item" :class="{ 'time-filter-tags__item--active': dateTabActive === 3 }" @click="dateTab(3)"
                        >{{ $t("近七日") }}
                    </view>
                </view>
                <view class="time-filter-custom" @click="showSelectDate = true">
                    <block v-if="timeDates.length">
                        <view class="time-filter__txt">
                            <view>{{ timeDates[0] }}~</view>
                            <view v-if="timeDates[1]">{{ timeDates[1] }}</view>
                        </view>
                    </block>
                    <block v-else>{{ $t("自定义时间") }}</block>
                    <up-icon name="arrow-down" color="#969799" size="8" />
                </view>
            </view>
        </view>
        <selectDate v-model="showSelectDate" @confirm="confirm" />
    </view>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";

/**
 * 时间筛选组件
 * - 搜索框和日期选择组合
 * - 用于订单、业绩等列表筛选
 */
import { computed, ref } from "vue";
import searchInput from "./searchInput.vue";
import selectDate from "./selectDate.vue";
import { formatDate } from "@/utils/format";
import { useSaveTopBoxHeight } from "@/hooks";

const { t } = useI18n();

const { height, contentHeight, contentWidth, statusBarHeight } = useSaveTopBoxHeight(60);
const props = defineProps({
    params: {
        type: Object,
        default: () => ({})
    },
    placeholder: {
        type: String,
        default: ""
    }
});

const displayPlaceholder = computed(() => props.placeholder || t("输入用户手机号、订单号或商品名"));
const emit = defineEmits(["update:params", "change"]);
const filterParams = computed({
    get() {
        return props.params;
    },
    set(val) {
        emit("update:params", val);
    }
});
const dateTabActive = ref(0);
const dateTab = (val: any) => {
    switch (val) {
        case 0:
            filterParams.value.orderTimeStart = "";
            filterParams.value.orderTimeEnd = "";
            break;
        case 1:
            filterParams.value.orderTimeStart = formatDate(new Date());
            filterParams.value.orderTimeEnd = formatDate(new Date());
            break;
        case 2:
            filterParams.value.orderTimeStart = formatDate(new Date(new Date().getTime() - 24 * 60 * 60 * 1000));
            filterParams.value.orderTimeEnd = formatDate(new Date(new Date().getTime() - 24 * 60 * 60 * 1000));
            break;
        case 3:
            filterParams.value.orderTimeStart = formatDate(new Date(new Date().getTime() - 24 * 60 * 60 * 1000 * 6));
            filterParams.value.orderTimeEnd = formatDate(new Date());
            break;
    }
    emit("change");
    dateTabActive.value = val;
    timeDates.value = [];
};
const showSelectDate = ref(false);
const timeDates = ref<any>([]);
const confirm = (e: any) => {
    timeDates.value = e;
    filterParams.value.orderTimeStart = e[0];
    filterParams.value.orderTimeEnd = e[1];
    dateTabActive.value = -1;
    emit("change");
};
</script>

<style lang="scss" scoped>
.time-filter {
    position: sticky;

    .search-box {
        background-color: #fff;
        height: v-bind("height + 'px'");
        .status-bar {
            height: v-bind("statusBarHeight + 'px'");
        }
        .search-input-box {
            height: v-bind("contentHeight + 'px'");
            width: v-bind("contentWidth");
            display: flex;
            padding: 0 25rpx;
            .input {
                display: flex;
                align-items: center;
                width: 100%;
            }
        }
    }
}
.time-filter__inner {
    padding: 20rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    font-size: 24rpx;
    z-index: 998;
    background-color: #fff;
    box-shadow: 0 6rpx 12rpx rgba(125, 126, 128, 0.16);
    .time-filter-tags {
        min-width: 360rpx;
        display: flex;
        gap: 20rpx;
        .time-filter-tags__item {
            border-radius: 24rpx;
            line-height: 44rpx;
            padding: 0 20rpx;
            display: inline-block;
            box-sizing: border-box;
            background-color: #f7f8fa;
            color: #999;
            &.time-filter-tags__item--active {
                background-color: #fff0e6;
                color: #ff720d;
            }
        }
    }
    .time-filter-custom {
        display: flex;
        align-items: center;
        text-align: right;
        color: #999;
        gap: 8rpx;
        .time-filter__txt {
            display: flex;
            flex-direction: column;
            font-size: 20rpx;
            line-height: 20rpx;
            color: #333;
        }
    }
}
</style>
