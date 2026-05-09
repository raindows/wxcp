<template>
  <div class="user-page">
    <PageHeader title="用户管理" description="管理企业成员信息">
      <template #actions>
        <el-button @click="handleSync">
          <el-icon><Refresh /></el-icon>
          同步用户
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </template>
    </PageHeader>

    <div class="user-page__content">
      <!-- 左侧部门树 -->
      <div class="user-page__sidebar">
        <div class="user-page__tree-header">部门列表</div>
        <el-input
          v-model="treeFilter"
          placeholder="搜索部门"
          clearable
          size="small"
          class="user-page__tree-filter"
        />
        <el-scrollbar class="user-page__tree-scroll">
          <el-tree
            ref="treeRef"
            :data="departmentTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            highlight-current
            default-expand-all
            :filter-node-method="filterTreeNode"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="user-page__tree-node">
                <span class="user-page__tree-label" :title="node.label">{{ node.label }}</span>
                <span class="user-page__tree-count">{{ data.memberCount }}</span>
              </span>
            </template>
          </el-tree>
        </el-scrollbar>
      </div>

      <!-- 右侧用户列表 -->
      <div class="user-page__main">
        <FilterBar @search="handleSearch" @reset="handleReset">
          <el-input
            v-model="query.keyword"
            placeholder="搜索姓名/手机号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </FilterBar>

        <div class="user-page__table">
          <el-table
            v-loading="loading"
            :data="list"
            stripe
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column label="姓名" width="120">
              <template #default="{ row }">
                <div class="user-page__user-info">
                  <el-avatar :size="32">{{ row.name?.charAt(0) }}</el-avatar>
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="departmentName" label="部门" min-width="140" />
            <el-table-column prop="position" label="职位" width="120" />
            <el-table-column prop="mobile" label="手机号" width="140" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right" align="center">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleEdit(row)">
                  编辑
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
          <div class="user-page__pagination">
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
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogEditId ? '编辑用户' : '新增用户'"
      width="550px"
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
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="所属部门" prop="departmentId">
          <el-tree-select
            v-model="form.departmentId"
            :data="departmentTree"
            :props="{ label: 'name', value: 'id', children: 'children' } as any"
            check-strictly
            clearable
            filterable
            placeholder="请选择部门"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="form.position" placeholder="请输入职位" maxlength="30" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="企微UserId" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入企微 UserId" />
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
import { ref, reactive, onMounted, watch } from 'vue'
import { Plus, Refresh } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useTable } from '@/composables/useTable'
import { useDialog } from '@/composables/useDialog'
import {
  getDepartmentTree,
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  syncUsers,
} from '@/api/contact'
import type { Department, ContactUser, ContactUserQuery, ContactUserForm } from '@/types/contact'

// ---------- dialog composable ----------
const { visible: dialogVisible, editId: dialogEditId, open: dialogOpen, close: dialogClose } = useDialog()

// ---------- table composable ----------
const { loading, list, total, query, loadData, resetQuery, handleSelectionChange, handleSizeChange, handleCurrentChange } =
  useTable<ContactUser, ContactUserQuery>()

// ---------- department tree ----------
const treeRef = ref()
const treeFilter = ref('')
const departmentTree = ref<Department[]>([])
const selectedDepartmentId = ref<number | undefined>(undefined)

watch(treeFilter, (val) => {
  treeRef.value?.filter(val)
})

function filterTreeNode(value: string, data: any) {
  return data.name.includes(value)
}

function handleNodeClick(data: Department) {
  selectedDepartmentId.value = data.id
  query.departmentId = data.id
  query.pageNum = 1
  loadData(getUserList)
}

// ---------- form ----------
const formRef = ref<FormInstance>()
const form = reactive<ContactUserForm>({
  name: '',
  departmentId: undefined,
  position: '',
  mobile: '',
  userId: '',
  status: 1,
})

const formRules: FormRules<ContactUserForm> = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}

const submitLoading = ref(false)

