<template>
    <div class="dec-edit-group dec-edit-group-block">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('轮播内容设置') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <PicList :isMultiple="true" v-model:photos="bannerContent.picList" :showText="false" :title="$t('添加一组')" decorateType="mobile"></PicList>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('图片圆角') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="bannerContent.picRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('指示器样式') }}</div>
            <div class="value">{{ selectLabel.picPageType[bannerContent.picPageType as PicPageType] }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="bannerContent.picPageType">
                <el-tooltip effect="light" placement="bottom" :content="$t('样式一')" :show-after="100">
                    <el-radio-button :value="1"><i class="ico-decorate icon-dec-indicator-1"></i></el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('样式二')" :show-after="100">
                    <el-radio-button :value="2"><i class="ico-decorate icon-dec-indicator-2"></i></el-radio-button>
                </el-tooltip>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('指示器选中颜色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group" need-refresh="true">
                <div class="dec-color-button">
                    <a class="dec-color-reset">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="bannerContent.swiperPageColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('指示器默认颜色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group" need-refresh="true">
                <div class="dec-color-button">
                    <a class="dec-color-reset">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="bannerContent.indicatorColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import { BannerContentType, PicPageType, GradientType } from "@/types/decorate/decorate.d";
import { selectLabel } from "@/views/decorate/decorate/src/modules/editIndex";
const bannerContent = defineModel<BannerContentType>("modelValue", { default: () => ({}) });
</script>
