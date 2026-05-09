<template>
  <div class="material-page">
    <PageHeader title="素材管理" description="管理图片、文章、视频、文件等各类素材">
      <template #actions>
        <el-button type="primary" @click="handleAddCategory">
          <el-icon><Plus /></el-icon>
          新建分类
        </el-button>
      </template>
    </PageHeader>

    <div class="material-page__content">
      <!-- 左侧分类树 -->
      <div class="material-page__sidebar">
        <div class="material-page__sidebar-header">
          <span>素材分类</span>
        </div>
        <div v-loading="categoryLoading" class="material-page__tree-wrap">
          <el-tree
            ref="treeRef"
            :data="categories"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          >
            <template #default="{ data }">
              <span class="material-page__tree-node">
                <span class="material-page__tree-label" :title="data.name">
                  {{ data.name }}
                </span>
                <span class="material-page__tree-actions" @click.stop>
                  <el-icon class="material-page__tree-action" @click="handleAddChildCategory(data)">
                    <Plus />
                  </el-icon>
                  <el-icon class="material-page__tree-action" @click="handleRenameCategory(data)">
                    <Edit />
                  </el-icon>
                  <el-popconfirm
                    title="确定删除该分类？"
                    @confirm="handleDeleteCategory(data)"
                  >
                    <template #reference>
                      <el-icon class="material-page__tree-action is-danger">
                        <Delete />
                      </el-icon>
                    </template>
                  </el-popconfirm>
                </span>
              </span>
            </template>
          </el-tree>
          <EmptyState
            v-if="!categoryLoading && categories.length === 0"
            description="暂无分类"
            :image-size="80"
          />
        </div>
      </div>

      <!-- 右侧素材区 -->
      <div class="material-page__main">
        <!-- 上传区域 -->
        <el-upload
          class="material-page__upload"
          drag
          action="#"
          :auto-upload="false"
          accept="image/*,video/*,.pdf,.doc,.docx,.xls,.xlsx"
          :show-file-list="false"
          :on-change="handleFileChange"
        >
          <el-icon class="material-page__upload-icon"><UploadFilled /></el-icon>
          <div class="el-upload__text">将文件拖到此处，或 <em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip">
              支持 图片 / 视频 / 文档 格式，单个文件不超过 10MB
            </div>
          </template>
        </el-upload>

        <!-- 类型筛选 -->
        <div class="material-page__filter">
          <el-radio-group v-model="filterType" size="small" @change="handleFilterChange">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="image">图片</el-radio-button>
            <el-radio-button value="article">文章</el-radio-button>
            <el-radio-button value="video">视频</el-radio-button>
            <el-radio-button value="file">文件</el-radio-button>
          </el-radio-group>
          <span class="material-page__count">共 {{ filteredMaterials.length }} 个素材</span>
        </div>

        <!-- 素材卡片网格 -->
        <div v-loading="materialLoading" class="material-page__grid">
          <div
            v-for="item in filteredMaterials"
            :key="item.id"
            class="material-page__card"
          >
            <div class="material-page__card-thumb">
              <img
                v-if="item.thumbnailUrl"
                :src="item.thumbnailUrl"
                :alt="item.name"
              />
              <div v-else class="material-page__card-placeholder">
                <el-icon :size="32"><component :is="typeIconMap[item.type]" /></el-icon>
              </div>
              <el-tag
                class="material-page__card-type"
                size="small"
                :type="typeTagMap[item.type]"
              >
                {{ typeLabelMap[item.type] }}
              </el-tag>
            </div>
            <div class="material-page__card-info">
              <span class="material-page__card-name" :title="item.name">
                {{ item.name }}
              </span>
              <span class="material-page__card-meta">
                {{ formatFileSize(item.fileSize) }}
              </span>
              <span class="material-page__card-meta">
                {{ item.uploadTime }}
              </span>
            </div>
            <div class="material-page__card-actions">
              <el-button link type="primary" size="small" @click="handlePreview(item)">
                预览
              </el-button>
              <el-popconfirm title="确定删除该素材？" @confirm="handleDeleteMaterial(item)">
                <template #reference>
                  <el-button link type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
          <EmptyState
            v-if="!materialLoading && filteredMaterials.length === 0"
            description="暂无素材，请上传或切换分类"
          />
        </div>
      </div>
    </div>

    <!-- 新建 / 重命名分类弹窗 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="categoryDialogTitle"
      width="420px"
      :close-on-click-modal="false"
      @closed="resetCategoryForm"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryFormRules"
        label-width="80px"
        label-position="right"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySubmitLoading" @click="handleCategorySubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog
      v-model="previewVisible"
      :title="previewMaterial?.name"
      width="680px"
      destroy-on-close
    >
      <div v-if="previewMaterial" class="material-page__preview">
        <img
          v-if="previewMaterial.type === 'image'"
          :src="previewMaterial.url"
          :alt="previewMaterial.name"
          style="max-width: 100%; max-height: 60vh; border-radius: 4px;"
        />
        <video
          v-else-if="previewMaterial.type === 'video'"
          :src="previewMaterial.url"
          controls
          style="width: 100%; max-height: 60vh; border-radius: 4px;"
        />
        <div v-else class="material-page__preview-file">
          <el-icon :size="64" color="#c0c4cc">
            <component :is="typeIconMap[previewMaterial.type]" />
          </el-icon>
          <p>该类型暂不支持在线预览</p>
          <p class="material-page__preview-meta">
            文件大小：{{ formatFileSize(previewMaterial.fileSize) }} | 类型：{{ previewMaterial.mimeType }}
          </p>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Edit, Delete, UploadFilled, Picture, Document, VideoCamera, FolderOpened } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import type { UploadFile } from 'element-plus'
