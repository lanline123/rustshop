<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="tip">
                <el-icon color="#006aff" size="16px">
                    <InfoFilled />
                </el-icon>
                <span>{{ $t('批量设置库存即：同时对商品设置多个门店的库存') }}</span>
            </div>

            <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                <el-form-item :rules="[{ validator: validateProductId, trigger: 'change', required: true }]" :label="$t('商品')" prop="productId">
                    <div style="display: flex; align-items: center" v-if="formState.productId">
                        <Image :src="productInfo?.picUrl" fit="contain" width="40" height="40" />
                        <div style="margin-left: 15px">
                            <div>{{ productInfo?.productName }}</div>
                            <div v-if="selectedSkuSummary" style="color: #8a8099; font-size: 12px; margin-top: 4px">
                                {{ selectedSkuSummary }}
                            </div>
                        </div>
                    </div>
                    <DialogForm
                        :params="{ isMultiple: false, isSku: true, reservationProduct: 1, selectedIds: [] }"
                        isDrawer path="product/product/src/SelectProduct" :title="$t('选择商品')" width="600px" @okCallback="onOk"
                        :maskClose="false" :showFooter="true">
                        <el-button type="primary" link>{{ formState.productId ? $t('修改') : $t('选择商品') }}</el-button>
                    </DialogForm>
                </el-form-item>

                <el-form-item :label="$t('门店')">
                    <span v-if="!formState.productId" style="color: #8a8099">{{ $t('请先选择商品') }}</span>

                    <el-radio-group v-model="formState.selectionType" v-else style="width: 100%">
                        <el-radio :value="1" size="large">
                            <span>{{ $t('全部门店') }}</span>
                            <DialogForm dialogClass="noPadding" :maskClose="false"
                                :params="{ isSelect: true, selectedIds, distributionProductId: formState.productId }"
                                isDrawer path="product/product/store/OrganizePop" :title="$t('选择门店')" width="1100px"
                                @okCallback="changeSelectedId">
                                <el-button v-show="formState.selectionType === 1" type="primary" link>{{ $t('排除门店') }}</el-button>
                            </DialogForm>
                        </el-radio>
                        <el-radio :value="3" size="large">
                            <span>{{ $t('指定门店') }}</span>
                            <DialogForm dialogClass="noPadding" :maskClose="false"
                                :params="{ isSelect: true, selectedIds, distributionProductId: formState.productId }"
                                isDrawer path="product/product/store/OrganizePop" :title="$t('选择门店')" width="1100px"
                                @okCallback="changeSelectedId">
                                <el-button v-show="formState.selectionType === 3" type="primary" link>{{ $t('选择门店') }}</el-button>
                            </DialogForm>
                        </el-radio>
                    </el-radio-group>

                    <div class="stores-box" v-show="selectedIds.length">
                        <el-tag closable v-for="tag in selectedIds" :key="tag.shopId" @close="onRemoveStore(tag)">{{
                            tag.shopTitle }}</el-tag>
                    </div>
                </el-form-item>

                <el-form-item :label="$t('日期范围')" prop="dateRange" :rules="[{ required: true, message: '请选择时间范围' }]">
                    <div class="date-range-row">
                        <el-date-picker v-model="formState.dateRange" type="daterange" range-separator="-"
                            start-placeholder="开始日期" end-placeholder="结束日期" class="date-range-picker"
                            :disabled-date="disabledMonthDate" />
                        <el-checkbox v-model="isExcluded" :true-value="1" :false-value="0">{{ $t('排除时间') }}</el-checkbox>
                    </div>
                    <el-form-item v-if="isExcluded" prop="excludeType" :rules="[{ required: true, message: '请选择排除类型' }]"
                        style="width: 100%">
                        <el-radio-group v-model="formState.excludeType">
                            <el-radio :value="1" size="large">{{ $t('每周排除') }}</el-radio>
                            <el-radio :value="2" size="large">{{ $t('每月排除') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item v-if="isExcluded" prop="excludeDay" :rules="[{ required: true, message: '请选择排除时间' }]">
                        <el-select v-model="formState.excludeDay" placeholder="" class="exclude-day-select" multiple>
                            <el-option v-for="item in excludeList" :key="item.key" :label="item.label"
                                :value="item.key" />
                        </el-select>
                    </el-form-item>
                    <div v-if="isExcluded" class="mt10">
                        <p style="color: #8a8099">{{ $t('仅修改商品在改日期范围内的可预约数量') }}</p>
                    </div>
                </el-form-item>

                <el-form-item :label="$t('可预约数量')" :rules="[{ required: true, message: '请输入变更库存数量' }]" prop="quantity">
                    <span style="margin-right: 10px">{{ $t('所选商品在每个可预约时间段上同时增加/减少') }}</span>
                    <TigInput type="integer" width="60px" allowNegative size="medium" v-model="formState.quantity"
                        placeholder=""></TigInput>
                </el-form-item>
            </el-form>
            <!-- <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" /> -->
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, nextTick, onMounted, ref, shallowRef, watch } from "vue";
import { message } from "ant-design-vue";
import { setInventoryCalendarUnifiedStock } from "@/api/product/inventoryCalendar";
import { getProductList } from "@/api/product/product";
import { DialogForm } from "@/components/dialog";
import { InfoFilled } from "@element-plus/icons-vue";
import dayjs from "dayjs";
import {
    ShopSelectionDTO,
    DateExclusionDTO,
    ExcludeOption,
    ShopInfo,
    SetUnifiedStockRequest,
    UnifiedStockSelectedProduct
} from "@/types/product/inventoryCalendar.d";
import { Image } from "@/components/image";

// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "callback"]);
const props = defineProps<{
    /** 从库存日历列表勾选行打开时，预设商品ID（用于自动关联商品名） */
    presetProductId?: number;
    /** 从库存日历列表勾选行打开时，预设SKU */
    presetSkuIds?: number[];
}>();
const loading = ref<boolean>(true);
const formRef = shallowRef();
const formState = ref<any>({
    productId: 0,
    selectionType: 1,
    quantity: 0,
    dateRange: [],
    excludeType: 1,
    excludeDay: []
});

const selectedIds = ref<ShopInfo[]>([]);
const isExcluded = ref<boolean>(false);
const excludeList = ref<ExcludeOption[]>([]);
const productInfo = ref<any>({});
const selectedProduct = ref<UnifiedStockSelectedProduct | null>(null);

const selectedSkuIds = computed<number[]>(() => {
    if (!Array.isArray(selectedProduct.value?.skuIds)) {
        return [];
    }
    return selectedProduct.value.skuIds.map((id) => Number(id)).filter((id) => id > 0);
});

const selectedSkuId = computed<number>(() => selectedSkuIds.value[0] || 0);

const formatSkuText = (skuItem: any): string => {
    return typeof skuItem?.skuValue === "string" ? skuItem.skuValue : "";
};

const selectedSkuSummary = computed(() => {
    const skuList = Array.isArray(productInfo.value?.productSku) ? productInfo.value.productSku : [];
    if (selectedSkuIds.value.length > 0) {
        const selectedTexts = selectedSkuIds.value
            .map((id) => skuList.find((item: any) => Number(item?.skuId) === id))
            .filter(Boolean)
            .map((item: any) => formatSkuText(item))
            .filter(Boolean);
        if (selectedTexts.length > 0) {
            return t('已选规格：{join}', { join: selectedTexts.join("，") });
        }
        return t('已选{length}个规格', { length: selectedSkuIds.value.length });
    }
    if (!skuList.length) {
        return "";
    }
    if (skuList.length === 1) {
        return t('单规格商品');
    }
    return t('请选择规格');
});

const syncSelectedSkuIds = () => {
    const skuList = Array.isArray(productInfo.value?.productSku) ? productInfo.value.productSku : [];
    if (!skuList.length) {
        if (selectedProduct.value) {
            selectedProduct.value.skuIds = [];
        }
        return;
    }
    const availableSkuIds = skuList.map((item: any) => Number(item?.skuId)).filter((id: number) => id > 0);
    const matchedSkuIds = selectedSkuIds.value.filter((id) => availableSkuIds.includes(id));
    if (selectedProduct.value) {
        selectedProduct.value.skuIds = matchedSkuIds.length > 0 ? matchedSkuIds : skuList.length === 1 ? [availableSkuIds[0]] : [];
    }
};

// 校验商品：必须已选择有效商品ID
const validateProductId = (_rule: unknown, value: number, callback: (error?: Error) => void) => {
    if (Number(value) > 0) {
        callback();
        return;
    }
    callback(new Error(t('请选择商品')));
};

const clearProductValidate = async () => {
    await nextTick();
    formRef.value?.clearValidate?.("productId");
};

// 禁用当天之前的日期
const disabledMonthDate = (date: any) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0); // 设置为当天0点
    return date.getTime() < today.getTime();
};

