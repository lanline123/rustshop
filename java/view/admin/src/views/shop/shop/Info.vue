<!--
 * @file 店铺信息编辑页面
 * @description 店铺基本信息设置页面，支持配置：
 *   - 店铺状态（开业/停业）
 *   - 店铺名称
 *   - 店铺LOGO
 *   - 商家电话
 *   - 店铺描述
 * @module views/shop/shop/Info
-->
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                        <el-form-item :label="$t('店铺状态')" prop="status">
                            <el-radio-group v-model="formState.status">
                                <el-radio-button :label="$t('停业')" :value="4" />
                                <el-radio-button :label="$t('开业')" :value="1" />
                            </el-radio-group>
                        </el-form-item>
                        <el-form-item :label="$t('店铺名称')" prop="shopTitle" :rules="[{ required: true, message: '店铺名称不能为空!' }]">
                            <TigInput classType="tig-form-input" v-model="formState.shopTitle" />
                        </el-form-item>
                        <el-form-item :label="$t('店铺LOGO')" prop="shopLogo">
                            <FormAddGallery v-if="!loading" v-model:photo="formState.shopLogo"></FormAddGallery>
                            <div class="extra">{{ $t('请统一上传比例为2:1的图片，宽200px，高100px（高清）') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('商家电话')" prop="contactMobile">
                            <TigInput classType="tig-form-input" v-model="formState.contactMobile"/>
                        </el-form-item>
                        <el-form-item :label="$t('店铺描述')" prop="description">
                            <TigInput classType="tig-form-input" v-model="formState.description" :row="5" type="textarea"/>
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
import type {UserInvoiceFormState} from '@/types/finance/userInvoice.d';
import { FormAddGallery } from "@/components/gallery";
import {getUserInvoice, updateUserInvoice} from "@/api/finance/userInvoice";

import { ShopFormState } from "@/types/shop/shop.d";
import { getShop, updateShop } from "@/api/shop/shop";

/**
 * 组件事件定义
 * @event submitCallback - 提交成功回调
 * @event update:confirmLoading - 确认按钮加载状态更新
 * @event close - 关闭页面/弹窗
 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

/**
 * 组件属性定义
 * @property {number} id - 店铺ID
 * @property {string} act - 操作类型：add/detail
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
const loading = ref<boolean>(false);

/** 路由查询参数 */
const query = useRouter().currentRoute.value.query;

/** 操作类型 */
const action = ref<string>(props.isDialog ? props.act : String(query.act));

/** 店铺ID */
const id = ref<number>(props.isDialog ? props.id : Number(query.id));

/** API操作类型 */
const operation = action.value === 'add' ? 'create' : 'update';

/** 表单引用 */
const formRef = shallowRef();

/** 店铺表单状态 */
const formState = ref<ShopFormState>({
    shopType: 1
});

/**
 * 获取店铺信息
 * @description 加载店铺详情数据填充表单
 */
const fetchShopInfo = async () => {
    try {
        const result = await getShop(action.value, {shopId: id.value});
        Object.assign(formState.value, result);
    } catch (error:any) {
        message.error(error.message);
        emit('close');
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchShopInfo();
});

/**
 * 表单提交
 * @description 验证表单并提交店铺信息
 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit('update:confirmLoading', true);
        const result = await updateShop(operation, {id: id.value, ...formState.value});
        emit('submitCallback', result);
        message.success(t('操作成功'));
    } catch (error:any) {
        message.error(error.message);
    } finally {
        emit('update:confirmLoading', false);
    }
};

/**
 * 表单提交入口
 * @description 供父组件调用的提交方法
 */
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({onFormSubmit});
</script>
<style scoped lang="less">

</style>
