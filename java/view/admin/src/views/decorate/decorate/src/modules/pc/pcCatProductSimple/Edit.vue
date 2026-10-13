<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3> {{ $t('分类商品（简约）设置') }} </h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('风格选择') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.baseStyle">
                    <el-radio-button :value="1">{{ $t('风格一') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('风格二') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('标题名称') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                    <el-input v-model="module.title" :placeholder="$t('请输入标题名称')"></el-input>
                </div>
            </div>
        </div>
        <div class="dec-edit-group ">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('更多链接') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-link-group">
                    <div class="lyecs-link-select">
                        <SelectLink v-model="module.moreLink"></SelectLink>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div v-if="module.baseStyle==2" class="dec-edit-group dec-edit-group-block">
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
        <CommonProductEdit v-model="module.products"></CommonProductEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { PicList, LinkList, ProductTagSelect } from "@/components/decorate";
import { ModuleType, PcCatProductType } from '@/types/decorate/decorate.d';
import { SelectCategory, SelectColor, SelectLink } from "@/components/select";
import { ProductSelect } from '@/components/decorate'
import CommonProductEdit from "@/views/decorate/decorate/src/modules/src/commonProduct/Edit.vue";
const module = defineModel<ModuleType & PcCatProductType>('module', {default: () => ({})});
</script>
