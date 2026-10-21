<template>
    <view class="menu-content">
        <scroll-view scroll-y="true" class="menu-scroll">
            <template v-for="(item, index) in menuList" :key="item.categoryId">
                <view class="menu-item" :class="{ active: currentCateId === item.categoryId }" @click="handleClick(item.categoryId)">{{
                    item.categoryName
                }}</view>
            </template>
        </scroll-view>
    </view>
</template>

<script setup lang="ts">
/**
 * 分类菜单组件
 * - 顶部分类Tab导航
 * - 支持滑动切换分类
 */
import { getCategoryList } from "@/api/productCate/productCate";
import type { filterSeleted } from "@/types/productCate/productCate";
import { shallowRef } from "vue";

const currentCateId = defineModel<number>("currentCateId");

const emit = defineEmits(["change"]);

const menuList = shallowRef<filterSeleted[]>([]);

const getMenuList = async () => {
    try {
        const result = await getCategoryList({ id: 0 });
        menuList.value = result;
        currentCateId.value = result[0].categoryId;
    } catch (error) {
        console.error(error);
    }
};

getMenuList();

const handleClick = (id: number) => {
    if (id === currentCateId.value) return;
    currentCateId.value = id;
    emit("change", id);
};
</script>

<style lang="scss" scoped>
.menu-content {
    height: 100%;
    width: 100%;
    .menu-scroll {
        height: 100%;

        .menu-item {
            min-height: 50px;
            padding-left: 6px;
            width: 100%;
            font-size: 13px;
            color: var(--cate-side-text, #424242);
            text-align: center;
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            justify-content: center;

            /* --cate-* 由分类页装修下发（见 buildCateCenterV2Vars），未配置时 fallback 即原有视觉 */
            &.active {
                color: var(--cate-side-active-text, var(--cate-theme, var(--general)));
                background-color: var(--cate-side-active-bg, #fff);

                &::after {
                    content: "";
                    width: 3px;
                    height: 18px;
                    background-color: var(--cate-side-indicator, var(--cate-theme, var(--general)));
                    border-radius: 2px;
                    position: absolute;
                    left: 0;
                }
            }
        }
    }
}
</style>
