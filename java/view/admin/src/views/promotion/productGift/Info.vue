<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="giftName" :label="$t('赠品名称')" :rules="[{ required: true, message: '赠品名称不能为空!' }]">
                        <TigInput v-model="formState.giftName" classType="tig-form-input" />
                    </el-form-item>
                    <el-form-item prop="productInfo" :label="$t('选择商品')">
                        <SelectProduct
                            :isSku="true"
                            :isSelf="1"
                            :isGift="true"
                            :isMultiple="false"
                            :isFilterPresaleProduct="true"
                            :other-params="{ onlyShowMe: true, excludeCycleProduct: true }"
                            v-model:ids="formState.productId"
                            v-model:skuId="formState.skuId"
                            v-model:skuStock="formState.giftStock"
                        >
                        </SelectProduct>
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
import { ProductGiftFormState } from "@/types/promotion/productGift.d";
import { getProductGift, updateProductGift } from "@/api/promotion/productGift";
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
const formState = ref<ProductGiftFormState>({
    productId: "",
    skuId: "",
    giftName: "",
    giftStock: 1
});

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchProductGift();
    } else {
        loading.value = false;
    }
});
const fetchProductGift = async () => {
    try {
        const result = await getProductGift(action.value, { giftId: id.value });
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
        if (params.productId && params.productId.length > 0) {
            params.productId = params.productId[0];
        } else {
            message.error(t('请选择商品!'));
            return;
        }
        if (params.giftStock > 0) {
            const result = await updateProductGift(operation, { id: id.value, ...params });
            emit("submitCallback", result);
            message.success(t('操作成功'));
        } else {
            message.error(t('赠品库存必须大于0'));
            return;
        }
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
