<template>
  <div class="tig-upload-image">
    <el-upload
      v-model:file-list="fileList"
      list-type="picture-card"
      :limit="limit"
      :multiple="limit > 1"
      :http-request="doUpload"
      :before-upload="beforeUpload"
      :on-exceed="handleExceed"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>

    <el-dialog v-model="previewVisible" title="图片预览" width="520px">
      <img :src="previewUrl" alt="预览图片" class="preview-img" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 凭证图片上传（对齐原版 `components/form/src/UploadImage.vue`）。
 *
 * 走客户端上传契约 `POST /api/user/user/uploadImg`（multipart `file`），
 * 返回 `GalleryPicUploadVO`；`v-model:uploadList` 对外暴露上传结果数组。
 */
import { ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import type { UploadFile, UploadProps, UploadUserFile } from 'element-plus';
import { userApi } from '@/api';
import type { UploadedPicture } from '@/api/types';
import { imageUrl } from '@/utils/format';

const props = withDefaults(
  defineProps<{
    limit?: number;
    fileTypes?: string[];
    maxSizeMb?: number;
  }>(),
  { limit: 5, fileTypes: () => ['gif', 'jpeg', 'png', 'jpg', 'webp'], maxSizeMb: 2 },
);

/** 上传成功后的图片对象（与后端 GalleryPicUploadVO 对齐）。 */
const uploadList = defineModel<UploadedPicture[]>('uploadList', { default: () => [] });

const fileList = ref<UploadUserFile[]>([]);
const previewVisible = ref(false);
const previewUrl = ref('');

watch(
  () => uploadList.value,
  (list) => {
    if (!list?.length) {
      fileList.value = [];
      return;
    }
    fileList.value = list.map((pic, index) => ({
      name: pic.picName || `凭证${index + 1}`,
      url: imageUrl(pic.picThumb || pic.picUrl),
      uid: pic.picId ?? index,
    }));
  },
  { immediate: true, deep: true },
);

function beforeUpload(rawFile: File): boolean {
  if (fileList.value.length >= props.limit) {
    ElMessage.error(`已经达到上传数量限制（最多 ${props.limit} 张）`);
    return false;
  }
  const ext = rawFile.type.replace(/.*\//, '').toLowerCase();
  const allowed = props.fileTypes.includes(ext) || (ext === 'jpeg' && props.fileTypes.includes('jpg'));
  if (!allowed) {
    ElMessage.error(`无法上传 ${ext} 文件！只能上传 ${props.fileTypes.join(', ')} 格式`);
    return false;
  }
  if (rawFile.size / 1024 / 1024 > props.maxSizeMb) {
    ElMessage.error(`只能上传 ${props.maxSizeMb}M 以内的图片`);
    return false;
  }
  return true;
}

async function doUpload(options: { file: File; onSuccess: (r: any) => void; onError: (e: Error) => void }) {
  try {
    const result = await userApi.uploadImage(options.file);
    // el-upload 需要同步响应体；真实结果写入 v-model
    options.onSuccess(result);
    uploadList.value = [...(uploadList.value ?? []), result];
  } catch (err: any) {
    options.onError(err instanceof Error ? err : new Error(String(err)));
  }
}

function handleRemove(file: UploadFile) {
  const picId = Number(file.uid);
  const target = (uploadList.value ?? []).find((pic) => pic.picId === picId);
  if (target) {
    uploadList.value = (uploadList.value ?? []).filter((pic) => pic.picId !== target.picId);
  } else {
    uploadList.value = (uploadList.value ?? []).filter(
      (pic) => imageUrl(pic.picThumb || pic.picUrl) !== file.url,
    );
  }
}

function handleExceed(files: File[]) {
  ElMessage.warning(`最多只能上传 ${props.limit} 张图片`);
}

function handlePreview(file: UploadFile) {
  previewUrl.value = file.url ?? '';
  previewVisible.value = true;
}

const uploadProps: UploadProps = {};
void uploadProps;
</script>

<style scoped lang="less">
.tig-upload-image {
  .preview-img {
    display: block;
    max-width: 100%;
    margin: 0 auto;
  }
}
</style>
