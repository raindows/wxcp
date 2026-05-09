<template>
  <div class="campaign-sop">
    <PageHeader title="SOP话术" description="管理自动触发的标准话术模板">
      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建话术
        </el-button>
      </template>
    </PageHeader>

    <!-- 筛选栏 -->
    <FilterBar @search="handleSearch" @reset="handleReset">
      <el-input
        v-model="query.keyword"
        placeholder="搜索话术名称"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="queryTriggerType"
        placeholder="触发类型"
        clearable
        style="width: 160px"
      >
        <el-option
          v-for="item in triggerTypeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </FilterBar>

    <!-- 表格 -->
    <div class="campaign-sop__table">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
      >
        <el-table-column prop="name" label="话术名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="触发类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="triggerTypeTagType(row.triggerType)">
              {{ triggerTypeLabel(row.triggerType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="适用范围" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.scopeValue || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="内容预览" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="campaign-sop__content-preview">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              inline-prompt
              active-text="启用"
              inactive-text="停用"
              @change="(val: string | number | boolean) => handleToggleStatus(row, !!val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="campaign-sop__pagination">
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
      :title="dialogEditId ? '编辑话术' : '新建话术'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="话术名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入话术名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="触发类型" prop="triggerType">
          <el-select v-model="formData.triggerType" placeholder="请选择触发类型" style="width: 100%">
            <el-option
              v-for="item in triggerTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="适用范围" prop="scopeValue">
          <el-input v-model="formData.scopeValue" placeholder="如：全部客户 / VIP客户标签" />
        </el-form-item>
        <el-form-item label="话术内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="6"
            placeholder="请输入话术内容"
            maxlength="1000"
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
  getSopTemplates,
  createSopTemplate,
  updateSopTemplate,
  deleteSopTemplate,
} from '@/api/campaign'
import type { SopTemplate, SopTemplateQuery, TriggerType } from '@/types/campaign'

// ---------- table composable ----------
const { loading, list, total, query, loadData, resetQuery, handleSizeChange, handleCurrentChange } =
  useTable<SopTemplate, SopTemplateQuery>()

// ---------- dialog composable ----------
const { visible: dialogVisible, editId: dialogEditId, open: openDialog, close: closeDialog } = useDialog()

// ---------- form ----------
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const queryTriggerType = ref('')

const formData = reactive({
  name: '',
  triggerType: 'new_customer' as TriggerType,
  scopeType: 'tag',
  scopeValue: '',
  content: '',
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入话术名称', trigger: 'blur' }],
  triggerType: [{ required: true, message: '请选择触发类型', trigger: 'change' }],
  scopeValue: [{ required: true, message: '请输入适用范围', trigger: 'blur' }],
  content: [{ required: true, message: '请输入话术内容', trigger: 'blur' }],
}

function resetForm() {
  formData.name = ''
  formData.triggerType = 'new_customer'
  formData.scopeType = 'tag'
  formData.scopeValue = ''
  formData.content = ''
}

// ---------- trigger type options ----------
const triggerTypeOptions: { label: string; value: TriggerType }[] = [
  { label: '新客户添加', value: 'new_customer' },
  { label: '添加天数', value: 'add_days' },
  { label: '手动触发', value: 'manual' },
]

// ---------- mock data ----------
const mockList: SopTemplate[] = [
  {
    id: 1, name: '新客户欢迎语', triggerType: 'new_customer', scopeType: 'tag',
    scopeValue: '全部客户', status: 1,
    content: '您好！欢迎关注我们，我是您的专属顾问，有任何问题随时联系我。',
    createTime: '2026-04-01 10:00:00',
  },
  {
    id: 2, name: '3天跟进话术', triggerType: 'add_days', scopeType: 'tag',
    scopeValue: '新客户标签', status: 1,
    content: '您好，距离您添加已经3天了，请问对我们的产品有什么疑问吗？我可以为您详细介绍。',
    createTime: '2026-04-05 14:30:00',
  },
  {
    id: 3, name: '7天促活话术', triggerType: 'add_days', scopeType: 'department',
    scopeValue: '销售一部', status: 1,
    content: '您好，我们最近推出了新功能，特别适合您这样的客户，要不要了解一下？',
    createTime: '2026-04-10 09:15:00',
  },
  {
    id: 4, name: 'VIP客户关怀', triggerType: 'manual', scopeType: 'tag',
    scopeValue: 'VIP客户标签', status: 0,
    content: '尊敬的VIP客户，感谢您一直以来的支持，为您准备了专属优惠活动...',
    createTime: '2026-04-15 16:00:00',
  },
  {
    id: 5, name: '流失挽回话术', triggerType: 'manual', scopeType: 'tag',
    scopeValue: '30天未活跃客户', status: 1,
    content: '您好，很久没和您联系了。我们近期做了很多改进，希望有机会再次为您服务。',
    createTime: '2026-04-20 11:20:00',
  },
  {
    id: 6, name: '产品推荐话术', triggerType: 'add_days', scopeType: 'tag',
    scopeValue: '高意向客户', status: 1,
    content: '根据您的需求，我为您推荐以下产品方案，您可以看看是否符合预期。',
    createTime: '2026-05-01 08:45:00',
  },
]

async function fetchTemplates(params: SopTemplateQuery) {
  try {
    return await getSopTemplates(params)
  } catch {
    const filtered = mockList.filter((item) => {
      if (params.keyword && !item.name.includes(params.keyword)) return false
      if (queryTriggerType.value && item.triggerType !== queryTriggerType.value) return false
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
  await loadData(fetchTemplates)
}

async function handleReset() {
  queryTriggerType.value = ''
  await resetQuery(fetchTemplates)
}

// ---------- create / edit ----------
function handleCreate() {
  resetForm()
  openDialog()
}

function handleEdit(row: SopTemplate) {
  resetForm()
  formData.name = row.name
  formData.triggerType = row.triggerType
  formData.scopeType = row.scopeType
  formData.scopeValue = row.scopeValue
  formData.content = row.content
  openDialog(row.id)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (dialogEditId.value) {
      await updateSopTemplate({ id: dialogEditId.value, ...formData })
    } else {
      await createSopTemplate({ ...formData, status: 1 })
    }
    ElMessage.success(dialogEditId.value ? '话术更新成功' : '话术创建成功')
    closeDialog()
    await loadData(fetchTemplates)
  } catch {
    ElMessage.success(dialogEditId.value ? '话术更新成功（Mock）' : '话术创建成功（Mock）')
    closeDialog()
    await loadData(fetchTemplates)
  } finally {
    submitLoading.value = false
  }
}

// ---------- toggle status ----------
async function handleToggleStatus(row: SopTemplate, val: boolean) {
  const newStatus = val ? 1 : 0
  try {
    await updateSopTemplate({ id: row.id, status: newStatus } as any)
    row.status = newStatus
    ElMessage.success(val ? '已启用' : '已停用')
  } catch {
    row.status = newStatus
    ElMessage.success(val ? '已启用（Mock）' : '已停用（Mock）')
  }
}

// ---------- delete ----------
async function handleDelete(row: SopTemplate) {
  await ElMessageBox.confirm(
    `确认删除话术「${row.name}」？删除后不可恢复。`,
    '删除确认',
    { type: 'warning' },
  )
  try {
    await deleteSopTemplate(row.id)
    ElMessage.success('话术已删除')
    await loadData(fetchTemplates)
  } catch {
    ElMessage.success('话术已删除（Mock）')
    await loadData(fetchTemplates)
  }
}

// ---------- label helpers ----------
function triggerTypeLabel(type: TriggerType): string {
  const map: Record<TriggerType, string> = {
    new_customer: '新客户添加',
    add_days: '添加天数',
    manual: '手动触发',
  }
  return map[type]
}

function triggerTypeTagType(type: TriggerType): 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined {
  const map: Record<TriggerType, 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined> = {
    new_customer: 'success',
    add_days: 'warning',
    manual: 'info',
  }
  return map[type]
}

// ---------- watch query changes ----------
watch(
  () => [query.pageNum, query.pageSize],
  () => {
    loadData(fetchTemplates)
  },
)

// ---------- lifecycle ----------
onMounted(() => {
  loadData(fetchTemplates)
})
</script>

<style lang="scss" scoped>
.campaign-sop {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__table {
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-lg;
  }

  &__content-preview {
    color: $color-text-regular;
    font-size: $font-size-caption;
    line-height: 1.5;
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-base;
  }
}
</style>
