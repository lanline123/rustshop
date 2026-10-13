<template>
    <!-- 分销佣金提现（D1107）：从佣金账户提现，复用收款账户与提现表单 -->
    <tig-layout :title="$t('佣金提现')">
        <raplyToCard :balance="commissionBalance" wallet="commission" @back-detail="onBack" />
    </tig-layout>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import raplyToCard from "@/pages/user/account/src/raplyToCard.vue";
import { getUser } from "@/api/salesman/salesmanCenter";

const commissionBalance = ref<string | number>(0);

const _getUser = async () => {
    try {
        const result = await getUser();
        commissionBalance.value = result.commissionBalance || 0;
    } catch (error: any) {
        commissionBalance.value = 0;
    }
};

const onBack = () => {
    uni.navigateBack();
};

onShow(() => {
    _getUser();
});
</script>
