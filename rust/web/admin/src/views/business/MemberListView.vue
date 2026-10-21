<template>
  <div class="business-page">
    <SchemaTable :schema="schema" />

    <el-drawer v-model="detailVisible" title="会员详情" size="620px" destroy-on-close>
      <div v-loading="detailLoading">
        <template v-if="detail">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="会员ID">{{ detail.userId }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ detail.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detail.mobile || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ detail.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ detail.regTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="余额">¥{{ money(detail.balance) }}</el-descriptions-item>
            <el-descriptions-item label="冻结余额">¥{{ money(detail.frozenBalance) }}</el-descriptions-item>
            <el-descriptions-item label="消费积分">{{ detail.points ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="成长积分">{{ detail.growthPoints ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="最后登录IP">{{ detail.lastIp || '-' }}</el-descriptions-item>
            <el-descriptions-item label="认证状态">
              <el-tag :type="detail.isCompanyAuth === 1 ? 'success' : 'info'">
                {{ detail.isCompanyAuth === 1 ? '已认证' : '未认证' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </template>
        <el-empty v-else description="未获取到会员详情" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
/**
 * 会员管理：列表 + 详情抽屉。
 *
 * 接口（Rust `routes/member/user_manage.rs`）：
 * - 列表：`GET /adminapi/user/user/list`
 * - 详情：`GET /adminapi/user/user/detail?id=`
 */
import { computed, ref } from 'vue';
import SchemaTable from '@/components/schema/SchemaTable.vue';
import request from '@/utils/request';
import type { TableSchema } from '@/components/schema/types';

function money(value: unknown) {
  return Number(value ?? 0).toFixed(2);
}

const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<any>(null);

async function openDetail(row: any) {
  detailVisible.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    detail.value = await request<any>({
      url: '/user/user/detail',
      method: 'get',
      params: { id: row.userId }
    });
  } finally {
    detailLoading.value = false;
  }
}

const schema = computed<TableSchema>(() => ({
  title: '会员用户列表',
  apiList: '/user/user/list',
  primaryKey: 'userId',
  searchFields: [
    { prop: 'keyword', label: '关键词', type: 'input', placeholder: '账号 / 昵称 / 手机号' },
    { prop: 'mobile', label: '手机号码', type: 'input', placeholder: '搜索绑定手机' }
  ],
  columns: [
    { prop: 'userId', label: 'UID', width: 80, align: 'center' },
    { prop: 'avatar', label: '头像', type: 'image', width: 80 },
    { prop: 'username', label: '客户名称', minWidth: 130 },
    { prop: 'nickname', label: '会员昵称', minWidth: 130 },
    { prop: 'mobile', label: '绑定手机', minWidth: 130 },
    { prop: 'email', label: '邮箱', minWidth: 160 },
    { prop: 'balance', label: '余额', type: 'money', width: 110 },
    { prop: 'frozenBalance', label: '冻结余额', type: 'money', width: 110 },
    { prop: 'points', label: '消费积分', width: 100, align: 'center' },
    { prop: 'growthPoints', label: '成长积分', width: 100, align: 'center' },
    { prop: 'regTime', label: '注册日期', minWidth: 160 }
  ],
  actions: [{ label: '详情', type: 'primary', onClick: openDetail }]
}));
</script>

<style scoped>
.business-page {
  width: 100%;
}
</style>
