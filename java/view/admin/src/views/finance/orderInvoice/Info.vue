/**
 * @file 订单发票详情/开票页
 * @description 查看订单发票详情并执行开票操作
 * @features
 * - 显示发票类型/抬头/税号/金额等信息
 * - 增值税专票显示公司详细开票资质
 * - 填写发票号码和上传发票附件
 * - 支持开具发票和作废发票
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" :rules="rules" label-width="auto">
                    <el-form-item :label="$t('申请会员')" prop="username">
                        {{ formState.username || "--" }}
                    </el-form-item>
                    <el-form-item :label="$t('发票类型')" prop="invoiceTypeName">
                        {{ formState.invoiceTypeName || "--" }}
                    </el-form-item>
                    <template v-if="formState.invoiceType === 2 && formState.userInvoice">
                        <el-form-item :label="$t('增票资质状态')" prop="companyName">
                            <DialogForm
                                :showOnOk="false"
                                :params="{ act: 'detail', id: formState.userInvoice.invoiceId }"
                                isDrawer
                                path="finance/userInvoice/Info"
                                :title="$t('编辑增票资质申请')"
                                width="600px"
                            >
                                {{ formState.userInvoice.statusName }} <a class="btn-link">{{ $t('[查看]') }}</a>
                            </DialogForm>
                        </el-form-item>
                        <el-form-item :label="$t('公司名称')" prop="companyName">
                            {{ formState.companyName || "--" }}
                        </el-form-item>
                        <el-form-item :label="$t('纳税人识别码')" prop="companyCode">
                            {{ formState.companyCode || "--" }}
                        </el-form-item>
                        <el-form-item :label="$t('注册地址')" prop="companyAddress">
                            {{ formState.companyAddress || "--" }}
                        </el-form-item>
                        <el-form-item :label="$t('注册电话')" prop="companyPhone">
                            {{ formState.companyPhone || "--" }}
                        </el-form-item>
                        <el-form-item :label="$t('开户银行')" prop="companyBank">
                            {{ formState.companyBank || "--" }}
                        </el-form-item>
                        <el-form-item :label="$t('银行账户')" prop="companyAccount">
                            {{ formState.companyAccount || "--" }}
                        </el-form-item>
                    </template>
                    <template v-else>
                        <el-form-item :label="$t('发票抬头')" prop="titleType">
                            {{ formState.titleType == 1 ? $t('个人') : $t('企业') }}
                        </el-form-item>
                        <el-form-item :label="$t('企业税号')" prop="companyCode">
                            {{ formState.companyCode || "--" }}
                        </el-form-item>
                        <el-form-item :label="$t('发票内容')" prop="invoiceContent">
                            {{ formState.invoiceContent || "--" }}
                        </el-form-item>
                    </template>
                    <el-form-item :label="$t('收票人姓名')" prop="companyName">
                        {{ formState.companyName || "--" }}
                    </el-form-item>
                    <el-form-item :label="$t('收票人电话')" prop="mobile">
                        {{ formState.mobile || "--" }}
                    </el-form-item>
                    <el-form-item :label="$t('收票人邮箱')" prop="email">
                        {{ formState.email || "--" }}
                    </el-form-item>
                    <el-form-item :label="$t('收票人地址')" prop="companyAddress">
                        {{ formState.companyAddress || formState.userAddress }}
                    </el-form-item>
                    <el-form-item :label="$t('申请状态')" prop="status">
                        <el-radio-group v-model="formState.status">
                            <el-radio :value="0">{{ $t('待处理') }}</el-radio>
                            <el-radio :value="1">{{ $t('已开') }}</el-radio>
                            <el-radio :value="2">{{ $t('失败/作废') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('发票金额')" prop="amount">
                        <TigInput classType="tig-form-input" type="decimal" v-model="formState.amount" />
                    </el-form-item>
                    <el-form-item :label="$t('上传发票附件')" prop="invoiceAttachment">
                        <div v-if="!loading" class="invoice-attachment-upload">
                            <div class="invoice-attachment-list">
                                <div v-for="(item, index) in formState.invoiceAttachment || []" :key="index" class="invoice-attachment-item">
                                    <div class="item-image-con">
                                        <img class="item-image-src" :src="imageFormat((item as any).picThumb)" />
                                        <DialogForm
                                            v-if="!examine"
                                            type="gallery"
                                            @okCallback="(result: any) => onEditImage(result, index)"
                                            :params="{ isMultiple: false }"
                                        >
                                            <span class="change-image">{{ $t('更换图片') }}</span>
                                        </DialogForm>
                                        <div v-if="!examine" class="del-item" @click="removeImage(index)">
                                            <i class="iconfont icon-cha"></i>
                                        </div>
                                    </div>
                                </div>
                                <div v-if="!examine" class="invoice-attachment-add">
                                    <DialogForm type="gallery" @okCallback="onAddImage" :params="{ isMultiple: true }">
                                        <div class="add-btn">
                                            <i class="ico-decorate icon-dec-jia"></i>
                                            <span>{{ $t('添加发票附件') }}</span>
                                        </div>
                                    </DialogForm>
                                </div>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('申请未通过原因')" prop="applyReply">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.applyReply" :row="2" type="textarea" />
                            <div class="extra">{{ $t('若通过可不写') }}</div>
                        </div>
                    </el-form-item>
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
import { onMounted, ref, shallowRef, computed } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { OrderInvoiceFormState } from "@/types/finance/orderInvoice.d";
import { getOrderInvoice, updateOrderInvoice } from "@/api/finance/orderInvoice";
import { DialogForm } from "@/components/dialog";
import { imageFormat } from "@/utils/format";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
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
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<OrderInvoiceFormState>({});
// 计算是否禁用编辑：有发票附件且状态不是待处理（0）时禁用
const examine = computed(() => {
    return formState.value.invoiceAttachment && formState.value.invoiceAttachment.length > 0 && formState.value.status !== 0;
});
// 审批为“失败/作废”(status=2)时，申请未通过原因必填
const rules = computed(() => ({
    applyReply:
        formState.value.status === 2
            ? [{ required: true, message: t('请填写申请未通过原因'), trigger: "blur" }]
            : []
}));
const fetchOrderInvoice = async () => {
    try {
        const result = await getOrderInvoice(action.value, { id: id.value });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    // 获取详情数据
    fetchOrderInvoice();
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateOrderInvoice(operation, { id: id.value, ...formState.value });
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

// 更换图片
const onEditImage = (result: any, index: number) => {
    if (!formState.value.invoiceAttachment) {
        formState.value.invoiceAttachment = [];
    }
    Object.assign(formState.value.invoiceAttachment[index], result[0]);
};

// 添加图片
const onAddImage = (result: any) => {
    if (!formState.value.invoiceAttachment) {
        formState.value.invoiceAttachment = [];
    }
    formState.value.invoiceAttachment = [...formState.value.invoiceAttachment, ...result];
};

// 删除图片
const removeImage = (index: number) => {
    if (formState.value.invoiceAttachment) {
        formState.value.invoiceAttachment.splice(index, 1);
    }
};

defineExpose({ onFormSubmit });
</script>
<style scoped lang="less">
.invoice-attachment-upload {
    .invoice-attachment-list {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
    }

    .invoice-attachment-item {
        position: relative;

        .item-image-con {
            position: relative;
            width: 120px;
            height: 120px;
            border: 1px solid #e5e5e5;
            text-align: center;
            background: #f2f2f2;
            cursor: pointer;
            transition: all 0.3s;

            .item-image-src {
                width: 100%;
                height: 100%;
                object-fit: contain;
            }

            .change-image {
                position: absolute;
                bottom: 0;
                left: 0;
                width: 100%;
                height: 24px;
                line-height: 24px;
                font-size: 12px;
                color: #fff;
                background: rgba(0, 0, 0, 0.5);
                cursor: pointer;
                text-align: center;
            }

            .del-item {
                position: absolute;
                top: -8px;
                right: -8px;
                width: 20px;
                height: 20px;
                background: #dbdbdb;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                z-index: 2;

                i {
                    font-size: 12px;
                    color: #fff;
                }

                &:hover {
                    background: #333;
                }
            }

            &:hover {
                border-color: #155bd4;
            }
        }
    }

    .invoice-attachment-add {
        .add-btn {
            width: 120px;
            height: 120px;
            border: 1px dashed #ddd;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            background: #fafafa;
            transition: all 0.3s;

            i {
                font-size: 24px;
                color: #999;
                margin-bottom: 8px;
            }

            span {
                font-size: 12px;
                color: #999;
            }

            &:hover {
                border-color: #155bd4;
                color: #155bd4;
                background: #f0f7ff;

                i,
                span {
                    color: #155bd4;
                }
            }
        }
    }
}
</style>
