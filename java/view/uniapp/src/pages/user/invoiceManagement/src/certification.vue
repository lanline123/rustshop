<template>
    <view v-if="Object.keys(formState).length > 0" class="certification safe-padding">
        <view class="tip-box">
            <view class="tig-title"> {{ $t("温馨提示") }} </view>
            <view>{{ $t("首次开具增值税专用发票") }}，{{ $t("请添加资质后并提交") }}。</view>
            <view>{{ $t("我们会在一个工作日内完成审核工作") }}。</view>
            <view>
                1.{{ $t("补发流程") }}：
                <text class="specaial-text"
                    >{{ $t("资质未审核通过") }}-{{ $t("登录会员中心") }}→{{ $t("增票资质") }}→{{ $t("填写资质信息") }}→{{ $t("提交") }}。</text
                >
            </view>
            <view>
                2. {{ $t("资质审核结果查询时间") }}：<text class="specaial-text">{{ $t("资质上传一到两个工作日后即可查询") }}。</text></view
            >
            <view> 3. {{ $t("注意有效增值税专用发票开票资质仅为一个") }}。</view>
        </view>
        <view class="status-box">
            <view class="status-text">
                <view> {{ $t("您的增票资质状态") }}：</view>
                <view class="specaial-text">{{ formatText }}</view>
            </view>
        </view>
        <view class="certification-content">
            <view v-if="showEdit" class="content-title">
                {{ $t("填写增票资质信息") }}<text class="specaial-text">（{{ $t("所有信息均为必填") }}）</text>
            </view>
            <view v-else-if="!showEdit && Object.keys(formState).length > 0" class="content-title"> {{ $t("增票资质信息") }}</view>

            <view>
                <template v-if="showEdit">
                    <uni-forms ref="formRef" :model-value="editFormState" :rules="rules" label-width="180rpx">
                        <uni-forms-item :label="$t('单位名称')" name="companyName">
                            <uni-easyinput
                                v-model="editFormState.companyName"
                                primary-color="rgb(192, 196, 204)"
                                :input-border="false"
                                :placeholder="$t('请输入单位名称')"
                            />
                        </uni-forms-item>
                        <uni-forms-item :label="$t('纳税人识别码')" name="companyCode">
                            <uni-easyinput
                                v-model="editFormState.companyCode"
                                primary-color="rgb(192, 196, 204)"
                                :input-border="false"
                                :placeholder="$t('请输入纳税人识别码')"
                            />
                        </uni-forms-item>
                        <uni-forms-item :label="$t('注册地址')" name="companyAddress">
                            <uni-easyinput
                                v-model="editFormState.companyAddress"
                                primary-color="rgb(192, 196, 204)"
                                :input-border="false"
                                :placeholder="$t('请输入注册地址')"
                            />
                        </uni-forms-item>
                        <uni-forms-item :label="$t('注册电话')" name="companyPhone">
                            <uni-easyinput
                                v-model="editFormState.companyPhone"
                                primary-color="rgb(192, 196, 204)"
                                :input-border="false"
                                :placeholder="$t('请输入注册电话')"
                            />
                        </uni-forms-item>
                        <uni-forms-item :label="$t('开户银行')" name="companyBank">
                            <uni-easyinput
                                v-model="editFormState.companyBank"
                                primary-color="rgb(192, 196, 204)"
                                :input-border="false"
                                :placeholder="$t('请输入开户银行')"
                            />
                        </uni-forms-item>
                        <uni-forms-item :label="$t('银行账户')" name="companyAccount">
                            <uni-easyinput
                                v-model="editFormState.companyAccount"
                                primary-color="rgb(192, 196, 204)"
                                :input-border="false"
                                :placeholder="$t('请输入银行账户')"
                            />
                        </uni-forms-item>
                    </uni-forms>
                    <!-- <view class="agreement-box">
                        <tig-checkbox v-model:checked="isConfirm" :checked-size="0.7" />
                        {{ $t("我已阅读并同意") }}<text class="specaial-text" @click="handleConfirmation">《{{ $t("增票资质确认书") }}》</text>
                    </view> -->
                </template>
                <template v-if="formState.status === 0 && !showEdit">
                    <view class="empty-text">{{ $t("暂无增票资质信息") }}!</view>
                </template>
                <template v-else-if="!showEdit && Object.keys(formState).length > 0">
                    <uni-forms ref="formRef" :class="{ readonly: !showEdit }" :model-value="formState" :rules="rules" label-width="180rpx">
                        <uni-forms-item :label="$t('单位名称')">
                            <view class="disabled-text">
                                {{ formState.companyName }}
                            </view>
                        </uni-forms-item>
                        <uni-forms-item :label="$t('纳税人识别码')">
                            <view class="disabled-text">
                                {{ formState.companyCode }}
                            </view>
                        </uni-forms-item>
                        <uni-forms-item :label="$t('注册地址')">
                            <view class="disabled-text">
                                {{ formState.companyAddress }}
                            </view>
                        </uni-forms-item>
                        <uni-forms-item :label="$t('注册电话')">
                            <view class="disabled-text">
                                {{ formState.companyPhone }}
                            </view>
                        </uni-forms-item>
                        <uni-forms-item :label="$t('开户银行')">
                            <view class="disabled-text">
                                {{ formState.companyBank }}
                            </view>
                        </uni-forms-item>
                        <uni-forms-item :label="$t('银行账户')">
                            <view class="disabled-text">
                                {{ formState.companyAccount }}
                            </view>
                        </uni-forms-item>
                    </uni-forms>
                </template>
            </view>
        </view>

        <template v-if="showEdit">
            <tig-fixed-placeholder background-color="#fff">
                <view class="btn-box">
                    <tig-button class="btn" plain @click="showEdit = false"> {{ $t("取消") }}</tig-button>
                    <tig-button class="btn" @click="onSubmit"> {{ $t("提交") }}</tig-button>
                </view>
            </tig-fixed-placeholder>
        </template>

        <template v-else>
            <tig-fixed-placeholder background-color="#fff">
                <view class="btn-box">
                    <tig-button @click="handleShowEdit"> {{ formState.status === 0 ? $t("添加增票资质") : $t("修改并重新审核") }}</tig-button>
                </view>
            </tig-fixed-placeholder>
        </template>
    </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getUserInvoice, updateUserInvoice, createUserInvoice } from "@/api/user/invoiceManagemen";
