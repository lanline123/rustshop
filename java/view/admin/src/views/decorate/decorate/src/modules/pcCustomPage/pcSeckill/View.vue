<template>
    <div class="module-seckill-goods_ad-wrapper">
        <div
            :style="
                allFormat.boxPadding +
                allFormat.boxPaddingTop +
                allFormat.boxPaddingBottom +
                allFormat.innerPadding +
                allFormat.boxRadius +
                ' ' +
                frameFormat.backgroundColor +
                allFormat.backgroundColor +
                ' ' +
                frameFormat.boxRadius +
                allFormat.boxRadius
            "
            style="position: relative"
            :class="
                'module-ad-con module-goods_ad module-seckill-goods_ad ad-style__' +
                module?.style +
                ' ad-buy_btn_style__' +
                module?.buyBtnStyle +
                ' ad-goods_style__' +
                module?.goodsStyle +
                ' ad-goods_radio_style__' +
                module?.goodsRadioStyle +
                ' ad-text_align__' +
                module?.textAlign +
                ' ad-text_weight__' +
                module?.textWeight +
                ' ad-goods_name_row__' +
                module?.goodsNameRow +
                ' ad-goods_name_padding__' +
                module?.goodsNamePadding +
                ' ad-goods-title_style__' +
                module?.title?.titleStyle
            "
        >
            <div class="module-ad-content">
                <!-- 占位显示：没有商品时 -->
                <div v-if="!seckillList || seckillList.length === 0" class="module-ad-empty empty-seckill">
                    <div class="image-empty-bg"></div>
                    <div class="desc">{{ $t('点击选择秒杀活动') }}</div>
                </div>
                <!-- 有商品时显示商品列表 -->
                <div v-else-if="seckillList.length > 0" class="mod_seckill">
                    <!-- 标题和倒计时一起显示隐藏 -->
                    <div
                        class="seckill-top"
                        v-if="module.title?.showTitle == 1"
                        :style="{
                            background: module.title?.titleBackground2
                                ? 'linear-gradient(to right, ' + (module.title?.titleBackground || '') + ', ' + module.title.titleBackground2 + ')'
                                : module.title?.titleBackground || '',
                            borderTopLeftRadius: module.title?.titleRadius ? module.title.titleRadius + 'px' : '',
                            borderTopRightRadius: module.title?.titleRadius ? module.title.titleRadius + 'px' : ''
                        }"
                    >
                        <div class="seckill-title-wrapper">
                            <div class="line" v-if="module.title?.titleStyle == 3"></div>
                            <el-image
                                v-if="module.title?.titleBackgroundPic?.picUrl"
                                class="image-size"
                                :src="imageFormat(module.title.titleBackgroundPic.picUrl)"
                            ></el-image>
                            <div class="seckill-title" :style="{ color: module.title?.titleColor || '#1a1a1a' }">
                                {{ module.title?.titleText ? module.title.titleText : $t('限时秒杀') }}
                            </div>
                            <div
                                class="seckill-desc"
                                v-if="module.title?.descText"
                                :style="{ color: module.title?.descColor || '#1a1a1a' }"
                                :class="[module.title?.titleStyle == 2 ? 'big-sm' : 'sm']"
                            >
                                {{ module.title.descText }}
                            </div>
                        </div>
                        <div class="seckill-top-right">
                            <div class="seckill-count-down" v-if="getTime > 0">
                                <div to="seckill/list/">
                                    <CountDown countdown-type="specifyTime" :specify-time="getTime">
                                        <template #default="{ days, hours, minutes, seconds }">
                                            <div class="countdown-box">
                                                <span class="countdown-text">{{ $t('距结束') }}: </span>
                                                <template v-if="days && Number(days) > 0">
                                                    <span class="countdown-item" :class="{ days: String(days).length > 2 }">{{ days }}</span>
                                                    <span class="countdown-separator-text">{{ $t('天') }}</span>
                                                </template>
                                                <span class="countdown-item">{{ hours }}</span>
                                                <span class="countdown-separator">:</span>
                                                <span class="countdown-item">{{ minutes }}</span>
                                                <span class="countdown-separator">:</span>
                                                <span class="countdown-item">{{ seconds }}</span>
                                                <span class="countdown-arrow" v-if="module.title?.showMore != 1">
                                                    <i class="iconfont-admin icon-login-youjiantou"></i>
                                                </span>
                                            </div>
                                        </template>
                                    </CountDown>
                                </div>
                            </div>
                            <div v-if="module.title?.showMore == 1" :style="{ color: module.title?.moreColor || '#1a1a1a' }" class="seckill-more-link">
                                {{ $t('查看更多') }}
                                <i class="iconfont-admin icon-login-youjiantou"></i>
                            </div>
                        </div>
                    </div>
                    <div class="seckill_list_wrap">
                        <!-- 使用 swiper 横向滑动布局（style 6） -->
                        <div v-if="seckillList.length > 0 && useSwiperLayout" class="seckill_list">
                            <Swiper v-model:swiperOption="seckillProductWrap" v-model="seckillList" style="width: 100%">
                                <template v-slot:default="{ item }">
                                    <div class="proditem goods-ad-item" style="height: 100%">
                                        <div :title="item.productName" class="">
                                            <div class="seckill-item-image-wrapper">
                                                <div class="seckill-item-image">
                                                    <el-image
                                                        :src="productImage(item)"
                                                        class="pro_pic"
                                                        loading="lazy"
                                                        style="transition: opacity 0.2s linear"
                                                    />
                                                </div>
                                                <div class="seckill-item-discount" v-if="(item as any)?.marketPrice">
                                                    {{ $t('优惠') }}{{ getDiscount(Number(item.productPrice), Number((item as any).marketPrice)) }}%
                                                </div>
                                            </div>
                                            <template v-if="module?.showName == 1">
                                                <div>
                                                    <div
                                                        class="seckill-item__name"
                                                        :style="{
                                                            textAlign: module?.textAlign == 1 ? 'left' : 'center',
                                                            fontWeight: module?.textWeight == 1 ? 'normal' : 'bold',
                                                            padding: module.goodsNamePadding == 1 ? '0 10px' : ''
                                                        }"
                                                    >
                                                        {{ item.productName }}
                                                    </div>
                                                </div>
                                            </template>
                                        </div>
                                        <template v-if="module?.showPrice == 1">
                                            <div
                                                class="seckill-item__price"
                                                :style="{
                                                    flexDirection: module?.textAlign == 1 ? 'row' : 'column',
                                                    padding: module.goodsNamePadding == 1 ? '0 10px' : ''
                                                }"
                                            >
                                                <div style="display: flex; flex-direction: column; align-items: flex-start">
                                                    <Price v-model="item.productPrice" class="price price-miaosha" />
                                                    <template v-if="(item as any)?.marketPrice && Number((item as any).marketPrice) > 0">
                                                        <Price v-model="(item as any).marketPrice" class="price price-origin" />
                                                    </template>
                                                </div>
                                                <div class="buy" v-if="module?.buyBtnStyle > 0">
                                                    <div v-if="(item as any)?.seckkillData?.seckillItem && (item as any).seckkillData.seckillItem.length > 1">
                                                        <div @click.stop>
                                                            <ProductButton :buyBtnStyle="module.buyBtnStyle"></ProductButton>
                                                        </div>
                                                    </div>
                                                    <div v-else>
                                                        <ProductButton :buyBtnStyle="module.buyBtnStyle"></ProductButton>
                                                    </div>
                                                </div>
                                                <div class="buy" v-else>
                                                    <div v-if="(item as any)?.seckkillData?.seckillItem && (item as any).seckkillData.seckillItem.length > 1">
                                                        <div @click.stop>
                                                            <div class="view_detail">
                                                                <i class="iconfont-pc icon-tianjia"></i>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div v-else>
                                                        <div class="view_detail">
                                                            <i class="iconfont-pc icon-tianjia"></i>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </template>
                                    </div>
                                </template>
                            </Swiper>
                        </div>
                        <!-- Flex 布局（style 1, 2） -->
                        <div v-else-if="seckillList.length > 0" class="goods-ad-warp">
                            <div
                                class="goods-ad-con"
                                :style="{
                                    // gap: module?.goodsPadding ? module.goodsPadding + 'px' : '0', // 商品中间间隔为边距的两倍
                                    gap: module?.goodsPadding ? module.goodsPadding * 2 + 'px' : '0'
                                    // paddingLeft: module?.goodsPadding ? module.goodsPadding + 'px' : '0',
                                    // paddingRight: module?.goodsPadding ? module.goodsPadding + 'px' : '0',
                                    // paddingTop: module?.goodsPadding ? module.goodsPadding + 'px' : '0',
                                    // paddingBottom: module?.goodsPadding ? module.goodsPadding + 'px' : '0'
                                }"
                            >
                                <div
                                    v-for="item in seckillList"
                                    :key="item.productId"
                                    class="proditem goods-ad-item"
                                    :style="{
                                        width: getItemWidth
                                    }"
                                >
                                    <div class="item-content">
                                        <div class="item-con">
                                            <div class="seckill-item-image-wrapper">
                                                <div class="item-photo seckill-item-image">
                                                    <div class="item-image-a">
                                                        <el-image
                                                            :src="productImage(item)"
                                                            fit="fill"
                                                            class="item-image pro_pic"
                                                        />
                                                    </div>
                                                </div>
                                                <div class="seckill-item-discount" v-if="(item as any)?.marketPrice">
                                                    {{ $t('优惠') }}{{ getDiscount(Number(item.productPrice), Number((item as any).marketPrice)) }}%
                                                </div>
                                            </div>
                                            <div class="item-info" :style="{ padding: module.goodsNamePadding == 1 ? '0 10px' : '' }">
                                                <template v-if="module?.showName == 1">
                                                    <div class="item-name seckill-item__name">
                                                        <div class="item-name-a">
                                                            {{ item.productName }}
                                                        </div>
                                                    </div>
                                                </template>
                                                <template v-if="module?.showPrice == 1">
                                                    <div class="item-action seckill-item__price">
                                                        <div class="item-price">
                                                            <Price v-model="item.productPrice" class="price price-miaosha" />
                                                        </div>
                                                        <div v-if="module?.buyBtnStyle > 0" class="item-buy">
                                                            <div
                                                                v-if="
                                                                    (item as any)?.seckkillData?.seckillItem &&
                                                                    (item as any).seckkillData.seckillItem.length > 1
                                                                "
                                                            >
                                                                <div>
                                                                    <ProductButton :buyBtnStyle="module.buyBtnStyle"></ProductButton>
                                                                </div>
                                                            </div>
                                                            <template v-else>
                                                                <ProductButton :buyBtnStyle="module.buyBtnStyle"></ProductButton>
                                                            </template>
                                                        </div>
                                                        <div v-else class="item-buy buy">
                                                            <div
                                                                v-if="
                                                                    (item as any)?.seckkillData?.seckillItem &&
                                                                    (item as any).seckkillData.seckillItem.length > 1
                                                                "
                                                            >
                                                                <div @click.stop>
                                                                    <div class="view_detail">
                                                                        <i class="iconfont-pc icon-tianjia"></i>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <div v-else>
                                                                <div class="view_detail">
                                                                    <i class="iconfont-pc icon-tianjia"></i>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <template v-if="(item as any)?.marketPrice && Number((item as any).marketPrice) > 0">
                                                        <Price v-model="(item as any).marketPrice" class="price price-origin" />
                                                    </template>
                                                </template>
                                            </div>
                                        </div>
                                    </div>
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
import { ref, computed, onMounted, Ref } from "vue";
import { getMobileSeckillList } from "@/api/decorate/mobileSeckill";
import { CountDown, ProductButton, HomeSkeleton } from "@/components/decorate/index";
import { imageFormat, productImage } from "@/utils/format";
import Price from "@/components/decorate/src/Price.vue";
import { Swiper } from "@/components/swiper";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/";
import { ModuleType, ModuleActivityType } from "@/types/decorate/decorate.d";

