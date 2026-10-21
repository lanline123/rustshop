<template>
    <el-form-item :label="$t('配送方式')" :rules="[{ required: true, message: '请选择配送方式' }]" prop="noShipping">
        <el-radio-group v-model="formState.noShipping" style="width: 100%; margin-bottom: 10px">
            <el-radio :value="1">{{ $t('无需配送') }}</el-radio>
        </el-radio-group>
    </el-form-item>

    <el-form-item v-if="formState.deliveryMode === 1" :label="$t('预约模板')" prop="reservationTplId" :rules="[{ required: true, message: '请选择预约模板' }]">
        <el-select v-model="formState.reservationTplId" :placeholder="$t('请选择预约模板')" style="width: 250px">
            <el-option v-for="item in tplOptions" :key="item?.reservationTplId" :label="item?.templateName" :value="item?.reservationTplId" />
        </el-select>
        <DialogForm
            isDrawer
            :params="reservationTplDialogParams"
            path="setting/appointmentTpl/Info"
            :title="reservationTplDialogTitle"
            width="900px"
            @okCallback="handleReservationTplSaved"
        >
            <el-button type="primary" link class="ml5">{{ $t('去设置') }}</el-button>
        </DialogForm>
        <p class="ml10 mr10" style="margin-bottom: 3px">|</p>
        <el-button :loading="tplLoading" type="primary" link @click="getTplOptions">{{ $t('刷新') }}</el-button>
    </el-form-item>
    <el-form-item :label="$t('预约时间段')" prop="reservationTimes" :rules="timeRangeRules">
        <div class="time-period" v-for="(item, index) in reservationTimes" :key="index">
            <div class="time-picker-row">
                <div class="time-picker-wrapper">
                    <el-time-picker
                        format="HH:mm"
                        value-format="HH:mm"
                        :automatic-dropdown="false"
                        style="width: 110px"
                        v-model="item.startTime"
                        :placeholder="$t('开始时间')"
                        :class="{ 'is-error': getStartTimeError(index) }"
                        @change="validateTimeRange(index)"
                    />
                </div>
                <div class="time-separator">-</div>
                <div class="time-picker-wrapper">
                    <el-time-picker
                        format="HH:mm"
                        value-format="HH:mm"
                        :automatic-dropdown="false"
                        style="width: 110px"
                        v-model="item.endTime"
                        :placeholder="$t('结束时间')"
                        :class="{ 'is-error': getEndTimeError(index) }"
                        @change="validateTimeRange(index)"
                    />
                </div>
                <el-icon color="#8a9099" @click="removeTimeRange(index)" class="remove-icon">
                    <RemoveFilled />
                </el-icon>
            </div>
            <div class="error-messages-row">
                <div v-if="getStartTimeError(index)" class="error-message">{{ getStartTimeError(index) }}</div>
                <div v-if="getEndTimeError(index)" class="error-message">{{ getEndTimeError(index) }}</div>
            </div>
        </div>
        <div class="add-tip"><el-button type="primary" link @click="addTimeRange">{{ $t('+添加时间段') }}</el-button><span>{{ $t('最多添加10个时间段') }}</span></div>
    </el-form-item>
    <el-form-item label=" ">
        <div style="color: #8a9099; width: 100%">{{ $t('请前往商品>库存管理>库存日历 设置库存') }}</div>
    </el-form-item>
</template>
<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { ProductFormState, TimePeriod } from "@/types/product/product.d";
import { RemoveFilled } from "@element-plus/icons-vue";
import { getReservationTplAll } from "@/api/setting/appointmentTpl";
import { ReservationTplFilterState } from "@/types/setting/appointmentTpl.d";
import { DialogForm } from "@/components/dialog";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const formState = defineModel<ProductFormState>("formState", { default: {} });
const reservationTimes = ref([
    {
        startTime: "",
        endTime: ""
    }
]);
const tplOptions = ref<ReservationTplFilterState[]>([]);
const tplLoading = ref(false);
const timeErrors = ref<{ [key: number]: { startTime?: string; endTime?: string } }>({});
const reservationTplDialogParams = { act: "add" };
const reservationTplDialogTitle = t('添加预约模板');

