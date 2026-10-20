<template>
  <div class="tig-product-detail-page container">
    <el-skeleton v-if="loading" :rows="12" animated class="detail-skeleton" />

    <el-empty v-else-if="!detail" description="商品不存在或已下架" :image-size="140" />

    <template v-else>
      <div class="detail-main">
        <!-- 图片区 -->
        <div class="gallery-panel">
          <el-carousel height="420px" :interval="5000" indicator-position="outside">
            <el-carousel-item v-for="pic in gallery" :key="pic.picId">
              <img class="gallery-img" :src="imageUrl(pic.picUrl || pic.picThumb)" :alt="item?.productName" />
            </el-carousel-item>
          </el-carousel>
          <div class="thumbs">
            <img
              v-for="pic in gallery"
              :key="`t-${pic.picId}`"
              :src="imageUrl(pic.picThumb || pic.picUrl)"
              :alt="item?.productName"
            />
          </div>
        </div>

        <!-- 信息区 -->
        <div class="info-panel">
          <h1 class="product-name">{{ item?.productName }}</h1>
          <div class="product-brief" v-if="item?.productBrief">{{ item?.productBrief }}</div>

          <div class="price-block">
            <div class="price-line flex-align-center">
              <span class="price-label">商城价</span>
              <span class="price-value">¥{{ formatPrice(currentPrice) }}</span>
              <span v-if="Number(item?.marketPrice) > Number(item?.productPrice)" class="market-price">
                ¥{{ formatPrice(item?.marketPrice) }}
              </span>
            </div>
            <div class="meta-line flex-align-center">
              <span>累计销量 {{ item?.sales || item?.virtualSales || 0 }}</span>
              <span class="sep">|</span>
              <span>库存 {{ currentStock }}</span>
              <span class="sep">|</span>
              <span>商品编号 {{ item?.productSn }}</span>
            </div>
          </div>

          <!-- SKU 选择 -->
          <div v-if="enabledSkus.length" class="sku-block">
            <div class="sku-label">规格</div>
            <div class="sku-options">
              <button
                v-for="sku in enabledSkus"
                :key="sku.skuId"
                class="sku-btn"
                :class="{ active: selectedSkuId === sku.skuId, disabled: sku.skuStock <= 0 }"
                :disabled="sku.skuStock <= 0"
                @click="selectedSkuId = sku.skuId"
              >
                {{ sku.skuValue || `规格 ${sku.skuId}` }}
                <span v-if="sku.skuStock <= 0" class="sold-out">无货</span>
              </button>
            </div>
          </div>

          <div class="quantity-block flex-align-center">
            <span class="sku-label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="Math.max(1, currentStock)" />
          </div>

          <div class="action-block">
            <el-button type="primary" size="large" :loading="adding" @click="addToCart">加入购物车</el-button>
            <el-button size="large" @click="buyNow">立即购买</el-button>
          </div>

          <div class="service-block" v-if="serviceList.length">
            <span class="service-label">服务</span>
            <span v-for="(s, idx) in serviceList" :key="idx" class="service-item">
              {{ s.name || s.title || s.serviceName || '官方服务' }}
            </span>
          </div>
        </div>
      </div>

      <!-- 详情 / 参数 -->
      <div class="detail-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="desc">
            <div class="desc-wrap">
              <template v-for="(block, idx) in descArr" :key="idx">
                <img v-if="block.type === 'image' && block.pic" :src="imageUrl(block.pic)" class="desc-img" />
                <video v-else-if="block.type === 'video' && block.videoUrl" :src="imageUrl(block.videoUrl)" controls class="desc-video" />
                <div v-else-if="block.html" class="desc-html" v-html="block.html"></div>
              </template>
              <el-empty v-if="descArr.length === 0" description="暂无图文详情" :image-size="100" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="规格参数" name="attrs">
            <table v-if="attrRows.length" class="attr-table">
              <tbody>
                <tr v-for="row in attrRows" :key="row.name">
                  <th>{{ row.name }}</th>
                  <td>{{ row.value }}</td>
                </tr>
              </tbody>
            </table>
            <el-empty v-else description="暂无参数" :image-size="100" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { productApi } from '@/api';
