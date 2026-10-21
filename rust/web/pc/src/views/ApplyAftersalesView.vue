<template>
  <div class="tig-apply-aftersales-page">
    <div class="container member-layout">
      <MemberNav />

      <div class="info-row">
        <!-- 顶部切换 -->
        <div class="member-top-tabs">
          <router-link to="/member/aftersales" class="top-tab">售后申请</router-link>
          <router-link :to="{ path: '/member/aftersales/apply', query: route.query }" class="top-tab active">申请记录</router-link>
        </div>

        <div v-loading="loading" class="apply-card">
          <template v-if="!loading && !applyData">
            <el-empty description="未找到可申请售后的订单">
              <el-button type="primary" @click="$router.push('/member/orders')">返回我的订单</el-button>
            </el-empty>
          </template>

          <template v-else-if="applyData">
            <!-- 商品表格 -->
            <div class="info-card">
              <table>
                <thead class="table-header">
                  <tr>
                    <th class="header-cell">商品名称</th>
                    <th class="header-cell" style="width: 90px">商品价格</th>
                    <th class="header-cell" style="width: 90px">购买数量</th>
                    <th class="header-cell" style="width: 90px">小计</th>
                    <th class="header-cell" style="width: 100px">可售后数量</th>
                    <th class="header-cell" style="width: 110px">售后数量</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in applyData.list" :key="item.itemId">
                    <td>
                      <router-link :to="`/product/${item.productId}`" class="goods-cell">
                        <img :src="imageUrl(item.picThumb)" :alt="item.productName || ''" class="pro-pic" />
                        <div class="text-ellipsis">
                          <ul>
                            <li>货号：{{ item.productSn }}</li>
                            <li>{{ item.productName }}</li>
                            <li>
                              <span v-for="(sku, index) in skuPairs(item)" :key="index">
                                <span class="sku-name">{{ sku.name }}：</span>{{ sku.value }}<span v-if="index < skuPairs(item).length - 1">；</span>
                              </span>
                            </li>
                          </ul>
                        </div>
                      </router-link>
                    </td>
                    <td class="price-cell">¥{{ formatPrice(item.price) }}</td>
                    <td>{{ item.quantity }}</td>
                    <td class="price-cell">¥{{ formatPrice(item.subtotal) }}</td>
                    <td>{{ item.canApplyQuantity }}</td>
                    <td>
                      <el-input-number
                        v-model.number="item.number"
                        :max="item.canApplyQuantity"
                        :min="0"
                        :disabled="item.isGift === 1"
                        placeholder="请输入数量"
                        size="small"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
              <div v-if="!applyData.list.length" class="muted">该订单暂无可申请售后的商品</div>
            </div>

            <!-- 表单 -->
            <div class="file-card">
              <el-form ref="formRef" label-width="auto" :model="formState" class="form-body" label-suffix="：">
                <el-form-item label="服务类型" prop="aftersaleType" :rules="[{ required: true, message: '请选择服务类型' }]">
                  <el-radio-group v-model="formState.aftersaleType">
                    <el-radio v-for="(name, code) in aftersaleType" :key="code" :value="Number(code)">{{ name }}</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item
                  v-if="afterSalesReasonEnabled"
                  label="售后原因"
                  prop="aftersaleReason"
                  :rules="[{ required: true, message: '请选择售后原因' }]"
                >
                  <el-select v-model="formState.aftersaleReason" placeholder="请选择" style="width: 500px">
                    <el-option v-for="reason in aftersaleReason" :key="reason" :label="reason" :value="reason" />
                  </el-select>
                </el-form-item>
                <el-form-item label="预计退款金额" prop="refundAmount">
                  <el-input disabled :model-value="formatPrice(formState.refundAmount)" placeholder="请输入申请金额" />
                </el-form-item>
                <el-form-item label="问题描述" prop="description" :rules="[{ required: true, message: '问题描述不能为空' }]">
                  <el-input v-model="formState.description" type="textarea" :rows="4" placeholder="请输入问题描述" />
                </el-form-item>
                <el-form-item label="上传文件">
                  <UploadImage v-model:upload-list="formState.pics" :limit="5" />
                </el-form-item>
                <el-form-item label="">
                  <el-button type="primary" size="large" :loading="submitting" @click="onSubmit">提交</el-button>
                  <el-button size="large" @click="$router.back()">取消</el-button>
                </el-form-item>
              </el-form>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 申请售后（对齐原版 `pages/member/return/Info.vue`）。
 *
 * 商品表格（可售后数量/售后数量输入）→ 服务类型 → 售后原因（开关控制）→
 * 预计退款金额（自动计算）→ 问题描述 → 上传凭证（`POST /api/user/user/uploadImg`）→ 提交。
 * 后端：`GET /user/aftersales/applyData`、`GET /user/aftersales/config`、`POST /user/aftersales/create`。
 */
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance } from 'element-plus';
import { aftersaleApi } from '@/api';
import type { AftersalesApplyData, AftersalesConfig, UploadedPicture } from '@/api/types';
import MemberNav from '@/components/MemberNav.vue';
import UploadImage from '@/components/UploadImage.vue';
import { formatPrice, imageUrl } from '@/utils/format';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const submitting = ref(false);
const applyData = ref<AftersalesApplyData | null>(null);
const config = ref<AftersalesConfig | null>(null);
const formRef = ref<FormInstance>();

