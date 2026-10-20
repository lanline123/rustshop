<template>
  <div class="tig-pc-home-page">
    <!-- Loading Skeleton -->
    <div v-if="loading" class="container loading-box">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- Empty State -->
    <div v-else-if="!loading && homeVersion === 'new' && modules.length === 0" class="container empty-box">
      <el-empty description="商城首页暂无装修数据，请先在管理后台完成装修发布" />
    </div>

    <!-- ==================== 1. PC 新版全景全宽模式 (New Suite) ==================== -->
    <template v-else-if="homeVersion === 'new'">
      <!-- Dynamic Modules Renderer for New PC Home -->
      <div v-for="(module, idx) in modules" :key="idx" class="dynamic-module-wrapper">
        <!-- New Fullscreen Banner -->
        <section v-if="module.type === 'pcNewBanner' || module.type === 'banner'" class="pc-hero-banner-section">
          <div class="banner-inner container" :style="{ height: `${module.params?.height || 420}px` }">
            <div class="hero-content">
              <span class="tag">旗舰精选</span>
              <h1 class="hero-title">{{ module.params?.title || '全景数字生活馆' }}</h1>
              <p class="hero-desc">{{ module.params?.subTitle || '官方正品保证 · 极速物流履约' }}</p>
              <router-link to="/search" class="hero-btn">立即选购 &gt;</router-link>
            </div>
          </div>
        </section>

        <!-- New Magic Cube Ads -->
        <section v-else-if="module.type === 'pcNewCube' || module.type === 'cube'" class="pc-cube-section container">
          <div class="cube-grid">
            <div class="cube-item cube-large" @click="$router.push('/search')">
              <div class="cube-text">
                <h3>超值爆款</h3>
                <p>每日特惠直降</p>
                <span class="action-link">立即抢购</span>
              </div>
            </div>
            <div class="cube-item cube-sub1" @click="$router.push('/search')">
              <div class="cube-text">
                <h3>新品推荐</h3>
                <p>本季上新专区</p>
              </div>
            </div>
            <div class="cube-item cube-sub2" @click="$router.push('/search')">
              <div class="cube-text">
                <h3>企业采购</h3>
                <p>批量采购专享</p>
              </div>
            </div>
          </div>
        </section>

        <!-- New Floor Goods Matrix -->
        <section v-else-if="module.type === 'pcNewFloor' || module.type === 'floor'" class="pc-floor-section container">
          <div class="floor-header flex-between" :style="{ borderBottomColor: module.params?.bgColor || '#e1251b' }">
            <div class="floor-title-box flex-align-center">
              <span class="floor-mark" :style="{ backgroundColor: module.params?.bgColor || '#e1251b' }"></span>
              <h2 class="floor-title" :style="{ color: module.params?.bgColor || '#e1251b' }">
                {{ module.params?.title || '热门精选好物' }}
              </h2>
            </div>
            <div class="floor-sub-nav">
              <router-link to="/search">全部好物 &gt;</router-link>
            </div>
          </div>

          <div v-if="floorProducts.length > 0" class="goods-matrix">
            <div
              v-for="product in floorProducts"
              :key="product.productId"
              class="goods-matrix-card"
              @click="$router.push(`/product/${product.productId}`)"
            >
              <div class="card-img-box">
                <img :src="product.picThumb || product.picUrl || '/default-goods.png'" />
              </div>
              <div class="card-info">
                <div class="card-name">{{ product.productName }}</div>
                <div class="card-price-row flex-between">
                  <div class="price-text">
                    <span class="symbol">¥</span>
                    <span class="integer">{{ Math.floor(product.productPrice || 0) }}</span>
                    <span class="decimal">.{{ ((product.productPrice || 0) % 1 * 100).toFixed(0).padStart(2, '0') }}</span>
                  </div>
                  <span class="sales-count">已售 {{ product.sales || 0 }} 件</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="本楼层暂无在售商品" :image-size="80" />
        </section>
      </div>
    </template>

    <!-- ==================== 2. PC 老版经典三栏模式 (Old Suite) ==================== -->
    <template v-else>
      <div class="classic-home-wrapper container">
        <!-- Top 3 Columns Banner Section -->
        <div class="classic-three-cols flex-between">
          <!-- Left: Dynamic Categories Menu -->
          <div class="classic-category-menu">
            <div
              v-for="cat in categoryTree.slice(0, 8)"
              :key="cat.categoryId"
              class="cat-menu-item flex-between"
              @click="$router.push(`/search?cat=${cat.categoryId}`)"
            >
              <span class="cat-name">{{ cat.categoryName }}</span>
              <span class="cat-sub">{{ cat.subCategoryList?.[0]?.categoryName || '精选' }}</span>
            </div>
            <div v-if="categoryTree.length === 0" style="padding: 20px; color: #999; text-align: center;">
              正在加载分类...
            </div>
          </div>

          <!-- Center: Main Banner Area -->
          <div class="classic-main-carousel">
            <el-carousel height="440px" :interval="4000">
              <el-carousel-item v-for="(b, i) in bannerList" :key="i">
                <div class="banner-slide" :style="{ background: b.bg || 'linear-gradient(135deg, #1456f0, #092b7a)' }">
                  <h2>{{ b.title || '品质甄选 官方商城' }}</h2>
                  <p>{{ b.sub || '全场正品保障 · 支持七天无理由退货' }}</p>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>

          <!-- Right: User & News Card -->
          <div class="classic-user-card">
            <div class="user-greeting">
              <div class="avatar-ph">Hi</div>
              <p>欢迎来到 TigShop 商城</p>
            </div>
            <div class="quick-notice-box">
              <div class="notice-header">商城快讯</div>
              <ul>
                <li><span>[特惠]</span> 官方甄选品质商品火热销售中</li>
                <li><span>[服务]</span> 全网支持在线下单与全国配送</li>
                <li><span>[保障]</span> 严格品质检验 售后贴心保障</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Classic Floor -->
        <div class="classic-floor-box">
          <div class="floor-title-row flex-between">
            <h3 class="c-floor-title">热门爆款精选</h3>
            <router-link to="/search" class="more-link">查看全部商品 &gt;</router-link>
          </div>
          <div v-if="floorProducts.length > 0" class="classic-floor-grid">
            <div
              v-for="p in floorProducts"
              :key="p.productId"
              class="goods-matrix-card"
              @click="$router.push(`/product/${p.productId}`)"
            >
              <div class="card-img-box">
                <img :src="p.picThumb || p.picUrl || '/default-goods.png'" />
              </div>
              <div class="card-info">
                <div class="card-name">{{ p.productName }}</div>
                <div class="card-price-row flex-between">
                  <span class="price-text">¥{{ p.productPrice }}</span>
                  <span class="sales-count">已售 {{ p.sales }}</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无推荐商品" :image-size="100" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import request from '@/utils/request';

