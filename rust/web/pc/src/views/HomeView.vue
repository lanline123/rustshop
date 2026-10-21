<template>
  <div class="tig-pc-home-page">
    <!-- 首屏骨架 -->
    <div v-if="loading" class="container loading-box">
      <el-skeleton :rows="10" animated />
    </div>

    <template v-else>
      <!-- 分类导航 + 首屏装修模块 -->
      <div class="container home-top">
        <aside class="home-side-categories">
          <div class="side-title">商品分类</div>
          <ul>
            <li
              v-for="node in categoryTree.slice(0, 10)"
              :key="node.categoryId"
              @click="goCategory(node.categoryId)"
            >
              <span class="cat-name">{{ node.categoryName }}</span>
              <span class="cat-arrow">›</span>
            </li>
            <li v-if="categoryTree.length === 0" class="cat-empty">暂无分类</li>
          </ul>
        </aside>

        <div class="home-banner-area">
          <template v-if="heroModule">
            <div
              class="hero-card"
              :style="heroModule.image ? { backgroundImage: `url(${heroModule.image})` } : {}"
              @click="goLink(heroModule.link)"
            >
              <div class="hero-mask">
                <h1>{{ heroModule.title || 'TigShop 官方旗舰商城' }}</h1>
                <p>{{ heroModule.subtitle || '官方正品 · 极速物流 · 售后无忧' }}</p>
                <span class="hero-btn">立即选购</span>
              </div>
            </div>
          </template>
          <div v-else class="hero-fallback">
            <h1>TigShop 官方旗舰商城</h1>
            <p>当前首页尚未配置装修首屏，以下为实时热销商品</p>
            <router-link to="/search" class="hero-btn">去商品大厅</router-link>
          </div>

          <!-- 图片广告模块：pc-image-ad / pcImageAd / pcBanner 等 -->
          <div v-if="adImages.length" class="home-ad-row">
            <div
              v-for="(ad, idx) in adImages"
              :key="idx"
              class="ad-card"
              @click="goLink(ad.link)"
            >
              <img :src="imageUrl(ad.image)" :alt="ad.title || '广告'" />
            </div>
          </div>
        </div>
      </div>

      <!-- 装修商品模块（pc-product / pc-recommend-product / pc-product-group 等） -->
      <section v-for="(section, idx) in productSections" :key="`ps-${idx}`" class="container home-section">
        <div class="section-header flex-between">
          <div class="section-title-box flex-align-center">
            <span class="section-mark"></span>
            <h2>{{ section.title || '精选好物' }}</h2>
          </div>
          <router-link to="/search" class="section-more">更多商品 ›</router-link>
        </div>
        <div class="goods-grid">
          <div
            v-for="product in section.products"
            :key="product.productId"
            class="goods-card"
            @click="goProduct(product.productId)"
          >
            <div class="card-img-box">
              <img :src="imageUrl(product.picThumb || product.picUrl)" :alt="product.productName" />
            </div>
            <div class="card-info">
              <div class="card-name">{{ product.productName }}</div>
              <div class="card-bottom flex-between">
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
        <el-empty v-if="section.products.length === 0" description="该楼层暂无在售商品" :image-size="80" />
      </section>

      <!-- 实时热销（真实商品接口，装修数据缺失时的兜底内容） -->
      <section class="container home-section">
        <div class="section-header flex-between">
          <div class="section-title-box flex-align-center">
            <span class="section-mark"></span>
            <h2>实时热销</h2>
          </div>
          <router-link to="/search" class="section-more">更多商品 ›</router-link>
        </div>
        <div v-if="hotProducts.length" class="goods-grid">
          <div
            v-for="product in hotProducts"
            :key="product.productId"
            class="goods-card"
            @click="goProduct(product.productId)"
          >
            <div class="card-img-box">
              <img :src="imageUrl(product.picThumb || product.picUrl)" :alt="product.productName" />
            </div>
            <div class="card-info">
              <div class="card-name">{{ product.productName }}</div>
              <div class="card-bottom flex-between">
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
        <el-empty v-else description="暂无在售商品" :image-size="100" />
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { homeApi, productApi } from '@/api';
import { pageItems } from '@/api/types';
import type { CategoryNode, DecorateIndex, DecorateModule, ProductVO } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { imageUrl, priceDecimal, priceInteger } from '@/utils/format';

