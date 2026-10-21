<template>
    <div>
        <!-- 新增：销售模式选择 -->
        <el-form-item v-if="formState.productType === 1" :label="$t('销售模式')" prop="saleMode" class="inner-item">
            <el-radio-group :model-value="saleModeRadioValue" :disabled="examine == 1" @change="handleSaleModeChange">
                <el-radio :value="0">{{ $t('现货销售') }}</el-radio>
                <el-radio :value="1" class="flex">
                    <div class="flex"> {{ $t('预售模式') }} <el-tooltip class="box-item" effect="light" placement="bottom" show-after="300">
                            <template #content>
                                <div style="width: 300px; padding: 5px 10px"> {{ $t('预售模式所设置的时间只在用户页面做展示，超出设定的时间客户依然可操作订单进行付款等操作。') }} </div>
                            </template>
                            <div class="flex flex-align-center refund-rules-link">
                                <el-icon style="margin-left: 5px; margin-top: 1px" size="14" color="#999">
                                    <QuestionFilled />
                                </el-icon>
                            </div>
                        </el-tooltip>
                    </div>
                </el-radio>
            </el-radio-group>
        </el-form-item>

        <!-- 新增：预售配置区域 -->
        <template v-if="formState.productType === 1 && (formState.saleMode === 1 || formState.saleMode === 2) && formState.presaleConfig">
            <div class="presale-section">
                <PresaleConfig
                    v-model="formState.presaleConfig"
                    :product-price="Number(formState.productPrice) || 0"
                    :product-list="formState.productList || []"
                    :disabled="examine == 1"
                />
            </div>
        </template>

        <!-- <el-form-item
            :rules="[{ required: true, message: '商品售价不能为空', validator: validatePrice }]"
            class="inner-item"
            label="商品售价"
            prop="productPrice"
        >
            <PriceInput
                v-model:modelValue="formState.productPrice"
                :disabled="
                    (getShopType() !== 2 && examine == 1) || (getShopType() === 2 && storeSettingInfo.storeAssignProductPrice === 0 && formState.shopId === 0)
                "
                placeholder="请输入商品售价"
                width="200px"
            ></PriceInput>
        </el-form-item> -->
        <!-- <el-form-item class="inner-item" extra="不填则根据设置的比例自动计算" label="市场价" prop="marketPrice">
            <div>
                <PriceInput v-model:modelValue="formState.marketPrice" :disabled="examine == 1" placeholder="请输入商品市场价" width="200px"></PriceInput>
                <div class="extra">划线价格，不填则不显示</div>
            </div>
        </el-form-item> -->
    </div>
    <Attr
        v-if="!loading"
        ref="attrForm"
        v-model:attrChanged="formState.attrChanged"
        v-model:attrList="formState.attrList"
        v-model:productList="productList"
        v-model:skuErrorText="skuErrorText"
        v-model:attrTplId="attrTplId"
        v-model:formState="formState"
        :action="action"
        :attrTplList="attrTplList"
        :disabled="examine == 1"
        :storeSettingInfo="storeSettingInfo"
        :shopId="String(formState.shopId || '')"
        v-model:skuType="skuType"
    ></Attr>
    <el-form-item class="inner-item" :label="$t('商品限购')" prop="limitNumber">
        <el-radio-group
            v-model="isLimit"
            :disabled="examine == 1"
            style="width: 100%; margin-bottom: 10px; flex-direction: column; align-items: flex-start"
            @change="handleLimitChange"
        >
            <el-radio :value="false">{{ $t('不限购') }}</el-radio>
            <el-radio :value="true">
                <div class="flex">
                    <div class="mr10">{{ $t('限购') }}</div>
                    <TigInput v-model="formState.limitNumber" :disabled="examine == 1" :placeholder="$t('请输入限购数量')" type="integer" width="200px" />
                    <div class="ml10">{{ $t('件') }}</div>
                </div>
            </el-radio>
        </el-radio-group>
    </el-form-item>
    <!--    成本价-->
    <!-- <el-form-item class="inner-item" label="成本价" prop="costPrice">
        <PriceInput v-model:modelValue="formState.costPrice" :disabled="examine == 1" placeholder="请输入商品成本价" width="200px"></PriceInput>
    </el-form-item> -->

</template>
<script lang="ts" setup>
import { ref, watch, computed } from "vue";
import { ProductFormState } from "@/types/product/product.d";
import PriceInput from "@/views/product/product/src/PriceInput.vue";
import Attr from "@/views/product/product/src/Attr.vue";
import { getAdminType, getShopType } from "@/utils/storage";
import { QuestionFilled } from "@element-plus/icons-vue";
import { isStore, isOverseas } from "@/utils/version";

