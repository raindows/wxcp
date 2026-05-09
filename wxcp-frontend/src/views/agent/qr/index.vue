<template>
  <div class="agent-qr">
    <PageHeader title="坐席二维码" description="管理企微「联系我」二维码，客户扫码可直接添加对应员工">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新建二维码
        </el-button>
      </template>
    </PageHeader>

    <!-- 筛选栏 -->
    <div class="agent-qr__filter">
      <el-input
        v-model="keyword"
        placeholder="搜索名称 / 渠道标识"
        clearable
        style="width: 240px"
        @clear="loadList"
        @keyup.enter="loadList"
      />
      <el-select v-model="filterType" placeholder="类型" clearable style="width: 140px" @change="loadList">
        <el-option label="单人" :value="1" />
        <el-option label="多人" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadList">查询</el-button>
      <el-button @click="handleResetFilter">重置</el-button>
    </div>

    <!-- 卡片列表 -->
    <div v-loading="loading" class="agent-qr__grid">
      <template v-if="list.length">
        <div v-for="item in list" :key="item.id" class="agent-qr__card">
          <div class="agent-qr__card-header">
            <div class="agent-qr__card-title">
              <span class="agent-qr__card-name">{{ item.name }}</span>
              <el-tag :type="item.type === 1 ? '' : 'warning'" size="small" effect="light">
                {{ item.type === 1 ? '单人' : '多人' }}
              </el-tag>
              <el-tag v-if="item.isTemp" type="danger" size="small" effect="light">临时</el-tag>
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

          <div class="agent-qr__card-qr">
            <el-image
              v-if="item.qrCode"
              :src="item.qrCode"
              fit="contain"
              class="agent-qr__card-img"
            >
              <template #error>
                <div class="agent-qr__card-img-placeholder">
                  <el-icon :size="48"><Link /></el-icon>
                  <span>二维码加载失败</span>
                </div>
              </template>
            </el-image>
            <div v-else class="agent-qr__card-img-placeholder">
              <el-icon :size="48"><Iphone /></el-icon>
              <span>小程序联系方式</span>
            </div>
          </div>

          <div class="agent-qr__card-body">
            <div class="agent-qr__card-row">
              <span class="agent-qr__card-label">渠道标识</span>
              <span class="agent-qr__card-value">{{ item.state || '--' }}</span>
            </div>
            <div class="agent-qr__card-row">
              <span class="agent-qr__card-label">关联用户</span>
              <span class="agent-qr__card-value" :title="item.userNames.join('、')">
                {{ item.userNames.join('、') }}
              </span>
            </div>
            <div class="agent-qr__card-row">
              <span class="agent-qr__card-label">无需验证</span>
              <span class="agent-qr__card-value">{{ item.skipVerify ? '是' : '否' }}</span>
            </div>
            <div v-if="item.isTemp" class="agent-qr__card-row">
              <span class="agent-qr__card-label">有效期</span>
              <span class="agent-qr__card-value">{{ formatExpiresIn(item.expiresIn) }}</span>
            </div>
            <div class="agent-qr__card-row">
              <span class="agent-qr__card-label">创建时间</span>
              <span class="agent-qr__card-value">{{ item.createTime }}</span>
            </div>
          </div>

          <div class="agent-qr__card-footer">
            <el-button link type="primary" size="small" :disabled="!item.qrCode" @click="handleDownload(item)">
              <el-icon><Download /></el-icon>
              下载
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(item)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-popconfirm title="确定删除该二维码配置？" @confirm="handleDelete(item)">
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

      <EmptyState v-else description="暂无坐席二维码，点击上方按钮新建" />
    </div>

    <!-- 新建 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogEditId ? '编辑坐席二维码' : '新建坐席二维码'"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="110px"
        label-position="right"
      >
        <el-form-item label="备注名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入备注名称，便于识别" maxlength="30" />
        </el-form-item>
        <el-form-item label="联系方式类型" prop="type">
          <el-radio-group v-model="form.type" @change="handleTypeChange">
            <el-radio :value="1">单人</el-radio>
            <el-radio :value="2">多人（随机分配）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="使用场景" prop="scene">
          <el-radio-group v-model="form.scene">
            <el-radio :value="2">二维码</el-radio>
            <el-radio :value="1">小程序</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关联用户" prop="users">
          <el-select
            v-model="form.users"
            :multiple="form.type === 2"
            filterable
            :placeholder="form.type === 1 ? '请选择用户' : '请选择用户（可多选）'"
            style="width: 100%"
          >
            <el-option
              v-for="user in mockUserList"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="渠道标识" prop="state">
          <el-input v-model="form.state" placeholder="自定义 state，用于区分渠道" maxlength="64" />
        </el-form-item>
        <el-form-item label="无需验证">
          <el-switch v-model="form.skipVerify" />
          <span class="agent-qr__form-tip">开启后客户扫码添加无需好友验证</span>
        </el-form-item>
        <el-form-item label="临时会话">
          <el-switch v-model="form.isTemp" @change="handleTempChange" />
          <span class="agent-qr__form-tip">开启后客户仅在有效期内可联系</span>
        </el-form-item>
        <el-form-item v-if="form.isTemp" label="有效期">
          <el-input-number
            v-model="form.expiresIn"
            :min="60"
            :max="86400"
            :step="3600"
          />
          <span class="agent-qr__form-tip">{{ formatExpiresIn(form.expiresIn) }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogClose">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ dialogEditId ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus, Edit, Delete, Download, Link, Iphone } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useDialog } from '@/composables/useDialog'
import { getAgentQRList, createAgentQR, updateAgentQR, deleteAgentQR } from '@/api/agent'
import type { AgentQRCode, AgentQRCodeForm } from '@/types/agent'

// ---------- dialog composable ----------
const { visible: dialogVisible, editId: dialogEditId, open: dialogOpen, close: dialogClose } = useDialog<number>()

// ---------- list state ----------
const loading = ref(false)
const list = ref<AgentQRCode[]>([])
const keyword = ref('')
const filterType = ref<number | undefined>(undefined)
const submitLoading = ref(false)

// ---------- form ----------
const formRef = ref<FormInstance>()
const form = reactive<AgentQRCodeForm>({
  name: '',
  type: 1,
  scene: 2,
  users: [],
  state: '',
  skipVerify: true,
  isTemp: false,
  expiresIn: 86400,
})

const formRules: FormRules<AgentQRCodeForm> = {
  name: [{ required: true, message: '请输入备注名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择联系方式类型', trigger: 'change' }],
  scene: [{ required: true, message: '请选择使用场景', trigger: 'change' }],
  users: [{ required: true, type: 'array', message: '请选择关联用户', trigger: 'change' }],
}

// ---------- mock user list for select ----------
const mockUserList = [
  { id: 'zhangsan', name: '张三' },
  { id: 'lisi', name: '李四' },
  { id: 'wangwu', name: '王五' },
  { id: 'zhaoliu', name: '赵六' },
  { id: 'sunqi', name: '孙七' },
  { id: 'zhouba', name: '周八' },
]

// ---------- data loading ----------
async function loadList() {
  loading.value = true
  try {
    const res = await getAgentQRList({
      pageNum: 1,
      pageSize: 50,
      keyword: keyword.value || undefined,
      type: filterType.value as 1 | 2 | undefined,
    })
    list.value = res.data.list
  } catch {
    list.value = [...mockList]
  } finally {
    loading.value = false
  }
}

// ---------- filter actions ----------
function handleResetFilter() {
  keyword.value = ''
  filterType.value = undefined
  loadList()
}

// ---------- card actions ----------
function handleAdd() {
  dialogOpen()
}

function handleEdit(item: AgentQRCode) {
  dialogOpen(item.id)
  Object.assign(form, {
    id: item.id,
    name: item.name,
    type: item.type,
    scene: item.scene,
    users: [...item.users],
    state: item.state,
    skipVerify: item.skipVerify,
    isTemp: item.isTemp,
    expiresIn: item.expiresIn,
  })
}

async function handleDelete(item: AgentQRCode) {
  try {
    await deleteAgentQR(item.id)
    ElMessage.success('删除成功')
    await loadList()
  } catch {
    ElMessage.success('删除成功（Mock）')
    list.value = list.value.filter((c) => c.id !== item.id)
  }
}

async function handleToggleStatus(item: AgentQRCode, val: boolean) {
  const newStatus: 0 | 1 = val ? 1 : 0
  try {
    await updateAgentQR({ id: item.id, name: item.name, type: item.type, scene: item.scene, users: item.users, state: item.state, skipVerify: item.skipVerify, isTemp: item.isTemp, expiresIn: item.expiresIn })
    item.status = newStatus
    ElMessage.success(val ? '已启用' : '已停用')
  } catch {
    item.status = newStatus
    ElMessage.success(val ? '已启用（Mock）' : '已停用（Mock）')
  }
}

function handleDownload(item: AgentQRCode) {
  if (!item.qrCode) {
    ElMessage.warning('暂无二维码可下载')
    return
  }
  const a = document.createElement('a')
  a.href = item.qrCode
  a.download = `${item.name}.png`
  a.target = '_blank'
  a.click()
  ElMessage.success('正在下载二维码')
}

// ---------- form actions ----------
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: undefined,
    name: '',
    type: 1,
    scene: 2,
    users: [],
    state: '',
    skipVerify: true,
    isTemp: false,
    expiresIn: 86400,
  })
}

