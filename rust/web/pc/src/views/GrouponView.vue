<template>
  <div class="tig-groupon-page">
    <!-- 活动轮播 -->
    <div class="container banner-wrap">
      <div v-if="bannerImages.length" class="banner-carousel">
        <el-carousel height="280px" :interval="5000" :autoplay="bannerImages.length > 1">
          <el-carousel-item v-for="(img, idx) in bannerImages" :key="idx">
            <img :src="imageUrl(img)" alt="拼团活动" class="banner-img" />
          </el-carousel-item>
        </el-carousel>
      </div>
      <div v-else class="banner-fallback">
        <h1>拼团活动</h1>
        <p>邀请好友一起拼 · 拼得多省得多</p>
      </div>
    </div>

    <div class="container groupon-content">
      <!-- 筛选 -->
      <div class="filter-bar flex-between">
        <div class="filter-title">拼团好物</div>
        <div class="filter-actions">
          <el-input
            v-model="keyword"
            placeholder="搜索拼团商品"
            clearable
            style="width: 240px"
            @keyup.enter="onSearch"
            @clear="onSearch"
          />
          <el-button type="primary" @click="onSearch">搜索</el-button>
        </div>
      </div>

      <div v-loading="loading" class="groupon-list-wrap">
        <div v-if="items.length" class="groupon-list">
          <div v-for="item in items" :key="item.grouponPromotionItemId" class="groupon-item">
            <router-link :to="`/product/${item.productId}`" class="item-link">
              <div class="item-pic">
                <img :src="imageUrl(item.picThumb || item.picUrl)" :alt="item.productName || ''" />
                <div class="groupon-badge">拼团</div>
              </div>
              <div class="item-name">{{ item.productName || `商品 #${item.productId}` }}</div>
            </router-link>

            <div class="item-info">
              <div class="price-row">
                <div class="groupon-price">
                  <span class="price-label">拼团价</span>
                  ¥{{ formatPrice(item.minGrouponPrice ?? item.productPrice) }}
                </div>
                <div class="group-num">
                  <span v-if="item.minGroupNum">{{ item.minGroupNum }} 人团</span>
                  <span v-else>多人团</span>
                </div>
              </div>
              <div class="join-info">
                已拼 {{ item.joinNumCount || 0 }} 件
                <span v-if="item.groupNum" class="group-num-text">（{{ item.groupNum }}）</span>
              </div>
              <div class="time-info">{{ item.startTime }} ~ {{ item.endTime }}</div>
            </div>

            <div class="item-action">
              <div v-if="item.canJoin === false" class="buy-btn disabled" :title="item.restrictTip || '仅限指定会员参与'">
                仅限会员
              </div>
              <router-link v-else :to="`/product/${item.productId}`" class="buy-btn">去拼团</router-link>
            </div>
          </div>
        </div>

        <el-empty v-else-if="!loading" description="当前没有进行中的拼团活动" :image-size="110" />

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
  </div>
</template>

<script setup lang="ts">
/**
 * 拼团活动页（对齐原版 `pages/joining/List.vue` + `pages/activity/pcGroupon`）。
 *
 * 接口：`GET /api/promotion/groupon/imageList`（活动轮播图）、
 * `GET /api/promotion/groupon/itemList`（活动商品，支持 keyword/排序/分页）。
 * 商品卡展示拼团价、成团人数、已拼件数、活动时间与参与限制。
 */
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { activityApi } from '@/api';
import { pageItems } from '@/api/types';
import type { GrouponImage, GrouponItem } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { formatPrice, imageUrl } from '@/utils/format';

const loading = ref(true);
const bannerImages = ref<string[]>([]);
const items = ref<GrouponItem[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(12);
const keyword = ref('');

async function loadBanners() {
  try {
    const result = await activityApi.grouponImages({ page: 1, size: 5 });
    const list: GrouponImage[] = pageItems(result);
    const seen = new Set<string>();
    const images: string[] = [];
    for (const item of list) {
      const img = String(item.pcGrouponImage ?? '').trim();
      if (!img || seen.has(img)) continue;
      seen.add(img);
      images.push(img);
    }
    bannerImages.value = images;
  } catch {
    bannerImages.value = [];
  }
}

async function loadItems() {
  loading.value = true;
  try {
    const result = await activityApi.grouponItems({
      keyword: keyword.value || undefined,
      page: page.value,
      size: pageSize.value,
    });
    items.value = pageItems(result);
    total.value = Number(result?.total ?? 0) || 0;
  } catch (err: any) {
    items.value = [];
    total.value = 0;
    ElMessage.error(err?.message || '获取拼团活动失败');
  } finally {
    loading.value = false;
  }
}

function onSearch() {
  page.value = 1;
  loadItems();
}

function onPageChange(next: number) {
  page.value = next;
  loadItems();
}

onMounted(() => {
  applySeo({ title: '拼团活动', noindex: true });
  loadBanners();
  loadItems();
});
</script>

<style scoped lang="less">
.tig-groupon-page {
  background: #f9f9f9;
  padding-bottom: 50px;
  min-height: 70vh;
}

.banner-wrap { padding: 20px 0; }

.banner-carousel {
  border-radius: 8px;
  overflow: hidden;
}

.banner-img { width: 100%; height: 100%; object-fit: cover; }

.banner-fallback {
  height: 280px;
  border-radius: 8px;
  background: linear-gradient(120deg, #ff7a18, #e1251b 70%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;

  h1 { font-size: 34px; font-weight: 800; letter-spacing: 4px; }
  p { color: #ffe3d6; font-size: 14px; }
}

.groupon-content { min-height: 320px; }

.filter-bar {
  margin-bottom: 14px;

  .filter-title { font-size: 20px; font-weight: 700; }
  .filter-actions { display: flex; gap: 10px; }
}

.groupon-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.groupon-item {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s;

  &:hover { box-shadow: 0 8px 18px rgba(0, 0, 0, 0.08); }

  .item-link { display: block; }

  .item-pic {
    position: relative;
    height: 200px;
    background: #fafafa;

    img { width: 100%; height: 100%; object-fit: cover; }

    .groupon-badge {
      position: absolute;
      left: 10px;
      top: 10px;
      background: #ff7a18;
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

  .item-info { padding: 8px 12px 0; }

  .price-row {
    display: flex;
    align-items: baseline;
    justify-content: space-between;

    .groupon-price {
      color: var(--tig-red);
      font-size: 20px;
      font-weight: 800;
      .price-label { font-size: 11px; color: #999; font-weight: 400; margin-right: 4px; }
    }
    .group-num { color: #ff7a18; font-size: 12px; }
  }

  .join-info { color: #999; font-size: 12px; margin-top: 6px; }
  .time-info { color: #bbb; font-size: 11px; margin-top: 4px; }

  .item-action { padding: 12px; margin-top: auto; }

  .buy-btn {
    display: block;
    text-align: center;
    padding: 8px 0;
    border-radius: 4px;
    background: #ff7a18;
    color: #fff;
    font-size: 13px;
    font-weight: 700;

    &.disabled { background: #ddd; color: #888; cursor: not-allowed; }
  }
}

.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; }

@media (max-width: 1280px) {
  .groupon-list { grid-template-columns: repeat(3, 1fr); }
}
</style>
