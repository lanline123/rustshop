<template>
    <div class="container">
        <div class="content_wrapper">
            <activityTable ref="tableRef" :promotionType="3">
                <DialogForm
                    isDrawer
                    @okCallback="touchEvent"
                    :title="$t('添加满赠优惠活动')"
                    width="1040px"
                    path="promotion/productActivity/Info"
                    :params="{ act: 'add', promotionType: 3, isFilterPresaleProduct: true }"
                >
                    <el-button type="primary">{{ $t('新增满赠活动') }}</el-button>
                </DialogForm>
            </activityTable>
        </div>
    </div>
</template>
<script setup lang="ts">
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { ref } from "vue";
import { useConfigStore } from "@/store/config";
import activityTable from "../src/activityTable.vue";
const config: any = useConfigStore();
// 基本参数定义
const tableRef = ref<any>(null);
const touchEvent = () => {
    tableRef.value.loadFilter();
};
</script>
