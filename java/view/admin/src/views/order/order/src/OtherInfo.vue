/**
 * @file 订单其他信息
 * @description 订单发票、留言等附加信息表单（功能待完善）
 * @features
 * - 发票类型和抬头
 * - 客户留言
 * - 缺货处理方式
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('发票类型')" prop="brandName">
                        <TigInput classType="tig-form-input" v-model="formState.brandName" />
                    </el-form-item>
                    <el-form-item :label="$t('发票抬头')" prop="brandName">
                        <TigInput classType="tig-form-input" v-model="formState.brandName" />
                    </el-form-item>
                    <el-form-item :label="$t('发票内容')" prop="brandName">
                        <TigInput classType="tig-form-input" v-model="formState.brandName" />
                    </el-form-item>
                    <el-form-item :label="$t('客户给商家的留言')" prop="brandName">
                        <TigInput classType="tig-form-input" v-model="formState.brandName" :autosize="{ minRows: 4, maxRows: 6 }" type="textarea" />
                    </el-form-item>
                    <el-form-item :label="$t('缺货处理')" prop="brandName">
                        <TigInput classType="tig-form-input" v-model="formState.brandName" />
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit(formRef)">{{ $t('提交') }} </el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from "vue";
import request from "@/utils/request";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits([
    "submitCallback", //确认后回调
    "update:confirmLoading", //确认按钮的loading状态
    "close" //关闭弹窗
]);
//获取来自父组件的参数
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: String,
    isDialog: Boolean
});
const action = ref();
const id = ref<number>(0); //表单Ref
const loading = ref(true);
if (!props.isDialog) {
    //获取来自路由的参数
    const { currentRoute } = useRouter();
    const query = <any>currentRoute.value.query;
    action.value = query.act; // add | edit
    id.value = query.id;
} else {
    // 获取来自组件的参数
    action.value = props.act; // add | edit
    id.value = props.id;
}

// 判断是处理更新还是添加
const operation = action.value == "add" ? "insert" : "update";

// 表单参数初使化
const formRef = ref(); //表单Ref
const formState = reactive<any>({}); //表单数据

onMounted(async () => {
    loading.value = false;
});

// 表单通过验证后提交
const onSubmit = async (e: any) => {
    await e.validate((valid: any, fields: any) => {
        if (valid) {
            // 确认按钮显示loading状态
            emit("update:confirmLoading", true);
            //values返回的是前端表单内有name的项
            new Promise((resolve, reject) => {
                request({
                    url: "brand/" + operation + "/",
                    method: "post",
                    data: {
                        id: id.value,
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
const submitBtn = ref<any>(null);
const onFormSubmit = () => {
    // 模拟点击提交Form表单
    submitBtn.value.$el.click();
};

defineExpose({
    onFormSubmit
});
</script>
