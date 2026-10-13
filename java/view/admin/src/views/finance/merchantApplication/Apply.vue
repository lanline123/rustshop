<template>
    <div class="container">
        <div class="content_wrapper">
            <a-spin :spinning="loading">
                <div class="merchant-apply-page">
                    <!-- 步骤条 -->
                    <el-steps :active="activeStep" align-center finish-status="finish" class="apply-steps">
                        <el-step :title="$t('提交申请')" />
                        <el-step :title="$t('等待审核')" />
                        <el-step :title="$t('完成')" />
                    </el-steps>

                    <!-- 步骤一：填写并提交申请（无申请 / 草稿 / 重新提交驳回申请 可填写） -->
                    <div v-if="showForm" class="apply-body">
                        <el-alert
                            v-if="reEditing"
                            type="error"
                            :closable="false"
                            show-icon
                            style="margin-bottom: 16px"
                            :title="$t('上次申请未通过：') + (detail.rejectReason || $t('请修改资料后重新提交'))"
                        />
                        <SubmitForm
                            ref="formRef"
                            hideShopSelect
                            hideSubmitButton
                            :act="reEditing ? 'edit' : ''"
                            :id="reEditing ? detail.id : ''"
                            @submitCallback="onSubmitted"
                        />
                        <div class="apply-actions">
                            <el-button v-if="reEditing" @click="cancelReEdit">{{ $t('返回') }}</el-button>
                            <el-button type="primary" :loading="submitLoading" @click="triggerSubmit">{{ $t('提交申请') }}</el-button>
                        </div>
                    </div>

                    <!-- 审核未通过（驳回） -->
                    <div v-else-if="isRejected" class="apply-status">
                        <img class="status-image status-image--abort" :src="abortImage" alt="" />
                        <div class="status-title">{{ $t('审核未通过') }}</div>
                        <div class="status-desc">{{ $t('您的进件审核未通过，请查看下方驳回原因。') }}</div>
                        <div class="status-desc">{{ $t('请您修改资料后重新提交支付进件。') }}</div>
                        <div v-if="detail.rejectReason" class="reject-reason">{{ $t('驳回原因：{rejectReason}', { rejectReason: detail.rejectReason }) }}</div>
                        <div class="status-actions">
                            <DialogForm
                                :params="{ act: 'detail', id: detail.id }"
                                isDrawer
                                :showFooter="false"
                                path="finance/merchantApplication/DetailForm"
                                :title="$t('进件详情')"
                                width="760px"
                            >
                                <a class="btn-link">{{ $t('进件详情') }}</a>
                            </DialogForm>
                            <el-button type="primary" @click="startReEdit">{{ $t('修改资料重新提交') }}</el-button>
                        </div>
                    </div>

                    <!-- 步骤二：等待审核 -->
                    <div v-else-if="isPending" class="apply-status">
                        <img class="status-image status-image--apply" :src="applyImage" alt="" />
                        <div class="status-title">{{ $t('进件信息正在审核中...') }}</div>
                        <div class="status-desc">{{ $t('您提交的进件资料已成功提交，正在等待平台审核，请耐心等待。') }}</div>
                        <DialogForm
                            :params="{ act: 'detail', id: detail.id }"
                            isDrawer
                            :showFooter="false"
                            path="finance/merchantApplication/DetailForm"
                            :title="$t('进件详情')"
                            width="760px"
                        >
                            <a class="btn-link">{{ $t('进件详情') }}</a>
                        </DialogForm>
                    </div>

                    <!-- 步骤三：审核通过 / 完成 -->
                    <div v-else-if="isFinish" class="apply-status">
                        <img class="status-image" :src="doneImage" alt="" />
                        <div class="status-title">{{ $t('审核通过') }}</div>
                        <div class="status-desc">{{ $t('您的进件资料已通过审核，账户已开通，可正常使用相关功能。') }}</div>
                        <div v-if="detail.subMchId" class="status-extra">{{ $t('子商户号：{subMchId}', { subMchId: detail.subMchId }) }}</div>
                        <DialogForm
                            :params="{ act: 'detail', id: detail.id }"
                            isDrawer
                            :showFooter="false"
                            path="finance/merchantApplication/DetailForm"
                            :title="$t('进件详情')"
                            width="760px"
                        >
                            <a class="btn-link">{{ $t('进件详情') }}</a>
                        </DialogForm>
                    </div>

                    <!-- 进行中（已提交微信，审核中等中间态） -->
                    <div v-else class="apply-status">
                        <img class="status-image status-image--apply" :src="applyImage" alt="" />
                        <div class="status-title">{{ detail.statusDesc || $t('进件处理中') }}</div>
                        <div class="status-desc">{{ $t('您的进件申请已提交至支付渠道，正在处理中，请耐心等待。') }}</div>
                        <DialogForm
                            :params="{ act: 'detail', id: detail.id }"
                            isDrawer
                            :showFooter="false"
                            path="finance/merchantApplication/DetailForm"
                            :title="$t('进件详情')"
                            width="760px"
                        >
                            <a class="btn-link">{{ $t('进件详情') }}</a>
                        </DialogForm>
                    </div>
                </div>
            </a-spin>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, computed } from "vue";
