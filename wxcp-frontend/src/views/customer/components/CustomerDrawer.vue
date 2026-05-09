<template>
  <el-drawer
    :model-value="visible"
    :title="customer ? customer.name : '客户详情'"
    size="640px"
    @close="handleClose"
  >
    <div v-loading="loading" class="customer-drawer">
      <template v-if="customer">
        <!-- 基本信息 -->
        <section class="customer-drawer__section">
          <h3 class="customer-drawer__section-title">基本信息</h3>
          <div class="customer-drawer__info-grid">
            <div class="customer-drawer__info-item">
              <div class="customer-drawer__info-avatar">
                <el-avatar :size="48" :src="customer.avatar">
                  {{ customer.name?.charAt(0) }}
                </el-avatar>
              </div>
              <div class="customer-drawer__info-main">
                <div class="customer-drawer__info-name">{{ customer.name }}</div>
                <div class="customer-drawer__info-company">{{ customer.company }}</div>
              </div>
            </div>
            <div class="customer-drawer__info-row">
              <span class="customer-drawer__info-label">职位</span>
              <span class="customer-drawer__info-value">{{ customer.position || '--' }}</span>
            </div>
            <div class="customer-drawer__info-row">
              <span class="customer-drawer__info-label">来源渠道</span>
              <span class="customer-drawer__info-value">{{ customer.source }}</span>
            </div>
            <div class="customer-drawer__info-row">
              <span class="customer-drawer__info-label">添加时间</span>
              <span class="customer-drawer__info-value">{{ customer.addTime }}</span>
            </div>
            <div class="customer-drawer__info-row">
              <span class="customer-drawer__info-label">跟进人</span>
              <span class="customer-drawer__info-value">{{ customer.followUser }}</span>
            </div>
            <div class="customer-drawer__info-row">
              <span class="customer-drawer__info-label">备注</span>
              <span class="customer-drawer__info-value">{{ customer.remark || '--' }}</span>
            </div>
          </div>
        </section>

        <!-- 标签管理 -->
        <section class="customer-drawer__section">
          <h3 class="customer-drawer__section-title">标签</h3>
          <div class="customer-drawer__tags">
            <el-tag
              v-for="(tag, index) in customer.tags"
              :key="index"
              closable
              size="default"
              class="customer-drawer__tag"
              @close="handleRemoveTag(index)"
            >
              {{ tag }}
            </el-tag>
            <el-button size="small" @click="showAddTag">
              + 添加标签
            </el-button>
          </div>
        </section>

        <!-- 跟进时间线 -->
        <section class="customer-drawer__section">
          <h3 class="customer-drawer__section-title">跟进记录</h3>
          <el-timeline>
            <el-timeline-item
              v-for="record in customer.followRecords"
              :key="record.id"
              :timestamp="record.time"
              placement="top"
              :type="recordTypeColor(record.type)"
            >
              <div class="customer-drawer__timeline-content">
                <div class="customer-drawer__timeline-header">
                  <el-tag :type="recordTypeTag(record.type)" size="small">
                    {{ recordTypeLabel(record.type) }}
                  </el-tag>
                  <span class="customer-drawer__timeline-operator">{{ record.operator }}</span>
                </div>
                <div class="customer-drawer__timeline-text">{{ record.content }}</div>
              </div>
            </el-timeline-item>
            <el-timeline-item v-if="customer.followRecords.length === 0" timestamp="" placement="top">
              <span class="text-muted">暂无跟进记录</span>
            </el-timeline-item>
          </el-timeline>
        </section>

        <!-- 操作记录 -->
        <section class="customer-drawer__section">
          <h3 class="customer-drawer__section-title">操作记录</h3>
          <el-table :data="operationRecords" size="small" stripe>
            <el-table-column prop="time" label="时间" width="160" />
            <el-table-column prop="operator" label="操作人" width="100" />
            <el-table-column prop="action" label="操作" min-width="120" />
            <el-table-column prop="detail" label="详情" min-width="160" show-overflow-tooltip />
          </el-table>
        </section>
      </template>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getCustomerDetail } from '@/api/customer'
import type { CustomerDetail, FollowRecordType } from '@/types/customer'

const props = defineProps<{
  visible: boolean
  customerId: number | undefined
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'refresh'): void
}>()

const loading = ref(false)
const customer = ref<CustomerDetail | null>(null)

/** 操作记录（从跟进记录派生） */
const operationRecords = ref<{ time: string; operator: string; action: string; detail: string }[]>([])

