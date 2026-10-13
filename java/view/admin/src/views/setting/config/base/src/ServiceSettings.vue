/**
 * @file 客服设置页
 * @description 配置商城客服系统
 * @features
 * - 选择客服类型（腾讯云/企业微信/自定义/TigChart）
 * - 配置各类型客服参数
 * - 设置客服工作时间
 * - 配置联系方式
 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" :rules="rules" label-width="140px" style="display: flex; gap: 12px; flex-direction: column">
                <div class="content_wrapper">
                    <div class="title">{{ $t('客服设置') }}</div>
                    <el-form-item :label="$t('客服选择')" prop="kefuType">
                        <el-radio-group v-model="formState.kefuType" class="itemWidth">
                            <el-radio-button :value="0">{{ $t('无客服') }}</el-radio-button>
                            <el-radio-button :value="1">{{ $t('腾讯云客服') }}</el-radio-button>
                            <el-radio-button :value="2">{{ $t('企业微信客服') }}</el-radio-button>
                            <el-radio-button :value="3">{{ $t('自定义客服') }}</el-radio-button>
                            <el-radio-button :value="4" v-if="isMerchant() || isPro()">{{ $t('TigChart客服') }}</el-radio-button>
                        </el-radio-group>
                        <div v-if="formState.kefuType == 1">
                            <div class="extra">{{ $t('腾讯云设置地址：https://yzf.qq.com/xv/html/admin/chat/channel/web') }}</div>
                            <div class="extra">{{ $t('设置流程：1、注册账号；2、会话->渠道设置->网站渠道，点击新增网站；3、在编辑里获取sign的值') }}</div>
                        </div>
                        <div v-if="formState.kefuType == 2">
                            <div class="extra">{{ $t('提示：PC端打开企业微信客服会唤起PC端微信，如果未安装PC端微信也可查看二维码后扫码使用;') }}</div>
                            <div class="extra">{{ $t('企业微信客服设置地址：https://work.weixin.qq.com/wework_admin/frame#/apps') }}</div>
                            <div class="extra">{{ $t('设置流程：1、注册账号；2、应用管理->应用->微信客服->客服帐号->创建账号；3、客服账号获取帐号ID') }}</div>
                        </div>
                        <div v-if="formState.kefuType == 3">
                            <div class="extra">{{ $t('提示：来回切换设置会丢失自定义客服的设置项，如需恢复请勿提交保存，刷新页面即可') }}</div>
                        </div>
                    </el-form-item>

                    <div v-if="formState.kefuType == 1">
                        <el-form-item :label="$t('腾讯云客服打开方式')" prop="kefuYzfType">
                            <el-radio-group v-model="formState.kefuYzfType">
                                <el-radio :value="0">{{ $t('打开链接地址') }}</el-radio>
                                <el-radio :value="1">{{ $t('小弹窗打开') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <el-form-item :label="$t('腾讯云客服sign')" prop="kefuYzfSign">
                            <!-- <div>
                                <TigInput classType="tig-form-input" v-model="formState.kefuYzfSign" />
                                <div class="extra">提示：sign为空时客服设置不生效！</div>
                            </div> -->
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.kefuYzfSign }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: '腾讯云客服sign', content: formState.kefuYzfSign }"
                                        path="setting/config/src/EditSecret"
                                        :title="$t('腾讯云客服sign')"
                                        width="600px"
                                        @okCallback="formState.kefuYzfSign = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('提示：sign为空时客服设置不生效！') }}</div>
                            </div>
                        </el-form-item>
                    </div>

                    <div v-if="formState.kefuType == 2">
                        <el-form-item :label="$t('企业微信客服ID')" prop="kefuWorkwxId">
                            <!-- <div>
                                <TigInput classType="tig-form-input" v-model="formState.kefuWorkwxId" />
                                <div class="extra">提示：ID为空时客服设置不生效！</div>
                            </div> -->
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.kefuWorkwxId }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: '企业微信客服ID', content: formState.kefuWorkwxId }"
                                        path="setting/config/src/EditSecret"
                                        :title="$t('企业微信客服ID')"
                                        width="600px"
                                        @okCallback="formState.kefuWorkwxId = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('提示：ID为空时客服设置不生效！') }}</div>
                            </div>
                        </el-form-item>
                    </div>
                    <div v-if="formState.kefuType == 2">
                        <el-form-item :label="$t('企业微信企业ID')" prop="corpId">
                            <!-- <div>
                                <TigInput classType="tig-form-input" v-model="formState.corpId" />
                                <div class="extra">提示：小程序打开微信客服需要设置企业ID！</div>
                            </div> -->
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.corpId }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: '企业微信企业ID', content: formState.corpId }"
                                        path="setting/config/src/EditSecret"
                                        :title="$t('企业微信企业ID')"
                                        width="600px"
                                        @okCallback="formState.corpId = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('提示：小程序打开微信客服需要设置企业ID！') }}</div>
                            </div>
                        </el-form-item>
                    </div>
                    <div v-if="formState.kefuType == 3">
                        <el-form-item :label="$t('客服链接')" prop="kefuCode">
                            <div>
                                <TigInput classType="tig-form-input" v-model="formState.kefuCode" />
                                <div class="extra">{{ $t('如果是脚本控制的链接，请在此处填入： 请填写http开头的客服链接;') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('客服链接打开方式')" prop="kefuCodeBlank">
                            <el-radio-group v-model="formState.kefuCodeBlank">
                                <el-radio :value="0">{{ $t('打开链接地址') }}</el-radio>
                                <el-radio :value="1">{{ $t('小弹窗打开') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </div>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('客服信息') }}</div>
                    <el-form-item :label="$t('客服热线电话')" prop="kefuPhone">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.kefuPhone" />
                            <div class="extra">{{ $t('不填则不显示') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('服务时间')" prop="kefuTime">
                        <TigInput classType="tig-form-input" v-model="formState.kefuTime" />
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 20px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }} </el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { BaseKefuConfig } from "@/types/setting/config";
import { getConfigKefu, saveConfigKefu } from "@/api/setting/config";
import { useConfigStore } from "@/store/config";
import { isMerchant, isPro, isOverseas } from "@/utils/version";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useRoute } from "vue-router";
import { useThemeStore } from "@/store/theme";
import { DialogForm } from "@/components/dialog";
import type { FormRules } from "element-plus";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const route = useRoute();
const configStore = useConfigStore();
const formRef = shallowRef();
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<Partial<BaseKefuConfig>>({
    kefuType: undefined,
    kefuYzfType: undefined,
    kefuYzfSign: "",
    kefuWorkwxId: "",
    kefuCode: "",
    kefuCodeBlank: undefined,
    corpId: "",
    kefuWhatsappPhone: "",
    kefuPhone: "",
    kefuTime: ""
});
const rules: FormRules = {
};
// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getConfigKefu();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    try {
        await formRef.value.validate();
    } catch {
        return;
    }
    confirmLoading.value = true;
    try {
        const result = await saveConfigKefu(formState.value);
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
    padding: 0 20px;
}

.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
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
