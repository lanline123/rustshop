<template>
    <div class="presale-config">
        <!-- 预售方式选择 -->
        <el-form-item prop="presaleConfig.saleMode" :rules="[{ required: true, message: '请选择预售方式' }]" class="sale-mode-item">
            <template #label>
                <span class="form-label">{{ $t('预售方式') }}</span>
            </template>
            <div class="sale-mode-wrapper">
                <el-radio-group v-model="localConfig.saleMode" :disabled="disabled" @change="handleSaleModeChange">
                    <el-radio :value="1">{{ $t('全款预售') }}</el-radio>
                    <el-radio :value="2">
                        <div class="flex">{{ $t('定金预售') }}</div>
                    </el-radio>
                </el-radio-group>
                <el-tooltip class="box-item" effect="light" placement="bottom" show-after="300">
                    <template #content>
                        <div style="width: 300px; padding: 5px 10px"> {{ $t('售后规则：') }}<br />{{ $t('1、当使用全款预售模式时，用户支付预售款后正常发起全款退款;') }}<br /> {{ $t('2、当使用定金预售时，若用户已付定金，但未在规定时间内支付尾款，订单会一直保留，直至付完尾款;') }}<br /> {{ $t('3、当用户已支付定金并支付尾款后，客户可申请售后') }} </div>
                    </template>
                    <div class="flex flex-align-center refund-rules-link">
                        <div>{{ $t('售后规则') }}</div>
                        <el-icon style="margin-left: 5px; margin-top: 1px" size="14" color="#999">
                            <QuestionFilled />
                        </el-icon>
                    </div>
                </el-tooltip>
            </div>
        </el-form-item>

        <!-- 定金预售配置 -->
        <template v-if="localConfig.saleMode === 2">
            <!-- 定金收取方式 -->
            <el-form-item
                prop="presaleConfig.depositType"
                :rules="[
                    { required: true, message: '请选择定金收取方式' },
                    { validator: validateDepositConfig, trigger: 'blur' }
                ]"
                class="section-item"
            >
                <template #label>
                    <span class="form-label">{{ $t('定金收取方式') }}</span>
                </template>
                <el-radio-group v-model="localConfig.depositType" :disabled="disabled" @change="handleDepositTypeChange" class="deposit-type-group">
                    <!-- 按百分比 -->
                    <el-radio :value="1" class="deposit-radio-item">
                        <span class="radio-text">{{ $t('按') }}</span>
                        <TigInput
                            v-model="localConfig.depositRatio"
                            :disabled="disabled || localConfig.depositType !== 1"
                            type="text"
                            placeholder=""
                            width="100px"
                            class="inline-input"
                            @input="handleDepositRatioInput"
                        >
                            <template #append>%</template>
                        </TigInput>
                        <span class="radio-text">{{ $t('收取定金') }}</span>
                        <el-checkbox
                            v-model="isRoundDownChecked"
                            :disabled="disabled || localConfig.depositType !== 1"
                            @change="handleRoundDownChange"
                            class="round-down-checkbox"
                        > {{ $t('向下取整数') }} </el-checkbox>
                    </el-radio>
                    <!-- 按固定金额 -->
                    <el-radio :value="2" class="deposit-radio-item">
                        <span class="radio-text">{{ $t('按固定金额') }}</span>
                        <TigInput
                            v-model="localConfig.depositFixedAmount"
                            :disabled="disabled || localConfig.depositType !== 2"
                            type="decimal"
                            placeholder=""
                            width="100px"
                            class="inline-input"
                        >
                            <template #append>{{ $t('元') }}</template>
                        </TigInput>
                        <span class="radio-text">{{ $t('收取定金') }}</span>
                    </el-radio>
                </el-radio-group>
            </el-form-item>

            <!-- 预售有效期 -->
            <el-form-item class="section-item">
                <template #label>
                    <span class="form-label">{{ $t('预售有效期') }}</span>
                </template>
                <el-radio-group :model-value="1" :disabled="true" class="validity-group">
                    <el-radio :value="1">{{ $t('长期有效') }}</el-radio>
                </el-radio-group>
            </el-form-item>

            <!-- 预售定金 -->
            <el-form-item class="section-item">
                <template #label>
                    <span class="form-label">{{ $t('预售定金') }}</span>
                </template>
                <el-radio-group :model-value="1" :disabled="true" class="deposit-refund-group">
                    <el-radio :value="1">{{ $t('定金不退') }}</el-radio>
                </el-radio-group>
                <div class="extra">{{ $t('预售定金不支持退款，若要退款请联系客服处理，或支付尾款后申请售后') }}</div>
            </el-form-item>

            <!-- 尾款支付时间 -->
            <el-form-item
                prop="presaleConfig.finalPaymentType"
                :rules="[
                    { required: true, message: '请选择尾款支付时间类型' },
                    { validator: validateFinalPaymentConfig, trigger: 'blur' }
                ]"
                class="section-item"
            >
                <template #label>
                    <span class="form-label">{{ $t('尾款支付时间') }}</span>
                </template>
                <el-radio-group v-model="localConfig.finalPaymentType" :disabled="disabled" @change="handleFinalPaymentTypeChange" class="final-payment-group">
                    <!-- 相对时间 -->
                    <el-radio :value="1" class="final-payment-radio-item">
                        <span class="radio-text">{{ $t('支付定金后') }}</span>
                        <TigInput
                            v-model="localConfig.finalPaymentRelativeDays"
                            :disabled="disabled || localConfig.finalPaymentType !== 1"
                            type="integer"
                            placeholder=""
                            width="100px"
                            class="inline-input"
                        >
                            <template #append>{{ $t('天') }}</template>
                        </TigInput>
                        <span class="radio-text">{{ $t('开始支付尾款,并需要在') }}</span>
                        <TigInput
                            v-model="localConfig.finalPaymentDurationDays"
                            :disabled="disabled || localConfig.finalPaymentType !== 1"
                            type="integer"
                            placeholder=""
                            width="100px"
                            class="inline-input"
                        >
                            <template #append>{{ $t('天') }}</template>
                        </TigInput>
                        <TigInput
                            v-model="localConfig.finalPaymentDurationHours"
                            :disabled="disabled || localConfig.finalPaymentType !== 1"
                            type="integer"
                            :min="localConfig.finalPaymentDurationDays === 0 ? 1 : 0"
                            placeholder=""
                            width="100px"
                            class="inline-input"
                        >
                            <template #append>{{ $t('小时') }}</template>
                        </TigInput>
                        <span class="radio-text">{{ $t('内完成支付') }}</span>
                    </el-radio>
                    <!-- 固定时间 -->
                    <el-radio :value="2" class="final-payment-radio-item">
                        <span class="radio-text">{{ $t('固定时间') }}</span>
                        <el-date-picker
                            v-model="finalPaymentTimeRange"
                            type="datetimerange"
                            :disabled="disabled || localConfig.finalPaymentType !== 2"
                            range-separator=" - "
                            start-placeholder="开始时间"
                            end-placeholder="结束时间"
                            format="YYYY-MM-DD HH:mm"
                            value-format="YYYY-MM-DD HH:mm"
                            @change="handleFinalPaymentTimeChange"
                            class="date-picker-inline"
                        />
                    </el-radio>
                </el-radio-group>
                <!-- 固定时间的提示 -->
                <!-- <div v-if="localConfig.finalPaymentType === 2" class="extra">此时间只在用户页面做展示，超出设定的时间客户依然可支付尾款</div> -->
            </el-form-item>
        </template>

        <!-- 发货时间配置（全款和定金预售都需要） -->
        <el-form-item
            prop="presaleConfig.deliveryType"
            :rules="[
                { required: true, message: '请选择发货时间类型' },
                { validator: validateDeliveryConfig, trigger: 'blur' }
            ]"
            class="section-item"
        >
            <template #label>
                <span class="form-label">{{ $t('发货时间') }}</span>
            </template>
            <el-radio-group
                v-model="localConfig.deliveryType"
                :disabled="disabled"
                @change="handleDeliveryTypeChange"
                class="delivery-time-group"
                style="width: 100%"
            >
                <el-radio :value="1" class="delivery-radio-item">
                    <span class="radio-text">{{ $t('支付全款后') }}</span>
                    <TigInput
                        v-model="localConfig.deliveryRelativeDays"
                        :disabled="disabled || localConfig.deliveryType !== 1"
                        type="integer"
                        placeholder=""
                        width="100px"
                        class="inline-input"
                    >
                        <template #append>{{ $t('天') }}</template>
                    </TigInput>
                    <TigInput
                        v-model="localConfig.deliveryRelativeHours"
                        :disabled="disabled || localConfig.deliveryType !== 1"
                        type="integer"
                        placeholder=""
                        width="100px"
                        class="inline-input"
                    >
                        <template #append>{{ $t('小时') }}</template>
                    </TigInput>
                    <span class="radio-text">{{ $t('后开始发货') }}</span>
                </el-radio>
                <el-radio :value="2" class="delivery-radio-item">
                    <span class="radio-text">{{ $t('指定日期') }}</span>
                    <el-date-picker
                        v-model="deliveryDateValue"
                        type="date"
                        :disabled="disabled || localConfig.deliveryType !== 2"
                        :placeholder="$t('请选择日期')"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        @change="handleDeliveryDateChange"
                        class="date-picker-inline"
                    />
                </el-radio>
            </el-radio-group>
        </el-form-item>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import type { PresaleConfig } from "@/types/product/product.d";
