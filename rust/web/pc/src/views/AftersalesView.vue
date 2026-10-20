<template>
  <div class="tig-aftersales-page">
    <div class="container member-layout">
      <MemberNav />

      <div class="info-row">
        <!-- 顶部切换：售后申请 / 申请记录 -->
        <div class="member-top-tabs">
          <router-link to="/member/aftersales" class="top-tab" :class="{ active: activeTab === 'apply' }" @click="switchTab('apply')">
            售后申请
          </router-link>
          <router-link to="/member/aftersales" class="top-tab" :class="{ active: activeTab === 'record' }" @click="switchTab('record')">
            申请记录
          </router-link>
        </div>

        <!-- 售后申请（可申请退换货的订单） -->
        <div v-if="activeTab === 'apply'" v-loading="loadingApply" class="content-card">
          <template v-if="applyList.length">
            <table class="custom-table">
              <thead class="table-header">
                <tr>
                  <th class="header-cell" style="width: 200px">订单编号</th>
                  <th class="header-cell">订单商品</th>
                  <th class="header-cell" style="width: 140px">发货时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="record in applyList" :key="record.orderId">
                  <td class="order-sn-cell">{{ record.orderSn }}</td>
                  <td>
                    <div class="product-list">
                      <div v-for="(item, index) in record.items" :key="item.itemId ?? index" class="product-line">
                        <img :src="imageUrl(item.productPicThumb || item.picThumb)" :alt="item.productName || ''" class="img" />
                        <div class="product-meta">
                          <div class="name">{{ item.productName }}</div>
                          <div v-if="item.aftersalesItem?.aftersalesSn" class="sn">
                            售后单号：{{ item.aftersalesItem.aftersalesSn }}
                          </div>
                        </div>
                        <div class="product-actions">
                          <el-button
                            v-if="item.toAftersalses"
                            type="primary"
                            size="small"
                            @click="goApply(record.orderId, item.itemId)"
                          >
                            申请
                          </el-button>
                          <el-button
                            v-if="item.aftersaleFlag !== 1 && item.aftersalesItem?.aftersaleId"
                            size="small"
                            @click="openDetail(item.aftersalesItem.aftersaleId)"
                          >
                            详情
                          </el-button>
                          <el-tag v-if="!item.toAftersalses && !item.aftersalesItem?.aftersaleId" size="small" type="info">
                            不可申请
                          </el-tag>
                        </div>
                      </div>
                    </div>
                  </td>
                  <td>{{ record.shippingTime || '未发货' }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="applyTotal > pageSize" class="pagination-wrap">
              <el-pagination
                :current-page="applyPage"
                :page-size="pageSize"
                :total="applyTotal"
                layout="prev, pager, next"
                background
                @current-change="onApplyPageChange"
              />
            </div>
          </template>
          <el-empty v-else-if="!loadingApply" description="暂无可申请退换货订单" />
        </div>

        <!-- 申请记录 -->
        <div v-else v-loading="loadingRecord" class="content-card">
          <template v-if="recordList.length">
            <table class="custom-table">
              <thead class="table-header">
                <tr>
                  <th class="header-cell" style="width: 180px">售后单号</th>
                  <th class="header-cell" style="width: 180px">订单编号</th>
                  <th class="header-cell" style="width: 110px">售后类型</th>
                  <th class="header-cell" style="width: 110px">售后状态</th>
                  <th class="header-cell" style="width: 120px">退款金额</th>
                  <th class="header-cell" style="width: 160px">申请时间</th>
                  <th class="header-cell" style="width: 100px">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="record in recordList" :key="record.aftersaleId">
                  <td>{{ record.aftersalesSn || record.aftersaleId }}</td>
                  <td>{{ record.orderSn || record.orderId || '-' }}</td>
                  <td>{{ aftersaleTypeText(record.aftersaleType) }}</td>
                  <td>
                    <el-tag size="small">{{ aftersaleStatusText(record.status) }}</el-tag>
                  </td>
                  <td class="price-cell">¥{{ formatPrice(record.refundAmount) }}</td>
                  <td>{{ formatTime(record.addTime) }}</td>
                  <td>
                    <el-button link type="primary" @click="openDetail(record.aftersaleId)">查看详情</el-button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="recordTotal > pageSize" class="pagination-wrap">
              <el-pagination
                :current-page="recordPage"
                :page-size="pageSize"
                :total="recordTotal"
                layout="prev, pager, next"
                background
                @current-change="onRecordPageChange"
              />
            </div>
          </template>
          <el-empty v-else-if="!loadingRecord" description="暂无售后申请记录" />
        </div>
      </div>
    </div>

    <!-- 售后详情 -->
    <el-dialog v-model="detailVisible" title="售后详情" width="680px">
      <div v-loading="detailLoading">
        <template v-if="detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="售后单号">{{ detail.aftersalesSn || '-' }}</el-descriptions-item>
            <el-descriptions-item label="售后状态">
              <el-tag>{{ detail.statusName || aftersaleStatusText(detail.status) }}</el-tag>
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
            <div v-for="(item, idx) in detail.aftersalesItems" :key="idx" class="detail-item">
              <img :src="imageUrl(item.picThumb)" :alt="item.productName" />
              <div class="item-meta">
                <div class="name">{{ item.productName }}</div>
                <div class="sku">{{ item.skuValue || '' }}</div>
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
/**
 * 售后服务（对齐原版 `pages/member/return/List.vue` + `Log.vue`）。
 *
 * - 售后申请：`GET /api/user/aftersales/list`（按订单聚合的可售后商品，含申请/详情入口）
 * - 申请记录：`GET /api/user/aftersales/getRecord`（售后单分页）
 * - 售后详情：`GET /api/user/aftersales/detail?id=`
 */
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { aftersaleApi } from '@/api';
import { pageItems } from '@/api/types';
import type { AftersaleDetail, AftersaleOrder } from '@/api/types';
import MemberNav from '@/components/MemberNav.vue';
import { applySeo } from '@/composables/useSeo';
import { formatPrice, formatTime, imageUrl } from '@/utils/format';

const router = useRouter();

const activeTab = ref<'apply' | 'record'>('apply');
const loadingApply = ref(false);
const loadingRecord = ref(false);
const pageSize = ref(10);

const applyList = ref<AftersaleOrder[]>([]);
const applyPage = ref(1);
const applyTotal = ref(0);

const recordList = ref<any[]>([]);
const recordPage = ref(1);
const recordTotal = ref(0);

const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<AftersaleDetail | null>(null);

const AFTERSALE_TYPE_TEXT: Record<number, string> = {
  1: '仅退款',
  2: '退货退款',
  3: '换货',
};

const AFTERSALE_STATUS_TEXT: Record<number, string> = {
  1: '审核处理中',
  2: '审核通过',
  3: '审核未通过',
  4: '待用户回寄',
  5: '待商家收货',
  6: '已完成',
  7: '已取消',
  21: '待供应商审核',
  22: '待供应商收货',
};

function aftersaleTypeText(type: number): string {
  return AFTERSALE_TYPE_TEXT[Number(type)] ?? `类型${type}`;
}

function aftersaleStatusText(status: number): string {
  return AFTERSALE_STATUS_TEXT[Number(status)] ?? `状态${status}`;
}

function switchTab(tab: 'apply' | 'record') {
  activeTab.value = tab;
  if (tab === 'apply') loadApply();
  else loadRecord();
}

function goApply(orderId: number, itemId: number) {
  router.push({ path: '/member/aftersales/apply', query: { orderId: String(orderId), itemId: String(itemId) } });
}

async function loadApply() {
  loadingApply.value = true;
  try {
    const result = await aftersaleApi.list({ page: applyPage.value, size: pageSize.value });
    applyList.value = pageItems(result);
    applyTotal.value = Number(result?.total ?? 0) || 0;
  } catch {
    applyList.value = [];
    applyTotal.value = 0;
  } finally {
    loadingApply.value = false;
  }
}

async function loadRecord() {
  loadingRecord.value = true;
  try {
    const result = await aftersaleApi.getRecord({ page: recordPage.value, size: pageSize.value });
    recordList.value = pageItems(result);
    recordTotal.value = Number(result?.total ?? 0) || 0;
  } catch {
    recordList.value = [];
    recordTotal.value = 0;
  } finally {
    loadingRecord.value = false;
  }
}

function onApplyPageChange(next: number) {
  applyPage.value = next;
  loadApply();
}

function onRecordPageChange(next: number) {
  recordPage.value = next;
  loadRecord();
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
  loadApply();
});
</script>

