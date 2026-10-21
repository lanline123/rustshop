<template>
    <ProductDesc
        v-if="formState.productType == 4"
        v-model:descArr="formState.paidContent"
        :disabled="isDescDisabled || type !== ''"
        :productType="formState.productType"
        :label="$t('付费内容详情')"
    ></ProductDesc>
    <ProductDesc
        v-model:descArr="formState.productDescArr"
        :disabled="isDescDisabled"
        :label="formState.productType == 4 ? $t('普通商品详情') : $t('商品详情')"
    ></ProductDesc>
    <el-form-item
        v-if="formState.productType == 2"
        :rules="[{ required: true, message: '虚拟商品信息不能为空!', validator: validateVirtualSample }]"
        :label="$t('虚拟商品信息')"
        prop="virtualSample"
    >
        <Editor v-model:html="formState.virtualSample" height="350px"></Editor>
        <div class="extra">{{ $t('此处填写内容，将在订单详情中展示') }}</div>
    </el-form-item>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { ProductFormState } from "@/types/product/product.d";
import ProductDesc from "@/views/product/product/src/ProductDesc.vue";
import { Editor } from "@/components/editor/index";
import { getAdminType, getShopType } from "@/utils/storage";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const adminType = getAdminType();

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
    examine: {
        type: Number,
        default: 0
    },
    type: {
        type: String,
        default: ""
    },
    loading: {
        type: Boolean,
        default: false
    },
    storeSettingInfo: {
        type: Object,
        default: () => ({})
    }
});
const formState = defineModel<ProductFormState>("formState", { default: {} });

const isDescDisabled = computed(() => {
    return (
        (getShopType() !== 2 && props.examine == 1) ||
        (getShopType() === 2 && props.storeSettingInfo.storeAssignProductImage === 0 && formState.value.shopId === 0)
    );
});

const validateVirtualSample = (rule: any, value: any, callback: any) => {
    if (value == "<p><br></p>" || value == "") {
        callback(new Error(t('虚拟商品信息不能为空')));
        return;
    } else {
        callback();
    }
};

</script>
<style lang="less" scoped>

</style>
