<template>
  <div class="welcome-page">
    <PageHeader title="欢迎语管理" description="管理客户首次添加时的欢迎消息">
      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建欢迎语
        </el-button>
      </template>
    </PageHeader>

    <!-- 表格 -->
    <div class="welcome-page__table">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column label="内容预览" min-width="240">
          <template #default="{ row }">
            <span class="welcome-page__content-preview">
              {{ row.content.length > 60 ? row.content.slice(0, 60) + '...' : row.content }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="适用成员" width="140" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.userNames.length }} 人</el-tag>
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
            <el-popconfirm title="确定删除该欢迎语？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState
        v-if="!loading && list.length === 0"
        description="暂无欢迎语，点击右上角新建"
      />
    </div>

    <!-- 新建 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑欢迎语' : '新建欢迎语'"
      width="580px"
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
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入欢迎语名称" maxlength="30" />
        </el-form-item>
        <el-form-item label="消息内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入欢迎语内容"
            maxlength="2048"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="适用成员" prop="userIds">
          <el-select
            v-model="form.userIds"
            multiple
            filterable
            placeholder="请选择适用成员"
            style="width: 100%"
          >
            <el-option
              v-for="item in allUsers"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
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
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  getWelcomeList,
  createWelcome,
  updateWelcome,
  deleteWelcome,
} from '@/api/autoReply'
import type { WelcomeMessage } from '@/api/autoReply'

// ---------- list state ----------
const loading = ref(false)
const list = ref<WelcomeMessage[]>([])

// ---------- dialog state ----------
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  content: '',
  userIds: [] as number[],
})
const formRules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  content: [{ required: true, message: '请输入消息内容', trigger: 'blur' }],
  userIds: [{ required: true, message: '请选择适用成员', trigger: 'change', type: 'array', min: 1 }],
}

// ---------- mock users for selector ----------
const allUsers = ref([
  { id: 1, name: '王小明' },
  { id: 2, name: '李丽华' },
  { id: 3, name: '赵磊' },
  { id: 4, name: '陈婷婷' },
  { id: 5, name: '张伟' },
  { id: 6, name: '刘洋' },
])

// ---------- data loading ----------
async function loadData() {
  loading.value = true
  try {
    const res = await getWelcomeList()
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

function handleEdit(row: WelcomeMessage) {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.content = row.content
  form.userIds = [...row.userIds]
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
  form.id = undefined
  form.name = ''
  form.content = ''
  form.userIds = []
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateWelcome({ id: form.id, name: form.name, content: form.content, userIds: form.userIds })
      ElMessage.success('欢迎语更新成功')
    } else {
      await createWelcome({ name: form.name, content: form.content, userIds: form.userIds })
      ElMessage.success('欢迎语创建成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch {
    // Mock fallback
    if (isEdit.value) {
      const idx = list.value.findIndex((r) => r.id === form.id)
      if (idx !== -1) {
        list.value[idx].name = form.name
        list.value[idx].content = form.content
        list.value[idx].userIds = [...form.userIds]
        list.value[idx].userNames = form.userIds.map(
          (uid) => allUsers.value.find((u) => u.id === uid)?.name || '未知',
        )
      }
      ElMessage.success('欢迎语更新成功（Mock）')
    } else {
      const newId = Math.max(...list.value.map((r) => r.id), 0) + 1
      list.value.push({
        id: newId,
        name: form.name,
        content: form.content,
        userIds: [...form.userIds],
        userNames: form.userIds.map(
          (uid) => allUsers.value.find((u) => u.id === uid)?.name || '未知',
        ),
        status: 1,
        createTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
      })
      ElMessage.success('欢迎语创建成功（Mock）')
    }
    dialogVisible.value = false
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: WelcomeMessage) {
  try {
    await deleteWelcome(row.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch {
    list.value = list.value.filter((r) => r.id !== row.id)
    ElMessage.success('删除成功（Mock）')
  }
}

async function handleToggleStatus(row: WelcomeMessage, val: boolean) {
  const newStatus = val ? 1 : 0
  try {
    await updateWelcome({ id: row.id, status: newStatus })
    row.status = newStatus
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch {
    row.status = newStatus
    ElMessage.success(val ? '已启用（Mock）' : '已禁用（Mock）')
  }
}

// ---------- mock data ----------
const mockData: WelcomeMessage[] = [
  {
    id: 1,
    name: '新客户欢迎语',
    content: '您好，感谢您的关注！我是您的专属客服，有任何问题都可以随时联系我哦~',
    userIds: [1, 2],
    userNames: ['王小明', '李丽华'],
    status: 1,
    createTime: '2026-04-15 10:30:00',
  },
  {
    id: 2,
    name: 'VIP欢迎语',
    content: '尊敬的VIP客户，欢迎您！我们将为您提供一对一专属服务，请随时向我们咨询。',
    userIds: [3],
    userNames: ['赵磊'],
    status: 1,
    createTime: '2026-04-20 14:00:00',
  },
  {
    id: 3,
    name: '活动欢迎语',
    content: '欢迎参加我们的活动！活动期间所有咨询均享受优惠，详情请回复"活动"了解。',
    userIds: [1, 3, 4],
    userNames: ['王小明', '赵磊', '陈婷婷'],
    status: 0,
    createTime: '2026-05-01 09:00:00',
  },
]

// ---------- lifecycle ----------
onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.welcome-page {
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
}
</style>
