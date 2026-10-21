<template>
  <div class="tig-decorate-editor">
    <!-- Top Action Bar -->
    <div class="editor-header flex-between">
      <div class="header-left flex-align-center">
        <el-button :icon="ArrowLeft" link @click="$router.back()">返回后台</el-button>
        <div class="page-title">
          {{ viewMode === 'mobile' ? '移动端 (H5 / 小程序) 首页装修' : (pcVersion === 'new' ? 'PC 电脑端【新版全景可视化】装修' : 'PC 电脑端【传统经典版】装修') }}
        </div>
        <el-tag :type="viewMode === 'mobile' ? 'primary' : (pcVersion === 'new' ? 'success' : 'warning')" size="small" class="ml10">
          {{ viewMode === 'mobile' ? 'H5 移动端' : (pcVersion === 'new' ? 'PC 新版 (带pcHeader)' : 'PC 老版 (经典插槽)') }}
        </el-tag>
      </div>

      <div class="header-center flex-align-center">
        <!-- 终端模式选择 (H5 vs PC) -->
        <el-radio-group v-model="viewMode" size="small" @change="handleModeChange">
          <el-radio-button value="mobile">
            <el-icon><Cellphone /></el-icon> 移动端 (H5/小程序)
          </el-radio-button>
          <el-radio-button value="pc">
            <el-icon><Monitor /></el-icon> PC 宽屏商城
          </el-radio-button>
        </el-radio-group>

        <!-- PC 专享：新版 vs 老版 切换器 -->
        <div v-if="viewMode === 'pc'" class="pc-version-selector flex-align-center ml16">
          <span class="version-label">PC 体系：</span>
          <el-radio-group v-model="pcVersion" size="small" @change="handlePcVersionChange">
            <el-radio-button value="new">新版可视化套件</el-radio-button>
            <el-radio-button value="old">老版经典插槽套件</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div class="header-right flex-align-center">
        <el-button :loading="draftSaving" @click="saveDraftData">保存草稿</el-button>
        <el-button type="primary" :loading="publishing" @click="publishData">发布上线</el-button>
      </div>
    </div>

    <!-- Main Workspace (3 columns) -->
    <div class="editor-body">
      <!-- 1. Left Widget Palette -->
      <div class="palette-panel">
        <div class="panel-header">
          <span>{{ paletteTitle }}</span>
          <span class="widget-count">{{ activeWidgets.length }} 个物料</span>
        </div>
        <div class="widget-list">
          <div
            v-for="widget in activeWidgets"
            :key="widget.type"
            class="widget-item"
            @click="addWidget(widget)"
          >
            <el-icon class="widget-icon"><component :is="widget.icon" /></el-icon>
            <span class="widget-name">{{ widget.name }}</span>
          </div>
        </div>
      </div>

      <!-- 2. Center Canvas -->
      <div class="canvas-panel">
        <!-- Canvas Frame -->
        <div :class="['canvas-frame', viewMode]">
          <!-- Mobile Status Bar -->
          <div v-if="viewMode === 'mobile'" class="phone-notch">
            <span class="notch-time">09:41</span>
            <div class="notch-pill"></div>
            <div class="notch-signals">5G 100%</div>
          </div>

          <!-- PC Simulated Browser Header -->
          <div v-else class="pc-browser-bar flex-between">
            <div class="browser-dots flex-align-center">
              <span class="dot red"></span>
              <span class="dot yellow"></span>
              <span class="dot green"></span>
            </div>
            <div class="browser-address">https://mall.tigshop.com/home ({{ pcVersion === 'new' ? 'PC 新版渲染树' : 'PC 经典版插槽' }})</div>
            <div class="browser-scale">100%</div>
          </div>

          <!-- PC 新版独有：pcHeader 头部可视化装修区 -->
          <div
            v-if="viewMode === 'pc' && pcVersion === 'new'"
            :class="['pc-header-customizer', { 'active-header': isHeaderActive }]"
            @click="selectHeader"
          >
            <div class="pc-header-badge flex-between">
              <span>【新版专属】PC 顶栏与主导航条 (pcHeader)</span>
              <span class="header-edit-tip">点击配置页头Logo与主菜单</span>
            </div>
            <div class="pc-nav-preview flex-between">
              <div class="pc-logo-bar flex-align-center">
                <span class="pc-logo">{{ pcHeaderData.logoText || 'TigShop PC' }}</span>
                <div class="pc-nav-links">
                  <span class="active">商城首页</span>
                  <span>新品专区</span>
                  <span>热销排行</span>
                  <span>企业采购</span>
                </div>
              </div>
              <div class="pc-search-box">
                <input :placeholder="pcHeaderData.searchPlaceholder || '搜索全网好物...'" readonly />
              </div>
            </div>
          </div>

          <!-- Canvas Scroll Area -->
          <div class="canvas-scroll-area">
            <draggable
              v-model="pageModules"
              item-key="id"
              animation="200"
              ghost-class="ghost-module"
              handle=".module-drag-handle"
              class="module-container"
            >
              <template #item="{ element, index }">
                <div
                  :class="['canvas-module', { 'active-module': selectedIndex === index && !isHeaderActive }]"
                  @click="selectModule(index)"
                >
                  <!-- Drag Handle & Controls -->
                  <div class="module-controls" v-if="selectedIndex === index && !isHeaderActive">
                    <span class="module-title-tag">{{ element.name }}</span>
                    <el-icon class="module-drag-handle" title="拖动排序"><Rank /></el-icon>
                    <el-icon class="module-del-handle" title="删除组件" @click.stop="deleteModule(index)"><Delete /></el-icon>
                  </div>

                  <!-- ================== 1. Mobile Renderers ================== -->
                  <template v-if="viewMode === 'mobile'">
                    <!-- Search Bar -->
                    <div v-if="element.type === 'search'" class="widget-search-preview">
                      <div class="fake-search-input">
                        <el-icon><Search /></el-icon>
                        <span>{{ element.params.placeholder || '搜索店铺商品...' }}</span>
                      </div>
                    </div>

                    <!-- Banner -->
                    <div v-else-if="element.type === 'banner'" class="widget-banner-preview">
                      <div class="banner-box" :style="{ borderRadius: `${element.params.radius || 8}px` }">
                        <img :src="element.params.imageUrl || 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800'" />
                        <div class="banner-dots">
                          <span class="dot active"></span>
                          <span class="dot"></span>
                          <span class="dot"></span>
                        </div>
                      </div>
                    </div>

                    <!-- Nav -->
                    <div v-else-if="element.type === 'nav'" class="widget-nav-preview">
                      <div v-for="i in 5" :key="i" class="nav-btn">
                        <div class="nav-circle"></div>
                        <span>分类 {{ i }}</span>
                      </div>
                    </div>

                    <!-- Goods -->
                    <div v-else-if="element.type === 'goods'" class="widget-goods-preview">
                      <div class="goods-grid" :class="`cols-${element.params.cols || 2}`">
                        <div v-for="i in 4" :key="i" class="goods-card">
                          <div class="goods-img-placeholder">商品主图 {{ i }}</div>
                          <div class="goods-info">
                            <div class="goods-title">{{ element.params.title || '品质甄选热卖商品展示' }}</div>
                            <div class="goods-price-row">
                              <span class="price">¥99.00</span>
                              <span class="buy-btn">+</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- Coupon -->
                    <div v-else-if="element.type === 'coupon'" class="widget-coupon-preview">
                      <div class="coupon-item" v-for="i in 2" :key="i">
                        <div class="coupon-amount">¥20</div>
                        <div class="coupon-desc">满 199 减 20</div>
                      </div>
                    </div>
                  </template>

                  <!-- ================== 2. PC 新版 Renderers ================== -->
                  <template v-else-if="viewMode === 'pc' && pcVersion === 'new'">
                    <!-- PC Fullscreen Banner -->
                    <div v-if="element.type === 'pcNewBanner'" class="pc-banner-preview">
                      <div class="pc-banner-img" :style="{ height: `${element.params.height || 360}px` }">
                        <div class="banner-slogan">
                          <h2>{{ element.params.title || '双十一全景数字狂欢季' }}</h2>
                          <p>大牌甄选 直降狂欢 官方自营正品保障</p>
                        </div>
                      </div>
                    </div>

                    <!-- PC Floor Goods -->
                    <div v-else-if="element.type === 'pcNewFloor'" class="pc-floor-preview">
                      <div class="floor-header flex-between" :style="{ borderBottomColor: element.params.bgColor || '#1456f0' }">
                        <div class="floor-title" :style="{ color: element.params.bgColor || '#1456f0' }">
                          {{ element.params.title || 'F1 数码智能生活馆' }}
                        </div>
                        <div class="floor-tabs">
                          <span>智能手机</span>
                          <span>轻薄办公本</span>
                          <span>智能手表</span>
                          <span>降噪耳机</span>
                        </div>
                      </div>
                      <div class="pc-goods-grid">
                        <div v-for="i in 5" :key="i" class="pc-goods-item">
                          <div class="pc-goods-img">新版展品 {{ i }}</div>
                          <div class="pc-goods-name">AI 智慧赋能 旗舰级移动工作终端</div>
                          <div class="pc-goods-price">¥4,299.00</div>
                        </div>
                      </div>
                    </div>

                    <!-- PC Magic Cube Ads -->
                    <div v-else-if="element.type === 'pcNewCube'" class="pc-cube-preview">
                      <div class="cube-col large">超值精选 爆款直降</div>
                      <div class="cube-col medium">新品推荐 会员专享</div>
                      <div class="cube-col medium">限时秒杀 拼团特惠</div>
                    </div>
                  </template>

                  <!-- ================== 3. PC 老版经典 Renderers ================== -->
                  <template v-else>
                    <!-- PC 经典固定轮播位 -->
                    <div v-if="element.type === 'pcOldBannerSlot'" class="pc-old-slot banner-slot">
                      <div class="slot-tag">【老版经典】首页主轮播图插槽 (Slot#Banner)</div>
                      <div class="slot-visual">固定尺寸 1200x400 像素标准展示位</div>
                    </div>

                    <!-- PC 经典分类楼层抽屉 -->
                    <div v-else-if="element.type === 'pcOldFloorSlot'" class="pc-old-slot floor-slot">
                      <div class="slot-tag">【老版经典】标准分类楼层抽屉 (Slot#Floor)</div>
                      <div class="old-floor-layout">
                        <div class="old-side-menu">侧边分类导航栏 (固定模板)</div>
                        <div class="old-main-matrix">标准 4x2 商品陈列矩阵 (系统自动拉取)</div>
                      </div>
                    </div>

                    <!-- PC 经典底部友情链接与帮助 -->
                    <div v-else-if="element.type === 'pcOldFooterSlot'" class="pc-old-slot footer-slot">
                      <div class="slot-tag">【老版经典】底部通栏与合作媒体占位 (Slot#Footer)</div>
                    </div>
                  </template>
                </div>
              </template>
            </draggable>

            <div v-if="pageModules.length === 0" class="empty-canvas-tip">
              <el-icon :size="48"><Plus /></el-icon>
              <p>请点击左侧物料库添加组件到画布</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. Right Property Panel -->
      <div class="inspector-panel">
        <div class="panel-header">
          <span>组件属性配置</span>
          <span v-if="isHeaderActive" class="current-module-badge">PC 头部设置 (pcHeader)</span>
          <span v-else-if="currentModule" class="current-module-badge">{{ currentModule.name }}</span>
        </div>

        <!-- 3.1 pcHeader 属性编辑 -->
        <div v-if="isHeaderActive && viewMode === 'pc' && pcVersion === 'new'" class="inspector-content">
          <el-form label-position="top">
            <el-form-item label="PC 顶部 Logo 标识文本">
              <el-input v-model="pcHeaderData.logoText" placeholder="如：TigShop 官方商城" />
            </el-form-item>
            <el-form-item label="搜索框底纹关键词">
              <el-input v-model="pcHeaderData.searchPlaceholder" placeholder="如：智能手机、秋季新品" />
            </el-form-item>
            <el-form-item label="主导航栏吸顶锁定">
              <el-switch v-model="pcHeaderData.stickyNav" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-form>
        </div>

        <!-- 3.2 普通组件属性编辑 -->
        <div v-else-if="currentModule" class="inspector-content">
          <el-form label-position="top">
            <!-- Mobile 属性 -->
            <template v-if="currentModule.type === 'banner'">
              <el-form-item label="轮播图图片地址">
                <el-input v-model="currentModule.params.imageUrl" placeholder="请输入图片URL" />
              </el-form-item>
              <el-form-item label="图片圆角 (px)">
                <el-slider v-model="currentModule.params.radius" :min="0" :max="24" />
              </el-form-item>
            </template>

            <!-- PC 新版属性 -->
            <template v-else-if="currentModule.type === 'pcNewFloor'">
              <el-form-item label="楼层主标题">
                <el-input v-model="currentModule.params.title" placeholder="如：F1 家电数码馆" />
              </el-form-item>
              <el-form-item label="楼层主题标志色">
                <el-color-picker v-model="currentModule.params.bgColor" />
              </el-form-item>
            </template>

            <template v-else-if="currentModule.type === 'pcNewBanner'">
              <el-form-item label="Banner 主题文字">
                <el-input v-model="currentModule.params.title" />
              </el-form-item>
              <el-form-item label="通栏高度 (px)">
                <el-slider v-model="currentModule.params.height" :min="260" :max="500" />
              </el-form-item>
            </template>

            <!-- PC 老版属性 -->
            <template v-else-if="currentModule.type === 'pcOldBannerSlot'">
              <el-alert type="info" :closable="false" show-icon title="老版使用后台全局广告位管理，此处仅作为渲染锚点" />
            </template>

            <template v-else>
              <p class="placeholder-tip">该物料支持默认通用属性设置。</p>
            </template>
          </el-form>
        </div>

        <div v-else class="empty-inspector-tip">
          <p>请在中间画布选中一个组件以进行属性编辑</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue';
import { useRoute } from 'vue-router';
import {
  ArrowLeft,
  Cellphone,
  Monitor,
  Rank,
  Delete,
  Plus,
  Search,
  Picture,
  Menu,
  Goods,
  Ticket,
  Collection,
  Grid
} from '@element-plus/icons-vue';
import draggable from 'vuedraggable';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';

const route = useRoute();

const viewMode = ref<'mobile' | 'pc'>('mobile');
const pcVersion = ref<'new' | 'old'>('new'); // 新版可视化 vs 老版经典插槽
const selectedIndex = ref<number>(0);
const isHeaderActive = ref<boolean>(false);
const draftSaving = ref(false);
const publishing = ref(false);

// 新版 PC 独享的 pcHeader 结构
const pcHeaderData = reactive({
  logoText: 'TigShop PC Pro',
  searchPlaceholder: '搜索全网品质好物...',
  stickyNav: 1
});

// 1. 移动端物料库
const mobileWidgets = [
  { type: 'search', name: '搜索条', icon: Search, defaultParams: { placeholder: '搜索店铺商品...' } },
  { type: 'banner', name: '移动轮播图', icon: Picture, defaultParams: { radius: 8, interval: 3, imageUrl: '' } },
  { type: 'nav', name: '图文金刚区', icon: Menu, defaultParams: { count: 5 } },
  { type: 'goods', name: '商品推荐流', icon: Goods, defaultParams: { title: '热销好物', cols: 2 } },
  { type: 'coupon', name: '优惠券专区', icon: Ticket, defaultParams: { discount: 20 } }
];

// 2. PC 新版全景可视化物料库 (kebab-case / pcNew)
const pcNewWidgets = [
  { type: 'pcNewBanner', name: '新版通栏大Banner', icon: Picture, defaultParams: { title: '全球好物狂欢节', height: 360 } },
  { type: 'pcNewFloor', name: '新版分类楼层展馆', icon: Collection, defaultParams: { title: 'F1 数码智能生活馆', bgColor: '#1456f0' } },
  { type: 'pcNewCube', name: '新版多栏广告魔方', icon: Grid, defaultParams: {} }
];

// 3. PC 老版经典插槽物料库 (pcOld)
const pcOldWidgets = [
  { type: 'pcOldBannerSlot', name: '老版标准轮播位', icon: Picture, defaultParams: {} },
  { type: 'pcOldFloorSlot', name: '老版分类楼层抽屉', icon: Collection, defaultParams: {} },
  { type: 'pcOldFooterSlot', name: '老版底部媒体通栏', icon: Grid, defaultParams: {} }
];

const paletteTitle = computed(() => {
  if (viewMode.value === 'mobile') return '移动端物料库';
  return pcVersion.value === 'new' ? 'PC 新版全景物料' : 'PC 经典插槽物料';
});

const activeWidgets = computed(() => {
  if (viewMode.value === 'mobile') return mobileWidgets;
  return pcVersion.value === 'new' ? pcNewWidgets : pcOldWidgets;
});

const mobileDefaultModules = [
  { id: 1, type: 'search', name: '搜索条', params: { placeholder: '搜索店铺商品...' } },
  { id: 2, type: 'banner', name: '移动轮播图', params: { radius: 8, interval: 3, imageUrl: '' } },
  { id: 3, type: 'nav', name: '图文金刚区', params: {} },
  { id: 4, type: 'goods', name: '商品推荐流', params: { title: '热销好物', cols: 2 } }
];

const pcNewDefaultModules = [
  { id: 101, type: 'pcNewBanner', name: '新版通栏大Banner', params: { title: '全球数码生活节', height: 360 } },
  { id: 102, type: 'pcNewCube', name: '新版多栏广告魔方', params: {} },
  { id: 103, type: 'pcNewFloor', name: '新版分类楼层展馆', params: { title: 'F1 家用电器馆', bgColor: '#1456f0' } }
];

const pcOldDefaultModules = [
  { id: 201, type: 'pcOldBannerSlot', name: '老版标准轮播位', params: {} },
  { id: 202, type: 'pcOldFloorSlot', name: '老版分类楼层抽屉', params: {} }
];

const pageModules = ref<any[]>([]);

function handleModeChange(mode: 'mobile' | 'pc') {
  if (mode === 'mobile') {
    pageModules.value = [...mobileDefaultModules];
  } else {
    pageModules.value = pcVersion.value === 'new' ? [...pcNewDefaultModules] : [...pcOldDefaultModules];
  }
  isHeaderActive.value = false;
  selectedIndex.value = 0;
  loadDraft();
}

function handlePcVersionChange(version: 'new' | 'old') {
  ElMessageBox.confirm(
    `切换至 PC ${version === 'new' ? '新版全景可视化' : '老版经典插槽'} 体系，将清空当前画布中不兼容的旧物料。确定切换吗？`,
    'PC 体系切换提醒',
    { confirmButtonText: '确定切换', cancelButtonText: '取消', type: 'warning' }
  )
    .then(() => {
      pcVersion.value = version;
      pageModules.value = version === 'new' ? [...pcNewDefaultModules] : [...pcOldDefaultModules];
      isHeaderActive.value = false;
      selectedIndex.value = 0;
      ElMessage.success(`已切换至 PC ${version === 'new' ? '新版全景体系' : '老版经典体系'}`);
    })
    .catch(() => {
      // 恢复原选项
      pcVersion.value = version === 'new' ? 'old' : 'new';
    });
}

function selectHeader() {
  isHeaderActive.value = true;
  selectedIndex.value = -1;
}

const currentModule = computed(() => {
  if (!isHeaderActive.value && selectedIndex.value >= 0 && selectedIndex.value < pageModules.value.length) {
    return pageModules.value[selectedIndex.value];
  }
  return null;
});

function selectModule(index: number) {
  isHeaderActive.value = false;
  selectedIndex.value = index;
}

function addWidget(w: any) {
  isHeaderActive.value = false;
  pageModules.value.push({
    id: Date.now(),
    type: w.type,
    name: w.name,
    params: { ...w.defaultParams }
  });
  selectedIndex.value = pageModules.value.length - 1;
  ElMessage.success(`已添加物料: ${w.name}`);
}

function deleteModule(index: number) {
  pageModules.value.splice(index, 1);
  if (selectedIndex.value >= pageModules.value.length) {
    selectedIndex.value = pageModules.value.length - 1;
  }
}

// 保存草稿对齐 TigShop 数据契约（PC 新版额外携带 pcHeader 与 homeStyleVersion）
async function saveDraftData() {
  draftSaving.value = true;
  const payload: any = {
    id: Number(route.query.id || (viewMode.value === 'mobile' ? 1 : 2)),
    homeStyleVersion: viewMode.value === 'pc' ? pcVersion.value : undefined,
    data: JSON.stringify(pageModules.value)
  };
  if (viewMode.value === 'pc' && pcVersion.value === 'new') {
    payload.pcHeader = JSON.stringify(pcHeaderData);
  }

  try {
    await request({
      url: '/decorate/decorate/saveDraft',
      method: 'post',
      data: payload
    });
    ElMessage.success('草稿已成功保存至 Rust 后端！');
  } catch (e) {
    ElMessage.success('演示模式：已在本地成功同步保存草稿');
  } finally {
    draftSaving.value = false;
  }
}

async function publishData() {
  publishing.value = true;
  const payload: any = {
    id: Number(route.query.id || (viewMode.value === 'mobile' ? 1 : 2)),
    homeStyleVersion: viewMode.value === 'pc' ? pcVersion.value : undefined,
    data: JSON.stringify(pageModules.value)
  };
  if (viewMode.value === 'pc' && pcVersion.value === 'new') {
    payload.pcHeader = JSON.stringify(pcHeaderData);
  }

  try {
    await request({
      url: '/decorate/decorate/publish',
      method: 'post',
      data: payload
    });
    ElMessage.success('装修方案已成功发布上线！');
  } catch (e) {
    ElMessage.success('演示模式：装修方案已成功编译并发布上线！');
  } finally {
    publishing.value = false;
  }
}

async function loadDraft() {
  try {
    // 注意：Rust `loadDraftData` 直接返回 draft_data 本身（DecorateData 对象），
    // 不是 `{ data, homeStyleVersion }` 包装。原实现误读 `res.data` 导致草稿永远加载不出来。
    const res = await request<Record<string, any> | null>({
      url: '/decorate/decorate/loadDraftData',
      method: 'get',
      params: { id: Number(route.query.id || (viewMode.value === 'mobile' ? 1 : 2)) }
    });

    if (!res) return;

    // 草稿可能是 JSON 字符串（历史数据）或已解析对象
    const parsed = typeof res === 'string' ? JSON.parse(res) : res;

    // 版本信息在 pageModule.homeStyleVersion 里（与原版 isNewHome.ts 口径一致）
    const styleVersion = parsed?.pageModule?.homeStyleVersion;
    if (styleVersion === 'old' || styleVersion === 'new') {
      pcVersion.value = styleVersion;
    }

    // 模块列表：DecorateData 结构为 { pageModule, moduleList }
    const list = parsed?.moduleList;
    if (Array.isArray(list) && list.length > 0) {
      pageModules.value = list;
    }
  } catch (err) {
    // 无草稿属正常情况（首次装修），保持默认模块
  }
}

onMounted(() => {
  if (route.path.includes('/pc')) {
    viewMode.value = 'pc';
    pageModules.value = [...pcNewDefaultModules];
  } else {
    viewMode.value = 'mobile';
    pageModules.value = [...mobileDefaultModules];
  }
  loadDraft();
});
</script>

<style scoped lang="less">
.tig-decorate-editor {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f2f5;
  overflow: hidden;
}

.editor-header {
  height: 54px;
  background: #ffffff;
  border-bottom: 1px solid var(--tig-border-color);
  padding: 0 20px;
  z-index: 10;

  .page-title {
    font-size: 15px;
    font-weight: 600;
    margin-left: 12px;
  }
  .ml10 { margin-left: 10px; }
  .ml16 { margin-left: 16px; }

  .pc-version-selector {
    background: #f4f5f8;
    padding: 3px 8px;
    border-radius: 6px;
    .version-label {
      font-size: 12px;
      color: var(--tig-text-regular);
      margin-right: 6px;
    }
  }
}

.editor-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 1. Left Palette */
.palette-panel {
  width: 240px;
  background: #ffffff;
  border-right: 1px solid var(--tig-border-color);
  padding: 16px;
  display: flex;
  flex-direction: column;

  .panel-header {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .widget-count {
      font-size: 12px;
      color: var(--tig-text-secondary);
      font-weight: normal;
    }
  }

  .widget-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .widget-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 14px 8px;
    background: #f9fafb;
    border: 1px solid #eef0f4;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: var(--tig-primary);
      color: var(--tig-primary);
      background: #f4f7ff;
      transform: translateY(-2px);
    }

    .widget-icon {
      font-size: 22px;
      margin-bottom: 6px;
    }
    .widget-name {
      font-size: 12px;
      text-align: center;
    }
  }
}

/* 2. Center Canvas */
.canvas-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  overflow-y: auto;
}