// 表单校验规则
const timeRangeRules = [
    {
        required: true,
        trigger: "blur",
        validator: (rule: any, value: any, callback: any) => {
            // 检查是否至少有一个时间段
            if (!reservationTimes.value || reservationTimes.value.length === 0) {
                console.log(111, "cuowu");

                callback(new Error(t('请至少添加一个时间段')));
                return;
            }
            // 检查是否至少有一个完整的时间段（开始时间和结束时间都不为空）
            const hasValidTimeRange = reservationTimes.value.some((item: any) => item.startTime && item.endTime);
            if (!hasValidTimeRange) {
                callback(new Error(t('请至少配置一个完整的时间段')));
                return;
            }
            // 检查时间段校验错误
            const errors = validateAllTimeRanges();
            if (errors.length > 0) {
                callback(new Error(t('请根据提示完成时间段调整')));
                return;
            }
            callback();
        }
    }
];

//新增时间段
const addTimeRange = () => {
    if (reservationTimes.value.length === 10) {
        return message.error(t('最多只能添加10个时间段'));
    }

    reservationTimes.value.push({ startTime: "", endTime: "" });
};
//删除时间段
const removeTimeRange = (index: number) => {
    if (reservationTimes.value.length === 1) {
        return message.error(t('请至少保留配置一个时间段'));
    }
    reservationTimes.value.splice(index, 1);
    // 清除对应的错误信息
    delete timeErrors.value[index];
    // 重新校验剩余的时间段
    validateAllTimeRanges();
};

// 校验单个时间段
const validateTimeRange = (index: number) => {
    const current = reservationTimes.value[index];
    if (!current.startTime || !current.endTime) {
        timeErrors.value[index] = {};
        // 更新表单数据
        formState.value.reservationTimes = reservationTimes.value;
        return;
    }

    const errors: { startTime?: string; endTime?: string } = {};

    // 1. 开始时间不能大于结束时间
    if (current.startTime >= current.endTime) {
        errors.startTime = t('开始时间不能大于或等于结束时间');
        errors.endTime = t('结束时间不能小于或等于开始时间');
    }

    // 2. 检查与其他时间段是否重复
    for (let i = 0; i < reservationTimes.value.length; i++) {
        if (i === index) continue;
        const other = reservationTimes.value[i];
        if (!other.startTime || !other.endTime) continue;

        // 检查时间段是否重叠
        if (isTimeOverlap(current.startTime, current.endTime, other.startTime, other.endTime)) {
            errors.startTime = errors.startTime || t('时间段不能重复');
            errors.endTime = errors.endTime || t('时间段不能重复');
        }
    }

    // 3. 第二条时间段不能小于第一条时间段
    if (index > 0) {
        const prev = reservationTimes.value[index - 1];
        if (prev.startTime && prev.endTime && current.startTime) {
            if (current.startTime < prev.endTime) {
                errors.startTime = errors.startTime || t('时间段不能早于前一个时间段的结束时间');
            }
        }
    }

    timeErrors.value[index] = errors;

    // 更新表单数据
    formState.value.reservationTimes = reservationTimes.value;

    // 重新校验所有时间段，确保其他时间段也更新
    validateAllTimeRanges();
};

// 校验所有时间段
const validateAllTimeRanges = () => {
    const allErrors: string[] = [];
    reservationTimes.value.forEach((_, index) => {
        // 只校验，不触发change事件
        const current = reservationTimes.value[index];
        if (!current.startTime || !current.endTime) {
            timeErrors.value[index] = {};
            return;
        }

        const errors: { startTime?: string; endTime?: string } = {};

        // 1. 开始时间不能大于结束时间
        if (current.startTime >= current.endTime) {
            errors.startTime = t('开始时间不能大于或等于结束时间');
            errors.endTime = t('结束时间不能小于或等于开始时间');
        }

        // 2. 检查与其他时间段是否重复
        for (let i = 0; i < reservationTimes.value.length; i++) {
            if (i === index) continue;
            const other = reservationTimes.value[i];
            if (!other.startTime || !other.endTime) continue;

            // 检查时间段是否重叠
            if (isTimeOverlap(current.startTime, current.endTime, other.startTime, other.endTime)) {
                errors.startTime = errors.startTime || t('时间段不能重复');
                errors.endTime = errors.endTime || t('时间段不能重复');
            }
        }

        // 3. 第二条时间段不能小于第一条时间段
        if (index > 0) {
            const prev = reservationTimes.value[index - 1];
            if (prev.startTime && prev.endTime && current.startTime) {
                if (current.startTime < prev.endTime) {
                    errors.startTime = errors.startTime || t('时间段不能早于前一个时间段的结束时间');
                }
            }
        }

        timeErrors.value[index] = errors;

        // 收集错误信息
        if (errors.startTime) {
            allErrors.push(t('第{index1}个时间段开始时间: {startTime}', { index1: index + 1, startTime: errors.startTime }));
        }
        if (errors.endTime) {
            allErrors.push(t('第{index1}个时间段结束时间: {endTime}', { index1: index + 1, endTime: errors.endTime }));
        }
    });
    return allErrors;
};