import { timestampToDate } from "@/utils/time";
import { QuestionFilled } from "@element-plus/icons-vue";
import { log } from "console";

// Props & Emits
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    modelValue: PresaleConfig;
    productPrice: number;
    productList: any[];
    disabled?: boolean;
}>();

const emit = defineEmits<{
    (e: "update:modelValue", value: PresaleConfig): void;
}>();

// 本地配置（双向绑定）
const localConfig = ref<PresaleConfig>(props.modelValue ? { ...props.modelValue } : ({} as PresaleConfig));

// 向下取整复选框状态
const isRoundDownChecked = computed({
    get: () => localConfig.value.isRoundDown === 1,
    set: (val) => {
        localConfig.value.isRoundDown = val ? 1 : 0;
    }
});

// 计算最低价格
const minPrice = computed(() => {
    if (props.productList?.length > 0) {
        const prices = props.productList.map((sku) => Number(sku.skuPrice || 0)).filter((p) => p > 0);
        return prices.length > 0 ? Math.min(...prices) : props.productPrice || 0;
    }
    return props.productPrice || 0;
});

// 尾款支付时间范围（用于日期选择器绑定）
const finalPaymentTimeRange = ref<[string, string] | null>(null);

// 发货日期（用于日期选择器绑定）
const deliveryDateValue = ref<string>("");

