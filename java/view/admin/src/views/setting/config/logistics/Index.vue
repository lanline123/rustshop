<template>
    <!-- <div style="display: flex; flex-direction: column"> -->
    <!-- <div class="content_wrapper1">
            <el-tabs v-model="activeKey" class="lyecs-tabs" tab-position="top" @tab-change="onTabChange">
                <el-tab-pane :key="1" label="配送设置" name="Delivery"></el-tab-pane>
                <el-tab-pane :key="2" label="物流公司" name="Logistics"></el-tab-pane>
                <el-tab-pane :key="3" label="运费模板" name="FreightTemplate"></el-tab-pane>
            </el-tabs>
        </div> -->
    <BaseLogistics></BaseLogistics>
    <!-- <Logistics v-if="activeKey === 'Logistics'"></Logistics> -->
    <!-- <FreightTemplate v-if="activeKey === 'FreightTemplate'"></FreightTemplate> -->
    <!-- </div> -->
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import { ref } from "vue";
import BaseLogistics from "./src/BaseLogistics.vue";
import Logistics from "@/views/setting/logisticsCompany/List.vue";
import FreightTemplate from "@/views/setting/shippingTpl/List.vue";

const activeKey = ref("Delivery");
const onTabChange = (key: string) => {
    activeKey.value = key;
};
</script>
<style lang="less" scoped>
.title {
    font-weight: bold;
    padding-top: 20px;
    padding-bottom: 30px;
    font-size: 14px;
}
.content_wrapper1 {
    background-color: #fff;
    padding: 10px 20px 0 20px;
}
.notice-warp {
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}
</style>
