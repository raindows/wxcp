<template>
  <div class="data-dashboard">
    <PageHeader title="数据看板" description="营销数据统计分析" />

    <!-- 时间周期 Tab -->
    <div class="data-dashboard__period">
      <el-radio-group v-model="period" @change="loadData">
        <el-radio-button value="today">今日</el-radio-button>
        <el-radio-button value="week">本周</el-radio-button>
        <el-radio-button value="month">本月</el-radio-button>
        <el-radio-button value="quarter">近三个月</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 4 统计卡片 -->
    <div class="data-dashboard__stats">
      <StatCard
        label="客户总数"
        :value="stats.totalCustomers"
        :icon="'UserFilled'"
      />
      <StatCard
        label="今日新增"
        :value="stats.todayNew"
        :trend="{ value: todayNewTrend, isUp: todayNewTrend >= 0 }"
        :icon="'Plus'"
      />
      <StatCard
        label="消息发送量"
        :value="stats.todayMessages"
        :trend="{ value: todayMessagesTrend, isUp: todayMessagesTrend >= 0 }"
        :icon="'ChatDotRound'"
      />
      <StatCard
        label="群发成功率"
        :value="stats.groupSendRate + '%'"
        :trend="{ value: groupSendRateTrend, isUp: groupSendRateTrend >= 0 }"
        :icon="'Promotion'"
      />
    </div>

    <!-- 客户趋势 + 员工排行 -->
    <div class="data-dashboard__row">
      <div class="data-dashboard__col--wide">
        <TrendChart
          title="客户趋势"
          :x-data="customerTrend.dates"
          :series="trendSeries"
          :height="280"
        />
      </div>
      <div class="data-dashboard__col--narrow">
        <RankList
          title="员工客户排行"
          :data="employeeRankData"
          :max="5"
        />
      </div>
    </div>

    <!-- 消息类型分布 + 群发效果摘要 -->
    <div class="data-dashboard__row">
      <div class="data-dashboard__col--wide">
        <PieChart
          title="消息类型分布"
          :data="messageTypeData"
          :height="280"
        />
      </div>
      <div class="data-dashboard__col--narrow">
        <div class="data-dashboard__summary">
          <h3 class="data-dashboard__summary-title">群发效果摘要</h3>
          <div class="data-dashboard__summary-body">
            <div class="data-dashboard__summary-item">
              <span class="data-dashboard__summary-label">群发任务数</span>
              <span class="data-dashboard__summary-value">{{ groupSendSummary.totalTasks }}</span>
            </div>
            <div class="data-dashboard__summary-item">
              <span class="data-dashboard__summary-label">触达总人数</span>
              <span class="data-dashboard__summary-value">{{ groupSendSummary.totalRecipients }}</span>
            </div>
            <div class="data-dashboard__summary-item">
              <span class="data-dashboard__summary-label">发送成功率</span>
              <span class="data-dashboard__summary-value" :class="successRateClass">
                {{ groupSendSummary.successRate }}%
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import StatCard from '@/components/common/StatCard.vue'
import TrendChart from '@/components/charts/TrendChart.vue'
import PieChart from '@/components/charts/PieChart.vue'
import RankList from '@/components/charts/RankList.vue'
import {
  getDashboardStats,
  getCustomerTrend,
  getEmployeeRank,
  getMessageTypeDistribution,
  getGroupSendSummary,
} from '@/api/dashboard'
import type {
  DashboardQuery,
  DashboardStats,
  CustomerTrend,
  EmployeeRank,
  MessageTypeDistribution,
  GroupSendSummary,
} from '@/types/dashboard'

// ---------- state ----------
const period = ref<DashboardQuery['period']>('today')

const stats = reactive<DashboardStats>({
  totalCustomers: 0,
  todayNew: 0,
  todayMessages: 0,
  groupSendRate: 0,
})

const customerTrend = reactive<CustomerTrend>({
  dates: [],
  newCustomers: [],
  followUps: [],
})

const employeeRank = ref<EmployeeRank[]>([])
const messageTypeList = ref<MessageTypeDistribution[]>([])
const groupSendSummary = reactive<GroupSendSummary>({
  totalTasks: 0,
  successRate: 0,
  totalRecipients: 0,
})

