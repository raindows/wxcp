<template>
  <div class="rank-list">
    <h3 class="rank-list__title">{{ title }}</h3>
    <div class="rank-list__body">
      <div
        v-for="(item, index) in data.slice(0, max)"
        :key="item.name"
        class="rank-list__item"
      >
        <div class="rank-list__info">
          <span
            class="rank-list__badge"
            :class="{
              'is-gold': index === 0,
              'is-silver': index === 1,
              'is-bronze': index === 2,
            }"
          >
            {{ index + 1 }}
          </span>
          <span class="rank-list__name">{{ item.name }}</span>
        </div>
        <div class="rank-list__bar-wrapper">
          <div class="rank-list__bar">
            <div
              class="rank-list__bar-fill"
              :style="{ width: getPercent(item.value) + '%' }"
            />
          </div>
          <span class="rank-list__value">{{ item.value }}</span>
        </div>
      </div>
      <div v-if="data.length === 0" class="rank-list__empty">暂无数据</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    title: string
    data: { name: string; value: number }[]
    max?: number
  }>(),
  {
    max: 5,
  },
)

const maxValue = computed(() => {
  if (props.data.length === 0) return 1
  return Math.max(...props.data.map((d) => d.value))
})

function getPercent(value: number): number {
  return Math.round((value / maxValue.value) * 100)
}
</script>

<style scoped lang="scss">
.rank-list {
  background: $color-bg-card;
  border-radius: $border-radius-card;
  border: 1px solid $color-border;
  padding: $spacing-lg;

  &__title {
    font-size: $font-size-card-title;
    font-weight: 600;
    color: $color-text-primary;
    margin: 0 0 $spacing-base;
  }

  &__body {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
  }

  &__item {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  &__info {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__badge {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    font-weight: 600;
    color: $color-text-placeholder;
    background: $color-bg-hover;
    flex-shrink: 0;

    &.is-gold {
      background: #fff7e8;
      color: #ff7d00;
    }

    &.is-silver {
      background: #f2f3f5;
      color: #86909c;
    }

    &.is-bronze {
      background: #fff2e8;
      color: #f77234;
    }
  }

  &__name {
    font-size: $font-size-body;
    color: $color-text-regular;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__bar-wrapper {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    margin-left: 28px;
  }

  &__bar {
    flex: 1;
    height: 8px;
    background: $color-bg-hover;
    border-radius: 4px;
    overflow: hidden;
  }

  &__bar-fill {
    height: 100%;
    background: linear-gradient(90deg, $color-primary, $color-primary-hover);
    border-radius: 4px;
    transition: width 0.6s ease;
  }

  &__value {
    font-size: $font-size-caption;
    color: $color-text-regular;
    font-weight: 500;
    min-width: 36px;
    text-align: right;
  }

  &__empty {
    text-align: center;
    padding: 32px 0;
    color: $color-text-placeholder;
    font-size: $font-size-body;
  }
}
</style>
