<template>
  <div class="trend-chart">
    <div class="trend-chart__header">
      <h3 class="trend-chart__title">{{ title }}</h3>
      <div class="trend-chart__actions">
        <el-button-group size="small">
          <el-button
            :type="activeType === 'bar' ? 'primary' : 'default'"
            @click="activeType = 'bar'"
          >
            柱状图
          </el-button>
          <el-button
            :type="activeType === 'line' ? 'primary' : 'default'"
            @click="activeType = 'line'"
          >
            折线图
          </el-button>
        </el-button-group>
      </div>
    </div>
    <div class="trend-chart__body" :style="{ height: height + 'px' }">
      <v-chart :option="chartOption" autoresize />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart } from 'echarts/charts'
import {
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components'
import type { ComposeOption } from 'echarts/core'
import type { BarSeriesOption, LineSeriesOption } from 'echarts/charts'
import type {
  TooltipComponentOption,
  LegendComponentOption,
  GridComponentOption,
} from 'echarts/components'

use([
  CanvasRenderer,
  BarChart,
  LineChart,
  TooltipComponent,
  LegendComponent,
  GridComponent,
])

type ECOption = ComposeOption<
  | BarSeriesOption
  | LineSeriesOption
  | TooltipComponentOption
  | LegendComponentOption
  | GridComponentOption
>

interface SeriesItem {
  name: string
  data: number[]
  type?: 'bar' | 'line'
}

const props = withDefaults(
  defineProps<{
    title: string
    xData: string[]
    series: SeriesItem[]
    height?: number
  }>(),
  {
    height: 240,
  },
)

const activeType = ref<'bar' | 'line'>('bar')

const colors = ['#165DFF', '#00b42a']

const chartOption = computed<ECOption>(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: activeType.value === 'bar' ? 'shadow' : 'cross',
    },
  },
  legend: {
    data: props.series.map((s) => s.name),
    bottom: 0,
    icon: 'roundRect',
    itemWidth: 12,
    itemHeight: 3,
    textStyle: {
      fontSize: 12,
      color: '#86909c',
    },
  },
  grid: {
    left: 8,
    right: 16,
    bottom: 36,
    top: 16,
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: props.xData,
    axisLine: { lineStyle: { color: '#e5e6eb' } },
    axisTick: { show: false },
    axisLabel: {
      color: '#86909c',
      fontSize: 12,
    },
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#f2f3f5', type: 'dashed' } },
    axisLabel: {
      color: '#86909c',
      fontSize: 12,
    },
    axisLine: { show: false },
    axisTick: { show: false },
  },
  series: props.series.map((s, i) => ({
    name: s.name,
    type: activeType.value,
    data: s.data,
    barWidth: props.xData.length > 15 ? 12 : 20,
    itemStyle: {
      color: colors[i % colors.length],
      borderRadius: activeType.value === 'bar' ? [4, 4, 0, 0] : 0,
    },
    lineStyle: {
      width: 2,
      smooth: true,
    },
    areaStyle:
      activeType.value === 'line'
        ? {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: colors[i % colors.length] + '40' },
                { offset: 1, color: colors[i % colors.length] + '05' },
              ],
            },
          }
        : undefined,
    smooth: true,
    symbol: activeType.value === 'line' ? 'circle' : undefined,
    symbolSize: activeType.value === 'line' ? 6 : undefined,
  })),
}))
</script>

<style scoped lang="scss">
.trend-chart {
  background: $color-bg-card;
  border-radius: $border-radius-card;
  border: 1px solid $color-border;
  padding: $spacing-lg;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-md;
  }

  &__title {
    font-size: $font-size-card-title;
    font-weight: 600;
    color: $color-text-primary;
    margin: 0;
  }

  &__body {
    width: 100%;
  }
}
</style>