const changeSelectedId = (ids: ShopInfo[]) => {
    selectedIds.value = ids;
    console.log(selectedIds.value);
};

const onRemoveStore = (tag: ShopInfo): void => {
    selectedIds.value = selectedIds.value.filter((item: ShopInfo) => item.shopId !== tag.shopId);
};

const onOk = (result: Array<number | UnifiedStockSelectedProduct>) => {
    const firstSelected = result && result.length ? result[0] : null;
    const normalizedSelection =
        typeof firstSelected === "number"
            ? {
                  productId: firstSelected,
                  skuIds: []
              }
            : {
                  productId: Number(firstSelected?.productId || 0),
                  skuIds: Array.isArray(firstSelected?.skuIds)
                      ? firstSelected.skuIds.map((id) => Number(id)).filter((id) => id > 0)
                      : []
              };
    selectedProduct.value = normalizedSelection?.productId ? normalizedSelection : null;
    formState.value.productId = normalizedSelection?.productId || 0;
    emit("callback", {
        type: "productChange",
        productId: formState.value.productId,
        skuIds: selectedProduct.value?.skuIds || [],
        skuId: selectedSkuId.value
    });
    if (formState.value.productId) {
        clearProductValidate();
    }
    //获取商品数据渲染商品信息
    getProductInfo();
    //门店列表置空
    selectedIds.value = [];
};

