<template>
  <div class="message-push">
    <PageHeader title="消息推送" description="向客户或群成员发送消息" />

    <!-- Step 1: 消息类型选择 -->
    <section class="message-push__section">
      <h3 class="message-push__section-title">
        <span class="message-push__step">1</span>
        选择消息类型
      </h3>
      <div class="message-push__type-cards">
        <div
          v-for="item in messageTypeOptions"
          :key="item.value"
          class="message-push__type-card"
          :class="{ 'is-active': form.type === item.value }"
          @click="form.type = item.value"
        >
          <el-icon :size="28" class="message-push__type-icon">
            <component :is="item.icon" />
          </el-icon>
          <span class="message-push__type-label">{{ item.label }}</span>
        </div>
      </div>
    </section>

    <!-- Step 2: 消息编辑区 -->
    <section class="message-push__section">
      <h3 class="message-push__section-title">
        <span class="message-push__step">2</span>
        编辑消息内容
      </h3>

      <!-- 文本消息 -->
      <div v-if="form.type === 'text'" class="message-push__editor">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="6"
          placeholder="请输入文本消息内容..."
          maxlength="2048"
          show-word-limit
        />
      </div>

      <!-- 图片消息 -->
      <div v-else-if="form.type === 'image'" class="message-push__editor">
        <el-upload
          class="message-push__upload"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleImageChange"
          accept="image/*"
          :limit="1"
        >
          <el-icon :size="40" class="message-push__upload-icon"><Upload /></el-icon>
          <div class="message-push__upload-text">将图片拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="message-push__upload-tip">支持 JPG、PNG 格式，大小不超过 2MB</div>
          </template>
        </el-upload>
        <el-image
          v-if="imagePreviewUrl"
          :src="imagePreviewUrl"
          fit="contain"
          class="message-push__preview"
        />
      </div>

      <!-- Markdown 消息 -->
      <div v-else-if="form.type === 'markdown'" class="message-push__editor">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="10"
          placeholder="请输入 Markdown 内容..."
          maxlength="4096"
          show-word-limit
          class="message-push__markdown-input"
        />
        <div class="message-push__markdown-tip">
          支持 Markdown 语法，可包含链接、图片等富文本内容
        </div>
      </div>

      <!-- 任务卡片消息 -->
      <div v-else-if="form.type === 'task_card'" class="message-push__editor">
        <el-form label-position="top" class="message-push__card-form">
          <el-form-item label="任务标题">
            <el-input v-model="taskCardForm.title" placeholder="请输入任务标题" maxlength="64" />
          </el-form-item>
          <el-form-item label="任务描述">
            <el-input
              v-model="taskCardForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入任务描述"
            />
          </el-form-item>
          <el-form-item label="截止时间">
            <el-date-picker
              v-model="taskCardForm.deadline"
              type="datetime"
              placeholder="选择截止时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
      </div>
    </section>

    <!-- Step 3: 接收人选择 -->
    <section class="message-push__section">
      <h3 class="message-push__section-title">
        <span class="message-push__step">3</span>
        选择接收人
      </h3>

      <div class="message-push__recipient-filter">
        <el-radio-group v-model="form.recipientType" @change="handleRecipientTypeChange">
          <el-radio-button value="department">按部门</el-radio-button>
          <el-radio-button value="tag">按标签</el-radio-button>
          <el-radio-button value="manual">手动选择</el-radio-button>
        </el-radio-group>
      </div>

      <div class="message-push__recipient-picker">
        <!-- 左侧：可选列表 -->
        <div class="message-push__available">
          <div class="message-push__available-header">
            <el-input
              v-model="recipientKeyword"
              placeholder="搜索..."
              clearable
              size="small"
              prefix-icon="Search"
            />
          </div>
          <div class="message-push__available-list">
            <el-checkbox-group v-model="form.recipientIds">
              <div
                v-for="item in filteredAvailableRecipients"
                :key="item.id"
                class="message-push__recipient-item"
              >
                <el-checkbox :value="item.id" :label="item.name" />
              </div>
            </el-checkbox-group>
            <div v-if="filteredAvailableRecipients.length === 0" class="message-push__recipient-empty">
              <span>暂无匹配结果</span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="message-push__recipient-actions">
          <el-button
            type="primary"
            size="small"
            circle
            :disabled="selectedAvailable.length === 0"
            @click="moveToSelected"
          >
            <el-icon><ArrowRight /></el-icon>
          </el-button>
          <el-button
            size="small"
            circle
            :disabled="selectedInPanel.length === 0"
            @click="moveToAvailable"
          >
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
        </div>

        <!-- 右侧：已选列表 -->
        <div class="message-push__selected">
          <div class="message-push__selected-header">
            已选择 {{ selectedRecipients.length }} 人
          </div>
          <div class="message-push__selected-list">
            <el-checkbox-group v-model="selectedInPanel">
              <div
                v-for="item in selectedRecipients"
                :key="item.id"
                class="message-push__recipient-item"
              >
                <el-checkbox :value="item.id" :label="item.name" />
              </div>
            </el-checkbox-group>
            <div v-if="selectedRecipients.length === 0" class="message-push__recipient-empty">
              <span>请从左侧选择接收人</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Step 4: 发送设置 -->
    <section class="message-push__section">
      <h3 class="message-push__section-title">
        <span class="message-push__step">4</span>
        发送设置
      </h3>
      <div class="message-push__send-settings">
        <el-radio-group v-model="sendMethod">
          <el-radio value="immediate">立即发送</el-radio>
          <el-radio value="scheduled">定时发送</el-radio>
        </el-radio-group>
        <el-date-picker
          v-if="sendMethod === 'scheduled'"
          v-model="form.scheduleTime"
          type="datetime"
          placeholder="选择发送时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="margin-left: 16px"
        />
      </div>
    </section>

    <!-- 底部操作栏 -->
    <div class="message-push__footer">
      <el-button @click="handleReset">重置</el-button>
      <el-button type="primary" :loading="sending" @click="handleSend">
        <el-icon><Promotion /></el-icon>
        发送消息
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Upload, ArrowRight, ArrowLeft, Promotion } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile } from 'element-plus'
import type { MessageType, SendMessageForm } from '@/types/message'
import { sendMessage } from '@/api/message'

