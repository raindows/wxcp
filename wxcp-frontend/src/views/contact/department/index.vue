<template>
  <div class="department-page">
    <PageHeader title="部门管理" description="管理企业组织架构">
      <template #actions>
        <el-button @click="handleSync">
          <el-icon><Refresh /></el-icon>
          同步部门
        </el-button>
        <el-button type="primary" @click="handleAddRoot">
          <el-icon><Plus /></el-icon>
          新增部门
        </el-button>
      </template>
    </PageHeader>

    <!-- 部门树表格 -->
    <div v-loading="loading" class="department-page__table">
      <el-table
        v-if="treeData.length"
        :data="treeData"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" label="部门名称" min-width="220" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="memberCount" label="成员数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.memberCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAddChild(row)">
              添加子部门
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-popconfirm title="确定删除该部门？子部门将一并删除。" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else description="暂无部门数据，点击同步或新增部门" />
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogEditId ? '编辑部门' : '新增部门'"
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
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentTreeOptions"
            :props="{ label: 'name', value: 'id', children: 'children' } as any"
            check-strictly
            clearable
            filterable
            placeholder="无（顶级部门）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人姓名" maxlength="20" />
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
import { Plus, Refresh } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useDialog } from '@/composables/useDialog'
import {
  getDepartmentTree,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  syncDepartments,
} from '@/api/contact'
import type { Department, DepartmentForm } from '@/types/contact'

// ---------- dialog composable ----------
const { visible: dialogVisible, editId: dialogEditId, open: dialogOpen, close: dialogClose } = useDialog()

// ---------- list state ----------
const loading = ref(false)
const treeData = ref<Department[]>([])

// ---------- form ----------
const formRef = ref<FormInstance>()
const form = reactive<DepartmentForm>({
  name: '',
  leader: '',
  sort: 0,
  status: 1,
})

const formRules: FormRules<DepartmentForm> = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
}

const submitLoading = ref(false)

// ---------- parent tree options (exclude current node and its children when editing) ----------
const parentTreeOptions = computed(() => {
  if (!dialogEditId.value) return [{ id: 0, name: '顶级部门', children: treeData.value }]
  return [{ id: 0, name: '顶级部门', children: filterTree(treeData.value, dialogEditId.value) }]
})

function filterTree(nodes: Department[], excludeId: number): Department[] {
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
    const res = await getDepartmentTree()
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

function handleAddChild(row: Department) {
  dialogOpen()
  form.parentId = row.id
}

function handleEdit(row: Department) {
  dialogOpen(row.id)
  Object.assign(form, {
    parentId: row.parentId || undefined,
    name: row.name,
    leader: row.leader,
    sort: row.sort,
    status: row.status,
  })
}

async function handleDelete(row: Department) {
  try {
    await deleteDepartment(row.id)
    ElMessage.success('删除成功')
    await loadTree()
  } catch {
    ElMessage.success('删除成功（Mock）')
    removeNodeFromTree(treeData.value, row.id)
  }
}

async function handleSync() {
  try {
    await syncDepartments()
    ElMessage.success('部门同步成功')
    await loadTree()
  } catch {
    ElMessage.success('部门同步成功（Mock）')
    await loadTree()
  }
}

function removeNodeFromTree(nodes: Department[], id: number): boolean {
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
    id: undefined,
    parentId: undefined,
    name: '',
    leader: '',
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
      await updateDepartment({ id: dialogEditId.value, ...form })
      ElMessage.success('更新成功')
    } else {
      await createDepartment(form)
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

// ---------- mock data ----------
const mockTree: Department[] = [
  {
    id: 1,
    parentId: 0,
    name: '总公司',
    leader: '张三',
    memberCount: 156,
    sort: 1,
    status: 1,
    children: [
      {
        id: 2,
        parentId: 1,
        name: '技术研发部',
        leader: '李四',
        memberCount: 45,
        sort: 1,
        status: 1,
        children: [
          { id: 5, parentId: 2, name: '前端组', leader: '王五', memberCount: 12, sort: 1, status: 1 },
          { id: 6, parentId: 2, name: '后端组', leader: '赵六', memberCount: 18, sort: 2, status: 1 },
          { id: 7, parentId: 2, name: '测试组', leader: '钱七', memberCount: 8, sort: 3, status: 1 },
        ],
      },
      {
        id: 3,
        parentId: 1,
        name: '市场营销部',
        leader: '孙八',
        memberCount: 32,
        sort: 2,
        status: 1,
        children: [
          { id: 8, parentId: 3, name: '品牌推广组', leader: '周九', memberCount: 10, sort: 1, status: 1 },
          { id: 9, parentId: 3, name: '渠道运营组', leader: '吴十', memberCount: 15, sort: 2, status: 1 },
        ],
      },
      {
        id: 4,
        parentId: 1,
        name: '人力资源部',
        leader: '郑十一',
        memberCount: 18,
        sort: 3,
        status: 1,
      },
    ],
  },
]

// ---------- lifecycle ----------
onMounted(() => {
  loadTree()
})
</script>

<style lang="scss" scoped>
.department-page {
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
