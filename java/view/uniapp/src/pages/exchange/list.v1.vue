<template>
    <tig-layout :need-safe-top="true" safe-top-bg-color="linear-gradient(90deg, #fa1ea7, #ffa324)">
        <view class="exchange-box">
            <view class="exchange-header">
                <view class="header-bg" />
                <view class="header-text"> {{ `${$t("当前")}${configStore.integralName}` }}{{ userInfo.points || 0 }} </view>
                <view class="header-title">{{ `${configStore.integralName}${$t("兑换")}` }}</view>
            </view>
            <block v-if="!isLoading">
                <view v-if="total > 0" class="exchange-list">
                    <masonry :commodity-list="lsit" :is-exchange="true" type="exchange" :style-map="styleMap" />
                </view>
            </block>

            <block v-if="!isLoading && total === 0">
                <view class="empty-box">
                    <up-empty :icon="staticResource('salesman/no_order.png')" :text="$t('暂无数据')" />
                </view>
            </block>
        </view>
    </tig-layout>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import masonry from "@/components/masonry/masonry.vue";
import { computed, reactive, ref } from "vue";
import { getExchangeList } from "@/api/exchange/exchange";
import { useList } from "@/hooks";
import { useProductStyle } from "@/hooks/useProductStyle";
import { staticResource } from "@/utils";
import { useConfigStore } from "@/store/config";
import { setPageMeta } from "@/utils/seo";
import { onMounted } from "vue";

const { t } = useI18n();
const configStore = useConfigStore();

const userInfo = ref(uni.getStorageSync("userInfo"));

onMounted(() => {
    // #ifdef H5
    setPageMeta({
        title: `${t("积分兑换")} - ${configStore.baseInfo.shopTitle}`,
        description: `${configStore.baseInfo.shopTitle}${t("积分兑换商品列表")}`
    });
    // #endif
});

const filterParams = reactive({
    //初始化用于查询的参数
    page: 1,
    size: 20
});

const {
    isLoading,
    total,
    data: lsit
} = useList(getExchangeList, {
    params: filterParams,
    path: {
        dataKey: "records"
    },
    immediate: true
});

const { styleMap } = useProductStyle(lsit);

const overseasExchangeBg = computed(() => {
    return `url(${staticResource("exchange/overseas-exchange-bg.png")})`;
});
</script>
<style scoped lang="scss">
.exchange-header {
    width: 100%;
    height: 440rpx;
    background: linear-gradient(90deg, #fa1ea7, #ffa324);
    position: relative;

    &::after {
        position: absolute;
        content: "";
        width: 100%;
        height: 114rpx;
        background: linear-gradient(180deg, rgba(233, 51, 35, 0), rgba(233, 51, 35, 0.1) 50%, #f5f5f5);
    }

    .header-text {
        position: absolute;
        top: 220rpx;
        left: 30rpx;
        color: hsla(0, 0%, 100%, 0.6);
    }

    .header-bg {
        background-repeat: no-repeat;
        background-image: v-bind(overseasExchangeBg);
        background-size: contain;
        background-position: center right;
        width: 760rpx;
        height: 328rpx;
        margin: 0 auto;
        padding-top: 96rpx;
    }

    .header-title {
        position: absolute;
        top: 120rpx;
        left: 30rpx;
        font-weight: bold;
        font-size: 56rpx;
        color: #fff;
    }
}

.exchange-list {
    position: relative;
    z-index: 1;
    padding: 20rpx;
    margin-top: -130rpx;
}
</style>
