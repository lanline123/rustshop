<template>
  <div class="tig-mall-login-page">
    <div class="login-box-container container flex-between">
      <div class="login-left-banner">
        <h2>正品保障 · 全球甄选</h2>
        <p>加入 TigShop 会员，尊享极速顺丰配送与专属会员价</p>
      </div>

      <div class="login-form-card">
        <div class="card-header">
          <h3>商城会员登录</h3>
          <router-link to="/register" class="reg-tip">没有账号？免费注册</router-link>
        </div>

        <el-form size="large" class="user-login-form" @keyup.enter="handleLogin">
          <el-form-item>
            <el-input v-model="form.username" placeholder="请输入会员手机号 / 账号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" show-password placeholder="请输入登录密码" :prefix-icon="Lock" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="loading" class="submit-btn" @click="handleLogin">
              立即登录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { User, Lock } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';

const router = useRouter();
const route = useRoute();
const loading = ref(false);

const form = reactive({
  username: '',
  password: ''
});

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码');
    return;
  }
  loading.value = true;
  try {
    const res: any = await request({
      url: '/user/login/signin',
      method: 'post',
      data: form
    });
    const token = res.token || res.accessToken;
    if (!token) {
      ElMessage.error('登录响应异常：未获取到授权凭证');
      return;
    }
    localStorage.setItem('tig_user_token', token);
    localStorage.setItem('tig_user_name', res.username || form.username);
    ElMessage.success('登录成功！');
    const redirect = (route.query.redirect as string) || '/';
    router.push(redirect);
  } catch (e: any) {
    // 接口失败直接交由全局拦截器提示错误，不作任何本地伪造
    console.error('会员登录失败:', e);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped lang="less">
.tig-mall-login-page {
  background: #f0f2f5;
  padding: 60px 0;
}

.login-box-container {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  height: 480px;

  .login-left-banner {
    flex: 1;
    background: linear-gradient(135deg, #e1251b, #8a0c06);
    color: #fff;
    padding: 60px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    h2 { font-size: 32px; font-weight: 800; margin-bottom: 16px; }
    p { font-size: 16px; opacity: 0.9; }
  }

  .login-form-card {
    width: 440px;
    padding: 60px 48px;

    .card-header {
      margin-bottom: 32px;
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      h3 { font-size: 20px; font-weight: 700; color: #333; }
      .reg-tip { font-size: 13px; color: var(--tig-red); }
    }

    .submit-btn {
      width: 100%;
      height: 44px;
      font-size: 16px;
      font-weight: 600;
      background: var(--tig-red);
      border-color: var(--tig-red);
      border-radius: 22px;
    }
  }
}
</style>
