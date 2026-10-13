<template>
    <view class="upload-media">
        <view class="media-preview" :style="{ 'grid-template-columns': `repeat(${props.cols}, 1fr)` }">
            <view v-for="(item, index) in mediaList" :key="index" class="media-item">
                <tig-image v-if="item.type === 'image'" preview :src="item.url" class="media-preview-image" />
                <!--                <view v-if="item.type === 'image'" class="media-preview-image" :style="['background-image: url(' + item.url + ')']" />-->
                <video
                    v-else-if="item.type === 'video'"
                    :id="'video-' + index"
                    :src="item.url"
                    :controls="true"
                    class="media-preview-video"
                    @click.stop="playVideoInFullScreen(index)"
                    @play="playVideoInFullScreen(index)"
                    @fullscreenchange="onFullScreenChange"
                />
                <view v-if="item.uploading" class="progress">
                    <up-loading-icon color="#ffffff" />
                    <view class="progressText">{{ $t("上传中...") }} {{ item.progress ?? 0 }}%</view>
                </view>
                <text class="iconfont-h5 icon-clear delete-btn" style="font-size: 50rpx; color: #cccccc" @click="removeMedia(index)" />
            </view>
            <template v-if="mediaList.length < props.maxCount">
                <view class="fileBox" @click="chooseMedia">
                    <view class="iconfont-h5 icon-8tupian-1 icon" />
                    <view class="tip">{{ $t("上传图片/视频最多上传") }}{{ props.maxCount }}{{ $t("张") }}</view>
                </view>
            </template>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, defineProps, ref } from "vue";
import { baseUrl, getSecret } from "@/utils/request";
import { imageFormat } from "@/utils/format";
import { isOverseas } from "@/utils";
import { useI18n } from "vue-i18n";
// #ifdef APP-PLUS
import { requestPhotosPermission } from "@/utils/appPermission";
import TigImage from "@/components/tig/tig-image/tig-image.vue";
// #endif

const { t, locale } = useI18n();

interface MediaItem {
    type: "image" | "video";
    videoUrl?: string;
    videoCover?: string;
    picUrl?: string;
    picId?: number;
    url?: string;
    progress?: number;
    uploading?: boolean;
    uploadId?: string;
}

const props = withDefaults(
    defineProps<{
        multiple?: boolean;
        maxCount?: number;
        cols?: number;
        imgApiUrl?: string;
        videoApiUrl?: string;
        /** 视频数量上限；0 表示不限制 */
        maxVideoCount?: number;
    }>(),
    {
        multiple: true,
        maxCount: 9,
        cols: 3,
        imgApiUrl: "user/user/uploadImg",
        videoApiUrl: "user/user/uploadVideo",
        maxVideoCount: 0
    }
);

const mediaList = defineModel<MediaItem[]>("mediaList", { default: [] });
/** 按 uploadId 保存上传任务，删除上传中的媒体时可准确中止对应任务。 */
const uploadTasks = new Map<string, any>();
const cancelledUploads = new Set<string>();
const currentVideoIndex = ref<number | null>(null);
const videoContexts = ref<Record<number, any>>({});

/** 已选视频数量 */
const videoCount = computed(() => mediaList.value.filter((item) => item.type === "video").length);

/** 中文文案中的数量用汉字表述，如 1 → 一 */
const CN_NUMBERS = ["零", "一", "二", "三", "四", "五", "六", "七", "八", "九"];
const cnCount = (count: number) => CN_NUMBERS[count] ?? count;

const maxVideoTip = (count: number) =>
    isOverseas() ? t("最多上传{0}个视频", [locale.value.startsWith("zh") ? cnCount(count) : count]) : `最多上传${cnCount(count)}个视频`;

/**
 * 依据 maxVideoCount 筛选本次选择：超出视频配额的视频项丢弃并提示，图片不受影响。
 * maxVideoCount 为 0 时不限制，原样返回。
 */
const limitVideoCount = <T extends { fileType: string }>(batch: T[]): T[] => {
    if (!props.maxVideoCount) return batch;
    let quota = props.maxVideoCount - videoCount.value;
    const kept: T[] = [];
    let dropped = false;
    batch.forEach((item) => {
        if (item.fileType !== "video") {
            kept.push(item);
            return;
        }
        if (quota > 0) {
            quota -= 1;
            kept.push(item);
        } else {
            dropped = true;
        }
    });
    if (dropped) {
        uni.showToast({ title: maxVideoTip(props.maxVideoCount), icon: "none" });
    }
    return kept;
};

// 选择媒体文件的主方法
const chooseMedia = async () => {
    // #ifdef APP-PLUS
    try {
        // 请求相册权限
        const hasPhotosPermission = await requestPhotosPermission();
        if (!hasPhotosPermission) {
            uni.showToast({
                title: t("需要相册权限才能选择图片"),
                icon: "none"
            });
            return;
        }
    } catch (error) {
        console.error("权限请求失败:", error);
        uni.showToast({
            title: t("权限请求失败，请重试"),
            icon: "none"
        });
    }
    // #endif
    // #ifdef H5
    // H5 平台使用动态创建 input 的方式
    createFileInput();
    // #endif

    // #ifndef H5
    // 非 H5 平台使用 uni.chooseMedia
    uniChooseMedia();
    // #endif
};

