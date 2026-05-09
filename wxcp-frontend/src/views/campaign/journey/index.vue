<template>
  <div class="campaign-journey">
    <PageHeader title="客户旅程" description="可视化追踪客户在不同阶段的转化情况" />

    <!-- 看板 -->
    <div v-loading="loading" class="campaign-journey__board">
      <div
        v-for="stage in stages"
        :key="stage.key"
        class="campaign-journey__column"
      >
        <!-- 列头 -->
        <div class="campaign-journey__column-header" :style="{ backgroundColor: stage.color }">
          <span class="campaign-journey__column-title">{{ stage.label }}</span>
          <span class="campaign-journey__column-count">{{ getColumnCustomers(stage.key).length }}</span>
        </div>

        <!-- 卡片列表 -->
        <div class="campaign-journey__column-body">
          <div
            v-for="customer in getColumnCustomers(stage.key)"
            :key="customer.id"
            class="campaign-journey__card"
          >
            <div class="campaign-journey__card-header">
              <el-avatar :size="32" :src="customer.avatar">
                {{ customer.customerName?.charAt(0) }}
              </el-avatar>
              <div class="campaign-journey__card-info">
                <div class="campaign-journey__card-name">{{ customer.customerName }}</div>
                <div class="campaign-journey__card-company">{{ customer.company || '--' }}</div>
              </div>
              <el-dropdown trigger="click" @command="(cmd: string) => handleMove(customer, cmd)">
                <el-button link size="small" class="campaign-journey__card-more">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="s in stages.filter(st => st.key !== stage.key)"
                      :key="s.key"
                      :command="s.key"
                    >
                      移动到「{{ s.label }}」
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div class="campaign-journey__card-footer">
              <span class="campaign-journey__card-time">
                <el-icon><Clock /></el-icon>
                {{ customer.enterTime }}
              </span>
            </div>
          </div>

          <!-- 空状态 -->
          <div
            v-if="getColumnCustomers(stage.key).length === 0"
            class="campaign-journey__empty"
          >
            暂无客户
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { MoreFilled, Clock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getJourneyBoard, moveJourneyStage } from '@/api/campaign'
import type { JourneyCustomer, JourneyStage } from '@/types/campaign'

// ---------- stages config ----------
interface StageConfig {
  key: JourneyStage
  label: string
  color: string
}

const stages: StageConfig[] = [
  { key: 'new', label: '新客', color: '#165DFF' },
  { key: 'interested', label: '意向', color: '#ff7d00' },
  { key: 'deal', label: '成交', color: '#00b42a' },
  { key: 'lost', label: '流失', color: '#f53f3f' },
]

// ---------- data ----------
const loading = ref(false)
const customers = ref<JourneyCustomer[]>([])

// ---------- mock data ----------
const mockCustomers: JourneyCustomer[] = [
  { id: 1, customerId: 101, customerName: '张三', company: '华创科技有限公司', stage: 'new', enterTime: '2026-05-01 09:30' },
  { id: 2, customerId: 102, customerName: '李芳', company: '远景集团', stage: 'new', enterTime: '2026-05-03 14:20' },
  { id: 3, customerId: 103, customerName: '王磊', company: '天启创新', stage: 'new', enterTime: '2026-05-05 10:00' },
  { id: 4, customerId: 104, customerName: '赵婷婷', company: '博远科技', stage: 'interested', enterTime: '2026-04-28 11:15' },
  { id: 5, customerId: 105, customerName: '陈志明', company: '恒信达', stage: 'interested', enterTime: '2026-04-30 16:45' },
  { id: 6, customerId: 106, customerName: '刘洋', company: '盛世传媒', stage: 'interested', enterTime: '2026-05-02 08:30' },
  { id: 7, customerId: 107, customerName: '孙丽', company: '星辰教育', stage: 'interested', enterTime: '2026-05-04 13:00' },
  { id: 8, customerId: 108, customerName: '周建', company: '鼎盛投资', stage: 'deal', enterTime: '2026-04-20 09:00' },
  { id: 9, customerId: 109, customerName: '吴静', company: '明德医疗', stage: 'deal', enterTime: '2026-04-25 15:20' },
  { id: 10, customerId: 110, customerName: '郑浩', company: '智联科技', stage: 'lost', enterTime: '2026-03-15 10:00' },
  { id: 11, customerId: 111, customerName: '黄蓉', company: '绿洲农业', stage: 'lost', enterTime: '2026-03-22 14:30' },
  { id: 12, customerId: 112, customerName: '马超', company: '锐捷网络', stage: 'new', enterTime: '2026-05-08 11:00' },
]

// ---------- computed ----------
function getColumnCustomers(stageKey: JourneyStage): JourneyCustomer[] {
  return customers.value.filter((c) => c.stage === stageKey)
}

// ---------- load data ----------
async function loadBoard() {
  loading.value = true
  try {
    const res = await getJourneyBoard()
    customers.value = res.data
  } catch {
    customers.value = [...mockCustomers]
  } finally {
    loading.value = false
  }
}

// ---------- move customer ----------
async function handleMove(customer: JourneyCustomer, targetStage: string) {
  const stage = targetStage as JourneyStage
  const targetLabel = stages.find((s) => s.key === stage)?.label || stage
  try {
    await moveJourneyStage(customer.customerId, stage)
    customer.stage = stage
    ElMessage.success(`已将「${customer.customerName}」移动到「${targetLabel}」`)
  } catch {
    customer.stage = stage
    ElMessage.success(`已将「${customer.customerName}」移动到「${targetLabel}」（Mock）`)
  }
}

// ---------- lifecycle ----------
onMounted(() => {
  loadBoard()
})
</script>

<style lang="scss" scoped>
.campaign-journey {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__board {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-base;
    min-height: calc(100vh - 200px);
  }

  &__column {
    display: flex;
    flex-direction: column;
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    overflow: hidden;
  }

  &__column-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-sm $spacing-base;
    color: #fff;
    flex-shrink: 0;
  }

  &__column-title {
    font-size: $font-size-card-title;
    font-weight: 600;
  }

  &__column-count {
    font-size: $font-size-caption;
    background: rgba(255, 255, 255, 0.25);
    border-radius: 10px;
    padding: 1px 8px;
    line-height: 18px;
  }

  &__column-body {
    flex: 1;
    overflow-y: auto;
    padding: $spacing-sm;
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
  }

  &__card {
    background: $color-bg-hover;
    border-radius: $border-radius-button;
    border: 1px solid $color-border;
    padding: $spacing-sm $spacing-base;
    transition: box-shadow 0.2s;

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }
  }

  &__card-header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__card-info {
    flex: 1;
    min-width: 0;
  }

  &__card-name {
    font-size: $font-size-body;
    font-weight: 500;
    color: $color-text-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__card-company {
    font-size: $font-size-caption;
    color: $color-text-placeholder;
    margin-top: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__card-more {
    color: $color-text-placeholder;
    padding: 2px;

    &:hover {
      color: $color-text-regular;
    }
  }

  &__card-footer {
    margin-top: $spacing-sm;
    padding-top: $spacing-xs;
    border-top: 1px solid $color-divider;
  }

  &__card-time {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: $font-size-caption;
    color: $color-text-placeholder;
  }

  &__empty {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 80px;
    color: $color-text-disabled;
    font-size: $font-size-caption;
  }
}
</style>