import type { UserInvoiceFormState } from "@/types/user/invoiceManagement";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

// 检查是否包含中文字符
const checkChinese = (value: string): boolean => {
    if (!value) return true; // 空值由 required 验证处理
    const chineseRegex = /[\u4e00-\u9fa5]/;
    return !chineseRegex.test(value);
};

const formState = ref<UserInvoiceFormState>({});
const rules = {
    companyName: {
        rules: [{ required: true, errorMessage: t("单位名称不能为空!") }]
    },
    companyCode: {
        rules: [
            { required: true, errorMessage: t("纳税人识别码不能为空!") },
            {
                validateFunction: (rule: any, value: string, data: any, callback: any) => {
                    if (!checkChinese(value)) {
                        callback(t("纳税人识别码格式不正确"));
                    } else {
                        return true;
                    }
                }
            }
        ]
    },
    companyAddress: {
        rules: [{ required: true, errorMessage: t("注册地址不能为空!") }]
    },
    companyPhone: {
        rules: [
            { required: true, errorMessage: t("注册电话不能为空!") },
            {
                validateFunction: (rule: any, value: string, data: any, callback: any) => {
                    if (!/^\d{11}$/.test(value)) {
                        callback(t("注册电话格式不正确"));
                    } else {
                        return true;
                    }
                }
            }
        ]
    },
    companyBank: {
        rules: [{ required: true, errorMessage: t("开户银行不能为空!") }]
    },
    companyAccount: {
        rules: [
            { required: true, errorMessage: t("银行账户不能为空!") },
            {
                validateFunction: (rule: any, value: string, data: any, callback: any) => {
                    if (!/^\d{1,20}$/.test(value)) {
                        callback(t("银行账户格式不正确"));
                    } else {
                        return true;
                    }
                }
            }
        ]
    }
};
const isConfirm = ref(false);
const showEdit = ref(false);
const editFormState = ref<UserInvoiceFormState>({});
const __getUserInvoice = async () => {
    try {
        const result = await getUserInvoice();
        Object.assign(formState.value, result);
    } catch (error: any) {
    } finally {
    }
};

onMounted(() => {
    __getUserInvoice();
});

const handleShowEdit = () => {
    editFormState.value = JSON.parse(JSON.stringify(formState.value));
    showEdit.value = true;
};

const formatText = computed(() => {
    switch (formState.value.status) {
        case 0:
            return t("未申请专用发票");
        case 1:
            return t("审核通过");
        case 2:
            return t("已申请，待审核");
        case 3:
            return t("审核未通过");
        default:
            return "";
    }
});

const formRef = ref();
const onSubmit = () => {
    formRef.value
        .validate()
        .then((res: any) => {
            __updateUserInvoice();
        })
        .catch((err: any) => {
            console.log("表单错误信息：", err);
        });
};

const __updateUserInvoice = async () => {
    try {
        editFormState.value.isConfirm = 1;
        editFormState.value.titleType = 2;
        editFormState.value.invoiceType = 1;
        editFormState.value.isAdd = editFormState.value.status == 0 ? 1 : 0;
        if (editFormState.value.invoiceId) {
            await updateUserInvoice({ id: editFormState.value.invoiceId, ...editFormState.value });
        } else {
            await createUserInvoice(editFormState.value);
        }
        uni.showToast({
            title: t("提交成功"),
            icon: "none"
        });
        __getUserInvoice();
        showEdit.value = false;
    } catch (error: any) {
        console.log("error", error);
    }
};
</script>

<style lang="scss" scoped>
.certification {
    .tip-box {
        padding: 20rpx;
        font-size: 26rpx;
        line-height: 45rpx;
        background-color: #fff;
        border-radius: 20rpx;
        .tig-title {
            padding: 10rpx 0;
        }
    }

    .status-box {
        background: none repeat scroll 0 0 #fffdee;
        border: 1px solid #edd28b;
        padding: 10px;
        margin-top: 20rpx;
        font-size: 28rpx;

        .status-text {
            display: flex;
            padding-bottom: 15rpx;
            &:last-child {
                padding-bottom: 0;
            }
        }
    }

    .empty-text {
        display: flex;
        align-items: center;
        justify-content: center;

        padding: 40rpx 0;
    }
}

.specaial-text {
    color: var(--general);
}

.disabled-text {
    height: 100%;
    line-height: 68rpx;
    font-size: 24rpx;
    width: 450rpx;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.certification-content {
    padding: 20rpx;
    background-color: #fff;
    border-radius: 20rpx;
    margin-top: 20rpx;
    padding-bottom: 5rpx;

    .content-title {
        font-weight: bold;
        color: #666;
        padding: 15rpx 0;
    }
    .agreement-box {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 15rpx 0;
        font-size: 28rpx;
    }
}

.btn-box {
    padding: 25rpx 15rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;

    .btn {
        width: 48%;
    }
}

:deep(.uni-forms-item) {
    margin-bottom: 30rpx;
}

.readonly {
    :deep(.uni-forms-item) {
        margin-bottom: 5rpx;
    }
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
</style>
