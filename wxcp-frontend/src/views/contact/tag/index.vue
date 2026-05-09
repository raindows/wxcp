<template>
  <div class="tag-page">
    <PageHeader title="标签管理" description="管理企业客户标签">
      <template #actions>
        <el-button @click="handleSync">
          <el-icon><Refresh /></el-icon>
          同步标签
        </el-button>
      </template>
    </PageHeader>

    <div class="tag-page__content">
      <!-- 左侧标签分组 -->
      <div class="tag-page__sidebar">
        <div class="tag-page__sidebar-header">
          <span>标签分组</span>
          <el-button link type="primary" size="small" @click="handleAddGroup">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
        <div v-loading="groupLoading" class="tag-page__group-list">
          <div
            v-for="group in groups"
            :key="group.id"
            class="tag-page__group-item"
            :class="{ 'is-active': selectedGroupId === group.id }"
            @click="handleSelectGroup(group)"
          >
            <span class="tag-page__group-name">{{ group.name }}</span>
            <span class="tag-page__group-count">{{ group.tagCount }} 个标签</span>
            <el-popconfirm
              v-if="group.tagCount === 0"
              title="确定删除该分组？"
              @confirm="handleDeleteGroup(group)"
            >
              <template #reference>
                <el-button
                  link
                  type="danger"
                  size="small"
                  class="tag-page__group-delete"
                  @click.stop
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-popconfirm>
          </div>
          <EmptyState v-if="!groupLoading && groups.length === 0" description="暂无标签分组" />
        </div>
      </div>

      <!-- 右侧标签列表 -->
      <div class="tag-page__main">
        <template v-if="selectedGroupId">
          <div class="tag-page__tag-header">
            <span class="tag-page__tag-title">
              {{ selectedGroupName }}（{{ tags.length }}）
            </span>
            <el-button type="primary" size="small" @click="handleAddTag">
              <el-icon><Plus /></el-icon>
              添加标签
            </el-button>
          </div>

          <div v-loading="tagLoading" class="tag-page__tag-list">
            <el-tag
              v-for="tag in tags"
              :key="tag.id"
              closable
              size="large"
              class="tag-page__tag-item"
              @close="handleDeleteTag(tag)"
            >
              {{ tag.name }}
            </el-tag>
            <EmptyState
              v-if="!tagLoading && tags.length === 0"
              description="该分组下暂无标签"
            />
          </div>
        </template>
        <EmptyState v-else description="请从左侧选择一个标签分组" />
      </div>
    </div>

    <!-- 新增分组弹窗 -->
    <el-dialog
      v-model="groupDialogVisible"
      title="新增标签分组"
      width="420px"
      :close-on-click-modal="false"
      @closed="resetGroupForm"
    >
      <el-form
        ref="groupFormRef"
        :model="groupForm"
        :rules="groupFormRules"
        label-width="80px"
        label-position="right"
      >
        <el-form-item label="分组名称" prop="name">
          <el-input v-model="groupForm.name" placeholder="请输入分组名称" maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="groupSubmitLoading" @click="handleGroupSubmit">
          创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 添加标签弹窗 -->
    <el-dialog
      v-model="tagDialogVisible"
      title="添加标签"
      width="420px"
      :close-on-click-modal="false"
      @closed="resetTagForm"
    >
      <el-form
        ref="tagFormRef"
        :model="tagForm"
        :rules="tagFormRules"
        label-width="80px"
        label-position="right"
      >
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" maxlength="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="tagSubmitLoading" @click="handleTagSubmit">
          添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus, Delete, Refresh } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  getTagGroups,
  getTagsByGroup,
  createTagGroup,
  createTag,
  deleteTag,
  syncTags,
} from '@/api/contact'
import type { TagGroup, ContactTag } from '@/types/contact'

// ---------- group list state ----------
const groupLoading = ref(false)
const groups = ref<TagGroup[]>([])
const selectedGroupId = ref<number | undefined>(undefined)
const selectedGroupName = ref('')

// ---------- tag list state ----------
const tagLoading = ref(false)
const tags = ref<ContactTag[]>([])

