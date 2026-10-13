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
                commonStyle.moduleContentStyle.boxRadius2 +
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
                                <template v-else>{{ $t('活动未开始') }}</template>
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
import { ref, computed, Ref, watchEffect, reactive } from "vue";
import { ModuleType, ModuleMixedProductDisplayType } from "@/types/decorate/decorate.d";
import { getDaysLaterEndTime, formatTime } from "@/utils/time";
import { mergeDefaultModule, defaultModuleStyle, defaultContentStyle, formatCommonStyle, CommonCountdown } from "@/views/decorate/decorate/src/modules/";
import { Swiper } from "@/components/swiper";
import { imageFormat, productImage } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import { getDemoList } from "@/views/decorate/decorate/src/modules";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const swiperOption = ref<any>({
    autoplay: false,
    pagination: false
});
const module = defineModel<ModuleType & ModuleMixedProductDisplayType>("module") as Ref<ModuleType & ModuleMixedProductDisplayType>;
const url = {
    picId: 1454,
    picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650293Kfvp2BXElAzXCOY7pq.png?x-oss-process=image/resize,m_pad,h_200,h_200",
    picUrl: "https://oss.tigshop.com/img/gallery/202411/1731650293Kfvp2BXElAzXCOY7pq.png",
    picName: "Fi_ZwwrzwDQddtmqTBCAyht4dPI"
};
const groups = [
    {
        groupTitle: t('限时福利'),
        groupSubTitle: t('直触底价 先到先得'),
        groupType: "product",
        pruductList: [
            {
                productId: 0,
                productSn: "",
                productName: t('德宝纸巾'),
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650293KF9TjvpDKHOATMR3H7.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('薯片'),
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650294a4EsPq1zkYme6p5OhC.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('饼干'),
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650294hYUnaJSx7UhsjhhGEi.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('奶粉'),
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650293zLV7bKUMAFWH6hrM7m.jpg"
            }
        ],
        moreLink: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        }
    },
    {
        groupTitle: t('发现好货'),
        groupSubTitle: t('精选推荐 多买多省'),
        groupType: "product",
        pruductList: [
            {
                productId: 0,
                productSn: "",
                productName: t('高露洁牙膏'),
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/17316502936Ow5WaZszFnuYuGzB8.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('黑人牙膏'),
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650293Tx6p8XKkDJPhDOOAT3.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('高露洁牙膏'),
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650293yDagFw4VE9FG1oJSnW.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('黑人牙膏'),
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650294KeYpvFBkgkloWdbqX7.jpg"
            }
        ],
        moreLink: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        }
    }
];
const defaultModule = ref({
    productGroups: groups, // 商品分组
    rightPic: url, // 右侧图片
    showCountdown: 1, // 是否显示倒计时 1:显示, 0:不显示
    countdownType: "dailyCycle", // 倒计时类型  dailyCycle: 每日循环 specifyTime: 指定时间
    dailyTime: [formatTime(new Date(), 8), formatTime(new Date(), 20)], // 倒计时开始和结束时间
    specifyTime: getDaysLaterEndTime(3), // 指定时间  默认值3天后
    timeContent: [t('时'), t('分'), t('秒')], // 倒计时内容  时 分 秒 毫秒

    picScale: "1:1", // 图片比例  3:2, 1:1, 3:4, 16:9
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    productRadius: 6, // 商品圆角
    morePosition: "top", // 更多链接占位 top:顶部标题区, auto:标题+商品区

    showProductPrice: 1, // 是否显示商品价格 1:显示, 0:不显示
    priceWeight: 400, // 商品价格字体粗细
    crossedOutPrice: 1, // 是否显示商品划线价 1:显示, 0:不显示
    productSales: 0, // 商品销量 0:不显示, 1:显示

    moduleColor: {
        // 模块颜色设置
        titleColor: "#171B1E", // 模块大标题颜色
        sellingPointColor: "#999999", // 卖点小标题颜色
        countdownBackgroundColor: "#72B76C", // 倒计时背景颜色
        countdownColor: "#ffffff", // 倒计时数字颜色

        priceBackgroundColor: "#72B76C", // 商品价格背景颜色
        priceColor: "#ffffff", // 商品价格颜色
        crossedOutPriceColor: "#999999", // 商品划线价格颜色
        lineColor: "#eeeeee" // 中间分割线颜色
    },

    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
});
mergeDefaultModule(module.value, defaultModule.value);
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
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
    if (module.value.timeContent && module.value.timeContent.length > 0) {
        for (let index = 0; index < module.value.timeContent.length; index++) {
            const element = module.value.timeContent[index];
            if (element) {
                str += formtMap[element];
                if (index < module.value.timeContent.length - 1) {
                    str += ":";
                }
            }
        }
    }
    return str;
});

const productGroupsList = ref<any>({});
watchEffect(() => {
    if (module.value.productGroups) {
        module.value.productGroups.forEach(async (group: any, index: number) => {
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
    if (module.value.rightPic) {
        const img = new Image();
        img.src = imageFormat(module.value.rightPic.picUrl);
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
