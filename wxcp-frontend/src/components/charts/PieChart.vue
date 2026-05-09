<template>
  <div class="pie-chart">
    <h3 class="pie-chart__title">{{ title }}</h3>
    <div class="pie-chart__body" :style="{ height: height + 'px' }">
      <v-chart :option="chartOption" autoresize />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import {
  TooltipComponent,
  LegendComponent,
} from 'echarts/components'
import type { ComposeOption } from 'echarts/core'
import type { PieSeriesOption } from 'echarts/charts'
import type {
  TooltipComponentOption,
  LegendComponentOption,
} from 'echarts/components'

use([
  CanvasRenderer,
  PieChart,
  TooltipComponent,
  LegendComponent,
])

type ECOption = ComposeOption<
  | PieSeriesOption
  | TooltipComponentOption
  | LegendComponentOption
>

const colors = ['#165DFF', '#00b42a', '#ff7d00', '#f53f3f', '#722ed1', '#0fc6c2', '#f77234', '#9254de']

const props = withDefaults(
  defineProps<{
    title: string
    data: { name: string; value: number }[]
    height?: number
  }>(),
  {
    height: 240,
  },
)

const chartOption = computed<ECOption>(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)',
  },
  legend: {
    type: 'scroll',
    orient: 'vertical',
    right: 8,
    top: 'center',
    icon: 'circle',
    itemWidth: 8,
    itemHeight: 8,
    itemGap: 12,
    textStyle: {
      fontSize: 12,
      color: '#4e5969',
    },
  },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2,
      },
      label: {
        show: false,
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 13,
          fontWeight: 600,
        },
      },
      data: props.data.map((item, i) => ({
        ...item,
        itemStyle: {
          color: colors[i % colors.length],
        },
      })),
    },
  ],
}))
</script>

<style scoped lang="scss">
.pie-chart {
  background: $color-bg-card;
  border-radius: $border-radius-card;
  border: 1px solid $color-border;
  padding: $spacing-lg;

  &__title {
    font-size: $font-size-card-title;
    font-weight: 600;
    color: $color-text-primary;
    margin: 0 0 $spacing-md;
  }

  &__body {
    width: 100%;
  }
}
</style>
