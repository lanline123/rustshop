<template>
    <div class="container">
        <div class="content_wrapper">
            <a-spin :spinning="loading" style="width: 100%; margin-top: 100px">
                <div class="header">
                    <div class="header-left">
                        <Image :src="product?.picUrl" fit="contain" width="40" height="40" />
                        <span style="margin-left: 15px">{{ product?.productName }}</span>
                    </div>
                    <span style="color: #8a9099">{{ $t('门店：{shopName}', { shopName: shopInfo?.shopName }) }}</span>
                </div>
                <el-form v-if="!loading" ref="formRef" :model="formState" inline label-width="auto">
                    <el-form-item :label="$t('日期范围')" prop="dateRange" :rules="[{ required: true, message: '请选择时间范围' }]">
                        <div class="date-range-row">
                            <el-date-picker v-model="formState.dateRange" type="daterange" range-separator="-"
                                start-placeholder="开始日期" end-placeholder="结束日期"
                                class="date-range-picker" />
                            <el-checkbox v-model="isExcluded" :true-value="1" :false-value="0">{{ $t('排除时间') }}</el-checkbox>
                        </div>
                        <el-form-item v-if="isExcluded" prop="excludeType"
                            :rules="[{ required: true, message: '请选择排除类型' }]">
                            <el-radio-group v-model="formState.excludeType">
                                <el-radio :value="1" size="large">{{ $t('每周排除') }}</el-radio>
                                <el-radio :value="2" size="large">{{ $t('每月排除') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <el-form-item v-if="isExcluded" prop="excludeDay"
                            :rules="[{ required: true, message: '请选择排除时间' }]">
                            <el-select v-model="formState.excludeDay" placeholder="" class="exclude-day-select" multiple>
                                <el-option v-for="item in excludeList" :key="item.key" :label="item.label"
                                    :value="item.key" />
                            </el-select>
                        </el-form-item>
                        <div v-if="isExcluded">
                            <p style="color: #8a8099; margin-top: 15px">{{ $t('仅修改商品在改日期范围内的可预约数量') }}</p>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('可预约时段')" :rules="[{ required: true }]">
                        <a-spin :spinning="loading">
                            <el-table :data="inventoryList" border align="center">
                                <el-table-column :label="$t('时间段')" align="center" min-width="200px">
                                    <template #default="{ row }">
                                        <span>{{ `${row.startTime}~${row.endTime}` }}</span>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="stockChange" min-width="200px" align="center">
                                    <template #header>
                                        <div class="table-header">
                                            <span>{{ $t('增加 / 减少数量') }}</span>
                                            <el-tooltip effect="dark" :content="$t('正数为增加数量 负数为减少数量')" placement="top">
                                                <el-icon>
                                                    <QuestionFilled />
                                                </el-icon>
                                            </el-tooltip>
                                        </div>
                                    </template>
                                    <template #default="{ row }">
                                        <div class="input-wrapper">
                                            <TigInput width="60px" classType="tig-input" type="integer" allowNegative
                                                size="medium" v-model="row.quantity" placeholder="" />
                                        </div>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </a-spin>
                    </el-form-item>
                </el-form>
            </a-spin>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef, watch } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { getInventoryCalendarProductTimeList, setInventoryCalendarTimeSlotStock } from "@/api/product/inventoryCalendar";
import {
    ProductInventoryCalendarDetail,
    ProductTimeSlotStock,
    SetTimeSlotStockRequest,
    ShopSelectionDTO,
    DateExclusionDTO,
    TimeSlotStockItem
} from "@/types/product/inventoryCalendar.d";
import dayjs from "dayjs";
import { Image } from "@/components/image";
import { QuestionFilled } from "@element-plus/icons-vue";

// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    productInfo: {
        type: Object,
        required: true
    },
    shopInfo: {
        type: Object,
        required: true
    }
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const formRef = shallowRef();
const formState = ref<any>({
    dateRange: [],
    excludeType: 1,
    excludeDay: [],
    selectionType: 3,
    shopIds: props.shopInfo?.shopId
});

const inventoryList = ref<ProductTimeSlotStock[]>([]);
const isExcluded = ref<Boolean>(false);
const excludeList = ref<any>([]);
const product = ref<ProductInventoryCalendarDetail>({} as ProductInventoryCalendarDetail);

const getProductInfo = async () => {
    loading.value = true;
    try {
        const res: any = await getInventoryCalendarProductTimeList({ productId: props.productInfo?.productId });
        product.value = res;
        inventoryList.value = res.timeSlotStockList.map((item: ProductTimeSlotStock) => ({
            ...item,
            quantity: 0
        }));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    console.log(formState.value.shopId, props.shopInfo);
    await formRef.value.validate();

    // 处理入参数据结构
    const shopSelection: ShopSelectionDTO = {
        selectionType: formState.value.selectionType,
        shopIds: formState.value.shopIds
    };
    console.log(formState.value.shopIds, props.shopInfo);

    const dateExclusion: DateExclusionDTO = {};
    if (isExcluded.value) {
        if (formState.value.excludeType === 1) {
            dateExclusion.excludeWeekdays = formState.value.excludeDay;
        } else if (formState.value.excludeType === 2) {
            dateExclusion.excludeMonthDays = formState.value.excludeDay;
        }
    }

    const timeSlotStockItems: TimeSlotStockItem[] = inventoryList.value.map((item) => ({
        productReservationTimeId: item.productReservationTimeId,
        quantity: item.quantity || 0
    }));

    const payload: SetTimeSlotStockRequest = {
        productId: props.productInfo?.productId,
        skuId: props.productInfo?.skuId,
        shopSelection,
        startDate: dayjs(formState.value.dateRange?.[0]).format("YYYY-MM-DD"),
        endDate: dayjs(formState.value.dateRange?.[1]).format("YYYY-MM-DD"),
        dateExclusion: Object.keys(dateExclusion).length > 0 ? dateExclusion : undefined,
        timeSlotStockItems
    };

    try {
        emit("update:confirmLoading", true);
        const result = await setInventoryCalendarTimeSlotStock(payload);
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
onMounted(() => {
    getProductInfo();
});
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
</script>
<style lang="less" scoped>
.content_wrapper {
    :deep(.el-select) {
        min-width: 50px !important;
    }
}

.header {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    background-color: #f5f7fa;
    box-sizing: border-box;
    margin-bottom: 30px;

    .header-left {
        display: flex;
        align-items: center;
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

.table-header {
    display: flex;
    align-items: center;
    justify-content: center;

    span {
        margin-right: 5px;
    }
}

.input-wrapper {
    display: flex;
    justify-content: center;
}

@media only screen and (max-width: 767px) {
    .header {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
        margin-bottom: 16px;
        padding: 12px;

        .header-left {
            width: 100%;
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
        :deep(.el-form--inline) {
            display: flex;
            flex-direction: column;

            .el-form-item {
                margin-right: 0;
                width: 100%;
            }
        }

        :deep(.date-range-picker.el-date-editor) {
            width: 260px !important;
            max-width: 100%;
            margin-right: 0 !important;
        }

        :deep(.el-select) {
            width: 100% !important;
        }
    }
}
</style>
