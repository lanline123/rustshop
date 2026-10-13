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
            <div class="productgrouping">
                <template v-if="module.groupTitle.showTitle || module.groupTitle.showSubTitle">
                    <div class="tablist">
                        <template v-for="(item, index) in tabList">
                            <div class="tablist-item">
                                <div class="tab-text">
                                    <div class="nav-item">
                                        <div v-if="index > 0" class="line" :style="`border-color:${module.groupTitle.splitLineColor}`"></div>
                                        <div class="nav-item-box" :style="`padding: 0px ${module.groupTitle.marginLeftRight}px;`">
                                            <template v-if="module.groupTitle.showTitle">
                                                <div
                                                    class="tab-title"
                                                    :style="tablistItemStyle.title + `${index === 0 ? `color:${module.groupTitle.activeTitleColor};` : ''}`"
                                                >
                                                    {{ item.title }}
                                                </div>
                                            </template>
                                            <template v-if="module.groupTitle.showSubTitle && item.subTitle">
                                                <div
                                                    class="tab-sub"
                                                    :style="
                                                        tablistItemStyle.subTitle +
                                                        `${index === 0 ? `color:${module.groupTitle.activeSubTitleColor};background-image: linear-gradient(${gradientMap[module.groupTitle.subTitleGradientType as GradientType]}, ${module.groupTitle.gradientColorA}, ${module.groupTitle.gradientColorB});` : ''}`
                                                    "
                                                >
                                                    {{ item.subTitle }}
                                                </div>
                                            </template>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </template>
                    </div>
                </template>

                <div class="productlist">
                    <div class="productlist-item">
                        <div class="" style="margin-top: 5px">
                            <div class="item-box">
                                <template v-for="(item, index) in displayList" :key="index">
                                    <div
                                        class="li"
                                        :style="`opacity: 1; width: ${module.listStyle < 5 ? 100 / module.listStyle : index % 3 === 0 ? 100 : 50}%;`"
                                    >
                                        <div class="goods-item" :style="goodsItemStyle + (item.__isBanner ? '; position: relative;' : '')">
                                            <div
                                                v-if="item.__isBanner"
                                                class="banner-fill"
                                                :style="`position: absolute; top: 0; right: 0; bottom: 0; left: 0;
                                                    background-image: url(${productImage(item)});
                                                    background-size: ${module.picFillType};
                                                    background-position: center;
                                                    background-repeat: no-repeat;
                                                    border-radius: ${module.cardRadius}px;
                                                    z-index: 1;`"
                                            ></div>
                                            <div class="goods-img" :style="`padding-top: ${picScaleMap[module.picScale!]}`">
                                                <div
                                                    v-if="!item.__isBanner"
                                                    class="img"
                                                    :style="`border-radius: ${module.picBottomRadius ? `${module.cardRadius}px` : `${module.cardRadius}px ${module.cardRadius}px 0 0`};
                                                        background-size: ${module.picFillType};
                                                        background-image: url(${productImage(item)});`"
                                                ></div>
                                            </div>
                                            <div
                                                class="goods-info"
                                                :style="`text-align: left; padding: ${module.productColor.infoTopPadding}px ${module.productColor.infoLeftRightPadding}px ${module.productColor.infoBottomPadding}px; ${item.__isBanner ? 'visibility: hidden;' : ''}`"
                                            >
                                                <div
                                                    v-if="module.showProductName"
                                                    class="goods-title"
                                                    :style="`margin-top: 3px;
                                                        height: ${module.productNameRow * 18}px;
                                                        line-height: 18px;
                                                        -webkit-line-clamp: ${module.productNameRow};
                                                        font-weight: ${module.productNameWeight};
                                                        color: ${module.productColor.titleColor};
                                                        font-size:  ${module.productNameSize}px;`"
                                                >
                                                    {{ item.productName }}
                                                </div>
                                                <div
                                                    class="goods-price-box"
                                                    :style="`margin-top: ${module.productPriceMarginTop}px; justify-content: left; display: flex; ${buyBtnWrapped ? 'flex-wrap: wrap;' : ''}`"
                                                >
                                                    <div class="price-origin-sale" :style="`justify-content: left; ${buyBtnWrapped ? 'flex-basis: 100%;' : ''}`">
                                                        <div class="price-origin-icon">
                                                            <div class="price-icon">
                                                                <img
                                                                    v-if="module.productPricePic && module.productPricePic.picUrl"
                                                                    class="icon"
                                                                    :src="module.productPricePic.picUrl"
                                                                    alt=""
                                                                />
                                                                <div
                                                                    v-if="module.showProductPrice"
                                                                    class="price"
                                                                    :style="`color: ${module.productColor.priceColor}; font-weight: ${module.productPriceWeight}; font-family: inherit;font-size:${module.productPriceSize}px;`"
                                                                >
                                                                    ￥{{ item.productPrice }}
                                                                </div>
                                                                <div
                                                                    v-if="module.crossedOutPrice"
                                                                    class="origin"
                                                                    :style="`color: ${module.productColor.crossedOutPriceColor}`"
                                                                >
                                                                    ￥{{ item.marketPrice }}
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <CommonBuyButton
                                                        v-if="Number(module.buyBtnStyle?.showBtn) === 1"
                                                        :style="buyBtnWrapped ? buyBtnWrapMarginStyle : 'margin-top: 0px'"
                                                        :styleOptions="module.buyBtnStyle"
                                                    ></CommonBuyButton>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </div>
                            <div class="more-box" v-if="module.bottomMoreBtn !== 'close'">
                                <div
                                    v-if="module.bottomMoreBtnText && module.bottomMoreBtn === 'text'"
                                    class="more-txt"
                                    :style="`margin: 10px 3px; background-color:${module.productColor.moreBtnBgColor}; border-color:${module.productColor.moreBtnBorderColor}; color: ${module.productColor.moreBtnTextColor}`"
                                >
                                    {{ module.bottomMoreBtnText }}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref, watchEffect } from "vue";
import { ModuleType, ModuleProductGroupingType, GradientType } from "@/types/decorate/decorate.d";
import {
    mergeDefaultModule,
    defaultModuleStyle,
    defaultContentStyle,
    formatCommonStyle,
    defaultBuyButtonStyle,
    gradientMap,
    CommonBuyButton
} from "@/views/decorate/decorate/src/modules/";
import { getProductList } from "@/api/product/product";
import { productImage } from "@/utils/format";
import { getDemoList } from "@/views/decorate/decorate/src/modules";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const productGroups = [
    {
        title: t('新品'),
        subTitle: t('每月上新'),
        titleLink: {},
        bannerPic: {},
        bannerLink: {},
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 88.8,
                marketPrice: 98.8,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872pw39D1oGqdAaZMpvyo.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 56.8,
                marketPrice: 68.88,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872f6EcZnbAqdW5kIGzxL.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872o3s8aqPGdICr1DOCdB.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872Mry8L9BWOCiawPhp72.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872yPYr4zSj7AXrfw9oON.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872NlX49KeGeYSN4Tw0wa.jpg"
            }
        ]
    },
    {
        title: t('热卖'),
        subTitle: t('特别精选'),
        titleLink: {},
        bannerPic: {},
        bannerLink: {},
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 88.8,
                marketPrice: 98.8,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872pw39D1oGqdAaZMpvyo.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 56.8,
                marketPrice: 68.88,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872f6EcZnbAqdW5kIGzxL.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872o3s8aqPGdICr1DOCdB.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872Mry8L9BWOCiawPhp72.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872yPYr4zSj7AXrfw9oON.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872NlX49KeGeYSN4Tw0wa.jpg"
            }
        ]
    },
    {
        title: t('折扣'),
        subTitle: t('便宜好货'),
        titleLink: {},
        bannerPic: {},
        bannerLink: {},
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 88.8,
                marketPrice: 98.8,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872pw39D1oGqdAaZMpvyo.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 56.8,
                marketPrice: 68.88,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872f6EcZnbAqdW5kIGzxL.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872o3s8aqPGdICr1DOCdB.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872Mry8L9BWOCiawPhp72.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872yPYr4zSj7AXrfw9oON.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872NlX49KeGeYSN4Tw0wa.jpg"
            }
        ]
    },
    {
        title: t('预售'),
        subTitle: t('提前锁定'),
        titleLink: {},
        bannerPic: {},
        bannerLink: {},
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 88.8,
                marketPrice: 98.8,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872pw39D1oGqdAaZMpvyo.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 56.8,
                marketPrice: 68.88,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872f6EcZnbAqdW5kIGzxL.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872o3s8aqPGdICr1DOCdB.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872Mry8L9BWOCiawPhp72.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872yPYr4zSj7AXrfw9oON.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('商品的大标题雨价格在后台他会自动调用读取'),
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872NlX49KeGeYSN4Tw0wa.jpg"
            }
        ]
    }
];
const picScaleMap: { [key: string]: string } = {
    "3:2": "calc(100% / 3 * 2)",
    "1:1": "calc(100% / 1 * 1)",
    "3:4": "calc(100% / 3 * 4)",
    "16:9": "calc(100% / 16 * 9)"
};
const module = defineModel<ModuleType & ModuleProductGroupingType>("module") as Ref<ModuleType & ModuleProductGroupingType>;
const defaultModule = ref({
    productSource: "products", //商品数据来源  products:手动选择  productGroups:商品分组
    productGroups: productGroups, // 手动选择商品列表
    groupList: [], // 商品分组列表

    productCardStyle: 1, // 商品卡片样式 1:无边白底, 2:卡片投影 3:描边白底 4:无边透明
    listStyle: 2, // 列表样式  1: t('大图模式'),2: t('一行两个'),3: t('一行三个'),4: t('一行四个'),5: t('一大两小')
    picScale: "1:1", // 图片比例  3:2, 1:1, 3:4, 16:9
    picFillType: "cover", // 图片填充方式  contain:包含  cover:覆盖
    // textAlignment: "center", //导航文字对齐方式 left center

    cardMargin: 6, // 商品横竖间距
    cardPadding: 0, // 商品内边距
    cardRadius: 5, // 商品圆角半径
    picBottomRadius: 1, // 图片底部圆角 1为圆角 0为无圆角

    showProductName: 1, //是否显示商品标题 0为不显示 1为显示
    productNameRow: 1, //商品标题行数 1为单行 2为双行
    productNameWeight: 400, //商品标题字体粗细
    productNameSize: 14, //商品标题字体大小

    // showProductDesc: 1, //是否显示商品描述 0为不显示 1为显示

    showProductPrice: 1, //是否显示商品价格 0为不显示 1为显示
    productPricePic: "", // 价格标签图
    productPriceMarginTop: 10, //商品价格上距
    productPriceWeight: 400, //商品价格字体粗细
    productPrice_size: 14, //商品价格字体大小

    crossedOutPrice: 1, // 商品划线价 0:不显示 1:显示

    buyBtnStyle: defaultBuyButtonStyle, // 购买按钮样式

    bottomMoreBtn: "text", //底部查看更多按钮 text: 文字按钮 image: 自定义图片 close: 不显示
    bottomMoreBtnText: t('查看更多'), // 底部查看更多按钮文字
    bottomMoreBtnImage: "", // 底部查看更多按钮自定义图片

    productColor: {
        infoTopPadding: 5, // 商品信息顶部内距
        infoBottomPadding: 5, // 商品信息底部内距
        infoLeftRightPadding: 3, // 商品信息左右内距
        backgroundColor: "#ffffff", // 商品背景色

        titleColor: "#555555", // 商品标题颜色
        priceColor: "#72B76C", // 商品价格颜色
        crossedOutPriceColor: "#999999", // 商品划线价颜色

        moreBtnBgColor: "#ffffff", // 底部查看更多按钮背景颜色
        moreBtnBorderColor: "#eeeeee", // 底部查看更多按钮边框颜色
        moreBtnTextColor: "#333333" // 底部查看更多按钮文字颜色
    },
    groupTitle: {
        // effectType: "ceiling", //搜索框/导航效果 ceiling 吸顶  default 不吸顶
        marginLeftRight: 0, // 分组标题间距
        showTitle: 1, //是否显示分组大标题 0为不显示 1为显示
        bigTitleFontSize: 16, //分组大标题字体大小 14:小 16:中 18:大
        titleColor: "#444444", //分组大标题颜色
        activeTitleColor: "#72B76C", //分组大标题选中颜色

        showSubTitle: 1, //是否显示分组副标题 0为不显示 1为显示
        titleFontSize: 14, //分组副标题字体大小 12:小 14:中 16:大
        subTitleColor: "#999999", //分组副标题颜色
        activeSubTitleColor: "#ffffff", //分组副标题选中颜色
        subTitleGradientType: "diagonal", //分组副标题渐变类型 diagonal:斜向 upDown:上下 leftRight:左右
        gradientColorA: "#72b76c", //分组副标题渐变颜色A
        gradientColorB: "#72b76c", //分组副标题渐变颜色B
        subTitleRadius: 15, //分组副标题圆角半径
        splitLineColor: "#DADADA" //标题分割线颜色
        // ceilingBgColor: "#ffffff" //吸顶标题背景色
    },
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
});
mergeDefaultModule(module.value, defaultModule.value);
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
});
const tablistItemStyle = computed(() => {
    return {
        title: `color: ${module.value.groupTitle.titleColor}; font-size: ${module.value.groupTitle.bigTitleFontSize}px;`,
        subTitle: `color: ${module.value.groupTitle.subTitleColor}; font-size: ${module.value.groupTitle.titleFontSize}px;border-radius:${module.value.groupTitle.subTitleRadius}px;`
    };
});
const buyBtnWrapped = computed(() => {
    return Number(module.value.buyBtnStyle?.showBtn) === 1 && Number(module.value.buyBtnStyle?.wrap) === 1;
});
// 换行后按 wrapAlign 对齐：按钮是 goods-price-box（flex）的子项，用 auto margin 实现左/中/右
const buyBtnWrapMarginStyle = computed(() => {
    const align = Number(module.value.buyBtnStyle?.wrapAlign ?? 2);
    if (align === 1) return "margin: 5px auto 0 0";
    if (align === 3) return "margin: 5px 0 0 auto";
    return "margin: 5px auto 0";
});
const goodsItemStyle = computed(() => {
    let str = `margin: ${module.value.cardMargin / 2}px;padding: ${module.value.cardPadding}px;border: ${module.value.productCardStyle === 3 ? 1 : 0}px solid rgb(238, 238, 238);border-radius: ${module.value.cardRadius}px;`;
    if (module.value.productCardStyle !== 4) {
        str += `background-color:${module.value.productColor.backgroundColor};`;
    }
    if (module.value.productCardStyle === 2) {
        str += `box-shadow: rgba(93, 113, 127, 0.08) 0px 2px 8px;`;
    }
    return str;
});

const tabList = computed(() => {
    return module.value.productSource === "productGroup" ? module.value.groupList : module.value.productGroups;
});
const currentIndex = ref(0);

const activeGroup = computed<any>(() => {
    const list = tabList.value;
    return Array.isArray(list) && list.length > 0 ? list[currentIndex.value] : null;
});

const activeBannerPic = computed<any>(() => {
    const pic = activeGroup.value?.bannerPic;
    return pic && (pic.picThumb || pic.picUrl) ? pic : null;
});

const list = ref<any>([]);

const displayList = computed<any[]>(() => {
    if (activeBannerPic.value) {
        // banner 卡填充与商品卡同形的占位字段，确保 goods-info 骨架高度与邻接商品卡对齐
        // 真实图片由模板里 .banner-fill 绝对定位覆盖整张卡片；占位字段被外层 visibility: hidden 整体隐藏
        const placeholder = list.value[0] ?? { productName: "_", productPrice: "0", marketPrice: "0" };
        const bannerItem = {
            __isBanner: true,
            picUrl: activeBannerPic.value.picUrl || activeBannerPic.value.picThumb,
            picThumb: activeBannerPic.value.picThumb || activeBannerPic.value.picUrl,
            productName: placeholder.productName,
            productPrice: placeholder.productPrice,
            marketPrice: placeholder.marketPrice
        };
        return [bannerItem, ...list.value];
    }
    return list.value;
});
const currentGroupData = ref<any>({});
const getGroupProductList = async (arr = []) => {
    try {
        const result = await getProductList(currentGroupData.value);
        list.value = [...arr, ...result.records];
    } catch (error) {
        console.error(error);
    }
};
watchEffect(() => {
    currentGroupData.value = {};
    if (module.value.productSource) {
        if (module.value.productSource === "productGroup") {
            if (module.value.groupList.length > 0) {
                const { productGroupId, productNum, productNumType } = module.value.groupList[currentIndex.value];
                currentGroupData.value.productGroupId = productGroupId;
                currentGroupData.value.size = productNumType === "number" ? productNum : 100;
                getGroupProductList();
            } else {
                list.value = [];
            }
        } else {
            if (module.value.productGroups && module.value.productGroups.length > 0) {
                currentGroupData.value.ids = module.value.productGroups[currentIndex.value].productList.map((item: any) => item.productId).join(",");
                getGroupProductList(getDemoList(module.value.productGroups[currentIndex.value].productList));
            }
        }
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
.tablist {
    overflow-x: auto;
    overflow-y: hidden;
    box-sizing: initial;
    height: 100%;
    position: relative;
    display: flex;
    &::-webkit-scrollbar {
        display: none;
        height: 0;
    }
    .tablist-item {
        padding: 0;
        flex: 1 0 auto;

        &.active {
            color: #323233;
            font-weight: 500;
        }

        .tab-text {
            width: 100%;

            .nav-item {
                text-align: center;
                overflow: hidden;
                position: relative;
                .line {
                    content: "";
                    border-right: #dadada solid 1px;
                    position: absolute;
                    left: 0;
                    top: 0;
                    bottom: 0;
                    -webkit-transform: scale(0.5);
                    transform: scale(0.5);
                }

                .nav-item-box {
                    display: inline-grid;
                    .tab-title {
                        margin: 0;
                        font-size: 18px;
                        font-weight: 700;
                        color: #333333;
                    }
                    .tab-sub {
                        margin-top: 5px;
                        padding: 3px 8px;
                        font-size: 14px;
                        color: #454545;
                    }
                }
            }
        }
    }
}
.productlist {
    position: relative;
    display: flex;
    width: 100%;
    height: 100%;

    .productlist-item {
        flex-shrink: 0;
        box-sizing: border-box;
        width: 100%;

        .item-box {
            display: flex;
            flex-wrap: wrap;
            flex-direction: row;
            align-items: stretch;

            .goods-item {
                display: block;
                overflow: hidden;

                .goods-img {
                    position: relative;
                    max-height: 500px;
                    overflow: hidden;
                    width: 100%;
                    height: 0;
                    background-repeat: no-repeat;
                    background-position: center;
                    .img {
                        position: absolute;
                        top: 0;
                        right: 0;
                        bottom: 0;
                        left: 0;
                        background-repeat: no-repeat;
                        background-position: center;
                        background-size: 100% auto;
                        overflow: hidden;
                    }
                }

                .goods-info {
                    padding: 0 5px;
                    flex-grow: 1;
                    flex-shrink: 1;
                    display: flex;
                    -webkit-box-orient: vertical;
                    flex-direction: column;
                    overflow: hidden;
                    font-size: 12px;
                    .goods-title {
                        line-height: 18px;
                        font-size: 14px;
                        overflow: hidden;
                        display: -webkit-box;
                        -webkit-line-clamp: 1;
                        -webkit-box-orient: vertical;
                    }

                    .goods-price-box {
                        display: flex;
                        position: relative;
                        flex-wrap: nowrap;
                        align-items: center;
                        justify-content: space-between;
                        .price-origin-sale {
                            display: flex;
                            align-items: baseline;
                            display: flex;
                            flex-wrap: wrap;
                            overflow: hidden;
                            flex: 1;

                            .price-origin-icon {
                                margin: 3px 0;
                                display: flex;
                                flex-wrap: wrap;
                                align-items: baseline;
                                overflow: hidden;

                                .price-icon {
                                    display: flex;
                                    flex-wrap: wrap;
                                    align-items: self-end;
                                    overflow: hidden;
                                    .icon {
                                        height: 14px;
                                        margin-right: 3px;
                                        display: inline-block;
                                    }
                                    .price {
                                        margin-right: 5px;
                                        text-overflow: ellipsis;
                                        white-space: nowrap;
                                        overflow: hidden;
                                        display: flex;
                                        -webkit-box-align: baseline;
                                        align-items: baseline;
                                    }
                                    .origin {
                                        margin-right: 10px;
                                        text-overflow: ellipsis;
                                        white-space: nowrap;
                                        overflow: hidden;
                                        text-decoration: line-through;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    .more-box {
        display: flex;
        align-items: center;
        justify-content: center;

        .more-txt {
            width: 100%;
            height: 40px;
            line-height: 40px;
            font-size: 14px;
            color: #333333;
            border: 1px solid #eee;
            border-radius: 5px;
            display: inline-block;
            text-align: center;
        }
    }
}
</style>
