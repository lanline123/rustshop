/**
 * @file 示例模板表单
 * @description 添加/编辑示例模板（开发示例代码）
 * @features
 * - 模板名称和描述设置
 * - LOGO图片上传
 * - 排序和显示状态设置
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="example_name" :label="$t('示例模板名称')" :rules="[{ required: true, message: '示例模板名称不能为空!' }]">
                        <TigInput classType="tig-form-input" v-model="formState.example_name" />
                    </el-form-item>
                    <el-form-item prop="example_logo" :label="$t('示例模板LOGO')">
                        <FormAddGallery v-model:photo="formState.example_logo"></FormAddGallery>
                        <div class="extra">{{ $t('请统一上传比例为2:1的图片，宽200px，高100px（高清）') }}</div>
                    </el-form-item>
                    <el-form-item prop="example_desc" :label="$t('示例模板描述')">
                        <TigInput classType="tig-form-input" type="textarea" :row="2" v-model="formState.example_desc" />
                    </el-form-item>
                    <el-form-item prop="sort_order" :label="$t('排序')">
                        <TigInput classType="tig-form-input" v-model="formState.sort_order" />
                    </el-form-item>
                    <el-form-item prop="is_show" :label="$t('是否显示')">
                        <el-radio-group v-model="formState.is_show">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import type { ExampleFormState } from "@/types/product/example.d";
import { getExample, updateExample } from "@/api/product/example";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: Number,
    act: String,
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "insert" : "update";
const formRef = shallowRef();
const formState = ref<ExampleFormState>({});

onMounted(() => {
    // 获取详情数据
    fetchExample();
});
const fetchExample = async () => {
    try {
        const result = await getExample({ id: id.value });
        Object.assign(formState.value, result);
    } catch (error) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateExample(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
