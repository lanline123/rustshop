<template>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label"> {{ $t('导航文字设置') }} </div>
            <div class="value"></div>
        </div>
        <!-- <div class="dec-edit-group-con">
            <div class="dec-radio-group">
                <el-radio-group class="dec-radio-group" v-model="navContent.showNav">
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">不显示</el-radio-button>
                </el-radio-group>
            </div>
        </div> -->
    </div>
    <div class="dec-edit-group dec-edit-group-block">
        <div class="dec-edit-group-con">
            <PicList :isMultiple="true" v-model:photos="navContent.navList" :showImage="false" :title="$t('添加文字和链接')" decorateType="mobile"></PicList>
        </div>
    </div>
 
</template>
<script lang="ts" setup>
import { PicList } from "@/components/decorate";
import { ref, defineComponent } from 'vue';
import { SelectLink } from '@/components/select';
import { BannerNavContent } from "@/types/decorate/decorate.d";
const navContent = defineModel<BannerNavContent>("modelValue", { default: () => ({}) });
</script>
