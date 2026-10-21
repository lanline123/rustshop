<template>
    <tig-popup v-model:show="show" :z-index="999" :show-close="false" background-color="#fff">
        <view class="groupon-container">
            <view class="groupon-title">{{ $t("可参与的拼团") }}</view>
            <scroll-view scroll-y="true" class="groupon-box" @scrolltolower="reachBottom">
                <view class="groupon-content">
                    <productGroupon :groups="recordList" bottom-border @join-click="handleJoinClick" />
                </view>
            </scroll-view>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * 更多拼团列表组件
 * - 弹窗展示更多正在进行中的拼团
 * - 支持分页加载
 * - 每项显示拼团人员和剩余时间
 */
import type { GrouponRecordItem, GrouponRecordListParams } from "@/types/groupon/groupon";
import productGroupon from "./productGroupon.vue";
import { getGrouponRecordList } from "@/api/groupon/groupon";
import { useList } from "@/hooks";
import { watch, watchEffect } from "vue";

interface IProps {
    productId?: number;
    groupList: GrouponRecordItem[];
    total: number;
}

interface IEmits {
    (e: "joinClick", item: GrouponRecordItem): void;
    (e: "loadEnd", groupRecords: GrouponRecordItem[]): void;
}

const props = withDefaults(defineProps<IProps>(), {
    groupList: () => [],
    total: 0
});

const show = defineModel("modelValue");

const emit = defineEmits<IEmits>();

const getGrouponRecordsParams: GrouponRecordListParams = {
    page: 1,
    size: 1,
    // isMine: false,
    joinStatus: 1,
    productId: 0
};

const {
    data: recordList,
    getList,
    isLoading,
    total,
    reachBottom
} = useList<GrouponRecordItem>(getGrouponRecordList, {
    params: getGrouponRecordsParams,
    path: {
        dataKey: "records"
    },
    needReachBottom: false,
    immediate: false
});

isLoading.value = false;

watch(
    () => props.groupList,
    (val) => {
        if (val.length) {
            recordList.value = [...val];
        }
    }
);
watchEffect(() => {
    getGrouponRecordsParams.productId = props.productId;
    total.value = props.total;
});
/*
 * 点击去参团
 */
const handleJoinClick = (item: GrouponRecordItem) => {
    emit("joinClick", item);
};
</script>

<style lang="scss" scoped>
.btn-box {
    padding: 25rpx;
}

.groupon-container {
    height: 60vh;
    overflow: hidden;
    overflow-y: auto;

    .groupon-title {
        height: 84rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28rpx;
    }

    .groupon-box {
        height: calc(100% - 84rpx);
    }
}
</style>