const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});

// 在modules加入要使用的模块
const module = defineModel<ModuleType & ModuleActivityType>("module") as Ref<ModuleType & ModuleActivityType>;
const defaultModule = ref({
    style: 1,
    goodsStyle: 1,
    goodsRadioStyle: 1,
    textAlign: 1,
    textWeight: 1,
    goodsNameRow: 2,
    goodsNamePadding: 1,
    showName: 1,
    showBrief: 1,
    showPrice: 1,
    goodsPadding: 5,
    buyBtnStyle: 1,
    backgroundColor: "",
    boxRadius: 0,
    innerPadding: 10,
    boxPadding: 10,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    frame: defaultFrame,
    title: defaultTitle
});
mergeDefaultModule(module.value, defaultModule.value);

const { frame } = module.value;

// 格式化 frame 配置
const frameFormat = computed(() => {
    const frameData = frame ?? {};
    return {
        boxPadding: `padding-left: ${frameData.boxPadding || 0}px; padding-right: ${frameData.boxPadding || 0}px;`,
        boxPaddingTop: `padding-top: ${frameData.boxPaddingTop || 0}px;`,
        boxPaddingBottom: `padding-bottom: ${frameData.boxPaddingBottom || 0}px;`,
        backgroundColor: frameData.backgroundColor ? `background-color: ${frameData.backgroundColor};` : "",
        boxRadius: frameData.boxRadius ? `border-radius: ${frameData.boxRadius}px;` : ""
    };
});

