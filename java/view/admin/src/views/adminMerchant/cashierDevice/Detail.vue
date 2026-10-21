<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <a-spin :spinning="loading">
                    <el-form label-width="auto">
                        <el-form-item :label="$t('设备名称')">
                            {{ detail.deviceName || detail.username || "--" }}
                        </el-form-item>
                        <el-form-item :label="$t('设备码')">
                            {{ detail.deviceCode || "--" }}
                        </el-form-item>
                        <el-form-item :label="$t('设备状态')">
                            {{ detail.status === 1 ? $t("启用") : $t("禁用") }}
                        </el-form-item>
                        <el-form-item :label="$t('最后使用时间')">
                            {{ detail.lastUsedTimeText || "--" }}
                        </el-form-item>
                    </el-form>
                    <div class="printer-list">
                        <div class="printer-list-title">{{ $t("绑定打印机") }}</div>
                        <el-table :data="printers" >
                            <el-table-column :label="$t('名称')" prop="printName" />
                            <el-table-column :label="$t('打印机类型')"><template #default="{ row }">{{ $t(row.platformText || "") }}</template></el-table-column>
                            <el-table-column :label="$t('打印机编码')" prop="printSn" />
                            <el-table-column :label="$t('更新时间')" prop="updateTime" />
                            <el-table-column :label="$t('状态')" prop="statusText" />
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t("暂无数据") }}</div>
                                </div>
                            </template>
                        </el-table>
                    </div>
                </a-spin>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { getCashierDeviceDetail } from "@/api/setting/cashier";
import { getPrintList } from "@/api/setting/receiptPrint";

const props = defineProps<{ id: number }>();
const loading = ref(false);
const detail = ref<any>({});
const printers = ref<any[]>([]);

const loadDetail = async () => {
    loading.value = true;
    try {
        const result: any = await getCashierDeviceDetail({ id: props.id });
        detail.value = result || {};
        const printResult: any = await getPrintList({ page: 1, size: 200, deviceCode: detail.value.deviceCode } as any);
        printers.value = printResult?.records || [];
    } finally {
        loading.value = false;
    }
};

loadDetail();
</script>

<style scoped lang="less">
.printer-list {
    margin-top: 24px;
}
.printer-list-title {
    margin-bottom: 12px;
    font-weight: 600;
}
</style>
