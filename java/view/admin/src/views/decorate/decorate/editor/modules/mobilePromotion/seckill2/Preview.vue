<template>
    <div
        :class="
            'module-ad-con module-goods_ad module-seckill-goods_ad ad-style__' +
            module?.style +
            ' ad-buyBtnStyle__' +
            module?.buyBtnStyle +
            ' ad-goods_style__' +
            module?.goodsStyle +
            ' ad-goods_radio_style__' +
            module?.goodsRadioStyle +
            ' ad-text_align__' +
            module?.textAlign +
            ' ad-text_weight__' +
            module?.textWeight +
            ' ad-goods_name_row__' +
            module?.goodsNameRow +
            ' ad-goods_name_padding__' +
            module?.goodsNamePadding
        "
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom + allFormat.backgroundColor"
    >
        <div
            class="module-ad-content"
            :style="
                frameFormat.backgroundColor +
                ' ' +
                frameFormat.boxRadius +
                allFormat.backgroundColor +
                allFormat.boxPadding +
                allFormat.boxPaddingBottom +
                allFormat.boxPaddingTop +
                allFormat.boxRadius +
                allFormat.innerPadding
            "
        >
            <CommonTitle v-if="title?.showTitle" v-model="title">
                <template #more>
                    <CommonCountdown countdownType="specifyTime" :specifyTime="getTime">
                        <template #default="{ days, hours, minutes, seconds }">
                            <div class="countdown-box">
                                <span class="countdown-text">距结束</span>
                                <template v-if="days && Number(days) > 0">
                                    <span class="countdown-item">{{ days }}</span>
                                    <span class="countdown-separator"> 天 </span>
                                </template>
                                <span class="countdown-item">{{ hours }}</span>
                                <span class="countdown-separator">:</span>
                                <span class="countdown-item">{{ minutes }}</span>
                                <span class="countdown-separator">:</span>
                                <span class="countdown-item">{{ seconds }}</span>
                                <i class="iconfont-h5 icon-youjiantou"></i>
                            </div>
                        </template>
                    </CommonCountdown>
                </template>
            </CommonTitle>
            <div class="goods-ad-warp">
                <div class="goods-ad-con" :style="goodsConStyle">
                    <template v-for="item in seckillList" :key="item.id">
                        <div class="goods-ad-item" :style="adItemStyle">
                            <div class="item-content" :style="itemContentStyle">
                                <div class="item-con">
                                    <div class="item-photo">
                                        <div class="item-photo-bg" :style="{ backgroundColor: module.imageBackgroundColor }"></div>
                                        <img @error="imageError" :src="productImage(item)" alt="" />
                                    </div>
                                    <div class="item-info">
                                        <div class="item-name">
                                            <a v-if="module?.showName" href="" title="" class="item-name-a">{{ item.productName }}</a>
                                        </div>
                                        <div class="tips">{{ `优惠${getDiscount(item.productPrice, item.orgProductPrice)}%` }}</div>
                                        <div class="item-action" v-if="module?.showPrice">
                                            <div class="item-price" :style="itemPrice">
                                                <span class="price_format" :style="priceStyle">
                                                    {{ priceFormat(item.productPrice) }}
                                                </span>
                                                <template v-if="module.style == 10">
                                                    <span class="price-through">{{ priceFormat(item.orgProductPrice) }}</span>
                                                </template>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed } from "vue";
import { priceFormat, productImage } from "@/utils/format";
import { mergeDefaultModule, formatFrame, CommonTitle, CommonCountdown } from "@/views/decorate/decorate/src/modules/index";
import { getMobileSeckillList } from "@/api/decorate/mobileSeckill";
import emptyImg from "@/views/decorate/decorate/src/img/empty-img-bg3.png";
import { seckill2Defaults } from "./index";
import type { Seckill2Module } from "./types";

const props = defineProps<{ module: Seckill2Module }>();
const module = props.module;

// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, seckill2Defaults);

const { frame, title } = module;

const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});

