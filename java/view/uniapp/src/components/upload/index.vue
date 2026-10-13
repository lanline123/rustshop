<template>
    <view @click="handleCheck">
        <up-upload
            v-if="!isCustom"
            :preview-full-image="true"
            :file-list="fileLists"
            name="1"
            multiple
            :max-count="limit"
            :size-type="['original', 'compressed']"
            @after-read="handlePicSelect"
            @delete="handlePicDelete"
        />
        <up-upload
            v-if="isCustom"
            :preview-full-image="true"
            :file-list="fileLists"
            name="1"
            multiple
            :max-count="limit"
            :size-type="['original', 'compressed']"
            @after-read="handlePicSelect"
            @delete="handlePicDelete"
        >
            <template #trigger>
                <slot name="trigger" />
            </template>
        </up-upload>
    </view>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from "vue";
import { imageFormat } from "@/utils/format";
import { getSecret, baseUrl } from "@/utils/request";
import { useI18n } from "vue-i18n";
// #ifdef APP-PLUS
import { requestPhotosPermission } from "@/utils/appPermission";
// #endif

const { t } = useI18n();

const props = defineProps({
    limit: {
        type: Number,
        default: 1
    },
    apiUrl: {
        type: String,
        default: "user/user/uploadImg"
    },
    modelValue: {
        type: [Array, Object],
        default: () => []
    },
    isValueArray: {
        type: Boolean,
        default: false
    },
    isCustom: {
        type: Boolean,
        default: false
    },
    responseDataType: {
        type: String,
        default: "gallery"
    }
});

const emit = defineEmits(["update:modelValue", "uploading-change"]);
const MAX_IMAGE_SIZE = 20 * 1024 * 1024;

// 处理上传点击事件，在APP端检查权限
const handleCheck = async () => {
    console.log("check");

    // #ifdef APP-PLUS
    try {
        // 请求相册权限
        const hasPhotosPermission = await requestPhotosPermission();
        if (!hasPhotosPermission) {
            uni.showToast({
                title: t("需要相册权限才能选择图片"),
                icon: "none"
            });
        }
    } catch (error) {
        console.error("权限请求失败:", error);
        uni.showToast({
            title: t("权限请求失败，请重试"),
            icon: "none"
        });
    }
    // #endif
};

interface FileLists {
    url: string;
}

const fileLists = ref<FileLists[]>([]);

watch(
    () => props.modelValue,
    (newVal: any) => {
        if (newVal.length === 0) {
            fileLists.value = [];
        }
        if (fileLists.value.length > 0) return;
        if (!newVal) return;
        if (typeof newVal == "object") {
            if (Array.isArray(newVal) && newVal.length === 0) {
                return;
            } else if (Object.values(newVal).length === 0 || !Object.values(newVal)[0]) {
                return;
            }
        }
        nextTick(() => {
            if (uni.$u.test.object(newVal)) {
                fileLists.value.push({
                    ...newVal,
                    url: imageFormat(newVal.picUrl)
                });
            } else {
                fileLists.value = newVal.map((item: any) => {
                    return {
                        ...item,
                        url: imageFormat(item.picUrl)
                    };
                });
                picsData.value = newVal;
            }
        });
    },
    {
        immediate: true,
        deep: true
    }
);

const picsData = ref<any[]>([]);

const handlePicSelect = async (e: any) => {
    const files = Array.isArray(e.file) ? e.file : [e.file];
    const validFiles = files.filter((file: any) => {
        if (typeof file?.size !== "number" || file.size <= MAX_IMAGE_SIZE) return true;
        uni.showToast({
            title: t("图片大小不能超过20MB"),
            icon: "none"
        });
        return false;
    });
    if (validFiles.length === 0) return;
    // 兼容多端
    if (validFiles.length === 1 || props.limit === 1) {
        uploadFile(validFiles[0]);
    } else {
        validFiles.forEach((item: string) => {
            uploadFile(item);
        });
    }
};

const handlePicDelete = (e: any) => {
    fileLists.value.splice(e.index, 1);
    if (props.limit > 1 || props.isValueArray) {
        picsData.value.splice(e.index, 1);
        emit("update:modelValue", picsData.value);
    } else {
        emit("update:modelValue", { picName: "", picThumb: "", picUrl: "" });
    }
};

const uploadFile = (filePath: any) => {
    let url;
    const token = uni.getStorageSync("token") || "";
    emit("uploading-change", true);
    uni.uploadFile({
        url: baseUrl + import.meta.env.VITE_API_PREFIX + props.apiUrl,
        filePath: filePath.url,
        name: "file",
        header: {
            Authorization: props.responseDataType === "url" ? "Bearer " + token : token,
            Secret: getSecret()
        },
        success: (uploadFileRes: any) => {
            emit("uploading-change", false);
            uni.hideLoading();
            let result: { code?: number; message?: string; data?: any } | null = null;
            try {
                result = JSON.parse(uploadFileRes.data);
            } catch (error) {
                console.error("上传响应解析失败:", error, uploadFileRes.data);
            }
            if (uploadFileRes.statusCode < 200 || uploadFileRes.statusCode >= 300 || result?.code !== 0 || !result?.data) {
                uni.showToast({
                    title: result?.message || t("图片上传失败"),
                    icon: "none"
                });
                return;
            }
            const { data } = result;
            const normalizedData = props.responseDataType === "url"
                ? {
                    picName: filePath.name || "营业执照",
                    picThumb: data,
                    picUrl: data,
                    url: data
                }
                : data;
            url = normalizedData.picThumb;
            fileLists.value.push({
                url: imageFormat(url)
            });

            if (props.limit > 1 || props.isValueArray) {
                picsData.value.push({
                    picName: normalizedData.picName,
                    picThumb: normalizedData.picThumb,
                    picUrl: normalizedData.picUrl,
                    ...(normalizedData.url ? { url: normalizedData.url } : {})
                });
                emit("update:modelValue", picsData.value);
            } else {
                emit("update:modelValue", {
                    picName: normalizedData.picName,
                    picThumb: normalizedData.picThumb,
                    picUrl: normalizedData.picUrl,
                    ...(normalizedData.url ? { url: normalizedData.url } : {})
                });
            }

            uni.showToast({
                title: t("图片上传成功")
            });
        },
        fail: (_error) => {
            emit("uploading-change", false);
            uni.hideLoading();

            uni.showToast({
                title: t("图片上传失败"),
                icon: "none"
            });
        }
    });
};
</script>

<style lang="scss" scoped></style>
