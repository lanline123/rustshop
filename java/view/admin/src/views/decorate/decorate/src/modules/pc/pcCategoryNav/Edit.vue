<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('分类导航') }}</h3>
        </div>
        <div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('每屏显示数量') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.rowNum">
                        <el-radio-button :value="2">{{ $t('2个') }}</el-radio-button>
                        <el-radio-button :value="3">{{ $t('3个') }}</el-radio-button>
                        <el-radio-button :value="4">{{ $t('4个') }}</el-radio-button>
                        <el-radio-button :value="5">{{ $t('5个') }}</el-radio-button>
                        <el-radio-button :value="6">{{ $t('6个') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="categoryA1"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="categoryA1"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import { ModuleType, ModuleCategoryA1Type } from "@/types/decorate/decorate.d";

const activeName = ref("category");

const module = defineModel<ModuleType & ModuleCategoryA1Type>("module", { default: () => ({}) });
</script>
