<template>
  <div class="tig-cart-page container">
    <div class="cart-title">
      <h1>我的购物车</h1>
      <span class="cart-sub">共 {{ cart.totalQuantity }} 件商品</span>
    </div>

    <div v-loading="cart.loading" class="cart-body">
      <el-empty v-if="!cart.items.length && !cart.loading" description="购物车还是空的，去挑点好物吧">
        <el-button type="primary" @click="$router.push('/search')">去逛逛</el-button>
      </el-empty>

      <template v-else>
        <div class="cart-table">
          <div class="cart-head">
            <div class="col-check">
              <el-checkbox :model-value="cart.allChecked" @change="onToggleAll">全选</el-checkbox>
            </div>
            <div class="col-product">商品信息</div>
            <div class="col-price">单价</div>
            <div class="col-qty">数量</div>
            <div class="col-subtotal">小计</div>
            <div class="col-op">操作</div>
          </div>

          <div v-for="item in cart.items" :key="item.cartId" class="cart-row">
            <div class="col-check">
              <el-checkbox :model-value="item.checked" @change="(val: boolean) => onToggle(item, val)" />
            </div>
            <div class="col-product">
              <div class="product-cell">
                <img
                  class="product-thumb"
                  :src="imageUrl(item.picThumb)"
                  :alt="item.productName"
                  @click="$router.push(`/product/${item.productId}`)"
                />
                <div class="product-meta">
                  <router-link :to="`/product/${item.productId}`" class="product-name">
                    {{ item.productName }}
                  </router-link>
                  <div v-if="item.skuData" class="sku-text">{{ item.skuData }}</div>
                  <div v-if="!item.stockEnough" class="stock-warn">库存不足，请调整数量</div>
                </div>
              </div>
            </div>
            <div class="col-price">¥{{ formatPrice(item.price) }}</div>
            <div class="col-qty">
              <el-input-number
                :model-value="item.quantity"
                :min="1"
                size="small"
                @change="(val: number | undefined) => onQuantityChange(item, val)"
              />
            </div>
            <div class="col-subtotal">¥{{ formatPrice(item.subtotal) }}</div>
            <div class="col-op">
              <el-button link type="danger" @click="onRemove(item)">删除</el-button>
            </div>
          </div>
        </div>

        <div class="cart-footer flex-between">
          <div class="footer-left">
            <el-button link @click="onClear">清空购物车</el-button>
          </div>
          <div class="footer-right flex-align-center">
            <div class="amount-info">
              <span>已选 <strong>{{ cart.checkedCount }}</strong> 件</span>
              <span class="amount-label">合计（不含运费）：</span>
              <span class="amount-value">¥{{ formatPrice(cart.checkedAmount) }}</span>
            </div>
            <el-button type="primary" size="large" :disabled="cart.checkedCount === 0" @click="goCheckout">
              去结算
            </el-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { CartItem } from '@/api/types';
import { useCartStore } from '@/stores/cart';
import { useUserStore } from '@/stores/user';
import { applySeo } from '@/composables/useSeo';
import { formatPrice, imageUrl } from '@/utils/format';

const router = useRouter();
const user = useUserStore();
const cart = useCartStore();

function goCheckout() {
  if (cart.checkedCount === 0) {
    ElMessage.warning('请先勾选需要结算的商品');
    return;
  }
  // 结算页按购物车勾选项装配数据，不在此处伪造结果
  router.push('/checkout');
}

async function onToggleAll(checked: boolean) {
  await cart.toggleAll(user.token, checked);
}

async function onToggle(item: CartItem, checked: boolean) {
  await cart.toggle(user.token, item.cartId, checked);
}

async function onQuantityChange(item: CartItem, value: number | undefined) {
  const quantity = Number(value ?? item.quantity);
  if (quantity < 1) return;
  await cart.updateQuantity(user.token, item.cartId, quantity);
}

async function onRemove(item: CartItem) {
  await ElMessageBox.confirm(`确定从购物车删除「${item.productName}」？`, '删除商品', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  });
  await cart.remove(user.token, [item.cartId]);
  ElMessage.success('已删除');
}

async function onClear() {
  await ElMessageBox.confirm('确定清空购物车中的全部商品？', '清空购物车', {
    type: 'warning',
    confirmButtonText: '清空',
    cancelButtonText: '取消',
  });
  await cart.clear(user.token);
  ElMessage.success('购物车已清空');
}

onMounted(async () => {
  applySeo({ title: '我的购物车', noindex: true });
  await cart.fetchSummary(user.token);
});
</script>

<style scoped lang="less">
.tig-cart-page {
  padding: 20px 0 50px;
}

.cart-title {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 16px;

  h1 { font-size: 22px; font-weight: 700; }
  .cart-sub { color: #999; font-size: 13px; }
}

.cart-body {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  padding: 16px;
  min-height: 260px;
}

.cart-head,
.cart-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
}

.cart-head {
  color: #888;
  font-size: 13px;
  border-bottom: 1px solid #f0f0f0;
}

.cart-row {
  border-bottom: 1px solid #f7f7f7;
  &:last-child { border-bottom: none; }
}

.col-check { width: 60px; flex: 0 0 60px; }
.col-product { flex: 1; min-width: 0; }
.col-price,
.col-subtotal { width: 120px; flex: 0 0 120px; text-align: center; }
.col-qty { width: 140px; flex: 0 0 140px; text-align: center; }
.col-op { width: 80px; flex: 0 0 80px; text-align: center; }

.product-cell {
  display: flex;
  gap: 12px;
  align-items: center;

  .product-thumb {
    width: 72px;
    height: 72px;
    object-fit: cover;
    border-radius: 6px;
    border: 1px solid #f0f0f0;
    cursor: pointer;
  }

  .product-meta {
    min-width: 0;

    .product-name {
      font-size: 14px;
      color: var(--tig-text-main);
      display: block;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 480px;
      &:hover { color: var(--tig-red); }
    }

    .sku-text { color: #999; font-size: 12px; margin-top: 4px; }
    .stock-warn { color: var(--tig-red); font-size: 12px; margin-top: 4px; }
  }
}

.col-subtotal { color: var(--tig-red); font-weight: 700; }

.cart-footer {
  border-top: 1px solid #f0f0f0;
  margin-top: 10px;
  padding-top: 14px;

  .footer-right { gap: 20px; }

  .amount-info {
    display: flex;
    align-items: baseline;
    gap: 10px;
    font-size: 13px;
    color: #666;

    .amount-label { color: #888; }
    .amount-value { color: var(--tig-red); font-size: 24px; font-weight: 800; }
  }
}
</style>
