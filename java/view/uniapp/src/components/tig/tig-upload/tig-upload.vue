<template>
    <view style="height: inherit; width: inherit" @click="handleChoose">
        <slot />
    </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { getSecret, baseUrl } from "@/utils/request";
import { useI18n } from "vue-i18n";
import { useConfigStore } from "@/store/config";

const { t } = useI18n();
const configStore = useConfigStore();

const props = defineProps({
    count: {
        type: Number,
        default: 1
    },
    modelValue: {
        type: String,
        default: ""
    },
    requestUrl: {
        type: String,
        default: "user/user/uploadImg"
    }
});
const files = ref<string[]>([]);
const emit = defineEmits(["update:modelValue", "change"]);

const handleChoose = () => {
    uni.chooseImage({
        count: props.count,
        success: async (res: any) => {
            uni.showLoading({
                title: t("上传中...")
            });
            res.tempFilePaths.map((file: string) => {
                uni.uploadFile({
                    url: baseUrl + import.meta.env.VITE_API_PREFIX + props.requestUrl,
                    filePath: file,
                    name: "file",
                    header: {
                        Authorization: uni.getStorageSync("token"),
                        "X-Client-Type": configStore.XClientType,
                        Secret: getSecret()
                    },
                    success: (uploadFileRes) => {
                        uni.hideLoading();

                        let result: { code?: number; message?: string; data?: any } | null = null;
                        try {
                            result = JSON.parse(uploadFileRes.data);
                        } catch (error) {
                            console.error("上传响应解析失败:", error, uploadFileRes.data);
                        }

                        if (result?.code !== 0) {
                            uni.showToast({
                                title: result?.message || t("图片上传失败"),
                                icon: "none"
                            });
                            return;
                        }

                        if (props.count > 1) {
                            files.value.push(result.data);
                            emit("update:modelValue", files.value);
                        } else {
                            uni.showToast({
                                title: t("图片上传成功")
                            });
                            emit("change");
                        }
                    },
                    fail: (error) => {
                        uni.hideLoading();
                        uni.showToast({
                            title: t("图片上传失败"),
                            icon: "none"
                        });
                    }
                });
            });
        },
        fail: (error) => {
            uni.showToast({
                title: t("图片选择失败"),
                icon: "none"
            });
        }
    });
};
</script>

<style lang="scss" scoped></style>
