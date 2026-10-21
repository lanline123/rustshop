<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-tabs v-model:activeKey="activeKey" class="lyecs-tabs" tab-position="top">
                    <el-tab-pane :key="1" :label="$t('基础设置')">
                        <div class="lyecs-form-table">
                            <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                                <el-form-item :rules="[{ required: true, message: '页面名称不能为空!' }]" :label="$t('页面名称')" prop="title">
                                    <TigInput classType="tig-form-input" v-model="formState.title" />
                                </el-form-item>
                                <el-form-item :label="$t('封面图片')" prop="img" :rules="[{ required: true, message: '封面图片不能为空!' }]">
                                    <FormAddGallery v-model:photo="formState.img"></FormAddGallery>
                                    <div class="extra">{{ $t('请统一上传比例为2:1的图片，宽600px，高450px（高清）') }}</div>
                                </el-form-item>
                                <el-form-item :label="$t('推送时间')" prop="startTime" :rules="[{ required: true, message: '推送时间不能为空!' }]">
                                    <el-date-picker
                                        v-model="formState.startTime"
                                        type="datetime"
                                        :placeholder="$t('请选择推送时间')"
                                        value-format="YYYY-MM-DD HH:mm:ss"
                                    />
                                </el-form-item>
                                <el-form-item :label="$t('失效时间')" prop="endTime">
                                    <el-date-picker
                                        v-model="formState.endTime"
                                        type="datetime"
                                        :placeholder="$t('请选择失效时间')"
                                        value-format="YYYY-MM-DD HH:mm:ss"
                                    />
                                </el-form-item>
                                <el-form-item :label="$t('页面描述')" prop="describe" :rules="[{ required: true, message: '页面描述不能为空!' }]">
                                    <TigInput classType="tig-form-input" v-model="formState.describe" :row="3" type="textarea" />
                                </el-form-item>
                                <el-form-item :label="$t('是否置顶')" prop="isTop">
                                    <el-radio-group v-model="formState.isTop">
                                        <el-radio :value="1">{{ $t('置顶') }}</el-radio>
                                        <el-radio :value="0">{{ $t('不置顶') }}</el-radio>
                                    </el-radio-group>
                                </el-form-item>
                                <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                                    <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                                </el-form-item>
                            </el-form>
                        </div>
                    </el-tab-pane>
                    <el-tab-pane :key="2" :label="$t('正文内容')">
                        <Editor v-model:html="formState.content"></Editor>
                    </el-tab-pane>
                </el-tabs>
                <a-spin :spinning="loading" style="width:100%;margin-top:100px"/>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { Editor } from "@/components/editor/index";
import type {noticeFormState} from "@/types/salesman/notice.d";
import {getnotice, updatenotice} from "@/api/salesman/notice";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

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
const activeKey = ref<number>(1);
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<noticeFormState>({
    isTop: 0,
    content: ""
});
const fetchBrand = async () => {
    try {
        const result = await getnotice(action.value, { id: id.value });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchBrand();
    } else {
        loading.value = false;
    }
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    if(formState.value.content === ""){
        message.error(t('正文内容不能为空!'));
        return;
    }
    try {
        emit("update:confirmLoading", true);
        const result = await updatenotice(operation, { id: id.value, ...formState.value });
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
