<template>
  <div class="tig-history-page container">
    <div class="page-head flex-between">
      <h1 class="page-title">浏览历史</h1>
      <router-link to="/member" class="back-link">返回个人中心</router-link>
    </div>

    <div v-loading="loading" class="history-body">
      <el-empty v-if="!items.length && !loading" description="还没有浏览记录">
        <el-button type="primary" @click="$router.push('/search')">去逛逛</el-button>
      </el-empty>

      <div v-else class="goods-grid">
        <div v-for="item in items" :key="item.productId" class="goods-card">
          <div class="card-img-box" @click="goProduct(item.productId)">
            <img :src="imageUrl(item.picThumb || item.picUrl)" :alt="item.productName" />
          </div>
          <div class="card-info">
            <div class="card-name" @click="goProduct(item.productId)">{{ item.productName }}</div>
            <div class="card-bottom flex-between">
              <div class="price-text">
                <span class="symbol">¥</span>
                <span class="integer">{{ priceInteger(item.productPrice) }}</span>
                <span class="decimal">.{{ priceDecimal(item.productPrice) }}</span>
              </div>
              <span class="stock-text">{{ item.productStock > 0 ? '有货' : '已售罄' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { historyApi } from '@/api';
import type { HistoryProduct } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { imageUrl, priceDecimal, priceInteger } from '@/utils/format';

const router = useRouter();

const loading = ref(false);
const items = ref<HistoryProduct[]>([]);

function goProduct(productId: number) {
  if (!productId) return;
  router.push(`/product/${productId}`);
}

async function load() {
  loading.value = true;
  try {
    items.value = await historyApi.list();
  } catch {
    items.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  applySeo({ title: '浏览历史', noindex: true });
  load();
});
</script>

<style scoped lang="less">
.tig-history-page {
  padding: 20px 0 50px;
}

.page-head {
  margin-bottom: 16px;
  .page-title { font-size: 22px; font-weight: 700; }
  .back-link { color: #888; font-size: 13px; &:hover { color: var(--tig-red); } }
}

.history-body { min-height: 240px; }

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

  .card-img-box {
    height: 180px;
    background: #f8f8f8;
    cursor: pointer;
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
      cursor: pointer;
      &:hover { color: var(--tig-red); }
    }

    .card-bottom {
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
      .stock-text { font-size: 12px; color: #999; }
    }
  }
}
</style>
