<template>
  <div class="campaign-task">
    <PageHeader title="群发任务" description="管理营销群发和定时推送任务">
      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建任务
        </el-button>
      </template>
    </PageHeader>

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
    </FilterBar>

    <!-- 表格 -->
    <div class="campaign-task__table">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
      >
        <el-table-column prop="name" label="任务名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.type === 'scheduled' ? 'warning' : 'info'">
              {{ row.type === 'scheduled' ? '定时群发' : '即时群发' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scopeName" label="发送范围" width="140" show-overflow-tooltip />
        <el-table-column label="接收人数" width="100" align="center">
          <template #default="{ row }">
            {{ row.recipientCount }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="campaign-task__status">
              <span class="campaign-task__status-dot" :class="`is-${row.status}`" />
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
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              v-if="row.status === 'pending' || row.status === 'sending'"
              link
              type="warning"
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.status === 'pending' || row.status === 'cancelled'"
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="campaign-task__pagination">
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

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogEditId ? '编辑任务' : '新建任务'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入任务名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="任务类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio value="immediate">即时群发</el-radio>
            <el-radio value="scheduled">定时群发</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发送范围" prop="scopeName">
          <el-input v-model="formData.scopeName" placeholder="如：高意向客户标签 / 销售部" />
        </el-form-item>
        <el-form-item label="接收人数" prop="recipientCount">
          <el-input-number v-model="formData.recipientCount" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="消息内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="4"
            placeholder="请输入群发消息内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useTable } from '@/composables/useTable'
import { useDialog } from '@/composables/useDialog'
import {
  getCampaignTasks,
  createCampaignTask,
  updateCampaignTask,
  deleteCampaignTask,
  cancelCampaignTask,
} from '@/api/campaign'
import type { CampaignTask, CampaignTaskQuery, CampaignStatus } from '@/types/campaign'

// ---------- table composable ----------
const { loading, list, total, query, loadData, resetQuery, handleSizeChange, handleCurrentChange } =
  useTable<CampaignTask, CampaignTaskQuery>()

// ---------- dialog composable ----------
const { visible: dialogVisible, editId: dialogEditId, open: openDialog, close: closeDialog } = useDialog()

// ---------- form ----------
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const formData = reactive({
  name: '',
  type: 'immediate' as string,
  scopeName: '',
  recipientCount: 100,
  content: '',
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  scopeName: [{ required: true, message: '请输入发送范围', trigger: 'blur' }],
  recipientCount: [{ required: true, message: '请输入接收人数', trigger: 'blur' }],
}

function resetForm() {
  formData.name = ''
  formData.type = 'immediate'
  formData.scopeName = ''
  formData.recipientCount = 100
  formData.content = ''
}

// ---------- status options ----------
const statusOptions: { label: string; value: CampaignStatus | '' }[] = [
  { label: '全部', value: '' },
  { label: '待发送', value: 'pending' },
  { label: '发送中', value: 'sending' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
  { label: '发送失败', value: 'failed' },
]

// ---------- mock data ----------
const mockList: CampaignTask[] = [
  {
    id: 1, name: '春季促销活动通知', type: 'immediate', scopeName: '高意向客户标签',
    recipientCount: 256, successCount: 248, status: 'completed', progress: 100,
    createTime: '2026-03-15 09:30:00',
  },
  {
    id: 2, name: '产品更新公告', type: 'scheduled', scopeName: '全部客户',
    recipientCount: 1200, successCount: 876, status: 'sending', progress: 73,
    createTime: '2026-05-01 14:20:00',
  },
  {
    id: 3, name: '客户回访计划', type: 'immediate', scopeName: '30天未活跃客户',
    recipientCount: 85, successCount: 0, status: 'pending', progress: 0,
    createTime: '2026-05-08 10:00:00',
  },
  {
    id: 4, name: '节日祝福推送', type: 'scheduled', scopeName: 'VIP客户标签',
    recipientCount: 320, successCount: 0, status: 'cancelled', progress: 0,
    createTime: '2026-04-28 16:00:00',
  },
  {
    id: 5, name: '新功能上线通知', type: 'immediate', scopeName: '销售部',
    recipientCount: 48, successCount: 45, status: 'completed', progress: 94,
    createTime: '2026-05-06 11:30:00',
  },
  {
    id: 6, name: '市场调研问卷', type: 'immediate', scopeName: '技术部',
    recipientCount: 120, successCount: 98, status: 'failed', progress: 82,
    createTime: '2026-05-07 15:45:00',
  },
  {
    id: 7, name: '年中活动预热', type: 'scheduled', scopeName: '新客户标签',
    recipientCount: 430, successCount: 0, status: 'pending', progress: 0,
    createTime: '2026-05-09 08:00:00',
  },
]

async function fetchTasks(params: CampaignTaskQuery) {
  try {
    return await getCampaignTasks(params)
  } catch {
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

// ---------- filter handlers ----------
async function handleSearch() {
  query.pageNum = 1
  await loadData(fetchTasks)
}

async function handleReset() {
  await resetQuery(fetchTasks)
}

// ---------- create / edit ----------
function handleCreate() {
  resetForm()
  openDialog()
}

function handleEdit(row: CampaignTask) {
  resetForm()
  formData.name = row.name
  formData.type = row.type
  formData.scopeName = row.scopeName
  formData.recipientCount = row.recipientCount
  openDialog(row.id)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (dialogEditId.value) {
      await updateCampaignTask({ id: dialogEditId.value, ...formData })
    } else {
      await createCampaignTask(formData)
    }
    ElMessage.success(dialogEditId.value ? '任务更新成功' : '任务创建成功')
    closeDialog()
    await loadData(fetchTasks)
  } catch {
    ElMessage.success(dialogEditId.value ? '任务更新成功（Mock）' : '任务创建成功（Mock）')
    closeDialog()
    await loadData(fetchTasks)
  } finally {
    submitLoading.value = false
  }
}

// ---------- cancel / delete ----------
async function handleCancel(row: CampaignTask) {
  await ElMessageBox.confirm(
    `确认取消任务「${row.name}」？取消后将停止发送。`,
    '取消确认',
    { type: 'warning' },
  )
  try {
    await cancelCampaignTask(row.id)
    ElMessage.success('任务已取消')
    await loadData(fetchTasks)
  } catch {
    ElMessage.success('任务已取消（Mock）')
    await loadData(fetchTasks)
  }
}

async function handleDelete(row: CampaignTask) {
  await ElMessageBox.confirm(
    `确认删除任务「${row.name}」？删除后不可恢复。`,
    '删除确认',
    { type: 'warning' },
  )
  try {
    await deleteCampaignTask(row.id)
    ElMessage.success('任务已删除')
    await loadData(fetchTasks)
  } catch {
    ElMessage.success('任务已删除（Mock）')
    await loadData(fetchTasks)
  }
}

// ---------- label helpers ----------
function statusLabel(status: CampaignStatus): string {
  const map: Record<CampaignStatus, string> = {
    pending: '待发送',
    sending: '发送中',
    completed: '已完成',
    cancelled: '已取消',
    failed: '发送失败',
  }
  return map[status]
}

function progressColor(row: CampaignTask): string {
  if (row.status === 'failed') return '#F56C6C'
  if (row.status === 'cancelled') return '#909399'
  if (row.progress === 100) return '#00b42a'
  return '#165DFF'
}

// ---------- watch query changes ----------
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
.campaign-task {
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
      animation: campaign-task-pulse 1.5s infinite;
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

  &__pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-base;
  }
}

@keyframes campaign-task-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
