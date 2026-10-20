<template>
  <div class="tig-cart-page container">
    <div class="cart-title">
      全部商品 ({{ cartSummary?.totalQuantity || 0 }})
    </div>

    <!-- Loading Skeleton -->
    <div v-if="loading" style="padding: 40px 0;">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- Empty State -->
    <div v-else-if="!cartSummary?.items || cartSummary.items.length === 0" class="empty-cart-card">
      <el-empty description="购物车空空如也，去挑选心仪的商品吧" :image-size="140">
        <el-button type="primary" size="large" @click="$router.push('/search')">去选购商品</el-button>
      </el-empty>
    </div>

    <!-- Cart Table -->
    <template v-else>
      <div class="cart-table-card">
        <div class="cart-header flex-between">
          <div class="th-select flex-align-center">
            <el-checkbox :model-value="allChecked" @change="toggleSelectAll">全选</el-checkbox>
            <span style="margin-left: 20px;">商品信息</span>
          </div>
          <div class="th-unit-price">单价</div>
          <div class="th-quantity">数量</div>
          <div class="th-total">小计</div>
          <div class="th-action">操作</div>
        </div>

        <!-- Item Rows -->
        <div v-for="item in cartSummary.items" :key="item.cartId" class="cart-item-row flex-between">
          <div class="item-product flex-align-center">
            <el-checkbox :model-value="item.checked" @change="(val: any) => updateCheck(item.cartId, Boolean(val))" />
            <img :src="item.picThumb || '/default-goods.png'" class="item-img" />
            <div class="item-details">
              <router-link :to="`/product/${item.productId}`" class="item-title" :title="item.productName">
                {{ item.productName }}
              </router-link>
              <div v-if="item.skuData" class="item-spec">{{ item.skuData }}</div>
            </div>
          </div>

          <div class="item-price">¥{{ Number(item.price).toFixed(2) }}</div>

          <div class="item-quantity">
            <el-input-number
              :model-value="item.quantity"
              :min="1"
              :max="99"
              size="small"
              @change="(val: any) => updateQuantity(item.cartId, Number(val))"
            />
          </div>

          <div class="item-subtotal">¥{{ Number(item.subtotal).toFixed(2) }}</div>

          <div class="item-action">
            <a href="javascript:;" @click="removeItem(item.cartId)">删除</a>
          </div>
        </div>
      </div>

      <!-- Bottom Checkout Bar -->
      <div class="cart-bottom-bar flex-between">
        <div class="bar-left flex-align-center">
          <el-checkbox :model-value="allChecked" @change="toggleSelectAll">全选</el-checkbox>
          <a href="javascript:;" class="batch-del" @click="clearAllCart">清空购物车</a>
        </div>

        <div class="bar-right flex-align-center">
          <div class="selected-text">
            已选择 <strong>{{ cartSummary.checkedQuantity }}</strong> 件商品
          </div>
          <div class="total-price-box">
            <span>总价 (不含运费)：</span>
            <span class="price-val">¥{{ Number(cartSummary.checkedAmount).toFixed(2) }}</span>
          </div>
          <button class="checkout-btn" :disabled="cartSummary.checkedQuantity === 0" @click="handleCheckout">
            去结算
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';

const router = useRouter();
const loading = ref(true);
const cartSummary = ref<any>(null);

const allChecked = computed(() => {
  if (!cartSummary.value?.items || cartSummary.value.items.length === 0) return false;
  return cartSummary.value.items.every((it: any) => it.checked);
});

async function fetchCartList() {
  loading.value = true;
  try {
    const res: any = await request({
      url: '/cart/cart/list',
      method: 'get'
    });
    cartSummary.value = res;
  } catch (err: any) {
    console.error('获取购物车列表失败:', err);
    cartSummary.value = null;
  } finally {
    loading.value = false;
  }
}

async function updateQuantity(cartId: number, quantity: number) {
  try {
    await request({
      url: '/cart/cart/updateItem',
      method: 'post',
      data: { cartId, quantity }
    });
    fetchCartList();
  } catch (err) {
    // 错误处理由拦截器捕获
  }
}

