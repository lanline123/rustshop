<template>
  <div class="business-page proxy-page">
    <el-row :gutter="14">
      <!-- 左：选择会员（原版 SelectUser / SelectUserDialog） -->
      <el-col :span="6">
        <div class="tig-card panel">
          <div class="panel-title">1. 选择会员</div>
          <div class="panel-toolbar">
            <el-input v-model="userKeyword" placeholder="会员名 / 手机号" clearable size="small" @keyup.enter="loadUsers" />
            <el-button size="small" type="primary" plain :icon="Search" @click="loadUsers">搜索</el-button>
          </div>
          <el-table v-loading="userLoading" :data="users" size="small" border highlight-current-row @row-click="selectUser">
            <el-table-column prop="userId" label="ID" width="60" align="center" />
            <el-table-column prop="username" label="会员名" min-width="100" show-overflow-tooltip />
            <el-table-column prop="mobile" label="手机号" width="110" />
          </el-table>
          <div class="panel-footer">
            <el-pagination
              v-model:current-page="userPage"
              :page-size="10"
              :total="userTotal"
              layout="prev, pager, next"
              small
              @current-change="loadUsers"
            />
          </div>
          <div v-if="currentUser" class="current-user">
            当前会员：<strong>{{ currentUser.username }}</strong>（UID {{ currentUser.userId }}）
            <el-button type="primary" link size="small" @click="loadCart">刷新购物车</el-button>
          </div>
          <el-empty v-else description="请点击左侧会员选择" :image-size="60" />
        </div>
      </el-col>

      <!-- 中：选择商品（原版商品搜索 + ProductSpecPop） -->
      <el-col :span="9">
        <div class="tig-card panel">
          <div class="panel-title">2. 选择商品</div>
          <div class="panel-toolbar">
            <el-input v-model="productKeyword" placeholder="商品名称 / 货号" clearable size="small" @keyup.enter="loadProducts" />
            <el-button size="small" type="primary" plain :icon="Search" @click="loadProducts">搜索</el-button>
          </div>
          <div v-loading="productLoading" class="product-grid">
            <div v-for="p in products" :key="p.productId" class="product-card" @click="openSkuDialog(p)">
              <el-image :src="resolveImage(p.picUrl)" class="product-pic" fit="cover" />
              <div class="product-name">{{ p.productName }}</div>
              <div class="product-meta">
                <span class="price">¥{{ money(p.productPrice) }}</span>
                <span class="muted">库存 {{ p.productStock }}</span>
              </div>
            </div>
            <el-empty v-if="!productLoading && products.length === 0" description="暂无商品，请输入关键词搜索" :image-size="60" />
          </div>
          <div class="panel-footer">
            <el-pagination
              v-model:current-page="productPage"
              :page-size="12"
              :total="productTotal"
              layout="prev, pager, next, total"
              small
              @current-change="loadProducts"
            />
          </div>
        </div>
      </el-col>

      <!-- 右：购物车 + 结算（原版 CheckPanel 结构的主流程） -->
      <el-col :span="9">
        <div class="tig-card panel cart-panel">
          <div class="panel-title">3. 购物车与结算</div>
          <div v-if="!currentUser" class="empty-tip">请先选择会员</div>
          <template v-else>
            <div v-loading="cartLoading" class="cart-list">
              <div v-for="store in cartList" :key="store.shopId" class="store-block">
                <div class="store-title">{{ store.shopTitle || ('店铺 ' + store.shopId) }}</div>
                <div v-for="item in store.carts" :key="item.cartId" class="cart-item">
                  <el-checkbox
                    :model-value="item.isChecked === 1"
                    @change="(v: boolean | string | number) => toggleCheck(item, Boolean(v))"
                  />
                  <el-image :src="resolveImage(item.picThumb)" class="cart-pic" fit="cover" />
                  <div class="cart-info">
                    <div class="cart-name">{{ item.productName || item.productSn }}</div>
                    <div class="muted">{{ item.skuData || '' }}</div>
                    <div class="cart-price">¥{{ money(item.originalPrice) }}</div>
                  </div>
                  <el-input-number
                    :model-value="item.quantity"
                    :min="1"
                    :max="item.productStock || 9999"
                    size="small"
                    style="width: 100px"
                    @change="(v: number | undefined) => updateQuantity(item, v)"
                  />
                  <el-button type="danger" link size="small" @click="removeCartItem(item)">删除</el-button>
                </div>
              </div>
              <el-empty v-if="cartList.length === 0" description="购物车暂无商品" :image-size="60" />
            </div>

            <div class="cart-total">
              <span>商品合计：<strong>¥{{ money(cartTotal.productAmount) }}</strong></span>
              <span>已选 {{ cartTotal.checkedCount ?? 0 }} / {{ cartTotal.totalCount ?? 0 }} 件</span>
            </div>

            <el-form label-width="90px" class="check-form">
              <el-form-item label="配送方式">
                <el-select v-model="checkForm.deliveryType" style="width: 180px">
                  <el-option :value="1" label="物流配送" />
                  <el-option :value="2" label="门店自提（未迁移）" />
                  <el-option :value="3" label="同城配送（未迁移）" />
                </el-select>
              </el-form-item>
              <el-form-item label="地址ID">
                <el-input-number v-model="checkForm.addressId" :min="0" style="width: 180px" />
              </el-form-item>
              <el-form-item label="支付方式">
                <el-select v-model="checkForm.payTypeId" style="width: 180px" clearable>
                  <el-option v-for="p in payTypes" :key="p.typeId" :value="p.typeId" :label="p.typeName" :disabled="p.disabled" />
                </el-select>
              </el-form-item>
              <el-form-item label="买家备注">
                <el-input v-model="checkForm.buyerNote" type="textarea" :rows="2" placeholder="选填" />
              </el-form-item>
              <el-form-item>
                <div class="dialog-actions">
                  <el-button :loading="checking" @click="previewCheck">结算预览</el-button>
                  <el-button type="primary" :loading="submitting" @click="submitOrder">提交订单</el-button>
                </div>
              </el-form-item>
            </el-form>

            <el-alert
              v-if="checkResult"
              :type="checkResult.ok ? 'success' : 'error'"
              :title="checkResult.ok ? '结算数据已返回（结构见下方）' : '结算失败'"
              :description="checkResult.message"
              :closable="false"
              show-icon
            />
            <pre v-if="checkResult?.data" class="result-json">{{ checkResult.data }}</pre>
          </template>
        </div>
      </el-col>
    </el-row>

    <!-- 商品规格选择（原版 ProductSpecPop） -->
    <el-dialog v-model="skuVisible" title="选择商品规格" width="560px" destroy-on-close>
      <div v-if="skuProduct" class="sku-dialog">
        <div class="sku-product">
          <el-image :src="resolveImage(skuProduct.picUrl)" style="width: 60px; height: 60px; border-radius: 4px" fit="cover" />
          <div>
            <div>{{ skuProduct.productName }}</div>
            <div class="muted">¥{{ money(skuProduct.productPrice) }}</div>
          </div>
        </div>
        <el-form label-width="90px">
          <el-form-item label="规格">
            <el-select v-model="skuForm.skuId" placeholder="选择规格（可无）" clearable style="width: 100%">
              <el-option v-for="s in availabilitySkus" :key="s.skuId" :value="s.skuId" :label="`${s.skuValue}（库存 ${s.skuStock}，¥${money(s.skuPrice)}）`" />
            </el-select>
          </el-form-item>
          <el-form-item label="数量">
            <el-input-number v-model="skuForm.number" :min="1" :max="9999" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="skuVisible = false">取消</el-button>
        <el-button type="primary" :loading="addingCart" @click="addToCart">加入购物车</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 代客下单（参照原版 order/proxyShopping/Index.vue 的三栏 POS 结构：
 * 选择会员 → 选择商品 → 购物车 → 结算提交）。
 *
 * 接口（Rust `routes/trade/proxy_shopping_*.rs`，均为契约内路径）：
 * - 会员：`GET /proxyShopping/user/list`
 * - 商品：`GET /product/product/list`、`GET /proxyShopping/product/getProductAvailability`
 * - 购物车：`POST /proxyShopping/cart/list|addToCart|updateItem|removeItem|updateCheck`
 * - 结算：`GET /proxyShopping/order/check/getAvailablePaymentType`、
 *   `POST /proxyShopping/order/check/index|submit`
 *
 * 说明：后端 `order/check/index`/`submit` 与配送方式依赖未迁移的结算引擎，
 * 会返回明确的「未迁移」业务错误 —— 页面按真实错误提示展示，不造假数据。
 */
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import request from '@/utils/request';

