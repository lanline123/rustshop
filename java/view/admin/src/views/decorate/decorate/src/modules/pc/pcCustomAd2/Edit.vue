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
                <PicList :isMultiple="false" v-model:photo="module.picList"></PicList>
            </div>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">{{ $t('添加左侧链接') }}</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">{{ $t('提示：超过6个不显示') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <LinkList v-model="module.linkList"></LinkList>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">{{ $t('中间广告图') }}</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">{{ $t('建议尺寸：第一张图848*460像素，其余四张300*300像素') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList2" :showDesc="true"></PicList>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">{{ $t('右侧广告图') }}</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">{{ $t('建议尺寸：200*200像素，超过6张不显示') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList3" :showDesc="true"></PicList>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { PicList, LinkList } from '@/components/decorate'
import { ModuleType, PcCustomAd2Type } from '@/types/decorate/decorate.d';
const module = defineModel<ModuleType & PcCustomAd2Type>('module', {default: () => ({})});
</script>
