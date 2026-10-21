<!--
  @file 保证金预警横幅
  @description 店铺端/供应商端概览页顶部展示，待补缴金额 > 0 时提醒补缴，平台管理员不展示
-->
<template>
    <div class="deposit-warning-wrapper" v-if="visible">
        <div class="tips-item">
            <div class="info flex flex-align-center">
                <div class="icon mr10">
                    <el-icon size="18" color="#ed6a18"><WarnTriangleFilled /></el-icon>
                </div>
                <div class="tips-content">
                    {{ warning.message }}
                    <a @click="goDeposit">去缴纳&gt;</a>
                </div>
            </div>
            <div class="btn" @click.stop="visible = false">
                <el-icon size="18"><Close /></el-icon>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { WarnTriangleFilled, Close } from "@element-plus/icons-vue";
import { getShopDepositWarning } from "@/api/merchant/capitalfund/deposit";
import { getAdminType } from "@/utils/storage";
import type { ShopDepositWarning } from "@/types/merchant/capitalfund/deposit.d";

const router = useRouter();
const visible = ref<boolean>(false);
const warning = ref<ShopDepositWarning>({ needWarning: false, pendingAmount: 0 });

const goDeposit = () => {
    router.push("/deposit/index");
};

onMounted(async () => {
    // 平台管理员无需缴纳保证金，不展示预警
    if (getAdminType() === "admin") return;
    try {
        const result = await getShopDepositWarning();
        warning.value = result;
        visible.value = !!result?.needWarning;
    } catch (error) {
        console.error(error);
    }
});
</script>

<style scoped lang="less">
.deposit-warning-wrapper {
    .tips-item {
        border: #ffe6d7 1px solid;
        background-color: #ffefe6;
        padding: 10px 20px;
        line-height: 24px;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .icon {
            margin-top: 5px;
        }

        .btn {
            margin-top: 5px;
            cursor: pointer;
        }

        .tips-content a {
            color: var(--tig-primary, #1456f0);
            cursor: pointer;
        }
    }
}
</style>