function money(value: unknown) {
  return Number(value ?? 0).toFixed(2);
}

function resolveImage(url?: string) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/static')) return url;
  return `/static/${url}`;
}

// ————————————————— 会员 —————————————————
const userLoading = ref(false);
const users = ref<any[]>([]);
const userKeyword = ref('');
const userPage = ref(1);
const userTotal = ref(0);
const currentUser = ref<any>(null);

async function loadUsers() {
  userLoading.value = true;
  try {
    const res = await request<any>({
      url: '/proxyShopping/user/list',
      method: 'get',
      params: { page: userPage.value, size: 10, keyword: userKeyword.value || undefined }
    });
    users.value = res?.list ?? [];
    userTotal.value = res?.total ?? users.value.length;
  } finally {
    userLoading.value = false;
  }
}

function selectUser(row: any) {
  currentUser.value = row;
  loadCart();
  loadPayTypes();
}

// ————————————————— 商品 —————————————————
const productLoading = ref(false);
const products = ref<any[]>([]);
const productKeyword = ref('');
const productPage = ref(1);
const productTotal = ref(0);

async function loadProducts() {
  productLoading.value = true;
  try {
    const res = await request<any>({
      url: '/product/product/list',
      method: 'get',
      params: { page: productPage.value, size: 12, keyword: productKeyword.value || undefined }
    });
    products.value = res?.list ?? [];
    productTotal.value = res?.total ?? products.value.length;
  } finally {
    productLoading.value = false;
  }
}

