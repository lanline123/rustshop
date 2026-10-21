/**
 * @file 商品转移分类表单
 * @description 用于将某分类下的所有商品转移到另一个分类的表单组件。
 * @features
 * - 显示当前分类（只读）
 * - 选择目标分类
 * - 批量转移该分类下的所有商品
 */
<template>
    <div>
        <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
            <el-form-item>
                <div class="extra"> {{ $t('什么是转移商品分类?') }} <br /> {{ $t('如果需要对商品的分类进行变更,那么你可以通过此功能,正确管理你的商品分类。') }} </div>
            </el-form-item>
            <el-form-item label="" prop="id">
                <SelectCategory v-if="!loading" v-model:categoryId="formState.id" :multiple="false" disabled></SelectCategory>
                <div class="extra" style="margin: 0 20px;">{{ $t('转移至') }}</div>
                <SelectCategory v-if="!loading" v-model:categoryId="formState.targetCatId" :multiple="false"></SelectCategory>
            </el-form-item>
            <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }} </el-button>
            </el-form-item>
        </el-form>
    </div>
</template>
<script lang="ts" setup>
import { SelectCategory } from "@/components/select";
import { onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { moveCategory } from "@/api/product/category";
import { useRouter } from "vue-router";
import { CategoryMoveState } from "@/types/product/category";

/** 加载状态 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const loading = ref<boolean>(true);

/**
 * @property {number} id - 源分类ID（必填）
 * @property {boolean} isDialog - 是否在弹窗中显示
 */
const props = defineProps({
    id: {
        type: Number,
        required: true
    },
    isDialog: Boolean
});
/** 表单引用 */
const formRef = ref();
/** 当前路由查询参数 */
const query = useRouter().currentRoute.value.query;
/** 源分类ID */
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
/**
 * @description 商品转移表单数据
 * @property {number} id - 源分类ID
 * @property {number} targetCatId - 目标分类ID
 */
const formState = ref<CategoryMoveState>({
    id: props.isDialog ? props.id : Number(query.id),
    targetCatId: 0
});
/**
 * @description 父组件回调事件
 * @event submitCallback - 表单提交成功后的回调
 * @event update:confirmLoading - 更新确认按钮加载状态
 * @event close - 关闭弹窗
 */
const emit = defineEmits([
    "submitCallback", //确认后回调
    "update:confirmLoading", //确认按钮的loading状态
    "close" //关闭弹窗
]);

onMounted(async () => {
    loading.value = false;
});

/**
 * @description 表单验证通过后执行商品转移
 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        console.log(formState.value);
        const result = await moveCategory({ ...formState.value });
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
    // 模拟点击提交Form表单
    onSubmit();
};

defineExpose({
    onFormSubmit
});
</script>
<style lang="less" scoped></style>