// H5 平台通过动态创建 input 实现文件选择
const createFileInput = () => {
    // #ifdef H5
    // 创建 input 元素
    const input = document.createElement("input");
    input.type = "file";
    input.accept = "image/*,video/*"; // 限制文件类型为图片和视频
    input.multiple = Boolean(props.multiple);

    // 设置样式（隐藏 input）
    input.style.display = "none";

    // 监听文件选择事件
    input.onchange = (event: Event) => {
        const target = event.target as HTMLInputElement;
        if (target.files && target.files.length > 0) {
            const files = Array.from(target.files);
            processH5Files(files);
        }
        // 清理 DOM 中的 input 元素
        if (input.parentNode) {
            input.parentNode.removeChild(input);
        }
    };
    // 将 input 添加到 DOM 中并触发点击
    document.body.appendChild(input);
    input.click();
    // #endif
};

// 处理 H5 平台选择的文件
const processH5Files = (files: File[]) => {
    // #ifdef H5
    // 限制总数
    const remainingSlots = props.maxCount - mediaList.value.length;
    if (remainingSlots <= 0) {
        uni.showToast({
            title: `${t("限制上传")}${props.maxCount}${t("个数")}`,
            icon: "none"
        });
        return;
    }
    const filesToProcess = files.slice(0, remainingSlots).map((file) => ({ file, fileType: validateFileType(file.type) }));
    if (filesToProcess.some((item) => item.fileType === "unknown")) {
        uni.showToast({
            title: t("不支持的文件类型"),
            icon: "none"
        });
    }

    limitVideoCount(filesToProcess.filter((item) => item.fileType !== "unknown")).forEach(({ file, fileType }) => {
        // 生成预览 URL
        const previewUrl = URL.createObjectURL(file);
        uploadFile(previewUrl, fileType, file);
    });
    // #endif
};

// 验证文件类型
const validateFileType = (mimeType: string): string => {
    if (mimeType.startsWith("image/")) {
        return "image";
    } else if (mimeType.startsWith("video/")) {
        return "video";
    } else {
        return "unknown";
    }
};

// 非 H5 平台使用 uni.chooseMedia
const uniChooseMedia = () => {
    if (mediaList.value.length > props.maxCount) {
        uni.showToast({
            title: `${t("限制上传")}${props.maxCount}${t("个数")}`,
            icon: "none"
        });
        return;
    }
    // #ifndef H5
    uni.chooseMedia({
        count: props.maxCount - mediaList.value.length,
        mediaType: ["image", "video"],
        sourceType: ["album", "camera"],
        maxDuration: 60,
        camera: "back",
        success: (res) => {
            const batch = (res.tempFiles as any[]).map((file: any) => ({ file, fileType: file.fileType, path: file.tempFilePath }));
            limitVideoCount(batch).forEach((item) => uploadFile(item.path, item.fileType, item.file));
        },
        fail: (err) => {
            if (err.errMsg.includes("cancel")) return;
            // 降级处理
            uni.showActionSheet({
                itemList: ["选择图片", "选择视频"],
                success: (res) => {
                    if (res.tapIndex === 0) {
                        chooseImage();
                    } else {
                        chooseVideo();
                    }
                }
            });
        }
    });
    // #endif
};

// 降级处理 - 选择图片
const chooseImage = () => {
    // #ifndef H5
    uni.chooseImage({
        count: 9 - mediaList.value.length,
        sizeType: ["original", "compressed"],
        sourceType: ["album", "camera"],
        success: (res: any) => {
            res.tempFilePaths.forEach((path: string, index: number) => {
                uploadFile(path, "image", res.tempFiles[index]);
            });
        }
    });
    // #endif
};

// 降级处理 - 选择视频
const chooseVideo = () => {
    // #ifndef H5
    if (props.maxVideoCount && videoCount.value >= props.maxVideoCount) {
        uni.showToast({ title: maxVideoTip(props.maxVideoCount), icon: "none" });
        return;
    }
    uni.chooseVideo({
        sourceType: ["album", "camera"],
        compressed: true,
        maxDuration: 60,
        camera: "back",
        success: (res) => {
            uploadFile(res.tempFilePath, "video", res.tempFile);
        }
    });
    // #endif
};

// 删除媒体文件
const removeMedia = (index: number) => {
    const item = mediaList.value[index];
    if (item?.uploading && item.uploadId) {
        cancelledUploads.add(item.uploadId);
        uploadTasks.get(item.uploadId)?.abort?.();
        uploadTasks.delete(item.uploadId);
    }

    // H5 平台需要释放预览 URL
    // #ifdef H5
    // if (item.path.startsWith("blob:")) {
    //     URL.revokeObjectURL(item.path);
    // }
    // #endif

    mediaList.value.splice(index, 1);
};