// 检查时间段是否重叠
const isTimeOverlap = (start1: string, end1: string, start2: string, end2: string) => {
    return start1 < end2 && start2 < end1;
};

// 获取开始时间错误信息
const getStartTimeError = (index: number) => {
    const current = reservationTimes.value[index];
    // 如果开始时间或结束时间为空，不显示错误
    if (!current.startTime || !current.endTime) {
        return "";
    }
    return timeErrors.value[index]?.startTime;
};

const handleReservationTplSaved = async () => {
    await getTplOptions();
};

// 获取结束时间错误信息
const getEndTimeError = (index: number) => {
    const current = reservationTimes.value[index];
    // 如果开始时间或结束时间为空，不显示错误
    if (!current.startTime || !current.endTime) {
        return "";
    }
    return timeErrors.value[index]?.endTime;
};

//获取预约模板
const getTplOptions = async () => {
    tplLoading.value = true;
    try {
        //如果不是自营商品,要传shopId查询
        const shopId = Number(localStorage.getItem("shopId"));
        const parseTplOptions = (result: any): ReservationTplFilterState[] => {
            if (Array.isArray(result)) return result;
            if (Array.isArray(result?.records)) return result.records;
            if (Array.isArray(result?.data)) return result.data;
            return [];
        };
        if (shopId > 0) {
            const res = await getReservationTplAll({ shopId: shopId });
            tplOptions.value = parseTplOptions(res);
        } else {
            const res = await getReservationTplAll();
            tplOptions.value = parseTplOptions(res);
        }
    } finally {
        setTimeout(() => {
            tplLoading.value = false;
        }, 200);
    }
};

getTplOptions();

// 数据回显
const initData = () => {
    console.log("初始化数据回显:", formState.value);
    // 回显时间段数据
    if (formState.value.reservationTimes && formState.value.reservationTimes.length > 0) {
        reservationTimes.value = formState.value.reservationTimes;
    }
};

// 监听formState变化，实现数据回显
watch(
    () => formState.value,
    (newVal: ProductFormState) => {
        if (newVal && newVal.reservationTimes) {
            reservationTimes.value = newVal.reservationTimes;
            validateAllTimeRanges();
        }
    },
    { deep: true, immediate: true }
);

onMounted(() => {
    initData();
});
</script>
<style lang="less" scoped>
.time-period {
    width: 100%;
    margin-bottom: 15px;
}

.time-picker-row {
    display: flex;
    align-items: center;
    margin-bottom: 4px;
}

.time-picker-wrapper {
    display: flex;
    align-items: center;
}

.time-separator {
    margin: 0 10px;
    color: #606266;
    font-size: 14px;
}

.remove-icon {
    margin-left: 10px;
    cursor: pointer;
}

.error-messages-row {
    display: flex;
    align-items: flex-start;
    margin-left: 0;
}

.error-message {
    color: #f56c6c;
    font-size: 12px;
    line-height: 1.2;
    margin-right: 20px;
    max-width: 110px;
}

// 表单校验错误样式
:deep(.el-time-picker.is-error) {
    .el-input__wrapper {
        border-color: #f56c6c !important;
        box-shadow: 0 0 0 1px #f56c6c inset !important;
    }
}

// 覆盖 Element Plus 默认的表单校验红框，只让各 time-picker 自身的 .is-error 控制
:deep(.time-picker-row) {
    .el-input__wrapper {
        border-color: var(--el-input-border-color, #dcdfe6) !important;
        box-shadow: 0 0 0 1px var(--el-input-border-color, #dcdfe6) inset !important;
    }
    .time-picker-wrapper{
        .is-error{
            .el-input__wrapper {
                border-color: #f56c6c !important;
                box-shadow: 0 0 0 1px #f56c6c inset !important;
            }
        }
    }
}

.add-tip {
    display: flex;
    align-items: center;

    span {
        margin-left: 10px;
    }
}
</style>