// trend mock values (relative to period change)
const todayNewTrend = ref(0)
const todayMessagesTrend = ref(0)
const groupSendRateTrend = ref(0)

// ---------- computed ----------
const trendSeries = computed(() => [
  { name: '新增客户', data: customerTrend.newCustomers },
  { name: '跟进次数', data: customerTrend.followUps },
])

const employeeRankData = computed(() =>
  employeeRank.value.map((e) => ({
    name: e.name,
    value: e.customerCount,
  })),
)

const messageTypeData = computed(() => messageTypeList.value)

const successRateClass = computed(() =>
  groupSendSummary.successRate >= 90 ? 'is-success' : groupSendSummary.successRate >= 70 ? 'is-warning' : 'is-danger',
)

// ---------- mock data ----------
const mockStats: DashboardStats = {
  totalCustomers: 3862,
  todayNew: 128,
  todayMessages: 1847,
  groupSendRate: 94.6,
}

const mockTrend: CustomerTrend = {
  dates: ['05-03', '05-04', '05-05', '05-06', '05-07', '05-08', '05-09'],
  newCustomers: [85, 102, 95, 110, 128, 118, 128],
  followUps: [210, 185, 195, 230, 248, 220, 240],
}

const mockEmployeeRank: EmployeeRank[] = [
  { name: '王小明', customerCount: 186 },
  { name: '李丽华', customerCount: 162 },
  { name: '赵磊', customerCount: 148 },
  { name: '张伟', customerCount: 135 },
  { name: '陈思宇', customerCount: 121 },
]

const mockMessageTypes: MessageTypeDistribution[] = [
  { name: '文本消息', value: 4520 },
  { name: '图片消息', value: 2380 },
  { name: '链接消息', value: 1560 },
  { name: '小程序卡片', value: 890 },
  { name: '文件消息', value: 420 },
]

const mockGroupSend: GroupSendSummary = {
  totalTasks: 24,
  successRate: 94.6,
  totalRecipients: 8560,
}

// ---------- data loading ----------
async function loadData() {
  const params: DashboardQuery = { period: period.value }

  try {
    const [statsRes, trendRes, rankRes, msgRes, groupRes] = await Promise.all([
      getDashboardStats(params),
      getCustomerTrend(params),
      getEmployeeRank(params),
      getMessageTypeDistribution(params),
      getGroupSendSummary(params),
    ])

    Object.assign(stats, statsRes.data)
    Object.assign(customerTrend, trendRes.data)
    employeeRank.value = rankRes.data
    messageTypeList.value = msgRes.data
    Object.assign(groupSendSummary, groupRes.data)
  } catch {
    // Fallback to mock data
    Object.assign(stats, mockStats)
    Object.assign(customerTrend, mockTrend)
    employeeRank.value = mockEmployeeRank
    messageTypeList.value = mockMessageTypes
    Object.assign(groupSendSummary, mockGroupSend)
  }

  // Set mock trends (these would come from API ideally)
  todayNewTrend.value = 12.5
  todayMessagesTrend.value = 8.1
  groupSendRateTrend.value = 2.3
}

loadData()
</script>

<style lang="scss" scoped>
.data-dashboard {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;

  &__period {
    display: flex;
  }

  &__stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-base;
  }

  &__row {
    display: grid;
    grid-template-columns: 1fr 320px;
    gap: $spacing-base;
  }

  &__summary {
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-lg;
  }

  &__summary-title {
    font-size: $font-size-card-title;
    font-weight: 600;
    color: $color-text-primary;
    margin: 0 0 $spacing-lg;
  }

  &__summary-body {
    display: flex;
    flex-direction: column;
    gap: $spacing-lg;
  }

  &__summary-item {
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;
    padding: $spacing-base;
    background: $color-bg-hover;
    border-radius: $border-radius-card;
  }

  &__summary-label {
    font-size: $font-size-body;
    color: $color-text-placeholder;
  }

  &__summary-value {
    font-size: $font-size-stat;
    font-weight: 700;
    color: $color-text-primary;

    &.is-success {
      color: $color-success;
    }
    &.is-warning {
      color: $color-warning;
    }
    &.is-danger {
      color: $color-danger;
    }
  }
}
</style>