// 规格弹窗
const skuVisible = ref(false);
const skuProduct = ref<any>(null);
const availabilitySkus = ref<any[]>([]);
const skuForm = reactive({ skuId: undefined as number | undefined, number: 1 });
const addingCart = ref(false);

async function openSkuDialog(product: any) {
  skuProduct.value = product;
  availabilitySkus.value = [];
  skuForm.skuId = undefined;
  skuForm.number = 1;
  skuVisible.value = true;
  try {
    const res = await request<any>({
      url: '/proxyShopping/product/getProductAvailability',
      method: 'get',
      params: { id: product.productId, userId: currentUser.value?.userId }
    });
    const skus: any[] = [];
    if (res?.sku) skus.push(res.sku);
    if (Array.isArray(res?.skuList)) skus.push(...res.skuList);
    availabilitySkus.value = skus;
  } catch (err) {
    // 无规格商品可继续按商品维度加入
  }
}

async function addToCart() {
  if (!currentUser.value) {
    ElMessage.warning('请先选择会员');
    return;
  }
  if (!skuProduct.value) return;
  if (availabilitySkus.value.length > 0 && !skuForm.skuId) {
    ElMessage.warning('请选择商品规格');
    return;
  }
  addingCart.value = true;
  try {
    await request({
      url: '/proxyShopping/cart/addToCart',
      method: 'post',
      data: {
        id: skuProduct.value.productId,
        number: skuForm.number,
        skuId: skuForm.skuId,
        userId: currentUser.value.userId
      }
    });
    ElMessage.success('已加入购物车');
    skuVisible.value = false;
    loadCart();
  } finally {
    addingCart.value = false;
  }
}

// ————————————————— 购物车 —————————————————
const cartLoading = ref(false);
const cartList = ref<any[]>([]);
const cartTotal = ref<any>({});

async function loadCart() {
  if (!currentUser.value) return;
  cartLoading.value = true;
  try {
    const res = await request<any>({
      url: '/proxyShopping/cart/list',
      method: 'post',
      data: { userId: currentUser.value.userId }
    });
    cartList.value = res?.cartList ?? [];
    cartTotal.value = res?.total ?? {};
  } finally {
    cartLoading.value = false;
  }
}

async function updateQuantity(item: any, quantity: number | undefined) {
  if (!quantity) return;
  await request({
    url: '/proxyShopping/cart/updateItem',
    method: 'post',
    data: { cartId: item.cartId, data: { quantity } }
  });
  item.quantity = quantity;
  loadCart();
}

async function removeCartItem(item: any) {
  await request({
    url: '/proxyShopping/cart/removeItem',
    method: 'post',
    data: { cartIds: [item.cartId] }
  });
  ElMessage.success('已删除');
  loadCart();
}

async function toggleCheck(item: any, checked: boolean) {
  await request({
    url: '/proxyShopping/cart/updateCheck',
    method: 'post',
    data: {
      userId: currentUser.value?.userId,
      data: [{ cartId: item.cartId, isChecked: checked ? 1 : 0 }]
    }
  });
  item.isChecked = checked ? 1 : 0;
  loadCart();
}

