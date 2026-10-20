<template>
  <div class="tig-member-page container">
    <div class="member-layout flex-between">
      <!-- Left Sidebar -->
      <aside class="member-sidebar">
        <div class="user-card-brief">
          <div class="avatar-circle">
            {{ (userInfo?.nickname || userInfo?.username || 'U').slice(0, 1).toUpperCase() }}
          </div>
          <div class="name-text">{{ userInfo?.nickname || userInfo?.username || '商城会员' }}</div>
          <el-tag type="danger" size="small">{{ userInfo?.rankName || '普通会员' }}</el-tag>
        </div>

        <div class="menu-group">
          <div class="group-title">订单中心</div>
          <ul>
            <li class="active"><a href="javascript:;">我的订单</a></li>
            <li><a href="javascript:;">评价晒单</a></li>
            <li><a href="javascript:;">售后退款</a></li>
          </ul>
        </div>

        <div class="menu-group">
          <div class="group-title">资产与账户</div>
          <ul>
            <li><a href="javascript:;">账户余额 (¥{{ Number(userInfo?.balance || 0).toFixed(2) }})</a></li>
            <li><a href="javascript:;">积分明细 ({{ userInfo?.points || 0 }})</a></li>
            <li><a href="javascript:;">收货地址管理</a></li>
          </ul>
        </div>
      </aside>

      <!-- Right Content -->
      <main class="member-main">
        <div class="member-panel-card">
          <div class="panel-title">我的商城订单</div>

          <div class="order-tabs">
            <span :class="{ active: currentTab === 'all' }" @click="switchTab('all')">全部订单</span>
            <span :class="{ active: currentTab === 'unpaid' }" @click="switchTab('unpaid')">待付款</span>
            <span :class="{ active: currentTab === 'shipping' }" @click="switchTab('shipping')">待收货</span>
            <span :class="{ active: currentTab === 'completed' }" @click="switchTab('completed')">已完成</span>
          </div>

          <!-- Loading Skeleton -->
          <div v-if="loading" style="padding: 30px 0;">
            <el-skeleton :rows="6" animated />
          </div>

          <!-- Empty Orders -->
          <div v-else-if="orders.length === 0" class="empty-orders-box">
            <el-empty description="暂无相关订单" :image-size="100">
              <el-button type="primary" @click="$router.push('/search')">去选购商品</el-button>
            </el-empty>
          </div>

          <!-- Order List -->
          <div v-else class="orders-list">
            <div v-for="order in orders" :key="order.orderId" class="order-box">
              <div class="order-box-header flex-between">
                <div class="order-meta">
                  <span class="order-time">{{ order.addTime || '近期订单' }}</span>
                  <span class="order-sn">订单号：{{ order.orderSn }}</span>
                </div>
                <div class="order-status-tag">{{ getOrderStatusText(order.orderStatus) }}</div>
              </div>

              <div class="order-box-body flex-between">
                <div class="goods-info flex-align-center">
                  <img :src="order.picThumb || '/default-goods.png'" />
                  <div class="goods-details">
                    <div class="goods-title">{{ order.productName || `订单 #${order.orderSn}` }}</div>
                    <div class="goods-spec">共 {{ order.totalQuantity || 1 }} 件商品</div>
                  </div>
                </div>

                <div class="order-price">
                  <div class="total-amount">¥{{ Number(order.totalAmount || order.orderAmount || 0).toFixed(2) }}</div>
                  <div class="pay-type">{{ order.payName || '在线支付' }}</div>
                </div>

                <div class="order-actions">
                  <button v-if="order.orderStatus === 0" class="btn-pay" @click="handlePay(order.orderId)">
                    立即付款
                  </button>
                  <button v-if="order.orderStatus === 0" class="btn-cancel" @click="cancelOrder(order.orderId)">
                    取消订单
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';

const loading = ref(true);
const currentTab = ref('all');
const userInfo = ref<any>(null);
const orders = ref<any[]>([]);

function getOrderStatusText(status: number) {
  switch (status) {
    case 0: return '待付款';
    case 1: return '待发货';
    case 2: return '配送中';
    case 3: return '已完成';
    case 4: return '已取消';
    default: return '处理中';
  }
}

async function fetchUserProfile() {
  try {
    const res: any = await request({
      url: '/user/user/detail',
      method: 'get'
    });
    userInfo.value = res;
  } catch (err) {
    console.error('获取用户信息失败:', err);
  }
}

async function fetchOrders() {
  loading.value = true;
  try {
    const params: Record<string, any> = {
      page: 1,
      pageSize: 20
    };

    if (currentTab.value === 'unpaid') params.order_status = 0;
    else if (currentTab.value === 'shipping') params.order_status = 2;
    else if (currentTab.value === 'completed') params.order_status = 3;

    const res: any = await request({
      url: '/user/order/list',
      method: 'get',
      params
    });

    if (res && Array.isArray(res.list)) {
      orders.value = res.list;
    } else {
      orders.value = [];
    }
  } catch (err: any) {
    console.error('获取订单列表失败:', err);
    orders.value = [];
  } finally {
    loading.value = false;
  }
}