// ---------- 消息类型选项 ----------
const messageTypeOptions: { value: MessageType; label: string; icon: string }[] = [
  { value: 'text', label: '文本消息', icon: 'ChatLineSquare' },
  { value: 'image', label: '图片消息', icon: 'Picture' },
  { value: 'markdown', label: 'Markdown', icon: 'Document' },
  { value: 'task_card', label: '任务卡片', icon: 'Postcard' },
]

// ---------- 表单 ----------
const form = reactive<SendMessageForm>({
  type: 'text',
  content: '',
  recipientType: 'department',
  recipientIds: [],
  scheduleTime: undefined,
})

const sendMethod = ref<'immediate' | 'scheduled'>('immediate')

// ---------- 任务卡片表单 ----------
const taskCardForm = reactive({
  title: '',
  description: '',
  deadline: '',
})

// ---------- 图片上传 ----------
const imagePreviewUrl = ref('')

function handleImageChange(file: UploadFile) {
  if (file.raw) {
    imagePreviewUrl.value = URL.createObjectURL(file.raw)
  }
}

// ---------- 接收人数据 ----------
interface Recipient {
  id: number
  name: string
}

const recipientKeyword = ref('')

const allRecipients = ref<Recipient[]>([
  { id: 1, name: '技术部' },
  { id: 2, name: '市场部' },
  { id: 3, name: '销售部' },
  { id: 4, name: '产品部' },
  { id: 5, name: '运营部' },
  { id: 6, name: '人事部' },
  { id: 7, name: '财务部' },
  { id: 8, name: '客户服务部' },
])

const selectedRecipients = ref<Recipient[]>([])
const selectedInPanel = ref<number[]>([])

const filteredAvailableRecipients = computed(() => {
  const selectedIds = new Set(selectedRecipients.value.map((r) => r.id))
  return allRecipients.value.filter(
    (r) =>
      !selectedIds.has(r.id) &&
      (recipientKeyword.value === '' || r.name.includes(recipientKeyword.value)),
  )
})

const selectedAvailable = computed(() => {
  const availableIds = new Set(filteredAvailableRecipients.value.map((r) => r.id))
  return form.recipientIds.filter((id) => availableIds.has(id))
})

function handleRecipientTypeChange() {
  form.recipientIds = []
  selectedRecipients.value = []
  selectedInPanel.value = []
}

function moveToSelected() {
  const ids = form.recipientIds.filter((id) => {
    const availableIds = new Set(filteredAvailableRecipients.value.map((r) => r.id))
    return availableIds.has(id)
  })
  const recipients = allRecipients.value.filter((r) => ids.includes(r.id))
  selectedRecipients.value.push(...recipients)
  form.recipientIds = form.recipientIds.filter((id) => !ids.includes(id))
  selectedInPanel.value = []
}

function moveToAvailable() {
  const toRemove = new Set(selectedInPanel.value)
  selectedRecipients.value = selectedRecipients.value.filter((r) => !toRemove.has(r.id))
  selectedInPanel.value = []
}

// ---------- 发送 ----------
const sending = ref(false)

