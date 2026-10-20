<template>
  <div class="tig-search-page container">
    <!-- Filter Breadcrumb -->
    <div class="filter-header flex-between">
      <div class="filter-crumbs">
        <router-link to="/">首页</router-link> &gt; <span>全部商品搜索结果</span>
      </div>
      <div class="goods-total">共找到 <strong>{{ total }}</strong> 件商品</div>
    </div>

    <!-- Filter Card -->
    <div class="filter-card">
      <div class="filter-row flex-align-center" v-if="categories.length > 0">
        <span class="row-label">商品分类：</span>
        <div class="row-tags">
          <span :class="{ active: currentCatId === null }" @click="filterCat(null)">全部</span>
          <span
            v-for="c in categories"
            :key="c.categoryId"
            :class="{ active: currentCatId === c.categoryId }"
            @click="filterCat(c.categoryId)"
          >
            {{ c.categoryName }}
          </span>
        </div>
      </div>
      <div class="filter-row flex-align-center">
        <span class="row-label">排序方式：</span>
        <div class="row-tags">
          <span :class="{ active: sortKey === 'default' }" @click="changeSort('default')">综合排序</span>
          <span :class="{ active: sortKey === 'sales' }" @click="changeSort('sales')">销量优先</span>
          <span :class="{ active: sortKey === 'price_asc' }" @click="changeSort('price_asc')">价格从低到高</span>
          <span :class="{ active: sortKey === 'price_desc' }" @click="changeSort('price_desc')">价格从高到低</span>
        </div>
      </div>
    </div>

    <!-- Loading Skeleton -->
    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- Product Grid -->
    <div v-else-if="productList.length > 0" class="product-grid">
      <div
        v-for="item in productList"
        :key="item.productId"
        class="search-goods-card"
        @click="$router.push(`/product/${item.productId}`)"
      >
        <div class="img-box">
          <img :src="item.picThumb || item.picUrl || '/default-goods.png'" />
        </div>
        <div class="info-box">
          <div class="price-row flex-between">
            <div class="price-text">
              <span class="symbol">¥</span>
              <span class="integer">{{ Math.floor(item.productPrice || 0) }}</span>
              <span class="decimal">.{{ ((item.productPrice || 0) % 1 * 100).toFixed(0).padStart(2, '0') }}</span>
            </div>
            <span class="sales-text">已售 {{ item.sales || 0 }} 件</span>
          </div>
          <div class="title-row" :title="item.productName">{{ item.productName }}</div>
          <div class="shop-row">自营品质 · 官方正品</div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-wrap">
      <el-empty description="没有找到符合条件的商品" :image-size="120">
        <el-button type="primary" @click="resetFilters">重置筛选条件</el-button>
      </el-empty>
    </div>

    <!-- Pagination -->
    <div v-if="total > pageSize" class="pagination-box">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        layout="prev, pager, next, jumper"
        :total="total"
        background
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import request from '@/utils/request';

const route = useRoute();

const loading = ref(false);
const currentCatId = ref<number | null>(null);
const sortKey = ref('default');
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);

const categories = ref<any[]>([]);
const productList = ref<any[]>([]);

async function loadCategories() {
  try {
    const res: any = await request({
      url: '/category/category/all',
      method: 'get'
    });
    if (Array.isArray(res)) {
      categories.value = res;
    }
  } catch (err) {
    console.error('加载商品分类失败:', err);
  }
}

async function fetchProducts() {
  loading.value = true;
  try {
    const params: Record<string, any> = {
      page: currentPage.value,
      size: pageSize.value
    };

    const kw = (route.query.keyword as string) || '';
    if (kw) params.keyword = kw;

    if (currentCatId.value !== null) {
      params.category_id = currentCatId.value;
    }

    if (sortKey.value === 'sales') {
      params.sort_by = 'sales';
      params.sort_order = 'desc';
    } else if (sortKey.value === 'price_asc') {
      params.sort_by = 'price';
      params.sort_order = 'asc';
    } else if (sortKey.value === 'price_desc') {
      params.sort_by = 'price';
      params.sort_order = 'desc';
    }

    const res: any = await request({
      url: '/product/product/list',
      method: 'get',
      params
    });

    if (res) {
      productList.value = res.list || [];
      total.value = res.total || 0;
    } else {
      productList.value = [];
      total.value = 0;
    }
  } catch (err) {
    console.error('搜索商品失败:', err);
    productList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function filterCat(id: number | null) {
  currentCatId.value = id;
  currentPage.value = 1;
  fetchProducts();
}

function changeSort(key: string) {
  sortKey.value = key;
  currentPage.value = 1;
  fetchProducts();
}

function handlePageChange(p: number) {
  currentPage.value = p;
  fetchProducts();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function resetFilters() {
  currentCatId.value = null;
  sortKey.value = 'default';
  currentPage.value = 1;
  fetchProducts();
}

watch(() => route.query.keyword, () => {
  currentPage.value = 1;
  fetchProducts();
});

onMounted(() => {
  if (route.query.cat) {
    currentCatId.value = Number(route.query.cat);
  }
  loadCategories();
  fetchProducts();
});
</script>

<style scoped lang="less">
.tig-search-page {
  padding: 20px 0 40px;
}

.filter-header {
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--tig-text-secondary);
  strong { color: var(--tig-red); font-size: 15px; }
}

.filter-card {
  background: #ffffff;
  border: 1px solid var(--tig-border-color);
  padding: 16px 20px;
  border-radius: 6px;
  margin-bottom: 24px;

  .filter-row {
    padding: 8px 0;
    font-size: 13px;
    border-bottom: 1px dashed #f0f0f0;
    &:last-child { border-bottom: none; }

    .row-label {
      width: 80px;
      color: #888;
      flex-shrink: 0;
    }

    .row-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;

      span {
        cursor: pointer;
        padding: 3px 10px;
        border-radius: 4px;
        color: #444;
        transition: all 0.2s;

        &:hover { color: var(--tig-red); }
        &.active {
          background: var(--tig-red);
          color: #fff;
        }
      }
    }
  }
}

.loading-wrap, .empty-wrap {
  padding: 40px 0;
  background: #fff;
  border-radius: 8px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.search-goods-card {
  background: #fff;
  border: 1px solid #ebebeb;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--tig-red);
    box-shadow: 0 4px 12px rgba(0,0,0,0.06);
    transform: translateY(-2px);
  }

  .img-box {
    width: 100%;
    height: 220px;
    background: #fbfbfb;
    display: flex;
    align-items: center;
    justify-content: center;
    img {
      max-width: 100%;
      max-height: 100%;
      object-fit: cover;
    }
  }

  .info-box {
    padding: 12px 14px 16px;

    .price-row {
      margin-bottom: 6px;
      .price-text {
        color: var(--tig-red);
        font-weight: bold;
        .symbol { font-size: 13px; }
        .integer { font-size: 20px; }
        .decimal { font-size: 13px; }
      }
      .sales-text {
        font-size: 12px;
        color: #999;
      }
    }

    .title-row {
      font-size: 13px;
      color: #333;
      line-height: 1.5;
      height: 38px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      margin-bottom: 8px;
    }

    .shop-row {
      font-size: 12px;
      color: #999;
    }
  }
}

.pagination-box {
  margin-top: 36px;
  display: flex;
  justify-content: center;
}
</style>
