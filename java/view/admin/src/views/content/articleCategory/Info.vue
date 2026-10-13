<template>
    <div class="container">
        <div class="content_wrapper">
            <div v-if="!loading" class="lyecs-form-table">
                <el-form ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '文章分类名称不能为空!' }]" extra="sfs" :label="$t('文章分类名称')" prop="articleCategoryName">
                        <TigInput classType="tig-form-input" v-model="formState.articleCategoryName" />
                    </el-form-item>
                    <el-form-item :label="$t('上级分类')" prop="parentId">
                        <SelectArticleCategory
                            :min-width="'100%'"
                            v-model="formState.parentId"
                            :disabled="action == 'add' || parentId.length > 0"
                        ></SelectArticleCategory>
                    </el-form-item>
                    <el-form-item :label="$t('关键字')" prop="keywords">
                        <TigInput classType="tig-form-input" v-model="formState.keywords" />
                    </el-form-item>
                    <el-form-item :label="$t('描述')" prop="description">
                        <TigInput classType="tig-form-input" v-model="formState.description" :row="2" type="textarea" />
                        <div class="extra">{{ $t('关键字为选填项，其目的在于方便外部搜索引擎搜索') }}</div>
                    </el-form-item>
                    <el-form-item prop="sortOrder" :label="$t('排序')">
                        <TigInput type="integer" v-model="formState.sortOrder" />
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }} </el-button>
                    </el-form-item>
                </el-form>
            </div>
            <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { FormState } from "@/types/content/articleCategory.d";
import { getArticleCategory, updateArticleCategory } from "@/api/content/articleCategory";
import SelectArticleCategory from "@/components/select/src/SelectArticleCategory.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    parentId: {
        type: Array,
        default: []
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});

const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<FormState>({
    articleCategoryName: "",
    parentId: props.parentId,
    keywords: "",
    description: "",
    sortOrder: 50
});

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchArticle();
    } else {
        loading.value = false;
    }
});
const fetchArticle = async () => {
    try {
        loading.value = true;
        const result = await getArticleCategory(action.value, { id: id.value });
        Object.assign(formState.value, result);
        if (props.parentId) {
            formState.value.parentId = props.parentId;
        }
    } catch (error: any) {
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
        if (Array.isArray(formState.value.parentId) && formState.value.parentId.length > 0) {
            formState.value.parentId = formState.value.parentId[formState.value.parentId.length - 1];
        } else {
            formState.value.parentId = 0;
            if (props.parentId && props.act === "add") {
                formState.value.parentId = props.parentId;
            }
        }
        const result = await updateArticleCategory(operation, { id: id.value, ...formState.value });
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
