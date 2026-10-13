<template>
    <div class="orderamount-statistics" ref="orderamountStatisticsRef"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, nextTick, inject, watchEffect } from "vue";
import * as echarts from "echarts";
import { useConfigStore } from "@/store/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const filterState = inject<any>("filterState");

const orderamountStatisticsRef = ref<HTMLDivElement>();
let myChart: echarts.ECharts;
const myChartOptions = ref<any>({
    title: {
        text: t('订单金额统计'),
        left: "center"
    },
    grid: {
        // left: "4%"
    },
    tooltip: {
        trigger: "axis"
    },
    legend: {
        data: [t('订单金额')],
        left: "center", // 将图例居中显示（水平方向）
        bottom: "bottom" // 设置图例距离底部为容器高度的10%（垂直方向）
    },
    xAxis: {
        type: "category",
        data: []
    },
    yAxis: {
        type: "value",
        name: `(${config.config.dollarSignCn})`
    },
    series: [
        {
            name: t('订单金额'),
            data: [],
            type: "line",
            smooth: true,
            lineStyle: {
                color: "#1456f0"
            },
            areaStyle: {
                color: {
                    type: "linear",
                    x: 0,
                    y: 0,
                    x2: 0,
                    y2: 1,
                    colorStops: [
                        {
                            offset: 0,
                            color: "#ECF5FF"
                        },
                        {
                            offset: 1,
                            color: "#ffffff"
                        }
                    ],
                    global: !1
                }
            },
            color: "#1456f0",
            emphasis: {
                scale: !1
            }
        }
    ]
});
const handleChartResize = () => {
    myChart?.resize();
};

watchEffect(() => {
    if (filterState && filterState.value?.hasOwnProperty("salesStatisticsData")) {
        nextTick(() => {
            myChartOptions.value.xAxis.data = filterState.value.salesStatisticsData.horizontalAxis;
            myChartOptions.value.series[0].data = filterState.value.salesStatisticsData.longitudinalAxis;
            myChart?.setOption(myChartOptions.value);
        });
    }
});
const initChart = () => {
    myChart = echarts.init(orderamountStatisticsRef.value);
    myChart.setOption(myChartOptions.value);
    window.addEventListener("resize", handleChartResize);
};

onMounted(() => {
    nextTick(() => {
        initChart();
    });
    window.onresize = () => {
        myChart?.resize();
    };
});
onUnmounted(() => {
    myChart?.dispose();
    window.removeEventListener("resize", handleChartResize);
});
</script>

<style lang="less" scoped>
.orderamount-statistics {
    width: 100%;
    height: 100%;
}
</style>
