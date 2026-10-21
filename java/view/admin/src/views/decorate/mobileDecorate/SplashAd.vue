<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="170px" style="display: flex; gap: 12px; flex-direction: column">
                <div class="content_wrapper">
                    <div class="title">{{ $t('开屏广告') }}</div>

                    <el-form-item :label="$t('开屏广告')" prop="state">
                        <div>
                            <div>
                                <el-radio-group v-model="formState.state" class="itemWidth">
                                    <el-radio :value="1">{{ $t('开启') }}</el-radio>
                                    <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                                </el-radio-group>
                            </div>
                            <div class="extra">{{ $t('该功能仅支持微信小程序和APP，开启进入商城首页时，出现一段品牌图片/视频，等待播放后进入。') }}</div>
                        </div>
                    </el-form-item>

                    <el-form-item :label="$t('添加素材')" prop="autoReturnGoods">
                        <div>
                            <div>
                                <el-radio-group v-model="formState.materialType" class="itemWidth" :disabled="formState.state == 0">
                                    <el-radio :value="0">{{ $t('图片') }}</el-radio>
                                    <el-radio :value="1">{{ $t('视频') }}</el-radio>
                                </el-radio-group>
                            </div>
                            <el-form-item label="" prop="materialImg" v-if="formState.materialType == 0" :rules="[{ required: true, message: '请上传图片' }]">
                                <FormAddGallery v-model:photo="formState.materialImg" :disabled="formState.state == 0" />
                            </el-form-item>
                            <el-form-item label="" prop="materialVideo" v-if="formState.materialType == 1" :rules="[{ required: true, message: '请上传视频' }]">
                                <FormAddGalleryVideo v-model:video="formState.materialVideo" :disabled="formState.state == 0"></FormAddGalleryVideo>
                            </el-form-item>
                            <div v-if="formState.materialType == 0" class="extra mt10"> {{ $t('建议尺寸1080*1920，格式jpg/png/gif, 单张图片文件大小限制{imageUploadMaxSize}M以内', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }} <el-popover :width="400" placement="right-end" trigger="click">
                                    <template #reference>
                                        <a>{{ $t('查看示例') }}</a>
                                    </template>
                                    <template #default>
                                        <img src="@/style/images/autoReturnGoods.png" style="width: 380px" />
                                    </template>
                                </el-popover>
                            </div>
                            <div v-if="formState.materialType == 1" class="extra mt10">{{ $t('格式mp4, 单个视频文件大小限制5M以内') }}</div>
                        </div>
                    </el-form-item>

                    <el-form-item
                        :label="$t('最大等待时长')"
                        prop="maxWaitTime"
                        :rules="[{ required: true, validator: (rule, value) => value && value > 0, message: '请输入大于0的整数' }]"
                    >
                        <div>
                            <div class="flex">
                                <p class="mr10">{{ $t('播放') }}</p>
                                <TigInput type="integer" v-model="formState.maxWaitTime" width="80px" :disabled="formState.state == 0"></TigInput>
                                <p class="ml10">{{ $t('秒后自动进入商城首页') }}</p>
                            </div>
                            <div class="extra">{{ $t('自定义等待时长生效，需更新小程序至3.97.1版本以上') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('跳转')" prop="redirectType">
                        <div>
                            <div>
                                <el-radio-group v-model="formState.redirectType" class="itemWidth" :disabled="formState.state == 0">
                                    <el-radio :value="0">{{ $t('不跳转') }}</el-radio>
                                    <el-radio :value="1">{{ $t('整体跳转') }}</el-radio>
                                </el-radio-group>
                            </div>
                            <el-form-item
                                v-if="formState.redirectType == 1"
                                label=""
                                prop="redirectUrl"
                                :rules="[{ required: true, validator: validateRedirectUrl }]"
                            >
                                <SelectLink v-model="formState.redirectUrl" decorateType="mobile" :disabled="formState.state == 0"></SelectLink>
                            </el-form-item>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 20px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>
``

<script lang="ts" setup>
import "@/style/css/list.less";
import { SelectLink } from "@/components/select";
import { FormAddGallery, FormAddGalleryVideo } from "@/components/gallery";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { getDecorateDiscrete, updateDecorateDiscrete, getMenberDecorateData } from "@/api/decorate/decorateDiscrete";
import { useConfigStore } from "@/store/config";
import { useRoute } from "vue-router";
import type { SplashAdFormState } from "@/types/decorate/splashAd.d";
import { useThemeStore } from "@/store/theme";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { resultProps } from "element-plus";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const route = useRoute();
const configStore = useConfigStore();
const formRef = shallowRef();
const confirmLoading = ref<boolean>(false);
const formState = ref<Partial<SplashAdFormState>>({
    state: undefined,
    materialType: undefined,
    materialImg: "",
    materialVideo: "",
    maxWaitTime: undefined,
    redirectType: undefined,
    redirectUrl: ""
});
// 加载列表
onMounted(async () => {
    await fetchDecorateDiscrete();
});
const loading = ref<boolean>(true);
const fetchDecorateDiscrete = async () => {
    try {
        const result = await getDecorateDiscrete({
            decorateSn: "openAdvertising"
        });
        if (result !== null) {
            Object.assign(formState.value, convertNullsToEmptyStrings(result.data));
        } else {
            formState.value = {
                state: 0
            };
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const validateRedirectUrl = (rule: any, value: any, callback: any) => {
    if (!value || JSON.stringify(value) == "{}") {
        callback(new Error(t('请选择跳转链接')));
        return;
    } else {
        callback();
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        confirmLoading.value = true;
        const result = await updateDecorateDiscrete({
            decorateSn: "openAdvertising",
            data: formState.value
        });
        message.success(t('提交成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>
.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}
.content_wrapper {
    min-height: calc(100vh - 280px);
}

.subtitle {
    color: #999;
    font-weight: normal;
    font-size: 12px;
}

.mr8 {
    margin-right: 8px;
}

.width60 {
    width: 60px;
}

.ml8 {
    margin-left: 8px;
}

.itemWidth {
    width: 100%;
}
.error {
    color: red;
}
</style>
