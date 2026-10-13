<template>
    <view
        class="module-ad-con module-shop-tips"
        :style="
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.boxMargin +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage
        "
    >
        <view
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <view v-if="module.picList && module.picList.length > 0" class="shop-tips-outer">
                <view v-for="(row, rowIndex) in itemRows" :key="rowIndex" class="shop-tips-row" :style="rowStyle">
                    <view v-for="(item, colIndex) in row" :key="colIndex" class="shop-tips-li">
                        <view class="shop-tips-link" :style="linkStyle" @click="urlFormat(item.picLink) && redirect({ url: urlFormat(item.picLink) })">
                            <view class="shop-tips-content" :style="textStyle">
                                <image v-if="item.picUrl || item.picThumb" class="shop-tips-icon" :src="productImage(item)" mode="aspectFit" />
                                <text class="shop-tips-text">{{ $t(item.picTitle || "") }}</text>
                            </view>
                            <view v-if="Number(module.showDivider) === 1 && colIndex < row.length - 1" class="shop-tips-sx" :style="dividerStyle"></view>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { formatCommonStyle } from "@/components/modules";
import { urlFormat, productImage } from "@/utils/format";
import { redirect } from "@/utils";

const props = defineProps({
    module: { type: Object, default: () => ({}) }
});

const commonStyle = computed(() => formatCommonStyle(props.module.moduleStyle ?? {}, props.module.contentStyle ?? {}));

const rowNum = computed(() => Number(props.module.rowNum) || 3);
const halfGap = computed(() => (props.module.itemGap ?? 0) / 2);

const itemRows = computed(() => {
    const list = props.module.picList || [];
    const n = rowNum.value;
    const rows: any[][] = [];
    for (let i = 0; i < list.length; i += n) {
        rows.push(list.slice(i, i + n));
    }
    return rows;
});

const rowStyle = computed(() => {
    const h = halfGap.value;
    return h > 0 ? `margin-left: -${h}px; margin-right: -${h}px;` : "";
});

const linkStyle = computed(() => {
    const h = halfGap.value;
    const bgColor = props.module.itemBackgroundColor;
    const bgStyle = bgColor ? `background-color: ${bgColor};` : "";
    const marginStyle = h > 0 ? `margin: ${h}px;` : "";
    return marginStyle + bgStyle;
});

const textStyle = computed(() => {
    return `color: ${props.module.titleColor || "#333333"};`;
});

const dividerStyle = computed(() => {
    const color = props.module.titleColor || "#333333";
    return `border-color: ${color};`;
});
</script>

<style lang="scss" scoped>
.module-shop-tips {
    min-height: auto !important;

    .module-ad-content {
        overflow: hidden;
    }
}

.shop-tips-outer {
    padding: 0;
    margin-left: 0;
    margin-right: 0;
}

.shop-tips-row {
    display: flex;
    flex-direction: row;
    justify-content: space-around;
    align-items: center;
}

.shop-tips-li {
    white-space: nowrap;
    box-sizing: border-box;
    min-width: 20%;
    max-width: 100%;
}

.shop-tips-link {
    display: block;
    position: relative;
    background: transparent;
    padding: 0;
    min-height: 20rpx;
}

.shop-tips-content {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8rpx;
    overflow: hidden;
    width: 100%;
}

.shop-tips-icon {
    width: 28rpx;
    height: 28rpx;
    flex-shrink: 0;
}

.shop-tips-text {
    min-width: 0;
    flex: 0 1 auto;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 24rpx;
}

.shop-tips-sx {
    position: absolute;
    box-sizing: border-box;
    pointer-events: none;
    top: -30%;
    bottom: -30%;
    right: -20rpx;
    transform: scale(0.5);
    border-right-width: 1px;
    border-right-style: solid;
}
</style>
