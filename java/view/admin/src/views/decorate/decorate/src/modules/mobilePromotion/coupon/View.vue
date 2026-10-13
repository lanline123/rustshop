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
                <div class="desc">{{ $t('优惠券') }}</div>
            </div>
            <div v-else class="coupon-ad-warp" :style="couponFormat.boxPadding + couponFormat.boxPaddingBottom + couponFormat.boxPaddingTop">
                <div class="coupon-title" v-if="module?.showTitle === 1">
                    <div class="coupon-title-text">
                        <h4 class="coupon-maintitle">{{ module.title }}</h4>
                        <span class="coupon-title-con">{{ module.desc }}</span>
                    </div>
                    <div>
                        <a class="oupon-title-more"
                            >{{ $t('更多') }} <el-icon><ArrowRight /></el-icon
                        ></a>
                    </div>
                </div>
                <div class="coupon-con">
                    <div class="coupon-item" :style="couponFormat.itemPadding">
                        <div class="item-coupon-con">
                            <div class="coupon-money">
                                <div class="coupon-money-c">5.00</div>
                                <div class="coupon-money-d">{{ config.config.amountUnit }}</div>
                            </div>
                            <div class="coupon-name">
                                <div class="coupon-name-text">{{ $t('满60减5') }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted, Ref } from "vue";
import { ModuleCouponType, ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";
import { ArrowRight } from "@element-plus/icons-vue";
import { useConfigStore } from "@/store/config";
const config: any = useConfigStore();
// 在modules加入要使用的模块
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & ModuleCouponType>("module") as Ref<ModuleType & ModuleCouponType>;
const defaultModule = ref({
    showTitle: 0,
    title: t('每日领券'),
    desc: t('天天优惠领不停'),
    colorStyle: 0,
    itemBackgroundColor: "#ffffff",
    backgroundColor: "#ffffff",
    itemPadding: 10,
    boxRadius: 8,
    boxPadding: 10,
    boxMargin: 0,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    frame: defaultFrame
});
mergeDefaultModule(module.value, defaultModule.value);
const { frame } = module.value;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});

const couponFormat = computed(() => {
    return {
        itemBackgroundColor: `background-color:${module.value?.itemBackgroundColor}`,
        boxPadding: `padding-left:${module.value?.boxPadding}px;padding-right:${module.value?.boxPadding}px;`,
        boxMargin: `margin-left:${module.value?.boxMargin}px;margin-right:${module.value?.boxMargin}px;`,
        boxPaddingBottom: `padding-bottom:${module.value?.boxPaddingBottom}px;`,
        boxPaddingTop: `padding-top:${module.value?.boxPaddingTop}px;`,
        boxRadius: `border-radius:${module.value?.boxRadius}px;`,
        itemPadding: `padding:${module.value?.itemPadding}px;`,
        backgroundColor: `background-color:${module.value?.backgroundColor}`
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