// 初始化时间选择器的值
watch(
    () => props.modelValue,
    (newVal) => {
        // 尾款支付时间范围（固定时间类型）
        if (newVal.finalPaymentType === 2 && newVal.finalPaymentStartTime && newVal.finalPaymentEndTime) {
            // 如果是时间戳（秒级），转换为日期时间字符串
            const startTime =
                typeof newVal.finalPaymentStartTime === "number"
                    ? timestampToDate(newVal.finalPaymentStartTime, "YYYY-MM-DD HH:mm:ss")
                    : String(newVal.finalPaymentStartTime);
            const endTime =
                typeof newVal.finalPaymentEndTime === "number"
                    ? timestampToDate(newVal.finalPaymentEndTime, "YYYY-MM-DD HH:mm:ss")
                    : String(newVal.finalPaymentEndTime);
            finalPaymentTimeRange.value = [startTime, endTime];
        } else {
            finalPaymentTimeRange.value = null;
        }

        // 发货日期（指定日期类型）
        if (newVal.deliveryType === 2 && newVal.deliveryDate) {
            // 如果是时间戳（秒级），转换为日期字符串
            if (typeof newVal.deliveryDate === "number") {
                deliveryDateValue.value = timestampToDate(newVal.deliveryDate, "YYYY-MM-DD");
            } else {
                // 如果是字符串，提取日期部分（YYYY-MM-DD）
                deliveryDateValue.value = String(newVal.deliveryDate).split(" ")[0];
            }
        } else {
            deliveryDateValue.value = "";
        }
    },
    { immediate: true }
);

