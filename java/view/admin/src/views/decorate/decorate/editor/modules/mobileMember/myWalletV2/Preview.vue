<template>
    <div class="module-ad-con module-member-my-wallet-v2" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content">
            <div class="my-wallet-v2-wrap" :style="wrapStyle">
                <div class="my-wallet-v2-grid" :style="containerStyle">
                    <div class="my-wallet-v2-cell" v-for="(item, index) in displayItems" :key="item.key">
                        <img class="my-wallet-v2-icon" :src="item.icon.picUrl || defaultIcons[item.key]" alt="" />
                        <span class="my-wallet-v2-label">{{ item.label }}</span>
                        <span class="my-wallet-v2-value">{{ demoValues[item.key] }}</span>
                        <span class="my-wallet-v2-arrow iconfont-h5 icon-a-gengduo20"></span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { myWalletV2Defaults } from "./index";
import type { MyWalletV2Module, WalletV2Item } from "./types";

const props = defineProps<{ module: MyWalletV2Module }>();
const module = props.module;

const defaultIcons: Record<string, string> = {
    balance: "https://oss.tigshop.com/official/img/pc/202605/177993337419dbsyRWczzneL4mhf.png",
    coupon: "https://oss.tigshop.com/official/img/pc/202605/17799333737quaCAwzzMe1XpmYHd.png",
    collect: "https://oss.tigshop.com/official/img/pc/202605/1779933373JYzFsV1Ky8YZ32BzDg.png",
    history: "https://oss.tigshop.com/official/img/pc/202605/1779933373CFhQRjCp2OMvTY1vjQ.png"
};

const demoValues: Record<string, string> = {
    balance: "640826.82",
    coupon: "328",
    collect: "1224",
    history: "5699"
};

const defaultItems: WalletV2Item[] = [
    { key: "balance", label: "余额", icon: { picUrl: defaultIcons.balance, picThumb: defaultIcons.balance } },
    { key: "coupon", label: "优惠券", icon: { picUrl: defaultIcons.coupon, picThumb: defaultIcons.coupon } },
    { key: "collect", label: "商品收藏", icon: { picUrl: defaultIcons.collect, picThumb: defaultIcons.collect } },
    { key: "history", label: "浏览记录", icon: { picUrl: defaultIcons.history, picThumb: defaultIcons.history } }
];

const defaultInnerPadding = 12;
mergeDefaultModule(module, myWalletV2Defaults);

const frame = computed(() => module.frame ?? {});
const frameFormat = computed(() => formatFrame(frame.value));

const displayItems = computed(() => {
    const items = module.items;
    if (!items || items.length === 0) return defaultItems;
    return items;
});

const wrapStyle = computed(() => ({
    backgroundColor: frame.value.backgroundColor || "#ffffff",
    borderRadius: `${frame.value.boxRadius ?? 0}px`
}));

const containerStyle = computed(() => ({
    padding: `${frame.value.innerPadding ?? defaultInnerPadding}px`
}));
</script>
<style lang="less" scoped>
.my-wallet-v2-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.my-wallet-v2-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    box-sizing: border-box;
}
.my-wallet-v2-cell {
    display: flex;
    align-items: center;
    padding: 8px;
    position: relative;

    &:nth-child(1),
    &:nth-child(2) {
        border-bottom: 1px solid #f2f3f5;
    }
    &:nth-child(odd) {
        border-right: 1px solid #eeeeee;
    }
}
.my-wallet-v2-icon {
    width: 20px;
    height: 20px;
    object-fit: contain;
    flex-shrink: 0;
    margin-right: 8px;
}
.my-wallet-v2-label {
    font-size: 12px;
    color: #666;
    line-height: 20px;
    flex-shrink: 0;
}
.my-wallet-v2-value {
    font-size: 12px;
    font-weight: 600;
    color: #333;
    line-height: 20px;
    flex: 1;
    text-align: right;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.my-wallet-v2-arrow {
    color: #999;
    flex-shrink: 0;
    margin-left: 3px;
    display: inline-flex;
    justify-content: center;
    &::before {
        font-size: 10px;
    }
}
</style>
