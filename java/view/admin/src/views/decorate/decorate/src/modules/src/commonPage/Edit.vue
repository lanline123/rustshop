<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('页面设置') }}</h3>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">{{ $t('页面标题') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-input-group" dynamic-class=".title-item">
                    <el-input v-model="module.title" :placeholder="$t('请输入页面标题，可不填，默认为页面名称')"></el-input>
                </div>
            </div>
        </div>
        <div class="dec-edit-group" v-if="pageType === 2">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('沉浸式标题') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-switch v-model="module.immersive"></el-switch>
            </div>
        </div>
        <!-- 头部风格（headerStyle）仅服务移动端/旧版头部；新版 PC 首页前台走 pcHeader 驱动的新版头部，此项无效，故隐藏 -->
        <div class="dec-edit-group pc-dec-show" v-if="!(store.decorateType === 2 && store.homeStyleVersion === 'new')">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('头部风格') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.headerStyle">
                    <el-radio-button :value="1">{{ $t('默认风格') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('简约风格') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('标题栏背景颜色') }}</div>
                <!-- <div class="value">{{ selectLabel.colorStyle[module.colorStyle || ''] }}</div> -->
            </div>
            <div class="dec-edit-group-con">
                <SelectColor v-model:color="module.titleBackgroundColor"></SelectColor>
                <!-- <el-radio-group class="dec-radio-group dec-radio-color-group" v-model="module.colorStyle">
                    <el-radio :value="1">
                        <div class="dec-radio-button dec-tip" :class="{'selected': module.colorStyle == 1}"><span class="dec-radio-color-button-con"><i style="background-color: rgb(0, 0, 0);"></i></span></div>
                    </el-radio>
                    <el-radio :value="2">
                        <div class="dec-radio-button dec-tip" :class="{'selected': module.colorStyle == 2}"><span class="dec-radio-color-button-con"><i style="background-color: #333;"></i></span></div>
                    </el-radio>
                    <el-radio :value="3">
                        <div class="dec-radio-button dec-tip" :class="{'selected': module.colorStyle == 3}"><span class="dec-radio-color-button-con"><i style="background-color: #e0f4e4;"></i></span></div>
                    </el-radio>
                </el-radio-group> -->
            </div>
        </div>
        <div class="dec-edit-group pc-dec-hide">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('标题栏文字颜色') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.titleColor = defaultModule.titleColor">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.titleColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group pc-dec-hide">
            <div class="dec-edit-group-desc">
                <div class="">{{ $t('提示：标题背景颜色和文字修改仅适用于微信小程序和原生/半原生APP。') }}</div>
            </div>
        </div>

        <div class="dec-divider-line pc-dec-hide"></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('页面背景颜色') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.backgroundColor = defaultModule.backgroundColor">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.backgroundColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('背景图') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-pic-group">
                    <PicSelect v-model:photo="module.backgroundImage" v-model:defaultValue="defaultModule.backgroundImage"></PicSelect>
                </div>
            </div>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('背景图显示') }}</div>
                <div class="value">{{ selectLabel.backgroundRepeat[module.backgroundRepeat] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.backgroundRepeat">
                    <el-radio-button :value="1">{{ $t('不平铺') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('平铺') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('纵向平铺') }}</el-radio-button>
                    <el-radio-button :value="4">{{ $t('横向平铺') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('背景图填充大小') }}</div>
                <div class="value">{{ selectLabel.backgroundSize[module.backgroundSize] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.backgroundSize">
                    <el-radio-button :value="1">{{ $t('横向100%') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('纵向100%') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('100%填充') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-divider-line pc-dec-show" v-if="store.decorateType === 2"></div>
        <div class="dec-edit-group dec-edit-group-block pc-dec-show" v-if="store.decorateType === 2">
            <div class="dec-edit-group-title">
                <div class="label">首页装修版本</div>
                <div class="value">{{ store.homeStyleVersion === "new" ? "新版" : "旧版" }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" :model-value="store.homeStyleVersion" @change="onSwitchHomeVersion">
                    <el-radio-button value="new">新版首页</el-radio-button>
                    <el-radio-button value="old">旧版首页</el-radio-button>
                </el-radio-group>
            </div>
            <div class="dec-edit-group-desc version-desc-group">
                <div class="version-desc-item">
                    <strong>新版首页：</strong>支持可视化网格、高级轮播等新模块，顶部带 PC 头部实时预览，排版更现代。
                </div>
                <div class="version-desc-item">
                    <strong>旧版首页：</strong>使用传统全宽占位模块，无头部预览，适合维护旧有页面。
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicSelect } from "@/components/decorate";
import { ref, onMounted } from "vue";
import { Modal } from "ant-design-vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const store = useDecorateEditorStore();

const onSwitchHomeVersion = (target: any) => {
    if (target === store.homeStyleVersion) return;

    const toNew = target === "new";
    const clearCount = toNew ? store.pcHomeOldModuleCount : store.pcHomeNewModuleCount;
    const clearLabel = toNew ? "旧版" : "新版";
    const targetLabel = toNew ? "新版" : "旧版";
    // 新版首页含一个固定头部装修数据，切到旧版保存时会随 pcHeader 一并清空，需单独提示（即便无普通模块也要弹确认）
    const clearsHeader = !toNew;

    if (clearCount > 0 || clearsHeader) {
        const clearParts: string[] = [];
        if (clearCount > 0) clearParts.push(`${clearCount} 个${clearLabel}模块`);
        if (clearsHeader) clearParts.push("头部装修数据");
        Modal.confirm({
            title: "切换首页装修版本",
            content: `切换到${targetLabel}后，将清空当前画布上的${clearParts.join("和")}；一经预览、保存草稿或发布，将无法恢复。确认切换？`,
            okText: "确认切换",
            cancelText: "取消",
            onOk: () => store.setHomeStyleVersion(target, true)
        });
        return;
    }
    store.setHomeStyleVersion(target, false);
};

const selectLabel = ref<any>({
    backgroundRepeat: {
        1: t('不平铺'),
        2: t('平铺'),
        3: t('纵向平铺'),
        4: t('横向平铺')
    },
    backgroundSize: {
        1: t('横向100%'),
        2: t('纵向100%'),
        3: t('100%填充')
    },
    colorStyle: {
        1: t('纯黑'),
        2: t('深灰'),
        3: t('浅绿')
    }
});

// 页面设置
const defaultModule = {
    title: "",
    titleColor: "",
    headerStyle: 1,
    titleBackgroundColor: "",
    immersive: false,
    backgroundImage: {
        picUrl: "",
        picThumb: ""
    },
    backgroundColor: "",
    backgroundRepeat: 1,
    backgroundSize: 1
};
const props = defineProps({
    module: {
        type: Object,
        default: () => {
            return {};
        }
    },
    pageType: {
        type: Number,
        default: 1
    }
});
const module = ref(props.module);
const dealDefault = () => {
    for (let i in defaultModule) {
        if (typeof (module.value as any)[i] === "undefined") {
            (module.value as any)[i] = (defaultModule as any)[i];
        }
    }
};
onMounted(async () => {
    dealDefault();
});
</script>
<style lang="less" scoped>
.dec-edit-group :deep(.el-radio .el-radio__input) {
    display: none;
}

.version-desc-group {
    margin-top: 10px;

    .version-desc-item {
        margin-bottom: 6px;
        color: #888;
        font-size: 12px;
        line-height: 1.6;

        &:last-child {
            margin-bottom: 0;
        }
    }
}
</style>
