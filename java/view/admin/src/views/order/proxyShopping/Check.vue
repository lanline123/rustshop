<template>
    <div class="container">
        <CheckPanel :userId="userId" :flowType="flowType" :active="true" @back="onBackToProxyShopping" />
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import CheckPanel from "./src/CheckPanel.vue";

const route = useRoute();
const router = useRouter();

const userId = computed(() => Number(route.query.userId));
const flowType = computed(() => {
    const n = Number(route.query.flowType);
    return Number.isFinite(n) && n > 0 ? n : 1;
});

const onBackToProxyShopping = () => {
    // 某些场景下 history 不可用/无上一页：兜底跳转到代客下单入口
    if (window.history.length > 1) {
        router.back();
        return;
    }
    router.push({ name: "proxyShoppingManage" }).catch(() => {});
};
</script>

<style scoped></style>