import PresaleConfig from "./PresaleConfig.vue";
import type { PresaleConfig as PresaleConfigType } from "@/types/product/product.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
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
    },
    storeSettingInfo: {
        type: Object,
        default: {}
    }
});
const action = ref<string>(props.act);
const formState = defineModel<ProductFormState>("formState", { default: {} });
const productList = defineModel<any>("productList", { default: [] });
const skuType = defineModel<number>("skuType", { default: 1 });
const isLimit = defineModel<boolean>("isLimit", { default: false });
const attrTplId = defineModel<number | null>("attrTplId", { default: null });
const validatePrice = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('商品售价不能为空')));
        return;
    } else {
        callback();
    }
};
const validateStock = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('商品库存不能为空')));
        return;
    } else {
        callback();
    }
};

const attrForm = ref<any>();
const skuErrorText = ref<string>("");
const handleLimitChange = (value: any) => {
    if (value) {
        formState.value.limitNumber = !formState.value.limitNumber ? 1 : formState.value.limitNumber;
    } else {
        formState.value.limitNumber = "";
    }
};

// 计算单选按钮的值：当 saleMode 为 1 或 2 时，显示为选中"预售模式"（值为1）
const saleModeRadioValue = computed(() => {
    if (formState.value.saleMode === 1 || formState.value.saleMode === 2) {
        return 1;
    }
    return 0;
});
defineExpose({
    attrForm,
    skuErrorText,
});

watch(
    () => formState.value.productPrice,
    (newValue) => {
        if (newValue) {
            const decimalPart = newValue.toString().split(".")[1];
            if (decimalPart && decimalPart.length > 2) {
                const integerPart = newValue.toString().split(".")[0];
                const truncatedValue = Number(integerPart + "." + decimalPart.slice(0, 2));
                formState.value.productPrice = truncatedValue;
            }
        }
    }
);

// 监听规格类型切换，重置库存和规格明细
// watch(
//     () => skuType.value,
//     (newValue, oldValue) => {
//         // 仅在规格类型实际发生变化时重置（排除初始化）
//         if (oldValue !== undefined && newValue !== oldValue) {
//             // 重置统一规格的库存
//             formState.value.productStock = null;
//             // 清空规格明细列表，初始化为一行空数据
//             formState.value.productList = [{}];
//         }
//     }
// );
watch(
    () => formState.value.productType,
    async (newType) => {
        if (newType !== 1) {
            formState.value.saleMode = 0;
            formState.value.presaleConfig = undefined;
        }
    },
    { immediate: true }
);

// 新增：初始化预售配置
watch(
    () => formState.value.saleMode,
    (newMode) => {
        // 只有普通商品才能设置预售
        if (formState.value.productType !== 1) {
            return;
        }
        // 当 saleMode 为 1 或 2 时，初始化预售配置
        if ((newMode === 1 || newMode === 2) && !formState.value.presaleConfig) {
            // 初始化预售配置（默认全款预售，发货时间输入框为空）
            formState.value.presaleConfig = {
                saleMode: newMode, // 使用当前的 saleMode 值（1 或 2）
                deliveryType: 1,
                deliveryRelativeDays: undefined,
                deliveryRelativeHours: undefined
            };
        }
    },
    { immediate: true }
);

// 新增：监听 presaleConfig.saleMode 变化，同步更新外层的 saleMode
watch(
    () => formState.value.presaleConfig?.saleMode,
    (newPresaleSaleMode) => {
        // 只有普通商品才能设置预售
        if (formState.value.productType !== 1) {
            return;
        }
        // 当 presaleConfig.saleMode 变化时（1 或 2），同步更新外层的 saleMode
        if (newPresaleSaleMode === 1 || newPresaleSaleMode === 2) {
            formState.value.saleMode = newPresaleSaleMode;
        }
    }
);

// 新增：销售模式变化处理
const handleSaleModeChange = (value: number) => {
    // 只有普通商品才能设置预售
    if (formState.value.productType !== 1) {
        formState.value.saleMode = 0;
        formState.value.presaleConfig = undefined;
        return;
    }

    if (value === 0) {
        // 切换为现货，清空预售配置
        formState.value.saleMode = 0;
        formState.value.presaleConfig = undefined;
    } else if (value === 1) {
        // 切换为预售模式
        // 如果已有配置，使用 presaleConfig.saleMode 的值（1-全款预售或2-定金预售）
        // 如果没有配置，默认为全款预售（saleMode = 1）
        if (formState.value.presaleConfig && formState.value.presaleConfig.saleMode) {
            formState.value.saleMode = formState.value.presaleConfig.saleMode;
        } else {
            formState.value.saleMode = 1; // 默认全款预售
            formState.value.presaleConfig = {
                saleMode: 1, // 默认全款预售
                deliveryType: 1,
                deliveryRelativeDays: undefined,
                deliveryRelativeHours: undefined
            };
        }
    }
};
</script>
<style lang="less" scoped>
.presale-section {
    background: #f5f7fa;
    padding: 10px;
    border-radius: 4px;
    margin-top: 10px;
    margin-left: 112px; // 对齐 el-form-item 的 content 区域（label 宽度 100px + 间距 12px）
    margin-bottom: 30px;
}

.extra {
    color: #999;
    font-size: 12px;
    margin-top: 5px;
}

@media (max-width: 768px) {
    .presale-section {
        margin-left: 0;
        padding: 10px 5px;
    }
}
</style>