// 监听本地配置变化，同步到父组件
watch(
    localConfig,
    (newVal) => {
        emit("update:modelValue", { ...newVal });
    },
    { deep: true }
);

// 预售类型变化
const handleSaleModeChange = () => {
    // 切换类型时重置某些字段
    if (localConfig.value.saleMode === 1) {
        // 全款预售：清空定金相关配置
        localConfig.value.depositType = undefined;
        localConfig.value.depositRatio = undefined;
        localConfig.value.depositFixedAmount = undefined;
        localConfig.value.finalPaymentType = undefined;
    } else {
        // 定金预售：默认选中第一个选项，输入框初始值全部为空
        localConfig.value.depositType = 1; // 默认选中第一个：按百分比
        localConfig.value.finalPaymentType = 1; // 默认选中第一个：相对时间
        // 输入框初始值全部为空
        localConfig.value.depositRatio = undefined;
        localConfig.value.depositFixedAmount = undefined;
        localConfig.value.finalPaymentRelativeDays = undefined;
        localConfig.value.finalPaymentDurationDays = undefined;
        localConfig.value.finalPaymentDurationHours = undefined;
    }
};

// 定金收取方式变化
const handleDepositTypeChange = () => {
    // 切换时重置所有相关字段
    if (localConfig.value.depositType === 1) {
        // 切换到按百分比，清空固定金额相关字段
        localConfig.value.depositFixedAmount = undefined;
        localConfig.value.depositRatio = undefined;
        localConfig.value.isRoundDown = undefined;
    } else if (localConfig.value.depositType === 2) {
        // 切换到按固定金额，清空百分比相关字段
        localConfig.value.depositRatio = undefined;
        localConfig.value.isRoundDown = undefined;
        localConfig.value.depositFixedAmount = undefined;
    }
};

// 计算定金金额（用于验证和显示）
const calculateDepositAmount = (price: number, ratio: number, roundDown: boolean = false): number => {
    if (!price || !ratio) return 0;
    const amount = (price * ratio) / 100;
    return roundDown ? Math.floor(amount) : amount;
};

// 定金比例输入事件处理
const handleDepositRatioInput = () => {
    const min = 1;
    const max = 99;
    let value = localConfig.value.depositRatio;
    let valueStr = value !== undefined && value !== null ? String(value) : "";

    // 只保留数字和小数点
    valueStr = valueStr.replace(/[^\d.]/g, "");
    console.log(valueStr, "valueStr");
    // 如果第一个字符是小数点，移除它
    if (valueStr.startsWith(".")) {
        valueStr = valueStr.substring(1);
    }

    // 确保只有一个小数点
    const parts = valueStr.split(".");
    if (parts.length > 2) {
        valueStr = parts[0] + "." + parts.slice(1).join("");
    }

    // 转换为数字
    let numValue: number | string | undefined = undefined;
    if ((valueStr.endsWith(".") || valueStr.endsWith(".0")) && Number(valueStr.slice(0, -1)) >= min && Number(valueStr.slice(0, -1)) <= max) {
        console.log(valueStr, "valueStr");
        localConfig.value.depositRatio = valueStr;
        return;
    }
    if (valueStr && valueStr !== ".") {
        numValue = parseFloat(valueStr);
        console.log(numValue, "numValue");
        if (isNaN(numValue)) {
            numValue = undefined;
        } else {
            // 控制最大最小值
            if (numValue < min) {
                numValue = min;
            } else if (numValue > max) {
                numValue = max;
            }
        }
    }

    // 更新值
    localConfig.value.depositRatio = numValue;

    // 如果勾选了向下取整，确保比例是整数
    // if (isRoundDownChecked.value && localConfig.value.depositRatio) {
    //     const ratio = Number(localConfig.value.depositRatio);
    //     if (!Number.isInteger(ratio)) {
    //         localConfig.value.depositRatio = Math.floor(ratio);
    //     }
    // }
};