import {
  getMaterialCategories,
  createMaterialCategory,
  updateMaterialCategory,
  deleteMaterialCategory,
  getMaterialList,
  deleteMaterial,
} from '@/api/material'
import type { MaterialCategory, Material, MaterialType } from '@/types/material'

// ---------- type helpers ----------
const typeIconMap: Record<MaterialType, typeof Picture> = {
  image: Picture,
  article: Document,
  video: VideoCamera,
  file: FolderOpened,
}

const typeTagMap: Record<MaterialType, 'primary' | 'success' | 'warning' | 'danger' | 'info' | undefined> = {
  image: 'primary',
  article: 'success',
  video: 'warning',
  file: 'info',
}

const typeLabelMap: Record<MaterialType, string> = {
  image: '图片',
  article: '文章',
  video: '视频',
  file: '文件',
}

// ---------- category state ----------
const categoryLoading = ref(false)
const categories = ref<MaterialCategory[]>([])
const selectedCategoryId = ref<number | undefined>(undefined)

// ---------- category dialog ----------
const categoryDialogVisible = ref(false)
const categoryDialogTitle = ref('新建分类')
const categorySubmitLoading = ref(false)
const categoryFormRef = ref<FormInstance>()
const categoryForm = reactive({
  id: undefined as number | undefined,
  parentId: 0,
  name: '',
})
const categoryFormRules: FormRules<{ name: string }> = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

// ---------- material state ----------
const materialLoading = ref(false)
const materials = ref<Material[]>([])
const filterType = ref('')

const filteredMaterials = computed(() => {
  let list = materials.value
  if (filterType.value) {
    list = list.filter((m) => m.type === filterType.value)
  }
  return list
})

// ---------- preview state ----------
const previewVisible = ref(false)
const previewMaterial = ref<Material | undefined>(undefined)

// ---------- data loading ----------
async function loadCategories() {
  categoryLoading.value = true
  try {
    const res = await getMaterialCategories()
    categories.value = res.data
  } catch {
    categories.value = mockCategories
  } finally {
    categoryLoading.value = false
  }
}

async function loadMaterials(categoryId?: number) {
  materialLoading.value = true
  try {
    const res = await getMaterialList({ categoryId, pageNum: 1, pageSize: 100 })
    materials.value = res.data.list
  } catch {
    materials.value = categoryId ? (mockMaterialsMap[categoryId] || []) : mockAllMaterials
  } finally {
    materialLoading.value = false
  }
}

