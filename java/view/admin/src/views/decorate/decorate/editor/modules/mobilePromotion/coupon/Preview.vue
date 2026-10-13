<template>
    <div
        :class="`module-ad-con module-image_hotarea ad-color_style__${module?.colorStyle}`"
        :style="frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom + couponFormat.boxMargin"
    >
        <div
            class="module-ad-content"
            :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius + couponFormat.boxRadius + couponFormat.backgroundColor"
        >
            <div class="module-ad-empty empty-image_ad" v-if="Object.values(module).length === 0">
                <div class="image-empty-bg"></div>
                <div class="desc">优惠券</div>
            </div>
            <div v-else class="coupon-ad-warp" :style="couponFormat.boxPadding + couponFormat.boxPaddingBottom + couponFormat.boxPaddingTop">
                <div class="coupon-title" v-if="module?.showTitle === 1">
                    <div class="coupon-title-text">
                        <h4 class="coupon-maintitle">{{ module.title }}</h4>
                        <span class="coupon-title-con">{{ module.desc }}</span>
                    </div>
                    <div>
                        <a class="oupon-title-more"
                            >更多 <el-icon><ArrowRight /></el-icon
                        ></a>
                    </div>
                </div>
                <div class="coupon-con">
                    <div class="coupon-item" :style="couponFormat.itemPadding">
                        <div class="item-coupon-con">
                            <div class="coupon-money">
                                <div class="coupon-money-c">5.00</div>
                                <div class="coupon-money-d">元</div>
                            </div>
                            <div class="coupon-name">
                                <div class="coupon-name-text">满60减5</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, onUnmounted } from "vue";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { ArrowRight } from "@element-plus/icons-vue";
import { couponDefaults } from "./index";
import type { CouponModule } from "./types";

const props = defineProps<{ module: CouponModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, couponDefaults);
const { frame } = module;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});

const couponFormat = computed(() => {
    return {
        itemBackgroundColor: `background-color:${module?.itemBackgroundColor}`,
        boxPadding: `padding-left:${module?.boxPadding}px;padding-right:${module?.boxPadding}px;`,
        boxMargin: `margin-left:${module?.boxMargin}px;margin-right:${module?.boxMargin}px;`,
        boxPaddingBottom: `padding-bottom:${module?.boxPaddingBottom}px;`,
        boxPaddingTop: `padding-top:${module?.boxPaddingTop}px;`,
        boxRadius: `border-radius:${module?.boxRadius}px;`,
        itemPadding: `padding:${module?.itemPadding}px;`,
        backgroundColor: `background-color:${module?.backgroundColor}`
    };
});

onMounted(async () => {});
onUnmounted(() => {});
</script>
<style lang="less" scoped>
.coupon-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;

    .coupon-title-text {
        display: flex;
        align-items: end;

        .coupon-maintitle {
            color: #2a3145;
            font-size: 16px;
            margin-right: 5px;
        }

        .coupon-title-con {
            color: #aaa;
            font-size: 12px;
        }
    }

    .oupon-title-more {
        display: flex;
        color: #aaa;
        font-size: 12px;
        align-items: center;
    }
}
</style>
