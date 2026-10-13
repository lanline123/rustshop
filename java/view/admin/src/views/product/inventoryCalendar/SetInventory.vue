<template>
    <div class="container">
        <div class="content_wrapper">
            <a-spin :spinning="loading">
                <el-table :data="inventoryList" border align="center">
                    <el-table-column :label="$t('时间段')" align="center">
                        <template #default="{ row }">
                            <span>{{ `${row.startTime}~${row.endTime}` }}</span>
                        </template>
                    </el-table-column>

                    <el-table-column v-if="type === 'edit'" :label="$t('当前可预约')" prop="availableStock" align="center">
                    </el-table-column>
                    <el-table-column prop="stockChange" align="center">
                        <template #header>
                            <div class="table-header">
                                <span>{{ type === "edit" ? $t('增加/减少数量') : $t('可预约数量') }}</span>
                                <el-tooltip v-if="type === 'edit'" effect="dark" :content="$t('正数为增加数量 负数为减少数量')"
                                    placement="top">
                                    <el-icon>
                                        <QuestionFilled />
                                    </el-icon>
                                </el-tooltip>
                            </div>
                        </template>
                        <template #default="{ row }">
                            <div class="input-wrapper">
                                <TigInput width="60px" classType="tig-input" type="integer"
                                    :allowNegative="type === 'edit'" size="medium" v-model="row.stockChange"
                                    placeholder="" :disabled="isTimeSlotDisabled(row)" @input="onStockChange(row)" />
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column align="center" :label="$t('实际可预约')" v-if="type === 'edit'" prop="usedStock">
                        <!-- <template #default="{ row }">
                            <span>{{ Number(row?.availableStock) + Number(row?.stockChange) }}</span>
                        </template> -->
                    </el-table-column>
                </el-table>
            </a-spin>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { QuestionFilled } from "@element-plus/icons-vue";
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { getInventoryCalendarDailyStock, updateInventoryCalendarDailyStock } from "@/api/product/inventoryCalendar";
import {
    InventoryCalendarFilterDailyStockState,
    InventoryCalendarFilterDailyStockResult,
    InventoryCalendarFilterDailyStockRequest
} from "@/types/product/inventoryCalendar";
import dayjs from "dayjs";

// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits([
    "submitCallback", //确认后回调
    "update:confirmLoading", //确认按钮的loading状态
    "close" //关闭弹窗
]);
const props = defineProps({
    type: {
        type: String,
        default: "set"
    },
    productId: {
        type: Number,
        required: true
    },
    skuId: {
        type: Number,
        required: true
    },
    shopId: {
        type: Number,
        required: true
    },
    stockDate: {
        type: String,
        required: true
    }
});
const loading = ref<boolean>(true);
const inventoryList = ref<InventoryCalendarFilterDailyStockState[]>([]);
const filterParams = ref<InventoryCalendarFilterDailyStockRequest>({
    productId: props?.productId,
    skuId: props?.skuId,
    shopId: props?.shopId,
    stockDate: props?.stockDate
});
const requestParam = ref<object>({});

// 判断时间段是否应该禁用
const isTimeSlotDisabled = (row: InventoryCalendarFilterDailyStockState) => {
    const now = new Date();
    const currentDate = dayjs(now).format("YYYY-MM-DD");
    const currentTime = now.getHours() * 60 + now.getMinutes();
    // 如果日期相同，则比较时间
    if (currentDate === props.stockDate) {
        // 解析开始时间和结束时间
        const startTimeParts = row.startTime.split(":");
        const endTimeParts = row.endTime.split(":");
        const startTimeMinutes = parseInt(startTimeParts[0]) * 60 + parseInt(startTimeParts[1]);
        const endTimeMinutes = parseInt(endTimeParts[0]) * 60 + parseInt(endTimeParts[1]);

        // 如果当前时间已经超过了时间段的开始时间，则禁用
        return currentTime >= endTimeMinutes;
    }

    // 如果当前日期小于设置的日期，则不禁用
    return false;
};

const loadFilter = async () => {
    loading.value = true;
    try {
        const res = await getInventoryCalendarDailyStock({ ...filterParams.value });
        const { timeSlotStockList, ...otherData } = res;
        requestParam.value = otherData;
        inventoryList.value = res.timeSlotStockList.map((i: InventoryCalendarFilterDailyStockState) => ({
            ...i,
            stockChange: 0,
            usedStock: i.availableStock
        }));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

// 库存变化
const onStockChange = (row: InventoryCalendarFilterDailyStockState) => {
    // 在输入负号时保持实际可预约不变
    if (typeof row.stockChange === "string" && row.stockChange === "-") {
        return;
    }
    //输入负数时,如果超出了当前可预约数,则保持实际可预约数为0
    if (Number(row.stockChange) < 0 && Math.abs(Number(row.stockChange)) > Number(row.availableStock)) {
        return row.usedStock = 0;
    }
    //输入数字时，实际可预约数等于当前可预约数加上输入的数字
    row.usedStock = Number(row.availableStock) + Number(row.stockChange);

};

// 表单通过验证后提交
const onSubmit = async () => {
    try {
        emit("update:confirmLoading", true);
        Object.assign(requestParam.value, {
            timeSlotStockChanges: inventoryList.value.map((i: InventoryCalendarFilterDailyStockState) => {
                const { usedStock, ...rest } = i;
                return {
                    ...rest,
                    stockChange: Number(i.stockChange)
                };
            })
        });
        const result = await updateInventoryCalendarDailyStock(requestParam.value);
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

loadFilter();

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.content_wrapper {
    :deep(.el-select) {
        min-width: 50px !important;
    }
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
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;

    .disabled-tip {
        position: absolute;
        top: -20px;
        right: 0;
        font-size: 12px;
        color: #f56c6c;
        background: #fef0f0;
        padding: 2px 6px;
        border-radius: 4px;
        border: 1px solid #fbc4c4;
        white-space: nowrap;
    }
}

@media only screen and (max-width: 767px) {
    .content_wrapper {
        overflow-x: auto;
    }

    .input-wrapper {
        .disabled-tip {
            position: static;
            margin-top: 4px;
            white-space: normal;
        }
    }
}
</style>
