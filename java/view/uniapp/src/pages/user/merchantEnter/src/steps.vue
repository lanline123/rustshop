<template>
    <view class="steps">
        <up-steps active-color="var(--general)" :current="currentStep">
            <up-steps-item v-for="(item, index) in stepList" :key="index" :item-style="{ fontSize: '20rpx' }" :title="$t(item)" />
        </up-steps>
    </view>
</template>

<script setup lang="ts">
const props = defineProps({
    currentStep: {
        type: Number,
        default: 1
    },
    stepList: {
        type: Array,
        default: () => []
    }
});
</script>

<style lang="scss" scoped>
.steps {
    background-color: #fff;
    height: 140rpx;
    padding: 20rpx 0;
    overflow: hidden;
}
</style>
