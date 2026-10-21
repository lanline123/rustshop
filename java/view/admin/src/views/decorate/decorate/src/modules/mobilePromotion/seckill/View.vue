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
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom"
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
                    <div class="goods-ad-item" v-for="item in seckillList" :key="item.id">
                        <div class="item-content" :style="allFormat.goodsPadding">
                            <div class="item-con">
                                <div class="item-photo">
                                    <a href="" title="" class="item-image-a"><img @error="imageError" :src="productImage(item)" alt="" /></a>
                                    <div :class="'cap-seckill-goods__tag ' + className">
                                        <span class="cap-seckill-goods__tag-title" v-if="module?.style === 1"> {{ $t('秒杀') }} </span>
                                        <el-countdown
                                            title=""
                                            format="DD[天] HH:mm:ss"
                                            :value="dateStringToTimestamp(item.seckkillData.seckillEndTime)"
                                            :value-style="{ color: '#fff', fontSize: '12px', 'font-weight': '700' }"
                                        />
                                    </div>
                                </div>
                                <div class="item-info">
                                    <div class="item-name">
                                        <a v-if="module?.showName" href="" title="" class="item-name-a">{{ item.productName }}</a>
                                    </div>
                                    <div class="item-action" v-if="module?.showPrice">
                                        <div class="item-price">
                                            <b class="price_format">
                                                {{ priceFormat(item.productPrice) }}
                                            </b>
                                        </div>
                                        <div class="item-buy">
                                            <a href="javascript:;" class="buy-btn lyecs-buyBtn">
                                                <template v-if="module.buyBtnStyle === 5 || module.buyBtnStyle === 6">
                                                    <span>{{ $t('购买') }}</span>
                                                </template>
                                                <template v-if="module.buyBtnStyle === 7 || module.buyBtnStyle === 8">
                                                    <span>{{ $t('马上抢') }}</span>
                                                </template>
                                                <template v-if="module.buyBtnStyle === 1">
                                                    <span class="iconfont-h5 icon-gouwuche iconh5-style"></span>
                                                </template>
                                                <template v-if="module.buyBtnStyle === 2">
                                                    <span class="iconfont-h5 icon-gouwuche1 iconh5-style"></span>
                                                </template>
                                                <template v-if="module.buyBtnStyle === 3">
                                                    <span class="iconfont-h5 icon-jia iconh5-style"></span>
                                                </template>
                                                <template v-if="module.buyBtnStyle === 4">
                                                    <span class="iconfont-h5 icon-jia1 iconh5-style"></span>
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
import { ref, computed, Ref, onMounted } from "vue";
import { priceFormat, productImage } from "@/utils/format";
import { ModuleActivityType, ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultTitle, CommonTitle } from "@/views/decorate/decorate/src/modules/";
import { getMobileSeckillList } from "@/api/decorate/mobileSeckill";
import emptyImg from "@/views/decorate/decorate/src/img/empty-img-bg3.png";
// 在modules加入要使用的模块
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
    frame: defaultFrame,
    title: defaultTitle
});
mergeDefaultModule(module.value, defaultModule.value);
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

const seckillList = ref<any>([]);
const getData = async () => {
    try {
        const result = await getMobileSeckillList();
        seckillList.value = result;
    } catch (error) {
        console.error(error);
    }
};

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

.iconh5-style {
    color: #f23030;
    font-size: 18px;
    margin-right: 5px;
}
</style>
