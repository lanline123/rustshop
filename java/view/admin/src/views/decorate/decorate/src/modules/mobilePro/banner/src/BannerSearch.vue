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
    <div class="dec-edit-group dec-edit-group-block" v-if="searchContent.showSearch == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('菜单图标') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="searchContent.menuIcon">
                <el-radio-button :value="1">
                    <i class="iconfont-admin icon-menu1"></i>
                </el-radio-button>
                <el-radio-button :value="2">
                    <i class="iconfont-admin icon-menu2"></i>
                </el-radio-button>
                <el-radio-button :value="3">
                    <i class="iconfont-admin icon-menu3"></i>
                </el-radio-button>
                <el-radio-button :value="4">
                    <i class="iconfont-admin icon-menu4"></i>
                </el-radio-button>
                <el-radio-button :value="5">
                    <i class="iconfont-admin icon-menu5"></i>
                </el-radio-button>
                <el-radio-button :value="6">
                    <i class="iconfont-admin icon-menu6"></i>
                </el-radio-button>
                <el-radio-button :value="7">
                    <i class="iconfont-admin icon-menu7"></i>
                </el-radio-button>
                <el-radio-button :value="'custom'">{{ $t('自定义') }}</el-radio-button>
                <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group" v-if="searchContent.showSearch == 1 && searchContent.menuIcon == 'custom'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('自定义菜单图标') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <PicSelect v-model:photo="searchContent.menuPic"></PicSelect>
        </div>
    </div>
    <div class="dec-edit-group" v-if="searchContent.showSearch == 1 && searchContent.menuIcon != 0">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('菜单跳转链接') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-link-group">
                <div class="lyecs-link-select">
                    <SelectLink v-model="searchContent.menuLink" decorateType="mobile"></SelectLink>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group dec-edit-group-block" v-if="searchContent.showSearch == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('签到图标') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="searchContent.signIcon">
                <el-radio-button :value="1">
                    <i class="iconfont-admin icon-sign1"></i>
                </el-radio-button>
                <el-radio-button :value="2">
                    <i class="iconfont-admin icon-sign2"></i>
                </el-radio-button>
                <el-radio-button :value="3">
                    <i class="iconfont-admin icon-sign3"></i>
                </el-radio-button>
                <el-radio-button :value="4">
                    <i class="iconfont-admin icon-sign4"></i>
                </el-radio-button>
                <el-radio-button :value="'custom'">{{ $t('自定义') }}</el-radio-button>
                <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group" v-if="searchContent.showSearch == 1 && searchContent.signIcon == 'custom'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('自定义签到图标') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <PicSelect v-model:photo="searchContent.signPic"></PicSelect>
        </div>
    </div>
    <div class="dec-edit-group" v-if="searchContent.showSearch == 1 && searchContent.signIcon != 0">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('签到跳转链接') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-link-group">
                <div class="lyecs-link-select">
                    <SelectLink v-model="searchContent.signLink" decorateType="mobile"></SelectLink>
                </div>
            </div>
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