const uploadFile = (filePath: any, fileType: string, file?: File) => {
    // 支持png、jpeg、jpg、gif、webp、tiff格式
    if (fileType === "image" && !file?.type.match(/\/(png|jpe?g|gif|webp|tiff)$/i)) {
        uni.showToast({
            title: t("不支持上传此文件格式"),
            icon: "none"
        });
        return;
    }
    const type = fileType as MediaItem["type"];
    const uploadId = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
    const uploadingItem: MediaItem = {
        type,
        url: filePath,
        progress: 0,
        uploading: true,
        uploadId
    };
    mediaList.value.push(uploadingItem);

    const findUploadingIndex = () => mediaList.value.findIndex((item) => item.uploadId === uploadId);
    const removeUploadingItem = () => {
        const uploadIndex = findUploadingIndex();
        if (uploadIndex >= 0) {
            mediaList.value.splice(uploadIndex, 1);
        }
    };

    const uploadTask = uni.uploadFile({
        url: baseUrl + import.meta.env.VITE_API_PREFIX + (fileType === "image" ? props.imgApiUrl : props.videoApiUrl),
        filePath: filePath,
        name: "file",
        header: {
            Authorization: "Bearer " + uni.getStorageSync("token"),
            Secret: getSecret()
        },
        // 视频需要此操作
        formData: {
            file
        },
        success: (uploadFileRes: any) => {
            uploadTasks.delete(uploadId);
            if (cancelledUploads.delete(uploadId)) return;
            uni.hideLoading();
            const { data, message } = JSON.parse(uploadFileRes.data);
            if (!data) {
                removeUploadingItem();
                uni.showToast({
                    title: message || "上传失败",
                    icon: "none"
                });
                return;
            }
            const uploadIndex = findUploadingIndex();
            if (uploadIndex < 0) return;
            mediaList.value.splice(uploadIndex, 1, {
                ...data,
                url: type === "image" ? imageFormat(data.picThumb) : data.videoUrl,
                type
            });
        },
        fail: (error) => {
            uploadTasks.delete(uploadId);
            if (cancelledUploads.delete(uploadId)) {
                uni.hideLoading();
                return;
            }
            uni.showToast({
                title: error.errMsg || t("上传失败"),
                icon: "none"
            });
            removeUploadingItem();
            uni.hideLoading();
        }
    });
    uploadTasks.set(uploadId, uploadTask);
    uploadTask.onProgressUpdate((progressEvent) => {
        const uploadIndex = findUploadingIndex();
        if (uploadIndex < 0) return;
        mediaList.value[uploadIndex] = {
            ...mediaList.value[uploadIndex],
            progress: Math.min(100, Math.max(0, Number(progressEvent.progress)))
        };
    });
};

const onFullScreenChange = (e: any) => {
    // 当退出全屏时重置当前视频索引
    if (!e.detail.fullScreen) {
        currentVideoIndex.value = null;
    }
};

const playVideoInFullScreen = (index: number) => {
    // 创建视频上下文（如果尚未创建）
    if (!videoContexts.value[index]) {
        videoContexts.value[index] = uni.createVideoContext(`video-${index}`);
    }

    // 设置当前视频索引
    currentVideoIndex.value = index;

    // 播放并进入全屏
    videoContexts.value[index].requestFullScreen();
    videoContexts.value[index].play();
};
</script>

<style scoped lang="scss">
:deep(.u-form-item__body__right__content) {
    width: 100%;
}

.upload-media {
    width: calc(100vw - 80rpx);
}

.fileBox {
    background: #fafafa;
    border-radius: 4rpx;
    border: 1px dashed #9f9f9e;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 0 30rpx;
    aspect-ratio: 1;
    .icon {
        font-size: 32rpx;
    }
    .tip {
        font-weight: 500;
        font-size: 22rpx;
        color: #9a9a9a;
        line-height: 30rpx;
        margin-top: 26rpx;
    }
}

/* 根据不同列数设置网格布局 */

.media-preview {
    gap: 20rpx;
    display: grid;
}

.media-item {
    position: relative;
    width: calc((100vw - 120rpx) / 3);
    height: 100%;
    border: 1rpx solid #eee;
    border-radius: 10rpx;
    //overflow: hidden;
    aspect-ratio: 1;
    .progress {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        z-index: 100;
        color: #fff;
        gap: 10rpx;
    }
}

.media-preview-image,
.media-preview-video {
    width: 100%;
    height: 100%;
}

.media-preview-image {
    background-size: 100% 100%;
    background-repeat: no-repeat;
    background-position: center;
    object-fit: cover;
}

.media-info {
    padding: 10rpx;
    font-size: 20rpx;
    color: #666;
}

.file-type {
    display: block;
    font-weight: bold;
}

.delete-btn {
    position: absolute;
    top: -25rpx;
    right: -25rpx;
    font-size: 30rpx;
    padding: 5rpx;
    z-index: 10;
}

button {
    margin: 10rpx 0;
}

:deep(.u-form-item__body__right__content__slot) {
    width: 100%;
}
</style>
