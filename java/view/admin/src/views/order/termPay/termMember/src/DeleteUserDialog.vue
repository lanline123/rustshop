<template>
    <el-dialog v-model="visible" width="420px" append-to-body :header-class="'header-class'" :footer-class="'footer-class'">
        <div class="content">
            <div class="title">{{ $t('确认删除选中的授信会员？') }}</div>
            <div class="tips">{{ $t('删除后此会员将无法使用账期支付功能') }}</div>
        </div>
        <template #footer>
            <el-button @click="visible = false">{{ $t('取消') }}</el-button>
            <el-button type="primary" :loading="loading" @click="onConfirm">{{ $t('确认') }}</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { message } from "ant-design-vue";
import { deleteCreditUser } from "@/api/termPay/creditUser";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const visible = defineModel("visible", { type: Boolean, default: false });

const props = defineProps<{
    ids: number[];
}>();

const emit = defineEmits(["success"]);

const loading = ref(false);

const ids = computed(() => props.ids || []);

const onConfirm = async () => {
    if (!ids.value.length) {
        visible.value = false;
        return;
    }
    loading.value = true;
    try {
        // 接口为单条删除：POST /credit/user/delete { id }
        for (const id of ids.value) {
            await deleteCreditUser({ id });
        }
        message.success(t('删除成功'));
        emit("success");
        visible.value = false;
    } finally {
        loading.value = false;
    }
};
</script>

<style scoped lang="less">
.content {
    line-height: 22px;
    .title{
        font-size: 18px;
        font-weight: 600;
        // color: #333;
    }
}
.tips {
    margin: 30px 0 50px 0;
    // color: #999;
}


</style>
 
<style>
.header-class{
    border-bottom: none !important;
}
.footer-class{
    border-top: none !important;
}
</style>