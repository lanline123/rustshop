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
            <div>
                <div class="groupsliding">
                    <template v-for="(group, index) in module.groupList" :key="group">
                        <div
                            class="slide-box"
                            :style="`background-color: ${group.backgroundColor}; border-radius: ${module.groupRadius}px;width: ${module.groupWidth}%;flex-shrink: 0;`"
                        >
                            <div class="bg_title">
                                <div class="title_box">
                                    <img class="img" :src="imageFormat(module.moduleColor.titlePic?.picUrl)" alt="" srcset="" />
                                    <div class="p" :style="`color: ${module.moduleColor.titleColor}; font-size: ${module.moduleColor.titleSize}px`">
                                        {{ group.title }}
                                    </div>
                                </div>
                                <div class="p" :style="`color: ${module.moduleColor.subTitleColor}; font-size: ${module.moduleColor.subTitleSize}px`">
                                    {{ group.subTitle }}
                                </div>
                            </div>
                            <div
                                class="item-box"
                                :style="`border-radius: ${module.groupRadius}px; background:${module.moduleColor.productBackgroundColor}`"
                            >
                                <template v-for="(item, itemIndex) in productGroupsList[index]" :key="itemIndex">
                                    <div class="li">
                                        <div
                                            class="goods-item"
                                            :style="`display: flex; border-radius: ${module.productPicRightRadius ? module.productPicRadius + 'px' : `${module.productPicRadius}px 0 0 ${module.productPicRadius}px`}`"
                                        >
                                            <div
                                                class="goods-img"
                                                :style="`flex-shrink: 0; width: ${module.productPicRatio}%; padding-top: ${module.productPicRatio}%`"
                                            >
                                                <div
                                                    class="img"
                                                    :style="`border-radius: ${module.productPicRightRadius ? module.productPicRadius + 'px' : `${module.productPicRadius}px 0 0 ${module.productPicRadius}px`};
                                                    background-size: cover;
                                                    background-image: url(${productImage(item)});`"
                                                ></div>
                                                <img class="ranking" v-if="itemIndex <= 2 && module.showRanking" :src="rankingImgs[itemIndex]" />
                                            </div>
                                            <div class="goods-info" :style="`justify-content: center; padding: 0px 0px 0px 10px`">
                                                <div
                                                    v-if="module.showProductName"
                                                    class="goods-title"
                                                    :style="`margin-top: 0px;
                                                        height: ${module.productNameRow * 18}px;
                                                        line-height: 18px;
                                                        -webkit-line-clamp: ${module.productNameRow};
                                                        font-weight: ${module.productNameWeight};
                                                        color: rgb(51, 51, 51);
                                                        font-size: ${module.productNameSize}px;`"
                                                >
                                                    {{ item.productName }}
                                                </div>
                                                <div class="tag" v-if="module.showProductLabel && formatLabel(group.label)[itemIndex]">
                                                    <div
                                                        class="span"
                                                        :style="`padding: 0px 5px;
                                                            background-color: ${module.moduleColor.labelBackgroundColor};
                                                            color: ${module.moduleColor.labelTextColor};
                                                            display: inline-block;`"
                                                    >
                                                        {{ formatLabel(group.label)[itemIndex] }}
                                                    </div>
                                                </div>
                                                <div class="goods-price-box" :style="`margin-top: ${module.productPriceMarginTop}px`">
                                                    <div class="price-origin-sale">
                                                        <div class="price-origin-icon">
                                                            <div class="price-icon">
                                                                <div
                                                                    v-if="module.showProductPrice"
                                                                    class="price"
                                                                    :style="`color: ${module.moduleColor.productPriceColor}; font-weight: 700; font-family: font1`"
                                                                >
                                                                    ￥{{ item.productPrice }}
                                                                </div>
                                                            </div>
                                                            <div
                                                                v-if="module.crossedOutPrice"
                                                                class="origin"
                                                                :style="`color: ${module.moduleColor.crossedOutPriceColor}`"
                                                            >
                                                                ¥{{ item.marketPrice }}
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <CommonBuyButton
                                                        v-if="module.buyBtnStyle && Number(module.buyBtnStyle.showBtn) === 1"
                                                        :styleOptions="module.buyBtnStyle"
                                                    ></CommonBuyButton>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref, watchEffect } from "vue";
import { ModuleType, ModuleGroupSlidingType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultModuleStyle, formatCommonStyle, defaultBuyButtonStyle, CommonBuyButton } from "@/views/decorate/decorate/src/modules/";
import { imageFormat, productImage } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import { getDemoList } from "@/views/decorate/decorate/src/modules";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const rankingImgs = [
    "https://oss.tigshop.com/img/gallery/202411/1732094008g4ZMmuQbNcBlYrNCuz.png",
    "https://oss.tigshop.com/img/gallery/202411/1732094013OJOLUek96BizcTvwmU.png",
    "https://oss.tigshop.com/img/gallery/202411/17320940168dzp9YD5LbpcMMjqe9.png"
];
const groupList = [
    {
        title: t('日用清洁热卖榜〉'),
        subTitle: t('快来跟大家一起买'),
        titleLink: {
            link: "",
            title: ""
        },
        backgroundColor: "#72b76c",
        label: `今天直降十元 
                加量不加价 
                抢超值2大桶
                加量不加价 
                抢超值2大桶`,
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/17319190474y0XWFxO8GJZF0dg64.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/17319190474uihMnJYOkEzvGBeWv.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919047ZXFKxeDYmDn9zNQSA3.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/17319190476keLc7Twr4BYAVlUBQ.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/173191904794zD2Sy4XugNwMUBOW.jpg"
            }
        ]
    },
    {
        title: t('家居日用热卖榜〉'),
        subTitle: t('正能家居 生活好帮手'),
        titleLink: {
            link: "",
            title: ""
        },
        backgroundColor: "#818dfc",
        label: `今天直降十元 
                加量不加价 
                抢超值2大桶
                加量不加价 
                抢超值2大桶`,
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/17319190597yMADwwbOKqaA9pN5O.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919059AeiNkel9O1UtD2eZeD.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919059ts7YHQo7rbPam9y8ZQ.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919059VFsxMuKvPxKoFNfq5y.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919059GV4LN9Xq2j5ACzoJqx.jpg"
            }
        ]
    },
    {
        title: t('家居日用热卖榜〉'),
        subTitle: t('正能家居 生活好帮手'),
        titleLink: {
            link: "",
            title: ""
        },
        backgroundColor: "#ff6600",
        label: `今天直降十元 
                加量不加价 
                抢超值2大桶
                加量不加价 
                抢超值2大桶`,
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071axvkfUDkQNFbIzF1iY.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071lOa0ZBx2mYlvq1lgjr.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071xUnObq2LokKPFecwXq.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071rwvbdR1BOrBne9YHqd.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('品牌旗舰电商版商超版洗洁精'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071r7emytS94COnCxRtcw.jpg"
            }
        ]
    }
];
const module = defineModel<ModuleType & ModuleGroupSlidingType>("module") as Ref<ModuleType & ModuleGroupSlidingType>;
const defaultModule = ref({
    groupList: groupList, //分组数据
    groupWidth: 70, //分组宽度
    groupRadius: 8, //分组上下圆角
    productPicRatio: 36, //商品图片占比
    productPicRadius: 6, //商品图片圆角
    productPicRightRadius: 1, //商品图片右侧圆角 0为无圆角 1为圆角

    showRanking: 1, //是否显示排名 0为不显示 1为显示
    showProductLabel: 1, //是否显示商品标签 0为不显示 1为显示

    showProductName: 1, //是否显示商品标题 0为不显示 1为显示
    productNameRow: 1, //商品标题行数 1为单行 2为双行
    productNameWeight: 400, //商品标题字体粗细
    productNameSize: 14, //商品标题字体大小

    showProductPrice: 1, //是否显示商品价格 0为不显示 1为显示
    productPrice_weight: 700, //商品价格字体粗细
    productPrice_size: 14, //商品价格字体大小
    // showDiscountLabel: 0, //是否显示商优惠价标签 0为不显示 1为显示
    productPriceMarginTop: 10, //商品价格上距

    crossedOutPrice: 1, // 商品划线价 0:不显示 1:显示

    buyBtnStyle: defaultBuyButtonStyle, // 购买按钮样式
    moduleColor: {
        titleSize: 20, // 模块大标题字体大小
        subTitleSize: 14, // 模块小标题字体大小
        titleColor: "#ffffff", // 模块大标题颜色
        subTitleColor: "#ffffff", // 模块小标题颜色
        titlePic: {
            // 模块大标题图标
            picId: 1489,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919039RxeYzkD0P8ByOeKiNa.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731919039RxeYzkD0P8ByOeKiNa.png",
            picName: "FljdiuyonglOZKlYhkv9ZcrSw44_"
        },

        productBackgroundColor: "#ffffff", // 商品背景颜色
        productNameColor: "#333333", // 商品背景颜色
        labelBackgroundColor: "#FFF1F6", // 商品标签背景颜色
        labelTextColor: "#FB264D", // 商品标签文字颜色
        productPriceColor: "#72b76c", // 商品价格颜色
        crossedOutPriceColor: "#999999" // 商品划线价颜色
    },
    moduleStyle: defaultModuleStyle // 模块样式
});
mergeDefaultModule(module.value, defaultModule.value);
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
});

const formatLabel = (label: string) => {
    return label.split("\n");
};

const productGroupsList = ref<any>({});
watchEffect(() => {
    if (module.value.groupList && module.value.groupList.length > 0) {
        module.value.groupList.forEach(async (group: any, index: number) => {
            if (group.productList && group.productList.length > 0) {
                try {
                    const result = await getProductList({ ids: group.productList.map((item: any) => item.productId).join(",") });
                    productGroupsList.value[index] = [...getDemoList(group.productList), ...result.records];
                } catch (error) {
                    console.error(error);
                }
            }
        });
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

.groupsliding {
    display: flex;
    width: 100%;
    overflow: hidden;
    overflow-x: auto;
    align-items: flex-start;
    &::-webkit-scrollbar {
        display: none;
        height: 0;
    }
}

.slide-box {
    margin: 0 5px;
    overflow: hidden;
    padding: 10px 8px 8px;
    &:first-child {
        margin-left: 0;
    }
}
.bg_title {
    display: block;
    overflow: hidden;
    margin-bottom: 10px;

    .title_box {
        display: flex;
        align-items: center;
        padding-bottom: 5px;

        .img {
            height: 20px;
            margin-right: 3px;
        }
    }
    .p {
        line-height: 20px;
        font-weight: 600;
    }
}

.item-box {
    padding: 4px 8px;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .li {
        flex-shrink: 0;
        overflow: hidden;
        margin: 4px 0;

        .goods-item {
            overflow: hidden;
            display: flex;

            .goods-img {
                position: relative;
                max-height: 500px;
                overflow: hidden;
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

            .ranking {
                position: absolute;
                top: 4px;
                left: 4px;
                width: 20px;
            }
        }

        .goods-info {
            padding: 0 5px;
            flex-grow: 1;
            flex-shrink: 1;
            display: flex;
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

            .tag {
                margin-top: 5px;
                overflow: hidden;

                .span {
                    height: 20px;
                    line-height: 20px;
                    font-size: 12px;
                    padding: 0 5px;
                    border-radius: 3px;
                    display: inline-block;
                    overflow: hidden;
                }
            }

            .goods-price-box {
                display: flex;
                position: relative;
                flex-wrap: nowrap;
                align-items: center;
                justify-content: space-between;

                .price-origin-sale {
                    display: flex;
                    -webkit-box-align: baseline;
                    align-items: baseline;
                    display: flex;
                    flex-wrap: wrap;
                    overflow: hidden;
                    -webkit-box-flex: 1;
                    flex: 1;

                    .price-origin-icon {
                        margin: 3px 0;
                        display: flex;
                        flex-wrap: wrap;
                        -webkit-box-align: baseline;
                        align-items: baseline;
                        overflow: hidden;

                        .price-icon {
                            display: flex;
                            flex-wrap: wrap;
                            align-items: center;
                            overflow: hidden;
                            .price {
                                margin-right: 5px;
                                text-overflow: ellipsis;
                                white-space: nowrap;
                                overflow: hidden;
                                display: flex;
                                align-items: baseline;
                            }
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
</style>
