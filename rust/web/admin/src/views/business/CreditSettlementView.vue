<template>
  <div class="business-page credit-settlement-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="结算会员" name="users">
        <div class="tig-card search-card">
          <el-form :inline="true" class="search-form" @submit.prevent>
            <el-form-item label="会员名：">
              <el-input v-model="userFilters.keyword" placeholder="会员用户名" clearable style="width: 200px" @keyup.enter="loadUsers" @clear="loadUsers" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" plain :icon="Search" @click="loadUsers">搜索</el-button>
              <el-button plain :icon="Refresh" @click="resetUsers">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="tig-card table-card">
          <div class="table-toolbar flex-between">
            <div class="table-title">账期结算会员</div>
            <el-button :icon="Refresh" circle @click="loadUsers" />
          </div>
          <el-table v-loading="userLoading" :data="users" border stripe style="width: 100%">
            <el-table-column prop="username" label="会员名" min-width="130" />
            <el-table-column prop="mobile" label="手机号" width="130" />
            <el-table-column prop="typeText" label="会员类型" width="100" />
            <el-table-column prop="companyName" label="公司名称" min-width="170" />
            <el-table-column label="信用额度" width="120" align="right">
              <template #default="{ row }">¥{{ money(row.creditLimit) }}</template>
            </el-table-column>
            <el-table-column label="已用额度" width="120" align="right">
              <template #default="{ row }">¥{{ money(row.usedAmount) }}</template>
            </el-table-column>
            <el-table-column label="可用额度" width="120" align="right">
              <template #default="{ row }">¥{{ money(row.availableAmount) }}</template>
            </el-table-column>
            <el-table-column prop="settlementTypeText" label="结算账期" width="110" />
            <el-table-column prop="remainingDays" label="剩余天数" width="100" align="center" />
            <el-table-column prop="pendingOrderCount" label="待结算单数" width="110" align="center" />
            <el-table-column label="待结算金额" width="130" align="right">
              <template #default="{ row }">¥{{ money(row.pendingAmount) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openSettle(row)">去结算</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-pagination">
            <el-pagination
              v-model:current-page="userPage"
              v-model:page-size="userSize"
              :total="userTotal"
              :page-sizes="[10, 15, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadUsers"
              @current-change="loadUsers"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="历史结算" name="history">
        <div class="tig-card search-card">
          <el-form :inline="true" class="search-form" @submit.prevent>
            <el-form-item label="回款流水号：">
              <el-input v-model="historyFilters.settlementSn" placeholder="结算流水号" clearable style="width: 200px" @keyup.enter="loadHistory" @clear="loadHistory" />
            </el-form-item>
            <el-form-item label="结算状态：">
              <el-select v-model="historyFilters.settlementStatus" clearable style="width: 150px" @change="loadHistory">
                <el-option :value="0" label="待结算" />
                <el-option :value="1" label="已结算" />
                <el-option :value="2" label="已取消" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" plain :icon="Search" @click="loadHistory">搜索</el-button>
              <el-button plain :icon="Refresh" @click="resetHistory">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="tig-card table-card">
          <div class="table-toolbar flex-between">
            <div class="table-title">历史结算记录</div>
            <el-button :icon="Refresh" circle @click="loadHistory" />
          </div>
          <el-table v-loading="historyLoading" :data="history" border stripe style="width: 100%">
            <el-table-column prop="settlementSn" label="回款流水号" min-width="180" />
            <el-table-column prop="username" label="会员名" width="130" />
            <el-table-column label="结算金额" width="130" align="right">
              <template #default="{ row }">¥{{ money(row.settlementAmount) }}</template>
            </el-table-column>
            <el-table-column prop="orderCount" label="订单数" width="90" align="center" />
            <el-table-column prop="settlementStatusText" label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.settlementStatus === 1 ? 'success' : row.settlementStatus === 2 ? 'info' : 'warning'">
                  {{ row.settlementStatusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="settlementTime" label="结算时间" width="170" />
            <el-table-column prop="operatorName" label="操作人" width="110" />
            <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
            <el-table-column label="操作" width="170" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openHistoryOrders(row)">订单明细</el-button>
                <el-button v-if="row.settlementStatus !== 2" type="danger" link size="small" @click="openCancel(row)">撤销</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-pagination">
            <el-pagination
              v-model:current-page="historyPage"
              v-model:page-size="historySize"
              :total="historyTotal"
              :page-sizes="[10, 15, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadHistory"
              @current-change="loadHistory"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 确认结算（原版 termPay/toPayForm） -->
    <el-dialog v-model="settleVisible" title="确认结算" width="760px" destroy-on-close>
      <div v-if="settleUser" class="settle-user">
        结算会员：<strong>{{ settleUser.username }}</strong>（UID {{ settleUser.userId }}）
      </div>
      <el-form label-width="120px">
        <el-form-item label="回款流水号" required>
          <el-input v-model="settleForm.settlementSn" placeholder="请输入回款流水单号" style="width: 320px" />
        </el-form-item>
        <el-form-item label="结算时间" required>
          <el-date-picker
            v-model="settleForm.settlementTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择结算时间"
            style="width: 320px"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="settleForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <div class="orders-title">待结算订单（勾选后提交）</div>
      <el-table
        v-loading="pendingLoading"
        :data="pendingOrders"
        border
        size="small"
        max-height="300"
        @selection-change="(rows: any[]) => (selectedOrders = rows)"
      >
        <el-table-column type="selection" width="46" />
        <el-table-column prop="orderSn" label="订单号" min-width="180" />
        <el-table-column label="订单金额" width="120" align="right">
          <template #default="{ row }">¥{{ money(row.orderAmount) }}</template>
        </el-table-column>
        <el-table-column label="账期金额" width="120" align="right">
          <template #default="{ row }">¥{{ money(row.creditAmount) }}</template>
        </el-table-column>
        <el-table-column prop="settlementStatusText" label="结算状态" width="110" />
        <el-table-column prop="orderTime" label="下单时间" width="170" />
      </el-table>
      <template #footer>
        <el-button @click="settleVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitSettle">确认结算</el-button>
      </template>
    </el-dialog>

    <!-- 历史订单明细 -->
    <el-dialog v-model="ordersVisible" title="结算订单明细" width="760px" destroy-on-close>
      <el-table v-loading="ordersLoading" :data="historyOrders" border size="small" max-height="380">
        <el-table-column prop="orderSn" label="订单号" min-width="180" />
        <el-table-column label="订单金额" width="120" align="right">
          <template #default="{ row }">¥{{ money(row.orderAmount) }}</template>
        </el-table-column>
        <el-table-column label="账期金额" width="120" align="right">
          <template #default="{ row }">¥{{ money(row.creditAmount) }}</template>
        </el-table-column>
        <el-table-column prop="settlementStatusText" label="结算状态" width="110" />
        <el-table-column prop="orderTime" label="下单时间" width="170" />
      </el-table>
    </el-dialog>

    <!-- 撤销结算 -->
    <el-dialog v-model="cancelVisible" title="撤销结算" width="480px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="撤销原因">
          <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="请输入撤销原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitCancel">确认撤销</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 账期结算（参照原版 order/termPay/termPay/List.vue + WaitPay.vue + toPayForm.vue 结构）。
 *
 * 接口：`/credit/settlement/userList|pendingOrders|submit|history|historyOrders|cancel`。
 */
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Refresh, Search } from '@element-plus/icons-vue';
import request from '@/utils/request';

const activeTab = ref<'users' | 'history'>('users');

function money(value: unknown) {
  return Number(value ?? 0).toFixed(2);
}

// ————————————————— 结算会员 —————————————————
const userLoading = ref(false);
const users = ref<any[]>([]);
const userPage = ref(1);
const userSize = ref(15);
const userTotal = ref(0);
const userFilters = reactive({ keyword: '' });

async function loadUsers() {
  userLoading.value = true;
  try {
    const res = await request<any>({
      url: '/credit/settlement/userList',
      method: 'get',
      params: { page: userPage.value, size: userSize.value, keyword: userFilters.keyword || undefined }
    });
    users.value = res?.list ?? [];
    userTotal.value = res?.total ?? users.value.length;
  } finally {
    userLoading.value = false;
  }
}

function resetUsers() {
  userFilters.keyword = '';
  userPage.value = 1;
  loadUsers();
}

// ————————————————— 历史结算 —————————————————
const historyLoading = ref(false);
const history = ref<any[]>([]);
const historyPage = ref(1);
const historySize = ref(15);
const historyTotal = ref(0);
const historyFilters = reactive({
  settlementSn: '',
  settlementStatus: undefined as number | undefined
});

async function loadHistory() {
  historyLoading.value = true;
  try {
    const res = await request<any>({
      url: '/credit/settlement/history',
      method: 'get',
      params: {
        page: historyPage.value,
        size: historySize.value,
        settlementSn: historyFilters.settlementSn || undefined,
        settlementStatus: historyFilters.settlementStatus
      }
    });
    history.value = res?.list ?? [];
    historyTotal.value = res?.total ?? history.value.length;
  } finally {
    historyLoading.value = false;
  }
}

function resetHistory() {
  historyFilters.settlementSn = '';
  historyFilters.settlementStatus = undefined;
  historyPage.value = 1;
  loadHistory();
}

// ————————————————— 提交结算 —————————————————
const settleVisible = ref(false);
const settleUser = ref<any>(null);
const pendingLoading = ref(false);
const pendingOrders = ref<any[]>([]);
const selectedOrders = ref<any[]>([]);
const submitting = ref(false);
const settleForm = reactive({
  settlementSn: '',
  settlementTime: '',
  remark: ''
});

async function openSettle(row: any) {
  settleUser.value = row;
  settleForm.settlementSn = '';
  settleForm.settlementTime = '';
  settleForm.remark = '';
  selectedOrders.value = [];
  settleVisible.value = true;
  pendingLoading.value = true;
  try {
    const res = await request<any>({
      url: '/credit/settlement/pendingOrders',
      method: 'get',
      params: { userId: row.userId, page: 1, size: 100 }
    });
    pendingOrders.value = res?.list ?? [];
  } finally {
    pendingLoading.value = false;
  }
}

async function submitSettle() {
  if (!settleUser.value) return;
  if (!settleForm.settlementSn.trim()) {
    ElMessage.warning('请输入回款流水单号');
    return;
  }
  if (!settleForm.settlementTime) {
    ElMessage.warning('请选择结算时间');
    return;
  }
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请勾选要结算的订单');
    return;
  }
  submitting.value = true;
  try {
    await request({
      url: '/credit/settlement/submit',
      method: 'post',
      data: {
        userId: settleUser.value.userId,
        settlementSn: settleForm.settlementSn.trim(),
        settlementTime: settleForm.settlementTime,
        remark: settleForm.remark || undefined,
        orderIds: selectedOrders.value.map((o) => o.orderId)
      }
    });
    ElMessage.success('结算成功');
    settleVisible.value = false;
    loadUsers();
    loadHistory();
  } finally {
    submitting.value = false;
  }
}

