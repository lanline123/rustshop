<template>
    <view class="category-content">
        <scroll-view :scroll-x="true" :scroll-into-view="scrollIntoView" class="category-scroll">
            <template v-for="item in menuList" :key="item.categoryId">
                <view :id="`category-${item.categoryId}`" class="category-item-box">
                    <view class="category-item" :class="{ active: currentMainCateId === item.categoryId }" @click="handleClick(item.categoryId)">
                        <view class="item-img-box">
                            <image :src="imageFormat(item.categoryPic)" />
                        </view>
                        <view class="item-text line1">
                            {{ item.categoryName }}
                        </view>
                    </view>
                </view>
            </template>
        </scroll-view>
        <view class="all-btn" @click="show = true">
            <view>
                <view class="btn-text">{{ $t("全") }}</view>
                <view class="btn-text">{{ $t("部") }}</view>
            </view>
            <view class="iconfont-h5 icon-quanbufenleizhankai" />
        </view>
    </view>

    <tig-popup v-model:show="show" position="top" :z-index="999" :show-close="false" background-color="#f4f4f4">
        <view class="category-popup">
            <view class="category-list">
                <template v-for="item in menuList" :key="item.categoryId">
                    <view
                        class="category-item"
                        :class="{ active: currentMainCateId === item.categoryId }"
                        @click="handleClick(item.categoryId, `category-${item.categoryId}`)"
                    >
                        <view class="item-img-box">
                            <image :src="imageFormat(item.categoryPic)" />
                        </view>
                        <view class="item-text line1">
                            {{ item.categoryName }}
                        </view>
                    </view>
                </template>
            </view>
            <view class="cate-popup-close" @click="show = false">{{ $t("点击收起") }}<text class="iconfont-h5 icon-xiajiantou1-copy" /></view>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * 主分类导航组件
 * - 左侧展示一级商品分类列表
 * - 点击切换分类
 */
import { getCategoryList } from "@/api/productCate/productCate";
import type { filterSeleted } from "@/types/productCate/productCate";
import { ref } from "vue";
import { imageFormat } from "@/utils/format";

const props = defineProps({
    shopId: {
        type: Number,
        default: 0
    }
});

const currentMainCateId = defineModel<number>("currentMainCateId");

const emit = defineEmits(["change"]);

const menuList = ref<filterSeleted[]>([]);

const scrollIntoView = ref("");

const getMenuList = async () => {
    try {
        const result = await getCategoryList({ id: 0, shopId: props.shopId });
        menuList.value = result;
        currentMainCateId.value = result[0].categoryId;
    } catch (error) {
        console.error(error);
    }
};

getMenuList();

const handleClick = (id: number, intoView = "") => {
    if (id === currentMainCateId.value) return;
    currentMainCateId.value = id;
    scrollIntoView.value = intoView;
    emit("change", id);
};

const show = ref(false);
</script>

<style lang="scss" scoped>
.category-content {
    height: 178rpx;
    width: 100%;
    padding: 0 20rpx;
    background-color: #fff;

    .all-btn {
        height: 80%;
        width: 58rpx;
        position: absolute;
        right: 0rpx;
        top: 50%;
        transform: translateY(-50%);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background: #ffffff;
        box-shadow: -4rpx 0rpx 6rpx 0rpx rgba(234, 234, 234, 0.35);
        font-size: 22rpx;
        color: #6a6a6a;
        row-gap: 14rpx;

        .icon-quanbufenleizhankai {
            font-size: 32rpx;
            margin-top: 6rpx;
        }
    }

    .category-scroll {
        width: 100%;
        height: 100%;
        white-space: nowrap;
        border-bottom: 1px solid #f4f4f4;

        .category-item-box {
            display: inline-block;
            padding: 0 20rpx;
            height: 100%;

            &:first-child {
                padding-left: 0;
            }
            &:last-child {
                padding-right: 58rpx;
            }
        }

        .category-item {
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;

            .item-img-box {
                width: 84rpx;
                height: 84rpx;
                background-color: #f8f8f8;
                border-radius: 50%;
                border: 1px solid transparent;

                image {
                    width: 100%;
                    height: 100%;
                    border-radius: 50%;
                    overflow: hidden;
                }
            }

            .item-text {
                font-size: 22rpx;
                color: #3a3a3a;
                padding-top: 14rpx;
                text-align: center;
                max-width: 84rpx;
                overflow: hidden;
            }

            &.active {
                .item-img-box {
                    border: 1px solid var(--general);
                }
                .item-text {
                    color: var(--general);
                }
            }
        }
    }
}

.category-popup {
    max-height: 60vh;
    overflow: hidden;
    overflow-y: auto;
    padding: 0 40rpx 80rpx;
    padding-top: max(40rpx, var(--safe-top));
    border-radius: 32rpx 32rpx 0rpx 0rpx;

    .cate-popup-close {
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        font-size: 24rpx;
        color: #999;
        display: flex;
        align-items: center;
        padding: 30rpx 20rpx;
        column-gap: 5rpx;

        .icon-xiajiantou1-copy {
            font-size: 28rpx;
            transform: rotate(180deg);
        }
    }

    .category-list {
        display: flex;
        flex-wrap: wrap;
        row-gap: 40rpx;
        column-gap: 62rpx;

        .category-item {
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;

            .item-img-box {
                width: 84rpx;
                height: 84rpx;
                background-color: #f8f8f8;
                border-radius: 50%;
                border: 1px solid transparent;

                image {
                    width: 100%;
                    height: 100%;
                    border-radius: 50%;
                    overflow: hidden;
                }
            }

            .item-text {
                font-size: 22rpx;
                color: #3a3a3a;
                padding-top: 14rpx;
                text-align: center;
                max-width: 84rpx;
                overflow: hidden;
            }

            &.active {
                .item-img-box {
                    border: 1px solid var(--general);
                }
                .item-text {
                    color: var(--general);
                }
            }
        }
    }
}
</style>
