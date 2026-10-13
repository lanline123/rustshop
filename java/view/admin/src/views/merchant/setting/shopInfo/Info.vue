<template>
    <el-form ref="formRef" :model="formState" class="form-body" label-suffix="：" label-width="auto">
        <el-form-item v-show="page" :label="$t('店铺Logo')">
            <FormAddGallery v-model:photo="formState.shopLogo" :isMultiple="false"></FormAddGallery>
        </el-form-item>
        <el-form-item :rules="[{ required: true, message: '请输入店铺名称!' }]" :label="$t('店铺名称')" prop="shopTitle">
            <TigInput v-if="!isOverseas()" classType="tig-form-input" v-model="formState.shopTitle" :maxlength="15" :placeholder="$t('请输入店铺名称')" />
            <div class="extra">{{ $t('店铺名称应尽量简洁易记, 建议15字以内') }}</div>
        </el-form-item>
        <el-form-item :label="$t('创建时间')" prop="addTime">
            <p>{{ shopInfo.addTime }}</p>
        </el-form-item>
        <el-form-item :label="$t('店铺简介')" prop="description">
            <TigInput classType="tig-form-input" v-model="formState.description" :rows="4" type="textarea" :maxlength="200" />
        </el-form-item>
    </el-form>
</template>
<script lang="ts" setup>
import { ref, shallowRef, reactive } from "vue";
import { FormAddGallery } from "@/components/gallery";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import { message } from "ant-design-vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/user";
import type { ShopInfoFormState } from "@/types/merchant/setting/team.d";
import { updateShopInfo } from "@/api/merchant/setting/team";
import { isOverseas } from "@/utils/version";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits([
    "submitCallback", //确认后回调
    "update:confirmLoading", //确认按钮的loading状态
    "close" //关闭弹窗
]);
const props = defineProps({
    act: {
        type: String,
        default: ""
    },
    id: {
        type: Number,
        default: 0
    },
    type: {
        type: Number,
        default: 1
    },
    page: {
        type: Number,
        default: 1
    },
    isDialog: Boolean
});
const shopInfo = reactive(JSON.parse(JSON.stringify(useUserStore().shopInfo)));
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<ShopInfoFormState>({
    shopId: shopInfo.shopId,
    shopTitle: shopInfo.shopTitle,
    shopLogo: shopInfo.shopLogo,
    description: shopInfo.description
});

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateShopInfo({ ...formState.value });
        await formRef.value.resetFields();
        message.success(t('操作成功'));
        emit("submitCallback", result);
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
<style lang="less" scoped></style>