const router = useRouter();

const loading = ref(true);
const categoryTree = ref<CategoryNode[]>([]);
const decorate = ref<DecorateIndex | null>(null);
const hotProducts = ref<ProductVO[]>([]);
const moduleProducts = ref<Record<number, ProductVO[]>>({});

interface HeroInfo {
  image?: string;
  title?: string;
  subtitle?: string;
  link?: string;
}
interface AdInfo {
  image: string;
  link?: string;
  title?: string;
}

/** 支持的图片/广告类模块（新旧两套命名，逐字对齐原版 registry）。 */
const IMAGE_MODULE_TYPES = new Set([
  'pc-image-ad',
  'pcImageAd',
  'pcBanner',
  'banner',
  'pc-image-square-ad',
  'pc-image-nav',
  'pcCustomAd1',
  'pcCustomAd2',
  'pc-home-hero',
]);

/** 商品类模块（旧版 camelCase + 新版 kebab）。 */
const PRODUCT_MODULE_TYPES = new Set([
  'pc-product',
  'pc-recommend-product',
  'pc-product-group',
  'pcProductGroup',
  'pcRecommendProduct',
  'pcSeckill',
  'pcGroupon',
  'pcCatProductNormal',
  'pcCatProductWide',
  'pcCatProductSimple',
  'pc-seckill',
  'pc-home-seckill',
  'pc-home-groupon',
]);

const modules = computed<DecorateModule[]>(() => {
  const list = decorate.value?.moduleList;
  return Array.isArray(list) ? list.filter((m) => m && m.isShow !== false) : [];
});

/** 递归从装修模块里找出所有图片/链接（覆盖后端松散 JSON 的多种字段命名）。 */
function collectImages(value: any, out: AdInfo[], depth = 0): void {
  if (!value || depth > 4 || out.length >= 8) return;
  if (Array.isArray(value)) {
    for (const item of value) collectImages(item, out, depth + 1);
    return;
  }
  if (typeof value !== 'object') return;

  const pick = (keys: string[]): string | undefined => {
    for (const key of keys) {
      const v = value[key];
      if (typeof v === 'string' && v.trim()) return v.trim();
    }
    return undefined;
  };
  const image = pick(['picUrl', 'image', 'imgUrl', 'pic', 'picture', 'imageUrl', 'picThumb', 'cover']);
  if (image) {
    out.push({
      image,
      link: pick(['link', 'url', 'href', 'jumpUrl', 'to']),
      title: pick(['title', 'name', 'text', 'adName']),
    });
    return;
  }
  for (const key of Object.keys(value)) {
    const child = value[key];
    if (child && typeof child === 'object') collectImages(child, out, depth + 1);
  }
}

/** 递归找出模块里形如商品对象的数组。 */
function collectProducts(value: any, out: ProductVO[], depth = 0): void {
  if (!value || depth > 4 || out.length >= 20) return;
  if (Array.isArray(value)) {
    const items = value.filter((it) => it && typeof it === 'object' && 'productId' in it && 'productName' in it) as ProductVO[];
    if (items.length) {
      out.push(...items);
      return;
    }
    for (const item of value) collectProducts(item, out, depth + 1);
    return;
  }
  if (typeof value !== 'object') return;
  if ('productId' in value && 'productName' in value) {
    out.push(value as ProductVO);
    return;
  }
  for (const key of Object.keys(value)) {
    const child = value[key];
    if (child && typeof child === 'object') collectProducts(child, out, depth + 1);
  }
}

function moduleData(m: DecorateModule): any {
  return m.module ?? m.data ?? m;
}