async function updateCheck(cartId: number, checked: boolean) {
  try {
    await request({
      url: '/cart/cart/updateCheck',
      method: 'post',
      data: { cartId, checked }
    });
    fetchCartList();
  } catch (err) {
    // 拦截器处理
  }
}

async function toggleSelectAll(val: any) {
  const target = Boolean(val);
  if (!cartSummary.value?.items) return;
  for (const item of cartSummary.value.items) {
    if (item.checked !== target) {
      await updateCheck(item.cartId, target);
    }
  }
}

async function removeItem(cartId: number) {
  try {
    await ElMessageBox.confirm('确定要从购物车中删除该商品吗？', '提示', {
      type: 'warning'
    });
    await request({
      url: '/cart/cart/removeItem',
      method: 'post',
      data: { cartIds: [cartId] }
    });
    ElMessage.success('已删除');
    fetchCartList();
  } catch (err) {
    // 用户取消或请求失败
  }
}

async function clearAllCart() {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '警告', {
      type: 'warning'
    });
    await request({
      url: '/cart/cart/clear',
      method: 'post'
    });
    ElMessage.success('购物车已清空');
    fetchCartList();
  } catch (err) {
    // 用户取消
  }
}

function handleCheckout() {
  if (!cartSummary.value || cartSummary.value.checkedQuantity === 0) {
    ElMessage.warning('请先勾选需要结算的商品');
    return;
  }
  ElMessage.info('正在前往收银结算台...');
  router.push('/member');
}

onMounted(() => {
  fetchCartList();
});
</script>

<style scoped lang="less">
.tig-cart-page {
  padding: 24px 0 50px;
}

.cart-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 20px;
}

.empty-cart-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid var(--tig-border-color);
  padding: 80px 0;
}

.cart-table-card {
  background: #ffffff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;

  .cart-header {
    background: #f8f9fa;
    padding: 12px 24px;
    font-size: 13px;
    color: #666;
    border-bottom: 1px solid #eee;

    .th-select { width: 40%; }
    .th-unit-price { width: 15%; text-align: center; }
    .th-quantity { width: 15%; text-align: center; }
    .th-total { width: 15%; text-align: center; }
    .th-action { width: 15%; text-align: center; }
  }

  .cart-item-row {
    padding: 20px 24px;
    border-bottom: 1px solid #f2f2f2;
    &:last-child { border-bottom: none; }

    .item-product {
      width: 40%;
      .item-img {
        width: 80px;
        height: 80px;
        border: 1px solid #eee;
        border-radius: 4px;
        margin: 0 16px;
        object-fit: cover;
      }
      .item-details {
        flex: 1;
        .item-title {
          font-size: 13px;
          color: #333;
          line-height: 1.5;
          margin-bottom: 6px;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
          &:hover { color: var(--tig-red); }
        }
        .item-spec {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .item-price {
      width: 15%;
      text-align: center;
      font-size: 14px;
      color: #333;
    }

    .item-quantity {
      width: 15%;
      display: flex;
      justify-content: center;
    }

    .item-subtotal {
      width: 15%;
      text-align: center;
      font-size: 15px;
      font-weight: bold;
      color: var(--tig-red);
    }

    .item-action {
      width: 15%;
      text-align: center;
      a {
        color: #999;
        font-size: 13px;
        &:hover { color: var(--tig-red); }
      }
    }
  }
}

.cart-bottom-bar {
  background: #ffffff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  height: 60px;
  padding: 0 24px;

  .bar-left {
    gap: 20px;
    .batch-del {
      font-size: 13px;
      color: #666;
      &:hover { color: var(--tig-red); }
    }
  }

  .bar-right {
    gap: 24px;

    .selected-text {
      font-size: 13px;
      color: #666;
      strong { color: var(--tig-red); font-size: 16px; }
    }

    .total-price-box {
      font-size: 14px;
      color: #333;
      .price-val {
        color: var(--tig-red);
        font-size: 22px;
        font-weight: bold;
      }
    }

    .checkout-btn {
      background: var(--tig-red);
      color: #fff;
      border: none;
      height: 44px;
      padding: 0 36px;
      font-size: 16px;
      font-weight: bold;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover:not(:disabled) { background: #c81c13; }
      &:disabled {
        background: #ccc;
        cursor: not-allowed;
      }
    }
  }
}
</style>
