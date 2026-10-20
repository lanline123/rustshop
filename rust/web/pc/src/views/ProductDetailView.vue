<template>
  <div class="tig-product-detail-page container">
    <!-- Loading Skeleton -->
    <div v-if="loading" style="padding: 40px 0;">
      <el-skeleton :rows="12" animated />
    </div>

    <!-- Not Found State -->
    <div v-else-if="!product || !product.item" style="padding: 60px 0; text-align: center;">
      <el-empty description="该商品不存在或已下架" :image-size="120">
        <el-button type="primary" @click="$router.push('/search')">去选购其他商品</el-button>
      </el-empty>
    </div>

    <!-- Product Detail Content -->
    <template v-else>
      <div class="product-crumb">
        <router-link to="/">首页</router-link> &gt;
        <router-link to="/search">商品列表</router-link> &gt;
        <span>{{ product.item.productName }}</span>
      </div>

      <!-- Main Detail Panel -->
      <div class="product-main-panel flex-between">
        <!-- Left: Gallery -->
        <div class="gallery-col">
          <div class="main-img-box">
            <img :src="currentImg || product.item.picUrl || product.item.picThumb || '/default-goods.png'" />
          </div>
          <div v-if="galleryList.length > 0" class="thumb-list flex-align-center">
            <div
              v-for="(img, idx) in galleryList"
              :key="idx"
              :class="['thumb-item', { active: currentImg === img }]"
              @mouseenter="currentImg = img"
            >
              <img :src="img" />
            </div>
          </div>
        </div>

        <!-- Right: Buying Form -->
        <div class="buy-col">
          <h1 class="p-title">{{ product.item.productName }}</h1>
          <p class="p-subtitle">{{ product.item.subTitle || '官方正品保证 · 极速物流 · 售后无忧' }}</p>

          <div class="price-banner">
            <div class="price-row flex-align-center">
              <span class="label">售价</span>
              <div class="price-text">
                <span class="symbol">¥</span>
                <span class="integer">{{ Math.floor(activePrice) }}</span>
                <span class="decimal">.{{ ((activePrice % 1) * 100).toFixed(0).padStart(2, '0') }}</span>
              </div>
              <span v-if="product.item.marketPrice" class="market-price">
                参考划线价 ¥{{ product.item.marketPrice }}
              </span>
            </div>
            <div class="coupon-tips flex-align-center">
              <span class="coupon-tag">正品保障</span>
              <span class="coupon-tag">顺丰速达</span>
              <span class="tip-text">支持 7 天无理由退换货（部分特殊商品除外）</span>
            </div>
          </div>

          <!-- Spec Selections (SKU) -->
          <div class="spec-section">
            <div v-if="product.skuList && product.skuList.length > 0" class="spec-row flex-align-center">
              <span class="spec-label">规格选择</span>
              <div class="spec-options flex-align-center">
                <span
                  v-for="sku in product.skuList"
                  :key="sku.skuId"
                  :class="['spec-btn', { active: selectedSkuId === sku.skuId }]"
                  @click="selectedSkuId = sku.skuId"
                >
                  {{ sku.skuName || sku.skuValue || `规格 #${sku.skuId}` }}
                </span>
              </div>
            </div>

            <div class="spec-row flex-align-center">
              <span class="spec-label">购买数量</span>
              <el-input-number v-model="quantity" :min="1" :max="Math.max(1, activeStock)" size="large" />
              <span class="stock-text">库存：{{ activeStock }} 件</span>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="action-buttons flex-align-center">
            <button class="btn-buy-now" :disabled="submitting || activeStock <= 0" @click="handleBuyNow">
              {{ activeStock > 0 ? '立即购买' : '暂时缺货' }}
            </button>
            <button class="btn-add-cart" :disabled="submitting || activeStock <= 0" @click="handleAddToCart">
              <el-icon><ShoppingCart /></el-icon> 加入购物车
            </button>
          </div>
        </div>
      </div>

      <!-- Product Introduction Tabs -->
      <div class="product-intro-tabs">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="商品详情介绍" name="detail">
            <div class="rich-content">
              <div v-if="product.item.productDesc" v-html="product.item.productDesc"></div>
              <div v-else-if="product.descArr && product.descArr.length > 0">
                <div v-for="(desc, i) in product.descArr" :key="i" class="desc-item">
                  <img v-if="desc.picUrl" :src="desc.picUrl" style="max-width: 100%;" />
                  <p v-if="desc.text">{{ desc.text }}</p>
                </div>
              </div>
              <el-empty v-else description="暂无图文详情介绍" :image-size="80" />
            </div>
          </el-tab-pane>
          <el-tab-pane label="规格与包装" name="specs">
            <div style="padding: 24px;">
              <table class="spec-table" v-if="product.attrList?.attrList?.length">
                <tr v-for="attr in product.attrList.attrList" :key="attr.attrId">
                  <td style="width: 160px; font-weight: bold; background: #fafafa; padding: 10px;">{{ attr.attrName }}</td>
                  <td style="padding: 10px;">{{ attr.attrValue }}</td>
                </tr>
              </table>
              <p v-else style="color: #888;">标准商品包装与官方出厂配置。</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ShoppingCart } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';
