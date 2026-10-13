<template>
    <SelectTime v-model:dateType="filterParams.dateType" v-model:dateValue="filterParams.startEndTime" @onOK="getData"></SelectTime>
    <div class="chart-content">
        <div class="chart" ref="chartRef"></div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import * as echarts from "echarts";
import { ref, reactive, onMounted, onUnmounted, nextTick, watchEffect } from "vue";
import { message } from "ant-design-vue";
import SelectTime from "./SelectTime.vue";
import { CoreTrendFilterParams } from "@/types/salesman/overview.d";
import { getOverviewCoreTrend } from "@/api/salesman/overview";
import { getDays } from "@/utils/util";
import { formattedDate } from "@/utils/time";
import { useConfigStore } from "@/store/config";

const config: any = useConfigStore();

// 响应式判断是否为移动端
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const isMobile = ref(window.innerWidth < 768);
const updateMobileStatus = () => {
    isMobile.value = window.innerWidth < 768;
};

const filterParams = reactive<CoreTrendFilterParams>({
    dateType: 3,
    startEndTime: formattedDate(getDays(0, "sub"), "YYYY-MM-DD")
});

const chartRef = ref<HTMLDivElement>();
let myChart: echarts.ECharts;

// 动态生成图表配置
const getChartOptions = () => ({
    grid: {
        left: isMobile.value ? "0%" : "2%", // 移动端增加左侧空间
        right: "4%",
        bottom: "25%",
        top: isMobile.value ? "15%" : "15%", // 移动端增加顶部空间
        containLabel: true // 防止标签被截断
    },
    tooltip: {
        trigger: "axis",
        confine: true // 防止tooltip超出屏幕
    },
    legend: {
        data: [`${t('分销员销售额')}(${t(config.config.dollarSignCn)})`],
        icon: "rect",
        itemWidth: isMobile.value ? 15 : 20, // 移动端缩小图例
        itemHeight: isMobile.value ? 3 : 4,
        top: isMobile.value ? 0 : undefined // 移动端图例靠上
    },
    xAxis: {
        type: "category",
        data: [],
        axisLabel: {
            interval: isMobile.value ? "auto" : 0, // 移动端自动间隔标签
            rotate: isMobile.value ? 30 : 0, // 移动端标签旋转
            fontSize: isMobile.value ? 10 : 12 // 移动端字体缩小
        }
    },
    yAxis: {
        type: "value",
        name: `${t('分销员销售额')}(${t(config.config.dollarSignCn)})`,
        nameLocation: "end",
        nameTextStyle: {
            padding: isMobile.value ? [0, 0, 0, 20] : [0, 0, 0, 50]
        },
        axisLabel: {
            formatter: isMobile.value ? (value: number) => (value >= 10000 ? t('{toFixed}万', { toFixed: (value / 10000).toFixed(1) }) : value) : undefined
        }
    },
    dataZoom: [
        {
            backgroundColor: "#fff",
            fillerColor: "rgb(234, 236, 253, 0.6)",
            borderColor: "#bbc2f9",
            showDetail: false,
            startValue: 0,
            endValue: 100,
            filterMode: "empty",
            left: "center",
            zoomLoxk: true,
            bottom: 3,
            height: isMobile.value ? 15 : 20 // 移动端缩小dataZoom高度
        }
    ],
    series: [
        {
            name: `${t('分销员销售额')}(${t(config.config.dollarSignCn)})`,
            data: [],
            type: "line",
            smooth: false,
            lineStyle: {
                color: "#3aa1ff",
                width: isMobile.value ? 2 : 3 // 移动端线条稍细
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
                            color: "rgba(58,161,255, 0.5)"
                        },
                        {
                            offset: 1,
                            color: "#ffffff"
                        }
                    ],
                    global: !1
                }
            },
            color: "#3aa1ff",
            emphasis: {
                scale: !1
            },
            symbolSize: isMobile.value ? 4 : 6 // 移动端缩小数据点
        }
    ]
});

const myChartOptions = ref(getChartOptions());

// 监听窗口变化
const handleResize = () => {
    updateMobileStatus();
    myChart?.resize();
    myChartOptions.value = getChartOptions();
    myChart?.setOption(myChartOptions.value, true);
};

const initChart = () => {
    if (!chartRef.value) return;
    myChart = echarts.init(chartRef.value);
    myChart.setOption(myChartOptions.value);
    window.addEventListener("resize", handleResize);
};

const getData = async () => {
    try {
        const result = await getOverviewCoreTrend({ ...filterParams });
        const { horizontalAxis, longitudinalAxis } = result;
        const xAxisData = horizontalAxis.map((item: any) => {
            let unit = t('月');
            if (filterParams.dateType === 2) unit = t('日');
            if (filterParams.dateType === 3) unit = t('时');
            return isMobile.value && filterParams.dateType === 3
                ? item // 移动端小时数据不显示单位
                : item + unit;
        });
        myChartOptions.value.xAxis.data = xAxisData as never[];
        myChartOptions.value.series[0].data = longitudinalAxis as never[];
        myChart?.setOption(myChartOptions.value);
    } catch (error: any) {
        message.error(error.message);
    }
};

onMounted(() => {
    nextTick(() => {
        initChart();
    });
    getData();
});

onUnmounted(() => {
    myChart?.dispose();
    window.removeEventListener("resize", handleResize);
});

// 监听筛选参数变化
watchEffect(() => {
    if (filterParams.dateType) {
        getData();
    }
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
    height: 382px;
    width: 100%;
    margin-bottom: 40px;

    // 移动端样式
    @media only screen and (max-width: 767px) {
        height: 280px; // 移动端高度减小
        margin-bottom: 20px;
    }
}

// 移动端容器优化
@media only screen and (max-width: 767px) {
    .chart-content {
        padding: 0 5px; // 增加左右内边距
    }
}
</style>
