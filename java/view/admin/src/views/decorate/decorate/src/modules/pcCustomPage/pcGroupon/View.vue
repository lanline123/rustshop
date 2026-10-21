<template>
    <div class="module-groupon-goods_ad-wrapper" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
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
                'module-ad-con module-goods_ad module-groupon-goods_ad ad-style__' +
                module?.style +
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
                <!-- 占位显示：没有选择活动或没有商品时 -->
                <div
                    v-if="!module.grouponActivityItem || !module.grouponActivityItem.grouponPromotionId || grouponItemList.length === 0"
                    class="module-ad-empty empty-groupon"
                >
                    <div class="image-empty-bg"></div>
                    <div class="desc">{{ $t('点击选择拼团活动') }}</div>
                </div>
                <!-- 有商品时显示商品列表 -->
                <div v-else-if="grouponItemList.length > 0" class="mod_groupon">
                    <!-- 标题和查看更多一起显示隐藏 -->
                    <div
                        class="groupon-top"
                        v-if="module.title?.showTitle == 1"
                        :style="{
                            background: module.title?.titleBackground2
                                ? 'linear-gradient(to right, ' + (module.title?.titleBackground || '') + ', ' + module.title.titleBackground2 + ')'
                                : module.title?.titleBackground || '',
                            borderTopLeftRadius: module.title?.titleRadius ? module.title.titleRadius + 'px' : '',
                            borderTopRightRadius: module.title?.titleRadius ? module.title.titleRadius + 'px' : ''
                        }"
                    >
                        <div class="groupon-title-wrapper">
                            <div class="line" v-if="module.title?.titleStyle == 3"></div>
                            <el-image
                                v-if="module.title?.titleBackgroundPic?.picUrl"
                                class="image-size"
                                :src="imageFormat(module.title.titleBackgroundPic.picUrl)"
                            ></el-image>
                            <div class="groupon-title" :style="{ color: module.title?.titleColor || '#1a1a1a' }">
                                {{ module.title?.titleText ? module.title.titleText : $t('拼团') }}
                            </div>
                            <div
                                class="groupon-desc"
                                v-if="module.title?.descText"
                                :style="{ color: module.title?.descColor || '#1a1a1a' }"
                                :class="[module.title?.titleStyle == 2 ? 'big-sm' : 'sm']"
                            >
                                {{ module.title.descText }}
                            </div>
                        </div>
                        <div class="groupon-top-right">
                            <a v-if="module.title?.showMore == 1" :style="{ color: module.title?.moreColor || '#1a1a1a' }" class="groupon-more-link">
                                {{ $t('查看更多') }}
                            </a>
                        </div>
                    </div>
                    <div class="groupon_list_wrap">
                        <!-- 使用 swiper 横向滑动布局（style 6） -->
                        <div
                            v-if="grouponItemList.length > 0 && useSwiperLayout"
                            class="groupon_list"
                            :style="{
                                paddingLeft: module?.goodsPadding ? module.goodsPadding + 'px' : '0',
                                paddingRight: module?.goodsPadding ? module.goodsPadding + 'px' : '0',
                                paddingTop: module?.goodsPadding ? module.goodsPadding + 'px' : '0',
                                paddingBottom: module?.goodsPadding ? module.goodsPadding + 'px' : '0'
                            }"
                        >
                            <Swiper v-model:swiperOption="grouponSwiperOption" v-model="grouponItemList" style="width: 100%">
                                <template v-slot:default="{ item }">
                                    <div class="proditem goods-ad-item">
                                        <a :title="item.productName">
                                            <div class="g-item-warp">
                                                <div class="pic">
                                                    <div class="groupon-item-image-wrapper">
                                                        <div class="groupon-item-image">
                                                            <el-image
                                                                :src="productImage(item)"
                                                                class="pro_pic"
                                                                loading="lazy"
                                                                fit="cover"
                                                                style="transition: opacity 0.2s linear"
                                                            />
                                                        </div>
                                                    </div>
                                                    <div class="countdown" v-if="Number(module?.cardStyle) === 1">
                                                        <div class="title">{{ $t('拼团') }}</div>
                                                        <div class="countdown-time" v-if="getCountdownText(item, '天')">
                                                            <div class="time">{{ isActivityStart(item) ? $t('距离结束仅剩') : $t('距离开始仅剩') }}</div>
                                                            <div class="text">{{ getCountdownText(item, $t('天')) }}</div>
                                                        </div>
                                                        <div class="countdown-time" v-else>
                                                            <div class="time">{{ $t('已结束') }}</div>
                                                        </div>
                                                    </div>
                                                </div>
                                                <template v-if="module?.showName == 1">
                                                    <div class="titleBox groupon-item__name">
                                                        <div
                                                            class="title product-name-text"
                                                            :data-group-label="(getMinGroupNum(item) || 0) + '人团'"
                                                            :style="{
                                                                textAlign: module?.textAlign == 1 ? 'left' : 'center',
                                                                fontWeight: module?.textWeight == 1 ? 'normal' : 'bold'
                                                            }"
                                                        >
                                                            {{ item.productName }}
                                                        </div>
                                                    </div>
                                                </template>
                                            </div>
                                        </a>
                                        <template v-if="module?.showPrice == 1">
                                            <template v-if="Number(module?.cardStyle) === 1">
                                                <div class="priceBox">
                                                    <div class="priceBoxNum">
                                                        <Price v-model="item.minGrouponPrice" class="minGrouponPrice" />
                                                        <div class="old">
                                                            <Price v-model="item.productPrice" />
                                                        </div>
                                                    </div>
                                                    <div class="text">{{ $t('拼') }}</div>
                                                </div>
                                            </template>
                                            <template v-else>
                                                <div class="groupon-item__price" :style="{ flexDirection: module?.textAlign == 1 ? 'row' : 'column' }">
                                                    <div style="display: flex; flex-direction: column; align-items: flex-start">
                                                        <Price v-model="item.minGrouponPrice" class="price price-miaosha" />
                                                        <template v-if="item.productPrice && Number(item.productPrice) > 0">
                                                            <Price v-model="item.productPrice" class="price price-origin" />
                                                        </template>
                                                    </div>
                                                    <div class="buy">
                                                        <div class="view_detail">
                                                            <i class="iconfont-pc icon-tianjia"></i>
                                                        </div>
                                                    </div>
                                                </div>
                                            </template>
                                        </template>
                                    </div>
                                </template>
                            </Swiper>
                        </div>
                        <!-- Flex 布局（style 1, 2） -->
                        <div v-else-if="grouponItemList.length > 0" class="goods-ad-warp">
                            <div
                                class="goods-ad-con"
                                :style="{
                                    // gap: module?.goodsPadding ? module.goodsPadding + 'px' : '0', // 商品中间间隔为边距的两倍
                                    gap: module?.goodsPadding ? module.goodsPadding * 2 + 'px' : '0',
                                    paddingLeft: module?.goodsPadding ? module.goodsPadding + 'px' : '0',
                                    paddingRight: module?.goodsPadding ? module.goodsPadding + 'px' : '0',
                                    paddingTop: module?.goodsPadding ? module.goodsPadding + 'px' : '0',
                                    paddingBottom: module?.goodsPadding ? module.goodsPadding + 'px' : '0'
                                }"
                            >
                                <div
                                    v-for="item in grouponItemList"
                                    :key="item.productId"
                                    class="proditem goods-ad-item"
                                    :style="{
                                        width: getItemWidth
                                    }"
                                >
                                    <a :title="item.productName">
                                        <div class="g-item-warp">
                                            <div class="pic">
                                                <div class="groupon-item-image-wrapper">
                                                    <div class="groupon-item-image">
                                                        <el-image
                                                            :src="productImage(item)"
                                                            class="pro_pic"
                                                            loading="lazy"
                                                            style="transition: opacity 0.2s linear"
                                                        />
                                                    </div>
                                                </div>
                                                <div class="countdown" v-if="Number(module?.cardStyle) === 1">
                                                    <div class="title">{{ $t('拼团') }}</div>
                                                    <div class="countdown-time" v-if="getCountdownText(item, '天')">
                                                        <div class="time">{{ isActivityStart(item) ? $t('距离结束仅剩') : $t('距离开始仅剩') }}</div>
                                                        <div class="text">{{ getCountdownText(item, $t('天')) }}</div>
                                                    </div>
                                                    <div class="countdown-time" v-else>
                                                        <div class="time">{{ $t('已结束') }}</div>
                                                    </div>
                                                </div>
                                            </div>
                                            <template v-if="module?.showName == 1">
                                                <div class="titleBox groupon-item__name">
                                                    <div
                                                        class="title product-name-text"
                                                        :data-group-label="(getMinGroupNum(item) || 0) + '人团'"
                                                        :style="{
                                                            textAlign: module?.textAlign == 1 ? 'left' : 'center',
                                                            fontWeight: module?.textWeight == 1 ? 'normal' : 'bold'
                                                        }"
                                                    >
                                                        {{ item.productName }}
                                                    </div>
                                                </div>
                                            </template>
                                        </div>
                                    </a>
                                    <template v-if="module?.showPrice == 1">
                                        <template v-if="Number(module?.cardStyle) === 1">
                                            <div class="priceBox">
                                                <div class="priceBoxNum">
                                                    <Price v-model="item.minGrouponPrice" class="minGrouponPrice" />
                                                    <div class="old">
                                                        <Price v-model="item.productPrice" />
                                                    </div>
                                                </div>
                                                <div class="text">{{ $t('拼') }}</div>
                                            </div>
                                        </template>
                                        <template v-else>
                                            <div class="groupon-item__price" :style="{ flexDirection: module?.textAlign == 1 ? 'row' : 'column' }">
                                                <div style="display: flex; flex-direction: column; align-items: flex-start">
                                                    <Price v-model="item.minGrouponPrice" class="price price-miaosha" />
                                                    <template v-if="item.productPrice && Number(item.productPrice) > 0">
                                                        <Price v-model="item.productPrice" class="price price-origin" />
                                                    </template>
                                                </div>
                                                <div class="buy">
                                                    <div class="view_detail">
                                                        <i class="iconfont-pc icon-tianjia"></i>
                                                    </div>
                                                </div>
                                            </div>
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
</template>

