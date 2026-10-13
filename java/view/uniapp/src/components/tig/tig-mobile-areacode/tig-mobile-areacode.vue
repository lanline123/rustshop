<template>
    <view class="areacode-box" :style="boxStyle" @click="handleClick">
        <slot :data="currentData">
            <view class="areacode-box-text">
                <view class="text-label" :style="labelStyle"> {{ currentData?.label }} </view>
                <!-- <view class="text-name" :style="nameStyle"> {{ currentData.name ? `(${$t(currentData.name)})` : "" }}</view> -->
            </view>
            <text class="iconfont-h5 icon-arrowdown" :style="iconStyle" />
        </slot>

        <up-picker
            :show="show"
            :cancel-text="$t('取消')"
            :confirm-text="$t('确认')"
            :columns="[list]"
            key-name="labelName"
            @cancel="show = false"
            @confirm="handleConfirm"
        />
    </view>
</template>

<script setup lang="ts">
import { getmobileAreaCode } from "@/api/common";
import type { MobileAreaCodeList } from "@/types/common";
import { ref, watch } from "vue";

const props = defineProps({
    labelStyle: {
        type: Object,
        default: () => ({})
    },
    nameStyle: {
        type: Object,
        default: () => ({})
    },
    boxStyle: {
        type: Object,
        default: () => ({})
    },
    iconStyle: {
        type: Object,
        default: () => ({})
    },
    placeholder: {
        type: String,
        default: ""
    },
    modelValue: {
        type: String,
        default: ""
    }
});
const emit = defineEmits(["update:modelValue"]);
const DEFAULT_MOBILE_AREA_CODE = "86";

const show = ref(false);
const list = ref<MobileAreaCodeList[]>([]);
const loading = ref<boolean>(true);
const currentData = ref<MobileAreaCodeList>({} as MobileAreaCodeList);
const syncCurrentData = (value?: string) => {
    if (list.value.length === 0) return;
    const targetCode = value ?? props.modelValue;
    const matchedItem = list.value.find((item) => String(item.code) === String(targetCode));
    const defaultItem =
        list.value.find((item) => String(item.code) === DEFAULT_MOBILE_AREA_CODE) || list.value.find((item) => item.isDefault === 1) || list.value[0];
    const selectedItem = matchedItem || defaultItem;
    currentData.value = selectedItem || ({} as MobileAreaCodeList);
    if (!matchedItem && selectedItem && String(props.modelValue) !== String(selectedItem.code)) {
        emit("update:modelValue", selectedItem.code);
    }
};
const getmobileAreaCodeList = async () => {
    loading.value = true;
    try {
        const result = await getmobileAreaCode();
        result.map((item) => {
            item.labelName = `${item.label} (${item.name})`;
        });
        list.value = result;
        syncCurrentData();
    } catch (error) {
        console.log(error);
    } finally {
        loading.value = false;
    }
};
getmobileAreaCodeList();
watch(
    () => props.modelValue,
    (value) => {
        syncCurrentData(value);
    }
);
const handleClick = () => {
    show.value = true;
};
const handleConfirm = (e: any) => {
    currentData.value = e.value[0];
    emit("update:modelValue", e.value[0].code);
    show.value = false;
};
</script>

<style lang="scss" scoped>
.areacode-box {
    display: flex;
    align-items: center;
    border-right: 1rpx solid #dfdfdf;
    margin-right: 25rpx;
    padding-right: 10rpx;

    .areacode-box-text {
        font-size: 28rpx;
        color: #333;
        display: flex;
        align-items: center;
        .text-name {
            padding: 0 2rpx 0 4rpx;
            font-size: 26rpx;
        }
    }
    .icon-arrowdown {
        display: inline-flex;
        align-items: center;
        color: #333;
        font-size: 22rpx;
        line-height: 1;
        margin-left: 6rpx;
        transform: translateY(3rpx);
    }
}
</style>