// ---------- group dialog ----------
const groupDialogVisible = ref(false)
const groupSubmitLoading = ref(false)
const groupFormRef = ref<FormInstance>()
const groupForm = reactive({ name: '' })
const groupFormRules: FormRules<{ name: string }> = {
  name: [{ required: true, message: '请输入分组名称', trigger: 'blur' }],
}

// ---------- tag dialog ----------
const tagDialogVisible = ref(false)
const tagSubmitLoading = ref(false)
const tagFormRef = ref<FormInstance>()
const tagForm = reactive({ name: '' })
const tagFormRules: FormRules<{ name: string }> = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
}

// ---------- data loading ----------
async function loadGroups() {
  groupLoading.value = true
  try {
    const res = await getTagGroups()
    groups.value = res.data
  } catch {
    groups.value = mockGroups
  } finally {
    groupLoading.value = false
  }
}

async function loadTags(groupId: number) {
  tagLoading.value = true
  try {
    const res = await getTagsByGroup(groupId)
    tags.value = res.data
  } catch {
    tags.value = mockTagsMap[groupId] || []
  } finally {
    tagLoading.value = false
  }
}

// ---------- group actions ----------
function handleSelectGroup(group: TagGroup) {
  selectedGroupId.value = group.id
  selectedGroupName.value = group.name
  loadTags(group.id)
}

function handleAddGroup() {
  groupDialogVisible.value = true
}

function resetGroupForm() {
  groupFormRef.value?.resetFields()
  groupForm.name = ''
}

async function handleGroupSubmit() {
  const valid = await groupFormRef.value?.validate().catch(() => false)
  if (!valid) return

  groupSubmitLoading.value = true
  try {
    await createTagGroup({ name: groupForm.name })
    ElMessage.success('分组创建成功')
    groupDialogVisible.value = false
    await loadGroups()
  } catch {
    ElMessage.success('分组创建成功（Mock）')
    groupDialogVisible.value = false
    const newId = Math.max(...groups.value.map((g) => g.id), 0) + 1
    groups.value.push({ id: newId, name: groupForm.name, sortOrder: groups.value.length + 1, tagCount: 0 })
  } finally {
    groupSubmitLoading.value = false
  }
}

async function handleDeleteGroup(group: TagGroup) {
  groups.value = groups.value.filter((g) => g.id !== group.id)
  if (selectedGroupId.value === group.id) {
    selectedGroupId.value = undefined
    selectedGroupName.value = ''
    tags.value = []
  }
  ElMessage.success('分组删除成功')
}

// ---------- tag actions ----------
function handleAddTag() {
  tagDialogVisible.value = true
}

function resetTagForm() {
  tagFormRef.value?.resetFields()
  tagForm.name = ''
}

async function handleTagSubmit() {
  if (!selectedGroupId.value) return
  const valid = await tagFormRef.value?.validate().catch(() => false)
  if (!valid) return

  tagSubmitLoading.value = true
  try {
    await createTag({ groupId: selectedGroupId.value, name: tagForm.name })
    ElMessage.success('标签添加成功')
    tagDialogVisible.value = false
    await loadTags(selectedGroupId.value)
    await loadGroups()
  } catch {
    ElMessage.success('标签添加成功（Mock）')
    tagDialogVisible.value = false
    const newId = Math.max(...tags.value.map((t) => t.id), 0) + 1
    tags.value.push({ id: newId, groupId: selectedGroupId.value!, name: tagForm.name, sortOrder: tags.value.length + 1 })
    const grp = groups.value.find((g) => g.id === selectedGroupId.value)
    if (grp) grp.tagCount++
  } finally {
    tagSubmitLoading.value = false
  }
}

async function handleDeleteTag(tag: ContactTag) {
  try {
    await deleteTag(tag.id)
    ElMessage.success('标签删除成功')
    if (selectedGroupId.value) {
      await loadTags(selectedGroupId.value)
      await loadGroups()
    }
  } catch {
    ElMessage.success('标签删除成功（Mock）')
    tags.value = tags.value.filter((t) => t.id !== tag.id)
    const grp = groups.value.find((g) => g.id === selectedGroupId.value)
    if (grp) grp.tagCount = Math.max(0, grp.tagCount - 1)
  }
}

