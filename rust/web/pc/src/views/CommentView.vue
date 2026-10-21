<template>
  <div class="tig-comment-page">
    <div class="container member-layout">
      <MemberNav />

      <div class="info-row">
        <div class="content-card">
          <div class="card-title">晒单评价</div>

          <div class="tig-tabs">
            <div
              v-for="tab in menuList"
              :key="tab.type"
              class="tab"
              :class="{ active: orderType === tab.type }"
              @click="changeType(tab.type)"
            >
              <p>
                {{ tab.value }}
                <span v-if="tabSize(tab.type)" class="tab-count">{{ tabSize(tab.type) }}</span>
              </p>
            </div>
          </div>

          <div v-loading="loading" class="page-info-body">
            <!-- 待评价 / 待晒单：订单表格 -->
            <template v-if="orderType !== 2">
              <table v-for="item in orderList" :key="item.orderId" class="order-table">
                <thead class="table-header">
                  <tr>
                    <th colspan="3">
                      订单号：<span class="font-color">{{ item.orderSn }}</span>
                      <span style="margin-left: 30px">下单时间：{{ formatTime(item.addTime) }}</span>
                    </th>
                    <th>
                      <router-link :to="`/member/orders/${item.orderId}`" class="order-detail-link">订单详情 ›</router-link>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr class="order-tr">
                    <td class="column-goods">
                      <div v-for="product in item.items" :key="product.itemId" class="order-product">
                        <img :src="imageUrl(product.picThumb || product.productPicThumb)" :alt="product.productName || ''" class="product-img" />
                        <span class="product-name">{{ product.productName }}</span>
                        <span class="product-qty">x{{ product.quantity }}</span>
                      </div>
                    </td>
                    <td class="column-meta">收货人：{{ item.consignee || '-' }}</td>
                    <td class="column-meta">
                      订单总额：<span class="price">¥{{ formatPrice(item.totalAmount) }}</span>
                    </td>
                    <td class="column-action">
                      <el-button type="primary" size="small" @click="goEvaluate">
                        {{ orderType === 0 ? '去评价' : '待晒单' }}
                      </el-button>
                    </td>
                  </tr>
                </tbody>
              </table>
              <el-empty v-if="!orderList.length && !loading" description="当前筛选下还没有任何订单哦！" :image-size="90" />
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
            </template>

            <!-- 已评价：评价卡片 -->
            <template v-else>
              <div v-for="(comment, cIndex) in commentList" :key="comment.commentId ?? cIndex" class="comment-item">
                <div class="comment-head">
                  <div class="product-info">
                    <img :src="imageUrl(comment.picThumb)" :alt="comment.productName || ''" class="product-img" />
                    <div class="product-meta">
                      <div class="name">{{ comment.productName }}</div>
                      <div class="sku">
                        <span v-for="(sku, idx) in comment.skuData ?? []" :key="idx">{{ sku.name }}：{{ sku.value }} </span>
                      </div>
                    </div>
                  </div>
                  <div class="comment-meta">
                    <el-rate :model-value="Number(comment.commentRank ?? 5)" disabled />
                    <span class="comment-time">{{ comment.addTime }}</span>
                  </div>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div v-if="comment.showPics?.length" class="comment-pics">
                  <img v-for="(pic, idx) in comment.showPics" :key="idx" :src="imageUrl(pic.picUrl || pic.picThumb)" alt="晒图" />
                </div>
                <div v-if="comment.replies?.length" class="comment-replies">
                  <div v-for="(reply, idx) in comment.replies" :key="idx" class="reply-item">
                    <span class="reply-label">商家回复：</span>{{ reply.content }}
                  </div>
                </div>
              </div>
              <el-empty v-if="!commentList.length && !loading" description="暂无已评价记录" :image-size="90" />
              <div v-if="commentTotal > pageSize" class="pagination-wrap">
                <el-pagination
                  :current-page="page"
                  :page-size="pageSize"
                  :total="commentTotal"
                  layout="prev, pager, next"
                  background
                  @current-change="onPageChange"
                />
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 评价晒单（对齐原版 `pages/member/comment/List.vue`）。
 *
 * tabs：待评价（`/user/order/list?orderStatus=3`）/ 待晒单（`/user/comment/showedList`）/
 * 已评价（`/user/comment/list`）；数量统计来自 `GET /user/comment/subNum`。
 * 评价提交页（原版 comment/Info）本轮未迁移：按钮点击给出明确提示。
 */
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { commentApi, orderApi } from '@/api';
import { pageItems } from '@/api/types';
import type { CommentItem, OrderVO } from '@/api/types';
import MemberNav from '@/components/MemberNav.vue';
import { applySeo } from '@/composables/useSeo';
import { formatPrice, formatTime, imageUrl } from '@/utils/format';

