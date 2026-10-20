<template>
  <div class="tig-mall-login-page">
    <div class="login-box-container container">
      <div class="login-left-banner">
        <h2>正品保障 · 全球甄选</h2>
        <p>登录 TigShop 会员账号，下单、查订单、管理收货地址一体化</p>
        <ul class="feature-list">
          <li>官方正品 · 假一赔十</li>
          <li>极速物流 · 全程可追踪</li>
          <li>7 天无理由退换 · 售后无忧</li>
        </ul>
      </div>

      <div class="login-form-card">
        <div class="card-header">
          <h1>会员登录</h1>
          <p>还没有账号？请联系官方客服开通</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent>
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="手机号 / 用户名" :prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="登录密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="submit"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-btn" :loading="loading" @click="submit">登 录</el-button>
          </el-form-item>
        </el-form>

        <div class="extra-links">
          <router-link to="/">返回首页</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
import { applySeo } from '@/composables/useSeo';
import { useUserStore } from '@/stores/user';
import { useCartStore } from '@/stores/cart';

const route = useRoute();
const router = useRouter();
const user = useUserStore();
const cart = useCartStore();

const formRef = ref<FormInstance>();
const loading = ref(false);
const form = reactive({ username: '', password: '' });

const rules: FormRules = {
  username: [{ required: true, message: '请输入手机号或用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
};

function redirectTarget(): string {
  const redirect = route.query.redirect;
  if (typeof redirect === 'string' && redirect.startsWith('/')) return redirect;
  return '/member';
}

async function submit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    await user.login(form.username.trim(), form.password);
    await cart.fetchCount(user.token);
    ElMessage.success('登录成功');
    router.replace(redirectTarget());
  } finally {
    loading.value = false;
  }
}

applySeo({ title: '会员登录', noindex: true });
</script>

<style scoped lang="less">
.tig-mall-login-page {
  min-height: 100vh;
  background: linear-gradient(120deg, #1f2329 0%, #33383f 55%, #4a2020 100%);
  display: flex;
  align-items: center;
}

.login-box-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
}

.login-left-banner {
  color: #fff;
  max-width: 520px;

  h2 { font-size: 38px; font-weight: 800; margin-bottom: 14px; }
  p { color: #d8d8d8; font-size: 15px; line-height: 1.8; }
  .feature-list {
    margin-top: 26px;
    padding-left: 18px;
    color: #cfcfcf;
    font-size: 14px;
    line-height: 2;
  }
}

.login-form-card {
  width: 400px;
  background: #ffffff;
  border-radius: 12px;
  padding: 30px 30px 22px;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.25);

  .card-header {
    margin-bottom: 22px;
    h1 { font-size: 22px; font-weight: 800; margin-bottom: 6px; }
    p { color: #999; font-size: 13px; }
  }

  .login-btn { width: 100%; font-weight: 700; letter-spacing: 4px; }

  .extra-links {
    display: flex;
    justify-content: center;
    font-size: 13px;
    color: #888;
    a:hover { color: var(--tig-red); }
  }
}

@media (max-width: 1000px) {
  .login-left-banner { display: none; }
  .login-box-container { justify-content: center; }
}
</style>
