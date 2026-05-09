<template>
  <div class="sidebar" :class="{ 'is-collapsed': appStore.sidebarCollapsed }">
    <!-- Logo 区域 -->
    <div class="sidebar__logo">
      <el-icon :size="24" color="#165DFF"><Promotion /></el-icon>
      <span v-show="!appStore.sidebarCollapsed" class="sidebar__logo-text">WXCP 营销平台</span>
    </div>

    <!-- 菜单列表 -->
    <el-scrollbar class="sidebar__menu-wrap">
      <template v-for="group in sidebarMenuGroups" :key="group.title">
        <!-- 分组标题 -->
        <div class="sidebar__group-title" v-show="!appStore.sidebarCollapsed">
          {{ group.title }}
        </div>
        <!-- 分组项折叠时显示分隔线 -->
        <div class="sidebar__divider" v-show="appStore.sidebarCollapsed" />
        <!-- 菜单项 -->
        <div
          v-for="item in group.items"
          :key="item.path"
          class="sidebar__item"
          :class="{ 'is-active': isActive(item.path) }"
          :title="item.title"
          @click="handleMenuClick(item.path)"
        >
          <el-icon :size="18">
            <component :is="item.icon" />
          </el-icon>
          <span v-show="!appStore.sidebarCollapsed" class="sidebar__item-text">
            {{ item.title }}
          </span>
        </div>
      </template>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { Promotion } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { sidebarMenuGroups } from '@/router/routes'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

function isActive(path: string): boolean {
  return route.path === path || route.path.startsWith(path + '/')
}

function handleMenuClick(path: string) {
  router.push(path).catch(() => {})
}
</script>

<style lang="scss" scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: $sidebar-width;
  background-color: $sidebar-bg;
  z-index: 1001;
  display: flex;
  flex-direction: column;
  transition: width 0.28s ease;
  overflow: hidden;

  &.is-collapsed {
    width: $sidebar-collapsed-width;
  }
}

.sidebar__logo {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 $spacing-base;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}

.sidebar__logo-text {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar__menu-wrap {
  flex: 1;
  overflow: hidden;
}

.sidebar__group-title {
  padding: 20px 20px 8px;
  font-size: 12px;
  color: $sidebar-group-title;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar__divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.08);
  margin: 12px 12px 4px;
}

.sidebar__item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  height: 40px;
  color: $sidebar-item-default;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
  margin: 2px 0;
  position: relative;

  &:hover {
    background-color: $sidebar-item-hover-bg;
    color: #fff;
  }

  &.is-active {
    background-color: $sidebar-item-active-bg;
    color: #fff;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 20px;
      background-color: #fff;
      border-radius: 0 2px 2px 0;
    }
  }

  .is-collapsed & {
    justify-content: center;
    padding: 0;
  }
}

.sidebar__item-text {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