const aftersaleType = computed<Record<string, string>>(() => config.value?.aftersaleType ?? {});
const aftersaleReason = computed<string[]>(() => config.value?.aftersaleReason ?? []);
const afterSalesReasonEnabled = computed(() => Number(config.value?.afterSalesReasonEnabled) === 1);

const formState = reactive({
  orderId: 0,
  aftersaleType: 1,
  aftersaleReason: '',
  description: '',
  refundAmount: 0,
  pics: [] as UploadedPicture[],
});

const orderId = computed(() => Number(route.query.orderId) || 0);

function skuPairs(item: Record<string, any>): Array<{ name: string; value: string }> {
  const raw = item.skuData;
  if (!Array.isArray(raw)) return [];
  return raw
    .map((sku: any) => ({ name: String(sku?.name ?? ''), value: String(sku?.value ?? '') }))
    .filter((sku) => sku.name || sku.value);
}

/** 退款金额：按售后数量 × 商品实际价格，受订单实付比例约束。 */
watch(
  () => applyData.value?.list,
  (list) => {
    if (!list) return;
    let selectedAmount = 0;
    for (const item of list) {
      const unit = Number(item.attrPrice ?? 0) + Number(item.price ?? 0);
      selectedAmount += Number(item.number ?? 0) * unit;
    }
    formState.refundAmount = Number(selectedAmount.toFixed(2));
  },
  { deep: true },
);

async function load() {
  if (!orderId.value) {
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    const [data, cfg] = await Promise.all([
      aftersaleApi.applyData(orderId.value),
      aftersaleApi.config().catch(() => null),
    ]);
    applyData.value = data;
    config.value = cfg;
    formState.orderId = orderId.value;
    data.list.forEach((item) => {
      // 默认售后数量：赠品默认全部、其余默认 0（对齐原版）
      item.number = item.isGift === 1 ? item.canApplyQuantity : 0;
    });
    const firstType = Object.keys(cfg?.aftersaleType ?? {})[0];
    if (firstType) formState.aftersaleType = Number(firstType);
  } catch (err: any) {
    applyData.value = null;
    ElMessage.error(err?.message || '获取售后信息失败');
  } finally {
    loading.value = false;
  }
}

async function onSubmit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  const items = (applyData.value?.list ?? [])
    .filter((item) => Number(item.number) > 0)
    .map((item) => ({ orderItemId: item.itemId, number: Number(item.number) }));
  if (!items.length) {
    ElMessage.error('请填写售后数量');
    return;
  }
  submitting.value = true;
  try {
    await aftersaleApi.create({
      orderId: orderId.value,
      aftersaleType: formState.aftersaleType,
      aftersaleReason: afterSalesReasonEnabled.value ? formState.aftersaleReason : undefined,
      description: formState.description,
      refundAmount: formState.refundAmount,
      items,
      // 凭证图片字段随请求提交（后端 CreateAftersalesParam.pics）
      ...(formState.pics.length ? { pics: formState.pics as any } : {}),
    } as any);
    ElMessage.success('提交成功');
    router.replace('/member/aftersales');
  } finally {
    submitting.value = false;
  }
}

onMounted(load);
</script>

<style scoped lang="less">
.tig-apply-aftersales-page {
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

    &.active { color: var(--tig-red); font-weight: 700; background: #fff; border-bottom: 2px solid var(--tig-red); }
  }
}

.apply-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  min-height: 340px;
}

.info-card {
  margin-bottom: 24px;

  table {
    width: 100%;
    border-collapse: collapse;
    border: 1px solid #ddd;
  }

  .table-header { background: #f5f5f5; }

  .header-cell {
    border: 1px solid #ddd;
    padding: 10px;
    font-size: 13px;
    color: #666;
    font-weight: 600;
    text-align: center;
  }

  td {
    border: 1px solid #e8e8e8;
    padding: 10px;
    text-align: center;
    font-size: 13px;
    color: #555;
  }

  .goods-cell {
    display: flex;
    gap: 10px;
    text-align: left;

    .pro-pic {
      width: 44px;
      height: 44px;
      object-fit: cover;
      border: 1px solid #f0f0f0;
    }

    .text-ellipsis {
      line-height: 1.8;
      font-size: 12px;
      color: #666;
      overflow: hidden;
      .sku-name { font-weight: 600; }
    }
  }

  .price-cell { color: var(--tig-red) !important; }
}

.file-card {
  padding: 0 10px;

  .form-body { width: 680px; max-width: 100%; }
}

.muted { color: #999; font-size: 13px; text-align: center; padding: 10px 0; }
</style>