function handleTypeChange() {
  // 切换为单人时，只保留第一个用户
  if (form.type === 1 && form.users.length > 1) {
    form.users = [form.users[0]]
  }
}

function handleTempChange(val: boolean) {
  if (val && form.expiresIn < 60) {
    form.expiresIn = 86400
  }
}

function formatExpiresIn(seconds: number): string {
  if (seconds >= 86400) return `（${Math.round(seconds / 86400)} 天）`
  if (seconds >= 3600) return `（${Math.round(seconds / 3600)} 小时）`
  return `（${Math.round(seconds / 60)} 分钟）`
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (dialogEditId.value) {
      await updateAgentQR({ ...form, id: dialogEditId.value })
      ElMessage.success('更新成功')
    } else {
      await createAgentQR(form)
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

// ---------- mock data ----------
const mockList: AgentQRCode[] = [
  {
    id: 1,
    configId: 'cwAAxxxxxx1',
    name: '张三-销售咨询',
    type: 1,
    scene: 2,
    users: ['zhangsan'],
    userNames: ['张三'],
    qrCode: 'https://via.placeholder.com/200x200?text=ZhangSan+QR',
    state: 'sales_zhangsan',
    skipVerify: true,
    isTemp: false,
    expiresIn: 0,
    status: 1,
    createTime: '2026-04-15 10:00:00',
  },
  {
    id: 2,
    configId: 'cwAAxxxxxx2',
    name: '李四-临时客服',
    type: 1,
    scene: 2,
    users: ['lisi'],
    userNames: ['李四'],
    qrCode: 'https://via.placeholder.com/200x200?text=LiSi+Temp+QR',
    state: 'temp_lisi',
    skipVerify: true,
    isTemp: true,
    expiresIn: 86400,
    status: 1,
    createTime: '2026-04-20 14:30:00',
  },
  {
    id: 3,
    configId: 'cwAAxxxxxx3',
    name: '技术支持组',
    type: 2,
    scene: 2,
    users: ['wangwu', 'zhaoliu', 'sunqi'],
    userNames: ['王五', '赵六', '孙七'],
    qrCode: 'https://via.placeholder.com/200x200?text=TechGroup+QR',
    state: 'tech_support',
    skipVerify: false,
    isTemp: false,
    expiresIn: 0,
    status: 1,
    createTime: '2026-05-01 09:15:00',
  },
  {
    id: 4,
    configId: 'cwAAxxxxxx4',
    name: '周八-小程序咨询',
    type: 1,
    scene: 1,
    users: ['zhouba'],
    userNames: ['周八'],
    qrCode: '',
    state: 'miniapp_zhouba',
    skipVerify: true,
    isTemp: false,
    expiresIn: 0,
    status: 0,
    createTime: '2026-05-05 16:45:00',
  },
]

// ---------- lifecycle ----------
onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.agent-qr {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__filter {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-sm $spacing-lg;
  }

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
    gap: $spacing-xs;
    flex-wrap: wrap;
  }

  &__card-name {
    font-size: $font-size-card-title;
    font-weight: 600;
    color: $color-text-primary;
  }

  &__card-qr {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: $spacing-lg $spacing-base;
    background: $color-bg-hover;
    min-height: 160px;
  }

  &__card-img {
    width: 140px;
    height: 140px;
    border-radius: $border-radius-button;
    border: 1px solid $color-border;
  }

  &__card-img-placeholder {
    width: 140px;
    height: 140px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
    color: $color-text-disabled;
    font-size: $font-size-caption;
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
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__card-footer {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-lg $spacing-base;
    border-top: 1px solid $color-divider;
  }

  &__form-tip {
    margin-left: $spacing-sm;
    font-size: $font-size-caption;
    color: $color-text-placeholder;
  }
}
</style>
