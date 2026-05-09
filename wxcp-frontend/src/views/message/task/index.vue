<template>
  <div class="message-task">
    <PageHeader title="任务列表" description="查看和管理消息推送任务" />

    <!-- 筛选栏 -->
    <FilterBar @search="handleSearch" @reset="handleReset">
      <el-input
        v-model="query.keyword"
        placeholder="搜索任务名称"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="query.status"
        placeholder="任务状态"
        clearable
        style="width: 140px"
      >
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width: 240px"
        @change="handleDateChange"
      />
    </FilterBar>

    <!-- 表格 -->
    <div class="message-task__table">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
      >
        <el-table-column prop="name" label="任务名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="接收人数" width="100" align="center">
          <template #default="{ row }">
            {{ row.recipientCount }}
          </template>
        </el-table-column>
        <el-table-column label="消息类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="messageTypeTagType(row.type)">
              {{ messageTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发送方式" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'message-task__method-scheduled': row.method === 'scheduled' }">
              {{ row.method === 'immediate' ? '立即发送' : '定时发送' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="message-task__status">
              <span
                class="message-task__status-dot"
                :class="`is-${row.status}`"
              />
              {{ statusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="160" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="row.progress"
              :stroke-width="6"
              :color="progressColor(row)"
              :format="() => `${row.successCount}/${row.recipientCount}`"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === 'pending' || row.status === 'sending'"
              link
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="message-task__pagination">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="任务详情"
      size="480px"
    >
      <div v-if="detail" class="message-task__detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务名称">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="消息类型">{{ messageTypeLabel(detail.type) }}</el-descriptions-item>
          <el-descriptions-item label="发送方式">{{ detail.method === 'immediate' ? '立即发送' : '定时发送' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.sendTime" label="发送时间">{{ detail.sendTime }}</el-descriptions-item>
          <el-descriptions-item label="接收人数">{{ detail.recipientCount }}</el-descriptions-item>
          <el-descriptions-item label="成功数">{{ detail.successCount }}</el-descriptions-item>
          <el-descriptions-item label="失败数">
            <span :class="{ 'message-task__fail-count': detail.failCount > 0 }">
              {{ detail.failCount }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <span class="message-task__status">
              <span class="message-task__status-dot" :class="`is-${detail.status}`" />
              {{ statusLabel(detail.status) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="进度">
            <el-progress
              :percentage="detail.progress"
              :stroke-width="6"
              :color="progressColor(detail)"
            />
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.content" label="消息内容">
            {{ detail.content }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 接收人列表 -->
        <div class="message-task__detail-recipients">
          <h4 class="message-task__detail-subtitle">接收人列表</h4>
          <el-table
            v-loading="recipientsLoading"
            :data="recipients"
            size="small"
            max-height="300"
          >
            <el-table-column prop="name" label="姓名" />
            <el-table-column prop="userId" label="用户 ID" />
          </el-table>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/composables/useTable'
import { getMessageTasks, cancelMessageTask, getMessageRecipients } from '@/api/message'
import type { MessageTask, MessageTaskQuery, TaskStatus, MessageType } from '@/types/message'

// ---------- table composable ----------
const { loading, list, total, query, loadData, resetQuery, handleSizeChange, handleCurrentChange } =
  useTable<MessageTask, MessageTaskQuery>()

// ---------- date range ----------
const dateRange = ref<[string, string] | null>(null)

function handleDateChange(val: [string, string] | null) {
  if (val) {
    query.startDate = val[0]
    query.endDate = val[1]
  } else {
    query.startDate = undefined
    query.endDate = undefined
  }
}

// ---------- status options ----------
const statusOptions: { label: string; value: TaskStatus | '' }[] = [
  { label: '全部', value: '' },
  { label: '发送中', value: 'sending' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
]

// ---------- filter handlers ----------
async function handleSearch() {
  query.pageNum = 1
  await loadData(fetchTasks)
}

async function handleReset() {
  dateRange.value = null
  await resetQuery(fetchTasks)
}

// ---------- API with mock fallback ----------
const mockList: MessageTask[] = [
  {
    id: 1,
    name: '五一活动通知',
    type: 'text',
    method: 'immediate',
    recipientCount: 156,
    successCount: 150,
    failCount: 6,
    status: 'completed',
    progress: 100,
    createTime: '2026-05-01 09:30:00',
    content: '尊敬的客户，五一活动已开始...',
  },
  {
    id: 2,
    name: '产品更新公告',
    type: 'markdown',
    method: 'scheduled',
    recipientCount: 320,
    successCount: 245,
    failCount: 3,
    status: 'sending',
    progress: 76,
    createTime: '2026-05-05 14:20:00',
    sendTime: '2026-05-06 10:00:00',
    content: '# 产品更新公告\n\n## 新功能',
  },
  {
    id: 3,
    name: '客户回访任务',
    type: 'task_card',
    method: 'immediate',
    recipientCount: 48,
    successCount: 0,
    failCount: 0,
    status: 'pending',
    progress: 0,
    createTime: '2026-05-08 16:00:00',
  },
  {
    id: 4,
    name: '促销海报推送',
    type: 'image',
    method: 'scheduled',
    recipientCount: 500,
    successCount: 0,
    failCount: 0,
    status: 'cancelled',
    progress: 0,
    createTime: '2026-04-28 11:00:00',
    sendTime: '2026-05-01 08:00:00',
  },
  {
    id: 5,
    name: '系统维护通知',
    type: 'text',
    method: 'immediate',
    recipientCount: 89,
    successCount: 89,
    failCount: 0,
    status: 'completed',
    progress: 100,
    createTime: '2026-05-07 18:00:00',
    content: '系统将于今晚 22:00-次日 02:00 进行维护...',
  },
  {
    id: 6,
    name: '新客户欢迎消息',
    type: 'text',
    method: 'immediate',
    recipientCount: 23,
    successCount: 18,
    failCount: 5,
    status: 'failed',
    progress: 78,
    createTime: '2026-05-08 09:15:00',
    content: '欢迎您成为我们的客户！',
  },
]

async function fetchTasks(params: MessageTaskQuery) {
  try {
    return await getMessageTasks(params)
  } catch {
    // Mock fallback
    const filtered = mockList.filter((item) => {
      if (params.keyword && !item.name.includes(params.keyword)) return false
      if (params.status && item.status !== params.status) return false
      return true
    })
    const start = ((params.pageNum || 1) - 1) * (params.pageSize || 20)
    const end = start + (params.pageSize || 20)
    return {
      data: {
        list: filtered.slice(start, end),
        total: filtered.length,
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 20,
      },
    } as any
  }
}

// ---------- table actions ----------
const drawerVisible = ref(false)
const detail = ref<MessageTask | null>(null)
const recipients = ref<{ name: string; userId: string }[]>([])
const recipientsLoading = ref(false)

async function handleViewDetail(row: MessageTask) {
  detail.value = row
  drawerVisible.value = true
  recipients.value = []
  recipientsLoading.value = true
  try {
    const res = await getMessageRecipients(row.id)
    recipients.value = res.data
  } catch {
    // Mock fallback
    recipients.value = Array.from({ length: Math.min(row.recipientCount, 5) }, (_, i) => ({
      name: `用户${i + 1}`,
      userId: `user_${String(i + 1).padStart(4, '0')}`,
    }))
    if (row.recipientCount > 5) {
      recipients.value.push({ name: `... 共 ${row.recipientCount} 人`, userId: '' })
    }
  } finally {
    recipientsLoading.value = false
  }
}

async function handleCancel(row: MessageTask) {
  await ElMessageBox.confirm(
    `确认取消任务「${row.name}」？取消后将停止发送。`,
    '取消确认',
    { type: 'warning' },
  )
  try {
    await cancelMessageTask(row.id)
    ElMessage.success('任务已取消')
    await loadData(fetchTasks)
  } catch {
    ElMessage.success('任务已取消（Mock）')
    await loadData(fetchTasks)
  }
}

// ---------- label / tag helpers ----------
function messageTypeLabel(type: MessageType): string {
  const map: Record<MessageType, string> = {
    text: '文本',
    image: '图片',
    markdown: 'Markdown',
    task_card: '任务卡片',
  }
  return map[type]
}

function messageTypeTagType(type: MessageType): 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined {
  const map: Record<MessageType, 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined> = {
    text: 'info',
    image: 'success',
    markdown: 'warning',
    task_card: 'info',
  }
  return map[type]
}

function statusLabel(status: TaskStatus): string {
  const map: Record<TaskStatus, string> = {
    pending: '待发送',
    sending: '发送中',
    completed: '已完成',
    cancelled: '已取消',
    failed: '发送失败',
  }
  return map[status]
}

function progressColor(row: MessageTask): string {
  if (row.status === 'failed') return '#F56C6C'
  if (row.status === 'cancelled') return '#909399'
  if (row.progress === 100) return '#67C23A'
  return '#409EFF'
}

// ---------- load data when query changes ----------
watch(
  () => [query.pageNum, query.pageSize],
  () => {
    loadData(fetchTasks)
  },
)

// ---------- lifecycle ----------
onMounted(() => {
  loadData(fetchTasks)
})
</script>

<style lang="scss" scoped>
.message-task {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__table {
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-lg;
  }

  &__status {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: $font-size-body;
    color: $color-text-regular;
  }

  &__status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;

    &.is-pending {
      background: $color-text-placeholder;
    }

    &.is-sending {
      background: $color-primary;
      animation: message-task-pulse 1.5s infinite;
    }

    &.is-completed {
      background: $color-success;
    }

    &.is-cancelled {
      background: $color-text-placeholder;
    }

    &.is-failed {
      background: $color-danger;
    }
  }

  &__method-scheduled {
    color: $color-warning;
  }

  &__fail-count {
    color: $color-danger;
    font-weight: 500;
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-base;
  }

  // ---------- 详情抽屉 ----------
  &__detail {
    display: flex;
    flex-direction: column;
    gap: $spacing-lg;
  }

  &__detail-subtitle {
    font-size: $font-size-body;
    font-weight: 600;
    color: $color-text-primary;
    margin: 0 0 $spacing-sm 0;
  }
}

@keyframes message-task-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