<script lang="ts" setup>
import { ref, computed, watchEffect, onMounted, onUnmounted, Ref } from "vue";
import { getGrouponItemList } from "@/api/promotion/groupon";
import type { GrouponItemFilterParams } from "@/types/promotion/groupon";
import { imageFormat, productImage } from "@/utils/format";
import Price from "@/components/decorate/src/Price.vue";
import { Swiper } from "@/components/swiper";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/";
import { ModuleType, ModuleActivityType } from "@/types/decorate/decorate.d";

// 类型定义
interface GrouponItem {
    productId: number;
    productSn?: string;
    productName: string;
    productBrief?: string;
    picLarge?: string;
    picUrl: string;
    picThumb?: string;
    minGrouponPrice: number;
    productPrice: number;
    productStock?: number;
    productSku?: any[];
    groupNum?: string;
    startTime: string;
    endTime: string;
}

interface GrouponItemListParams {
    page: number;
    size: number;
    sortField: string;
    keyword: string;
    sortOrder: string;
    grouponPromotionId: number;
}

const module = defineModel<ModuleType & ModuleActivityType>("module") as Ref<ModuleType & ModuleActivityType>;

// 定义默认值
const defaultModule = ref({
    style: 2,
    groupType: 1,
    goodsStyle: 1,
    goodsRadioStyle: 1,
    textAlign: 1,
    textWeight: 1,
    goodsNameRow: 1,
    goodsNamePadding: 0,
    showName: 1,
    showPrice: 1,
    cardStyle: 0,
    goodsPadding: 0,
    innerPadding: 0,
    boxPadding: 0,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    backgroundColor: defaultFrame.backgroundColor,
    boxRadius: 0,
    frame: defaultFrame,
    title: defaultTitle,
    grouponActivityItem: undefined
});

mergeDefaultModule(module.value, defaultModule.value);

