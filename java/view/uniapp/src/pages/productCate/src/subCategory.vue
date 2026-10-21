<template>
    <view class="menu-content">
        <scroll-view :scroll-y="true" class="menu-scroll">
            <view class="menu-item" :class="{ active: subCurrentCateId === 0 }" @click="handleClick(0)">{{ $t("全部") }}</view>
            <template v-for="item in menuList" :key="item.categoryId">
                <view class="menu-item" :class="{ active: subCurrentCateId === item.categoryId }" @click="handleClick(item.categoryId)">{{
                    item.categoryName
                }}</view>
            </template>
        </scroll-view>
    </view>
</template>

<script setup lang="ts">
/**
 * 子分类导航组件
 * - 横向展示二级分类标签
 * - 点击切换分类筛选商品
 */
import { getCategoryList } from "@/api/productCate/productCate";
import type { filterSeleted } from "@/types/productCate/productCate";
import { ref, watchEffect } from "vue";

const props = defineProps({
    currentMainCateId: {
        type: Number,
        default: 0
    },
    shopId: {
        type: Number,
        default: 0
    }
});

const emit = defineEmits(["change"]);

const subCurrentCateId = defineModel<number>("subCurrentCateId", {
    default: 0
});

const handleClick = (id: number) => {
    if (id === subCurrentCateId.value) return;
    subCurrentCateId.value = id;
    emit("change");
};

const menuList = ref<filterSeleted[]>([]);

const getMenuList = async (id: number) => {
    try {
        const result = await getCategoryList({ id, shopId: props.shopId });
        menuList.value = result;

        emit("change");
    } catch (error) {
        console.error(error);
    }
};

watchEffect(() => {
    if (props.currentMainCateId) {
        getMenuList(props.currentMainCateId);
    }
});
</script>

<style lang="scss" scoped>
.menu-content {
    height: 100%;
    width: 100%;
    background: #f4f4f4;

    .menu-scroll {
        height: 100%;

        .menu-item {
            min-height: 100rpx;
            padding-left: 11rpx;
            width: 100%;
            font-size: 28rpx;
            color: #595959;
            text-align: center;
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            justify-content: center;
            font-weight: 500;

            &.active {
                color: #1a1a1a;
                background-color: #fff;
                font-weight: bold;
            }
        }
    }
}
</style>
