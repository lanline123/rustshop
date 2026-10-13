<template>
    <div class="lyecs-popedit">
        <slot></slot>
        <span class="lyecs-popedit-btn iconfont icon-bianji1" @click="editBtn"></span>
        <!-- v-if="loaded" 用于优化表格中的加载性能 -->
        <div v-if="loaded" style="position: relative">
            <Popconfirm v-model:open="visible" placement="bottomLeft" :okText="$t('确认')" :cancelText="$t('取消')" @confirm="onConfirm" @cancel="onCancel">
                <div style="position: absolute; left: -20px; top: -15px">&nbsp;</div>
                <template #description>
                    <div class="popForm-con">
                        <el-form ref="formRef" :model="formState" layout="horizontal" name="form_in_modal" :wrapper-col="{ span: 38 }" :label-wrap="false">
                            <el-form-item prop="paramValue" :label="label" :rules="rules" :extra="extra" :max="max">
                                <TigInput :type="type" v-model="formState.paramValue" :max="maxNum" :min="minNum" />
                                <div class="extra" v-if="extra">{{ extra }}</div>
                            </el-form-item>
                        </el-form>
                    </div>
                </template>
                <template #icon></template>
            </Popconfirm>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, reactive, shallowRef } from "vue";
import request from "@/utils/request";
import { message, Popconfirm } from "ant-design-vue";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    orgValue: [String, Number], //父组件使用v-model双向绑定
    requestApi: { type: Function, default: request },
    label: { type: String, default: "编辑项" },
    extra: { tyoe: String, default: "" },
    max: { type: Number, default: 0 },
    min: { type: Number, default: 0 },
    maxNum: { type: Number, default: 0 },
    minNum: { type: Number, default: 0 },
    len: { type: Number, default: 0 },
    required: { type: Boolean, default: true },
    type: { type: String, default: "input" },
    params: {
        type: Object,
        default: { id: null, field: "" }
    }
});
const rules = ref<any>([]);
const validateValue = (rule: any, value: any, callback: any) => {
    let paramValue = formState.paramValue;
    if (!paramValue && props.required) {
        callback(new Error(t('请输入') + props.label));
        return;
    }
    if (!paramValue && !props.required) {
        callback();
        return;
    }
    const isNumber = props.type === "number" || props.type === "decimal" || props.type === "integer";
    if (isNumber && isNaN(paramValue)) {
        callback(new Error(t('请输入数字')));
        return;
    }
    if (props.type === "integer" && !/^\d+$/.test(String(paramValue))) {
        callback(new Error(t('请输入正整数')));
        return;
    }
    if (isNumber && props.minNum > 0 && paramValue < props.minNum) {
        callback(new Error(t('请输入不少于') + props.minNum + t('的数字')));
        return;
    }
    if (isNumber && props.maxNum > 0 && paramValue > props.maxNum) {
        callback(new Error(t('请输入不大于') + props.maxNum + t('的数字')));
        return;
    }
    callback();
};
rules.value.push({
    required: props.required,
    validator: validateValue,
    trigger: "blur"
});
const extra = ref(props.extra);
if (props.max > 0) {
    rules.value.push({
        max: props.max
    });
    if (!extra.value) {
        extra.value = t('请输入不超') + props.max + t('个字符的') + props.label;
    }
}
if (props.min > 0) {
    rules.value.push({
        min: props.min
    });
    if (!extra.value) {
        extra.value = t('请输入不少于') + props.min + t('个字符的') + props.label;
    }
}
if (props.len > 0) {
    rules.value.push({
        len: props.len
    });
    if (!extra.value) {
        extra.value = t('请输入') + props.len + t('个字符的') + props.label;
    }
}
if (props.minNum > 0 && props.maxNum > 0) {
    rules.value.push({
        minNum: props.minNum,
        maxNum: props.maxNum
    });
    if (!extra.value) {
        extra.value = t('请输入') + props.minNum + "-" + props.maxNum + t('之间的数字');
    }
}

const visible = ref<any>(false);
const loaded = ref<Boolean>(false);
const type = ref(props.type);
const formRef = shallowRef();
let formState = reactive<any>({
    //表单数据
    paramValue: props.orgValue
});
const emit = defineEmits(["update:orgValue", "callback"]);
const loading = ref<Boolean>(false);

const onConfirm = async () => {
    await formRef.value.validate();
    try {
        loading.value = true;
        const result = await props.requestApi({
            id: props.params.id,
            field: props.params.field,
            val: formState.paramValue
        });
        emit("update:orgValue", formState.paramValue);
        emit("callback");
        message.success(result.message || t('操作成功'));
        visible.value = false;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const editBtn = () => {
    loaded.value = true;
    visible.value = true;
    formState.paramValue = props.orgValue;
};

const show = (e: any) => {
    visible.value = true;
};
const onCancel = () => {
    // 还原表单的值
    formState.paramValue = props.orgValue;
    // 清除验证信息
    formRef.value.clearValidate();
};

defineExpose({
    show
    // close
});
</script>

<style lang="less" scoped>
.popForm-con {
    width: 370px;
    position: relative;
    margin-left: -20px;
}

.lyecs-popedit {
    display: flex;
    position: relative;
    align-items: center;
}

.lyecs-popedit .lyecs-popedit-btn {
    cursor: pointer;
    color: #999;
    font-size: 16px;
    padding-left: 2px;
    line-height: 1;
}
</style>