// ---------- load detail ----------
async function loadDetail() {
  if (!props.customerId) return
  loading.value = true
  try {
    const res = await getCustomerDetail(props.customerId)
    customer.value = res.data
    operationRecords.value = res.data.followRecords.map((record) => ({
      time: record.time,
      operator: record.operator,
      action: recordTypeLabel(record.type),
      detail: record.content,
    }))
  } catch {
    // Mock fallback
    customer.value = getMockDetail(props.customerId)
    operationRecords.value = customer.value.followRecords.map((record) => ({
      time: record.time,
      operator: record.operator,
      action: recordTypeLabel(record.type),
      detail: record.content,
    }))
  } finally {
    loading.value = false
  }
}

// ---------- watchers ----------
watch(
  () => props.visible,
  (val) => {
    if (val && props.customerId) {
      loadDetail()
    }
    if (!val) {
      customer.value = null
      operationRecords.value = []
    }
  },
)

// ---------- handlers ----------
function handleClose() {
  emit('update:visible', false)
}

function handleRemoveTag(index: number) {
  if (!customer.value) return
  customer.value.tags.splice(index, 1)
  emit('refresh')
}

function showAddTag() {
  ElMessage.info('添加标签功能开发中')
}

// ---------- helpers ----------
function recordTypeLabel(type: FollowRecordType): string {
  const map: Record<FollowRecordType, string> = {
    follow: '跟进',
    note: '备注',
    call: '电话',
  }
  return map[type]
}

function recordTypeTag(type: FollowRecordType): 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined {
  const map: Record<FollowRecordType, 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined> = {
    follow: 'primary',
    note: 'info',
    call: 'success',
  }
  return map[type]
}

function recordTypeColor(type: FollowRecordType): 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined {
  const map: Record<FollowRecordType, 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined> = {
    follow: 'primary',
    note: 'info',
    call: 'success',
  }
  return map[type]
}

// ---------- mock data ----------
function getMockDetail(id: number): CustomerDetail {
  return {
    id,
    externalUserId: 'wm_' + id,
    name: '张明远',
    avatar: '',
    company: '深圳星辰科技有限公司',
    position: '技术总监',
    source: '扫描二维码',
    tags: ['高意向客户', 'VIP', '技术负责人'],
    followUser: '王小明',
    status: 'active',
    addTime: '2026-04-15 14:30:00',
    remark: '对SaaS产品非常感兴趣，计划下周安排产品演示',
    followRecords: [
      { id: 1, customerId: id, content: '电话沟通产品需求，客户表示满意', operator: '王小明', time: '2026-05-09 10:30:00', type: 'call' },
      { id: 2, customerId: id, content: '发送产品报价单及技术方案文档', operator: '王小明', time: '2026-05-07 16:20:00', type: 'follow' },
      { id: 3, customerId: id, content: '客户反馈方案基本认可，需要内部讨论', operator: '李丽华', time: '2026-05-05 11:00:00', type: 'note' },
      { id: 4, customerId: id, content: '首次接触，了解客户基本需求和预算范围', operator: '王小明', time: '2026-04-15 15:00:00', type: 'follow' },
    ],
  }
}
</script>

<style scoped lang="scss">
.customer-drawer {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;

  &__section {
    background: $color-bg-page;
    border-radius: $border-radius-card;
    padding: $spacing-lg;
  }

  &__section-title {
    font-size: $font-size-card-title;
    font-weight: 600;
    color: $color-text-primary;
    margin: 0 0 $spacing-base;
  }

  &__info-grid {
    display: flex;
    flex-direction: column;
    gap: $spacing-md;
  }

  &__info-item {
    display: flex;
    align-items: center;
    gap: $spacing-base;
    margin-bottom: $spacing-sm;
  }

  &__info-avatar {
    flex-shrink: 0;
  }

  &__info-main {
    min-width: 0;
  }

  &__info-name {
    font-size: $font-size-card-title;
    font-weight: 600;
    color: $color-text-primary;
  }

  &__info-company {
    font-size: $font-size-body;
    color: $color-text-placeholder;
    margin-top: 2px;
  }

  &__info-row {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__info-label {
    flex-shrink: 0;
    width: 72px;
    font-size: $font-size-body;
    color: $color-text-placeholder;
  }

  &__info-value {
    font-size: $font-size-body;
    color: $color-text-regular;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  &__tag {
    border-radius: $border-radius-tag;
  }

  &__timeline-content {
    max-width: 100%;
  }

  &__timeline-header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    margin-bottom: $spacing-xs;
  }

  &__timeline-operator {
    font-size: $font-size-body;
    color: $color-text-regular;
  }

  &__timeline-text {
    font-size: $font-size-body;
    color: $color-text-regular;
    line-height: 1.5;
  }

  .text-muted {
    color: $color-text-disabled;
    font-size: $font-size-body;
  }
}
</style>