// 向下取整变化
const handleRoundDownChange = () => {
    // 向下取整逻辑会在后端计算定金金额时应用
};

// 尾款支付时间类型变化
const handleFinalPaymentTypeChange = () => {
    // 切换时重置所有相关字段
    if (localConfig.value.finalPaymentType === 1) {
        // 切换到相对时间，清空固定时间相关字段
        localConfig.value.finalPaymentStartTime = undefined;
        localConfig.value.finalPaymentEndTime = undefined;
        finalPaymentTimeRange.value = null;
        // 重置相对时间字段
        localConfig.value.finalPaymentRelativeDays = undefined;
        localConfig.value.finalPaymentDurationDays = undefined;
        localConfig.value.finalPaymentDurationHours = undefined;
    } else if (localConfig.value.finalPaymentType === 2) {
        // 切换到固定时间，清空相对时间相关字段
        localConfig.value.finalPaymentRelativeDays = undefined;
        localConfig.value.finalPaymentDurationDays = undefined;
        localConfig.value.finalPaymentDurationHours = undefined;
        // 重置固定时间字段
        localConfig.value.finalPaymentStartTime = undefined;
        localConfig.value.finalPaymentEndTime = undefined;
        finalPaymentTimeRange.value = null;
    }
};

// 发货时间类型变化
const handleDeliveryTypeChange = () => {
    // 切换时重置所有相关字段
    if (localConfig.value.deliveryType === 1) {
        // 切换到相对时间，清空指定日期相关字段
        localConfig.value.deliveryDate = undefined;
        deliveryDateValue.value = "";
        // 重置相对时间字段
        localConfig.value.deliveryRelativeDays = undefined;
        localConfig.value.deliveryRelativeHours = undefined;
    } else if (localConfig.value.deliveryType === 2) {
        // 切换到指定日期，清空相对时间相关字段
        localConfig.value.deliveryRelativeDays = undefined;
        localConfig.value.deliveryRelativeHours = undefined;
        // 重置指定日期字段
        localConfig.value.deliveryDate = undefined;
        deliveryDateValue.value = "";
    }
};

// 尾款支付时间范围变化
const handleFinalPaymentTimeChange = (value: [string, string] | null) => {
    if (value) {
        // 直接使用年月日时分格式字符串，不转换为时间戳
        localConfig.value.finalPaymentStartTime = value[0];
        localConfig.value.finalPaymentEndTime = value[1];
    } else {
        localConfig.value.finalPaymentStartTime = undefined;
        localConfig.value.finalPaymentEndTime = undefined;
    }
};

// 发货日期变化
const handleDeliveryDateChange = (value: string | null) => {
    if (value) {
        // 直接使用年月日格式字符串，不转换为时间戳
        localConfig.value.deliveryDate = value;
    } else {
        localConfig.value.deliveryDate = undefined;
    }
};

// ==================== 表单验证 ====================

// ==================== 统一校验函数 ====================

