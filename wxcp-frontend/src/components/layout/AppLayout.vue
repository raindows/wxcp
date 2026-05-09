<template>
  <div class="app-layout">
    <Sidebar />
    <div class="app-layout__main" :class="{ 'is-collapsed': appStore.sidebarCollapsed }">
      <Header />
      <div class="app-layout__content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'

const appStore = useAppStore()
</script>

<style lang="scss" scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  width: 100%;
}

.app-layout__main {
  flex: 1;
  margin-left: $sidebar-width;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left 0.28s ease;

  &.is-collapsed {
    margin-left: $sidebar-collapsed-width;
  }
}

.app-layout__content {
  flex: 1;
  padding: $spacing-lg $spacing-xl;
  background-color: $color-bg-page;
  overflow-y: auto;
}
</style>