const loading = ref(true);
const homeVersion = ref<'new' | 'old'>('new');
const modules = ref<any[]>([]);
const floorProducts = ref<any[]>([]);
const categoryTree = ref<any[]>([]);
const bannerList = ref<any[]>([
  { title: '正品行货 极速到家', sub: '全场大牌 极速发货', bg: 'linear-gradient(135deg, #1456f0, #092b7a)' },
  { title: '新品首发 独家礼赠', sub: '品质保障 售后无忧', bg: 'linear-gradient(135deg, #e1251b, #8a0c06)' }
]);

// 1. 加载首页动态装修配置
async function loadHomeDecorate() {
  try {
    // 正确入口是 `GET /api/home/home/pcIndex`（返回 DecorateModuleListVO）。
    // 原实现误用 `/decorate/decorate/detail`——那是 admin 端接口（需 admin JWT），前台必 404。
    const res = await request<{
      moduleList?: any[];
      pageModule?: Record<string, any>;
    }>({
      url: '/home/home/pcIndex',
      method: 'get'
    });

    if (!res) return;

    // 版本口径与原版 isNewHome.ts 一致：优先 pageModule.homeStyleVersion
    const styleVersion = res.pageModule?.homeStyleVersion;
    if (styleVersion === 'old' || styleVersion === 'new') {
      homeVersion.value = styleVersion;
    }

    // moduleList 已由后端过滤掉 isShow=false 的模块
    if (Array.isArray(res.moduleList)) {
      modules.value = res.moduleList;
    }
  } catch (err) {
    console.error('获取装修数据失败:', err);
    modules.value = [];
  }
}