// 统一的定金配置校验
const validateDepositConfig = (rule: any, value: any, callback: any) => {
    // value 是 depositType 的值，但我们不依赖它，直接读取 localConfig
    if (localConfig.value.depositType === 1) {
        // 校验定金比例
        const ratio = localConfig.value.depositRatio;
        if (ratio === null || ratio === undefined || (typeof ratio === "string" && ratio === "")) {
            callback(new Error(t('请输入定金比例')));
            return;
        }
        const ratioNum = Number(ratio);
        if (isNaN(ratioNum)) {
            callback(new Error(t('请输入有效的定金比例')));
            return;
        }
        if (ratioNum === 0) {
            callback(new Error(t('定金比例不能为0')));
            return;
        }
        if (ratioNum < 1 || ratioNum > 99) {
            callback(new Error(t('定金比例必须在1-99%之间')));
            return;
        }
        if (!Number.isInteger(ratioNum)) {
            callback(new Error(t('定金比例必须是整数，不能输入小数点')));
            return;
        }
    } else if (localConfig.value.depositType === 2) {
        // 校验定金金额
        const amount = localConfig.value.depositFixedAmount;
        if (amount === null || amount === undefined || (typeof amount === "string" && amount === "")) {
            callback(new Error(t('请输入定金金额')));
            return;
        }
        const amountNum = Number(amount);
        if (isNaN(amountNum)) {
            callback(new Error(t('请输入有效的定金金额')));
            return;
        }
        if (amountNum === 0) {
            callback(new Error(t('定金金额不能为0')));
            return;
        }
        if (amountNum < 0) {
            callback(new Error(t('定金金额必须大于0')));
            return;
        }
        if (amountNum >= minPrice.value) {
            callback(new Error(t('定金金额必须小于商品最低售价')));
            return;
        }
    }
    callback(); // 校验通过
};

// 统一的尾款支付时间校验
const validateFinalPaymentConfig = (rule: any, value: any, callback: any) => {
    if (localConfig.value.finalPaymentType === 1) {
        // 校验相对时间
        const relativeDays = localConfig.value.finalPaymentRelativeDays;
        if (
            relativeDays === null ||
            relativeDays === undefined ||
            (typeof relativeDays === "string" && relativeDays === "") ||
            (typeof relativeDays === "number" && (isNaN(relativeDays) || relativeDays < 0))
        ) {
            callback(new Error(t('请输入支付时间')));
            return;
        }

        const durationDays = localConfig.value.finalPaymentDurationDays;
        if (
            durationDays === null ||
            durationDays === undefined ||
            (typeof durationDays === "string" && durationDays === "") ||
            (typeof durationDays === "number" && (isNaN(durationDays) || durationDays < 0))
        ) {
            callback(new Error(t('请设置支付期限天数')));
            return;
        }

        const durationHours = localConfig.value.finalPaymentDurationHours;
        const durationDaysNum = Number(durationDays);

        // 如果天数为0，则小时数至少为1
        if (durationDaysNum === 0) {
            if (durationHours === null || durationHours === undefined || (typeof durationHours === "string" && durationHours === "")) {
                callback(new Error(t('请设置支付期限小时数')));
                return;
            }
            const durationHoursNum = Number(durationHours);
            if (isNaN(durationHoursNum) || durationHoursNum < 1) {
                callback(new Error(t('支付期限小时数至少为1')));
                return;
            }
        } else {
            // 如果天数大于0，小时数可以为0，但必须填写（不能为空）
            if (durationHours === null || durationHours === undefined || (typeof durationHours === "string" && durationHours === "")) {
                callback(new Error(t('请设置支付期限小时数')));
                return;
            }
            const durationHoursNum = Number(durationHours);
            if (isNaN(durationHoursNum) || durationHoursNum < 0) {
                callback(new Error(t('支付期限小时数不能小于0')));
                return;
            }
        }
    } else if (localConfig.value.finalPaymentType === 2) {
        // 校验固定时间
        if (!finalPaymentTimeRange.value || !Array.isArray(finalPaymentTimeRange.value) || finalPaymentTimeRange.value.length !== 2) {
            callback(new Error(t('请选择尾款支付时间')));
            return;
        }
        // 检查配置中的时间值
        if (!localConfig.value.finalPaymentStartTime || !localConfig.value.finalPaymentEndTime) {
            callback(new Error(t('请选择尾款支付时间')));
            return;
        }
    }
    callback(); // 校验通过
};

