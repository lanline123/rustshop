<template>
  <div class="business-page credit-user-page">
    <div class="tig-card search-card">
      <el-form :inline="true" class="search-form" @submit.prevent>
        <el-form-item label="会员类型：">
          <el-select v-model="filters.type" clearable style="width: 140px" @change="handleSearch">
            <el-option :value="1" label="个人" />
            <el-option :value="2" label="企业" />
          </el-select>
        </el-form-item>
        <el-form-item label="会员名：">
          <el-input v-model="filters.username" placeholder="会员用户名" clearable style="width: 180px" @keyup.enter="handleSearch" @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="状态：">
          <el-select v-model="filters.status" clearable style="width: 140px" @change="handleSearch">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
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
        <div class="table-title">账期会员</div>
        <div class="toolbar-actions">
          <el-button type="primary" :icon="Plus" @click="openCreate">新增授信会员</el-button>
          <el-button :icon="Refresh" circle @click="loadList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="list" border stripe style="width: 100%">
        <el-table-column prop="creditUserId" label="ID" width="70" align="center" />
        <el-table-column prop="userId" label="会员ID" width="80" align="center" />
        <el-table-column prop="username" label="会员名" min-width="130" />
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column prop="typeText" label="会员类型" width="100" />
        <el-table-column prop="companyName" label="公司名称" min-width="180" />
        <el-table-column prop="contactName" label="联系人" width="110" />
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
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val: number) => toggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该授信会员吗？" confirm-button-text="删除" cancel-button-text="取消" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
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

    <!-- 新增/编辑授信会员 -->
    <el-dialog v-model="formVisible" :title="form.creditUserId ? '编辑授信会员' : '新增授信会员'" width="520px" destroy-on-close>
      <el-form label-width="110px">
        <el-form-item v-if="!form.creditUserId" label="会员ID" required>
          <el-input-number v-model="form.userId" :min="1" placeholder="请输入会员ID" />
          <div class="form-extra">对应会员列表中的 UID（user_id）</div>
        </el-form-item>
        <el-form-item v-else label="会员名">{{ form.username || form.userId }}</el-form-item>
        <el-form-item label="信用额度" required>
          <el-input-number v-model="form.creditLimit" :min="0.01" :precision="2" :step="100" />
        </el-form-item>
        <el-form-item label="结算账期" required>
          <el-select v-model="form.settlementType" style="width: 200px">
            <el-option :value="1" label="月结" />
            <el-option :value="2" label="季结" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-drawer v-model="detailVisible" title="授信会员详情" size="560px" destroy-on-close>
      <div v-loading="detailLoading">
        <el-descriptions v-if="detail" :column="2" border size="small">
          <el-descriptions-item label="授信ID">{{ detail.creditUserId }}</el-descriptions-item>
          <el-descriptions-item label="会员ID">{{ detail.userId }}</el-descriptions-item>
          <el-descriptions-item label="会员名">{{ detail.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.mobile || '-' }}</el-descriptions-item>
          <el-descriptions-item label="会员类型">{{ detail.typeText || '-' }}</el-descriptions-item>
          <el-descriptions-item label="公司名称">{{ detail.companyName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ detail.contactName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="信用额度">¥{{ money(detail.creditLimit) }}</el-descriptions-item>
          <el-descriptions-item label="已用额度">¥{{ money(detail.usedAmount) }}</el-descriptions-item>
          <el-descriptions-item label="可用额度">¥{{ money(detail.availableAmount) }}</el-descriptions-item>
          <el-descriptions-item label="结算账期">{{ detail.settlementTypeText }}</el-descriptions-item>
          <el-descriptions-item label="账期天数">{{ detail.settlementDays ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detail.updateTime }}</el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="未获取到详情" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
/**
 * 账期会员管理（复刻原版 order/termPay/termMember/List.vue 结构）。
 *
 * 接口：`/credit/user/list|detail|create|update|delete|updateField`。
 */
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, Refresh, Search } from '@element-plus/icons-vue';
import request from '@/utils/request';

const loading = ref(false);
const list = ref<any[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(15);
const filters = reactive({
  type: undefined as number | undefined,
  username: '',
  status: undefined as number | undefined
});

function money(value: unknown) {
  return Number(value ?? 0).toFixed(2);
}

async function loadList() {
  loading.value = true;
  try {
    const res = await request<any>({
      url: '/credit/user/list',
      method: 'get',
      params: {
        page: page.value,
        size: size.value,
        type: filters.type,
        username: filters.username || undefined,
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
  filters.type = undefined;
  filters.username = '';
  filters.status = undefined;
  handleSearch();
}

async function toggleStatus(row: any, val: number) {
  try {
    await request({
      url: '/credit/user/updateField',
      method: 'post',
      data: { id: row.creditUserId, field: 'status', val: String(val) }
    });
    ElMessage.success('状态已更新');
  } catch (err) {
    row.status = row.status === 1 ? 0 : 1;
  }
}

// ————————————————— 详情 —————————————————
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<any>(null);

async function openDetail(row: any) {
  detailVisible.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    detail.value = await request<any>({
      url: '/credit/user/detail',
      method: 'get',
      params: { id: row.creditUserId }
    });
  } finally {
    detailLoading.value = false;
  }
}

// ————————————————— 新增/编辑 —————————————————
const formVisible = ref(false);
const submitting = ref(false);
const form = reactive({
  creditUserId: 0,
  userId: 0,
  username: '',
  creditLimit: 1000,
  settlementType: 1,
  status: 1
});

function openCreate() {
  Object.assign(form, { creditUserId: 0, userId: 0, username: '', creditLimit: 1000, settlementType: 1, status: 1 });
  formVisible.value = true;
}

function openEdit(row: any) {
  Object.assign(form, {
    creditUserId: row.creditUserId,
    userId: row.userId,
    username: row.username || '',
    creditLimit: Number(row.creditLimit ?? 0),
    settlementType: row.settlementType ?? 1,
    status: row.status ?? 1
  });
  formVisible.value = true;
}

async function submitForm() {
  if (!form.creditUserId && !form.userId) {
    ElMessage.warning('请输入会员ID');
    return;
  }
  if (Number(form.creditLimit) < 0.01) {
    ElMessage.warning('信用额度不能小于 0.01');
    return;
  }
  submitting.value = true;
  try {
    if (form.creditUserId) {
      await request({
        url: '/credit/user/update',
        method: 'post',
        data: {
          creditUserId: form.creditUserId,
          creditLimit: form.creditLimit,
          settlementType: form.settlementType,
          status: form.status
        }
      });
    } else {
      await request({
        url: '/credit/user/create',
        method: 'post',
        data: {
          userId: form.userId,
          creditLimit: form.creditLimit,
          settlementType: form.settlementType,
          status: form.status
        }
      });
    }
    ElMessage.success('保存成功');
    formVisible.value = false;
    loadList();
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(row: any) {
  await request({ url: '/credit/user/delete', method: 'post', data: { id: row.creditUserId } });
  ElMessage.success('删除成功');
  loadList();
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
.toolbar-actions {
  display: flex;
  gap: 10px;
}
.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.form-extra {
  margin-left: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
