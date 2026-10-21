<template>
    <div
        :class="
            'module-ad-con module-goods_ad module-seckill-goods_ad ad-style__' +
            module?.style +
            ' ad-buyBtnStyle__' +
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
        <div
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
            <CommonTitle v-if="title?.showTitle" v-model="title"></CommonTitle>
            <div class="goods-ad-warp" style="">
                <div class="goods-ad-con">
                    <div class="goods-ad-item" v-for="item in grouponItemList" :key="item.grouponPromotionId">
                        <div class="item-content" :style="allFormat.goodsPadding">
                            <div class="item-con">
                                <div class="item-photo">
                                    <a href="" title="" class="item-image-a"><img @error="imageError" :src="productImage(item)" alt="" /></a>
                                    <div :class="'cap-seckill-goods__tag ' + className" v-if="module.style < 3 || module.style === 5">
                                        <span class="cap-seckill-goods__tag-title" v-if="module?.style === 1"> {{ $t('拼团') }} </span>
                                        <!-- <div class="countdown">
                                            <div class="countdown__title">{{ distanceTimeText }}</div>
                                            <el-countdown title="" format="DD[天] HH:mm:ss"
                                                :value="dateStringToTimestamp(module.grouponActivityItem.startTime)"
                                                :value-style="{ color: '#fff', fontSize: '12px', 'font-weight': '700' }" />
                                        </div> -->
                                    </div>
                                </div>
                                <div class="item-info groupon">
                                    <div class="item-name-a">
                                        <div v-if="module?.showName" href="" title="" class="groupon-product-name">
                                                <span class="group-num">
                                                    <span class="group-num-text">{{ $t('{getMinGroupNum}人团', { getMinGroupNum: getMinGroupNum(item) || 0 }) }}</span>
                                                </span>
                                                 {{ item.productName }}
                                            </div>
                                    </div>
                                    <div v-if="(module.style === 1 || module.style === 5) && item.productBrief" class="item-name">
                                        <div class="item-name-a product-desc">{{ item.productBrief }}</div>
                                    </div>
                                    <div class="item-action" v-if="module?.showPrice">
                                        <div class="item-price">
                                            <b class="price_format" :style="[{color: isOverseas() ? '#555' : ''}]">
                                                {{ priceFormat(item.minGroupPrice) }}
                                            </b>
                                            <b v-if="module.style == 1 || module.style == 5" class="price_format source_price" :style="[{color: isOverseas() ? '#555' : ''}]">
                                                {{ priceFormat(item.productPrice) }}
                                            </b>
                                        </div>
                                        <div class="item-buy">
                                            <a href="javascript:;" class="buy-btn lyecs-buyBtn" style="color: #555">
                                                <template v-if="module.buyBtnStyle === 5 || module.buyBtnStyle === 6">
                                                    <span :class="[{'activeBtnStyle': module.buyBtnStyle === 6},]">{{ $t('购买') }}</span>
                                                </template>
                                                <template v-if="module.buyBtnStyle === 7 || module.buyBtnStyle === 8">
                                                    <span :class="[{'activeBtnStyle': module.buyBtnStyle === 8}]">{{ $t('马上抢') }}</span>
                                                </template>
                                                <template v-if="module.buyBtnStyle === 1">
                                                    <span class="iconfont-pc icon-gouwuche iconh5-style"></span>
                                                </template>
                                                <template v-if="module.buyBtnStyle === 2">
                                                    <span class="iconfont-pc icon-gouwuche1 iconh5-style"></span>
                                                </template>
                                                <template v-if="module.buyBtnStyle === 3">
                                                    <span class="iconfont-pc icon-jia iconh5-style"></span>
                                                </template>
                                                <template v-if="module.buyBtnStyle === 4">
                                                    <span class="iconfont-pc icon-jia1 iconh5-style"></span>
                                                </template>
                                            </a>
                                        </div>
                                    </div>

                                    <div class="blank" style="height: 10px"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="swiper-pagination-con">
                    <div class="swiper-pagination"></div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref, onMounted, watchEffect } from "vue";
