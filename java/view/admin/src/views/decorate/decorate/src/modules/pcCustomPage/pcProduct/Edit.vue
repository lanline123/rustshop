<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('商品') }}</h3>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('展示样式') }}</div>
                <div class="value">{{ selectLabel.style[module.style] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.style">
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行四个')" :show-after="100">
                        <el-radio-button :value="1">{{ $t('一行四个') }}</el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行五个')" :show-after="100">
                        <el-radio-button :value="2">{{ $t('一行五个') }}</el-radio-button>
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
        <CommonTitleEdit v-model="module.title" type="product" decorateType="pc"></CommonTitleEdit>
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
        1: t('一行四个'),
        2: t('一行五个')
    },
    picPageType: {
        1: t('样式一'),
        2: t('样式二'),
        3: t('样式三 ')
    }
});
const module = defineModel<ModuleType>("module", { default: () => ({}) });
</script>
