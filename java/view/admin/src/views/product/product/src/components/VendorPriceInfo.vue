<template>
    <div>
        <el-form-item class="inner-item" :extra="$t('不填则根据设置的比例自动计算')" :label="$t('市场价')" prop="marketPrice">
            <div>
                <PriceInput v-model:modelValue="formState.marketPrice" :disabled="examine == 1" :placeholder="$t('请输入商品市场价')" width="200px"></PriceInput>
                <div class="extra">{{ $t('划线价格，不填则不显示') }}</div>
            </div>
        </el-form-item>
    </div>
    <VendorAttr
        v-if="!loading && formState.productType !== 3"
        ref="attrForm"
        v-model:attrChanged="formState.attrChanged"
        v-model:attrList="formState.attrList"
        v-model:productList="formState.productList"
        v-model:skuErrorText="skuErrorText"
        :action="action"
        :attrTplList="attrTplList"
    ></VendorAttr>
    <el-form-item class="inner-item" :label="$t('商品限购')" prop="limitNumber">
        <el-radio-group v-model="isLimit" style="width: 100%; margin-bottom: 10px; flex-direction: column; align-items: flex-start" @change="handleLimitChange">
            <el-radio :value="false">{{ $t('不限购') }}</el-radio>
            <el-radio :value="true">
                <div class="flex">
                    <div class="mr10">{{ $t('限购') }}</div>
                    <TigInput v-model="formState.limitNumber" :disabled="!isLimit" :placeholder="$t('请输入限购数量')" type="integer" width="200px" />
                    <div class="ml10">{{ $t('件') }}</div>
                </div>
            </el-radio>
        </el-radio-group>
    </el-form-item>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import { ProductFormState } from "@/types/product/product.d";
import PriceInput from "@/views/product/product/src/PriceInput.vue";
import VendorAttr from "@/views/product/product/src/VendorAttr.vue";
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "closeConfirm"]);
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    shopId: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    attrTplList: {
        type: Array,
        default: []
    },
    examine: {
        type: Number,
        default: 0
    },
    loading: {
        type: Boolean,
        default: false
    }
});
const action = ref<string>(props.act);
const formState = defineModel<ProductFormState>("formState", { default: {} });
console.log(formState.value, "formState.value");
const isLimit = defineModel<boolean>("isLimit", { default: false });

const attrForm = ref<any>();
const skuErrorText = ref<string>("");
const handleLimitChange = (value: any) => {
    if (value) {
        formState.value.limitNumber = !formState.value.limitNumber ? 1 : formState.value.limitNumber;
    } else {
        formState.value.limitNumber = "";
    }
};
defineExpose({
    attrForm,
    skuErrorText
});
</script>
<style lang="less" scoped></style>
