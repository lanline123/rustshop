<template>
  <div class="tig-member-sub-page">
    <div class="container member-layout">
      <MemberNav />

      <div class="info-row">
        <div class="content-card" v-loading="loading">
          <div class="card-title">账户安全</div>

          <ul class="security-list">
            <li class="security-row" @click="openDialog('mobile')">
              <span class="row-title">手机</span>
              <span class="row-value">{{ maskedMobile || '未绑定' }}</span>
              <span class="row-action">{{ profile.mobile ? '换绑' : '绑定' }} ›</span>
            </li>
            <li class="security-row" @click="openDialog('email')">
              <span class="row-title">邮箱</span>
              <span class="row-value">{{ maskedEmail || '未绑定' }}</span>
              <span class="row-action">{{ profile.email ? '换绑' : '绑定' }} ›</span>
            </li>
            <li class="security-row" @click="openDialog('password')">
              <span class="row-title">登录密码</span>
              <span class="row-value">已设置</span>
              <span class="row-action">修改 ›</span>
            </li>
          </ul>

          <div class="security-tips">
            <div class="tips-title">安全服务提示</div>
            <p>1. 注意防范进入钓鱼网站，不要轻信各种即时通讯工具发送的商品或支付链接，谨防网购诈骗。</p>
            <p>2. 建议您安装杀毒软件，并定期更新操作系统等软件补丁，确保账户及交易安全。</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码 -->
    <el-dialog v-model="passwordVisible" title="密码修改" width="480px">
      <el-form label-width="90px" class="dialog-form">
        <el-form-item label="手机验证码">
          <div class="code-row">
            <el-input v-model="passwordForm.code" placeholder="请输入验证码" maxlength="10" />
            <el-button :disabled="countdown > 0" @click="sendPasswordCode">{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.password" type="password" show-password placeholder="请输入 6-32 位新密码" maxlength="32" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirm" type="password" show-password placeholder="请再次输入新密码" maxlength="32" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPassword">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 手机换绑/绑定 -->
    <el-dialog v-model="mobileVisible" :title="profile.mobile ? '手机换绑' : '手机绑定'" width="520px">
      <el-steps :active="mobileStep" simple class="dialog-steps">
        <el-step title="验证当前手机" v-if="profile.mobile" />
        <el-step :title="profile.mobile ? '绑定新手机' : '绑定手机'" />
      </el-steps>

      <el-form v-if="profile.mobile && mobileStep === 0" label-width="100px" class="dialog-form">
        <el-form-item label="当前手机号">
          <el-input :model-value="maskedMobile" disabled />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="mobileForm.verifyCode" placeholder="请输入当前手机验证码" maxlength="10" />
            <el-button :disabled="countdown > 0" @click="sendMobileVerifyCode">{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</el-button>
          </div>
        </el-form-item>
      </el-form>

      <el-form v-else label-width="100px" class="dialog-form">
        <el-form-item label="新手机号">
          <el-input v-model="mobileForm.newMobile" placeholder="请输入新手机号" maxlength="20" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="mobileForm.code" placeholder="请输入新手机验证码" maxlength="10" />
            <el-button :disabled="countdown > 0" @click="sendMobileNewCode">{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="mobileVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="profile.mobile && mobileStep === 0 ? verifyCurrentMobile() : submitModifyMobile()"
        >
          {{ profile.mobile && mobileStep === 0 ? '下一步' : '确认绑定' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 邮箱换绑/绑定 -->
    <el-dialog v-model="emailVisible" :title="profile.email ? '邮箱换绑' : '邮箱绑定'" width="520px">
      <el-steps :active="emailStep" simple class="dialog-steps">
        <el-step title="验证当前邮箱" v-if="profile.email" />
        <el-step :title="profile.email ? '绑定新邮箱' : '绑定邮箱'" />
      </el-steps>

      <el-form v-if="profile.email && emailStep === 0" label-width="100px" class="dialog-form">
        <el-form-item label="当前邮箱">
          <el-input :model-value="maskedEmail" disabled />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="emailForm.verifyCode" placeholder="请输入当前邮箱验证码" maxlength="10" />
            <el-button :disabled="countdown > 0" @click="sendEmailVerifyCode">{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</el-button>
          </div>
        </el-form-item>
      </el-form>

      <el-form v-else label-width="100px" class="dialog-form">
        <el-form-item label="新邮箱">
          <el-input v-model="emailForm.newEmail" placeholder="请输入新邮箱" maxlength="60" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="emailForm.code" placeholder="请输入新邮箱验证码" maxlength="10" />
            <el-button :disabled="countdown > 0" @click="sendEmailNewCode">{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="emailVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="profile.email && emailStep === 0 ? verifyCurrentEmail() : submitModifyEmail()"
        >
          {{ profile.email && emailStep === 0 ? '下一步' : '确认绑定' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 账户安全（对齐原版 `pages/member/security/Info.vue` + `VerificationSecurity.vue`）。
 *
 * 手机换绑/绑定、邮箱换绑/绑定、登录密码修改均走真实契约：
 * - 修改密码：`sendMobileCodeByModifyPassword` → `checkModifyPasswordMobileCode`
 * - 手机：`sendMobileCodeByMobileValidate`/`mobileValidate` → `sendMobileCodeByModifyMobile`/`modifyMobile`
 * - 邮箱：`sendEmailCodeByEmailValidate`/`emailValidateNew` → `sendEmailCodeByModifyEmail`/`modifyEmail`
 * 未迁移的短信/邮件发送能力会在发送验证码时返回真实业务错误并原样提示。
 */
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { accountApi, userApi } from '@/api';
import MemberNav from '@/components/MemberNav.vue';
import { applySeo } from '@/composables/useSeo';

const loading = ref(true);
const submitting = ref(false);
const profile = reactive<Record<string, any>>({ mobile: '', email: '' });

const passwordVisible = ref(false);
const mobileVisible = ref(false);
const emailVisible = ref(false);
const mobileStep = ref(0);
const emailStep = ref(0);
const countdown = ref(0);
let timer: number | null = null;

const passwordForm = reactive({ code: '', password: '', confirm: '' });
const mobileForm = reactive({ verifyCode: '', newMobile: '', code: '' });
const emailForm = reactive({ verifyCode: '', newEmail: '', code: '' });

const maskedMobile = computed(() =>
  profile.mobile ? String(profile.mobile).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2') : '',
);
const maskedEmail = computed(() =>
  profile.email ? String(profile.email).replace(/(.{2})(.*)(@.*)/, '$1*****$3') : '',
);

function startCountdown() {
  countdown.value = 60;
  if (timer) window.clearInterval(timer);
  timer = window.setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0 && timer) {
      window.clearInterval(timer);
      timer = null;
    }
  }, 1000);
}

async function sendWithFeedback(task: () => Promise<unknown>) {
  try {
    await task();
    ElMessage.success('验证码已发送');
    startCountdown();
  } catch (err: any) {
    // 短信/邮件通道未迁移时如实展示后端错误
    ElMessage.error(err?.message || '验证码发送失败');
  }
}

const sendPasswordCode = () => sendWithFeedback(() => accountApi.sendModifyPasswordCode());
const sendMobileVerifyCode = () => sendWithFeedback(() => accountApi.sendMobileValidateCode());
const sendMobileNewCode = () =>
  sendWithFeedback(() => {
    if (!mobileForm.newMobile) {
      ElMessage.warning('请先输入新手机号');
      return Promise.reject(new Error('请先输入新手机号'));
    }
    return accountApi.sendModifyMobileCode(mobileForm.newMobile);
  });
const sendEmailVerifyCode = () => sendWithFeedback(() => accountApi.sendEmailValidateCode());
const sendEmailNewCode = () =>
  sendWithFeedback(() => {
    if (!emailForm.newEmail) {
      ElMessage.warning('请先输入新邮箱');
      return Promise.reject(new Error('请先输入新邮箱'));
    }
    return accountApi.sendModifyEmailCode(emailForm.newEmail);
  });

function openDialog(kind: 'mobile' | 'email' | 'password') {
  if (kind === 'mobile') {
    mobileStep.value = 0;
    mobileForm.verifyCode = '';
    mobileForm.newMobile = '';
    mobileForm.code = '';
    mobileVisible.value = true;
  } else if (kind === 'email') {
    emailStep.value = 0;
    emailForm.verifyCode = '';
    emailForm.newEmail = '';
    emailForm.code = '';
    emailVisible.value = true;
  } else {
    passwordForm.code = '';
    passwordForm.password = '';
    passwordForm.confirm = '';
    passwordVisible.value = true;
  }
}

async function verifyCurrentMobile() {
  if (!mobileForm.verifyCode) {
    ElMessage.warning('请输入当前手机验证码');
    return;
  }
  submitting.value = true;
  try {
    await accountApi.mobileValidate({ mobile: profile.mobile, code: mobileForm.verifyCode });
    ElMessage.success('验证通过');
    mobileStep.value = 1;
  } finally {
    submitting.value = false;
  }
}

async function submitModifyMobile() {
  if (!mobileForm.newMobile || !mobileForm.code) {
    ElMessage.warning('请填写新手机号与验证码');
    return;
  }
  submitting.value = true;
  try {
    await accountApi.modifyMobile({ mobile: mobileForm.newMobile, code: mobileForm.code });
    ElMessage.success('手机号修改成功');
    mobileVisible.value = false;
    await loadProfile();
  } finally {
    submitting.value = false;
  }
}

async function verifyCurrentEmail() {
  if (!emailForm.verifyCode) {
    ElMessage.warning('请输入当前邮箱验证码');
    return;
  }
  submitting.value = true;
  try {
    await accountApi.emailValidateNew({ email: profile.email, code: emailForm.verifyCode });
    ElMessage.success('验证通过');
    emailStep.value = 1;
  } finally {
    submitting.value = false;
  }
}

async function submitModifyEmail() {
  if (!emailForm.newEmail || !emailForm.code) {
    ElMessage.warning('请填写新邮箱与验证码');
    return;
  }
  submitting.value = true;
  try {
    await accountApi.modifyEmail({ email: emailForm.newEmail, code: emailForm.code });
    ElMessage.success('邮箱修改成功');
    emailVisible.value = false;
    await loadProfile();
  } finally {
    submitting.value = false;
  }
}

async function submitPassword() {
  if (!passwordForm.code) {
    ElMessage.warning('请输入验证码');
    return;
  }
  if (!passwordForm.password || passwordForm.password.length < 6) {
    ElMessage.warning('密码至少 6 位');
    return;
  }
  if (passwordForm.password !== passwordForm.confirm) {
    ElMessage.warning('两次输入的密码不一致');
    return;
  }
  submitting.value = true;
  try {
    await accountApi.checkModifyPassword({
      mobile: profile.mobile,
      code: passwordForm.code,
      password: passwordForm.password,
    });
    ElMessage.success('密码修改成功');
    passwordVisible.value = false;
  } finally {
    submitting.value = false;
  }
}

async function loadProfile() {
  loading.value = true;
  try {
    const result = await userApi.profile();
    if (result) Object.assign(profile, result);
  } catch {
    // 资料加载失败时仍可打开安全入口，具体操作会返回真实错误
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  applySeo({ title: '账户安全', noindex: true });
  loadProfile();
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
  min-height: 340px;
}

.card-title { font-size: 16px; font-weight: 700; margin-bottom: 16px; }

.security-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.security-row {
  display: flex;
  align-items: center;
  gap: 14px;
  height: 60px;
  border-radius: 8px;
  background: #f7f8fc;
  padding: 0 16px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover { background: #fff3f1; }

  .row-title { width: 120px; font-size: 14px; color: #1a1a1a; }
  .row-value { flex: 1; color: #8a919f; font-size: 14px; }
  .row-action { color: var(--tig-red); font-size: 13px; }
}

.security-tips {
  margin-top: 22px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 14px 16px;
  color: #666;
  font-size: 13px;
  line-height: 1.9;

  .tips-title { font-weight: 700; margin-bottom: 6px; }
}

.dialog-form { padding: 6px 0; }
.dialog-steps { margin-bottom: 18px; }

.code-row {
  display: flex;
  gap: 10px;
  width: 100%;

  .el-input { flex: 1; }
}
</style>
