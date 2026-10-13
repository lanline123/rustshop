<template>
    <view
        v-if="grouponItemList && grouponItemList?.length > 0"
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
            module?.goodsNamePadding
        "
    >
        <view
            class="module-ad-content"
            :style="
                frameFormat.backgroundColor +
                ' ' +
                frameFormat.boxRadius +
                allFormat.backgroundColor +
                allFormat.boxPadding +
                allFormat.boxPaddingBottom +
                allFormat.boxPaddingTop +
                allFormat.boxRadius +
                allFormat.innerPadding
            "
        >
            <commonTitle v-if="module.title.showTitle == 1" :module="module.title" />
            <view class="goods-ad-warp">
                <view class="goods-ad-con">
                    <block v-for="item in grouponItemList" :key="item.productId">
                        <template v-if="!isActivityEnd(item)">
                            <view class="goods-ad-item">
                                <view class="item-content" :style="allFormat.goodsPadding">
                                    <view class="item-con">
                                        <view class="item-photo">
                                            <navigator :url="'/pages/product/index?id=' + item.productId" class="item-image-a">
                                                <tig-image :src="pickPic(item)" class="item-image" mode="widthFix" />
                                            </navigator>
                                            <view v-if="module.style < 3 || module.style == 5" :class="'cap-seckill-goods__tag ' + className">
                                                <text v-if="module?.style == 1" class="cap-seckill-goods__tag-title"> {{ $t("拼团") }} </text>
                                                <!-- <view class="countdown">
                                                    <view class="countdown__title">{{ $t(getTimeText(item)) }}</view>
                                                    <tig-countdown
                                                        :end-time="isActivityStart(item) ? item.endTime : item.startTime" />
                                                </view> -->
                                            </view>
                                            <image
                                                v-if="styleMap[item.productId]?.borderImage"
                                                class="product-style-border"
                                                :src="styleMap[item.productId].borderImage"
                                                mode="scaleToFill"
                                            />
                                        </view>
                                        <view class="item-info">
                                            <view class="item-name">
                                                <block v-if="module.showName">
                                                    <view class="item-name-a line2">
                                                        <view class="group-num">
                                                            <text class="group-num-text">{{ item.maxGroupNum || 0 }}{{ $t("人团") }}</text>
                                                        </view>
                                                        <navigator :url="'/pages/product/index?id=' + item.productId">
                                                            {{ item.productName ?? "" }}
                                                        </navigator>
                                                    </view>
                                                </block>
                                                <view
                                                    v-if="(module.style == 1 || module.style == 5) && module.grouponActivityItem.productBrief"
                                                    class="item-brief product-desc"
                                                >
                                                    <text>{{ item.productBrief }} </text>
                                                </view>
                                            </view>
                                            <block v-if="module.showPrice">
                                                <view class="item-action">
                                                    <view class="item-price">
                                                        <format-price
                                                            :font-style="groupPirceStyle"
                                                            :currency-style="groupCurrencyStyle"
                                                            :decimals-style="groupCurrencyStyle"
                                                            :price-data="item.minGrouponPrice"
                                                        />
                                                        <format-price
                                                            v-if="module.style == 1 || module.style == 5"
                                                            style="margin-left: 8rpx"
                                                            :font-style="{ fontSize: '20rpx', color: '#ccc', 'text-decoration': 'line-through' }"
                                                            :currency-style="{
                                                                fontSize: '18rpx',
                                                                color: '#ccc',
                                                                'text-decoration': 'line-through'
                                                            }"
                                                            :decimals-style="{
                                                                fontSize: '18rpx',
                                                                color: '#ccc',
                                                                'text-decoration': 'line-through'
                                                            }"
                                                            :price-data="item.productPrice"
                                                        />
                                                    </view>
                                                    <view class="item-buy">
                                                        <view :data-id="item.productId" class="buy-btn" @click="buy(item.productId)">
                                                            <block v-if="module.buyBtnStyle == 5 || module.buyBtnStyle == 6">
                                                                <view>{{ $t("购买") }}</view>
                                                            </block>
                                                            <block v-else-if="module.buyBtnStyle == 7 || module.buyBtnStyle == 8">
                                                                <view>{{ $t("马上抢") }}</view>
                                                            </block>
                                                            <block v-else>
                                                                <view class="iconfont-h5" />
                                                            </block>
                                                        </view>
                                                    </view>
                                                </view>
                                            </block>
                                            <block v-else>
                                                <view class="blank" style="height: 20rpx" />
                                            </block>
                                        </view>
                                    </view>
                                </view>
                            </view>
                        </template>
                    </block>
                </view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, reactive } from "vue";
import { formatFrame } from "@/components/modules";
import { pickPic } from "@/utils/format";
import commonTitle from "@/pages/customPage/modules/commonTitle/index.vue";
import { getHomeGrouponList } from "@/api/home/home";
import type { GrouponItem } from "@/types/home/home";
import type { GrouponItemListParams } from "@/types/groupon/groupon";
import { useProductStyle } from "@/hooks/useProductStyle";
import { getMaxGroupNumFromJson } from "@/utils/groupon";
const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});

type GrouponListItem = GrouponItem & {
    maxGroupNum: number;
};

