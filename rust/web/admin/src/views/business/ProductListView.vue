<template>
  <div class="business-page">
    <SchemaTable :key="tableKey" :schema="schema" />
    <ProductEditDrawer
      v-model="drawerVisible"
      :product-id="editingId"
      @saved="handleSaved"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * 商品管理：列表 + 专用编辑抽屉（多规格 SKU / 规格属性 / 相册 / 描述 / 视频）。
 *
 * 接口（以 `docs/api-contract.json` / Rust 路由为准）：
 * - 列表：`GET /adminapi/product/product/list`
 * - 分类/品牌下拉：`GET /adminapi/product/category/list`、`GET /adminapi/product/brand/list`
 * - 详情/新增/编辑：`/product/product/detail|create|update`（见 ProductEditDrawer）
 * - 删除/批量删除：`POST /adminapi/product/product/batch`（BatchDTO { ids, type: 'del' }）
 */
import { computed, ref } from 'vue';
import SchemaTable from '@/components/schema/SchemaTable.vue';
import ProductEditDrawer from '@/components/business/ProductEditDrawer.vue';
import type { TableSchema } from '@/components/schema/types';

type TagKind = 'success' | 'warning' | 'danger' | 'info' | 'primary';

const PRODUCT_STATUS_TAG: Record<number, { text: string; type: TagKind }> = {
  1: { text: '已上架', type: 'success' },
  0: { text: '已下架', type: 'info' }
};

const CHECK_STATUS_TAG: Record<number, { text: string; type: TagKind }> = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已拒绝', type: 'danger' }
};

const tableKey = ref(0);
const drawerVisible = ref(false);
const editingId = ref<number | null>(null);

function openCreate() {
  editingId.value = null;
  drawerVisible.value = true;
}

function openEdit(row: any) {
  editingId.value = Number(row.productId);
  drawerVisible.value = true;
}

function handleSaved() {
  // SchemaTable 内部维护分页/查询状态，用 key 强制重挂载即刷新列表
  tableKey.value += 1;
}

const schema = computed<TableSchema>(() => ({
  title: '商品列表',
  apiList: '/product/product/list',
  apiDelete: '/product/product/batch',
  apiBatchDelete: '/product/product/batch',
  primaryKey: 'productId',
  createLabel: '新增商品',
  onCreate: openCreate,
  searchFields: [
    {
      prop: 'keyword',
      label: '关键词',
      type: 'input',
      placeholder: '商品名称 / 货号 / 商品ID'
    },
    {
      prop: 'productStatus',
      label: '上架状态',
      type: 'select',
      options: [
        { label: '已上架', value: 1 },
        { label: '已下架', value: 0 }
      ]
    },
    {
      prop: 'checkStatus',
      label: '审核状态',
      type: 'select',
      options: [
        { label: '待审核', value: 0 },
        { label: '审核通过', value: 1 },
        { label: '审核拒绝', value: 2 }
      ]
    }
  ],
  columns: [
    { prop: 'picUrl', label: '主图', type: 'image', width: 80 },
    { prop: 'productId', label: 'ID', width: 70, align: 'center' },
    { prop: 'productName', label: '商品名称', minWidth: 220 },
    { prop: 'productSn', label: '货号', minWidth: 130 },
    { prop: 'productPrice', label: '销售价', type: 'money', width: 110 },
    { prop: 'marketPrice', label: '市场价', type: 'money', width: 110 },
    { prop: 'productStock', label: '库存', width: 90, align: 'center' },
    {
      prop: 'productStatus',
      label: '上架状态',
      type: 'tag',
      width: 100,
      tagMap: PRODUCT_STATUS_TAG
    },
    {
      prop: 'checkStatus',
      label: '审核状态',
      type: 'tag',
      width: 100,
      tagMap: CHECK_STATUS_TAG
    },
    { prop: 'sortOrder', label: '排序', width: 80, align: 'center' }
  ],
  actions: [{ label: '编辑', type: 'primary', onClick: openEdit }]
}));
</script>

<style scoped>
.business-page {
  width: 100%;
}
</style>
