<template>
    <div class="container">
        <div class="content_wrapper">
            <div>
                <el-tabs v-model="filterParams.isHits" @tab-change="handleClick">
                    <el-tab-pane :label="$t('点击量统计')" :name="1"></el-tab-pane>
                    <el-tab-pane :label="$t('访客数统计')" :name="0"></el-tab-pane>
                </el-tabs>
                <el-form :model="filterParams">
                    <div class="filtrate-menu">
                        <el-form-item class="mr-10">
                            <SelectTimeInterval
                                type="date"
                                v-model:start-date="filterParams.startTime"
                                v-model:end-date="filterParams.endTime"
                            ></SelectTimeInterval>
                        </el-form-item>
                        <el-form-item class="mr-10">
                            <el-button @click="handleSearch">{{ $t('搜索') }}</el-button>
                        </el-form-item>
                    </div>
                </el-form>
                <template v-if="filterParams.isHits === 1">
                    <div class="chart">
                        <ClickThroughStatistics></ClickThroughStatistics>
                    </div>
                </template>
                <template v-else-if="filterParams.isHits === 0">
                    <div class="chart">
                        <VisitorStatistics></VisitorStatistics>
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive, provide } from "vue";
import type { TabsPaneContext, TabPaneName } from "element-plus";
import { SelectTimeInterval } from "@/components/select";
import { getDays } from "@/utils/util";
import { formattedDate } from "@/utils/time";
import ClickThroughStatistics from "./src/ClickThroughStatistics.vue";
import VisitorStatistics from "./src/VisitorStatistics.vue";
import type { statisticsAccessFilterParams, FilterResult } from "@/types/panel/statisticsAccess.d.ts";
import { getStatisticsAccess } from "@/api/panel/statisticsAccess";
import { message } from "ant-design-vue";

const filterParams = reactive<statisticsAccessFilterParams>({
    startTime: formattedDate(getDays(30, "sub"), "YYYY-MM-DD"),
    endTime: formattedDate(new Date(), "YYYY-MM-DD"),
    isHits: 1
});
const handleClick = (name: TabPaneName) => {
    getData();
};

const filterState = ref<FilterResult>();
provide("filterState", filterState);
const getData = async () => {
    try {
        const result = await getStatisticsAccess(filterParams);
        filterState.value = result;
    } catch (error: any) {
        message.error(error.message);
        console.log(error);
    }
};

const handleSearch = () => {
    getData();
};

onMounted(() => {
    getData();
});
</script>

<style lang="less" scoped>
.filtrate-menu {
    display: flex;

    .mr-10 {
        margin-right: 5px;
    }
}

.chart {
    width: 100%;
    height: 400px;
}
</style>
