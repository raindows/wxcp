<template>
  <div class="header">
    <div class="header__left">
      <!-- 折叠按钮 -->
      <el-icon
        class="header__collapse-btn"
        :size="20"
        @click="appStore.toggleSidebar"
      >
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>

      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="header__breadcrumb">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="header__right">
      <!-- 通知徽标 -->
      <el-badge :value="0" :hidden="true" class="header__notification">
        <el-icon :size="18"><Bell /></el-icon>
      </el-badge>

      <!-- 用户下拉 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="header__user">
          <el-avatar :size="28" :src="userStore.userInfo?.avatar || undefined">
            {{ userStore.userInfo?.name?.charAt(0) || 'U' }}
          </el-avatar>
          <span class="header__username">{{ userStore.userInfo?.name || '用户' }}</span>
          <el-icon :size="12"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Fold, Expand, Bell, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { sidebarMenuGroups } from '@/router/routes'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

/** 根据当前路由计算面包屑 */
const breadcrumbs = computed(() => {
  const crumbs: { path: string; title: string }[] = []
  for (const group of sidebarMenuGroups) {
    for (const item of group.items) {
      if (route.path === item.path || route.path.startsWith(item.path + '/')) {
        crumbs.push({ path: item.path, title: item.title })
      }
    }
  }
  return crumbs
})

/** 下拉菜单命令处理 */
async function handleCommand(command: string) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
    } catch {
      return
    }
    await userStore.logout()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.header {
  height: $header-height;
  background-color: $color-bg-card;
  border-bottom: 1px solid $color-border;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-lg;
  position: sticky;
  top: 0;
  z-index: 10;
  flex-shrink: 0;
}

.header__left {
  display: flex;
  align-items: center;
  gap: $spacing-base;
}

.header__collapse-btn {
  cursor: pointer;
  color: $color-text-regular;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;

  &:hover {
    background-color: $color-bg-hover;
    color: $color-text-primary;
  }
}

.header__breadcrumb {
  font-size: $font-size-body;
}

.header__right {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
}

.header__notification {
  cursor: pointer;
  color: $color-text-regular;

  &:hover {
    color: $color-text-primary;
  }
}

.header__user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;

  &:hover {
    background-color: $color-bg-hover;
  }
}

.header__username {
  font-size: $font-size-body;
  color: $color-text-primary;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
