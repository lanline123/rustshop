<template>
  <div class="tig-schema-table">
    <!-- Filter Bar -->
    <div v-if="schema.searchFields && schema.searchFields.length > 0" class="tig-card search-card">
      <el-form :inline="true" :model="searchParams" class="search-form" @submit.prevent>
        <el-form-item
          v-for="item in schema.searchFields"
          :key="item.prop"
          :label="item.label"
        >
          <el-input
            v-if="!item.type || item.type === 'input'"
            v-model="searchParams[item.prop]"
            :placeholder="item.placeholder || `请输入${item.label}`"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-select
            v-else-if="item.type === 'select'"
            v-model="searchParams[item.prop]"
            :placeholder="item.placeholder || `请选择${item.label}`"
            clearable
          >
            <el-option
              v-for="opt in item.options"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleResetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Table Container -->
    <div class="tig-card table-card">
      <!-- Toolbar -->
      <div class="table-toolbar flex-between">
        <div class="table-title">{{ schema.title }}</div>
        <div class="toolbar-actions flex-align-center">
          <el-button
            v-if="schema.formSchema"
            type="primary"
            :icon="Plus"
            @click="handleCreate"
          >
            新增
          </el-button>

          <el-button
            v-if="schema.exportable !== false"
            :icon="Download"
            @click="handleExportCsv"
          >
            导出 CSV
          </el-button>

          <el-button :icon="Refresh" circle @click="fetchData" />
        </div>
      </div>

      <!-- Batch Actions Bar -->
      <el-alert
        v-if="selectedRows.length > 0"
        type="info"
        show-icon
        :closable="false"
        class="batch-alert"
      >
        <template #title>
          <div class="batch-bar flex-between">
            <span>已选中 <strong>{{ selectedRows.length }}</strong> 项数据</span>
            <div class="batch-buttons">
              <template v-for="(act, idx) in schema.batchActions" :key="idx">
                <el-button
                  size="small"
                  :type="act.type || 'default'"
                  @click="act.onClick(selectedRows, fetchData)"
                >
                  {{ act.label }}
                </el-button>
              </template>
              <el-button
                v-if="schema.apiDelete"
                size="small"
                type="danger"
                @click="handleBatchDelete"
              >
                批量删除
              </el-button>
            </div>
          </div>
        </template>
      </el-alert>

      <!-- ElTable -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        class="tig-el-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />

        <template v-for="col in schema.columns" :key="col.prop">
          <!-- Image Column -->
          <el-table-column
            v-if="col.type === 'image'"
            :prop="col.prop"
            :label="col.label"
            :width="col.width || 80"
            :align="col.align || 'center'"
          >
            <template #default="{ row }">
              <el-image
                style="width: 44px; height: 44px; border-radius: 4px"
                :src="resolveImageUrl(row[col.prop])"
                :preview-src-list="[resolveImageUrl(row[col.prop])]"
                preview-teleported
                fit="cover"
              />
            </template>
          </el-table-column>

          <!-- Tag Column -->
          <el-table-column
            v-else-if="col.type === 'tag'"
            :prop="col.prop"
            :label="col.label"
            :width="col.width"
            :align="col.align || 'center'"
          >
            <template #default="{ row }">
              <el-tag
                :type="col.tagMap?.[row[col.prop]]?.type || 'info'"
                effect="light"
              >
                {{ col.tagMap?.[row[col.prop]]?.text || row[col.prop] }}
              </el-tag>
            </template>
          </el-table-column>

          <!-- Money Column -->
          <el-table-column
            v-else-if="col.type === 'money'"
            :prop="col.prop"
            :label="col.label"
            :width="col.width"
            :align="col.align || 'right'"
          >
            <template #default="{ row }">
              <span class="money-text">¥{{ Number(row[col.prop] || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>

          <!-- Switch Column -->
          <el-table-column
            v-else-if="col.type === 'switch'"
            :prop="col.prop"
            :label="col.label"
            :width="col.width || 90"
            :align="col.align || 'center'"
          >
            <template #default="{ row }">
              <el-switch
                v-model="row[col.prop]"
                :active-value="1"
                :inactive-value="0"
                @change="handleSwitchChange(row, col)"
              />
            </template>
          </el-table-column>

          <!-- Standard Column -->
          <el-table-column
            v-else
            :prop="col.prop"
            :label="col.label"
            :width="col.width"
            :min-width="col.minWidth || 100"
            :align="col.align || 'left'"
            :sortable="col.sortable"
          />
        </template>

        <!-- Actions Column -->
        <el-table-column
          label="操作"
          fixed="right"
          :width="actionColumnWidth"
          align="center"
        >
          <template #default="{ row }">
            <div class="row-actions">
              <el-button
                v-if="schema.formSchema"
                type="primary"
                link
                size="small"
                @click="handleEdit(row)"
              >
                编辑
              </el-button>

              <template v-for="(act, idx) in schema.actions" :key="idx">
                <el-button
                  v-if="!act.show || act.show(row)"
                  :type="act.type || 'primary'"
                  link
                  size="small"
                  @click="act.onClick(row)"
                >
                  {{ act.label }}
                </el-button>
              </template>

              <el-popconfirm
                v-if="schema.apiDelete"
                title="确定要删除此条记录吗？"
                confirm-button-text="删除"
                cancel-button-text="取消"
                confirm-button-type="danger"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button type="danger" link size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="table-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-if="schema.formSchema"
      v-model="dialogVisible"
      :title="isEdit ? `编辑${schema.title}` : `新增${schema.title}`"
      width="720px"
      destroy-on-close
    >
      <SchemaForm
        :schema="schema.formSchema"
        :initial-data="currentEditRow"
        :mode="isEdit ? 'edit' : 'create'"
        @success="handleFormSuccess"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { Search, Refresh, Plus, Download } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';
import { pageItems, type PageResult } from '@tigshop/shared';
import type { TableSchema, TableColumnSchema } from './types';
import SchemaForm from './SchemaForm.vue';

const props = defineProps<{
  schema: TableSchema;
}>();

const loading = ref(false);
const tableData = ref<any[]>([]);
const selectedRows = ref<any[]>([]);
const searchParams = reactive<Record<string, any>>({});

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
});

const dialogVisible = ref(false);
const isEdit = ref(false);
const currentEditRow = ref<any>(null);

const actionColumnWidth = computed(() => {
  const extraCount = props.schema.actions?.length || 0;
  const base = props.schema.formSchema ? 60 : 0;
  const del = props.schema.apiDelete ? 60 : 0;
  return Math.max(120, base + del + extraCount * 55);
});

function resolveImageUrl(url: string) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  if (url.startsWith('/static')) return url;
  return `/static/${url}`;
}

