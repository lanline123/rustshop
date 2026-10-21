<template>
    <el-form :model="formState" :rules="rules" ref="formRef" label-width="130px">
        <div class="formTitle">{{ $t('基本信息') }}</div>
        <el-form-item :label="$t('活动名称')" prop="activityName">
            <TigInput v-model="formState.activityName" style="width: 500px" :placeholder="$t('请输入活动名称')" />
        </el-form-item>
        <el-form-item :label="$t('活动时间')" prop="timeRange" required>
            <div class="flex flex-align-center">
                <SelectTimeInterval
                    v-model:end-date="formState.endTime"
                    v-model:start-date="formState.startTime"
                    clearable
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    :disable-before-today="true"
                    @callback="changeDateType(-1)"
                >
                </SelectTimeInterval>
                <el-radio-group class="itemWidth" v-model="dateType" @change="changeDateType">
                    <el-radio-button :value="0">{{ $t('1天') }}</el-radio-button>
                    <el-radio-button :value="1">{{ $t('7天') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('15天') }}</el-radio-button>
                </el-radio-group>
            </div>
        </el-form-item>
        <el-form-item :label="$t('中奖次数')" prop="winLimitType" class="formItem">
            <el-radio-group class="formRadio" v-model="formState.winLimitType">
                <el-radio :label="0">{{ $t('无限制') }}</el-radio>
                <el-radio :label="1" class="flex flex-align-center">
                    <div class="flex flex-align-center" style="position: relative">
                        <span>{{ $t('每人最多抽中') }}</span>
                        <TigInput v-model="formState.winLimit" type="integer" :min="1" :disabled="formState.winLimitType !== 1" style="width: 100px; margin: 0 10px" />
                        <span>{{ $t('次') }}</span>
                        <div style="position: absolute; top: 30px; left: 0; font-size: 12px; color: #9598a6"> {{ $t('每位用户有中奖次数上限，一旦达到上限，后续抽奖将不再中奖') }} </div>
                    </div>
                </el-radio>
            </el-radio-group>
        </el-form-item>
        <div class="formTitle">{{ $t('抽奖次数限制') }}</div>
        <el-form-item :label="$t('每人基础抽奖次数')" class="formItem" prop="drawLimitType">
            <el-radio-group class="formRadio" v-model="formState.drawLimitType">
                <el-radio :label="0">{{ $t('无限制') }}</el-radio>
                <el-radio :label="1" class="flex flex-align-center radioRight">
                    <div class="limit-item">
                        <span class="label">{{ $t('每日最大抽奖次数') }}</span>
                        <TigInput v-model="formState.dailyLimit" type="integer" :min="1" :disabled="formState.drawLimitType !== 1" style="width: 100px" />
                    </div>
                    <div class="limit-item ml10">
                        <span class="label">{{ $t('活动时间内最大抽奖次数') }}</span>
                        <TigInput v-model="formState.totalLimit" type="integer" :min="1" :disabled="formState.drawLimitType !== 1" style="width: 100px" />
                    </div>
                </el-radio>
            </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('基础抽奖参与条件')" class="formItem" prop="costPoints">
            <el-radio-group class="formRadio" v-model="formState.isConsumePoints">
                <el-radio :label="0">{{ $t('无限制') }}</el-radio>
                <el-radio :label="1" class="flex flex-align-center">
                    <div class="flex flex-align-center limit-item">
                        <span style="color: #000" class="label">{{ $t('每次抽奖消耗') }}</span>
                        <TigInput
                            v-model="formState.costPoints"
                            type="integer"
                            :min="1"
                            :disabled="formState.isConsumePoints !== 1"
                            style="width: 100px"
                        />
                        <span style="color: #000" class="ml10">{{ $t('积分') }}</span>
                    </div>
                </el-radio>
            </el-radio-group>
        </el-form-item>
    </el-form>
</template>

<script setup lang="ts">
import { ref, watch, reactive, nextTick } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { LotteryFormState } from "@/types/promotion/luckyWheel.d";
import { SelectTimeInterval } from "@/components/select";
import { formattedDate } from "@/utils/time";
import { getDays } from "@/utils/util";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const formState = defineModel<LotteryFormState>({ required: true });

const formRef = ref<FormInstance>();

const dateType = ref(-1);

const changeDateType = (event: number) => {
    // 任何触发 changeDateType 的场景都先清掉“活动时间”校验提示（避免错误残留）
    nextTick(() => formRef.value?.clearValidate?.(["timeRange"]));
    if (event === -1) {
        dateType.value = event;
        return;
    }
    formState.value.startTime = formattedDate(new Date(), "YYYY-MM-DD HH:mm:ss");
    if (event === 0) {
        formState.value.endTime = formattedDate(getDays(1, "add"), "YYYY-MM-DD HH:mm:ss");
    }
    if (event === 1) {
        formState.value.endTime = formattedDate(getDays(7, "add"), "YYYY-MM-DD HH:mm:ss");
    }
    if (event === 2) {
        formState.value.endTime = formattedDate(getDays(15, "add"), "YYYY-MM-DD HH:mm:ss");
    }
};