// 格式化 all 配置
const allFormat = computed(() => {
    return {
        backgroundColor: module.value.backgroundColor ? `background-color: ${module.value.backgroundColor};` : "",
        boxPadding: `margin-left: ${module.value.boxPadding || 0}px; margin-right: ${module.value.boxPadding || 0}px;`,
        boxPaddingBottom: `margin-bottom: ${module.value.boxPaddingBottom || 0}px;`,
        boxPaddingTop: `margin-top: ${module.value.boxPaddingTop || 0}px;`,
        boxRadius: module.value.boxRadius ? `border-radius: ${module.value.boxRadius}px;` : "",
        innerPadding: module.value.innerPadding ? `padding: ${module.value.innerPadding}px;` : "",
        goodsPadding: module.value.goodsPadding ? `padding: ${module.value.goodsPadding}px;` : ""
    };
});

const getDiscount = (currentPrice: number, originalPrice: number) => {
    if (originalPrice === 0 || currentPrice >= originalPrice) {
        return 0;
    }

    const discountPercentage = ((originalPrice - currentPrice) / originalPrice) * 100;

    return Math.floor(discountPercentage * 10) / 10;
};

// 根据 style 获取 swiper 配置
const getSwiperConfig = computed(() => {
    const style = typeof module.value?.style === "string" ? Number(module.value.style) : module.value?.style;
    // style 6 使用横向滑动，显示5个商品，样式与 style 2 保持一致
    const slidesPerView = 5;
    // const spaceBetween = module.value.goodsPadding; // 商品中间间隔为边距的两倍
    const spaceBetween = module.value.goodsPadding ? module.value.goodsPadding * 2 : 0;

    return {
        autoplay: false,
        slidesPerGroup: 5, // 每次滑动1个商品
        slidesPerView: slidesPerView,
        loopFillGroupWithBlank: true,
        spaceBetween: spaceBetween, // 使用 spaceBetween 控制间距，商品中间间隔为边距的两倍
        pagination: false,
        navigation: true,
        effect: "slide",
        loop: style === 6,
        watchOverflow: false // 取消根据商品数量自动禁用按钮的判断
    };
});

