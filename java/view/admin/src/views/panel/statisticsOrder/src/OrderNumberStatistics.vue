<template>
    <div class="order-number-statistics" ref="orderNumberStatisticsRef"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, nextTick, inject, watchEffect } from "vue";
import * as echarts from "echarts";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const filterState = inject<any>("filterState");
const orderNumberStatisticsRef = ref<HTMLDivElement>();
let myChart: echarts.ECharts;
const myChartOptions = ref<any>({
    title: {
        text: t('订单数统计'),
        left: "center"
    },
    grid: {
        // left: "4%"
    },
    tooltip: {
        trigger: "axis"
    },
    legend: {
        data: [t('订单数')],
        left: "center", // 将图例居中显示（水平方向）
        bottom: "bottom" // 设置图例距离底部为容器高度的10%（垂直方向）
    },
    xAxis: {
        type: "category",
        data: []
    },
    yAxis: {
        type: "value",
        name: t('(件)')
    },
    series: [
        {
            name: t('订单数'),
            data: [],
            type: "line",
            smooth: true,
            lineStyle: {
                color: "#fac858"
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
                            color: "#fac858"
                        },
                        {
                            offset: 1,
                            color: "#ffffff"
                        }
                    ],
                    global: !1
                }
            },
            color: "#fac858",
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
    myChart = echarts.init(orderNumberStatisticsRef.value);
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
.order-number-statistics {
    width: 100%;
    height: 100%;
}
</style>
