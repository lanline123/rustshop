<template>
    <div class="select-layout" v-for="item in options">
        <el-tooltip effect="dark" :content="item.label" placement="top">
            <div class="layout-item" :class="item.value" @click="change(item.value)">
                <div class="left-row"></div>
                <div class="top-row"></div>
                <div class="icon" v-if="modelValue == item.value">
                    <el-icon><Check /></el-icon>
                </div>
            </div>
        </el-tooltip>
    </div>
</template>
<script setup lang="ts">
import {PropType, ref} from "vue"
import { Check } from "@element-plus/icons-vue";
interface Option {
    label: string;
    value: string;
}
// 传值
const props = defineProps({
    options: {
        type: Array as PropType<Option[]>,
        default: () => []
    },
    fun: {
        type: Function as PropType<(type: any) => void>,
        default: (type: any) => {}
    }
})
const modelValue = defineModel<string>('modelValue',{ type: String, default: "" });
const change = (value: string) => {
    modelValue.value = value;
    props.fun(value);
}
</script>
<style lang="less" scoped>
.select-layout {
    .layout-item {
        width: 44px;
        height: 36px;
        margin-right: 20px;
        background-color: #ffffff;
        border-radius: 4px;
        box-shadow: 0 1px 2.5px rgba(0, 0, 0, 0.18);
        cursor: pointer;
        overflow: hidden;
        display: flex;
        position: relative;
        .el-icon {
            color: var(--tig-primary);
            font-size: 18px !important;
        }
        .top-row {
            flex: 1;
            height: 10px;
            background-color: #fff;
        }
        .left-row {
            height: 100%;
            width: 15px;
            background-color: #fff;
        }
        .icon {
            width: 18px;
            height: 18px;
            position: absolute;
            bottom: 2px;
            right: 3px;
        }
    }
    .light {
        .top-row {
            background-color: #fff;
            border-bottom: 1px solid #e6e6e6;
        }
        .left-row {
            background-color: #eef2ff;
            position: relative;
            z-index: 10;
        }
    }
    .dark,
    .side {
        background-color: #f0f2f5;
        .left-row {
            background-color: #001529;
            position: relative;
            z-index: 10;
        }
    }
    .realDark {
        background-color: #263849;
        .top-row {
            background-color: #061a2e;
        }
        .left-row {
            background-color: #0d2234;
        }
    }
    .top {
        background-color: #f0f2f5;
        .left-row {
            display: none;
        }
        .top-row {
            background-color: #061a2e;
        }
    }
    .mix,.topMenu {
        background-color: #f0f2f5;
        .top-row {
            background-color: #061a2e;
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 10px;
        }
        .left-row {
            background-color: #fff;
        }
    }
    .default {
        background-color: #f0f2f5;
        .left-row {
            background: linear-gradient(to right, #061a2e 50%, #ffffff 50%);
        }
    }
}
</style>