import type { ProductDetail, ProductGallery, ProductSku } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { useCartStore } from '@/stores/cart';
import { useUserStore } from '@/stores/user';
import { formatPrice, imageUrl } from '@/utils/format';

const route = useRoute();
const router = useRouter();
const user = useUserStore();
const cart = useCartStore();

const loading = ref(true);
const adding = ref(false);
const detail = ref<ProductDetail | null>(null);
const selectedSkuId = ref<number | null>(null);
const quantity = ref(1);
const activeTab = ref('desc');

const item = computed(() => detail.value?.item);
const gallery = computed<ProductGallery[]>(() => {
  const list = detail.value?.picList ?? [];
  if (list.length) return list;
  if (!item.value) return [];
  return [
    {
      picId: 0,
      picUrl: item.value.picUrl,
      picThumb: item.value.picThumb,
      picOriginal: item.value.picUrl,
      picLarge: item.value.picUrl,
      sortOrder: 0,
    },
  ];
});
const enabledSkus = computed<ProductSku[]>(() =>
  (detail.value?.skuList ?? []).filter((sku) => Number(sku.isEnabled) !== 0),
);
const selectedSku = computed<ProductSku | null>(() =>
  enabledSkus.value.find((sku) => sku.skuId === selectedSkuId.value) ?? null,
);
const currentPrice = computed(() => {
  if (selectedSku.value?.skuPrice !== null && selectedSku.value?.skuPrice !== undefined) {
    return selectedSku.value.skuPrice;
  }
  return item.value?.productPrice ?? 0;
});
const currentStock = computed(() => {
  if (selectedSku.value) return Number(selectedSku.value.skuStock ?? 0);
  return Number(item.value?.productStock ?? 0);
});
const descArr = computed(() => detail.value?.descArr ?? []);
const serviceList = computed<any[]>(() => detail.value?.serviceList ?? []);

/** 属性表展平：normal / spe / extra 三组。 */
const attrRows = computed<Array<{ name: string; value: string }>>(() => {
  const groups = detail.value?.attrList;
  const rows: Array<{ name: string; value: string }> = [];
  const pushGroup = (list?: any[]) => {
    for (const g of list ?? []) {
      if (Array.isArray(g.attrList)) {
        for (const a of g.attrList) {
          rows.push({ name: String(a.attrName ?? a.name ?? ''), value: String(a.attrValue ?? a.value ?? '') });
        }
      } else if (g.attrName) {
        rows.push({ name: String(g.attrName), value: String(g.attrValue ?? '') });
      }
    }
  };
  pushGroup(groups?.normal);
  pushGroup(groups?.spe);
  pushGroup(groups?.extra);
  return rows.filter((r) => r.name);
});

function restoreSku() {
  if (!enabledSkus.value.length) {
    selectedSkuId.value = null;
    return;
  }
  const inStock = enabledSkus.value.find((sku) => Number(sku.skuStock) > 0);
  selectedSkuId.value = (inStock ?? enabledSkus.value[0]).skuId;
}

function requireLogin(): boolean {
  if (user.isLoggedIn) return true;
  router.push({ path: '/login', query: { redirect: route.fullPath } });
  return false;
}

async function addToCart() {
  if (!requireLogin()) return;
  if (enabledSkus.value.length && !selectedSkuId.value) {
    ElMessage.warning('请选择商品规格');
    return;
  }
  if (currentStock.value <= 0) {
    ElMessage.warning('该规格库存不足');
    return;
  }
  adding.value = true;
  try {
    await cart.add(user.token, {
      productId: Number(item.value?.productId ?? 0),
      skuId: selectedSkuId.value,
      quantity: quantity.value,
    });
    ElMessage.success('已加入购物车');
  } finally {
    adding.value = false;
  }
}

