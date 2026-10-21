<template>
    <div
        class="module-ad-con"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage
        "
    >
        <div
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <div class="comp">
                <div class="cont">
                    <div>
                        <div class="mk-title" :style="`color:${module.moduleColor.titleColor}`">
                            {{ module.productGroups[0].groupTitle }}
                            <div
                                v-if="module.showCountdown"
                                class="time"
                                :style="`background:${module.moduleColor.countdownBackgroundColor}; color:${module.moduleColor.countdownColor}`"
                            >
                                <template v-if="new Date().getTime() > Number(module.dailyTime[0])">
                                    <CommonCountdown
                                        :countdownType="module.countdownType"
                                        :value-style="{
                                            color: module.moduleColor.countdownColor,
                                            'font-size': '12px',
                                            display: 'block',
                                            height: '20px',
                                            'line-height': '20px'
                                        }"
                                        :format="formtText"
                                        :dailyTime="module.dailyTime"
                                        :specifyTime="module.specifyTime"
                                    ></CommonCountdown>
                                </template>
                                <template v-else>活动未开始</template>
                            </div>
                        </div>
                        <div class="mk-stitle" :style="`color:${module.moduleColor.sellingPointColor}`">{{ module.productGroups[0].groupSubTitle }}</div>
                    </div>
                    <div class="item-box">
                        <Swiper :swiperOption="{ ...swiperOption, slidesPerView: 2, spaceBetween: 6 }" v-model="productGroupsList[0]" @activeIndexChange="">
                            <template v-slot:default="{ item }">
                                <div class="item">
                                    <div class="goods-img" :style="`padding-top: ${picScaleMap[module.picScale!]}; border-radius: ${module.productRadius}px`">
                                        <div
                                            class="img"
                                            :style="`padding-top: ${picScaleMap[module.picScale!]};
                                                border-radius: ${module.productRadius}px;
                                                background-size: ${module.picFillType};
                                                background-image: url(${productImage(item)});`"
                                        ></div>
                                    </div>
                                    <div class="price-box" v-if="module.showProductPrice">
                                        <div
                                            class="price"
                                            :style="`background: ${module.moduleColor.priceBackgroundColor}; color: ${module.moduleColor.priceColor}; font-weight: 700; font-family: font1`"
                                        >
                                            ￥{{ item.productPrice }}
                                        </div>
                                    </div>
                                    <div class="origin" v-if="item.marketPrice && module.crossedOutPrice">
                                        <div :style="`color: ${module.moduleColor.crossedOutPriceColor};text-decoration: line-through;`">
                                            {{ item.marketPrice }}
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </Swiper>
                    </div>
                </div>
                <div class="line-box">
                    <div class="line" :style="`background:${module.moduleColor.lineColor}`"></div>
                </div>
                <div class="cont">
                    <div>
                        <div class="mk-title" :style="`color:${module.moduleColor.titleColor}`">
                            {{ module.productGroups[1].groupTitle }}
                            <div
                                :style="`margin-left: 2px; width: ${rightPicInfo.width}px; height: 20px; background: url(${imageFormat(module.rightPic?.picUrl)}) center top / cover no-repeat;`"
                            ></div>
                        </div>
                        <div class="mk-stitle" :style="`color:${module.moduleColor.sellingPointColor}`">{{ module.productGroups[1].groupSubTitle }}</div>
                    </div>
                    <div class="item-box">
                        <Swiper :swiperOption="{ ...swiperOption, slidesPerView: 2, spaceBetween: 6 }" v-model="productGroupsList[1]" @activeIndexChange="">
                            <template v-slot:default="{ item }">
                                <div class="item">
                                    <div class="goods-img" :style="`padding-top: ${picScaleMap[module.picScale!]}; border-radius: ${module.productRadius}px`">
                                        <div
                                            class="img"
                                            :style="`padding-top: ${picScaleMap[module.picScale!]};
                                                border-radius: ${module.productRadius}px;
                                                background-size: ${module.picFillType};
                                                background-image: url(${productImage(item)});`"
                                        ></div>
                                    </div>
                                    <div class="price-box" v-if="module.showProductPrice">
                                        <div
                                            class="price"
                                            :style="`background: ${module.moduleColor.priceBackgroundColor}; color: ${module.moduleColor.priceColor}; font-weight: 700; font-family: font1`"
                                        >
                                            ￥{{ item.productPrice }}
                                        </div>
                                    </div>
                                    <div class="origin" v-if="item.marketPrice && module.crossedOutPrice">
                                        <div :style="`color: ${module.moduleColor.crossedOutPriceColor};text-decoration: line-through;`">
                                            {{ item.marketPrice }}
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </Swiper>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, watchEffect, reactive } from "vue";
import { mergeDefaultModule, formatCommonStyle, CommonCountdown } from "@/views/decorate/decorate/src/modules/index";
import { Swiper } from "@/components/swiper";
import { imageFormat, productImage } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import { getDemoList } from "@/views/decorate/decorate/src/modules/index";
import { mixedProductDisplayDefaults } from "./index";
import type { MixedProductDisplayModule } from "./types";

const swiperOption = ref<any>({
    autoplay: false,
    pagination: false
});
const props = defineProps<{ module: MixedProductDisplayModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, mixedProductDisplayDefaults);
const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});
const picScaleMap: { [key: string]: string } = {
    "3:2": "calc(100% / 3 * 2)",
    "1:1": "calc(100% / 1 * 1)",
    "3:4": "calc(100% / 3 * 4)",
    "16:9": "calc(100% / 16 * 9)"
};

const formtMap: { [key: string]: string } = {
    天: "DD",
    时: "HH",
    分: "mm",
    秒: "ss",
    毫秒: "SSS"
};
const formtText = computed(() => {
    let str = "";
    if (module.timeContent && module.timeContent.length > 0) {
        for (let index = 0; index < module.timeContent.length; index++) {
            const element = module.timeContent[index];
            if (element) {
                str += formtMap[element];
                if (index < module.timeContent.length - 1) {
                    str += ":";
                }
            }
        }
    }
    return str;
});

const productGroupsList = ref<any>({});
watchEffect(() => {
    if (module.productGroups) {
        module.productGroups.forEach(async (group: any, index: number) => {
            if (group.pruductList.length > 0) {
                try {
                    const result = await getProductList({ ids: group.pruductList.map((item: any) => item.productId).join(",") });
                    productGroupsList.value[index] = [...getDemoList(group.pruductList), ...result.records];
                } catch (error) {
                    console.error(error);
                }
            }
        });
    }
});

const rightPicInfo = reactive({
    width: 0,
    height: 20
});
watchEffect(() => {
    if (module.rightPic) {
        const img = new Image();
        img.src = imageFormat(module.rightPic.picUrl);
        img.onload = () => {
            const ratio = img.width / img.height;
            rightPicInfo.width = ratio * 20;
        };
    }
});
</script>
<style lang="less" scoped>
.decorate-slide-box::-webkit-scrollbar {
    display: none;
    height: 0;
}
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}
.comp {
    width: 100%;
    overflow: hidden;
    position: relative;
    display: flex;
}
.cont {
    width: 50%;
    flex: 1;
    overflow: hidden;
    position: relative;
}
.mk-title {
    height: 20px;
    font-size: 16px;
    font-weight: 700;
    overflow: hidden;
    display: flex;
    align-items: center;
}
.mk-stitle {
    height: 20px;
    line-height: 20px;
    font-size: 12px;
    overflow: hidden;
}
.time {
    height: 20px;
    line-height: 20px;
    margin-left: 5px;
    padding: 0 4px;
    border-radius: 4px;
    font-size: 12px;
    display: inline-block;
    font-weight: 400;
}
.item-box {
    margin: 5px -4px 0;
    .item {
        margin: 0 4px;
        display: block;
        position: relative;
        overflow: hidden;
        text-align: center;
        .goods-img {
            display: block;
            width: 100%;
            position: relative;
            padding-top: 100%;
            max-height: 500px;
            overflow: hidden;

            .img {
                position: absolute;
                top: 0;
                right: 0;
                bottom: 0;
                left: 0;
                width: 100%;
                height: 0;
                padding-top: 100%;
                background-repeat: no-repeat;
                background-position: center;
                background-size: 100% auto;
                overflow: hidden;
            }
        }
    }

    .price {
        margin: 5px auto 0;
        padding: 3px 8px;
        overflow: hidden;
        display: inline-block;
        font-size: 12px;
        border-radius: 24px;
    }
    .origin {
        margin-top: 5px;
        height: 16px;
        line-height: 16px;
        font-size: 12px;
    }
}
.line-box {
    padding: 0 10px;
    position: relative;
    .line {
        width: 1px;
        position: absolute;
        top: 0;
        bottom: 0;
        left: 10px;
    }
}
</style>
