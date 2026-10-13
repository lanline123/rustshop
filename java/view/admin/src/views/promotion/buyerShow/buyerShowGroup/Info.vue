<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="groupName" :label="$t('分组名称')" :rules="[{ required: true, message: '分组名称不能为空!' }]">
                        <BusinessData width="248px" v-model:modelValue="formState.groupName" :placeholder="$t('请输入分组名称')" :dataType="16" :maxlength="60"></BusinessData>
                    </el-form-item>
                    <el-form-item prop="productGroups" :label="$t('关联商品分组')" :rules="[{ required: true, message: '关联商品不能为空!', trigger: 'change', type: 'array' }]">
                        <buyShowProductGroupSelect v-model:productGroups="formState.productGroups" isMultiple></buyShowProductGroupSelect>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted, watch } from "vue";
import { useRouter } from 'vue-router'
import { message } from "ant-design-vue";
import { buyerShowGroupFormState } from '@/types/promotion/buyerShowGroup.d';
import { updateBuyerShowGroup, getBuyerShowGroupDetail } from "@/api/promotion/buyShow";
import { buyShowProductGroupSelect } from "@/components/decorate";
import BusinessData from "@/components/multilingual/BusinessData.vue";
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
        default: ""
    },
    //是否自营商品
    isSelf: {
        type: Number,
        default: 0
    },
    examine: {
        type: Number,
        default: 0
    },
    isDialog: {
        type: Boolean,
        default: true
    },
    loading: {
        type: Boolean,
        default: false
    },
    type: {
        type: String,
        default: ""
    },
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === 'add' ? 'create' : 'update';
const formRef = shallowRef();
const formState = ref<buyerShowGroupFormState>({});

const fetchLive = async () => {
    try {
        const result: any = await getBuyerShowGroupDetail({ buyerShowGroupId: id.value });
        const productGroups = result.groupIds.map((item: any, index: number) => {
            return {
                productGroupId: item,
                productGroupName: result.productGroupName[index]
            }
        });
        formState.value = {
            ...result,
            productGroups
        }
    } catch (error: any) {
        message.error(error.message);
        emit('close');
    } finally {
        loading.value = false;
    }
};


// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        const params: any = {
            buyerShowGroupId: id.value,
            groupName: formState.value.groupName,
            groupIds: formState.value.productGroups?.map(item => item.productGroupId)
        };
        if (action.value === 'add') {
            delete params.buyerShowGroupId;
        }
        emit('update:confirmLoading', true);
        const result = await updateBuyerShowGroup(operation, params);
        emit('submitCallback', result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit('update:confirmLoading', false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit()
};

defineExpose({ onFormSubmit });

watch(()=>formState.value.productGroups, (val: any) => {
    val?.length > 0 ? formRef.value?.clearValidate('productGroups') : formRef.value?.validateField('productGroups');
})

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchLive();
    } else {
        loading.value = false;
    }
});
</script>

<style lang="less" scoped>
.limitUserRankBox{
    display: flex;
    gap: 16px;
    border: 1px solid #ddd;
    border-radius: 2px;
    padding: 10px;
    align-items: center;
}
/deep/ .gallery-pic{
    width: 100px;
}
/deep/ .add-photo-btn, .have_image{
    border-radius: 50%;
}
</style>
