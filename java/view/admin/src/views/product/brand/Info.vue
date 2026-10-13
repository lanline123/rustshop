/**
 * @file 品牌信息表单
 * @description 用于添加或编辑商品品牌的表单组件。
 * @features
 * - 品牌名称设置（支持多语言）
 * - 品牌LOGO上传
 * - 品牌描述编辑
 * - 排序设置（仅平台管理员）
 * - 首字母设置（可自动生成）
 * - 显示状态和热门状态设置
 * - 审核查看模式（只读）
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '品牌名称不能为空!' }]" :label="$t('品牌名称')" prop="brandName">
                        <BusinessData
                            v-model:modelValue="formState.brandName"
                            :placeholder="$t('请输入品牌名称')"
                            :dataType="4"
                            :dataId="id"
                            :disabled="examine == 1"
                        ></BusinessData>
                    </el-form-item>
                    <el-form-item :label="$t('品牌LOGO')" prop="brandLogo">
                        <FormAddGallery v-model:photo="formState.brandLogo" :disabled="examine == 1"></FormAddGallery>
                        <div class="extra">{{ $t('请统一上传比例为2:1的图片，宽200px，高100px（高清）') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('品牌描述')" prop="brandDesc">
                        <TigInput classType="tig-form-input" v-model="formState.brandDesc" :row="2" type="textarea" :disabled="examine == 1" />
                    </el-form-item>
                    <el-form-item :label="$t('排序')" prop="sortOrder" v-if="adminType === 'admin'">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.sortOrder" :disabled="examine == 1" />
                    </el-form-item>
                    <el-form-item :label="$t('首字母')" prop="firstWord">
                        <TigInput classType="tig-form-input" v-model="formState.firstWord" :disabled="examine == 1" />
                        <div class="extra">{{ $t('如果不填则自动生成，不过有些多音字无法准确判断，如“重”字系统则无法确认是Z还是C') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('是否显示')" prop="isShow">
                        <el-radio-group v-model="formState.isShow" :disabled="examine == 1">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('是否热门品牌')" prop="brandIsHot" v-if="adminType === 'admin'">
                        <el-radio-group v-model="formState.brandIsHot" :disabled="examine == 1">
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
import { BrandFormState } from "@/types/product/brand";
import { getBrand, updateBrand } from "@/api/product/brand";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import { getAdminType } from "@/utils/storage";
/** 当前管理员类型 */
const adminType = getAdminType();
/**
 * @description 父组件回调事件
 * @event submitCallback - 表单提交成功后的回调
 * @event update:confirmLoading - 更新确认按钮加载状态
 * @event close - 关闭弹窗
 */
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

/**
 * @property {number} id - 品牌ID
 * @property {string} act - 操作类型：'add'-新增, 'detail'-编辑
 * @property {number} examine - 是否为审核模式：1-只读查看
 * @property {boolean} isDialog - 是否在弹窗中显示
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
    examine: {
        type: Number,
        default: 0
    },
    isDialog: Boolean
});
/** 加载状态 */
const loading = ref<boolean>(true);
/** 当前路由查询参数 */
const query = useRouter().currentRoute.value.query;
/** 当前操作类型 */
const action = ref<string>(props.isDialog ? props.act : String(query.act));
/** 品牌ID */
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
/** API操作类型 */
const operation = action.value === "add" ? "create" : "update";
/** 表单引用 */
const formRef = shallowRef();
/**
 * @description 品牌表单数据
 * @property {number} sortOrder - 排序值
 * @property {number} isShow - 是否显示：0-否，1-是
 * @property {number} brandIsHot - 是否热门：0-否，1-是
 */
const formState = ref<BrandFormState>({
    sortOrder: 50,
    isShow: 1,
    brandIsHot: 0
});
/**
 * @description 获取品牌详情数据
 */
const fetchBrand = async () => {
    try {
        const result = await getBrand(action.value, { id: id.value });
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

/**
 * @description 表单验证通过后提交品牌数据
 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateBrand(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(result);
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

defineExpose({ onFormSubmit });
</script>
