/**
 * @file 预约模板编辑页
 * @description 添加/编辑预约时间模板
 * @features
 * - 设置模板名称
 * - 配置预约时间规则（提前时间/预约单位）
 * - 设置预约时段和可预约天数
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                <el-form-item :rules="[{ required: true, message: '模板名称不能为空!' }]" :label="$t('模板名称')" prop="templateName">
                    <TigInput width="300px" v-model="formState.templateName" :placeholder="$t('请输入模板名称，最多20个字')"
                        :maxlength="20" showWordLimit />
                </el-form-item>
                <el-form-item :label="$t('预约时间')" prop="reservationType"
                    :rules="[{ required: formState.reservationType == 1 ? true : false, validator: validateReservationTime }]">
                    <div class="ml10">
                        <div>
                            <el-radio-group v-model="formState.reservationType">
                                <el-radio :value="0">{{ $t('无需提前') }}</el-radio>
                            </el-radio-group>
                        </div>
                        <div class="flex flex-align-center">
                            <el-radio-group v-model="formState.reservationType">
                                <el-radio :value="1">{{ $t('需提前') }}</el-radio>
                            </el-radio-group>
                            <div class="flex flex-align-center ml10">
                                <TigInput width="160px" v-model="formState.advanceNumber" type="integer"
                                    :disabled="formState.reservationType != 1">
                                    <template #append>
                                        <el-select v-model="formState.advanceUnit" style="width: 80px"
                                            :disabled="formState.reservationType != 1">
                                            <el-option :label="$t('天')" value="d" />
                                            <el-option :label="$t('小时')" value="h" />
                                        </el-select>
                                    </template>
                                </TigInput>
                                <p class="ml10">{{ $t('预约') }}</p>
                            </div>
                        </div>
                    </div>
                </el-form-item>
                <el-form-item :label="$t('最长预约')" prop="maxDays" :rules="[{ required: true, validator: validateValue }]">
                    <div>
                        <div class="flex flex-align-center ml10">
                            <p class="mr10">{{ $t('最长允许预约') }}</p>
                            <TigInput width="160px" v-model="formState.maxDays" type="integer" :maxNum="30">
                                <template #append> {{ $t('天') }} </template>
                            </TigInput>
                            <p class="ml10">{{ $t('内的订单') }}</p>
                        </div>
                        <div class="extra ml10">{{ $t('如：最长预约7天内订单，则买家最早可以选择当天+未来6天内的预约时间') }}</div>
                    </div>
                </el-form-item>
                <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                    <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                </el-form-item>
            </el-form>
            <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef, watch } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { ReservationTplFilterState } from "@/types/setting/appointmentTpl.d";
import { getReservationTpl, updateReservationTpl } from "@/api/setting/appointmentTpl";

/**
 * @description 父组件回调事件
 * @event submitCallback - 表单提交成功后的回调
 * @event update:confirmLoading - 更新确认按钮加载状态
 * @event close - 关闭弹窗
 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

/**
 * @property {number} id - 模板ID，编辑时传入
 * @property {string} act - 操作类型：'add'-新增, 'detail'-编辑
 * @property {boolean} isDialog - 是否在弹窗中显示
 */
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<ReservationTplFilterState>({
    templateName: "",
    reservationType: 0,
    advanceNumber: 0,
    advanceUnit: "d",
    maxDays: 0
});

/**
 * @description 获取预约模板详情数据
 */
const fetchShippingTpl = async () => {
    try {
        const result = await getReservationTpl(action.value, { id: id.value });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

/**
 * @description 验证预约时间配置
 * 业务规则：当选择"需提前"时，提前时间不能为空且必须小于最长预约时间
 * @param {any} rule - 验证规则
 * @param {any} value - 输入值
 * @param {Function} callback - 回调函数
 */
const validateReservationTime = (rule: any, value: any, callback: any) => {
    let advanceNumber = Number(formState.value.advanceNumber);
    let maxDays: number = Number(formState.value.maxDays);
    // 如果提前时间单位是小时，需要将最长预约天数转换为小时进行比较
    if (formState.value.advanceUnit == 'h') {
        maxDays = formState.value.maxDays ? Number(formState.value.maxDays) * 24 : 0;
    }
    if (formState.value.reservationType == 1 && !Number(formState.value.advanceNumber)) {
        callback(new Error(t('提前预约天数不能为空或为0!')));
    } else if (advanceNumber && maxDays && advanceNumber >= maxDays) {
        callback(new Error(t('提前预约时间需小于最长预约时间!')));
    } else {
        callback();
    }
};

/**
 * @description 验证最长预约天数
 * 业务规则：最长预约天数必须大于提前预约时间，且不能超过30天
 * @param {any} rule - 验证规则
 * @param {any} value - 输入值
 * @param {Function} callback - 回调函数
 */
const validateValue = (rule: any, value: any, callback: any) => {
    // 统一转换为小时进行比较，避免单位不一致导致的验证错误
    let advanceNumber = formState.value.advanceNumber;
    let maxDays = formState.value.maxDays;
    if (formState.value.advanceUnit == 'd') {
        advanceNumber = formState.value.advanceNumber ? Number(formState.value.advanceNumber) * 24 : '';
    }
    maxDays = formState.value.maxDays ? Number(formState.value.maxDays) * 24 : '';
    console.log(!Number(formState.value.maxDays));

    if (formState.value.maxDays && !Number(formState.value.maxDays)) {
        callback(new Error(t('最长预约天数不能为空或为0!')));
    } else if (advanceNumber && maxDays && advanceNumber >= maxDays) {
        callback(new Error(t('最长预约时间需大于提前预约时间!')));
    } else if (formState.value.maxDays && Number(formState.value.maxDays) > 30) {
        callback(new Error(t('最长预约天数不能超过30天!')));
    } else {
        callback();
    }
};

/**
 * @description 表单通过验证后提交预约模板数据
 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateReservationTpl(operation, { reservationTplId: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

/**
 * @description 表单提交入口，供父组件调用
 */
const onFormSubmit = () => {
    onSubmit();
};
defineExpose({ onFormSubmit });


onMounted(async () => {
    if (action.value === "detail") {
        // 获取详情数据
        await fetchShippingTpl();
    } else {
        loading.value = false;
    }
});
watch(
    () => formState.value.reservationType,
    (val) => {
        if (val === 0) formState.value.advanceNumber = '';
    }
);
</script>
<style lang="less" scoped>
.content_wrapper {
    :deep(.el-select) {
        min-width: 50px !important;
    }
}
</style>
