<template>
    <div class="bar-container">
        <div class="right-bar pc-shop-style">
            <div class="content">
                <div class="section-title">{{ $t('PC风格设置') }}</div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('默认样式') }}</div>
                    </div>
                    <div class="dec-edit-group-con" style="flex-direction: column; align-items: flex-start">
                        <el-checkbox v-model="module.pcDefaultStyle" true-value="1" false-value="0">{{ $t('开启PC店铺默认样式') }}</el-checkbox>
                        <div class="tips-text">
                            <div>
                                <div>{{ $t('开启后，PC店铺首页将采用系统内置样式') }}</div>
                                <div>{{ $t('若不勾选则使用移动端首页装修样式') }}</div>
                            </div>
                            <a class="btn-link" @click="previewPcShop">{{ $t('预览') }}</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="left-bar pc-shop-style">
            <div class="content">
                <div class="section-title">{{ $t('PC店铺顶部样式') }}</div>
                <div class="dec-edit-group">
                    <Bar v-model:module="module"></Bar>
                </div>
            </div>
        </div>
        <div class="right-bar">
            <div class="title">{{ $t('添加店铺头部图') }}</div>
            <div class="pic-change-desc">{{ $t('高度为110px（高清图为220px），宽度1500px（高清图为3000px）') }}</div>
            <ImageCard v-model:photo="module.image"></ImageCard>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('页面导航是否显示子级') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showSublevel">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="2">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('导航条背景颜色') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.backgroundColor = ''">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.backgroundColor"></SelectColor>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('导航条文字颜色') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.textColor = ''">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.textColor"></SelectColor>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('文字悬停背景色') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.hoverColor = ''">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.hoverColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
            <div class="selected-action">
                <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import "@/style/css/list.less";
import Bar from "@/views/merchant/setting/mobileShopDecorate/src/Bar.vue";
import { onMounted, ref, computed } from "vue";
import ImageCard from "@/views/merchant/setting/mobileShopDecorate/src/ImageCard.vue";
import { SelectColor } from "@/components/select";
import { defaultTitle } from "@/views/decorate/decorate/src/modules";
import { getDecorateDiscrete, updateDecorateDiscrete } from "@/api/decorate/decorateDiscrete";
import { message } from "ant-design-vue";
import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";
import { useThemeStore } from "@/store/theme";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const userStore = useUserStore();
const configStore = useConfigStore();
const { themeInfo } = useThemeStore();
const shopId = computed(() => localStorage.getItem("shopId") || userStore.userInfo.shopId);
const pcDomain = computed(() => configStore.config.pcDomain || "");

const module = ref({
    image: "",
    showSublevel: 1,
    backgroundColor: "",
    textColor: "",
    hoverColor: "",
    pcDefaultStyle: 0
});
const loading = ref(true);

onMounted(() => {
    // 获取详情数据
    fetchDecorateDiscrete();
});
const fetchDecorateDiscrete = async () => {
    try {
        const result = await getDecorateDiscrete({
            decorateSn: "shopHead"
        });
        if (result && result.data) {
            Object.assign(module.value, result.data);
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const confirmLoading = ref(false);
const onSubmit = async () => {
    try {
        confirmLoading.value = true;
        const result = await updateDecorateDiscrete({
            decorateSn: "shopHead",
            data: module.value
        });
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
const previewPcShop = () => {
    window.open(`${pcDomain.value}/shop/${shopId.value}`, "_blank");
};
</script>
<style scoped lang="less">
.bar-container {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .left-bar {
        flex: 1;
        border-radius: 2px;
    }

    .right-bar {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 10px;

        .title {
            margin-right: 16px;
            font-size: 14px;
            color: #323233;
            line-height: 18px;
            white-space: nowrap;
        }

        .pic-change-desc {
            font-size: 12px;
            line-height: 18px;
            color: #999;
        }

        .dec-edit-group {
            padding: 12px 16px;
            display: flex;
            flex-direction: row;
            justify-content: flex-start;

            .dec-edit-group-title {
                display: flex;
                align-items: flex-start;
                padding-top: 8px;

                .label {
                    margin-right: 16px;
                    font-size: 14px;
                    color: #969799;
                    line-height: 18px;
                    white-space: nowrap;
                }
            }

            .dec-edit-group-con {
                flex: 1;
                display: flex;
                justify-content: flex-end;
                align-items: center;

                .dec-color-button {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                }
            }
        }

        .sub {
            display: flex;
            align-items: center;
            justify-content: center;
        }
    }

    .pc-shop-style {
        margin-left: -20px;
        margin-right: -20px;
        background-color: #f5f7fa;
        padding-top: 15px;

        .content {
            padding: 15px 20px;
            background: #fff;
        }

        .section-title {
            font-size: 16px;
            font-weight: bold;
            color: #323233;
            margin-bottom: 15px;
        }

        .tips-text {
            font-size: 12px;
            color: #999;
            margin-top: 8px;
            line-height: 18px;
            display: flex;
            gap: 10px;
        }
    }
}
</style>