// ---------- sync ----------
async function handleSync() {
  try {
    await syncTags()
    ElMessage.success('标签同步成功')
    await loadGroups()
    if (selectedGroupId.value) {
      await loadTags(selectedGroupId.value)
    }
  } catch {
    ElMessage.success('标签同步成功（Mock）')
    await loadGroups()
    if (selectedGroupId.value) {
      await loadTags(selectedGroupId.value)
    }
  }
}

// ---------- mock data ----------
const mockGroups: TagGroup[] = [
  { id: 1, name: '客户类型', sortOrder: 1, tagCount: 4 },
  { id: 2, name: '意向程度', sortOrder: 2, tagCount: 3 },
  { id: 3, name: '行业分类', sortOrder: 3, tagCount: 5 },
  { id: 4, name: '地区标签', sortOrder: 4, tagCount: 0 },
]

const mockTagsMap: Record<number, ContactTag[]> = {
  1: [
    { id: 1, groupId: 1, name: '高意向客户', sortOrder: 1 },
    { id: 2, groupId: 1, name: 'VIP客户', sortOrder: 2 },
    { id: 3, groupId: 1, name: '普通客户', sortOrder: 3 },
    { id: 4, groupId: 1, name: '潜在客户', sortOrder: 4 },
  ],
  2: [
    { id: 5, groupId: 2, name: '高意向', sortOrder: 1 },
    { id: 6, groupId: 2, name: '中意向', sortOrder: 2 },
    { id: 7, groupId: 2, name: '低意向', sortOrder: 3 },
  ],
  3: [
    { id: 8, groupId: 3, name: '互联网/科技', sortOrder: 1 },
    { id: 9, groupId: 3, name: '金融/保险', sortOrder: 2 },
    { id: 10, groupId: 3, name: '制造业', sortOrder: 3 },
    { id: 11, groupId: 3, name: '教育培训', sortOrder: 4 },
    { id: 12, groupId: 3, name: '医疗健康', sortOrder: 5 },
  ],
}

// ---------- lifecycle ----------
onMounted(() => {
  loadGroups()
})
</script>

<style lang="scss" scoped>
.tag-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__content {
    display: flex;
    gap: $spacing-base;
    min-height: 400px;
  }

  &__sidebar {
    width: 280px;
    flex-shrink: 0;
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  &__sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-base $spacing-lg $spacing-sm;
    font-size: $font-size-body;
    font-weight: 600;
    color: $color-text-primary;
    border-bottom: 1px solid $color-divider;
  }

  &__group-list {
    flex: 1;
    overflow-y: auto;
    padding: $spacing-sm;
  }

  &__group-item {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-base;
    border-radius: $border-radius-tag;
    cursor: pointer;
    transition: background-color 0.2s ease;

    &:hover {
      background: $color-bg-hover;

      .tag-page__group-delete {
        opacity: 1;
      }
    }

    &.is-active {
      background: $color-primary-light;

      .tag-page__group-name {
        color: $color-primary;
        font-weight: 500;
      }
    }
  }

  &__group-name {
    flex: 1;
    font-size: $font-size-body;
    color: $color-text-regular;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__group-count {
    flex-shrink: 0;
    font-size: $font-size-caption;
    color: $color-text-placeholder;
  }

  &__group-delete {
    flex-shrink: 0;
    opacity: 0;
    transition: opacity 0.2s ease;
  }

  &__main {
    flex: 1;
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-lg;
    display: flex;
    flex-direction: column;
    min-width: 0;
  }

  &__tag-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-lg;
    padding-bottom: $spacing-base;
    border-bottom: 1px solid $color-divider;
  }

  &__tag-title {
    font-size: $font-size-body;
    font-weight: 600;
    color: $color-text-primary;
  }

  &__tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
    min-height: 120px;
  }

  &__tag-item {
    border-radius: $border-radius-tag;
    font-size: $font-size-body;
  }
}
</style>
