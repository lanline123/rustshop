<template>
    <div class="login-page-left">
        <div class="top-bar-btn" href="#" :title="$t('起始页')" style="padding: 0 3px" v-if="licensedData">
            <img v-if="!licensedData.shopLogo || licensedData.shopLogo == null" src="~@/style/images/logo2.png" />
            <img v-else :src="imageFormat(licensedData.shopLogo)" />
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import { imageFormat } from "@/utils/format";
import { getAdminConfig } from "@/api/setting/config";
const licensedData = ref();
const updateConfig = async () => {
    try {
        const result = await getAdminConfig();
        licensedData.value = result;
    } catch (error) {
    } finally {
    }
};
updateConfig()
</script>

<style lang="less" scoped>
.login-page-left {
    .top-bar-btn{
        margin: 20px;
    }
    img{
        height: 35px;
    }
}
</style>
