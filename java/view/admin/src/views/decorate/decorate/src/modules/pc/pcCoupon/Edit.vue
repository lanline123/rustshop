<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('优惠券') }}</h3>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-desc">{{ $t('请前往营销->优惠券->"首页显示"，用于在首页展示所推荐的优惠券') }}</div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { PicList } from "@/components/decorate";
import { ModuleType } from "@/types/decorate/decorate.d";
const module = defineModel<ModuleType>("module", { default: () => ({}) });
</script>
