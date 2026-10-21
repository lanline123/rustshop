<template>
  <div class="tig-member-sub-page">
    <div class="container member-layout">
      <MemberNav />

      <div class="info-row">
        <div class="content-card" v-loading="loading">
          <div class="card-title">会员实名认证</div>

          <!-- 已提交：状态展示 -->
          <template v-if="current && !editing">
            <div class="status-card" :class="statusClass">
              <div class="status-head">
                <span class="status-title">当前实名认证状态</span>
                <el-tag :type="statusTagType">{{ current.statusText || statusText(current.status) }}</el-tag>
              </div>
              <el-descriptions :column="2" border class="status-desc">
                <el-descriptions-item label="认证类型">{{ current.type === 2 ? '企业' : '个人' }}</el-descriptions-item>
                <el-descriptions-item label="认证主体">{{ current.companyName || current.contactName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="联系人">{{ current.contactName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ current.contactMobile || '-' }}</el-descriptions-item>
                <el-descriptions-item label="提交时间">{{ current.addTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="审核时间">{{ current.auditTime || '-' }}</el-descriptions-item>
                <el-descriptions-item v-if="current.auditRemark" label="审核备注" :span="2">
                  {{ current.auditRemark }}
                </el-descriptions-item>
              </el-descriptions>
              <div class="status-actions">
                <el-button type="primary" @click="startEdit">修改并重新提交</el-button>
              </div>
            </div>
          </template>

          <!-- 申请表单 -->
          <template v-else>
            <div class="tips-item">请填写真实有效的身份信息，提交后由平台审核，审核通过后不可随意变更。</div>
            <el-form ref="formRef" :model="formState" :rules="rules" label-width="120px" class="auth-form">
              <el-form-item label="认证类型" prop="type">
                <el-radio-group v-model="formState.type">
                  <el-radio :value="1">个人</el-radio>
                  <el-radio :value="2">企业</el-radio>
                </el-radio-group>
              </el-form-item>

              <template v-if="formState.type === 1">
                <el-form-item label="证件类型" prop="documentType">
                  <el-select v-model="formState.documentType" placeholder="请选择证件类型" style="width: 320px">
                    <el-option label="身份证" :value="1" />
                    <el-option label="护照" :value="2" />
                    <el-option label="港澳台通行证" :value="3" />
                  </el-select>
                </el-form-item>
                <el-form-item label="姓名" prop="corporateName">
                  <el-input v-model="formState.corporateName" placeholder="请输入姓名" maxlength="30" />
                </el-form-item>
              </template>

              <template v-else>
                <el-form-item label="企业名称" prop="companyName">
                  <el-input v-model="formState.companyName" placeholder="请输入企业名称" maxlength="60" />
                </el-form-item>
                <el-form-item label="统一社会信用代码" prop="businessLicense">
                  <el-input v-model="formState.businessLicense" placeholder="请输入统一社会信用代码" maxlength="40" />
                </el-form-item>
                <el-form-item label="法定代表人" prop="corporateName">
                  <el-input v-model="formState.corporateName" placeholder="请输入法定代表人姓名" maxlength="30" />
                </el-form-item>
                <el-form-item label="法人证件号" prop="documentNumber">
                  <el-input v-model="formState.documentNumber" placeholder="请输入法定代表人证件号码" maxlength="40" />
                </el-form-item>
                <el-form-item label="企业注册地址">
                  <el-input v-model="formState.companyAddress" placeholder="请输入企业注册地址" maxlength="120" />
                </el-form-item>
              </template>

              <template v-if="formState.type === 1">
                <el-form-item label="证件号码" prop="documentNumber">
                  <el-input v-model="formState.documentNumber" placeholder="请输入证件号码" maxlength="40" />
                </el-form-item>
              </template>

              <el-form-item label="联系人" prop="contactName">
                <el-input v-model="formState.contactName" placeholder="请输入联系人姓名" maxlength="30" />
              </el-form-item>
              <el-form-item label="联系人手机" prop="contactMobile">
                <el-input v-model="formState.contactMobile" placeholder="请输入联系人手机号" maxlength="20" />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="submitting" @click="onSubmit">提交认证</el-button>
                <el-button v-if="current" @click="editing = false">取消</el-button>
              </el-form-item>
            </el-form>
          </template>

          <!-- 申请记录 -->
          <div class="history">
            <div class="history-title">实名认证记录</div>
            <el-table :data="history" border>
              <el-table-column label="认证类型" width="110">
                <template #default="{ row }">{{ row.type === 2 ? '企业' : '个人' }}</template>
              </el-table-column>
              <el-table-column label="认证主体" min-width="200">
                <template #default="{ row }">{{ row.companyName || row.contactName || '-' }}</template>
              </el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag size="small">{{ row.statusText || statusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="提交时间" width="180" prop="addTime" />
              <el-table-column label="审核备注" min-width="200">
                <template #default="{ row }">{{ row.auditRemark || '-' }}</template>
              </el-table-column>
            </el-table>
            <el-empty v-if="history.length === 0 && !loading" description="暂无实名认证记录" :image-size="80" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 会员实名认证（对齐原版 `pages/member/userCertification/Info.vue` 的结构）。
 *
 * 接口：`GET /user/company/detail`、`GET /user/company/myApply`、`POST /user/company/apply`。
 * 原版为多步骤长表单（企业资料/法人/证件照），本轮保留「个人/企业 + 证件 + 联系人」核心字段，
 * 证件照上传等原版扩展项未迁移（后端 `company_data` 为松散 JSON，可继续扩展）。
 */
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance } from 'element-plus';
import { companyApi } from '@/api';
import type { UserCompany } from '@/api/types';
import MemberNav from '@/components/MemberNav.vue';
import { applySeo } from '@/composables/useSeo';

const loading = ref(true);
const submitting = ref(false);
const editing = ref(false);
const formRef = ref<FormInstance>();
const current = ref<UserCompany | null>(null);
const history = ref<UserCompany[]>([]);

const formState = reactive({
  type: 2,
  documentType: 1,
  corporateName: '',
  documentNumber: '',
  companyName: '',
  businessLicense: '',
  companyAddress: '',
  contactName: '',
  contactMobile: '',
});

const rules = {
  type: [{ required: true, message: '请选择认证类型', trigger: 'change' }],
  corporateName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  documentNumber: [{ required: true, message: '请输入证件号码', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  businessLicense: [{ required: true, message: '请输入统一社会信用代码', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactMobile: [
    { required: true, message: '请输入联系人手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' },
  ],
};

const statusTagType = computed(() => {
  const status = Number(current.value?.status ?? 0);
  if (status === 1) return 'success';
  if (status === 2) return 'warning';
  if (status === 3) return 'danger';
  return 'info';
});
const statusClass = computed(() => {
  const status = Number(current.value?.status ?? 0);
  return status === 1 ? 'is-approved' : status === 3 ? 'is-rejected' : '';
});

function statusText(status: number): string {
  switch (Number(status)) {
    case 1:
      return '审核通过';
    case 2:
      return '已提交，待审核';
    case 3:
      return '审核未通过';
    default:
      return '未提交';
  }
}

function startEdit() {
  editing.value = true;
  const data = current.value?.companyData ?? {};
  formState.type = current.value?.type ?? 2;
  formState.documentType = Number(data.documentType ?? 1);
  formState.corporateName = String(data.corporateName ?? current.value?.contactName ?? '');
  formState.documentNumber = String(data.documentNumber ?? '');
  formState.companyName = current.value?.companyName ?? '';
  formState.businessLicense = String(data.businessLicense ?? '');
  formState.companyAddress = String(data.companyAddress ?? '');
  formState.contactName = current.value?.contactName ?? '';
  formState.contactMobile = current.value?.contactMobile ?? '';
}

async function load() {
  loading.value = true;
  try {
    const [detail, applies] = await Promise.all([
      companyApi.detail().catch(() => null),
      companyApi.myApply().catch(() => []),
    ]);
    current.value = detail;
    history.value = applies;
  } finally {
    loading.value = false;
  }
}

async function onSubmit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  submitting.value = true;
  try {
    await companyApi.apply({
      type: formState.type,
      contactName: formState.contactName,
      contactMobile: formState.contactMobile,
      companyName: formState.type === 2 ? formState.companyName : formState.corporateName,
      companyData: {
        documentType: formState.documentType,
        corporateName: formState.corporateName,
        documentNumber: formState.documentNumber,
        businessLicense: formState.businessLicense || undefined,
        companyAddress: formState.companyAddress || undefined,
      },
    });
    ElMessage.success('实名认证申请已提交');
    editing.value = false;
    await load();
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  applySeo({ title: '会员实名认证', noindex: true });
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

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px;
  min-height: 360px;
}

.card-title { font-size: 16px; font-weight: 700; margin-bottom: 16px; }

.tips-item {
  background: #fdf6ec;
  border: 1px solid #f5e1bf;
  color: #a06b13;
  border-radius: 6px;
  padding: 10px 14px;
  font-size: 13px;
  margin-bottom: 16px;
}

.status-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 18px;

  &.is-approved { border-color: #d9ecff; background: #f5faff; }
  &.is-rejected { border-color: #ffdcdc; background: #fff7f6; }

  .status-head {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
    .status-title { font-weight: 700; }
  }

  .status-actions { margin-top: 14px; text-align: right; }
}

.auth-form { max-width: 720px; }

.history {
  margin-top: 26px;
  .history-title { font-size: 15px; font-weight: 700; margin-bottom: 12px; }
}
</style>
