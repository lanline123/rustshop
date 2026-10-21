<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('头条公告A2') }}</h3>
            <div class="dec-edit-desc">{{ $t('公告信息可以循环滚动展示，可设置点击关闭，箭头位置') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'noticeA2'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('公告内容') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <el-input v-model="module.noticeContent" style="width: 100%" :rows="3" type="textarea" :placeholder="$t('请输入公告内容')" />
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('公告跳转链接') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-link-group">
                        <div class="lyecs-link-select">
                            <SelectLink v-model="module.nociceLink" decorateType="mobile"></SelectLink>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('参数设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('公告文字效果') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.nociceEffect">
                        <el-tooltip effect="light" placement="bottom" :content="$t('滚动')" :show-after="100">
                            <el-radio-button :value="'scroll'">{{ $t('滚动') }}</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('静止')" :show-after="100">
                            <el-radio-button :value="'still'">{{ $t('静止') }}</el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.nociceEffect == 'still'">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题对齐方式') }}</div>
                    <div class="value">{{ selectLabel.textAlignment[module.textAlignment as TextAlignmentType] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.textAlignment">
                        <el-tooltip effect="light" placement="bottom" :content="$t('左对齐')" :show-after="100">
                            <el-radio-button :value="'left'"><i class="iconfont-admin icon-alignLeft"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('居中对齐')" :show-after="100">
                            <el-radio-button :value="'center'"><i class="iconfont-admin icon-center"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('右对齐')" :show-after="100">
                            <el-radio-button :value="'right'"><i class="iconfont-admin icon-alignRight"></i></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.nociceEffect == 'scroll'">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('滚动耗时') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.scrollSpeed" show-input :show-input-controls="false" size="small" input-size="default" :max="40" :min="5" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('公告图标类型') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.noticeIconType">
                        <el-radio-button :value="'default'">{{ $t('默认') }}</el-radio-button>
                        <el-radio-button :value="'custom'">{{ $t('自定义') }}</el-radio-button>
                        <el-radio-button :value="'hide'">{{ $t('隐藏') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.noticeIconType === 'custom'">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('公告图标，宽度不限，高60') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="module.iconPic"></PicSelect>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('公告文字颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.contentColor = '#333333'">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.contentColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('关闭按钮设置') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.closeIcon">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="noticeA2"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="noticeA2"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectLink } from "@/components/select";
import { SelectColor } from "@/components/select";
import { PicSelect } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref, watch } from "vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import { selectLabel } from "@/views/decorate/decorate/src/modules/editIndex";
import { ModuleType, ModuleNoticeA2Type, TextAlignmentType } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([
    { label: t('内容设置'), name: "noticeA2" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("noticeA2");
const module = defineModel<ModuleType & ModuleNoticeA2Type>("module", { default: () => ({}) });
const customIcon = {
    picUrl: "https://oss.tigshop.com/img/gallery/202411/17315607538zqwwuwCIA2X97Slx5.png",
    picThumb: "https://oss.tigshop.com/img/gallery/202411/17315607538zqwwuwCIA2X97Slx5.png?x-oss-process=image/resize,m_pad,h_200,h_200",
    picId: 1447,
    picName: "FmNDutABVdVhDqScSHImoufkXdBL"
};
const defaultIcon = {
    picUrl: "https://oss.tigshop.com/img/gallery/202411/1731560722X2AIPCOsyzKVyTB3ZD.png",
    picThumb: "https://oss.tigshop.com/img/gallery/202411/1731560722X2AIPCOsyzKVyTB3ZD.png?x-oss-process=image/resize,m_pad,h_200,h_200",
    picId: 1446,
    picName: t('通知')
};
watch(() => module.value.noticeIconType, (val) => {
    if (val === "custom") {
        module.value.iconPic = customIcon;
    } else {
        module.value.iconPic = defaultIcon;
    }
});
</script>