// ————————————————— 历史订单明细 —————————————————
const ordersVisible = ref(false);
const ordersLoading = ref(false);
const historyOrders = ref<any[]>([]);

async function openHistoryOrders(row: any) {
  ordersVisible.value = true;
  ordersLoading.value = true;
  historyOrders.value = [];
  try {
    const res = await request<any>({
      url: '/credit/settlement/historyOrders',
      method: 'get',
      params: { settlementId: row.settlementId, page: 1, size: 100 }
    });
    historyOrders.value = res?.list ?? [];
  } finally {
    ordersLoading.value = false;
  }
}

// ————————————————— 撤销 —————————————————
const cancelVisible = ref(false);
const cancelRow = ref<any>(null);
const cancelReason = ref('');

function openCancel(row: any) {
  cancelRow.value = row;
  cancelReason.value = '';
  cancelVisible.value = true;
}

async function submitCancel() {
  if (!cancelRow.value) return;
  submitting.value = true;
  try {
    await request({
      url: '/credit/settlement/cancel',
      method: 'post',
      data: { settlementId: cancelRow.value.settlementId, cancelReason: cancelReason.value || undefined }
    });
    ElMessage.success('撤销成功');
    cancelVisible.value = false;
    loadHistory();
  } finally {
    submitting.value = false;
  }
}

onMounted(async () => {
  await loadUsers();
  await loadHistory();
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
.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.settle-user {
  margin-bottom: 12px;
  font-size: 13px;
}
.orders-title {
  margin: 8px 0;
  font-weight: 600;
  font-size: 13px;
}
</style>
