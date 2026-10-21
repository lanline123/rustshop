<template>
    <view
        v-if="seckillList && seckillList?.length > 0"
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
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom"
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
                    <block v-for="item in seckillList" :key="item.productId">
                        <view class="goods-ad-item">
                            <view class="item-content" :style="allFormat.goodsPadding">
                                <view class="item-con">
                                    <view class="item-photo">
                                        <navigator :url="'/pages/product/index?id=' + item.productId" class="item-image-a">
                                            <tig-image :src="pickPic(item)" :mode="module?.style == 6 ? 'aspectFill' : 'widthFix'" />
                                        </navigator>
                                        <view :class="'cap-seckill-goods__tag ' + className">
                                            <text v-if="module?.style == 1" class="cap-seckill-goods__tag-title"> {{ $t("秒杀") }} </text>
                                            <tig-countdown :end-time="item.seckkillData.seckillEndTime" />
                                        </view>
                                        <image
                                            v-if="styleMap[item.productId]?.borderImage"
                                            class="product-style-border"
                                            :src="styleMap[item.productId].borderImage"
                                            mode="scaleToFill"
                                        />
                                    </view>
                                    <view class="item-info">
                                        <block v-if="module.showName">
                                            <view class="item-name">
                                                <navigator :url="'/pages/product/index?id=' + item.productId" class="item-name-a">
                                                    {{ item.productName ?? "" }}
                                                </navigator>
                                            </view>
                                        </block>
                                        <block v-if="module.showPrice">
                                            <view class="item-action">
                                                <view class="item-price">
                                                    <format-price
                                                        :font-style="{ fontSize: '32rpx' }"
                                                        :currency-style="{
                                                            fontSize: '22rpx'
                                                        }"
                                                        :decimals-style="{
                                                            fontSize: '24rpx'
                                                        }"
                                                        :price-data="item.seckillPrice"
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
                    </block>
                </view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from "vue";
import { formatFrame } from "@/components/modules";
import { pickPic } from "@/utils/format";
import commonTitle from "@/pages/customPage/modules/commonTitle/index.vue";
import { getHomeSeckill } from "@/api/home/home";
import type { SeckillList } from "@/types/home/home";
import { useProductStyle } from "@/hooks/useProductStyle";
const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});

const { frame } = props.module;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
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
        case 1:
            return "big";
        case 2:
            return "small flex-end";
        case 5:
            return "list flex-end";
        case 6:
            return "small flex-end";
        default:
            return "";
    }
});

const buy = (id: any) => {
    uni.navigateTo({ url: "/pages/product/index?id=" + id });
};

const seckillList = ref<SeckillList[]>([]);
const { styleMap } = useProductStyle(seckillList);
const getData = async () => {
    try {
        const result = await getHomeSeckill();
        seckillList.value = result.records;
    } catch (error) {
        console.error(error);
    }
};

onMounted(() => {
    getData();
});
</script>
<style lang="scss" scoped>
.item-photo {
    position: relative;

    .item-image-a {
        display: block;
    }

    .cap-seckill-goods__tag {
        z-index: 6;
    }

    .product-style-border {
        position: absolute;
        left: 0;
        top: 0;
        width: 100% !important;
        height: 100% !important;
        z-index: 5;
        pointer-events: none;
    }
}
</style>
