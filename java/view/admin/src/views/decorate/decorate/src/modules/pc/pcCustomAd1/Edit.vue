<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3> {{ $t('Banner设置') }} </h3>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">{{ $t('添加左侧大图') }}</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">{{ $t('提示：您可以通过拖拽进行图片排序，建议尺寸：540*800像素（高清）或270*400像素（低分辨率）') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList"></PicList>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">{{ $t('中间每日推荐') }}</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">{{ $t('建议尺寸：第一张图500*720像素，其余三张180*180像素') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList2" :showDesc="true"></PicList>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">{{ $t('右侧热销精品') }}</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">{{ $t('建议尺寸：第一张图420*420，第二、三张图180*180,第四、五张图410*210像素') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList3" :showDesc="true"></PicList>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { PicList } from '@/components/decorate'
import { ref } from 'vue';
import { ModuleType, PcCustomAd1Type } from '@/types/decorate/decorate.d';
const module = defineModel<ModuleType & PcCustomAd1Type>('module', {default: () => ({})});
</script>