const menuList = reactive([
  { type: 0, value: '待评价' },
  { type: 1, value: '待晒单' },
  { type: 2, value: '已评价' },
]);

const orderType = ref(0);
const loading = ref(true);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);

const orderList = ref<any[]>([]);
const commentList = ref<CommentItem[]>([]);
const commentTotal = ref(0);
const subNum = ref({ awaitComment: 0, showPics: 0, commented: 0 });

function tabSize(type: number): number {
  if (type === 0) return subNum.value.awaitComment;
  if (type === 1) return subNum.value.showPics;
  return subNum.value.commented;
}

function changeType(type: number) {
  orderType.value = type;
  page.value = 1;
  load();
}

function onPageChange(next: number) {
  page.value = next;
  load();
}

function goEvaluate() {
  ElMessage.info('评价提交页尚未迁移，请暂通过原版入口或稍后再试');
}

async function load() {
  loading.value = true;
  try {
    if (orderType.value === 0) {
      const result = await orderApi.list({ orderStatus: 3, page: page.value, pageSize: pageSize.value });
      orderList.value = pageItems(result) as OrderVO[];
      total.value = Number(result?.total ?? 0) || 0;
      commentList.value = [];
    } else if (orderType.value === 1) {
      const result = await commentApi.showedList({ page: page.value, size: pageSize.value });
      orderList.value = pageItems(result);
      total.value = Number(result?.total ?? 0) || 0;
      commentList.value = [];
    } else {
      const result = await commentApi.list({ page: page.value, pageSize: pageSize.value });
      commentList.value = pageItems(result);
      commentTotal.value = Number(result?.total ?? 0) || 0;
      orderList.value = [];
    }
  } catch {
    orderList.value = [];
    commentList.value = [];
    total.value = 0;
    commentTotal.value = 0;
  } finally {
    loading.value = false;
  }
}

async function loadSubNum() {
  try {
    subNum.value = await commentApi.subNum();
  } catch {
    subNum.value = { awaitComment: 0, showPics: 0, commented: 0 };
  }
}

onMounted(() => {
  applySeo({ title: '评价晒单', noindex: true });
  loadSubNum();
  load();
});
</script>

<style scoped lang="less">
.tig-comment-page {
  background: #f5f6fa;
  padding: 20px 0 50px;
}

.member-layout {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.info-row { flex: 1; min-width: 0; }

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px 20px;
  min-height: 360px;
}

.card-title { font-size: 16px; font-weight: 700; margin-bottom: 14px; }

.tig-tabs {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 14px;

  .tab {
    padding: 10px 22px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    border-bottom: 2px solid transparent;

    &.active { color: var(--tig-red); border-bottom-color: var(--tig-red); font-weight: 700; }
    .tab-count { color: var(--tig-red); font-size: 12px; margin-left: 2px; }
  }
}

.order-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #eee;
  margin-bottom: 14px;

  .table-header {
    background: #fafafa;
    th {
      padding: 10px 12px;
      font-size: 13px;
      color: #666;
      text-align: left;
      font-weight: 600;
    }
    .order-detail-link { color: #333; font-size: 13px; float: right; &:hover { color: var(--tig-red); } }
  }

  .order-tr td {
    border-top: 1px solid #f5f5f5;
    padding: 12px;
    font-size: 13px;
    color: #666;
    vertical-align: middle;
  }

  .column-goods { width: 46%; }
  .column-meta { width: 16%; text-align: center; }
  .column-action { width: 12%; text-align: center; }

  .order-product {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 4px 0;

    .product-img { width: 50px; height: 50px; object-fit: cover; border-radius: 4px; border: 1px solid #f0f0f0; }
    .product-name { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .product-qty { color: #999; }
  }

  .price { color: var(--tig-red); }
}

.comment-item {
  border-bottom: 1px solid #f5f5f5;
  padding: 14px 0;

  .comment-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .product-info {
    display: flex;
    align-items: center;
    gap: 10px;

    .product-img { width: 52px; height: 52px; object-fit: cover; border-radius: 4px; border: 1px solid #f0f0f0; }
    .product-meta {
      .name { font-size: 13px; color: #333; }
      .sku { color: #999; font-size: 12px; margin-top: 4px; }
    }
  }

  .comment-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    .comment-time { color: #999; font-size: 12px; }
  }

  .comment-content { color: #555; font-size: 13px; line-height: 1.8; }

  .comment-pics {
    display: flex;
    gap: 8px;
    margin-top: 10px;
    flex-wrap: wrap;

    img { width: 80px; height: 80px; object-fit: cover; border-radius: 4px; border: 1px solid #f0f0f0; }
  }

  .comment-replies {
    background: #fafafa;
    border-radius: 4px;
    padding: 8px 12px;
    margin-top: 10px;
    font-size: 12px;
    color: #666;
    .reply-label { color: #999; }
  }
}

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }

.font-color { color: var(--tig-red); }
</style>