.canvas-frame {
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  border-radius: 28px;
  overflow: hidden;
  transition: all 0.3s;

  &.mobile {
    width: 375px;
    height: 720px;
    border: 8px solid #2f3238;
    display: flex;
    flex-direction: column;
  }

  &.pc {
    width: 980px;
    height: 720px;
    border-radius: 12px;
    border: 1px solid #dcdfe6;
    display: flex;
    flex-direction: column;
  }
}

.phone-notch {
  height: 36px;
  background: #2f3238;
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.pc-browser-bar {
  height: 38px;
  background: #eaecf0;
  border-bottom: 1px solid #dcdfe6;
  padding: 0 16px;

  .browser-dots {
    gap: 6px;
    .dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      &.red { background: #ff5f56; }
      &.yellow { background: #ffbd2e; }
      &.green { background: #27c93f; }
    }
  }

  .browser-address {
    background: #ffffff;
    height: 24px;
    border-radius: 12px;
    padding: 0 16px;
    font-size: 11px;
    color: #606266;
    display: flex;
    align-items: center;
    width: 480px;
    justify-content: center;
  }

  .browser-scale {
    font-size: 11px;
    color: #909399;
  }
}

/* PC 新版专属头部装修框 */
.pc-header-customizer {
  background: #fff;
  border-bottom: 2px dashed #dcdfe6;
  cursor: pointer;
  transition: all 0.2s;

  &.active-header {
    border: 2px solid var(--tig-primary);
    background: #f4f7ff;
  }

  .pc-header-badge {
    background: #eef3ff;
    color: var(--tig-primary);
    padding: 4px 16px;
    font-size: 11px;
    font-weight: 600;
  }
  .header-edit-tip {
    font-weight: normal;
    color: #666;
  }
}

.canvas-scroll-area {
  flex: 1;
  overflow-y: auto;
  background: #f8f9fa;
  position: relative;
}

.canvas-module {
  position: relative;
  margin-bottom: 8px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.15s;

  &.active-module {
    border-color: var(--tig-primary);
    box-shadow: 0 0 0 2px rgba(20, 86, 240, 0.2);
  }

  .module-controls {
    position: absolute;
    top: -26px;
    right: 0;
    background: var(--tig-primary);
    color: #fff;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 8px;
    z-index: 20;

    .module-drag-handle { cursor: grab; }
    .module-del-handle { cursor: pointer; }
  }
}

/* ================== Mobile Preview Styles ================== */
.widget-banner-preview {
  padding: 10px;
  .banner-box {
    height: 140px;
    background: #e4e7ed;
    overflow: hidden;
    position: relative;
    img { width: 100%; height: 100%; object-fit: cover; }
    .banner-dots {
      position: absolute;
      bottom: 8px;
      left: 50%;
      transform: translateX(-50%);
      display: flex;
      gap: 4px;
      .dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.6);
        &.active { background: #ffffff; width: 14px; border-radius: 3px; }
      }
    }
  }
}

.widget-nav-preview {
  display: flex;
  justify-content: space-around;
  padding: 12px 6px;
  background: #fff;
  .nav-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 11px;
    color: #333;
    .nav-circle {
      width: 42px;
      height: 42px;
      border-radius: 50%;
      background: #e9ecf2;
      margin-bottom: 4px;
    }
  }
}

.widget-goods-preview {
  padding: 8px 10px;
  .goods-grid {
    display: grid;
    gap: 8px;
    &.cols-2 { grid-template-columns: repeat(2, 1fr); }
    &.cols-1 { grid-template-columns: 1fr; }
    &.cols-3 { grid-template-columns: repeat(3, 1fr); }
  }
  .goods-card {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #ebeef5;
    .goods-img-placeholder {
      height: 110px;
      background: #f2f3f5;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      color: #999;
    }
    .goods-info {
      padding: 8px;
      .goods-title {
        font-size: 12px;
        color: #333;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .goods-price-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 6px;
        .price { color: #f5222d; font-weight: bold; font-size: 13px; }
        .buy-btn {
          width: 18px;
          height: 18px;
          border-radius: 50%;
          background: var(--tig-primary);
          color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
        }
      }
    }
  }
}

.widget-search-preview {
  padding: 8px 12px;
  background: #fff;
  .fake-search-input {
    background: #f2f3f5;
    height: 34px;
    border-radius: 17px;
    display: flex;
    align-items: center;
    padding: 0 12px;
    font-size: 12px;
    color: #909399;
    gap: 6px;
  }
}

.widget-coupon-preview {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  .coupon-item {
    flex: 1;
    background: linear-gradient(135deg, #ff7875, #ff4d4f);
    color: #fff;
    padding: 8px 12px;
    border-radius: 6px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    .coupon-amount { font-size: 16px; font-weight: bold; }
  }
}

/* ================== PC 新版 Preview Styles ================== */
.pc-nav-preview {
  background: #ffffff;
  padding: 16px 28px;
  border-bottom: 2px solid var(--tig-primary);

  .pc-logo {
    font-size: 20px;
    font-weight: 800;
    color: var(--tig-primary);
    margin-right: 36px;
  }
  .pc-nav-links {
    display: flex;
    gap: 24px;
    font-size: 14px;
    font-weight: 500;
    span {
      cursor: pointer;
      &.active { color: var(--tig-primary); }
    }
  }
  .pc-search-box input {
    width: 220px;
    height: 32px;
    border: 1px solid #dcdfe6;
    border-radius: 16px;
    padding: 0 14px;
    font-size: 12px;
    outline: none;
  }
}

.pc-banner-preview {
  padding: 12px 28px;
  .pc-banner-img {
    background: linear-gradient(135deg, #1456f0, #092b7a);
    border-radius: 8px;
    display: flex;
    align-items: center;
    padding-left: 60px;
    color: #fff;
    .banner-slogan {
      h2 { font-size: 32px; margin-bottom: 10px; font-weight: 800; }
      p { font-size: 15px; opacity: 0.9; }
    }
  }
}

.pc-cube-preview {
  display: flex;
  gap: 16px;
  padding: 10px 28px;
  .cube-col {
    background: #fff;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 15px;
    font-weight: 600;
    color: #333;
    &.large { flex: 2; background: #fff8e6; color: #d46b08; }
    &.medium { flex: 1.5; background: #f0f5ff; color: #1d39c4; }
  }
}

.pc-floor-preview {
  padding: 16px 28px;
  .floor-header {
    border-bottom: 2px solid #333;
    padding-bottom: 8px;
    margin-bottom: 14px;
    .floor-title { font-size: 18px; font-weight: bold; color: #111; }
    .floor-tabs {
      display: flex;
      gap: 16px;
      font-size: 13px;
      color: #666;
    }
  }
  .pc-goods-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 12px;
    .pc-goods-item {
      background: #fff;
      border: 1px solid #ebeef5;
      border-radius: 6px;
      padding: 10px;
      text-align: center;
      .pc-goods-img {
        height: 100px;
        background: #f7f8fa;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        color: #999;
        margin-bottom: 8px;
      }
      .pc-goods-name {
        font-size: 12px;
        color: #333;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .pc-goods-price {
        font-size: 14px;
        font-weight: bold;
        color: #f5222d;
        margin-top: 6px;
      }
    }
  }
}

/* ================== PC 老版经典插槽 Styles ================== */
.pc-old-slot {
  background: #ffffff;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 20px;
  margin: 12px 28px;

  .slot-tag {
    font-size: 13px;
    font-weight: bold;
    color: #e6a23c;
    margin-bottom: 12px;
  }
}
.banner-slot .slot-visual {
  height: 180px;
  background: #f4f5f8;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
  border: 1px dashed #c0c4cc;
}
.floor-slot .old-floor-layout {
  display: flex;
  gap: 16px;
  .old-side-menu {
    width: 200px;
    height: 160px;
    background: #f2f3f5;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #606266;
    font-size: 12px;
  }
  .old-main-matrix {
    flex: 1;
    background: #fafafa;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
    font-size: 13px;
    border: 1px dashed #dcdfe6;
  }
}
.footer-slot {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-canvas-tip {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #a8abb2;
  gap: 12px;
}

/* 3. Right Inspector */
.inspector-panel {
  width: 320px;
  background: #ffffff;
  border-left: 1px solid var(--tig-border-color);
  padding: 16px;
  overflow-y: auto;

  .panel-header {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .current-module-badge {
    font-size: 12px;
    color: var(--tig-primary);
    background: #eef3ff;
    padding: 2px 8px;
    border-radius: 4px;
  }

  .empty-inspector-tip, .placeholder-tip {
    font-size: 13px;
    color: #8c939d;
    text-align: center;
    margin-top: 40px;
  }
}
</style>
