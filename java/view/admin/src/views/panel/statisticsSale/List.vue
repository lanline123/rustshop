<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="notice-warp">{{ $t('提示：查询的时间间隔上限默认为一月，如果数据量过大导至查询时间过久或者超时报错，请尝试缩小查询的时间间隔！') }}</div>
            <div>
                <el-tabs v-model="activeName" @tab-click="handleClick">
                    <el-tab-pane :label="$t('销售明细')" name="销售明细"></el-tab-pane>
                    <el-tab-pane :label="$t('销售商品明细')" name="销售商品明细"></el-tab-pane>
                </el-tabs>

                <template v-if="activeName === '销售明细'">
                    <SalesDetail></SalesDetail>
                </template>

                <template v-if="activeName === '销售商品明细'">
                    <DetailsGoodsSold></DetailsGoodsSold>
                </template>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import type { TabsPaneContext } from "element-plus";
import SalesDetail from "./src/SalesDetail.vue";
import DetailsGoodsSold from "./src/DetailsGoodsSold.vue";

const activeName = ref("销售明细");
const handleClick = (tab: TabsPaneContext, event: Event) => {};
</script>

<style lang="less" scoped></style>
