/** 页面底部组件 - 显示版权信息和版本号 */
<template>
    <div class="footer">
        <span>
            {{licensedStore.licensedData.shopCompany == 1 ? licensedStore.licensedData.defaultCopyright : licensedStore.licensedData.shopCompanyTxt}}
        </span>
        <span v-if="licensedStore.licensedData.versionInfoHidden == 0">{{ licensedStore.licensedData.versionType }} {{ licensedStore.licensedData.version }}</span>
    </div>
</template>
<script setup lang="ts">
import { useLicensedStore } from "@/store/licensed";

/** 授权信息Store */
const licensedStore = useLicensedStore();
</script>
<style lang="less">
.footer {
    text-align: center;
    padding: 10px 0 10px 0;
    color: #c4c4c4;
    span{
        margin-right: 5px;
    }
}
</style>
