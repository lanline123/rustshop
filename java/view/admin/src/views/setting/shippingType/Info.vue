<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '分类名称不能为空!' }]" :label="$t('分类名称')" prop="shippingTypeName">
                        <TigInput classType="tig-form-input" v-model="formState.shippingTypeName" />
                    </el-form-item>
                    <el-form-item :label="$t('分类描述')" prop="shippingTypeDesc">
                        <TigInput classType="tig-form-input" v-model="formState.shippingTypeDesc" :row="3" type="textarea" />
                    </el-form-item>
                    <el-form-item :label="$t('默认物流')" prop="shippingDefaultId">
                        <SelectLogisticsCompany
                            v-model:logisticsId="formState.shippingDefaultId"
                            source="tenant-config"
                            :is-enabled="1"
                            auto-select-default
                        ></SelectLogisticsCompany>
                    </el-form-item>
                    <el-form-item :label="$t('配送时间描述')" prop="shippingTimeDesc">
                        <TigInput classType="tig-form-input" v-model="formState.shippingTimeDesc" :row="3" type="textarea" />
                    </el-form-item>
                    <el-form-item :label="$t('排序')" prop="sortOrder">
                        <TigInput type="integer" classType="tig-form-input" v-model="formState.sortOrder" />
                    </el-form-item>
                    <el-form-item :label="$t('货到付款')" prop="isSupportCod">
                        <el-radio-group v-model="formState.isSupportCod" style="width: 100%">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t('该设置会影响所有所属的配送方式') }}</div>
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
import { getShippingType, updateShippingType } from "@/api/setting/shippingType";

import { SelectLogisticsCompany } from "@/components/select";
import type { ShippingTypeFormState } from "@/types/setting/shippingType";

// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0,
    },
    act: {
        type: String,
        default: "",
    },
    isDialog: Boolean,
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<ShippingTypeFormState>({
    sortOrder:50,
    isSupportCod:0
});
const fetchShippingType = async () => {
    try {
        const result = await getShippingType(action.value, { id: id.value });
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
        fetchShippingType();
    } else {
        loading.value = false;
    }
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateShippingType(operation, { id: id.value, ...formState.value });
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
