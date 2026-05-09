<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-card__logo">
        <el-icon :size="40" color="#165DFF"><Promotion /></el-icon>
      </div>
      <h1 class="login-card__title">WXCP 营销平台</h1>
      <p class="login-card__subtitle">企业微信营销管理工具</p>

      <div class="login-card__divider">
        <span>登录方式</span>
      </div>

      <!-- 模拟登录按钮（开发环境使用） -->
      <el-button
        type="primary"
        size="large"
        class="login-card__btn"
        :loading="loading"
        @click="handleMockLogin"
      >
        模拟登录（开发模式）
      </el-button>

      <p class="login-card__hint">
        生产环境将通过企业微信 OAuth2 自动授权登录
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Promotion } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)

/** 模拟登录（开发环境无后端时使用） */
async function handleMockLogin() {
  loading.value = true
  try {
    // 模拟网络延迟
    await new Promise((resolve) => setTimeout(resolve, 500))
    userStore.mockLogin()
    // 跳转到登录前目标页，或默认工作台
    const redirect = (route.query.redirect as string) || '/dashboard/workbench'
    router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 380px;
  padding: 48px 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  text-align: center;
}

.login-card__logo {
  margin-bottom: 16px;
}

.login-card__title {
  font-size: 24px;
  font-weight: 700;
  color: $color-text-primary;
  margin: 0 0 8px;
}

.login-card__subtitle {
  font-size: 14px;
  color: $color-text-placeholder;
  margin: 0 0 32px;
}

.login-card__divider {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  color: $color-text-disabled;
  font-size: 12px;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: $color-border;
  }

  span {
    padding: 0 12px;
  }
}

.login-card__btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
}

.login-card__hint {
  margin-top: 20px;
  font-size: 12px;
  color: $color-text-disabled;
}
</style>
