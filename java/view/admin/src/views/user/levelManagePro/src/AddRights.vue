/**
 * @file 添加/编辑自定义权益表单
 * @description 用于创建或编辑会员自定义权益的弹窗表单组件。
 * @features
 * - 权益名称设置
 * - 展示名称设置
 * - 权益图标上传
 * - 权益简介编辑
 * - 权益价值设置
 * - 支持新增和编辑两种模式
 */
<template>
    <div class="container">
        <el-form ref="formRef" :rules="rules" :model="formState" label-width="auto">
            <el-form-item prop="name" :label="$t('权益名称')" :rules="rules.name">
                <TigInput width="100%" v-model="formState.name"></TigInput>
            </el-form-item>
            <el-form-item prop="showName" :label="$t('展示名称')" :rules="rules.showName">
                <TigInput width="100%" v-model="formState.showName"></TigInput>
            </el-form-item>
            <el-form-item prop="icon" :label="$t('权益图标')" :rules="rules.icon">
                <FormAddGallery v-model:photo="formState.icon"></FormAddGallery>
            </el-form-item>
            <el-form-item prop="describe" :label="$t('权益简介')" :rules="rules.describe">
                <TigInput width="100%" type="textarea" :rows="4" v-model="formState.describe" :placeholder="$t('请输入权益简介')"></TigInput>
            </el-form-item>
            <el-form-item prop="value" :label="$t('权益价值')">
                {{ $t('该权益价值') }}&nbsp;&nbsp;<TigInput style="width: 100px" type="decimal" :min="0" v-model="formState.value" :placeholder="$t('请输入')"></TigInput
                >&nbsp;&nbsp;{{ config.config.amountUnit }}
            </el-form-item>
            <el-form-item :label="$t('服务方式')"> {{ $t('商家线下核销') }} </el-form-item>
        </el-form>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, reactive, onMounted } from "vue";
import type { FormRules } from "element-plus";
import { FormAddGallery } from "@/components/gallery";
import { useConfigStore } from "@/store/config";

const config: any = useConfigStore();
/**
 * @description 父组件回调事件
 * @event submitCallback - 表单提交成功后的回调，返回权益数据
 * @event editCallback - 编辑回调
 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "editCallback"]);
/**
 * @property {string} act - 操作类型：'add'-新增, 'edit'-编辑
 * @property {number} rowIndex - 编辑时的行索引
 * @property {Object} data - 编辑时的权益数据
 */
const props = defineProps({
    act: {
        type: String,
        default: "add"
    },
    rowIndex: {
        type: Number,
        default: 0
    },
    data: {
        type: Object,
        default: {}
    }
});

/** 表单引用 */
const formRef = shallowRef();
/**
 * @description 表单数据状态
 * @property {string} name - 权益名称
 * @property {string} showName - 展示名称
 * @property {string} icon - 权益图标URL
 * @property {number} value - 权益价值（元）
 * @property {string} describe - 权益简介
 * @property {number} isChecked - 权益是否选中：0-否，1-是
 */
const formState = ref<any>({
    name: "",
    showName: "", // 展示名称
    icon: "", // 权益图标
    value: 0, // 权益价值
    describe: "", // 权益描述
    isChecked: 0 // 权益是否选中
});
interface RuleForm {
    name: string;
    showName: string;
    icon: string;
    describe: string;
}
const rules = reactive<FormRules<RuleForm>>({
    name: [{ required: true, message: t('请输入权益名称'), trigger: "blur" }],
    showName: [{ required: true, message: t('请输入权益显示'), trigger: "blur" }],
    icon: [{ required: true, message: t('请输入权益图标'), trigger: "blur" }],
    describe: [{ required: true, message: t('请输入权益简介'), trigger: "blur" }]
});
/**
 * @description 表单验证通过后提交权益数据
 */
const onSubmit = async () => {
    await formRef.value.validate();
    emit("submitCallback", { row: formState.value, act: props.act, index:props.rowIndex });
};
/**
 * @description 表单提交入口，供父组件调用
 */
const onFormSubmit = () => {
    onSubmit();
};
onMounted(() => {
    if(props.act === "edit"){
        formState.value = props.data;
    }
});

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped></style>
