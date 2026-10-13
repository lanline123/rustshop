<template>
    <!-- 海外版筛选栏（layoutStyle=2）：横向可滚动的 pill 条，右侧渐隐箭头进「所有筛选条件」 -->
    <view class="filter-overseas">
        <view class="filter-row">
            <view class="tab-box flex filterBoxW">
                <view class="tab-title" @click="emit('showDrawer')">
                    <text class="iconfont-h5 icon-a-gengduo11" />
                    <view>{{ $t("筛选") }}</view>
                </view>

                <view class="tab-title" :class="{ active: sortPopup }" @click="emit('handleSort')">
                    <view style="margin-right: 8rpx">{{ $t("排序") }}</view>
                    <text v-if="sortPopup" class="iconfont-h5 icon-up" />
                    <text v-else class="iconfont-h5 icon-arrowdown" />
                </view>

                <template v-if="brandList.length > 0">
                    <view class="tab-title" :class="{ active: brandIds.length > 0 || brandPopup }" @click="emit('brandClick')">
                        <view style="margin-right: 8rpx">
                            {{ $t("品牌") }}
                            <text v-if="brandIds.length > 0">({{ brandIds.length }})</text>
                        </view>
                        <text v-if="brandPopup" class="iconfont-h5 icon-up" />
                        <text v-else class="iconfont-h5 icon-arrowdown" />
                    </view>
                </template>

                <template v-for="(attr, index) in attrList" :key="attr.name">
                    <view
                        class="tab-title"
                        :class="{ active: getSelectedAttrCount(attr.name) > 0 || (attrPopup && activeAttrIndex === index) }"
                        @click="emit('attrClick', attr, index)"
                    >
                        <view style="margin-right: 8rpx">
                            {{ $t(attr.name) }}
                            <text v-if="getSelectedAttrCount(attr.name) > 0">({{ getSelectedAttrCount(attr.name) }})</text>
                        </view>
                        <text v-if="attrPopup && activeAttrIndex === index" class="iconfont-h5 icon-up" />
                        <text v-else class="iconfont-h5 icon-arrowdown" />
                    </view>
                </template>

                <view class="tab-title tab-title-new" :class="{ active: isNewSelected }" @click="emit('toggleNew')">
                    <view>{{ $t("新品") }}</view>
                </view>
            </view>
            <view class="more-filter" @click="emit('showDrawer')">
                <text class="iconfont-h5 icon-arrowdown" />
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
/**
 * 搜索结果页 - 海外版筛选栏
 * 属性弹层的展开态由父级 attrPopup + activeAttrIndex 表示，
 * 不往 attrsList 数据里塞 UI 状态。
 */
import { computed, type PropType } from "vue";
import type { attrsData, attrsListData, Brand } from "@/types/search/search";

const props = defineProps({
    /** 已选属性，用于在 pill 上显示选中数量 */
    selectedAttrs: {
        type: Array as PropType<attrsData[]>,
        default: () => []
    },
    /** 当前排序字段，用于判断「新品」是否选中 */
    sort: {
        type: String,
        default: ""
    },
    brandList: {
        type: Array as PropType<Brand[]>,
        default: () => []
    },
    brandIds: {
        type: Array as PropType<number[]>,
        default: () => []
    },
    attrList: {
        type: Array as PropType<attrsListData[]>,
        default: () => []
    },
    sortPopup: {
        type: Boolean,
        default: false
    },
    brandPopup: {
        type: Boolean,
        default: false
    },
    attrPopup: {
        type: Boolean,
        default: false
    },
    activeAttrIndex: {
        type: Number,
        default: -1
    }
});

const emit = defineEmits<{
    (e: "showDrawer"): void;
    (e: "handleSort"): void;
    (e: "brandClick"): void;
    (e: "attrClick", attr: attrsListData, index: number): void;
    (e: "toggleNew"): void;
}>();

const getSelectedAttrCount = (attrName: string) => {
    const matched = props.selectedAttrs.find((item) => item.attrName === attrName);
    return matched?.attrValue?.length ?? 0;
};

const isNewSelected = computed(() => props.sort === "time");
</script>

<style lang="scss" scoped>
.filter-overseas {
    width: 100%;
    background-color: var(--search-item-bg, #fff);
}

.filter-row {
    display: flex;
    background: var(--search-item-bg, #fff);
    width: 100%;
    padding: 0 var(--search-item-padding-lr, 20rpx);
    box-sizing: border-box;
    overflow: hidden;

    .more-filter {
        position: relative;
        right: 0;
        z-index: 1;
        margin-left: auto;
        display: flex;
        align-items: center;
        padding: 0 15rpx;

        &:before {
            content: "";
            position: absolute;
            left: -64rpx;
            top: 0;
            bottom: 0;
            width: 32px;
            background: linear-gradient(270deg, var(--search-item-bg, #fff), transparent);
            pointer-events: none;
            z-index: 2;
        }

        .icon-arrowdown {
            font-size: 32rpx;
            color: #000;
            display: inline-block;
            transform: translateY(6rpx);
        }
    }
}

.tab-box {
    background-color: var(--search-item-bg, #fff);
    column-gap: 12rpx;
    position: relative;
}

.filterBoxW {
    display: flex;
    overflow-x: scroll;
    flex-wrap: nowrap;
    background-color: var(--search-item-bg, #fff);
    padding: 14rpx 0;

    .tab-title {
        display: flex;
        white-space: nowrap;
        padding: 2rpx 14rpx 2rpx 21rpx;
        background-color: #fff;
        border-radius: 7rpx;
        font-size: 25rpx;
        color: #505259;
        line-height: 49rpx;
        border: 1rpx solid #c2c4cc;

        &.active {
            outline: 3rpx solid #1a1a1a;
            outline-offset: -2rpx;
            color: #1a1a1a;
            background-color: #f7f8fc;
            border-bottom-right-radius: 7rpx;
            border-bottom-left-radius: 7rpx;
        }

        .icon-a-gengduo11 {
            font-size: 18rpx;
            margin-right: 8rpx;
        }
        .icon-arrowdown,
        .icon-up {
            font-size: 22rpx;
            color: #333;
            display: inline-block;
            transform: translateY(3rpx);
        }
        .icon-up {
            transform: translateY(0);
        }
    }
    .tab-title-new {
        padding: 2rpx 21rpx;
    }
}
</style>
