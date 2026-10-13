<template>
    <view>
        <tig-layout :need-safe-top="true">
            <view class="leave-message">
                <uni-forms ref="formRef" :model-value="form" label-width="170rpx" :rules="rules">
                    <uni-forms-item :label="$t('问题描述')" name="logInfo">
                        <uni-easyinput
                            v-model="form.logInfo"
                            type="textarea"
                            primary-color="rgb(192, 196, 204)"
                            maxlength="100"
                            :input-border="false"
                            auto-height
                            :placeholder="$t('为保障你的权益，请尽可能详细的描述，最多200字')"
                            placeholder-style="font-size: 26rpx;"
                        />
                    </uni-forms-item>
                    <uni-forms-item :label="$t('上传凭证')" name="description">
                        <uni-file-picker
                            v-model="fileLists"
                            :auto-upload="false"
                            limit="3"
                            :title="$t('最多选择3张图片')"
                            @select="handlePicSelect"
                            @delete="handlePicDelete"
                        >
                            <text class="iconfont-h5 icon-camera" style="font-size: 56rpx; color: #cccccc" />
                        </uni-file-picker>
                    </uni-forms-item>
                </uni-forms>
            </view>
            <tig-fixed-placeholder background-color="#fff">
                <view class="leave-message-btn">
                    <tig-button class="btn" @click="handleSubmit">{{ $t("提交") }}</tig-button>
                </view>
            </tig-fixed-placeholder>
        </tig-layout>
    </view>
</template>

<script setup lang="ts">
/**
 * 售后留言页
 * - 填写问题描述（最多100字）
 * - 上传凭证图片（最多3张）
 * - 提交后跳转协商记录页
 */
import { ref, reactive } from "vue";
import { getSecret, baseUrl } from "@/utils/request";
import { aftersalesFeedback } from "@/api/user/afterSale";
import { onLoad } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

interface ReturnPic {
    picName: string;
    picThumb: string;
    picUrl: string;
}

const form = reactive({
    logInfo: "",
    returnPic: [] as ReturnPic[],
    id: 0
});
const rules = {
    logInfo: {
        rules: [{ required: true, errorMessage: t("留言不能为空") }]
    }
};

interface FileLists {
    name: string;
    extname: string;
    url: string;
}
const fileLists = ref<FileLists[]>([]);

onLoad((options) => {
    if (options && options.id) {
        form.id = options.id;
    }
});

const handlePicSelect = (e: any) => {
    // 兼容多端
    if (e.tempFilePaths.length === 1) {
        uploadFile(e.tempFilePaths[0]);
    } else {
        e.tempFilePaths.forEach((item: string) => {
            uploadFile(item);
        });
    }
};

const handlePicDelete = (e: any) => {
    fileLists.value.splice(e.index, 1);
};

const uploadFile = (filePath: any) => {
    let name, extname, url;
    uni.uploadFile({
        url: baseUrl + import.meta.env.VITE_API_PREFIX + "user/user/uploadImg",
        filePath,
        name: "file",
        header: {
            Authorization: uni.getStorageSync("token"),
            Secret: getSecret()
        },
        success: (uploadFileRes: any) => {
            uni.hideLoading();
            const { data } = JSON.parse(uploadFileRes.data);
            name = data.picName;
            extname = data.picUrl.split(".")[1];
            url = data.picThumb;
            fileLists.value.push({
                name,
                extname,
                url
            });
            form.returnPic.push({
                picName: data.picName,
                picThumb: data.picThumb,
                picUrl: data.picUrl
            });
            uni.showToast({
                title: t("图片上传成功")
            });
        },
        fail: (error) => {
            uni.hideLoading();

            uni.showToast({
                title: t("图片上传失败"),
                icon: "none"
            });
        }
    });
};
const formRef = ref();
const handleSubmit = () => {
    formRef.value
        .validate()
        .then(() => {
            __aftersalesFeedback();
        })
        .catch((err: any) => {
            console.log("表单错误信息：", err);
        });
};

const __aftersalesFeedback = async () => {
    try {
        const result = await aftersalesFeedback(form);

        uni.redirectTo({
            url: "/pages/user/afterSale/negotiate",
            success: () => {
                uni.$emit("refreshNegotiate", form.id);
            }
        });
    } catch (error) {
        console.error(error);
    }
};
</script>
<style>
page {
    background-color: #fff;
}
</style>
<style lang="scss" scoped>
.leave-message {
    height: 100%;
    padding: 0 20rpx;
}
:deep(.special-item) {
    &.uni-forms-item {
        border-bottom: none;
        margin-bottom: 0;
    }
}
.uni-forms-item {
    margin: 10rpx 25rpx 10rpx 25rpx;
    border-bottom: 1px solid #eee;
    &:last-child {
        border-bottom: none;
        padding-bottom: 20rpx;
    }
}
.leave-message-btn {
    padding: 25rpx;

    .btn {
        font-size: 28rpx;
    }

    .btn-box {
        width: 100%;
        height: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: var(--general);
        color: #fff;

        &:active {
            opacity: 0.7;
        }
    }
}

:deep(.uni-forms-item__error) {
    top: 75%;
}
:deep(.uni-progress-bar) {
    display: none;
}
</style>
