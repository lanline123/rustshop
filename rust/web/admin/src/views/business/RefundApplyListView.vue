<template>
  <div class="business-page refund-page">
    <!-- 筛选：原版为「退款状态 + 售后单号」 -->
    <div class="tig-card search-card">
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="退款状态：">
          <el-select v-model="filters.refundStatus" placeholder="请选择状态" clearable style="width: 160px" @change="handleSearch">
            <el-option v-for="(label, code) in statusList" :key="code" :value="Number(code)" :label="label" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单/售后单号：">
          <el-input v-model="filters.orderSn" placeholder="输入订单号" clearable style="width: 200px" @keyup.enter="handleSearch" @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" plain :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button plain :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="tig-card table-card">
      <div class="table-toolbar flex-between">
        <div class="table-title">退款申请</div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <el-table v-loading="loading" :data="list" border stripe style="width: 100%">
        <el-table-column prop="refundTypeName" label="退款类型" width="130" />
        <el-table-column label="退款信息" min-width="260">
          <template #default="{ row }">
            <div class="refund-info">
              <span>退款编号：{{ row.refundId }}</span>
              <el-divider direction="vertical" />
              <span>售后 ID：{{ row.aftersaleId }}</span>
              <el-divider direction="vertical" />
              <span>订单 ID：{{ row.orderId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="refundStatusName" label="退款状态" width="120">
          <template #default="{ row }">
            <el-tag :type="refundStatusTag(row.refundStatus)">{{ row.refundStatusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="退款金额" width="130" align="right">
          <template #default="{ row }">¥{{ money(row.refundBalance) }}</template>
        </el-table-column>
        <el-table-column label="申请时间" width="170">
          <template #default="{ row }">{{ formatTime(row.addTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">
              {{ row.refundStatus === 0 || row.refundStatus === 1 ? '处理申请' : '查看详情' }}
            </el-button>
          </template>
        </el-table-column>
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

    <!-- 退款详情 / 处理抽屉，复刻原版 finance/refundApply/Info.vue -->
    <el-drawer v-model="detailVisible" title="编辑退款申请" size="760px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-body">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="最终退款金额可根据实际需要填写；处理状态为未处理或已处理时佣金等不计算最终退款金额，取消时忽略该退款。积分、优惠券退还与本操作无关。"
        />
        <template v-if="detail">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="退款编号">{{ detail.refundId }}</el-descriptions-item>
            <el-descriptions-item label="退款类型">{{ detail.refundTypeName }}</el-descriptions-item>
            <el-descriptions-item label="订单 ID">{{ detail.orderId }}</el-descriptions-item>
            <el-descriptions-item label="售后 ID">{{ detail.aftersaleId }}</el-descriptions-item>
            <el-descriptions-item label="退款状态">
              <el-tag :type="refundStatusTag(detail.refundStatus)">{{ detail.refundStatusName }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ formatTime(detail.addTime) }}</el-descriptions-item>
            <el-descriptions-item label="线上退款金额">¥{{ money(detail.onlineBalance) }}</el-descriptions-item>
            <el-descriptions-item label="线下退款金额">¥{{ money(detail.offlineBalance) }}</el-descriptions-item>
            <el-descriptions-item label="退款总额">¥{{ money(detail.refundBalance) }}</el-descriptions-item>
            <el-descriptions-item label="是否线下">{{ detail.isOffline === 1 ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="处理备注" :span="2">{{ detail.refundNote || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-form label-width="120px" class="refund-form">
            <el-form-item label="线上退款金额">
              <el-input-number v-model="form.onlineBalance" :min="0" :precision="2" :step="0.01" :disabled="readonly" style="width: 240px" />
            </el-form-item>
            <el-form-item label="线下退款金额">
              <el-input-number v-model="form.offlineBalance" :min="0" :precision="2" :step="0.01" :disabled="readonly" style="width: 240px" />
            </el-form-item>
            <el-form-item label="本次操作备注">
              <el-input v-model="form.refundNote" type="textarea" :rows="3" :disabled="readonly" placeholder="请输入退款备注" />
            </el-form-item>
            <el-form-item v-if="detail.isOffline === 1" label="线下转账凭证">
              <el-input v-model="form.paymentVoucher" :disabled="readonly" placeholder="凭证地址 / 单号" />
            </el-form-item>
            <el-form-item>
              <div class="dialog-actions">
                <el-button
                  v-if="!readonly"
                  type="primary"
                  :loading="submitting"
                  @click="submitAudit(1)"
                >同意退款</el-button>
                <el-button
                  v-if="!readonly"
                  type="danger"
                  :loading="submitting"
                  @click="submitAudit(4)"
                >拒绝退款</el-button>
                <el-button
                  v-if="detail.isOffline === 1 && !readonly"
                  type="success"
                  :loading="submitting"
                  @click="submitOfflineAudit"
                >确认已线下转账</el-button>
                <el-button @click="detailVisible = false">关闭</el-button>
              </div>
            </el-form-item>
          </el-form>

          <h4 class="section-title">退款操作日志</h4>
          <el-timeline v-if="(detail.logs || []).length > 0">
            <el-timeline-item
              v-for="log in detail.logs"
              :key="log.logId"
              :timestamp="formatTime(log.addTime)"
            >
              <div>{{ log.refundTypeName }} · {{ log.refundPayCode }} · ¥{{ money(log.refundAmount) }}</div>
              <div v-if="log.transactionId" class="muted">交易号：{{ log.transactionId }}</div>
              <div v-if="log.description" class="muted">{{ log.description }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无退款日志" :image-size="60" />
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
/**
 * 退款申请（复刻 java/view/admin/src/views/finance/refundApply/List.vue + Info.vue 结构）。
 *
 * 接口（Rust `routes/payment/refund_apply.rs`）：
 * - 列表 `GET /finance/refundApply/list`（RefundApplyListQuery：orderSn/refundStatus/page/pageSize）
 * - 详情 `GET /finance/refundApply/detail?id=`（RefundApplyDetailVO）
 * - 状态字典 `GET /finance/refundApply/config`
 * - 金额/备注保存 `POST /finance/refundApply/update`（RefundApplyUpdateDTO）
 * - 审核 `POST /finance/refundApply/audit`（RefundApplyAuditDTO）
 * - 线下转账确认 `POST /finance/refundApply/offlineAudit`（OfflineAuditDTO）
 */
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Search, Refresh } from '@element-plus/icons-vue';
import request from '@/utils/request';

const loading = ref(false);
const list = ref<any[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(15);
const statusList = ref<Record<number, string>>({});

const filters = reactive({
  orderSn: '',
  refundStatus: undefined as number | undefined
});

function money(value: unknown) {
  return Number(value ?? 0).toFixed(2);
}

function formatTime(ts: unknown) {
  const n = Number(ts ?? 0);
  if (!n) return '-';
  const d = new Date(n * 1000);
  const pad = (v: number) => String(v).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

function refundStatusTag(status: number): 'success' | 'warning' | 'danger' | 'info' | 'primary' {
  if (status === 0) return 'warning';
  if (status === 1) return 'primary';
  if (status === 2) return 'success';
  if (status === 3) return 'info';
  if (status === 4) return 'danger';
  return 'info';
}

async function loadConfig() {
  const res = await request<any>({ url: '/finance/refundApply/config', method: 'get' });
  statusList.value = res || {};
}

async function loadList() {
  loading.value = true;
  try {
    const res = await request<any>({
      url: '/finance/refundApply/list',
      method: 'get',
      params: {
        page: page.value,
        pageSize: size.value,
        orderSn: filters.orderSn || undefined,
        refundStatus: filters.refundStatus
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
  filters.refundStatus = undefined;
  handleSearch();
}

// ————————————————— 详情/处理 —————————————————
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<any>(null);
const submitting = ref(false);
const form = reactive({
  onlineBalance: 0,
  offlineBalance: 0,
  refundNote: '',
  paymentVoucher: ''
});

const readonly = ref(true);

async function openDetail(row: any) {
  detailVisible.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    const res = await request<any>({
      url: '/finance/refundApply/detail',
      method: 'get',
      params: { id: row.refundId }
    });
    detail.value = res;
    form.onlineBalance = Number(res?.onlineBalance ?? 0);
    form.offlineBalance = Number(res?.offlineBalance ?? 0);
    form.refundNote = res?.refundNote ?? '';
    form.paymentVoucher = res?.paymentVoucher ?? '';
    readonly.value = ![0, 1].includes(Number(res?.refundStatus));
  } finally {
    detailLoading.value = false;
  }
}

async function saveAmounts() {
  if (!detail.value) return;
  await request({
    url: '/finance/refundApply/update',
    method: 'post',
    data: {
      refundId: detail.value.refundId,
      onlineBalance: form.onlineBalance,
      offlineBalance: form.offlineBalance,
      refundNote: form.refundNote
    }
  });
}

async function submitAudit(refundStatus: number) {
  if (!detail.value) return;
  if (refundStatus === 4 && !form.refundNote.trim()) {
    ElMessage.warning('拒绝退款时必须填写备注');
    return;
  }
  submitting.value = true;
  try {
    await saveAmounts();
    await request({
      url: '/finance/refundApply/audit',
      method: 'post',
      data: {
        refundId: detail.value.refundId,
        refundStatus,
        refundNote: form.refundNote || undefined
      }
    });
    ElMessage.success(refundStatus === 1 ? '退款申请已通过' : '退款申请已拒绝');
    detailVisible.value = false;
    loadList();
  } finally {
    submitting.value = false;
  }
}

async function submitOfflineAudit() {
  if (!detail.value) return;
  if (!form.paymentVoucher.trim()) {
    ElMessage.warning('请填写线下转账凭证');
    return;
  }
  submitting.value = true;
  try {
    await request({
      url: '/finance/refundApply/offlineAudit',
      method: 'post',
      data: { refundId: detail.value.refundId, paymentVoucher: form.paymentVoucher }
    });
    ElMessage.success('线下转账已确认');
    detailVisible.value = false;
    loadList();
  } finally {
    submitting.value = false;
  }
}

onMounted(async () => {
  await loadConfig();
  await loadList();
});
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

.refund-info {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.refund-form {
  margin-top: 8px;
}

.dialog-actions {
  display: flex;
  gap: 10px;
}

.section-title {
  margin: 8px 0;
  font-size: 14px;
  font-weight: 600;
}

.muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
