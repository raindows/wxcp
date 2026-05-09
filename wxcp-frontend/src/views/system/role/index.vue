<template>
  <div class="sys-role-page">
    <PageHeader title="角色管理" description="管理系统角色与权限分配">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增角色
        </el-button>
      </template>
    </PageHeader>

    <!-- 表格 -->
    <div class="sys-role-page__table">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="name" label="角色名称" min-width="140" />
        <el-table-column prop="roleKey" label="角色标识" width="140" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="success" size="small" @click="handleAssignMenus(row)">
              分配权限
            </el-button>
            <el-popconfirm title="确定删除该角色？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogEditId ? '编辑角色' : '新增角色'"
      width="500px"
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
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" maxlength="30" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="请输入角色标识（如 admin）" maxlength="30" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入角色描述"
            :rows="3"
            maxlength="200"
          />
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

    <!-- 分配权限弹窗 -->
    <el-dialog
      v-model="permDialogVisible"
      title="分配权限"
      width="520px"
      :close-on-click-modal="false"
    >
      <div class="sys-role-page__perm-header">
        <span>当前角色：<strong>{{ permRoleName }}</strong></span>
      </div>
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        show-checkbox
        node-key="id"
        :default-checked-keys="permCheckedKeys"
        :props="{ label: 'name', children: 'children' }"
        default-expand-all
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSubmitLoading" @click="handlePermSubmit">
          确认分配
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useDialog } from '@/composables/useDialog'
import {
  getSysRoleList,
  createSysRole,
  updateSysRole,
  deleteSysRole,
  getMenuTree,
  assignRoleMenus,
} from '@/api/system'
import type { SysRole, SysMenu } from '@/types/system'

// ---------- list state ----------
const loading = ref(false)
const list = ref<SysRole[]>([])

// ---------- dialog composable (create/edit) ----------
const { visible: dialogVisible, editId: dialogEditId, open: dialogOpen, close: dialogClose } = useDialog()

// ---------- form ----------
const formRef = ref<FormInstance>()
const form = reactive({
  name: '',
  roleKey: '',
  description: '',
  status: 1 as 0 | 1,
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }],
}

const submitLoading = ref(false)

// ---------- permission dialog state ----------
const permDialogVisible = ref(false)
const permRoleName = ref('')
const permRoleId = ref<number | undefined>(undefined)
const permCheckedKeys = ref<number[]>([])
const permSubmitLoading = ref(false)
const menuTreeRef = ref()

// ---------- menu tree data ----------
const menuTreeData = ref<SysMenu[]>([])

// ---------- data loading ----------
async function loadRoles() {
  loading.value = true
  try {
    const res = await getSysRoleList()
    list.value = res.data
  } catch {
    list.value = mockRoles
  } finally {
    loading.value = false
  }
}

async function loadMenuTree() {
  try {
    const res = await getMenuTree()
    menuTreeData.value = res.data
  } catch {
    menuTreeData.value = mockMenuTree
  }
}

// ---------- table actions ----------
function handleAdd() {
  dialogOpen()
}

function handleEdit(row: SysRole) {
  dialogOpen(row.id)
  Object.assign(form, {
    name: row.name,
    roleKey: row.roleKey,
    description: row.description,
    status: row.status,
  })
}

async function handleDelete(row: SysRole) {
  try {
    await deleteSysRole(row.id)
    ElMessage.success('删除成功')
    await loadRoles()
  } catch {
    ElMessage.success('删除成功（Mock）')
    const idx = list.value.findIndex((r) => r.id === row.id)
    if (idx !== -1) list.value.splice(idx, 1)
  }
}

async function handleAssignMenus(row: SysRole) {
  permRoleId.value = row.id
  permRoleName.value = row.name
  permCheckedKeys.value = row.menuIds || []
  await loadMenuTree()
  permDialogVisible.value = true
}

// ---------- form actions ----------
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    name: '',
    roleKey: '',
    description: '',
    status: 1,
  })
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (dialogEditId.value) {
      await updateSysRole({ id: dialogEditId.value, ...form })
      ElMessage.success('更新成功')
    } else {
      await createSysRole(form)
      ElMessage.success('创建成功')
    }
    dialogClose()
    await loadRoles()
  } catch {
    ElMessage.success(dialogEditId.value ? '更新成功（Mock）' : '创建成功（Mock）')
    dialogClose()
    await loadRoles()
  } finally {
    submitLoading.value = false
  }
}