watch(
    () => formState.value.isConsumePoints,
    (val) => {
        // “消耗积分时默认显示 1；无限制默认显示空”
        if (val === 1) {
            const points = Number(formState.value.costPoints);
            if (!points || points <= 0) formState.value.costPoints = 1 as any;
        } else {
            formState.value.costPoints = "" as any;
            formRef.value?.clearValidate?.(["costPoints"]);
        }
    },
    { immediate: true }
);

const validActivityTime = (rule: any, value: any, callback: any) => {
    if (!formState.value.startTime && !formState.value.endTime) {
        callback(new Error(t('请输入活动时间')));
        return;
    }
    callback();
};

const validDrawLimit = (rule: any, value: any, callback: any) => {
    if (formState.value.drawLimitType === 1) {
        if (!formState.value.dailyLimit || formState.value.dailyLimit <= 0) {
            callback(new Error(t('请输入每日最大抽奖次数')));
            return;
        }
        if (!formState.value.totalLimit || formState.value.totalLimit <= 0) {
            callback(new Error(t('请输入活动最大抽奖次数')));
            return;
        }
        if (+formState.value.dailyLimit > +formState.value.totalLimit) {
            callback(new Error(t('每日最大抽奖次数不能大于活动最大抽奖次数')));
            return;
        }
    }
    callback();
};

const validWinLimit = (rule: any, value: any, callback: any) => {
    if (formState.value.winLimitType === 1) {
        const limit = Number(formState.value.winLimit);
        if (!limit || limit <= 0) {
            callback(new Error(t('请输入中奖次数，且必须大于0')));
            return;
        }
    }
    callback();
};

const validConsumePoints = (rule: any, value: any, callback: any) => {
    // 未勾选时不校验
    if (formState.value.isConsumePoints !== 1) return callback();

    const points = Number(formState.value.costPoints);
    if (!points || points <= 0) {
        callback(new Error(t('每次抽奖消耗积分必须大于0')));
        return;
    }
    callback();
};

const rules = reactive<FormRules>({
    activityName: [{ required: true, message: t('请输入活动名称'), trigger: "blur" }],
    timeRange: [{ required: true, validator: validActivityTime, trigger: "blur" }],
    drawLimitType: [{ validator: validDrawLimit, trigger: ["change", "blur"] }],
    winLimitType: [{ validator: validWinLimit, trigger: ["change", "blur"] }],
    costPoints: [{ validator: validConsumePoints, trigger: ["change", "blur"] }]
});

watch(
    () => [formState.value.drawLimitType, formState.value.isConsumePoints, formState.value.costPoints],
    () => {
        // 勾选时才校验，避免默认空显示报错
        if (formState.value.isConsumePoints === 1) {
            formRef.value?.validateField?.("costPoints").catch(() => void 0);
        }
    }
);

// “默认显示空；选择后才默认显示 1”
watch(
    () => formState.value.winLimitType,
    (val) => {
        if (val === 1) {
            const limit = Number(formState.value.winLimit);
            if (!limit || limit <= 0) formState.value.winLimit = 1 as any;
        } else {
            formState.value.winLimit = "" as any;
        }
    },
    { immediate: true }
);

watch(
    () => formState.value.drawLimitType,
    (val) => {
        if (val === 1) {
            const daily = Number(formState.value.dailyLimit);
            const total = Number(formState.value.totalLimit);
            if (!daily || daily <= 0) formState.value.dailyLimit = 1 as any;
            if (!total || total <= 0) formState.value.totalLimit = 1 as any;
        } else {
            formState.value.dailyLimit = "" as any;
            formState.value.totalLimit = "" as any;
        }
    },
    { immediate: true }
);

watch(
    () => [formState.value.drawLimitType, formState.value.dailyLimit, formState.value.totalLimit],
    () => {
        // dailyLimit/totalLimit 变化时触发 drawLimitType 校验
        formRef.value?.validateField?.("drawLimitType").catch(() => void 0);
    }
);

watch(
    () => [formState.value.winLimitType, formState.value.winLimit],
    () => {
        formRef.value?.validateField?.("winLimitType").catch(() => void 0);
    }
);

const validate = async () => {
    if (!formRef.value) return false;
    return await formRef.value.validate();
};

defineExpose({ validate });
</script>

<style scoped lang="less">
.limit-item {
    display: flex;
    align-items: center;
}
.limit-item .label {
    margin-right: 10px;
}
.limit-item .tip {
    color: #999;
    font-size: 12px;
    margin-left: 5px;
}
.itemWidth {
    margin-left: 10px;
    flex-wrap: nowrap;
}

:deep(.el-form-item) {
    align-items: center;
}
:deep(.formRadio) {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    .radioRight {
        .el-radio__label {
            display: flex;
        }
    }
}

.formItem {
    align-items: flex-start;
}

:deep(.lyecs-time-select-group .time-con) {
    flex-wrap: nowrap;
}

/* 仅当前弹窗/页面生效：屏幕小于 1200 时压缩时间选择器宽度 */
@media only screen and (max-width: 1200px) {
    :deep(.lyecs-time-select-group .time-con) {
        flex-wrap: nowrap;
    }
    :deep(.lyecs-time-select-group .el-date-editor) {
        width: 180px !important;
    }
}
</style>
