<template>
    <view
        class="module-ad-con"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage
        "
    >
        <view class="main">
            <template v-for="(group, index) in module.comboGroups" :key="index">
                <view class="list" :style="listStyle + `${Number(index) > 1 ? 'margin-top: 10px;' : ''}`">
                    <view style="padding: 6px">
                        <view class="top" @click="urlFormat(group.moreLink) && redirect({ url: urlFormat(group.moreLink) })">
                            <view class="top-tit">
                                <template v-if="group.titleImage?.picUrl">
                                    <image class="img" :src="imageFormat(group.titleImage.picUrl)" mode="heightFix" />
                                </template>
                                <template v-else>
                                    <view class="big-title line1" :style="`color:${module.groupTitleColor}`">{{ $t(group.groupTitle) }}</view>
                                </template>
                                <view class="title line1" :style="`background:${group.groupColor}`">{{ $t(group.groupSubTitle) }}</view>
                            </view>
                            <view class="desc" :style="`color: ${group.groupColor}`">{{ $t(group.groupDesc) }}</view>
                        </view>
                        <view :style="`border-radius: ${module.groupRadius}px; overflow: hidden; position: relative; height: ${height}px;`">
                            <template v-if="group.groupType === 'image'">
                                <tig-swiper
                                    v-model="group.picList"
                                    :height="height + 'px'"
                                    :dot="false"
                                    :interval="interval"
                                    :autoplay="autoplay"
                                    @change="swiperChange"
                                >
                                    <template #default="{ item }">
                                        <view class="pic-box" @click="urlFormat(item.picLink) && redirect({ url: urlFormat(item.picLink) })">
                                            <view class="pic-img" :style="` height: ${height}px;background-image: url(${productImage(item)});`" />
                                            <view class="tit" :style="`color: ${module.bannerTittleColor};`">{{ $t(item.picTitle ?? "") }}</view>
                                        </view>
                                    </template>
                                </tig-swiper>
                                <view :style="`position: absolute; right: 10px; bottom: 5px; line-height: 18px; color:${module.bannerDotColor}; z-index: 9`">
                                    {{ `${currentIndex + 1}/${group.picList?.length}` }}
                                </view>
                            </template>
                            <template v-if="group.groupType === 'product'">
                                <tig-swiper
                                    v-model="productGroupsList[index]"
                                    :interval="interval"
                                    :autoplay="autoplay"
                                    :height="height + 'px'"
                                    :dot="false"
                                    :display-multiple-items="2"
                                >
                                    <template #default="{ item }">
                                        <view
                                            class="item"
                                            @click="item.productId && redirect({ url: urlFormat({ path: 'product', data: { id: item.productId } }) })"
                                        >
                                            <view
                                                class="goods-img"
                                                :style="`width: 100%;
                                                                padding-top: 100%;
                                                                background-size: cover;
                                                                border-radius: ${module.imageRadius}px;
                                                                background-image: url(${productImage(item)});`"
                                            />
                                            <view class="goods-price">
                                                <view
                                                    class="price-text"
                                                    :style="`color: ${module.regularPriceColor}; background: ${module.regularPriceBgColor || 'transparent'}; border-radius: ${module.regularPriceRadius || 0}px; font-size: ${module.regularPriceSize || 14}px;`"
                                                >
                                                    <format-price :price-data="item.productPrice" />
                                                </view>
                                            </view>
                                        </view>
                                    </template>
                                </tig-swiper>
                            </template>
                        </view>
                    </view>
                </view>
            </template>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, onMounted, onUnmounted, ref } from "vue";
import { formatCommonStyle, gradientMap } from "@/components/modules";
import { urlFormat, imageFormat, productImage } from "@/utils/format";
import { redirect, getElementRect } from "@/utils";
import { getProductsList } from "@/api/common";

const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});

const commonStyle = computed(() => {
    return formatCommonStyle(
        props.module.moduleStyle ?? {
            boxMarginTop: 5,
            boxMarginBottom: 0,
            boxMargin: 10,
            backgroundColor: "",
            backgroundPic: {
                picUrl: "",
                picThumb: ""
            },
            backgroundPicFillType: "cover"
        },
        props.module.contentStyle ?? {
            paddingTop: 0,
            paddingBottom: 0,
            paddingLeftRight: 0,
            padding: 0,
            boxRadiusTop: 0,
            boxRadiusBottom: 0,
            gradientType: "upDown",
            gradientColorA: "#ffffff",
            gradientColorB: "#ffffff",
            backgroundPic: {
                picUrl: "",
                picThumb: ""
            },
            backgroundPicFillType: "cover",
            boxShadow: 0
        },
        props.module.subTitle ?? {}
    );
});

const listStyle = computed(() => {
    let str = `border-radius: ${props.module.groupRadius}px;background-image: linear-gradient(${gradientMap[props.module.backgroundGradientType]},${
        props.module?.gradientColorA
    } -50%, ${props.module.gradientColorB});`;
    if (props.module.groupShadow == 1) {
        str += `box-shadow: rgba(0, 0, 0, 0.06) 0px 2px 4px;`;
    }
    return str;
});

const interval = computed(() => {
    return props.module.interval ? props.module.interval * 1000 : 3000;
});

const autoplay = computed(() => {
    return props.module.autoPlay == 1;
});

const currentIndex = ref(0);
const swiperChange = (e: any) => {
    currentIndex.value = e;
};
const height = ref(0);
const instance = getCurrentInstance();
const getHeight = async () => {
    try {
        const res = await getElementRect(".main", instance);
        if (res && res.width) {
            height.value = (res.width / 2 - 4 - 12) * 0.63;
        }
    } catch (error) {
        console.error(error);
    }
};

const productGroupsList = ref<any>({});
const getProductGroupsList = () => {
    props.module.comboGroups.forEach(async (group: any, index: number) => {
        if (group.groupType === "product") {
            const ids = group.pruductList.map((item: any) => item.productId);
            try {
                const result = await getProductsList({ ids: ids.join(","), size: ids.length });
                productGroupsList.value[index] = result.records;
            } catch (error) {
                console.error(error);
            }
        }
    });
};
getProductGroupsList();

onMounted(() => {
    getHeight();
});
const windowResizeCallback = () => {
    getHeight();
};
uni.onWindowResize(windowResizeCallback);
onUnmounted(() => {
    uni.offWindowResize(windowResizeCallback);
});
</script>

<style lang="scss" scoped>
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
            margin-right: 6px;
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
