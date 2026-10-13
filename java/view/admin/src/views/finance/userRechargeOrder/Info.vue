/**
 * @file 会员充值订单详情/添加页
 * @description 查看充值订单详情或手动创建充值订单
 * @features
 * - 添加模式：选择会员/填写金额/设置状态
 * - 详情模式：显示充值信息/处理到款
 * - 支持确认到款和取消操作
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <template v-if="action === 'add'">
                        <el-form-item :rules="[{ required: true, message: '会员名称不能为空!' }]" :label="$t('会员名称')" prop="userId">
                            <SelectUser :multiple="false" v-model:userId="formState.userId"></SelectUser>
                        </el-form-item>
                        <el-form-item :rules="[{ required: true, message: '充值金额不能为空!' }]" :label="$t('金额')" prop="amount">
                            <TigInput classType="tig-form-input" v-model="formState.amount" type="decimal"  :min="0" />
                        </el-form-item>
                        <el-form-item :label="$t('管理员备注')" prop="postscript">
                            <TigInput classType="tig-form-input" v-model="formState.postscript" :row="2" type="textarea"/>
                        </el-form-item>
                        <el-form-item :label="$t('到款状态')" prop="status">
                            <el-radio-group v-model="formState.status">
                                <el-radio :value="0">{{ $t('待支付') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                            <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                        </el-form-item>
                    </template>
                    <template v-else>
                        <div class="notice-warp">
                            <p> {{ $t('会员金额信息') }}</p>
                            <hr style="border: 0.5px solid #dddddd;">{{ $t('会员名称：{username}', { username: formState.user?.username }) }}<br>{{ $t('金额：{priceFormat}', { priceFormat: priceFormat(formState.amount) }) }}<br>{{ $t('赠送金额：{priceFormat}', { priceFormat: priceFormat(formState.discountMoney) }) }}<br>{{ $t('操作日期：{addTime}', { addTime: formState.addTime }) }}
                        </div>
                        <el-form-item  :rules="[{ required: true, message: '管理员备注不能为空!' }]"  :label="$t('管理员备注')" prop="postscript">
                            <TigInput classType="tig-form-input" v-model="formState.postscript" :row="2" type="textarea" :disabled="!isEdit"/>
                        </el-form-item>
                        <el-form-item :label="$t('到款状态')" prop="status">
                            <el-radio-group v-model="formState.status" :disabled="!isEdit">
                                <el-radio :value="1">{{ $t('已支付') }}</el-radio>
                                <el-radio :value="2">{{ $t('无效') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </template>
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
import type {UserRechargeOrderFormState} from '@/types/finance/userRechargeOrder.d';
import {getUserRechargeOrder, updateUserRechargeOrder} from "@/api/finance/userRechargeOrder";
import {SelectUser} from "@/components/select";
import {priceFormat} from "@/utils/format";
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
const formState = ref<UserRechargeOrderFormState>({
    amount: 0,
    status: 0
});
const isEdit = ref<boolean>(false);
const fetchUserRechargeOrder = async () => {
    try {
        const result = await getUserRechargeOrder(action.value, {id: id.value});
        Object.assign(
          formState.value,
          result
        )
        if(result.status === 0){
            isEdit.value = true;
        }
    } catch (error:any) {
        message.error(error.message);
        emit('close');
    } finally {
        loading.value = false;
    }
};


onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchUserRechargeOrder();
    } else {
        loading.value = false;
    }
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit('update:confirmLoading', true);
        const result = await updateUserRechargeOrder(operation, {id: id.value, ...formState.value});
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
.notice-warp {
    font-weight: 500;
    color: black;
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}
</style>