const getProductInfo = async () => {
    // loading.value = true;
    try {
        const result = await getProductList({ ids: formState.value.productId });
        productInfo.value = result.records[0];
        syncSelectedSkuIds();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }

};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    if (Array.isArray(productInfo.value?.productSku) && productInfo.value.productSku.length > 1 && selectedSkuIds.value.length === 0) {
        message.error(t('请选择规格'));
        return;
    }
    //处理接口传参
    const shopIds: number[] = selectedIds.value.map((item: ShopInfo) => item.shopId).filter((id: number) => id !== undefined);

    const excludeWeekdays: number[] = isExcluded.value && formState.value.excludeType === 1 && formState.value.excludeDay ? formState.value.excludeDay : [];
    const excludeMonthDays: number[] = isExcluded.value && formState.value.excludeType === 2 && formState.value.excludeDay ? formState.value.excludeDay : [];
    const excludeSpecificDates: string[] = [];

    // 如果选择全部门店但有排除门店，则改为排除模式
    if (formState.value.selectionType === 1 && selectedIds.value.length) {
        formState.value.selectionType = 2;
    }

    const shopSelection: ShopSelectionDTO = {
        selectionType: formState.value.selectionType,
        shopIds
    };

    const dateExclusion: DateExclusionDTO = {
        excludeWeekdays,
        excludeMonthDays,
        excludeSpecificDates
    };

    const payload: SetUnifiedStockRequest = {
        productId: formState.value.productId,
        skuId: selectedSkuId.value || undefined,
        shopSelection,
        startDate: dayjs(formState.value.dateRange[0]).format("YYYY-MM-DD"),
        endDate: dayjs(formState.value.dateRange[1]).format("YYYY-MM-DD"),
        dateExclusion,
        quantity: Number(formState.value.quantity)
    };

    try {
        emit("update:confirmLoading", true);
        const result = await setInventoryCalendarUnifiedStock(payload);
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

const buildExcludeList = () => {
    if (formState.value.excludeType === 1) {
        //周列表映射
        excludeList.value = [
            {
                key: 1,
                label: t('周一')
            },
            {
                key: 2,
                label: t('周二')
            },
            {
                key: 3,
                label: t('周三')
            },
            {
                key: 4,
                label: t('周四')
            },
            {
                key: 5,
                label: t('周五')
            },
            {
                key: 6,
                label: t('周六')
            },
            {
                key: 7,
                label: t('周日')
            }
        ];
    } else if (formState.value.excludeType === 2) {
        // 月列表映射
        excludeList.value = Array.from({ length: 31 }, (_, i) => {
            return { key: i + 1, label: t('{i1}号', { i1: i + 1 }) };
        });
    } else {
        excludeList.value = [];
    }
};

watch(
    () => formState.value.excludeType,
    () => {
        // 切换排除类型时，清空已选门店与排除项，并重建列表
        // selectedIds.value = [];
        formState.value.excludeDay = [];
        buildExcludeList();
    },
    { immediate: true }
);

watch(isExcluded, (val) => {
    if (!val) {
        // 关闭排除时间时清空相关选择
        formState.value.excludeDay = [];
        excludeList.value = [];
    } else {
        buildExcludeList();
    }
});
watch(
    () => formState.value.productId,
    (val) => {
        if (Number(val) > 0) {
            clearProductValidate();
        }
    }
);
watch(
    () => formState.value.selectionType,
    () => {
        selectedIds.value = [];
    }
);
onMounted(() => {
    // 如果从列表已勾选商品打开，则自动回填商品（显示商品名/图片），用户仍可点“修改”重新选择
    const pid = Number(props.presetProductId || 0);
    if (pid > 0) {
        loading.value = true;
        const presetSkuIds = Array.isArray(props.presetSkuIds) ? props.presetSkuIds.map((id) => Number(id)).filter((id) => id > 0) : [];
        selectedProduct.value = {
            productId: pid,
            skuIds: presetSkuIds
        };
        formState.value.productId = pid;
        getProductInfo();
    } else {
        loading.value = false;
    }
});
</script>
<style lang="less" scoped>
.content_wrapper {
    :deep(.el-select) {
        min-width: 50px !important;
    }
}

.stores-box {
    width: 100%;
    background-color: #f5f7fa;
    padding: 10px 15px;

    .el-tag {
        margin-right: 8px;
    }
}

.tip {
    background-color: #f2f7ff;
    border: 1px solid #bfdaff;
    padding: 5px 10px;
    margin-bottom: 10px;
    display: flex;
    align-items: center;

    span {
        margin-left: 5px;
    }
}

.date-range-row {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    width: 100%;
}

.date-range-picker {
    width: 50%;
    margin-right: 10px;
}

.exclude-day-select {
    width: 240px;
}

@media only screen and (max-width: 767px) {
    .tip {
        align-items: flex-start;

        span {
            line-height: 1.4;
        }
    }

    .date-range-row {
        flex-direction: column;
        align-items: flex-start;
    }

    .date-range-picker {
        width: 260px !important;
        max-width: 100%;
        margin-right: 0;
    }

    .exclude-day-select {
        width: 100%;
    }

    .content_wrapper {
        :deep(.el-form-item) {
            flex-direction: column;
            align-items: flex-start;
        }

        :deep(.el-form-item__content) {
            width: 100%;
        }

        :deep(.el-radio-group) {
            flex-wrap: wrap;
        }
    }
}
</style>