const grouponItemList = ref<GrouponListItem[]>([]);
const { styleMap } = useProductStyle(grouponItemList);
const { frame } = props.module;

const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});

const filterParams = reactive<GrouponItemListParams>({
    //初始化用于查询的参数
    page: 1,
    size: 20,
    sortField: "",
    keyword: "",
    sortOrder: "",
    grouponPromotionId: 0
});

const allFormat = computed(() => {
    return {
        backgroundColor: `background-color: ${props.module.backgroundColor};`,
        boxPadding: `margin-left: ${props.module.boxPadding}px;margin-right: ${props.module.boxPadding}px;`,
        boxPaddingBottom: `margin-bottom: ${props.module.boxPaddingBottom}px;`,
        boxPaddingTop: `margin-top: ${props.module.boxPaddingTop}px;`,
        boxRadius: `border-radius: ${props.module.boxRadius}px;`,
        innerPadding: `padding: ${props.module.innerPadding}px;`,
        goodsPadding: `padding: ${props.module.goodsPadding}px;`
    };
});
const className = computed(() => {
    switch (props.module.style) {
        case "1":
            return "big";
        case "2":
            return "small flex-end";
        case "5":
            return "list flex-end";
        case "6":
            return "small flex-end";
        default:
            return "";
    }
});

const groupPirceStyle = computed(() => {
    if (props.module.style != 3) {
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
    if (props.module.style != 3) {
        return {
            fontSize: "12px"
        };
    } else {
        return {
            fontSize: "11px"
        };
    }
});
/*转时间搓 */
const dateStringToTimestamp = (dateString: any) => {
    const dateParts = dateString.split(" ");
    const [year, month, day] = dateParts[0].split("-");
    const [hour, minute, second] = dateParts[1].split(":");
    const date = new Date(year, month - 1, day, hour, minute, second);
    return date.getTime();
};

/**
 * 团是否结束了
 */
const isActivityEnd = (item: GrouponItem) => {
    // 不是待成团的状态，拼团结束了
    const endTime = dateStringToTimestamp(item.endTime);
    return endTime < new Date().getTime();
};

const buy = (id: any) => {
    uni.navigateTo({ url: "/pages/product/index?id=" + id });
};

const getData = async () => {
    try {
        const result = await getHomeGrouponList(filterParams);
        // 过滤掉拼团活动结束的
        const records = result.records.filter((item: GrouponItem) => {
            return !isActivityEnd(item);
        });
        grouponItemList.value = records.map((item: GrouponItem) => ({
            ...item,
            maxGroupNum: getMaxGroupNumFromJson(item.groupNum)
        }));
    } catch (error) {
        console.error(error);
    }
};

onMounted(() => {
    if (props.module.grouponActivityItem) {
        filterParams.grouponPromotionId = props.module.grouponActivityItem.grouponPromotionId;
        // 根据模式设置显示的参数
        if (props.module.style < 5) {
            filterParams.size = Number(props.module.style);
        } else if (props.module.style === 5) {
            // 大图模式
            filterParams.size = 1;
        }
        getData();
    }
});
</script>
<style lang="scss" scoped>
.item-info .item-name {
    .group-num {
        flex-shrink: 0;
        float: left;
        display: inline-flex;
        align-items: baseline;
        margin-right: 10rpx;
        padding-top: 3rpx;
    }

    .group-num-text {
        color: #f23030 !important;
        background-color: #fff2f2;
        padding: 4rpx 9rpx;
        border-radius: 60rpx;
        font-size: 24rpx;
        line-height: 24rpx;
        font-weight: normal;
        white-space: nowrap;
        box-sizing: border-box;
        display: inline-flex;
    }
}

.goods-ad-warp .goods-ad-item .item-photo {
    background: #ffffff;
    text-align: center;
    position: relative;
    width: 100%;
    aspect-ratio: 1; /* 宽高比 1:1，高度跟随宽度 */
    overflow: hidden;
}

.goods-ad-warp .goods-ad-item .item-photo .item-image-a {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
}

.goods-ad-warp .goods-ad-item .item-photo {
    :deep(.base-image) {
        height: 100% !important;
        width: 100%;
        object-fit: cover;
    }
}

.product-style-border {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100% !important;
    z-index: 5;
    pointer-events: none;
}

.goods-ad-warp .goods-ad-item .item-photo .cap-seckill-goods__tag {
    z-index: 6;
}

.product-desc {
    margin-top: 4rpx;
}

.source_price {
    color: #ccc;
    font-size: 24rpx !important;
    margin-left: 8rpx;
    text-decoration: line-through;
    margin-top: 10rpx;
}

.ad-style__1 {
    .countdown {
        position: relative;
        display: flex;
        flex-direction: column;
        line-height: 30rpx;
        align-items: flex-start;

        .countdown__title {
            font-size: 24rpx;
            color: rgb(229 229 229 / 70%);
            padding-left: 7rpx;
        }
    }
}

.ad-style__2,
.ad-style__5 {
    .cap-seckill-goods__tag.list {
        padding: 0 10rpx;
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
            font-size: 22rpx;
            color: rgb(229 229 229 / 70%);
        }
    }
}

.item-action {
    margin-top: 10rpx;
}

.ad-style__5 .goods-ad-item .item-content {
    min-height: 300rpx;
}
</style>
