<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('商品') }}</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-desc">
                <div>{{ $t('提示：仅“一行两个”模板类型支持') }}<b style="color: #333">{{ $t('瀑布流') }}</b>{{ $t('展示，为了展示效果，请尽量将瀑布流组件置于底部，一个页面不要超过一个') }}</div>
            </div>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('选择模板') }}</div>
                <div class="value">{{ selectLabel.style[module.style] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.style">
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行一个')" :show-after="100">
                        <el-radio-button :value="1"><i class="ico-decorate icon-dec-up2end"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行两个')" :show-after="100">
                        <el-radio-button :value="2"><i class="ico-decorate icon-dec-small"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行三个')" :show-after="100">
                        <el-radio-button :value="3"><i class="ico-decorate icon-dec-three"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('一大两小')" :show-after="100">
                        <el-radio-button :value="4"><i class="ico-decorate icon-dec-hybrid"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('详细列表')" :show-after="100">
                        <el-radio-button :value="5"><i class="ico-decorate icon-dec-list"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('横向滑动')" :show-after="100">
                        <el-radio-button :value="6"><i class="ico-decorate icon-dec-swipe"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('轮播滑动')" :show-after="100">
                        <el-radio-button :value="7"><i class="ico-decorate icon-dec-indicator-1"></i></el-radio-button>
                    </el-tooltip>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <CommonActivityFrameEdit v-model="module" type="product"></CommonActivityFrameEdit>
        <div class="dec-edit-group" v-if="module.style == 7">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('指示器颜色') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.swiperPageColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <CommonTitleEdit v-model="module.title" type="product" decorateType="mobile"></CommonTitleEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ref, reactive } from "vue";
import CommonTitleEdit from "../../src/commonTitle/Edit.vue";
import { SelectColor } from "@/components/select";
import CommonActivityFrameEdit from "../../src/commonActivityFrame/Edit.vue";
import { ModuleType } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const selectLabel = ref<any>({
    style: {
        1: t('一行一个'),
        2: t('一行两个'),
        3: t('一行三个'),
        4: t('一大两小'),
        5: t('详细列表'),
        6: t('横向滑动'),
        7: t('轮播滑动')
    },
    picPageType: {
        1: t('样式一'),
        2: t('样式二'),
        3: t('样式三 ')
    }
});
const module = defineModel<ModuleType>("module", { default: () => ({}) });
</script>
