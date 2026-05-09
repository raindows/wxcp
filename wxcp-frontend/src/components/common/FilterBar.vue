<template>
  <div class="filter-bar">
    <div class="filter-bar__fields">
      <slot />
    </div>
    <div class="filter-bar__actions">
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        查询
      </el-button>
      <el-button @click="handleReset">
        <el-icon><RefreshRight /></el-icon>
        重置
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Search, RefreshRight } from '@element-plus/icons-vue'

const emit = defineEmits<{
  /** 点击查询按钮 */
  (e: 'search'): void
  /** 点击重置按钮 */
  (e: 'reset'): void
}>()

function handleSearch() {
  emit('search')
}

function handleReset() {
  emit('reset')
}
</script>

<style scoped lang="scss">
.filter-bar {
  background: $color-bg-card;
  border-radius: $border-radius-card;
  padding: $spacing-base $spacing-lg;
  border: 1px solid $color-border;
  margin-bottom: $spacing-base;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex-wrap: wrap;

  &__fields {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    flex: 1;
    flex-wrap: wrap;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    flex-shrink: 0;
  }
}
</style>