// ---------- permission submit ----------
async function handlePermSubmit() {
  if (!permRoleId.value) return
  const tree = menuTreeRef.value
  const checkedKeys = tree.getCheckedKeys() as number[]
  const halfCheckedKeys = tree.getHalfCheckedKeys() as number[]
  const allKeys = [...checkedKeys, ...halfCheckedKeys]

  permSubmitLoading.value = true
  try {
    await assignRoleMenus(permRoleId.value, allKeys)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
    await loadRoles()
  } catch {
    ElMessage.success('权限分配成功（Mock）')
    permDialogVisible.value = false
  } finally {
    permSubmitLoading.value = false
  }
}

// ---------- mock data ----------
const mockRoles: SysRole[] = [
  { id: 1, name: '超级管理员', roleKey: 'admin', description: '拥有系统全部权限', status: 1, menuIds: [1, 2, 3, 4, 5], createTime: '2025-01-01 10:00:00' },
  { id: 2, name: '普通管理员', roleKey: 'manager', description: '拥有大部分管理权限', status: 1, menuIds: [1, 2, 3], createTime: '2025-02-15 14:30:00' },
  { id: 3, name: '运营人员', roleKey: 'operator', description: '运营相关操作权限', status: 1, menuIds: [1, 2], createTime: '2025-03-20 09:15:00' },
  { id: 4, name: '只读用户', roleKey: 'viewer', description: '仅查看权限', status: 0, menuIds: [1], createTime: '2025-04-10 16:45:00' },
]

const mockMenuTree: SysMenu[] = [
  {
    id: 1, parentId: 0, name: '概览', icon: 'Monitor', path: '/dashboard', component: '', permission: '', type: 'directory', sort: 1, status: 1,
    children: [
      { id: 2, parentId: 1, name: '工作台', icon: 'Monitor', path: '/dashboard/workbench', component: 'dashboard/workbench/index', permission: 'dashboard:workbench', type: 'menu', sort: 1, status: 1 },
      { id: 3, parentId: 1, name: '数据看板', icon: 'DataAnalysis', path: '/dashboard/data', component: 'dashboard/data/index', permission: 'dashboard:data', type: 'menu', sort: 2, status: 1 },
    ],
  },
  {
    id: 4, parentId: 0, name: '营销中心', icon: 'Promotion', path: '/marketing', component: '', permission: '', type: 'directory', sort: 2, status: 1,
    children: [
      { id: 5, parentId: 4, name: '客户管理', icon: 'User', path: '/customer/list', component: 'customer/list/index', permission: 'customer:list', type: 'menu', sort: 1, status: 1 },
      { id: 6, parentId: 4, name: '消息推送', icon: 'Promotion', path: '/message/push', component: 'message/push/index', permission: 'message:push', type: 'menu', sort: 2, status: 1 },
    ],
  },
  {
    id: 7, parentId: 0, name: '系统管理', icon: 'Setting', path: '/system', component: '', permission: '', type: 'directory', sort: 3, status: 1,
    children: [
      { id: 8, parentId: 7, name: '用户管理', icon: 'User', path: '/system/user', component: 'system/user/index', permission: 'system:user:list', type: 'menu', sort: 1, status: 1,
        children: [
          { id: 9, parentId: 8, name: '用户新增', icon: '', path: '', component: '', permission: 'system:user:add', type: 'button', sort: 1, status: 1 },
          { id: 10, parentId: 8, name: '用户编辑', icon: '', path: '', component: '', permission: 'system:user:edit', type: 'button', sort: 2, status: 1 },
          { id: 11, parentId: 8, name: '用户删除', icon: '', path: '', component: '', permission: 'system:user:delete', type: 'button', sort: 3, status: 1 },
        ],
      },
    ],
  },
]

// ---------- lifecycle ----------
onMounted(() => {
  loadRoles()
})
</script>

<style lang="scss" scoped>
.sys-role-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__table {
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-lg;
    min-height: 200px;
  }

  &__perm-header {
    margin-bottom: $spacing-base;
    font-size: $font-size-body;
    color: $color-text-placeholder;
  }
}
</style>