async function handleSend() {
  if (form.type === 'task_card') {
    if (!taskCardForm.title) {
      ElMessage.warning('请输入任务标题')
      return
    }
    form.content = JSON.stringify({
      title: taskCardForm.title,
      description: taskCardForm.description,
      deadline: taskCardForm.deadline,
    })
  } else if (form.type === 'image') {
    if (!imagePreviewUrl.value) {
      ElMessage.warning('请上传图片')
      return
    }
    form.content = '[图片消息]'
  } else if (!form.content.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  if (selectedRecipients.value.length === 0) {
    ElMessage.warning('请选择接收人')
    return
  }

  form.recipientIds = selectedRecipients.value.map((r) => r.id)

  if (sendMethod.value === 'immediate') {
    form.scheduleTime = undefined
  }

  await ElMessageBox.confirm(
    `确认向 ${selectedRecipients.value.length} 个接收人发送消息？`,
    '发送确认',
    { type: 'warning' },
  )

  sending.value = true
  try {
    const res = await sendMessage({ ...form })
    ElMessage.success(`消息发送成功，任务 ID: ${res.data.taskId}`)
    handleReset()
  } catch {
    ElMessage.success('消息发送成功（Mock）')
    handleReset()
  } finally {
    sending.value = false
  }
}

function handleReset() {
  form.type = 'text'
  form.content = ''
  form.recipientIds = []
  form.scheduleTime = undefined
  sendMethod.value = 'immediate'
  selectedRecipients.value = []
  selectedInPanel.value = []
  recipientKeyword.value = ''
  imagePreviewUrl.value = ''
  taskCardForm.title = ''
  taskCardForm.description = ''
  taskCardForm.deadline = ''
}
</script>

<style lang="scss" scoped>
.message-push {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__section {
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-lg;
  }

  &__section-title {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: $font-size-body;
    font-weight: 600;
    color: $color-text-primary;
    margin: 0 0 $spacing-base 0;
  }

  &__step {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: $color-primary;
    color: #fff;
    font-size: $font-size-caption;
    font-weight: 600;
    flex-shrink: 0;
  }

  // ---------- 类型卡片 ----------
  &__type-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-base;
  }

  &__type-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-lg $spacing-base;
    border: 2px solid $color-border;
    border-radius: $border-radius-card;
    cursor: pointer;
    transition: all 0.2s ease;
    background: $color-bg-card;

    &:hover {
      border-color: $color-primary-light;
      background: $color-primary-light;
    }

    &.is-active {
      border-color: $color-primary;
      background: $color-primary-light;

      .message-push__type-icon {
        color: $color-primary;
      }

      .message-push__type-label {
        color: $color-primary;
        font-weight: 600;
      }
    }
  }

  &__type-icon {
    color: $color-text-placeholder;
    transition: color 0.2s ease;
  }

  &__type-label {
    font-size: $font-size-body;
    color: $color-text-regular;
    transition: all 0.2s ease;
  }

  // ---------- 编辑器 ----------
  &__editor {
    min-height: 120px;
  }

  &__markdown-tip {
    margin-top: $spacing-sm;
    font-size: $font-size-caption;
    color: $color-text-placeholder;
  }

  &__upload {
    width: 100%;

    :deep(.el-upload-dragger) {
      width: 100%;
    }
  }

  &__upload-icon {
    color: $color-text-placeholder;
  }

  &__upload-text {
    margin-top: $spacing-sm;
    color: $color-text-regular;
    font-size: $font-size-body;

    em {
      color: $color-primary;
      font-style: normal;
    }
  }

  &__upload-tip {
    font-size: $font-size-caption;
    color: $color-text-placeholder;
  }

  &__preview {
    margin-top: $spacing-base;
    max-width: 300px;
    max-height: 200px;
    border-radius: $border-radius-card;
  }

  &__card-form {
    max-width: 480px;
  }

  // ---------- 接收人选择器 ----------
  &__recipient-filter {
    margin-bottom: $spacing-base;
  }

  &__recipient-picker {
    display: flex;
    gap: $spacing-base;
    min-height: 300px;
  }

  &__available,
  &__selected {
    flex: 1;
    border: 1px solid $color-border;
    border-radius: $border-radius-card;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  &__available-header {
    padding: $spacing-sm $spacing-base;
    border-bottom: 1px solid $color-border;
  }

  &__selected-header {
    padding: $spacing-sm $spacing-base;
    border-bottom: 1px solid $color-border;
    font-size: $font-size-body;
    font-weight: 500;
    color: $color-text-primary;
  }

  &__available-list,
  &__selected-list {
    flex: 1;
    overflow-y: auto;
    padding: $spacing-sm;
  }

  &__recipient-item {
    padding: $spacing-xs $spacing-sm;
    border-radius: $border-radius-tag;

    &:hover {
      background: $color-bg-page;
    }
  }

  &__recipient-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    min-height: 80px;
    color: $color-text-placeholder;
    font-size: $font-size-caption;
  }

  &__recipient-actions {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: $spacing-sm;
    flex-shrink: 0;
  }

  // ---------- 发送设置 ----------
  &__send-settings {
    display: flex;
    align-items: center;
  }

  // ---------- 底部操作栏 ----------
  &__footer {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-sm;
    padding: $spacing-lg 0;
  }
}
</style>
