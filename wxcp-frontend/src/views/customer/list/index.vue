<template>
  <div class="customer-list">
    <PageHeader title="客户管理" description="管理企微外部联系人">
      <template #actions>
        <el-button @click="handleSync">
          <el-icon><Refresh /></el-icon>
          同步客户
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <div class="customer-list__stats">
      <StatCard label="客户总数" :value="stats.total" icon="User" />
      <StatCard label="今日新增" :value="stats.todayNew" icon="Plus" />
      <StatCard label="待跟进" :value="stats.pendingFollow" icon="Phone" />
      <StatCard label="客户群数" :value="stats.groupCount" icon="ChatDotRound" />
    </div>

    <!-- 筛选栏 -->
    <FilterBar @search="handleSearch" @reset="handleReset">
      <el-input
        v-model="query.keyword"
        placeholder="搜索客户姓名/企业"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="query.followUserId"
        placeholder="跟进人"
        clearable
        style="width: 140px"
      >
        <el-option
          v-for="item in followUserOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-select
        v-model="query.tagId"
        placeholder="标签"
        clearable
        style="width: 140px"
      >
        <el-option
          v-for="item in tagOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width: 240px"
        @change="handleDateChange"
      />
    </FilterBar>

    <!-- 表格 -->
    <div class="customer-list__table">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="客户信息" min-width="220">
          <template #default="{ row }">
            <div class="customer-list__customer-info">
              <el-avatar :size="36" :src="row.avatar">
                {{ row.name?.charAt(0) }}
              </el-avatar>
              <div class="customer-list__customer-text">
                <div class="customer-list__customer-name">{{ row.name }}</div>
                <div class="customer-list__customer-company">{{ row.company || '--' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.source }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="160">
          <template #default="{ row }">
            <div class="customer-list__tags">
              <el-tag
                v-for="(tag, index) in row.tags.slice(0, 3)"
                :key="index"
                size="small"
                class="customer-list__tag"
              >
                {{ tag }}
              </el-tag>
              <el-tag v-if="row.tags.length > 3" size="small" type="info">
                +{{ row.tags.length - 3 }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="followUser" label="跟进人" width="100" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small" effect="light">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="addTime" label="添加时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button link type="primary" size="small" @click="handleAddFollow(row)">
              添加跟进
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="customer-list__pagination">
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

    <!-- 底部浮动操作栏 -->
    <FloatingBar
      :selected-count="selectedRows.length"
      :visible="true"
      @clear="handleClearSelection"
    >
      <el-button type="primary" size="small" @click="handleBatchAddTags">
        批量添加标签
      </el-button>
    </FloatingBar>

    <!-- 客户详情抽屉 -->
    <CustomerDrawer
      :visible="drawerVisible"
      :customer-id="drawerEditId"
      @update:visible="drawerVisible = $event"
      @refresh="loadAllData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Refresh, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useTable } from '@/composables/useTable'
import { useDialog } from '@/composables/useDialog'
import { getCustomerList, getCustomerStats, syncCustomers, exportCustomers } from '@/api/customer'
import type { Customer, CustomerQuery, CustomerStats, CustomerStatus } from '@/types/customer'
import CustomerDrawer from '../components/CustomerDrawer.vue'

// ---------- table composable ----------
const { loading, list, total, selectedRows, query, loadData, resetQuery, handleSelectionChange, handleSizeChange, handleCurrentChange } =
  useTable<Customer, CustomerQuery>()

// ---------- dialog composable ----------
const { visible: drawerVisible, editId: drawerEditId, open: openDrawer } = useDialog()

// ---------- stats ----------
const stats = reactive<CustomerStats>({
  total: 0,
  todayNew: 0,
  pendingFollow: 0,
  groupCount: 0,
})

// ---------- date range ----------
const dateRange = ref<[string, string] | null>(null)

function handleDateChange(val: [string, string] | null) {
  if (val) {
    query.startDate = val[0]
    query.endDate = val[1]
  } else {
    query.startDate = undefined
    query.endDate = undefined
  }
}

// ---------- filter options ----------
const followUserOptions = [
  { label: '王小明', value: 1 },
  { label: '李丽华', value: 2 },
  { label: '赵磊', value: 3 },
  { label: '陈婷婷', value: 4 },
]

const tagOptions = [
  { label: '高意向客户', value: 1 },
  { label: 'VIP', value: 2 },
  { label: '技术负责人', value: 3 },
  { label: '新客户', value: 4 },
  { label: '待跟进', value: 5 },
]

// ---------- data loading ----------
async function loadAllData() {
  await Promise.all([loadData(getCustomerList), loadStats()])
}

async function loadStats() {
  try {
    const res = await getCustomerStats()
    Object.assign(stats, res.data)
  } catch {
    // Mock fallback
    Object.assign(stats, mockStats)
  }
}

// ---------- filter handlers ----------
async function handleSearch() {
  query.pageNum = 1
  await loadData(getCustomerList)
}

async function handleReset() {
  dateRange.value = null
  await resetQuery(getCustomerList)
}

// ---------- table action handlers ----------
function handleViewDetail(row: Customer) {
  openDrawer(row.id)
}

function handleAddFollow(_row: Customer) {
  ElMessage.info('添加跟进功能开发中')
}

function handleClearSelection() {
  selectedRows.value = []
}

function handleBatchAddTags() {
  const ids = selectedRows.value.map((r) => r.id)
  ElMessage.info(`已选择 ${ids.length} 位客户，批量添加标签功能开发中`)
}

// ---------- header action handlers ----------
async function handleSync() {
  try {
    await syncCustomers()
    ElMessage.success('客户同步成功')
    await loadAllData()
  } catch {
    ElMessage.success('客户同步成功（Mock）')
    await loadAllData()
  }
}

async function handleExport() {
  try {
    const blob = await exportCustomers(query)
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `客户列表_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.info('导出功能开发中')
  }
}

// ---------- status helpers ----------
function statusLabel(status: CustomerStatus): string {
  const map: Record<CustomerStatus, string> = {
    active: '活跃',
    lost: '已流失',
    unfollow: '未跟进',
  }
  return map[status]
}

function statusTagType(status: CustomerStatus): 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined {
  const map: Record<CustomerStatus, 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined> = {
    active: 'success',
    lost: 'danger',
    unfollow: 'warning',
  }
  return map[status]
}

// ---------- mock data ----------
const mockStats: CustomerStats = {
  total: 3842,
  todayNew: 23,
  pendingFollow: 156,
  groupCount: 48,
}

// ---------- lifecycle ----------
onMounted(() => {
  loadAllData()
})
</script>

<style lang="scss" scoped>
.customer-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-base;
  }

  &__table {
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-lg;
  }

  &__customer-info {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__customer-text {
    min-width: 0;
  }

  &__customer-name {
    font-size: $font-size-body;
    font-weight: 500;
    color: $color-text-primary;
  }

  &__customer-company {
    font-size: $font-size-caption;
    color: $color-text-placeholder;
    margin-top: 2px;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }

  &__tag {
    border-radius: $border-radius-tag;
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-base;
  }
}
</style>
