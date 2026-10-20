<template>
  <div class="business-page">
    <SchemaTable :schema="schema" />
  </div>
</template>

<script setup lang="ts">
/**
 * 商品管理：列表 + 新增/编辑弹窗。
 *
 * 接口（以 `docs/api-contract.json` / Rust 路由为准）：
 * - 列表：`GET /adminapi/product/product/list`（PageResult）
 * - 分类：`GET /adminapi/product/category/list`；品牌：`GET /adminapi/product/brand/list`
 * - 详情：`GET /adminapi/product/product/detail?id=`
 * - 新增：`POST /adminapi/product/product/create`
 * - 编辑：`POST /adminapi/product/product/update`
 * - 删除：`POST /adminapi/product/product/batch`（BatchDTO { ids, type: 'del' }）
 */
import { computed } from 'vue';
import SchemaTable from '@/components/schema/SchemaTable.vue';
import request from '@/utils/request';
import type { FormOption, TableSchema } from '@/components/schema/types';

interface CategoryOption {
  categoryId: number;
  categoryName: string;
  parentId: number;
}

interface BrandOption {
  brandId: number;
  brandName: string;
}

/** 后端返回扁平分类列表（含 parentId），这里组装成 el-cascader 需要的树。 */
async function loadCategoryTree(): Promise<FormOption[]> {
  const res = await request<any>({
    url: '/product/category/list',
    method: 'get',
    params: { page: 1, size: 100 }
  });
  const cats: CategoryOption[] = res?.list ?? [];
  const nodes = new Map<number, FormOption>();
  for (const c of cats) {
    nodes.set(c.categoryId, { label: c.categoryName, value: c.categoryId, children: [] });
  }
  const roots: FormOption[] = [];
  for (const c of cats) {
    const node = nodes.get(c.categoryId);
    if (!node) continue;
    const parent = c.parentId ? nodes.get(c.parentId) : undefined;
    if (parent) {
      parent.children = parent.children || [];
      parent.children.push(node);
    } else {
      roots.push(node);
    }
  }
  return roots;
}

async function loadBrandOptions(): Promise<FormOption[]> {
  const res = await request<any>({
    url: '/product/brand/list',
    method: 'get',
    params: { page: 1, size: 100 }
  });
  const brands: BrandOption[] = res?.list ?? [];
  return brands.map((b) => ({ label: b.brandName, value: b.brandId }));
}

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

const schema = computed<TableSchema>(() => ({
  title: '商品列表',
  apiList: '/product/product/list',
  apiDelete: '/product/product/batch',
  primaryKey: 'productId',
  apiBatchDelete: '/product/product/batch',
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
  formSchema: {
    title: '商品',
    primaryKey: 'productId',
    apiCreate: '/product/product/create',
    apiUpdate: '/product/product/update',
    apiDetail: '/product/product/detail',
    labelWidth: '130px',
    sections: [
      {
        title: '基础信息',
        fields: [
          {
            prop: 'productName',
            label: '商品名称',
            type: 'input',
            rules: [{ required: true, message: '请输入商品名称', trigger: 'blur' }]
          },
          { prop: 'productSn', label: '商品货号', type: 'input', placeholder: '不填则系统自动生成' },
          {
            prop: 'categoryId',
            label: '商品分类',
            type: 'cascader',
            cascaderProps: { emitPath: false, checkStrictly: true },
            optionsLoader: loadCategoryTree,
            rules: [{ required: true, message: '请选择商品分类', trigger: 'change' }]
          },
          {
            prop: 'brandId',
            label: '品牌',
            type: 'select',
            optionsLoader: loadBrandOptions
          },
          {
            prop: 'productBrief',
            label: '商品卖点',
            type: 'textarea',
            placeholder: '简要描述商品卖点（列表/详情展示）'
          },
          {
            prop: 'picUrl',
            label: '商品主图',
            type: 'image',
            extra: '支持 jpg/png，建议 800x800'
          }
        ]
      },
      {
        title: '价格与库存',
        fields: [
          {
            prop: 'productPrice',
            label: '销售价(元)',
            type: 'number',
            min: 0,
            step: 0.01,
            rules: [{ required: true, message: '请输入销售价', trigger: 'blur' }]
          },
          { prop: 'marketPrice', label: '市场价(元)', type: 'number', min: 0, step: 0.01 },
          {
            prop: 'productStock',
            label: '库存',
            type: 'number',
            min: 0,
            defaultValue: 0
          },
          { prop: 'sortOrder', label: '排序权重', type: 'number', min: 0, defaultValue: 50 },
          { prop: 'productStatus', label: '上架销售', type: 'switch', defaultValue: 1 },
          { prop: 'isBest', label: '推荐商品', type: 'switch', defaultValue: 0 },
          { prop: 'isNew', label: '新品', type: 'switch', defaultValue: 0 },
          { prop: 'isHot', label: '热销', type: 'switch', defaultValue: 0 }
        ]
      },
      {
        title: '',
        fields: [
          // 后端 `ProductSaveDTO` 的必填/默认业务字段：普通商品 + 物流配送 + 非包邮
          { prop: 'productType', label: '', type: 'number', defaultValue: 1, hidden: () => true },
          { prop: 'saleMode', label: '', type: 'number', defaultValue: 0, hidden: () => true },
          { prop: 'deliveryMode', label: '', type: 'number', defaultValue: 1, hidden: () => true },
          { prop: 'freeShipping', label: '', type: 'number', defaultValue: 0, hidden: () => true },
          { prop: 'noShipping', label: '', type: 'number', defaultValue: 0, hidden: () => true }
        ]
      }
    ]
  }
}));
</script>

<style scoped>
.business-page {
  width: 100%;
}
</style>
