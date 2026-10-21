<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('上传方式')">
                        <el-radio-group v-model="uploadMode">
                            <el-radio value="local">{{ $t('本地上传') }}</el-radio>
                            <el-radio value="external">{{ $t('外部链接') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item v-if="uploadMode === 'local'" prop="videoUrl" :label="$t('本地视频')" :rules="[{ required: uploadMode === 'local', message: '请上传本地视频!' }]">
                        <div>
                            <div class="video-preview" v-if="formState.videoUrl">
                                <div class="iconfont icon-cha1 btn-remove" @click="deleteVideo"></div>
                                <video :src="imageFormat(formState.videoUrl)" controls class="preview-video"></video>
                            </div>
                            <div v-else class="upload-video-box">
                                <Upload
                                    name="file"
                                    ref="uploadRef"
                                    v-model:file-list="fileList"
                                    :action="requestUrl.prefix + '/setting/galleryVideoInfo/uploadVideo'"
                                    :headers="requestUrl.headers()"
                                    :showUploadList="showUploadList"
                                    @change="handleChange"
                                    :before-upload="beforeUpload"
                                    :progress="progress"
                                    accept=".mp4,video/*"
                                >
                                    <div class="gallery-add-item flex flex-justify-center flex-align-center">
                                        <el-icon size="25"><Plus /></el-icon>
                                    </div>
                                </Upload>
                            </div>
                            <div class="extra mt10">{{ $t('视频大小不超过{uploadMaxSize}MB，时长5秒-5分钟以内，建议时长15-190秒， 支持的视频文件类型 .mp4。', { uploadMaxSize }) }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item v-if="uploadMode === 'external'" :label="$t('外部视频地址')" prop="videoUrl" :rules="[{ required: uploadMode === 'external', message: '请输入外部视频地址!' }]">
                        <TigInput classType="tig-form-input" v-model="formState.videoUrl" :placeholder="$t('请输入视频文件的网络地址')" clearable />
                        <div class="extra mt10">{{ $t('需要填写视频文件的网络地址，格式支持 mp4/flv/webm/avi；不能填视频网站播放页这种"页面地址"') }}</div>
                    </el-form-item>
                    <el-form-item prop="videoName" :label="$t('视频名称')" :rules="[{ required: true, validator: validateName }]">
                        <TigInput classType="tig-form-input" v-model="formState.videoName" :placeholder="$t('20个字以内')" :maxlength="20" />
                    </el-form-item>
                    <el-form-item prop="galleryId" :label="$t('所在视频相册')">
                        <div class="flex flex-justify-between">
                            <el-cascader v-model="formState.galleryId" :options="cascaderOptions" :props="props1" />
                            <div class="con-btn ml10 flex flex-align-center flex-justify-center">
                                <DialogForm
                                    :params="{ act: 'add', parentId: 0 }"
                                    path="gallery/video/GalleryVideoEdit"
                                    :title="$t('添加视频相册')"
                                    width="400px"
                                    @okCallback="loadGallery"
                                >
                                    <el-button link type="primary">{{ $t('添加相册') }}</el-button>
                                </DialogForm>
                                <p class="ml10 mr10" style="margin-bottom: 3px">|</p>
                                <el-button link type="primary" :loading="calleryLoding" @click="loadGallery(0)">{{ $t('刷新') }}</el-button>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="videoCover" :label="$t('视频封面')">
                        <FormAddGallery v-model:photo="formState.videoCover"></FormAddGallery>
                        <div class="extra"> {{ $t('建议尺寸：800*800px，支持.jpg，.gif，.png格式，大小不超过{imageUploadMaxSize}MB。如果不添加封面， 系统会默认截取视频的第一个画面作为封面。', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }} </div>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { DialogForm } from "@/components/dialog";
import { imageFormat } from "@/utils/format";
import { FormAddGallery } from "@/components/gallery";
import request, { requestUrl } from "@/utils/request";
import { computed, ref, toRefs, onMounted, nextTick } from "vue";
import { message, Upload } from "ant-design-vue";
import { Plus } from "@element-plus/icons-vue";
import { getGalleryVideoInfoDetail, updateVideoInfo, getAllCategory } from "@/api/setting/gallery";
import { toApiGalleryId } from "@/utils/galleryTree";
import type { VideoDetail } from "@/types/setting/gallery.d";
import type { UploadChangeParam, UploadProps } from "ant-design-vue";
import { formatDuration } from "@/utils/time";
import { useConfigStore } from "@/store/config";
import { useGalleryStore } from "@/store/gallery";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const uploadRef = ref<InstanceType<typeof Upload>>();
const uploadMode = ref<"local" | "external">("local");
const configStore = useConfigStore();
configStore.updateConfig();
const uploadMaxSize = configStore.config.uploadMaxSize;
const fileList = ref<any[]>([]);
const showUploadList = ref(true);
const props1 = {
    label: "name",
    value: "id",
    children: "children",
    checkStrictly: true,
    multiple: false
};
const progress: UploadProps["progress"] = {
    strokeWidth: 5,
    format: (percent) => {
        return `${parseFloat((percent ?? "0").toString())}%`;
    }
};
//获取来自父组件的参数
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    parentId: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    galleryId: {
        type: Number,
        default: 0
    },
    isDialog: Boolean
});
const { id, act } = toRefs(props);
// 判断是处理更新还是添加
const operation = act.value == "add" ? "create" : "update";
const validateName = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('视频名称不能为空')));
        return;
    } else if (value.length > 20) {
        callback(new Error(t('视频名称不能超过20个字')));
        return;
    } else {
        callback();
    }
};

