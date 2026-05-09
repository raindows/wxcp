<template>
  <div class="workbench">
    <PageHeader title="工作台" description="欢迎使用 WXCP 营销平台" />

    <!-- 4 core metric cards -->
    <div class="workbench__stats">
      <StatCard
        v-for="stat in statCards"
        :key="stat.label"
        :label="stat.label"
        :value="stat.value"
        :trend="stat.trend"
        :icon="stat.icon"
      />
    </div>

    <!-- Two-column: todo list + quick actions -->
    <div class="workbench__middle">
      <div class="workbench__section workbench__todos">
        <div class="workbench__section-header">
          <h3 class="workbench__section-title">待办事项</h3>
          <el-tag type="info" size="small">{{ todoList.length }} 项</el-tag>
        </div>
        <el-table :data="todoList" style="width: 100%" max-height="420" stripe>
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="todoTypeTag(row.type)" size="small">{{ todoTypeLabel(row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="内容" min-width="180" show-overflow-tooltip />
          <el-table-column prop="customerName" label="客户" width="120" show-overflow-tooltip />
          <el-table-column label="截止时间" width="170">
            <template #default="{ row }">
              <span v-if="row.deadline" :class="{ 'is-overdue': row.status === 'overdue' }">
                {{ row.deadline }}
              </span>
              <span v-else class="text-muted">--</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'overdue' ? 'danger' : 'warning'" size="small" effect="light">
                {{ row.status === 'overdue' ? '已过期' : '待处理' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="workbench__section workbench__shortcuts">
        <div class="workbench__section-header">
          <h3 class="workbench__section-title">快捷操作</h3>
        </div>
        <div class="workbench__shortcut-grid">
          <div
            v-for="item in quickActions"
            :key="item.label"
            class="workbench__shortcut-item"
          >
            <el-icon :size="28"><component :is="item.icon" /></el-icon>
            <span class="workbench__shortcut-label">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent operations -->
    <div class="workbench__section workbench__recent">
      <div class="workbench__section-header">
        <h3 class="workbench__section-title">最近操作</h3>
      </div>
      <el-table :data="recentActions" style="width: 100%" stripe>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="action" label="操作" min-width="180" show-overflow-tooltip />
        <el-table-column prop="target" label="对象" min-width="150" show-overflow-tooltip />
        <el-table-column prop="time" label="时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import { getWorkbenchStats, getTodoList, getRecentActions } from '@/api/dashboard'
import type { WorkbenchStats, TodoItem, TodoItemType, RecentAction } from '@/types/dashboard'

// ---------- reactive data ----------
const stats = reactive<WorkbenchStats>({
  newCustomers: 0,
  newCustomersTrend: 0,
  pendingFollow: 0,
  pendingFollowTrend: 0,
  activeGroupSends: 0,
  activeGroupSendsTrend: 0,
  todayMessages: 0,
  todayMessagesTrend: 0,
})

const todoList = ref<TodoItem[]>([])
const recentActions = ref<RecentAction[]>([])

// ---------- computed ----------
const statCards = computed(() => [
  {
    label: '今日新增客户',
    value: stats.newCustomers,
    trend: { value: stats.newCustomersTrend, isUp: stats.newCustomersTrend >= 0 },
    icon: 'UserFilled',
  },
  {
    label: '待跟进客户',
    value: stats.pendingFollow,
    trend: { value: stats.pendingFollowTrend, isUp: stats.pendingFollowTrend >= 0 },
    icon: 'Phone',
  },
  {
    label: '进行中群发',
    value: stats.activeGroupSends,
    trend: { value: stats.activeGroupSendsTrend, isUp: stats.activeGroupSendsTrend >= 0 },
    icon: 'Promotion',
  },
  {
    label: '今日消息发送量',
    value: stats.todayMessages,
    trend: { value: stats.todayMessagesTrend, isUp: stats.todayMessagesTrend >= 0 },
    icon: 'ChatDotRound',
  },
])

const quickActions = [
  { label: '添加客户', icon: 'UserFilled' },
  { label: '发送消息', icon: 'Promotion' },
  { label: '素材管理', icon: 'PictureFilled' },
  { label: '数据看板', icon: 'DataAnalysis' },
  { label: '应用配置', icon: 'Setting' },
  { label: '系统管理', icon: 'Tools' },
]

// ---------- helpers ----------
function todoTypeLabel(type: TodoItemType): string {
  const map: Record<TodoItemType, string> = {
    follow_up: '跟进提醒',
    task: '待执行任务',
    expired_sop: '过期SOP',
  }
  return map[type]
}

function todoTypeTag(type: TodoItemType): 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined {
  const map: Record<TodoItemType, 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined> = {
    follow_up: 'info',
    task: 'warning',
    expired_sop: 'danger',
  }
  return map[type]
}

// ---------- mock data ----------
const mockStats: WorkbenchStats = {
  newCustomers: 128,
  newCustomersTrend: 12.5,
  pendingFollow: 36,
  pendingFollowTrend: -5.2,
  activeGroupSends: 4,
  activeGroupSendsTrend: 33.3,
  todayMessages: 1847,
  todayMessagesTrend: 8.1,
}

const mockTodos: TodoItem[] = [
  { id: 1, type: 'follow_up', title: '3天未联系，需跟进', customerName: '张明远', deadline: '2026-05-09 18:00', status: 'pending' },
  { id: 2, type: 'follow_up', title: '意向客户回访', customerName: '李晓梅', deadline: '2026-05-09 17:00', status: 'pending' },
  { id: 3, type: 'expired_sop', title: '新客欢迎 SOP 已过期', customerName: '王建国', deadline: '2026-05-08 10:00', status: 'overdue' },
  { id: 4, type: 'task', title: '完成本月客户标签整理', deadline: '2026-05-10 12:00', status: 'pending' },
  { id: 5, type: 'follow_up', title: '合同签约后回访', customerName: '赵云飞', deadline: '2026-05-09 20:00', status: 'pending' },
  { id: 6, type: 'expired_sop', title: '流失预警 SOP 已过期', customerName: '周雅琴', deadline: '2026-05-07 09:00', status: 'overdue' },
  { id: 7, type: 'task', title: '导出上周群发数据报告', deadline: '2026-05-09 23:59', status: 'pending' },
  { id: 8, type: 'follow_up', title: '产品咨询客户跟进', customerName: '陈思宇', deadline: '2026-05-10 09:00', status: 'pending' },
]

const mockRecentActions: RecentAction[] = [
  { id: 1, operator: '王小明', action: '群发消息', target: '春季促销活动 (328人)', time: '2026-05-09 10:32:15' },
  { id: 2, operator: '李丽华', action: '添加客户', target: '刘芳芳', time: '2026-05-09 10:28:03' },
  { id: 3, operator: '王小明', action: '更新标签', target: '张明远 - 高意向客户', time: '2026-05-09 10:15:47' },
  { id: 4, operator: '赵磊', action: '上传素材', target: '产品宣传海报 (3张)', time: '2026-05-09 09:50:22' },
  { id: 5, operator: '李丽华', action: '发送消息', target: '李晓梅 - 产品报价单', time: '2026-05-09 09:42:18' },
]

// ---------- data loading ----------
function assignStats(data: WorkbenchStats) {
  stats.newCustomers = data.newCustomers
  stats.newCustomersTrend = data.newCustomersTrend
  stats.pendingFollow = data.pendingFollow
  stats.pendingFollowTrend = data.pendingFollowTrend
  stats.activeGroupSends = data.activeGroupSends
  stats.activeGroupSendsTrend = data.activeGroupSendsTrend
  stats.todayMessages = data.todayMessages
  stats.todayMessagesTrend = data.todayMessagesTrend
}

async function loadData() {
  try {
    const [statsRes, todosRes, recentRes] = await Promise.all([
      getWorkbenchStats(),
      getTodoList(),
      getRecentActions(),
    ])
    assignStats(statsRes.data)
    todoList.value = todosRes.data
    recentActions.value = recentRes.data
  } catch {
    // Fallback to mock data when API is unavailable
    assignStats(mockStats)
    todoList.value = mockTodos
    recentActions.value = mockRecentActions
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.workbench {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;

  // ---------- 4 metric cards ----------
  &__stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-base;
  }

  // ---------- two-column middle section ----------
  &__middle {
    display: grid;
    grid-template-columns: 1fr 320px;
    gap: $spacing-base;
  }

  // ---------- section card ----------
  &__section {
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-lg;
  }

  &__section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-base;
  }

  &__section-title {
    font-size: $font-size-card-title;
    font-weight: 600;
    color: $color-text-primary;
    margin: 0;
  }

  // ---------- shortcut grid ----------
  &__shortcut-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: $spacing-md;
  }

  &__shortcut-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: $spacing-sm;
    padding: $spacing-lg $spacing-sm;
    border-radius: $border-radius-card;
    cursor: pointer;
    transition: background-color 0.2s ease;
    color: $color-text-regular;

    &:hover {
      background-color: $color-bg-hover;
      color: $color-primary;
    }
  }

  &__shortcut-label {
    font-size: $font-size-caption;
  }

  // ---------- helpers ----------
  .is-overdue {
    color: $color-danger;
  }

  .text-muted {
    color: $color-text-disabled;
  }
}
</style>
