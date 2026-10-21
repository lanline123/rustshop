<template>
    <div class="click-through-statistics" ref="clickThroughStatisticsRef"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, nextTick, watchEffect, inject } from "vue";
import * as echarts from "echarts";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const filterState = inject<any>("filterState");

const clickThroughStatisticsRef = ref<HTMLDivElement>();
let myChart: echarts.ECharts;
const myChartOptions = ref<any>({
    title: {
        text: t('点击量统计'),
        left: "center"
    },
    grid: {
        left: "4%"
    },
    tooltip: {
        trigger: "axis"
    },
    legend: {
        data: [t('点击量')],
        left: "center", // 将图例居中显示（水平方向）
        bottom: "bottom" // 设置图例距离底部为容器高度的10%（垂直方向）
    },
    xAxis: {
        type: "category",
        data: []
    },
    yAxis: {
        type: "value",
        name: t('(次)'),
        axisLabel: {
            formatter: "{value} "
        }
    },
    series: [
        {
            name: t('点击量'),
            data: [],
            type: "line",
            smooth: true,
            lineStyle: {
                color: "#f39393"
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
                            color: "#f39393"
                        },
                        {
                            offset: 1,
                            color: "#ffffff"
                        }
                    ],
                    global: !1
                }
            },
            color: "#f39393",
            emphasis: {
                scale: !1
            },
            tooltip: {
                valueFormatter: function (value: string) {
                    return value + t('次');
                }
            }
        }
    ]
});
const handleChartResize = () => {
    myChart?.resize();
};
const initChart = () => {
    myChart = echarts.init(clickThroughStatisticsRef.value);
    myChart?.setOption(myChartOptions.value);
    window.addEventListener("resize", handleChartResize);
};

watchEffect(() => {
    if (filterState && filterState.value?.hasOwnProperty("horizontalAxis")) {
        nextTick(() => {
            myChartOptions.value.xAxis.data = filterState.value.horizontalAxis;
            myChartOptions.value.series[0].data = filterState.value.longitudinalAxis;
            myChart?.setOption(myChartOptions.value);
        });
    }
});

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
.click-through-statistics {
    width: 100%;
    height: 100%;
}
</style>
