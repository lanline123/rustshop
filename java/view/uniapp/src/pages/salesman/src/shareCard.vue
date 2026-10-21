<template>
    <tig-popup v-model:show="showShare" :z-index="999" :show-close="false" background-color="#fff" @close="close">
        <view class="salesman-share-box">
            <view class="salesman-share-title">
                {{ Number(currentData.finalCommissionPrice) > 0 ? `${$t("分享后预计可赚")}` : `${$t("立即分享给好友")}` }}
                <format-price v-if="Number(currentData.finalCommissionPrice) > 0" base :price-data="currentData.finalCommissionPrice" />
                <text class="iconfont-h5 icon-jiaochacross80" @click="$emit('update:modelValue', false)" />
            </view>
            <view class="salesman-share-menu">
                <template v-for="(item, index) in menuList" :key="index">
                    <view class="salesman-share-menu-item" @click="handleMenu(item.type)">
                        <view class="menu-item-text">
                            {{ $t(item.name) }}
                            <view class="menu-item-line" :class="{ show: activeType === item.type }" />
                        </view>
                    </view>
                </template>
            </view>

            <view class="salesman-share-content">
                <view v-show="activeType === 'poster'" style="height: 100%">
                    <posterCard :qrcode-path="qrcodePath" :current-data="currentData" :active-type="activeType" />
                </view>
                <view v-show="activeType === 'imageText'">
                    <imageTextCard
                        :current-data="currentData"
                        :link="link"
                        @show-save-image-text-card="$emit('showSaveImageTextCard')"
                        @show-share-qrcode="$emit('showShareQrcode')"
                        @close="$emit('update:modelValue', false)"
                    />
                </view>
            </view>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * 分享卡片组件
 * - 切换图文分享和海报分享模式
 * - 弹窗展示分享内容
 */
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import imageTextCard from "./imageTextCard.vue";
import posterCard from "./posterCard.vue";

const { t } = useI18n();
const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    },
    currentData: {
        type: Object,
        default: () => {}
    },
    link: {
        type: String,
        default: ""
    },
    qrcodePath: {
        type: String,
        default: ""
    }
});
const emit = defineEmits(["update:modelValue", "showSaveImageTextCard", "showShareQrcode"]);
const showShare = computed({
    get() {
        return props.modelValue;
    },
    set(val) {
        emit("update:modelValue", val);
    }
});

const menuList = [
    {
        type: "imageText",
        name: t("推广图文")
    },
    {
        type: "poster",
        name: t("生成海报")
    }
];
const activeType = ref("imageText");
const handleMenu = (type: string) => {
    activeType.value = type;
};

const close = () => {
    activeType.value = "imageText";
};
</script>

<style lang="scss" scoped>
.icon-jiaochacross80 {
    padding: 10px;
}

.salesman-share-box {
    height: 80vh;
    padding: 32rpx 0 0;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-direction: column;

    .salesman-share-content {
        flex: 1;
        overflow: hidden;
        overflow-y: auto;
    }

    .salesman-share-title {
        color: #ed6a0c;
        font-weight: bolder;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28rpx;
        position: relative;
        padding: 15rpx 0;

        .iconfont-h5 {
            position: absolute;
            right: 0;
            top: 50%;
            transform: translateY(-50%);
            font-size: 28rpx;
            color: #c8c9cc;
        }
    }

    .salesman-share-menu {
        display: flex;

        .salesman-share-menu-item {
            flex: 1;
            font-size: 26rpx;
            color: #323233;

            .menu-item-text {
                position: relative;
                height: 80rpx;
                display: flex;
                align-items: center;
                justify-content: center;
                .menu-item-line {
                    position: absolute;
                    width: 42rpx;
                    bottom: 0;
                    left: 50%;
                    transform: translateX(-50%);
                    z-index: 1;
                    height: 6rpx;
                    background-color: #ee0a24;
                    border-radius: 6rpx;
                    display: none;
                    &.show {
                        display: block;
                    }
                }
            }
        }
    }
}
</style>