// ---------- tree actions ----------
function handleNodeClick(data: MaterialCategory) {
  selectedCategoryId.value = data.id
  filterType.value = ''
  loadMaterials(data.id)
}

function handleAddCategory() {
  categoryDialogTitle.value = '新建分类'
  categoryForm.id = undefined
  categoryForm.parentId = 0
  categoryForm.name = ''
  categoryDialogVisible.value = true
}

function handleAddChildCategory(parent: MaterialCategory) {
  categoryDialogTitle.value = '新建子分类'
  categoryForm.id = undefined
  categoryForm.parentId = parent.id
  categoryForm.name = ''
  categoryDialogVisible.value = true
}

function handleRenameCategory(data: MaterialCategory) {
  categoryDialogTitle.value = '重命名分类'
  categoryForm.id = data.id
  categoryForm.parentId = data.parentId
  categoryForm.name = data.name
  categoryDialogVisible.value = true
}

async function handleDeleteCategory(data: MaterialCategory) {
  try {
    await deleteMaterialCategory(data.id)
    ElMessage.success('分类删除成功')
    await loadCategories()
  } catch {
    removeCategoryFromTree(categories.value, data.id)
    ElMessage.success('分类删除成功（Mock）')
  }
  if (selectedCategoryId.value === data.id) {
    selectedCategoryId.value = undefined
    materials.value = []
  }
}

function removeCategoryFromTree(list: MaterialCategory[], id: number): boolean {
  for (let i = 0; i < list.length; i++) {
    if (list[i].id === id) {
      list.splice(i, 1)
      return true
    }
    if (list[i].children && removeCategoryFromTree(list[i].children!, id)) {
      return true
    }
  }
  return false
}

function resetCategoryForm() {
  categoryFormRef.value?.resetFields()
  categoryForm.id = undefined
  categoryForm.parentId = 0
  categoryForm.name = ''
}

async function handleCategorySubmit() {
  const valid = await categoryFormRef.value?.validate().catch(() => false)
  if (!valid) return

  categorySubmitLoading.value = true
  try {
    if (categoryForm.id) {
      await updateMaterialCategory({ id: categoryForm.id, name: categoryForm.name })
      ElMessage.success('分类更新成功')
    } else {
      await createMaterialCategory({ parentId: categoryForm.parentId, name: categoryForm.name })
      ElMessage.success('分类创建成功')
    }
    categoryDialogVisible.value = false
    await loadCategories()
  } catch {
    if (categoryForm.id) {
      updateCategoryNameInTree(categories.value, categoryForm.id, categoryForm.name)
      ElMessage.success('分类更新成功（Mock）')
    } else {
      const newId = getMaxCategoryId(categories.value) + 1
      const newNode: MaterialCategory = { id: newId, parentId: categoryForm.parentId, name: categoryForm.name }
      if (categoryForm.parentId === 0) {
        categories.value.push(newNode)
      } else {
        addCategoryToParent(categories.value, categoryForm.parentId, newNode)
      }
      ElMessage.success('分类创建成功（Mock）')
    }
    categoryDialogVisible.value = false
  } finally {
    categorySubmitLoading.value = false
  }
}

function getMaxCategoryId(list: MaterialCategory[]): number {
  let max = 0
  for (const item of list) {
    if (item.id > max) max = item.id
    if (item.children) {
      const childMax = getMaxCategoryId(item.children)
      if (childMax > max) max = childMax
    }
  }
  return max
}

function updateCategoryNameInTree(list: MaterialCategory[], id: number, name: string): boolean {
  for (const item of list) {
    if (item.id === id) {
      item.name = name
      return true
    }
    if (item.children && updateCategoryNameInTree(item.children, id, name)) {
      return true
    }
  }
  return false
}

function addCategoryToParent(list: MaterialCategory[], parentId: number, newNode: MaterialCategory): boolean {
  for (const item of list) {
    if (item.id === parentId) {
      if (!item.children) item.children = []
      item.children.push(newNode)
      return true
    }
    if (item.children && addCategoryToParent(item.children, parentId, newNode)) {
      return true
    }
  }
  return false
}

// ---------- material actions ----------
function handleFilterChange() {
  // computed handles filtering
}

