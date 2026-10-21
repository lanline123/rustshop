<template>
    <view v-if="guessLike.length && configStore.isGuestLikeGoods" class="recommend_wrapper">
        <view class="title" :style="{ background: titleBg }">
            <view class="text">
                <!-- 后台保存的是语言包 key（如“猜你喜欢”），展示时需经过本地翻译。 -->
                <view class="name">{{ $t(configStore.guestLikeGoodsName || "") }}</view>
                <view class="desc">{{ $t("您还可以逛一逛") }}</view>
            </view>
        </view>
        <view class="masonry-content">
            <masonry :commodity-list="guessLike" :promotion-list="promotionList" :style-map="productStyleMap" @callback="$emit('callback')" />
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import masonry from "@/components/masonry/masonry.vue";
import { useList } from "@/hooks";
import { useProductStyle } from "@/hooks/useProductStyle";
import { getGuessLikeIds, getProductsList } from "@/api/common";
import { useConfigStore } from "@/store/config";

defineProps({
    // 是否显示
    titleBg: {
        type: String,
        default: ""
    }
});

defineEmits(["callback"]);

const configStore = useConfigStore();

const ids = ref("");

const {
    data: guessLike,
    promotionList,
    getList
} = useList(getProductsList, {
    params: {
        ids: ids.value
    },
    path: {
        dataKey: "records"
    },
    immediate: false,
    needPromotionList: true,
    needReachBottom: true
});

const { styleMap: productStyleMap } = useProductStyle(guessLike);

const getGuessLikeIdsData = async () => {
    try {
        const result = await getGuessLikeIds({ page: 1, size: 10 });
        ids.value = result;
        getList({
            ids: ids.value
        });
    } catch (error) {
        console.error("获取猜你喜欢商品ID失败", error);
    }
};
getGuessLikeIdsData();
</script>

<style lang="scss" scoped>
.recommend_wrapper {
    margin-top: 20rpx;
}
.recommend_wrapper .title {
    padding: 20rpx;
}
.recommend_wrapper .title .text {
    font-size: 24rpx;
    color: #999;
}
.recommend_wrapper .title .text .name {
    color: #282828;
    font-size: 30rpx;
    font-weight: bold;
    margin-bottom: 5rpx;
    display: inline-block;
}
.recommend_wrapper .title .text .desc {
    display: inline-block;
    padding-left: 20rpx;
}

.masonry-content {
    padding: 0 20rpx;
}
</style>
