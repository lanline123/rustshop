<template>
    <!-- 海外版排序下拉面板（layoutStyle=2） -->
    <view class="sort-filter-box" @click.stop>
        <view class="sort-filter-title">{{ $t("排序") }}</view>
        <up-radio-group v-model="radioValue" placement="column">
            <template v-for="(item, index) in options" :key="index">
                <view class="sort-item">
                    <up-radio
                        active-color="#1a1a1a"
                        :class="{ active: item.value === radioValue }"
                        :label="$t(item.label)"
                        :name="item.value"
                        @change="radioChange(item)"
                    />
                </view>
            </template>
        </up-radio-group>
    </view>
</template>

<script setup lang="ts">
/** 搜索结果页 - 海外版排序面板 */
import { ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

export interface SortRadioOption {
    label: string;
    value: string;
    sort: string;
}

const emit = defineEmits<{
    (e: "sortChange", val: SortRadioOption): void;
}>();

const options: SortRadioOption[] = [
    { label: t("推荐"), value: "", sort: "default" },
    { label: t("价格从高到低"), value: "desc", sort: "price" },
    { label: t("价格从低到高"), value: "asc", sort: "price" }
];

const radioValue = ref("");

const radioChange = (val: SortRadioOption) => {
    emit("sortChange", val);
};
</script>

<style lang="scss" scoped>
.sort-filter-box {
    position: relative;
    top: calc(var(--safe-top) * -1);
    background: #fff;
    border-bottom-left-radius: 21rpx;
    border-bottom-right-radius: 21rpx;
    width: 100%;
    padding: 21rpx 32rpx;
    // #ifdef MP-WEIXIN
    padding-top: calc(var(--safe-top, 21rpx));
    // #endif
    max-height: calc(100vh - 398rpx);
    overflow-y: scroll;

    .sort-filter-title {
        width: 100%;
        font-weight: 700;
        margin-bottom: 20rpx;
    }

    .sort-item {
        padding-bottom: 20rpx;
        :deep(.u-radio) {
            width: 100%;
            .u-radio__icon-wrap {
                width: 34rpx !important;
                height: 34rpx !important;
                margin-right: 28rpx;
                background: #fff !important;
                border-width: 1px;
            }
            .u-radio__text {
                font-size: 30rpx !important;
                color: #1a1a1a !important;
            }
            .u-radio__label-wrap {
                flex: 1;
            }
            &.active {
                .u-icon__icon {
                    width: 20rpx;
                    height: 20rpx;
                    border-radius: 50%;
                    background: #1a1a1a !important;
                    span {
                        display: none;
                    }
                }

                .u-radio__icon-wrap {
                    border-width: 2px;
                }
            }
        }
    }
}
</style>
