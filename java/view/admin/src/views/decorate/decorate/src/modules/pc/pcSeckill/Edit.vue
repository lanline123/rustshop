<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('限时秒杀设置') }}</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('活动标题') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-input-group">
                    <el-input v-model="module.seckillTitle" :placeholder="$t('请输活动标题')"></el-input>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { PicList } from "@/components/decorate";
import { ModuleType, PcSeckillType } from "@/types/decorate/decorate.d";
const module = defineModel<ModuleType & PcSeckillType>("module", { default: () => ({}) });
</script>