// 统一的发货时间校验
const validateDeliveryConfig = (rule: any, value: any, callback: any) => {
    if (localConfig.value.deliveryType === 1) {
        // 校验相对时间
        const relativeDays = localConfig.value.deliveryRelativeDays;
        const relativeHours = localConfig.value.deliveryRelativeHours;

        if (
            relativeDays === null ||
            relativeDays === undefined ||
            (typeof relativeDays === "string" && relativeDays === "") ||
            (typeof relativeDays === "number" && isNaN(relativeDays))
        ) {
            callback(new Error(t('请输入天数')));
            return;
        }

        if (
            relativeHours === null ||
            relativeHours === undefined ||
            (typeof relativeHours === "string" && relativeHours === "") ||
            (typeof relativeHours === "number" && isNaN(relativeHours))
        ) {
            callback(new Error(t('请输入小时数')));
            return;
        }
    } else if (localConfig.value.deliveryType === 2) {
        // 校验指定日期
        if (!deliveryDateValue.value) {
            callback(new Error(t('请选择发货日期')));
            return;
        }
        // 校验：发货时间不能比固定开始时间早（只有定金预售模式且尾款支付时间为固定时间类型才需要）
        if (localConfig.value.saleMode === 2 && localConfig.value.deliveryType === 2) {
            // 如果是固定时间类型的尾款支付
            if (localConfig.value.finalPaymentType === 2 && finalPaymentTimeRange.value && finalPaymentTimeRange.value[0]) {
                // 获取发货日期（转换为日期字符串进行比较）
                const deliveryDateStr = deliveryDateValue.value;
                // 获取尾款支付开始时间（提取日期部分）
                const finalPaymentStartDateStr = finalPaymentTimeRange.value[0].split(" ")[0];
                // 比较日期字符串（YYYY-MM-DD格式可以直接比较）
                if (deliveryDateStr < finalPaymentStartDateStr) {
                    callback(new Error(t('发货时间不能比固定开始时间早')));
                    return;
                }
            }
            // 如果是相对时间类型的尾款支付，验证逻辑较复杂，暂时不在这里处理
        }
    }
    callback(); // 校验通过
};
</script>

<style scoped lang="less">
.presale-config {
    .flex-row {
        display: flex;
        align-items: center;
    }

    .form-label {
        font-weight: 500;
        color: #333;
    }

    .error-tip {
        color: #f56c6c;
        font-size: 12px;
        // margin-top: 8px;
        line-height: 1.5;
    }

    // 预售方式
    .sale-mode-item {
        margin-bottom: 24px;

        :deep(.el-form-item__label) {
            width: 120px !important;
        }

        .sale-mode-wrapper {
            display: flex;
            align-items: center;
            justify-content: space-between;
            width: 100%;

            .refund-rules-link {
                cursor: pointer;
            }
        }
    }

    // 各个配置项
    .section-item {
        margin-bottom: 24px;

        :deep(.el-form-item__label) {
            width: 120px !important;
        }

        :deep(.el-form-item__content) {
            width: 100%;
            max-width: 100%;
        }
    }

    // 定金收取方式
    .deposit-type-group {
        display: flex;
        flex-direction: column;
        gap: 12px;
        width: 100%;

        .deposit-radio-item {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 4px;
            height: auto;
            margin-right: 0;
            padding: 0;
            line-height: 32px;

            :deep(.el-radio__label) {
                display: flex;
                align-items: center;
                flex-wrap: wrap;
                gap: 4px;
                padding-left: 8px;
            }

            .radio-text {
                color: #606266;
                font-size: 14px;
                white-space: nowrap;
            }

            .inline-input {
                margin: 0 2px;

                :deep(.el-input-group__append),
                :deep(.el-input-group__prepend) {
                    padding: 0 8px;
                    min-width: auto;
                }
            }

            .round-down-checkbox {
                margin-left: 8px;

                :deep(.el-checkbox__label) {
                    font-size: 14px;
                    color: #606266;
                    padding-left: 4px;
                }
            }
        }
    }

    // 预售有效期
    .validity-group {
        :deep(.el-radio) {
            margin-right: 0;
        }
    }

    // 预售定金
    .deposit-refund-group {
        :deep(.el-radio) {
            margin-right: 0;
        }
    }

    // 尾款支付时间
    .final-payment-group {
        display: flex;
        flex-direction: column;
        gap: 12px;

        .final-payment-radio-item {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 4px;
            height: auto;
            margin-right: 0;
            padding: 0;
            line-height: 32px;

            :deep(.el-radio__label) {
                display: flex;
                align-items: center;
                flex-wrap: wrap;
                gap: 4px;
                padding-left: 8px;
            }

            .radio-text {
                color: #606266;
                font-size: 14px;
                white-space: nowrap;
            }

            .inline-input {
                margin: 0 2px;

                :deep(.el-input-group__append),
                :deep(.el-input-group__prepend) {
                    padding: 0 8px;
                    min-width: auto;
                }
            }

            .date-picker-inline {
                margin-left: 8px;
            }
        }
    }

    // 发货时间
    .delivery-time-group {
        display: flex;
        flex-direction: column;
        gap: 12px;

        .delivery-radio-item {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 4px;
            height: auto;
            margin-right: 0;
            padding: 0;
            line-height: 32px;

            :deep(.el-radio__label) {
                display: flex;
                align-items: center;
                flex-wrap: wrap;
                gap: 4px;
                padding-left: 8px;
            }

            .radio-text {
                color: #606266;
                font-size: 14px;
                white-space: nowrap;
            }

            .inline-input {
                margin: 0 2px;

                :deep(.el-input-group__append),
                :deep(.el-input-group__prepend) {
                    padding: 0 8px;
                    min-width: auto;
                }
            }

            .date-picker-inline {
                margin-left: 8px;
            }
        }
    }

    .ml10 {
        margin-left: 10px;
    }
    .mr10 {
        margin-right: 10px;
    }

    // 额外说明文本
    .extra {
        display: block;
        color: #909399;
        font-size: 12px;
        margin-top: 8px;
        line-height: 1.5;
        white-space: normal !important;
        word-wrap: break-word;
        word-break: break-all;
        width: 100%;
        max-width: 100%;
        overflow-wrap: break-word;
    }
}

