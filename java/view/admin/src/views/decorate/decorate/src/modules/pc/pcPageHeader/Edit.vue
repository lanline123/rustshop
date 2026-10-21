<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3> {{ $t('头部设置') }} </h3>
        </div>
        <div class="dec-edit-group ">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('头部风格') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.headerStyle">
                    <el-radio-button :value="1">{{ $t('京东风格') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('简约风格') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">LOGO</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-pic-group">
                    <PicSelect v-model:photo="module.logo"></PicSelect>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="title">{{ $t('导航菜单设置') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-input-group" dynamic-class=".title-item">
                    <a href="#" class="f14">{{ $t('前往设置') }}</a>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { PicSelect } from '@/components/decorate'
import { ModuleType,PcPageHeaderType } from '@/types/decorate/decorate.d';
const module = defineModel<ModuleType & PcPageHeaderType>('module', {default: () => ({})});
</script>