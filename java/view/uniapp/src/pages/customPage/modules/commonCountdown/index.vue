<template>
    <div class="countdown-box">
        <template v-if="countdownType === 'dailyCycle'">
            <template v-if="Array.isArray(props.dailyTime) && getCurrentTimestamp() < Number(timeInfo.dailyStarTime)">
                <up-count-down :format="format" :time="Number(timeInfo.dailyStarTime) - getCurrentTimestamp()" @change="onChange">
                    <template v-for="(item, index) in arr" :key="index">
                        <text class="time-item" :style="timeStyle">
                            {{ formatTime(timeData[timeDateMap[item]])
                            }}<template v-if="!zh">
                                <text class="time-item-unit" :style="unitStyle">{{ index < arr.length - 1 ? ":" : "" }}</text>
                            </template>
                            <template v-else>
                                <text class="time-item-unit" :style="unitStyle">{{ textMap[item] }}</text>
                            </template>
                        </text>
                    </template>
                </up-count-down>
            </template>
            <template v-else>
                <up-count-down :format="format" :time="dailyEndTimeData" @change="onChange">
                    <template v-for="(item, index) in arr" :key="index">
                        <text class="time-item" :style="timeStyle">
                            {{ formatTime(timeData[timeDateMap[item]])
                            }}<template v-if="!zh">
                                <text class="time-item-unit" :style="unitStyle">{{ index < arr.length - 1 ? ":" : "" }}</text>
                            </template>
                            <template v-else>
                                <text class="time-item-unit" :style="unitStyle">{{ textMap[item] }}</text>
                            </template>
                        </text>
                    </template>
                </up-count-down>
            </template>
        </template>
        <template v-else>
            <up-count-down :format="format" :time="specifyTimeData" @change="onChange">
                <template v-for="(item, index) in arr" :key="index">
                    <text class="time-item" :style="timeStyle">
                        {{ formatTime(timeData[timeDateMap[item]])
                        }}<template v-if="!zh">
                            <text class="time-item-unit" :style="unitStyle">{{ index < arr.length - 1 ? ":" : "" }}</text>
                        </template>
                        <template v-else>
                            <text class="time-item-unit" :style="unitStyle">{{ textMap[item] }}</text>
                        </template>
                    </text>
                </template>
            </up-count-down>
        </template>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
defineOptions({
    styleIsolation: "shared"
});
const { t } = useI18n();
const props = defineProps({
    countdownType: {
        type: String,
        default: "dailyCycle"
    },
    valueStyle: {
        type: Object,
        default: () => ({})
    },
    dailyTime: {
        type: [Array as any, Number, String],
        default: () => []
    },
    specifyTime: {
        type: [Number, String],
        default: ""
    },
    format: {
        type: String,
        default: "HH:mm:ss"
    },
    color: {
        type: String,
        default: "#000"
    },
    zh: {
        type: Boolean,
        default: false
    },
    timeStyle: {
        type: Object,
        default: () => ({})
    },
    unitStyle: {
        type: Object,
        default: () => ({})
    }
});
const arr = computed(() => props.format.split(":"));
const timeData = ref<any>({});
const onChange = (e: any) => {
    timeData.value = e;
    // console.log('timeData', timeData.value)
};
// 格式化时间，保证两位数显示
const formatTime = (time: string) => {
    return time?.toString().padStart(2, "0");
};
const timeInfo = computed(() => {
    let obj = {
        dailyStarTime: 0,
        dailyEndTime: 0
    };
    if (props.dailyTime) {
        // 取配置时分秒，锚定到今天的固定时刻
        if (typeof props.dailyTime === "object") {
            const start = new Date(Number(props.dailyTime[0]));
            obj.dailyStarTime = new Date().setHours(start.getHours(), start.getMinutes(), start.getSeconds(), 0);
            const end = new Date(Number(props.dailyTime[1]));
            obj.dailyEndTime = new Date().setHours(end.getHours(), end.getMinutes(), end.getSeconds(), 0);
        } else {
            const end = new Date(Number(props.dailyTime));
            obj.dailyEndTime = new Date().setHours(end.getHours(), end.getMinutes(), end.getSeconds(), 0);
        }
    }

    return obj;
});

const textMap: { [key: string]: string } = {
    DD: t("天"),
    HH: t("时"),
    mm: t("分"),
    ss: t("秒")
};

const timeDateMap = {
    DD: "days",
    HH: "hours",
    mm: "minutes",
    ss: "seconds"
};

const color = computed(() => props.color);

const specifyTimeData = computed(() => {
    let num = Number(props.specifyTime) - getCurrentTimestamp();
    return num > 0 ? num : 0;
});

const dailyEndTimeData = computed(() => {
    let num = Number(timeInfo.value.dailyEndTime) - getCurrentTimestamp();
    return num > 0 ? num : 0;
});

const getCurrentTimestamp = () => {
    return new Date().getTime();
};
</script>

<style lang="scss" scoped>
.countdown-box {
    .time-item {
        color: v-bind(color) !important;
        font-size: 12px;
    }
}
</style>