/** 首屏视觉模块（第一条含图片的图片类模块）。 */
const heroModule = computed<HeroInfo | null>(() => {
  for (const m of modules.value) {
    if (!IMAGE_MODULE_TYPES.has(String(m.type))) continue;
    const data = moduleData(m);
    const ads: AdInfo[] = [];
    collectImages(data, ads);
    const first = ads[0];
    if (!first) continue;
    const title = data?.title ?? data?.name ?? m.style?.title;
    const subtitle = data?.subTitle ?? data?.subtitle ?? data?.desc;
    const link = first.link ?? data?.link ?? data?.url;
    return {
      image: first.image,
      title: typeof title === 'string' ? title : undefined,
      subtitle: typeof subtitle === 'string' ? subtitle : undefined,
      link: typeof link === 'string' ? link : undefined,
    };
  }
  return null;
});

/** 首屏之外的图片广告位（取图片类模块中的图片，最多 4 张）。 */
const adImages = computed<AdInfo[]>(() => {
  const out: AdInfo[] = [];
  for (const m of modules.value) {
    if (!IMAGE_MODULE_TYPES.has(String(m.type))) continue;
    collectImages(moduleData(m), out);
  }
  // 去掉首屏 hero 已使用的那张
  return out.slice(heroModule.value?.image ? 1 : 0, heroModule.value?.image ? 5 : 4);
});

/** 装修商品楼层：优先使用模块自带商品，缺省时回退到已加载的热销商品。 */
const productSections = computed<Array<{ title?: string; products: ProductVO[] }>>(() => {
  const result: Array<{ title?: string; products: ProductVO[] }> = [];
  modules.value.forEach((m, index) => {
    if (!PRODUCT_MODULE_TYPES.has(String(m.type))) return;
    const data = moduleData(m);
    const fromModule = moduleProducts.value[index] ?? [];
    const inline: ProductVO[] = [];
    collectProducts(data, inline);
    const products = fromModule.length ? fromModule : inline;
    result.push({
      title: typeof data?.title === 'string' ? data.title : typeof data?.name === 'string' ? data.name : undefined,
      products: products.length ? products : hotProducts.value.slice(0, 10),
    });
  });
  return result;
});

function goLink(link?: string) {
  if (!link) return;
  if (/^https?:\/\//i.test(link)) {
    window.open(link, '_blank', 'noopener');
    return;
  }
  router.push(link.startsWith('/') ? link : `/${link}`);
}

function goCategory(categoryId: number) {
  router.push({ path: '/search', query: { categoryId: String(categoryId) } });
}

function goProduct(productId: number) {
  router.push(`/product/${productId}`);
}

async function loadCategories() {
  try {
    categoryTree.value = await homeApi.categoryTree();
  } catch {
    categoryTree.value = [];
  }
}

async function loadHotProducts() {
  try {
    const page = await productApi.list({ sortBy: 'sales', sortOrder: 'desc', page: 1, pageSize: 10 });
    hotProducts.value = pageItems(page);
  } catch {
    hotProducts.value = [];
  }
}

async function loadDecorate() {
  try {
    decorate.value = await homeApi.pcIndex();
    const pageModule = decorate.value?.pageModule;
    const seo = pageModule?.seo;
    applySeo({
      title: typeof pageModule?.pageTitle === 'string' ? pageModule.pageTitle : undefined,
      description: typeof seo?.description === 'string' ? seo.description : undefined,
      keywords: typeof seo?.keywords === 'string' ? seo.keywords : undefined,
      image: typeof pageModule?.shareImage === 'string' ? imageUrl(pageModule.shareImage) : undefined,
    });
  } catch {
    decorate.value = null;
    applySeo({});
  }
}

/** 商品类模块若未自带商品，按模块类型从真实商品接口补数据。 */
async function fillModuleProducts() {
  if (!modules.value.length) return;
  const entries = await Promise.all(
    modules.value.map(async (m, index) => {
      if (!PRODUCT_MODULE_TYPES.has(String(m.type))) return null;
      const inline: ProductVO[] = [];
      collectProducts(moduleData(m), inline);
      if (inline.length) return null;
      try {
        const page = await productApi.list({ sortBy: 'sales', sortOrder: 'desc', page: 1, pageSize: 10 });
        return [index, pageItems(page)] as const;
      } catch {
        return null;
      }
    }),
  );
  for (const entry of entries) {
    if (entry) moduleProducts.value[entry[0]] = entry[1];
  }
}

