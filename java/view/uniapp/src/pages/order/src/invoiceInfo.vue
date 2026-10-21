<template>
    <view class="invoice-info">
        <view class="invoice">
            <view class="invoice-title">{{ $t("发票") }}</view>
            <view class="invoice-content" @click="handleInvoice">
                <view class="invoice-text">{{ $t(typeCodeText || "") }}</view>
                <image lazy-load class="more-ico" :src="staticResource('common/more.png')" />
            </view>
        </view>
        <tig-popup v-model:show="show">
            <scroll-view scroll-y="true" class="invoice-popup-content">
                <view class="invoice-popup-box">
                    <view class="popup-title title">{{ $t("发票") }}</view>
                    <view class="invoice-typemenu">
                        <view class="invoice-typemenu-item" :class="{ active: formState.invoiceType === 1 }" @click="handleInvoiceType(1)">{{
                            $t("普通发票")
                        }}</view>
                        <view
                            v-if="configStore.invoiceAdded == 1"
                            class="invoice-typemenu-item"
                            :class="{ active: formState.invoiceType === 2 }"
                            @click="handleInvoiceType(2)"
                            >{{ $t("增值税专用发票") }}</view
                        >
                        <view class="invoice-explain">
                            我公司依法开具发票，请您按照税法规定使用发票。根据现行税收政策，部分公司提供数电票,
                            数电（专用发票）法律效力与现有增值税专用发票相同;如商品由第三方卖家销售，发票类型和内容将由该卖家决定，发票由卖家开具并提供。如您为企业采购，需要多单合并开具或批量提交发票可注册企业用户；<text
                                class="special-text"
                                >{{ $t("使用礼品卡支付部分的金额，不支持开发票") }}</text
                            >
                        </view>
                    </view>

                    <view class="invoice-formState">
                        <uni-forms ref="formRef" :model-value="formState" :rules="rules" label-width="170rpx">
                            <block v-if="formState.invoiceType === 1">
                                <uni-forms-item :label="$t('发票抬头')" name="name">
                                    <view class="item-layout" @change="radioChange">
                                        <up-radio-group v-model="formState.titleType" placement="row">
                                            <up-radio :custom-style="{ marginRight: '22rpx' }" :name="1" active-color="var(--general)" :label="$t('个人')" />
                                            <up-radio :name="2" active-color="var(--general)" :label="$t('企业')" />
                                        </up-radio-group>
                                    </view>
                                </uni-forms-item>
                                <block v-if="formState.titleType == 1">
                                    <uni-forms-item
                                        :label="$t('个人名称')"
                                        name="companyName"
                                        :rules="[{ required: true, errorMessage: $t('个人名称不能为空') }]"
                                    >
                                        <uni-easyinput
                                            v-model="formState.companyName"
                                            primary-color="rgb(192, 196, 204)"
                                            :input-border="false"
                                            :placeholder="$t('请输入个人名称')"
                                        />
                                    </uni-forms-item>
                                </block>
                                <block v-else>
                                    <uni-forms-item
                                        :label="$t('企业名称')"
                                        name="companyName"
                                        :rules="[{ required: true, errorMessage: $t('企业名称不能为空') }]"
                                    >
                                        <uni-easyinput
                                            v-model="formState.companyName"
                                            primary-color="rgb(192, 196, 204)"
                                            :input-border="false"
                                            :placeholder="$t('请输入企业名称')"
                                        />
                                    </uni-forms-item>
                                    <uni-forms-item :label="$t('纳税人识号')" name="companyCode">
                                        <uni-easyinput
                                            v-model="formState.companyCode"
                                            primary-color="rgb(192, 196, 204)"
                                            :input-border="false"
                                            :placeholder="$t('请输入纳税人识号')"
                                        />
                                    </uni-forms-item>
                                    <uni-forms-item :label="$t('单位地址')" name="companyAddress">
                                        <uni-easyinput
                                            v-model="formState.companyAddress"
                                            primary-color="rgb(192, 196, 204)"
                                            :input-border="false"
                                            :placeholder="$t('请输入单位地址')"
                                        />
                                    </uni-forms-item>
                                    <uni-forms-item :label="$t('单位电话')" name="companyPhone">
                                        <uni-easyinput
                                            v-model="formState.companyPhone"
                                            primary-color="rgb(192, 196, 204)"
                                            :input-border="false"
                                            :placeholder="$t('请输入单位电话')"
                                        />
                                    </uni-forms-item>
                                    <uni-forms-item :label="$t('开户银行')" name="companyBank">
                                        <uni-easyinput
                                            v-model="formState.companyBank"
                                            primary-color="rgb(192, 196, 204)"
                                            :input-border="false"
                                            :placeholder="$t('请输入开户银行')"
                                        />
                                    </uni-forms-item>
                                    <uni-forms-item :label="$t('银行账户')" name="companyAccount">
                                        <uni-easyinput
                                            v-model="formState.companyAccount"
                                            primary-color="rgb(192, 196, 204)"
                                            :input-border="false"
                                            :placeholder="$t('请输入银行账户')"
                                        />
                                    </uni-forms-item>
                                </block>
                                <uni-forms-item :label="$t('收票人手机')" name="mobile">
                                    <uni-easyinput
                                        v-model="formState.mobile"
                                        primary-color="rgb(192, 196, 204)"
                                        :input-border="false"
                                        :placeholder="$t('请输入收票人手机')"
                                    />
                                </uni-forms-item>
                                <uni-forms-item :label="$t('收票人邮箱')" name="email">
                                    <uni-easyinput
                                        v-model="formState.email"
                                        primary-color="rgb(192, 196, 204)"
                                        :input-border="false"
                                        :placeholder="$t('请输入收票人邮箱')"
                                    />
                                </uni-forms-item>
                            </block>
                            <block v-else-if="formState.invoiceType === 2 && invoiceStatus">
                                <view class="invoiceInfo-content-item">
                                    <view class="item-label">{{ $t("单位名称") }}</view>
                                    <view class="item-value">{{ formState.companyName }}</view>
                                </view>
                                <view class="invoiceInfo-content-item">
                                    <view class="item-label">{{ $t("纳税人识号") }}</view>
                                    <view class="item-value">{{ formState.companyCode }}</view>
                                </view>
                                <view class="invoiceInfo-content-item">
                                    <view class="item-label">{{ $t("单位地址") }}</view>
                                    <view class="item-value">{{ formState.companyAddress }}</view>
                                </view>
                                <view class="invoiceInfo-content-item">
                                    <view class="item-label">{{ $t("单位电话") }}</view>
                                    <view class="item-value">{{ formState.companyPhone }}</view>
                                </view>
                                <view class="invoiceInfo-content-item">
                                    <view class="item-label">{{ $t("开户银行") }}</view>
                                    <view class="item-value">{{ formState.companyBank }}</view>
                                </view>
                                <view class="invoiceInfo-content-item">
                                    <view class="item-label">{{ $t("银行账户") }}</view>
                                    <view class="item-value">{{ formState.companyAccount }}</view>
                                </view>
                                <uni-forms-item :label="$t('收票人手机')" name="mobile">
                                    <uni-easyinput
                                        v-model="formState.mobile"
                                        primary-color="rgb(192, 196, 204)"
                                        :input-border="false"
                                        :placeholder="$t('请输入收票人手机')"
                                    />
                                </uni-forms-item>
                                <uni-forms-item :label="$t('收票人邮箱')" name="email">
                                    <uni-easyinput
                                        v-model="formState.email"
                                        primary-color="rgb(192, 196, 204)"
                                        :input-border="false"
                                        :placeholder="$t('请输入收票人邮箱')"
                                    />
                                </uni-forms-item>
                            </block>
                            <block v-else>
                                <view class="notPass">
                                    <view
                                        >{{ $t("您还未通过增票资质申请，暂时无法开具增值税专用发票") }}
                                        <text class="notPassBtn" @click="handleApply">{{ $t("去申请") }}</text></view
                                    >
                                </view>
                            </block>
                        </uni-forms>
                    </view>
                    <tig-fixed-placeholder background-color="#fff">
                        <view class="btn-box">
                            <tig-button class="btn" plain :disabled="formState.invoiceType === 2 && !invoiceStatus" @click="onCancel">
                                {{ $t("暂不开票") }}</tig-button
                            >
                            <tig-button class="btn" :disabled="formState.invoiceType === 2 && !invoiceStatus" @click="onSubmit"> {{ $t("提交") }}</tig-button>
                        </view>
                    </tig-fixed-placeholder>
                </view>
            </scroll-view>
        </tig-popup>
    </view>
