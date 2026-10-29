import { useEffect, useRef } from 'react';
import * as echarts from 'echarts/core';
import { LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([LineChart, GridComponent, TooltipComponent, CanvasRenderer]);

interface TrendChartProps {
  data?: number[];
  labels?: string[];
  height?: number;
  name?: string;
  yAxisPrefix?: string;
}

export default function TrendChart({
  data = [18.2, 22.6, 20.4, 28.8, 25.2, 34.6, 31.8],
  labels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  height = 268,
  name = '成交额',
  yAxisPrefix = '¥',
}: TrendChartProps) {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!containerRef.current) return;
    const chart = echarts.init(containerRef.current);
    chart.setOption({
      grid: { left: 8, right: 16, top: 24, bottom: 8, containLabel: true },
      tooltip: {
        trigger: 'axis',
        backgroundColor: '#1c1f23',
        borderWidth: 0,
        textStyle: { color: '#fff', fontSize: 12 },
        formatter: (params: any) => {
          const item = Array.isArray(params) ? params[0] : params;
          return `${item.axisValue}<br/>${name}：${yAxisPrefix === '¥' ? '¥ ' + item.value + ' 万' : item.value}`;
        },
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: labels,
        axisLine: { lineStyle: { color: '#e5e9f0' } },
        axisTick: { show: false },
        axisLabel: { color: '#8a94a6', fontSize: 12, margin: 14 },
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#eef1f6', type: 'dashed' } },
        axisLabel: { color: '#8a94a6', fontSize: 12, formatter: yAxisPrefix === '¥' ? '¥{value}万' : '{value}' },
      },
      series: [
        {
          name,
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 7,
          showSymbol: false,
          data,
          lineStyle: { width: 3, color: '#165dff' },
          itemStyle: { color: '#165dff', borderColor: '#fff', borderWidth: 2 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(22, 93, 255, 0.28)' },
              { offset: 1, color: 'rgba(22, 93, 255, 0.02)' },
            ]),
          },
        },
      ],
    });

    const observer = new ResizeObserver(() => chart.resize());
    observer.observe(containerRef.current);
    return () => {
      observer.disconnect();
      chart.dispose();
    };
  }, [data, labels]);

  return <div ref={containerRef} style={{ width: '100%', height }} />;
}