import { priceFormat, productImage } from "@/utils/format";
import { ModuleActivityType, ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultTitle, CommonTitle } from "@/views/decorate/decorate/src/modules/";
import emptyImg from "@/views/decorate/decorate/src/img/empty-img-bg3.png";
import { getGrouponItemList } from "@/api/promotion/groupon";
import type { GrouponItemFilterParams } from "@/types/promotion/groupon.d";
import { isOverseas } from "@/utils/version";
// 在modules加入要使用的模块
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & ModuleActivityType>("module") as Ref<ModuleType & ModuleActivityType>;
const time = ref(Date.now() + 1000 * 60 * 60 * 7);
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
    innerPadding: 0,
    boxPadding: 10,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    // 边距由顶层 boxPadding* 控制，frame 内边距置 0，避免与外层叠成双倍空隙
    frame: {
        ...defaultFrame,
        boxPadding: 0,
        boxPaddingTop: 0,
        boxPaddingBottom: 0
    },
    title: defaultTitle,
    groupType: 1,
    grouponActivityItem: undefined
});
mergeDefaultModule(module.value, defaultModule.value);

if (module.value.grouponActivityItem) {
    module.value.groupType = module.value.grouponActivityItem.groupType;
}

const { frame, title } = module.value;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const allFormat = computed(() => {
    return {
        backgroundColor: `background-color: ${module?.value?.backgroundColor};`,
        boxPadding: `margin-left: ${module?.value?.boxPadding}px;margin-right: ${module?.value?.boxPadding}px;`,
        boxPaddingBottom: `margin-bottom: ${module?.value?.boxPaddingBottom}px;`,
        boxPaddingTop: `margin-top: ${module?.value?.boxPaddingTop}px;`,
        boxRadius: `border-radius: ${module?.value?.boxRadius}px;`,
        innerPadding: `padding: ${module?.value?.innerPadding}px;`,
        goodsPadding: `padding: ${module?.value?.goodsPadding}px;`
    };
});

const className = computed(() => {
    switch (module?.value?.style) {
        case 1:
            return "big";
        case 2:
            return "small flex-end";
        case 5:
            return "list flex-end";
        case 6:
            return "small flex-end";
    }
});

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

const distanceTimeText = computed(() => {
    // if(module.value.grouponActivityItem.)
    return module.value.style === 1 ? t('距离开始仅剩') : t('距开始');
});

const grouponItemList = ref<any>([]);

const defaultGrouponItems = [
    {
        grouponPromotionItemId: 0,
        productId: 0,
        groupNum: 2,
        sort: 1,
        productName: t('PP折叠盆'),
        productSn: "",
        picLarge: "https://oss.tigshop.com/img/gallery/202510/1761024185GjoiVoxyZCFBgz02sj.jpeg?x-oss-process=image/resize,m_pad,h_800,h_800",
        productPrice: "15.80",
        minGroupPrice: "8.8",
        maxGroupPrice: "12.8",
        minGroupNum: 2,
        maxGroupNum: 5,
        minLimitNum: 10,
        maxLimitNum: 12
    },
    {
        grouponPromotionItemId: 0,
        productId: 0,
        groupNum: 2,
        sort: 1,
        productName: t('曲奇饼饼'),
        productSn: "",
        picLarge: "https://oss.tigshop.com/img/gallery/202510/17605171914YIcsTf34FQjP0qq3r.jpeg?x-oss-process=image/resize,m_pad,h_800,h_800",
        productPrice: "30.00",
        minGroupPrice: "18.8",
        maxGroupPrice: "26.8",
        minGroupNum: 3,
        maxGroupNum: 8,
        minLimitNum: 10,
        maxLimitNum: 12,
        productBrief: ""
    },
    {
        grouponPromotionItemId: 0,
        productId: 0,
        groupNum: 2,
        sort: 1,
        productName: t('脚踏车'),
        productSn: "",
        picLarge: "https://oss.tigshop.com/img/gallery/202508/1755141476MeZ9q6HfncgkfbysBG.jpeg?x-oss-process=image/resize,m_pad,h_800,h_800",
        productPrice: "30.00",
        minGroupPrice: "15.8",
        maxGroupPrice: "22.8",
        minGroupNum: 2,
        maxGroupNum: 6,
        minLimitNum: 10,
        maxLimitNum: 12,
        productBrief: ""
    },
    {
        grouponPromotionItemId: 0,
        productId: 0,
        groupNum: 2,
        sort: 1,
        productName: t('传统阿胶糕1000g东阿原产即食胶原蛋白阿胶片营养品'),
        productSn: "",
        picLarge: "https://oss.tigshop.com/img/item/demo/1682046308g5LOOn4OKbSHNNn7kT!!pic800x800.jpeg",
        productPrice: "93.00",
        minGroupPrice: "48.00",
        maxGroupPrice: "68.8",
        minGroupNum: 4,
        maxGroupNum: 10,
        minLimitNum: 10,
        maxLimitNum: 12,
        productBrief: ""
    }
];