import { applySeo, resolveImageUrl } from '@/composables/useSeo';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const submitting = ref(false);
const activeTab = ref('detail');
const quantity = ref(1);
const selectedSkuId = ref<number | null>(null);
const currentImg = ref('');
const product = ref<any>(null);

const galleryList = computed(() => {
  if (!product.value) return [];
  if (Array.isArray(product.value.picList) && product.value.picList.length > 0) {
    return product.value.picList.map((p: any) => p.picUrl || p.picThumb).filter(Boolean);
  }
  if (product.value.item?.picUrl) {
    return [product.value.item.picUrl];
  }
  return [];
});

const activePrice = computed(() => {
  if (!product.value?.item) return 0;
  if (selectedSkuId.value && Array.isArray(product.value.skuList)) {
    const sku = product.value.skuList.find((s: any) => s.skuId === selectedSkuId.value);
    if (sku && sku.skuPrice) return Number(sku.skuPrice);
  }
  return Number(product.value.item.productPrice || 0);
});

const activeStock = computed(() => {
  if (!product.value?.item) return 0;
  if (selectedSkuId.value && Array.isArray(product.value.skuList)) {
    const sku = product.value.skuList.find((s: any) => s.skuId === selectedSkuId.value);
    if (sku && sku.skuStock !== undefined) return Number(sku.skuStock);
  }
  return Number(product.value.item.productStock || 0);
});

async function fetchProductDetail() {
  const id = route.params.id || route.query.id;
  if (!id) {
    loading.value = false;
    return;
  }

  loading.value = true;
  try {
    const res: any = await request({
      url: '/product/product/detail',
      method: 'get',
      params: { id }
    });

    if (res) {
      product.value = res;
      if (Array.isArray(res.skuList) && res.skuList.length > 0) {
        selectedSkuId.value = res.skuList[0].skuId;
      }
      if (Array.isArray(res.picList) && res.picList.length > 0) {
        currentImg.value = res.picList[0].picUrl || res.picList[0].picThumb || '';
      } else if (res.item?.picUrl) {
        currentImg.value = res.item.picUrl;
      }

      // SEO：商品详情是最需要被索引的页面，标题/描述/主图都取自真实数据
      applySeo({
        title: res.item?.productName || '商品详情',
        description: buildProductDescription(res),
        keywords: res.item?.keywords || res.item?.productName || '',
        image: currentImg.value
          ? resolveImageUrl(currentImg.value, '/static')
          : undefined,
        type: 'product',
      });
    } else {
      product.value = null;
    }
  } catch (err: any) {
    console.error('获取商品详情失败:', err);
    product.value = null;
  } finally {
    loading.value = false;
  }
}

/**
 * 生成商品描述（SEO meta description）。
 *
 * 优先用商品副标题/简介，缺失时用「名称 + 价格 + 商城名」兜底，
 * 并截断到 SEO 建议的 160 字符以内。
 */
function buildProductDescription(res: any): string {
  const name = res.item?.productName || '';
  const brief = res.item?.productBrief || res.item?.productDesc || '';
  const price = res.item?.productPrice;
  let desc = brief || `${name} - 正品保障，价格 ¥${price ?? '--'}`;
  if (desc.length > 160) desc = desc.slice(0, 157) + '...';
  return desc;
}

async function handleAddToCart() {
  const id = Number(route.params.id || route.query.id);
  if (!id) return;

  submitting.value = true;
  try {
    await request({
      url: '/cart/cart/addToCart',
      method: 'post',
      data: {
        productId: id,
        skuId: selectedSkuId.value || undefined,
        quantity: quantity.value
      }
    });
    ElMessage.success('已成功加入购物车！');
  } catch (err: any) {
    // 拦截器已弹出错误提示或引导去登录
  } finally {
    submitting.value = false;
  }
}

