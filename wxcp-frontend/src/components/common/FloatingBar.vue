<template>
  <Transition name="floating-bar-fade">
    <div v-if="visible && selectedCount > 0" class="floating-bar">
      <div class="floating-bar__info">
        <span class="floating-bar__count">已选择 <strong>{{ selectedCount }}</strong> 项</span>
      </div>
      <div class="floating-bar__actions">
        <slot />
        <el-button @click="handleClear">
          取消选择
        </el-button>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
defineProps<{
  /** 已选中的行数 */
  selectedCount: number
  /** 是否显示 */
  visible: boolean
}>()

const emit = defineEmits<{
  /** 点击取消选择 */
  (e: 'clear'): void
}>()

function handleClear() {
  emit('clear')
}
</script>

<style scoped lang="scss">
.floating-bar {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  background: $color-bg-card;
  border-radius: $border-radius-card;
  padding: $spacing-sm $spacing-lg;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  border: 1px solid $color-border;

  &__info {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__count {
    font-size: $font-size-body;
    color: $color-text-regular;
    white-space: nowrap;

    strong {
      color: $color-primary;
      font-weight: 600;
    }
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    border-left: 1px solid $color-border;
    padding-left: $spacing-lg;
  }
}

.floating-bar-fade-enter-active,
.floating-bar-fade-leave-active {
  transition: all 0.3s ease;
}

.floating-bar-fade-enter-from,
.floating-bar-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(12px);
}
</style>