const processData = (size: number) => {
    if (grouponItemList.value < size) {
        // 根据缺少的，补充进去
        let index = 0;
        for (let i = grouponItemList.value.length; i < size; i++) {
            grouponItemList.value.push(defaultGrouponItems[index]);
            index++;
        }
    }
};

const getData = async () => {
    let filterParams: GrouponItemFilterParams = {
        grouponPromotionId: 0,
        page: 1,
        size: 1
    };
    // 根据模式设置显示的参数
    if (module.value.style < 5) {
        filterParams.size = Number(module.value.style);
    } else if (module.value.style === 5) {
        // 大图模式
        filterParams.size = 1;
    }
    // 如果没有选中的活动
    if (!module.value.grouponActivityItem || !module.value.grouponActivityItem.grouponPromotionId) {
        grouponItemList.value.length = 0;
        processData(filterParams.size);
        return;
    }
    filterParams.grouponPromotionId = module.value.grouponActivityItem.grouponPromotionId;
    try {
        // 拿到商品信息
        const result = await getGrouponItemList(filterParams);
        // console.log('获取到的数据', result);
        grouponItemList.value = result.records;
        processData(filterParams.size);
    } catch (error) {
        console.error(error);
    }
};

watchEffect(() => {
    getData();
});

/*转时间搓 */
const dateStringToTimestamp = (dateString: any) => {
    const dateParts = dateString.split(" ");
    const [year, month, day] = dateParts[0].split("-");
    const [hour, minute, second] = dateParts[1].split(":");
    const date = new Date(year, month - 1, day, hour, minute, second);
    return date.getTime();
};

const imageError = (e: any) => {
    e.target.src = emptyImg;
};

onMounted(() => {
    getData();
});
</script>

<style lang="less" scoped>
.cap-seckill-goods__tag {
    display: flex;
    align-items: center;
    justify-content: space-between;

    &.flex-end {
        justify-content: flex-end;
    }
}

.item-name {
    display: flex;
}

.iconh5-style {
    color: #555;
    font-size: 18px;
    margin-right: 5px;
}

.groupon-product-name {
    color: #555;
    overflow: hidden;
}

.goods-ad-warp .goods-ad-item .item-photo {
    background: #ffffff;
    text-align: center;
    position: relative;
    width: 100%;
    aspect-ratio: 1 / 1; /* 宽高比 1:1，高度跟随宽度 */
    overflow: hidden;
}

.ad-style__5 .goods-ad-warp .goods-ad-item .item-photo{
    width: 30%;
}

.goods-ad-warp .goods-ad-item .item-photo .item-image-a {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
}

.goods-ad-warp .goods-ad-item .item-photo img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.item-info .group-num {
    flex-shrink: 0;
    display: inline-flex;
    align-items: baseline;

    .group-num-text {
        color: #f23030 !important;
        background-color: #fff2f2;
        padding: 0 5px;
        border-radius: 30px;
        font-size: 12px;
    }
}

.product-desc {
    color: #9fa0a1 !important;
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
        }
    }
}

.ad-style__2,
.ad-style__5 {
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

.ad-style__5 .goods-ad-warp .goods-ad-item .item-content .item-con {
    gap: 10px;
}

.ad-goods_name_row__1 .groupon-product-name {
    line-height: 20px;
    height: 20px;
    text-overflow: ellipsis;
    display: -webkit-box;
    display: -moz-box;
    -webkit-line-clamp: 1;
    -moz-line-clamp: 1;
    -webkit-box-orient: vertical;
    -moz-box-orient: vertical;
}

.ad-goods_name_row__2 .groupon-product-name {
    line-height: 20px;
    height: 40px;
    text-overflow: ellipsis;
    display: -webkit-box;
    display: -moz-box;
    -webkit-line-clamp: 2;
    -moz-line-clamp: 2;
    -webkit-box-orient: vertical;
    -moz-box-orient: vertical;
}

.item-action {
    margin-top: 5px;
    padding: 0 !important;
}

.lyecs-buyBtn{
    span{
        color: var(--tig-red-text-color);
    }
    .activeBtnStyle{
        display: inline-block;
        background: var(--tig-red-text-color);
        padding: 3px 10px;
        font-size: 14px;
        color: #fff;
        border-radius: 10px;
        text-align: center;
        white-space: nowrap;
    }

}
</style>
