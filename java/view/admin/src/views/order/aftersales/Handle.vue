/**
 * @file 售后处理表单
 * @description 处理退货退款/仅退款申请的表单组件
 * @features
 * - 支持同意退款和拒绝退款两种操作
 * - 退货退款需填写退货地址
 * - 支持退款金额校验（不能超过最大退款金额）
 * - 支持S2B2C模式下的多级审批流程
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <div class="negotiation-box">
                    <el-form ref="formRef" :model="formState" label-width="auto">
                        <el-form-item :label="$t('售后方式：')">
                            <p>{{ formData.aftersalesTypeName }}</p>
                        </el-form-item>
                        <el-form-item :label="$t('售后状态：')">
                            <p>{{ formData.statusName }}</p>
                        </el-form-item>
                        <template v-if="formData.vendorId && formData.vendorId > 0">
                            <el-form-item
                                :label="$t('退款金额：')"
                                prop="refundAmount"
                                v-if="status == 2 && formData.status == 1"
                                :rules="[
                                    { required: true, message: '退款金额不能为空' },
                                    { validator: validateRefundAmount, trigger: 'blur' }
                                ]"
                            >
                                <TigInput classType="tig-form-input" v-model="formState.refundAmount" type="decimal" :placeholder="$t('请输入退款金额')" />
                                <div class="extra">{{ $t('最大退款金额{priceFormat}', { priceFormat: priceFormat(formData.suggestRefundAmount) || "0" }) }}</div>
                            </el-form-item>
                            <el-form-item
                                :label="$t('退货地址：')"
                                prop="returnAddress"
                                v-if="formData.aftersaleType == 1 && status == 2 && formData.status == 21"
                                :rules="[{ required: status == 2 ? true : false, message: '退货地址不能为空' }]"
                            >
                                <TigInput classType="tig-form-input" v-model="formState.returnAddress" :rows="6" type="textarea" :placeholder="$t('请输入退货地址')" />
                            </el-form-item>
                        </template>
                        <template v-else>
                            <el-form-item
                                :label="$t('退款金额：')"
                                prop="refundAmount"
                                v-if="status == 2"
                                :rules="[
                                    { required: true, message: '退款金额不能为空' },
                                    { validator: validateRefundAmount, trigger: 'blur' }
                                ]"
                            >
                                <TigInput classType="tig-form-input" v-model="formState.refundAmount" type="decimal" :placeholder="$t('请输入退款金额')" />
                                <div class="extra">{{ $t('最大退款金额{priceFormat}', { priceFormat: priceFormat(formData.suggestRefundAmount) || "0" }) }}</div>
                            </el-form-item>
                            <el-form-item
                                :label="$t('退货地址：')"
                                prop="returnAddress"
                                v-if="formData.aftersaleType == 1 && status == 2"
                                :rules="[{ required: status == 2 ? true : false, message: '退货地址不能为空' }]"
                            >
                                <TigInput classType="tig-form-input" v-model="formState.returnAddress" :rows="6" type="textarea" :placeholder="$t('请输入退货地址')" />
                            </el-form-item>
                        </template>

                        <el-form-item
                            :label="$t('拒绝说明：')"
                            prop="reply"
                            v-if="status == 3"
                            :rules="[{ required: status == 3 ? true : false, message: '拒绝说明不能为空' }]"
                        >
                            <TigInput classType="tig-form-input" v-model="formState.reply" :rows="6" type="textarea" :placeholder="$t('请输入拒绝说明')" />
                            <div>
                                <el-tag v-for="item in formData.refuseReason" :key="item" type="info" style="margin-right: 5px" @click="onChangeReason">
                                    {{ item }}
                                </el-tag>
                            </div>
                        </el-form-item>
                        <el-form-item>
                            <div class="btn-box" v-if="!isS2b2c()">
                                <el-button type="primary" v-if="formData.aftersaleType == 1" @click="onSubmit">{{ status == 2 ? $t('同意并发送退货地址') : $t('拒绝退款') }}</el-button>
                                <el-button type="primary" v-if="formData.aftersaleType == 2" @click="onSubmit">{{ status == 2 ? $t('同意仅退款') : $t('拒绝仅退款') }}</el-button>
                                <el-button @click="close">{{ $t('取消') }}</el-button>
                            </div>
                        </el-form-item>
                    </el-form>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef, computed, PropType } from "vue";
import { message } from "ant-design-vue";
import { FormState } from "@/types/order/aftersales";
import { updateAftersales } from "@/api/order/aftersales";
import { getShopInfo } from "@/api/authority/accountEditing";
import { getConfigShopping } from "@/api/setting/config";
import { vendorSetting } from "@/api/vendor/setting";
import { priceFormat } from "@/utils/format";
import { isS2b2c } from "@/utils/version";

/** 当前管理员类型：platform-平台 / shop-商家 / vendor-供应商 */
const adminType = ref(localStorage.getItem("adminType"));
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    formData: {
        type: Object as PropType<FormState>,
        default: 0
    },
    status: {
        type: Number,
        default: 2
    }
});
const loading = ref<boolean>(true);
const formRef = shallowRef();
const formState = ref<any>({
    status: props.status,
    refundAmount: null,
    reply: "",
    returnAddress: props.formData.returnAddress || ""
});
const validateRefundAmount = (rule: any, value: any, callback: any) => {
    if (Number(value) > Number(props.formData.suggestRefundAmount)) {
        callback(new Error(t('退款金额不能大于最大退款金额')));
    } else {
        callback();
    }
};
const close = () => {
    emit("close");
};
const onChangeReason = (e: any) => {
    formState.value.reply = e.target.innerText;
};