// 父组件回调
const emit = defineEmits(["submitCallback", "okType", "callback"]);
// 表单参数初使化
const formRef = ref(); //表单Ref
let formState = ref<VideoDetail>({
    galleryId: [toApiGalleryId(props.galleryId)],
    videoUrl: "",
    videoName: "",
    videoCover: "",
    format: "",
    duration: "",
    size: ""
}); //表单数据
onMounted(() => {
    if (act.value === "detail") {
        // 获取详情数据
        fetchGalleryInfo();
    }
    loadGallery(0);
});
const galleryStore = useGalleryStore();
const options = ref<any[]>([]);
const cascaderOptions = computed(() => [{ id: 0, name: t('全部视频') }, ...options.value]);
const calleryLoding = ref(false);
const loadGallery = async (data?: any) => {
    calleryLoding.value = true;
    try {
        const result = await getAllCategory();
        options.value = result;
        galleryStore.isRefresh = true;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        setTimeout(() => {
            calleryLoding.value = false;
        }, 200);
    }
};
const fetchGalleryInfo = async () => {
    try {
        const result = await getGalleryVideoInfoDetail({ id: id.value });
        formState.value = result;
        if (formState.value.galleryId != null && !Array.isArray(formState.value.galleryId)) {
            formState.value.galleryId = [formState.value.galleryId];
        }
    } catch (error: any) {
        message.error(error.message);
    }
};
// 支持的MIME类型映射表
const supportedTypes = {
    ".mp4": "video/mp4"
};
const beforeUpload = (file: any) => {
    // 通过文件扩展名验证
    showUploadList.value = true;
    const fileExt = "." + file.name.split(".").pop().toLowerCase();
    if (!Object.keys(supportedTypes).includes(fileExt)) {
        message.error(t('不支持 {fileExt} 格式的视频文件', { fileExt }));
        return false;
    }
    const isLtSize = file.size / 1024 / 1024 < Number(uploadMaxSize);
    if (!isLtSize) {
        message.error(t('只能上传{uploadMaxSize}M以内的视频', { uploadMaxSize }));
    }
    useVideoInfo(file);
    return isLtSize;
};
const useVideoInfo = (file: any) => {
    // 3. 创建临时URL用于获取视频信息
    const videoUrl = URL.createObjectURL(file);
    const video = document.createElement("video");
    video.src = videoUrl;

    // 4. 获取视频时长和第一帧
    video.onloadedmetadata = () => {
        // 获取视频时长（秒）
        const duration = video.duration;
        formState.value.duration = formatDuration(duration);
        // 必须设置currentTime才能触发seeked事件
        video.currentTime = 0.1;
    };

    video.onerror = () => {
        message.error(t('视频文件损坏或无法读取'));
        URL.revokeObjectURL(videoUrl);
        return;
    };
};

const handleChange = (info: UploadChangeParam) => {
    if (!info.file.status) {
        showUploadList.value = false;
        fileList.value = [];
    }
    if (info.file.status == "uploading") {
    }
    if (info.file.status !== "uploading") {
    }
    if (info.file.status === "done") {
        // 上传完成
        if (info.file.response.code !== 0) {
            info.file.status = "error";
            message.error(info.file.response.message);
        } else {
            fileList.value = [info.file];
            formState.value.size = info.file && info.file.size ? `${(info.file.size / 1024 / 1024).toFixed(2)}MB` : "";
            Object.assign(formState.value, info.file.response.data);
            message.success(t('视频上传成功！'));
            showUploadList.value = false;
        }
    } else if (info.file.status === "error") {
        message.error(t('{name} 视频上传失败！', { name: info.file.name }));
    }
};

const deleteVideo = () => {
    formState.value.videoUrl = "";
    formState.value.videoName = "";
    formState.value.duration = "";
    fileList.value = [];
    showUploadList.value = true;
};

// 表单通过验证后提交
const formSubmit = async () => {
    await formRef.value.validate();
    if (!Array.isArray(formState.value.galleryId)) {
        formState.value.galleryId = [toApiGalleryId(formState.value.galleryId)];
    }
    if (!formState.value.galleryId.length) {
        formState.value.galleryId = [0];
    }
    try {
        const submitData: any = {
            id: id.value,
            ...formState.value,
            source: uploadMode.value === "external" ? "external" : "upload"
        };
        if (uploadMode.value === "external") {
            submitData.duration = "";
            submitData.size = "";
        }
        const result = await updateVideoInfo(operation, submitData);
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    }
};

// 处理
const onFormSubmit = () => {
    formSubmit();
};

defineExpose({
    onFormSubmit
});
</script>
<style scoped lang="less">
.gallery-add-item {
    width: 280px;
    max-width: 100%;
    height: 160px;
    background-color: #f7f8fa;
}
.upload-video-box {
    width: 400px;
    max-width: 100%;
}
.video-preview {
    width: 280px;
    max-width: 100%;
    height: 160px;
    border: 1px solid #ddd;
    padding: 5px;
    position: relative;
    video {
        width: 100%;
        height: 100%;
    }
    .btn-remove {
        position: absolute;
        top: -10px;
        right: -10px;
        background-color: #7b7b7b;
        color: #fff;
        width: 20px;
        height: 20px;
        line-height: 22px;
        text-align: center;
        border-radius: 200px;
        cursor: pointer;
        font-size: 14px;
    }
}
@media only screen and (max-width: 767px) {
    .gallery-add-item {
        width: 100%;
    }
    .video-preview {
        width: 100%;
    }
    .upload-video-box {
        width: 100%;
    }
}
</style>
