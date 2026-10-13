<template>
    <view v-if="hasContent" class="dec-fixed-area" :style="cssVars">
        <view class="dec-fixed-area__container">
            <termPayCard />
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useFrameVars, type ModuleDataWithFrame } from "@/utils/decorateFrame";
import { useUserStore } from "@/store/user";
import termPayCard from "../../termPay/termPayCard.vue";
import { useCreditInfo } from "@/hooks/useCreditInfo";

const props = defineProps<{
    data: ModuleDataWithFrame;
}>();

const userStore = useUserStore();

// 账期卡显示条件与 termPayCard 同源，两处共用 useCreditInfo 的判断（含授信信息补偿请求），
// 否则外壳判不出来时整块不渲染，卡片挂不上、补偿请求也就永远发不出去。
const { showTermPay: showTermPayCard } = useCreditInfo();


// 所有子卡片都不显示时整块不渲染，避免留下带内边距的空节点
const hasContent = computed(() => {
    if (showTermPayCard.value) return true;
    return false;
});

const cssVars = computed(() =>
    useFrameVars(props.data, {
        backgroundColor: "transparent",
        boxRadius: 0,
        innerPadding: 0,
        boxPadding: 5,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    })
);
</script>

<style lang="scss" scoped>
.dec-fixed-area {
    box-sizing: border-box;
    padding-left: var(--dec-box-padding, 10rpx);
    padding-right: var(--dec-box-padding, 10rpx);
    padding-top: var(--dec-box-padding-top, 20rpx);
    padding-bottom: var(--dec-box-padding-bottom, 0);

    &__container {
        background-color: var(--dec-bg-color, transparent);
        border-radius: var(--dec-border-radius, 0);
        padding: var(--dec-inner-padding, 0);
    }
}
</style>
