/**
 * @file 品牌审核表单
 * @description 用于审核待审核品牌的表单组件，支持通过或拒绝审核。
 * @features
 * - 审核通过/不通过选择
 * - 审核失败原因填写（拒绝时必填）
 * - 表单验证和提交
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('处理申请')" prop="status">
                        <el-radio-group style="width: 100%" v-model="formState.status">
                            <el-radio :value="1">{{ $t('审核通过') }}</el-radio>
                            <el-radio :value="2">{{ $t('审核不通过') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item v-if="formState.status == 2" :label="$t('审核失败原因')" prop="rejectRemark" :rules="[{ required: true, message: '审核失败原因不能为空!' }]">
                        <TigInput classType="tig-form-input" v-model="formState.rejectRemark" cols="40" :rows="2" type="textarea"></TigInput>
                    </el-form-item>
                    <el-form-item v-show="!isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()">{{ $t('提交') }} </el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { auditBrandForm } from "@/types/product/brand.d";
import { auditBrand } from "@/api/product/brand";
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore().config;

/**
 * @description 父组件回调事件
 * @event submitCallback - 表单提交成功后的回调
 * @event update:confirmLoading - 更新确认按钮加载状态
 * @event close - 关闭弹窗
 */
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

/**
 * @property {number} id - 品牌ID
 * @property {boolean} isDialog - 是否在弹窗中显示
 */
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    isDialog: Boolean
});
/** 加载状态 */
const loading = ref<boolean>(false);
/** 当前路由查询参数 */
const query = useRouter().currentRoute.value.query;
/** 品牌ID */
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
/** 表单引用 */
const formRef = shallowRef();
/**
 * @description 审核表单数据
 * @property {number} brandId - 品牌ID
 * @property {number} status - 审核状态：1-通过，2-不通过
 * @property {string} rejectRemark - 拒绝原因
 */
const formState = ref<auditBrandForm>({
    brandId: id.value,
    status: 1,
    rejectRemark: "",
});

/**
 * @description 表单验证通过后提交审核结果
 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await auditBrand({ ...formState.value });
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
</script>
<style lang="less" scoped>
.content_wrapper {
    padding: 10px 28px;
}
</style>
