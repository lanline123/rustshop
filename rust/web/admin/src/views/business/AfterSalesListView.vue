<template>
  <div class="business-page aftersales-page">
    <!-- 筛选区：与原版 List.vue 一致（交易单号 / 售后编号 / 申请类型 / 退换状态） -->
    <div class="tig-card search-card">
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="交易单号：">
          <el-input v-model="filters.outTradeNo" placeholder="交易单号" clearable style="width: 180px" @keyup.enter="handleSearch" @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="售后编号：">
          <el-input v-model="filters.aftersalesSn" placeholder="售后编号" clearable style="width: 180px" @keyup.enter="handleSearch" @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="申请类型：">
          <el-select v-model="filters.aftersaleType" clearable style="width: 150px" @change="handleSearch">
            <el-option v-for="(label, code) in typeList" :key="code" :value="Number(code)" :label="label" />
          </el-select>
        </el-form-item>
        <el-form-item label="退换状态：">
          <el-select v-model="filters.status" clearable style="width: 150px" @change="handleSearch">
            <el-option v-for="(label, code) in statusList" :key="code" :value="Number(code)" :label="label" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" plain :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button plain :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 列表：按售后单分组的“订单头 + 商品行”结构（复刻原版 custom-table 分组表） -->
    <div class="tig-card table-card">
      <div class="table-toolbar flex-between">
        <div class="table-title">售后管理</div>
        <el-button :icon="Refresh" circle @click="loadList" />
      </div>

      <div v-loading="loading">
        <el-table :data="list" border stripe style="width: 100%" row-key="aftersaleId">
          <el-table-column label="退换货商品" min-width="320">
            <template #default="{ row }">
              <div class="group-header">
                <span>订单编号：{{ row.orderSn }}</span>
                <el-divider direction="vertical" />
                <span>售后编号：{{ row.aftersalesSn }}</span>
                <el-divider direction="vertical" />
                <span>申请时间：{{ row.addTime }}</span>
                <template v-if="row.userName">
                  <el-divider direction="vertical" />
                  <span>申请人：{{ row.userName }}</span>
                </template>
              </div>
              <div v-for="item in row.aftersalesItems" :key="item.aftersalesItemId" class="item-row">
                <el-image :src="resolveImage(item.picThumb)" style="width: 44px; height: 44px; border-radius: 4px" fit="cover" />
                <div class="item-name">
                  <div>{{ item.productName }}</div>
                  <div class="muted">{{ item.skuValue }}</div>
                </div>
                <div class="item-price">
                  <div>¥{{ money(item.price) }}</div>
                  <div class="muted">× {{ item.quantity }}<span class="danger-text">（退货数量：{{ item.number }}）</span></div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="售后原因" min-width="140">
            <template #default="{ row }">{{ row.aftersaleReason || '-' }}</template>
          </el-table-column>
          <el-table-column label="服务类型" width="120">
            <template #default="{ row }">{{ row.aftersalesTypeName }}</template>
          </el-table-column>
          <el-table-column label="售后状态" width="130">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">{{ row.statusName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <el-button type="primary" link size="small" @click="openDetail(row)">售后详情</el-button>
                <el-button v-if="row.orderId" type="info" link size="small" @click="openSign(row)">标记</el-button>
                <el-button type="info" link size="small" @click="openRecord(row)">协商记录</el-button>
                <template v-if="Number(row.aftersaleType) === 1 && Number(row.status) === 1">
                  <el-button type="danger" link size="small" @click="openHandle(row, 2)">同意并发送退货地址</el-button>
                  <el-button link size="small" @click="openHandle(row, 3)">拒绝退货退款</el-button>
                </template>
                <template v-else-if="Number(row.aftersaleType) === 2 && Number(row.status) === 1">
                  <el-button type="danger" link size="small" @click="openHandle(row, 2)">同意仅退款</el-button>
                  <el-button link size="small" @click="openHandle(row, 3)">拒绝仅退款</el-button>
                </template>
                <template v-else-if="Number(row.aftersaleType) === 1 && Number(row.status) === 5">
                  <el-button type="danger" link size="small" @click="confirmReceive(row)">确认收货</el-button>
                  <el-button link size="small" @click="openRejectReceive(row)">拒收</el-button>
                </template>
                <span v-else class="muted">-</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && list.length === 0" description="暂无数据" />
      </div>

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

    <!-- 售后详情抽屉 -->
    <el-drawer v-model="detailVisible" :title="`售后详情 ${detail?.aftersalesSn || ''}`" size="820px" destroy-on-close>
      <div v-loading="detailLoading">
        <template v-if="detail">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="售后编号">{{ detail.aftersalesSn }}</el-descriptions-item>
            <el-descriptions-item label="售后状态">
              <el-tag :type="statusTagType(detail.status)">{{ detail.statusName }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单编号">{{ detail.orderSn }}</el-descriptions-item>
            <el-descriptions-item label="服务类型">{{ detail.aftersalesTypeName }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ detail.addTime }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">{{ detail.auditTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="售后原因">{{ detail.aftersaleReason || '-' }}</el-descriptions-item>
            <el-descriptions-item label="退款金额">¥{{ money(detail.refundAmount) }}</el-descriptions-item>
            <el-descriptions-item label="实际退款金额">¥{{ money(detail.actualRefundAmount) }}</el-descriptions-item>
            <el-descriptions-item label="平台建议退款">¥{{ money(detail.suggestRefundAmount) }}</el-descriptions-item>
            <el-descriptions-item label="物流公司">{{ detail.logisticsName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="物流单号">{{ detail.trackingNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="退货地址" :span="2">{{ detail.returnAddress || '-' }}</el-descriptions-item>
            <el-descriptions-item label="退货提示" :span="2">{{ detail.returnGoodsTip || '-' }}</el-descriptions-item>
            <el-descriptions-item label="问题描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
            <el-descriptions-item label="处理备注" :span="2">{{ detail.reply || '-' }}</el-descriptions-item>
          </el-descriptions>

          <h4 class="section-title">售后商品</h4>
          <el-table :data="detail.aftersalesItems || []" border size="small">
            <el-table-column label="商品" min-width="240">
              <template #default="{ row }">
                <div class="item-row">
                  <el-image :src="resolveImage(row.picThumb)" style="width: 40px; height: 40px; border-radius: 4px" fit="cover" />
                  <div>
                    <div>{{ row.productName }}</div>
                    <div class="muted">{{ row.skuValue }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="价格" width="110" align="right">
              <template #default="{ row }">¥{{ money(row.price) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="购买数量" width="90" align="center" />
            <el-table-column prop="number" label="退货数量" width="90" align="center" />
            <el-table-column prop="taxRate" label="税率" width="80" align="center" />
          </el-table>

          <h4 class="section-title">协商记录</h4>
          <el-timeline v-if="(detail.aftersalesLog || []).length > 0">
            <el-timeline-item
              v-for="log in detail.aftersalesLog"
              :key="log.logId"
              :timestamp="`${log.addTime} ${log.adminName || ''}`"
            >
              <div>{{ log.logInfo }}</div>
              <div v-if="log.refundDesc" class="muted">{{ log.refundDesc }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无协商记录" :image-size="60" />

          <div class="drawer-actions">
            <el-button v-if="detail.status === 5" type="primary" @click="confirmReceive(detail)">确认收货</el-button>
            <el-button v-if="detail.status === 5" @click="openRejectReceive(detail)">拒收</el-button>
            <el-button v-if="[2, 4, 5, 22].includes(detail.status)" type="success" @click="completeAftersales(detail)">完成售后</el-button>
            <el-button v-if="detail.status === 1 || detail.status === 21" type="primary" @click="openHandle(detail, 2)">处理申请</el-button>
            <el-button @click="openRecord(detail)">添加协商记录</el-button>
          </div>
        </template>
        <el-empty v-else description="未获取到售后详情" />
      </div>
    </el-drawer>

    <!-- 处理售后（同意/拒绝）弹窗，复刻原版 Handle.vue 字段 -->
    <el-dialog v-model="handleVisible" title="处理售后" width="600px" destroy-on-close>
      <el-form label-width="110px">
        <el-form-item label="售后方式：">{{ handleRow?.aftersalesTypeName }}</el-form-item>
        <el-form-item label="售后状态：">{{ handleRow?.statusName }}</el-form-item>
        <el-form-item v-if="handleStatus === 2" label="退款金额：" required>
          <el-input-number v-model="handleForm.refundAmount" :min="0" :precision="2" :step="0.01" style="width: 220px" />
          <div class="form-extra">最大退款金额 ¥{{ money(handleRow?.suggestRefundAmount ?? handleRow?.refundAmount) }}</div>
        </el-form-item>
        <el-form-item v-if="handleStatus === 2 && Number(handleRow?.aftersaleType) === 1" label="退货地址：" required>
          <el-input v-model="handleForm.returnAddress" type="textarea" :rows="5" placeholder="请输入退货地址" />
        </el-form-item>
        <el-form-item v-if="handleStatus === 3" label="拒绝说明：" required>
          <el-input v-model="handleForm.reply" type="textarea" :rows="5" placeholder="请输入拒绝说明" />
        </el-form-item>
        <el-form-item>
          <div class="dialog-actions">
            <el-button type="primary" :loading="submitting" @click="submitHandle">
              {{ handleStatus === 2 ? '同意退款' : '拒绝退款' }}
            </el-button>
            <el-button @click="handleVisible = false">取消</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 拒收弹窗 -->
    <el-dialog v-model="rejectVisible" title="拒收商品" width="520px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="拒收理由：" required>
          <el-input v-model="rejectReply" type="textarea" :rows="4" placeholder="请输入拒收理由（最多 200 字）" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitRejectReceive">确认拒收</el-button>
      </template>
    </el-dialog>

    <!-- 售后标记 / 订单备注（原版 order/order/src/EditSign.vue 联动订单 setAdminNote） -->
    <el-dialog v-model="signVisible" title="订单标记" width="480px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="标记">
          <el-radio-group v-model="signForm.mark">
            <el-radio :value="0">无</el-radio>
            <el-radio :value="1">红</el-radio>
            <el-radio :value="2">黄</el-radio>
            <el-radio :value="3">绿</el-radio>
            <el-radio :value="4">蓝</el-radio>
            <el-radio :value="5">紫</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="signForm.adminNote" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="signVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitSign">提交</el-button>
      </template>
    </el-dialog>

    <!-- 售后协商记录 / 凭证（原版 Info 协商记录 + record 接口） -->
    <el-dialog v-model="recordVisible" title="添加协商记录" width="620px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="售后编号：">
          <span>{{ recordRow?.aftersalesSn }}</span>
        </el-form-item>
        <el-form-item label="记录内容：" required>
          <el-input v-model="recordForm.logInfo" type="textarea" :rows="4" placeholder="请输入协商记录内容" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="物流公司：">
          <el-input v-model="recordForm.logisticsName" placeholder="买家回寄物流公司（选填）" style="width: 320px" />
        </el-form-item>
        <el-form-item label="物流单号：">
          <el-input v-model="recordForm.trackingNo" placeholder="买家回寄物流单号（选填）" style="width: 320px" />
        </el-form-item>
        <el-form-item label="凭证图片：">
          <div class="voucher-list">
            <div v-for="(_, idx) in recordForm.pics" :key="idx" class="voucher-item">
              <ImageUpload v-model="recordForm.pics[idx]" />
              <el-button type="danger" link size="small" @click="recordForm.pics.splice(idx, 1)">删除</el-button>
            </div>
            <el-button type="primary" link @click="recordForm.pics.push('')">+ 添加凭证图片</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRecord">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 售后管理（复刻 java/view/admin/src/views/order/aftersales/List.vue 结构）。
 *
 * 接口（Rust `routes/trade/aftersales.rs`）：
 * - 列表 `GET /order/aftersales/list`（AftersalesListQuery）
 * - 详情 `GET /order/aftersales/detail?id=`
 * - 下拉 `GET /order/aftersales/applyType`、`GET /order/aftersales/returnGoodsStatus`
 * - 审核 `POST /order/aftersales/update`（AftersalesEditParam：status 2 同意 / 3 拒绝）
 * - 确认收货 `POST /order/aftersales/receive`、拒收 `POST /order/aftersales/rejectReceive`
 * - 完成 `POST /order/aftersales/complete`
 */
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Refresh } from '@element-plus/icons-vue';
import request from '@/utils/request';
import ImageUpload from '@/components/widgets/ImageUpload.vue';

interface AftersalesItem {
  aftersalesItemId: number;
  productName: string;
  picThumb: string;
  skuValue: string;
  price: number;
  quantity: number;
  number: number;
  taxRate: number;
}

interface AftersalesLog {
  logId: number;
  addTime: string;
  adminName: string;
  logInfo: string;
  refundDesc: string;
}

interface AftersalesRow {
  aftersaleId: number;
  aftersalesSn: string;
  orderId: number;
  orderSn: string;
  addTime: string;
  userName?: string;
  status: number;
  statusName: string;
  aftersaleType: number;
  aftersalesTypeName: string;
  aftersaleReason?: string;
  refundAmount: string | number;
  actualRefundAmount: number;
  suggestRefundAmount?: number;
  description: string;
  logisticsName: string;
  trackingNo: string;
  returnAddress?: string;
  returnGoodsTip: string;
  reply: string;
  auditTime: string;
  aftersalesItems: AftersalesItem[];
  aftersalesLog: AftersalesLog[];
}

const loading = ref(false);
const list = ref<AftersalesRow[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(15);
const typeList = ref<Record<number, string>>({});
const statusList = ref<Record<number, string>>({});

const filters = reactive({
  outTradeNo: '',
  aftersalesSn: '',
  aftersaleType: undefined as number | undefined,
  status: undefined as number | undefined
});

function money(value: unknown) {
  return Number(value ?? 0).toFixed(2);
}

function resolveImage(url?: string) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/static')) return url;
  return `/static/${url}`;
}

function statusTagType(status: number): 'success' | 'warning' | 'danger' | 'info' | 'primary' {
  if (status === 1) return 'warning';
  if (status === 2 || status === 4) return 'primary';
  if (status === 3 || status === 7) return 'info';
  if (status === 5) return 'success';
  if (status === 6) return 'success';
  return 'info';
}

async function loadConfig() {
  const [types, statuses] = await Promise.all([
    request<any>({ url: '/order/aftersales/applyType', method: 'get' }),
    request<any>({ url: '/order/aftersales/returnGoodsStatus', method: 'get' })
  ]);
  typeList.value = types || {};
  statusList.value = statuses || {};
}

async function loadList() {
  loading.value = true;
  try {
    const res = await request<any>({
      url: '/order/aftersales/list',
      method: 'get',
      params: {
        page: page.value,
        size: size.value,
        outTradeNo: filters.outTradeNo || undefined,
        aftersalesSn: filters.aftersalesSn || undefined,
        aftersaleType: filters.aftersaleType,
        status: filters.status
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
  filters.outTradeNo = '';
  filters.aftersalesSn = '';
  filters.aftersaleType = undefined;
  filters.status = undefined;
  handleSearch();
}

// ————————————————— 详情 —————————————————
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<AftersalesRow | null>(null);

async function openDetail(row: AftersalesRow) {
  detailVisible.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    detail.value = await request<any>({
      url: '/order/aftersales/detail',
      method: 'get',
      params: { id: row.aftersaleId }
    });
  } finally {
    detailLoading.value = false;
  }
}

// ————————————————— 处理（同意/拒绝） —————————————————
const handleVisible = ref(false);
const handleStatus = ref(2);
const handleRow = ref<AftersalesRow | null>(null);
const submitting = ref(false);
const handleForm = reactive({
  refundAmount: 0,
  returnAddress: '',
  reply: ''
});

function openHandle(row: AftersalesRow, status: number) {
  handleRow.value = row;
  handleStatus.value = status;
  handleForm.refundAmount = Number(row.suggestRefundAmount ?? row.refundAmount ?? 0);
  handleForm.returnAddress = row.returnAddress || '';
  handleForm.reply = '';
  handleVisible.value = true;
}

async function submitHandle() {
  if (!handleRow.value) return;
  if (handleStatus.value === 2 && Number(handleForm.refundAmount) <= 0) {
    ElMessage.warning('请输入退款金额');
    return;
  }
  if (handleStatus.value === 2 && Number(handleRow.value.aftersaleType) === 1 && !handleForm.returnAddress.trim()) {
    ElMessage.warning('请输入退货地址');
    return;
  }
  if (handleStatus.value === 3 && !handleForm.reply.trim()) {
    ElMessage.warning('请输入拒绝说明');
    return;
  }
  submitting.value = true;
  try {
    await request({
      url: '/order/aftersales/update',
      method: 'post',
      data: {
        id: handleRow.value.aftersaleId,
        status: handleStatus.value,
        refundAmount: handleStatus.value === 2 ? handleForm.refundAmount : undefined,
        returnAddress: handleStatus.value === 2 ? handleForm.returnAddress : undefined,
        reply: handleForm.reply || undefined
      }
    });
    ElMessage.success('操作成功');
    handleVisible.value = false;
    loadList();
    if (detailVisible.value) {
      detail.value = await request<any>({ url: '/order/aftersales/detail', method: 'get', params: { id: handleRow.value.aftersaleId } });
    }
  } finally {
    submitting.value = false;
  }
}

// ————————————————— 确认收货 / 拒收 / 完成 —————————————————
async function confirmReceive(row: AftersalesRow) {
  await ElMessageBox.confirm('确认已收到买家退回的商品吗？', '确认收货', { type: 'warning' });
  await request({ url: '/order/aftersales/receive', method: 'post', data: { id: row.aftersaleId } });
  ElMessage.success('确认收货成功');
  loadList();
}

const rejectVisible = ref(false);
const rejectReply = ref('');
const rejectRow = ref<AftersalesRow | null>(null);

function openRejectReceive(row: AftersalesRow) {
  rejectRow.value = row;
  rejectReply.value = '';
  rejectVisible.value = true;
}

async function submitRejectReceive() {
  if (!rejectRow.value) return;
  if (!rejectReply.value.trim()) {
    ElMessage.warning('请输入拒收理由');
    return;
  }
  submitting.value = true;
  try {
    await request({
      url: '/order/aftersales/rejectReceive',
      method: 'post',
      data: { id: rejectRow.value.aftersaleId, reply: rejectReply.value.trim() }
    });
    ElMessage.success('拒收成功');
    rejectVisible.value = false;
    loadList();
  } finally {
    submitting.value = false;
  }
}

async function completeAftersales(row: AftersalesRow) {
  await ElMessageBox.confirm('确认完成该售后单吗？', '完成售后', { type: 'warning' });
  await request({ url: '/order/aftersales/complete', method: 'post', data: { id: row.aftersaleId } });
  ElMessage.success('售后已完成');
  detailVisible.value = false;
  loadList();
}

// ————————————————— 标记 / 协商记录 —————————————————
const signVisible = ref(false);
const signForm = reactive({ orderId: 0, adminNote: '', mark: 0 });

function openSign(row: AftersalesRow) {
  signForm.orderId = row.orderId;
  signForm.adminNote = '';
  signForm.mark = 0;
  signVisible.value = true;
}

async function submitSign() {
  if (!signForm.orderId) {
    ElMessage.warning('未关联订单，无法标记');
    return;
  }
  submitting.value = true;
  try {
    await request({
      url: '/order/order/setAdminNote',
      method: 'post',
      data: { id: signForm.orderId, adminNote: signForm.adminNote, mark: signForm.mark }
    });
    ElMessage.success('标记已更新');
    signVisible.value = false;
  } finally {
    submitting.value = false;
  }
}

const recordVisible = ref(false);
const recordRow = ref<AftersalesRow | null>(null);
const recordForm = reactive({
  logInfo: '',
  logisticsName: '',
  trackingNo: '',
  pics: [] as string[]
});

function openRecord(row: AftersalesRow) {
  recordRow.value = row;
  recordForm.logInfo = '';
  recordForm.logisticsName = row.logisticsName || '';
  recordForm.trackingNo = row.trackingNo || '';
  recordForm.pics = [];
  recordVisible.value = true;
}

async function submitRecord() {
  if (!recordRow.value) return;
  if (!recordForm.logInfo.trim()) {
    ElMessage.warning('请输入协商记录内容');
    return;
  }
  submitting.value = true;
  try {
    await request({
      url: '/order/aftersales/record',
      method: 'post',
      data: {
        aftersaleId: recordRow.value.aftersaleId,
        logInfo: recordForm.logInfo.trim(),
        logisticsName: recordForm.logisticsName || undefined,
        trackingNo: recordForm.trackingNo || undefined,
        returnPic: recordForm.pics.filter((p) => p).map((p) => ({ picUrl: p }))
      }
    });
    ElMessage.success('协商记录已添加');
    recordVisible.value = false;
    if (detailVisible.value && detail.value) {
      detail.value = await request<any>({
        url: '/order/aftersales/detail',
        method: 'get',
        params: { id: detail.value.aftersaleId }
      });
    }
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

.group-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
  color: var(--el-text-color-regular);
  font-size: 12px;
}

.item-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
}

.item-name {
  flex: 1;
  min-width: 160px;
}

.item-price {
  width: 150px;
  text-align: right;
}

.muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.danger-text {
  color: var(--el-color-danger);
}

.row-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.section-title {
  margin: 16px 0 8px;
  font-size: 14px;
  font-weight: 600;
}

.drawer-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 16px;
}

.form-extra {
  margin-left: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.dialog-actions {
  display: flex;
  gap: 10px;
}

.voucher-list {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 10px;
}

.voucher-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