const seckillProductWrap = computed(() => getSwiperConfig.value);

// 判断是否使用 swiper 布局
const useSwiperLayout = computed(() => {
    const style = typeof module.value?.style === "string" ? Number(module.value.style) : module.value?.style;
    // style 6 使用 swiper 横向滑动，style 1 和 2 使用 flex 布局
    return style === 6;
});

// 获取商品项宽度（非 swiper 布局时使用，仅 style 1 和 2）
const getItemWidth = computed(() => {
    const style = typeof module.value?.style === "string" ? Number(module.value.style) : module.value?.style;
    // 获取商品间距（gap），gap = goodsPadding * 2（商品中间间隔为边距的两倍）
    // const gap = module.value?.goodsPadding ? Number(module.value.goodsPadding) : 0; // gap 现在是边距的两倍
    const gap = module.value?.goodsPadding ? Number(module.value.goodsPadding) * 2 : 0;

    switch (style) {
        case 1: {
            // 一行4个，每个25%
            // 4个商品之间有3个间隔，每个间隔 = gap（gap = goodsPadding * 2）
            if (gap > 0) {
                const totalGap = (gap * 3) / 4;
                return `calc(25% - ${totalGap}px)`;
            }
            return "25%";
        }
        case 2: {
            // 一行5个，每个20%
            // 5个商品之间有4个间隔，每个间隔 = gap（gap = goodsPadding * 2）
            if (gap > 0) {
                const totalGap = (gap * 4) / 5;
                return `calc(20% - ${totalGap}px)`;
            }
            return "20%";
        }
        case 6:
            return "248px"; // style 6 使用 swiper 布局，不会使用此宽度
        default:
            return "25%"; // 默认一行4个
    }
});

const seckillList = ref<any[]>([]);
const getData = async () => {
    try {
        const result = await getMobileSeckillList();
        seckillList.value = result;
    } catch (error) {
        console.error(error);
    }
};

onMounted(() => {
    getData();
});

const getTime = computed(() => {
    if (seckillList.value && seckillList.value.length > 0) {
        const firstItem = seckillList.value[0] as any;
        if (firstItem?.seckkillData?.seckillEndTime) {
            return new Date(firstItem.seckkillData.seckillEndTime).getTime();
        }
        if (firstItem?.seckillEndTime) {
            return new Date(firstItem.seckillEndTime).getTime();
        }
    }
    return 0;
});
</script>
<style lang="less" scoped>
// ========== 基础样式 ==========
.module-seckill-goods_ad-wrapper {
    box-sizing: border-box;
}

// 公共样式：图片过渡效果
.pro_pic,
.item-image {
    transition:
        transform 0.4s ease,
        -webkit-transform 0.4s ease,
        -moz-transform 0.4s ease,
        -o-transform 0.4s ease;
}