// ————————————————— 结算 —————————————————
const payTypes = ref<any[]>([]);
const checking = ref(false);
const submitting = ref(false);
const checkResult = ref<{ ok: boolean; message: string; data?: string } | null>(null);

const checkForm = reactive({
  deliveryType: 1,
  addressId: 0,
  payTypeId: undefined as number | undefined,
  buyerNote: ''
});

async function loadPayTypes() {
  const res = await request<any>({
    url: '/proxyShopping/order/check/getAvailablePaymentType',
    method: 'get',
    params: { flowType: 15 }
  });
  payTypes.value = res?.list ?? res ?? [];
}

function buildCheckParam(selectFrom: number) {
  return {
    userId: currentUser.value?.userId,
    flowType: 15,
    selectFrom,
    deliveryType: checkForm.deliveryType,
    addressId: checkForm.addressId || undefined,
    payTypeId: checkForm.payTypeId,
    buyerNote: checkForm.buyerNote || undefined,
    usePoint: 0,
    useBalance: 0
  };
}

async function previewCheck() {
  if (!currentUser.value) {
    ElMessage.warning('请先选择会员');
    return;
  }
  checking.value = true;
  checkResult.value = null;
  try {
    const data = await request<any>({
      url: '/proxyShopping/order/check/index',
      method: 'post',
      data: buildCheckParam(1)
    });
    checkResult.value = { ok: true, message: '结算接口返回成功', data: JSON.stringify(data, null, 2) };
  } catch (err: any) {
    // 请求基座已 toast 真实错误；这里把错误信息落到面板里
    checkResult.value = { ok: false, message: err?.message || '结算失败（后端未迁移）' };
  } finally {
    checking.value = false;
  }
}

async function submitOrder() {
  if (!currentUser.value) {
    ElMessage.warning('请先选择会员');
    return;
  }
  submitting.value = true;
  checkResult.value = null;
  try {
    const data = await request<any>({
      url: '/proxyShopping/order/check/submit',
      method: 'post',
      data: buildCheckParam(4)
    });
    checkResult.value = { ok: true, message: '下单成功', data: JSON.stringify(data, null, 2) };
    loadCart();
  } catch (err: any) {
    checkResult.value = { ok: false, message: err?.message || '提交失败（后端未迁移）' };
  } finally {
    submitting.value = false;
  }
}

onMounted(async () => {
  await loadUsers();
  await loadProducts();
});
</script>

<style scoped>
.business-page {
  width: 100%;
}

.panel {
  min-height: calc(100vh - 150px);
  display: flex;
  flex-direction: column;
  padding: 12px;
}

.panel-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 10px;
}

.panel-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.panel-footer {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.current-user {
  margin-top: 10px;
  padding: 8px;
  border-radius: 6px;
  background: var(--el-color-primary-light-9);
  font-size: 12px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
  max-height: 56vh;
  overflow: auto;
}

.product-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  padding: 6px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.product-card:hover {
  border-color: var(--el-color-primary);
}

.product-pic {
  width: 100%;
  height: 110px;
  border-radius: 4px;
}

.product-name {
  margin-top: 6px;
  font-size: 12px;
  height: 34px;
  overflow: hidden;
  line-height: 17px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  margin-top: 4px;
}

.price {
  color: var(--el-color-danger);
  font-weight: 600;
}

.muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.empty-tip {
  padding: 30px 0;
  text-align: center;
  color: var(--el-text-color-secondary);
}

.cart-list {
  max-height: 38vh;
  overflow: auto;
}

.store-block {
  margin-bottom: 10px;
}

.store-title {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}

.cart-pic {
  width: 42px;
  height: 42px;
  border-radius: 4px;
}

.cart-info {
  flex: 1;
  min-width: 120px;
}

.cart-name {
  font-size: 13px;
}

.cart-price {
  color: var(--el-color-danger);
  font-size: 12px;
}

.cart-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-top: 1px solid var(--el-border-color-lighter);
  font-size: 13px;
}

.check-form {
  margin-top: 6px;
}

.dialog-actions {
  display: flex;
  gap: 10px;
}

.result-json {
  max-height: 160px;
  overflow: auto;
  background: #f7f8fa;
  padding: 8px;
  border-radius: 6px;
  font-size: 12px;
  white-space: pre-wrap;
}

.sku-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sku-product {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
