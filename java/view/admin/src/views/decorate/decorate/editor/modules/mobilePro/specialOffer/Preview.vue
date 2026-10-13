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
                        <span class="text" :style="`color: ${module.moduleColor.countdownTitleColor}`">{{
                            new Date().getTime() > Number(module.moduleTitle.dailyTime[0]) ? module.moduleTitle.countdownTitle : "距开始"
                        }}</span>
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
                        <span class="text">{{ module.moduleTitle.buttonText || "立即抢购" }}</span>
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
import { ref, computed, watchEffect } from "vue";
import { ModuleColorStyle } from "@/types/decorate/decorate.d";
import { productImage } from "@/utils/format";
import { mergeDefaultModule, formatCommonStyle, CommonCountdown } from "@/views/decorate/decorate/src/modules/index";
import { ModuleTheme } from "@/views/decorate/decorate/src/modules/editIndex";
import { getProductList } from "@/api/product/product";
import { getDemoList } from "@/views/decorate/decorate/src/modules/index";
import { specialOfferDefaults } from "./index";
import type { SpecialOfferModule } from "./types";

const props = defineProps<{ module: SpecialOfferModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, specialOfferDefaults);
// mergeDefaultModule 是浅合并，对 moduleTitle/moduleColor 等已存在的嵌套对象不会回填新增字段。
// 此处显式为本轮 CR 新增字段做兜底，确保老数据加载后 Edit 面板能拿到默认值。
if (module.moduleTitle) {
    if (module.moduleTitle.topRightType === undefined) {
        module.moduleTitle.topRightType = "countdown";
    }
    if (module.moduleTitle.buttonText === undefined) {
        module.moduleTitle.buttonText = "立即抢购";
    }
    if (module.moduleTitle.buttonLink === undefined) {
        module.moduleTitle.buttonLink = {};
    }
}
if (module.moduleColor) {
    if (module.moduleColor.buttonTextColor === undefined) {
        module.moduleColor.buttonTextColor = "#ffffff";
    }
    if (module.moduleColor.buttonGradientColorA === undefined) {
        module.moduleColor.buttonGradientColorA = "";
    }
    if (module.moduleColor.buttonGradientColorB === undefined) {
        module.moduleColor.buttonGradientColorB = "";
    }
}

const list = ref<any>([]);
const getGroupProductList = async (params: any) => {
    try {
        const result = await getProductList(params);
        list.value = [...getDemoList(module.productList), ...result.records];
    } catch (error) {
        console.error(error);
    }
};
watchEffect(() => {
    if (module.productSource) {
        const params: any = {};
        if (module.productSource === "products") {
            params.ids = module.productList.map((item) => item.productId).join(",");
            getGroupProductList(params);
        } else if (module.productGroups[0]) {
            const { productGroupId, productNum, productNumType } = module.productGroups[0];
            params.productGroupId = productGroupId;
            params.size = productNumType === "number" ? productNum : 100;
            getGroupProductList(params);
        } else {
            list.value = [];
        }
    }
});

const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});
const topRightType = computed(() => module.moduleTitle?.topRightType ?? "countdown");
const buttonGradient = computed(() => {
    const theme = ModuleTheme[(module.moduleColorStyle as ModuleColorStyle) || "orange"];
    const userA = module.moduleColor?.buttonGradientColorA;
    const userB = module.moduleColor?.buttonGradientColorB;
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
