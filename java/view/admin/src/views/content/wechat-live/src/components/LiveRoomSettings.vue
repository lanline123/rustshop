<!--
 * @file 直播间设置组件
 * @description 显示和编辑直播间的配置信息
 * @module views/content/wechat-live/src/components/LiveRoomSettings
-->
<template>
    <el-form ref="formRef" :model="formState" :rules="formRules" label-width="0" class="live-room-settings">
        <section class="settings-block">
            <div class="section-title">{{ $t('分享卡片样式配置') }}</div>
            <div class="settings-card">
                <div class="preview preview-share">
                    <img src="@/assets/live/liveShare.png" alt="分享卡片" />
                    <div class="preview-share-mask" :style="getPreviewStyle(formState.shareCardCover)"></div>
                </div>
                <div class="settings-content">
                    <div class="setting-title">
                        <span class="required-mark">*</span>
                        <span class="title-text">{{ $t('分享卡片样式：') }}</span>
                    </div>
                    <div>
                        <div class="title-desc">{{ $t('官方推荐的小程序直播样式会出现在购物直播频道内。') }}</div>
                        <div class="setting-hint">{{ $t('建议尺寸：800像素*800像素，图片大小不得超过{imageUploadMaxSize}MB。', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }}</div>
                        <el-form-item prop="shareCardCover">
                            <FormAddGallery
                                v-if="!loading"
                                v-model:photo="formState.shareCardCover"
                                :disabled="act === 'detail'"
                                :isMultiple="false"
                                pageType="overseas"
                                :dataType="LIVE_ROOM_IMAGE_TRANSLATION_DATA_TYPE"
                            />
                        </el-form-item>
                    </div>
                </div>
            </div>
        </section>

        <section class="settings-block">
            <div class="section-title">{{ $t('官方收录样式') }}</div>
            <div class="settings-card">
                <div class="preview preview-live-card">
                    <img class="preview-live-card-img" src="@/assets/live/liveCard.png" alt="直播卡片" />
                    <div class="preview-live-card-mask" :style="getPreviewStyle(formState.liveCardCover)"></div>
                </div>
                <div class="settings-content">
                    <div class="setting-title">
                        <span class="required-mark">*</span>
                        <span class="title-text">{{ $t('直播卡片封面：') }}</span>
                    </div>
                    <div>
                        <div class="title-desc">{{ $t('官方推荐的小程序直播样式会出现在购物直播频道内。') }}</div>
                        <div class="setting-hint">{{ $t('建议尺寸：800像素*800像素，图片大小不得超过{imageUploadMaxSize}MB。', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }}</div>
                        <el-form-item prop="liveCardCover">
                            <FormAddGallery
                                v-if="!loading"
                                v-model:photo="formState.liveCardCover"
                                :disabled="act === 'detail'"
                                :isMultiple="false"
                                pageType="overseas"
                                :dataType="LIVE_ROOM_IMAGE_TRANSLATION_DATA_TYPE"
                            />
                        </el-form-item>
                    </div>
                </div>
            </div>
        </section>

        <section class="settings-block">
            <div class="section-title">{{ $t('直播间样式配置') }}</div>
            <div class="settings-card settings-card--wall">
                <div class="preview preview-wall" :class="{ 'preview-wall--device-live': isDeviceLive }">
                    <img class="preview-wall-img" src="@/assets/live/live_bg.png" alt="直播间背景墙" />
                    <div class="preview-wall-mask" :style="getPreviewStyle(formState.backgroundWall)"></div>
                    <div v-if="isDeviceLive" class="preview-wall-stream">
                        <div class="preview-wall-stream__title">{{ $t('视频播放区域示意') }}</div>
                        <div class="preview-wall-stream__desc">{{ $t('实际区域以最终推流画面为准') }}</div>
                    </div>
                    <div class="preview-wall-bottom">
                        <div class="preview-wall-bottom-text">{{ $t('跟主播说点什么吧') }}</div>
                        <div class="preview-wall-bottom-actions">
                            <img src="@/assets/live/buyIcon.png" alt="购买" />
                            <img src="@/assets/live/shareIcon.png" alt="分享" />
                            <img src="@/assets/live/likeIcon.png" alt="点赞" />
                        </div>
                    </div>
                </div>
                <div>
                    <div class="settings-content">
                        <div class="setting-title" style="transform: translateX(12px)">
                            <span class="required-mark">*</span>
                            <span class="title-text">{{ $t('直播背景墙：') }}</span>
                        </div>
                        <div>
                            <div class="title-desc">{{ $t('直播间背景墙是每个直播间的默认背景。') }}</div>
                            <div class="setting-hint">{{ $t('建议尺寸：600像素 * 1300像素，图片大小不得超过 {imageUploadMaxSize}M。', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }}</div>
                            <el-form-item prop="backgroundWall">
                                <FormAddGallery
                                    v-if="!loading"
                                    v-model:photo="formState.backgroundWall"
                                    :disabled="act === 'detail'"
                                    :isMultiple="false"
                                    pageType="overseas"
                                    :dataType="LIVE_ROOM_IMAGE_TRANSLATION_DATA_TYPE"
                                />
                            </el-form-item>
                        </div>
                    </div>
                    <div class="function-tip">
                        <div class="function-tip__title">{{ $t('功能配置：') }}</div>
                        <div class="function-tip__content"> {{ $t('评论、商品货架、分享、点赞、回放和客服功能均默认开启。') }} <br /> {{ $t('如需修改，请前往微信公众平台-小程序管理-功能-直播进行操作。') }} <a class="function-tip__link" href="https://live.weixin.qq.com/" target="_blank" rel="noopener noreferrer">{{ $t('去设置') }}</a>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </el-form>
</template>

<script lang="ts" setup>
import { computed, ref } from "vue";
import type { CSSProperties } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { useConfigStore } from "@/store/config";
import { FormAddGallery } from "@/components/gallery";

/**
 * 直播间表单数据类型
 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const configStore = useConfigStore();
interface LiveRoomFormState {
    liveType?: number; // 直播类型：0-手机直播，1-推流设备直播
    shareCardCover?: string; // 分享卡片封面
    liveCardCover?: string; // 直播卡片封面
    backgroundWall?: string; // 直播间背景墙
}

const props = defineProps({
    act: {
        type: String,
        default: ""
    },
    loading: {
        type: Boolean,
        default: false
    },
    formState: {
        type: Object as () => LiveRoomFormState,
        default: () => ({})
    }
});

const LIVE_ROOM_IMAGE_TRANSLATION_DATA_TYPE = 13;
const isDeviceLive = computed(() => Number(props.formState?.liveType ?? 0) === 1);
const formRules: FormRules<LiveRoomFormState> = {
    shareCardCover: [{ required: true, message: t('请上传分享卡片样式'), trigger: "change" }],
    liveCardCover: [{ required: true, message: t('请上传直播卡片封面'), trigger: "change" }],
    backgroundWall: [{ required: true, message: t('请上传直播背景墙'), trigger: "change" }]
};

// 生成预览区域背景图样式
const getPreviewStyle = (url?: string): CSSProperties => {
    if (!url) {
        return {};
    }

    return {
        backgroundImage: `url(${url})`,
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
        backgroundSize: "cover"
    };
};

// 表单引用，用于验证
const formRef = ref<FormInstance>();

// 暴露 validate 方法供父组件调用
const validate = async () => {
    if (!formRef.value) return false;
    try {
        await formRef.value.validate();
        return true;
    } catch (_error) {
        return false;
    }
};

// 暴露方法
defineExpose({
    validate
});
</script>

<style lang="less" scoped>
.live-room-settings {
    display: flex;
    flex-direction: column;
    gap: 28px;

    :deep(.el-form-item) {
        margin: 0;
    }

    .settings-block {
        .section-title {
            position: relative;
            padding-left: 12px;
            margin-bottom: 16px;
            font-size: 16px;
            font-weight: 600;
            color: #303133;

            &::before {
                content: "";
                position: absolute;
                left: 0;
                top: 3px;
                width: 3px;
                height: 18px;
                border-radius: 2px;
                background: var(--tig-primary);
            }
        }
    }

    .settings-card {
        display: flex;
        align-items: flex-start;
        gap: 16px;
        padding: 4px 0;
    }

    .preview {
        flex: 0 0 auto;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f7fa;
        border-radius: 6px;
        overflow: hidden;
        position: relative;

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    }

    .preview-share {
        width: 300px;

        .preview-share-mask {
            position: absolute;
            top: 74px;
            left: 62px;
            width: 185px;
            height: 148px;
        }
    }

    .preview-live-card {
        width: 300px;

        .preview-live-card-mask {
            position: absolute;
            top: 7px;
            left: 7px;
            width: 152px;
            height: 141px;
            border-radius: 8px 0 0 8px;
        }
    }

    .preview-wall {
        width: 300px;

        .preview-wall-img {
            position: relative;
            z-index: 2;
            object-fit: contain;
        }

        .preview-wall-mask {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 1;
            background: linear-gradient(180deg, #7173a3 0%, #5a5d8a 100%);
        }

        .preview-wall-stream {
            position: absolute;
            left: 0;
            right: 0;
            top: 152px;
            height: 200px;
            z-index: 2;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background: #000;
            color: rgba(255, 255, 255, 0.72);
            text-align: center;
        }

        .preview-wall-stream__title {
            font-size: 14px;
            font-weight: 600;
            line-height: 22px;
        }

        .preview-wall-stream__desc {
            margin-top: 4px;
            font-size: 12px;
            line-height: 18px;
        }

        .preview-wall-bottom {
            position: absolute;
            left: 12px;
            right: 12px;
            bottom: 14px;
            z-index: 3;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .preview-wall-bottom-text {
            flex: 1;
            height: 34px;
            padding: 0 14px;
            display: flex;
            align-items: center;
            box-sizing: border-box;
            border-radius: 999px;
            background: rgba(14, 12, 49, 0.88);
            color: rgba(255, 255, 255, 0.9);
            font-size: 12px;
            line-height: 1;
            letter-spacing: 0.2px;
        }

        .preview-wall-bottom-actions {
            display: flex;
            align-items: center;
            gap: 8px;

            img {
                display: block;
                width: 34px;
                height: 34px;
                object-fit: contain;
                border-radius: 50%;
                flex: 0 0 34px;
            }
        }

        // &.preview-wall--device-live {
        //     .preview-wall-mask {
        //         // height: calc(100% - 175px);
        //         clip-path: polygon(0 0, 100% 0, 100% 44%, 0 44%, 0 100%, 100% 100%, 100% 56%, 0 56%);
        //     }
        // }
    }

    .settings-content {
        flex: 1;
        min-width: 320px;
        max-width: 520px;
        padding-top: 2px;
        display: flex;
        gap: 6px;
        align-items: flex-start;
    }

    .setting-title {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        margin-bottom: 8px;
        font-size: 14px;
        line-height: 22px;
        flex-shrink: 0;
        width: 107px;
        text-align: right;
    }

    .required-mark {
        margin-right: 2px;
        color: #f56c6c;
        font-size: 14px;
        font-weight: 600;
    }

    .title-text {
        color: rgba(0, 0, 0, 0.88);
        font-size: 14px;
        flex-shrink: 0;
    }

    .title-desc {
        color: #999;
        font-size: 12px;
        line-height: 22px;
    }

    .setting-hint {
        margin-bottom: 10px;
        color: #b3b3b3;
        font-size: 12px;
        line-height: 22px;
    }

    .function-tip {
        display: flex;
        align-items: flex-start;
        gap: 6px;
        margin-top: 14px;
        font-size: 14px;
        line-height: 24px;
        color: #666;
    }

    .function-tip__title {
        color: rgba(0, 0, 0, 0.88);
        white-space: nowrap;
        width: 107px;
        text-align: right;
        flex-shrink: 0;
    }

    .function-tip__content {
        color: #999;
        font-size: 12px;
    }

    .function-tip__link {
        cursor: pointer;
        white-space: nowrap;
    }
}
</style>