// 格式化 frame 配置
const frameFormat = computed(() => {
    const frameData = formatFrame(module.value.frame ?? {});
    const paddingLeft = module.value.boxPadding || 0;
    const paddingRight = module.value.boxPadding || 0;
    return {
        boxPadding: `padding-left: ${paddingLeft}px; padding-right: ${paddingRight}px; --frame-padding-left: ${paddingLeft}px; --frame-padding-right: ${paddingRight}px;`,
        boxPaddingTop: `padding-top: ${module.value.boxPaddingTop || 0}px;`,
        boxPaddingBottom: `padding-bottom: ${module.value.boxPaddingBottom || 0}px;`,
        backgroundColor: frameData.backgroundColor || "",
        boxRadius: frameData.boxRadius || "",
        innerPadding: frameData.innerPadding || ""
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

const grouponItemList = ref<GrouponItem[]>([]);
const now = ref(Date.now());
let countdownTimer: number | null = null;

// 获取拼团商品列表数据
const getData = async () => {
    // 如果没有选中的活动，清空列表
    if (!module.value.grouponActivityItem || !module.value.grouponActivityItem.grouponPromotionId) {
        grouponItemList.value = [];
        return;
    }

    // 有选中的活动，获取该活动的所有商品列表（不限制数量）
    let filterParams: GrouponItemFilterParams = {
        grouponPromotionId: module.value.grouponActivityItem.grouponPromotionId,
        page: 1,
        size: 100 // 获取更多商品，不限制数量
    };

    try {
        const result = await getGrouponItemList(filterParams);
        // 转换API返回的数据格式，有多少个就显示多少个
        grouponItemList.value = (result.records || []).map((item: any) => {
            return {
                productId: item.productId || 0,
                productSn: item.productSn || "",
                productName: item.productName || "",
                productBrief: "",
                picLarge: item.picLarge || "",
                picUrl: item.picUrl || "",
                picThumb: item.picThumb || "",
                minGrouponPrice: Number(item.minGroupPrice || item.productPrice || 0),
                productPrice: Number(item.productPrice || 0),
                productStock: 0,
                productSku: [],
                groupNum: item.groupNum || "2",
                startTime: module.value.grouponActivityItem?.startTime || "",
                endTime: module.value.grouponActivityItem?.endTime || ""
            };
        });
    } catch (error) {
        console.error("获取拼团商品列表失败:", error);
        // 出错时清空列表
        grouponItemList.value = [];
    }
};

// 监听活动变化和样式变化，自动刷新数据
watchEffect(() => {
    // 监听 grouponActivityItem 和 style 的变化
    module.value.grouponActivityItem;
    module.value.style;
    getData();
});

// 组件挂载时立即执行一次，确保初始化时如果有活动信息就加载商品列表
onMounted(() => {
    getData();
    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }
    countdownTimer = window.setInterval(() => {
        now.value = Date.now();
    }, 1000);
});

onUnmounted(() => {
    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }
});
const time = ref(60 * 60 * 1000);

const className = computed(() => {
    const style = typeof module.value.style === "string" ? Number(module.value.style) : module.value.style;
    switch (style) {
        case 1:
            return "big";
        case 2:
            return "small flex-end";
        case 6:
            return "list flex-end";
        default:
            return "";
    }
});

const groupPirceStyle = computed(() => {
    if (module.value.style != 3) {
        return {
            fontSize: "16px"
        };
    } else {
        return {
            fontSize: "14px"
        };
    }
});

const groupCurrencyStyle = computed(() => {
    if (module.value.style != 3) {
        return {
            fontSize: "12px"
        };
    } else {
        return {
            fontSize: "11px"
        };
    }
});

/**
 * 拿到成团人数
 * @param item
 */
const getMinGroupNum = (item: GrouponItem) => {
    if (item.groupNum && item.groupNum.length) {
        try {
            const groupNum = JSON.parse(item.groupNum);
            // 找到最小团人数
            return Math.min(...groupNum.map((x: any) => x.groupNum));
        } catch (e) {
            return 0;
        }
    }
};

/*转时间搓 */
const dateStringToTimestamp = (dateString: any) => {
    if (!dateString) return 0;
    const dateParts = dateString.split(" ");
    const [year, month, day] = dateParts[0].split("-");
    const [hour, minute, second] = dateParts[1] ? dateParts[1].split(":") : ["0", "0", "0"];
    const date = new Date(year, month - 1, day, hour, minute, second);
    return date.getTime();
};

const isActivityStart = (item: GrouponItem) => {
    const startTime = dateStringToTimestamp(item.startTime);
    return startTime < Date.now();
};

const getCountdownFormat = (endTime: number, dayText: string): string => {
    const diff = endTime - Date.now();
    if (diff <= 0) return "";
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    const remainingSeconds = String(seconds % 60).padStart(2, "0");
    const remainingMinutes = String(minutes % 60).padStart(2, "0");
    const remainingHours = String(hours % 24).padStart(2, "0");
    if (days === 0) {
        return `${remainingHours}:${remainingMinutes}:${remainingSeconds}`;
    }
    return `${days}${dayText} ${remainingHours}:${remainingMinutes}:${remainingSeconds}`;
};

const getCountdownText = (item: GrouponItem, dayText: string) => {
    void now.value;
    const targetTime = isActivityStart(item) ? dateStringToTimestamp(item.endTime) : dateStringToTimestamp(item.startTime);
    return getCountdownFormat(targetTime, dayText);
};

// 根据 style 获取 swiper 配置
const getSwiperConfig = computed(() => {
    const style = typeof module.value?.style === "string" ? Number(module.value.style) : module.value?.style;
    // style 6 使用横向滑动，显示5个商品
    const slidesPerView = 5;
    // const spaceBetween = module.value?.goodsPadding ? Number(module.value.goodsPadding) : 0; // 商品中间间隔为边距的两倍
    const spaceBetween = module.value?.goodsPadding ? Number(module.value.goodsPadding) * 2 : 0;

    return {
        autoplay: {
            delay: 5000,
            disableOnInteraction: false,
            pauseOnMouseEnter: true
        },
        slidesPerGroup: 1, // 每次滑动1个商品
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

const grouponSwiperOption = computed(() => getSwiperConfig.value);

// 判断是否使用 swiper 布局
const useSwiperLayout = computed(() => {
    const style = typeof module.value?.style === "string" ? Number(module.value.style) : module.value?.style;
    // style 6 使用 swiper 横向滑动，style 2 和 3 使用 flex 布局
    return style === 6;
});

// 获取商品项宽度（非 swiper 布局时使用，style 2 和 3）
const getItemWidth = computed(() => {
    const style = typeof module.value?.style === "string" ? Number(module.value.style) : module.value?.style;
    // 对于 flex 布局，使用 gap 属性处理间距，gap = goodsPadding * 2（商品中间间隔为边距的两倍），需要在宽度计算中减去相应的 gap
    // 对于 swiper 布局，使用 margin 处理间距，不影响宽度计算
    // const gap = module.value?.goodsPadding ? Number(module.value.goodsPadding) : 0; // gap 现在是边距的两倍
    const gap = module.value?.goodsPadding ? Number(module.value.goodsPadding) * 2 : 0;

    switch (style) {
        case 2: {
            // 一行4个，每个25%
            // 4个商品之间有3个间隔，每个间隔 = gap（gap = goodsPadding * 2）
            // 每个商品的宽度 = (100% - 3 * gap) / 4 = 25% - (3 * gap) / 4
            if (gap > 0) {
                const totalGap = (gap * 3) / 4;
                return `calc(25% - ${totalGap}px)`;
            }
            return "25%";
        }
        case 3: {
            // 一行5个，每个20%
            // 5个商品之间有4个间隔，每个间隔 = gap（gap = goodsPadding * 2）
            // 每个商品的宽度 = (100% - 4 * gap) / 5 = 20% - (4 * gap) / 5
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

// onMounted 中的逻辑已经整合到 watchEffect 中，不需要单独处理
</script>
<style lang="less" scoped>
.mod_groupon {
    --main-bg: #ee0a24;
    display: flex;
    flex-direction: column;
    width: 100%;
    overflow: hidden;
    .groupon-top {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 0;
        box-sizing: border-box;
        .groupon-title-wrapper {
            display: flex;
            align-items: baseline;
            gap: 4px;
            .line {
                width: 4px;
                height: 16px;
                background-color: #1a1a1a;
                border-radius: 8px;
            }
            .image-size {
                width: 30px;
                height: 30px;
                margin-left: 10px;
            }
            .groupon-title {
                font-size: 16px;
                font-weight: bold;
                color: #1a1a1a;
                margin-right: 5px;
            }
            .groupon-desc {
                font-size: 12px;
                color: #1a1a1a;
                &.big-sm {
                    font-size: 16px;
                }
            }
        }
        .groupon-top-right {
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .groupon-more-link {
            font-size: 14px;
            color: #1a1a1a;
            text-decoration: none;
            &:hover {
                color: var(--main-bg);
            }
        }
    }
}

.groupon-card {
    background: #fff;
    border: 1px solid rgba(64, 112, 255, 0.35);
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    height: 100%;
    transition:
        border-color 0.2s ease,
        box-shadow 0.2s ease,
        transform 0.2s ease;
    box-shadow: 0 6px 16px rgba(15, 35, 95, 0.05);
}

.groupon-card:hover {
    border-color: var(--main-bg);
    box-shadow: 0 12px 30px rgba(15, 35, 95, 0.12);
    transform: translateY(-4px);
}

.groupon-card > a {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.groupon-card .groupon-item-image {
    width: 100%;
    border-radius: 14px;
    overflow: hidden;
    aspect-ratio: 3 / 4;
    background: #f8f8f8;
    align-items: center;
    justify-content: center;
}

.groupon-card .groupon-item-image :deep(.el-image) {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.groupon-card .groupon-item__name {
    margin-bottom: 4px;
}

.groupon-card .groupon-item__name .item-name-a {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #2a3145;
    font-size: 16px;
    font-weight: 600;
    line-height: 22px;
}

.groupon-card .group-num-text {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 44px;
    // padding: 0 10px;
    height: 24px;
    border-radius: 2px;
    border: 1px solid var(--main-bg);
    background: transparent;
    color: var(--main-bg);
    font-size: 12px;
    font-weight: 500;
}

.groupon-card .product-desc {
    margin-top: 4px;
    font-size: 13px;
    color: #999;
}

.groupon-card .groupon-item__price {
    margin-top: 12px;
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 8px;
}

.groupon-card .groupon-item__price .item-price {
    display: flex;
    align-items: baseline;
    gap: 8px;
}

.groupon-card .groupon-item__price .price-miaosha {
    margin-right: 0;
    :deep(.num) {
        color: var(--main-bg);
        font-size: 26px;
        font-weight: 700;
    }
    :deep(.util),
    :deep(.decimal) {
        color: var(--main-bg);
        font-size: 16px;
    }
}

.groupon-card .groupon-item__price .price-origin {
    align-items: baseline;
    justify-content: center;
    height: 19px;
    color: #888b94;
    text-decoration: line-through;
    :deep(.price-row) {
        line-height: 22px;
        font-weight: 500;
    }
    :deep(.num) {
        font-size: 16px;
        color: #888b94;
    }
    :deep(.util) {
        color: #888b94;
    }
}

.groupon-card .item-buy {
    display: flex;
    align-items: center;
}

.groupon-card .view_detail {
    background: #eef3ff;
    color: var(--main-bg);
}

.mod_groupon .groupon_list_wrap {
    position: relative;
    z-index: 1;
    flex: 1;
    height: 100%;
    width: 100%;
}

.mod_groupon .groupon_list {
    position: relative;
    z-index: 1;
    height: 100%;
    overflow: hidden;
    width: 100%;
}

.mod_groupon .groupon_list :deep(.swiper) {
    width: 100%;
    padding: 0;
}

.mod_groupon .groupon_list :deep(.swiper-slide) {
    // 宽度由 swiper 根据 slidesPerView 和 spaceBetween 自动计算
    flex-shrink: 0;
    height: auto;
    box-sizing: border-box;
}

.mod_groupon .groupon_list .proditem {
    box-sizing: border-box;
    position: relative;
    z-index: 1;
    width: 100%; // 宽度填满 slide
    background-color: #ffffff;
    padding-bottom: 10px;

    .ad-goods_style__4 & {
        background-color: transparent;
    }
    &:hover {
        .groupon-item-image {
            display: flex;
            justify-content: center;
            overflow: hidden;
            .pro_pic {
                filter: brightness(105%);
            }
        }
    }
}

.mod_groupon .groupon_list .proditem a {
    display: block;
}

.groupon-item-image {
    width: 100%;
    display: flex;
    justify-content: center;
    overflow: hidden;
    background-color: transparent;
    aspect-ratio: 1 / 1; // 设置固定宽高比，确保所有商品卡片高度一致
}

.mod_groupon .groupon_list .groupon-item-image {
    aspect-ratio: 1 / 1; // 设置固定宽高比，确保所有商品卡片高度一致
}
.mod_groupon .groupon_list .pro_pic {
    transition:
        transform 0.4s ease,
        -webkit-transform 0.4s ease,
        -moz-transform 0.4s ease,
        -o-transform 0.4s ease;
    width: 100%;
    height: 100%;
    object-fit: cover; // 确保图片填满容器
}

.mod_groupon .groupon_list .groupon-item__name {
    height: auto;
    min-height: 20px;
    margin-top: 10px;
    margin-bottom: 12px;
    // padding: 0 14px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    color: #1a1a1a;
    font-size: 16px;
    font-weight: 400;
    line-height: 18px;
    cursor: pointer;
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    gap: 4px;
    .group-num {
        flex-shrink: 0;
        display: inline-flex;
        align-items: baseline;
        margin-right: 4px;
        .group-num-text {
            color: var(--main-bg) !important;
            background-color: transparent;
            border: 1px solid var(--main-bg);
            padding: 2px 4.5px;
            border-radius: 2px;
            font-size: 12px;
            line-height: 12px;
            font-weight: normal;
            white-space: nowrap;
            box-sizing: border-box;
            display: inline-flex;
            margin-top: -3px;
        }
    }
    .product-name-text {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        min-width: 0;
        position: relative;
        &::before {
            content: attr(data-group-label);
            display: inline-block;
            color: var(--main-bg) !important;
            background-color: transparent;
            border: 1px solid var(--main-bg);
            padding: 2px 4.5px;
            border-radius: 2px;
            font-size: 12px;
            line-height: 12px;
            font-weight: normal;
            white-space: nowrap;
            box-sizing: border-box;
            margin-right: 4px;
            margin-top: -3px;
            vertical-align: middle;
            flex-shrink: 0;
        }
    }
}
.ad-goods_name_row__2 .mod_groupon .groupon_list .groupon-item__name {
    .product-name-text {
        white-space: normal;
        display: -webkit-box;
        display: -moz-box;
        -webkit-line-clamp: 2;
        -moz-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        -moz-box-orient: vertical;
        line-height: 24px;
        height: 48px;
        overflow: hidden;
        text-overflow: ellipsis;
        word-break: break-word;
        position: relative;
        padding-left: 0;
        text-indent: 0;
        &::before {
            content: attr(data-group-label);
            display: inline-block;
            color: var(--main-bg) !important;
            background-color: transparent;
            border: 1px solid var(--main-bg);
            padding: 2px 4.5px;
            border-radius: 2px;
            font-size: 12px;
            line-height: 12px;
            font-weight: normal;
            white-space: nowrap;
            box-sizing: border-box;
            margin-right: 4px;
            margin-top: -3px;
            vertical-align: middle;
            flex-shrink: 0;
        }
    }
}
.mod_groupon .groupon_list .groupon-item__price {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    // padding: 0 14px;
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
            color: var(--main-bg);
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
}

.groupon-item__price .price-miaosha {
    align-items: baseline;
    justify-content: center;
    margin-right: 8px;
    color: var(--main-bg);
    line-height: 22px;

    :deep(.price-row) {
        align-items: baseline !important;
    }

    :deep(.num) {
        font-size: 24px;
        color: var(--main-bg);
        font-weight: 600;
    }

    :deep(.util) {
        color: var(--main-bg);
        font-size: 16px;
    }
    :deep(.decimal) {
        color: var(--main-bg);
        font-size: 16px;
    }
}

.groupon-item__price .price-origin {
    align-items: baseline;
    justify-content: center;
    height: 19px;
    color: #888b94;
    text-decoration: line-through;
    :deep(.price-row) {
        line-height: 22px;
        font-weight: 500;
    }
    :deep(.num) {
        font-size: 16px;
        color: #888b94;
    }

    :deep(.util) {
        color: #888b94;
    }
}

/* 横向滑动时的原价样式，与其他布局保持一致 */
.mod_groupon .groupon_list .proditem .groupon-item__price + .price-origin,
.mod_groupon .groupon_list .proditem .price-origin {
    align-items: baseline;
    justify-content: center;
    height: 19px;
    color: #888b94;
    text-decoration: line-through;
    margin-top: 4px;
    :deep(.price-row) {
        line-height: 22px;
        font-weight: 500;
    }
    :deep(.num) {
        font-size: 16px;
        color: #888b94;
    }

    :deep(.util) {
        color: #888b94;
    }
}

.price-origin {
    // padding: 0 14px;
    box-sizing: border-box;
    text-decoration: line-through;
}

:deep(.swiper) {
    box-sizing: border-box;
    // padding: 0 12px;
}

:deep(.swiper-wrapper) {
    display: flex;
}

.module-groupon-goods_ad-wrapper {
    min-height: auto; /* 允许高度自适应 */
    height: auto; /* 高度自适应内容 */
    box-sizing: border-box;
}
.module-groupon-goods_ad:deep(.swiper-button-prev) {
    box-sizing: border-box;
    left: 0;
    background-color: rgba(0, 0, 0, 0.122);
    width: 40px;
    height: 48px;
    padding-right: 7px;
    line-height: 35px;
    border-radius: 0 24px 24px 0 !important;
    z-index: 2;
    outline: none;
    transition: background-color 0.2s ease;
    &:hover {
        background-color: rgba(0, 0, 0, 0.239);
    }
}

.module-groupon-goods_ad:deep(.swiper-button-next) {
    box-sizing: border-box;
    right: 0;
    background-color: rgba(0, 0, 0, 0.122);
    width: 40px;
    height: 48px;
    padding-left: 7px;
    line-height: 35px;
    z-index: 2;
    border-radius: 24px 0 0 24px !important;
    outline: none;
    transition: background-color 0.2s ease;
    &:hover {
        background-color: rgba(0, 0, 0, 0.239);
    }
}

.module-groupon-goods_ad:deep(.swiper-button-prev:after),
.module-groupon-goods_ad:deep(.swiper-button-next:after) {
    font-family: "iconfont-pc";
    content: "\e70d";
    color: #fff;
    font-size: 14px;
}

.module-groupon-goods_ad:deep(.swiper-button-prev:after) {
    transform: rotate(180deg);
}

.module-groupon-goods_ad:deep(.swiper-button-prev.swiper-button-disabled),
.module-groupon-goods_ad:deep(.swiper-button-next.swiper-button-disabled) {
    pointer-events: fill;
}

.cap-seckill-goods__tag {
    display: flex;
    align-items: center;
    justify-content: space-between;
    &.flex-end {
        justify-content: flex-end;
    }
}
/*商品类型*/
.module-goods_ad .module-ad-content {
    overflow: visible; /* 改为visible，允许内容撑开容器 */
    z-index: 1;
    position: relative;
    min-height: auto; /* 允许高度自适应 */
    height: auto; /* 高度自适应内容 */
}
.goods-ad-warp .goods-ad-item {
    /* width 由内联样式或具体样式类控制，不在此处设置固定值 */
    position: relative;
    overflow: hidden;
    display: inline-block;
    transition: opacity 0.4s ease-in-out;
    box-sizing: border-box;
    background-color: #ffffff;
    padding-bottom: 10px;

    .ad-goods_radio_style__2 & {
        background-color: transparent;
    }

    .ad-goods_style__4 & {
        background-color: transparent !important;
    }

    &:hover {
        .groupon-item-image {
            display: flex;
            justify-content: center;
            overflow: hidden;
            .pro_pic {
                filter: brightness(105%);
            }
        }
    }
}
.goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: wrap;
    width: 100%;
    min-height: auto; /* 允许高度自适应 */
    height: auto; /* 高度自适应内容 */
}
.groupon-item-image-wrapper {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}
.mod_groupon .groupon_list .proditem .groupon-item-image-wrapper {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}

/* ========== 复用前台卡片结构（cardStyle=1） ========== */
.g-item-warp {
    display: flex;
    flex-direction: column;
    width: 100%;
}
.g-item-warp .pic {
    width: 100%;
}
.g-item-warp .countdown {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: linear-gradient(90deg, #f86e30, #e91817);
    height: 34px;
    padding: 0 10px;
    box-sizing: border-box;
    font-size: 10px;
    color: #ffffff;
}
.g-item-warp .countdown .title {
    font-weight: bold;
    font-size: 16px;
    color: #ffffff;
}
.g-item-warp .countdown .countdown-time {
    display: flex;
    align-items: center;
    gap: 6px;
    white-space: nowrap;
}
.g-item-warp .titleBox {
    display: flex;
    align-items: center;
    gap: 5px;
    margin-top: 11px;
    margin-bottom: 12px;
    width: 100%;
}
.g-item-warp .titleBox .tag {
    font-weight: 500;
    font-size: 12px;
    color: var(--general);
    min-width: 40px;
    height: 20px;
    background: var(--ump-tag-bg);
    border-radius: 4px;
    line-height: 20px;
    text-align: center;
    flex-shrink: 0;
    padding: 0 5px;
    box-sizing: border-box;
}
.g-item-warp .titleBox .title {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.ad-goods_name_row__2 .g-item-warp .titleBox .title {
    white-space: normal;
    display: -webkit-box;
    line-clamp: 2;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

/* 让“人团”标签保持原来有背景的视觉（但布局使用 product-name-text::before） */
.g-item-warp .titleBox .product-name-text::before {
    color: var(--general) !important;
    background: var(--ump-tag-bg);
    // border: 0 !important;
    border-radius: 4px;
    min-width: 40px;
    height: 20px;
    line-height: 16px !important;
    padding: 0 5px;
    box-sizing: border-box;
    text-align: center;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-weight: 500;
    font-size: 12px;
    margin-right: 5px;
    margin-top: 0;
    vertical-align: middle;
}

.goods-ad-item .priceBox,
.mod_groupon .groupon_list .proditem .priceBox {
    width: 100%;
    height: 35px;
    background: #fff2f3;
    border-radius: 3px;
    margin-top: 5px;
    display: flex;
    align-items: center;
    position: relative;
}
.goods-ad-item .priceBox:before,
.mod_groupon .groupon_list .proditem .priceBox:before {
    content: "";
    position: absolute;
    left: -23px;
    top: -11px;
    width: calc(100% + 46px);
    height: 1px;
    background: #f6f6f6;
}
.goods-ad-item .priceBox .priceBoxNum,
.mod_groupon .groupon_list .proditem .priceBox .priceBoxNum {
    display: flex;
    align-items: flex-end;
}
.goods-ad-item .priceBox .priceBoxNum .old,
.mod_groupon .groupon_list .proditem .priceBox .priceBoxNum .old {
    margin-left: 7px;
    display: flex;
    align-items: flex-end;
    font-weight: 500;
    font-size: 12px;
    color: #9a9a9a;
    text-decoration-line: line-through;
}
.goods-ad-item .priceBox .priceBoxNum :deep(.minGrouponPrice) {
    font-weight: 500;
    font-size: 22px;
    padding-left: 9px;
    height: 26px;
    color: var(--price);
}
.goods-ad-item .priceBox .text,
.mod_groupon .groupon_list .proditem .priceBox .text {
    padding: 0 20px;
    height: 30px;
    background: #ee0a25;
    border-radius: 3px;
    font-weight: 600;
    font-size: 20px;
    color: #ffffff;
    line-height: 30px;
    margin-left: auto;
    clip-path: polygon(20% 0, 100% 0, 100% 100%, 0 100%);
    text-align: center;
}

/* <= 750px：缩小字号/间距，避免窄卡片内容挤压（仅影响小屏预览） */
@media (max-width: 1920px) {
    .g-item-warp .countdown {
        padding: 0 8px;
    }
    .g-item-warp .countdown .title {
        font-size: 10px;
        white-space: nowrap;
    }
    .g-item-warp .countdown .countdown-time {
        gap: 4px;
    }
    .g-item-warp .countdown .countdown-time .time,
    .g-item-warp .countdown .countdown-time .text {
        font-size: 8px;
        line-height: 12px;
    }

    .goods-ad-item .priceBox .priceBoxNum :deep(.minGrouponPrice) {
        font-size: 16px;
    }
    .goods-ad-item .priceBox .text,
    .mod_groupon .groupon_list .proditem .priceBox .text {
        padding: 0 12px;
        font-size: 14px;
    }
}
.goods-ad-item .priceBox .priceBoxNum :deep(.minGrouponPrice) {
    .price-row {
        align-items: flex-end !important;
    }
}
.goods-ad-warp .goods-ad-item .groupon-item-image {
    width: 100%;
    display: flex;
    justify-content: center;
    overflow: hidden;
    background-color: transparent;
    aspect-ratio: 1 / 1; // 设置固定宽高比，确保所有商品卡片高度一致
}
.goods-ad-warp .goods-ad-item .groupon-item-image .pro_pic {
    width: 100%;
    height: 100%;
    object-fit: cover; // 确保图片填满容器
    transition:
        transform 0.4s ease,
        -webkit-transform 0.4s ease,
        -moz-transform 0.4s ease,
        -o-transform 0.4s ease;
}
.mod_groupon .groupon_list .pro_pic {
    width: 100%;
    height: 100%;
    object-fit: contain;
}
.goods-ad-warp .goods-ad-item .item-info .count-down {
    font-size: 12px;
    font-weight: normal;
    clear: both;
}
.goods-ad-warp .goods-ad-item .item-info .count-down i {
    font-size: 18px;
    padding-right: 5px;
}
.goods-ad-warp .goods-ad-item .item-info .count-down em {
    font-size: 16px;
    font-weight: normal;
    color: var(--general);
    padding: 0 2px;
}
.goods-ad-warp .goods-ad-item .groupon-item__name {
    height: auto;
    min-height: 20px;
    margin-top: 10px;
    margin-bottom: 12px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    color: #1a1a1a;
    font-size: 16px;
    font-weight: 400;
    line-height: 18px;
    cursor: pointer;
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    gap: 4px;
    .group-num {
        flex-shrink: 0;
        display: inline-flex;
        align-items: baseline;
        margin-right: 4px;
        .group-num-text {
            color: var(--main-bg) !important;
            background-color: transparent;
            border: 1px solid var(--main-bg);
            padding: 2px 4.5px;
            border-radius: 2px;
            font-size: 12px;
            line-height: 12px;
            font-weight: normal;
            white-space: nowrap;
            box-sizing: border-box;
            display: inline-flex;
            margin-top: -3px;
        }
    }
    .product-name-text {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        min-width: 0;
        position: relative;
        &::before {
            content: attr(data-group-label);
            display: inline-block;
            color: var(--main-bg) !important;
            background-color: transparent;
            border: 1px solid var(--main-bg);
            padding: 2px 4.5px;
            border-radius: 2px;
            font-size: 12px;
            line-height: 12px;
            font-weight: normal;
            white-space: nowrap;
            box-sizing: border-box;
            margin-right: 4px;
            margin-top: -3px;
            vertical-align: middle;
            flex-shrink: 0;
        }
    }
}
.ad-goods_name_row__2 .goods-ad-warp .goods-ad-item .groupon-item__name {
    .product-name-text {
        white-space: normal;
        display: -webkit-box;
        display: -moz-box;
        -webkit-line-clamp: 2;
        -moz-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        -moz-box-orient: vertical;
        line-height: 24px;
        height: 48px;
        overflow: hidden;
        text-overflow: ellipsis;
        word-break: break-word;
        position: relative;
        padding-left: 0;
        text-indent: 0;
        &::before {
            content: attr(data-group-label);
            display: inline-block;
            color: var(--main-bg) !important;
            background-color: transparent;
            border: 1px solid var(--main-bg);
            padding: 2px 4.5px;
            border-radius: 2px;
            font-size: 12px;
            line-height: 12px;
            font-weight: normal;
            white-space: nowrap;
            box-sizing: border-box;
            margin-right: 4px;
            margin-top: -3px;
            vertical-align: middle;
            flex-shrink: 0;
        }
    }
}
.goods-ad-warp .goods-ad-item .item-info .item-name .item-name-a .item-name-link {
    color: #2a3145;
    text-decoration: none;
}
.goods-ad-warp .goods-ad-item .item-info .item-name .item-brief {
    line-height: 20px;
    height: 20px;
    overflow: hidden;
    display: block;
    color: #aaa;
    overflow: hidden;
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
.goods-ad-warp .goods-ad-item .groupon-item__price {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    // padding: 0 14px;
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
            color: var(--main-bg);
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
}
.goods-ad-warp .goods-ad-item .groupon-item__price + .price-origin {
    align-items: baseline;
    justify-content: center;
    height: 19px;
    color: #888b94;
    text-decoration: line-through;
    margin-top: 4px;
    :deep(.price-row) {
        line-height: 22px;
        font-weight: 500;
    }
    :deep(.num) {
        font-size: 16px;
        color: #888b94;
    }

    :deep(.util) {
        color: #888b94;
    }
}
.ad-style__1 .goods-ad-warp .goods-ad-item .item-info .item-name:not(.groupon-item__name) .item-name-a {
    font-size: 14px;
}
.ad-style__2 .goods-ad-warp .goods-ad-item .item-info .item-name:not(.groupon-item__name) .item-name-a,
.ad-style__6 .goods-ad-warp .goods-ad-item .item-info .item-name:not(.groupon-item__name) .item-name-a {
    font-size: 12px;
    font-weight: bold;
}
.ad-style__2 .goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: wrap;
}
/* .ad-style__2 的宽度由内联样式动态计算，一行4个，每个25% */
.ad-style__3 .goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: wrap;
}
/* .ad-style__3 的宽度由内联样式动态计算，一行5个，每个20% */
.ad-style__3 .goods-ad-warp .goods-ad-item .item-info .item-buy {
    display: none;
}
.ad-style__4 .goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: wrap;
}
.ad-style__4 .goods-ad-warp .goods-ad-con .goods-ad-item {
    width: 100%;
}
.ad-style__4 .goods-ad-warp .goods-ad-con .goods-ad-item:nth-child(3n + 2) {
    width: 50%;
}
.ad-style__4 .goods-ad-warp .goods-ad-con .goods-ad-item:nth-child(3n + 3) {
    width: 50%;
}
.ad-style__6 .goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: wrap;
}
.ad-style__6 .goods-ad-warp .goods-ad-con .goods-ad-item {
    width: 100%;
}
.ad-style__6 .goods-ad-warp .goods-ad-item .item-content {
    display: flex;
    flex-wrap: nowrap;
}
.ad-style__6 .goods-ad-warp .goods-ad-item .item-content .item-con {
    display: flex;
    flex-wrap: nowrap;
    flex: 1;
}
.ad-style__6 .goods-ad-warp .goods-ad-item .item-photo {
    width: 40%;
}
.ad-style__6 .goods-ad-warp .goods-ad-item .item-info {
    width: 60%;
    text-align: left;
}
.ad-style__6 .goods-ad-warp .goods-ad-item .item-action {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding-bottom: 0;
}
.ad-style__6 .goods-ad-warp {
    overflow: hidden;
}
.ad-style__6 .goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: nowrap;
    overflow-x: scroll;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none; /* Firefox */
    -ms-overflow-style: none; /* IE and Edge */

    &::-webkit-scrollbar {
        display: none; /* Chrome, Safari, Opera */
    }
}
.ad-style__6 .goods-ad-warp .goods-ad-con .goods-ad-item {
    width: 40%;
    flex: none;
}
.ad-style__7 .goods-ad-warp {
    overflow: hidden;
    padding-bottom: 25px;
}
.ad-style__7 .goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: nowrap;
}
.ad-style__7 .goods-ad-warp .goods-ad-con {
    display: block;
    flex-wrap: nowrap;
    padding: 0 !important;
    height: 100%;
}
.ad-style__7 .goods-ad-warp swiper {
    height: 200px;
    position: relative;
    overflow: hidden;
}
.ad-style__7 .goods-ad-warp .goods-ad-con .goods-ad-item {
    width: 33.333%;
}
.ad-style__7 .goods-ad-warp .goods-ad-item .item-info .item-buy {
    display: none;
}
.ad-style__7 .goods-ad-warp .swiper-pagination-con {
    position: absolute;
    bottom: 15px;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
}
.ad-style__7 .goods-ad-warp .swiper-pagination {
    display: inline-block;
    width: 12px;
    border-radius: 0;
    height: 2px;
    margin: 0 1px;
    background: #000;
    opacity: 0.2;
}
.ad-style__7 .goods-ad-warp .swiper-pagination.active {
    opacity: 1;
    background-color: #fff;
}
/* 商品卡样式 - 投影 (style 2) */
.ad-goods_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item {
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}
.ad-goods_style__2 .groupon_list .proditem {
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

/* 商品卡样式 - 描边 (style 3) */
.ad-goods_style__3 .goods-ad-warp .goods-ad-con .goods-ad-item {
    border: 1px solid rgba(50, 50, 51, 0.1);
}
.ad-goods_style__3 .groupon_list .proditem {
    border: 1px solid rgba(50, 50, 51, 0.1);
}

/* 商品卡样式 - 透明底 (style 4) */
.ad-goods_style__4 .goods-ad-warp .goods-ad-item {
    background-color: transparent !important;
}
.ad-goods_style__4 .groupon_list .proditem {
    background-color: transparent !important;
}
.ad-goods_style__4 .ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item {
    background-color: transparent !important;
}
.ad-goods_style__4 .ad-goods_radio_style__2 .groupon_list .proditem {
    background-color: transparent !important;
}
.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item {
    background-color: #ffffff;
    border-radius: 6px;
    overflow: hidden;
}
.ad-goods_style__4.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item {
    background-color: transparent !important;
}
.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .groupon-item-image-wrapper {
    background-color: transparent;
}
.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .groupon-item-image {
    border-radius: 6px 6px 0 0;
    overflow: hidden;
}
.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .groupon-item__name,
.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .groupon-item__price,
.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .price-origin {
    background-color: #ffffff;
}
.ad-goods_style__4.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .groupon-item__name,
.ad-goods_style__4.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .groupon-item__price,
.ad-goods_style__4.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .price-origin {
    background-color: transparent !important;
}
.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .groupon-item__price,
.ad-goods_radio_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .price-origin {
    border-radius: 0 0 6px 6px;
}
/* 横向滚动布局（style 6）的圆角配置 */
.ad-goods_radio_style__2 .groupon_list .proditem {
    background-color: #ffffff;
    border-radius: 6px;
    overflow: hidden;
}
.ad-goods_style__4.ad-goods_radio_style__2 .groupon_list .proditem {
    background-color: transparent !important;
}
.ad-goods_radio_style__2 .groupon_list .proditem .groupon-item-image-wrapper {
    background-color: transparent;
}
.ad-goods_radio_style__2 .groupon_list .proditem .groupon-item-image {
    border-radius: 6px 6px 0 0;
    overflow: hidden;
}
.ad-goods_radio_style__2 .groupon_list .proditem .groupon-item__name {
    background-color: #ffffff;
}
.ad-goods_style__4.ad-goods_radio_style__2 .groupon_list .proditem .groupon-item__name {
    background-color: transparent !important;
}
.ad-goods_radio_style__2 .groupon_list .proditem .groupon-item__price {
    background-color: #ffffff;
    border-radius: 0 0 6px 6px;
}
.ad-goods_style__4.ad-goods_radio_style__2 .groupon_list .proditem .groupon-item__price {
    background-color: transparent !important;
}
.ad-goods_radio_style__2 .groupon_list .proditem .price-origin {
    background-color: #ffffff;
    border-radius: 0 0 6px 6px;
}
.ad-goods_style__4.ad-goods_radio_style__2 .groupon_list .proditem .price-origin {
    background-color: transparent !important;
}
.ad-text_align__2 .goods-ad-warp .goods-ad-con .goods-ad-item {
    text-align: center;
}
.ad-text_align__2 .goods-ad-warp .goods-ad-item .item-info .item-buy {
    height: auto;
    padding-bottom: 15px;
}
.ad-text_align__2 .goods-ad-warp .goods-ad-item .item-info .item-action {
    flex-wrap: wrap;
    justify-content: normal;
}
.ad-text_align__2 .goods-ad-warp .goods-ad-item .item-info .item-action .item-price {
    width: 100%;
}
.ad-text_align__2 .goods-ad-warp .goods-ad-item .item-info .item-action .item-buy {
    width: 100%;
}
/* 居中对齐时的价格和购买按钮样式 - flex布局 */
.ad-text_align__2 .goods-ad-warp .goods-ad-item .groupon-item__price {
    align-items: center;
    justify-content: center;
}
.ad-text_align__2 .goods-ad-warp .goods-ad-item .groupon-item__price .buy {
    margin-left: 0;
    margin-top: 8px;
}
/* 居中对齐时的价格和购买按钮样式 - swiper布局 */
.ad-text_align__2 .groupon_list .proditem .groupon-item__price {
    align-items: center;
    justify-content: center;
}
.ad-text_align__2 .groupon_list .proditem .groupon-item__price .buy {
    margin-left: 0;
    margin-top: 8px;
}
.ad-text_weight__2 .goods-ad-warp .goods-ad-item .item-info .item-name .item-name-a {
    font-weight: bold;
}
.ad-goods_name_row__1 .goods-ad-warp .goods-ad-item .item-info .item-name .item-name-a {
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
.ad-goods_name_row__2 .goods-ad-warp .goods-ad-item .item-info .item-name .item-name-a {
    line-height: 20px;
    height: 40px;
    text-overflow: ellipsis;
    display: -webkit-box;
    display: -moz-box;
    -webkit-line-clamp: 2;
    -moz-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    -moz-box-orient: vertical;
    overflow: hidden;
}
.ad-goods_name_padding__0 .goods-ad-warp .goods-ad-item .groupon-item__name {
    margin-left: 0;
    margin-right: 0;
}
.ad-goods_name_padding__0 .goods-ad-warp .goods-ad-item .groupon-item__price {
    padding-left: 0;
    padding-right: 0;
}
.ad-goods_name_padding__1 .goods-ad-warp .goods-ad-item .groupon-item__name {
    margin-left: 10px;
    margin-right: 10px;
}
.ad-goods_name_padding__1 .goods-ad-warp .goods-ad-item .groupon-item__price {
    padding-left: 10px;
    padding-right: 10px;
}
.ad-goods_name_padding__1 .goods-ad-warp .goods-ad-item .price-origin {
    padding-left: 1px;
    // padding-right: 10px;
}
.ad-goods_name_padding__0 .groupon_list .groupon-item__name {
    margin-left: 0 !important;
    margin-right: 0 !important;
}
.ad-goods_name_padding__0 .groupon_list .groupon-item__price {
    padding-left: 0 !important;
    padding-right: 0 !important;
}
.ad-goods_name_padding__0 .groupon_list .price-origin {
    padding-left: 0 !important;
    padding-right: 0 !important;
}
.ad-goods_name_padding__1 .groupon_list .groupon-item__name {
    margin-left: 10px !important;
    margin-right: 10px !important;
}
.ad-goods_name_padding__1 .groupon_list .groupon-item__price {
    padding-left: 10px !important;
    padding-right: 10px !important;
}
.ad-goods_name_padding__1 .groupon_list .price-origin {
    padding-left: 10px !important;
    padding-right: 10px !important;
}
.ad-goods-title_style__3 .image-ad-title {
    padding-bottom: 28px !important;
}
.ad-goods-title_style__3 .goods-ad-warp {
    border-top-left-radius: 15px;
    margin-top: -25px !important;
    overflow: hidden;
    background: #fff;
    padding-top: 10px;
}

/* (buyBtnStyle 已移除：后台预览不再提供/依赖购买按钮样式变体) */

/*拼团*/
.module-seckill-goods_ad .cap-seckill-goods__tag {
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
}
.module-seckill-goods_ad .cap-seckill-goods__tag .cap-seckill-goods__tag-title {
    font-weight: 700;
    font-size: 20px;
    float: left;
}
.module-seckill-goods_ad .cap-seckill-goods__tag .cap-seckill-goods__countdown rich-text {
    font-weight: bold;
}
.module-seckill-goods_ad .cap-seckill-goods__tag .cap-seckill-goods__countdown rich-text {
    font-size: 12px;
    text-align: center;
}
.module-seckill-goods_ad .cap-seckill-goods__tag.big {
    height: 40px;
    line-height: 40px;
}
.module-seckill-goods_ad .cap-seckill-goods__tag.big .cap-seckill-goods__tag-countdown {
    float: right;
    height: 40px;
    font-size: 12px;
    line-height: 16px;
}
.module-seckill-goods_ad .cap-seckill-goods__tag.big .cap-seckill-goods__tag-countdown-text {
    display: block;
    height: 16px;
    margin: 5px 0 0;
    line-height: 16px;
    text-align: left;
    opacity: 0.6;
}
.module-seckill-goods_ad .cap-seckill-goods__tag.big .cap-seckill-goods__countdown {
    margin-top: 0;
}
.module-seckill-goods_ad .cap-seckill-goods__tag.big .cap-seckill-goods__countdown text {
    padding: 0 2px;
}
.module-seckill-goods_ad .cap-seckill-goods__tag.list .cap-seckill-goods__tag-countdown,
.module-seckill-goods_ad .cap-seckill-goods__tag.small .cap-seckill-goods__tag-countdown {
    font-size: 12px;
    display: -webkit-box;
    display: -webkit-flex;
    display: flex;
    white-space: nowrap;
    -webkit-box-pack: justify;
    -webkit-justify-content: space-between;
    justify-content: space-between;
    height: auto;
}
.module-seckill-goods_ad .cap-seckill-goods__tag.list,
.module-seckill-goods_ad .cap-seckill-goods__tag.small {
    height: auto;
    padding: 5px 10px;
}
.module-seckill-goods_ad .cap-seckill-goods__tag.list .cap-seckill-goods__tag-countdown-text,
.module-seckill-goods_ad .cap-seckill-goods__tag.small .cap-seckill-goods__tag-countdown-text {
    display: block;
    height: 24px;
    line-height: 24px;
}

.item-info .item-name {
    .group-num {
        flex-shrink: 0;
        float: left;
        display: inline-flex;
        align-items: baseline;
        margin-right: 5px;
        padding-top: 1.5px;
    }

    .group-num-text {
        color: var(--main-bg) !important;
        background-color: transparent;
        border: 1px solid var(--main-bg);
        padding: 2px 4.5px;
        border-radius: 2px;
        font-size: 12px;
        line-height: 12px;
        font-weight: normal;
        white-space: nowrap;
        box-sizing: border-box;
        display: inline-flex;
    }
}

.product-desc {
    margin-top: 2px;
}

.source_price {
    color: #ccc;
    font-size: 12px !important;
    margin-left: 4px;
    text-decoration: line-through;
    margin-top: 5px;
}

.ad-style__1 {
    .countdown {
        position: relative;
        display: flex;
        flex-direction: column;
        line-height: 15px;
        align-items: flex-start;

        .countdown__title {
            font-size: 12px;
            color: rgb(229 229 229 / 70%);
            padding-left: 3.5px;
        }
    }
}

.ad-style__2,
.ad-style__6 {
    .cap-seckill-goods__tag.list {
        padding: 0 5px;
    }

    .countdown {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: space-between;
        flex: 1;
        white-space: nowrap;
        margin: 0;

        .countdown__title {
            font-size: 11px;
            color: rgb(229 229 229 / 70%);
        }
    }
}

.item-action {
    margin-top: 5px;
}

.ad-style__6 .goods-ad-item .item-content {
    min-height: 150px;
}

/* 占位图片样式 */
.empty-groupon {
    padding-top: 30px;
    padding-bottom: 50px;
    text-align: center;

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
