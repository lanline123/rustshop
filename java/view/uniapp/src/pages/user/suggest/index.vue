<template>
    <tig-layout :title="$t('意见建议')" :border-bottom="true">
        <view class="container">
            <view class="title"> {{ $t("意见建议") }}</view>
            <view class="content">
                <up-textarea
                    v-model="message"
                    input-align="right"
                    height="120"
                    :placeholder="$t('您填写的信息越全，问题越有的效解决哦~')"
                    border="none"
                    maxlength="200"
                    style="background: #f5f5f5"
                    :focus="false"
                />
                <view class="bottom">
                    <view class="upload-box">
                        <upload v-model="feedbackPics" :is-value-array="true" :limit="3" :is-custom="true">
                            <template #trigger>
                                <view class="tip">
                                    <up-icon name="photo" size="25" color="#a6a7a7"></up-icon>
                                    <view class="txt">{{ $t("上传图片") }}</view>
                                    <view class="txt">{{ $t("最多上传{0}张", [3]) }}</view>
                                </view>
                            </template>
                        </upload>
                    </view>
                    <view class="popup-item-footer"> {{ message.length }}/200 </view>
                </view>
            </view>
        </view>
        <tig-fixed-placeholder background-color="#fff">
            <view class="btn-box">
                <tig-button class="btn" @click="submit" :loading="isLoading" :disabled="message == ''"> {{ $t("提交意见") }} </tig-button>
            </view>
        </tig-fixed-placeholder>
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 意见建议页
 * - 用户填写反馈内容（最多200字）
 * - 支持上传图片（最多3张）
 * - 提交后清空表单
 */
import { nextTick, reactive, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { addConsultation } from "@/api/product/product";
import upload from "@/components/upload/index.vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const message = ref("");
const feedbackPics = ref([]);
const isLoading = ref(false);

const submit = async () => {
    if (message.value == "") {
        uni.showToast({
            title: t("请填写您的意见")
        });
    }
    isLoading.value = true;
    try {
        await addConsultation({
            type: 0, // 0留言类型使用
            content: message.value,
            feedbackPics: feedbackPics.value?.map((item) => item?.picUrl)
        });
        message.value = "";
        feedbackPics.value = [];
        isLoading.value = false;
        uni.showToast({
            title: t("已收到您的反馈")
        });
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    } finally {
        isLoading.value = false;
    }
};
</script>

<style lang="scss" scoped>
.container {
    width: 100%;
    background: #fff;
    margin-top: 20rpx;
    padding: 20rpx;
    border-radius: 10rpx;
    .title {
        font-size: 26rpx;
    }
    .content {
        width: 100%;
        height: fit-content;
        margin-top: 18rpx;
        background: #f5f5f5;
        border-radius: 10rpx;
        padding-bottom: 18rpx;
        .bottom {
            display: flex;
            justify-content: space-between;
            .popup-item-footer {
                align-self: flex-end;
                padding-right: 10rpx;
                font-size: 20rpx;
                color: #a6a7a7;
            }
        }
        .upload-box {
            flex: 1;
            padding-top: 20rpx;
            padding-left: 18rpx;
            background: #f5f5f5;
            .tip {
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                width: 180rpx;
                height: 180rpx;
                // margin-left: 18rpx;
                border: 2rpx dashed #cfd0d1;
                border-radius: 6rpx;
                .txt {
                    font-size: 22rpx;
                    line-height: 30rpx;
                    color: #a6a7a7;
                    text-align: center;
                }
            }
        }
    }
}

.btn-box {
    padding: 25rpx 15rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;

    .btn {
        width: 100%;
        font-size: 28rpx;
        height: 100%;
    }
}
</style>
