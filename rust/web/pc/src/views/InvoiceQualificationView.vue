<template>
  <div class="tig-member-sub-page">
    <div class="container member-layout">
      <MemberNav />

      <div class="info-row">
        <div class="member-top-tabs">
          <router-link to="/member/orders" class="top-tab">发票申请</router-link>
          <span class="top-tab active">增票资质</span>
        </div>

        <div class="content-card" v-loading="loading">
          <!-- 温馨提示 -->
          <div class="tips">
            <p><strong>温馨提示</strong></p>
            <p>我们会在一个工作日内完成审核工作。</p>
            <p>1）资质审核结果查询时间：<span class="font-color">资质上传一到两个工作日后即可查询</span>。</p>
            <p>2）注意有效增值税专用发票开票资质仅为一个。</p>
          </div>

          <!-- 当前状态 -->
          <div v-show="!showEdit" class="remind-box">
            <div class="remind-line">
              <strong>您的增票资质：</strong>
              <span :class="statusClass">{{ statusLabel }}</span>
            </div>
            <el-button type="primary" size="small" class="mt10" @click="toggleEdit">
              {{ Number(qualification.status) === 0 ? '添加增票资质' : '修改并重新审核' }}
            </el-button>
            <div v-if="Number(qualification.status) === 3" class="error-info">
              审核失败原因：{{ qualification.applyReply || '-' }}
            </div>
          </div>

          <!-- 已填写信息展示 -->
          <div v-if="!showEdit && qualification.companyName" class="info-block">
            <div class="info-block-title">填写增票资质信息</div>
            <el-form label-width="140px" class="info-form">
              <el-form-item label="单位名称">{{ qualification.companyName }}</el-form-item>
              <el-form-item label="纳税人识别码">{{ qualification.companyCode }}</el-form-item>
              <el-form-item label="注册地址">{{ qualification.companyAddress }}</el-form-item>
              <el-form-item label="注册电话">{{ qualification.companyPhone }}</el-form-item>
              <el-form-item label="开户银行">{{ qualification.companyBank }}</el-form-item>
              <el-form-item label="银行账户">{{ qualification.companyAccount }}</el-form-item>
            </el-form>
          </div>

          <!-- 编辑表单 -->
          <div v-if="showEdit" class="info-block">
            <div class="edit-tips">填写增票资质信息 <span class="font-color">（所有信息均为必填）</span></div>
            <el-form ref="formRef" :label-width="140" :model="formState" :rules="rules" class="edit-form" label-suffix="：">
              <el-form-item label="单位名称" prop="companyName" :rules="[{ required: true, message: '单位名称不能为空' }]">
                <el-input v-model="formState.companyName" clearable placeholder="请输入单位名称" maxlength="60" />
              </el-form-item>
              <el-form-item label="纳税人识别码" prop="companyCode">
                <el-input v-model="formState.companyCode" clearable placeholder="请输入纳税人识别码" maxlength="40" />
              </el-form-item>
              <el-form-item label="注册地址" prop="companyAddress" :rules="[{ required: true, message: '注册地址不能为空' }]">
                <el-input v-model="formState.companyAddress" clearable placeholder="请输入注册地址" maxlength="120" />
              </el-form-item>
              <el-form-item label="注册电话" prop="companyPhone">
                <el-input v-model="formState.companyPhone" clearable placeholder="请输入注册电话" maxlength="30" />
              </el-form-item>
              <el-form-item label="开户银行" prop="companyBank" :rules="[{ required: true, message: '开户银行不能为空' }]">
                <el-input v-model="formState.companyBank" clearable placeholder="请输入开户银行" maxlength="60" />
              </el-form-item>
              <el-form-item label="银行账户" prop="companyAccount">
                <el-input v-model="formState.companyAccount" clearable placeholder="请输入银行账户" maxlength="40" />
              </el-form-item>
              <el-form-item label="" prop="isConfirm">
                <el-checkbox v-model="formState.isConfirm" :false-value="0" :true-value="1">
                  我已阅读并同意
                  <router-link class="font-color" to="/member/invoice/confirm" target="_blank">《增票资质确认书》</router-link>
                </el-checkbox>
              </el-form-item>
              <el-form-item label="">
                <el-button type="primary" :loading="submitting" @click="onSubmit">确认</el-button>
                <el-button @click="toggleEdit">取消</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 增票资质（对齐原版 `pages/member/userInvoice/Info.vue`）。
 *
 * 接口：`GET /user/invoice/getStatus`、`GET /user/invoice/detail`、
 * `POST /user/invoice/create`（未申请时）、`POST /user/invoice/update`（已申请时）。
 * 结算页发票信息与本页联动：专票仅审核通过后可选。
 */
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance } from 'element-plus';
import { invoiceQualificationApi } from '@/api';
import type { UserInvoiceQualification, UserInvoicePayload } from '@/api/types';
import MemberNav from '@/components/MemberNav.vue';
import { applySeo } from '@/composables/useSeo';

