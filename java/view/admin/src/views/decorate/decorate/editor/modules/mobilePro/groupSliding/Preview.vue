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
                                                        v-if="module.buyBtnStyle.showBtn"
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
import { ref, computed, watchEffect } from "vue";
import { mergeDefaultModule, formatCommonStyle, CommonBuyButton, getDemoList } from "@/views/decorate/decorate/src/modules/index";
import { imageFormat, productImage } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import { groupSlidingDefaults } from "./index";
import type { GroupSlidingModule } from "./types";

const rankingImgs = [
    "https://oss.tigshop.com/img/gallery/202411/1732094008g4ZMmuQbNcBlYrNCuz.png",
    "https://oss.tigshop.com/img/gallery/202411/1732094013OJOLUek96BizcTvwmU.png",
    "https://oss.tigshop.com/img/gallery/202411/17320940168dzp9YD5LbpcMMjqe9.png"
];
const props = defineProps<{ module: GroupSlidingModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, groupSlidingDefaults);
const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});

const formatLabel = (label: string) => {
    return label.split("\n");
};

const productGroupsList = ref<any>({});
watchEffect(() => {
    if (module.groupList && module.groupList.length > 0) {
        module.groupList.forEach(async (group: any, index: number) => {
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
