<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-tabs v-model="activeKey" class="lyecs-tabs" tab-position="top" @tab-change="onTabChange">
                    <template v-for="item in tabs">
                        <el-tab-pane v-if="(formState as any)[item.prop] > -1" :label="item.label" :name="item.label"></el-tab-pane>
                    </template>
                </el-tabs>
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <div v-if="activeKey === '消息'">
                        <el-form-item :label="$t('通知标题')" prop="templateName">
                            <TigInput classType="tig-form-input" v-model="formState.templateMessage.messageData.templateName" />
                        </el-form-item>
                        <el-form-item :label="$t('通知内容')" prop="content">
                            <div class="tznr">
                                <TigInput
                                    classType="tig-form-input"
                                    v-model="formState.templateMessage.messageData.content"
                                    :autosize="{ minRows: 9, maxRows: 60 }"
                                    class="fl1"
                                    type="textarea"
                                />
                                <div v-if="messageId !== 2" class="fl1 right-div">
                                    <p>{{ $t('请输入模板消息详细内容对应的变量，关键字个数需与添加的模板一致。可以使用如下变量：') }}</p>
                                    <template v-if="messageId == 3">
                                        <p>{{ $t('{orderSn}订单编号') }}</p>
                                        <p>{{ $t('{logisticsName}物流公司') }}</p>
                                        <p>{{ $t('{trackingNo}物流单号') }}</p>
                                    </template>
                                    <template v-else>
                                        <p>{{ $t('{orderSn}订单编号') }}</p>
                                    </template>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('是否开启')" prop="isMessage">
                            <el-radio-group v-model="formState.isMessage">
                                <el-radio :value="1">{{ $t('是') }}</el-radio>
                                <el-radio :value="0">{{ $t('否') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </div>
                    <div v-if="activeKey === '短信通知'">
                        <el-form-item :label="$t('当前短信服务商')">
                            <el-radio-group :model-value="normalizeSmsProvider(formState.smsProvider)" disabled>
                                <el-radio :value="1">{{ $t('阿里云') }}</el-radio>
                                <el-radio :value="2">{{ $t('腾讯云') }}</el-radio>
                                <el-radio :value="3">{{ $t('华为云') }}</el-radio>
                                <el-radio :value="4">{{ $t('火山引擎') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <el-form-item :label="$t('是否开启')" prop="isMsg">
                            <el-radio-group v-model="formState.isMsg">
                                <el-radio :value="1">{{ $t('是') }}</el-radio>
                                <el-radio :value="0">{{ $t('否') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <div class="channel-title">{{ $t('+86国内通道模版') }}</div>
                        <el-form-item :label="$t('短信模板ID')" prop="templateId">
                            <div class="w100">
                                <TigInput classType="tig-form-input" v-model="formState.templateMessage.msgData.templateId" />
                                <div class="field-tip">{{ $t('请填写当前短信服务商平台上的模板 ID，切换服务商后需手动更新') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('通知内容')" prop="content">
                            <div class="tznr">
                                <TigInput
                                    classType="tig-form-input"
                                    v-model="formState.templateMessage.msgData.content"
                                    :autosize="{ minRows: 9, maxRows: 60 }"
                                    class="fl1"
                                    disabled
                                    type="textarea"
                                />
                                <div class="fl1 right-div">
                                    <p>{{ $t('通知内容请在对应短信服务商控制台的模板中编辑') }}</p>
                                    <p>{{ $t('可使用如下变量：') }}</p>
                                    <template v-for="item in smsVariableDisplayList" :key="item">
                                        <p>{{ item }}</p>
                                    </template>
                                </div>
                            </div>
                        </el-form-item>
                    </div>
                    <div v-if="activeKey === '微信公众号'">
                        <el-form-item :label="$t('通知标题')" prop="templateName">
                            <TigInput classType="tig-form-input" v-model="formState.templateMessage.wechatData.templateName" disabled />
                        </el-form-item>
                        <el-form-item :label="$t('模板ID')" prop="templateId">
                            <TigInput classType="tig-form-input" v-model="formState.templateMessage.wechatData.templateId" disabled />
                        </el-form-item>
                        <el-form-item :label="$t('模板')" prop="content">
                            <TigInput
                                classType="tig-form-input"
                                v-model="formState.templateMessage.wechatData.content"
                                :autosize="{ minRows: 9, maxRows: 60 }"
                                class="fl1"
                                disabled
                                type="textarea"
                            />
                        </el-form-item>
                        <el-form-item :label="$t('是否开启')" prop="isWechat">
                            <el-radio-group v-model="formState.isWechat">
                                <el-radio :value="1">{{ $t('是') }}</el-radio>
                                <el-radio :value="0">{{ $t('否') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </div>
                    <div v-if="activeKey === '微信小程序'">
                        <el-form-item :label="$t('通知标题')" prop="templateName">
                            <TigInput classType="tig-form-input" v-model="formState.templateMessage.miniProgramData.templateName" disabled />
                        </el-form-item>
                        <el-form-item :label="$t('模板ID')" prop="templateId">
                            <TigInput classType="tig-form-input" v-model="formState.templateMessage.miniProgramData.templateId" disabled />
                        </el-form-item>
                        <el-form-item :label="$t('模板')" prop="content">
                            <TigInput
                                classType="tig-form-input"
                                v-model="formState.templateMessage.miniProgramData.content"
                                :autosize="{ minRows: 9, maxRows: 60 }"
                                class="fl1"
                                disabled
                                type="textarea"
                            />
                        </el-form-item>
                        <el-form-item :label="$t('是否开启')" prop="isMiniProgram">
                            <el-radio-group v-model="formState.isMiniProgram">
                                <el-radio :value="1">{{ $t('是') }}</el-radio>
                                <el-radio :value="0">{{ $t('否') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </div>
                    <div v-if="activeKey === 'APP'">
                        <el-form-item :label="$t('通知标题')" prop="templateName">
                            <TigInput classType="tig-form-input" v-model="formState.templateMessage.appData.templateName" />
                        </el-form-item>
                        <el-form-item :label="$t('通知内容')" prop="content">
                            <TigInput
                                classType="tig-form-input"
                                v-model="formState.templateMessage.appData.content"
                                :autosize="{ minRows: 9, maxRows: 60 }"
                                class="fl1"
                                type="textarea"
                            />
                        </el-form-item>
                        <el-form-item :label="$t('是否开启')" prop="isApp">
                            <el-radio-group v-model="formState.isApp">
                                <el-radio :value="1">{{ $t('是') }}</el-radio>
                                <el-radio :value="0">{{ $t('否') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </div>
                    <div v-if="activeKey === '邮件'">
                        <el-form-item :label="$t('通知标题')" prop="templateName">
                            <TigInput classType="tig-form-input" v-model="formState.templateMessage.emailData.templateName" />
                        </el-form-item>
                        <!-- <el-form-item label="模板" prop="content">
                            <TigInput
                                classType="tig-form-input"
                                v-model="formState.templateMessage.emailData.content"
                                :autosize="{ minRows: 9, maxRows: 60 }"
                                class="fl1"
                                disabled
                                type="textarea"
                            />
                        </el-form-item> -->
                        <el-form-item :label="$t('模板')" prop="content">
                            <div class="tznr">
                                <TigInput
                                    classType="tig-form-input"
                                    v-model="formState.templateMessage.emailData.content"
                                    :autosize="{ minRows: 9, maxRows: 60 }"
                                    class="fl1"
                                    type="textarea"
                                />
                                <div class="fl1 right-div">
                                    <p>{{ $t('请输入模板消息详细内容对应的变量，关键字个数需与添加的模板一致。可以使用如下变量：') }}</p>
                                    <template v-if="messageId == 11">
                                        <p>{{ $t('{merchantName}商家名称') }}</p>
                                        <p>{{ $t('{shopName}店铺名称') }}</p>
                                    </template>
                                    <template v-if="messageId == 10">
                                        <p>{{ $t('{merchantName}平台名称') }}</p>
                                        <p>{{ $t('{username}账号') }}</p>
                                        <p>{{ $t('{password}初始密码') }}</p>
                                    </template>
                                    <template v-if="messageId == 9">
                                        <p>{{ $t('{userNickName}用户名') }}</p>
                                        <p>{{ $t('{submitTime}提交时间') }}</p>
                                    </template>
                                    <template v-if="messageId == 8">
                                        <p>{{ $t('{code}验证码') }}</p>
                                    </template>
                                    <template v-if="messageId == 4">
                                        <p>{{ $t('{shopTitle}店铺名称') }}</p>
                                        <p>{{ $t('{userName}用户名称') }}</p>
                                        <p>{{ $t('{afterSaleSn}订单售后编码') }}</p>
                                        <p>{{ $t('{totalRefund}退款金额') }}</p>
                                        <p>{{ $t('{refundTime}退款时间') }}</p>
                                    </template>
                                    <template v-if="messageId == 3">
                                        <p>{{ $t('{shippingName}物流名称') }}</p>
                                        <p>{{ $t('{code}物流编号') }}</p>
                                    </template>
                                    <template v-if="messageId == 2">
                                        <p>{{ $t('{shopTitle}店铺名称') }}</p>
                                        <p>{{ $t('{userName}用户名') }}</p>
                                        <p>{{ $t('{productName}商品信息') }}</p>
                                    </template>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('是否开启')" prop="isMail">
                            <el-radio-group v-model="formState.isMail">
                                <el-radio :value="1">{{ $t('是') }}</el-radio>
                                <el-radio :value="0">{{ $t('否') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </div>
                    <div v-if="activeKey === '钉钉'">
                        <!-- <el-form-item label="选择钉钉成员" prop="templateName">
                            <TigInput classType="tig-form-input" v-model="formState.templateMessage.dingData.templateName" disabled />
                        </el-form-item> -->
                        <!-- <el-form-item label="通知内容" prop="content">
                            <TigInput
                                classType="tig-form-input"
                                v-model="formState.templateMessage.dingData.content"
                                :autosize="{ minRows: 9, maxRows: 60 }"
                                disabled
                                class="fl1"
                                type="textarea"
                            />
                        </el-form-item> -->
                        <el-form-item :label="$t('通知内容')" prop="content">
                            <div class="tznr">
                                <TigInput
                                    classType="tig-form-input"
                                    v-model="formState.templateMessage.dingData.content"
                                    :autosize="{ minRows: 9, maxRows: 60 }"
                                    class="fl1"
                                    type="textarea"
                                />
                                <div class="fl1 right-div">
                                    <p>{{ $t('请输入模板消息详细内容对应的变量，关键字个数需与添加的模板一致。可以使用如下变量：') }}</p>
                                    <p>{{ $t('{order} 订单号') }}</p>
                                    <p>{{ $t('{fee} 支付金额') }}</p>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('是否开启')" prop="isDing">
                            <el-radio-group v-model="formState.isDing">
                                <el-radio :value="1">{{ $t('是') }}</el-radio>
                                <el-radio :value="0">{{ $t('否') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </div>
                    <div v-if="activeKey === '企业微信'">
                        <!-- <el-form-item label="机器人地址" prop="templateName">
                            <TigInput classType="tig-form-input" v-model="formState.templateMessage.dingData.templateName" disabled />
                        </el-form-item>
                        <el-form-item label="通知内容" prop="content">
                            <TigInput
                                classType="tig-form-input"
                                v-model="formState.templateMessage.dingData.content"
                                :autosize="{ minRows: 9, maxRows: 60 }"
                                disabled
                                class="fl1"
                                type="textarea"
                            />
                        </el-form-item> -->
                        <el-form-item :label="$t('通知内容')" prop="content">
                            <div class="tznr">
                                <TigInput
                                    classType="tig-form-input"
                                    v-model="formState.templateMessage.wxWorkData.content"
                                    :autosize="{ minRows: 9, maxRows: 60 }"
                                    class="fl1"
                                    type="textarea"
                                />
                                <div class="fl1 right-div">
                                    <p>{{ $t('请输入模板消息详细内容对应的变量，关键字个数需与添加的模板一致。可以使用如下变量：') }}</p>
                                    <p>{{ $t('{order} 订单号') }}</p>
                                    <p>{{ $t('{fee} 支付金额') }}</p>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('是否开启')" prop="isWxWork">
                            <el-radio-group v-model="formState.isWxWork">
                                <el-radio :value="1">{{ $t('是') }}</el-radio>
                                <el-radio :value="0">{{ $t('否') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </div>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { MessageTypeFormState } from "@/types/setting/messageType.d";
import type { SmsProviderType } from "@/types/setting/config";
import { getMessageType, updateMessageType } from "@/api/setting/messageType";
import { isOverseas } from "@/utils/version";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const DEFAULT_SMS_PROVIDER: SmsProviderType = 1;

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const messageId = ref<number>(props.id);
const formState = ref<MessageTypeFormState>({
    smsProvider: DEFAULT_SMS_PROVIDER,
    templateMessage: {
        wechatData: {},
        appData: {},
        messageData: {},
        miniProgramData: {},
        msgData: {},
        emailData: {}, //邮件
        dingData: {}, // 钉钉
        wxWorkData: {} // 企业微信
    }
});

const SUPPORTED_SMS_PROVIDERS: readonly SmsProviderType[] = [1, 2, 3, 4];

const normalizeSmsProvider = (raw: unknown): SmsProviderType => {
    let provider: number;
    if (typeof raw === "number") {
        provider = raw;
    } else if (typeof raw === "string" && raw.trim() !== "") {
        provider = parseInt(raw, 10);
    } else {
        return DEFAULT_SMS_PROVIDER;
    }
    if (!Number.isInteger(provider)) return DEFAULT_SMS_PROVIDER;
    return (SUPPORTED_SMS_PROVIDERS as readonly number[]).includes(provider) ? (provider as SmsProviderType) : DEFAULT_SMS_PROVIDER;
};
const smsVariableMap: Record<number, Array<{ key: string; label: string }>> = {
    1: [{ key: "order_sn", label: t('订单号') }],
    2: [{ key: "order", label: t('订单号') }],
    3: [
        { key: "shippingName", label: t('物流名称') },
        { key: "code", label: t('物流编号') }
    ],
    4: [{ key: "order", label: t('订单号') }],
    5: [
        { key: "order", label: t('订单号') },
        { key: "fee", label: t('支付金额') }
    ],
    6: [
        { key: "order", label: t('订单号') },
        { key: "fee", label: t('支付金额') }
    ],
    8: [{ key: "code", label: t('验证码') }],
    9: [
        { key: "userNickName", label: t('用户名') },
        { key: "submitTime", label: t('提交时间') }
    ],
    10: [
        { key: "username", label: t('账号') },
        { key: "password", label: t('初始密码') }
    ],
    11: []
};
// 阿里云短信通道是否勾选了“其他海外通道”(2)
const overseaChannelEnabled = computed(() => {
    const channel = formState.value.smsAliyunChannel;
    if (!channel) return false;
    return channel
        .split(",")
        .map((item) => item.trim())
        .includes("2");
});
// 海外短信模板：仅跨境版 + 阿里云 + 已勾选“其他海外通道”时才展示；未勾选则隐藏整块
const showOverseaTemplate = computed(
    () => isOverseas() && normalizeSmsProvider(formState.value.smsProvider) === 1 && overseaChannelEnabled.value
);
const smsVariableDisplayList = computed(() => {
    const provider = normalizeSmsProvider(formState.value.smsProvider);
    const variables = smsVariableMap[messageId.value] ?? [];

    return variables.map((item, index) => {
        if (provider === 2) {
            return `{${index + 1}}${item.label}`;
        }
        if (provider === 1 || provider === 3) {
            return `\${${item.key}}${item.label}`;
        }
        return `{${item.key}}${item.label}`;
    });
});
const activeKey = ref<string>(t('消息'));
const onTabChange = (val: number) => {
    console.log(val);
};
let tabs = ref([
    { prop: "isMessage", label: t('消息') },
    { prop: "isMsg", label: t('短信通知') },
    { prop: "isWechat", label: t('微信公众号') },
    { prop: "isMiniProgram", label: t('微信小程序') },
    { prop: "isMail", label: t('邮件') },
    { prop: "isDing", label: t('钉钉') },
    { prop: "isWxWork", label: t('企业微信') }
]);
const fetchMessageType = async () => {
    try {
        const result = await getMessageType(action.value, { id: id.value });
        Object.assign(formState.value, result);
        formState.value.smsProvider = normalizeSmsProvider(result?.smsProvider);
        messageId.value = Number(result?.messageId || id.value);
        for (const item of tabs.value) {
            const propValue = (formState.value as any)[item.prop];
            if (typeof propValue === "number" && propValue > -1) {
                activeKey.value = item.label;
                break;
            }
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchMessageType();
    } else {
        loading.value = false;
    }
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateMessageType(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.w100 {
    width: 100%;
}
.field-tip {
    color: #999;
    font-size: 12px;
    line-height: 1.6;
    margin-top: 6px;
}
.channel-title {
    position: relative;
    padding-left: 10px;
    margin: 8px 0 16px;
    font-size: 14px;
    font-weight: 600;
    color: #333;
    line-height: 1.4;

    &::before {
        content: "";
        position: absolute;
        left: 0;
        top: 1px;
        bottom: 1px;
        width: 3px;
        border-radius: 2px;
        background-color: var(--el-color-primary, #f50);
    }
}
.tznr {
    display: flex;
    flex-direction: row;

    width: 100%;

    .fl1 {
        flex: 1;
    }

    .right-div {
        margin-left: 20px;
        background-color: #f2f2f2;
        padding: 10px;
        border-radius: 3px;
        color: #999999;
        line-height: 2;
    }
}
</style>
