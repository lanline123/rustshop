<template>
  <div class="tig-aftersales-page container">
    <div class="page-head flex-between">
      <h1 class="page-title">售后服务</h1>
      <router-link to="/member" class="back-link">返回个人中心</router-link>
    </div>

    <div class="tip-bar">
      如需申请售后，请在「我的订单」中选择需要售后的订单并点击「申请售后」。
    </div>

    <div v-loading="loading" class="aftersales-body">
      <el-empty v-if="!records.length && !loading" description="暂无售后记录">
        <el-button type="primary" @click="$router.push('/member/orders')">查看我的订单</el-button>
      </el-empty>

      <div v-for="record in records" :key="record.orderId" class="order-card">
        <div class="order-head flex-between">
          <div class="head-left">
            <span class="order-sn">订单号：{{ record.orderSn }}</span>
            <span v-if="record.shippingTime" class="shipping-time">发货时间：{{ record.shippingTime }}</span>
          </div>
          <el-button link type="primary" @click="$router.push(`/member/orders/${record.orderId}`)">订单详情</el-button>
        </div>

        <div class="order-items">
          <div v-for="(item, idx) in record.items" :key="item.itemId ?? idx" class="item-row flex-between">
            <div class="item-left">
              <img :src="imageUrl(item.picThumb || item.productPicThumb)" :alt="item.productName" class="thumb" />
              <div class="item-meta">
                <div class="name">{{ item.productName }}</div>
                <div class="sku">{{ skuText(item) }}</div>
              </div>
            </div>
            <div class="item-right">
              <span class="price">¥{{ formatPrice(item.price) }} × {{ item.quantity }}</span>
              <el-button
                v-if="aftersalesIdOf(item)"
                size="small"
                @click="openDetail(aftersalesIdOf(item)!)"
              >
                查看售后
              </el-button>
              <el-tag v-else size="small" type="info">未申请售后</el-tag>
            </div>
          </div>
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="onPageChange"
        />
      </div>
    </div>

    <!-- 售后详情 -->
    <el-dialog v-model="detailVisible" title="售后详情" width="640px">
      <div v-loading="detailLoading">
        <template v-if="detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="售后单号">{{ detail.aftersalesSn || '-' }}</el-descriptions-item>
            <el-descriptions-item label="售后状态">
              <el-tag>{{ detail.statusName || detail.status }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="售后类型">{{ detail.aftersalesTypeName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ detail.addTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="退款金额">¥{{ formatPrice(detail.refundAmount) }}</el-descriptions-item>
            <el-descriptions-item label="售后原因">{{ detail.aftersaleReason || '-' }}</el-descriptions-item>
            <el-descriptions-item label="问题描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.returnAddress" label="退货地址" :span="2">
              {{ detail.returnAddress }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detail.trackingNo" label="物流单号" :span="2">
              {{ detail.logisticsName }} {{ detail.trackingNo }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detail.reply" label="商家回复" :span="2">{{ detail.reply }}</el-descriptions-item>
          </el-descriptions>

          <div v-if="detail.aftersalesItems?.length" class="detail-block">
            <h3>售后商品</h3>
            <div v-for="(item, idx) in detail.aftersalesItems" :key="idx" class="item-row flex-between">
              <div class="item-left">
                <img :src="imageUrl(item.picThumb)" :alt="item.productName" class="thumb" />
                <div class="item-meta">
                  <div class="name">{{ item.productName }}</div>
                  <div class="sku">{{ item.skuValue || '' }}</div>
                </div>
              </div>
              <span class="price">¥{{ formatPrice(item.price) }} × {{ item.quantity }}</span>
            </div>
          </div>

          <div v-if="detail.aftersalesLog?.length" class="detail-block">
            <h3>处理进度</h3>
            <el-timeline>
              <el-timeline-item v-for="(log, idx) in detail.aftersalesLog" :key="idx" :timestamp="log.addTime">
                {{ log.logInfo || log.log_info || '-' }}
              </el-timeline-item>
            </el-timeline>
          </div>
        </template>
        <el-empty v-else-if="!detailLoading" description="暂无售后详情" :image-size="80" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { aftersaleApi } from '@/api';
import { pageItems } from '@/api/types';
import type { AftersaleDetail, AftersaleOrder } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { formatPrice, imageUrl } from '@/utils/format';

const loading = ref(false);
const records = ref<AftersaleOrder[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);

const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<AftersaleDetail | null>(null);

function skuText(item: Record<string, any>): string {
  return item.skuValue || item.skuData || '';
}

/** 列表项 → 售后单 ID（`aftersalesItem.aftersaleId`）。 */
function aftersalesIdOf(item: Record<string, any>): number | null {
  const aftersalesItem = item.aftersalesItem;
  const id = aftersalesItem?.aftersaleId ?? aftersalesItem?.aftersale_id;
  return typeof id === 'number' && id > 0 ? id : null;
}

async function load() {
  loading.value = true;
  try {
    const result = await aftersaleApi.list({ page: page.value, size: pageSize.value });
    records.value = pageItems(result);
    total.value = Number(result?.total ?? 0) || 0;
  } catch {
    records.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function onPageChange(next: number) {
  page.value = next;
  load();
}

async function openDetail(id: number) {
  detailVisible.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    detail.value = await aftersaleApi.detail(id);
  } catch {
    detail.value = null;
  } finally {
    detailLoading.value = false;
  }
}

onMounted(() => {
  applySeo({ title: '售后服务', noindex: true });
  load();
});
</script>

<style scoped lang="less">
.tig-aftersales-page {
  padding: 20px 0 50px;
}

.page-head {
  margin-bottom: 12px;
  .page-title { font-size: 22px; font-weight: 700; }
  .back-link { color: #888; font-size: 13px; &:hover { color: var(--tig-red); } }
}

.tip-bar {
  background: #fdf6ec;
  border: 1px solid #f5e1bf;
  color: #a06b13;
  border-radius: 6px;
  padding: 10px 14px;
  font-size: 13px;
  margin-bottom: 14px;
}

.aftersales-body { min-height: 240px; }

.order-card {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  margin-bottom: 14px;
  overflow: hidden;

  .order-head {
    background: #fafafa;
    padding: 10px 16px;
    border-bottom: 1px solid #f0f0f0;
    font-size: 13px;
    color: #666;

    .head-left { display: flex; gap: 18px; }
    .shipping-time { color: #999; }
  }
}

.order-items { padding: 4px 16px; }

.item-row {
  padding: 10px 0;
  border-bottom: 1px solid #f7f7f7;
  &:last-child { border-bottom: none; }

  .item-left {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;

    .thumb { width: 56px; height: 56px; object-fit: cover; border-radius: 4px; border: 1px solid #f0f0f0; }
    .item-meta {
      .name { font-size: 13px; }
      .sku { color: #999; font-size: 12px; margin-top: 3px; }
    }
  }

  .item-right { display: flex; align-items: center; gap: 14px; }
  .price { font-size: 13px; color: #666; }
}

.detail-block {
  margin-top: 18px;

  h3 { font-size: 14px; font-weight: 700; margin-bottom: 10px; }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
