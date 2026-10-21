<template>
    <tig-popup v-model:show="show" :mask-click="false" position="center" bg-color="transparent" :show-close="false">
        <view class="hot-update-box">
            <view class="box-bg">
                <image class="box-bg-img" :src="staticResource('common/hot-update-bg.png')" />
                <image class="box-bg-ico" :src="staticResource('common/hot-update-ico.png')" />
                <view class="close" @click="appVersionStore.setShowUpdateAppPop(false)">
                    <text class="iconfont-h5 icon-chacha" />
                </view>
                <view class="hot-update">
                    <view class="hot-update-title">{{ downloadLoading ? "下载中~" : "有新版本更新啦！" }}</view>
                    <view class="hot-update-content">
                        <view class="hot-update-row"> {{ $t("有数据需要更新，请点击升级") }} </view>
                    </view>
                    <view class="hot-update-btn">
                        <view class="update-height">
                            <template v-if="downloadLoading">
                                <view class="progress-box">
                                    <view class="progress" :style="{ width: progress + '%' }">
                                        <view class="progress-text">{{ progress }}%</view>
                                    </view>
                                </view>
                            </template>
                            <template v-else>
                                <view class="btn-box" @click="handleUpdate">{{ $t("立即更新") }}</view>
                            </template>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { computed, ref } from "vue";
import { useAppVersionStore } from "@/store/appVersion";
import { staticResource } from "@/utils";

const { t } = useI18n();

const appVersionStore = useAppVersionStore();

const show = computed({
    // get: () => appVersionStore.showUpdateAppPop,
    get: () => appVersionStore.showUpdateAppPop,
    set: (value) => appVersionStore.setShowUpdateAppPop(value)
});

const downloadLoading = ref(false);

const progress = ref(0);
const handleUpdate = () => {
    if (downloadLoading.value) return;
    downloadLoading.value = true;
    let downloadTask = uni.downloadFile({
        url: appVersionStore.updateUrl,
        success: (res) => {
            console.log("下载成功", res);
            plus.runtime.install(
                res.tempFilePath,
                {
                    force: true
                },
                () => {
                    appVersionStore.setShowUpdateAppPop(false);
                    progress.value = 0;
                    downloadLoading.value = false;
                    uni.showModal({
                        title: t("系统提示"),
                        content: t("新版本已经更新完成，需要重启应用"),
                        showCancel: false,
                        confirmText: t("确定"),
                        success: (modalRes) => {
                            if (modalRes.confirm) {
                                // #ifdef APP-PLUS
                                plus.runtime.restart();
                                // #endif
                                // #ifdef APP-HARMONY
                                plus.runtime.quit(); // 关闭应用
                                // #endif
                            } else if (appVersionStore.osName !== "android") {
                                const threadClass = plus.ios.importClass("NSThread");

                                const mainThread = plus.ios.invoke(threadClass, "mainThread");
                                plus.ios.invoke(mainThread, "exit");
                            } else {
                                plus.runtime.quit();
                            }
                        }
                    });
                },
                (error) => {
                    console.log("安装失败", error);
                }
            );
        },
        complete: () => {}
    });

    downloadTask.onProgressUpdate((res) => {
        progress.value = res.progress;
    });
};
</script>

<style lang="scss" scoped>
.hot-update-box {
    width: 100vw;
    box-sizing: border-box;
    padding: 0 60rpx;
    .box-bg {
        border-radius: 38rpx;
        background: rgba(255, 255, 255, 1);
        position: relative;

        .box-bg-img {
            width: 100%;
            height: 200rpx;
            top: 0;
            left: 0;
            position: absolute;
        }

        .box-bg-ico {
            width: 242rpx;
            height: 228rpx;
            top: -60rpx;
            left: 50%;
            transform: translateX(-50%);
            position: absolute;
        }

        .close {
            position: absolute;
            top: 10rpx;
            right: 10rpx;
            z-index: 3;
            color: #ffffff;
            cursor: pointer;
            padding: 20rpx;

            .icon-chacha {
                font-size: 36rpx;
                font-weight: 600;
            }
        }
    }
    .hot-update {
        padding-top: 191rpx;
        position: relative;
        z-index: 2;

        .hot-update-title {
            font-weight: 600;
            font-size: 34rpx;
            color: #013148;
            line-height: 48rpx;
            height: 48rpx;
            text-align: center;
        }

        .hot-update-content {
            padding: 34rpx 40rpx 0;

            .hot-update-row {
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 26rpx;
                color: #013148;
                padding-bottom: 20rpx;
                font-weight: 400;
            }
        }

        .hot-update-btn {
            padding: 34rpx 40rpx 46rpx;
            width: 100%;
            .update-height {
                height: 80rpx;
                display: flex;
                align-items: center;
                justify-content: center;

                .progress-box {
                    width: 100%;
                    height: 40rpx;
                    border-radius: 40rpx;
                    background: #f0f0f0;
                    .progress {
                        height: 100%;
                        width: 0;
                        border-radius: 40rpx;
                        background: linear-gradient(270deg, #ff5f01 0%, #df0703 100%);
                        position: relative;
                        overflow: hidden;
                        .progress-text {
                            font-size: 24rpx;
                            color: #ffffff;
                            position: absolute;
                            right: 0;
                            top: 50%;
                            transform: translateY(-50%);
                            padding-right: 10rpx;
                        }
                    }
                }
            }
            .btn-box {
                background: linear-gradient(270deg, #ff5f01 0%, #df0703 100%);
                border-radius: 40rpx;
                width: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 34rpx;
                color: #ffffff;
            }
        }
    }
}
</style>
