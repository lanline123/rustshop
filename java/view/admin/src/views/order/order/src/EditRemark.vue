/**
 * @file 订单备注编辑
 * @description 订单备注信息编辑表单
 * @features
 * - 支持添加订单操作日志（addNote）
 * - 支持修改管理员备注（adminNote）
 * - 备注最大200字符
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('备注')" :prop="valueName">
                        <TigInput classType="tig-form-input" v-model="formState[valueName]" :rows="4" :placeholder="$t('请输入备注')" type="textarea" show-word-limit :maxlength="200"/>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()">{{ $t('提交') }} </el-button>
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
import {OrderFormState} from '@/types/order/order.d';
import {getOrder, updateOrder, updateOrderLog, setAdminNote} from "@/api/order/order";

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
    valueName: {
        type: String,
        default: ''
    },
    form: {
        type: Object as () => OrderFormState,
        default: () => ({})
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === 'add' ? 'create' : 'update';
const formRef = shallowRef();
const formState = ref<any>(props.form);

onMounted(() => {
    // 获取详情数据
    fetchOrder();
});
const fetchOrder = async () => {
    try {
        loading.value = true;
        const result = await getOrder(action.value, { id: id.value });
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


// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit('update:confirmLoading', true);
        if (props.valueName == 'addNote') {
            const result = await updateOrderLog({orderId: id.value,orderSn: formState.value.orderSn,shopId:formState.value.shopId,description:formState.value[props.valueName]});
            emit('submitCallback', result);
            message.success(t('操作成功'));
        }

        if (props.valueName == 'adminNote') {
            const result = await setAdminNote({id: id.value,adminNote:formState.value[props.valueName]});
            emit('submitCallback', result);
            message.success(t('操作成功'));
        }
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
