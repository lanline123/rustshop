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
                  <el-option :value="2" label="门店自提（需地址/自提点）" />
                  <el-option :value="3" label="同城配送" />
                </el-select>
                <el-button type="primary" link @click="openDeliveryOptions">配送选项</el-button>
              </el-form-item>
              <el-form-item label="收货地址">
                <el-input-number v-model="checkForm.addressId" :min="0" style="width: 150px" />
                <el-button type="primary" link @click="openAddressBook">地址簿</el-button>
              </el-form-item>
              <el-form-item v-if="checkForm.deliveryType === 2" label="自提信息">
                <el-button type="primary" link @click="openPickup">选择自提点 / 联系人</el-button>
                <span class="muted">已选：{{ pickedPickupName || '未选择' }}</span>
              </el-form-item>
              <el-form-item label="支付方式">
                <el-select v-model="checkForm.payTypeId" style="width: 180px" clearable>
                  <el-option v-for="p in payTypes" :key="p.typeId" :value="p.typeId" :label="p.typeName" :disabled="p.disabled" />
                </el-select>
              </el-form-item>
              <el-form-item label="优惠券">
                <el-button type="primary" link @click="openCouponDrawer">选择优惠券</el-button>
                <span class="muted">已选 {{ checkForm.useCouponIds.length }} 张</span>
              </el-form-item>
              <el-form-item label="改价">
                <el-button type="primary" link @click="openAlterPrice">订单改价</el-button>
                <span class="muted">{{ alterPriceSummary }}</span>
              </el-form-item>
              <el-form-item label="抹零">
                <el-checkbox v-model="checkForm.roundOffCents">抹分</el-checkbox>
                <el-checkbox v-model="checkForm.roundOffDime">抹角</el-checkbox>
              </el-form-item>
              <el-form-item label="现金/找零">
                <el-input-number v-model="checkForm.cashAmount" :min="0" :precision="2" :step="0.01" style="width: 150px" />
                <span class="muted" style="margin: 0 6px">找零</span>
                <el-input-number v-model="checkForm.cashChangeAmount" :min="0" :precision="2" :step="0.01" style="width: 150px" />
              </el-form-item>
              <el-form-item label="发票">
                <el-switch v-model="checkForm.needInvoice" />
              </el-form-item>
              <template v-if="checkForm.needInvoice">
                <el-form-item label="发票类型">
                  <el-select v-model="checkForm.invoiceType" style="width: 180px">
                    <el-option :value="1" label="电子普通发票" />
                    <el-option :value="2" label="增值税专用发票" />
                  </el-select>
                </el-form-item>
                <el-form-item label="抬头类型">
                  <el-select v-model="checkForm.titleType" style="width: 180px">
                    <el-option :value="1" label="个人" />
                    <el-option :value="2" label="企业" />
                  </el-select>
                </el-form-item>
                <el-form-item label="发票抬头">
                  <el-input v-model="checkForm.invoiceTitle" placeholder="请输入发票抬头" />
                </el-form-item>
                <el-form-item label="税号">
                  <el-input v-model="checkForm.taxNo" placeholder="企业抬头必填" />
                </el-form-item>
                <el-form-item label="发票内容">
                  <el-input v-model="checkForm.invoiceContent" placeholder="如：商品明细" />
                </el-form-item>
              </template>
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

    <!-- 地址簿抽屉（复刻原版 MoreAddressDrawer / EditAddressDrawer） -->
    <el-drawer v-model="addressVisible" title="收货地址簿" size="480px" destroy-on-close>
      <div class="drawer-header-actions">
        <el-button type="primary" size="small" @click="openAddressEdit(null)">新增地址</el-button>
        <el-button size="small" :loading="addressLoading" @click="loadAddresses">刷新</el-button>
      </div>
      <div v-loading="addressLoading" class="address-list">
        <div
          v-for="addr in addresses"
          :key="addr.addressId"
          class="address-card"
          :class="{ selected: checkForm.addressId === addr.addressId }"
          @click="selectAddress(addr)"
        >
          <div class="address-head">
            <strong>{{ addr.consignee }}</strong>
            <span class="muted">{{ addr.mobile }}</span>
            <el-tag v-if="addr.isDefault === 1" size="small" type="success">默认</el-tag>
            <el-tag v-if="addr.isSelected === 1" size="small" type="primary">已选</el-tag>
          </div>
          <div class="address-text">
            {{ (addr.regionNames || []).join(' ') }} {{ addr.address }}
          </div>
          <div class="address-actions">
            <el-button type="primary" link size="small" @click.stop="openAddressEdit(addr)">编辑</el-button>
            <el-button type="primary" link size="small" @click.stop="setSelectedAddress(addr)">设为选中</el-button>
          </div>
        </div>
        <el-empty v-if="!addressLoading && addresses.length === 0" description="暂无收货地址" :image-size="60" />
      </div>
    </el-drawer>

    <!-- 地址新增/编辑 -->
    <el-dialog v-model="addressEditVisible" :title="addressForm.addressId ? '编辑地址' : '新增地址'" width="520px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="收货人" required>
          <el-input v-model="addressForm.consignee" placeholder="收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="addressForm.mobile" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="地区ID">
          <el-input v-model="addressForm.regionIdsText" placeholder="省市区 regionId，英文逗号分隔，如 1,2,3" />
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="addressForm.address" type="textarea" :rows="2" placeholder="街道、门牌号等" />
        </el-form-item>
        <el-form-item label="邮编">
          <el-input v-model="addressForm.postcode" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="addressForm.email" />
        </el-form-item>
        <el-form-item label="是否默认">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAddress">保存</el-button>
      </template>
    </el-dialog>

    <!-- 优惠券抽屉（原版 CouponDrawer；后端契约仅 claim/updateCoupon，无 visible-list） -->
    <el-drawer v-model="couponVisible" title="选择优惠券" size="440px" destroy-on-close>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="契约中无代客下单优惠券「可领取列表」接口，后端未迁移；可输入优惠券 ID 领取并用于结算。"
      />
      <div class="coupon-manual">
        <el-input-number v-model="couponInputId" :min="1" placeholder="优惠券ID" style="width: 160px" />
        <el-button type="primary" size="small" :loading="submitting" @click="claimCoupon">领取优惠券</el-button>
        <el-button size="small" @click="applyCoupon">使用该券</el-button>
      </div>
      <div class="selected-coupons">
        已选优惠券：{{ checkForm.useCouponIds.length ? checkForm.useCouponIds.join('、') : '无' }}
        <el-button v-if="checkForm.useCouponIds.length" type="danger" link size="small" @click="clearCoupons">清空</el-button>
      </div>
      <template #footer>
        <el-button @click="couponVisible = false">关闭</el-button>
        <el-button type="primary" @click="updateCoupons">应用到结算</el-button>
      </template>
    </el-drawer>

    <!-- 自提点 / 自提联系人抽屉 -->
    <el-drawer v-model="pickupVisible" title="自提点与联系人" size="560px" destroy-on-close>
      <div class="panel-title">自提点（GET /proxyShopping/order/check/pickupList）</div>
      <el-table v-loading="pickupLoading" :data="pickupList" size="small" border max-height="220">
        <el-table-column prop="pickupName" label="自提点" min-width="160" />
        <el-table-column prop="address" label="地址" min-width="200" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="selectPickup(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="panel-title" style="margin-top: 12px">自提联系人</div>
      <div class="drawer-header-actions">
        <el-button type="primary" size="small" @click="openPickupContactEdit(null)">新增联系人</el-button>
        <el-button size="small" :loading="pickupContactLoading" @click="loadPickupContacts">刷新</el-button>
      </div>
      <el-table v-loading="pickupContactLoading" :data="pickupContacts" size="small" border max-height="200">
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="selectPickupContact(row)">选择</el-button>
            <el-button type="primary" link size="small" @click="openPickupContactEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="deletePickupContact(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <!-- 联系人新增/编辑 -->
    <el-dialog v-model="pickupContactEditVisible" :title="pickupContactForm.id ? '编辑联系人' : '新增联系人'" width="420px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="联系人" required>
          <el-input v-model="pickupContactForm.contactName" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="pickupContactForm.contactPhone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pickupContactEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPickupContact">保存</el-button>
      </template>
    </el-dialog>

    <!-- 改价抽屉（原版 AlterPriceDrawer） -->
    <el-drawer v-model="alterPriceVisible" title="订单改价" size="420px" destroy-on-close>
      <div class="alter-price">
        <div class="alter-row">
          <span class="alter-label">应付金额</span>
          <strong class="alter-amount">¥{{ money(cartTotal.productAmount) }}</strong>
        </div>
        <div class="alter-row">
          <span class="alter-label">改价方式</span>
          <el-radio-group v-model="alterPriceForm.changeType">
            <el-radio :value="1">一口价</el-radio>
            <el-radio :value="2">折扣</el-radio>
            <el-radio :value="3">减少</el-radio>
          </el-radio-group>
        </div>
        <div class="alter-row">
          <span class="alter-label">改价数值</span>
          <el-input-number v-model="alterPriceForm.changeValue" :min="0" :precision="2" :step="0.01" style="width: 180px" />
        </div>
        <div class="alter-row">
          <span class="alter-label">改价后金额</span>
          <strong class="alter-amount">¥{{ money(alterPriceForm.previewAmount) }}</strong>
        </div>
        <el-alert
          v-if="alterPriceForm.error"
          type="error"
          :closable="false"
          :title="alterPriceForm.error"
          show-icon
        />
        <div class="dialog-actions" style="margin-top: 16px">
          <el-button :loading="alterPriceForm.previewing" @click="previewAlterPrice">预览</el-button>
          <el-button type="primary" :loading="submitting" @click="submitAlterPrice">确认改价</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 配送选项（原版 DeliveryOptionsDrawer，后端未迁移返回真实错误） -->
    <el-dialog v-model="deliveryOptionsVisible" title="配送选项" width="560px" destroy-on-close>
      <el-alert
        v-if="deliveryOptionsError"
        type="error"
        :title="deliveryOptionsError"
        :closable="false"
        show-icon
      />
      <el-table v-else v-loading="deliveryOptionsLoading" :data="deliveryOptions" size="small" border>
        <el-table-column prop="deliveryTypeName" label="配送方式" width="140" />
        <el-table-column prop="deliveryType" label="类型" width="80" />
        <el-table-column label="商品数" width="90">
          <template #default="{ row }">{{ (row.deliveryProducts || []).length + (row.pickupProducts || []).length }}</template>
        </el-table-column>
      </el-table>
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
  buyerNote: '',
  useCouponIds: [] as number[],
  roundOffCents: false,
  roundOffDime: false,
  cashAmount: 0,
  cashChangeAmount: 0,
  needInvoice: false,
  invoiceType: 1,
  titleType: 1,
  invoiceTitle: '',
  taxNo: '',
  invoiceContent: ''
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
    useCouponIds: checkForm.useCouponIds.length ? [...checkForm.useCouponIds] : undefined,
    roundOffCents: checkForm.roundOffCents,
    roundOffDime: checkForm.roundOffDime,
    cashAmount: checkForm.cashAmount,
    cashChangeAmount: checkForm.cashChangeAmount,
    pickupId: pickedPickupId.value || undefined,
    pickupContactId: pickedPickupContactId.value || undefined,
    pickupContactName: pickedPickupContactName.value || undefined,
    pickupContactMobile: pickedPickupContactMobile.value || undefined,
    invoiceData: checkForm.needInvoice
      ? {
          invoiceType: checkForm.invoiceType,
          titleType: checkForm.titleType,
          title: checkForm.invoiceTitle,
          taxNo: checkForm.taxNo,
          content: checkForm.invoiceContent
        }
      : undefined,
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

// ————————————————— 其他 CheckPanel 子面板 —————————————————

// 地址簿
const addressVisible = ref(false);
const addressLoading = ref(false);
const addresses = ref<any[]>([]);
const addressEditVisible = ref(false);
const addressForm = reactive({
  addressId: 0,
  consignee: '',
  mobile: '',
  regionIdsText: '',
  address: '',
  postcode: '',
  email: '',
  isDefault: 0
});

async function loadAddresses() {
  if (!currentUser.value) {
    ElMessage.warning('请先选择会员');
    return;
  }
  addressLoading.value = true;
  try {
    const res = await request<any>({
      url: '/proxyShopping/address/list',
      method: 'post',
      data: { userId: currentUser.value.userId, page: 1, size: 50 }
    });
    addresses.value = res?.list ?? [];
  } finally {
    addressLoading.value = false;
  }
}

function openAddressBook() {
  if (!currentUser.value) {
    ElMessage.warning('请先选择会员');
    return;
  }
  addressVisible.value = true;
  loadAddresses();
}

function selectAddress(addr: any) {
  checkForm.addressId = addr.addressId;
  addressVisible.value = false;
}

async function setSelectedAddress(addr: any) {
  await request({
    url: '/proxyShopping/address/setSelected',
    method: 'post',
    data: { id: addr.addressId, userId: currentUser.value?.userId }
  });
  checkForm.addressId = addr.addressId;
  ElMessage.success('已设为选中地址');
  loadAddresses();
}

function openAddressEdit(addr: any | null) {
  addressForm.addressId = addr?.addressId ?? 0;
  addressForm.consignee = addr?.consignee ?? '';
  addressForm.mobile = addr?.mobile ?? '';
  addressForm.regionIdsText = Array.isArray(addr?.regionIds) ? addr.regionIds.join(',') : '';
  addressForm.address = addr?.address ?? '';
  addressForm.postcode = addr?.postcode ?? '';
  addressForm.email = addr?.email ?? '';
  addressForm.isDefault = Number(addr?.isDefault ?? 0);
  addressEditVisible.value = true;
}

async function submitAddress() {
  if (!currentUser.value) return;
  if (!addressForm.consignee.trim() || !addressForm.mobile.trim() || !addressForm.address.trim()) {
    ElMessage.warning('请填写收货人、手机号与详细地址');
    return;
  }
  submitting.value = true;
  try {
    const base = {
      userId: currentUser.value.userId,
      consignee: addressForm.consignee,
      mobile: addressForm.mobile,
      regionIds: addressForm.regionIdsText
        .split(',')
        .map((s) => Number(s.trim()))
        .filter((n) => n > 0),
      address: addressForm.address,
      postcode: addressForm.postcode || undefined,
      email: addressForm.email || undefined,
      isDefault: addressForm.isDefault
    };
    if (addressForm.addressId) {
      await request({
        url: '/proxyShopping/address/update',
        method: 'post',
        data: { ...base, addressId: addressForm.addressId }
      });
    } else {
      await request({ url: '/proxyShopping/address/create', method: 'post', data: base });
    }
    ElMessage.success('地址已保存');
    addressEditVisible.value = false;
    loadAddresses();
  } finally {
    submitting.value = false;
  }
}

// 优惠券
const couponVisible = ref(false);
const couponInputId = ref<number>(0);

function openCouponDrawer() {
  if (!currentUser.value) {
    ElMessage.warning('请先选择会员');
    return;
  }
  couponVisible.value = true;
}

async function claimCoupon() {
  if (!currentUser.value || !couponInputId.value) {
    ElMessage.warning('请输入优惠券 ID');
    return;
  }
  submitting.value = true;
  try {
    await request({
      url: '/proxyShopping/coupon/claim',
      method: 'post',
      data: { couponId: couponInputId.value, userId: currentUser.value.userId }
    });
    ElMessage.success('优惠券领取成功');
    if (!checkForm.useCouponIds.includes(couponInputId.value)) {
      checkForm.useCouponIds.push(couponInputId.value);
    }
  } finally {
    submitting.value = false;
  }
}

function applyCoupon() {
  if (!couponInputId.value) {
    ElMessage.warning('请输入优惠券 ID');
    return;
  }
  if (!checkForm.useCouponIds.includes(couponInputId.value)) {
    checkForm.useCouponIds.push(couponInputId.value);
  }
  ElMessage.success('已加入结算优惠券');
}

function clearCoupons() {
  checkForm.useCouponIds = [];
}

async function updateCoupons() {
  if (!currentUser.value) return;
  submitting.value = true;
  try {
    await request({
      url: '/proxyShopping/order/check/updateCoupon',
      method: 'post',
      data: {
        userId: currentUser.value.userId,
        flowType: 15,
        selectFrom: 3,
        useCouponIds: checkForm.useCouponIds
      }
    });
    ElMessage.success('优惠券已应用');
    couponVisible.value = false;
  } finally {
    submitting.value = false;
  }
}

// 自提点 / 联系人
const pickupVisible = ref(false);
const pickupLoading = ref(false);
const pickupList = ref<any[]>([]);
const pickupContactLoading = ref(false);
const pickupContacts = ref<any[]>([]);
const pickupContactEditVisible = ref(false);
const pickupContactForm = reactive({ id: 0, contactName: '', contactPhone: '' });
const pickedPickupId = ref(0);
const pickedPickupName = ref('');
const pickedPickupContactId = ref(0);
const pickedPickupContactName = ref('');
const pickedPickupContactMobile = ref('');

function openPickup() {
  if (!currentUser.value) {
    ElMessage.warning('请先选择会员');
    return;
  }
  pickupVisible.value = true;
  loadPickupList();
  loadPickupContacts();
}

async function loadPickupList() {
  pickupLoading.value = true;
  try {
    const res = await request<any>({
      url: '/proxyShopping/order/check/pickupList',
      method: 'get',
      params: { page: 1, size: 50, userId: currentUser.value?.userId }
    });
    pickupList.value = res?.list ?? [];
  } finally {
    pickupLoading.value = false;
  }
}

function selectPickup(row: any) {
  pickedPickupId.value = Number(row.pickupId ?? row.id ?? 0);
  pickedPickupName.value = row.pickupName || row.name || '';
  pickupVisible.value = false;
}

async function loadPickupContacts() {
  pickupContactLoading.value = true;
  try {
    const res = await request<any>({
      url: '/proxyShopping/pickupContact/listByUserId',
      method: 'get',
      params: { userId: currentUser.value?.userId }
    });
    pickupContacts.value = res?.list ?? res ?? [];
  } finally {
    pickupContactLoading.value = false;
  }
}

function openPickupContactEdit(row: any | null) {
  pickupContactForm.id = row?.id ?? 0;
  pickupContactForm.contactName = row?.contactName ?? '';
  pickupContactForm.contactPhone = row?.contactPhone ?? '';
  pickupContactEditVisible.value = true;
}

async function submitPickupContact() {
  if (!currentUser.value) return;
  if (!pickupContactForm.contactName.trim() || !pickupContactForm.contactPhone.trim()) {
    ElMessage.warning('请填写联系人姓名与电话');
    return;
  }
  submitting.value = true;
  try {
    if (pickupContactForm.id) {
      await request({
        url: '/proxyShopping/pickupContact/update',
        method: 'post',
        data: {
          id: pickupContactForm.id,
          userId: currentUser.value.userId,
          contactName: pickupContactForm.contactName,
          contactPhone: pickupContactForm.contactPhone
        }
      });
    } else {
      await request({
        url: '/proxyShopping/pickupContact/create',
        method: 'post',
        data: {
          userId: currentUser.value.userId,
          contactName: pickupContactForm.contactName,
          contactPhone: pickupContactForm.contactPhone
        }
      });
    }
    ElMessage.success('联系人已保存');
    pickupContactEditVisible.value = false;
    loadPickupContacts();
  } finally {
    submitting.value = false;
  }
}

async function deletePickupContact(row: any) {
  await request({
    url: '/proxyShopping/pickupContact/del',
    method: 'post',
    data: { id: row.id }
  });
  ElMessage.success('联系人已删除');
  loadPickupContacts();
}

function selectPickupContact(row: any) {
  pickedPickupContactId.value = Number(row.id ?? 0);
  pickedPickupContactName.value = row.contactName || '';
  pickedPickupContactMobile.value = row.contactPhone || '';
  pickupVisible.value = false;
}

// 改价
const alterPriceVisible = ref(false);
const alterPriceSummary = ref('');
const alterPriceForm = reactive({
  changeType: 1,
  changeValue: 0,
  previewAmount: 0,
  previewing: false,
  error: ''
});

function openAlterPrice() {
  if (!currentUser.value) {
    ElMessage.warning('请先选择会员');
    return;
  }
  alterPriceVisible.value = true;
}

async function previewAlterPrice() {
  alterPriceForm.previewing = true;
  alterPriceForm.error = '';
  try {
    const data = await request<any>({
      url: '/proxyShopping/order/check/changePriceShow',
      method: 'post',
      data: {
        userId: currentUser.value?.userId,
        flowType: 15,
        isCartList: false,
        onlyShow: true,
        useChangePrice: true,
        totalChangePriceType: alterPriceForm.changeType,
        changePriceValue: alterPriceForm.changeValue,
        originalPrice: Number(cartTotal.value.productAmount ?? 0)
      }
    });
    alterPriceForm.previewAmount = Number(
      data?.totalAmount ?? data?.payAmount ?? data?.total ?? alterPriceForm.changeValue
    );
    alterPriceSummary.value = `已改价 ¥${money(alterPriceForm.previewAmount)}`;
  } catch (err: any) {
    alterPriceForm.error = err?.message || '改价预览失败（后端未迁移结算引擎）';
  } finally {
    alterPriceForm.previewing = false;
  }
}

async function submitAlterPrice() {
  submitting.value = true;
  alterPriceForm.error = '';
  try {
    await request({
      url: '/proxyShopping/order/check/changePrice',
      method: 'post',
      data: {
        userId: currentUser.value?.userId,
        flowType: 15,
        isCartList: false,
        onlyShow: false,
        useChangePrice: true,
        totalChangePriceType: alterPriceForm.changeType,
        changePriceValue: alterPriceForm.changeValue,
        originalPrice: Number(cartTotal.value.productAmount ?? 0)
      }
    });
    ElMessage.success('改价已应用');
    alterPriceVisible.value = false;
  } catch (err: any) {
    alterPriceForm.error = err?.message || '改价失败（后端未迁移结算引擎）';
  } finally {
    submitting.value = false;
  }
}

// 配送选项
const deliveryOptionsVisible = ref(false);
const deliveryOptionsLoading = ref(false);
const deliveryOptions = ref<any[]>([]);
const deliveryOptionsError = ref('');

async function openDeliveryOptions() {
  deliveryOptionsVisible.value = true;
  deliveryOptionsError.value = '';
  deliveryOptionsLoading.value = true;
  try {
    const res = await request<any>({
      url: '/proxyShopping/delivery/getDeliveryOptions',
      method: 'get',
      params: { userId: currentUser.value?.userId }
    });
    deliveryOptions.value = res?.list ?? res ?? [];
  } catch (err: any) {
    deliveryOptionsError.value =
      err?.message ||
      '代客下单配送方式依赖 O2O 配送引擎，Rust 侧尚未迁移，本接口暂不可用';
  } finally {
    deliveryOptionsLoading.value = false;
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
