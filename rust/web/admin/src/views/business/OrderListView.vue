<template>
  <div class="business-page">
    <SchemaTable :schema="schema" />

    <!-- 订单详情抽屉 -->
    <el-drawer v-model="detailVisible" title="订单详情" size="720px" destroy-on-close>
      <div v-loading="detailLoading" class="order-detail">
        <template v-if="detail">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="订单号">{{ detail.orderSn }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="statusTagType(detail.orderStatus)">{{ detail.orderStatusName || statusText(detail.orderStatus) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付状态">{{ detail.payStatusName || '未支付' }}</el-descriptions-item>
            <el-descriptions-item label="发货状态">{{ detail.shippingStatusName || '未发货' }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ detail.addTime }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ detail.payTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="收货人">{{ detail.consignee }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.mobile }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ detail.userAddress || detail.address }}</el-descriptions-item>
            <el-descriptions-item label="订单金额">¥{{ money(detail.totalAmount) }}</el-descriptions-item>
            <el-descriptions-item label="实付金额">¥{{ money(detail.paidAmount) }}</el-descriptions-item>
            <el-descriptions-item label="运费">¥{{ money(detail.shippingFee) }}</el-descriptions-item>
            <el-descriptions-item label="优惠金额">¥{{ money(detail.discountAmount) }}</el-descriptions-item>
            <el-descriptions-item label="物流公司">{{ detail.logisticsName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="快递单号">{{ detail.trackingNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="买家留言" :span="2">{{ detail.buyerNote || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商家备注" :span="2">{{ detail.adminNote || '-' }}</el-descriptions-item>
          </el-descriptions>

          <h4 class="section-title">商品明细</h4>
          <el-table :data="detail.items || []" border size="small">
            <el-table-column label="商品" min-width="220">
              <template #default="{ row }">
                <div class="product-cell">
                  <el-image
                    v-if="row.picThumb"
                    :src="resolveImage(row.picThumb)"
                    style="width: 40px; height: 40px; border-radius: 4px"
                    fit="cover"
                  />
                  <div>
                    <div>{{ row.productName }}</div>
                    <div v-if="row.skuValue" class="sku-text">{{ row.skuValue }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="productSn" label="货号" width="130" />
            <el-table-column label="单价" width="100" align="right">
              <template #default="{ row }">¥{{ money(row.price) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
            <el-table-column prop="deliveryQuantity" label="已发" width="80" align="center" />
          </el-table>

          <div class="drawer-actions">
            <el-button type="primary" :disabled="detail.orderStatus !== 1" @click="openDeliver(detail)">发货</el-button>
            <el-button :disabled="detail.payStatus === 1" @click="openMoney(detail)">改价</el-button>
            <el-button @click="openLogistics(detail)">物流跟踪</el-button>
          </div>
        </template>
        <el-empty v-else description="未获取到订单详情" />
      </div>
    </el-drawer>

    <!-- 发货弹窗 -->
    <el-dialog v-model="deliverVisible" title="订单发货" width="600px" destroy-on-close>
      <el-form :model="deliverForm" label-width="110px">
        <el-form-item label="发货方式" required>
          <el-select v-model="deliverForm.shippingMethod" style="width: 260px">
            <el-option :value="1" label="物流发货（快递）" />
            <el-option :value="2" label="商家配送" />
            <el-option :value="3" label="无需配送（虚拟/自提）" />
            <el-option :value="4" label="电子面单" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="deliverForm.shippingMethod === 1 || deliverForm.shippingMethod === 4" label="物流公司ID" required>
          <el-input-number v-model="deliverForm.logisticsId" :min="1" />
        </el-form-item>
        <el-form-item v-if="deliverForm.shippingMethod === 1" label="快递单号" required>
          <el-input v-model="deliverForm.trackingNo" placeholder="请输入快递单号" style="width: 320px" />
        </el-form-item>
        <el-form-item label="发货明细">
          <div class="deliver-items">
            <div v-for="it in deliverItems" :key="it.itemId" class="deliver-item">
              <span class="item-name">{{ it.productName }}</span>
              <el-input-number v-model="it.toDeliveryQuantity" :min="0" :max="it.maxQuantity" size="small" />
              <span class="item-max">/ 待发 {{ it.maxQuantity }}</span>
            </div>
            <el-empty v-if="deliverItems.length === 0" description="暂无可发货明细" :image-size="60" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitDeliver">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 改价弹窗 -->
    <el-dialog v-model="moneyVisible" title="订单改价" width="520px" destroy-on-close>
      <el-form :model="moneyForm" label-width="110px">
        <el-form-item label="运费(元)">
          <el-input-number v-model="moneyForm.shippingFee" :min="0" :precision="2" :step="0.01" />
        </el-form-item>
        <el-form-item label="发票费(元)">
          <el-input-number v-model="moneyForm.invoiceFee" :min="0" :precision="2" :step="0.01" />
        </el-form-item>
        <el-form-item label="服务费(元)">
          <el-input-number v-model="moneyForm.serviceFee" :min="0" :precision="2" :step="0.01" />
        </el-form-item>
        <el-form-item label="优惠金额(元)">
          <el-input-number v-model="moneyForm.discountAmount" :min="0" :precision="2" :step="0.01" />
        </el-form-item>
        <el-form-item label="">
          <span class="dialog-tip">优惠金额会按明细小计比例分摊到各订单商品价格。</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="moneyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitMoney">确认改价</el-button>
      </template>
    </el-dialog>

    <!-- 物流跟踪弹窗 -->
    <el-dialog v-model="logisticsVisible" title="物流跟踪" width="620px" destroy-on-close>
      <div v-loading="logisticsLoading">
        <el-descriptions :column="2" border size="small" v-if="logistics">
          <el-descriptions-item label="物流单号">{{ logistics.logisticCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="物流状态">{{ logistics.state || logistics.stateEx || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-timeline v-if="logistics?.traces?.length" class="trace-timeline">
          <el-timeline-item
            v-for="(t, idx) in logistics.traces"
            :key="idx"
            :timestamp="t.acceptTime || ''"
          >
            {{ t.acceptStation || t.action || t.location || '' }}
          </el-timeline-item>
        </el-timeline>
        <el-alert
          v-else-if="logistics"
          type="info"
          :closable="false"
          title="暂无物流轨迹"
          :description="logistics.success === '0' ? 'Rust 后端未接入第三方物流轨迹查询，仅返回订单运单快照。' : ''"
        />
        <el-empty v-else description="未获取到物流信息" />
      </div>
    </el-dialog>

    <!-- 改收货人弹窗（复刻原版 order/order/src/EditConsignee.vue） -->
    <el-dialog v-model="consigneeVisible" title="修改收货人信息" width="560px" destroy-on-close>
      <el-form :model="consigneeForm" label-width="100px">
        <el-form-item label="收货人" required>
          <el-input v-model="consigneeForm.consignee" placeholder="收货人不能为空" />
        </el-form-item>
        <el-form-item label="所在地区">
          <el-input v-model="consigneeForm.regionIdsText" placeholder="地区 ID，多个用英文逗号分隔，如 1,2,3" />
        </el-form-item>
        <el-form-item label="手机" required>
          <el-input v-model="consigneeForm.mobile" placeholder="手机不能为空" />
        </el-form-item>
        <el-form-item label="地址" required>
          <el-input v-model="consigneeForm.address" placeholder="地址不能为空" />
        </el-form-item>
        <el-form-item label="邮编">
          <el-input v-model="consigneeForm.postcode" />
        </el-form-item>
        <el-form-item label="电子邮件">
          <el-input v-model="consigneeForm.email" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="consigneeForm.telephone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consigneeVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitConsignee">提交</el-button>
      </template>
    </el-dialog>

    <!-- 标记/备注弹窗（复刻原版 order/order/src/EditSign.vue） -->
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
  </div>
</template>

<script setup lang="ts">
/**
 * 订单管理：列表 + 详情抽屉 / 发货 / 改价 / 物流跟踪。
 *
 * 接口（Rust `routes/trade/order_admin.rs`）：
 * - 列表：`GET /adminapi/order/order/list`
 * - 详情：`GET /adminapi/order/order/detail?id=`
 * - 发货：`POST /adminapi/order/order/deliver`（DeliverOrderParam）
 * - 改价：`POST /adminapi/order/order/modifyMoney`（OrderModifyMoneyParam）
 * - 物流：`GET /adminapi/order/order/shippingInfo?id=`（ShippingInfoVO）
 */
import { computed, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import SchemaTable from '@/components/schema/SchemaTable.vue';
import request from '@/utils/request';
import type { TableSchema } from '@/components/schema/types';

const ORDER_STATUS: Record<number, { text: string; type: 'success' | 'warning' | 'danger' | 'info' | 'primary' }> = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '待发货', type: 'primary' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已取消', type: 'info' },
  4: { text: '已失效', type: 'info' },
  5: { text: '已完成', type: 'success' }
};

const PAY_STATUS: Record<number, { text: string; type: 'success' | 'warning' | 'danger' | 'info' | 'primary' }> = {
  0: { text: '未支付', type: 'warning' },
  1: { text: '已支付', type: 'success' }
};

const SHIPPING_STATUS: Record<number, { text: string; type: 'success' | 'warning' | 'danger' | 'info' | 'primary' }> = {
  0: { text: '未发货', type: 'warning' },
  1: { text: '已发货', type: 'primary' },
  2: { text: '部分发货', type: 'primary' },
  3: { text: '已收货', type: 'success' }
};

function statusText(status?: number) {
  return ORDER_STATUS[status ?? 0]?.text ?? '-';
}

function statusTagType(status?: number): 'success' | 'warning' | 'danger' | 'info' | 'primary' {
  return ORDER_STATUS[status ?? 0]?.type ?? 'info';
}

function money(value: unknown) {
  return Number(value ?? 0).toFixed(2);
}

function resolveImage(url?: string) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/static')) return url;
  return `/static/${url}`;
}

// ————————————————— 详情 —————————————————
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<any>(null);

async function fetchOrderDetail(id: number) {
  return request<any>({ url: '/order/order/detail', method: 'get', params: { id } });
}

async function openDetail(row: any) {
  detailVisible.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    detail.value = await fetchOrderDetail(row.orderId);
  } finally {
    detailLoading.value = false;
  }
}

// ————————————————— 发货 —————————————————
const deliverVisible = ref(false);
const submitting = ref(false);
const deliverForm = reactive({
  id: 0,
  shippingMethod: 1,
  logisticsId: undefined as number | undefined,
  trackingNo: ''
});

interface DeliverItem {
  itemId: number;
  productName: string;
  maxQuantity: number;
  toDeliveryQuantity: number;
}

const deliverItems = ref<DeliverItem[]>([]);

async function openDeliver(row: any) {
  const order = detail.value?.orderId === row.orderId ? detail.value : await fetchOrderDetail(row.orderId);
  if (!order) return;
  deliverForm.id = order.orderId;
  deliverForm.shippingMethod = 1;
  deliverForm.logisticsId = order.logisticsId > 0 ? order.logisticsId : undefined;
  deliverForm.trackingNo = '';
  deliverItems.value = (order.items || [])
    .map((it: any) => {
      const maxQuantity = Math.max(0, (it.quantity ?? 0) - (it.deliveryQuantity ?? 0));
      return {
        itemId: it.itemId,
        productName: it.productName,
        maxQuantity,
        toDeliveryQuantity: maxQuantity
      };
    })
    .filter((it: DeliverItem) => it.itemId && it.maxQuantity > 0);
  deliverVisible.value = true;
}

async function submitDeliver() {
  const method = deliverForm.shippingMethod;
  if ((method === 1 || method === 4) && !deliverForm.logisticsId) {
    ElMessage.warning('请填写物流公司 ID');
    return;
  }
  if (method === 1 && !deliverForm.trackingNo.trim()) {
    ElMessage.warning('请填写快递单号');
    return;
  }
  const deliverData = deliverItems.value
    .filter((it) => it.toDeliveryQuantity > 0)
    .map((it) => ({ itemId: it.itemId, toDeliveryQuantity: it.toDeliveryQuantity }));
  if (deliverData.length === 0) {
    ElMessage.warning('请填写本次发货数量');
    return;
  }
  submitting.value = true;
  try {
    await request({
      url: '/order/order/deliver',
      method: 'post',
      data: {
        id: deliverForm.id,
        shippingMethod: method,
        logisticsId: deliverForm.logisticsId,
        trackingNo: deliverForm.trackingNo,
        deliverData
      }
    });
    ElMessage.success('发货成功');
    deliverVisible.value = false;
    detail.value = await fetchOrderDetail(deliverForm.id);
  } finally {
    submitting.value = false;
  }
}

// ————————————————— 改价 —————————————————
const moneyVisible = ref(false);
const moneyForm = reactive({
  id: 0,
  shippingFee: 0,
  invoiceFee: 0,
  serviceFee: 0,
  discountAmount: 0
});

function openMoney(row: any) {
  moneyForm.id = row.orderId;
  moneyForm.shippingFee = Number(row.shippingFee ?? 0);
  moneyForm.invoiceFee = Number(row.invoiceFee ?? 0);
  moneyForm.serviceFee = Number(row.serviceFee ?? 0);
  moneyForm.discountAmount = Number(row.discountAmount ?? 0);
  moneyVisible.value = true;
}

async function submitMoney() {
  submitting.value = true;
  try {
    await request({
      url: '/order/order/modifyMoney',
      method: 'post',
      data: {
        id: moneyForm.id,
        shippingFee: moneyForm.shippingFee,
        invoiceFee: moneyForm.invoiceFee,
        serviceFee: moneyForm.serviceFee,
        discountAmount: moneyForm.discountAmount
      }
    });
    ElMessage.success('改价成功');
    moneyVisible.value = false;
    detail.value = await fetchOrderDetail(moneyForm.id);
  } finally {
    submitting.value = false;
  }
}

// ————————————————— 物流 —————————————————
const logisticsVisible = ref(false);
const logisticsLoading = ref(false);
const logistics = ref<any>(null);

async function openLogistics(row: any) {
  logisticsVisible.value = true;
  logisticsLoading.value = true;
  logistics.value = null;
  try {
    logistics.value = await request<any>({
      url: '/order/order/shippingInfo',
      method: 'get',
      params: { id: row.orderId }
    });
  } finally {
    logisticsLoading.value = false;
  }
}

// ————————————————— 改收货人 —————————————————
const consigneeVisible = ref(false);
const consigneeForm = reactive({
  id: 0,
  consignee: '',
  mobile: '',
  telephone: '',
  email: '',
  postcode: '',
  regionIdsText: '',
  address: ''
});

function openConsignee(row: any) {
  consigneeForm.id = row.orderId;
  consigneeForm.consignee = row.consignee ?? '';
  consigneeForm.mobile = row.mobile ?? '';
  consigneeForm.telephone = '';
  consigneeForm.email = row.email ?? '';
  consigneeForm.postcode = '';
  consigneeForm.regionIdsText = Array.isArray(row.regionIds) ? row.regionIds.join(',') : '';
  consigneeForm.address = row.address ?? '';
  consigneeVisible.value = true;
}

async function submitConsignee() {
  if (!consigneeForm.consignee.trim()) {
    ElMessage.warning('收货人不能为空');
    return;
  }
  if (!consigneeForm.mobile.trim()) {
    ElMessage.warning('手机不能为空');
    return;
  }
  if (!consigneeForm.address.trim()) {
    ElMessage.warning('地址不能为空');
    return;
  }
  submitting.value = true;
  try {
    await request({
      url: '/order/order/modifyConsignee',
      method: 'post',
      data: {
        id: consigneeForm.id,
        consignee: consigneeForm.consignee,
        mobile: consigneeForm.mobile,
        telephone: consigneeForm.telephone || undefined,
        email: consigneeForm.email || undefined,
        postcode: consigneeForm.postcode || undefined,
        regionIds: consigneeForm.regionIdsText
          .split(',')
          .map((s) => Number(s.trim()))
          .filter((n) => n > 0),
        address: consigneeForm.address
      }
    });
    ElMessage.success('收货人信息已修改');
    consigneeVisible.value = false;
  } finally {
    submitting.value = false;
  }
}

// ————————————————— 标记 / 备注 —————————————————
const signVisible = ref(false);
const signForm = reactive({ id: 0, adminNote: '', mark: 0 });

function openSign(row: any) {
  signForm.id = row.orderId;
  signForm.adminNote = row.adminNote ?? '';
  signForm.mark = Number(row.mark ?? 0);
  signVisible.value = true;
}

async function submitSign() {
  submitting.value = true;
  try {
    await request({
      url: '/order/order/setAdminNote',
      method: 'post',
      data: { id: signForm.id, adminNote: signForm.adminNote, mark: signForm.mark }
    });
    ElMessage.success('标记已更新');
    signVisible.value = false;
  } finally {
    submitting.value = false;
  }
}

// ————————————————— 拆单 / 确认收货 / 导出 —————————————————
async function splitOrder(row: any) {
  await ElMessageBox.confirm(`确定对订单 ${row.orderSn} 执行拆单吗？`, '拆单', { type: 'warning' });
  const res = await request<any>({ url: '/order/order/splitStoreOrder', method: 'post', data: { id: row.orderId } });
  ElMessage.success(res?.split ? '拆单成功' : '拆单请求已提交');
}

async function confirmReceipt(row: any) {
  await ElMessageBox.confirm(`确认订单 ${row.orderSn} 已收货吗？`, '确认收货', { type: 'warning' });
  await request({ url: '/order/order/confirmReceipt', method: 'post', data: { id: row.orderId } });
  ElMessage.success('确认收货成功');
}

async function exportOrders() {
  const res: any = await request({
    url: '/order/order/orderExport',
    method: 'get',
    responseType: 'blob'
  });
  const blob: Blob = res?.data instanceof Blob ? res.data : new Blob([res?.data]);
  const disposition = res?.headers?.['content-disposition'] || '';
  const match = /filename\*?=(?:UTF-8'')?["']?([^"';]+)/i.exec(disposition);
  const fileName = match ? decodeURIComponent(match[1]) : `订单导出_${Date.now()}.xlsx`;
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
  ElMessage.success('导出文件已开始下载');
}

const schema = computed<TableSchema>(() => ({
  title: '订单列表',
  apiList: '/order/order/list',
  primaryKey: 'orderId',
  searchFields: [
    { prop: 'keyword', label: '关键词', type: 'input', placeholder: '订单号 / 收货人 / 手机号' },
    {
      prop: 'orderStatus',
      label: '订单状态',
      type: 'select',
      options: Object.entries(ORDER_STATUS).map(([value, v]) => ({ label: v.text, value: Number(value) }))
    },
    {
      prop: 'payStatus',
      label: '支付状态',
      type: 'select',
      options: [
        { label: '未支付', value: 0 },
        { label: '已支付', value: 1 }
      ]
    },
    {
      prop: 'shippingStatus',
      label: '发货状态',
      type: 'select',
      options: [
        { label: '未发货', value: 0 },
        { label: '已发货', value: 1 },
        { label: '部分发货', value: 2 },
        { label: '已收货', value: 3 }
      ]
    }
  ],
  columns: [
    { prop: 'orderSn', label: '订单号', minWidth: 180 },
    { prop: 'consignee', label: '收货人', width: 110 },
    { prop: 'mobile', label: '手机号', width: 130 },
    { prop: 'totalAmount', label: '应付金额', type: 'money', width: 120 },
    {
      prop: 'orderStatus',
      label: '订单状态',
      type: 'tag',
      width: 100,
      tagMap: ORDER_STATUS
    },
    {
      prop: 'payStatus',
      label: '支付状态',
      type: 'tag',
      width: 100,
      tagMap: PAY_STATUS
    },
    {
      prop: 'shippingStatus',
      label: '发货状态',
      type: 'tag',
      width: 100,
      tagMap: SHIPPING_STATUS
    },
    { prop: 'addTime', label: '下单时间', minWidth: 160 }
  ],
  extraButtons: [{ label: '导出订单', type: 'default', onClick: exportOrders }],
  actions: [
    { label: '详情', type: 'primary', onClick: openDetail },
    { label: '发货', type: 'success', show: (row) => row.orderStatus === 1, onClick: openDeliver },
    { label: '改价', type: 'warning', show: (row) => row.payStatus !== 1, onClick: openMoney },
    { label: '物流', type: 'info', show: (row) => !!row.trackingNo, onClick: openLogistics },
    { label: '改收货人', type: 'primary', show: (row) => row.orderStatus <= 1, onClick: openConsignee },
    { label: '标记', type: 'info', onClick: openSign },
    { label: '拆单', type: 'warning', show: (row) => row.payStatus === 1 && row.isStoreSplited !== 1, onClick: splitOrder },
    { label: '确认收货', type: 'success', show: (row) => row.orderStatus === 2, onClick: confirmReceipt }
  ]
}));
</script>

<style scoped>
.business-page {
  width: 100%;
}

.order-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  margin: 8px 0 0;
  font-size: 14px;
  font-weight: 600;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sku-text {
  font-size: 12px;
  color: #909399;
}

.drawer-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 12px;
}

.deliver-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.deliver-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-name {
  min-width: 180px;
}

.item-max {
  font-size: 12px;
  color: #909399;
}

.dialog-tip {
  font-size: 12px;
  color: #909399;
}

.trace-timeline {
  margin-top: 16px;
  padding-left: 4px;
}
</style>
