<template>
    <view class="remark">
        <view class="remark-title">{{ $t("备注") }}</view>
        <view class="remark-content" @click="showPopup = true">
            <text v-if="remark" class="remark-text">{{ remark }}</text>
            <text v-else class="remark-placeholder">{{ $t("备注建议提前协商（250字以内）") }}</text>
            <view class="iconfont-h5 icon-fanhui" />
        </view>
    </view>
    <tig-popup :show="showPopup" @close="showPopup = false">
        <view class="popup-content">
            <view class="popup-content-title">
                {{ $t("备注") }}
            </view>
            <view>
                <up-textarea
                    v-model="remark"
                    :placeholder="$t('备注建议提前协商（250字以内）')"
                    maxlength="250"
                    count
                    :placeholder-style="{ backgroundColor: '#FAFAFA', padding: '0', color: '#9A9A9A', fontSize: '24rpx' }"
                />
            </view>
            <tig-fixed-placeholder background-color="#fff" height="100rpx">
                <view class="footer" style="padding: 15rpx 30rpx">
                    <tig-button style="height: 70rpx" theme @click="showPopup = false">
                        <text class="submit-btn-text">
                            {{ $t("确定") }}
                        </text>
                    </tig-button>
                </view>
            </tig-fixed-placeholder>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";

const props = defineProps({
    modelValue: {
        type: String,
        default: ""
    }
});
const showPopup = ref(false);
const emit = defineEmits(["update:modelValue"]);
const remark = computed({
    get() {
        return props.modelValue;
    },
    set(val) {
        emit("update:modelValue", val);
    }
});
</script>

<style lang="scss" scoped>
.remark {
    border-radius: 0;
    background: #fff;
    padding: 21rpx 28rpx;
    margin-bottom: 14rpx;
    font-size: 24.5rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;

    .remark-content {
        width: 550rpx;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .remark-text {
            flex: 1;
            text-align: right;
            font-size: 24.5rpx;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .remark-placeholder {
            flex: 1;
            text-align: right;
            font-size: 24.5rpx;
            color: #9a9a9a;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .icon-fanhui {
            font-size: 17rpx;
            color: #888b94;
            transform: rotate(180deg);
            padding: 10rpx 10rpx 10rpx 0;
            flex-shrink: 0;
        }
    }
}

.popup-content {
    height: 80vh;
    padding: 30rpx;
    .popup-content-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        text-align: center;
        margin: 10rpx 0 20rpx 0;
    }
}

:deep(.u-textarea) {
    padding: 10rpx;
    height: 300rpx;
    background-color: #fafafa !important;
    background: #fafafa !important;
}
</style>
