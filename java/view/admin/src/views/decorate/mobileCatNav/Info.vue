<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('选择分类')" :rules="[{ required: true, message: '名称不能为空!' }]" prop="categoryId">
                        <SelectCategory v-model:categoryId="formState.categoryId"></SelectCategory>
                    </el-form-item>
                    <el-form-item :label="$t('分类颜色')" prop="catColor">
                        <SelectColor v-model:color="formState.catColor"></SelectColor>
                    </el-form-item>
                    <el-form-item :label="$t('分类banner')" prop="catColor">
                        <FormAddGallery v-model:photos="formState.imgUrl" :isMultiple="true"></FormAddGallery>
                        <div class="extra">{{ $t('提示：您可以通过拖拽来调整相册图片顺序，请统一上传比例为2:1的图片') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('推荐子分类')" prop="childCatIds">
                        <SelectCategory v-model:categoryId="formState.childCatIds" :multiple="true"></SelectCategory>
                    </el-form-item>
                    <el-form-item :label="$t('推荐品牌')" prop="brandIds">
                        <!-- <div v-for="(item,key) in formState.brandIds" style="width: 100%;margin-bottom: 10px;"> -->
                            <SelectBrand v-model.brandIds="formState.brandIds" style="width:192px" multiple></SelectBrand>
                        <!-- </div> -->
                        <!-- <el-button type="primary" style="margin-top: 10px;" @click="formState.brandIds.push('')">增加</el-button> -->
                    </el-form-item>
                    <el-form-item :label="$t('排序')" prop="sortOrder">
                        <TigInput type="integer" width="192px" v-model="formState.sortOrder" />
                    </el-form-item>
                    <el-form-item :label="$t('是否显示')" prop="isShow">
                        <el-radio-group v-model="formState.isShow">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
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
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { SelectBrand, SelectCategory, SelectColor } from "@/components/select";
import { MobileCatNavFormState } from "@/types/decorate/mobileCatNav.d";
import { getMobileCatNavInfo, updateMobileCatNav } from "@/api/decorate/mobileCatNav";
import {DeleteFilled} from '@element-plus/icons-vue'
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    type: {
        type: Number,
        default: 0
    },
    typeName: {
        type: String,
        default: ""
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean,

});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<MobileCatNavFormState>({
    brandIds:[],
    isShow: 1,
    sortOrder: 50
});
const fetchPcNavigation = async () => {
    try {
        const result = await getMobileCatNavInfo(action.value, { id: id.value });
        if(result.brandIds == null){
            result.brandIds = []
        }
        Object.assign(formState.value, result);
        if (!Array.isArray(formState.value.imgUrl)){
            formState.value.imgUrl = []
        }
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
        fetchPcNavigation();
    } else {
        loading.value = false;
    }
});
// 表单通过验证后提交
const onSubmit = async () => {
    // console.log(formState)
    // return
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateMobileCatNav(operation, { id: id.value, ...formState.value });
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
