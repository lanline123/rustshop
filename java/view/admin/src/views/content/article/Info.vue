<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form ref="formRef" :model="formState" label-width="auto">
                    <el-tabs v-model="activeKey" class="lyecs-tabs" tab-position="top">
                        <el-tab-pane :name="1" :label="$t('基本信息')">
                            <el-form-item :rules="[{ required: true, message: '文章标题不能为空!' }]" extra="sfs" :label="$t('文章标题')"
                                prop="articleTitle">
                                <BusinessData v-model:modelValue="formState.articleTitle" :placeholder="$t('请输入文章标题')"
                                    :dataType="6" :dataId="id"></BusinessData>
                            </el-form-item>
                            <el-form-item :label="$t('文章编号')" prop="articleSn">
                                <TigInput classType="tig-form-input" v-model="formState.articleSn" />
                                <div class="extra">{{ $t('编号用于前台调用某些指定的文章') }}</div>
                            </el-form-item>
                            <el-form-item :label="$t('文章分类')" prop="articleCategoryId"
                                :rules="[{ required: true, message: '请选择文章分类!' }]">
                                <SelectArticleCategory v-if="!loading" v-model="formState.articleCategoryId"
                                    :min-width="'100%'">
                                </SelectArticleCategory>
                                <!-- <SelectArticleCategory v-model="formState.articleCategoryId" :min-width="'100%'"></SelectArticleCategory> -->
                            </el-form-item>
                            <el-form-item :label="$t('缩略图')" prop="articleThumb">
                                <FormAddGallery v-model:photo="formState.articleThumb"></FormAddGallery>
                                <div class="extra">{{ $t('请统一上传比例为1:2的图片，宽100px，高200px（高清）') }}</div>
                            </el-form-item>
                            <el-form-item :label="$t('文章作者')" prop="articleAuthor">
                                <TigInput classType="tig-form-input" v-model="formState.articleAuthor" />
                            </el-form-item>
                            <el-form-item :label="$t('关键字')" prop="keywords">
                                <TigInput classType="tig-form-input" v-model="formState.keywords" />
                            </el-form-item>
                            <el-form-item :label="$t('标签')" prop="articleTag">
                                <TigInput classType="tig-form-input" v-model="formState.articleTag" />
                            </el-form-item>
                            <el-form-item :label="$t('描述')" prop="description">
                                <TigInput classType="tig-form-input" v-model="formState.description" :row="2"
                                    type="textarea" />
                            </el-form-item>
                            <el-form-item :label="$t('相关链接')" prop="link">
                                <TigInput classType="tig-form-input" v-model="formState.link" />
                            </el-form-item>
                            <el-form-item :label="$t('文章类型')" prop="articleType">
                                <el-radio-group v-model="formState.articleType">
                                    <el-radio :value="1">{{ $t('普通文章') }}</el-radio>
                                    <el-radio :value="2">{{ $t('帮助文章') }}</el-radio>
                                </el-radio-group>
                            </el-form-item>
                            <el-form-item :label="$t('是否置顶')" prop="isTop">
                                <el-radio-group v-model="formState.isTop">
                                    <el-radio :value="1">{{ $t('是') }}</el-radio>
                                    <el-radio :value="0">{{ $t('否') }}</el-radio>
                                </el-radio-group>
                            </el-form-item>
                            <el-form-item :label="$t('是否热门')" prop="isHot">
                                <el-radio-group v-model="formState.isHot">
                                    <el-radio :value="1">{{ $t('是') }}</el-radio>
                                    <el-radio :value="0">{{ $t('否') }}</el-radio>
                                </el-radio-group>
                            </el-form-item>
                            <el-form-item :label="$t('是否显示')" prop="isShow">
                                <el-radio-group v-model="formState.isShow">
                                    <el-radio :value="1">{{ $t('是') }}</el-radio>
                                    <el-radio :value="0">{{ $t('否') }}</el-radio>
                                </el-radio-group>
                            </el-form-item>
                            <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                                <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }} </el-button>
                            </el-form-item>
                        </el-tab-pane>
                        <el-tab-pane :name="2" :label="$t('文章内容')">
                            <template v-if="activeKey == 2">
                                <div class="relative">
                                    <Editor v-model:html="formState.content"></Editor>
                                    <BusinessData v-model:modelValue="formState.content" :dataType="12" type="editor">
                                    </BusinessData>
                                </div>
                            </template>
                        </el-tab-pane>
                        <el-tab-pane :name="3" :label="$t('关联商品')">
                            <el-form-item label="" prop="productIds">
                                <SelectProduct v-if="!loading" v-model:ids="formState.productIds" :max="10">
                                </SelectProduct>
                                <div class="extra">{{ $t('最多添加10个商品，添加的商品会在结算后自动加入订单（不会计价格）') }}</div>
                            </el-form-item>
                        </el-tab-pane>
                    </el-tabs>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { ArticleFormState } from "@/types/content/article.d";
import { getArticle, updateArticle } from "@/api/content/article";
import { SelectArticleCategory, SelectProduct } from "@/components/select";
import { Editor } from "@/components/editor/index";
import BusinessData from "@/components/multilingual/BusinessData.vue";
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
const formState = ref<ArticleFormState>({
    isShow: 1,
    isHot: 0,
    isTop: 0,
    articleType: 1,
    articleCategoryId: [],
    productIds: []
});
const validateValue = (rule: any, value: any, callback: any) => {
    if (value.length > 10) {
        message.error(t('最多添加10个商品'));
        callback(new Error(t('最多添加10个商品')));
        return;
    }
    callback();
};
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
        const result = await getArticle(action.value, { id: id.value });
        Object.assign(formState.value, result);
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
        const result = await updateArticle(operation, { id: id.value, ...formState.value });
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
