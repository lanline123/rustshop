<template>
    <div v-if="!loading" style="line-height: 30px;" v-html="formState"></div>
</template>
<script lang="ts" setup>

import { ref, onMounted } from "vue";
import { message } from "ant-design-vue";
import {getShopAgreement} from "@/api/merchant/capitalfund/dashboard";
const formState = ref({});
const loading = ref(true);

const fetchInfo = async () => {
try {
    loading.value = true;
    const result = await getShopAgreement();
    formState.value = result;
} catch (error: any) {
    message.error(error.message);
} finally {
    loading.value = false;
}
};
onMounted(() => {
      // 获取详情数据
      fetchInfo();
});
</script>
<style lang="less" scoped>

</style>
