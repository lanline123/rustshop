/** * @file 店铺端 保证金协议抽屉 */
<template>
    <a-spin :spinning="loading">
        <div v-if="content" class="agreement-content" v-html="content"></div>
    </a-spin>
</template>

<script lang="ts" setup>
import { onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import { getShopDepositProtocol } from "@/api/merchant/capitalfund/deposit";

const loading = ref(true);
const content = ref("");

const fetchProtocol = async () => {
    loading.value = true;
    try {
        const result = await getShopDepositProtocol();
        content.value = result?.content || "";
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchProtocol();
});
</script>

<style lang="less" scoped>
.agreement-content {
    line-height: 1.8;
}
</style>
