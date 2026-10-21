/**
 * @file 店铺商品分类表单
 * @description 添加/编辑店铺自定义商品分类
 * @features
 * - 设置分类名称
 * - 选择上级分类
 * - 上传分类图片
 * - 设置排序和显示状态
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '分类名称不能为空!' }]" :label="$t('分类名称')" prop="categoryName">
                        <TigInput classType="tig-form-input" v-model="formState.categoryName" />
                    </el-form-item>
                    <el-form-item :label="$t('上级分类')" prop="parentId">
                        <SelectShopProductCategory v-if="!loading" v-model:categoryId="formState.parentId" :multiple="false" style="width: 100%"></SelectShopProductCategory>
                    </el-form-item>
                    <el-form-item :label="$t('分类图片')" prop="categoryPic">
                        <FormAddGallery v-model:photo="formState.categoryPic"></FormAddGallery>
                    </el-form-item>
                    <el-form-item :label="$t('排序')" prop="sortOrder">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.sortOrder" />
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
import { onMounted, ref, shallowRef } from "vue";
import { FormAddGallery } from "@/components/gallery";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import {  SelectShopProductCategory } from "@/components/select";
import { useAppStore } from "@/store/app";
import { ShopProductCategoryFormState } from "@/types/product/shopProductCategory";
import { getShopProductCategory, updateShopProductCategory } from "@/api/product/shopProductCategory";
import { useShopProductCategoryStore } from "@/store/shopProductCategory";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const appStore = useAppStore();
// 父组件回调
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
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean,
    parentId: Number
});

const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<ShopProductCategoryFormState>({
    isShow: 1,
    sortOrder: 50
});
const fetchShopProductCategory = async () => {
    try {
        const result = await getShopProductCategory(action.value, { id: id.value });
        Object.assign(
            formState.value,
            result
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
        fetchShopProductCategory();
    } else {
        loading.value = false;
    }
    if (operation === "create") {
        formState.value.parentId = props.parentId;
        console.log(formState.value.parentId);
    }
});

// 表单通过验证后提交

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateShopProductCategory(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
        const categoryStore = useShopProductCategoryStore();
        categoryStore.clearShopProductCategory();
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
