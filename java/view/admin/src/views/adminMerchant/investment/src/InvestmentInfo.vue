<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '流程标题不能为空!' }]" :label="$t('流程标题')" prop="title">
                        <!-- <TigInput classType="tig-form-input" v-model="formState.title" /> -->
                        <BusinessData v-model:modelValue="formState.title" :dataType="13" classType="tig-form-input"></BusinessData>
                    </el-form-item>
                    <el-form-item v-if="decorateSn == 'investmentPromotionHomepage' && index != 0" :label="$t('流程图')" prop="image">
                        <FormAddGallery v-model:photo="formState.image" pageType="overseas" :dataType="13"></FormAddGallery>
                        <div class="extra">{{ $t('提示：请根据具体需求设置对应大小的图片，高清需要上传双倍大小') }}</div>
                    </el-form-item>
                    <el-form-item v-if="decorateSn == 'investmentPromotionHomepage' && index == 0" :label="$t('轮播图')" prop="images">
                        <PicList :isMultiple="true" v-model:photos="formState.images" pageType="overseas" :dataType="13"></PicList>
                        <div class="extra">{{ $t('提示：请根据具体需求设置对应大小的图片，高清需要上传双倍大小') }}</div>
                    </el-form-item>
                    <el-form-item v-if="decorateSn != 'investmentPromotionHomepage'" :label="$t('流程描述')" prop="content">
                        <div class="relative">
                            <Editor v-model:html="formState.content" height="300px"></Editor>
                            <BusinessData v-model:modelValue="formState.content" :dataType="9" type="editor"></BusinessData>
                        </div>
                    </el-form-item>
                    <el-form-item v-if="decorateSn == 'investmentPromotionHomepage' && index != 0" :label="$t('流程描述')" prop="content">
                        <div class="relative">
                            <Editor v-model:html="formState.content" height="300px"></Editor>
                            <BusinessData v-model:modelValue="formState.content" :dataType="9" type="editor"></BusinessData>
                        </div>
                    </el-form-item>

                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { Editor } from "@/components/editor";
import { updateDecorateDiscrete } from "@/api/decorate/decorateDiscrete";
import { FormAddGallery } from "@/components/gallery";
import { PicList } from "@/components/decorate";
import BusinessData from "@/components/multilingual/BusinessData.vue";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    module: {
        type: Object,
        default: {}
    },
    decorateSn: {
        type: String,
        default: ""
    },
    index: {
        type: Number,
        default: 0
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const formRef = shallowRef();
const formState = ref({
    title: "",
    content: "",
    image: "",
    images: [],
    index: 0
});
onMounted(() => {
    Object.assign(formState.value, props.module[props.index]);
    formState.value.index = props.index;
    loading.value = false;
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        let temp = props.module;
        temp[props.index] = formState.value;
        const result = await updateDecorateDiscrete({
            decorateSn: props.decorateSn,
            data: temp
        });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
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
