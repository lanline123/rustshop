<template>
    <div
        class="module-ad-con"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage
        "
    >
        <div class="main" ref="mainRef">
            <template v-for="(group, index) in module.comboGroups" :key="group">
                <div class="list" :style="listStyle + `${index > 1 ? 'margin-top: 10px;' : ''}`">
                    <div style="padding: 6px">
                        <div class="top">
                            <div class="top-tit">
                                <template v-if="group.titleImage.picUrl">
                                    <img class="img" :src="imageFormat(group.titleImage.picUrl)" />
                                </template>
                                <template v-else>
                                    <div class="big-title" :style="`color:${module.groupTitleColor}`">{{ group.groupTitle }}</div>
                                </template>
                                <div class="title" :style="`background:${group.groupColor}`">{{ group.groupSubTitle }}</div>
                            </div>
                            <div class="desc" :style="`color: ${group.groupColor}`">{{ group.groupDesc }}</div>
                        </div>
                        <div :style="`border-radius: ${module.groupRadius}px; overflow: hidden; position: relative; height: ${height}px;`">
                            <template v-if="group.groupType === 'image'">
                                <Swiper :swiperOption="swiperOption" v-model="group.picList" @activeIndexChange="getIndex">
                                    <template v-slot:default="{ item }">
                                        <div class="pic-box">
                                            <div class="pic-img" :style="` height: ${height}px;background-image: url(${imageFormat(item.picUrl)});`"></div>
                                            <div class="tit" :style="`color: ${module.bannerTittleColor};`">{{ item.picTitle }}</div>
                                        </div>
                                    </template>
                                </Swiper>
                                <div :style="`position: absolute; right: 10px; bottom: 5px; line-height: 18px; color:${module.bannerDotColor}; z-index: 9`">
                                    {{ `${currentIndex + 1}/${group.picList?.length}` }}
                                </div>
                            </template>
                            <template v-if="group.groupType === 'product'">
                                <Swiper
                                    :swiperOption="{ ...swiperOption, slidesPerView: 2, spaceBetween: 6 }"
                                    v-model="productGroupsList[index]"
                                    @activeIndexChange=""
                                >
                                    <template v-slot:default="{ item }">
                                        <div class="item">
                                            <div
                                                class="goods-img"
                                                :style="`width: 100%;
                                                            padding-top: 100%;
                                                            background-size: cover;
                                                            border-radius: ${module.imageRadius}px;
                                                            background-image: url(${productImage(item)});`"
                                            ></div>
                                            <div class="goods-price">
                                                <span
                                                    class="price-text"
                                                    :style="`color: ${module.regularPriceColor}; background: ${module.regularPriceBgColor || 'transparent'}; border-radius: ${module.regularPriceRadius}px; font-size: ${module.regularPriceSize}px;`"
                                                    >￥{{ item.productPrice }}</span
                                                >
                                            </div>
                                        </div>
                                    </template>
                                </Swiper>
                            </template>
                        </div>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref, watchEffect, onMounted } from "vue";
import { ModuleType, ModuleComboSuggestionsEditType,GradientType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultModuleStyle, formatCommonStyle, gradientMap } from "@/views/decorate/decorate/src/modules/";
import { Swiper } from "@/components/swiper";
import { imageFormat, productImage } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import { getDemoList } from "@/views/decorate/decorate/src/modules";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & ModuleComboSuggestionsEditType>("module") as Ref<ModuleType & ModuleComboSuggestionsEditType>;

const comboGroups = [
    {
        titleImage: {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731485744JPyV2kPmhJjwi9b1OD.png",
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485744JPyV2kPmhJjwi9b1OD.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picId: 1429,
            picName: "FpMGh7lhbRC26udZs5OVGsD1MQkm"
        },
        groupTitle: t('限时特惠'),
        groupSubTitle: t('心动福利'),
        groupDesc: t('限时！西冷牛排降20.1元'),
        groupColor: "rgba(218, 79, 51, 1)",
        groupType: "image",
        picList: [
            {
                picId: 1428,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485744Z36HLJIkKupT78pAPB.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202411/1731485744Z36HLJIkKupT78pAPB.jpg",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: t('禽蛋抢先购')
            },
            {
                picId: 1427,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485744G6SecTiZ2YnnqTOrzO.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202411/1731485744G6SecTiZ2YnnqTOrzO.jpg",
                picName: "Fk9ihEJjXDwwG174KZcjuaQeyGus",
                picTitle: t('清洁好帮手')
            },
            {
                picId: 1426,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485744OYBNIbPBqdFUwc8YMO.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202411/1731485744OYBNIbPBqdFUwc8YMO.jpg",
                picName: "FlX7c5fV9sbgl9QZ0i0z1K1DK6DB",
                picTitle: t('精选好食材')
            }
        ],
        pruductList: [],
        moreLink: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        }
    },
    {
        titleImage: {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/17314857564qYuFM87aXbimtojrK.png",
            picThumb: "https://oss.tigshop.com/img/gallery/202411/17314857564qYuFM87aXbimtojrK.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picId: 1434,
            picName: "FqaBzM4nqSLzx4ydDgi3SfbS5R3g"
        },
        groupTitle: t('生鲜惠选'),
        groupSubTitle: t('榜单推荐'),
        groupDesc: t('猫山王榴莲冰皮月饼香甜爆浆'),
        groupColor: "rgba(177, 123, 57, 1)",
        groupType: "product",
        picList: [],
        pruductList: [
            {
                productId: 0,
                productSn: "",
                productName: t('刺身拼盘'),
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485756wtyOOnqoeEn6W2XfNP.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('冰鲜剥皮鱼'),
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485756sGPXRzI7o84S5HAPo5.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('北极甜虾'),
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485756JsgKabEE4BO09iRqnc.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('秋刀鱼'),
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485756ivBG9CydBhi21do1xk.jpg"
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
        titleImage: {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731485768izrm5sZYVmQOne5yYf.png",
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485768izrm5sZYVmQOne5yYf.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picId: 1439,
            picName: "FrEc7JTpoNjpEJR8HlE4Klv6trKM"
        },
        groupTitle: t('全球好物'),
        groupSubTitle: t('大牌严选'),
        groupDesc: t('七夕礼~欧舒丹明细沐浴礼盒'),
        groupColor: "rgba(36, 78, 179, 1)",
        groupType: "product",
        picList: [],
        pruductList: [
            {
                productId: 0,
                productSn: "",
                productName: t('牙膏'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485768ECo8ixzZvZVOLDsJf3.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('卫生巾'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485768PFd7qRdzskII6k0ZTU.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('卫生巾'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485768yn1Oq5lqvNyx7N8gbE.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('沐浴露'),
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485768bH293zqhEdmoNbO7VQ.jpg"
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
        titleImage: {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731485780Z15LILaEOLfudumi3H.png",
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485780Z15LILaEOLfudumi3H.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picId: 1441,
            picName: "FlF_0eUIigJ19i2n6VOTIhZuMn0G"
        },
        groupTitle: t('逛出新奇'),
        groupSubTitle: t('上新日历'),
        groupDesc: t('颗颗饱满！甜！巨峰葡萄~'),
        groupColor: "rgba(99, 49, 119, 1)",
        groupType: "product",
        picList: [],
        pruductList: [
            {
                productId: 0,
                productSn: "",
                productName: t('摆件'),
                productDesc: "",
                productPrice: 55.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485780O9GnV2rCLcOodydJAb.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('桌面垃圾桶'),
                productDesc: "",
                productPrice: 25.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/17314857806ABCknloual53A5XH0.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('垃圾袋'),
                productDesc: "",
                productPrice: 15.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485780y9OjupFgDDLmI31GhJ.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: t('脸盆'),
                productDesc: "",
                productPrice: 25.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485780Okip8X3FFefKxhz8OG.jpg"
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
    comboGroups: comboGroups,
    regularPriceColor: "#e84a40", //常规价格颜色
    regularPriceBgColor: "", //常规价格背景颜色（留空时无背景）
    regularPriceRadius: 24, //常规价格圆角半径（胶囊）
    regularPriceSize: 14, //常规价格文字大小
    saleTagBackgroundColorA: "#f8d7a0", //活动标签背景颜色a
    saleTagBackgroundColorB: "#ffffff", //活动标签背景颜色b
    saleTagFontColor: "#83441c", //活动标签文字颜色
    salePriceBackgroundColor: "#e73a68", //活动价格背景颜色
    salePriceFontColor: "#ffffff", //活动价格文字颜色
    salePriceDecimal: 0, //活动价格小数位数 0:不显示  1:显示1位 2:显示2位
    imageRadius: 6, //商品图或图片圆角半径
    soldOutIcon: 1, //缺货图标 1:显示 0:不显示

    backgroundGradientType: "upDown", //背景方向
    gradientColorA: "#ffeadf", //背景渐变色A
    gradientColorB: "#ffffff", //背景渐变色B
    groupTitleColor: "#000000", //分组大标题颜色
    bannerTittleColor: "#ffffff", //banner标题颜色
    bannerDotColor: "#ffffff", //banner指示数颜色
    groupRadius: 10, //内容圆角
    groupShadow: 0, //内容投影 1:显示 0:不显示

    moduleStyle: defaultModuleStyle, //模块样式
    carouselDirection: "horizontal", //轮播方向  横向 horizontal  纵向 vertical
    autoPlay: 1, //自动轮播 1:开启 0:关闭
    interval: 5 //轮播间隔
});
mergeDefaultModule(module.value, defaultModule.value);
const swiperOption = ref<any>({
    autoplay: {
        delay: module.value.interval * 1000,
        disableOnInteraction: false, //用户操作swiper之后，是否禁止autoplay
        pauseOnMouseEnter: true //鼠标置于swiper是否时暂停自动切换
    },
    pagination: false
});
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
});

const listStyle = computed(() => {
    let str = `border-radius: ${module.value.groupRadius}px;background-image: linear-gradient(${gradientMap[module.value.backgroundGradientType as GradientType]},${module.value.gradientColorA} -50%, ${module.value.gradientColorB});`;
    if (module.value.groupShadow === 1) {
        str += `box-shadow: rgba(0, 0, 0, 0.06) 0px 2px 4px;`;
    }
    return str;
});

const height = ref(0);
const getHeight = () => {
    if (mainRef.value && mainRef.value.clientWidth > 0) {
        height.value = (mainRef.value.clientWidth / 2 - 4 - 12) * 0.63;
    }
};
const mainRef = ref();

const currentIndex = ref(0);
const getIndex = (e: any) => {
    currentIndex.value = e.realIndex;
};

const productGroupsList = ref<any>({});
watchEffect(() => {
    if (module.value.comboGroups) {
        module.value.comboGroups.forEach(async (group: any, index: number) => {
            if (group.groupType === "product") {
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

watchEffect(() => {
    if (module.value.autoPlay == 1) {
        swiperOption.value.autoplay = {
            delay: module.value.interval * 1000,
            disableOnInteraction: false, //用户操作swiper之后，是否禁止autoplay
            pauseOnMouseEnter: true //鼠标置于swiper是否时暂停自动切换
        };
    } else {
        swiperOption.value.autoplay = false;
    }

    if (module.value.moduleStyle?.boxPadding) {
        getHeight();
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
.main {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    .list {
        width: calc(50% - 4px);
        flex-shrink: 0;
        overflow: hidden;
        font-size: 12px;
        .top {
            display: block;
            margin-bottom: 5px;

            .top-tit {
                height: 18px;
                display: flex;
                align-items: center;
                .big-title {
                    font-size: 15px;
                    font-weight: 700;
                    margin-right: 5px;
                }
                .img {
                    height: 15px;
                    display: block;
                    margin-right: 5px;
                }
                .title {
                    height: 18px;
                    line-height: 18px;
                    padding: 0 6px;
                    border-radius: 5px;
                    font-size: 11px;
                    color: #fff;
                }
            }
            .desc {
                margin-top: 3px;
                height: 18px;
                line-height: 18px;
                overflow: hidden;
            }
        }

        .pic-box {
            position: relative;

            .pic-img {
                display: block;
                position: relative;
                background-size: cover;
                background-position: center;
                background-repeat: no-repeat;
                width: 100%;
            }
            .tit {
                height: 18px;
                line-height: 18px;
                color: #fff;
                overflow: hidden;
                position: absolute;
                left: 10px;
                bottom: 5px;
            }
        }
        .item {
            .goods-img {
                position: relative;
                width: 100%;
                height: 0;
                background-repeat: no-repeat;
                background-position: center;
                overflow: hidden;
                flex-shrink: 0;
            }
            .goods-price {
                position: relative;
                flex-wrap: nowrap;
                margin-top: 5px;
                display: flex;
                align-items: center;
                justify-content: center;
                color: #e84a40;
                font-family: "font1";
                overflow: hidden;
                .price-text {
                    display: inline-block;
                    padding: 0 8px;
                    line-height: 1.4;
                    max-width: 100%;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                }
            }
        }
    }
}
</style>