// ========== 秒杀模块 ==========
.mod_seckill {
    display: flex;
    flex-direction: column;
    width: 100%;
    border-radius: 8px;
    overflow: hidden;

    // 顶部区域（标题和倒计时）
    .seckill-top {
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
        padding: 10px 0;
        box-sizing: border-box;
        margin-bottom: 10px;

        .seckill-title-wrapper {
            display: flex;
            align-items: baseline;
            gap: 4px;

            .line {
                width: 4px;
                height: 16px;
                background-color: #fff;
                border-radius: 8px;
            }

            .image-size {
                width: 30px;
                height: 30px;
                margin-left: 10px;
            }

            .seckill-title {
                font-size: 16px;
                font-weight: bold;
                color: #1a1a1a;
                margin-right: 5px;
            }

            .seckill-desc {
                font-size: 12px;
                color: #1a1a1a;

                &.big-sm {
                    font-size: 16px;
                }
            }
        }

        .seckill-top-right {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .seckill-count-down {
            display: flex;
            align-items: center;
            font-size: 18px;
            color: #1a1a1a;

            a {
                text-decoration: none;
                color: inherit;

                &:hover {
                    text-decoration: none;
                }
            }
        }

        .seckill-more-link {
            cursor: pointer;
            text-decoration: none;

            i {
                font-size: 14px !important;
            }

            &:hover {
                text-decoration: underline;
            }
        }
    }

    // 商品列表容器
    .seckill_list_wrap {
        position: relative;
        z-index: 1;
        flex: 1;
        height: 100%;
        // padding: 0 10px;
        // padding-bottom: 10px;
    }

    .seckill_list {
        position: relative;
        z-index: 1;
        height: 100%;
        overflow: hidden;
        width: 100%;

        :deep(.swiper) {
            width: 100%;
            padding: 0;
        }

        :deep(.swiper-slide) {
            flex-shrink: 0;
            height: auto;
            box-sizing: border-box;
        }

        .proditem {
            box-sizing: border-box;
            position: relative;
            z-index: 1;
            width: 100%;
            background-color: #ffffff;

            a {
                display: block;
            }

            &:hover {
                .seckill-item-image {
                    display: flex;
                    justify-content: center;
                    overflow: hidden;

                    .pro_pic {
                        filter: brightness(105%);
                    }
                }
            }
        }

        .pro_pic {
            width: 100%;
        }

        .seckill-item__price {
            display: flex;
            align-items: flex-start;
        }
    }
}

// 倒计时盒子
.countdown-box {
    .countdown-separator-text {
        font-size: 18px;
        color: #1a1a1a;
    }

    .countdown-separator {
        color: #1a1a1a;
        margin: 0 1px;
    }

    .countdown-text {
        font-size: 18px;
        font-weight: 400;
        line-height: 24px;
        color: #1a1a1a;
        text-transform: capitalize;
    }

    .countdown-item {
        display: inline-block;
        padding: 2px 0;
        width: 32px;
        text-align: center;
        background-color: #1a1a1a;
        color: #fff;
        border-radius: 4px;
    }

    .days {
        padding: 0 5px;
    }

    .iconfont-pc {
        color: #1a1a1a;
        font-size: 14px;
        padding-left: 4px;
    }

    .countdown-arrow {
        color: #000;
        display: inline-block;
        font-size: 18px;
        font-weight: normal;
        margin-left: 8px;
    }

    span {
        margin: 0 1px;
    }
}

// 商品图片
.seckill-item-image {
    width: 100%;
    display: flex;
    justify-content: center;
    overflow: hidden;
    background-color: transparent;
}

// 商品折扣标签
.seckill-item-discount {
    margin: 5px;
    width: fit-content;
    height: 20px;
    line-height: 16px;
    color: #fff;
    text-align: center;
    border-radius: 2px;
    background-color: #ee0a24;
    padding: 2px 6px;
    font-size: 14px;
    box-sizing: border-box;
}

// 商品名称
.seckill-item__name {
    height: 20px;
    margin-bottom: 12px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    color: #1a1a1a;
    font-size: 16px;
    font-weight: 400;
    line-height: 18px;
    cursor: pointer;
    padding: 0 5px;

    &:hover {
        color: #ee0a24;
    }
}

// 秒杀价格区域
.seckill-item__price {
    padding: 0 5px;
    margin-bottom: 5px;
    .buy {
        margin-left: auto;

        .view_detail {
            display: flex;
            justify-content: center;
            align-items: center;
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: var(--primary-light-9);
            color: #ee0a24;
            cursor: pointer;

            &:hover {
                color: var(--primary-dark-2);
            }

            &.disabled {
                opacity: 0.5;
                cursor: not-allowed;
            }
        }
    }

    .price-miaosha {
        align-items: baseline;
        justify-content: center;
        margin-right: 8px;
        color: #1a1a1a;

        :deep(.price-row) {
            align-items: baseline !important;
        }

        :deep(.num) {
            font-size: 24px;
            color: #1a1a1a;
            font-weight: 600;
        }

        :deep(.util),
        :deep(.decimal) {
            color: #1a1a1a;
            font-size: 16px;
        }
    }
}
.price-origin {
    align-items: baseline;
    justify-content: center;
    height: 19px;
    color: #888b94 !important;

    :deep(.price-row) {
        line-height: 22px;
        font-weight: 500;
    }

    :deep(.num),
    :deep(.util) {
        color: #888b94 !important;
    }

    :deep(.num) {
        font-size: 14px;
    }
}

.price-origin {
    box-sizing: border-box;
}

// Swiper 相关样式
:deep(.swiper) {
    box-sizing: border-box;
}

:deep(.swiper-wrapper) {
    display: flex;
}

.module-seckill-goods_ad {
    :deep(.swiper-button-prev),
    :deep(.swiper-button-next) {
        box-sizing: border-box;
        background-color: rgba(0, 0, 0, 0.122);
        width: 40px;
        height: 48px;
        line-height: 35px;
        z-index: 2;
        outline: none;
        transition: background-color 0.2s ease;

        &:hover {
            background-color: rgba(0, 0, 0, 0.239);
        }

        &:after {
            font-family: "iconfont-pc";
            content: "\e70d";
            color: #fff;
            font-size: 14px;
        }

        &.swiper-button-disabled {
            pointer-events: fill;
        }
    }

    :deep(.swiper-button-prev) {
        left: 0;
        padding-right: 7px;
        border-radius: 0 24px 24px 0 !important;

        &:after {
            transform: rotate(180deg);
        }
    }

    :deep(.swiper-button-next) {
        right: 0;
        padding-left: 7px;
        border-radius: 24px 0 0 24px !important;
    }

    // 秒杀标签相关
    .cap-seckill-goods__tag {
        position: absolute;
        bottom: 0;
        left: 0;
        box-sizing: border-box;
        width: 100%;
        height: 24px;
        padding: 0 12px;
        color: #fff;
        line-height: 24px;
        background: -webkit-linear-gradient(left, #fd3e20, #fc6340);
        background: linear-gradient(90deg, #fd3e20, #fc6340);
        background: linear-gradient(to right, var(--ump-start-bg, #f86f30), var(--ump-end-bg, #e91717));
        color: var(--ump-main-text, #ffffff);

        .cap-seckill-goods__tag-title {
            font-weight: 700;
            font-size: 20px;
            float: left;
        }

        .cap-seckill-goods__countdown {
            rich-text {
                font-weight: bold;
                font-size: 12px;
                text-align: center;
            }
        }

        &.big {
            height: 40px;
            line-height: 40px;

            .cap-seckill-goods__tag-countdown {
                float: right;
                height: 40px;
                font-size: 12px;
                line-height: 16px;
            }

            .cap-seckill-goods__tag-countdown-text {
                display: block;
                height: 16px;
                margin: 5px 0 0;
                line-height: 16px;
                text-align: left;
                opacity: 0.6;
            }

            .cap-seckill-goods__countdown {
                margin-top: 0;

                text {
                    padding: 0 2px;
                }
            }
        }

        &.list,
        &.small {
            height: auto;
            padding: 5px 10px;

            .cap-seckill-goods__tag-countdown {
                font-size: 12px;
                display: flex;
                white-space: nowrap;
                justify-content: space-between;
                height: auto;
            }

            .cap-seckill-goods__tag-countdown-text {
                display: block;
                height: 24px;
                line-height: 24px;
            }
        }
    }
}

// ========== 商品广告样式 ==========
.cap-seckill-goods__tag {
    display: flex;
    align-items: center;
    justify-content: space-between;

    &.flex-end {
        justify-content: flex-end;
    }
}

.module-goods_ad {
    .module-ad-content {
        overflow: hidden;
        z-index: 1;
        position: relative;
    }
}

.seckill-item-image-wrapper {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}

.goods-ad-warp {
    .goods-ad-con {
        display: flex;
        flex-wrap: wrap;
    }

    .goods-ad-item {
        position: relative;
        overflow: hidden;
        display: inline-block;
        transition: opacity 0.4s ease-in-out;
        box-sizing: border-box;
        background-color: #ffffff;

        .item-content,
        .item-con {
            background-color: #ffffff;
        }

        &:hover {
            .item-photo.seckill-item-image,
            .seckill-item-image {
                display: flex;
                justify-content: center;
                overflow: hidden;

                .pro_pic,
                .item-image {
                    filter: brightness(105%);
                }
            }
        }

        .item-photo.seckill-item-image {
            background: #ffffff;
            text-align: center;
            position: relative;
            width: 100%;
            display: flex;
            justify-content: center;

            .item-image,
            .item-image.pro_pic {
                height: auto;
                width: 100%;
            }
        }

        .item-info {
            background: #ffffff;
            position: relative;
            padding: 5px;

            .item-name.seckill-item__name {
                margin: 0;
                margin-top: 0;
                display: block;

                .item-name-a {
                    line-height: 18px;
                    height: 20px;
                    overflow: hidden;
                    display: block;
                }
            }

            .item-name {
                .item-brief {
                    line-height: 20px;
                    height: 20px;
                    overflow: hidden;
                    display: block;
                    color: #aaa;
                    text-overflow: ellipsis;
                    display: -webkit-box;
                    display: -moz-box;
                    -webkit-line-clamp: 1;
                    -moz-line-clamp: 1;
                    line-clamp: 1;
                    -webkit-box-orient: vertical;
                    -moz-box-orient: vertical;
                    font-size: 12px;
                }
            }

            .item-action.seckill-item__price {
                margin-top: 0;
                vertical-align: middle;
                padding: 0;
                position: relative;
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-top: 12px;

                + .price-origin {
                    text-decoration: line-through;
                }
            }

            .item-buy {
                justify-content: center;
                height: 30px;
                display: flex;
                align-items: center;

                .buy-btn {
                    div {
                        font-size: 12px;
                    }
                }

                .iconfont-h5 {
                    color: var(--general);
                    font-size: 18px;

                    &::before {
                        content: "\e611";
                    }
                }
            }
        }
    }
}

// ========== 广告样式变体 ==========
.ad-style__1 {
    .goods-ad-warp {
        .goods-ad-con {
            display: flex;
            flex-wrap: wrap;
        }

        .goods-ad-item {
            .item-info {
                .item-name:not(.seckill-item__name) {
                    .item-name-a {
                        font-size: 14px;
                    }
                }
            }
        }
    }
}

.ad-style__2 {
    .goods-ad-warp {
        .goods-ad-con {
            display: flex;
            flex-wrap: wrap;
        }

        .goods-ad-item {
            .item-info {
                .item-name:not(.seckill-item__name) {
                    .item-name-a {
                        font-size: 12px;
                        font-weight: bold;
                    }
                }
            }
        }
    }
}

.ad-style__3 {
    .goods-ad-warp {
        .goods-ad-item {
            .item-info {
                .item-buy {
                    display: none;
                }
            }
        }
    }
}

.ad-style__4 {
    .goods-ad-warp {
        .goods-ad-con {
            display: flex;
            flex-wrap: wrap;

            .goods-ad-item {
                width: 100%;

                &:nth-child(3n + 2),
                &:nth-child(3n + 3) {
                    width: 50%;
                }
            }
        }
    }
}

.ad-style__6 {
    .goods-ad-warp {
        .goods-ad-con {
            display: flex;
            flex-wrap: wrap;

            .goods-ad-item {
                width: 100%;
            }
        }

        .goods-ad-item {
            .item-content {
                display: flex;
                flex-wrap: nowrap;

                .item-con {
                    display: flex;
                    flex-wrap: nowrap;
                    flex: 1;
                }
            }

            .item-photo {
                width: 40%;
            }

            .item-info {
                width: 60%;
                text-align: left;

                .item-name:not(.seckill-item__name) {
                    .item-name-a {
                        font-size: 12px;
                        font-weight: bold;
                    }
                }
            }

            .item-action {
                position: absolute;
                bottom: 0;
                left: 0;
                right: 0;
                padding-bottom: 0;
            }
        }
    }
}

.ad-style__7 {
    .goods-ad-warp {
        overflow: hidden;
        padding-bottom: 25px;

        .goods-ad-con {
            display: block;
            flex-wrap: nowrap;
            padding: 0 !important;
            height: 100%;

            .goods-ad-item {
                width: 33.333%;
            }
        }

        swiper {
            height: 200px;
            position: relative;
            overflow: hidden;
        }

        .goods-ad-item {
            .item-info {
                .item-buy {
                    display: none;
                }
            }
        }

        .swiper-pagination-con {
            position: absolute;
            bottom: 15px;
            display: flex;
            justify-content: center;
            align-items: center;
            width: 100%;
        }

        .swiper-pagination {
            display: inline-block;
            width: 12px;
            border-radius: 0;
            height: 2px;
            margin: 0 1px;
            background: #000;
            opacity: 0.2;

            &.active {
                opacity: 1;
                background-color: #fff;
            }
        }
    }
}

// ========== 购买按钮样式变体 ==========
.ad-buy_btn_style__0 {
    .goods-ad-warp {
        .goods-ad-item {
            .item-info {
                .item-buy {
                    display: none;
                }
            }
        }
    }
}

.ad-buy_btn_style__1 {
    .goods-ad-warp {
        .goods-ad-item {
            .item-info {
                .item-buy {
                    .iconfont-h5::before {
                        content: "\e611";
                        font-size: 18px;
                    }
                }
            }
        }
    }
}

.ad-buy_btn_style__2 {
    .goods-ad-warp {
        .goods-ad-item {
            .item-info {
                .item-buy {
                    .iconfont-h5::before {
                        content: "\e664";
                        font-size: 20px;
                    }
                }
            }
        }
    }
}

.ad-buy_btn_style__3 {
    .goods-ad-warp {
        .goods-ad-item {
            .item-info {
                .item-buy {
                    .iconfont-h5::before {
                        content: "\e74a";
                        font-size: 18px;
                    }
                }
            }
        }
    }
}

.ad-buy_btn_style__4 {
    .goods-ad-warp {
        .goods-ad-item {
            .item-info {
                .item-buy {
                    .iconfont-h5::before {
                        content: "\e620";
                        font-size: 18px;
                    }
                }
            }
        }
    }
}

// 带边框按钮样式（样式5和7）
.ad-buy_btn_style__5,
.ad-buy_btn_style__7 {
    .goods-ad-warp {
        .goods-ad-item {
            .item-info {
                .item-buy {
                    .buy-btn {
                        div {
                            color: var(--general);
                            height: 22px;
                            line-height: 22px;
                            padding: 0 8px;
                            display: inline-block;
                            position: relative;
                            border-radius: 2px;
                            border: 0 solid var(--general);

                            &:before {
                                content: "";
                                position: absolute;
                                top: 0;
                                left: 0;
                                box-sizing: border-box;
                                width: 200%;
                                height: 200%;
                                transform: scale(0.5);
                                transform-origin: 0 0;
                                pointer-events: none;
                                border: 1px solid #f44;
                                border-radius: 2px;
                            }
                        }
                    }
                }
            }
        }
    }
}

// 圆角填充按钮样式（样式6和8）
.ad-buy_btn_style__6,
.ad-buy_btn_style__8 {
    .goods-ad-warp {
        .goods-ad-item {
            .item-info {
                .item-buy {
                    .buy-btn {
                        div {
                            background: var(--general);
                            color: #fff;
                            height: 22px;
                            line-height: 22px;
                            padding: 0 10px;
                            display: inline-block;
                            position: relative;
                            border-radius: 22px;
                        }
                    }
                }
            }
        }
    }
}

// ========== 商品样式变体 ==========
.ad-goods_style__2 {
    .goods-ad-warp {
        .goods-ad-con {
            .goods-ad-item {
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06) !important;
            }
        }
    }
    .mod_seckill .seckill_list .proditem {
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06) !important;
    }
}

.ad-goods_style__3 {
    .goods-ad-warp {
        .goods-ad-con {
            .goods-ad-item {
                // 保留外层边框
                border: 1px solid rgba(50, 50, 51, 0.1) !important;

                // 移除内层 .item-con 的边框，避免双边框
                .item-con {
                    border: none !important;
                }
            }
        }
    }
    // swiper 布局的商品卡没有 .item-con，保留边框
    .mod_seckill .seckill_list .proditem {
        border: 1px solid rgba(50, 50, 51, 0.1) !important;
    }
}

.ad-goods_style__4 {
    .goods-ad-warp {
        .goods-ad-item {
            background: transparent !important;
            .item-content,
            .item-con,
            .item-info,
            .item-photo,
            .item-photo.seckill-item-image {
                background: transparent !important;
            }
            .seckill-item-discount {
                margin: 5px 0px !important;
            }
            .item-info {
                padding: 0 !important;
            }
        }
    }
    .proditem {
        background: transparent !important;
        .item-content,
        .item-con,
        .item-info,
        .item-photo,
        .item-photo.seckill-item-image {
            background: transparent !important;
        }
        .seckill-item-discount {
            margin: 5px 0px !important;
        }
        .item-info {
            padding: 0 !important;
        }
    }
    .seckill-item__name {
        padding: 0px !important;
    }
    .seckill-item__price {
        padding: 0 !important;
    }
}

// ========== 商品圆角样式 ==========
.ad-goods_radio_style__2 {
    .goods-ad-item {
        border-radius: 6px;
        overflow: hidden;
    }

    .goods-ad-warp {
        .goods-ad-con {
            .goods-ad-item {
                .item-con {
                    border-radius: 6px;

                    .item-photo {
                        border-radius: 6px 6px 0 0;
                        overflow: hidden;
                    }

                    .item-info {
                        border-radius: 0 0 6px 6px;
                    }
                }
            }
        }
    }
}

// ========== 文本对齐样式 ==========
.ad-text_align__2 {
    .goods-ad-warp {
        .goods-ad-con {
            .goods-ad-item {
                text-align: center;
            }
        }

        .goods-ad-item {
            .item-info {
                .item-buy {
                    height: auto;
                    padding-bottom: 10px;
                }

                .item-action {
                    flex-wrap: wrap;
                    justify-content: normal;

                    .item-price,
                    .item-buy {
                        width: 100%;
                    }
                }
            }
        }
    }
}

// ========== 文本粗细样式 ==========
.ad-text_weight__2 {
    .goods-ad-warp {
        .goods-ad-item {
            .item-info {
                .item-name {
                    .item-name-a {
                        font-weight: bold;
                    }
                }
            }
        }
    }
}

// ========== 商品名称行数 ==========
.ad-goods_name_row__1 {
    .item-name-a {
        line-height: 20px;
        height: 20px;
        text-overflow: ellipsis;
        display: -webkit-box;
        display: -moz-box;
        -webkit-line-clamp: 1;
        -moz-line-clamp: 1;
        line-clamp: 1;
        -webkit-box-orient: vertical;
        -moz-box-orient: vertical;
    }
}
.ad-goods_name_row__2 {
    .seckill-item__name {
        height: auto;
        white-space: normal !important;
        display: -webkit-box;
        display: -moz-box;
        -webkit-line-clamp: 2;
        -moz-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        -moz-box-orient: vertical;
    }
    .item-name-a {
        height: 40px !important;
        white-space: normal;
        display: -webkit-box !important;
        display: -moz-box !important;
        -webkit-line-clamp: 2;
        -moz-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        -moz-box-orient: vertical;
        line-height: 20px !important;
        overflow: hidden;
        word-break: break-all;
        text-overflow: ellipsis;
    }
}
// ========== 商品名称内边距 ==========
.ad-goods_name_padding__0 {
    .goods-ad-warp {
        .goods-ad-item {
            .item-info {
                .item-name {
                    margin-left: 0;
                    margin-right: 0;
                }

                .item-action {
                    padding-left: 0;
                    padding-right: 0;
                }
            }
        }
    }
}

// ========== 商品标题样式 ==========
.ad-goods-title_style__3 {
    .image-ad-title {
        padding-bottom: 28px !important;
    }
    .goods-ad-warp {
        border-top-left-radius: 15px;
        margin-top: -25px !important;
        overflow: hidden;
        background: #fff;
        padding-top: 10px;
    }
}

/* 占位图片样式 */
.module-ad-empty {
    padding-top: 30px;
    padding-bottom: 50px;
    text-align: center;
    background-color: #f2f4f6;

    .image-empty-bg {
        background: url("@/views/decorate/decorate/src/img/empty-img-bg.png") no-repeat center center;
        background-size: 100% auto;
        width: 100px;
        margin: 0 auto 10px;
        height: 100px;
    }

    .desc {
        text-align: center;
        color: #888;
        font-size: 14px;
    }
}
</style>
