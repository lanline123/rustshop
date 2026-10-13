<template>
    <div class="buy-btn-settings">
        <!-- 购买按钮显示/隐藏 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">购买按钮</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group
                    class="dec-radio-group"
                    :model-value="localData.showBtn"
                    @update:model-value="(v: string | number | boolean | undefined) => handleChange('showBtn', Number(v))"
                >
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">不显示</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <template v-if="localData.showBtn === 1">
            <!-- 购买样式 -->
            <div class="dec-edit-group bg-grey dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">购买样式</div>
                    <div class="value">{{ buyStyleLabel }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group
                        class="dec-radio-group dec-radio-group--inline"
                        :model-value="localData.btnType"
                        @update:model-value="(v: string | number | boolean | undefined) => handleChange('btnType', Number(v))"
                    >
                        <el-radio-button :value="1">样式一</el-radio-button>
                        <el-radio-button :value="2">样式二</el-radio-button>
                        <el-radio-button :value="3">样式三</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <!-- 按钮宽度 -->
            <div v-if="localData.text !== ''" class="dec-edit-group bg-grey">
                <div class="dec-edit-group-title">
                    <div class="label">按钮宽度</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            :model-value="localData.paddingLeftRight ?? defaults.paddingLeftRight"
                            :max="10"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            @update:model-value="(v: number | number[]) => handleChange('paddingLeftRight', Array.isArray(v) ? v[0] : v)"
                        />
                    </div>
                </div>
            </div>
        </template>
    </div>
</template>

<script lang="ts" setup>
/**
 * 购买按钮设置组件（移植自 Vort pc-product-group/components/BuyBtnSettings）
 */
import { computed } from "vue";
import type { BuyBtnStyle } from "../types";
import { defaultBuyBtnStyle } from "../index";

defineOptions({ name: "BuyBtnSettings" });

const props = withDefaults(
    defineProps<{
        modelValue?: BuyBtnStyle;
    }>(),
    {
        modelValue: () => ({})
    }
);

const emit = defineEmits<{
    (e: "update:modelValue", value: BuyBtnStyle): void;
}>();

// 默认值
const defaults = defaultBuyBtnStyle;

// 本地数据
const localData = computed(() => props.modelValue || {});

const buyStyleLabels: Record<number, string> = { 1: "样式一", 2: "样式二", 3: "样式三" };
const buyStyleLabel = computed(() => buyStyleLabels[localData.value.btnType ?? 1] || "样式一");

/**
 * 处理字段变更
 */
function handleChange(key: keyof BuyBtnStyle, value: unknown) {
    emit("update:modelValue", { ...props.modelValue, [key]: value });
}
</script>

<style lang="less" scoped>
.buy-btn-settings {
    .bg-grey {
        background-color: #f7f8fa;
    }

    .dec-range-group {
        display: flex;
        align-items: center;
    }
}

.dec-radio-group--inline {
    width: 100%;
}
</style>