function handleFileChange(_uploadFile: UploadFile) {
  // Mock: simulate upload
  const newId = Math.max(...materials.value.map((m) => m.id), 0) + 1
  materials.value.unshift({
    id: newId,
    categoryId: selectedCategoryId.value || 0,
    name: `新上传素材_${newId}`,
    type: 'image',
    url: 'https://via.placeholder.com/300x200?text=Uploaded',
    thumbnailUrl: 'https://via.placeholder.com/300x200?text=Uploaded',
    fileSize: 1024 * 256,
    mimeType: 'image/png',
    uploadTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
  })
  ElMessage.success('素材上传成功（Mock）')
}

function handlePreview(item: Material) {
  previewMaterial.value = item
  previewVisible.value = true
}

async function handleDeleteMaterial(item: Material) {
  try {
    await deleteMaterial(item.id)
    ElMessage.success('素材删除成功')
    await loadMaterials(selectedCategoryId.value)
  } catch {
    materials.value = materials.value.filter((m) => m.id !== item.id)
    ElMessage.success('素材删除成功（Mock）')
  }
}

// ---------- helpers ----------
function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// ---------- mock data ----------
const mockCategories: MaterialCategory[] = [
  {
    id: 1,
    parentId: 0,
    name: '营销素材',
    children: [
      { id: 4, parentId: 1, name: '活动海报' },
      { id: 5, parentId: 1, name: '产品介绍' },
    ],
  },
  {
    id: 2,
    parentId: 0,
    name: '客服话术',
    children: [],
  },
  {
    id: 3,
    parentId: 0,
    name: '公司资料',
    children: [],
  },
]

const mockAllMaterials: Material[] = [
  {
    id: 1, categoryId: 1, name: '春节活动海报.jpg', type: 'image',
    url: 'https://via.placeholder.com/600x400?text=Spring+Festival', thumbnailUrl: 'https://via.placeholder.com/300x200?text=Spring+Festival',
    fileSize: 1024 * 512, mimeType: 'image/jpeg', uploadTime: '2026-04-20 10:30:00',
  },
  {
    id: 2, categoryId: 4, name: '618大促海报.png', type: 'image',
    url: 'https://via.placeholder.com/600x400?text=618+Sale', thumbnailUrl: 'https://via.placeholder.com/300x200?text=618+Sale',
    fileSize: 1024 * 768, mimeType: 'image/png', uploadTime: '2026-04-22 14:15:00',
  },
  {
    id: 3, categoryId: 5, name: '产品功能介绍.pdf', type: 'file',
    url: '', fileSize: 1024 * 2048, mimeType: 'application/pdf', uploadTime: '2026-04-18 09:00:00',
  },
  {
    id: 4, categoryId: 2, name: '常见问题解答.txt', type: 'article',
    url: '', fileSize: 1024 * 32, mimeType: 'text/plain', uploadTime: '2026-04-15 16:45:00',
  },
  {
    id: 5, categoryId: 3, name: '公司宣传片.mp4', type: 'video',
    url: '', fileSize: 1024 * 1024 * 50, mimeType: 'video/mp4', uploadTime: '2026-04-10 11:20:00',
  },
  {
    id: 6, categoryId: 4, name: '新品发布海报.jpg', type: 'image',
    url: 'https://via.placeholder.com/600x400?text=New+Product', thumbnailUrl: 'https://via.placeholder.com/300x200?text=New+Product',
    fileSize: 1024 * 640, mimeType: 'image/jpeg', uploadTime: '2026-04-25 08:30:00',
  },
  {
    id: 7, categoryId: 3, name: '员工手册.docx', type: 'file',
    url: '', fileSize: 1024 * 1024 * 3, mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', uploadTime: '2026-04-08 13:10:00',
  },
  {
    id: 8, categoryId: 2, name: '客户感谢信模板.txt', type: 'article',
    url: '', fileSize: 1024 * 16, mimeType: 'text/plain', uploadTime: '2026-04-28 17:00:00',
  },
]

