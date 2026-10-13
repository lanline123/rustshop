/**
 * @file 商品分类信息表单
 * @description 用于添加或编辑商品分类的表单组件。
 * @features
 * - 分类名称设置（支持多语言）
 * - SEO链接自动生成（海外版）
 * - 上级分类选择
 * - 分类图片上传
 * - SEO相关设置（标题、关键字、描述）
 * - 显示状态和热门状态设置
 * - 数量单位设置
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '分类名称不能为空!' }]" :label="$t('分类名称')" prop="categoryName">
                        <BusinessData v-model:modelValue="formState.categoryName" :placeholder="$t('请输入分类名称')" :dataType="3"
                            :dataId="id"></BusinessData>
                    </el-form-item>
                    <el-form-item :label="$t('分类短名')" prop="shortName">
                        <TigInput classType="tig-form-input" v-model="formState.shortName" />
                    </el-form-item>
                    <el-form-item :label="$t('上级分类')" prop="parentId">
                        <SelectCategory
                            v-if="!loading"
                            v-model:categoryId="formState.parentId"
                            :multiple="false"
                            :excludeName="props.act === 'add' ? '' : formState.categoryName"
                        >
                        </SelectCategory>
                    </el-form-item>
                    <el-form-item :label="$t('分类图片')" prop="categoryPic">
                        <FormAddGallery v-model:photo="formState.categoryPic"></FormAddGallery>
                        <div class="extra">{{ $t('请统一上传比例为2:1的图片，宽200px，高100px（高清）') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('数量单位')" prop="measureUnit">
                        <TigInput classType="tig-form-input" v-model="formState.measureUnit" />
                    </el-form-item>
                    <!-- <el-form-item label="ICO图标" prop="categoryIco">
                        <TigInput classType="tig-form-input" v-model="formState.categoryIco" />
                        <div class="extra">请先在Tigshop设置中的"接口设置"中设置"ico图标库地址"</div>
                    </el-form-item> -->
                    <el-form-item :label="$t('SEO标题')" prop="seoTitle">
                        <TigInput classType="tig-form-input" v-model="formState.seoTitle" />
                        <div class="extra">{{ $t('如果不填，则该分类页的标题则为默认标题；如果该项不为空，则该分类页的标题显示此项的内容。') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('热门搜索词')" prop="searchKeywords" v-if="false">
                        <TigInput classType="tig-form-input" v-model="formState.searchKeywords" />
                    </el-form-item>
                    <el-form-item :label="$t('关键字')" prop="keywords">
                        <TigInput classType="tig-form-input" v-model="formState.keywords" />
                    </el-form-item>
                    <el-form-item :label="$t('分类描述')" prop="categoryDesc">
                        <TigInput classType="tig-form-input" v-model="formState.categoryDesc" :row="2"
                            type="textarea" />
                    </el-form-item>
                    <el-form-item :label="$t('排序')" prop="sortOrder">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.sortOrder" />
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
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef, watch } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { SelectCategory } from "@/components/select";
import { useAppStore } from "@/store/app";
import { CategoryFormState } from "@/types/product/category";
import { getCategory, updateCategory } from "@/api/product/category";
import { AnyObject } from "ant-design-vue/es/_util/type";
import { useCategoryStore } from "@/store/category";
import BusinessData from "@/components/multilingual/BusinessData.vue"
import { isOverseas } from "@/utils/version";
import { getTranslateToSeoIdentification } from "@/api/setting/seo";
import { QuestionFilled } from "@element-plus/icons-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const appStore = useAppStore();
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
/**
 * @property {number} id - 分类ID
 * @property {string} act - 操作类型：'add'-新增, 'detail'-编辑
 * @property {boolean} isDialog - 是否在弹窗中显示
 * @property {number} parentId - 父分类ID
 */
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean,
    parentId: Number
});

/** 加载状态 */
const loading = ref<boolean>(true);
/** SEO链接生成加载状态 */
const seoLoading = ref<boolean>(false);
/** 当前路由查询参数 */
const query = useRouter().currentRoute.value.query;
/** 当前操作类型 */
const action = ref<string>(props.isDialog ? props.act : String(query.act));
/** 分类ID */
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
/** API操作类型 */
const operation = action.value === "add" ? "create" : "update";
/** 表单引用 */
const formRef = shallowRef();
/**
 * @description 分类表单数据
 * @property {number} isShow - 是否显示：0-否，1-是
 * @property {number} sortOrder - 排序值
 */
const formState = ref<CategoryFormState>({
    isShow: 1,
    sortOrder: 50
});
/**
 * @description 获取分类详情数据
 */
const fetchCategory = async () => {
    try {
        const result = await getCategory(action.value, { id: id.value });
        // const additionalData = action.value === 'add' ? {parentId: props.parentId} : {};
        Object.assign(
            formState.value,
            result
            // additionalData
        );
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
        fetchCategory();
    } else {
        loading.value = false;
    }
    if (operation === "create") {
        formState.value.parentId = props.parentId;
        console.log(formState.value.parentId);
    }
});

/**
 * @description 自动生成SEO链接
 * 根据分类名称翻译生成英文SEO标识
 */
const onGenerateSeoLink = async () => {
    try {
        seoLoading.value = true;
        const result = await getTranslateToSeoIdentification(formState.value.categoryName || "");
        formState.value.seoIdentification = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        seoLoading.value = false;
    }
};

/**
 * @description 验证SEO链接，需先填写分类名称
 */
const validateSeoIdentification = (rule: any, value: any, callback: any) => {
    if (!formState.value.categoryName && !value) {
        callback(new Error(t('请先输入分类名称')));
        return;
    }
    callback();
};

/**
 * @description 表单验证通过后提交分类数据
 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateCategory(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
        const categoryStore = useCategoryStore();
        categoryStore.clearCategory();
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


watch(() => formState.value.categoryName, (newVal: string | undefined) => {
    if (newVal) {
        formRef.value?.validateField("seoIdentification");
    }
}, { immediate: true });

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.keywords {
    display: flex;
    align-items: center;
    width: 100%;
    gap: 8px;
}

.table-question {
    display: flex;
    align-items: center;
    gap: 4px;
}

.pre-wrap {
    display: block;
    max-width: 250px;
    white-space: pre-wrap;
}
</style>
