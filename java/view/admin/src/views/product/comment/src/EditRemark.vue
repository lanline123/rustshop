/**
 * @file 商品评价回复组件
 * @description 商家回复用户评价的表单
 * @features
 * - 显示回复者信息（平台管理员/店铺）
 * - 填写回复内容
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('用户名')" v-if="adminType === 'admin'">
                        <TigInput classType="tig-form-input" v-model="userInfo.username" disabled />
                    </el-form-item>
                    <el-form-item :label="$t('用户名')" v-if="adminType === 'shop'">
                        <TigInput classType="tig-form-input" v-model="shopInfo.shopTitle" disabled />
                    </el-form-item>
                    <el-form-item :label="$t('回复内容')" prop="content">
                        <TigInput classType="tig-form-input" type="textarea" :rows="5" v-model="formState.content"/>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import {onMounted, ref, shallowRef} from "vue"
import {useRouter} from 'vue-router'
import {message} from "ant-design-vue";
import { useUserStore } from "@/store/user";
import { replyComment } from "@/api/product/comment";
import { getAdminType } from "@/utils/storage";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const adminType = getAdminType();
// 父组件回调
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    isDialog: Boolean
});
const userInfo = ref<any>(useUserStore().userInfo)
const shopInfo = ref<any>(useUserStore().shopInfo)
const query = useRouter().currentRoute.value.query;
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const formRef = shallowRef();
const formState = ref<any>({
    content:''
});
onMounted(() => {
});
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit('update:confirmLoading', true);
        const result = await replyComment({commentId: id.value, ...formState.value});
        emit('submitCallback', result);
        message.success(t('操作成功'));
    } catch (error:any) {
        message.error(error.message);
    } finally {
        emit('update:confirmLoading', false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit()
};
defineExpose({onFormSubmit});
</script>
<style lang="less" scoped>
.lyecs-form-table{
    h2{
        line-height: 30px;
    }
    p{
        line-height: 20px;
    }
}
</style>
