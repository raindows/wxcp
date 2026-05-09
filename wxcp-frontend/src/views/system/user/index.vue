<template>
  <div class="sys-user-page">
    <PageHeader title="用户管理" description="管理系统用户账号">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </template>
    </PageHeader>

    <!-- 筛选栏 -->
    <FilterBar @search="handleSearch" @reset="handleReset">
      <el-input
        v-model="query.keyword"
        placeholder="搜索用户名/姓名/手机号"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      />
    </FilterBar>

    <!-- 表格 -->
    <div class="sys-user-page__table">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="deptName" label="部门" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="roleName" label="角色" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.roleName || '--' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              inline-prompt
              active-text="启用"
              inactive-text="禁用"
              @change="(val: string | number | boolean) => handleStatusChange(row, !!val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="warning" size="small" @click="handleResetPwd(row)">
              重置密码
            </el-button>
            <el-popconfirm title="确定删除该用户？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="sys-user-page__pagination">
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

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogEditId ? '编辑用户' : '新增用户'"
      width="520px"
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" maxlength="30" />
        </el-form-item>
        <el-form-item v-if="!dialogEditId" label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            maxlength="30"
            show-password
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptOptions"
            :props="{ label: 'name', children: 'children' }"
            value-key="id"
            check-strictly
            clearable
            filterable
            placeholder="请选择部门"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" clearable style="width: 100%">
            <el-option
              v-for="item in roleOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/composables/useTable'
import { useDialog } from '@/composables/useDialog'
import {
  getSysUserList,
  createSysUser,
  updateSysUser,
  deleteSysUser,
  resetPassword,
} from '@/api/system'
import type { SysUser, SysUserQuery } from '@/types/system'

// ---------- table composable ----------
const { loading, list, total, query, loadData, resetQuery, handleSizeChange, handleCurrentChange } =
  useTable<SysUser, SysUserQuery>()

// ---------- dialog composable ----------
const { visible: dialogVisible, editId: dialogEditId, open: dialogOpen, close: dialogClose } = useDialog()

// ---------- form ----------
const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  password: '',
  realName: '',
  deptId: undefined as number | undefined,
  phone: '',
  roleId: undefined as number | undefined,
  status: 1 as 0 | 1,
})

const formRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
}

const submitLoading = ref(false)

// ---------- dropdown options (mock) ----------
const deptOptions = [
  { id: 1, name: '总公司', children: [
    { id: 2, name: '技术研发部' },
    { id: 3, name: '市场营销部' },
    { id: 4, name: '人力资源部' },
  ] },
]

const roleOptions = [
  { id: 1, name: '超级管理员' },
  { id: 2, name: '普通管理员' },
  { id: 3, name: '运营人员' },
]

// ---------- data loading ----------
async function fetchList() {
  try {
    await loadData(getSysUserList)
  } catch {
    list.value = mockUserList
    total.value = mockUserList.length
  }
}

// ---------- filter handlers ----------
async function handleSearch() {
  query.pageNum = 1
  await fetchList()
}

async function handleReset() {
  await resetQuery(getSysUserList)
}

// ---------- table actions ----------
function handleAdd() {
  dialogOpen()
}

function handleEdit(row: SysUser) {
  dialogOpen(row.id)
  Object.assign(form, {
    username: row.username,
    realName: row.realName,
    deptId: row.deptId,
    phone: row.phone,
    roleId: row.roleId,
    status: row.status,
  })
}

async function handleDelete(row: SysUser) {
  try {
    await deleteSysUser(row.id)
    ElMessage.success('删除成功')
    await fetchList()
  } catch {
    ElMessage.success('删除成功（Mock）')
    const idx = list.value.findIndex((u) => u.id === row.id)
    if (idx !== -1) list.value.splice(idx, 1)
    total.value = list.value.length
  }
}

async function handleStatusChange(row: SysUser, val: boolean) {
  try {
    await updateSysUser({ id: row.id, status: val ? 1 : 0 })
    row.status = val ? 1 : 0
    ElMessage.success('状态已更新')
  } catch {
    row.status = val ? 1 : 0
    ElMessage.success('状态已更新（Mock）')
  }
}

async function handleResetPwd(row: SysUser) {
  try {
    await ElMessageBox.confirm('确定要重置该用户的密码吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    await resetPassword(row.id)
    ElMessage.success('密码已重置为默认密码')
  } catch (e) {
    if (e === 'cancel') return
    ElMessage.success('密码已重置为默认密码（Mock）')
  }
}

// ---------- form actions ----------
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    username: '',
    password: '',
    realName: '',
    deptId: undefined,
    phone: '',
    roleId: undefined,
    status: 1,
  })
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (dialogEditId.value) {
      await updateSysUser({ id: dialogEditId.value, ...form })
      ElMessage.success('更新成功')
    } else {
      await createSysUser(form as Partial<SysUser> & { password: string })
      ElMessage.success('创建成功')
    }
    dialogClose()
    await fetchList()
  } catch {
    ElMessage.success(dialogEditId.value ? '更新成功（Mock）' : '创建成功（Mock）')
    dialogClose()
    await fetchList()
  } finally {
    submitLoading.value = false
  }
}

// ---------- mock data ----------
const mockUserList: SysUser[] = [
  { id: 1, username: 'admin', realName: '张三', deptId: 1, deptName: '总公司', phone: '138****1001', roleId: 1, roleName: '超级管理员', status: 1, createTime: '2025-01-01 10:00:00' },
  { id: 2, username: 'zhangwei', realName: '张伟', deptId: 2, deptName: '技术研发部', phone: '138****1002', roleId: 2, roleName: '普通管理员', status: 1, createTime: '2025-02-15 14:30:00' },
  { id: 3, username: 'lihua', realName: '李华', deptId: 3, deptName: '市场营销部', phone: '138****1003', roleId: 3, roleName: '运营人员', status: 1, createTime: '2025-03-20 09:15:00' },
  { id: 4, username: 'wangfang', realName: '王芳', deptId: 4, deptName: '人力资源部', phone: '138****1004', roleId: 3, roleName: '运营人员', status: 0, createTime: '2025-04-10 16:45:00' },
  { id: 5, username: 'zhaolei', realName: '赵磊', deptId: 2, deptName: '技术研发部', phone: '138****1005', roleId: 3, roleName: '运营人员', status: 1, createTime: '2025-05-08 11:20:00' },
]

// ---------- lifecycle ----------
onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
.sys-user-page {
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
