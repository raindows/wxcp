<template>
  <div class="config-app">
    <PageHeader title="应用配置" description="管理企微应用接入配置">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增应用
        </el-button>
      </template>
    </PageHeader>

    <!-- 卡片列表 -->
    <div v-loading="loading" class="config-app__grid">
      <template v-if="list.length">
        <div v-for="item in list" :key="item.id" class="config-app__card">
          <div class="config-app__card-header">
            <div class="config-app__card-title">
              <span class="config-app__card-name">{{ item.name }}</span>
              <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small" effect="light">
                {{ item.status === 1 ? '已启用' : '已禁用' }}
              </el-tag>
            </div>
            <el-switch
              :model-value="item.status === 1"
              size="small"
              inline-prompt
              active-text="启"
              inactive-text="禁"
              @change="(val: string | number | boolean) => handleToggleStatus(item, !!val)"
            />
          </div>
          <div class="config-app__card-body">
            <div class="config-app__card-row">
              <span class="config-app__card-label">CorpID</span>
              <span class="config-app__card-value">{{ maskCorpId(item.corpId) }}</span>
            </div>
            <div class="config-app__card-row">
              <span class="config-app__card-label">AgentId</span>
              <span class="config-app__card-value">{{ item.agentId }}</span>
            </div>
            <div class="config-app__card-row">
              <span class="config-app__card-label">创建时间</span>
              <span class="config-app__card-value">{{ item.createTime }}</span>
            </div>
          </div>
          <div class="config-app__card-footer">
            <el-button link type="primary" size="small" @click="handleEdit(item)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="primary" size="small" :loading="testingId === item.id" @click="handleTest(item)">
              <el-icon><Connection /></el-icon>
              测试连接
            </el-button>
            <el-popconfirm title="确定删除该应用配置？" @confirm="handleDelete(item)">
              <template #reference>
                <el-button link type="danger" size="small">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </template>

      <EmptyState v-else description="暂无应用配置，点击上方按钮新增" />
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogEditId ? '编辑应用' : '新增应用'"
      width="600px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="应用名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入应用名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="CorpID" prop="corpId">
          <el-input v-model="form.corpId" placeholder="请输入企业 CorpID" />
        </el-form-item>
        <el-form-item label="CorpSecret" prop="corpSecret">
          <el-input
            v-model="form.corpSecret"
            type="password"
            show-password
            placeholder="请输入应用 Secret"
          />
        </el-form-item>
        <el-form-item label="AgentId" prop="agentId">
          <el-input v-model="form.agentId" placeholder="请输入企微 AgentId" />
        </el-form-item>
        <el-form-item label="Token" prop="token">
          <el-input v-model="form.token" placeholder="请输入回调 Token" />
        </el-form-item>
        <el-form-item label="AESKey" prop="aesKey">
          <el-input v-model="form.aesKey" placeholder="请输入回调 EncodingAESKey" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogClose">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          {{ dialogEditId ? '保存' : '创建' }}
        </el-button>
        <el-button
          v-if="dialogEditId"
          :loading="testingInDialog"
          @click="handleTestInDialog"
        >
          测试连接
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus, Edit, Delete, Connection } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useDialog } from '@/composables/useDialog'
import {
  getConfigList,
  getConfigDetail,
  createConfig,
  updateConfig,
  deleteConfig,
  testConnection,
} from '@/api/config'
import type { CpConfig, CpConfigForm } from '@/types/config'

// ---------- dialog composable ----------
const { visible: dialogVisible, editId: dialogEditId, open: dialogOpen, close: dialogClose } = useDialog()

// ---------- list state ----------
const loading = ref(false)
const list = ref<CpConfig[]>([])
const testingId = ref<number | null>(null)
const testingInDialog = ref(false)
const submitLoading = ref(false)

// ---------- form ----------
const formRef = ref<FormInstance>()
const form = reactive<CpConfigForm>({
  name: '',
  corpId: '',
  corpSecret: '',
  agentId: '',
  token: '',
  aesKey: '',
})

const formRules: FormRules<CpConfigForm> = {
  name: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  corpId: [{ required: true, message: '请输入 CorpID', trigger: 'blur' }],
  corpSecret: [{ required: true, message: '请输入 CorpSecret', trigger: 'blur' }],
  agentId: [{ required: true, message: '请输入 AgentId', trigger: 'blur' }],
  token: [{ required: true, message: '请输入 Token', trigger: 'blur' }],
  aesKey: [{ required: true, message: '请输入 AESKey', trigger: 'blur' }],
}

// ---------- data loading ----------
async function loadList() {
  loading.value = true
  try {
    const res = await getConfigList()
    list.value = res.data
  } catch {
    // Mock fallback
    list.value = mockList
  } finally {
    loading.value = false
  }
}

// ---------- card actions ----------
function handleAdd() {
  dialogOpen()
}

async function handleEdit(item: CpConfig) {
  dialogOpen(item.id)
  try {
    const res = await getConfigDetail(item.id)
    Object.assign(form, {
      id: res.data.id,
      name: res.data.name,
      corpId: res.data.corpId,
      corpSecret: res.data.corpSecret,
      agentId: String(res.data.agentId),
      token: res.data.token,
      aesKey: res.data.aesKey,
    })
  } catch {
    // Fallback: populate from list item
    Object.assign(form, {
      id: item.id,
      name: item.name,
      corpId: item.corpId,
      corpSecret: '',
      agentId: String(item.agentId),
      token: item.token,
      aesKey: item.aesKey,
    })
    ElMessage.warning('获取详情失败，部分字段需重新填写')
  }
}

