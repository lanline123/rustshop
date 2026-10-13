/**
 * @file 会员提现管理（D1107）
 * @description 提现管理页：提现设置（会员余额提现开关/方式/说明）+ 会员提现（提现申请记录）
 */
<template>
    <div class="container">
        <el-tabs v-model="activeName" class="lyecs-tabs">
            <el-tab-pane :label="$t('提现设置')" name="setting" />
            <el-tab-pane :label="$t('会员提现')" name="list" />
        </el-tabs>
        <WithdrawSettings v-if="activeName === 'setting'" />
        <List v-if="activeName === 'list'" :wallet-type="0" />
    </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { useRoute } from "vue-router";
import WithdrawSettings from "./WithdrawSettings.vue";
import List from "./List.vue";

const route = useRoute();
const activeName = ref<string>(route.query.tab === "list" ? "list" : "setting");
</script>
