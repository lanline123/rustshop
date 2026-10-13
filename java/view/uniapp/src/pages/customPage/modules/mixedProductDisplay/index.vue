<template>
    <view
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
        <view
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <view class="comp">
                <view class="cont">
                    <view @click="urlFormat(module.productGroups[0].moreLink) && redirect({ url: urlFormat(module.productGroups[0].moreLink) })">
                        <view class="mk-title" :style="`color:${module.moduleColor.titleColor}`">
                            <text class="line1">
                                {{ $t(module.productGroups[0].groupTitle) }}
                            </text>
                            <view
                                v-if="module.showCountdown"
                                class="time"
                                :style="`background:${module.moduleColor.countdownBackgroundColor}; color:${module.moduleColor.countdownColor}`"
                            >
                                <template v-if="new Date().getTime() > Number(module.dailyTime[0])">
                                    <commonCountdown
                                        :countdown-type="module.countdownType"
                                        :format="formtText"
                                        :daily-time="module.dailyTime"
                                        :specify-time="module.specifyTime"
                                        :color="module.moduleColor.countdownColor"
                                    />
                                </template>
                                <template v-else>{{ $t("活动未开始") }}</template>
                            </view>
                        </view>
                        <view class="mk-stitle line1" :style="`color:${module.moduleColor.sellingPointColor}`">{{
                            $t(module.productGroups[0].groupSubTitle)
                        }}</view>
                    </view>
                    <view class="item-box">
                        <tig-swiper v-model="productGroupsList[0]" :display-multiple-items="2" :height="height + 'px'" :dot="false" :autoplay="false">
                            <template #default="{ item }">
                                <view class="item" @click="item.productId && redirect({ url: urlFormat({ path: 'product', data: { id: item.productId } }) })">
                                    <view class="goods-img" :style="`padding-top: ${picScaleMap[module.picScale!]}; border-radius: ${module.productRadius}px`">
                                        <view
                                            class="img"
                                            :style="`padding-top: ${picScaleMap[module.picScale!]};
                                                border-radius: ${module.productRadius}px;
                                                background-size: ${module.picFillType};
                                                background-image: url(${productImage(item)});`"
                                        />
                                    </view>
                                    <view v-if="module.showProductPrice" class="price-box">
                                        <view
                                            class="price"
                                            :style="`background: ${module.moduleColor.priceBackgroundColor}; color: ${module.moduleColor.priceColor}; font-weight: 700; font-family: font1`"
                                        >
                                            <format-price
                                                :font-style="{ fontSize: '14px', lineHeight: 1 }"
                                                :currency-style="{
                                                    fontSize: '12px'
                                                }"
                                                :decimals-style="{
                                                    fontSize: '12px'
                                                }"
                                                :price-data="item.productPrice"
                                            />
                                        </view>
                                    </view>
                                    <view v-if="module.crossedOutPrice && item.marketPrice && item.marketPrice > 0 && !showB2bText" class="origin">
                                        <view :style="`color: ${module.moduleColor.crossedOutPriceColor};text-decoration: line-through;`">
                                            <format-price
                                                :font-style="{ fontSize: '12px', 'text-decoration': 'line-through' }"
                                                :price-data="item.marketPrice"
                                            />
                                        </view>
                                    </view>
                                </view>
                            </template>
                        </tig-swiper>
                    </view>
                </view>
                <view class="line-box">
                    <view class="line" :style="`background:${module.moduleColor.lineColor}`" />
                </view>
                <view class="cont">
                    <view @click="urlFormat(module.productGroups[1].moreLink) && redirect({ url: urlFormat(module.productGroups[1].moreLink) })">
                        <view class="mk-title line1" :style="`color:${module.moduleColor.titleColor}`">
                            <text class="line1">
                                {{ $t(module.productGroups[1].groupTitle) }}
                            </text>
                            <view
                                :style="`margin-left: 2px; width: ${rightPicInfo.width}px; height: 20px; background: url(${imageFormat(
                                    module.rightPic?.picUrl
                                )}) center top / cover no-repeat;`"
                            />
                        </view>
                        <view class="mk-stitle line1" :style="`color:${module.moduleColor.sellingPointColor}`">{{
                            $t(module.productGroups[1].groupSubTitle)
                        }}</view>
                    </view>
                    <view class="item-box">
                        <tig-swiper v-model="productGroupsList[1]" :display-multiple-items="2" :height="height + 'px'" :dot="false" :autoplay="false">
                            <template #default="{ item }">
                                <view class="item" @click="item.productId && redirect({ url: urlFormat({ path: 'product', data: { id: item.productId } }) })">
                                    <view class="goods-img" :style="`padding-top: ${picScaleMap[module.picScale!]}; border-radius: ${module.productRadius}px`">
                                        <view
                                            class="img"
                                            :style="`padding-top: ${picScaleMap[module.picScale!]};
                                                border-radius: ${module.productRadius}px;
                                                background-size: ${module.picFillType};
                                                background-image: url(${productImage(item)});`"
                                        />
                                    </view>
                                    <view v-if="module.showProductPrice" class="price-box">
                                        <view
                                            class="price"
                                            :style="`background: ${module.moduleColor.priceBackgroundColor}; color: ${module.moduleColor.priceColor}; font-weight: 700; font-family: font1`"
                                        >
                                            <format-price
                                                :font-style="{ fontSize: '14px', lineHeight: 1 }"
                                                :currency-style="{
                                                    fontSize: '12px'
                                                }"
                                                :decimals-style="{
                                                    fontSize: '12px'
                                                }"
                                                :price-data="item.productPrice"
                                            />
                                        </view>
                                    </view>
                                    <view v-if="item.marketPrice && module.crossedOutPrice && !showB2bText" class="origin">
                                        <view :style="`color: ${module.moduleColor.crossedOutPriceColor};text-decoration: line-through;`">
                                            <format-price
                                                :font-style="{ fontSize: '12px', 'text-decoration': 'line-through' }"
                                                :price-data="item.marketPrice"
                                            />
                                        </view>
                                    </view>
                                </view>
                            </template>
                        </tig-swiper>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watchEffect, getCurrentInstance, nextTick } from "vue";
import { formatCommonStyle } from "@/components/modules";
import { urlFormat, imageFormat, productImage } from "@/utils/format";
import commonCountdown from "@/pages/customPage/modules/commonCountdown/index.vue";
import { redirect, getElementRect, getImageInfo } from "@/utils";
import { getProductsList } from "@/api/common";
import { isShowB2bPriceText } from "@/utils/b2bPrice";

const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});

const isLoading = ref(true);
const productGroupsList = ref<Record<number, any[]>>({});
const getProductGroupsList = async () => {
    const promises = props.module.productGroups.map(async (group: any, index: number) => {
        try {
            const ids = group.pruductList.map((item: any) => item.productId);
            const result = await getProductsList({ ids: ids.join(","), size: ids.length });
            return { index, data: result.records };
        } catch (error) {
            console.error(`Error fetching product group ${index}:`, error);
            return { index, data: [] };
        }
    });

    const results = await Promise.all(promises);
    results.forEach(({ index, data }) => {
        productGroupsList.value[index] = data;
    });
    isLoading.value = false;
    // nextTick(() => {
    //     getImgHeight();
    // });
    // setTimeout(() => {
    //     getImgHeight();
    // }, 350);
};

const commonStyle = computed(() => {
    return formatCommonStyle(props.module.moduleStyle ?? {}, props.module.contentStyle ?? {}, props.module.subTitle ?? {});
});

const showB2bText = computed(() => isShowB2bPriceText());

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
    if (props.module.timeContent && props.module.timeContent.length > 0) {
        for (let index = 0; index < props.module.timeContent.length; index++) {
            const element = props.module.timeContent[index];
            if (element) {
                str += formtMap[element];
                if (index < props.module.timeContent.length - 1) {
                    str += ":";
                }
            }
        }
    }
    return str;
});
const imgHeight = ref(0);
const height = computed(() => {
    let num = imgHeight.value;
    if (props.module.showProductPrice) {
        num += 31;
    }
    if (props.module.crossedOutPrice) {
        num += 30;
    }
    return num;
});
const instance = getCurrentInstance();
const getImgHeight = async () => {
    try {
        const res = await getElementRect(".goods-img", instance);
        if (res) {
            imgHeight.value = res.height;
        }
    } catch (error) {
        console.error(error);
    }
};

const rightPicInfo = reactive({
    width: 0,
    height: 20
});
const getRightPicInfo = async () => {
    try {
        const res = await getImageInfo(imageFormat(props.module.rightPic.picUrl));
        rightPicInfo.width = (res.width / res.height) * 20;
    } catch (error) {
        console.error(error);
    }
};
const windowResizeCallback = () => {
    if (props.module.productGroups.length > 0) {
        nextTick(() => {
            getImgHeight();
        });
    }
    if (props.module.rightPic) {
        getRightPicInfo();
    }
};
uni.onWindowResize(windowResizeCallback);
onUnmounted(() => {
    uni.offWindowResize(windowResizeCallback);
});
onMounted(() => {
    if (props.module.productGroups.length > 0) {
        getProductGroupsList();
    }
});
watchEffect(() => {
    if (props.module.rightPic) {
        getRightPicInfo();
    }
    if (!isLoading.value) {
        setTimeout(() => {
            getImgHeight();
        }, 350);
    }
});
</script>

<style lang="scss" scoped>
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
        white-space: nowrap;
        overflow: hidden;
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