</template>

<script setup lang="ts">
/**
 * 发票信息组件
 * - 支持普通发票（个人/企业）和增值税专用发票
 * - 弹窗填写发票抬头、纳税人识别号等开票信息
 * - 增值税专用发票需先申请资质审核
 */
import { computed, reactive, ref, watch } from "vue";
import { getInvoiceStatus, getCheckInvoice } from "@/api/order/invoice";
import { staticResource } from "@/utils";
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n";
const { t } = useI18n();

const configStore = useConfigStore();
const props = defineProps({
    getAddressInfo: {
        type: Object,
        default: () => {}
    },
    invoiceInfo: {
        type: Object,
        default: () => {}
    }
});
const emit = defineEmits(["update:invoiceInfo", "change"]);
const typeCodeText = computed(() => {
    switch (props.invoiceInfo.invoiceType) {
        case 1:
            return t("普通发票");
        case 2:
            return t("增值税专用发票");
        default:
            return t("不开发票");
    }
});

const rules: any = {
    companyCode: {
        rules: [{ required: true, errorMessage: t("纳税人识号不能为空!") }]
    },
    companyAddress: {
        rules: [{ required: true, errorMessage: t("单位地址不能为空!") }]
    },
    companyPhone: {
        rules: [{ required: true, errorMessage: t("单位电话不能为空!") }]
    },
    companyBank: {
        rules: [{ required: true, errorMessage: t("开户银行不能为空!") }]
    },
    companyAccount: {
        rules: [{ required: true, errorMessage: t("银行账户不能为空!") }]
    },
    mobile: {
        rules: [{ required: true, errorMessage: t("手机不能为空!") }]
    }
};

