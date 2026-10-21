/**
 * @file 商品分组信息表单
 * @description 用于添加或编辑商品分组的表单组件。
 * @features
 * - 分组名称设置
 * - 分组别名设置
 * - 分组描述编辑
 * - 分组商品选择
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '分组名称不能为空!' }]" :label="$t('分组名称')" prop="productGroupName">
                        <TigInput classType="tig-form-input" v-model="formState.productGroupName"/>
                    </el-form-item>
                    <el-form-item :label="$t('分组别名')" prop="productGroupSn">
                        <TigInput classType="tig-form-input" v-model="formState.productGroupSn"/>
                    </el-form-item>
                    <el-form-item :label="$t('分组描述')" prop="productGroupDescription">
                        <TigInput classType="tig-form-input" v-model="formState.productGroupDescription" :row="2" type="textarea"/>
                    </el-form-item>
                    <el-form-item :label="$t('分组商品')" prop="productIds">
                        <SelectProduct v-if="!loading" v-model:ids="formState.productIds"></SelectProduct>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width:100%;margin-top:100px"/>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import {onMounted, ref, shallowRef} from "vue"
import {useRouter} from 'vue-router'
import {message} from "ant-design-vue";
import {ProductGroupFormState} from '@/types/product/productGroup';
import {getProductGroup, updateProductGroup} from "@/api/product/productGroup";
import {SelectProduct} from "@/components/select";
/**
 * @description 父组件回调事件
 * @event submitCallback - 表单提交成功后的回调
 * @event update:confirmLoading - 更新确认按钮加载状态
 * @event close - 关闭弹窗
 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

/**
 * @property {number} id - 分组ID
 * @property {string} act - 操作类型：'add'-新增, 'detail'-编辑
 * @property {boolean} isDialog - 是否在弹窗中显示
 */
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ''
    },
    isDialog: Boolean
});
/** 加载状态 */
const loading = ref<boolean>(true);
/** 当前路由查询参数 */
const query = useRouter().currentRoute.value.query;
/** 当前操作类型 */
const action = ref<string>(props.isDialog ? props.act : String(query.act));
/** 分组ID */
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
/** API操作类型 */
const operation = action.value === 'add' ? 'create' : 'update';
/** 表单引用 */
const formRef = shallowRef();
/**
 * @description 分组表单数据
 * @property {number[]} productIds - 分组内的商品ID列表
 */
const formState = ref<ProductGroupFormState>({
    productIds:[]
});
/**
 * @description 获取分组详情数据
 */
const fetchProductGroup = async () => {
    try {
        const result = await getProductGroup(action.value, {id: id.value});
        Object.assign(
            formState.value,
            result
        )
    } catch (error: any) {
        message.error(error.message);
        emit('close');
    } finally {
        loading.value = false;
    }
};


onMounted(() => {
    console.log(action.value);
    if (action.value === "detail") {
        // 获取详情数据
        fetchProductGroup();
    } else {
        loading.value = false;
    }
});

/**
 * @description 表单验证通过后提交分组数据
 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit('update:confirmLoading', true);
        const result = await updateProductGroup(operation, {id: id.value, ...formState.value});
        emit('submitCallback', result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit('update:confirmLoading', false);
    }
};
/**
 * @description 表单提交入口，供父组件调用
 */
const onFormSubmit = () => {
    onSubmit()
};

defineExpose({onFormSubmit});
</script>
