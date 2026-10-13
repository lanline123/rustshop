<template>
    <!-- 标题设置 -->
    <div class="dec-divider-line" v-if="type != 'title'"></div>

    <div class="dec-spread-title" v-if="type == 'categoryNavA3'">
        <div class="title">{{ $t('顶部标题设置') }}</div>
    </div>

    <div class="dec-spread-title" v-else>
        <div class="title">{{ $t('标题设置') }}</div>
    </div>

    <div class="dec-edit-group" v-if="type != 'title'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('显示标题栏') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-radio-group">
                <el-radio-group class="dec-radio-group" v-model="title.showTitle">
                    <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题栏风格') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="title.titleStyle">
                <el-radio-button :label="1">{{ $t('风格一') }}</el-radio-button>
                <el-radio-button :label="2">{{ $t('风格二') }}</el-radio-button>
                <el-radio-button :label="3" v-if="type == 'product'">{{ $t('风格三') }}</el-radio-button>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group"  v-if="type == 'title'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题位置') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="title.titleAlign">
                <el-radio-button :label="1">{{ $t('居左') }}</el-radio-button>
                <el-radio-button :label="2">{{ $t('居中') }}</el-radio-button>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type != 'title'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题背景色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="title.titleBackground = defaultTitle.titleBackground">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="title.titleBackground"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type == 'product'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('背景渐变色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="title.titleBackground2 = defaultTitle.titleBackground2">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="title.titleBackground2"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type != 'title'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题图片') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-pic-group dec-picSingle-group">
                <PicSelect v-model:photo="title.titleBackgroundPic" v-model:defaultValue="defaultTitle.titleBackgroundPic"></PicSelect>
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题圆角') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="title.titleRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题内容') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-input-group">
                <el-input v-model="title.titleText" :placeholder="$t('请输入标题内容')"></el-input>
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题颜色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="title.titleColor = defaultTitle.titleColor">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="title.titleColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type == 'title'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题粗细') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="title.titleWeight">
                <el-radio-button label="normal">{{ $t('常规') }}</el-radio-button>
                <el-radio-button label="500">{{ $t('中粗') }}</el-radio-button>
                <el-radio-button label="bold">{{ $t('加粗') }}</el-radio-button>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type != 'seckill2'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('描述内容') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                <el-input v-model="title.descText" :placeholder="$t('请输入标题内容')"></el-input>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type != 'seckill2'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('描述颜色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="title.descColor = defaultTitle.descColor">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="title.descColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t(moreSwitchLabel) }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="title.showMore">
                <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
            </el-radio-group>
        </div>
    </div>

    <!-- 右侧内容二选（拼团、秒杀等支持倒计时的模块） -->
    <template v-if="enableMoreType">
        <div class="dec-edit-group" v-if="title.showMore == 1">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('右侧内容') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="moreType">
                    <el-radio-button :value="MORE_TYPE_LINK">{{ $t('显示更多') }}</el-radio-button>
                    <el-radio-button :value="MORE_TYPE_COUNTDOWN">{{ $t('显示倒计时') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <template v-if="title.showMore == 1 && moreType === MORE_TYPE_LINK">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('链接文字') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-input-group">
                        <el-input v-model="moreLinkTitle" :placeholder="$t('更多')"></el-input>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('链接颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="title.moreColor = defaultTitle.moreColor">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="title.moreColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('链接地址') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-link-group">
                        <div class="lyecs-link-select">
                            <SelectLink v-model="moreLinkModel" :decorateType="decorateType"></SelectLink>
                        </div>
                    </div>
                </div>
            </div>
        </template>

        <template v-if="title.showMore == 1 && moreType === MORE_TYPE_COUNTDOWN">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('前缀文字') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-input-group">
                        <el-input v-model="title.countdownLabel" :placeholder="countdownLabelPlaceholder"></el-input>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('文字颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="title.moreColor = defaultTitle.moreColor">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="title.moreColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
        </template>
    </template>

    <template v-else>
        <div class="dec-edit-group" v-if="type != 'seckill2'">
            <div class="dec-edit-group-title">
                <div class="label">{{ isOverseas() && type == 'pcGroupon' ? $t('倒计时颜色') : $t('更多文字颜色') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="title.moreColor = defaultTitle.moreColor">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="title.moreColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group ">
            <div class="dec-edit-group-title">
                <div class="label">{{ isOverseas() && type == 'pcGroupon' ? $t('倒计时链接') : $t('更多链接') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-link-group">
                    <div class="lyecs-link-select">
                        <SelectLink v-model="title.moreLink" :decorateType="decorateType"></SelectLink>
                    </div>
                </div>
            </div>
        </div>
    </template>
</template>
<script lang="ts" setup>

import { SelectColor } from '@/components/select'
import { PicSelect } from '@/components/decorate'
import { ref, onMounted, ComputedRef, computed } from 'vue';
import { SelectLink } from '@/components/select';
import { ModuleTitleType } from '@/types/decorate/decorate.d';
import { imageFormat } from "@/utils/format";
import { defaultTitle } from '@/views/decorate/decorate/src/modules/'
import { isOverseas } from '@/utils/version';
import { MORE_TYPE_COUNTDOWN, MORE_TYPE_LINK, resolveTitleMore } from '@/views/decorate/decorate/editor/utils/titleMore';
const title = defineModel<ModuleTitleType>('modelValue', {default: () => ({})});
const props = defineProps({
    type: {
        type: String,
        default: ''
    },
    decorateType: {
        type: String,
        default: ''
    },
    /** 是否开启"更多链接 / 倒计时"二选（拼团、秒杀等支持倒计时的模块） */
    enableMoreType: {
        type: Boolean,
        default: false
    },
    /** moreType 缺失时的老语义，用于推导默认选中项 */
    moreTypeLegacy: {
        type: String as () => 'link' | 'seckillCustom',
        default: 'link'
    },
    /** 倒计时前缀文字输入框的占位文案 */
    countdownLabelPlaceholder: {
        type: String,
        default: '倒计时'
    }
})

const moreSwitchLabel = computed(() => {
    if (props.enableMoreType) return '显示右侧内容';
    return isOverseas() && props.type == 'pcGroupon' ? '显示倒计时' : '显示查看更多';
});

// 老数据没有 moreType，读取时按模块原语义推导；写入时落到 title.moreType
const moreType = computed({
    get: () => resolveTitleMore(title.value, props.moreTypeLegacy).mode,
    set: (v: number) => {
        title.value.moreType = v;
    }
});

// "更多"链接文字存在 moreLink.title 上
const moreLinkTitle = computed({
    get: () => title.value.moreLink?.title ?? '',
    set: (v: string) => {
        title.value.moreLink = { ...(title.value.moreLink ?? {}), title: v };
    }
});

// SelectLink 会整体替换链接对象，这里保留用户填写的链接文字
const moreLinkModel = computed({
    get: () => title.value.moreLink,
    set: (v: ModuleTitleType['moreLink']) => {
        const currentTitle = title.value.moreLink?.title ?? '';
        title.value.moreLink = currentTitle ? { ...(v ?? {}), title: currentTitle } : v;
    }
});

onMounted(() => {
    if (props.type === 'title' && !title.value.titleWeight) {
        title.value.titleWeight = 'normal';
    }
    // 老数据没有 moreType，首次打开面板时按原语义落库，
    // 否则「显示右侧内容」开关会与前台实际渲染不一致（如自定义页秒杀 showMore=0 也显示倒计时）
    if (props.enableMoreType && title.value.moreType === undefined) {
        const { visible, mode } = resolveTitleMore(title.value, props.moreTypeLegacy);
        title.value.moreType = mode;
        title.value.showMore = visible ? 1 : 0;
    }
});
</script>