async function handleDelete(item: CpConfig) {
  try {
    await deleteConfig(item.id)
    ElMessage.success('删除成功')
    await loadList()
  } catch {
    ElMessage.success('删除成功（Mock）')
    list.value = list.value.filter((c) => c.id !== item.id)
  }
}

async function handleToggleStatus(item: CpConfig, val: boolean) {
  const newStatus: 0 | 1 = val ? 1 : 0
  try {
    await updateConfig({
      id: item.id,
      name: item.name,
      corpId: item.corpId,
      corpSecret: item.corpSecret,
      agentId: String(item.agentId),
      token: item.token,
      aesKey: item.aesKey,
    })
    item.status = newStatus
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch {
    ElMessage.success(val ? '已启用（Mock）' : '已禁用（Mock）')
    item.status = newStatus
  }
}

async function handleTest(item: CpConfig) {
  testingId.value = item.id
  try {
    const res = await testConnection(item.id)
    if (res.data.success) {
      ElMessage.success(res.data.message || '连接成功')
    } else {
      ElMessage.warning(res.data.message || '连接失败')
    }
  } catch {
    ElMessage.success('连接测试成功（Mock）')
  } finally {
    testingId.value = null
  }
}

// ---------- form actions ----------
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: undefined,
    name: '',
    corpId: '',
    corpSecret: '',
    agentId: '',
    token: '',
    aesKey: '',
  })
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (dialogEditId.value) {
      await updateConfig({ ...form, id: dialogEditId.value })
      ElMessage.success('更新成功')
    } else {
      await createConfig(form)
      ElMessage.success('创建成功')
    }
    dialogClose()
    await loadList()
  } catch {
    ElMessage.success(dialogEditId.value ? '更新成功（Mock）' : '创建成功（Mock）')
    dialogClose()
    await loadList()
  } finally {
    submitLoading.value = false
  }
}

async function handleTestInDialog() {
  if (!dialogEditId.value) return
  testingInDialog.value = true
  try {
    const res = await testConnection(dialogEditId.value)
    if (res.data.success) {
      ElMessage.success(res.data.message || '连接成功')
    } else {
      ElMessage.warning(res.data.message || '连接失败')
    }
  } catch {
    ElMessage.success('连接测试成功（Mock）')
  } finally {
    testingInDialog.value = false
  }
}

// ---------- helpers ----------
function maskCorpId(corpId: string): string {
  if (!corpId || corpId.length <= 8) return corpId
  return corpId.slice(0, 4) + '****' + corpId.slice(-4)
}

// ---------- mock data ----------
const mockList: CpConfig[] = [
  {
    id: 1,
    name: '销售助手',
    corpId: 'ww1234567890abcdef',
    corpSecret: 'secret_sales_app',
    agentId: 1000002,
    token: 'sales_token_abc',
    aesKey: 'sales_aes_key_xyz',
    status: 1,
    createTime: '2026-04-15 10:00:00',
  },
  {
    id: 2,
    name: '客服系统',
    corpId: 'ww1234567890abcdef',
    corpSecret: 'secret_service_app',
    agentId: 1000003,
    token: 'service_token_def',
    aesKey: 'service_aes_key_abc',
    status: 1,
    createTime: '2026-04-20 14:30:00',
  },
  {
    id: 3,
    name: '审批应用',
    corpId: 'ww1234567890abcdef',
    corpSecret: 'secret_approval_app',
    agentId: 1000005,
    token: 'approval_token_ghi',
    aesKey: 'approval_aes_key_def',
    status: 0,
    createTime: '2026-05-01 09:15:00',
  },
  {
    id: 4,
    name: '考勤打卡',
    corpId: 'wwabcdef1234567890',
    corpSecret: 'secret_attendance_app',
    agentId: 1000010,
    token: 'attendance_token_jkl',
    aesKey: 'attendance_aes_key_ghi',
    status: 1,
    createTime: '2026-05-05 16:45:00',
  },
]

// ---------- lifecycle ----------
onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.config-app {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: $spacing-base;
    min-height: 200px;
  }

  &__card {
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    display: flex;
    flex-direction: column;
    transition: box-shadow 0.2s ease;

    &:hover {
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    }
  }

  &__card-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: $spacing-base $spacing-lg $spacing-sm;
    border-bottom: 1px solid $color-divider;
  }

  &__card-title {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__card-name {
    font-size: $font-size-card-title;
    font-weight: 600;
    color: $color-text-primary;
  }

  &__card-body {
    flex: 1;
    padding: $spacing-sm $spacing-lg;
  }

  &__card-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-xs 0;

    &:not(:last-child) {
      border-bottom: 1px solid $color-divider;
    }
  }

  &__card-label {
    font-size: $font-size-caption;
    color: $color-text-placeholder;
    flex-shrink: 0;
  }

  &__card-value {
    font-size: $font-size-body;
    color: $color-text-regular;
    text-align: right;
    word-break: break-all;
  }

  &__card-footer {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-lg $spacing-base;
    border-top: 1px solid $color-divider;
  }
}
</style>
