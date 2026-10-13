<template>
    <tig-popup v-model:show="show" :z-index="999" :show-close="false" background-color="#fff">
        <view class="card-list-container">
            <view class="popup-title">{{ $t("选择礼品卡") }}</view>
            <template v-if="cards && cards.length">
                <scroll-view scroll-y="true" class="card-list">
                    <!-- <up-radio-group v-model="selectedCardId" placement="column" icon-placement="right">
                        <template v-for="item in cards" :key="item.giftCardsId">
                            <view class="radio-item">
                                <div class="gift-card-item">
                                    <giftCard :card-info="item" />
                                </div>
                                <up-radio :name="item.giftCardsId" active-color="var(--general)" />
                            </view>
                        </template>
                    </up-radio-group> -->
                    <radio-group @change="handleChange">
                        <view class="radio-list">
                            <label v-for="item in cards" :key="item.giftCardsId" class="radio-item">
                                <view class="gift-card-item">
                                    <giftCard :card-info="item" />
                                </view>
                                <view>
                                    <radio color="var(--general)" :value="item.giftCardsId.toString()" :checked="item.giftCardsId === selectedCardId" />
                                </view>
                            </label>
                        </view>
                    </radio-group>
                </scroll-view>
            </template>
            <tig-fixed-placeholder background-color="#fff">
                <view class="btn-box">
                    <tig-button class="btn" @click="handleOkClick"> {{ $t("确定") }} </tig-button>
                </view>
            </tig-fixed-placeholder>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
import type { GiftCardInfo } from "@/types/giftCard/giftCard";
import { ref, watch } from "vue";
import giftCard from "./giftCard.vue";
import { useI18n } from "vue-i18n";

interface IProps {
    cards?: GiftCardInfo[];
}

interface IEmits {
    (e: "okCallBack", giftCard: GiftCardInfo): void;
}

const props = defineProps<IProps>();
const show = defineModel<boolean>("modelValue");
const emit = defineEmits<IEmits>();
const selectedCardId = ref<number>(0);
const { t } = useI18n();

const handleChange = (e: any) => {
    selectedCardId.value = Number(e.detail.value);
};
const handleOkClick = () => {
    if (selectedCardId.value == 0) {
        // 提示用户选择卡片
        uni.showToast({
            title: t("请选择礼品卡"),
            icon: "none"
        });
        return;
    }
    const selectedCard = props.cards?.find((item) => item.giftCardsId === selectedCardId.value);
    if (!selectedCard) {
        return;
    }
    show.value = false;
    emit("okCallBack", selectedCard);
};

watch(
    () => show.value,
    (val) => {
        if (val) {
            // 默认选中第一个
            if (props.cards && props.cards.length) {
                selectedCardId.value = props.cards[0].giftCardsId;
            }
        }
    }
);
</script>

<style lang="scss" scoped>
.card-list-container {
    height: 60vh;
    overflow: hidden;
    overflow-y: auto;
    border-top-left-radius: 10px;
    border-top-right-radius: 10px;
    .gift-card-item {
        width: 546rpx;
        height: 318rpx;
    }
    .card-list {
        height: calc(60vh - 123rpx - 120rpx);
    }
    .btn-box {
        padding: 25rpx;
    }
}
.radio-list {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
    padding: 0 30rpx;
    .radio-item {
        flex: 1;
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: space-between;
        // gap: 20rpx;
    }
}
</style>
