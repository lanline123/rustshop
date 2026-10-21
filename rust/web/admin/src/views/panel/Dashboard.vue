<template>
  <div class="tig-dashboard">
    <!-- Top Stats Row -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6" v-for="stat in statCards" :key="stat.title">
        <div class="tig-card stat-card">
          <div class="stat-left">
            <div class="stat-title">{{ stat.title }}</div>
            <div class="stat-val">{{ stat.prefix || '' }}{{ stat.value }}</div>
            <div class="stat-trend">
              <span>较昨日</span>
              <span :class="stat.rate >= 0 ? 'trend-up' : 'trend-down'">
                {{ stat.rate >= 0 ? '+' : '' }}{{ stat.rate }}%
              </span>
            </div>
          </div>
          <div class="stat-icon" :style="{ backgroundColor: stat.bgColor }">
            <el-icon :style="{ color: stat.color }"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Urgent Reminders -->
    <div class="tig-card reminder-card">
      <div class="tig-page-title">待办与履约提醒</div>
      <el-row :gutter="16">
        <el-col :span="6" v-for="rem in reminders" :key="rem.title">
          <div class="reminder-item" @click="$router.push(rem.path)">
            <div class="rem-count">{{ rem.count }}</div>
            <div class="rem-label">{{ rem.title }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Chart & Sales Trend -->
    <div class="tig-card chart-card">
      <div class="flex-between" style="margin-bottom: 20px">
        <div class="tig-page-title" style="margin-bottom: 0">近 7 天销售趋势</div>
        <el-radio-group v-model="chartDays" size="small" @change="loadChartData">
          <el-radio-button :value="7">近 7 天</el-radio-button>
          <el-radio-button :value="30">近 30 天</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="chartRef" class="echarts-container"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { Money, ShoppingBag, User, View } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import request from '@/utils/request';

const chartRef = ref<HTMLDivElement>();
let chartInstance: echarts.ECharts | null = null;
const chartDays = ref(7);

const statCards = ref([
  { title: '今日销售总额', value: '0.00', prefix: '¥', rate: 0, icon: Money, color: '#1456f0', bgColor: '#eef3ff' },
  { title: '今日支付订单', value: '0', rate: 0, icon: ShoppingBag, color: '#52c41a', bgColor: '#f6ffed' },
  { title: '今日新增会员', value: '0', rate: 0, icon: User, color: '#722ed1', bgColor: '#f9f0ff' },
  { title: '今日访客浏览', value: '0', rate: 0, icon: View, color: '#fa8c16', bgColor: '#fff7e6' }
]);

const reminders = ref([
  { title: '待发货订单', count: 0, path: '/order/list' },
  { title: '待处理售后退款', count: 0, path: '/order/list' },
  { title: '待付款订单', count: 0, path: '/order/list' },
  { title: '待审核商户申请', count: 0, path: '/merchant/merchant' }
]);

async function loadDashboardData() {
  try {
    const res: any = await request({
      url: '/panel/panel/index',
      method: 'get'
    });
    if (res) {
      const stats = res.panelStatisticalData || res;
      if (stats.todayOrderAmount !== undefined) {
        statCards.value[0].value = Number(stats.todayOrderAmount).toFixed(2);
        statCards.value[0].rate = stats.orderAmountGrowthRate || 0;
      }
      if (stats.todayOrderNum !== undefined) {
        statCards.value[1].value = String(stats.todayOrderNum);
        statCards.value[1].rate = stats.orderNumGrowthRate || 0;
      }
      if (stats.todayUserNum !== undefined) {
        statCards.value[2].value = String(stats.todayUserNum);
      }
      if (stats.todayVisitNum !== undefined) {
        statCards.value[3].value = String(stats.todayVisitNum);
      }

      reminders.value[0].count = res.awaitShip || 0;
      reminders.value[1].count = res.afterSales || 0;
      reminders.value[2].count = res.awaitPay || 0;
      reminders.value[3].count = res.merchantApplyCount || 0;
    }
  } catch (err) {
    console.error('获取罗盘面板数据失败:', err);
  }
}

async function loadChartData() {
  if (!chartInstance) return;
  let dates: string[] = [];
  let values: number[] = [];

  try {
    const res: any = await request({
      url: '/panel/panel/statisticalData',
      method: 'get',
      params: { days: chartDays.value }
    });
    if (res && Array.isArray(res.dates)) {
      dates = res.dates;
      values = res.orderAmountList || res.values || [];
    }
  } catch (err) {
    console.error('获取统计趋势图数据失败:', err);
  }

  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: '¥{value}' }
    },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: values,
        itemStyle: { color: '#1456f0' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(20, 86, 240, 0.28)' },
            { offset: 1, color: 'rgba(20, 86, 240, 0.02)' }
          ])
        }
      }
    ]
  });
}

function handleResize() {
  chartInstance?.resize();
}

onMounted(() => {
  loadDashboardData();
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value);
    loadChartData();
    window.addEventListener('resize', handleResize);
  }
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  chartInstance?.dispose();
});
</script>

<style scoped lang="less">
.tig-dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px;
  margin-bottom: 0;

  .stat-title {
    font-size: 13px;
    color: var(--tig-text-secondary);
    margin-bottom: 8px;
  }
  .stat-val {
    font-size: 24px;
    font-weight: 700;
    color: var(--tig-text-main);
    font-family: Consolas, Monaco, monospace;
    margin-bottom: 6px;
  }
  .stat-trend {
    font-size: 12px;
    color: var(--tig-text-secondary);
    span:last-child {
      margin-left: 6px;
      font-weight: 600;
    }
    .trend-up { color: var(--tig-success); }
    .trend-down { color: var(--tig-danger); }
  }

  .stat-icon {
    width: 52px;
    height: 52px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26px;
  }
}

.reminder-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #eef3ff;
    transform: translateY(-2px);
  }

  .rem-count {
    font-size: 24px;
    font-weight: bold;
    color: var(--tig-primary);
  }
  .rem-label {
    font-size: 13px;
    color: var(--tig-text-regular);
    margin-top: 4px;
  }
}

.echarts-container {
  width: 100%;
  height: 340px;
}
</style>
