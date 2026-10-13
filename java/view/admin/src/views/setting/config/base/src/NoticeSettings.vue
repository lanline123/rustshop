/**
 * @file 短信通知设置页
 * @description 配置短信服务和通知规则
 * @features
 * - 配置阿里云短信服务账号
 * - 设置短信签名和模板
 * - 配置各类通知短信开关
 * - 测试短信发送
 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="140px">
                <div class="content_wrapper">
                    <div class="title">{{ $t('短信设置') }}</div>
                    <el-form-item :label="$t('服务商')" required>
                        <div>
                            <el-radio-group v-model="value" class="itemWidth">
                                <el-radio :value="1">{{ $t('阿里云') }}</el-radio>
                            </el-radio-group>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('短信帐户用户名')" prop="smsKeyId">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.smsKeyId" />
                            <div class="extra">{{ $t('您所申请的短信服务用户名，不填则表示不开通') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('短信帐户密码')" prop="smsKeySecret">
                        <div>
                            <div class="flex flex-align-center" style="gap: 10px">
                                <div class="secret-txt line1" style="max-width: 380px">
                                    {{ formState.smsKeySecret }}
                                </div>
                                <DialogForm
                                    :maskClose="false"
                                    :isDrawer="false"
                                    :params="{ title: '短信帐户密码', content: formState.smsKeySecret }"
                                    path="setting/config/src/EditSecret"
                                    :title="$t('短信帐户密码')"
                                    width="600px"
                                    @okCallback="formState.smsKeySecret = $event"
                                >
                                    <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                </DialogForm>
                            </div>
                            <div class="extra">{{ $t('您所申请的短信服务密码，编辑完成后，点击下方提交按钮确认修改，请确保填写正确的短信帐户密码') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('短信签名')" prop="smsSignName">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.smsSignName" />
                            <div class="extra">{{ $t('阿里短信这里填短信签名，其它的根据具体短信接口填写') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('商家短信号码')" prop="smsShopMobile">
                        <div>
                            <TigInput classType="tig-form-input" type="integer" v-model="formState.smsShopMobile" />
                            <div class="extra">{{ $t('用于接收会员订单等信息') }}</div>
                        </div>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('邮件通知') }}</div>
                    <div class="notice-warp">
                        <p>{{ $t('提示：推荐使用QQ的域名邮箱（免费），设置流程请看本系统教程，另外请确保服务器防火墙未拦截邮箱发送，且对应的端口开放。') }}</p>
                    </div>
                    <el-form-item :label="$t('邮件服务')" prop="mailService">
                        <el-radio-group v-model="formState.mailService" class="itemWidth">
                            <el-radio :value="0">{{ $t('本地邮件服务器') }}</el-radio>
                            <el-radio :value="1">{{ $t('SMTP服务') }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t('如果您选择了采用服务器内置的 Mail 服务，您不需要填写下面的内容。') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('是否加密连接(SSL)')" prop="smtpSsl">
                        <el-radio-group v-model="formState.smtpSsl" class="itemWidth">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t('SMTP一般要求加密') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('发送邮件服务器地址(SMTP)')" prop="smtpHost">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.smtpHost" type="text" />
                            <div class="extra">{{ $t('邮件服务器主机地址。如果本机可以发送邮件则设置为localhost') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('服务器端口')" prop="smtpPort">
                        <TigInput classType="tig-form-input" TigInput type="integer" v-model="formState.smtpPort" />
                    </el-form-item>
                    <el-form-item :label="$t('邮件发送帐号')" prop="smtpUser">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.smtpUser" type="text" />
                            <div class="extra">{{ $t('发送邮件所需的认证帐号，如果没有就为空着') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('帐号密码')" prop="smtpPass">
                        <TigInput classType="tig-form-input" v-model="formState.smtpPass" type="password" />
                    </el-form-item>
                    <el-form-item :label="$t('邮件回复地址')" prop="smtpMail">
                        <TigInput classType="tig-form-input" v-model="formState.smtpMail" type="text" />
                    </el-form-item>
                    <el-form-item :label="$t('邮件编码')" prop="mailCharset">
                        <el-radio-group v-model="formState.mailCharset">
                            <el-radio label="UTF8">{{ $t('国际编码(UTF8)') }}</el-radio>
                            <el-radio label="GB2312">{{ $t('简体中文(GB2312)') }}</el-radio>
                            <el-radio label="BIG5">{{ $t('繁体中文(BIG5)') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('测试邮件地址')" prop="testMailAddress">
                        <TigInput classType="tig-form-input" v-model="formState.testMailAddress" type="text">
                            <template #append>
                                <el-button :loading="confirmLoading" @click="onSendTestEmail">{{ $t('发送测试邮件') }} </el-button>
                            </template>
                        </TigInput>
                    </el-form-item>
                    <el-form-item :label="$t('客服收件地址')" prop="serviceEmail">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.serviceEmail" />
                            <div class="extra">{{ $t('该客服邮件地址可用于接收下面所设置的通知') }}</div>
                        </div>
                    </el-form-item>
                    <!-- <el-form-item label="提交订单发送邮件" prop="sendConfirmEmail">
                        <el-radio-group v-model="formState.sendConfirmEmail" class="itemWidth">
                            <el-radio :value="1">是</el-radio>
                            <el-radio :value="0">否</el-radio>
                        </el-radio-group>
                        <div class="extra">订单提交后发送（未支付），建议选“否”</div>
                    </el-form-item>
                    <el-form-item label="订单支付发送邮件" prop="orderPayEmail">
                        <el-radio-group v-model="formState.orderPayEmail" class="itemWidth">
                            <el-radio :value="1">是</el-radio>
                            <el-radio :value="0">否</el-radio>
                        </el-radio-group>
                        <div class="extra">会员支付订单成功时，是否给会员发送邮件</div>
                    </el-form-item>
                    <el-form-item label="下单给客服发邮件" prop="sendServiceEmail">
                        <el-radio-group v-model="formState.sendServiceEmail" class="itemWidth">
                            <el-radio :value="1">是</el-radio>
                            <el-radio :value="0">否</el-radio>
                        </el-radio-group>
                        <div class="extra">提示：此处为发送给客服，商城信息中的客服邮件地址不为空时，该选项有效</div>
                    </el-form-item>
                    <el-form-item label="发货时发送邮件" prop="sendShipEmail">
                        <el-radio-group v-model="formState.sendShipEmail" class="itemWidth">
                            <el-radio :value="1">是</el-radio>
                            <el-radio :value="0">否</el-radio>
                        </el-radio-group>
                        <div class="extra">提示：该邮件是发给客户</div>
                    </el-form-item> -->
                </div>
            </el-form>
            <div style="height: 20px"></div>
        </a-spin>
    </div>
    <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
        <div class="selected-action">
            <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { onMounted, ref, shallowRef, nextTick, watch } from "vue";
import { message } from "ant-design-vue";
import { BaseNotice } from "@/types/setting/config";
import { getConfigNotify, saveConfigNotify } from "@/api/setting/config";
import { sendTestEmail } from "@/api/setting/mail";
import { useRoute } from "vue-router";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const route = useRoute();
const value = ref<number>(1);
const formRef = shallowRef();
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<Partial<BaseNotice>>({
    smsKeyId: "",
    smsKeySecret: "",
    smsSignName: "",
    smsShopMobile: "",
    serviceEmail: "",
    mailService: undefined,
    smtpSsl: undefined,
    mailCharset: undefined
    // sendConfirmEmail: undefined,
    // orderPayEmail: undefined,
    // sendServiceEmail: undefined,
    // sendShipEmail: undefined
});

// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getConfigNotify();
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
        const result = await saveConfigNotify(formState.value);
        message.success(t('修改成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};

const onSendTestEmail = async () => {
    confirmLoading.value = true;
    try {
        const result = await sendTestEmail({
            testMailAddress: formState.value.testMailAddress
        });
        message.success(t('操作成功'));
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

.notice-warp {
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}
</style>
