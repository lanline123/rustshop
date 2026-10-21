<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                        <el-form-item :label="$t('店铺名称')" prop="shopTitle" :rules="[{ required: true, message: '店铺名称不能为空!' }]">
                            <TigInput classType="tig-form-input" v-model="formState.shopTitle" />
                        </el-form-item>
                        <el-form-item :label="$t('店铺LOGO')" prop="shopLogo" :rules="[{ required: true, message: '请上传店铺LOGO!' }]">
                            <FormAddGallery v-if="!loading" v-model:photo="formState.shopLogo"> </FormAddGallery>
                            <div class="extra">{{ $t('请统一上传比例为2:1的图片，宽200px，高100px（高清）') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('所属商户')" prop="merchantId" :rules="[{ required: true, message: '请选择所属商户!' }]">
                            <DialogForm
                                v-if="!isSpecified"
                                :params="{ isMultiple: false, selectedIds:[formState.merchantId] }"
                                isDrawer
                                path="adminMerchant/merchant/src/SelectMerchant"
                                :title="$t('选择商户')"
                                width="900px"
                                @okCallback="callback">
                                <el-button type="primary">{{ $t('选择商户') }}</el-button>
                            </DialogForm>
                            <el-table
                                v-if="formTable.length > 0"
                                :data="formTable"
                                row-key="merchantId"
                                class="mt10"
                            >
                                <el-table-column :label="$t('商户所属单位')">
                                    <template #default="{ row }">
                                        <div class="ssdw">
                                            <template v-if="row.type == 1">
                                                <Tag :transparent="true" :text="$t('个人')"></Tag>
                                            </template>
                                            <template v-else>
                                                <Tag color="#409EFF" :transparent="false" :text="$t('企业')"></Tag>
                                            </template>
                                            {{ row.type == 1 ? ("" + row.corporateName) : ("" + row.companyName) }}
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('商户所属会员(ID)')">
                                    <template #default="{ row }">
                                        {{ row.user?.username }}{{ (row.user?.userId)?'('+(row.user?.userId)+')':'' }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('商户类型')" prop="typeText"></el-table-column>
                                <el-table-column :label="$t('管理员')">
                                    <template #default="{ row }">
                                        {{ row.admin?.username || '--'}}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('认证状态')" prop="statusText" sortable="custom">
                                    <template #default="{ row }">
                                        <template v-if="row.status==1">
                                            <StatusDot color="#52c41a" :flicker="true"></StatusDot>
                                        </template>
                                        <template v-if="row.status==2">
                                            <StatusDot color="#f5222d" :flicker="false"></StatusDot>
                                        </template>
                                        {{ row.statusText }}
                                    </template>
                                </el-table-column>
                            </el-table>
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
import { DialogForm } from "@/components/dialog";
import {onMounted, ref, shallowRef} from "vue"
import {useRouter} from 'vue-router'
import {message} from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { ShopFormState } from "@/types/shop/shop.d";
import { getMerchantList } from "@/api/adminMerchant/merchant";
import { getShop, updateShop } from "@/api/shop/shop";
import StatusDot from "@/components/form/src/StatusDot.vue";

// 父组件回调
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
        default: ''
    },
    isDialog: Boolean
});
const loading = ref<boolean>(false);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === 'add' ? 'create' : 'update';
const formRef = shallowRef();
const isSpecified = ref<boolean>(false)
const formState = ref<ShopFormState>({
    shopType: 1,
    merchantId: Number(query.id)
});
const fetchShopInfo = async () => {
    try {
        const result = await getShop(action.value, {shopId: id.value});
        Object.assign(
          formState.value,
          result
        )
    } catch (error:any) {
        message.error(error.message);
        emit('close');
    } finally {
        loading.value = false;
    }
};
const _getMerchantList = async () => {
    try {
        const result = await getMerchantList({merchantId: id.value});
        formState.value.merchantId = id.value;
        formTable.value = result.records;
    } catch (error:any) {
        message.error(error.message);
        emit('close');
    } finally {
        loading.value = false;
    }
};
const formTable = ref<any[]>([]);
const callback = async (data:any) => {
    formTable.value = data.merchant
    formState.value.merchantId = data.merchantId
};

onMounted(() => {
    // 获取详情数据
    if(operation === 'update'){
        fetchShopInfo();
    }
    formTable.value = []
    formState.value.merchantId = 0
    if(id.value != 0){
        isSpecified.value = true;
        _getMerchantList()
    }
});

// 表单通过验证后提交
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
// 表单提交
const onFormSubmit = () => {
    onSubmit()
};

defineExpose({onFormSubmit});
</script>
<style scoped lang="less">

</style>