const radioChange = () => {
    clearFormState();
    __getCheckInvoice();
};

const formState = reactive({
    titleType: 1, // 抬头类型
    invoiceType: 1, // 发票类型
    companyName: "",
    mobile: "",
    email: "",
    companyCode: "",
    companyAddress: "",
    companyPhone: "",
    companyBank: "",
    companyAccount: ""
});

const clearFormState = () => {
    formState.companyName = "";
    formState.mobile = "";
    formState.email = "";
    formState.companyCode = "";
    formState.companyAddress = "";
    formState.companyPhone = "";
    formState.companyBank = "";
    formState.companyAccount = "";
};

const invoiceStatus = ref(false);
const show = ref(false);
const __getInvoiceStatus = async () => {
    try {
        const result = await getInvoiceStatus();
        if (result && Object.keys(result).length > 0) {
            invoiceStatus.value = true;

            if (formState.invoiceType === 2) {
                formState.companyCode = result.companyCode;
                formState.companyAddress = result.companyAddress;
                formState.companyPhone = result.companyPhone;
                formState.companyBank = result.companyBank;
                formState.companyAccount = result.companyAccount;
                formState.companyName = result.companyName;
            }
        } else {
            invoiceStatus.value = false;
        }
    } catch (error) {
        console.error(error);
    }
};