function switchTab(tab: string) {
  currentTab.value = tab;
  fetchOrders();
}

async function cancelOrder(orderId: number) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      type: 'warning'
    });
    await request({
      url: '/user/order/cancelOrder',
      method: 'post',
      data: { orderId }
    });
    ElMessage.success('订单已成功取消');
    fetchOrders();
  } catch (err) {
    // 忽略取消
  }
}

function handlePay(orderId: number) {
  ElMessage.info(`正在唤起订单 #${orderId} 支付通道...`);
}

onMounted(() => {
  fetchUserProfile();
  fetchOrders();
});
</script>

<style scoped lang="less">
.tig-member-page {
  padding: 24px 0 50px;
}

.member-sidebar {
  width: 240px;
  background: #ffffff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  padding: 24px;
  margin-right: 20px;

  .user-card-brief {
    text-align: center;
    border-bottom: 1px solid #f0f0f0;
    padding-bottom: 20px;
    margin-bottom: 20px;

    .avatar-circle {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background: var(--tig-red);
      color: #fff;
      font-size: 24px;
      line-height: 60px;
      margin: 0 auto 10px;
      font-weight: bold;
    }

    .name-text {
      font-size: 15px;
      font-weight: bold;
      color: #333;
      margin-bottom: 8px;
    }
  }

  .menu-group {
    margin-bottom: 20px;

    .group-title {
      font-size: 14px;
      font-weight: bold;
      color: #333;
      margin-bottom: 10px;
    }

    ul {
      list-style: none;
      padding: 0;
      margin: 0;

      li {
        padding: 8px 12px;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.2s;

        a {
          color: #666;
          font-size: 13px;
        }

        &:hover, &.active {
          background: #fff3f3;
          a { color: var(--tig-red); font-weight: bold; }
        }
      }
    }
  }
}

.member-main {
  flex: 1;

  .member-panel-card {
    background: #ffffff;
    border: 1px solid var(--tig-border-color);
    border-radius: 8px;
    padding: 24px 30px;

    .panel-title {
      font-size: 18px;
      font-weight: 700;
      color: #333;
      margin-bottom: 20px;
    }

    .order-tabs {
      display: flex;
      gap: 30px;
      border-bottom: 1px solid #eee;
      padding-bottom: 12px;
      margin-bottom: 20px;

      span {
        font-size: 14px;
        color: #666;
        cursor: pointer;
        position: relative;
        padding-bottom: 12px;

        &.active {
          color: var(--tig-red);
          font-weight: bold;

          &::after {
            content: '';
            position: absolute;
            bottom: -1px;
            left: 0;
            right: 0;
            height: 2px;
            background: var(--tig-red);
          }
        }
      }
    }

    .empty-orders-box {
      padding: 60px 0;
    }

    .orders-list {
      .order-box {
        border: 1px solid #ebebeb;
        border-radius: 6px;
        margin-bottom: 16px;
        overflow: hidden;

        .order-box-header {
          background: #fbfbfb;
          padding: 10px 16px;
          font-size: 12px;
          color: #777;
          border-bottom: 1px solid #ebebeb;

          .order-time { margin-right: 20px; }
          .order-status-tag { color: var(--tig-red); font-weight: bold; }
        }

        .order-box-body {
          padding: 16px;

          .goods-info {
            width: 50%;
            img {
              width: 60px;
              height: 60px;
              border: 1px solid #eee;
              border-radius: 4px;
              margin-right: 14px;
              object-fit: cover;
            }
            .goods-title {
              font-size: 13px;
              color: #333;
              margin-bottom: 4px;
            }
            .goods-spec {
              font-size: 12px;
              color: #999;
            }
          }

          .order-price {
            text-align: center;
            .total-amount {
              font-size: 15px;
              font-weight: bold;
              color: #333;
              margin-bottom: 4px;
            }
            .pay-type {
              font-size: 12px;
              color: #999;
            }
          }

          .order-actions {
            display: flex;
            flex-direction: column;
            gap: 8px;

            button {
              padding: 6px 14px;
              border-radius: 4px;
              font-size: 12px;
              cursor: pointer;
            }

            .btn-pay {
              background: var(--tig-red);
              color: #fff;
              border: none;
              &:hover { background: #c81c13; }
            }

            .btn-cancel {
              background: #fff;
              color: #666;
              border: 1px solid #ccc;
              &:hover { color: var(--tig-red); border-color: var(--tig-red); }
            }
          }
        }
      }
    }
  }
}
</style>
