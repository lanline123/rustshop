/**
 * @file 时间区间选择器组件
 * @description 用于选择开始和结束日期的组合选择器
 * @features
 * - 开始/结束日期联动限制
 * - 可选禁止选择今天之前的日期
 * - 支持多种日期格式
 * - 响应式布局适配移动端
 */
<template>
    <div class="lyecs-time-select-group">
        <div class="time-con">
            <el-date-picker
                :clearable="!clearable"
                v-model="startDate"
                :type="type"
                :disabled-date="disabledStartDate"
                :placeholder="$t('开始日期')"
                @change="onChangeStartDate"
                :value-format="valueFormat"
                :format="format"
                :default-time="startDefaultTime"
                :editable="false"
                :style="{width}"
                :disabled="disabled"
            ></el-date-picker>
            <span style="display: inline-block; padding: 0 10px">{{ $t(separator) }}</span>
            <el-date-picker
                :clearable="!clearable"
                v-model="endDate"
                :type="type"
                :disabled-date="disabledEndDate"
                :placeholder="$t('结束日期')"
                @change="onChangeEndDate"
                :value-format="valueFormat"
                :format="format"
                :default-time="endDefaultTime"
                :editable="false"
                :style="{width}"
                :disabled="disabled"
            ></el-date-picker>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, computed } from "vue";
import { formattedDate } from "@/utils/time";
import { getDays } from "@/utils/util";

const props = defineProps({
    /** 日期选择器类型 */
    type: { type: String, default: "datetime" },
    /** 是否可清空 */
    clearable: { type: Boolean, default: false },
    /** 是否禁用 */
    disabled: { type: Boolean, default: false },
    /** 日期格式（绑定值格式） */
    valueFormat: { type: String, default: "YYYY-MM-DD" },
    /** 显示格式（不传则使用 el-date-picker 默认） */
    format: { type: String, default: undefined },
    /** 开始日期选中后默认时间点（datetime 类型生效） */
    startDefaultTime: { type: Date, default: undefined },
    /** 结束日期选中后默认时间点（datetime 类型生效） */
    endDefaultTime: { type: Date, default: undefined },
    /** 选择器宽度 */
    width: { type: String, default : "200px" },
    /** 是否禁止选择今天之前的日期 */
    disableBeforeToday: { type: Boolean, default: false },
    /** 两个日期之间的分隔符 */
    separator: { type: String, default: "至" }
});

const emit = defineEmits(["update:startDate", "update:endDate", "callback"]);
/** 开始日期 */
const startDate = defineModel<String>("startDate", { type: String, default: formattedDate(new Date(), "YYYY-MM-DD 00:00:00") });
/** 结束日期 */
const endDate = defineModel<String>("endDate", { type: String, default: formattedDate(getDays(30, "add"), "YYYY-MM-DD 23:59:59") });

onMounted(() => {
    emit("update:startDate", startDate.value);
    emit("update:endDate", endDate.value);
});

/** 获取今天零点的时间戳 */
const todayStartMs = () => {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    return d.getTime();
};

/**
 * 开始日期禁用判断
 * @param {any} time - 待判断的日期
 * @returns {boolean} 是否禁用
 */
const disabledStartDate = (time: any) => {
    const t = time.getTime();
    if (props.disableBeforeToday && t < todayStartMs()) return true;
    if (endDate.value) return t > convertToMilliseconds(endDate.value);
    return false;
};

/**
 * 结束日期禁用判断
 * @param {any} time - 待判断的日期
 * @returns {boolean} 是否禁用
 */
const disabledEndDate = (time: any) => {
    const t = time.getTime();
    if (props.disableBeforeToday && t < todayStartMs()) return true;
    if (startDate.value) return t < convertToMilliseconds(startDate.value);
    return false;
};

/**
 * 日期字符串转时间戳
 * @param {any} dateString - 日期字符串
 */
function convertToMilliseconds(dateString: any) {
    return new Date(dateString).getTime(); 
}

/**
 * 开始日期变更处理
 * @param {any} value - 新的日期值
 */
const onChangeStartDate = (value: any) => {
    startDate.value = value;
    emit("update:startDate", value);
    emit("callback", true);
};

/**
 * 结束日期变更处理
 * @param {any} value - 新的日期值
 */
const onChangeEndDate = (value: any) => {
    endDate.value = value;
    emit("update:endDate", value);
    emit("callback", true);
};
</script>

<style lang="less" scoped>
.time-con {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
}

@media only screen and (max-width: 767px) {
    .time-con {
        flex-direction: column;
        align-items: start;
        :deep(.el-input) {
            width: 100% !important;
        }
        > span {
            line-height: 30px;
            text-align: left;
        }
    }
}
</style>
