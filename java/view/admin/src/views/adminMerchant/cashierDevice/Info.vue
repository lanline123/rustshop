<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('设备名称')" prop="deviceName" :rules="[{ required: true, message: t('设备名称不能为空') }]">
                        <TigInput classType="tig-form-input" v-model="formState.deviceName" :placeholder="$t('请输入设备名称')" />
                    </el-form-item>
                    <el-form-item :label="$t('设备码')" prop="deviceCode" :rules="[{ required: true, message: t('设备码不能为空') }]">
                        <TigInput classType="tig-form-input" v-model="formState.deviceCode" :placeholder="$t('请输入设备码')" />
                    </el-form-item>
                    <el-form-item :label="$t('设备状态')" prop="status">
                        <el-switch v-model="formState.status" :active-value="1" :inactive-value="2" />
                    </el-form-item>
                    <div v-if="props.act !== 'create'" class="printer-list">
                        <div class="printer-list-title">{{ $t("绑定打印机") }}</div>
                        <el-table :data="printers" >
                            <el-table-column :label="$t('名称')" prop="printName" />
                            <el-table-column :label="$t('打印机类型')"><template #default="{ row }">{{ $t(row.platformText || "") }}</template></el-table-column>
                            <el-table-column :label="$t('打印机编码')" prop="printSn" />
                            <el-table-column :label="$t('更新时间')" prop="updateTime" />
                            <el-table-column :label="$t('状态')" prop="statusText" />
                            <template #empty>
                                <div class="empty-warp">
                                    <div class="empty-bg">{{ $t("暂无数据") }}</div>
                                </div>
                            </template>
                        </el-table>
                    </div>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { useI18n } from "vue-i18n";
import { getCashierDeviceDetail, updateCashierDevice } from "@/api/setting/cashier";
import { getPrintList } from "@/api/setting/receiptPrint";

const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps<{ act: "create" | "update" | "detail"; id?: number }>();
const formRef = shallowRef();
const loading = ref(false);
const formState = ref({ deviceName: "", deviceCode: "", status: 1 });
const printers = ref<any[]>([]);

const loadDetail = async () => {
    if (props.act === "create" || !props.id) return;
    loading.value = true;
    try {
        const detail: any = await getCashierDeviceDetail({ id: props.id });
        formState.value = { deviceName: detail.deviceName || detail.username || "", deviceCode: detail.deviceCode || "", status: detail.status ?? 1 };
        const result: any = await getPrintList({ page: 1, size: 200, deviceCode: detail.deviceCode } as any);
        printers.value = result?.records || [];
    } finally {
        loading.value = false;
    }
};

const onFormSubmit = async () => {
    await formRef.value.validate();
    emit("update:confirmLoading", true);
    try {
        await updateCashierDevice(props.act === "create" ? "create" : "update", { ...formState.value, id: props.id, deviceCode: formState.value.deviceCode.trim() });
        message.success(t("操作成功"));
        emit("submitCallback");
    } catch (error: any) {
        message.error(error?.message || error?.msg || t("操作失败"));
    } finally {
        emit("update:confirmLoading", false);
    }
};

loadDetail();
defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
.printer-list { margin-top: 24px; }
.printer-list-title { margin-bottom: 12px; font-weight: 600; }
</style>