onMounted(async () => {
  loading.value = true;
  try {
    await Promise.all([loadCategories(), loadHotProducts(), loadDecorate()]);
    await fillModuleProducts();
  } finally {
    loading.value = false;
  }
});

</script>

<style scoped lang="less">
.tig-pc-home-page {
  padding-bottom: 40px;
}

.loading-box {
  padding: 60px 0;
}

.home-top {
  display: flex;
  gap: 16px;
  padding: 16px 0;
}

.home-side-categories {
  width: 220px;
  flex: 0 0 220px;
  background: #ffffff;
  border: 1px solid var(--tig-border-color);
  border-radius: 6px;
  overflow: hidden;

  .side-title {
    height: 40px;
    line-height: 40px;
    padding: 0 16px;
    font-weight: 700;
    background: #fafafa;
    border-bottom: 1px solid #f0f0f0;
  }

  ul {
    list-style: none;
    padding: 6px 0;

    li {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 16px;
      font-size: 13px;
      color: var(--tig-text-regular);
      cursor: pointer;
      &:hover {
        color: var(--tig-red);
        background: #fff5f4;
      }
      .cat-arrow { color: #ccc; }
      &.cat-empty { color: #999; cursor: default; }
    }
  }
}

.home-banner-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  height: 300px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  overflow: hidden;
  cursor: pointer;
  position: relative;

  .hero-mask {
    position: absolute;
    inset: 0;
    background: linear-gradient(90deg, rgba(0, 0, 0, 0.55), rgba(0, 0, 0, 0.05));
    color: #fff;
    padding: 40px;
    display: flex;
    flex-direction: column;
    justify-content: center;

    h1 { font-size: 32px; margin-bottom: 10px; }
    p { font-size: 15px; color: #eee; margin-bottom: 18px; }
    .hero-btn {
      display: inline-block;
      width: fit-content;
      background: var(--tig-red);
      color: #fff;
      padding: 9px 26px;
      border-radius: 20px;
      font-weight: 600;
    }
  }
}

.hero-fallback {
  height: 300px;
  border-radius: 8px;
  background: linear-gradient(135deg, #1f2329, #3a3f47);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;

  h1 { font-size: 30px; }
  p { color: #ddd; }
  .hero-btn {
    background: var(--tig-red);
    color: #fff;
    padding: 9px 26px;
    border-radius: 20px;
    font-weight: 600;
  }
}

.home-ad-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;

  .ad-card {
    height: 110px;
    border-radius: 6px;
    overflow: hidden;
    cursor: pointer;
    background: #fff;
    img { width: 100%; height: 100%; object-fit: cover; }
  }
}

.home-section {
  margin-top: 26px;

  .section-header {
    margin-bottom: 14px;

    .section-title-box {
      gap: 10px;
      .section-mark {
        width: 6px;
        height: 22px;
        background: var(--tig-red);
        border-radius: 3px;
      }
      h2 { font-size: 22px; font-weight: 700; }
    }

    .section-more {
      color: #888;
      font-size: 13px;
      &:hover { color: var(--tig-red); }
    }
  }
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}

.goods-card {
  background: #ffffff;
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
    height: 180px;
    background: #f8f8f8;
    img { width: 100%; height: 100%; object-fit: cover; }
  }

  .card-info {
    padding: 10px 12px 14px;

    .card-name {
      font-size: 13px;
      color: var(--tig-text-main);
      line-height: 1.5;
      height: 39px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
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
      .sales-count { font-size: 12px; color: #999; }
    }
  }
}

@media (max-width: 1280px) {
  .goods-grid { grid-template-columns: repeat(4, 1fr); }
}
</style>