const __getCheckInvoice = async () => {
    try {
        const result = await getCheckInvoice({
            invoiceType: formState.invoiceType,
            titleType: formState.titleType
        });
        if (result) {
            if (formState.invoiceType === 1 && formState.titleType === 1) {
                formState.companyName = result.companyName;
                formState.mobile = result.mobile;
                formState.email = result.email;
            } else {
                formState.companyCode = result.companyCode;
                formState.companyName = result.companyName;
                formState.companyAddress = result.companyAddress;
                formState.companyPhone = result.companyPhone;
                formState.companyBank = result.companyBank;
                formState.companyAccount = result.companyAccount;
            }

            formState.mobile = result.mobile;
            formState.email = result.email;
        } else if (formState.titleType === 1) {
            formState.companyName = props.getAddressInfo.consignee;
            formState.mobile = props.getAddressInfo.mobile;
            formState.email = props.getAddressInfo.email;
        }
    } catch (error) {
        console.error(error);
    }
};

watch(show, (newVal) => {
    if (newVal) {
        __getInvoiceStatus();
        if (Object.keys(props.invoiceInfo)?.length == 0) {
            __getCheckInvoice();
        }
    }
});

const handleInvoice = () => {
    show.value = true;
};

const handleInvoiceType = (type: number) => {
    formState.invoiceType = type;
    clearFormState();
    if (type == 1) {
        __getCheckInvoice();
    } else {
        __getInvoiceStatus();
    }
};

const formRef = ref();

const onCancel = () => {
    show.value = false;
    emit("update:invoiceInfo", {});
};

const onSubmit = () => {
    formRef.value
        .validate()
        .then(() => {
            emit("update:invoiceInfo", formState);
            show.value = false;
        })
        .catch((err: any) => {
            console.log("表单错误信息：", err);
        });
};

const handleApply = () => {
    uni.navigateTo({
        url: "/pages/user/invoiceManagement/index?type=invoice"
    });
};
</script>

<style lang="scss" scoped>
.invoice {
    border-radius: 0;
    background: #fff;
    padding: 20rpx 28rpx;
    margin-bottom: 14rpx;
    font-size: 24.5rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;

    .invoice-content {
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .more-ico {
        width: 36rpx;
        height: 36rpx;
        padding-left: 10rpx;
    }
}

.invoice-popup-content {
    height: 70vh;

    .invoice-popup-box {
        padding: 30rpx 30rpx 0;
    }

    .title {
        padding-left: 0;
        height: 90rpx;
        line-height: 50rpx;
        padding-bottom: 40rpx;
    }

    .invoice-typemenu {
        //padding-bottom: 20rpx;
        border-bottom: 1rpx solid #eee;
        .invoice-typemenu-item {
            background: #f2f2f2;
            color: #282828;
            padding: 13rpx 36rpx;
            border-radius: 50rpx;
            font-size: 25rpx;
            display: inline-block;
            margin-right: 28rpx;
            margin-bottom: 10rpx;

            &.active {
                color: #fff;
                background-color: var(--general);
            }
        }

        .invoice-explain {
            padding: 20rpx 0;
            font-size: 24rpx;
            line-height: 35rpx;
            color: #999;

            .special-text {
                color: var(--general);
            }
        }
    }
}

.item-layout {
    height: 100%;
    display: flex;
    align-items: center;
}
.btn-box {
    height: 100%;
    width: 100%;
    padding: 25rpx;
    display: flex;
    justify-content: space-between;

    .btn {
        width: 48%;
    }
}
:deep(.uni-forms-item) {
    margin-bottom: 30rpx;
}
:deep(.uni-forms-item__error) {
    top: 90%;
    left: 18rpx;
}
:deep(.uni-easyinput__placeholder-class) {
    font-size: 26rpx;
}
:deep(.uni-forms-item__label) {
    font-size: 26rpx;
}

.invoiceInfo-content-item {
    display: flex;
    padding: 30rpx 0;
    font-size: 26rpx;
    /*  #ifdef MP-WEIXIN  */
    font-size: 26rpx;
    /*  #endif  */

    &:last-child {
        border-bottom: none;
        padding-bottom: 0;
    }

    .item-label {
        width: 140rpx;
    }
    .item-value {
        padding-left: 48rpx;
    }
}

.notPass {
    padding: 20rpx;
    .notPassBtn {
        color: var(--general);
    }
}
</style>
