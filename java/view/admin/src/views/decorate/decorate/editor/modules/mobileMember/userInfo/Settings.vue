<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>用户信息</h3>
            <div class="dec-edit-desc">展示用户头像、昵称、会员等级和统计数据，数据为示例展示</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">签到入口</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-switch v-model="module.signIn.show" />
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">自定义图标</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="module.signIn.icon" :default-value="defaultSignInIcon"></PicSelect>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">入口名称</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-input v-model="module.signIn.title" placeholder="请输入入口名称" style="width: 240px" />
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">设置入口</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-switch v-model="module.settings.show" />
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">自定义图标</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="module.settings.icon" :default-value="defaultSettingsIcon"></PicSelect>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">背景渐变色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group" need-refresh="true">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="resetGradient">重置</a>
                            <div class="flex flex-align-center">
                                <div><SelectColor v-model:color="module.gradient.colorA"></SelectColor></div>
                                <div style="margin: 0 10px">-</div>
                                <div><SelectColor v-model:color="module.gradient.colorB"></SelectColor></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">内边距</div>
                    <div class="value">
                        <a class="dec-color-reset" @click="resetInnerPadding">重置</a>
                    </div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.frame.innerPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <CommonFrameEdit v-model="module.frame" :hide-bg="true" :hide-padding="true"></CommonFrameEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ref, computed } from "vue";
import Tabs from "@/components/tabs/Index.vue";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { PicSelect } from "@/components/decorate";
import { SelectColor } from "@/components/select";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { UserInfoModule, UserInfoGradient } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as UserInfoModule);

const tabs = ref<any[]>([
    { label: "内容设置", name: "content" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");

const defaultSignInIcon = {
    picUrl: "https://oss.tigshop.com/img/gallery/202605/1779691601SIdmgIcby2Mii610eQ.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
    picThumb: "https://oss.tigshop.com/img/gallery/202605/1779691601SIdmgIcby2Mii610eQ.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200"
};
const defaultSettingsIcon = {
    picUrl: "https://oss.tigshop.com/img/gallery/202605/1779679345kBAM5aU9K9EuPRQFfo.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
    picThumb: "https://oss.tigshop.com/img/gallery/202605/1779679345kBAM5aU9K9EuPRQFfo.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200"
};
const defaultGradient: UserInfoGradient = { colorA: "#fcf6fc", colorB: "#f3f3fe" };

const mod = module.value as any;
if (mod.signIn === undefined && mod.showSignIn !== undefined) {
    mod.signIn = { show: !!mod.showSignIn };
}
if (mod.settings === undefined && mod.showSettings !== undefined) {
    mod.settings = { show: !!mod.showSettings };
}
mergeDefaultModule(module.value, {
    signIn: { show: true, icon: { ...defaultSignInIcon }, title: "签到有礼" },
    settings: { show: true, icon: { ...defaultSettingsIcon }, title: "设置" },
    gradient: { ...defaultGradient },
    frame: { ...defaultFrame, backgroundColor: "", boxPaddingTop: 0, boxPaddingBottom: 0, boxPadding: 0, innerPadding: 16 }
});
if (!mod.signIn.icon || !mod.signIn.icon.picUrl) mod.signIn.icon = { ...defaultSignInIcon };
if (mod.signIn.title === undefined || mod.signIn.title === null) mod.signIn.title = "签到有礼";
if (!mod.settings.icon || !mod.settings.icon.picUrl) mod.settings.icon = { ...defaultSettingsIcon };
if (!mod.gradient || typeof mod.gradient !== "object") mod.gradient = { ...defaultGradient };
if (!mod.gradient.colorA) mod.gradient.colorA = defaultGradient.colorA;
if (!mod.gradient.colorB) mod.gradient.colorB = defaultGradient.colorB;

const resetGradient = () => {
    module.value.gradient = { ...defaultGradient };
};

const defaultInnerPadding = 16;
const resetInnerPadding = () => {
    if (module.value.frame) {
        module.value.frame.innerPadding = defaultInnerPadding;
    }
};
</script>
