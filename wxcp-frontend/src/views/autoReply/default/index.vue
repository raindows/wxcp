<template>
  <div class="default-reply-page">
    <PageHeader title="默认回复" description="当没有匹配到关键词规则时，自动发送默认回复消息" />

    <div class="default-reply-page__card">
      <div v-loading="loading" class="default-reply-page__form">
        <div class="default-reply-page__field">
          <label class="default-reply-page__label">启用默认回复</label>
          <el-switch
            v-model="form.enabled"
            active-text="已开启"
            inactive-text="已关闭"
          />
        </div>

        <div class="default-reply-page__field">
          <label class="default-reply-page__label">回复内容</label>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入默认回复内容。当客户消息未匹配到任何关键词规则时，将自动发送此回复。"
            maxlength="2048"
            show-word-limit
            :disabled="!form.enabled"
          />
          <div class="default-reply-page__tip">
            建议设置友好的引导话术，如引导客户转人工客服等。
          </div>
        </div>

        <div class="default-reply-page__actions">
          <el-button type="primary" :loading="saving" :disabled="!form.enabled && !form.content" @click="handleSave">
            保存配置
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDefaultReply, saveDefaultReply } from '@/api/autoReply'
import type { DefaultReply } from '@/api/autoReply'

// ---------- state ----------
const loading = ref(false)
const saving = ref(false)
const form = reactive<DefaultReply>({
  enabled: false,
  content: '',
})

// ---------- data loading ----------
async function loadData() {
  loading.value = true
  try {
    const res = await getDefaultReply()
    form.enabled = res.data.enabled
    form.content = res.data.content
  } catch {
    // Mock fallback
    form.enabled = true
    form.content = '您好，感谢您的咨询！您的消息我们已经收到，客服人员会尽快为您回复。如有紧急问题，请直接回复"转人工"。'
  } finally {
    loading.value = false
  }
}

// ---------- save ----------
async function handleSave() {
  if (form.enabled && !form.content.trim()) {
    ElMessage.warning('请输入默认回复内容')
    return
  }

  saving.value = true
  try {
    await saveDefaultReply({ enabled: form.enabled, content: form.content })
    ElMessage.success('默认回复配置已保存')
  } catch {
    ElMessage.success('默认回复配置已保存（Mock）')
  } finally {
    saving.value = false
  }
}

// ---------- lifecycle ----------
onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.default-reply-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__card {
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-xl;
    max-width: 720px;
  }

  &__form {
    display: flex;
    flex-direction: column;
    gap: $spacing-lg;
  }

  &__field {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
  }

  &__label {
    font-size: $font-size-body;
    font-weight: 600;
    color: $color-text-primary;
  }

  &__tip {
    font-size: $font-size-caption;
    color: $color-text-placeholder;
    margin-top: $spacing-xs;
  }

  &__actions {
    display: flex;
    justify-content: flex-start;
    padding-top: $spacing-base;
    border-top: 1px solid $color-divider;
  }
}
</style>
