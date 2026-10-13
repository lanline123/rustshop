/**
 * @file 增票资质详情/审核页
 * @description 查看和审核会员增值税专用发票资质申请
 * @features
 * - 显示公司名称/税号/地址/银行账户等开票信息
 * - 显示营业执照/税务登记证等证件图片
 * - 支持审核通过/拒绝操作
 * - 填写审核备注
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                        <el-form-item :label="$t('申请会员')" prop="username">
                            {{formState.username}}
                        </el-form-item>
                        <el-form-item :label="$t('公司名称')" prop="companyName">
                            {{formState.companyName}}
                        </el-form-item>
                        <el-form-item :label="$t('纳税人识别码')" prop="companyCode">
                            {{formState.companyCode}}
                        </el-form-item>
                        <el-form-item :label="$t('注册地址')" prop="companyAddress">
                            {{formState.companyAddress}}
                        </el-form-item>
                        <el-form-item :label="$t('注册电话')" prop="companyPhone">
                            {{formState.companyPhone}}
                        </el-form-item>
                        <el-form-item :label="$t('开户银行')" prop="companyBank">
                            {{formState.companyBank}}
                        </el-form-item>
                        <el-form-item :label="$t('银行账户')" prop="companyAccount">
                            {{formState.companyAccount}}
                        </el-form-item>
<!--                        <el-form-item label="税务登记证副本" prop="postscript">-->
<!--                        </el-form-item>-->
                        <el-form-item :label="$t('申请状态')" prop="status">
                            <el-radio-group v-model="formState.status">
                                <el-radio :value="1">{{ $t('审核通过') }}</el-radio>
                                <el-radio :value="2">{{ $t('待审核') }}</el-radio>
                                <el-radio :value="3">{{ $t('审核未通过') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <el-form-item :label="$t('申请未通过原因')" prop="applyReply">
                            <TigInput classType="tig-form-input" v-model="formState.applyReply" :row="2" type="textarea"/>
                            <div class="extra">{{ $t('若通过可不写') }}</div>
                        </el-form-item>
                        <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                            <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                        </el-form-item>


                </el-form>
                <a-spin :spinning="loading" style="width:100%;margin-top:100px"/>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import {onMounted, ref, shallowRef} from "vue"
import {useRouter} from 'vue-router'
import {message} from "ant-design-vue";
import type {UserInvoiceFormState} from '@/types/finance/userInvoice.d';
import {getUserInvoice, updateUserInvoice} from "@/api/finance/userInvoice";
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
        default: ''
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === 'add' ? 'create' : 'update';
const formRef = shallowRef();
const formState = ref<UserInvoiceFormState>({
});
const fetchUserInvoice = async () => {
    try {
        const result = await getUserInvoice(action.value, {id: id.value});
        Object.assign(
          formState.value,
          result
        )
    } catch (error:any) {
        message.error(error.message);
        emit('close');
    } finally {
        loading.value = false;
    }
};


onMounted(() => {
    // 获取详情数据
    fetchUserInvoice();
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit('update:confirmLoading', true);
        const result = await updateUserInvoice(operation, {id: id.value, ...formState.value});
        emit('submitCallback', result);
        message.success(t('操作成功'));
    } catch (error:any) {
        message.error(error.message);
    } finally {
        emit('update:confirmLoading', false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit()
};

defineExpose({onFormSubmit});
</script>
<style scoped lang="less">

</style>
