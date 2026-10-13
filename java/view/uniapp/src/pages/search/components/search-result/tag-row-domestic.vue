<template>
    <view v-if="hasVisibleTags" class="tag-row-domestic" :class="{ embedded, 'in-row': inRow }">
        <view class="tag-row">
            <view class="tag-list flex-wrap align-center">
                <view v-if="filterParams.intro && introList[filterParams.intro]" class="tag-item mr10" @click.stop="emit('delete', 'intro')">
                    <text>{{ introList[filterParams.intro] }}</text>
                    <text class="iconfont-h5 icon-chacha tag-icon" />
                </view>

                <view v-if="couponName" class="tag-item mr10" @click.stop="emit('delete', 'couponInfo')">
                    <text>{{ $t("优惠券") }}: {{ couponName }}</text>
                    <text class="iconfont-h5 icon-chacha tag-icon" />
                </view>

                <view v-if="brandName" class="tag-item mr10 brand" @click.stop="emit('delete', 'brand')">
                    <text>{{ brandName }}</text>
                    <text class="iconfont-h5 icon-chacha tag-icon" />
                </view>

                <view v-for="(item, index) in filterParams.attrs" :key="item.attrName" class="tag-item mr10 brand" @click.stop="emit('deleteAttr', index)">
                    <text>
                        {{ item.attrName }}:
                        <text v-for="value in item.attrValue" :key="value">{{ value }}</text>
                    </text>
                    <text class="iconfont-h5 icon-chacha tag-icon" />
                </view>

                <view v-if="filterParams.max || filterParams.min" class="tag-item mr10" @click.stop="emit('delete', 'price')">
                    <text>{{ filterParams.min || 0 }} - {{ filterParams.max || 0 }}</text>
                    <text class="iconfont-h5 icon-chacha tag-icon" />
                </view>

                <!-- 分类路径：框内横滑展示整条路径（含当前选中的二级/末级） -->
                <view v-for="(item, index) in treeList" :key="index" class="flex align-center" @click.stop="emit('deleteCategory', index)">
                    <view class="tag-item">
                        <text>{{ item.categoryName }}</text>
                        <text class="iconfont-h5 icon-chacha tag-icon" />
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import type { attrsData, filterSeleted } from "@/types/search/search";

interface TagFilterParams {
    intro?: string;
    attrs?: attrsData[];
    max?: string | number | null;
    min?: string | number | null;
}

const props = withDefaults(
    defineProps<{
        filterParams: TagFilterParams;
        brandName?: string;
        couponName?: string;
        treeList?: filterSeleted[];
        introList?: Record<string, string>;
        /** 嵌入搜索框内：透明底、收紧间距 */
        embedded?: boolean;
        /** 与关键词同一行、横向排列不换行 */
        inRow?: boolean;
    }>(),
    {
        brandName: "",
        couponName: "",
        treeList: () => [],
        introList: () => ({}),
        embedded: false,
        inRow: false
    }
);

const emit = defineEmits<{
    (e: "delete", type: string): void;
    (e: "deleteAttr", index: number): void;
    (e: "deleteCategory", index: number): void;
}>();

const hasVisibleTags = computed(() => {
    const fp = props.filterParams;
    const introKey = fp.intro != null ? String(fp.intro) : "";
    if (introKey && props.introList[introKey]) return true;
    if (props.couponName) return true;
    if (props.brandName) return true;
    if (fp.attrs?.length) return true;
    if (fp.max || fp.min) return true;
    if (props.treeList.length) return true;
    return false;
});
</script>

<style lang="scss" scoped>
.tag-row-domestic {
    width: 100%;

    &.embedded .tag-row {
        background-color: transparent;
        padding: 8rpx 4rpx 4rpx;

        .tag-list .tag-item {
            margin-bottom: 8rpx;
        }

        .tag-list .tag-interval {
            margin-bottom: 8rpx;
        }
    }

    &.embedded.in-row {
        flex-shrink: 0;
        /* 按内容撑开，不能相对 scroll-view 视口限宽，否则关键词会叠在 tag 上 */
        width: max-content;
        max-width: none;

        .tag-row {
            width: max-content;
            padding: 0 8rpx 0 0;
        }

        .tag-list {
            flex-wrap: nowrap;
            width: max-content;
            align-items: center;
        }

        .tag-list .tag-item {
            margin-bottom: 0;
            flex-shrink: 0;
            padding: 4rpx 10rpx;
            font-size: 22rpx;
        }

        .tag-list .mr10 {
            margin-right: 8rpx;
        }

        .tag-list .tag-interval {
            margin-bottom: 0;
            flex-shrink: 0;
        }
    }
}

.tag-row {
    background-color: #fff;
    padding: 0 30rpx;

    .tag-list {
        display: flex;
        flex-wrap: wrap;
        align-items: center;

        .tag-item {
            display: flex;
            align-items: center;
            padding: 5rpx 10rpx;
            text-align: center;
            color: #232326;
            background-color: #f7f7f7;
            border: solid 1px #f0f2f5;
            font-size: 24rpx;
            border-radius: 27px;
            vertical-align: middle;
            margin-bottom: 20rpx;

            text {
                margin: 0 5rpx;
            }
        }

        .mr10 {
            margin-right: 10rpx;
        }

        .tag-interval {
            margin: 0 10rpx 20rpx;
            font-size: 18rpx;

            .icon-gengduo1 {
                font-size: 13rpx;
                color: #999;
                transform: translateY(-2rpx);
                display: inline-block;
            }
        }

        .tag-icon {
            font-size: 18rpx;
            color: #000;
        }
    }
}
</style>
