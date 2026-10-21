<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="140px">
                    <el-form-item :label="$t('打印机类型')" prop="platform" :rules="[{ required: true, message: '请选择打印机类型!' }]">
                        <el-radio-group v-model="formState.platform" @change="onPlatformChange">
                            <el-radio v-for="item in visiblePlatformOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('绑定设备')" prop="deviceCode">
                        <el-select v-model="formState.deviceCode" class="tig-form-select" clearable filterable :placeholder="$t('请选择设备')">
                            <el-option
                                v-for="device in cashierDevices"
                                :key="device.id"
                                :value="device.deviceCode"
                                :label="device.deviceName ? device.deviceName + '（' + device.deviceCode + '）' : device.deviceCode"
                            />
                        </el-select>
                    </el-form-item>
                    <el-form-item :rules="[{ required: true, message: '名称不能为空!' }]" :label="$t('名称')" prop="printName">
                        <TigInput classType="tig-form-input" v-model="formState.printName" :placeholder="$t('请输入打印机名称')" />
                    </el-form-item>
                    <template v-if="formState.platform === 1">
                        <el-form-item label="USER" prop="thirdAccount" :rules="[{ required: true, message: 'USER不能为空!' }]">
                            <TigInput classType="tig-form-input" v-model="formState.thirdAccount" />
                            <div class="extra">{{ $t("飞鹅云后台账号") }}</div>
                        </el-form-item>
                        <el-form-item label="UYEK" prop="thirdKey" :rules="[{ required: true, message: 'UYEK不能为空!' }]">
                            <!-- <TigInput classType="tig-form-input" v-model="formState.thirdKey" /> -->
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.thirdKey }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'UYEK', content: formState.thirdKey }"
                                        path="setting/config/src/EditSecret"
                                        title="UYEK"
                                        width="600px"
                                        @okCallback="formState.thirdKey = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t("编辑") }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">
                                    {{ $t("飞鹅云后台生成的UYEK，") }}
                                    <el-popover :width="400" placement="right-end" trigger="hover">
                                        <template #reference>
                                            <a>{{ $t("查看示例") }}</a>
                                        </template>
                                        <template #default>
                                            <img src="https://oss.tigshop.com/img/gallery/202507/1753684560T8OSH0XHLs8kXZKODe.jpeg" style="width: 380px" />
                                        </template>
                                    </el-popover>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('打印机SN')" prop="printSn" :rules="[{ required: true, message: '打印机SN不能为空!' }]">
                            <!-- <TigInput classType="tig-form-input" v-model="formState.printSn" /> -->
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.printSn }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: '打印机SN', content: formState.printSn }"
                                        path="setting/config/src/EditSecret"
                                        :title="$t('打印机SN')"
                                        width="600px"
                                        @okCallback="formState.printSn = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t("编辑") }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">
                                    {{ $t("飞鹅云打印机标签上SN，") }}
                                    <el-popover :width="400" placement="right-end" trigger="hover">
                                        <template #reference>
                                            <a>{{ $t("查看示例") }}</a>
                                        </template>
                                        <template #default>
                                            <img src="https://oss.tigshop.com/img/gallery/202507/1753768520lp0CnlW3J9YOgwBS1P.jpg" style="width: 380px" />
                                        </template>
                                    </el-popover>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('打印机KEY')" prop="printKey" :rules="[{ required: true, message: '打印机KEY不能为空!' }]">
                            <!-- <TigInput classType="tig-form-input" v-model="formState.printKey" /> -->
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.printKey }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: '打印机KEY', content: formState.printKey }"
                                        path="setting/config/src/EditSecret"
                                        :title="$t('打印机KEY')"
                                        width="600px"
                                        @okCallback="formState.printKey = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t("编辑") }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">
                                    {{ $t("飞鹅云打印机标签上KEY，") }}
                                    <el-popover :width="400" placement="right-end" trigger="hover">
                                        <template #reference>
                                            <a>{{ $t("查看示例") }}</a>
                                        </template>
                                        <template #default>
                                            <img src="https://oss.tigshop.com/img/gallery/202507/1753768520XcpUlaVT9k5Gdp5SD5.jpg" style="width: 380px" />
                                        </template>
                                    </el-popover>
                                </div>
                            </div>
                        </el-form-item>
                    </template>
                    <template v-if="formState.platform === 2">
                        <el-form-item label="AppID" prop="thirdAccount" :rules="[{ required: true, message: 'AppID不能为空!' }]">
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.thirdAccount }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'AppID', content: formState.thirdAccount }"
                                        path="setting/config/src/EditSecret"
                                        title="AppID"
                                        width="600px"
                                        @okCallback="formState.thirdAccount = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t("编辑") }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">
                                    {{ $t("商米开放平台应用唯一标识，登录商米合作伙伴平台，申请「云打印」权限后，系统自动分配") }}
                                    <el-popover :width="400" placement="right-end" trigger="hover">
                                        <template #reference>
                                            <a>{{ $t("查看示例") }}</a>
                                        </template>
                                        <template #default>
                                            <img src="https://oss.tigshop.com/img/gallery/202604/1776928610VA1fKezK8BQvVlzt3P.jpeg" style="width: 380px" />
                                        </template>
                                    </el-popover>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item label="AppSecret" prop="thirdKey" :rules="[{ required: true, message: 'AppSecret不能为空!' }]">
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.thirdKey }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'AppSecret', content: formState.thirdKey }"
                                        path="setting/config/src/EditSecret"
                                        title="AppSecret"
                                        width="600px"
                                        @okCallback="formState.thirdKey = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t("编辑") }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">
                                    {{ $t("接口签名的核心密钥（对应开放平台 AppKey），保存时随请求提交至后端用于商米鉴权。") }}
                                    <el-popover :width="400" placement="right-end" trigger="hover">
                                        <template #reference>
                                            <a>{{ $t("查看示例") }}</a>
                                        </template>
                                        <template #default>
                                            <img src="https://oss.tigshop.com/img/gallery/202604/1776928610VA1fKezK8BQvVlzt3P.jpeg" style="width: 380px" />
                                        </template>
                                    </el-popover>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('打印机 SN')" prop="printSn" :rules="[{ required: true, message: '打印机SN不能为空!' }]">
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.printSn }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: '打印机 SN', content: formState.printSn }"
                                        path="setting/config/src/EditSecret"
                                        :title="$t('打印机 SN')"
                                        width="600px"
                                        @okCallback="formState.printSn = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t("编辑") }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">
                                    {{ $t("商米云打印机设备序列号（如 NT21x/NT31x/NT32x），需已在商米渠道下绑定后方可保存成功。") }}
                                    <el-popover :width="400" placement="right-end" trigger="hover">
                                        <template #reference>
                                            <a>{{ $t("查看示例") }}</a>
                                        </template>
                                        <template #default>
                                            <img src="https://oss.tigshop.com/img/gallery/202604/1776928610IUZf9ANywyCicygz1o.jpeg" style="width: 380px" />
                                        </template>
                                    </el-popover>
                                </div>
                            </div>
                        </el-form-item>
                    </template>
                    <el-form-item :label="$t('订单支付自动打印')" prop="">
                        <el-radio-group v-model="formState.autoPrint">
                            <el-radio :value="1">{{ $t("开启") }}</el-radio>
                            <el-radio :value="2">{{ $t("关闭") }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('语音播报')" prop="voiceEnabled">
                        <div>
                            <el-radio-group v-model="formState.voiceEnabled">
                                <el-radio :value="1">{{ $t("开启") }}</el-radio>
                                <el-radio :value="2">{{ $t("关闭") }}</el-radio>
                            </el-radio-group>
                            <div v-if="formState.platform === 1" class="extra">{{ $t("语音播报功能生效，需配合自动打印一同开启。") }}</div>
                            <div v-else-if="formState.platform === 2" class="extra">{{ $t("开启商米云语音自动播报，实时播报新订单语音提醒。") }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item v-if="formState.voiceEnabled === 1" :label="$t('播报内容')" prop="voiceContentType">
                        <el-radio-group v-if="formState.platform === 2" v-model="formState.voiceContentType">
                            <el-radio :value="0">{{ $t("订单编号+订单金额") }}</el-radio>
                            <el-radio :value="1">{{ $t("仅播报订单金额") }}</el-radio>
                        </el-radio-group>
                        <el-radio-group v-else-if="formState.platform === 1" :model-value="0" disabled>
                            <el-radio :value="0">{{ $t("您有一个新的订单") }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('打印机状态')" prop="">
                        <el-radio-group v-model="formState.status">
                            <el-radio :value="1">{{ $t("启用") }}</el-radio>
                            <el-radio :value="2">{{ $t("禁用") }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <!-- <el-form-item label="打印联数" prop="printNumber">
                        <TigInput classType="tig-form-input" v-model="formState.printNumber" />
                        <div class="extra">打印机单次打印张数</div>
                    </el-form-item> -->
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
import { getPrintDetail, printCreateAndUpdate } from "@/api/setting/receiptPrint";
import { getCashierDeviceList } from "@/api/setting/cashier";
import type { PrintCreateParams } from "@/types/setting/receiptPrint";
import { DialogForm } from "@/components/dialog";

// 父组件回调
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

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
const cashierDevices = ref<any[]>([]);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<Omit<PrintCreateParams, "printId">>({
    platform: 1,
    printName: "",
    printNumber: 1,
    autoPrint: 2,
    voiceEnabled: 2,
    voiceContentType: 0,
    status: 2,
    thirdAccount: "",
    thirdKey: "",
    printSn: "",
    printKey: "",
    deviceCode: ""
} as PrintCreateParams);
const platformOptions = [
    { label: t("飞鹅云打印"), value: 1 },
    { label: t("商米云打印"), value: 2 }
];
const visiblePlatformOptions = computed(() => {
    if (operation === "create") {
        return platformOptions;
    }
    return platformOptions.filter((item) => item.value === formState.value.platform);
});
const fetchGetPrintDetail = async () => {
    try {
        const result = await getPrintDetail({ id: id.value });
        let { addTime, platformText, shopId, statusText, updateTime, printNumber, ...form } = result;
        Object.assign(formState.value, form);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

const onPlatformChange = () => {
    formState.value.thirdAccount = "";
    formState.value.thirdKey = "";
    formState.value.printSn = "";
    formState.value.printKey = "";
    formState.value.voiceContentType = 0;
};

const fetchCashierDevices = async () => {
    try {
        const result: any = await getCashierDeviceList({ page: 1, size: 200, status: 1 });
        cashierDevices.value = result?.records || [];
    } catch {
        cashierDevices.value = [];
    }
};

onMounted(() => {
    fetchCashierDevices();
    if (action.value === "detail") {
        // 获取详情数据
        fetchGetPrintDetail();
    } else {
        loading.value = false;
    }
});

// 表单通过验证后提交
const buildSubmitPayload = (): PrintCreateParams => {
    const { printName, printNumber, autoPrint, voiceEnabled, status, deviceCode } = formState.value;
    const platform = formState.value.platform ?? 1;
    const base: PrintCreateParams = {
        printId: operation === "create" ? undefined : id.value,
        platform,
        printName,
        printNumber,
        autoPrint,
        voiceEnabled,
        status,
        deviceCode: deviceCode || ""
    };
    if (platform === 1) {
        return {
            ...base,
            voiceContentType: 0,
            thirdAccount: formState.value.thirdAccount,
            thirdKey: formState.value.thirdKey,
            printSn: formState.value.printSn,
            printKey: formState.value.printKey
        };
    }
    return {
        ...base,
        platform: 2,
        voiceContentType: formState.value.voiceContentType ?? 0,
        thirdAccount: formState.value.thirdAccount,
        thirdKey: formState.value.thirdKey,
        printSn: formState.value.printSn,
        printKey: ""
    };
};

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await printCreateAndUpdate(operation, buildSubmitPayload());
        emit("submitCallback", result);
        message.success(t("操作成功"));
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
<style scoped lang="less">
.tig-form-select {
    width: 450px;
}
@media (max-width: 768px) {
    .tig-form-select {
        width: 80%;
    }
}
</style>
