<template>
    <div class="module-ad-con module-member-account" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content">
            <div class="account-wrap" :style="wrapStyle">
                <div class="account-container" :style="containerStyle">
                    <div class="account-header">
                        <div class="account-title">{{ module.moduleTitle || $t('账号') }}</div>
                    </div>
                    <div class="account-list">
                        <div v-for="(item, idx) in accountList" :key="idx" class="account-item">
                            <div class="account-item-left">
                                <div class="account-item-icon">
                                    <img v-if="item.picUrl" :src="item.picUrl" :alt="item.picTitle" />
                                </div>
                                <div class="account-item-title">{{ item.picTitle }}</div>
                            </div>
                            <div class="account-item-right">
                                <span v-if="item.picDesc" class="account-item-subtitle">{{ item.picDesc }}</span>
                                <span class="iconfont-h5 icon-a-gengduo20"></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, Ref } from "vue";
import { ModuleType, ModuleFrameType, ModulePicListType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";
import { isOverseas } from "@/utils/version";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
type ModuleAccountType = Omit<ModuleType, "frame"> & {
    moduleTitle?: string;
    accountList?: ModulePicListType[];
    frame: ModuleFrameType;
};

const module = defineModel<ModuleAccountType>("module") as Ref<ModuleAccountType>;

const defaultAccountList: ModulePicListType[] = [
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779691120KR8ldT5BHs0ZKZ4Uxk.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779691120KR8ldT5BHs0ZKZ4Uxk.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('账号详情'),
        picDesc: ""
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779691120QiTfocEWPiXAr7ILcG.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779691120QiTfocEWPiXAr7ILcG.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('地址'),
        picDesc: ""
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779691120penAIAi6kRrFMWeeoe.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779691120penAIAi6kRrFMWeeoe.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: t('设置'),
        picDesc: ""
    }
];

const defaultInnerPadding = 15;
const defaultModule = {
    moduleTitle: t('账号'),
    accountList: defaultAccountList.map((item) => ({ ...item })),
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 0,
        innerPadding: defaultInnerPadding,
        boxPadding: 0,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
};
mergeDefaultModule(module.value, defaultModule);

const frame = computed(() => module.value.frame ?? {});
const frameFormat = computed(() => formatFrame(frame.value));
const accountList = computed(() => (module.value.accountList ?? []).filter((item) => item.isShow !== false));

const wrapStyle = computed(() => ({
    backgroundColor: frame.value.backgroundColor || "#ffffff",
    borderRadius: `${frame.value.boxRadius ?? 0}px`
}));

const containerStyle = computed(() => ({
    padding: `${frame.value.innerPadding ?? defaultInnerPadding}px`
}));
</script>
<style lang="less" scoped>
.account-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.account-container {
    box-sizing: border-box;
}
.account-header {
    margin-bottom: 10px;
}
.account-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
}
.account-list {
    display: flex;
    flex-direction: column;
}
.account-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 0;
    border-bottom: 1px solid #f5f5f5;
    &:last-child {
        border-bottom: none;
    }
}
.account-item-left {
    display: flex;
    align-items: center;
    gap: 12px;
}
.account-item-icon {
    width: 20px;
    height: 20px;
    flex-shrink: 0;
    img {
        width: 100%;
        height: 100%;
        object-fit: contain;
    }
}
.account-item-title {
    font-size: 15px;
    color: #1a1a1a;
}
.account-item-right {
    display: flex;
    align-items: center;
    gap: 4px;
    .icon-a-gengduo20 {
        font-size: 10px;
    }
}
.account-item-subtitle {
    font-size: 14px;
    color: #1a1a1a;
}
.account-item-arrow {
    font-size: 12px;
    color: #888b94;
}
</style>