:deep(.el-radio-group) {
    align-items: start;
}

@media (max-width: 768px) {
    .presale-config {
        :deep(.el-form-item) {
            display: flex !important;
            flex-direction: column;
            align-items: flex-start;

            .el-form-item__label {
                width: auto !important;
                text-align: left;
                padding: 0 0 8px 0;
                line-height: 1.5;
            }

            .el-form-item__content {
                width: 100%;
                margin-left: 0 !important;
            }
        }

        :deep(.el-radio-group) {
            width: 100%;
        }

        .deposit-type-group .deposit-radio-item,
        .final-payment-group .final-payment-radio-item,
        .delivery-time-group .delivery-radio-item {
            white-space: normal;
            height: auto;
            align-items: flex-start;
            flex-wrap: wrap;
            width: 100%;

            :deep(.el-radio__input) {
                margin-top: 10px;
            }
            
            :deep(.el-radio__label) {
                display: flex;
                flex-wrap: wrap;
                gap: 4px;
                padding-left: 8px;
                width: calc(100% - 24px);
                align-items: center;
            }

            .radio-text {
                white-space: normal;
                line-height: 24px;
                width: 100%;
                margin: 4px 0;
            }

            .inline-input {
                margin-bottom: 4px;
                width: 100% !important;
                margin-left: 0 !important;
                margin-right: 0 !important;
                
                :deep(.el-input) {
                    width: 100% !important;
                }
            }
            
            .date-picker-inline {
                width: 100% !important;
                margin-left: 0;
                margin-top: 4px;
            }
        }
        
        .sale-mode-item {
            .sale-mode-wrapper {
                flex-wrap: wrap;
                gap: 10px;
                
                :deep(.el-radio-group) {
                    width: 100%;
                }
            }
        }
        
        .round-down-checkbox {
            margin-left: 0 !important;
            width: 100%;
        }
        
        :deep(.tig-input) {
            width: 100% !important;
        }
    }
}
</style>