// 2. 加载热门真实商品列表
async function loadRealProducts() {
  try {
    const res: any = await request({
      url: '/product/product/list',
      method: 'get',
      params: {
        page: 1,
        size: 10,
        sort_by: 'sales',
        sort_order: 'desc'
      }
    });
    if (res && Array.isArray(res.list)) {
      floorProducts.value = res.list;
    }
  } catch (err) {
    console.error('获取商品列表失败:', err);
    floorProducts.value = [];
  }
}

// 3. 加载分类树
async function loadCategories() {
  try {
    const res: any = await request({
      url: '/category/category/all',
      method: 'get'
    });
    if (Array.isArray(res)) {
      categoryTree.value = res;
    }
  } catch (err) {
    console.error('获取分类树失败:', err);
  }
}

onMounted(async () => {
  loading.value = true;
  await Promise.allSettled([
    loadHomeDecorate(),
    loadRealProducts(),
    loadCategories()
  ]);
  loading.value = false;
});
</script>

<style scoped lang="less">
.tig-pc-home-page {
  padding-bottom: 40px;
}

.loading-box, .empty-box {
  padding: 60px 0;
}

/* ================== PC 新版样式 ================== */
.pc-hero-banner-section {
  background: linear-gradient(180deg, #1f2329 0%, #2f3238 100%);
  margin-bottom: 24px;

  .banner-inner {
    display: flex;
    align-items: center;
    color: #fff;
    padding: 0 40px;

    .hero-content {
      .tag {
        background: rgba(255, 255, 255, 0.2);
        padding: 4px 14px;
        border-radius: 14px;
        font-size: 13px;
        letter-spacing: 1px;
      }
      .hero-title {
        font-size: 42px;
        margin: 16px 0 10px;
        font-weight: 700;
        letter-spacing: 2px;
      }
      .hero-desc {
        font-size: 16px;
        color: #ddd;
        margin-bottom: 24px;
      }
      .hero-btn {
        display: inline-block;
        background: var(--tig-red);
        color: #fff;
        padding: 10px 28px;
        border-radius: 24px;
        font-size: 15px;
        font-weight: bold;
        transition: all 0.3s;
        &:hover {
          background: #c81c13;
          transform: translateY(-2px);
        }
      }
    }
  }
}

.pc-cube-section {
  margin-bottom: 30px;
  .cube-grid {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr;
    gap: 16px;
    height: 220px;

    .cube-item {
      background: #f7f8fa;
      border-radius: 8px;
      padding: 24px;
      position: relative;
      cursor: pointer;
      overflow: hidden;
      transition: all 0.3s;
      border: 1px solid transparent;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 20px rgba(0,0,0,0.06);
        border-color: #eee;
      }

      h3 { font-size: 20px; font-weight: 700; color: #222; margin-bottom: 6px; }
      p { font-size: 13px; color: #888; }
      .action-link {
        display: inline-block;
        margin-top: 14px;
        color: var(--tig-red);
        font-weight: 600;
        font-size: 13px;
      }
    }

    .cube-large { background: linear-gradient(135deg, #fff1f0 0%, #fff7f7 100%); }
    .cube-sub1 { background: linear-gradient(135deg, #e6f7ff 0%, #f0faff 100%); }
    .cube-sub2 { background: linear-gradient(135deg, #f6ffed 0%, #fcffe6 100%); }
  }
}

.pc-floor-section {
  margin-bottom: 36px;
  .floor-header {
    border-bottom: 2px solid;
    padding-bottom: 12px;
    margin-bottom: 16px;

    .floor-mark {
      width: 4px;
      height: 20px;
      border-radius: 2px;
      margin-right: 10px;
    }

    .floor-title {
      font-size: 20px;
      font-weight: 700;
      margin: 0;
    }

    .floor-sub-nav {
      a {
        color: #666;
        font-size: 13px;
        margin-left: 20px;
        &:hover { color: var(--tig-red); }
      }
    }
  }

  .goods-matrix {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
  }
}

.goods-matrix-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: var(--tig-red);
    box-shadow: 0 6px 18px rgba(0,0,0,0.06);
    transform: translateY(-3px);
  }

  .card-img-box {
    width: 100%;
    height: 210px;
    background: #fcfcfc;
    display: flex;
    align-items: center;
    justify-content: center;
    img {
      max-width: 100%;
      max-height: 100%;
      object-fit: cover;
    }
  }

  .card-info {
    padding: 12px 14px 16px;

    .card-name {
      font-size: 13px;
      color: #333;
      line-height: 1.5;
      height: 38px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      margin-bottom: 10px;
    }

    .card-price-row {
      .price-text {
        color: var(--tig-red);
        font-weight: bold;
        .symbol { font-size: 13px; }
        .integer { font-size: 18px; }
        .decimal { font-size: 12px; }
      }
      .sales-count {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

/* ================== PC 老版样式 ================== */
.classic-three-cols {
  height: 440px;
  margin-bottom: 30px;

  .classic-category-menu {
    width: 220px;
    background: #2b303b;
    border-radius: 6px;
    padding: 10px 0;

    .cat-menu-item {
      padding: 10px 20px;
      cursor: pointer;
      color: #eee;
      font-size: 14px;
      transition: all 0.2s;

      .cat-sub {
        font-size: 12px;
        color: #888;
      }

      &:hover {
        background: var(--tig-red);
        color: #fff;
        .cat-sub { color: #ffe5e5; }
      }
    }
  }

  .classic-main-carousel {
    flex: 1;
    margin: 0 16px;
    border-radius: 6px;
    overflow: hidden;

    .banner-slide {
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: center;
      padding: 0 60px;
      color: #fff;

      h2 { font-size: 36px; font-weight: 700; margin-bottom: 12px; }
      p { font-size: 16px; opacity: 0.9; }
    }
  }

  .classic-user-card {
    width: 240px;
    background: #fff;
    border: 1px solid var(--tig-border-color);
    border-radius: 6px;
    padding: 20px;

    .user-greeting {
      text-align: center;
      border-bottom: 1px solid #f0f0f0;
      padding-bottom: 16px;

      .avatar-ph {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        background: var(--tig-red);
        color: #fff;
        line-height: 50px;
        font-size: 20px;
        margin: 0 auto 10px;
      }

      p { font-size: 13px; color: #555; }
    }

    .quick-notice-box {
      margin-top: 16px;

      .notice-header {
        font-size: 13px;
        font-weight: bold;
        color: #333;
        margin-bottom: 10px;
      }

      ul {
        list-style: none;
        padding: 0;
        margin: 0;

        li {
          font-size: 12px;
          color: #666;
          line-height: 22px;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;

          span { color: var(--tig-red); font-weight: 600; margin-right: 4px; }
        }
      }
    }
  }
}

.classic-floor-box {
  margin-bottom: 30px;

  .floor-title-row {
    border-bottom: 2px solid #333;
    padding-bottom: 8px;
    margin-bottom: 16px;

    .c-floor-title {
      font-size: 20px;
      font-weight: 700;
      color: #222;
    }

    .more-link {
      font-size: 13px;
      color: #666;
      &:hover { color: var(--tig-red); }
    }
  }

  .classic-floor-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
  }
}
</style>
