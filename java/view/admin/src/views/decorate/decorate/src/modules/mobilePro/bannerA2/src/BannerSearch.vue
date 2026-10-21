<template>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label"> {{ $t('搜索框设置') }} </div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-radio-group">
                <el-radio-group class="dec-radio-group" v-model="searchContent.showSearch">
                    <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="searchContent.showSearch == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('默认搜索词') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <el-input v-model="searchContent.searchText" style="width: 240px" :placeholder="$t('请输入搜索框文字')" />
        </div>
    </div>
</template>
<script lang="ts" setup>
import { PicSelect } from "@/components/decorate";
import { ref, defineComponent } from 'vue';
import { SelectLink } from '@/components/select';
import { BannerSearchContent } from "@/types/decorate/decorate.d";
const searchContent = defineModel<BannerSearchContent>("modelValue", { default: () => ({}) });
</script>