async function handleBuyNow() {
  const id = Number(route.params.id || route.query.id);
  if (!id) return;

  submitting.value = true;
  try {
    await request({
      url: '/cart/cart/addToCart',
      method: 'post',
      data: {
        productId: id,
        skuId: selectedSkuId.value || undefined,
        quantity: quantity.value
      }
    });
    router.push('/cart');
  } catch (err: any) {
    // 拦截器处理
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  fetchProductDetail();
});
</script>

<style scoped lang="less">
.tig-product-detail-page {
  padding: 20px 0 50px;
}

.product-crumb {
  font-size: 13px;
  color: #999;
  margin-bottom: 20px;
  a { color: #666; &:hover { color: var(--tig-red); } }
  span { color: #333; }
}

.product-main-panel {
  background: #ffffff;
  padding: 30px;
  border-radius: 8px;
  border: 1px solid var(--tig-border-color);
  margin-bottom: 30px;

  .gallery-col {
    width: 440px;

    .main-img-box {
      width: 440px;
      height: 440px;
      border: 1px solid #eee;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16px;
      overflow: hidden;

      img {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
      }
    }

    .thumb-list {
      gap: 12px;

      .thumb-item {
        width: 68px;
        height: 68px;
        border: 2px solid #eee;
        border-radius: 4px;
        cursor: pointer;
        padding: 2px;
        transition: all 0.2s;

        img { width: 100%; height: 100%; object-fit: cover; }

        &:hover, &.active {
          border-color: var(--tig-red);
        }
      }
    }
  }

  .buy-col {
    flex: 1;
    margin-left: 40px;

    .p-title {
      font-size: 22px;
      font-weight: 700;
      color: #222;
      line-height: 1.4;
      margin-bottom: 8px;
    }

    .p-subtitle {
      font-size: 14px;
      color: #999;
      margin-bottom: 20px;
    }

    .price-banner {
      background: #fdf5f5;
      padding: 16px 20px;
      border-radius: 6px;
      margin-bottom: 24px;

      .label {
        font-size: 13px;
        color: #888;
        margin-right: 14px;
      }

      .price-text {
        color: var(--tig-red);
        font-weight: bold;
        .symbol { font-size: 16px; }
        .integer { font-size: 32px; }
        .decimal { font-size: 18px; }
      }

      .market-price {
        font-size: 13px;
        color: #999;
        text-decoration: line-through;
        margin-left: 16px;
      }

      .coupon-tips {
        margin-top: 12px;
        padding-top: 10px;
        border-top: 1px dashed rgba(225, 37, 27, 0.2);

        .coupon-tag {
          background: #fff;
          color: var(--tig-red);
          border: 1px solid var(--tig-red);
          font-size: 12px;
          padding: 2px 8px;
          border-radius: 2px;
          margin-right: 8px;
        }

        .tip-text {
          font-size: 12px;
          color: #777;
        }
      }
    }

    .spec-section {
      margin-bottom: 30px;

      .spec-row {
        margin-bottom: 18px;

        .spec-label {
          width: 70px;
          font-size: 13px;
          color: #777;
          flex-shrink: 0;
        }

        .spec-options {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;

          .spec-btn {
            border: 1px solid #ddd;
            padding: 6px 16px;
            font-size: 13px;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.2s;

            &:hover { border-color: var(--tig-red); color: var(--tig-red); }
            &.active {
              border-color: var(--tig-red);
              color: var(--tig-red);
              background: #fff6f6;
              font-weight: bold;
            }
          }
        }

        .stock-text {
          font-size: 13px;
          color: #999;
          margin-left: 16px;
        }
      }
    }

    .action-buttons {
      gap: 16px;

      button {
        height: 48px;
        padding: 0 36px;
        font-size: 16px;
        font-weight: bold;
        border-radius: 4px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        transition: all 0.2s;

        &:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }
      }

      .btn-buy-now {
        background: #ffeded;
        color: var(--tig-red);
        border: 1px solid var(--tig-red);
        &:hover:not(:disabled) { background: #ffe0e0; }
      }

      .btn-add-cart {
        background: var(--tig-red);
        color: #fff;
        border: none;
        &:hover:not(:disabled) { background: #c81c13; }
      }
    }
  }
}

.product-intro-tabs {
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid var(--tig-border-color);
  padding: 20px 30px;

  .rich-content {
    padding: 20px 0;
    min-height: 200px;
    font-size: 14px;
    line-height: 1.8;
    color: #444;
  }

  .spec-table {
    width: 100%;
    border-collapse: collapse;
    td {
      border: 1px solid #eee;
    }
  }
}
</style>
