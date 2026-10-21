<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('用户信息3') }}</h3>
            <div class="dec-edit-desc">{{ $t('展示用户头像、昵称、会员等级和统计数据，数据为示例展示（三种布局可切换）') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>

        <!-- ==================== 内容设置 ==================== -->
        <div v-if="activeName === 'content'">
            <!-- 布局风格 -->
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('布局风格') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.layoutStyle">
                        <el-radio-button :value="1">{{ $t('样式一') }}</el-radio-button>
                        <el-radio-button :value="2">{{ $t('样式二') }}</el-radio-button>
                        <el-radio-button :value="3">{{ $t('样式三') }}</el-radio-button>
                        <el-radio-button :value="4">{{ $t('样式四') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-divider-line"></div>

            <!-- 文字颜色 -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title"><div class="label">{{ $t('昵称颜色') }}</div></div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="resetTextColor('nameColor')">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.textColors.nameColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title"><div class="label">{{ $t('副标题颜色') }}</div></div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="resetTextColor('subColor')">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.textColors.subColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <template v-if="module.layoutStyle === 1 || module.layoutStyle === 2">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title"><div class="label">{{ $t('统计数值颜色') }}</div></div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="resetTextColor('statValueColor')">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.textColors.statValueColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title"><div class="label">{{ $t('统计标签颜色') }}</div></div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="resetTextColor('statLabelColor')">{{ $t('重置') }}</a>
                                <SelectColor v-model:color="module.textColors.statLabelColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
            </template>

            <div class="dec-divider-line"></div>

            <!-- ===== Style 1 专属 ===== -->
            <template v-if="module.layoutStyle === 1">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title"><div class="label">{{ $t('会员等级条') }}</div></div>
                    <div class="dec-edit-group-con">
                        <el-switch v-model="module.memberBar.show" />
                    </div>
                </div>
                <template v-if="module.memberBar.show">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('等级条背景') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group">
                                <div class="dec-color-button">
                                    <a class="dec-color-reset" @click="resetMemberBarBg">{{ $t('重置') }}</a>
                                    <SelectColor v-model:color="module.memberBar.backgroundColor"></SelectColor>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title"><div class="label">{{ $t('等级条上圆角') }}</div></div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    v-model="module.memberBar.borderRadius"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    :max="30"
                                />
                            </div>
                        </div>
                    </div>
                </template>
            </template>

            <!-- ===== Style 2 专属 ===== -->
            <template v-if="module.layoutStyle === 2">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title"><div class="label">{{ $t('设置入口') }}</div></div>
                    <div class="dec-edit-group-con">
                        <el-switch v-model="module.settings.show" />
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.settings.show">
                    <div class="dec-edit-group-title"><div class="label">{{ $t('设置图标') }}</div></div>
                    <div class="dec-edit-group-con">
                        <PicSelect v-model:photo="module.settings.icon" :default-value="defaultSettingsIcon"></PicSelect>
                    </div>
                </div>
                <div class="dec-divider-line"></div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title"><div class="label">{{ $t('消息通知') }}</div></div>
                    <div class="dec-edit-group-con">
                        <el-switch v-model="module.notification.show" />
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.notification.show">
                    <div class="dec-edit-group-title"><div class="label">{{ $t('通知图标') }}</div></div>
                    <div class="dec-edit-group-con">
                        <PicSelect v-model:photo="module.notification.icon" :default-value="defaultNotificationIcon"></PicSelect>
                    </div>
                </div>
            </template>

            <!-- ===== Style 3 专属 ===== -->
            <template v-if="module.layoutStyle === 3">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title"><div class="label">{{ $t('成长值条') }}</div></div>
                    <div class="dec-edit-group-con">
                        <el-switch v-model="module.memberBar.show" />
                    </div>
                </div>
                <template v-if="module.memberBar.show">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('成长值条渐变色') }}</div>
                            <div class="value">
                                <a class="dec-color-reset" @click="resetMemberBarBg">{{ $t('重置') }}</a>
                            </div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group" style="display: flex; align-items: center; gap: 10px">
                                <SelectColor v-model:color="module.memberBar.backgroundColor"></SelectColor>
                                <span style="color: #999; font-size: 12px">→</span>
                                <SelectColor v-model:color="module.memberBar.backgroundColorB"></SelectColor>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title"><div class="label">{{ $t('成长值条上圆角') }}</div></div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    v-model="module.memberBar.borderRadius"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    :max="30"
                                />
                            </div>
                        </div>
                    </div>
                </template>
            </template>

            <!-- ===== Style 4 专属 ===== -->
            <template v-if="module.layoutStyle === 4">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title"><div class="label">{{ $t('头像编辑图标') }}</div></div>
                    <div class="dec-edit-group-con">
                        <el-switch v-model="module.showEditIcon" />
                    </div>
                </div>
                <div class="dec-divider-line"></div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title"><div class="label">{{ $t('成长值卡') }}</div></div>
                    <div class="dec-edit-group-con">
                        <el-switch v-model="module.memberBar.show" />
                    </div>
                </div>
                <template v-if="module.memberBar.show">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('成长值卡渐变色') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-color-group" style="display: flex; align-items: center; gap: 10px">
                                <SelectColor v-model:color="module.memberBar.backgroundColor"></SelectColor>
                                <span style="color: #999; font-size: 12px">→</span>
                                <SelectColor v-model:color="module.memberBar.backgroundColorB"></SelectColor>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title"><div class="label">{{ $t('成长值卡圆角') }}</div></div>
                        <div class="dec-edit-group-con">
                            <div class="dec-range-group">
                                <el-slider
                                    v-model="module.memberBar.borderRadius"
                                    show-input
                                    :show-input-controls="false"
                                    size="small"
                                    input-size="default"
                                    :max="30"
                                />
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title"><div class="label">{{ $t('按钮文案') }}</div></div>
                        <div class="dec-edit-group-con">
                            <el-input v-model="module.memberBar.buttonText" :placeholder="$t('查看')" style="width: 160px" />
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title"><div class="label">{{ $t('成长值卡背景图') }}</div></div>
                        <div class="dec-edit-group-con">
                            <PicSelect v-model:photo="module.memberBar.waveImage" :default-value="defaultWaveImage"></PicSelect>
                        </div>
                    </div>
                </template>
            </template>

            <!-- ===== Style 1/2 共享：统计数据 ===== -->
            <template v-if="module.layoutStyle === 1 || module.layoutStyle === 2">
                <div class="dec-divider-line"></div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title"><div class="label">{{ $t('统计数据') }}</div></div>
                    <div class="dec-edit-group-con">
                        <el-switch v-model="module.stats.show" />
                    </div>
                </div>
                <template v-if="module.stats.show">
                    <div class="dec-edit-group" v-for="(item, index) in module.stats.items" :key="item.key">
                        <div class="dec-edit-group-title">
                            <div class="label">
                                <el-checkbox v-model="item.show">{{ item.label }}</el-checkbox>
                            </div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-input v-model="item.label" :placeholder="$t('统计项名称')" style="width: 160px" />
                        </div>
                    </div>
                </template>
            </template>
        </div>

        <!-- ==================== 扩展设置 ==================== -->
        <div v-if="activeName === 'extend'">
            <!-- 背景渐变色（所有样式共用） -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ module.layoutStyle === 4 ? $t('顶部背景渐变色') : $t('背景渐变色') }}</div>
                    <div class="value">
                        <a class="dec-color-reset" @click="resetGradient">{{ $t('重置') }}</a>
                    </div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
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
                    <div class="label">{{ module.layoutStyle === 4 ? $t('顶部背景渐变方向') : $t('背景渐变方向') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.gradient.gradientType">
                        <el-tooltip effect="light" placement="bottom" :content="$t('上下')" :show-after="100">
                            <el-radio-button :value="'upDown'">{{ $t('上下') }}</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('左右')" :show-after="100">
                            <el-radio-button :value="'leftRight'">{{ $t('左右') }}</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('斜向')" :show-after="100">
                            <el-radio-button :value="'diagonal'">{{ $t('斜向') }}</el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>

            <!-- Style 1: 背景图 + 弧底 -->
            <template v-if="module.layoutStyle === 1">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('背景图') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-switch v-model="module.backgroundImageShow" />
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.backgroundImageShow">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('选择背景图') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <PicSelect v-model:photo="module.backgroundImage" :default-value="defaultStyle1BgImage"></PicSelect>
                    </div>
                </div>
                <div class="dec-divider-line"></div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title"><div class="label">{{ $t('弧底效果') }}</div></div>
                    <div class="dec-edit-group-con">
                        <el-switch v-model="module.bottomArc.show" />
                    </div>
                </div>
            </template>

            <div class="dec-divider-line"></div>

            <!-- 内边距 -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('左右内边距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.frame.innerPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <CommonFrameEdit
                v-model="module.frame"
                :hide-bg="true"
                :hide-padding="true"
                :hide-box-padding="module.layoutStyle === 2 || module.layoutStyle === 3 || module.layoutStyle === 4"
                :hide-box-radius="module.layoutStyle === 2 || module.layoutStyle === 3 || module.layoutStyle === 4"
            ></CommonFrameEdit>
        </div>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
import { ref, watch } from "vue";
import Tabs from "@/components/tabs/Index.vue";
import CommonFrameEdit from "../../src/commonFrame/Edit.vue";
import { PicSelect } from "@/components/decorate";
import { SelectColor } from "@/components/select";
import { ModuleType, ModuleIcoPicType, ModuleFrameType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface UserInfoShortcut {
    show: boolean;
    icon: ModuleIcoPicType;
    title?: string;
}
interface UserInfoGradient {
    colorA: string;
    colorB: string;
    gradientType?: string;
}
interface UserInfoStatsItem {
    key: string;
    label: string;
    show: boolean;
}
interface UserInfoMemberBar {
    show: boolean;
    backgroundColor: string;
    backgroundColorB?: string;
    borderRadius: number;
    buttonText?: string;
    waveImage?: ModuleIcoPicType;
}
interface UserInfoBottomArc {
    show: boolean;
    height: number;
}
interface UserInfoTextColors {
    nameColor: string;
    subColor: string;
    statValueColor: string;
    statLabelColor: string;
}
type ModuleUserInfoType = Omit<ModuleType, "frame"> & {
    layoutStyle: number;
    signIn: UserInfoShortcut;
    settings: UserInfoShortcut;
    gradient: UserInfoGradient;
    bgColor?: string;
    backgroundImage: ModuleIcoPicType;
    backgroundImageShow?: boolean;
    bottomArc: UserInfoBottomArc;
    memberBar: UserInfoMemberBar;
    notification: UserInfoShortcut;
    stats: { show: boolean; items: UserInfoStatsItem[] };
    textColors: UserInfoTextColors;
    frame: ModuleFrameType;
    showEditIcon?: boolean;
    showSignIn?: boolean;
    showSettings?: boolean;
};

const module = defineModel<ModuleUserInfoType>("module", { default: () => ({}) });

const tabs = ref<any[]>([
    { label: t('内容设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");

const defaultSignInIcon: ModuleIcoPicType = {
    picUrl: "https://oss.tigshop.com/img/gallery/202605/1779691601SIdmgIcby2Mii610eQ.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
    picThumb: "https://oss.tigshop.com/img/gallery/202605/1779691601SIdmgIcby2Mii610eQ.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200"
};
const defaultSettingsIcon: ModuleIcoPicType = {
    picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779872068yYRoxuCDxWgGcOti6b.png",
    picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779872068yYRoxuCDxWgGcOti6b.png"
};
const defaultNotificationIcon: ModuleIcoPicType = {
    picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779958812O0gl221tWQVIEt7OXD.png",
    picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779958812O0gl221tWQVIEt7OXD.png"
};
const styleGradientDefaults: Record<number, UserInfoGradient> = {
    1: { colorA: "#57D45C", colorB: "#34C759", gradientType: "leftRight" },
    2: { colorA: "#E0FCED", colorB: "#FDF1F3", gradientType: "leftRight" },
    3: { colorA: "#F1EFE9", colorB: "#E7F0E6", gradientType: "leftRight" },
    4: { colorA: "#FBD9E1", colorB: "#FDEDEF", gradientType: "leftRight" }
};
const getGradientDefaults = (style: number): UserInfoGradient => ({ ...(styleGradientDefaults[style] || styleGradientDefaults[1]) });
const defaultMemberBarBg = "";

const defaultGrowthCardBg = "https://oss.tigshop.com/official/img/pc/202606/17808830195Hk80LmOeUpDCCCEKW.png";
const defaultWaveImage: ModuleIcoPicType = { picUrl: defaultGrowthCardBg, picThumb: defaultGrowthCardBg };

const styleTextColorDefaults: Record<number, UserInfoTextColors> = {
    1: { nameColor: "#ffffff", subColor: "#ffffff", statValueColor: "#ffffff", statLabelColor: "#ffffff" },
    2: { nameColor: "#333333", subColor: "#666666", statValueColor: "#333333", statLabelColor: "#666666" },
    3: { nameColor: "#333333", subColor: "#666666", statValueColor: "#333333", statLabelColor: "#666666" },
    4: { nameColor: "#333333", subColor: "#666666", statValueColor: "#333333", statLabelColor: "#666666" }
};
const getTextColorDefaults = (style: number): UserInfoTextColors => ({ ...(styleTextColorDefaults[style] || styleTextColorDefaults[1]) });

const defaultStatsItems: UserInfoStatsItem[] = [
    { key: "collect", label: t('商品收藏'), show: true },
    { key: "follow", label: t('门店关注'), show: true },
    { key: "history", label: t('浏览记录'), show: true }
];

// Old data migration
const mod = module.value as any;
if (mod.signIn === undefined && mod.showSignIn !== undefined) {
    mod.signIn = { show: !!mod.showSignIn };
}
if (mod.settings === undefined && mod.showSettings !== undefined) {
    mod.settings = { show: !!mod.showSettings };
}
if (mod.bgColor !== undefined) {
    if (!mod.gradient || typeof mod.gradient !== "object") mod.gradient = {};
    mod.gradient.colorA = mod.bgColor;
    mod.gradient.colorB = mod.bgColorB || mod.bgColor;
    mod.gradient.gradientType = mod.bgGradientType || mod.gradient.gradientType || "leftRight";
    delete mod.bgColor;
    delete mod.bgColorB;
    delete mod.bgGradientType;
}

const defaultStyle1BgImage: ModuleIcoPicType = {
    picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779860190FsB8OM4MrGwgOCvKsA.png",
    picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779860190FsB8OM4MrGwgOCvKsA.png"
};

mergeDefaultModule(module.value, {
    layoutStyle: 1,
    showEditIcon: true,
    signIn: { show: true, icon: { ...defaultSignInIcon }, title: t('签到有礼') },
    settings: { show: true, icon: { ...defaultSettingsIcon } },
    gradient: getGradientDefaults(mod.layoutStyle ?? 1),
    backgroundImage: { ...defaultStyle1BgImage },
    backgroundImageShow: true,
    bottomArc: { show: true, height: 24 },
    memberBar: {
        show: true,
        backgroundColor: defaultMemberBarBg,
        backgroundColorB: "",
        borderRadius: 10,
        buttonText: t('查看'),
        waveImage: { ...defaultWaveImage }
    },
    notification: { show: true, icon: { ...defaultNotificationIcon } },
    stats: { show: true, items: defaultStatsItems.map((i) => ({ ...i })) },
    textColors: getTextColorDefaults(1),
    frame: { ...defaultFrame, backgroundColor: "", boxPaddingTop: 0, boxPaddingBottom: 0, boxPadding: 0, innerPadding: 16 }
});

// Ensure nested objects exist after merge
if (!mod.signIn.icon || !mod.signIn.icon.picUrl) mod.signIn.icon = { ...defaultSignInIcon };
if (mod.signIn.title === undefined || mod.signIn.title === null) mod.signIn.title = t('签到有礼');
if (!mod.settings.icon || !mod.settings.icon.picUrl) mod.settings.icon = { ...defaultSettingsIcon };
if (!mod.notification || typeof mod.notification !== "object") mod.notification = { show: true, icon: { ...defaultNotificationIcon } };
if (!mod.notification.icon || !mod.notification.icon.picUrl) mod.notification.icon = { ...defaultNotificationIcon };
if (mod.backgroundImageShow === undefined || mod.backgroundImageShow === null) mod.backgroundImageShow = true;
if (!mod.gradient || typeof mod.gradient !== "object") mod.gradient = getGradientDefaults(mod.layoutStyle ?? 1);
if (!mod.gradient.colorA) mod.gradient.colorA = getGradientDefaults(mod.layoutStyle ?? 1).colorA;
if (!mod.gradient.colorB) mod.gradient.colorB = getGradientDefaults(mod.layoutStyle ?? 1).colorB;
if (mod.gradient.gradientType === undefined || mod.gradient.gradientType === null) mod.gradient.gradientType = "leftRight";
if (!mod.bottomArc || typeof mod.bottomArc !== "object") mod.bottomArc = { show: true, height: 20 };
if (!mod.memberBar || typeof mod.memberBar !== "object") mod.memberBar = { show: true, backgroundColor: "", backgroundColorB: "", borderRadius: 25 };
if (!mod.memberBar.backgroundColor) {
    if (mod.layoutStyle === 4) mod.memberBar.backgroundColor = "#FF9EB5";
    else mod.memberBar.backgroundColor = mod.layoutStyle === 3 ? "#292929" : "#333";
}
if (!mod.memberBar.backgroundColorB) {
    mod.memberBar.backgroundColorB = mod.layoutStyle === 4 ? "#FF6E97" : "#525252";
}
if (mod.memberBar.buttonText === undefined || mod.memberBar.buttonText === null) mod.memberBar.buttonText = t('查看');
if (!mod.memberBar.waveImage || typeof mod.memberBar.waveImage !== "object") mod.memberBar.waveImage = { ...defaultWaveImage };
if (mod.showEditIcon === undefined) mod.showEditIcon = true;
if (!mod.stats || typeof mod.stats !== "object") mod.stats = { show: true, items: defaultStatsItems.map((i) => ({ ...i })) };
if (!mod.stats.items || !Array.isArray(mod.stats.items) || mod.stats.items.length === 0) {
    mod.stats.items = defaultStatsItems.map((i) => ({ ...i }));
}
if (!mod.textColors || typeof mod.textColors !== "object") {
    mod.textColors = getTextColorDefaults(mod.layoutStyle ?? 1);
}

const resetGradient = () => {
    module.value.gradient = getGradientDefaults(module.value.layoutStyle);
};

const resetMemberBarBg = () => {
    if (module.value.layoutStyle === 3) {
        module.value.memberBar.backgroundColor = "#292929";
        module.value.memberBar.backgroundColorB = "#525252";
    } else {
        module.value.memberBar.backgroundColor = "#333";
    }
};

const resetTextColor = (key: keyof UserInfoTextColors) => {
    const defaults = getTextColorDefaults(module.value.layoutStyle);
    module.value.textColors[key] = defaults[key];
};

watch(
    () => module.value.layoutStyle,
    (newStyle) => {
        module.value.textColors = getTextColorDefaults(newStyle);
        module.value.gradient = getGradientDefaults(newStyle);
        if (newStyle === 4) {
            module.value.memberBar.backgroundColor = "#FF9EB5";
            module.value.memberBar.backgroundColorB = "#FF6E97";
            module.value.memberBar.borderRadius = 10;
        }
    }
);
</script>
