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
            class="module-ad-content main"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding +
                `border-radius: ${module.contentStyle.boxRadius}px`
            "
        >
            <div class="main">
                <div class="mk-top">
                    <div>
                        <span class="mk-title" :style="`color: ${module.moduleColor.titleColor}`">{{ module.moduleTitle.title }}</span>
                        <span class="mk-small-title" :style="`color: ${module.moduleColor.subTitleColor}`">{{ module.moduleTitle.subTitle }}</span>
                    </div>
                    <div v-if="topRightType === 'countdown'" class="mk-countdown" :style="`--color: ${module.moduleColor.countdownColor}`">
                        <span class="text" :style="`color: ${module.moduleColor.countdownTitleColor}`">{{ new Date().getTime() > Number(module.moduleTitle.dailyTime[0]) ? module.moduleTitle.countdownTitle : $t('距开始') }}</span>
                        <div class="countdown">
                            <CommonCountdown
                                :countdownType="module.moduleTitle.countdownType"
                                :value-style="{
                                    color: module.moduleColor.countdownColor,
                                    'font-size': '12px',
                                    display: 'block',
                                    height: '20px',
                                    'line-height': '20px'
                                }"
                                format="HH:mm:ss"
                                :dailyTime="module.moduleTitle.dailyTime"
                                :specifyTime="module.moduleTitle.specifyTime"
                            ></CommonCountdown>
                        </div>
                    </div>
                    <div
                        v-else
                        class="mk-action-btn"
                        :style="`background: linear-gradient(90deg, ${buttonGradient.a} 0%, ${buttonGradient.b} 100%); color: ${module.moduleColor.buttonTextColor || '#ffffff'};`"
                    >
                        <span class="text">{{ module.moduleTitle.buttonText || $t('立即抢购') }}</span>
                        <i class="iconfont-admin icon-login-youjiantou"></i>
                    </div>
                </div>
                <div class="lists" :style="`border-radius: ${module.contentStyle.boxRadius}px`">
                    <div class="item-box">
                        <template v-for="(item, index) in list" :key="index">
                            <div :style="`width: ${module.productWidth}%; flex-wrap: nowrap; flex-shrink: 0`">
                                <div class="item">
                                    <div
                                        class="img"
                                        :style="`width: 100%;
                                    padding-top: ${picScaleMap[module.picScale!]};
                                    background-size: ${module.picFillType};
                                    border-radius: ${module.productRadius}px;
                                    background-image: url(${productImage(item)});`"
                                    ></div>
                                    <div class="goods-info" v-if="module.prodcutTitles.length > 0">
                                        <div
                                            v-if="module.showProductName && module.showProdcutTitles"
                                            class="tit"
                                            :style="`padding-bottom: 0px; font-size: ${module.productNameSize}px; font-weight: ${module.productNameWeight}; color:${module.moduleColor.productNameColor}`"
                                        >
                                            {{ module.prodcutTitles[index] && module.prodcutTitles[index][`title${index + 1}`] }}
                                        </div>
                                        <div
                                            v-if="module.showProductPrice"
                                            class="price-box"
                                            :style="`color: rgb(255, 255, 255); font-weight: 700; font-family: inherit`"
                                        >
                                            <div class="price">￥{{ item.productPrice }}</div>
                                        </div>
                                        <div
                                            v-if="module.showSellingPoints && module.showProdcutTitles"
                                            class="desc"
                                            :style="`color: ${module.moduleColor.sellingPointsColor}`"
                                        >
                                            {{ module.prodcutTitles[index] && module.prodcutTitles[index][`sellingPoints${index + 1}`] }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </template>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref, watchEffect } from "vue";
import { ModuleType, ModuleSpecialOfferType, ModuleColorStyle } from "@/types/decorate/decorate.d";
import { getDaysLaterEndTime, formatTime } from "@/utils/time";
import { productImage } from "@/utils/format";
import { mergeDefaultModule, defaultModuleStyle, defaultContentStyle, formatCommonStyle, CommonCountdown } from "@/views/decorate/decorate/src/modules/";
import { ModuleTheme } from "@/views/decorate/decorate/src/modules/editIndex";
import { getProductList } from "@/api/product/product";
import { getDemoList } from "@/views/decorate/decorate/src/modules";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & ModuleSpecialOfferType>("module") as Ref<ModuleType & ModuleSpecialOfferType>;
const pruductList = [
    {
        productId: 0,
        productSn: "",
        productName: t('零食'),
        productDesc: "",
        productPrice: 32.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17316338156QyBKObq7Wuw7KCqKa.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: t('饼干'),
        productDesc: "",
        productPrice: 32.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731633815i7FmGX6OEWOBsQ3DUh.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: t('茶叶'),
        productDesc: "",
        productPrice: 32.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731633815zoFfIRIsE4yJ0a5LJn.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: t('干货'),
        productDesc: "",
        productPrice: 32.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731633815UdOFkntVJP4GXu9KsW.jpg"
    }
];
// 模块颜色风格 orange:番茄橙,red:中国红,blue:天空蓝,green:森林绿,purple:紫罗兰,grey:深灰色,chocolate:巧克力,brown:马鞍棕
const defaultModule = ref({
    productSource: "products", // 商品来源 手动选择: products, 商品分组: productGroup
    productGroups: [], // 商品分组
    productList: pruductList || [], // 商品列表
    moduleColorStyle: "orange", // 模块颜色风格
    prodcutTitles: [], // 自定义商品标题/卖点
    showProdcutTitles: 1, // 是否显示自定义商品标题/卖点 1:显示, 0:不显示
    moduleTitle: {
        // 模块标题
        title: t('天天特价'), // 大标题
        subTitle: t('每日精选'), // 小标题
        countdownTitle: t('距结束'), // 倒计时文案
        countdownType: "dailyCycle", // 倒计时类型  dailyCycle: 每日循环 specifyTime: 指定时间
        dailyTime: [formatTime(new Date(), 8), formatTime(new Date(), 20)], // 倒计时开始和结束时间
        specifyTime: getDaysLaterEndTime(3), // 指定时间  默认值3天后
        topRightType: "countdown", // 顶部右侧展示：countdown=倒计时（默认） / button=按钮
        buttonText: t('立即抢购'), // 按钮文案（topRightType=button 时生效）
        buttonLink: {} // 按钮跳转链接（SelectLink LinkType 数据结构）
    },
    picScale: "1:1", // 图片比例  3:2, 1:1, 3:4, 16:9
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    productWidth: 25, // 商品宽度
    productRadius: 6, // 商品圆角
    showProductName: 1, // 是否显示商品名称 1:显示, 0:不显示
    productNameSize: 14, // 商品名称字体大小
    productNameWeight: 400, // 商品名称字体粗细
    showProductPrice: 1, // 是否显示商品价格 1:显示, 0:不显示
    // priceDecimal: 0, //商品价格小数位数 0:不显示  1:显示1位 2:显示2位
    priceWeight: 400, // 商品价格字体粗细
    showSellingPoints: 1, // 是否显示商品卖点 1:显示, 0:不显示
    // soldOutIcon: 0, //缺货图标 1:显示 0:不显示

    moduleColor: {
        // 模块颜色设置
        titleColor: "#ffffff", // 模块大标题颜色
        subTitleColor: "#ffffff", // 模块小标题颜色
        countdownColor: "#ffffff", // 倒计时颜色
        countdownTitleColor: "#72B76C", // 倒计时文案颜色
        buttonTextColor: "#ffffff", // 按钮文字颜色
        buttonGradientColorA: "", // 按钮渐变起始色（留空 → 跟随主题色）
        buttonGradientColorB: "", // 按钮渐变结束色（留空 → 跟随主题色）

        productNameColor: "#555555", // 商品名称颜色
        sellingPointsColor: "#FB6D08" // 商品卖点颜色
    },

    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
});
mergeDefaultModule(module.value, defaultModule.value);
// mergeDefaultModule 是浅合并，对 moduleTitle/moduleColor 等已存在的嵌套对象不会回填新增字段。
// 此处显式为本轮 CR 新增字段做兜底，确保老数据加载后 Edit 面板能拿到默认值。
if (module.value.moduleTitle) {
    if (module.value.moduleTitle.topRightType === undefined) {
        module.value.moduleTitle.topRightType = "countdown";
    }
    if (module.value.moduleTitle.buttonText === undefined) {
        module.value.moduleTitle.buttonText = t('立即抢购');
    }
    if (module.value.moduleTitle.buttonLink === undefined) {
        module.value.moduleTitle.buttonLink = {};
    }
}
if (module.value.moduleColor) {
    if (module.value.moduleColor.buttonTextColor === undefined) {
        module.value.moduleColor.buttonTextColor = "#ffffff";
    }
    if (module.value.moduleColor.buttonGradientColorA === undefined) {
        module.value.moduleColor.buttonGradientColorA = "";
    }
    if (module.value.moduleColor.buttonGradientColorB === undefined) {
        module.value.moduleColor.buttonGradientColorB = "";
    }
}

const list = ref<any>([]);
const getGroupProductList = async (params: any) => {
    try {
        const result = await getProductList(params);
        list.value = [...getDemoList(module.value.productList), ...result.records];
    } catch (error) {
        console.error(error);
    }
};
watchEffect(() => {
    if (module.value.productSource) {
        const params: any = {};
        if (module.value.productSource === "products") {
            params.ids = module.value.productList.map((item) => item.productId).join(",");
            getGroupProductList(params);
        } else if (module.value.productGroups[0]) {
            const { productGroupId, productNum, productNumType } = module.value.productGroups[0];
            params.productGroupId = productGroupId;
            params.size = productNumType === "number" ? productNum : 100;
            getGroupProductList(params);
        } else {
            list.value = [];
        }
    }
});

const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
});
const topRightType = computed(() => module.value.moduleTitle?.topRightType ?? "countdown");
const buttonGradient = computed(() => {
    const theme = ModuleTheme[(module.value.moduleColorStyle as ModuleColorStyle) || "orange"];
    const userA = module.value.moduleColor?.buttonGradientColorA;
    const userB = module.value.moduleColor?.buttonGradientColorB;
    return {
        a: userA || theme?.gradientColorA || "#ff3e02",
        b: userB || theme?.gradientColorB || "#ff7b19"
    };
});
const picScaleMap: { [key: string]: string } = {
    "3:2": "calc(100% / 3 * 2)",
    "1:1": "calc(100% / 1 * 1)",
    "3:4": "calc(100% / 3 * 4)",
    "16:9": "calc(100% / 16 * 9)"
};
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
    position: relative;
    padding: 10px;
    overflow: hidden;
}

.mk-top {
    margin-bottom: 2px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    overflow: hidden;
    .mk-title {
        margin-right: 5px;
        height: 30px;
        line-height: 30px;
        font-size: 18px;
        font-weight: 700;
        color: #333333;
    }

    .mk-small-title {
        height: 30px;
        line-height: 35px;
        font-size: 14px;
        font-weight: 400;
        color: #333333;
    }

    .mk-countdown {
        --color: #f44;
        display: flex;
        justify-content: center;
        flex-direction: row;
        align-items: center;
        overflow: hidden;
        font-size: 12px;
        height: 20px;
        border-radius: 10px;

        .text {
            background-color: var(--color);
            color: #fff;
            padding: 0 4px 0 8px;
            height: 20px;
            line-height: 20px;
            border-top-left-radius: 10px;
            border-bottom-left-radius: 10px;
        }

        .countdown {
            box-sizing: border-box;
            height: 20px;
            border: solid 1px var(--color);
            font-size: 12px;
            background: transparent;
            padding: 0 10px 0 7px;
            color: var(--color);
            border-top-right-radius: 10px;
            border-bottom-right-radius: 10px;
            display: flex;
            align-items: center;
        }
    }

    .mk-action-btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        height: 24px;
        padding: 0 10px 0 12px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: 500;
        line-height: 1;
        white-space: nowrap;
        cursor: pointer;

        .text {
            margin-right: 2px;
        }

        .iconfont-admin {
            font-size: 12px;
            line-height: 1;
        }
    }
}
.item-box {
    display: flex;
    flex-wrap: nowrap;
    overflow-x: auto;
    margin: 8px 4px 5px;
    &::-webkit-scrollbar {
        display: none;
        height: 0;
    }
}
.lists {
    background: #fff;
    border-radius: 6px;
    overflow: hidden;
}
.item {
    display: block;
    margin: 0 3px;
    text-align: center;
    overflow: hidden;
    .img {
        position: relative;
        height: 0;
        background-repeat: no-repeat;
        background-position: center;
        background-size: cover;
        overflow: hidden;
    }
    .goods-info {
        padding: 5px 2px 3px;
        padding: 0 5px;
        flex-grow: 1;
        flex-shrink: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        font-size: 12px;

        .tit {
            height: 20px;
            line-height: 20px;
            font-size: 16px;
            font-weight: 700;
            white-space: nowrap;
        }

        .price-box {
            margin: 2px auto 0;
            width: 100%;
            max-width: 80px;
            height: 22.5px;
            line-height: 22.5px;
            background: url(https://oss.tigshop.com/img/gallery/202504/1743571805Kri1ocOOKkgrcXAbKe.png) no-repeat center;
            background-size: contain;
            .price {
                height: 100%;
                padding-left: 20px;
                display: flex;
                justify-content: center;
                align-items: center;
            }
        }

        .desc {
            height: 16px;
            line-height: 18px;
            margin: 3px 0 0;
            font-size: 12px;
            overflow: hidden;
            white-space: nowrap;
            color: #fb6d08;
        }
    }
}
</style>
