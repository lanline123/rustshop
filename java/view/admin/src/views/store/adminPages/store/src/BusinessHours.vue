<template>
    <div>
        <el-form ref="formDataRef" :model="defaultData" :rules="rules" label-width="auto">
            <el-form-item :label="$t('营业时间')" prop="type" required>
                <div>
                    <div>
                        <el-radio-group v-model="defaultData.type" @change="changeType">
                            <el-radio :value="1">{{ $t('全天') }}</el-radio>
                        </el-radio-group>
                    </div>
                    <div>
                        <div>
                            <el-radio-group v-model="defaultData.type" @change="changeType">
                                <el-radio :value="2">{{ $t('每天重复') }}</el-radio>
                            </el-radio-group>
                            <div v-if="defaultData.type === 2" style="margin: 10px 0px 0px 20px" class="time-cycle">
                                <template v-for="(item, index) in defaultData.times">
                                    <el-form-item :prop="'defaultData.times' + index" :rules="rules.times" style="margin-bottom: 10px">
                                        <div class="flex align-center">
                                            <el-time-picker
                                                format="HH:mm"
                                                value-format="HH:mm"
                                                style="width: 110px"
                                                v-model="item.start"
                                                :placeholder="$t('开始时间')"
                                            />
                                            <div style="margin: 0 10px">{{ $t('至') }}</div>
                                            <el-time-picker
                                                format="HH:mm"
                                                value-format="HH:mm"
                                                style="width: 110px"
                                                v-model="item.end"
                                                :placeholder="$t('结束时间')"
                                            />
                                            <el-button class="ml10" type="primary" link @click="deleteDayTime(index)">{{ $t('删除') }}</el-button>
                                        </div>
                                    </el-form-item>
                                </template>
                                <el-button v-if="defaultData.times.length < 3" type="primary" link @click="addDayTime">{{ $t('添加时间') }}</el-button>
                            </div>
                        </div>
                        <div>
                            <el-radio-group v-model="defaultData.type" @change="changeType">
                                <el-radio :value="3">{{ $t('每周重复') }}</el-radio>
                            </el-radio-group>
                            <div v-if="defaultData.type === 3">
                                <div style="margin: 10px 0px 0px 20px" class="time-cycle">
                                    <template v-for="(item, index) in defaultData.timesWeek">
                                        <el-form-item :prop="'defaultData.timesWeek' + index" :rules="rules.timesWeek" style="margin-bottom: 10px">
                                            <div class="week flex align-center mb10">
                                                <p v-if="item.dayOfWeek && Array.isArray(item.dayOfWeek)">
                                                    {{ item.dayOfWeek.map((day: any) => weekList.find((week) => week.value === day)!.label).join("、") }}
                                                </p>
                                                <el-button class="ml10" type="primary" link @click="deleteWeekTime(index)">{{ $t('删除') }}</el-button>
                                            </div>
                                            <template v-for="(day, index1) in item.ranges">
                                                <el-form-item
                                                    :prop="'defaultData.timesWeek.' + index + '.ranges.' + index1"
                                                    :rules="rules.timesWeekChild"
                                                    style="margin-bottom: 10px"
                                                >
                                                    <div class="flex align-center">
                                                        <el-time-picker
                                                            format="HH:mm"
                                                            value-format="HH:mm"
                                                            style="width: 110px"
                                                            v-model="day.start"
                                                            :placeholder="$t('开始时间')"
                                                        />
                                                        <div style="margin: 0 10px">{{ $t('至') }}</div>
                                                        <el-time-picker
                                                            format="HH:mm"
                                                            value-format="HH:mm"
                                                            style="width: 110px"
                                                            v-model="day.end"
                                                            :placeholder="$t('结束时间')"
                                                        />
                                                        <el-button class="ml10" type="primary" link @click="deleteWeekDayTime(index, index1)">{{ $t('删除') }}</el-button>
                                                    </div>
                                                </el-form-item>
                                            </template>
                                            <el-button v-if="defaultData.timesWeek[index].ranges.length < 3" type="primary" link @click="addWeekDayTime(index)"
                                                >{{ $t('添加时间') }}</el-button
                                            >
                                        </el-form-item>
                                    </template>
                                </div>
                                <el-popover :visible="visible" placement="right-end" :width="150">
                                    <div class="week-list">
                                        <el-checkbox-group v-model="checkeds">
                                            <el-checkbox
                                                v-for="item in weekList"
                                                :key="item.label"
                                                :label="item.label"
                                                :value="item.value"
                                                :disabled="disabledWeek(item.value)"
                                            >
                                                {{ item.label }}
                                            </el-checkbox>
                                        </el-checkbox-group>
                                        <div class="flex mt10">
                                            <el-button type="primary" @click="addWeekTime">{{ $t('确定') }}</el-button>
                                            <el-button @click="visible = false">{{ $t('取消') }}</el-button>
                                        </div>
                                    </div>
                                    <template #reference>
                                        <el-button type="primary" link @click="visible = true">{{ $t('添加星期') }}</el-button>
                                    </template>
                                </el-popover>
                                <div class="errorMsg">{{ errorMsg }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </el-form-item>
        </el-form>
        <!-- <el-button type="primary" @click="submit">提交</el-button> -->
    </div>
</template>

<script lang="ts" setup>
import { ref, shallowRef, reactive, watch } from "vue";
import { cloneDeep } from "lodash";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const visible = ref(false);
const weekList = [
    {
        label: t('周一'),
        value: 1
    },
    {
        label: t('周二'),
        value: 2
    },
    {
        label: t('周三'),
        value: 3
    },
    {
        label: t('周四'),
        value: 4
    },
    {
        label: t('周五'),
        value: 5
    },
    {
        label: t('周六'),
        value: 6
    },
    {
        label: t('周日'),
        value: 7
    }
];
const defaultData = ref<any>({
    type: 1,
    times: [
        {
            start: "",
            end: ""
        }
    ],
    timesWeek: [
        {
            dayOfWeek: [1],
            ranges: [
                {
                    start: "",
                    end: ""
                }
            ]
        }
    ]
});
const props = defineProps({
    formData: {
        type: Object,
        default: {}
    }
});
const formDataRef = shallowRef();
watch(
    () => props.formData,
    (newVal: any) => {
        if (!newVal || typeof newVal !== "object") return;
        const copiedVal = cloneDeep(newVal);
        if (copiedVal.type === 3) {
            (copiedVal.times || []).map((item: any) => {
                if (typeof item.dayOfWeek === "string") {
                    item.dayOfWeek = item.dayOfWeek.split(",").map(Number);
                }
            });
            defaultData.value.type = copiedVal.type;
            defaultData.value.timesWeek = copiedVal.times;
        } else if (copiedVal.type) {
            defaultData.value.type = copiedVal.type;
            defaultData.value.times = copiedVal.times;
        }
    },
    { deep: true, immediate: true }
);

const changeType = (val: number) => {
    // if (val === 3) {
};

const checkeds = ref<any[]>([1]);
const disabledWeek = (day: number) => {
    if (defaultData.value.timesWeek && defaultData.value.timesWeek.length > 0) {
        return defaultData.value.timesWeek.some((item: any) => {
            // 确保 dayOfWeek 存在并且是一个数组
            if (item.dayOfWeek && Array.isArray(item.dayOfWeek)) {
                return item.dayOfWeek.includes(day);
            }
            return false;
        });
    }
};

const addWeekTime = () => {
    if (checkeds.value.length === 0) {
        errorMsg.value = t('请选择至少一个星期');
        return;
    }
    // 去重处理
    const newDayOfWeek = [...checkeds.value];
    const existingDays = defaultData.value.timesWeek.flatMap((item: any) => item.dayOfWeek);
    const uniqueDayOfWeek = newDayOfWeek.filter((day) => !existingDays.includes(day));

    defaultData.value.timesWeek.push({
        dayOfWeek: uniqueDayOfWeek,
        ranges: [
            {
                start: "",
                end: ""
            }
        ]
    });
    visible.value = false;
};

const addDayTime = () => {
    defaultData.value.times.push({
        start: "",
        end: ""
    });
};
const deleteDayTime = (index: number) => {
    defaultData.value.times.splice(index, 1);
};

const deleteWeekTime = (index: number) => {
    const daysToRemove = defaultData.value.timesWeek[index].dayOfWeek;
    defaultData.value.timesWeek.splice(index, 1);
    checkeds.value = checkeds.value.filter((day) => !daysToRemove.includes(day));
};
const addWeekDayTime = (index: number) => {
    defaultData.value.timesWeek[index].ranges.push({
        start: "",
        end: ""
    });
};
const deleteWeekDayTime = (index: number, index1: number) => {
    defaultData.value.timesWeek[index].ranges.splice(index1, 1);
};

interface RuleForm {
    times: Array<{
        validator: (rule: any, value: any, callback: (error?: Error) => void) => void;
        trigger: string;
    }>;
    timesWeek: Array<{
        validator: (rule: any, value: any, callback: (error?: Error) => void) => void;
        trigger: string;
    }>;
    timesWeekChild: Array<{
        validator: (rule: any, value: any, callback: (error?: Error) => void) => void;
        trigger: string;
    }>;
}
const validateDayTime = (rule: any, value: any, callback: any, rowIndex: number) => {
    let condition = defaultData.value.times[rowIndex] as any;
    if (!condition.start || !condition.end) {
        callback(new Error(t('请选择时间')));
        return;
    }
    const [startHour, startMinute] = condition.start.split(":").map(Number);
    const [endHour, endMinute] = condition.end.split(":").map(Number);
    const startDateTime = new Date();
    startDateTime.setHours(startHour, startMinute, 0, 0);
    const endDateTime = new Date();
    endDateTime.setHours(endHour, endMinute, 0, 0);
    if (endDateTime <= startDateTime) {
        callback(new Error(t('结束时间必须大于开始时间')));
        return;
    }
    callback();
};

const validateWeekTime = (rule: any, value: any, callback: any, rowIndex: number) => {
    if (defaultData.value.type === 3 && defaultData.value.timesWeek.length === 0) {
        callback(new Error(t('请至少选择一个周')));
        return;
    }
    if (defaultData.value.type === 3 && defaultData.value.timesWeek[rowIndex].ranges.length === 0) {
        callback(new Error(t('请至少添加一个时间')));
        return;
    }
    callback();
};
const validateWeekDayTime = (rule: any, value: any, callback: any, rowIndex: number, rowIndexChild: number) => {
    let condition = defaultData.value.timesWeek[rowIndex].ranges[rowIndexChild] as any;
    if (!condition.start || !condition.end) {
        callback(new Error(t('请选择时间')));
        return;
    }
    const [startHour, startMinute] = condition.start.split(":").map(Number);
    const [endHour, endMinute] = condition.end.split(":").map(Number);
    const startDateTime = new Date();
    startDateTime.setHours(startHour, startMinute, 0, 0);
    const endDateTime = new Date();
    endDateTime.setHours(endHour, endMinute, 0, 0);
    if (endDateTime <= startDateTime) {
        callback(new Error(t('结束时间必须大于开始时间')));
        return;
    }
    callback();
};

const rules = reactive<RuleForm>({
    times: [
        {
            validator: (rule: any, value: any, callback: any) => {
                const regex = /times(\d+)/;
                const match: any = rule?.field.match(regex);
                const rowIndex = match[1];
                validateDayTime(rule, value, callback, rowIndex);
            },
            trigger: "blur"
        }
    ],
    timesWeek: [
        {
            validator: (rule: any, value: any, callback: any) => {
                const regex = /timesWeek(\d+)/;
                const match: any = rule?.field.match(regex);
                const rowIndex = match[1];
                validateWeekTime(rule, value, callback, rowIndex);
            },
            trigger: "blur"
        }
    ],
    timesWeekChild: [
        {
            validator: (rule: any, value: any, callback: any) => {
                const regex = /defaultData\.timesWeek\.(\d+)\.ranges\.(\d+)/;
                const match: any = rule?.field.match(regex);
                const rowIndex = match[1];
                const rowIndexChild = match[2];
                validateWeekDayTime(rule, value, callback, rowIndex, rowIndexChild);
            },
            trigger: "blur"
        }
    ]
});
const errorMsg = ref("");
const submit = async () => {
    await formDataRef.value.validate();
    errorMsg.value = "";
    if (defaultData.value.type === 3 && defaultData.value.timesWeek.length === 0) {
        errorMsg.value = t('请至少选择一个周');
        return false;
    }
    return true;
};

defineExpose({ submit, formData: defaultData });
</script>
<style lang="less" scoped>
.time-cycle {
    :deep(.el-form-item__content) {
        display: block;
    }
    :deep(.el-form-item__error) {
        position: relative;
        font-size: 14px;
        margin-top: 5px;
        top: 0;
    }
}
.errorMsg {
    color: #f56c6c;
}
</style>
