<template>
  <div class="sys-config-page">
    <PageHeader title="系统配置" description="管理系统配置项" />

    <!-- 筛选栏 -->
    <FilterBar @search="handleSearch" @reset="handleReset">
      <el-select
        v-model="query.configGroup"
        placeholder="配置分组"
        clearable
        style="width: 160px"
      >
        <el-option
          v-for="item in groupOptions"
          :key="item"
          :label="item"
          :value="item"
        />
      </el-select>
    </FilterBar>

    <!-- 表格 -->
    <div class="sys-config-page__table">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="configName" label="配置名称" min-width="180" />
        <el-table-column prop="configKey" label="配置键" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <code class="sys-config-page__key">{{ row.configKey }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="configValue" label="配置值" min-width="180" show-overflow-tooltip />
        <el-table-column prop="configGroup" label="分组" width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="light">{{ row.configGroup }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="sys-config-page__pagination">
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

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑配置"
      width="520px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="配置名称">
          <el-input :model-value="form.configName" disabled />
        </el-form-item>
        <el-form-item label="配置键">
          <el-input :model-value="form.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input
            v-model="form.configValue"
            placeholder="请输入配置值"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            placeholder="请输入备注"
            :rows="3"
            maxlength="200"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogClose">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useTable } from '@/composables/useTable'
import { useDialog } from '@/composables/useDialog'
import { getSysConfigList, updateSysConfig } from '@/api/system'
import type { SysConfig } from '@/types/system'
import type { PageQuery } from '@/types/api'

interface ConfigQuery extends PageQuery {
  configGroup?: string
}

// ---------- table composable ----------
const { loading, list, total, query, loadData, resetQuery, handleSizeChange, handleCurrentChange } =
  useTable<SysConfig, ConfigQuery>()

// ---------- dialog composable ----------
const { visible: dialogVisible, open: dialogOpen, close: dialogClose } = useDialog<number>()

// ---------- form ----------
const formRef = ref<FormInstance>()
const form = reactive({
  id: 0,
  configName: '',
  configKey: '',
  configValue: '',
  remark: '',
})

const submitLoading = ref(false)

// ---------- group filter options ----------
const groupOptions = ['基础配置', '邮件配置', '存储配置', '安全配置']

// ---------- data loading ----------
async function fetchList() {
  try {
    await loadData(getSysConfigList)
  } catch {
    list.value = mockConfigList
    total.value = mockConfigList.length
  }
}

// ---------- filter handlers ----------
async function handleSearch() {
  query.pageNum = 1
  await fetchList()
}

async function handleReset() {
  await resetQuery(getSysConfigList)
}

// ---------- table actions ----------
function handleEdit(row: SysConfig) {
  dialogOpen(row.id)
  Object.assign(form, {
    id: row.id,
    configName: row.configName,
    configKey: row.configKey,
    configValue: row.configValue,
    remark: row.remark,
  })
}

// ---------- form actions ----------
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: 0,
    configName: '',
    configKey: '',
    configValue: '',
    remark: '',
  })
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    await updateSysConfig({ id: form.id, configValue: form.configValue, remark: form.remark })
    ElMessage.success('保存成功')
    dialogClose()
    await fetchList()
  } catch {
    ElMessage.success('保存成功（Mock）')
    dialogClose()
    await fetchList()
  } finally {
    submitLoading.value = false
  }
}

// ---------- mock data ----------
const mockConfigList: SysConfig[] = [
  { id: 1, configName: '系统名称', configKey: 'sys.app_name', configValue: '企微运营管理平台', configGroup: '基础配置', remark: '平台显示名称' },
  { id: 2, configName: '默认分页大小', configKey: 'sys.page_size', configValue: '20', configGroup: '基础配置', remark: '列表默认每页条数' },
  { id: 3, configName: '文件上传大小限制', configKey: 'sys.upload_max_size', configValue: '10', configGroup: '基础配置', remark: '单位 MB' },
  { id: 4, configName: 'SMTP 主机', configKey: 'mail.smtp_host', configValue: 'smtp.example.com', configGroup: '邮件配置', remark: '邮件服务器地址' },
  { id: 5, configName: 'SMTP 端口', configKey: 'mail.smtp_port', configValue: '465', configGroup: '邮件配置', remark: '邮件服务器端口' },
  { id: 6, configName: '发件人邮箱', configKey: 'mail.from_address', configValue: 'noreply@example.com', configGroup: '邮件配置', remark: '系统发件人地址' },
  { id: 7, configName: '存储方式', configKey: 'storage.type', configValue: 'local', configGroup: '存储配置', remark: 'local / oss / cos' },
  { id: 8, configName: '上传路径', configKey: 'storage.upload_path', configValue: '/data/uploads', configGroup: '存储配置', remark: '本地存储上传路径' },
  { id: 9, configName: '密码最小长度', configKey: 'security.password_min_length', configValue: '8', configGroup: '安全配置', remark: '用户密码最低位数' },
  { id: 10, configName: '登录失败锁定次数', configKey: 'security.lock_count', configValue: '5', configGroup: '安全配置', remark: '连续失败多少次后锁定账号' },
]

// ---------- lifecycle ----------
onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
.sys-config-page {
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

  &__key {
    font-size: $font-size-caption;
    color: $color-text-placeholder;
    background: $color-bg-page;
    padding: 2px 6px;
    border-radius: $border-radius-input;
    font-family: monospace;
  }
}
</style>
