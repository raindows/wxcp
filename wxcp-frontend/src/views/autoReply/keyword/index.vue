<template>
  <div class="keyword-page">
    <PageHeader title="关键词回复" description="根据客户发送的关键词自动匹配回复规则">
      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建规则
        </el-button>
      </template>
    </PageHeader>

    <!-- 表格 -->
    <div class="keyword-page__table">
      <el-table v-loading="loading" :data="list" stripe row-key="id">
        <el-table-column prop="keyword" label="关键词" min-width="140" />
        <el-table-column label="匹配方式" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.matchType === 'exact' ? 'danger' : 'warning'" size="small">
              {{ row.matchType === 'exact' ? '精准匹配' : '模糊匹配' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="回复内容预览" min-width="240">
          <template #default="{ row }">
            <span class="keyword-page__content-preview">
              {{ row.replyContent.length > 60 ? row.replyContent.slice(0, 60) + '...' : row.replyContent }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="160" align="center">
          <template #default="{ row }">
            <div class="keyword-page__priority">
              <el-button
                link
                size="small"
                :disabled="row === list[0]"
                @click="handleMoveUp(row)"
              >
                <el-icon><ArrowUp /></el-icon>
              </el-button>
              <el-input-number
                v-model="row.priority"
                :min="1"
                :max="999"
                size="small"
                controls-position="right"
                style="width: 90px"
                @change="(val: number | undefined) => handlePriorityChange(row, val || 1)"
              />
              <el-button
                link
                size="small"
                :disabled="row === list[list.length - 1]"
                @click="handleMoveDown(row)"
              >
                <el-icon><ArrowDown /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
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
            <el-popconfirm title="确定删除该规则？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState
        v-if="!loading && list.length === 0"
        description="暂无关键词规则，点击右上角新建"
      />
    </div>

    <!-- 新建 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑关键词规则' : '新建关键词规则'"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="80px"
        label-position="right"
      >
        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="form.keyword" placeholder="请输入关键词" maxlength="50" />
        </el-form-item>
        <el-form-item label="匹配方式" prop="matchType">
          <el-radio-group v-model="form.matchType">
            <el-radio value="exact">精准匹配</el-radio>
            <el-radio value="fuzzy">模糊匹配</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="回复内容" prop="replyContent">
          <el-input
            v-model="form.replyContent"
            type="textarea"
            :rows="6"
            placeholder="请输入回复内容"
            maxlength="2048"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number
            v-model="form.priority"
            :min="1"
            :max="999"
            controls-position="right"
          />
          <span class="keyword-page__priority-tip">数字越小优先级越高</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  getKeywordRules,
  createKeywordRule,
  updateKeywordRule,
  deleteKeywordRule,
  updateKeywordPriority,
} from '@/api/autoReply'
import type { KeywordRule } from '@/api/autoReply'

// ---------- list state ----------
const loading = ref(false)
const list = ref<KeywordRule[]>([])

// ---------- dialog state ----------
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  id: undefined as number | undefined,
  keyword: '',
  matchType: 'exact' as 'exact' | 'fuzzy',
  replyContent: '',
  priority: 1,
})
const formRules: FormRules = {
  keyword: [{ required: true, message: '请输入关键词', trigger: 'blur' }],
  matchType: [{ required: true, message: '请选择匹配方式', trigger: 'change' }],
  replyContent: [{ required: true, message: '请输入回复内容', trigger: 'blur' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }],
}

// ---------- data loading ----------
async function loadData() {
  loading.value = true
  try {
    const res = await getKeywordRules()
    list.value = res.data
  } catch {
    list.value = mockData
  } finally {
    loading.value = false
  }
}