async function buyNow() {
  if (!requireLogin()) return;
  if (enabledSkus.value.length && !selectedSkuId.value) {
    ElMessage.warning('请选择商品规格');
    return;
  }
  adding.value = true;
  try {
    await cart.add(user.token, {
      productId: Number(item.value?.productId ?? 0),
      skuId: selectedSkuId.value,
      quantity: quantity.value,
    });
    router.push('/cart');
  } finally {
    adding.value = false;
  }
}

async function loadDetail() {
  const id = Number(route.params.id);
  if (!Number.isFinite(id) || id <= 0) {
    detail.value = null;
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    detail.value = await productApi.detail(id);
    restoreSku();
    const info = detail.value?.item;
    applySeo({
      title: info?.productName,
      description: info?.productBrief || info?.productName,
      keywords: info?.keywords || undefined,
      image: info ? imageUrl(info.picUrl) : undefined,
    });
  } catch {
    detail.value = null;
  } finally {
    loading.value = false;
  }
}

watch(
  () => route.params.id,
  () => {
    loadDetail();
  },
);

onMounted(loadDetail);
</script>

<style scoped lang="less">
.tig-product-detail-page {
  padding: 20px 0 50px;
}

.detail-skeleton { padding: 30px 0; }

.detail-main {
  display: flex;
  gap: 30px;
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  padding: 20px;
}

.gallery-panel {
  width: 460px;
  flex: 0 0 460px;

  .gallery-img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    background: #fafafa;
  }

  .thumbs {
    display: flex;
    gap: 8px;
    margin-top: 10px;
    overflow-x: auto;

    img {
      width: 64px;
      height: 64px;
      object-fit: cover;
      border: 1px solid #eee;
      border-radius: 4px;
      cursor: pointer;
    }
  }
}

.info-panel {
  flex: 1;
  min-width: 0;

  .product-name { font-size: 22px; font-weight: 700; line-height: 1.4; }
  .product-brief { color: #888; font-size: 13px; margin-top: 8px; }
}

.price-block {
  background: #fff7f6;
  border-radius: 6px;
  padding: 14px 16px;
  margin: 16px 0;

  .price-line {
    gap: 10px;
    .price-label { color: #888; font-size: 13px; }
    .price-value { color: var(--tig-red); font-size: 28px; font-weight: 800; }
    .market-price { color: #aaa; text-decoration: line-through; font-size: 13px; }
  }

  .meta-line {
    gap: 10px;
    color: #888;
    font-size: 12px;
    margin-top: 8px;
    .sep { color: #ddd; }
  }
}

.sku-block {
  margin-bottom: 14px;

  .sku-label { color: #888; font-size: 13px; margin-bottom: 8px; }
  .sku-options { display: flex; flex-wrap: wrap; gap: 8px; }

  .sku-btn {
    min-width: 72px;
    padding: 7px 12px;
    background: #fff;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    font-size: 13px;

    &.active {
      border-color: var(--tig-red);
      color: var(--tig-red);
      background: #fff5f4;
    }

    &.disabled { color: #bbb; cursor: not-allowed; }
    .sold-out { font-size: 11px; margin-left: 4px; }
  }
}

.quantity-block {
  gap: 12px;
  margin-bottom: 18px;
  .sku-label { color: #888; font-size: 13px; }
}

.action-block {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
}

.service-block {
  border-top: 1px dashed #eee;
  padding-top: 12px;
  font-size: 12px;
  color: #888;

  .service-label { margin-right: 10px; }
  .service-item {
    margin-right: 14px;
    color: #555;
  }
}

.detail-tabs {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  margin-top: 20px;
  padding: 0 20px 24px;

  .desc-wrap { padding: 10px 0; }
  .desc-img { display: block; max-width: 100%; margin: 0 auto 10px; }
  .desc-video { display: block; max-width: 100%; margin: 0 auto 10px; }
  .desc-html { line-height: 1.8; }
}

.attr-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 12px;
  font-size: 13px;

  th,
  td {
    border: 1px solid #eee;
    padding: 8px 12px;
    text-align: left;
  }

  th {
    width: 180px;
    background: #fafafa;
    color: #666;
    font-weight: 600;
  }
}
</style>