<style scoped lang="less">
.tig-aftersales-page {
  background: #f5f6fa;
  padding: 20px 0 50px;
}

.member-layout {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.info-row { flex: 1; min-width: 0; }

.member-top-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;

  .top-tab {
    padding: 9px 22px;
    background: #fff;
    border-radius: 6px 6px 0 0;
    font-size: 14px;
    color: #555;

    &.active { color: var(--tig-red); font-weight: 700; border-bottom: 2px solid var(--tig-red); }
  }
}

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  min-height: 320px;
}

.custom-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #ddd;

  .table-header { background: #f5f5f5; }

  .header-cell {
    border: 1px solid #ddd;
    padding: 10px;
    font-size: 13px;
    color: #666;
    font-weight: 600;
  }

  td {
    border: 1px solid #e8e8e8;
    padding: 10px;
    text-align: center;
    font-size: 13px;
    color: #555;
    vertical-align: top;
  }

  .order-sn-cell { color: #333; }
  .price-cell { color: var(--tig-red); }
}

.product-list { display: flex; flex-direction: column; gap: 10px; }

.product-line {
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;

  .img { width: 50px; height: 50px; object-fit: cover; border-radius: 4px; border: 1px solid #f0f0f0; }
  .product-meta {
    flex: 1;
    min-width: 0;
    .name { font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .sn { color: #999; font-size: 12px; margin-top: 3px; }
  }
  .product-actions { display: flex; gap: 6px; align-items: center; }
}

.pagination-wrap { display: flex; justify-content: center; margin-top: 16px; }

.detail-block {
  margin-top: 18px;
  h3 { font-size: 14px; font-weight: 700; margin-bottom: 10px; }
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f7f7f7;

  img { width: 48px; height: 48px; object-fit: cover; border-radius: 4px; border: 1px solid #f0f0f0; }
  .item-meta {
    flex: 1;
    .name { font-size: 13px; }
    .sku { color: #999; font-size: 12px; margin-top: 3px; }
  }
  .price { color: #666; font-size: 13px; }
}
</style>