// ---------- CRUD ----------
function handleCreate() {
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row: KeywordRule) {
  isEdit.value = true
  form.id = row.id
  form.keyword = row.keyword
  form.matchType = row.matchType
  form.replyContent = row.replyContent
  form.priority = row.priority
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
  form.id = undefined
  form.keyword = ''
  form.matchType = 'exact'
  form.replyContent = ''
  form.priority = 1
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateKeywordRule({
        id: form.id,
        keyword: form.keyword,
        matchType: form.matchType,
        replyContent: form.replyContent,
        priority: form.priority,
      })
      ElMessage.success('规则更新成功')
    } else {
      await createKeywordRule({
        keyword: form.keyword,
        matchType: form.matchType,
        replyContent: form.replyContent,
        priority: form.priority,
      })
      ElMessage.success('规则创建成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch {
    // Mock fallback
    if (isEdit.value) {
      const idx = list.value.findIndex((r) => r.id === form.id)
      if (idx !== -1) {
        list.value[idx].keyword = form.keyword
        list.value[idx].matchType = form.matchType
        list.value[idx].replyContent = form.replyContent
        list.value[idx].priority = form.priority
      }
      ElMessage.success('规则更新成功（Mock）')
    } else {
      const newId = Math.max(...list.value.map((r) => r.id), 0) + 1
      list.value.push({
        id: newId,
        keyword: form.keyword,
        matchType: form.matchType,
        replyContent: form.replyContent,
        priority: form.priority,
        status: 1,
        createTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
      })
      sortByPriority()
      ElMessage.success('规则创建成功（Mock）')
    }
    dialogVisible.value = false
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: KeywordRule) {
  try {
    await deleteKeywordRule(row.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch {
    list.value = list.value.filter((r) => r.id !== row.id)
    ElMessage.success('删除成功（Mock）')
  }
}

async function handleToggleStatus(row: KeywordRule, val: boolean) {
  const newStatus = val ? 1 : 0
  try {
    await updateKeywordRule({ id: row.id, status: newStatus })
    row.status = newStatus
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch {
    row.status = newStatus
    ElMessage.success(val ? '已启用（Mock）' : '已禁用（Mock）')
  }
}

// ---------- priority helpers ----------
function sortByPriority() {
  list.value.sort((a, b) => a.priority - b.priority)
}

async function handlePriorityChange(row: KeywordRule, val: number) {
  row.priority = val
  sortByPriority()
  try {
    await updateKeywordPriority(row.id, val)
  } catch {
    // Mock: silently succeed
  }
}

function handleMoveUp(row: KeywordRule) {
  const idx = list.value.findIndex((r) => r.id === row.id)
  if (idx <= 0) return
  const above = list.value[idx - 1]
  const tempPriority = row.priority
  row.priority = above.priority
  above.priority = tempPriority
  sortByPriority()
  handlePriorityChange(row, row.priority)
  handlePriorityChange(above, above.priority)
}

function handleMoveDown(row: KeywordRule) {
  const idx = list.value.findIndex((r) => r.id === row.id)
  if (idx < 0 || idx >= list.value.length - 1) return
  const below = list.value[idx + 1]
  const tempPriority = row.priority
  row.priority = below.priority
  below.priority = tempPriority
  sortByPriority()
  handlePriorityChange(row, row.priority)
  handlePriorityChange(below, below.priority)
}

// ---------- mock data ----------
const mockData: KeywordRule[] = [
  {
    id: 1,
    keyword: '价格',
    matchType: 'exact',
    replyContent: '您好，以下是我们的产品价格表：\n1. 基础版 - ¥99/月\n2. 专业版 - ¥299/月\n3. 企业版 - ¥899/月\n如需了解更多详情，请联系我们的销售团队。',
    priority: 1,
    status: 1,
    createTime: '2026-04-10 09:00:00',
  },
  {
    id: 2,
    keyword: '优惠',
    matchType: 'fuzzy',
    replyContent: '目前我们正在进行春季优惠活动！所有套餐享受8折优惠，活动截止到本月底。详情请咨询在线客服。',
    priority: 2,
    status: 1,
    createTime: '2026-04-12 14:30:00',
  },
  {
    id: 3,
    keyword: '你好',
    matchType: 'exact',
    replyContent: '您好！欢迎咨询，请问有什么可以帮您的？',
    priority: 3,
    status: 1,
    createTime: '2026-04-15 11:00:00',
  },
  {
    id: 4,
    keyword: '地址',
    matchType: 'fuzzy',
    replyContent: '我们的办公地址：北京市朝阳区科技园路100号。工作时间：周一至周五 9:00-18:00。',
    priority: 4,
    status: 0,
    createTime: '2026-04-18 16:00:00',
  },
]

// ---------- lifecycle ----------
onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.keyword-page {
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
    font-size: $font-size-body;
    color: $color-text-regular;
    line-height: 1.5;
  }

  &__priority {
    display: inline-flex;
    align-items: center;
    gap: $spacing-xs;
  }

  &__priority-tip {
    margin-left: $spacing-sm;
    font-size: $font-size-caption;
    color: $color-text-placeholder;
  }
}
</style>
