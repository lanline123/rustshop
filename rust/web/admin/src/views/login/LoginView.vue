<template>
  <div class="tig-login-wrapper">
    <div class="login-card-container">
      <!-- Left Illustration / Brand -->
      <div class="login-visual-side">
        <div class="visual-brand">
          <div class="brand-logo-mark">T</div>
          <span class="brand-name">TigShop Pro</span>
        </div>
        <div class="visual-content">
          <h2>全场景数智化电商系统</h2>
          <p>基于 Rust 高并发底座驱动，提供毫秒级响应、高可靠的综合商城管理体验。</p>
        </div>
        <div class="visual-footer">
          <span>&copy; 2026 TigShop Platform. All rights reserved.</span>
        </div>
      </div>

      <!-- Right Form Box -->
      <div class="login-form-side">
        <div class="form-header">
          <h1 class="welcome-title">欢迎登录</h1>
          <p class="welcome-subtitle">商城运营管理控制台</p>
        </div>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="loginRules"
          size="large"
          class="login-el-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入管理员账号"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              show-password
              placeholder="请输入登录密码"
              :prefix-icon="Lock"
            />
          </el-form-item>

          <div class="form-options flex-between">
            <el-checkbox v-model="rememberMe">记住登录状态</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-submit-btn"
              @click="handleLogin"
            >
              立即登录
            </el-button>
          </el-form-item>

          <div class="agreement-box">
            <el-checkbox v-model="agreeProtocol">
              <span class="agreement-text">
                我已阅读并同意 <a href="javascript:;">《服务协议》</a> 与 <a href="javascript:;">《隐私政策》</a>
              </span>
            </el-checkbox>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { User, Lock } from '@element-plus/icons-vue';
import { ElMessage, type FormInstance } from 'element-plus';
import request from '@/utils/request';

const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);
const rememberMe = ref(true);
const agreeProtocol = ref(true);

const loginForm = reactive({
  username: 'admin',
  password: ''
});

const loginRules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }]
};

async function handleLogin() {
  if (!agreeProtocol.value) {
    ElMessage.warning('请阅读并勾选同意服务协议与隐私政策');
    return;
  }

  await formRef.value?.validate(async (valid) => {
    if (!valid) return;

    loading.value = true;
    try {
      const res: any = await request({
        url: '/login/signin',
        method: 'post',
        data: {
          username: loginForm.username,
          password: loginForm.password
        }
      });

      // 提取返回结果中的 token
      const token = res.token || res.accessToken;
      if (!token) {
        ElMessage.error('登录响应异常：未获取到授权凭证');
        return;
      }
      localStorage.setItem('tig_token', token);
      localStorage.setItem('tig_username', res.username || loginForm.username);

      ElMessage.success('登录成功，欢迎回来！');
      router.push('/dashboard');
    } catch (err: any) {
      console.error('管理员登录失败:', err);
    } finally {
      loading.value = false;
    }
  });
}
</script>

<style scoped lang="less">
.tig-login-wrapper {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 10% 20%, #eef3ff 0%, #f4f6fa 90%);
}

.login-card-container {
  width: 920px;
  height: 540px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 16px 48px rgba(0, 21, 41, 0.08);
  display: flex;
  overflow: hidden;
}

/* Left Visual */
.login-visual-side {
  width: 420px;
  background: linear-gradient(135deg, #1456f0 0%, #0d3eb8 100%);
  color: #ffffff;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;

  &::after {
    content: "";
    position: absolute;
    right: -40px;
    bottom: -40px;
    width: 200px;
    height: 200px;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 50%;
  }

  .visual-brand {
    display: flex;
    align-items: center;
    gap: 12px;

    .brand-logo-mark {
      width: 36px;
      height: 36px;
      background: #ffffff;
      color: #1456f0;
      font-weight: 900;
      font-size: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
    }
    .brand-name {
      font-size: 18px;
      font-weight: 700;
      letter-spacing: 0.5px;
    }
  }

  .visual-content {
    h2 {
      font-size: 26px;
      font-weight: 700;
      line-height: 1.4;
      margin-bottom: 16px;
    }
    p {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.85);
      line-height: 1.6;
    }
  }

  .visual-footer {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.6);
  }
}

/* Right Form */
.login-form-side {
  flex: 1;
  padding: 48px 48px 36px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .form-header {
    margin-bottom: 28px;
    .welcome-title {
      font-size: 24px;
      font-weight: 700;
      color: var(--tig-text-main);
      margin-bottom: 6px;
    }
    .welcome-subtitle {
      font-size: 13px;
      color: var(--tig-text-secondary);
    }
  }

  .form-options {
    margin-bottom: 18px;
    font-size: 13px;
  }

  .login-submit-btn {
    width: 100%;
    height: 44px;
    font-size: 15px;
    font-weight: 600;
    border-radius: 8px;
  }

  .agreement-box {
    margin-top: 12px;
    .agreement-text {
      font-size: 12px;
      color: var(--tig-text-secondary);
      a {
        color: var(--tig-primary);
      }
    }
  }
}
</style>
