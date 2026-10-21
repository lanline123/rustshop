<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="exchangeIntegral" :label="$t('兑换积分')" :rules="[{ required: true, message: '兑换积分不能为空!' }]">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.exchangeIntegral" />
                    </el-form-item>
                    <el-form-item prop="pointsDeductedAmount" :label="$t('减免金额')" :rules="[{ required: true, message: '减免金额不能为空!' }]">
                        <TigInput classType="tig-form-input" type="decimal" v-model="formState.pointsDeductedAmount" />
                    </el-form-item>
                    <el-form-item prop="isEnabled" :label="$t('兑换')">
                        <el-radio-group v-model="formState.isEnabled">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item prop="isHot" :label="$t('是否热销')">
                        <el-radio-group v-model="formState.isHot">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item prop="productId" :label="$t('选择商品')">
                        <SelectProduct
                            :isSku="true"
                            :isMultiple="false"
                            v-model:ids="formState.productId"
                            v-model:skuId="formState.skuId"
                            :isSelf="1"
                            :isFilterPresaleProduct="true"
                        ></SelectProduct>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { PointsExchangeFormState } from "@/types/promotion/pointsExchange.d";
import { getPointsExchange, updatePointsExchange } from "@/api/promotion/pointsExchange";
import { SelectProduct } from "@/components/select";
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
const formState = ref<PointsExchangeFormState>({
    productId: "",
    skuId: "",
    isEnabled: 1,
    isHot: 0
});

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchPointsExchange();
    } else {
        loading.value = false;
    }
});
const fetchPointsExchange = async () => {
    try {
        const result = await getPointsExchange(action.value, { id: id.value });
        if (result.productId) {
            result.productId = [result.productId];
        }
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        let params = JSON.parse(JSON.stringify(formState.value));
        if (params.productId) {
            params.productId = params.productId[0];
        } else {
            message.error(t('请选择商品!'));
            return;
        }
        const result = await updatePointsExchange(operation, { id: id.value, ...params });
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
