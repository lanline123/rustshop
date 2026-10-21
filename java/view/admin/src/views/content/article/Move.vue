<template>
    <div>
        <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
            <el-form-item> </el-form-item>
            <el-form-item :label="$t('转移至')" prop="targetCat">
                <SelectArticleCategory :min-width="'100%'" v-model="formState.targetCat"></SelectArticleCategory>
            </el-form-item>
            <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit(formRef)">{{ $t('提交') }} </el-button>
            </el-form-item>
        </el-form>
    </div>
</template>
<script setup lang="ts">
import { SelectArticleCategory } from "@/components/select";
import { ref, reactive, onMounted } from "vue";
import request from "@/utils/request";
import { message } from "ant-design-vue";
import type { FormInstance } from "element-plus";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const loading = ref(true);
//获取来自父组件的参数
const props = defineProps({
    ids: {
        type: Array,
        default: () => []
    },
    isDialog: Boolean
});
// 表单参数初使化
const formRef = ref<FormInstance>(); //表单Ref
const formState = reactive<any>({
    ids: props.ids,
    targetCat: []
}); //表单数据
// 父组件回调
const emit = defineEmits([
    "submitCallback", //确认后回调
    "update:confirmLoading", //确认按钮的loading状态
    "close" //关闭弹窗
]);

onMounted(async () => {
    // 获取详情数据
    loading.value = false;
});

// 表单通过验证后提交
const onSubmit = async (e: FormInstance | undefined) => {
    if (!e) return;
    await e.validate((valid, fields) => {
        if (valid) {
            // 确认按钮显示loading状态
            emit("update:confirmLoading", true);
            //values返回的是前端表单内有name的项
            new Promise((resolve, reject) => {
                request({
                    url: "content/article/batch",
                    method: "post",
                    data: {
                        ids: props.ids.join(","),
                        type: "moveCat",
                        ...formState
                    }
                })
                    .then((result: any) => {
                        // 延时关闭确认按钮loading状态
                        setTimeout(() => {
                            emit("update:confirmLoading", false);
                        }, 600);
                        // 如果是弹窗，回调给弹窗父组件，执行关闭弹窗等操作
                        message.success(t('操作成功'));
                        emit("submitCallback", result);
                    })
                    .catch((error: any) => {
                        message.error(error.message);
                        emit("submitCallback", error);
                    });
            });
        }
    });
};

// 处理
const submitBtn = ref();
const onFormSubmit = () => {
    // 模拟点击提交Form表单
    submitBtn.value.$el.click();
};

defineExpose({
    onFormSubmit
});
</script>
<style scoped lang="less"></style>
