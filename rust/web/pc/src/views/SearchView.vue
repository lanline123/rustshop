<template>
  <div class="tig-search-page container">
    <!-- 面包屑 -->
    <div class="search-breadcrumb">
      <router-link to="/">首页</router-link>
      <span class="sep">/</span>
      <span>{{ activeCategoryName || '全部商品' }}</span>
      <template v-if="keyword">
        <span class="sep">/</span>
        <span>搜索“{{ keyword }}”</span>
      </template>
    </div>

    <!-- 分类筛选栏 -->
    <div class="filter-card">
      <div class="filter-row flex-align-center">
        <span class="filter-label">分类：</span>
        <div class="filter-options">
          <a :class="{ active: !categoryId }" @click="selectCategory(undefined)">全部</a>
          <a
            v-for="node in categoryTree"
            :key="node.categoryId"
            :class="{ active: categoryId === node.categoryId }"
            @click="selectCategory(node.categoryId)"
          >
            {{ node.categoryName }}
          </a>
        </div>
      </div>
      <div class="filter-row flex-align-center">
        <span class="filter-label">价格：</span>
        <div class="filter-options price-filter flex-align-center">
          <el-input-number
            v-model="priceMinInput"
            :min="0"
            :controls="false"
            size="small"
            placeholder="最低价"
            class="price-input"
          />
          <span class="dash">-</span>
          <el-input-number
            v-model="priceMaxInput"
            :min="0"
            :controls="false"
            size="small"
            placeholder="最高价"
            class="price-input"
          />
          <el-button size="small" @click="applyPrice">确定</el-button>
          <el-button v-if="hasPriceFilter" size="small" text @click="clearPrice">清除</el-button>
        </div>
      </div>
    </div>

    <!-- 排序 -->
    <div class="sort-bar flex-between">
      <div class="sort-options flex-align-center">
        <a :class="{ active: sortBy === 'default' }" @click="selectSort('default')">综合</a>
        <a :class="{ active: sortBy === 'sales' }" @click="selectSort('sales')">销量</a>
        <a :class="{ active: sortBy === 'price' }" @click="selectSort('price')">
          价格
          <span v-if="sortBy === 'price'" class="sort-arrow">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
        </a>
        <a :class="{ active: sortBy === 'add_time' }" @click="selectSort('add_time')">上新</a>
      </div>
      <div class="result-count">共 {{ total }} 件商品</div>
    </div>

    <!-- 商品列表 -->
    <div v-loading="loading" class="product-list-wrap">
      <div v-if="products.length" class="goods-grid">
        <div
          v-for="product in products"
          :key="product.productId"
          class="goods-card"
          @click="goProduct(product.productId)"
        >
          <div class="card-img-box">
            <img :src="imageUrl(product.picThumb || product.picUrl)" :alt="product.productName" />
          </div>
          <div class="card-info">
            <div class="card-name">{{ product.productName }}</div>
            <div class="card-price-row flex-between">
              <div class="price-text">
                <span class="symbol">¥</span>
                <span class="integer">{{ priceInteger(product.productPrice) }}</span>
                <span class="decimal">.{{ priceDecimal(product.productPrice) }}</span>
              </div>
              <span class="sales-count">已售 {{ product.sales || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else-if="!loading" description="没有找到符合条件的商品" :image-size="120" />
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, jumper, total"
        background
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { homeApi, productApi } from '@/api';
import { pageItems } from '@/api/types';
import type { CategoryNode, ProductVO } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { imageUrl, priceDecimal, priceInteger } from '@/utils/format';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const categoryTree = ref<CategoryNode[]>([]);
const products = ref<ProductVO[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(20);

const keyword = ref('');
const categoryId = ref<number | undefined>(undefined);
const sortBy = ref('default');
const sortOrder = ref<'asc' | 'desc'>('desc');
const priceMinInput = ref<number | undefined>(undefined);
const priceMaxInput = ref<number | undefined>(undefined);
const priceMin = ref<number | undefined>(undefined);
const priceMax = ref<number | undefined>(undefined);

const hasPriceFilter = computed(() => priceMin.value !== undefined || priceMax.value !== undefined);

const activeCategoryName = computed(() => {
  if (!categoryId.value) return '';
  const found = flattenCategories(categoryTree.value).find((c) => c.categoryId === categoryId.value);
  return found?.categoryName ?? '';
});

function flattenCategories(nodes: CategoryNode[]): CategoryNode[] {
  const out: CategoryNode[] = [];
  const walk = (list: CategoryNode[]) => {
    for (const n of list) {
      out.push(n);
      if (n.children?.length) walk(n.children);
    }
  };
  walk(nodes);
  return out;
}

function syncFromRoute() {
  keyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : '';
  const rawCategory = route.query.categoryId;
  categoryId.value = typeof rawCategory === 'string' ? Number(rawCategory) || undefined : undefined;
  const rawPage = route.query.page;
  page.value = typeof rawPage === 'string' ? Math.max(1, Number(rawPage) || 1) : 1;
  const rawSort = route.query.sortBy;
  sortBy.value = typeof rawSort === 'string' && rawSort ? rawSort : 'default';
  sortOrder.value = route.query.sortOrder === 'asc' ? 'asc' : 'desc';
  const rawMin = route.query.priceMin;
  const rawMax = route.query.priceMax;
  priceMin.value = typeof rawMin === 'string' ? Number(rawMin) || undefined : undefined;
  priceMax.value = typeof rawMax === 'string' ? Number(rawMax) || undefined : undefined;
  priceMinInput.value = priceMin.value;
  priceMaxInput.value = priceMax.value;
}

function updateQuery(patch: Record<string, string | undefined>) {
  const next = { ...route.query, ...patch };
  Object.keys(next).forEach((key) => {
    if (next[key] === undefined || next[key] === '') delete next[key];
  });
  router.replace({ path: '/search', query: next });
}

function selectCategory(id?: number) {
  updateQuery({ categoryId: id ? String(id) : undefined, page: undefined });
}

function selectSort(value: string) {
  if (value === 'default') {
    updateQuery({ sortBy: undefined, sortOrder: undefined, page: undefined });
    return;
  }
  const nextOrder = sortBy.value === value && value === 'price' ? (sortOrder.value === 'asc' ? 'desc' : 'asc') : value === 'price' ? 'asc' : 'desc';
  updateQuery({ sortBy: value, sortOrder: nextOrder, page: undefined });
}

function applyPrice() {
  if (priceMinInput.value !== undefined && priceMaxInput.value !== undefined && priceMinInput.value > priceMaxInput.value) {
    ElMessage.warning('最低价不能高于最高价');
    return;
  }
  updateQuery({
    priceMin: priceMinInput.value !== undefined ? String(priceMinInput.value) : undefined,
    priceMax: priceMaxInput.value !== undefined ? String(priceMaxInput.value) : undefined,
    page: undefined,
  });
}

function clearPrice() {
  priceMinInput.value = undefined;
  priceMaxInput.value = undefined;
  updateQuery({ priceMin: undefined, priceMax: undefined, page: undefined });
}

function onPageChange(next: number) {
  updateQuery({ page: String(next) });
}

function goProduct(id: number) {
  router.push(`/product/${id}`);
}

async function loadCategories() {
  try {
    categoryTree.value = await homeApi.categoryTree();
  } catch {
    categoryTree.value = [];
  }
}

async function loadProducts() {
  loading.value = true;
  try {
    const result = await productApi.list({
      keyword: keyword.value || undefined,
      categoryId: categoryId.value,
      sortBy: sortBy.value === 'default' ? undefined : sortBy.value,
      sortOrder: sortBy.value === 'default' ? undefined : sortOrder.value,
      priceMin: priceMin.value,
      priceMax: priceMax.value,
      page: page.value,
      pageSize: pageSize.value,
    });
    products.value = pageItems(result);
    total.value = Number(result?.total ?? 0) || 0;
  } catch {
    products.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
  applySeo({
    title: activeCategoryName.value ? `${activeCategoryName.value} - 商品列表` : keyword.value ? `搜索：${keyword.value}` : '商品大厅',
    description: 'TigShop 官方商城商品列表',
    noindex: true,
  });
}

watch(
  () => route.query,
  async () => {
    syncFromRoute();
    await loadProducts();
  },
);

onMounted(async () => {
  syncFromRoute();
  await Promise.all([loadCategories(), loadProducts()]);
});
</script>

<style scoped lang="less">
.tig-search-page {
  padding: 18px 0 40px;
}

.search-breadcrumb {
  font-size: 13px;
  color: #888;
  margin-bottom: 14px;
  .sep { margin: 0 6px; color: #ccc; }
  a:hover { color: var(--tig-red); }
}

.filter-card {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 6px;
  padding: 14px 18px;
  margin-bottom: 14px;
}

.filter-row {
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px dashed #f0f0f0;
  &:last-child { border-bottom: none; }

  .filter-label { color: #888; font-size: 13px; width: 52px; flex: 0 0 52px; }

  .filter-options {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;

    a {
      font-size: 13px;
      color: #555;
      cursor: pointer;
      &:hover { color: var(--tig-red); }
      &.active { color: var(--tig-red); font-weight: 700; }
    }
  }

  .price-filter {
    .price-input { width: 110px; }
    .dash { color: #ccc; }
  }
}

.sort-bar {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 6px;
  padding: 10px 18px;
  margin-bottom: 14px;

  .sort-options {
    gap: 20px;
    a {
      font-size: 14px;
      color: #555;
      cursor: pointer;
      &:hover { color: var(--tig-red); }
      &.active { color: var(--tig-red); font-weight: 700; }
      .sort-arrow { font-size: 12px; }
    }
  }

  .result-count { font-size: 13px; color: #999; }
}

.product-list-wrap { min-height: 240px; }

.goods-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}

.goods-card {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;

  &:hover {
    box-shadow: 0 8px 18px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }

  .card-img-box {
    height: 190px;
    background: #f8f8f8;
    img { width: 100%; height: 100%; object-fit: cover; }
  }

  .card-info {
    padding: 10px 12px 14px;

    .card-name {
      font-size: 13px;
      line-height: 1.5;
      height: 39px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .card-price-row {
      margin-top: 8px;
      .price-text {
        display: flex;
        align-items: baseline;
        gap: 2px;
        font-weight: 700;
        .symbol { font-size: 12px; }
        .integer { font-size: 20px; }
        .decimal { font-size: 12px; }
      }
      .sales-count { font-size: 12px; color: #999; }
    }
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 26px;
}

@media (max-width: 1280px) {
  .goods-grid { grid-template-columns: repeat(4, 1fr); }
}
</style>
