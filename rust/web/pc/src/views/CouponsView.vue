<template>
  <div class="tig-coupons-page">
    <div class="container member-layout">
      <MemberNav />
      <div class="info-row">
    <div class="page-head flex-between">
      <h1 class="page-title">我的优惠券</h1>
      <router-link to="/member" class="back-link">返回个人中心</router-link>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="我的优惠券" name="mine" />
      <el-tab-pane label="领券中心" name="claim" />
    </el-tabs>

    <div v-loading="loading" class="coupon-body">
      <!-- 我的优惠券 -->
      <template v-if="activeTab === 'mine'">
        <el-empty v-if="!myCoupons.length && !loading" description="暂无优惠券" />
        <div v-else class="coupon-list">
          <div
            v-for="coupon in myCoupons"
            :key="coupon.id"
            class="coupon-card"
            :class="{ disabled: coupon.status !== 0 }"
          >
            <div class="coupon-left">
              <div class="money"><span class="symbol">¥</span>{{ formatPrice(coupon.couponMoney) }}</div>
              <div class="condition">满 {{ formatPrice(coupon.minOrderAmount) }} 可用</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">
                {{ coupon.couponName }}
                <el-tag size="small" :type="coupon.status === 0 ? 'danger' : 'info'">{{ coupon.statusName }}</el-tag>
              </div>
              <div class="coupon-desc">{{ coupon.couponDesc }}</div>
              <div class="coupon-time">
                有效期：{{ formatDate(coupon.startDate) }} ~ {{ formatDate(coupon.endDate) }}
              </div>
            </div>
            <div class="coupon-action">
              <el-button v-if="coupon.status === 0" size="small" type="primary" @click="$router.push('/search')">
                去使用
              </el-button>
            </div>
          </div>
        </div>
        <div v-if="mineTotal > minePageSize" class="pagination-wrap">
          <el-pagination
            :current-page="minePage"
            :page-size="minePageSize"
            :total="mineTotal"
            layout="prev, pager, next"
            background
            @current-change="onMinePageChange"
          />
        </div>
      </template>

      <!-- 领券中心 -->
      <template v-else>
        <el-empty v-if="!claimable.length && !loading" description="暂无可领取的优惠券" />
        <div v-else class="coupon-list">
          <div v-for="coupon in claimable" :key="coupon.couponId" class="coupon-card claimable">
            <div class="coupon-left">
              <div class="money"><span class="symbol">¥</span>{{ formatPrice(coupon.couponMoney) }}</div>
              <div class="condition">满 {{ formatPrice(coupon.minOrderAmount) }} 可用</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ coupon.couponName }}</div>
              <div class="coupon-desc">{{ coupon.couponDesc }}</div>
              <div class="coupon-time">
                有效期：{{ formatDate(coupon.useStartDate) }} ~ {{ formatDate(coupon.useEndDate) }}
              </div>
            </div>
            <div class="coupon-action">
              <el-button
                v-if="coupon.isReceive !== 1"
                size="small"
                type="primary"
                :loading="claimingId === coupon.couponId"
                @click="claim(coupon.couponId)"
              >
                立即领取
              </el-button>
              <el-button v-else size="small" disabled>已领取</el-button>
            </div>
          </div>
        </div>
        <div v-if="claimTotal > claimPageSize" class="pagination-wrap">
          <el-pagination
            :current-page="claimPage"
            :page-size="claimPageSize"
            :total="claimTotal"
            layout="prev, pager, next"
            background
            @current-change="onClaimPageChange"
          />
        </div>
      </template>
    </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { couponApi } from '@/api';
import { pageItems } from '@/api/types';
import type { ClaimableCoupon, UserCoupon } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { formatPrice } from '@/utils/format';
import dayjs from 'dayjs';
import MemberNav from '@/components/MemberNav.vue';

const activeTab = ref('mine');
const loading = ref(false);
const claimingId = ref<number | null>(null);

const myCoupons = ref<UserCoupon[]>([]);
const minePage = ref(1);
const minePageSize = ref(10);
const mineTotal = ref(0);

const claimable = ref<ClaimableCoupon[]>([]);
const claimPage = ref(1);
const claimPageSize = ref(10);
const claimTotal = ref(0);

function formatDate(seconds: number): string {
  if (!seconds || seconds <= 0) return '-';
  return dayjs.unix(seconds).format('YYYY-MM-DD');
}

async function loadMine() {
  loading.value = true;
  try {
    const page = await couponApi.myCoupons({ page: minePage.value, pageSize: minePageSize.value });
    myCoupons.value = pageItems(page);
    mineTotal.value = Number(page?.total ?? 0) || 0;
  } catch {
    myCoupons.value = [];
    mineTotal.value = 0;
  } finally {
    loading.value = false;
  }
}

async function loadClaimable() {
  loading.value = true;
  try {
    const page = await couponApi.claimable({ page: claimPage.value, pageSize: claimPageSize.value });
    claimable.value = pageItems(page);
    claimTotal.value = Number(page?.total ?? 0) || 0;
  } catch {
    claimable.value = [];
    claimTotal.value = 0;
  } finally {
    loading.value = false;
  }
}

function onTabChange() {
  if (activeTab.value === 'mine') loadMine();
  else loadClaimable();
}

function onMinePageChange(next: number) {
  minePage.value = next;
  loadMine();
}

function onClaimPageChange(next: number) {
  claimPage.value = next;
  loadClaimable();
}

async function claim(couponId: number) {
  claimingId.value = couponId;
  try {
    await couponApi.claim(couponId);
    ElMessage.success('领取成功');
    await loadClaimable();
  } finally {
    claimingId.value = null;
  }
}

onMounted(() => {
  applySeo({ title: '我的优惠券', noindex: true });
  loadMine();
});
</script>

<style scoped lang="less">
.tig-coupons-page {
  padding: 20px 0 50px;
}

.page-head {
  margin-bottom: 10px;
  .page-title { font-size: 22px; font-weight: 700; }
  .back-link { color: #888; font-size: 13px; &:hover { color: var(--tig-red); } }
}

.coupon-body { min-height: 240px; }

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coupon-card {
  display: flex;
  align-items: center;
  gap: 18px;
  background: #fff;
  border: 1px solid #ffe1df;
  border-radius: 8px;
  padding: 14px 18px;

  &.disabled { opacity: 0.6; }
  &.claimable { border-color: #ffdcdc; }

  .coupon-left {
    width: 130px;
    flex: 0 0 130px;
    text-align: center;
    border-right: 1px dashed #f0d9d7;
    padding-right: 14px;

    .money {
      color: var(--tig-red);
      font-size: 28px;
      font-weight: 800;
      .symbol { font-size: 14px; }
    }
    .condition { color: #999; font-size: 12px; margin-top: 4px; }
  }

  .coupon-right {
    flex: 1;
    min-width: 0;

    .coupon-name { font-size: 15px; font-weight: 700; display: flex; align-items: center; gap: 8px; }
    .coupon-desc { color: #666; font-size: 13px; margin-top: 5px; }
    .coupon-time { color: #aaa; font-size: 12px; margin-top: 5px; }
  }

  .coupon-action { flex: 0 0 100px; text-align: right; }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.member-layout {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.info-row {
  flex: 1;
  min-width: 0;
}
</style>