import { message } from "ant-design-vue";
import applyImage from "@/assets/onboarding/apply.png";
import abortImage from "@/assets/onboarding/abort.png";
import doneImage from "@/assets/onboarding/done.png";
import { DialogForm } from "@/components/dialog";
import SubmitForm from "@/views/finance/merchantApplication/SubmitForm.vue";
import { getCurrentMerchantApplication } from "@/api/finance/merchantApplication";
import type { MerchantApplicationItem } from "@/types/finance/merchantApplication.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const loading = ref(true);
const submitLoading = ref(false);
const formRef = ref();
const detail = ref<Partial<MerchantApplicationItem>>({});
// 驳回后点击"修改资料重新提交"进入表单编辑态
const reEditing = ref(false);

// 可重新填写的状态：无记录 / 草稿
const FORM_STATUS = ["", "DRAFT"];
const status = computed(() => detail.value.status || "");
const isRejected = computed(() => status.value === "REJECTED");
const isPending = computed(() => status.value === "PENDING_REVIEW");
const isFinish = computed(() => status.value === "FINISH");
// 表单展示：草稿/新申请，或驳回后主动进入重新提交
const showForm = computed(() => FORM_STATUS.includes(status.value) || reEditing.value);

const activeStep = computed(() => {
    if (showForm.value) return 0;
    if (isFinish.value) return 3;
    return 1;
});

const startReEdit = () => {
    reEditing.value = true;
};
const cancelReEdit = () => {
    reEditing.value = false;
};

const loadCurrent = async () => {
    loading.value = true;
    try {
        const result = await getCurrentMerchantApplication();
        detail.value = result || {};
    } catch (error: any) {
        detail.value = {};
    } finally {
        loading.value = false;
    }
};

const triggerSubmit = async () => {
    if (!formRef.value?.onFormSubmit) return;
    submitLoading.value = true;
    try {
        await formRef.value.onFormSubmit();
    } finally {
        submitLoading.value = false;
    }
};

const onSubmitted = () => {
    message.success(t('提交成功，等待平台审核'));
    reEditing.value = false;
    loadCurrent();
};

loadCurrent();
</script>

<style lang="less" scoped>
.merchant-apply-page {
    padding: 20px 10px;
    .status-image {
        display: block;
        width: 120px;
        height: 120px;
        object-fit: contain;
        margin: 0 auto 20px;
    }
    .status-image--apply {
        height: 135px;
    }
    .apply-steps {
        max-width: 1400px;
        margin: 10px auto 30px;
        :deep(.el-step__head.is-finish),
        :deep(.el-step__title.is-finish) {
            color: var(--theme-color, #1456f0);
            border-color: var(--theme-color, #1456f0);
        }
        :deep(.el-step__head.is-finish .el-step__icon.is-text) {
            color: #ffffff;
            background-color: var(--theme-color, #1456f0);
        }
        :deep(.el-step__head.is-process),
        :deep(.el-step__title.is-process) {
            color: var(--theme-color, #1456f0);
            border-color: var(--theme-color, #1456f0);
        }
        :deep(.el-step__title.is-process) {
            font-weight: normal;
        }
        :deep(.el-step__title.is-wait) {
            color: #b5b7c0;
        }
        :deep(.el-step__line-inner) {
            border-color: #e5e7eb;
            background-color: #e5e7eb;
        }
        :deep(.el-step.is-center .el-step__line) {
            left: calc(50% + 32px);
            right: calc(-50% + 32px);
        }
    }
    .apply-body {
        margin: 0;
        .apply-actions {
            margin-top: 24px;
        }
    }
    .apply-status {
        text-align: center;
        padding: 50px 0 70px;
        .status-icon {
            font-size: 64px;
            &.success {
                color: var(--theme-color, #1456f0);
            }
            &.pending {
                color: #1456f0;
            }
            &.error {
                color: #f5483b;
            }
        }
        .status-image--abort {
            width: 180px;
        }
        .status-title {
            margin-top: 20px;
            font-size: 18px;
            font-weight: 600;
            color: #1f2937;
        }
        .status-desc {
            margin-top: 10px;
            font-size: 13px;
            color: #6b7280;
            line-height: 20px;
        }
        .status-desc + .status-desc {
            margin-top: 2px;
        }
        .status-extra {
            margin-top: 8px;
            font-size: 13px;
            color: #374151;
        }
        .reject-reason {
            margin-top: 16px;
            font-size: 13px;
            color: #f5483b;
        }
        .status-actions {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 16px;
            margin-top: 20px;
        }
        .btn-link {
            display: inline-block;
            margin-top: 16px;
        }
        .status-actions .btn-link {
            margin-top: 0;
        }
    }
}
</style>
