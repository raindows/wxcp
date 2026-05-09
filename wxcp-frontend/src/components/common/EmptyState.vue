<template>
  <div class="empty-state">
    <el-empty :description="description" :image-size="imageSize">
      <template v-if="$slots.image" #image>
        <slot name="image" />
      </template>
      <template v-if="$slots.description" #description>
        <slot name="description" />
      </template>
      <template v-if="$slots.action" #default>
        <slot name="action" />
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  /** 空状态描述文案 */
  description?: string
  /** 图片尺寸 */
  imageSize?: number
}>(), {
  description: '暂无数据',
  imageSize: 120,
})
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl 0;
}
</style>
