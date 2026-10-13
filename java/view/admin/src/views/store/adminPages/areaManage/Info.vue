<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '区域名称不能为空!' }]" :label="$t('区域名称')" prop="areaStoreName">
                        <TigInput classType="tig-form-input" v-model="formState.areaStoreName" :maxlength="30" showWordLimit />
                    </el-form-item>
                    <el-form-item :label="$t('排序')" prop="sortOrder">
                        <TigInput width="100px" type="integer" v-model="formState.sortOrder" :disabled="examine == 1" />
                    </el-form-item>
                    <el-form-item
                        :label="$t('区域门店')"
                        prop="shopIds"
                    >
                        <DialogForm
                            :params="{ isMultiple: true, selectedIds: [formState.shopIds] }"
                            isDrawer
                            path="store/adminPages/selfPackManage/src/SelectStoreList"
                            :title="$t('选择门店')"
                            width="700px"
                            @okCallback="callback"
                        >
                            <el-button type="primary">{{ $t('选择门店') }}</el-button>
                        </DialogForm>
                        <el-table v-if="formTable.length > 0" :data="formTable" row-key="storeParentId" class="mt10">
                            <el-table-column :label="$t('门店名称')">
                                <template #default="{ row }">
                                    {{ row.shopTitle || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('门店LOGO')">
                                <template #default="{ row }">
                                    <Image :src="row.shopLogo" fit="contain" style="height: 25px; width: 60px" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商户名称')" :width="200">
                                <template #default="{ row }">
                                    <div v-if="row.merchant">
                                        <span v-if="row.merchant.type == 2">{{ row.merchant.merchantData?.merchantName || "--" }}</span>
                                        <span v-if="row.merchant.type == 1">{{ row.merchant.corporateName || "--" }}</span>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('门店状态')" sortable="custom">
                                <template #default="{ row }">
                                    <template v-if="row.status == 10">
                                        <StatusDot color="red" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 4">
                                        <StatusDot color="red" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 1">
                                        <StatusDot color="green" :flicker="true"></StatusDot>
                                    </template>
                                    <span v-if="row.status === 10 || row.status === 4" style="color: red">{{ row.statusText }}</span>
                                    <span v-else-if="row.status === 1" style="color: green">{{ row.statusText }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('认证日期')" prop="addTime" sortable="custom"></el-table-column>
                        </el-table>
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
import { Image } from "@/components/image";
import { DialogForm } from "@/components/dialog";
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { getAdminType } from "@/utils/storage";
import type { RequestInfo } from "@/types/store/areaManage";
import { getAreaStoreManager, updateAreaStoreManager } from "@/api/store/areaManage";
import { getShopList } from "@/api/shop/shop";
import StatusDot from "@/components/form/src/StatusDot.vue";
const adminType = getAdminType();
// 父组件回调
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
    examine: {
        type: Number,
        default: 0
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<RequestInfo>({
    sortOrder: 50,
    areaStoreName: "",
    shopIds: []
});
const fetchBrand = async () => {
    try {
        const result = await getAreaStoreManager(action.value, { id: id.value });
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

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateAreaStoreManager(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

const _getShopList = async () => {
    try {
        const result = await getShopList({ shopIds: formState.value.shopIds.join(","), shopType: 2 });
        formTable.value = result.records;
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const formTable = ref<any[]>([]);
const callback = async (data: any) => {
    formState.value.shopIds = data;
    _getShopList()
};

// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
