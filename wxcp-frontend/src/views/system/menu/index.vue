<template>
  <div class="sys-menu-page">
    <PageHeader title="菜单管理" description="管理系统菜单与权限标识">
      <template #actions>
        <el-button type="primary" @click="handleAddRoot">
          <el-icon><Plus /></el-icon>
          新增菜单
        </el-button>
      </template>
    </PageHeader>

    <!-- 菜单树表格 -->
    <div v-loading="loading" class="sys-menu-page__table">
      <el-table
        v-if="treeData.length"
        :data="treeData"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" label="菜单名称" min-width="180" />
        <el-table-column label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon" style="font-size: 16px">
              <component :is="row.icon" />
            </el-icon>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" width="180" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" width="200" show-overflow-tooltip />
        <el-table-column prop="permission" label="权限标识" width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.permission" size="small" type="warning">{{ row.permission }}</el-tag>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="menuTypeTag(row.type)"
              size="small"
              effect="light"
            >
              {{ menuTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.type !== 'button'"
              link
              type="primary"
              size="small"
              @click="handleAddChild(row)"
            >
              添加子级
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-popconfirm title="确定删除该菜单？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else description="暂无菜单数据" />
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogEditId ? '编辑菜单' : '新增菜单'"
      width="580px"
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
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentTreeOptions"
            :props="{ label: 'name', children: 'children' }"
            value-key="id"
            check-strictly
            clearable
            filterable
            placeholder="无（顶级菜单）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio-button value="directory">目录</el-radio-button>
            <el-radio-button value="menu">菜单</el-radio-button>
            <el-radio-button value="button">按钮</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.type === 'button' ? '按钮名称' : '菜单名称'" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" maxlength="30" />
        </el-form-item>
        <el-form-item v-if="form.type !== 'button'" label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标名称" maxlength="50" />
        </el-form-item>
        <el-form-item v-if="form.type !== 'button'" label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入路由路径" maxlength="100" />
        </el-form-item>
        <el-form-item v-if="form.type === 'menu'" label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="请输入组件路径" maxlength="100" />
        </el-form-item>
        <el-form-item label="权限标识" prop="permission">
          <el-input v-model="form.permission" placeholder="如 system:user:list" maxlength="100" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useDialog } from '@/composables/useDialog'
import { getSysMenuTree, createSysMenu, updateSysMenu, deleteSysMenu } from '@/api/system'
import type { SysMenu } from '@/types/system'

// ---------- dialog composable ----------
const { visible: dialogVisible, editId: dialogEditId, open: dialogOpen, close: dialogClose } = useDialog()

// ---------- list state ----------
const loading = ref(false)
const treeData = ref<SysMenu[]>([])

// ---------- form ----------
const formRef = ref<FormInstance>()
const form = reactive({
  parentId: undefined as number | undefined,
  name: '',
  icon: '',
  path: '',
  component: '',
  permission: '',
  type: 'directory' as 'directory' | 'menu' | 'button',
  sort: 0,
  status: 1 as 0 | 1,
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
}

const submitLoading = ref(false)

// ---------- parent tree options ----------
const parentTreeOptions = computed(() => {
  if (!dialogEditId.value) return [{ id: 0, name: '顶级菜单', children: treeData.value }]
  return [{ id: 0, name: '顶级菜单', children: filterTree(treeData.value, dialogEditId.value) }]
})

function filterTree(nodes: SysMenu[], excludeId: number): SysMenu[] {
  return nodes
    .filter((n) => n.id !== excludeId)
    .map((n) => ({
      ...n,
      children: n.children ? filterTree(n.children, excludeId) : undefined,
    }))
}

// ---------- data loading ----------
async function loadTree() {
  loading.value = true
  try {
    const res = await getSysMenuTree()
    treeData.value = res.data
  } catch {
    treeData.value = mockTree
  } finally {
    loading.value = false
  }
}

// ---------- actions ----------
function handleAddRoot() {
  dialogOpen()
}

function handleAddChild(row: SysMenu) {
  dialogOpen()
  form.parentId = row.id
}

function handleEdit(row: SysMenu) {
  dialogOpen(row.id)
  Object.assign(form, {
    parentId: row.parentId || undefined,
    name: row.name,
    icon: row.icon,
    path: row.path,
    component: row.component,
    permission: row.permission,
    type: row.type,
    sort: row.sort,
    status: row.status,
  })
}

async function handleDelete(row: SysMenu) {
  try {
    await deleteSysMenu(row.id)
    ElMessage.success('删除成功')
    await loadTree()
  } catch {
    ElMessage.success('删除成功（Mock）')
    removeNodeFromTree(treeData.value, row.id)
  }
}

function removeNodeFromTree(nodes: SysMenu[], id: number): boolean {
  const idx = nodes.findIndex((n) => n.id === id)
  if (idx !== -1) {
    nodes.splice(idx, 1)
    return true
  }
  for (const node of nodes) {
    if (node.children && removeNodeFromTree(node.children, id)) return true
  }
  return false
}

// ---------- form actions ----------
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    parentId: undefined,
    name: '',
    icon: '',
    path: '',
    component: '',
    permission: '',
    type: 'directory',
    sort: 0,
    status: 1,
  })
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (dialogEditId.value) {
      await updateSysMenu({ id: dialogEditId.value, ...form })
      ElMessage.success('更新成功')
    } else {
      await createSysMenu(form)
      ElMessage.success('创建成功')
    }
    dialogClose()
    await loadTree()
  } catch {
    ElMessage.success(dialogEditId.value ? '更新成功（Mock）' : '创建成功（Mock）')
    dialogClose()
    await loadTree()
  } finally {
    submitLoading.value = false
  }
}

