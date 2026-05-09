<template>
  <div class="stat-card">
    <div v-if="icon" class="stat-card__icon">
      <el-icon :size="20"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-card__content">
      <div class="stat-card__label">{{ label }}</div>
      <div class="stat-card__value">{{ value }}</div>
      <div v-if="trend" class="stat-card__trend" :class="trend.isUp ? 'is-up' : 'is-down'">
        <el-icon :size="12">
          <ArrowUp v-if="trend.isUp" />
          <ArrowDown v-else />
        </el-icon>
        <span>{{ Math.abs(trend.value) }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'

interface Trend {
  value: number
  isUp: boolean
}

defineProps<{
  /** 卡片标签，如 "今日新增客户" */
  label: string
  /** 统计数值 */
  value: string | number
  /** 趋势指标 */
  trend?: Trend
  /** Element Plus 图标名称（可选） */
  icon?: string
}>()
</script>

<style scoped lang="scss">
.stat-card {
  display: flex;
  align-items: flex-start;
  gap: $spacing-base;
  background: $color-bg-card;
  border-radius: $border-radius-card;
  padding: $spacing-base $spacing-lg;
  border: 1px solid $color-border;
  transition: box-shadow 0.2s ease;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  }

  &__icon {
    flex-shrink: 0;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $color-primary-light;
    border-radius: $border-radius-card;
    color: $color-primary;
    font-size: 20px;

    :deep(svg) {
      width: 20px;
      height: 20px;
    }
  }

  &__content {
    flex: 1;
    min-width: 0;
  }

  &__label {
    font-size: $font-size-body;
    color: $color-text-placeholder;
    margin-bottom: $spacing-xs;
  }

  &__value {
    font-size: $font-size-stat;
    font-weight: 700;
    color: $color-text-primary;
    line-height: 1.2;
  }

  &__trend {
    display: inline-flex;
    align-items: center;
    gap: 2px;
    font-size: $font-size-caption;
    margin-top: $spacing-xs;

    &.is-up {
      color: $color-success;
    }

    &.is-down {
      color: $color-danger;
    }
  }
}
</style>
