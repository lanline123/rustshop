/** IM客服布局 - 客服系统专用布局，包含左侧菜单和主内容区 */
<template>
    <div class="common-layout-im">
        <div class="aside-full-height">
            <LeftMenu></LeftMenu>
        </div>
        <router-view />
    </div>
</template>

<script setup lang="ts">
import LeftMenu from "@/views/im/src/LeftMenu.vue";
</script>

<style lang="less" scoped>
.common-layout-im {
    display: flex;
    flex-direction: row;
    width: 100vw;
    height:  100vh;
    //position: relative;
    overflow-x: hidden;
    //min-width: 1270px;
    //min-height: 645px;

    .aside-full-height {
        height: 100vh; // 设置 el-aside 高度为视口高度的100%
        display: flex;
     width: auto;

    }
    .im-main-content{
        flex: 1;
    }
}


</style>
