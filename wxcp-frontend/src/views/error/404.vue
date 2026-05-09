<template>
  <div class="not-found">
    <div class="not-found-content">
      <h1 class="not-found-code">404</h1>
      <p class="not-found-text">抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped lang="scss">
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: $color-bg-page;
}

.not-found-content {
  text-align: center;
}

.not-found-code {
  font-size: 72px;
  font-weight: 700;
  color: $color-primary;
  margin: 0 0 16px;
}

.not-found-text {
  font-size: 16px;
  color: $color-text-regular;
  margin: 0 0 24px;
}
</style>
