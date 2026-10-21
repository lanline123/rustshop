<template>
    <div class="container">
        <div class="content_wrapper">
            <div>
                <el-form :model="filterParams">
                    <div class="filtrate-menu">
                        <el-form-item class="mr-10">
                            <el-select v-model="filterParams.dateType" placeholder="Select" style="width: 94.5px" @change="handlesearch">
                                <el-option v-for="(item, index) in timeSorterList" :key="index" :label="item.label" :value="item.value" />
                            </el-select>
                        </el-form-item>
                        <el-form-item class="mr-10">
                            <el-date-picker
                                v-if="filterParams.dateType === '3'"
                                style="width: 200px"
                                :shortcuts="dayShortcuts"
                                v-model="selectDate['3']"
                                value-format="YYYY-MM-DD"
                                :editable="false"
                                type="date"
                            />
                            <el-date-picker
                                v-if="filterParams.dateType === '2'"
                                style="width: 200px"
                                :shortcuts="monthShortcuts"
                                v-model="selectDate['2']"
                                value-format="YYYY-MM"
                                :editable="false"
                                type="month"
                            />
                            <el-date-picker
                                v-if="filterParams.dateType === '1'"
                                v-model="selectDate['1']"
                                type="year"
                                value-format="YYYY"
                                style="width: 200px"
                                :shortcuts="yearShortcuts"
                                :editable="false"
                            />
                        </el-form-item>
                        <el-form-item class="mr-10">
                            <el-button plain @click="handlesearch">{{ $t('搜索') }}</el-button>
                        </el-form-item>
                        <el-form-item>
                            <el-button plain @click="handleExport" :loading="Exportloading">{{ $t('导出EXCEL') }}</el-button>
                        </el-form-item>
                    </div>
                </el-form>
            </div>
            <div class="chart" ref="chartRef"></div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive, onUnmounted, nextTick } from "vue";
import * as echarts from "echarts";
import { formatDate } from "@/utils/util";
import type { newMemberFilterParams, newMemberFilterState } from "@/types/panel/newMembers.d";
import { getAddUserTrends, exportAddUserTrends } from "@/api/panel/newMembers";
import { message } from "ant-design-vue";
import requestExport from "@/utils/export";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const selectDate = reactive<any>({
    "1": "",
    "2": "",
    "3": ""
});

const chartRef = ref<HTMLDivElement>();
let myChart: echarts.ECharts;
const myChartOptions = ref<any>({
    title: {
        text: t('新增会员趋势'),
        left: "center"
    },
    grid: {
        left: "3%",
        right: "4%"
    },
    tooltip: {
        trigger: "axis"
    },
    legend: {
        data: [t('新增会员数')],
        left: "center", // 将图例居中显示（水平方向）
        bottom: "bottom" // 设置图例距离底部为容器高度的10%（垂直方向）
    },
    xAxis: {
        type: "category",
        data: []
    },
    yAxis: {
        type: "value",
        name: t('(人)')
    },
    series: [
        {
            name: t('新增会员数'),
            data: [],
            type: "line",
            smooth: true,
            lineStyle: {
                color: "#F7DD32"
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
                            color: "rgba(247,221,50,0.25)"
                        },
                        {
                            offset: 1,
                            color: "rgba(247,221,50,0)"
                        }
                    ],
                    global: !1
                }
            },
            color: "#F7DD32",
            emphasis: {
                scale: !1
            },
            tooltip: {
                valueFormatter: function (value: string) {
                    return value + t('人');
                }
            }
        }
    ]
});

const filterParams = reactive<newMemberFilterParams>({
    dateType: "1",
    startEndTime: "",
    isExport: "0"
});
const filterState = ref<newMemberFilterState>();

const getData = async () => {
    try {
        filterParams.startEndTime = selectDate[filterParams.dateType];
        const result = await getAddUserTrends({
            dateType: filterParams.dateType,
            startEndTime: filterParams.startEndTime
        });
        const { horizontalAxis, longitudinalAxis } = result;
        const xAxisData = horizontalAxis.map((item) => {
            let unit = t('月');
            if (filterParams.dateType === "2") unit = t('日');
            if (filterParams.dateType === "3") unit = t('时');
            return item + unit;
        });
        myChartOptions.value.xAxis.data = xAxisData;
        myChartOptions.value.series[0].data = longitudinalAxis;
        myChart.setOption(myChartOptions.value);
    } catch (error: any) {
        message.error(error.message);
        console.error(error);
    }
};

const handlesearch = () => {
    getData();
};

const Exportloading = ref<boolean>(false);
const handleExport = async () => {
    Exportloading.value = true;
    try {
        const result = await exportAddUserTrends({ ...filterParams, isExport: "1" });
        Exportloading.value = false;
        requestExport(result, t('新增会员数导出'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        Exportloading.value = false;
    }
};

const handleChartResize = () => {
    myChart?.resize();
};
const initChart = () => {
    myChart = echarts.init(chartRef.value);
    myChart.setOption(myChartOptions.value);
    window.addEventListener("resize", handleChartResize);
};

const timeSorterList = [
    {
        label: t('按年筛选'),
        value: "1"
    },
    {
        label: t('按月筛选'),
        value: "2"
    },
    {
        label: t('按日筛选'),
        value: "3"
    }
];
const yearShortcuts = [
    {
        text: t('本年'),
        value: () => {
            return getDateYearsAgo(0);
        }
    },
    {
        text: t('去年'),
        value: () => {
            return getDateYearsAgo(1);
        }
    },
    {
        text: t('三年前'),
        value: () => {
            return getDateYearsAgo(3);
        }
    },
    {
        text: t('五年前'),
        value: () => {
            return getDateYearsAgo(5);
        }
    }
];
const monthShortcuts = [
    {
        text: t('本月'),
        value: new Date()
    },
    {
        text: t('上月'),
        value: () => {
            const date = new Date();
            date.setTime(date.getTime() - 3600 * 1000 * 24 * 30);
            return date;
        }
    },
    {
        text: t('本年一月'),
        value: () => {
            const date = new Date();
            const firstMonthOfYear = new Date(date.getFullYear(), 0, 1);
            return firstMonthOfYear;
        }
    }
];
const dayShortcuts = [
    {
        text: t('今天'),
        value: new Date()
    },
    {
        text: t('昨天'),
        value: () => {
            const date = new Date();
            date.setTime(date.getTime() - 3600 * 1000 * 24);
            return date;
        }
    },
    {
        text: t('本月第一天'),
        value: () => {
            const date = new Date();
            const years = date.getFullYear();
            const month = date.getMonth();
            return new Date(years, month, 1);
        }
    }
];
const getDateYearsAgo = (years: number) => {
    const date = new Date();
    const dateYearsAgo = new Date(date.getFullYear() - years, date.getMonth(), date.getDate());
    return dateYearsAgo;
};

onMounted(() => {
    nextTick(() => {
        initChart();
    });
    window.onresize = () => {
        myChart?.resize();
    };

    const nowDate = formatDate(new Date());
    if (nowDate) {
        const dates = nowDate.split("-");
        for (const key in selectDate) {
            if (key === "3") selectDate[key] = nowDate;
            if (key === "1") selectDate[key] = dates[0];
            if (key === "2") selectDate[key] = dates[0] + "-" + dates[1];
        }
    }
    void getData();
});
onUnmounted(() => {
    myChart?.dispose();
    window.removeEventListener("resize", handleChartResize);
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
    height: 400px;
    width: 100%;
}
</style>
