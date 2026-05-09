<template>
  <div class="sys-log-page">
    <PageHeader title="操作日志" description="查看系统操作日志记录" />

    <!-- 筛选栏 -->
    <FilterBar @search="handleSearch" @reset="handleReset">
      <el-input
        v-model="query.keyword"
        placeholder="搜索模块/内容"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-input
        v-model="query.operator"
        placeholder="操作人"
        clearable
        style="width: 140px"
        @keyup.enter="handleSearch"
      />
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
    <div class="sys-log-page__table">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="logTypeTag(row.type)"
              size="small"
              effect="light"
            >
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="content" label="操作内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createTime" label="操作时间" width="170" />
      </el-table>

      <!-- 分页 -->
      <div class="sys-log-page__pagination">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useTable } from '@/composables/useTable'
import { getSysLogList } from '@/api/system'
import type { SysLog } from '@/types/system'
import type { PageQuery } from '@/types/api'

interface LogQuery extends PageQuery {
  keyword?: string
  operator?: string
  startDate?: string
  endDate?: string
}

// ---------- table composable ----------
const { loading, list, total, query, loadData, resetQuery, handleSizeChange, handleCurrentChange } =
  useTable<SysLog, LogQuery>()

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

// ---------- data loading ----------
async function fetchList() {
  try {
    await loadData(getSysLogList)
  } catch {
    list.value = mockLogList
    total.value = mockLogList.length
  }
}

// ---------- filter handlers ----------
async function handleSearch() {
  query.pageNum = 1
  await fetchList()
}

async function handleReset() {
  dateRange.value = null
  await resetQuery(getSysLogList)
}

// ---------- helpers ----------
function logTypeTag(type: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    INFO: 'success',
    WARN: 'warning',
    ERROR: 'danger',
  }
  return map[type]
}

// ---------- mock data ----------
const mockLogList: SysLog[] = [
  { id: 1, module: '用户管理', type: 'INFO', operator: '张三', ip: '192.168.1.100', content: '新增用户 zhangwei', createTime: '2025-05-08 14:30:00' },
  { id: 2, module: '用户管理', type: 'INFO', operator: '张三', ip: '192.168.1.100', content: '修改用户 zhangwei 状态为启用', createTime: '2025-05-08 14:32:00' },
  { id: 3, module: '角色管理', type: 'WARN', operator: '李华', ip: '192.168.1.101', content: '角色 operator 权限已变更', createTime: '2025-05-08 15:10:00' },
  { id: 4, module: '登录认证', type: 'ERROR', operator: '王芳', ip: '10.0.0.55', content: '登录失败：密码错误（连续第3次）', createTime: '2025-05-08 15:45:00' },
  { id: 5, module: '消息推送', type: 'INFO', operator: '赵磊', ip: '192.168.1.102', content: '群发任务 #1024 推送完成，成功 150 条，失败 2 条', createTime: '2025-05-08 16:00:00' },
  { id: 6, module: '菜单管理', type: 'INFO', operator: '张三', ip: '192.168.1.100', content: '新增菜单：系统配置', createTime: '2025-05-08 16:20:00' },
  { id: 7, module: '系统配置', type: 'WARN', operator: '张三', ip: '192.168.1.100', content: '修改系统配置：上传文件大小限制从 5MB 调整为 10MB', createTime: '2025-05-08 16:30:00' },
  { id: 8, module: '客户管理', type: 'INFO', operator: '李华', ip: '192.168.1.101', content: '同步企微客户，新增 23 条，更新 156 条', createTime: '2025-05-08 17:00:00' },
  { id: 9, module: '登录认证', type: 'ERROR', operator: '未知', ip: '45.33.32.156', content: '登录失败：IP 不在白名单范围内', createTime: '2025-05-08 17:15:00' },
  { id: 10, module: '素材管理', type: 'INFO', operator: '赵磊', ip: '192.168.1.102', content: '上传图片素材 3 张', createTime: '2025-05-08 17:30:00' },
]

// ---------- lifecycle ----------
onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
.sys-log-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__table {
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-lg;
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-base;
  }
}
</style>
