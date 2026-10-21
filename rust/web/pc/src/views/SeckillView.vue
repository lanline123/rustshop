<template>
  <div class="tig-seckill-page">
    <div class="slogan-bar">
      <div class="container slogan-inner">
        <div class="slogan-title">限时秒杀</div>
        <div class="slogan-sub">限时抢购 · 售完即止 · 每日上新</div>
      </div>
    </div>

    <div class="container seckill-content">
      <div v-loading="loading" class="seckill-list">
        <div v-for="item in items" :key="item.recId || `${item.productId}-${item.skuId}`" class="seckill-item">
          <router-link :to="`/product/${item.productId}`" class="item-link">
            <div class="item-pic">
              <img :src="imageUrl(item.picThumb || item.picUrl)" :alt="item.productName || ''" />
              <div class="seckill-badge">秒杀</div>
            </div>
            <div class="item-name">{{ item.productName || `商品 #${item.productId}` }}</div>
            <div class="item-tip">限时秒杀 抢先提醒</div>
          </router-link>

          <div class="item-operation">
            <div class="price-row">
              <div class="seckill-price">¥{{ formatPrice(item.seckillPrice) }}</div>
              <div class="sell-info">
                <div class="sell-text">已售 {{ soldPercent(item) }}%</div>
                <div class="progress-bar">
                  <div class="progress" :style="{ width: `${soldPercent(item)}%` }"></div>
                </div>
              </div>
            </div>

            <div v-if="item.canJoin === false" class="buy-btn sold-out" :title="item.restrictTip || '仅限指定会员参与'">
              仅限会员
            </div>
            <router-link v-else-if="Number(item.seckillStock) > 0" :to="`/product/${item.productId}`" class="buy-btn">
              立即抢购
            </router-link>
            <div v-else class="buy-btn sold-out">已抢完</div>
          </div>
        </div>
      </div>

      <el-empty v-if="!items.length && !loading" description="当前没有进行中的秒杀活动" :image-size="110" />

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
  </div>
</template>

<script setup lang="ts">
/**
 * 限时秒杀列表（对齐原版 `pages/seckill/List.vue`）。
 *
 * 数据源：`GET /api/home/home/getSeckill`（进行中的秒杀场次）；
 * 接口只返回商品 ID，前端逐条用 `GET /api/product/product/detail?id=` 补齐名称与图片
 * （与原版商品卡信息口径一致）。售罄/仅限会员/可抢购状态按接口字段展示。
 */
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { activityApi, productApi } from '@/api';
import { pageItems } from '@/api/types';
import type { SeckillItem } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { formatPrice, imageUrl } from '@/utils/format';

const loading = ref(true);
const items = ref<SeckillItem[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(15);

function soldPercent(item: SeckillItem): number {
  if (typeof item.soldPercent === 'number') return Math.min(100, Math.max(0, item.soldPercent));
  const stock = Number(item.seckillStock ?? 0);
  const sales = Number(item.seckillSales ?? 0);
  const t = stock + sales;
  return t > 0 ? Math.round((sales / t) * 100) : 100;
}

/** 用商品详情补齐秒杀卡片展示信息（失败时保留 ID 占位，不伪造商品数据）。 */
async function enrichProducts(list: SeckillItem[]) {
  await Promise.all(
    list.map(async (item) => {
      if (!item.productId) return;
      try {
        const detail = await productApi.detail(item.productId);
        item.productName = detail.item?.productName ?? item.productName;
        item.picThumb = detail.item?.picThumb ?? item.picThumb;
        item.picUrl = detail.item?.picUrl ?? item.picUrl;
      } catch {
        // 商品详情不可用时保留占位展示
      }
    }),
  );
}

async function load() {
  loading.value = true;
  try {
    const result = await activityApi.seckill({ page: page.value, size: pageSize.value });
    const list = pageItems(result);
    items.value = list;
    total.value = Number(result?.total ?? 0) || 0;
    await enrichProducts(items.value);
  } catch (err: any) {
    items.value = [];
    total.value = 0;
    ElMessage.error(err?.message || '获取秒杀列表失败');
  } finally {
    loading.value = false;
  }
}

function onPageChange(next: number) {
  page.value = next;
  load();
}

onMounted(() => {
  applySeo({ title: '限时秒杀', noindex: true });
  load();
});
</script>

<style scoped lang="less">
.tig-seckill-page {
  background: #f9f9f9;
  padding-bottom: 50px;
  min-height: 70vh;
}

.slogan-bar {
  background: linear-gradient(120deg, #e1251b, #8a0c06 70%);
  color: #ffffff;
  padding: 36px 0;
  margin-bottom: 20px;

  .slogan-inner {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  .slogan-title { font-size: 34px; font-weight: 800; letter-spacing: 4px; }
  .slogan-sub { font-size: 14px; color: #ffd9d6; }
}

.seckill-content { min-height: 320px; }

.seckill-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}

.seckill-item {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.2s;

  &:hover { box-shadow: 0 8px 18px rgba(0, 0, 0, 0.08); }

  .item-link { display: block; }

  .item-pic {
    position: relative;
    height: 190px;
    background: #fafafa;

    img { width: 100%; height: 100%; object-fit: cover; }

    .seckill-badge {
      position: absolute;
      left: 10px;
      top: 10px;
      background: var(--tig-red);
      color: #fff;
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 4px;
    }
  }

  .item-name {
    font-size: 13px;
    padding: 10px 12px 0;
    height: 40px;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .item-tip { color: #999; font-size: 12px; padding: 4px 12px 0; }

  .item-operation { padding: 10px 12px 14px; }

  .price-row {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    margin-bottom: 10px;

    .seckill-price { color: var(--tig-red); font-size: 22px; font-weight: 800; }
    .sell-info { width: 96px; }
    .sell-text { color: #999; font-size: 11px; text-align: right; }
    .progress-bar {
      height: 6px;
      background: #f3f3f3;
      border-radius: 3px;
      margin-top: 4px;
      overflow: hidden;
      .progress { height: 100%; background: var(--tig-red); }
    }
  }

  .buy-btn {
    display: block;
    text-align: center;
    padding: 8px 0;
    border-radius: 4px;
    background: var(--tig-red);
    color: #fff;
    font-size: 13px;
    font-weight: 700;

    &.sold-out { background: #ddd; color: #888; cursor: not-allowed; }
  }
}

.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; }

@media (max-width: 1280px) {
  .seckill-list { grid-template-columns: repeat(4, 1fr); }
}
</style>
