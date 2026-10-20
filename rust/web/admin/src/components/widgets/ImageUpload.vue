<template>
  <div class="tig-image-uploader">
    <el-upload
      class="avatar-uploader"
      :action="uploadUrl"
      :headers="headers"
      :show-file-list="false"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      name="file"
    >
      <div v-if="modelValue" class="image-preview">
        <img :src="resolveImageUrl(modelValue)" class="avatar" />
        <div class="image-mask">
          <span>更换图片</span>
        </div>
      </div>
      <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
    </el-upload>
    <div v-if="tip" class="upload-tip">{{ tip }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

const props = defineProps<{
  modelValue?: string;
  tip?: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

/**
 * 素材库上传接口（契约：`POST /adminapi/setting/galleryPic/uploadImg`，multipart `file`）。
 * 原 `/adminapi/common/upload/upload` 在契约中不存在 → 一直 404。
 */
const uploadUrl = '/adminapi/setting/galleryPic/uploadImg';

const headers = computed(() => {
  const token = localStorage.getItem('tig_token');
  const shopId = localStorage.getItem('tig_shop_id');
  return {
    Authorization: token ? `Bearer ${token}` : '',
    'X-ADMIN-TYPE': 'admin',
    'X-CLIENT-TYPE': 'admin',
    ...(shopId ? { 'X-Shop-Id': shopId } : {})
  };
});

function resolveImageUrl(url: string) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  if (url.startsWith('/static')) return url;
  return `/static/${url}`;
}

function handleSuccess(res: any) {
  // 契约返回 GalleryPicUploadVO：{ picUrl, picThumb, picName, ... }
  const data = res?.data ?? res;
  const url = data?.picUrl || data?.pic_url || data?.url;
  if (res?.code === 0 && url) {
    emit('update:modelValue', url);
    ElMessage.success('图片上传成功');
  } else if (typeof res === 'string') {
    emit('update:modelValue', res);
  } else if (url) {
    emit('update:modelValue', url);
  } else {
    ElMessage.error(res?.message || res?.msg || '图片上传失败');
  }
}

function handleError(err: any) {
  // el-upload 的 error 是原始 XHR/Error，真正的业务文案在响应体里
  let message = '图片上传失败';
  try {
    const body = JSON.parse(err?.message ?? '{}');
    message = body.message || body.msg || message;
  } catch (_) {
    // ignore
  }
  ElMessage.error(message);
}

function beforeUpload(file: File) {
  const isImage = file.type.startsWith('image/');
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isImage) {
    ElMessage.error('只能上传图片格式文件!');
    return false;
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!');
    return false;
  }
  return true;
}
</script>

<style scoped lang="less">
.tig-image-uploader {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color-darker);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    background: #fafafa;

    &:hover {
      border-color: var(--el-color-primary);
    }
  }
}

.avatar-uploader-icon {
  font-size: 26px;
  color: #8c939d;
  width: 90px;
  height: 90px;
  text-align: center;
  line-height: 90px;
}

.image-preview {
  position: relative;
  width: 90px;
  height: 90px;
  display: flex;
  align-items: center;
  justify-content: center;

  .avatar {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
  }

  .image-mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 12px;
    opacity: 0;
    transition: opacity 0.2s;
  }

  &:hover .image-mask {
    opacity: 1;
  }
}

.upload-tip {
  font-size: 12px;
  color: #86909c;
}
</style>
