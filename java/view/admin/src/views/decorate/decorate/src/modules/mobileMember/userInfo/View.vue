<template>
    <div class="module-ad-con module-member-user-info" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content">
            <div class="user-info-wrap" :style="wrapStyle">
                <div class="user-info-container" :style="containerStyle">
                    <div class="user-info-header">
                        <div class="user-info-profile">
                            <div class="user-info-avatar">
                                <img
                                    src="https://oss.tigshop.com/img/gallery/202605/1779691335wtQOROIkUPPNOzuply.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200"
                                    alt="头像"
                                />
                            </div>
                            <div class="user-info-text">
                                <div class="user-info-name">{{ $t('演示账号') }}</div>
                                <div class="user-info-level">
                                    <img
                                        class="user-info-level-icon"
                                        src="https://oss.tigshop.com/img/gallery/202605/17796914549H8jHXMHCnnkf8uOXg.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200"
                                        alt=""
                                    />
                                    <span class="user-info-level-text">{{ $t('V6会员') }}</span>
                                </div>
                            </div>
                        </div>
                        <div class="user-info-shortcuts">
                            <div class="shortcut-item" v-if="module.signIn?.show !== false">
                                <img :src="module.signIn?.icon?.picUrl || defaultSignInIcon" alt="" class="shortcut-icon" />
                                <span class="shortcut-text">{{ module.signIn?.title || $t('签到有礼') }}</span>
                            </div>
                            <div class="shortcut-item shortcut-settings" v-if="module.settings?.show !== false">
                                <img :src="module.settings?.icon?.picUrl || defaultSettingsIcon" alt="" class="shortcut-icon" />
                            </div>
                        </div>
                    </div>
                    <div class="user-info-stats">
                        <div class="stat-item">
                            <span class="stat-label">{{ $t('商品收藏') }}</span>
                            <span class="stat-value">13</span>
                        </div>
                        <div class="stat-divider"></div>
                        <div class="stat-item">
                            <span class="stat-label">{{ $t('店铺收藏') }}</span>
                            <span class="stat-value">0</span>
                        </div>
                        <div class="stat-divider"></div>
                        <div class="stat-item">
                            <span class="stat-label">{{ $t('待评价') }}</span>
                            <span class="stat-value">246</span>
                        </div>
                        <div class="stat-divider"></div>
                        <div class="stat-item">
                            <span class="stat-label">{{ $t('浏览记录') }}</span>
                            <span class="stat-value">10</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, Ref } from "vue";
import { ModuleType, ModuleIcoPicType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface UserInfoShortcut {
    show?: boolean;
    icon?: ModuleIcoPicType;
    title?: string;
}
interface UserInfoGradient {
    colorA?: string;
    colorB?: string;
}
type ModuleUserInfoType = ModuleType & {
    signIn?: UserInfoShortcut;
    settings?: UserInfoShortcut;
    gradient?: UserInfoGradient;
    showSignIn?: boolean;
    showSettings?: boolean;
};

const module = defineModel<ModuleUserInfoType>("module") as Ref<ModuleUserInfoType>;

const defaultSignInIcon = "https://oss.tigshop.com/img/gallery/202605/1779691601SIdmgIcby2Mii610eQ.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200";
const defaultSettingsIcon = "https://oss.tigshop.com/img/gallery/202605/1779679345kBAM5aU9K9EuPRQFfo.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200";
const defaultGradient = { colorA: "#fcf6fc", colorB: "#f3f3fe" };

const mod = module.value as any;
if (mod.signIn === undefined && mod.showSignIn !== undefined) {
    mod.signIn = { show: !!mod.showSignIn };
}
if (mod.settings === undefined && mod.showSettings !== undefined) {
    mod.settings = { show: !!mod.showSettings };
}
const defaultModule = {
    signIn: { show: true, icon: { picUrl: defaultSignInIcon, picThumb: defaultSignInIcon }, title: t('签到有礼') },
    settings: { show: true, icon: { picUrl: defaultSettingsIcon, picThumb: defaultSettingsIcon }, title: t('设置') },
    gradient: { ...defaultGradient },
    frame: { ...defaultFrame, backgroundColor: "", boxPaddingTop: 0, boxPaddingBottom: 0, boxPadding: 0, innerPadding: 16 }
};
mergeDefaultModule(module.value, defaultModule);

const frame = computed(() => module.value.frame ?? {});
const gradient = computed(() => module.value.gradient ?? defaultGradient);
const frameFormat = computed(() => formatFrame(frame.value));

const wrapStyle = computed(() => {
    const colorA = gradient.value.colorA || defaultGradient.colorA;
    const colorB = gradient.value.colorB || defaultGradient.colorB;
    return {
        background: `linear-gradient(90deg, ${colorA}, ${colorB})`,
        borderRadius: `${frame.value.boxRadius ?? 0}px`
    };
});

const containerStyle = computed(() => {
    const ip = frame.value.innerPadding;
    return {
        padding: `${ip ?? 16}px`
    };
});
</script>
<style lang="less" scoped>
.user-info-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.user-info-container {
    box-sizing: border-box;
}
.user-info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
}
.user-info-profile {
    display: flex;
    align-items: center;
}
.user-info-avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    overflow: hidden;
    margin-right: 16px;
    flex-shrink: 0;
    background-color: #fff;
    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}
.user-info-text {
    display: flex;
    flex-direction: column;
    justify-content: center;
}
.user-info-name {
    font-size: 16px;
    color: #1a1a1a;
    margin-bottom: 20px;
    font-weight: 500;
}
.user-info-level {
    display: flex;
    align-items: center;
    gap: 4px;
}
.user-info-level-icon {
    width: 16px;
}
.user-info-level-text {
    font-size: 12px;
}
.user-info-shortcuts {
    display: flex;
    align-items: center;
    gap: 20px;
    align-self: flex-start;
    margin-top: 4px;
}
.shortcut-item {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    color: #333;
    font-size: 12px;
    font-weight: 500;
}
.shortcut-icon {
    width: 16px;
}
.shortcut-settings {
    padding: 4px;
    margin-right: -4px;
    color: #666;
}
.user-info-stats {
    display: flex;
    align-items: center;
    justify-content: space-between;
}
.stat-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    font-size: 12px;
    color: #999;
    &:first-child {
        justify-content: flex-start;
    }
    &:last-child {
        justify-content: flex-end;
    }
}
.stat-label {
    color: #333;
}
.stat-value {
    font-weight: 500;
    color: #333;
}
.stat-divider {
    width: 1px;
    height: 14px;
    background: #e2e8f0;
    margin: 0 4px;
}
</style>