const loading = ref(true);
const submitting = ref(false);
const showEdit = ref(false);
const formRef = ref<FormInstance>();
const qualification = ref<Partial<UserInvoiceQualification> & Record<string, any>>({ status: 0 });

const formState = reactive<UserInvoicePayload & { isConfirm: number }>({
  companyName: '',
  companyCode: '',
  companyAddress: '',
  companyPhone: '',
  companyBank: '',
  companyAccount: '',
  titleType: 2,
  isConfirm: 0,
});

const statusLabel = computed(() => {
  switch (Number(qualification.value.status)) {
    case 1:
      return '审核通过';
    case 2:
      return '已申请，待审核';
    case 3:
      return '审核未通过';
    default:
      return '未申请专用发票';
  }
});
const statusClass = computed(() => {
  switch (Number(qualification.value.status)) {
    case 1:
      return 'green';
    case 2:
      return 'orange';
    case 3:
      return 'red';
    default:
      return 'gray';
  }
});

function noChinese(fieldLabel: string) {
  return (_rule: any, value: any, callback: (error?: Error) => void) => {
    if (value && /[\u4e00-\u9fa5]/.test(String(value))) {
      callback(new Error(`${fieldLabel}格式不正确`));
    } else {
      callback();
    }
  };
}

const rules = {
  companyCode: [
    { required: true, message: '纳税人识别码不能为空', trigger: 'blur' },
    { validator: noChinese('纳税人识别码'), trigger: 'blur' },
  ],
  companyPhone: [
    { required: true, message: '注册电话不能为空', trigger: 'blur' },
    { validator: noChinese('注册电话'), trigger: 'blur' },
  ],
  companyAccount: [
    { required: true, message: '银行账户不能为空', trigger: 'blur' },
    { validator: noChinese('银行账户'), trigger: 'blur' },
  ],
  isConfirm: [
    { required: true, message: '请勾选确认书', trigger: 'change' },
    {
      validator: (_rule: any, value: number, callback: (error?: Error) => void) => {
        if (Number(value) !== 1) callback(new Error('需要勾选确认才能继续'));
        else callback();
      },
      trigger: 'change',
    },
  ],
};

async function load() {
  loading.value = true;
  try {
    const [status, detail] = await Promise.all([
      invoiceQualificationApi.status().catch(() => null),
      invoiceQualificationApi.detail().catch(() => null),
    ]);
    qualification.value = {
      ...(detail ?? {}),
      status: Number(status ?? detail?.status ?? 0),
    };
    if (detail) {
      Object.assign(formState, {
        companyName: detail.companyName ?? '',
        companyCode: detail.companyCode ?? '',
        companyAddress: detail.companyAddress ?? '',
        companyPhone: detail.companyPhone ?? '',
        companyBank: detail.companyBank ?? '',
        companyAccount: detail.companyAccount ?? '',
      });
    }
  } finally {
    loading.value = false;
  }
}

function toggleEdit() {
  showEdit.value = !showEdit.value;
  if (showEdit.value) {
    formState.isConfirm = 0;
    load();
  }
}

async function onSubmit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  submitting.value = true;
  try {
    const payload: UserInvoicePayload = {
      titleType: 2,
      companyName: formState.companyName,
      companyCode: formState.companyCode,
      companyAddress: formState.companyAddress,
      companyPhone: formState.companyPhone,
      companyBank: formState.companyBank,
      companyAccount: formState.companyAccount,
    };
    if (Number(qualification.value.status) === 0) {
      await invoiceQualificationApi.create(payload);
    } else {
      await invoiceQualificationApi.update({ ...payload, invoiceId: qualification.value.invoiceId });
    }
    ElMessage.success('操作成功');
    showEdit.value = false;
    await load();
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  applySeo({ title: '增票资质', noindex: true });
  load();
});
</script>

<style scoped lang="less">
.tig-member-sub-page {
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
  padding: 20px 24px;
  min-height: 340px;
}

.font-color { color: var(--tig-red); }

.tips {
  font-size: 14px;
  line-height: 1.9;
  color: #666;
  margin-bottom: 16px;
}

.remind-box {
  background: #fffdee;
  border: 1px solid #edd28b;
  padding: 12px 16px;
  margin-bottom: 18px;

  .remind-line { font-size: 14px; color: #333; }
  .mt10 { margin-top: 10px; }
  .error-info { margin-top: 10px; color: #666; font-size: 14px; }

  .gray { color: #999; }
  .green { color: #2e9e44; }
  .orange { color: #e6a23c; }
  .red { color: var(--tig-red); }
}

.info-block {
  margin-bottom: 10px;
  line-height: 1.5;

  .info-block-title { color: #999; margin-bottom: 10px; }
  .edit-tips { color: #999; margin-bottom: 10px; }
  .info-form,
  .edit-form { max-width: 680px; }
}
</style>
