<template>
    <div v-if="hasContent" class="module-ad-con module-fixed-functional-area" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content fixed-area-wrap" :style="wrapStyle">
            <div class="fixed-area-container">
                <!-- 账期支付卡 -->
                <div v-if="isB2b()" class="ffa-card ffa-card--term" :style="termCardStyle">
                    <div class="ffa-card__title">
                        <div class="ffa-card__title-left">
                            <span class="ffa-card__title-text ffa-card__title-text--term">账期支付</span>
                            <i class="iconfont-h5 icon-buxianshi ffa-card__toggle-eye ffa-card__toggle-eye--term"></i>
                        </div>
                        <div class="ffa-card__title-more ffa-card__title-more--term">
                            <span class="ffa-card__title-more-text">查看详情</span>
                            <i class="iconfont-h5 icon-a-gengduo20"></i>
                        </div>
                    </div>
                    <div class="ffa-card__content">
                        <div class="ffa-card__wrap">
                            <div class="ffa-card__item">
                                <div class="ffa-card__item-number ffa-card__item-number--term">******</div>
                                <div class="ffa-card__item-text ffa-card__item-text--term">信用额度</div>
                            </div>
                            <div class="ffa-card__item">
                                <div class="ffa-card__item-number ffa-card__item-number--term">******</div>
                                <div class="ffa-card__item-text ffa-card__item-text--term">已用额度</div>
                            </div>
                            <div class="ffa-card__item">
                                <div class="ffa-card__item-number ffa-card__item-number--term">******</div>
                                <div class="ffa-card__item-text ffa-card__item-text--term">剩余额度</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 配送订单卡 -->
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import termPayBg from "@/assets/decorate/mobileMember/fixedFunctionalArea/termPay_card.png";
import statisticsBg from "@/assets/decorate/mobileMember/fixedFunctionalArea/statistics_bg.png";
import { isB2b, isStore } from "@/utils/version";
import { fixedFunctionalAreaDefaults } from "./index";
import type { FixedFunctionalAreaModule } from "./types";

const props = defineProps<{ module: FixedFunctionalAreaModule }>();
const module = props.module;

mergeDefaultModule(module, fixedFunctionalAreaDefaults);

const frame = computed(() => module.frame ?? {});
const frameFormat = computed(() => formatFrame(frame.value));

const hasContent = computed(() => isB2b() || isStore());

const wrapStyle = computed(() => ({
    backgroundColor: frame.value.backgroundColor || "transparent",
    borderRadius: `${frame.value.boxRadius ?? 0}px`,
    padding: `${frame.value.innerPadding ?? 0}px`,
    boxSizing: "border-box" as const
}));

const termCardStyle = computed(() => ({
    backgroundImage: `url(${termPayBg})`,
    backgroundSize: "cover",
    backgroundPosition: "center",
    backgroundRepeat: "no-repeat"
}));

const deliveryCardStyle = computed(() => ({
    backgroundImage: `url(${statisticsBg})`,
    backgroundSize: "cover",
    backgroundPosition: "center",
    backgroundRepeat: "no-repeat"
}));
</script>
<style lang="less" scoped>
.fixed-area-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.fixed-area-container {
    box-sizing: border-box;
}

.ffa-card {
    margin: 10px 0;
    border-radius: 9px;
    overflow: hidden;
    box-sizing: border-box;
    .icon-a-gengduo20 {
        font-size: 8px;
    }

    &__title {
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 44px;
        padding: 0 15px;
        font-size: 13px;
        font-weight: bold;
        border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    }

    &__title-left {
        display: flex;
        align-items: center;
        column-gap: 6px;
    }

    &__title-text {
        font-size: 13px;
        font-weight: 600;

        &--term {
            color: #ae5d34;
        }

        &--delivery {
            color: #2a2a2a;
        }
    }

    &__toggle-eye {
        font-size: 15px;

        &--term {
            color: #ae5d34;
        }
    }

    &__title-more {
        display: flex;
        align-items: center;
        padding: 4px 10px;
        border-radius: 20px;
        font-size: 11px;
        font-weight: normal;

        &--term {
            background-color: #fefdf4;
            color: #ae5d34;
        }

        &--delivery {
            background-color: #fff9cc;
            color: #333;
        }
    }

    &__title-more-text {
        margin-right: 4px;
    }

    &__title-more .iconfont {
        font-size: 10px;
    }

    &__content {
        position: relative;
        min-height: 60px;
        padding: 15px 0;
    }

    &__wrap {
        display: flex;
        flex-direction: row;
        flex-wrap: nowrap;
        justify-content: space-around;
        align-items: center;
        padding: 0 10px;
    }

    &__item {
        position: relative;
        flex: 1;
        text-align: center;
    }

    &__item-number {
        font-weight: 600;
        margin-bottom: 4px;
        line-height: 1.2;
        min-height: 22px;
        display: flex;
        align-items: center;
        justify-content: center;

        &--term {
            font-size: 14px;
            color: #ae5d34;
        }

        &--delivery {
            font-size: 18px;
            color: #333;
        }
    }

    &__item-text {
        font-size: 12px;
        line-height: 1.5;

        &--term {
            color: #ae5d34;
        }

        &--delivery {
            color: #333;
        }
    }
}
</style>
