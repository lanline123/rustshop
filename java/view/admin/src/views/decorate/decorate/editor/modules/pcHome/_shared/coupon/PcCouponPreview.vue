<template>
    <div class="dec-pc-coupon-outer" :style="outerStyle">
        <div :class="['dec-pc-coupon', `dec-pc-coupon--style-${normalizedColorStyle}`]" :style="cssVars">
            <div v-if="isEmpty" class="dec-pc-coupon__empty">
                <div class="dec-pc-coupon__empty-bg"></div>
                <div class="dec-pc-coupon__empty-text">优惠券</div>
            </div>

            <!-- 圆角作用在最外层内容容器（含标题+领券中心条） -->
            <div v-else class="dec-pc-coupon__content">
                <!-- 标题栏：参考"PC 文本标题"模块样式（不是左侧领券中心） -->
                <div v-if="moduleConfig.showTitle === 1" class="dec-pc-coupon__header">
                    <div class="dec-pc-coupon__header-title">{{ moduleConfig.title }}</div>
                    <div v-if="moduleConfig.desc" class="dec-pc-coupon__header-desc">{{ moduleConfig.desc }}</div>
                </div>

                <div class="dec-pc-coupon__main">
                    <div class="dec-pc-coupon__tit">
                        <a class="dec-pc-coupon__tit-link" href="javascript:;">
                            <h4 class="dec-pc-coupon__tit-zh">领券中心</h4>
                            <p class="dec-pc-coupon__tit-en">COUPON</p>
                            <p class="dec-pc-coupon__tit-go">GO <em>&gt;&gt;</em></p>
                        </a>
                        <div class="dec-pc-coupon__edge dec-pc-coupon__edge--left"></div>
                    </div>

                    <!-- 右侧：券列表 -->
                    <div class="dec-pc-coupon__list">
                        <div v-for="(item, index) in mockCoupons" :key="index" class="dec-pc-coupon__item">
                            <a class="dec-pc-coupon__item-link" href="javascript:;">
                                <div class="dec-pc-coupon__detail">
                                    <div class="dec-pc-coupon__price">
                                        <template v-if="item.couponType === 1">
                                            <div class="dec-pc-coupon__price-row">
                                                <span class="dec-pc-coupon__currency">¥</span>
                                                <span class="dec-pc-coupon__integer">{{ item.priceInteger }}</span>
                                                <span class="dec-pc-coupon__decimals">.{{ item.priceDecimals }}</span>
                                            </div>
                                        </template>
                                        <template v-else>
                                            <div class="dec-pc-coupon__discount">{{ item.couponDiscount }}<span>折</span></div>
                                        </template>
                                    </div>
                                    <div class="dec-pc-coupon__direction">
                                        <p class="dec-pc-coupon__sort">{{ item.couponName }}</p>
                                        <p class="dec-pc-coupon__desc">{{ item.couponDesc }}</p>
                                    </div>
                                </div>
                                <p class="dec-pc-coupon__use-btn">马上领<em>&nbsp;&gt;</em></p>
                            </a>
                            <div class="dec-pc-coupon__edge dec-pc-coupon__edge--item"></div>
                        </div>

                        <div class="dec-pc-coupon__more">
                            <a class="dec-pc-coupon__more-link" href="javascript:;">查看更多</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 优惠券模块 - 共享预览组件
 * 纯 mock 渲染不调 API（4 张演示券：满减×2 + 折扣×2）
 */
import { computed } from "vue";
import type { PcCouponModule } from "./types";
import { pcCouponDefaults } from "./defaults";
import { deepMerge } from "../../../../utils/object";

defineOptions({ name: "PcCouponPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcCouponModule>(() => {
    return deepMerge(pcCouponDefaults, props.module as Partial<PcCouponModule>);
});

const isEmpty = computed(() => {
    return Object.keys(props.module || {}).length === 0;
});

const normalizedColorStyle = computed(() => {
    const raw = Number(moduleConfig.value.colorStyle ?? 0);
    const safe = [0, 1, 2, 3, 4].includes(raw) ? raw : 0;
    return safe === 0 ? 1 : safe;
});

/**
 * CSS 变量（传递动态值到样式）
 */
const cssVars = computed(() => {
    const data = moduleConfig.value;
    return {
        "--dec-box-padding": `${data.boxPadding ?? 10}px`,
        "--dec-box-padding-top": `${data.boxPaddingTop ?? 5}px`,
        "--dec-box-padding-bottom": `${data.boxPaddingBottom ?? 5}px`,
        "--dec-box-radius": `${data.boxRadius ?? 8}px`,
        "--dec-bg-color": data.backgroundColor ?? data.itemBackgroundColor ?? "#ffffff"
    };
});

const outerStyle = computed(() => {
    const padding = moduleConfig.value.itemPadding ?? 0;
    return {
        padding: `${padding}px`
    };
});

interface MockCoupon {
    couponType: number;
    priceInteger?: string;
    priceDecimals?: string;
    couponDiscount?: string;
    couponName: string;
    couponDesc: string;
}

const mockCoupons: MockCoupon[] = [
    {
        couponType: 1,
        priceInteger: "5",
        priceDecimals: "00",
        couponName: "满60减5",
        couponDesc: "可领取"
    },
    {
        couponType: 2,
        couponDiscount: "8",
        couponName: "折扣券",
        couponDesc: "可领取"
    },
    {
        couponType: 1,
        priceInteger: "5",
        priceDecimals: "00",
        couponName: "满60减5",
        couponDesc: "可领取"
    },
    {
        couponType: 2,
        couponDiscount: "8",
        couponName: "折扣券",
        couponDesc: "可领取"
    }
];
</script>

<style lang="less" src="./preview.less" />
