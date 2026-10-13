/**
 * @file 接口设置组件
 * @description 配置微信公众号接口参数（子组件）
 * @features
 * - 设置微信公众号APPID和AppSecret
 * - 配置微信服务器接口地址和Token
 * - 配置消息加解密密钥
 */
<template>
    <a-spin :spinning="loading">
        <el-form ref="formRef" :model="formState" label-width="145px" style="margin-left: 22px">
            <div class="title" id="weChatofficialaccount">{{ $t('微信公众号') }}</div>
            <!-- <el-form-item label="开启公众号授权登录" prop="wechatOauth">
                <el-radio-group v-model="formState.wechatOauth" class="itemWidth">
                    <el-radio :value="1">开启</el-radio>
                    <el-radio :value="0">关闭</el-radio>
                </el-radio-group>
            </el-form-item> -->
            <el-form-item :label="$t('微信APPID')" prop="wechatAppId">
                <TigInput width="100%" v-model="formState.wechatAppId" />
                <div class="extra">{{ $t('请从微信公众号后台获取，链接：https://mp.weixin.qq.com') }}</div>
            </el-form-item>
            <el-form-item :label="$t('微信AppSecret')" prop="wechatAppSecret">
                <TigInput width="100%" v-model="formState.wechatAppSecret" />
                <div class="extra">{{ $t('请从微信公众号后台获取') }}</div>
            </el-form-item>
            <el-form-item :label="$t('微信服务器接口地址')" prop="wechatServerUrl">
                <TigInput width="100%" v-model="formState.wechatServerUrl" readonly />
                <div class="extra">{{ $t('系统默认项目，配置公众号服务器地址时请带项目访问域名') }}</div>
            </el-form-item>
            <el-form-item :label="$t('微信服务器Token')" prop="wechatServerToken">
                <TigInput width="100%" v-model="formState.wechatServerToken" />
                <div class="extra">{{ $t('请从微信公众号后台获取') }}</div>
            </el-form-item>
            <el-form-item :label="$t('微信服务器消息加密密钥')" prop="wechatServerSecret">
                <TigInput width="100%" v-model="formState.wechatServerSecret" />
                <div class="extra">{{ $t('请从微信公众号后台获取') }}</div>
            </el-form-item>
        </el-form>
        <div style="height: 20px"></div>
        <div class="selected-action-warp selected-warp-left">
            <div class="selected-action" style="padding-left: 80px">
                <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
            </div>
        </div>
    </a-spin>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { BaseInterfactSettings } from "@/types/setting/config";
import { getConfigApi, saveConfigApi } from "@/api/setting/config";
import { useConfigStore } from "@/store/config";
import { convertNullsToEmptyStrings } from "@/utils/format"
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const configStore = useConfigStore();
const formRef = shallowRef();
const confirmLoading = ref<boolean>(false);
const formState = ref<BaseInterfactSettings>({
    wechatOauth: 0,
    wechatAppId: "",
    wechatAppSecret: "",
    wechatServerUrl: "",
    wechatServerToken: "",
    wechatServerSecret: "",
});

// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getConfigApi();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        const result = await saveConfigApi(formState.value);
        message.success(t('修改成功'));
        configStore.updateConfig();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>
.content {
    padding: 10px;

    .title {
        font-weight: bold;
        padding-top: 20px;
        padding-bottom: 20px;
        font-size: 14px;
    }

    .subtitle {
        color: #999;
        font-weight: normal;
        font-size: 12px;
    }
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