/**
 * 是否展示退货地址字段
 * 退货退款(aftersaleType==1)且同意(status==2)时展示；供应商订单需在待供应商处理(status==21)节点
 */
const showReturnAddressField = computed(() => {
    if (Number(props.formData.aftersaleType) !== 1 || props.status !== 2) {
        return false;
    }
    if (Number(props.formData.vendorId) > 0) {
        return Number(props.formData.status) === 21;
    }
    return true;
});

/** 兼容后端 receiveInfo 为对象或 JSON 字符串两种形态 */
const parseReceiveInfo = (raw: any) => {
    if (!raw) {
        return null;
    }
    if (typeof raw === "string") {
        try {
            return JSON.parse(raw);
        } catch {
            return null;
        }
    }
    return raw;
};

/**
 * 预填退货地址
 * 从售后列表直接打开处理弹框时，行数据不含退货地址，此处按登录身份主动拉取
 * 店铺/供应商「配送设置 → 收货信息」的 receiveInfo 并拼接预填，商家仍可手动修改。
 * 已有值或不展示退货地址字段时跳过，拉取失败静默处理（不阻断同意流程）。
 */
const fillReturnAddress = async () => {
    if (!showReturnAddressField.value || formState.value.returnAddress) {
        return;
    }
    try {
        // 平台使用交易设置中的回寄信息；店铺/门店使用当前店铺的收货信息；供应商使用供应商收货信息。
        const detail: any =
            adminType.value === "admin" ? await getConfigShopping() : adminType.value === "vendor" ? await vendorSetting() : await getShopInfo();
        const configuredInfo = adminType.value === "admin" ? detail : parseReceiveInfo(detail?.receiveInfo);
        if (!configuredInfo) {
            return;
        }
        const consignee = configuredInfo.returnConsignee || configuredInfo.sender;
        const mobile = configuredInfo.returnMobile || configuredInfo.mobile;
        const region =
            configuredInfo.returnAddress ||
            [configuredInfo.provinceName, configuredInfo.cityName, configuredInfo.areaName, configuredInfo.address].filter(Boolean).join("");
        const parts = [
            consignee ? `收货人：${consignee}` : "",
            mobile ? `联系电话：${mobile}` : "",
            region ? `退货地址：${region}` : ""
        ].filter(Boolean);
        if (parts.length) {
            formState.value.returnAddress = parts.join("\n");
        }
    } catch {
        // 静默失败：商家仍可手动填写退货地址
    }
};

onMounted(fillReturnAddress);

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateAftersales({
            id: props.formData.aftersaleId,
            ...formState.value
        });
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
.negotiation-box {
    cursor: pointer;
}
.btn-box {
    display: flex;
    justify-content: flex-end;
    width: 100%;
}
</style>