const mockMaterialsMap: Record<number, Material[]> = {
  1: mockAllMaterials.filter((m) => m.categoryId === 1 || m.categoryId === 4 || m.categoryId === 5),
  2: mockAllMaterials.filter((m) => m.categoryId === 2),
  3: mockAllMaterials.filter((m) => m.categoryId === 3),
  4: mockAllMaterials.filter((m) => m.categoryId === 4),
  5: mockAllMaterials.filter((m) => m.categoryId === 5),
}

// ---------- lifecycle ----------
onMounted(() => {
  loadCategories()
  loadMaterials()
})
</script>

<style lang="scss" scoped>
.material-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-base;

  &__content {
    display: flex;
    gap: $spacing-base;
    min-height: 500px;
  }

  // ---------- 左侧分类树 ----------
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

  &__sidebar-header {
    padding: $spacing-base $spacing-lg $spacing-sm;
    font-size: $font-size-body;
    font-weight: 600;
    color: $color-text-primary;
    border-bottom: 1px solid $color-divider;
  }

  &__tree-wrap {
    flex: 1;
    overflow-y: auto;
    padding: $spacing-sm;
  }

  &__tree-node {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex: 1;
    min-width: 0;
    padding-right: $spacing-xs;
  }

  &__tree-label {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: $font-size-body;
    color: $color-text-regular;
  }

  &__tree-actions {
    display: none;
    align-items: center;
    gap: $spacing-xs;
  }

  &__tree-action {
    cursor: pointer;
    font-size: 14px;
    color: $color-text-placeholder;
    transition: color 0.2s ease;

    &:hover {
      color: $color-primary;
    }

    &.is-danger:hover {
      color: $color-danger;
    }
  }

  // hover tree node to show actions
  :deep(.el-tree-node__content) {
    &:hover .material-page__tree-actions {
      display: flex;
    }
  }

  // ---------- 右侧素材区 ----------
  &__main {
    flex: 1;
    background: $color-bg-card;
    border-radius: $border-radius-card;
    border: 1px solid $color-border;
    padding: $spacing-lg;
    display: flex;
    flex-direction: column;
    gap: $spacing-base;
    min-width: 0;
  }

  // ---------- 上传区域 ----------
  &__upload {
    :deep(.el-upload-dragger) {
      border-radius: $border-radius-card;
      padding: $spacing-xl 0;
    }

    :deep(.el-upload) {
      width: 100%;
    }
  }

  &__upload-icon {
    font-size: 40px;
    color: $color-text-disabled;
    margin-bottom: $spacing-sm;
  }

  // ---------- 筛选栏 ----------
  &__filter {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__count {
    font-size: $font-size-caption;
    color: $color-text-placeholder;
  }

  // ---------- 卡片网格 ----------
  &__grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-base;
    min-height: 200px;
  }

  &__card {
    background: $color-bg-card;
    border: 1px solid $color-border;
    border-radius: $border-radius-card;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    transition: box-shadow 0.2s ease;

    &:hover {
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

      .material-page__card-actions {
        opacity: 1;
      }
    }
  }

  &__card-thumb {
    position: relative;
    width: 100%;
    height: 140px;
    background: $color-bg-page;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__card-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    color: $color-text-disabled;
  }

  &__card-type {
    position: absolute;
    top: $spacing-sm;
    right: $spacing-sm;
  }

  &__card-info {
    padding: $spacing-sm $spacing-base;
    display: flex;
    flex-direction: column;
    gap: 2px;
    flex: 1;
    min-width: 0;
  }

  &__card-name {
    font-size: $font-size-body;
    font-weight: 500;
    color: $color-text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__card-meta {
    font-size: $font-size-caption;
    color: $color-text-placeholder;
  }

  &__card-actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: $spacing-xs;
    padding: $spacing-xs $spacing-base $spacing-sm;
    opacity: 0;
    transition: opacity 0.2s ease;
  }

  // ---------- 预览 ----------
  &__preview {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 200px;
  }

  &__preview-file {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-sm;
    color: $color-text-placeholder;

    p {
      margin: 0;
    }
  }

  &__preview-meta {
    font-size: $font-size-caption;
    color: $color-text-placeholder;
  }
}
</style>