async function fetchData() {
  loading.value = true;
  try {
    // 后端分页可能是 PageResult（records/list）或裸数组，统一用 pageItems 兼容
    const res = await request<PageResult<Record<string, any>> | Record<string, any>[]>({
      url: props.schema.apiList,
      method: 'get',
      params: {
        page: pagination.page,
        size: pagination.size,
        ...searchParams
      }
    });

    tableData.value = pageItems(res);
    if (Array.isArray(res)) {
      pagination.total = res.length;
    } else {
      pagination.total = res?.total ?? tableData.value.length;
    }
  } catch (err) {
    // 错误提示由请求基座统一处理
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.page = 1;
  fetchData();
}

function handleResetSearch() {
  for (const k in searchParams) {
    delete searchParams[k];
  }
  pagination.page = 1;
  fetchData();
}

function handleSelectionChange(rows: any[]) {
  selectedRows.value = rows;
}

function handleCreate() {
  isEdit.value = false;
  currentEditRow.value = null;
  dialogVisible.value = true;
}

function handleEdit(row: any) {
  isEdit.value = true;
  currentEditRow.value = { ...row };
  dialogVisible.value = true;
}

async function handleDelete(row: any) {
  if (!props.schema.apiDelete) return;
  const pk = props.schema.primaryKey || 'id';
  const url = props.schema.apiBatchDelete || props.schema.apiDelete;
  const data = props.schema.apiBatchDelete
    ? { ids: [row[pk]], type: 'del' }
    : { id: row[pk] };
  try {
    await request({
      url,
      method: 'post',
      data
    });
    ElMessage.success('删除成功');
    fetchData();
  } catch (err) {}
}

async function handleBatchDelete() {
  if (!props.schema.apiDelete || selectedRows.value.length === 0) return;
  const pk = props.schema.primaryKey || 'id';
  const ids = selectedRows.value.map((r) => r[pk]);
  // Rust 批量删除走 `/batch` + BatchDTO { ids, type: 'del' }；单条 OperateDTO 只认 { id }
  const url = props.schema.apiBatchDelete || props.schema.apiDelete;

  ElMessageBox.confirm(`确定要批量删除选中的 ${ids.length} 项记录吗？`, '批量删除', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url,
        method: 'post',
        data: { ids, type: 'del' }
      });
      ElMessage.success('批量删除成功');
      fetchData();
    } catch (e) {}
  });
}

function handleExportCsv() {
  if (tableData.value.length === 0) {
    ElMessage.warning('暂无数据可导出');
    return;
  }
  const headers = props.schema.columns.map((c) => c.label).join(',');
  const rows = tableData.value.map((row) => {
    return props.schema.columns
      .map((c) => {
        const val = row[c.prop] ?? '';
        return `"${String(val).replace(/"/g, '""')}"`;
      })
      .join(',');
  });
  const csvContent = '\uFEFF' + [headers, ...rows].join('\n');
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.setAttribute('href', url);
  link.setAttribute('download', `${props.schema.title}_${Date.now()}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  ElMessage.success('已导出 CSV 文件');
}

async function handleSwitchChange(row: any, col: TableColumnSchema) {
  if (!col.switchApi) return;
  const pk = props.schema.primaryKey || 'id';
  try {
    await request({
      url: col.switchApi,
      method: 'post',
      data: {
        id: row[pk],
        field: col.prop,
        // 后端 UpdateFieldDTO.val 统一为字符串（serde 不接受 number）
        val: String(row[col.prop])
      }
    });
    ElMessage.success('更新成功');
  } catch (err) {
    row[col.prop] = row[col.prop] === 1 ? 0 : 1;
  }
}

function handleFormSuccess() {
  dialogVisible.value = false;
  fetchData();
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="less">
.tig-schema-table {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card {
  padding-bottom: 4px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}

.table-toolbar {
  margin-bottom: 16px;

  .table-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--tig-text-main);
  }

  .toolbar-actions {
    display: flex;
    gap: 10px;
  }
}

.batch-alert {
  margin-bottom: 14px;
}
.batch-bar {
  width: 100%;
}
.batch-buttons {
  display: flex;
  gap: 8px;
}

.money-text {
  font-weight: 600;
  color: var(--tig-danger);
  font-family: Consolas, Monaco, monospace;
}

.row-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