// ---------- data loading ----------
async function loadDepartmentTree() {
  try {
    const res = await getDepartmentTree()
    departmentTree.value = res.data
  } catch {
    departmentTree.value = mockDepartmentTree
  }
}

async function loadAllData() {
  await Promise.all([loadDepartmentTree(), loadData(getUserList)])
}

// ---------- filter handlers ----------
async function handleSearch() {
  query.pageNum = 1
  await loadData(getUserList)
}

async function handleReset() {
  selectedDepartmentId.value = undefined
  await resetQuery(getUserList)
}

// ---------- actions ----------
function handleAdd() {
  dialogOpen()
  // Pre-select current department
  if (selectedDepartmentId.value) {
    form.departmentId = selectedDepartmentId.value
  }
}

function handleEdit(row: ContactUser) {
  dialogOpen(row.id)
  Object.assign(form, {
    name: row.name,
    departmentId: row.departmentId,
    position: row.position,
    mobile: row.mobile,
    userId: row.userId,
    status: row.status,
  })
}

async function handleDelete(row: ContactUser) {
  try {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    await loadData(getUserList)
  } catch {
    ElMessage.success('删除成功（Mock）')
    list.value = list.value.filter((u) => u.id !== row.id)
  }
}

async function handleSync() {
  try {
    await syncUsers()
    ElMessage.success('用户同步成功')
    await loadAllData()
  } catch {
    ElMessage.success('用户同步成功（Mock）')
    await loadAllData()
  }
}

// ---------- form actions ----------
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: undefined,
    name: '',
    departmentId: undefined,
    position: '',
    mobile: '',
    userId: '',
    status: 1,
  })
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (dialogEditId.value) {
      await updateUser({ id: dialogEditId.value, ...form })
      ElMessage.success('更新成功')
    } else {
      await createUser({ ...form, departmentId: form.departmentId! })
      ElMessage.success('创建成功')
    }
    dialogClose()
    await loadData(getUserList)
  } catch {
    ElMessage.success(dialogEditId.value ? '更新成功（Mock）' : '创建成功（Mock）')
    dialogClose()
    await loadData(getUserList)
  } finally {
    submitLoading.value = false
  }
}

// ---------- mock data ----------
const mockDepartmentTree: Department[] = [
  {
    id: 1,
    parentId: 0,
    name: '总公司',
    leader: '张三',
    memberCount: 156,
    sort: 1,
    status: 1,
    children: [
      { id: 2, parentId: 1, name: '技术研发部', leader: '李四', memberCount: 45, sort: 1, status: 1 },
      { id: 3, parentId: 1, name: '市场营销部', leader: '孙八', memberCount: 32, sort: 2, status: 1 },
      { id: 4, parentId: 1, name: '人力资源部', leader: '郑十一', memberCount: 18, sort: 3, status: 1 },
    ],
  },
]

// ---------- lifecycle ----------
onMounted(() => {
  loadAllData()
})
</script>

<style lang="scss" scoped>
.user-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__content {
    display: flex;
    gap: $spacing-base;
    min-height: 400px;
  }

  &__sidebar {
    width: 240px;
    flex-shrink: 0;
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  &__tree-header {
    padding: $spacing-base $spacing-lg $spacing-sm;
    font-size: $font-size-body;
    font-weight: 600;
    color: $color-text-primary;
    border-bottom: 1px solid $color-divider;
  }

  &__tree-filter {
    padding: $spacing-sm $spacing-lg;
  }

  &__tree-scroll {
    flex: 1;
    padding: 0 $spacing-sm $spacing-sm;
  }

  &__tree-node {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex: 1;
    overflow: hidden;
    padding-right: $spacing-xs;
  }

  &__tree-label {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: $font-size-body;
    color: $color-text-regular;
  }

  &__tree-count {
    flex-shrink: 0;
    font-size: $font-size-caption;
    color: $color-text-placeholder;
    margin-left: $spacing-xs;
  }

  &__main {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: $spacing-base;
    min-width: 0;
  }

  &__table {
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-lg;
  }

  &__user-info {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-base;
  }
}
</style>