// ---------- helpers ----------
function menuTypeLabel(type: string): string {
  const map: Record<string, string> = { directory: '目录', menu: '菜单', button: '按钮' }
  return map[type] || type
}

function menuTypeTag(type: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined {
  const map: Record<string, 'success' | 'warning' | 'info'> = {
    directory: 'info',
    menu: 'success',
    button: 'warning',
  }
  return map[type]
}

// ---------- mock data ----------
const mockTree: SysMenu[] = [
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
      { id: 7, parentId: 4, name: '素材管理', icon: 'PictureFilled', path: '/material/list', component: 'material/index', permission: 'material:list', type: 'menu', sort: 3, status: 1 },
    ],
  },
  {
    id: 8, parentId: 0, name: '系统管理', icon: 'Setting', path: '/system', component: '', permission: '', type: 'directory', sort: 3, status: 1,
    children: [
      { id: 9, parentId: 8, name: '用户管理', icon: 'User', path: '/system/user', component: 'system/user/index', permission: 'system:user:list', type: 'menu', sort: 1, status: 1,
        children: [
          { id: 10, parentId: 9, name: '用户新增', icon: '', path: '', component: '', permission: 'system:user:add', type: 'button', sort: 1, status: 1 },
          { id: 11, parentId: 9, name: '用户编辑', icon: '', path: '', component: '', permission: 'system:user:edit', type: 'button', sort: 2, status: 1 },
          { id: 12, parentId: 9, name: '用户删除', icon: '', path: '', component: '', permission: 'system:user:delete', type: 'button', sort: 3, status: 1 },
        ],
      },
      { id: 13, parentId: 8, name: '角色管理', icon: 'Lock', path: '/system/role', component: 'system/role/index', permission: 'system:role:list', type: 'menu', sort: 2, status: 1 },
      { id: 14, parentId: 8, name: '菜单管理', icon: 'Menu', path: '/system/menu', component: 'system/menu/index', permission: 'system:menu:list', type: 'menu', sort: 3, status: 1 },
      { id: 15, parentId: 8, name: '操作日志', icon: 'Document', path: '/system/log', component: 'system/log/index', permission: 'system:log:list', type: 'menu', sort: 4, status: 1 },
      { id: 16, parentId: 8, name: '系统配置', icon: 'Tools', path: '/system/config', component: 'system/config/index', permission: 'system:config:list', type: 'menu', sort: 5, status: 1 },
    ],
  },
]

// ---------- lifecycle ----------
onMounted(() => {
  loadTree()
})
</script>

<style lang="scss" scoped>
.sys-menu-page {
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
}
</style>
