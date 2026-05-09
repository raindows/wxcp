<template>
  <div class="page-header">
    <div class="page-header__title">
      <h2>{{ title }}</h2>
      <p v-if="description" class="page-header__desc">{{ description }}</p>
    </div>
    <div class="page-header__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<style lang="scss" scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-md;
  border-bottom: 1px solid $color-divider;
}

.page-header__title {
  h2 {
    font-size: $font-size-title;
    font-weight: 600;
    color: $color-text-primary;
    margin: 0;
    line-height: 1.4;
  }
}

.page-header__desc {
  font-size: $font-size-body;
  color: $color-text-placeholder;
  margin: 4px 0 0;
}

.page-header__actions {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}
</style>