const allFormat = computed(() => {
    return {
        backgroundColor: `background-color: ${module?.backgroundColor};`,
        boxPadding: `margin-left: ${module?.boxPadding}px;margin-right: ${module?.boxPadding}px;`,
        boxPaddingBottom: `margin-bottom: ${module?.boxPaddingBottom}px;`,
        boxPaddingTop: `margin-top: ${module?.boxPaddingTop}px;`,
        boxRadius: `border-radius: ${module?.boxRadius}px;`,
        innerPadding: `padding: ${module?.innerPadding}px;`
    };
});

const itemContentStyle = computed(() => {
    return `${module.style == 10 ? "padding-left" : "padding"}: ${module?.goodsPadding}px;`;
});

const goodsConStyle = computed(() => {
    let str = "";

    if (module.style == 10) {
        str += `display: flex; overflow: hidden;overflow-x: auto;flex-wrap: nowrap;`;
    }

    return str;
});

const adItemStyle = computed(() => {
    return module.style == 10 ? `width: ${100 / 3}%;flex-shrink: 0;flex-grow: 0;` : 0;
});

const priceStyle = computed(() => {
    return module.style == 10 ? `font-size: 16px;font-weight: 400;line-height: 24px;color: #1a1a1a;display: block;height: 24px;margin-right: 8px;` : "";
});

const itemPrice = computed(() => {
    return module.style == 10 ? `display: flex;align-items: baseline;` : "";
});

const getDiscount = (currentPrice: number, originalPrice: number) => {
    if (originalPrice === 0 || currentPrice >= originalPrice) {
        return 0;
    }

    const discountPercentage = ((originalPrice - currentPrice) / originalPrice) * 100;

    return Math.floor(discountPercentage * 10) / 10;
};

const getTime = computed(() => {
    return seckillList.value[0] ? new Date(seckillList.value[0].seckkillData.seckillEndTime).getTime() : 0;
});

const seckillList = ref<any>([]);

const getData = async () => {
    try {
        const result = await getMobileSeckillList();
        seckillList.value = result;
    } catch (error) {
        console.error(error);
    }
};
getData();

const imageError = (e: any) => {
    e.target.src = emptyImg;
};
</script>

<style lang="less" scoped>
.countdown-box {
    .countdown-text {
        margin-right: 4px;
        font-size: 14px;
        font-weight: 400;
        line-height: 18px;
        color: #1a1a1a;
    }
    .countdown-item {
        display: inline-block;
        font-size: 14px;
        font-weight: 400;
        min-width: 20px;
        min-height: 20px;
        text-align: center;
        background-color: #1a1a1a;
        color: #fff;
        border-radius: 4px;
        line-height: 20px;
    }

    .icon-youjiantou {
        color: #1a1a1a;
        font-size: 14px;
        padding-left: 4px;
    }

    span {
        margin: 0 1px;
    }
}

.goods-ad-con {
    scrollbar-width: none;
    &::-webkit-scrollbar {
        display: none;
    }
}

.tips {
    color: #fff;
    background-color: #cc0c1c;
    border-radius: 2px;
    text-align: center;
    padding: 1px 2px;
    height: 16px;
    font-size: 10px;
    display: inline-flex;
    align-items: center;
    margin-top: 6px;
    margin-left: 8px;
}

.goods-ad-warp .goods-ad-item .item-info {
    background: #ffffff;
    position: relative;
    padding-top: 0px;
}

.item-con {
    overflow: hidden;
}

.item-photo {
    overflow: hidden;
    position: relative;
    border-radius: 4px 4px 0 0;

    .item-photo-bg {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        display: block;
    }
}

.price-through {
    height: 12px;
    line-height: 12px;
    font-size: 12px;
    color: #888b94;
    text-decoration: line-through;
}

.cap-seckill-goods__tag {
    display: flex;
    align-items: center;
    justify-content: space-between;

    &.flex-end {
        justify-content: flex-end;
    }
}

.iconh5-style {
    color: #f23030;
    font-size: 18px;
    margin-right: 5px;
}
</style>
