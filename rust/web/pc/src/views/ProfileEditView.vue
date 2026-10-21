<template>
  <div class="tig-member-sub-page">
    <div class="container member-layout">
      <MemberNav />

      <div class="info-row">
        <div class="content-card" v-loading="loading">
          <div class="card-title">个人资料</div>

          <el-form ref="formRef" label-position="top" :model="formState" :rules="rules" class="form-body" label-suffix="：">
            <el-form-item>
              <div class="avatar-box">
                <el-upload
                  :show-file-list="false"
                  :http-request="uploadAvatar"
                  accept="image/*"
                  class="avatar-uploader"
                >
                  <el-avatar :size="90" :src="imageUrl(formState.avatar)">
                    {{ (formState.nickname || 'U').slice(0, 1) }}
                  </el-avatar>
                  <div class="avatar-tip">点击更换头像</div>
                </el-upload>
              </div>
            </el-form-item>
            <el-form-item label="生日" prop="birthday" :rules="[{ required: true, message: '请选择生日' }]">
              <el-date-picker
                v-model="formState.birthday"
                type="date"
                placeholder="请选择生日"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                size="large"
              />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname" :rules="[{ required: true, message: '请输入昵称' }]">
              <el-input v-model="formState.nickname" clearable placeholder="请输入昵称" size="large" maxlength="30" />
            </el-form-item>
            <el-form-item label="手机">
              <span class="bound-info">
                <template v-if="formState.mobile">{{ maskedMobile }}</template>
                <template v-else>未绑定</template>
                <router-link to="/member/security" class="a-btn">[{{ formState.mobile ? '修改' : '去绑定' }}]</router-link>
              </span>
            </el-form-item>
            <el-form-item label="邮箱">
              <span class="bound-info">
                <template v-if="formState.email">{{ maskedEmail }}</template>
                <template v-else>未绑定</template>
                <router-link to="/member/security" class="a-btn">[{{ formState.email ? '修改' : '去绑定' }}]</router-link>
              </span>
            </el-form-item>
          </el-form>

          <div class="form-actions">
            <el-button size="large" @click="$router.back()">取消</el-button>
            <el-button type="primary" size="large" :loading="saving" @click="onSubmit">确认修改</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 个人资料编辑（对齐原版 `pages/member/profile/Info.vue`）。
 *
 * 头像上传 `POST /user/user/modifyAvatar`（multipart file）；
 * 生日/昵称提交 `POST /user/user/updateInformation`；资料读取 `GET /user/user/detail`。
 */
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance } from 'element-plus';
import { accountApi, userApi } from '@/api';
import MemberNav from '@/components/MemberNav.vue';
import { applySeo } from '@/composables/useSeo';
import { useUserStore } from '@/stores/user';
import { imageUrl } from '@/utils/format';

const user = useUserStore();

const loading = ref(true);
const saving = ref(false);
const formRef = ref<FormInstance>();
const formState = reactive<Record<string, any>>({
  avatar: '',
  birthday: '',
  nickname: '',
  mobile: '',
  email: '',
});

const rules = {
  birthday: [{ required: true, message: '请选择生日', trigger: 'change' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
};

const maskedMobile = computed(() =>
  formState.mobile ? String(formState.mobile).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2') : '',
);
const maskedEmail = computed(() =>
  formState.email ? String(formState.email).replace(/(.{2})(.*)(@.*)/, '$1*****$3') : '',
);

async function load() {
  loading.value = true;
  try {
    const profile = await userApi.profile();
    if (profile) {
      Object.assign(formState, profile);
      if (formState.birthday === '0000-00-00') formState.birthday = '';
    }
  } catch (err: any) {
    ElMessage.error(err?.message || '获取个人资料失败');
  } finally {
    loading.value = false;
  }
}

async function uploadAvatar(options: { file: File; onSuccess?: (r: any) => void; onError?: (e: Error) => void }) {
  try {
    await accountApi.modifyAvatar(options.file);
    ElMessage.success('头像已更新');
    options.onSuccess?.({});
    await load();
    await user.fetchProfile();
  } catch (err: any) {
    options.onError?.(err instanceof Error ? err : new Error(String(err)));
    ElMessage.error(err?.message || '头像上传失败');
  }
}

async function onSubmit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    await accountApi.updateInformation({
      birthday: formState.birthday || undefined,
      nickname: formState.nickname,
    });
    ElMessage.success('修改成功');
    await user.fetchProfile();
    await load();
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  applySeo({ title: '个人资料', noindex: true });
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
  min-height: 380px;
}

.card-title { font-size: 16px; font-weight: 700; margin-bottom: 16px; }

.form-body { max-width: 620px; }

.avatar-box {
  display: flex;
  align-items: center;
  gap: 16px;

  .avatar-uploader {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
  }
  .avatar-tip { color: #999; font-size: 12px; }
}

.bound-info {
  color: #666;
  font-size: 13px;
  .a-btn { color: var(--tig-red); margin-left: 6px; }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  max-width: 620px;
}
</style>
