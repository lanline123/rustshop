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
            <div class="countdow-warp">
                <div class="mk-top">
                    <div class="mk-top-left">
                        <div class="mk-title line1" :style="`color: ${module.moduleColor.titleColor}`">{{ module.moduleTitle.title }}</div>
                        <div class="mk-stitle line1" :style="`color: ${module.moduleColor.subTitleColor}`">{{ module.moduleTitle.subTitle }}</div>
                    </div>
                    <div class="">
                        <div class="mk-notice" :style="`color: ${module.moduleColor.tipsColor}`">{{ module.moduleTitle.moreText }}</div>
                    </div>
                </div>
                <div class="itemBox">
                    <template v-for="(item, index) in list" :key="index">
                        <div class="li" :style="`flex: 0 0 ${100 / module.rowNum}%`">
                            <div
                                class="item"
                                :style="`padding: ${module.productPadding}px; background-color: ${module.moduleColor.productBackgroundColor}; border-radius: ${module.productRadius}px`"
                            >
                                <div class="imgBox" :style="`padding-top: ${picScaleMap[module.picScale!]}`">
                                    <div
                                        class="img"
                                        :style="` border-radius: ${module.picBottomRadius === 1 ? `${module.productRadius}px  ${module.productRadius}px 0 0` : `${module.productRadius}px`};
                                            background-size: ${module.picFillType};
                                            background-image: url(${productImage(item)});`"
                                    ></div>
                                </div>
                                <div class="goods-info" :style="`padding-bottom: 0px; text-align: ${module.textAlignment}`">
                                    <div class="title line2" v-if="module.showProductName" :style="`color: ${module.moduleColor.productNameColor}`">
                                        {{ item.productName ? item.productName : $t('商品名称') }}
                                    </div>
                                    <div class="priceBox" v-if="module.showProductPrice" :style="`justify-content: ${module.textAlignment}`">
                                        <div
                                            class="price"
                                            :style="`color: ${module.moduleColor.productPriceColor}; font-weight: ${module.headlines}; font-family: inherit`"
                                        >
                                            ￥{{ item.productPrice }}
                                        </div>
                                        <div class="origin" v-if="module.crossedOutPrice" :style="`color: ${module.moduleColor.crossedOutPriceColor}`">
                                            {{ item.marketPrice }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
            <div
                v-if="module.countdownData.showCountdown"
                class="countdown"
                :style="`margin-top: -${module.countdownData.countdownPicBottom}px; border-radius: 0px 0px 6px 6px`"
            >
                <img class="countdown-img" :src="imageFormat(module.countdownData.countdownBackgroundPic?.picUrl)" />
                <div class="count-down" :style="`top: ${module.countdownData.marginTop}%;`">
                    <CommonCountdown
                        :countdownType="module.countdownData.countdownType"
                        :value-style="{
                            color: module.moduleColor.countdownColor,
                            'font-size': '16px',
                            display: 'block'
                        }"
                        :format="formtText"
                        :dailyTime="module.countdownData.dailyTime"
                        :specifyTime="module.countdownData.specifyTime"
                    >
                    </CommonCountdown>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref, watchEffect } from "vue";
import { ModuleType, ModuleCountdownType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultModuleStyle, defaultContentStyle, formatCommonStyle, CommonCountdown } from "@/views/decorate/decorate/src/modules/";
import { getDaysLaterEndTime, formatTime } from "@/utils/time";
import { imageFormat, productImage } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import { getDemoList } from "@/views/decorate/decorate/src/modules";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const pruductList = [
    {
        productId: 0,
        productSn: "",
        productName: t('麦片'),
        productDesc: "",
        productPrice: 29.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908662JqyhZ6f1BCsWeUJlgj.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: t('巧克力棒'),
        productDesc: "",
        productPrice: 29.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908662cOmZ28i8gKl6JiUBad.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: t('汤达人'),
        productDesc: "",
        productPrice: 29.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908662AztoZ2OADHp1b8vUoQ.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: t('士力架'),
        productDesc: "",
        productPrice: 29.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908662O7VZIzaFtItS4OvAOR.jpg"
    }
];
const module = defineModel<ModuleType & ModuleCountdownType>("module") as Ref<ModuleType & ModuleCountdownType>;
const defaultModule = ref({
    pruductList: pruductList, //商品列表
    moduleColorStyle: "orange", // 模块颜色风格
    moduleTitle: {
        // 模块标题
        title: t('限时抢购'), // 大标题
        subTitle: t('超低价格，史无前例'), // 小标题
        moreText: t('向左滑动查看更多'), // 查看更多文案
        titleLink: {
            // 标题跳转链接
            link: "",
            title: ""
        }
    },
    countdownData: {
        //倒计时数据
        showCountdown: 1, // 是否显示倒计时
        countdownBackgroundPic: "", // 倒计时背景图
        countdownType: "dailyCycle", // 倒计时类型  dailyCycle: 每日循环 specifyTime: 指定时间
        dailyTime: formatTime(new Date(), 20), // 倒计时开始和结束时间
        specifyTime: getDaysLaterEndTime(1), // 指定时间  默认值3天后
        timeContent: [t('天'), t('时'), t('分'), t('秒')], // 倒计时内容 天 时 分 秒
        marginTop: 50, // 倒计时距离顶部距离
        countdownPicBottom: 30 // 倒计时图片上浮距离
    },

    rowNum: 3, //每屏显示数量  2 / 2.5 / 3 / 3.5
    picScale: "1:1", // 图片比例  3:2, 1:1, 3:4, 16:9
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    textAlignment: "center", //导航文字对齐方式 left center
    productPadding: 5, // 商品内部间距
    productRadius: 5, // 商品圆角半径
    picBottomRadius: 1, // 图片底部圆角  1: 直角 2: 圆角

    showProductName: 0, // 显示商品名称 0:不显示 1:显示
    showProductPrice: 1, // 显示商品价格 0:不显示 1:显示
    priceWeight: 700, // 商品价格字体粗细
    crossedOutPrice: 1, // 商品划线价 0:不显示 1:显示
    // productSales: 1,  // 显示商品销量 0:不显示 1:显示
    moduleColor: {
        // 模块颜色设置
        titleColor: "#ffffff", // 模块大标题颜色
        subTitleColor: "#ffffff", // 模块小标题颜色
        tipsColor: "#fafafa", // 提示信息颜色
        productBackgroundColor: "#ffffff", // 商品背景颜色
        productNameColor: "#333333", // 商品标题颜色
        productPriceColor: "#72b76c", // 商品价格颜色
        crossedOutPriceColor: "#999999", // 商品划线价颜色
        countdownColor: "#f7f7f7" // 倒计时文本颜色
    },
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
});
mergeDefaultModule(module.value, defaultModule.value);
const picScaleMap: { [key: string]: string } = {
    "3:2": "calc(100% / 3 * 2)",
    "1:1": "calc(100% / 1 * 1)",
    "3:4": "calc(100% / 3 * 4)",
    "16:9": "calc(100% / 16 * 9)"
};
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
});

const formtMap: { [key: string]: string } = {
    天: "DD",
    时: "HH",
    分: "mm",
    秒: "ss"
};
const textMap: { [key: string]: string } = {
    DD: t('天'),
    HH: t('时'),
    mm: t('分'),
    ss: t('秒')
};
const formtText = computed(() => {
    let str = "";
    if (module.value.countdownData.timeContent && module.value.countdownData.timeContent.length > 0) {
        for (let index = 0; index < module.value.countdownData.timeContent.length; index++) {
            const element = module.value.countdownData.timeContent[index];
            if (element) {
                str += ` ${formtMap[element]} ${textMap[formtMap[element]]}`;
            }
        }
    }
    return str;
});

const list = ref<any[]>([]);
watchEffect(async () => {
    if (module.value.pruductList && module.value.pruductList.length > 0) {
        try {
            const result = await getProductList({ ids: module.value.pruductList.map((item: any) => item.productId).join(",") });
            list.value = [...getDemoList(module.value.pruductList), ...result.records];
        } catch (error) {
            console.error(error);
        }
    }
});
</script>
<style lang="less" scoped>
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}

.countdow-warp {
    position: relative;
    padding: 8px 10px 10px 10px;
    overflow: hidden;
    .mk-top {
        display: flex;
        margin-bottom: 3px;
        justify-content: space-between;
        align-items: baseline;

        .mk-top-left {
            flex: 1;
            height: 30px;
            display: flex;
            align-items: baseline;
            padding-right: 5px;
            overflow: hidden;

            .mk-title {
                height: 30px;
                line-height: 30px;
                font-size: 20px;
                font-weight: 700;
                overflow: hidden;
            }
            .mk-stitle {
                margin-left: 5px;
                height: 20px;
                line-height: 20px;
                font-size: 14px;
                overflow: hidden;
            }
        }

        .mk-notice {
            font-size: 12px;
        }
    }

    .itemBox {
        margin: 0 -4px;
        display: flex;
        flex-wrap: nowrap;
        flex-direction: row;
        overflow-x: auto;
        overflow-y: hidden;
        &::-webkit-scrollbar {
            display: none;
            height: 0;
        }

        .li {
            flex-shrink: 0;
            overflow: hidden;
            .item {
                margin: 0 4px;
                display: block;
                overflow: hidden;
                .imgBox {
                    position: relative;
                    max-height: 500px;
                    overflow: hidden;
                    .img {
                        position: absolute;
                        top: 0;
                        right: 0;
                        bottom: 0;
                        left: 0;
                        width: 100%;
                        background-repeat: no-repeat;
                        background-position: center;
                        background-size: 100% auto;
                        background-color: #ffffff;
                        overflow: hidden;
                    }
                }

                .goods-info {
                    padding: 3px 0 5px;
                    flex-grow: 1;
                    flex-shrink: 1;
                    display: flex;
                    flex-direction: column;
                    overflow: hidden;
                    text-align: center;
                    font-size: 12px;

                    .priceBox {
                        height: 20px;
                        line-height: 20px;
                        overflow: hidden;
                        display: flex;
                        align-items: baseline;
                        font-size: 12px;
                        .origin {
                            text-decoration: line-through;
                            margin-left: 5px;
                        }
                        .title {
                            height: 18px;
                            line-height: 18px;
                            font-size: 12px;
                        }
                    }
                }
            }
        }
    }
}

.countdown {
    margin-top: -20px;
    width: 100%;
    z-index: 1;
    -webkit-transform: translate3d(0, 0, 1px);
    transform: translate3d(0, 0, 1px);
    overflow: hidden;
    .countdown-img {
        position: relative;
        display: block;
        width: 100%;
        height: 100%;
        z-index: 1;
    }
    .count-down {
        width: 100%;
        position: absolute;
        display: flex;
        align-items: baseline;
        justify-content: center;
        z-index: 2;
    }
}

.line2 {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  /* white-space: nowrap;  去掉这行才会显示2行，不然只能显示1行省略号 */
}

</style>
