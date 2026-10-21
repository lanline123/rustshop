<template>
  <div class="business-page credit-order-page">
    <div class="tig-card search-card">
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="订单号：">
          <el-input v-model="filters.orderSn" placeholder="订单号" clearable style="width: 180px" @keyup.enter="handleSearch" @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="会员名：">
          <el-input v-model="filters.username" placeholder="会员用户名" clearable style="width: 160px" @keyup.enter="handleSearch" @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号：">
          <el-input v-model="filters.mobile" placeholder="手机号" clearable style="width: 160px" @keyup.enter="handleSearch" @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="结算状态：">
          <el-select v-model="filters.settlementStatus" clearable style="width: 150px" @change="handleSearch">
            <el-option :value="0" label="未结算" />
            <el-option :value="1" label="已结算" />
            <el-option :value="2" label="已取消" />
            <el-option :value="3" label="退款中" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" plain :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button plain :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="tig-card table-card">
      <div class="table-toolbar flex-between">
        <div class="table-title">账期订单</div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border stripe style="width: 100%">
        <el-table-column prop="orderSn" label="订单号" min-width="180" />
        <el-table-column prop="username" label="会员名" width="130" />
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column prop="companyName" label="公司名称" min-width="170" />
        <el-table-column label="订单金额" width="120" align="right">
          <template #default="{ row }">¥{{ money(row.orderAmount) }}</template>
        </el-table-column>
        <el-table-column label="账期金额" width="120" align="right">
          <template #default="{ row }">¥{{ money(row.creditAmount) }}</template>
        </el-table-column>
        <el-table-column label="已退金额" width="110" align="right">
          <template #default="{ row }">¥{{ money(row.refundedAmount) }}</template>
        </el-table-column>
        <el-table-column prop="orderStatusText" label="订单状态" width="110" />
        <el-table-column prop="settlementStatusText" label="结算状态" width="110">
          <template #default="{ row }">
            <el-tag :type="settlementTag(row.settlementStatus)">{{ row.settlementStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="settlementSn" label="回款流水号" width="180" />
        <el-table-column prop="orderTime" label="下单时间" width="170" />
      </el-table>

      <div class="table-pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 15, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 账期订单列表（复刻原版 order/termPay/termOrder/List.vue 结构）。
 *
 * 接口：`GET /credit/order/list`（CreditOrderListQuery）。
 */
import { onMounted, reactive, ref } from 'vue';
import { Refresh, Search } from '@element-plus/icons-vue';
import request from '@/utils/request';

const loading = ref(false);
const list = ref<any[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(15);
const filters = reactive({
  orderSn: '',
  username: '',
  mobile: '',
  settlementStatus: undefined as number | undefined
});

function money(value: unknown) {
  return Number(value ?? 0).toFixed(2);
}

function settlementTag(status: number): 'success' | 'warning' | 'danger' | 'info' | 'primary' {
  if (status === 0) return 'warning';
  if (status === 1) return 'success';
  if (status === 2) return 'info';
  if (status === 3) return 'danger';
  return 'info';
}

async function loadList() {
  loading.value = true;
  try {
    const res = await request<any>({
      url: '/credit/order/list',
      method: 'get',
      params: {
        page: page.value,
        size: size.value,
        orderSn: filters.orderSn || undefined,
        username: filters.username || undefined,
        mobile: filters.mobile || undefined,
        settlementStatus: filters.settlementStatus
      }
    });
    list.value = res?.list ?? [];
    total.value = res?.total ?? list.value.length;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  page.value = 1;
  loadList();
}

function handleReset() {
  filters.orderSn = '';
  filters.username = '';
  filters.mobile = '';
  filters.settlementStatus = undefined;
  handleSearch();
}

onMounted(loadList);
</script>

<style scoped>
.business-page {
  width: 100%;
}
.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}
.table-toolbar {
  margin-bottom: 16px;
}
.table-title {
  font-size: 16px;
  font-weight: 600;
}
.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
