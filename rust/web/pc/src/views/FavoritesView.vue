<template>
  <div class="tig-favorites-page container">
    <div class="page-head flex-between">
      <h1 class="page-title">我的收藏</h1>
      <router-link to="/member" class="back-link">返回个人中心</router-link>
    </div>

    <div v-loading="loading" class="favorites-body">
      <el-empty v-if="!items.length && !loading" description="还没有收藏商品">
        <el-button type="primary" @click="$router.push('/search')">去逛逛</el-button>
      </el-empty>

      <div v-else class="goods-grid">
        <div v-for="item in items" :key="item.collectId" class="goods-card">
          <div class="card-img-box" @click="goProduct(item.productId)">
            <img :src="imageUrl(item.picThumb)" :alt="item.productName || '商品'" />
          </div>
          <div class="card-info">
            <div class="card-name" @click="goProduct(item.productId)">{{ item.productName || '商品已下架' }}</div>
            <div class="card-meta">收藏于 {{ formatTime(item.addTime) }}</div>
            <div class="card-actions">
              <el-button size="small" @click="goProduct(item.productId)">查看商品</el-button>
              <el-button size="small" type="danger" plain :loading="cancelingId === item.productId" @click="cancel(item)">
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { favoriteApi } from '@/api';
import { pageItems } from '@/api/types';
import type { CollectProduct } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { formatTime, imageUrl } from '@/utils/format';

const router = useRouter();

const loading = ref(false);
const items = ref<CollectProduct[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(12);
const cancelingId = ref<number | null>(null);

function goProduct(productId: number) {
  if (!productId) return;
  router.push(`/product/${productId}`);
}

async function load() {
  loading.value = true;
  try {
    const result = await favoriteApi.list({ page: page.value, size: pageSize.value });
    items.value = pageItems(result);
    total.value = Number(result?.total ?? 0) || 0;
  } catch {
    items.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function onPageChange(next: number) {
  page.value = next;
  load();
}

async function cancel(item: CollectProduct) {
  cancelingId.value = item.productId;
  try {
    await favoriteApi.cancel(item.productId);
    ElMessage.success('已取消收藏');
    await load();
  } finally {
    cancelingId.value = null;
  }
}

onMounted(() => {
  applySeo({ title: '我的收藏', noindex: true });
  load();
});
</script>

<style scoped lang="less">
.tig-favorites-page {
  padding: 20px 0 50px;
}

.page-head {
  margin-bottom: 16px;
  .page-title { font-size: 22px; font-weight: 700; }
  .back-link { color: #888; font-size: 13px; &:hover { color: var(--tig-red); } }
}

.favorites-body { min-height: 240px; }

.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.goods-card {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  overflow: hidden;

  .card-img-box {
    height: 200px;
    background: #f8f8f8;
    cursor: pointer;
    img { width: 100%; height: 100%; object-fit: cover; }
  }

  .card-info {
    padding: 12px 14px 14px;

    .card-name {
      font-size: 13px;
      line-height: 1.5;
      height: 40px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      cursor: pointer;
      &:hover { color: var(--tig-red); }
    }

    .card-meta { color: #aaa; font-size: 12px; margin-top: 8px; }
    .card-actions { display: flex; gap: 8px; margin-top: 10px; }
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
